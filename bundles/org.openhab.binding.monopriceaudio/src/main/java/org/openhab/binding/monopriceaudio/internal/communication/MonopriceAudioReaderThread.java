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
package org.openhab.binding.monopriceaudio.internal.communication;

import java.io.InterruptedIOException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.monopriceaudio.internal.MonopriceAudioException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that reads messages from the MonopriceAudio device in a dedicated thread
 *
 * @author Michael Lobstein - Adapted for the MonopriceAudio binding
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class MonopriceAudioReaderThread extends Thread {

    private final Logger logger = LoggerFactory.getLogger(MonopriceAudioReaderThread.class);

    private static final int READ_BUFFER_SIZE = 16;

    private MonopriceAudioConnector connector;

    /**
     * Constructor
     *
     * @param connector the object that should handle the received message
     */
    public MonopriceAudioReaderThread(MonopriceAudioConnector connector) {
        this.connector = connector;
    }

    @Override
    public void run() {
        logger.debug("Data listener started");

        final int size = 64;
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
        } catch (MonopriceAudioException e) {
            logger.debug("Reading failed: {}", e.getMessage(), e);
            connector.handleIncomingMessage(MonopriceAudioConnector.READ_ERROR.getBytes());
        }

        logger.debug("Data listener stopped");
    }
}
