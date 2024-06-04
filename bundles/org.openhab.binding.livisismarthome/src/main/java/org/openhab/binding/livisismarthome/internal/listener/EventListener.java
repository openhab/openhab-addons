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
package org.openhab.binding.livisismarthome.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link EventListener} is called by the {@link org.openhab.binding.livisismarthome.internal.LivisiWebSocket} on
 * new Events and if the {@link org.openhab.binding.livisismarthome.internal.LivisiWebSocket}
 * closed the connection.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@NonNullByDefault
public interface EventListener {

    /**
     * This method is called, whenever a new event comes from the LIVISI SmartHome service (like a device change for
     * example).
     *
     * @param msg message
     */
    void onEvent(String msg);

    /**
     * This method is called when the LIVISI SmartHome websocket services throws an onError.
     *
     * @param cause cause / throwable
     */
    void onError(Throwable cause);

    /**
     * This method is called, when the evenRunner stops abnormally ({@code statuscode <> 1000}).
     */
    void connectionClosed();
}
