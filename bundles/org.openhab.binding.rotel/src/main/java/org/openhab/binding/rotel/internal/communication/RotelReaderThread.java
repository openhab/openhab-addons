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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.rotel.internal.RotelException;
import org.openhab.binding.rotel.internal.protocol.RotelAbstractProtocolHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that reads messages from the Rotel device in a dedicated thread
 *
 * @author Laurent Garnier - Initial contribution
 */
@NonNullByDefault
public class RotelReaderThread extends Thread {

    private static final int READ_BUFFER_SIZE = 16;

    private final Logger logger = LoggerFactory.getLogger(RotelReaderThread.class);

    private final RotelConnector connector;
    private final RotelAbstractProtocolHandler protocolHandler;

    /**
     * Constructor
     *
     * @param connector the connector to read input data
     * @param protocolHandler the protocol handler
     * @param threadName the name of the thread
     */
    public RotelReaderThread(RotelConnector connector, RotelAbstractProtocolHandler protocolHandler,
            String threadName) {
        super(threadName);
        this.connector = connector;
        this.protocolHandler = protocolHandler;
    }

    @Override
    public void run() {
        logger.debug("Data listener started");

        byte[] readDataBuffer = new byte[READ_BUFFER_SIZE];

        try {
            while (!Thread.interrupted()) {
                int len = connector.readInput(readDataBuffer);
                if (len > 0) {
                    protocolHandler.handleIncomingData(readDataBuffer, len);
                }
            }
        } catch (InterruptedIOException e) {
            Thread.currentThread().interrupt();
            logger.debug("Interrupted via InterruptedIOException");
        } catch (RotelException e) {
            logger.debug("Reading failed: {}", e.getMessage(), e);
            protocolHandler.handleInIncomingError();
        }

        logger.debug("Data listener stopped");
    }
}
