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
package com.ericsson.oss.sec;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;

/**
 * Core Application, the starting point of the application.
 */
@SpringBootApplication(scanBasePackages = {
    "com.ericsson.bos.so.security.mtls.*",
    "com.ericsson.oss.sec.*",
    "com.ericsson.bos.so.shared.spring.security" })
@EnableRetry
public class KeyManagementAgentApplication {
    /**
     * Main entry point of the application.
     *
     * @param args Command line arguments
     */
    public static void main(final String[] args) {
        SpringApplication.run(KeyManagementAgentApplication.class, args);
    }

}
