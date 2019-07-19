/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 *
 * Code generated by Microsoft (R) AutoRest Code Generator.
 */

package com.microsoft.azure.management.datafactoryv2.v2018_06_01;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * SSIS access credential.
 */
public class SSISAccessCredential {
    /**
     * Domain for windows authentication.
     */
    @JsonProperty(value = "domain", required = true)
    private Object domain;

    /**
     * UseName for windows authentication.
     */
    @JsonProperty(value = "userName", required = true)
    private Object userName;

    /**
     * Password for windows authentication.
     */
    @JsonProperty(value = "password", required = true)
    private SecureString password;

    /**
     * Get domain for windows authentication.
     *
     * @return the domain value
     */
    public Object domain() {
        return this.domain;
    }

    /**
     * Set domain for windows authentication.
     *
     * @param domain the domain value to set
     * @return the SSISAccessCredential object itself.
     */
    public SSISAccessCredential withDomain(Object domain) {
        this.domain = domain;
        return this;
    }

    /**
     * Get useName for windows authentication.
     *
     * @return the userName value
     */
    public Object userName() {
        return this.userName;
    }

    /**
     * Set useName for windows authentication.
     *
     * @param userName the userName value to set
     * @return the SSISAccessCredential object itself.
     */
    public SSISAccessCredential withUserName(Object userName) {
        this.userName = userName;
        return this;
    }

    /**
     * Get password for windows authentication.
     *
     * @return the password value
     */
    public SecureString password() {
        return this.password;
    }

    /**
     * Set password for windows authentication.
     *
     * @param password the password value to set
     * @return the SSISAccessCredential object itself.
     */
    public SSISAccessCredential withPassword(SecureString password) {
        this.password = password;
        return this;
    }

}
