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
package org.openhab.binding.nuvo.internal.communication;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.nuvo.internal.NuvoException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that reads messages from the Nuvo device in a dedicated thread
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the Nuvo binding
 */
@NonNullByDefault
public class NuvoReaderThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(NuvoReaderThread.class);

    private static final int READ_BUFFER_SIZE = 16;
    private static final int SIZE = 256;

    private static final char TERM_CHAR = '\r';

    private NuvoConnector connector;

    /**
     * Constructor
     *
     * @param connector the object that should handle the received message
     * @param uid the thing uid string
     * @param connectionId a string that uniquely identifies the particular connection
     */
    public NuvoReaderThread(NuvoConnector connector, String uid, String connectionId) {
        super("OH-binding-" + uid + "-" + connectionId);
        this.connector = connector;
        setDaemon(true);
    }

    @Override
    public void run() {
        logger.debug("Data listener started");

        byte[] readDataBuffer = new byte[READ_BUFFER_SIZE];
        byte[] dataBuffer = new byte[SIZE];
        int index = 0;

        try {
            while (!Thread.interrupted()) {
                int len = connector.readInput(readDataBuffer);
                if (len > 0) {
                    for (int i = 0; i < len; i++) {

                        if (index < SIZE) {
                            dataBuffer[index++] = readDataBuffer[i];
                        }
                        if (readDataBuffer[i] == TERM_CHAR) {
                            if (index >= SIZE) {
                                dataBuffer[index - 1] = (byte) TERM_CHAR;
                            }
                            byte[] msg = Arrays.copyOf(dataBuffer, index);
                            connector.handleIncomingMessage(msg);
                            index = 0;
                        }

                    }
                }
            }
        } catch (NuvoException e) {
            logger.debug("Reading failed: {}", e.getMessage(), e);
            connector.handleIncomingMessage(NuvoConnector.COMMAND_ERROR.getBytes());
        }

        logger.debug("Data listener stopped");
    }
}
