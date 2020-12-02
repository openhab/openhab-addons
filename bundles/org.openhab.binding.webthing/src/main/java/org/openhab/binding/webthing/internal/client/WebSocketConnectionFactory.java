/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Factory to create new instances of a WebSocket connection
 *
 * @author Gregor Roth - Initial contribution
 */
@NonNullByDefault
interface WebSocketConnectionFactory {

    /**
     * create (and opens) a new WebSocket connection
     *
     * @param webSocketURI the websocket uri
     * @param connectionListener the connection listener to observe the connection state of the WebSocket connection
     * @param pingPeriod the ping period to check the healthiness of the connection
     * @return the newly opened WebSocket connection
     */
    WebSocketConnection create(URI webSocketURI, ConnectionListener connectionListener, Duration pingPeriod);

    /**
     * @return the default instance of the factory
     */
    static WebSocketConnectionFactory instance() {
        return new WebSocketConnectionFactory() {
            @Override
            public WebSocketConnection create(URI webSocketURI, ConnectionListener connectionListener,
                    Duration pingPeriod) {
                var webSocketConnection = new WebSocketConnectionImpl(connectionListener, pingPeriod);
                HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(webSocketURI, webSocketConnection).join();
                return webSocketConnection;
            }
        };
    }
}
