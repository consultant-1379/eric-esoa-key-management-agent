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

import com.ericsson.oss.sec.KeyManagementAgentApplication;
import com.ericsson.oss.sec.presentation.controller.CredentialsController;
import com.ericsson.oss.sec.presentation.controller.exception.KmsAgentExceptionHandler;
import com.ericsson.oss.sec.presentation.services.KeyManagementService;
import com.ericsson.oss.sec.presentation.services.validation.CredentialRequestValidator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static com.ericsson.bos.so.shared.spring.security.utils.Contants.TOKEN;
/**
 * ConfigurationCredentialTestBase
 */
@ActiveProfiles("test")
@ContextConfiguration
@SpringBootTest(
        classes = {
            KeyManagementAgentApplication.class,
            CredentialsController.class,
            KeyManagementService.class,
            KmsAgentExceptionHandler.class,
            CredentialRequestValidator.class
        }
    )
@TestPropertySource(properties = { "spring.config.additional-location = src/test/resources/app/config/truststore.yaml" })
public class ConfigurationCredentialTestBase {
    protected static final String DEFAULT_CREDENTIAL_VALUE = "password";
    protected static final String DEFAULT_TENANT = "master";
    protected static final String TEST_CREDENTIAL_BASE = "/test/credential";
    protected static final String TEST_CREDENTIAL_REFERENCE_DEFAULT = TEST_CREDENTIAL_BASE + "/key";

    protected static final String URL_KMS_AGENT_CREDENTIALS_PATH = "/v1/configuration/credentials";
    protected static final String QUERY_PARAM_CREDENTIAL_REFERENCE = "?credentialReference="
            + DEFAULT_TENANT + TEST_CREDENTIAL_REFERENCE_DEFAULT;
    protected static final String CREDENTIALS_PATH = "/secret-v2/data/credentials/";

    protected static final String KMS_BASE_URL = "https://eric-sec-key-management:8200";
    protected static final String KMS_DATA_URL = KMS_BASE_URL + CREDENTIALS_PATH;

    protected static final String CREDENTIAL_REFERENCE_DEFAULT_RESPONSE = "responses/KmsAgentPostResponseBody_Default.json";

    protected static final String GET_SECRET_SUCCESS_KMS_RESPONSE = "responses/GetKmsSecretResponseBody_Success.json";

    protected static final String GET_SECRET_NOT_FOUND_KMS_RESPONSE = "responses/GetKmsSecretResponseBody_NotFound.json";

    protected static final String KMS_VALID_LOGIN_RESPONSE_JSON = "responses/PostKmsLoginResponseBody_Success.json";


    protected static final String ERROR_SERVICE_UNAVAILABLE = "responses/ServiceUnavailableError.json";

    protected static final String ERROR_BAD_GATEWAY = "responses/BadGateway.json";
    protected static String vaultLoginResponse;
    protected static String vaultCredentialResponse;
    protected static String vaultNotFoundResponse;

    protected static String serviceUnavailable;
    protected static String badGateway;

    @Autowired
    protected WebApplicationContext webApplicationContext;
    protected MockMvc mvc;

    /**
     * The Kms mock server.
     */
    protected MockWebServer kmsMockServer;
    @Value("${spring.cloud.vault.port}")
    private int kmsPort;

    /**
     *  setUpAll
     * @throws IOException throws IO exception for reading config files
     */
    @BeforeAll
    public static void setUpAll() throws IOException {

        final UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken("test", "test");
        final Map<String, Object> details = new HashMap<>();
        final String tokenValue = Resources.toString(
                Resources.getResource("token.txt"), Charsets.UTF_8);

        details.put(TOKEN, tokenValue);
        details.put("tenant_name", DEFAULT_TENANT);
        authentication.setDetails(details);
        SecurityContextHolder.getContext().setAuthentication(
                authentication);
        vaultLoginResponse =
                Resources.toString(
                        Resources.getResource(KMS_VALID_LOGIN_RESPONSE_JSON), Charsets.UTF_8);
        vaultCredentialResponse =
                Resources.toString(
                        Resources.getResource(GET_SECRET_SUCCESS_KMS_RESPONSE), Charsets.UTF_8);
        serviceUnavailable =
                Resources.toString(
                        Resources.getResource(ERROR_SERVICE_UNAVAILABLE), Charsets.UTF_8);
        badGateway =
                Resources.toString(
                        Resources.getResource(ERROR_BAD_GATEWAY), Charsets.UTF_8);

        vaultNotFoundResponse = Resources.toString(Resources.getResource(GET_SECRET_NOT_FOUND_KMS_RESPONSE), Charsets.UTF_8);

    }

    /**
     * setUp for test
     *
     * @throws Exception
     *         the exception
     */
    @BeforeEach
    public void setUp() throws Exception{
        mvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        kmsMockServer = new MockWebServer();
        kmsMockServer.start(kmsPort);
    }

    /**
     * Tear down kms.
     *
     * @throws Exception
     *             The Exception that may be thrown when running this method.
     */
    @AfterEach
    void tearDownKms() throws Exception {
        kmsMockServer.shutdown();
    }

    /**
     * Create mock response mock response.
     *
     * @param httpStatus
     *            the http status
     * @param body
     *            the body
     *
     * @return the mock response
     */
    public static MockResponse createMockResponse(final HttpStatus httpStatus, final String body) throws JsonProcessingException {
        return new MockResponse()
                .setResponseCode(httpStatus.value())
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .setBody(body);
    }
}
