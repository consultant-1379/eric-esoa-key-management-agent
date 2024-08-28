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

import com.ericsson.oss.sec.presentation.controller.CredentialsController;
import com.ericsson.oss.sec.presentation.controller.exception.KmsAgentExceptionHandler;
import com.ericsson.oss.sec.presentation.services.KeyManagementService;
import com.ericsson.oss.sec.presentation.services.util.TenantProvider;
import com.ericsson.oss.sec.presentation.services.validation.CredentialRequestValidator;
import io.restassured.module.mockmvc.RestAssuredMockMvc;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;

/**
 * AllContractsBase
 */
@ExtendWith(MockitoExtension.class)
public class AllContractsBase {

    @InjectMocks
    protected CredentialsController credentialKeyController;


    @Mock
    protected KeyManagementService keyManagementService;

    @Mock
    protected TenantProvider tenantProvider;

    @Mock
    protected CredentialRequestValidator credentialRequestValidator;

    /**
     * mockMvcSetupForPositive
     */
    protected void mockMvcSetupForPositive(){
        final StandaloneMockMvcBuilder standaloneMockMvcBuilder =
                MockMvcBuilders.standaloneSetup(credentialKeyController, keyManagementService,
                        tenantProvider, credentialRequestValidator);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }

    /**
     * mockMvcSetupForNegative
     */
    protected void mockMvcSetupForNegative(){
        final StandaloneMockMvcBuilder standaloneMockMvcBuilder =
                MockMvcBuilders.standaloneSetup(credentialKeyController, keyManagementService,
                        new KmsAgentExceptionHandler(), tenantProvider, credentialRequestValidator);
        RestAssuredMockMvc.standaloneSetup(standaloneMockMvcBuilder);
    }

}
