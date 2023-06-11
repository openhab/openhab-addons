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

import java.io.IOException;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.websocket.client.WebSocketClient;

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
     * @param executor the executor to use
     * @param errorHandler the error handler
     * @param pingPeriod the ping period to check the healthiness of the connection
     * @return the newly opened WebSocket connection
     * @throws IOException if the web socket connection can not be established
     */
    WebSocketConnection create(URI webSocketURI, ScheduledExecutorService executor, Consumer<String> errorHandler,
            Duration pingPeriod) throws IOException;

    /**
     * @param webSocketClient the web socket client to use
     * @return the default instance of the factory
     */
    static WebSocketConnectionFactory instance(WebSocketClient webSocketClient) {
        return (webSocketURI, executor, errorHandler, pingPeriod) -> {
            var webSocketConnection = new WebSocketConnectionImpl(executor, errorHandler, pingPeriod);
            webSocketClient.connect(webSocketConnection, webSocketURI);
            return webSocketConnection;
        };
    }
}
