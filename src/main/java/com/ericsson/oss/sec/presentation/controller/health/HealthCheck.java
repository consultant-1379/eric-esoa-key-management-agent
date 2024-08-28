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
package com.ericsson.oss.sec.presentation.controller.health;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Health Check component for microservice chassis.
 * Any internal logic can change health state of the chassis.
 */

@ConfigurationProperties(prefix = "info.app")
@Component
@Slf4j
@Data
public final class HealthCheck implements HealthIndicator {

    /**
     * Error upon health check.
     */
    private String errorMessage;

    /**
     * Name of the service.
     */
    private String name;

    @Override
    public Health health() {
        log.trace("Invoking chassis specific health check");
        final String errorCode = getErrorMessage();
        if (errorCode != null) {
            return Health.down().withDetail("Error: ", errorCode).build();
        }
        log.info("{} is UP and healthy", this.name);
        return Health.up().build();
    }

    /**
     * Set the error message that will cause fail health check of micro service.
     *
     * @param errorMessage Error message from health check.
     */
    public void failHealthCheck(final String errorMessage) {
        this.errorMessage = errorMessage;
    }

}
