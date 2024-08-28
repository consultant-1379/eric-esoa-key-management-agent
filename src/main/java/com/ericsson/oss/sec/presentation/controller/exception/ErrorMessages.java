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
package com.ericsson.oss.sec.presentation.controller.exception;

/**
 * String definitions for specific errors in KMS Agent
 */
public class ErrorMessages {

    // Problem Details : title
    public static final String STORE_CREDENTIAL_REQUEST_FAILED = "Store Credential request failed";
    public static final String UPDATE_CREDENTIAL_REQUEST_FAILED = "Update Credential request failed";
    public static final String DELETE_CREDENTIAL_REQUEST_FAILED = "Delete Credential request failed";
    public static final String SERVLET_BINDING_PROBLEM = "Validation error on client request data";
    public static final String VALIDATION_FAILED = "Failure during request validation";
    public static final String TENANT_PROVIDER_ERROR = "Failed to retrieve logged in tenant";

    // Problem Details : details
    public static final String ACCESS_TOKEN_ERROR = "Failed to retrieve AccessToken from authorization context.";

    public static final String CREDENTIAL_ALREADY_EXISTS = "The credential with key %s already exists in KMS";
    public static final String TENANT_FORBIDDEN = "The logged-in tenant [%s] is not permitted to perform the requested operation"
            + " on a credential reference with tenant name [%s].";

    public static final String CREDENTIAL_NOT_EXISTING = "The credential with reference %s for tenant %s does not exist in KMS";

    public static final String KMS_UNAVAILABLE = "Key Management Service is unavailable";
    public static final String INTERNAL_SERVER_ERROR = "Internal Server Error";

    private ErrorMessages() {}
}
