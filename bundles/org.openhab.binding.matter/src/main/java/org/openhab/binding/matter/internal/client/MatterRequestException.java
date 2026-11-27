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
package org.openhab.binding.matter.internal.client;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception thrown when a request to the Matter server fails.
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public class MatterRequestException extends Exception {
    private static final long serialVersionUID = 1L;
    private final String errorMessage;
    private final @Nullable MatterErrorCode errorCode;
    private final boolean fromServer;

    /**
     * @param message the error message
     * @param errorCode the error code, if the server sent a known one
     * @param fromServer true when the server answered the request with an error, false when the request never
     *            reached it
     */
    public MatterRequestException(String message, @Nullable MatterErrorCode errorCode, boolean fromServer) {
        super(message);
        this.errorMessage = message;
        this.errorCode = errorCode;
        this.fromServer = fromServer;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public @Nullable MatterErrorCode getErrorCode() {
        return errorCode;
    }

    /**
     * Whether the server answered the request with an error. False when the request never reached the server.
     */
    public boolean isFromServer() {
        return fromServer;
    }
}
