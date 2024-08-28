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
package com.ericsson.oss.sec.api.contract;

import com.ericsson.oss.sec.presentation.controller.exception.ErrorMessages;
import com.ericsson.oss.sec.presentation.controller.exception.KmsAgentHttpException;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;

/**
 * NegativeStoreCredentialBadRequestBase
 */
public class NegativeStoreCredentialBadRequestBase extends AllContractsBase {
    /**
     * setup - tests
     */
    @BeforeEach
    public void setup() {
        // No need to mock any service implementation calls here. Spring validation (@Valid)
        // on the API for all incoming requests will prevent the request from reaching the
        // controller and KeyManagementAgent. Spring will throw the binding exception
        // which is then handled in the exception handler.

        final KmsAgentHttpException badRequestException =
                new KmsAgentHttpException(ErrorMessages.SERVLET_BINDING_PROBLEM,
                        HttpStatus.BAD_REQUEST, ErrorMessages.SERVLET_BINDING_PROBLEM);

        willThrow(badRequestException)
                .given(keyManagementService).storeSecret(any(),any());


        mockMvcSetupForNegative();
    }
}
