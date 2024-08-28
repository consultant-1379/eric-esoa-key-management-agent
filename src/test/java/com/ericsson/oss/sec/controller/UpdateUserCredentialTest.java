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

import com.ericsson.oss.sec.api.model.UpdateCredentialRequestBody;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UpdateUserCredentialTest
 */
class UpdateUserCredentialTest extends ConfigurationCredentialTestBase {

    /**
     * givenExistingCredentialStored_WhenCredentialUpdateRequest_ThenSuccess
     *
     * @throws Exception - Exception
     */
    @Test
    void givenExistingCredentialStored_WhenCredentialUpdateRequest_ThenSuccess() throws Exception {
        // Given
        // * credential already stored, so GET credential returns success
        // * mock server expects a PUT request to the default secret URI with a secret value to update to
        kmsMockServer.enqueue(createMockResponse(HttpStatus.OK, vaultCredentialResponse));

        final UpdateCredentialRequestBody credentialToUpdate = new UpdateCredentialRequestBody();
        credentialToUpdate.setCredentialValue(DEFAULT_CREDENTIAL_VALUE);
        final String updateRequestBody =  new ObjectMapper().writeValueAsString(credentialToUpdate);

        kmsMockServer.enqueue(createMockResponse(HttpStatus.CREATED, vaultCredentialResponse));

        // When
        // * we send the PUT request to Key Management Agent
        // Then
        // * Key Management agent returns a 204 No Content status code
        // * mock server verifies that it received the expected UPDATE request
        mvc.perform(put(URL_KMS_AGENT_CREDENTIALS_PATH + QUERY_PARAM_CREDENTIAL_REFERENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isNoContent());
    }


    /**
     * givenWebClientException_WhenCredentialUpdateRequest_ThenInternalServerError
     *
     * @throws Exception - Exception
     */
    @Test
    void givenWebClientException_WhenCredentialUpdateRequest_ThenInternalServerError() throws Exception {
        // Given
        // * credential already stored, so GET credential returns success
        // * mock server expects a PUT request to the default secret URI with a secret value to update to
        kmsMockServer.enqueue(createMockResponse(HttpStatus.OK, vaultCredentialResponse));

        final UpdateCredentialRequestBody credentialToUpdate = new UpdateCredentialRequestBody();
        credentialToUpdate.setCredentialValue(DEFAULT_CREDENTIAL_VALUE);
        final String updateRequestBody =  new ObjectMapper().writeValueAsString(credentialToUpdate);

        kmsMockServer.enqueue(createMockResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));
        kmsMockServer.enqueue(createMockResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error"));

        // When
        // * we send the PUT request to Key Management Agent
        // Then
        // * Key Management agent returns a 204 No Content status code
        // * mock server verifies that it received the expected UPDATE request
        mvc.perform(put(URL_KMS_AGENT_CREDENTIALS_PATH + QUERY_PARAM_CREDENTIAL_REFERENCE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateRequestBody))
                .andExpect(status().isInternalServerError());

        assertEquals(3, kmsMockServer.getRequestCount());
    }

    /**
     * givenNoCredentialExists_WhenCredentialUpdateRequest_ThenNotFound
     *
     * @throws Exception - Exception
     */
    @Test
    void givenNoCredentialExists_WhenCredentialUpdateRequest_ThenNotFound() throws Exception {
        kmsMockServer.enqueue(createMockResponse(HttpStatus.OK, vaultLoginResponse));

        // Given
        // * credential does not already exist in KMS
        kmsMockServer.enqueue(createMockResponse(HttpStatus.NOT_FOUND, vaultNotFoundResponse));

        final UpdateCredentialRequestBody credentialToUpdate = new UpdateCredentialRequestBody();
        credentialToUpdate.setCredentialValue(DEFAULT_CREDENTIAL_VALUE);
        final String updateRequestBody =  new ObjectMapper().writeValueAsString(credentialToUpdate);

        // When
        // * we send the PUT request to Key Management Agent
        // Then
        // * Key Management agent returns a 403 Forbidden status code
        // * mock server verifies that it received the expected UPDATE request
        mvc.perform(put(URL_KMS_AGENT_CREDENTIALS_PATH + QUERY_PARAM_CREDENTIAL_REFERENCE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestBody))
                .andExpect(status().isNotFound());
    }

    /**
     * givenCredentialReferenceWithInvalidUriChar_WhenUpdateCredentialRequest_ThenBadRequest
     *
     * @throws Exception - Exception
     */
    @Test
    void givenCredentialReferenceWithInvalidUriChar_WhenUpdateCredentialRequest_ThenBadRequest() throws Exception {
        // Given
        // * an invalid path is provided by the user, with
        // * ASCII control characters (e.g. backspace, vertical tab, horizontal tab, line feed etc), or
        // * unsafe characters like space, \, <, >, {, } etc, and any character outside the ASCII, or
        // * charset placed directly within URLs.
        final String INVALID_PATH_QUERY_PARAM =
                QUERY_PARAM_CREDENTIAL_REFERENCE.replaceFirst(DEFAULT_TENANT + "/.*", DEFAULT_TENANT + "/invalid/path} >");

        // When
        // * we send the PUT request to Key Management Agent
        // Then
        // * Key Management agent returns a 400 Bad Request status code
        // * mock server verifies that it did not receive any request (i.e., validation failed the request before the point of calling KMS)
        final UpdateCredentialRequestBody credentialToUpdate = new UpdateCredentialRequestBody();
        credentialToUpdate.setCredentialValue(DEFAULT_CREDENTIAL_VALUE);
        final String updateRequestBody =  new ObjectMapper().writeValueAsString(credentialToUpdate);
        mvc.perform(put(URL_KMS_AGENT_CREDENTIALS_PATH + INVALID_PATH_QUERY_PARAM)
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateRequestBody))
                .andExpect(status().isBadRequest());

    }
}
