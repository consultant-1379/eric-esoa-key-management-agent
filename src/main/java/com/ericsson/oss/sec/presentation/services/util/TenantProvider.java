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
package com.ericsson.oss.sec.presentation.services.util;

import org.springframework.stereotype.Service;
import com.ericsson.bos.so.shared.spring.security.utils.AuthenticationUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * Provides the logged in tenant name as extracted from the AuthorizationContext.
 */
@Service
@Slf4j
public class TenantProvider {

    /**
     * Get the logged in tenant for the current request
     *
     * @return tenant name
     */
    public String getLoggedInTenant() {
        final String tenantName = AuthenticationUtils.getTenant().get();
        log.debug("Tenant name {} retrieved from auth context", tenantName);
        return tenantName;
    }

}
