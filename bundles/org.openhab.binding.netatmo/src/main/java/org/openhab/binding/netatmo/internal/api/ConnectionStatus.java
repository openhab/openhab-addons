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

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 *
 * @author Gaël L'hopital - Initial contribution
 *
 */
@NonNullByDefault
public class ConnectionStatus {
    private final boolean isConnected;
    private final String message;

    private ConnectionStatus(boolean isConnected, String message) {
        this.isConnected = isConnected;
        this.message = message;
    }

    public static ConnectionStatus Success() {
        return new ConnectionStatus(true, "Successfully connected");
    }

    public static ConnectionStatus Failed(String message) {
        return new ConnectionStatus(false, message);
    }

    public static ConnectionStatus Failed(String format, Exception e) {
        return new ConnectionStatus(false, String.format(format, e.getMessage()));
    }

    public static ConnectionStatus Unknown() {
        return new ConnectionStatus(false, "No connection tried yet.");
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getMessage() {
        return message;
    }
}
