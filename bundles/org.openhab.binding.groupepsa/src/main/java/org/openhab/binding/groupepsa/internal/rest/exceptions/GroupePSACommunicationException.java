/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.groupepsa.internal.rest.exceptions;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An exception that occurred while communicating with a groupepsa or a groupepsa bridge
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public class GroupePSACommunicationException extends IOException {
    private static final long serialVersionUID = 1L;
    private int statusCode = -1;

    public GroupePSACommunicationException(Exception e) {
        super(e);
    }

    public GroupePSACommunicationException(int statusCode, Exception e) {
        super(e);
        this.statusCode = statusCode;
    }

    public GroupePSACommunicationException(int statusCode) {
        this.statusCode = statusCode;
    }

    public GroupePSACommunicationException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public GroupePSACommunicationException(String message, Exception e) {
        super(message, e);
    }

    public GroupePSACommunicationException(String message) {
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
