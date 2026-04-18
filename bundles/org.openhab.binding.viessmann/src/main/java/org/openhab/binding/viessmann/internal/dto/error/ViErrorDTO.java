/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.viessmann.internal.dto.error;

/**
 * The {@link ViErrorDTO} provides the viessmann error message
 *
 * @author Ronny Grun - Initial contribution
 */
public class ViErrorDTO {
    private String viErrorId;
    private Integer statusCode;
    private String errorType;
    private String message;
    private ExtendedPayload extendedPayload;

    public String getViErrorId() {
        return viErrorId;
    }

    public void setViErrorId(String viErrorId) {
        this.viErrorId = viErrorId;
    }

    public Integer getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode) {
        this.statusCode = statusCode;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ExtendedPayload getExtendedPayload() {
        return extendedPayload;
    }

    public void setExtendedPayload(ExtendedPayload extendedPayload) {
        this.extendedPayload = extendedPayload;
    }
}
