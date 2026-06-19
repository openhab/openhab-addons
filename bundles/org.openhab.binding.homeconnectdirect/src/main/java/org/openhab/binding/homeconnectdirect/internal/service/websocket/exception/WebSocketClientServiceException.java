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
package org.openhab.binding.homeconnectdirect.internal.service.websocket.exception;

import java.io.Serial;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Exception thrown by WebSocket client services.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public class WebSocketClientServiceException extends Exception {

    @Serial
    private static final long serialVersionUID = -8417710695707997749L;

    public WebSocketClientServiceException(@Nullable String message) {
        super(message);
    }

    public WebSocketClientServiceException(@Nullable String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
