/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.authentication;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.microsoft.rest.credentials.ServiceClientCredentials;
import com.microsoft.azure.keyvault.messagesecurity.HttpMessageSecurity;

import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;

import org.apache.commons.lang3.tuple.Pair;

/**
 * An implementation of {@link ServiceClientCredentials} that supports automatic bearer token refresh.
 *
 */
public abstract class KeyVaultCredentials implements ServiceClientCredentials {

    private static final String WWW_AUTHENTICATE = "WWW-Authenticate";
    private static final String BEARER_TOKEP_REFIX = "Bearer ";

    private final ChallengeCache cache = new ChallengeCache();

    @Override
    public void applyCredentialsFilter(OkHttpClient.Builder clientBuilder) {

        clientBuilder.addInterceptor(new Interceptor() {

            @Override
            public Response intercept(Chain chain) throws IOException {

                Request originalRequest = chain.request();
                HttpUrl url = chain.request().url();

                Map<String, String> challengeMap = cache.getCachedChallenge(url);
                Response response;
                Pair<Request, HttpMessageSecurity> authenticatedRequestPair;

                if (challengeMap != null) {
                    // challenge is cached, so there is no need to send an empty auth request.
                    authenticatedRequestPair = buildAuthenticatedRequest(originalRequest, challengeMap);
                } else {
                    // challenge is new for the URL and is not cached,
                    // so the request is sent out to get the challenges in
                    // response
                    response = chain.proceed(buildEmptyRequest(originalRequest));

                    if (response.code() == 200){
                        return response;
                    } else if (response.code() != 401){
                        throw new IOException("Unexpected unauthorized response.");
                    }
                    authenticatedRequestPair = buildAuthenticatedRequest(originalRequest, response);
                }

                response = chain.proceed(authenticatedRequestPair.getLeft());

                // @TODO: cleanup this. There are 2 failing unit tests because mock server
                // responds 401 and only after this 200.
                if (response.code() == 401){
                    authenticatedRequestPair = buildAuthenticatedRequest(originalRequest, response);
                    response = chain.proceed(authenticatedRequestPair.getLeft());
                }

                if (response.code() == 200){
                    return authenticatedRequestPair.getRight().unprotectResponse(response);
                }
                else{
                    return response;
                }
            }
        });
    }

    /**
     * Builds request with authenticated header. Protects request body if supported.
     *
     * @param originalRequest
     *            unprotected request without auth token.
     * @param challengeMap
     *            the challenge map.
     * @return Pair of protected request and HttpMessageSecurity used for encryption.
     */
    private Pair<Request, HttpMessageSecurity> buildAuthenticatedRequest(Request originalRequest, Map<String, String> challengeMap) throws IOException{
        AuthenticationResult authResult = getAuthenticationCredentials(challengeMap);

        if (authResult == null) {
            return null;
        }

        HttpMessageSecurity httpMessageSecurity =
            new HttpMessageSecurity(
                authResult.getAuthToken(),
                authResult.getPopKey(),
                challengeMap.get("x-ms-message-encryption-key"),
                challengeMap.get("x-ms-message-signing-key"));

        Request request = httpMessageSecurity.protectRequest(originalRequest);
        return Pair.of(request, httpMessageSecurity);
    }

    /**
     * Builds request with authenticated header. Protects request body if supported.
     *
     * @param originalRequest
     *            unprotected request without auth token.
     * @param response
     *            response with unauthorized return code.
     * @return Pair of protected request and HttpMessageSecurity used for encryption.
     */
    private Pair<Request, HttpMessageSecurity> buildAuthenticatedRequest(Request originalRequest, Response response) throws IOException{
        String authenticateHeader = response.header(WWW_AUTHENTICATE);

        Map<String, String> challengeMap = extractChallenge(authenticateHeader, BEARER_TOKEP_REFIX);

        challengeMap.put("x-ms-message-encryption-key", response.header("x-ms-message-encryption-key"));
        challengeMap.put("x-ms-message-signing-key", response.header("x-ms-message-signing-key"));

        // Cache the challenge
        cache.addCachedChallenge(originalRequest.url(), challengeMap);

        return buildAuthenticatedRequest(originalRequest, challengeMap);
    }

    /**
     * Removes request body used for EKV authorization.
     *
     * @param request
     *            unprotected request without auth token.
     * @return request with removed body.
     */
    private Request buildEmptyRequest(Request request){
        RequestBody body = RequestBody.create(MediaType.parse("application/jose+json"), "{}");
        if (request.method().equalsIgnoreCase("post")){
            return request.newBuilder().post(body).build();
        }
        else if (request.method().equalsIgnoreCase("put")){
            return request.newBuilder().put(body).build();
        }
        else if (request.method().equalsIgnoreCase("get")){
            return request;
        }
        return null;
    }

    /**
     * Extracts the authentication challenges from the challenge map and calls
     * the authentication callback to get the bearer token and return it.
     *
     * @param challengeMap
     *            the challenge map.
     * @return AuthenticationResult with bearer token and PoP key.
     */
    private AuthenticationResult getAuthenticationCredentials(Map<String, String> challengeMap) {

        String authorization = challengeMap.get("authorization");
        if (authorization == null) {
            authorization = challengeMap.get("authorization_uri");
        }

        String resource = challengeMap.get("resource");
        String scope = challengeMap.get("scope");
        String schema = "true".equals(challengeMap.get("supportspop")) ? "pop" : "bearer";
        return doAuthenticate(authorization, resource, scope, schema);
    }

    /**
     * Extracts the challenge off the authentication header.
     *
     * @param authenticateHeader
     *            the authentication header containing all the challenges.
     * @param authChallengePrefix
     *            the authentication challenge name.
     * @return a challenge map.
     */
    private static Map<String, String> extractChallenge(String authenticateHeader, String authChallengePrefix) {
        if (!isValidChallenge(authenticateHeader, authChallengePrefix)) {
            return null;
        }

        authenticateHeader = authenticateHeader.toLowerCase().replace(authChallengePrefix.toLowerCase(), "");

        String[] challenges = authenticateHeader.split(", ");
        Map<String, String> challengeMap = new HashMap<String, String>();
        for (String pair : challenges) {
            String[] keyValue = pair.split("=");
            challengeMap.put(keyValue[0].replaceAll("\"", ""), keyValue[1].replaceAll("\"", ""));
        }
        return challengeMap;
    }

    /**
     * Verifies whether a challenge is bearer or not.
     *
     * @param authenticateHeader
     *            the authentication header containing all the challenges.
     * @param authChallengePrefix
     *            the authentication challenge name.
     * @return
     */
    private static boolean isValidChallenge(String authenticateHeader, String authChallengePrefix) {
        if (authenticateHeader != null && !authenticateHeader.isEmpty()
                && authenticateHeader.toLowerCase().startsWith(authChallengePrefix.toLowerCase())) {
            return true;
        }
        return false;
    }

    /**
     * Abstract method to be implemented.
     *
     * @param authorization
     *            Identifier of the authority, a URL.
     * @param resource
     *            Identifier of the target resource that is the recipient of the
     *            requested token, a URL.
     * @param scope
     *            The scope of the authentication request.
     *
     * @param schema
     *            Authentication schema. Can be 'pop' or 'bearer'.
     *
     * @return AuthenticationResult with authorization token and PoP key.
     *
     *         Answers a server challenge with a token header.
     *         <p>
     *         Implementations typically use ADAL to get a token, as performed
     *         in the sample below:
     *         </p>
     *
     *         <pre>
     * &#064;Override
     * public String doAuthenticate(String authorization, String resource, String scope) {
     *     String clientId = ...; // client GUID as shown in Azure portal.
     *     String clientKey = ...; // client key as provided by Azure portal.
     *     AuthenticationResult token = getAccessTokenFromClientCredentials(authorization, resource, clientId, clientKey);
     *     return token.getAccessToken();;
     * }
     *
     * private static AuthenticationResult getAccessTokenFromClientCredentials(String authorization, String resource, String clientId, String clientKey) {
     *     AuthenticationContext context = null;
     *     AuthenticationResult result = null;
     *     ExecutorService service = null;
     *     try {
     *         service = Executors.newFixedThreadPool(1);
     *         context = new AuthenticationContext(authorization, false, service);
     *         ClientCredential credentials = new ClientCredential(clientId, clientKey);
     *         Future&lt;AuthenticationResult&gt; future = context.acquireToken(resource, credentials, null);
     *         result = future.get();
     *     } catch (Exception e) {
     *         throw new RuntimeException(e);
     *     } finally {
     *         service.shutdown();
     *     }
     *
     *     if (result == null) {
     *         throw new RuntimeException(&quot;authentication result was null&quot;);
     *     }
     *     return result;
     * }
     *         </pre>
     *
     *         <p>
     *         <b>Note: The client key must be securely stored. It's advised to
     *         use two client applications - one for development and other for
     *         production - managed by separate parties.</b>
     *         </p>
     *
     */
    public String doAuthenticate(String authorization, String resource, String scope){
        return "";
    }

    public AuthenticationResult doAuthenticate(String authorization, String resource, String scope, String schema){
        return new AuthenticationResult(doAuthenticate(authorization, resource, scope), "");
    }
}
