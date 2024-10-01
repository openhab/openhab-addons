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
package org.openhab.binding.powermax.internal.connector;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.powermax.internal.message.PowermaxCommManager;
import org.openhab.binding.powermax.internal.message.PowermaxReceiveType;
import org.openhab.core.util.HexUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that reads messages from the Visonic alarm panel in a dedicated thread
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class PowermaxReaderThread extends Thread {

    private static final int READ_BUFFER_SIZE = 20;
    private static final int MAX_MSG_SIZE = 0xC0;

    private final Logger logger = LoggerFactory.getLogger(PowermaxReaderThread.class);

    private final PowermaxConnector connector;

    /**
     * Constructor
     *
     * @param connector the object that should handle the received message
     * @param threadName the name of the thread
     */
    public PowermaxReaderThread(PowermaxConnector connector, String threadName) {
        super(threadName);
        this.connector = connector;
    }

    @Override
    public void run() {
        logger.info("Data listener started");

        byte[] readDataBuffer = new byte[READ_BUFFER_SIZE];
        byte[] dataBuffer = new byte[MAX_MSG_SIZE];
        int index = 0;
        int msgLen = 0;
        boolean variableLen = false;

        try {
            while (!Thread.interrupted()) {
                int len = connector.read(readDataBuffer);
                if (len > 0) {
                    for (int i = 0; i < len; i++) {
                        if (index >= MAX_MSG_SIZE) {
                            // too many bytes received, try to find new start
                            if (logger.isDebugEnabled()) {
                                byte[] logData = Arrays.copyOf(dataBuffer, index);
                                logger.debug("Truncating message {}", HexUtils.bytesToHex(logData));
                            }
                            index = 0;
                        }

                        if (index == 0 && readDataBuffer[i] == 0x0D) {
                            // Preamble

                            dataBuffer[index++] = readDataBuffer[i];
                        } else if (index > 0) {
                            dataBuffer[index++] = readDataBuffer[i];

                            if (index == 2) {
                                try {
                                    PowermaxReceiveType msgType = PowermaxReceiveType.fromCode(readDataBuffer[i]);
                                    msgLen = msgType.getLength();
                                    variableLen = ((readDataBuffer[i] & 0x000000FF) > 0x10) && (msgLen == 0);
                                } catch (IllegalArgumentException arg0) {
                                    msgLen = 0;
                                    variableLen = false;
                                }
                            } else if (index == 5 && variableLen) {
                                msgLen = (readDataBuffer[i] & 0x000000FF) + 7;
                            } else if ((msgLen == 0 && readDataBuffer[i] == 0x0A) || (index == msgLen)) {
                                // Postamble

                                if (readDataBuffer[i] != 0x0A && dataBuffer[index - 1] == 0x43) {
                                    // adjust message length for 0x43
                                    msgLen++;
                                } else if (checkCRC(dataBuffer, index)) {
                                    // whole message received with a right CRC

                                    byte[] msg = new byte[index];
                                    msg = Arrays.copyOf(dataBuffer, index);

                                    connector.setWaitingForResponse(System.currentTimeMillis());
                                    connector.handleIncomingMessage(msg);

                                    // find new preamble
                                    index = 0;
                                } else if (msgLen == 0) {
                                    // CRC check failed for a message with an unknown length
                                    logger.debug("Message length is now {} but message is apparently not complete",
                                            index + 1);
                                } else {
                                    // CRC check failed for a message with a known length

                                    connector.setWaitingForResponse(System.currentTimeMillis());

                                    // find new preamble
                                    index = 0;
                                }
                            }
                        }
                    }
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted via InterruptedIOException");
        } catch (IOException e) {
            logger.debug("Reading failed: {}", e.getMessage(), e);
            connector.handleCommunicationFailure(e.getMessage());
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : e.toString();
            logger.debug("Error reading or processing message: {}", msg, e);
            connector.handleCommunicationFailure(msg);
        }

        logger.info("Data listener stopped");
    }

    /**
     * Check if the CRC inside a received message is valid or not
     *
     * @param data the buffer containing the message
     * @param len the size of the message in the buffer
     *
     * @return true if the CRC is valid or false if not
     */
    private boolean checkCRC(byte[] data, int len) {
        // Messages of type 0xF1 are always sent with a bad CRC (possible panel bug?)
        if (len == 9 && (data[1] & 0xFF) == 0xF1) {
            return true;
        }

        byte checksum = PowermaxCommManager.computeCRC(data, len);
        byte expected = data[len - 2];
        if (checksum != expected) {
            byte[] logData = Arrays.copyOf(data, len);
            logger.warn("Powermax alarm binding: message CRC check failed (expected {}, got {}, message {})",
                    String.format("%02X", expected), String.format("%02X", checksum), HexUtils.bytesToHex(logData));
        }
        return (checksum == expected);
    }
}
