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

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DeleteUserCredentialTest
 */
class DeleteUserCredentialTest extends ConfigurationCredentialTestBase {

    /**
     * givenExistingCredentialStored_WhenCredentialDeleteRequest_ThenSuccess
     *
     * @throws Exception - Exception
     */
    @Test
    void givenExistingCredentialStored_WhenCredentialDeleteRequest_ThenSuccess() throws Exception {
        // Given
        // * successful login to get the client token
        // * mock server expects a DELETE request to the default secret URI
        kmsMockServer.enqueue(createMockResponse(HttpStatus.OK, vaultCredentialResponse));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.NO_CONTENT, ""));

        // When
        // * we send the DELETE request to Key Management Agent
        // Then
        // * Key Management agent returns a 204 No Content status code
        // * mock server verifies that it received the expected DELETE request
        mvc.perform(delete(URL_KMS_AGENT_CREDENTIALS_PATH + QUERY_PARAM_CREDENTIAL_REFERENCE))
                .andExpect(status().isNoContent());

    }

    /**
     * givenNoCredentialExists_WhenCredentialDeleteRequest_ThenNotFound
     *
     * @throws Exception - Exception
     */
    @Test
    void givenNoCredentialExists_WhenCredentialDeleteRequest_ThenNotFound() throws Exception {
        // Given
        // * successful login to get a client token
        // * credential does not already exist in KMS
        kmsMockServer.enqueue(createMockResponse(HttpStatus.NOT_FOUND, vaultNotFoundResponse));

        // When
        // * we send the DELETE request to Key Management Agent
        // Then
        // * Key Management agent returns a 403 Forbidden status code
        // * mock server verifies that it received the expected DELETE request
        mvc.perform(delete(URL_KMS_AGENT_CREDENTIALS_PATH + QUERY_PARAM_CREDENTIAL_REFERENCE))
                .andExpect(status().isNotFound());

    }

    /**
     * givenCredentialReferenceWithInvalidUriChar_WhenDeleteCredentialRequest_ThenBadRequest
     *
     * @throws Exception - Exception
     */
    @Test
    void givenCredentialReferenceWithInvalidUriChar_WhenDeleteCredentialRequest_ThenBadRequest() throws Exception {
        // Given
        // * an invalid path is provided by the user, with
        // * ASCII control characters (e.g. backspace, vertical tab, horizontal tab, line feed etc), or
        // * unsafe characters like space, \, <, >, {, } etc, and any character outside the ASCII, or
        // * charset placed directly within URLs.
        kmsMockServer.enqueue(createMockResponse(HttpStatus.OK, vaultLoginResponse));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.OK, vaultCredentialResponse));

        final String INVALID_PATH_QUERY_PARAM =
                QUERY_PARAM_CREDENTIAL_REFERENCE.replaceFirst(DEFAULT_TENANT + "/.*", DEFAULT_TENANT + "/invalid/path} >");

        // When
        // * we send the DELETE request to Key Management Agent
        // Then
        // * Key Management agent returns a 403 Forbidden status code
        // * mock server verifies that it did not receive any request (i.e., validation failed the request before the point of calling KMS)
        mvc.perform(delete(URL_KMS_AGENT_CREDENTIALS_PATH + INVALID_PATH_QUERY_PARAM))
                .andExpect(status().isBadRequest());


    }

    /**
     * givenKmsServerUnavailable_WhenDeleteCredentialRequest_ThenServiceUnavailable
     *
     * @throws Exception - Exception
     */
    @Test
    void givenKmsServerUnavailable_WhenDeleteCredentialRequest_ThenServiceUnavailable() throws Exception {
        // Given
        // * a successful login to get the client token
        // * the secret exists in KMS
        // * and we try to delete it, but get a 503 ServiceUnavailable

        // Given the secret doesn't already exist in KMS we try to store it
        kmsMockServer.enqueue(createMockResponse(HttpStatus.OK, vaultLoginResponse));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.OK, vaultCredentialResponse));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.SERVICE_UNAVAILABLE, serviceUnavailable));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.SERVICE_UNAVAILABLE, serviceUnavailable));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.SERVICE_UNAVAILABLE, serviceUnavailable));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.SERVICE_UNAVAILABLE, serviceUnavailable));

        // When
        // * we send the DELETE request to Key Management Agent
        // Then
        // * Key Management agent returns a 503 Service Unavailable status code
        // * mock server verifies that it received the expected requests
        mvc.perform(delete(URL_KMS_AGENT_CREDENTIALS_PATH + QUERY_PARAM_CREDENTIAL_REFERENCE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isServiceUnavailable());


        assertEquals(4,kmsMockServer.getRequestCount());
    }

    /**
     * givenKmsGatewayTimeout_WhenDeleteCredentialRequest_ThenGatewayTimeout
     *
     * @throws Exception - Exception
     */
    @Test
    void givenKmsGatewayTimeout_WhenDeleteCredentialRequest_ThenGatewayTimeout() throws Exception {
        // Given
        // * a successful login to get the client token
        // * the secret exists in KMS
        // * and we try to delete it, but get a 504 Gateway Timeout
        kmsMockServer.enqueue(createMockResponse(HttpStatus.OK, vaultCredentialResponse));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.GATEWAY_TIMEOUT, badGateway));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.GATEWAY_TIMEOUT, badGateway));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.GATEWAY_TIMEOUT, badGateway));

        // When
        // * we send the DELETE request to Key Management Agent
        // Then
        // * Key Management agent returns a 504 Gateway Timeout status code
        // * mock server verifies that it received the expected requests
        mvc.perform(delete(URL_KMS_AGENT_CREDENTIALS_PATH + QUERY_PARAM_CREDENTIAL_REFERENCE)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isGatewayTimeout());


        assertEquals(3, kmsMockServer.getRequestCount());
    }

}
