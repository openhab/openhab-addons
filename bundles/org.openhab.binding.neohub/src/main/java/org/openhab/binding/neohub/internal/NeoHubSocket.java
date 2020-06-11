/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.neohub.internal;

import static org.openhab.binding.neohub.internal.NeoHubBindingConstants.*;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

import java.net.InetSocketAddress;
import java.net.Socket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * NeoHubConnector handles the ASCII based communication via TCP between openHAB
 * and NeoHub
 * 
 * @author Sebastian Prehn - Initial contribution
 * @author Andrew Fiddian-Green - Refactoring for openHAB v2.x
 * 
 */
public class NeoHubSocket {

    private final Logger logger = LoggerFactory.getLogger(NeoHubSocket.class);

    /**
     * Name of host or IP to connect to.
     */
    private final String hostname;

    /**
     * The port to connect to
     */
    private final int port;

    public NeoHubSocket(final String hostname, final int portNumber) {
        this.hostname = hostname;
        this.port = portNumber;
    }

    /**
     * sends the message over the network to the NeoHub and returns its response
     * 
     * @param request the message to be sent to the NeoHub
     * @return response received from NeoHub
     * @throws IOException, RuntimeException
     * 
     */
    public synchronized String sendMessage(final String request) throws IOException, NeoHubException {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(hostname, port), TCP_SOCKET_IMEOUT * 1000);

            try (InputStreamReader reader = new InputStreamReader(socket.getInputStream(), US_ASCII);
                    OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), US_ASCII)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("sending {} characters..", request.length());
                    logger.debug(">> {}", request);
                }

                writer.write(request);
                writer.write(0); // NULL terminate the command string
                writer.flush();

                StringBuilder builder = new StringBuilder();
                int inChar;
                while ((inChar = reader.read()) > 0) { // NULL termination & end of stream (-1)
                    builder.append((char) inChar);
                }

                String response = builder.toString();

                if (logger.isTraceEnabled()) {
                    logger.trace("received {} characters..", response.length());
                    logger.trace("<< {}", response);
                } else

                if (logger.isDebugEnabled()) {
                    logger.debug("received {} characters (set log level to TRACE to see full string)..",
                            response.length());
                    logger.debug("<< {} ...", response.substring(1, Math.min(response.length(), 30)));
                }

                if (response.isEmpty()) {
                    throw new NeoHubException("empty response string");
                }

                return response;
            }
        }
    }

}
