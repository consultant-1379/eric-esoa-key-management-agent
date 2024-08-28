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
package com.ericsson.oss.sec.presentation.services.validation;

import com.ericsson.oss.sec.api.model.StoreCredentialRequestBody;
import com.ericsson.oss.sec.presentation.controller.exception.ErrorMessages;
import com.ericsson.oss.sec.presentation.controller.exception.KmsAgentHttpException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Validates Credentials
 */
@Slf4j
@Service
public class CredentialRequestValidator {

    /**
     *
     * @param credential -  credentials to store
     */
    public void validateStoreCredentialRequest(final StoreCredentialRequestBody credential) {
        log.info("Validating data provided in request to Store Credential");
        validatePathForKmsUrl(credential.getCredentialKey());
        validatePathForKmsRestrictedChars(credential.getCredentialKey());
    }

    /**
     *
     * @param credentialReference - path to credentials
     */
    public void validateUpdateDeleteCredentialRequest(final String credentialReference) {
        log.info("Validating data provided in credential request");
        validatePathForKmsUrl(credentialReference);
        validatePathForKmsRestrictedChars(credentialReference);
    }

    /**
     *
     * @param path - path to credentials
     */
    private void validatePathForKmsRestrictedChars(final String path) {
        if (StringUtils.endsWithIgnoreCase(path, ".")) {
            final String detail = String.format("Invalid path %s provided. Cannot end with '.' char ", path);
            throw new KmsAgentHttpException(ErrorMessages.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, detail);
        }
    }

    /**
     * The path string will be used to construct the KMS request URL. This path must conform to the generic URI
     * syntax (RFC3986), otherwise the KMS server will respond with an error. In general these characters include
     * digits (0-9), letters(A-Z, a-z), and a few special characters.
     * Also to keep secret path names readable and meaningful we will not allow control chars, unsafe chars or
     * anything outside the ASCII charset.
     *
     * @param path provided by user as key for the secret location in KMS.
     */
    private void validatePathForKmsUrl(final String path) {
        try {
            new URI(path);
        } catch (final URISyntaxException exception) {
            final String detail = String.format("Invalid path %s provided. Reason: %s", path, exception.getMessage());
            log.error(detail, exception);
            throw new KmsAgentHttpException(ErrorMessages.VALIDATION_FAILED, HttpStatus.BAD_REQUEST, detail);
        }
    }
}
