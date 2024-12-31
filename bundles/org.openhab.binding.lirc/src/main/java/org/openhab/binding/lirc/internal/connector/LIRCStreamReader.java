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
package org.openhab.binding.lirc.internal.connector;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openhab.binding.lirc.internal.LIRCBindingConstants;
import org.openhab.binding.lirc.internal.LIRCResponseException;
import org.openhab.binding.lirc.internal.messages.LIRCButtonEvent;
import org.openhab.binding.lirc.internal.messages.LIRCResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stream reader to parse LIRC output into messages
 *
 * @author Andrew Nagle - Initial contribution
 */
public class LIRCStreamReader extends Thread {

    private final Logger logger = LoggerFactory.getLogger(LIRCStreamReader.class);

    private static final Pattern EVENT_PATTERN = Pattern.compile("^([a-f0-9]+)\\s([a-f0-9]+)\\s(.+)\\s(.+)$");

    private InputStream in;
    private boolean interrupted = false;
    private BufferedReader reader;
    private LIRCConnector connector;

    public LIRCStreamReader(LIRCConnector connector, InputStream in) {
        super(String.format("OH-binding-%s-%s", LIRCBindingConstants.BINDING_ID, "StreamReader"));
        this.connector = connector;
        this.in = in;
    }

    @Override
    public void interrupt() {
        interrupted = true;
        super.interrupt();
    }

    @Override
    public void run() {
        reader = new BufferedReader(new InputStreamReader(in));
        String line;
        String responseText = "";
        while (!interrupted) {
            try {
                line = reader.readLine();
                if (line == null) {
                    throw new EOFException("lost connection");
                } else {
                    logger.trace("Received message: {}", line);
                    Matcher m = EVENT_PATTERN.matcher(line);
                    if (m.matches()) {
                        String code = m.group(1);
                        String repeatsHex = m.group(2);
                        String button = m.group(3);
                        String remote = m.group(4);
                        int repeats = Integer.parseInt(repeatsHex, 16);
                        LIRCButtonEvent buttonMessage = new LIRCButtonEvent(remote, button, repeats, code);
                        connector.sendButtonToListeners(buttonMessage);
                    } else {
                        if ("BEGIN".equals(line)) {
                            responseText = "";
                        } else if ("END".equals(line)) {
                            processResponse(responseText);
                            responseText = null;
                        } else {
                            responseText += line + "\n";
                        }
                    }
                }
            } catch (InterruptedIOException e) {
                Thread.currentThread().interrupt();
                logger.error("Interrupted via InterruptedIOException");
            } catch (EOFException e) {
                logger.error("Lost connection to LIRC server", e);
                connector.sendErrorToListeners(e.getMessage());
                this.interrupt();
            } catch (IOException e) {
                if (!interrupted) {
                    logger.error("Reading from socket failed", e);
                    connector.sendErrorToListeners(e.getMessage());
                }
            } catch (LIRCResponseException e) {
                logger.error("Invalid message received", e);
            }
        }
        try {
            reader.close();
        } catch (IOException e) {
            logger.debug("Error while closing the input stream: {}", e.getMessage());
        }
    }

    private void processResponse(String responseText) throws LIRCResponseException {
        String[] parts = responseText.split("\n");
        String command = parts[0];
        boolean success = true;
        int dataLength = 0;
        String[] data = null;
        if (parts.length > 1) {
            if ("SUCCESS".equals(parts[1]) || "ERROR".equals(parts[1])) {
                success = "SUCCESS".equals(parts[1]);
            } else {
                throw new LIRCResponseException("Malformed response");
            }
        }
        if (parts.length > 2) {
            if ("DATA".equals(parts[2]) && parts.length > 3) {
                dataLength = Integer.parseInt(parts[3]);
            } else {
                throw new LIRCResponseException("Malformed response");
            }
        }
        if (parts.length > 4) {
            data = Arrays.copyOfRange(parts, 4, parts.length);
            if (data.length != dataLength) {
                throw new LIRCResponseException(String
                        .format("Data does not match expected length. Expected: %s, Got: %s", dataLength, data.length));
            }
        }
        LIRCResponse response = new LIRCResponse(command, success, data);
        connector.sendMessageToListeners(response);
    }
}
