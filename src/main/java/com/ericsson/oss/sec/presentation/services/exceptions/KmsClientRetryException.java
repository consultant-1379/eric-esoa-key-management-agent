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

import com.ericsson.oss.sec.api.model.ProblemDetails;
import lombok.Data;
import lombok.ToString;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

/**
 *  KmsClientRetryException
 */
@Data
@ToString(callSuper = true)
public class KmsClientRetryException extends RuntimeException {

    private final transient ProblemDetails problemDetails;

    private final HttpStatusCode httpStatus;

    /**
     * Create KmsClientRetryException with ProblemDetails object
     *
     * @param summary - description
     *
     * @param status - http status
     *
     * @param detailMessage - detailed description
     */
    public KmsClientRetryException(final String summary, final HttpStatusCode httpStatusCode, final String detailMessage) {
        problemDetails = new ProblemDetails()
                .title(summary)
                .type(((HttpStatus) httpStatusCode).getReasonPhrase())
                .status(httpStatusCode.value())
                .detail(detailMessage);
        this.httpStatus =  httpStatusCode;
    }

}
