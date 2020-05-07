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
package org.openhab.binding.kaleidescape.internal.communication;

import java.io.InterruptedIOException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.kaleidescape.internal.KaleidescapeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that reads messages from the Kaleidescape component in a dedicated thread
 *
 * @author Laurent Garnier - Initial contribution
 * @author Michael Lobstein - Adapted for the Kaleidescape binding
 */
@NonNullByDefault
public class KaleidescapeReaderThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(KaleidescapeReaderThread.class);

    private static final int READ_BUFFER_SIZE = 16;

    private KaleidescapeConnector connector;

    /**
     * Constructor
     *
     * @param connector the object that should handle the received message
     */
    public KaleidescapeReaderThread(KaleidescapeConnector connector) {
        this.connector = connector;
    }

    @Override
    public void run() {
        logger.debug("Data listener started");

        final int size = 512;
        byte[] readDataBuffer = new byte[READ_BUFFER_SIZE];
        byte[] dataBuffer = new byte[size];
        int index = 0;
        final char terminatingChar = '\r';

        try {
            while (!Thread.interrupted()) {
                int len = connector.readInput(readDataBuffer);
                if (len > 0) {
                    for (int i = 0; i < len; i++) {

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
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted via InterruptedIOException");
        } catch (KaleidescapeException e) {
            logger.debug("Reading failed: {}", e.getMessage(), e);
        }

        logger.debug("Data listener stopped");
    }
}
