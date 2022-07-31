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
package org.openhab.binding.neohub.internal;

import static java.nio.charset.StandardCharsets.US_ASCII;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the ASCII based communication via TCP socket between openHAB and NeoHub
 *
 * @author Sebastian Prehn - Initial contribution
 * @author Andrew Fiddian-Green - Refactoring for openHAB v2.x
 *
 */
@NonNullByDefault
public class NeoHubSocket extends NeoHubSocketBase {

    private final Logger logger = LoggerFactory.getLogger(NeoHubSocket.class);

    public NeoHubSocket(NeoHubConfiguration config) {
        super(config);
    }

    @Override
    public synchronized String sendMessage(final String requestJson) throws IOException, NeoHubException {
        IOException caughtException = null;
        StringBuilder builder = new StringBuilder();

        try (Socket socket = new Socket()) {
            int port = config.portNumber > 0 ? config.portNumber : NeoHubBindingConstants.PORT_TCP;
            socket.connect(new InetSocketAddress(config.hostName, port), config.socketTimeout * 1000);
            socket.setSoTimeout(config.socketTimeout * 1000);

            try (InputStreamReader reader = new InputStreamReader(socket.getInputStream(), US_ASCII);
                    OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), US_ASCII)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("sending {} characters..", requestJson.length());
                    logger.debug(">> {}", requestJson);
                }

                writer.write(requestJson);
                writer.write(0); // NULL terminate the command string
                writer.flush();
                socket.shutdownOutput();

                if (logger.isTraceEnabled()) {
                    logger.trace("sent {} characters..", requestJson.length());
                }

                int inChar;
                boolean done = false;
                // read until end of stream
                while ((inChar = reader.read()) != -1) {
                    // a JSON block is terminated by a newline or NULL
                    if (!(done |= (inChar == '\n') || (inChar == 0))) {
                        builder.append((char) inChar);
                    }
                }
            }
        } catch (IOException e) {
            // catch IOExceptions here, and save them to be re-thrown later
            caughtException = e;
        }

        String responseJson = builder.toString();

        if (logger.isTraceEnabled()) {
            logger.trace("received {} characters..", responseJson.length());
            logger.trace("<< {}", responseJson);
        } else

        if (logger.isDebugEnabled()) {
            logger.debug("received {} characters (set log level to TRACE to see full string)..", responseJson.length());
            logger.debug("<< {} ...", responseJson.substring(0, Math.min(responseJson.length(), 30)));
        }

        // if any type of Exception was caught above, re-throw it again to the caller
        if (caughtException != null) {
            throw caughtException;
        }

        if (responseJson.isEmpty()) {
            throw new NeoHubException("empty response string");
        }

        return responseJson;
    }

    @Override
    public void close() {
        // nothing to do
    }
}
