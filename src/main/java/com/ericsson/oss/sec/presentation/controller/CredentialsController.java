/*******************************************************************************
 * COPYRIGHT Ericsson 2023-2024
 *
 *
 *
 * The copyright to the computer program(s) herein is the property of
 *
 * Ericsson Inc. The programs may be used and/or copied only with written
 *
 * permission from Ericsson Inc. or in accordance with the terms and
 *
 * conditions stipulated in the agreement/contract under which the
 *
 * program(s) have been supplied.
 ******************************************************************************/
package com.ericsson.oss.sec.presentation.controller;

import com.ericsson.oss.sec.api.ConfigurationCredentialsApi;
import com.ericsson.oss.sec.api.model.StoreCredential201Response;
import com.ericsson.oss.sec.api.model.StoreCredentialRequestBody;
import com.ericsson.oss.sec.api.model.UpdateCredentialRequestBody;
import com.ericsson.oss.sec.models.KmsCredentials;
import com.ericsson.oss.sec.models.KmsRequest;
import com.ericsson.oss.sec.presentation.controller.exception.ErrorMessages;
import com.ericsson.oss.sec.presentation.controller.exception.KmsAgentHttpException;
import com.ericsson.oss.sec.presentation.services.KeyManagementService;
import com.ericsson.oss.sec.presentation.services.util.TenantProvider;
import com.ericsson.oss.sec.presentation.services.validation.CredentialRequestValidator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.ericsson.bos.so.common.logging.security.SecurityLogger;

/**
 * REST Controller class implementing the KMS Agent endpoints
 */
@Slf4j
@RestController
@RequestMapping("/v1")
public class CredentialsController implements ConfigurationCredentialsApi {

    @Autowired
    private KeyManagementService keyManagementService;

    @Autowired
    private CredentialRequestValidator credentialRequestValidator;

    @Autowired
    private TenantProvider tenantProvider;

    @Override
    public ResponseEntity<StoreCredential201Response> storeCredential(final StoreCredentialRequestBody credential) {
        SecurityLogger.withFacility(()->log.info("Store Credential called with credentialKey {}", credential.getCredentialKey()));
        credentialRequestValidator.validateStoreCredentialRequest(credential);
        final String credentialKey = credential.getCredentialKey();
        // Get the logged in tenant and build the KMS url
        final String tenant = tenantProvider.getLoggedInTenant();
        final String credentialReference = keyManagementService.buildKmsReference(tenant, credentialKey);

        log.info("Checking to see if credential already stored at path {} for tenant {}", credentialKey, tenant);
        if (keyManagementService.getSecret(credentialReference).isPresent()) {
            log.error("Cannot store new credential as a credential already exists in KMS for path '{}'", credentialReference);
            final String problemDetail = String.format("Credential with key %s for tenant %s already exists in KMS", credentialKey, tenant);
            throw new KmsAgentHttpException(ErrorMessages.STORE_CREDENTIAL_REQUEST_FAILED, HttpStatus.CONFLICT, problemDetail);
        }

        final KmsRequest kmsRequest = new KmsRequest();
        final KmsCredentials kmsCredentials = new KmsCredentials(credential.getCredentialValue());
        kmsRequest.setKmsCredentials(kmsCredentials);
        log.info("Storing the client credential data in KMS");
        keyManagementService.storeSecret(credentialReference, kmsRequest);
        log.info("Successfully stored credential in KMS at credentialReference '{}'", credentialReference);

        return new ResponseEntity<>(new StoreCredential201Response()
                .credentialReference(credentialReference), HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> updateCredential(final String credentialReference,
                                                   final UpdateCredentialRequestBody credential) {
        SecurityLogger.withFacility(()->log.info("Update Credential called for credentialReference {}", credentialReference));
        final String credentialRef = keyManagementService.removeLeadingPathSeparatorIfPresent(credentialReference);
        final String tenant = tenantProvider.getLoggedInTenant();
        final String credentialRefWithTenant = keyManagementService.buildKmsReference(tenant, credentialRef);
        credentialRequestValidator.validateUpdateDeleteCredentialRequest(credentialRefWithTenant);

        log.info("Checking to see if credential exists at path {}", credentialRefWithTenant);
        if (keyManagementService.getSecret(credentialRefWithTenant).isPresent()) {
            log.info("Updating credential data in KMS");
            final KmsRequest kmsRequest = new KmsRequest();
            final KmsCredentials kmsCredentials = new KmsCredentials(credential.getCredentialValue());
            kmsRequest.setKmsCredentials(kmsCredentials);
            keyManagementService.storeSecret(credentialRefWithTenant, kmsRequest);
            log.info("Successfully updated credential from credentialReference '{}' in KMS.", credentialRefWithTenant);
        } else {
            log.error("Cannot update non-existent credential {} in KMS", credentialRefWithTenant);
            final String detailMessage =
                    String.format(ErrorMessages.CREDENTIAL_NOT_EXISTING, credentialRefWithTenant,
                            tenantProvider.getLoggedInTenant());
            throw new KmsAgentHttpException(ErrorMessages.UPDATE_CREDENTIAL_REQUEST_FAILED, HttpStatus.NOT_FOUND, detailMessage);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Override
    public ResponseEntity<Void> deleteCredential(final String credentialReference) {
        SecurityLogger.withFacility(()->log.info("Delete Credential called for credentialReference {}", credentialReference));
        final String tenant = tenantProvider.getLoggedInTenant();
        final String credentialRefWithTenant = keyManagementService.buildKmsReference(tenant, credentialReference);
        credentialRequestValidator.validateUpdateDeleteCredentialRequest(credentialRefWithTenant);

        log.info("Checking to see if credential exists at path {}", credentialRefWithTenant);
        if (keyManagementService.getSecret(credentialRefWithTenant).isPresent()) {
            log.info("Deleting credential data from KMS");
            keyManagementService.deleteSecret(credentialRefWithTenant);
            log.info("Successfully deleted credential from credentialReference '{}' in KMS.", credentialRefWithTenant);
        } else {

            log.error("Cannot delete non-existent credential {} from KMS", credentialRefWithTenant);
            final String detailMessage =
                    String.format("Credential with key %s for tenant %s does not exist in KMS", credentialRefWithTenant,
                            tenantProvider.getLoggedInTenant());
            throw new KmsAgentHttpException(ErrorMessages.DELETE_CREDENTIAL_REQUEST_FAILED, HttpStatus.NOT_FOUND, detailMessage);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
