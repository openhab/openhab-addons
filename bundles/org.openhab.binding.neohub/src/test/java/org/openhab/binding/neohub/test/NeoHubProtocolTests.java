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
package org.openhab.binding.neohub.test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.neohub.internal.NeoHubBindingConstants;
import org.openhab.binding.neohub.internal.NeoHubConfiguration;
import org.openhab.binding.neohub.internal.NeoHubException;
import org.openhab.binding.neohub.internal.NeoHubSocket;
import org.openhab.binding.neohub.internal.NeoHubWebSocket;
import org.openhab.core.io.net.http.WebSocketFactory;
import org.openhab.core.thing.ThingUID;

/**
 * JUnit for testing WSS and TCP socket protocols.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class NeoHubProtocolTests {

    /**
     * Test online communication. Requires an actual Neohub to be present on the LAN. Configuration parameters must be
     * entered for the actual specific Neohub instance as follows:
     *
     * - HUB_IP_ADDRESS the dotted ip address of the hub
     * - HUB_API_TOKEN the api access token for the hub
     * - SOCKET_TIMEOUT the connection time out
     * - RUN_WSS_TEST enable testing the WSS communication
     * - RUN_TCP_TEST enable testing the TCP communication
     *
     * NOTE: only run these tests if a device is actually available
     *
     */
    private static final String HUB_IP_ADDRESS = "192.168.1.xxx";
    private static final String HUB_API_TOKEN = "12345678-1234-1234-1234-123456789ABC";
    private static final int SOCKET_TIMEOUT = 5;
    private static final boolean RUN_WSS_TEST = false;
    private static final boolean RUN_TCP_TEST = false;

    /**
     * Use web socket to send a request, and check for a response.
     *
     * @throws NeoHubException
     * @throws IOException
     */
    @Test
    void testWssConnection() throws NeoHubException, IOException {
        if (RUN_WSS_TEST) {
            if (!NeoHubJsonTests.VALID_IP_V4_ADDRESS.matcher(HUB_IP_ADDRESS).matches()) {
                fail();
            }

            NeoHubConfiguration config = new NeoHubConfiguration();
            config.hostName = HUB_IP_ADDRESS;
            config.socketTimeout = SOCKET_TIMEOUT;
            config.apiToken = HUB_API_TOKEN;

            SslContextFactory.Client sslContextFactory = new SslContextFactory.Client();
            sslContextFactory.setTrustAll(true);
            HttpClient httpClient = new HttpClient(sslContextFactory);
            WebSocketClient webSocketClient = new WebSocketClient(httpClient);

            WebSocketFactory webSocketFactory = mock(WebSocketFactory.class);
            when(webSocketFactory.createWebSocketClient(anyString(), any())).thenReturn(webSocketClient);

            NeoHubWebSocket socket = new NeoHubWebSocket(config, webSocketFactory, new ThingUID("neohub:account:test"));
            String requestJson = NeoHubBindingConstants.CMD_CODE_FIRMWARE;
            String responseJson = socket.sendMessage(requestJson);
            assertNotEquals(0, responseJson.length());
            socket.close();
        }
    }

    /**
     * Use TCP socket to send a request, and check for a response.
     *
     * @throws NeoHubException
     * @throws IOException
     */
    @Test
    void testTcpConnection() throws IOException, NeoHubException {
        if (RUN_TCP_TEST) {
            if (!NeoHubJsonTests.VALID_IP_V4_ADDRESS.matcher(HUB_IP_ADDRESS).matches()) {
                fail();
            }

            NeoHubConfiguration config = new NeoHubConfiguration();
            config.hostName = HUB_IP_ADDRESS;
            config.socketTimeout = SOCKET_TIMEOUT;
            config.apiToken = HUB_API_TOKEN;

            NeoHubSocket socket = new NeoHubSocket(config, "test");
            String requestJson = NeoHubBindingConstants.CMD_CODE_FIRMWARE;
            String responseJson = socket.sendMessage(requestJson);
            assertNotEquals(0, responseJson.length());
            socket.close();
        }
    }
}
