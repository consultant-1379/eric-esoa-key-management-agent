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
package com.ericsson.oss.sec.presentation.services.exceptions;

import com.ericsson.oss.sec.presentation.controller.exception.KmsAgentHttpException;
import org.springframework.http.HttpStatus;

/**
 *  KmsPermissionDeniedException
 */
public class KmsPermissionDeniedException extends KmsAgentHttpException {
    /**
     * Create KmsPermissionDeniedException with ProblemDetails object
     *
     * @param summary - description
     *
     * @param status - http status
     *
     * @param detailMessage - detailed description
     */
    public KmsPermissionDeniedException(final String summary, final HttpStatus status, final String detailMessage) {
        super(summary, status, detailMessage);
    }
}
