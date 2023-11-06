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
package org.openhab.binding.webthing.internal.client;

import java.util.function.BiConsumer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The WebsocketConnection represents an open WebSocket connection on the Web Thing. It provides a realtime mechanism
 * to be notified of events as soon as they happen. Refer https://iot.mozilla.org/wot/#web-thing-websocket-api
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
interface WebSocketConnection {

    /**
     * Makes a request for Property value change notifications
     *
     * @param propertyName the property to be observed
     * @param listener the listener to call on changes
     */
    void observeProperty(String propertyName, BiConsumer<String, Object> listener);

    /**
     * closes the WebSocket connection
     */
    void close();

    /**
     * @return true, if connection is alive
     */
    boolean isAlive();
}
