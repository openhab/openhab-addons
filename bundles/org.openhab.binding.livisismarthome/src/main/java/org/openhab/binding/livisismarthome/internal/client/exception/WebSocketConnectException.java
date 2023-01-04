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
package org.openhab.binding.livisismarthome.internal.client.exception;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Thrown, if the WebSocket couldn't get started / connected.
 *
 * @author Sven Strohschein - Initial contribution
 */
@NonNullByDefault
public class WebSocketConnectException extends IOException {

    private static final long serialVersionUID = -5594715669510573378L;

    public WebSocketConnectException(String message, Throwable cause) {
        super(message, cause);
    }
}
