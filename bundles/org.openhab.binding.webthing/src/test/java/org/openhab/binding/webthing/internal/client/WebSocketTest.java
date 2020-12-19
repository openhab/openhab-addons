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
import java.time.Duration;
import java.util.concurrent.Executors;

import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 *
 *
 * @author Gregor Roth - Initial contribution
 */
public class WebSocketTest {

    @Disabled
    @Test
    public void testSimple() throws Exception {
        var webSocketClient = new WebSocketClient();
        webSocketClient.start();
        var factory = WebSocketConnectionFactory.instance(webSocketClient);
        var webSocketConnection = factory.create(URI.create("ws://192.168.1.48:9060/"),
                Executors.newScheduledThreadPool(1), error -> {
                }, Duration.ofSeconds(10));

        webSocketConnection.close();
        webSocketClient.stop();
    }
}
