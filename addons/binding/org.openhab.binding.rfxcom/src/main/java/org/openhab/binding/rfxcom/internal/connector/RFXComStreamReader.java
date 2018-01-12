/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.connector;

import java.io.IOException;
import java.util.Arrays;

import org.openhab.binding.rfxcom.internal.exceptions.RFXComTimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFXCOM stream reader to parse RFXCOM output into messages.
 *
 * @author Martin van Wingerden - Slight refactoring for read loop for TCP connector
 * @author Mike Jagdis - Interruptible read loop
 * @author James Hewitt-Thomas - New class
 * @author Pauli Anttila - Original read loop
 */
public class RFXComStreamReader extends Thread {
    private final Logger logger = LoggerFactory.getLogger(RFXComStreamReader.class);
    private static final int MAX_READ_TIMEOUTS = 4;

    private RFXComBaseConnector connector;

    private class ExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable throwable) {
            logger.error("Connector died: ", throwable);
        }
    }

    public RFXComStreamReader(RFXComBaseConnector connector) {
        this.connector = connector;
        setUncaughtExceptionHandler(new ExceptionHandler());
    }

    @Override
    public void run() {
        logger.debug("Data listener started");
        byte[] buf = new byte[Byte.MAX_VALUE];

        // The stream has (or SHOULD have) a read timeout set. Taking a
        // read timeout (read returns 0) between packets gives us a chance
        // to check if we've been interrupted. Read interrupts during a
        // packet are ignored but if too many timeouts occur we take it as
        // meaning the RFXCOM has become missing presumed dead.
        try {
            while (!Thread.interrupted()) {
                // First byte tells us how long the packet is
                int bytesRead = connector.read(buf, 0, 1);
                int packetLength = buf[0];

                if (bytesRead > 0 && packetLength > 0) {
                    processMessage(buf, packetLength);
                    connector.sendMsgToListeners(Arrays.copyOfRange(buf, 0, packetLength + 1));
                }
            }
        } catch (IOException | RFXComTimeoutException e) {
            logger.debug("Received exception, will report it to listeners", e);
            connector.sendErrorToListeners(e.getMessage());
        }

        logger.debug("Data listener stopped");
    }

    private void processMessage(byte[] buf, int packetLength) throws IOException, RFXComTimeoutException {
        // Now read the rest of the packet
        int bufferIndex = 1;
        int readTimeoutCount = 1;
        int bytesRead;
        while (bufferIndex <= packetLength) {
            int bytesRemaining = packetLength - bufferIndex + 1;

            bytesRead = connector.read(buf, bufferIndex, bytesRemaining);

            if (bytesRead > 0) {
                bufferIndex += bytesRead;
                readTimeoutCount = 1;
            } else if (readTimeoutCount++ == MAX_READ_TIMEOUTS) {
                throw new RFXComTimeoutException("Timeout during packet read");
            }
        }
    }
}
