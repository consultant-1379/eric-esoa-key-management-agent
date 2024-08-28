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
 * NegativeStoreCredentialAlreadyExistsInKmsBase
 */
public class NegativeStoreCredentialAlreadyExistsInKmsBase extends AllContractsBase {

    /**
     * setup - test
     */
    @BeforeEach
    public void setup() {
        final KmsAgentHttpException conflictException =
                new KmsAgentHttpException(ErrorMessages.STORE_CREDENTIAL_REQUEST_FAILED,
                        HttpStatus.CONFLICT, ErrorMessages.CREDENTIAL_ALREADY_EXISTS);

        willThrow(conflictException)
                .given(keyManagementService).storeSecret(any(),any());

        mockMvcSetupForNegative();
    }
}
