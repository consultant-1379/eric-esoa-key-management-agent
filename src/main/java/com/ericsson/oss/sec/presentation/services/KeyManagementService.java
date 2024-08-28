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
package com.ericsson.oss.sec.presentation.services;

import com.ericsson.oss.sec.models.KmsCredentials;
import com.ericsson.oss.sec.models.KmsRequest;
import com.ericsson.oss.sec.presentation.controller.exception.KmsAgentHttpException;
import com.ericsson.oss.sec.presentation.services.exceptions.KmsClientRetryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.vault.VaultException;
import org.springframework.vault.core.ReactiveVaultTemplate;
import org.springframework.vault.support.VaultResponse;

import java.util.Objects;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Provides access to ADP KMS (Key Management Service). This aggregator class delegates requests
 * from application level classes to the appropriate KMS service (e.g. KV Secret Engine).
 */
@Service
@Slf4j
public class KeyManagementService {

    @Value("${kms.path}")
    private String prefixPath;

    @Autowired
    private ReactiveVaultTemplate vaultTemplate;


    /**
     * This API is used to store user secrets at a defined path within the configured physical
     * storage in KMS.
     *
     * @param credentialReference the path at which to store the secret in KMS
     * @param kmsRequest the user secret to be stored in KMS
     */
    @Retryable(
            retryFor = KmsClientRetryException.class,
            maxAttemptsExpression = "${key-management-agent.retry-policy.attempts}",
            backoff = @Backoff(delayExpression = "${key-management-agent.retry-policy.backoffDelay}")
    )
    public void storeSecret(final String credentialReference, final KmsRequest kmsRequest) {
        try {
            vaultTemplate.write(prefixPath.concat(credentialReference), kmsRequest).block();
        } catch (VaultException vaultException) {
            handleVaultException(vaultException, credentialReference, "Cannot store credentials to KMS");

        } catch (Exception exception) {
            throw new KmsAgentHttpException("Cannot store credentials to KMS",
                    HttpStatus.BAD_REQUEST,
                    exception.getMessage());
        }

    }

    /**
     * This API is used to read an existing user secret stored in KMS at the provided path location.
     *
     * @param credentialReference path location where the secret is stored in KMS
     *
     * @return Optional of KmsCredentials
     */
    @Retryable(
            retryFor = KmsClientRetryException.class,
            maxAttemptsExpression = "${key-management-agent.retry-policy.attempts}",
            backoff = @Backoff(delayExpression = "${key-management-agent.retry-policy.backoffDelay}")
    )
    public Optional<KmsCredentials> getSecret(final String credentialReference) {
        try {
            final VaultResponse vaultResponse =
                    vaultTemplate.read(prefixPath.concat(credentialReference)).block();

            return Optional.ofNullable(mapVaultResponse(vaultResponse));

        } catch (Exception exception) {
            log.error(
                    "Cannot get credentials from KMS: {}",
                    exception.getMessage(),
                    exception);

            final List<String> errorData = Arrays.asList(credentialReference, exception.getMessage());
            throw new KmsAgentHttpException("Cannot get credentials from KMS",
                    HttpStatus.BAD_REQUEST,
                    errorData.toString());
        }
    }

    private KmsCredentials mapVaultResponse(VaultResponse vaultResponse) {
        Map<String, String> credentialsMap = null;
        if (Objects.nonNull(vaultResponse)) {
            final Map<String, Object> vaultResponseData = vaultResponse.getData();
            if (Objects.nonNull(vaultResponseData))
            {
                credentialsMap = (Map<String, String>) vaultResponseData.get("data");
            }
        }
        return CollectionUtils.isEmpty(credentialsMap)? null : new KmsCredentials(credentialsMap.get("credentials"));
    }

    /**
     * This API is used to delete an existing user secret from the provided path location in KMS.
     *
     * @param credentialReference path location where the secret is stored in KMS
     */
    @Retryable(
            retryFor = KmsClientRetryException.class,
            maxAttemptsExpression = "${key-management-agent.retry-policy.attempts}",
            backoff = @Backoff(delayExpression = "${key-management-agent.retry-policy.backoffDelay}")
    )
    public void deleteSecret(final String credentialReference) {
        try {
            vaultTemplate.delete(prefixPath.concat(credentialReference)).block();
        } catch (VaultException vaultException) {
            handleVaultException(vaultException, credentialReference, "Cannot delete credentials from KMS");
        } catch (Exception exception) {
            log.error(
                    "KMS Agent caught. Cannot get credentials from KMS: {}",
                    exception.getMessage(),
                    exception);
            final List<String> errorData = Arrays.asList(credentialReference, exception.getMessage());
            throw new KmsAgentHttpException("Cannot delete credentials from KMS",
                    HttpStatus.BAD_REQUEST,
                    errorData.toString());
        }
    }

    private void handleVaultException(final VaultException vaultException, final String credentialReference,
                                      final String summary) {
        log.error(
                "Caught VaultException. Cannot get credentials from KMS: {}",
                vaultException.getMessage(),
                vaultException);
        final List<String> errorData = Arrays.asList(credentialReference, vaultException.getMessage());
        final HttpStatusCode statusCode = extractStatusCodeValue(vaultException.getMessage());
        final HttpStatusCode httpStatusCode = HttpStatusCode.valueOf(statusCode.value());
        if (httpStatusCode.is5xxServerError()) {
            throw new KmsClientRetryException(
                    summary,
                    httpStatusCode,
                    errorData.toString());
        }

        throw new KmsAgentHttpException(
                summary,
                HttpStatus.BAD_REQUEST,
                errorData.toString());
    }

    private HttpStatusCode extractStatusCodeValue(String message) {
        String httpStatusCode = String.valueOf(HttpStatus.BAD_REQUEST.value());
        final Matcher m = Pattern.compile("\\d{3}").matcher(message);
        if (m.find()) {
            httpStatusCode = m.group();
        }
        return HttpStatusCode.valueOf(Integer.parseInt(httpStatusCode));
    }

    /**
     * buildKmsReference - adds tenant to credential reference
     *
     * @param tenant - user tenant
     * @param credentialKey - original credential key
     * @return String tenant + credentialKey
     */
    public String buildKmsReference(final String tenant, final String credentialKey) {
        return tenant + ((credentialKey.startsWith("/")) ? "" : "/") + credentialKey;
    }

    /**
     * removeLeadingPathSeparatorIfPresent - removes a starting forward slash
     * @param path - String reference pathi.e. /test/key
     * @return String - forward slash is removed at beginning
     */
    public String removeLeadingPathSeparatorIfPresent(final String path){
        return ((path.startsWith("/")) ? path.substring(1) : path);
    }
}
