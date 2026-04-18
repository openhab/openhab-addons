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
package org.openhab.binding.solaredge.internal.connector;

import java.io.EOFException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.SSLException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.http.HttpStatus.Code;

/**
 * this class contains the HTTP status of the communication and an optional exception that might occoured during
 * communication
 *
 * @author Alexander Friese - initial contribution
 */
@NonNullByDefault
public class CommunicationStatus {

    private @Nullable Code httpCode;
    private @Nullable Exception error;

    public final Code getHttpCode() {
        Code httpCode = this.httpCode;
        return httpCode == null ? Code.INTERNAL_SERVER_ERROR : httpCode;
    }

    public final void setHttpCode(Code httpCode) {
        this.httpCode = httpCode;
    }

    public final @Nullable Exception getError() {
        return error;
    }

    public final void setError(Exception error) {
        this.error = error;
    }

    public final String getMessage() {
        Code httpCode = this.httpCode;
        Exception error = this.error;
        if (error != null) {
            String message = error.getMessage();
            if (message != null && !message.isEmpty()) {
                return message;
            }
        } else if (httpCode != null && httpCode.getMessage() != null && !httpCode.getMessage().isEmpty()) {
            return httpCode.getMessage();
        }
        return "";
    }

    /**
     * Returns a sanitized message suitable for Thing status details.
     */
    public final String getUserFacingMessage() {
        Exception error = this.error;
        if (error != null) {
            if (error instanceof SocketTimeoutException || error instanceof TimeoutException) {
                return "Request timed out";
            } else if (error instanceof UnknownHostException) {
                return "DNS resolution failed";
            } else if (error instanceof EOFException || error instanceof SSLException) {
                return "Connection to SolarEdge interrupted";
            }
            return "Communication error";
        }

        Code httpCode = this.httpCode;
        if (httpCode != null && httpCode.getMessage() != null && !httpCode.getMessage().isEmpty()) {
            return httpCode.getMessage();
        }
        return "Communication error";
    }
}
