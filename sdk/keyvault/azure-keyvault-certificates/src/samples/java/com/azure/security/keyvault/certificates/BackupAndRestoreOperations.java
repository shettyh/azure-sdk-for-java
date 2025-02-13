// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.certificates;

import com.azure.core.util.Context;
import com.azure.identity.credential.DefaultAzureCredentialBuilder;
import com.azure.security.keyvault.certificates.models.CertificatePolicy;
import com.azure.security.keyvault.certificates.models.EcKeyOptions;
import com.azure.security.keyvault.certificates.models.SubjectAlternativeNames;
import com.azure.security.keyvault.certificates.models.webkey.KeyCurveName;
import com.azure.security.keyvault.certificates.models.CertificateOperation;
import com.azure.security.keyvault.certificates.models.Certificate;
import com.azure.security.keyvault.certificates.models.DeletedCertificate;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Sample demonstrates how to backup and restore certificates in the key vault.
 */
public class BackupAndRestoreOperations {
    /**
     * Authenticates with the key vault and shows how to backup and restore certificates in the key vault.
     *
     * @param args Unused. Arguments to the program.
     * @throws IllegalArgumentException when invalid key vault endpoint is passed.
     * @throws InterruptedException when the thread is interrupted in sleep mode.
     * @throws IOException when writing backup to file is unsuccessful.
     */
    public static void main(String[] args) throws IOException, InterruptedException, IllegalArgumentException {

        // Instantiate a certificate client that will be used to call the service. Notice that the client is using default Azure
        // credentials. To make default credentials work, ensure that environment variables 'AZURE_CLIENT_ID',
        // 'AZURE_CLIENT_KEY' and 'AZURE_TENANT_ID' are set with the service principal credentials.
        CertificateClient certificateClient = new CertificateClientBuilder()
            .endpoint("https://{YOUR_VAULT_NAME}.vault.azure.net")
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildClient();

        // Let's create a self signed certificate valid for 1 year. if the certificate
        //   already exists in the key vault, then a new version of the certificate is created.
        CertificatePolicy policy = new CertificatePolicy("Self", "CN=SelfSignedJavaPkcs12")
            .subjectAlternativeNames(SubjectAlternativeNames.fromEmails(Arrays.asList("wow@gmail.com")))
            .keyOptions(new EcKeyOptions()
                .reuseKey(true)
                .curve(KeyCurveName.P_256));
        Map<String, String> tags = new HashMap<>();
        tags.put("foo", "bar");

        try {
            CertificateOperation certificateOperation = certificateClient.createCertificate("certificateName",
                policy, tags, Duration.ofMillis(60000));
            System.out.printf("Certificate operation status %s \n", certificateOperation.status());
        } catch (IllegalStateException e) {
            // Certificate wasn't created in the specified duration.
            // Log / Handle here
        }

        // Backups are good to have, if in case certificates get accidentally deleted by you.
        // For long term storage, it is ideal to write the backup to a file.
        String backupFilePath = "YOUR_BACKUP_FILE_PATH";
        byte[] certificateBackup = certificateClient.backupCertificate("certificateName");
        System.out.printf("Backed up certificate with back up blob length %d", certificateBackup.length);
        writeBackupToFile(certificateBackup, backupFilePath);

        // The certificate is no longer in use, so you delete it.
        DeletedCertificate deletedCertificate = certificateClient.deleteCertificate("certificateName");
        System.out.printf("Deleted certitifcate with name %s and recovery id %s", deletedCertificate.name(),
            deletedCertificate.recoveryId());

        //To ensure certificate is deleted on server side.
        Thread.sleep(30000);

        // If the vault is soft-delete enabled, then you need to purge the certificate as well for permanent deletion.
        certificateClient.purgeDeletedCertificateWithResponse("certificateName", new Context("key1", "value1"));

        //To ensure certificate is purged on server side.
        Thread.sleep(15000);

        // After sometime, the certificate is required again. We can use the backup value to restore it in the key vault.
        byte[] backupFromFile = Files.readAllBytes(new File(backupFilePath).toPath());
        Certificate restoredCertificate = certificateClient.restoreCertificate(backupFromFile);
        System.out.printf(" Restored certificate with name %s and id %s", restoredCertificate.name(), restoredCertificate.id());

    }

    private static void writeBackupToFile(byte[] bytes, String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
            file.createNewFile();
            OutputStream os = new FileOutputStream(file);
            os.write(bytes);
            System.out.println("Successfully wrote backup to file.");
            // Close the file
            os.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
