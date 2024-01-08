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
package org.openhab.binding.heos.internal.resources;

import java.io.IOException;

import org.openhab.binding.heos.internal.json.HeosJsonParser;
import org.openhab.binding.heos.internal.json.dto.HeosResponseObject;
import org.openhab.binding.heos.internal.resources.Telnet.ReadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link HeosSendCommand} is responsible to send a command
 * to the HEOS bridge
 *
 * @author Johannes Einig - Initial contribution
 */
public class HeosSendCommand {
    private final Logger logger = LoggerFactory.getLogger(HeosSendCommand.class);

    private final Telnet client;
    private final HeosJsonParser parser = new HeosJsonParser();

    public HeosSendCommand(Telnet client) {
        this.client = client;
    }

    public <T> HeosResponseObject<T> send(String command, Class<T> clazz) throws IOException, ReadException {
        HeosResponseObject<T> result;
        int attempt = 0;

        boolean send = client.send(command);
        if (clazz == null) {
            return null;
        } else if (send) {
            String line = client.readLine();
            if (line == null) {
                throw new IOException("No valid input was received");
            }
            result = parser.parseResponse(line, clazz);

            while (!result.isFinished() && attempt < 3) {
                attempt++;
                logger.trace("Retrying \"{}\" (attempt {})", command, attempt);
                line = client.readLine(15000);

                if (line != null) {
                    result = parser.parseResponse(line, clazz);
                }
            }

            if (attempt >= 3 && !result.isFinished()) {
                throw new IOException("No valid input was received after multiple attempts");
            }

            return result;
        } else {
            throw new IOException("Not connected");
        }
    }

    public boolean isHostReachable() {
        return client.isHostReachable();
    }

    public boolean isConnected() {
        return client.isConnected();
    }

    public void stopInputListener(String registerChangeEventOFF) {
        logger.debug("Stopping HEOS event line listener");
        client.stopInputListener();

        if (client.isConnected()) {
            try {
                client.send(registerChangeEventOFF);
            } catch (IOException e) {
                logger.debug("Failure during closing connection to HEOS with message: {}", e.getMessage());
            }
        }
    }

    public void disconnect() {
        if (client.isConnected()) {
            return;
        }

        try {
            logger.debug("Disconnecting HEOS command line");
            client.disconnect();
        } catch (IOException e) {
            logger.debug("Failure during closing connection to HEOS with message: {}", e.getMessage());
        }

        logger.debug("Connection to HEOS system closed");
    }

    public void startInputListener(String command) throws IOException, ReadException {
        HeosResponseObject<Void> response = send(command, Void.class);
        if (response.result) {
            client.startInputListener();
        }
    }
}
