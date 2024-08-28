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

import com.ericsson.oss.sec.models.KmsCredentials;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;

/**
 * PositiveUpdateCredentialBase
 */
@ExtendWith(MockitoExtension.class)
public class PositiveUpdateCredentialBase extends AllContractsBase {

    /**
     * setup for tests
     */
    @BeforeEach
    public void setup() {
        given(tenantProvider.getLoggedInTenant()).willReturn("master");
        given(keyManagementService.buildKmsReference(any(), any())).willReturn("master/credential/key");

        final KmsCredentials kmsCredentials = new KmsCredentials("test");
        given(keyManagementService.getSecret(any())).willReturn(Optional.ofNullable(kmsCredentials));

        willDoNothing().given(keyManagementService)
                .storeSecret(any(),any());

        mockMvcSetupForPositive();
    }
}
