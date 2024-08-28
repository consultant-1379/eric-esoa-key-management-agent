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
import com.ericsson.oss.sec.presentation.services.exceptions.KmsClientRetryException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.mvc.support.DefaultHandlerExceptionResolver;

import static org.springframework.core.Ordered.HIGHEST_PRECEDENCE;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

/**
 * Handler to manage exceptions thrown and return a response.
 */
@ControllerAdvice
@Order(HIGHEST_PRECEDENCE)
@Slf4j
public class KmsAgentExceptionHandler extends DefaultHandlerExceptionResolver {
    /**
     * @param ex - KmsAgentHttpException
     *
     * @return ProblemDetails
     */
    @ExceptionHandler(KmsAgentHttpException.class)
    public final ResponseEntity<ProblemDetails> handleKmsAgentHttpException(final KmsAgentHttpException ex) {
        log.error("{}: {} ", ex.getProblemDetails().getTitle(), ex.getProblemDetails().getDetail());
        return new ResponseEntity<>(ex.getProblemDetails(), ex.getStatusCode());
    }

    /**
     * @param ex - KmsClientRetryException
     *
     * @return ProblemDetails
     */
    @ExceptionHandler(KmsClientRetryException.class)
    public final ResponseEntity<ProblemDetails> handleMaxRetriesExceededException(final KmsClientRetryException ex) {
        log.error("{}: {} ", ex.getProblemDetails().getTitle(), ex.getProblemDetails().getDetail());
        return new ResponseEntity<>(ex.getProblemDetails(), ex.getHttpStatus());
    }

    /**
     * @param ex - MethodArgumentNotValidException
     *
     * @return ProblemDetails
     */
    @ResponseBody
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetails handleRequestValidationException(final MethodArgumentNotValidException ex) {
        log.error(ex.getMessage(), ex);
        return new ProblemDetails()
                .title(ErrorMessages.SERVLET_BINDING_PROBLEM)
                .type(BAD_REQUEST.getReasonPhrase())
                .status(BAD_REQUEST.value())
                .detail(ex.getMessage());
    }
}
