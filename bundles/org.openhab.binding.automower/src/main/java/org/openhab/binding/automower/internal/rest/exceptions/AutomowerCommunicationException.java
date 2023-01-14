/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.automower.internal.rest.exceptions;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An exception that occurred while communicating with an automower or an automower bridge
 *
 * @author Markus Pfleger - Initial contribution
 */
@NonNullByDefault
public class AutomowerCommunicationException extends IOException {
    private static final long serialVersionUID = 1L;
    private int statusCode = -1;

    public AutomowerCommunicationException(Exception e) {
        super(e);
    }

    public AutomowerCommunicationException(int statusCode, Exception e) {
        super(e);
        this.statusCode = statusCode;
    }

    public AutomowerCommunicationException(int statusCode) {
        this.statusCode = statusCode;
    }

    public AutomowerCommunicationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public AutomowerCommunicationException(String message, Exception e) {
        super(message, e);
    }

    public AutomowerCommunicationException(String message) {
        super(message);
    }

    public int getStatusCode() {
        return statusCode;
    }

    @Override
    public @Nullable String getMessage() {
        String message = super.getMessage();
        return message == null ? null : "Rest call failed: statusCode=" + statusCode + ", message=" + message;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + ": statusCode=" + statusCode + ", message=" + super.getMessage()
                + ", cause: " + getCause();
    }
}
