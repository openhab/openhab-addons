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
package org.openhab.binding.rotel.internal.communication;

import java.io.InterruptedIOException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rotel.internal.RotelException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that reads messages from the Rotel device in a dedicated thread
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelReaderThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(RotelReaderThread.class);

    private static final int READ_BUFFER_SIZE = 16;

    private RotelConnector connector;

    /**
     * Constructor
     *
     * @param connector the object that should handle the received message
     * @param threadName the name of the thread
     */
    public RotelReaderThread(RotelConnector connector, String threadName) {
        super(threadName);
        this.connector = connector;
    }

    @Override
    public void run() {
        logger.debug("Data listener started");

        RotelProtocol protocol = connector.getProtocol();
        final int size = (protocol == RotelProtocol.HEX)
                ? (6 + connector.getModel().getRespNbChars() + connector.getModel().getRespNbFlags())
                : 64;
        byte[] readDataBuffer = new byte[READ_BUFFER_SIZE];
        byte[] dataBuffer = new byte[size];
        boolean startCodeReached = false;
        int count = 0;
        int index = 0;
        final char terminatingChar = (protocol == RotelProtocol.ASCII_V1) ? '!' : '$';

        try {
            while (!Thread.interrupted()) {
                int len = connector.readInput(readDataBuffer);
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        if (protocol == RotelProtocol.HEX) {
                            if (readDataBuffer[i] == RotelConnector.START) {
                                startCodeReached = true;
                                count = 0;
                                index = 0;
                            }
                            if (startCodeReached) {
                                if (index < size) {
                                    dataBuffer[index++] = readDataBuffer[i];
                                }
                                if (index == 2) {
                                    count = readDataBuffer[i];
                                } else if ((count > 0) && (index == (count + 3))) {
                                    if ((readDataBuffer[i] & 0x000000FF) == 0x000000FD) {
                                        count++;
                                    } else {
                                        byte[] msg = Arrays.copyOf(dataBuffer, index);
                                        connector.handleIncomingMessage(msg);
                                        startCodeReached = false;
                                    }
                                }
                            }
                        } else {
                            if (index < size) {
                                dataBuffer[index++] = readDataBuffer[i];
                            }
                            if (readDataBuffer[i] == terminatingChar) {
                                if (index >= size) {
                                    dataBuffer[index - 1] = (byte) terminatingChar;
                                }
                                byte[] msg = Arrays.copyOf(dataBuffer, index);
                                connector.handleIncomingMessage(msg);
                                index = 0;
                            }
                        }
                    }
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted via InterruptedIOException");
        } catch (RotelException e) {
            logger.debug("Reading failed: {}", e.getMessage(), e);
            connector.handleIncomingMessage(RotelConnector.READ_ERROR);
        }

        logger.debug("Data listener stopped");
    }
}
