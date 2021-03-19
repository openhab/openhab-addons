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
package org.openhab.binding.moonraker.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EventListener} is called by the {@link MoonrakerWebSocket} on new Events and if the
 * {@link MoonrakerWebSocket}
 * closed the connection.
 *
 * @author Arjan Mels - Initial contribution
 */
@NonNullByDefault
public interface EventListener {

    /**
     * This method is called, whenever a new event comes from the Moonraker service).
     *
     * @param response
     */
    void onEvent(RPCResponse response);

    /**
     * This method is called when the Moonraker websocket services throws an onError.
     *
     * @param cause
     */
    void onError(Throwable cause);

    /**
     * This method is called, when the socket is connected.
     */
    void onConnect();

    /**
     * This method is called, when the evenRunner stops abnormally (statuscode <> 1000).
     */
    void connectionClosed();
}
