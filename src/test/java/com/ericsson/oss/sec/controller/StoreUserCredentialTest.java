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
package com.ericsson.oss.sec.controller;

import com.ericsson.oss.sec.api.model.StoreCredentialRequestBody;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static com.ericsson.oss.sec.utils.ResourceLoaderUtils.getClasspathResourceAsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Test class for use case Store Credential
 */
class StoreUserCredentialTest extends ConfigurationCredentialTestBase {

    /**
     * givenNoCredentialExists_WhenStoreCredentialIsRequested_ThenSuccess
     *
     * @throws Exception - Exception
     */
    @Test
    void givenNoCredentialExists_WhenStoreCredentialIsRequested_ThenSuccess() throws Exception {

        // Given the credential does not already exist in KMS
        kmsMockServer.enqueue(createMockResponse(HttpStatus.NOT_FOUND, vaultNotFoundResponse));

        // Mock ADP KMS server request and response for POST
        kmsMockServer.enqueue(createMockResponse(HttpStatus.CREATED, vaultCredentialResponse));

        // and the external user store credential request body
        final String requestBody = getDefaultStoreCredentialRequestBody();
        final String kmsAgentResponseBody = getClasspathResourceAsString(CREDENTIAL_REFERENCE_DEFAULT_RESPONSE);

        // When / Then
        mvc.perform(post(URL_KMS_AGENT_CREDENTIALS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(content().string(kmsAgentResponseBody));

    }

    /**
     * givenKmsResourceAccessException_WhenStoreCredentialIsRequested_ThenServiceUnavailable
     *
     * @throws Exception - Exception
     */
    @Test
    void givenKmsResourceAccessException_WhenStoreCredentialIsRequested_ThenServiceUnavailable() throws Exception {
        // Given the credential does not already exist in KMS;
        kmsMockServer.enqueue(createMockResponse(HttpStatus.NOT_FOUND, vaultNotFoundResponse));

        // Given a new store credential request
        final String requestBody = getDefaultStoreCredentialRequestBody();

        // Given KMS vault exception throws internal service error
        kmsMockServer.enqueue(createMockResponse(HttpStatus.SERVICE_UNAVAILABLE, serviceUnavailable));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.SERVICE_UNAVAILABLE, serviceUnavailable));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.SERVICE_UNAVAILABLE, serviceUnavailable));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.SERVICE_UNAVAILABLE, serviceUnavailable));

        // When / Then
        mvc.perform(post(URL_KMS_AGENT_CREDENTIALS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isServiceUnavailable());

        assertEquals(3,kmsMockServer.getRequestCount());
    }

    /**
     * givenInvalidKmsPath_WhenStoreCredentialIsRequested_ThenBadRequest
     *
     * @throws Exception - Exception
     */
    @Test
    void givenInvalidKmsPath_WhenStoreCredentialIsRequested_ThenBadRequest() throws Exception {

        // Given an invalid path is provided by the user (kms path can not have . char)
        final StoreCredentialRequestBody credential = new StoreCredentialRequestBody();
        credential.setCredentialKey("/invalid/pat.");
        credential.setCredentialValue(DEFAULT_CREDENTIAL_VALUE);
        final String requestBody = new ObjectMapper().writeValueAsString(credential);

        // When / Then
        mvc.perform(post(URL_KMS_AGENT_CREDENTIALS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

    }

    /**
     * givenCredentialKeyWithInvalidUriChar_WhenStoreCredentialIsRequested_ThenBadRequest
     *
     * @throws Exception - Exception
     */
    @Test
    void givenCredentialKeyWithInvalidUriChar_WhenStoreCredentialIsRequested_ThenBadRequest() throws Exception {

        // Given an invalid path is provided by the user
        // ASCII control characters (e.g. backspace, vertical tab, horizontal tab, line feed etc),
        // unsafe characters like space, \, <, >, {, } etc, and any character outside the ASCII
        // charset is not allowed to be placed directly within URLs.
        final StoreCredentialRequestBody credential = new StoreCredentialRequestBody();
        credential.setCredentialKey("/invalid/path} >");
        credential.setCredentialValue(DEFAULT_CREDENTIAL_VALUE);
        final String requestBody = new ObjectMapper().writeValueAsString(credential);

        // When / Then
        mvc.perform(post(URL_KMS_AGENT_CREDENTIALS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());

    }

    /**
     * givenRequestBodyWithInvalidJson_WhenStoreCredentialIsRequested_ThenBadRequest
     *
     * @throws Exception - Exception
     */
    @Test
    void givenRequestBodyWithInvalidJson_WhenStoreCredentialIsRequested_ThenBadRequest() throws Exception {
        // Given a request body with invalid JSON is provided by the user
        final String requestBodyWithInvalidJson = "{,},";

        // When / Then
        mvc.perform(post(URL_KMS_AGENT_CREDENTIALS_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBodyWithInvalidJson))
                .andExpect(status().isBadRequest());

    }

    /**
     * givenKmsGatewayTimeout_WhenStoreCredentialIsRequested_ThenGatewayTimeout
     *
     * @throws Exception - Exception
     */
    @Test
    void givenKmsGatewayTimeout_WhenStoreCredentialIsRequested_ThenGatewayTimeout() throws Exception {

        // Given the secret doesn't already exist in KMS we try to store it
        kmsMockServer.enqueue(createMockResponse(HttpStatus.NOT_FOUND, vaultNotFoundResponse));

        // Given gateway times out
        kmsMockServer.enqueue(createMockResponse(HttpStatus.GATEWAY_TIMEOUT, badGateway));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.GATEWAY_TIMEOUT, badGateway));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.GATEWAY_TIMEOUT, badGateway));

        // Set up Request body
        final String requestBody = getDefaultStoreCredentialRequestBody();

        // When / Then
        mvc.perform(post(URL_KMS_AGENT_CREDENTIALS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isGatewayTimeout());


        assertEquals(3,kmsMockServer.getRequestCount());
    }

    /**
     * givenCredentialAlreadyExists_WhenStoreCredentialIsRequested_ThenConflict
     *
     * @throws Exception - Exception
     */
    @Test
    void givenCredentialAlreadyExists_WhenStoreCredentialIsRequested_ThenConflict() throws Exception {
        // Given GET request for credential returns an existing credential
        kmsMockServer.enqueue(createMockResponse(HttpStatus.OK, vaultCredentialResponse));
        // Set up Request body
        final String postRequestBody = getDefaultStoreCredentialRequestBody();

        // When / Then
        mvc.perform(post(URL_KMS_AGENT_CREDENTIALS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(postRequestBody))
                .andExpect(status().isConflict());

    }

    /**
     * givenIncorrectCredentialKeyName_WhenStoreCredentialIsRequested_ThenBadRequest
     *
     * @throws Exception - Exception
     */
    @Test
    void givenIncorrectCredentialKeyName_WhenStoreCredentialIsRequested_ThenBadRequest() throws Exception {
        // Given a credential key name incorrectly configured in the client json request
        final String jsonString = new JSONObject()
                .put("credentialKeyyyy", "invalid")
                .put("credentialValue", DEFAULT_CREDENTIAL_VALUE)
                .toString();

        // When / Then
        mvc.perform(post(URL_KMS_AGENT_CREDENTIALS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonString))
                .andExpect(status().isBadRequest());

    }

    private String getDefaultStoreCredentialRequestBody() throws JsonProcessingException {
        final StoreCredentialRequestBody credential = new StoreCredentialRequestBody();
        credential.setCredentialKey(TEST_CREDENTIAL_REFERENCE_DEFAULT);
        credential.setCredentialValue(DEFAULT_CREDENTIAL_VALUE);
        return new ObjectMapper().writeValueAsString(credential);
    }
}
