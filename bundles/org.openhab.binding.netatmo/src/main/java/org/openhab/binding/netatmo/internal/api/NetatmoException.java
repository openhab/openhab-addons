/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal.api;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An exception that occurred while communicating with an automower or an automower bridge
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetatmoException extends IOException {
    private static final long serialVersionUID = 1513549973502021727L;
    private int statusCode = -1;

    public NetatmoException(Exception e) {
        super(e);
    }

    public NetatmoException(int statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public NetatmoException(String message, Exception e) {
        super(message, e);
    }

    public NetatmoException(String message) {
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
