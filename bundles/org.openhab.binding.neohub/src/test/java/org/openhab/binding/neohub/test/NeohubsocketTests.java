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
package org.openhab.binding.neohub.test;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.neohub.internal.NeoHubBindingConstants;
import org.openhab.binding.neohub.internal.NeoHubConfiguration;
import org.openhab.binding.neohub.internal.NeoHubSocket;
import org.openhab.binding.neohub.internal.NeoHubWebSocket;

/**
 * JUnit for testing web and TCP sockets.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 *
 */
@NonNullByDefault
public class NeohubsocketTests {

    private boolean runOnline = false;

    @SuppressWarnings("null")
    @Test
    void testWebSocket() {
        if (runOnline) {
            // create the web socket class
            NeoHubConfiguration config = new NeoHubConfiguration();
            config.hostName = "192.168.1.xxx";
            config.socketTimeout = 5;
            config.apiToken = "12345678-1234-1234-1234-123456789ABC";

            // use web socket to send the request, and log the response
            try {
                config.portNumber = NeoHubBindingConstants.PORT_WSS;
                NeoHubWebSocket socket = new NeoHubWebSocket(config);
                String requestJson = NeoHubBindingConstants.CMD_CODE_FIRMWARE;
                System.out.println(requestJson);
                String responseJson = socket.sendMessage(requestJson);
                System.out.println(responseJson);
                socket.close();
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }

            // use TCP socket to send the request, and log the response
            try {
                config.portNumber = NeoHubBindingConstants.PORT_TCP;
                NeoHubSocket socket = new NeoHubSocket(config);
                String requestJson = NeoHubBindingConstants.CMD_CODE_FIRMWARE;
                System.out.println(requestJson);
                String responseJson = socket.sendMessage(requestJson);
                System.out.println(responseJson);
                socket.close();
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }
    }
}
