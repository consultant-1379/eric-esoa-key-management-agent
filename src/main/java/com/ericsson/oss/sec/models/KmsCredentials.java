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
package com.ericsson.oss.sec.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Kms credentials.
 */
@Getter
@Setter
public class KmsCredentials implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonProperty("credentialValue")
    private String credentialValue;

    /**
     * Instantiates a new Kms credentials.
     *
     * @param credentialValue
     *         the credential value
     */
    @JsonCreator
    public KmsCredentials(@JsonProperty("credentialValue") String credentialValue) {
        this.credentialValue = credentialValue;
    }
}
