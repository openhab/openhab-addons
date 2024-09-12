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
package org.openhab.binding.gardena.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link GardenaSmartWebSocketListener} is called by the {@link GardenaSmartWebSocket} on new Events and if the
 * {@link GardenaSmartWebSocket}
 * closed the connection.
 *
 * @author Gerhard Riegler - Initial contribution
 */
@NonNullByDefault
public interface GardenaSmartWebSocketListener {
    /**
     * This method is called, when the evenRunner stops abnormally ({@code statuscode <> 1000}).
     */
    void onWebSocketClose(String id);

    /**
     * This method is called when the Gardena websocket services throws an onError.
     */
    void onWebSocketError(String id);

    /**
     * This method is called, whenever a new event comes from the Gardena service.
     *
     * @param msg
     */
    void onWebSocketMessage(String msg);
}
