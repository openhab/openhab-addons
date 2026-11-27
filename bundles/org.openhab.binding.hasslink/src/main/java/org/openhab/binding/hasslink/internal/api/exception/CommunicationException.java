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
package org.openhab.binding.hasslink.internal.api.exception;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Thrown when the {@link org.openhab.binding.hasslink.internal.api.HomeAssistantWebSocketClient}
 * fails to establish or maintain the WebSocket connection to Home Assistant.
 *
 * @author Jimmy Tanagra - Initial contribution
 */
@NonNullByDefault
public class CommunicationException extends Exception {

    private static final long serialVersionUID = 1L;

    public CommunicationException(String message) {
        super(message);
    }

    public CommunicationException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
