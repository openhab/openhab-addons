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
package org.openhab.binding.androidtv.internal.protocol.philipstv.service.api;

import java.net.NoRouteToHostException;

import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.openhab.core.types.Command;

/**
 * Interface for Philips TV services.
 *
 * @author Benjamin Meyer - Initial contribution
 */
public interface PhilipsTvService {

    /**
     * Procedure for sending command.
     *
     * @param channel the channel to which the command applies
     * @param command the command to be handled
     */
    void handleCommand(String channel, Command command);

    default boolean isTvOfflineException(Exception exception) {
        if ((exception instanceof NoRouteToHostException) && exception.getMessage().contains("Host unreachable")) {
            return true;
        } else {
            return (exception instanceof ConnectTimeoutException) && exception.getMessage().contains("timed out");
        }
    }

    default boolean isTvNotListeningException(Exception exception) {
        return (exception instanceof HttpHostConnectException) && exception.getMessage().contains("Connection refused");
    }
}
