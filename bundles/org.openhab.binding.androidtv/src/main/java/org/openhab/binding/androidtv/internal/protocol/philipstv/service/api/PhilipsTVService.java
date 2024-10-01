/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.api;

import java.net.NoRouteToHostException;
import java.util.Optional;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.types.Command;

/**
 * Interface for Philips TV services.
 *
 * @author Benjamin Meyer - Initial contribution
 * @author Ben Rosenblum - Merged into AndroidTV
 */
@NonNullByDefault
public interface PhilipsTVService {

    /**
     * Procedure for sending command.
     *
     * @param channel the channel to which the command applies
     * @param command the command to be handled
     */
    void handleCommand(String channel, Command command);

    default boolean isTvOfflineException(Exception exception) {
        String message = Optional.ofNullable(exception.getMessage()).orElse("");
        if (!message.isEmpty()) {
            if ((exception instanceof NoRouteToHostException) && message.contains("Host unreachable")) {
                return true;
            } else {
                return (exception instanceof ConnectTimeoutException) && message.contains("timed out");
            }
        } else {
            return false;
        }
    }

    default boolean isTvNotListeningException(Exception exception) {
        String message = Optional.ofNullable(exception.getMessage()).orElse("");
        if (!message.isEmpty()) {
            return (exception instanceof HttpHostConnectException) && message.contains("Connection refused");
        } else {
            return false;
        }
    }
}
