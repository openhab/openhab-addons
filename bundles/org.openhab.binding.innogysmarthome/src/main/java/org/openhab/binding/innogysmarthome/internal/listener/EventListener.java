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
package org.openhab.binding.innogysmarthome.internal.listener;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.innogysmarthome.internal.InnogyWebSocket;

/**
 * The {@link EventListener} is called by the {@link InnogyWebSocket} on new Events and if the {@link InnogyWebSocket}
 * closed the connection.
 *
 * @author Oliver Kuhl - Initial contribution
 */
@NonNullByDefault
public interface EventListener {

    /**
     * This method is called, whenever a new event comes from the innogy service (like a device change for example).
     *
     * @param msg
     */
    void onEvent(String msg);

    /**
     * This method is called when the innogy websocket services throws an onError.
     *
     * @param cause
     */
    void onError(Throwable cause);

    /**
     * This method is called, when the evenRunner stops abnormally (statuscode <> 1000).
     */
    void connectionClosed();
}
