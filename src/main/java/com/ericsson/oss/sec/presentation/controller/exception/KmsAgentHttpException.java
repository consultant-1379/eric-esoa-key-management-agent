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

import com.ericsson.oss.sec.api.model.ProblemDetails;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpStatusCodeException;

/**
 * Generic exception to handle error cases in KMS Agent microservice
 */
public class KmsAgentHttpException extends HttpStatusCodeException {

    @Getter
    private final transient ProblemDetails problemDetails;

    /**
     * Create KmsAgentHttpException with ProblemDetails object
     *
     * @param summary - description
     *
     * @param status - http status
     *
     * @param detailMessage - detailed description
     */
    public KmsAgentHttpException(final String summary, final HttpStatus status, final String detailMessage) {
        super(status);
        problemDetails = new ProblemDetails()
                .title(summary)
                .type(status.getReasonPhrase())
                .status(status.value())
                .detail(detailMessage);
    }
}
