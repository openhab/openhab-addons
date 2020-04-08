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
package org.openhab.binding.vallox.internal.se.connection;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.vallox.internal.se.ValloxSEConstants;
import org.openhab.binding.vallox.internal.se.configuration.ValloxSEConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ValloxIpConnector} is creates TCP/IP connection to Vallox.
 *
 * @author Miika Jukka - Initial contribution
 */
@NonNullByDefault
public class ValloxIpConnector extends ValloxBaseConnector {

    private final Logger logger = LoggerFactory.getLogger(ValloxIpConnector.class);
    private final Thread readerThread = new TelegramReader("binding.vallox.ValloxIpConnector");
    private final Socket socket = new Socket();

    public ValloxIpConnector(ScheduledExecutorService scheduler) {
        super(scheduler);
        logger.debug("Tcp Connection initialized");
    }

    /**
     * Connect to socket
     */
    @Override
    public void connect(ValloxSEConfiguration config) throws IOException {
        if (isConnected()) {
            return;
        }
        buffer.clear();
        panelNumber = config.getPanelAsByte();
        socket.connect(new InetSocketAddress(config.tcpHost, config.tcpPort), ValloxSEConstants.CONNECTION_TIMEOUT);
        socket.setSoTimeout(ValloxSEConstants.SOCKET_READ_TIMEOUT);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        logger.debug("Connected to {}:{}", config.tcpHost, config.tcpPort);

        readerThread.setDaemon(true);
        readerThread.start();
        connected = true;
    }

    /**
     * Close socket
     */
    @Override
    public void close() {
        super.close();
        logger.debug("Interrupting telegram listener");
        readerThread.interrupt();
        try {
            readerThread.join();
        } catch (InterruptedException e) {
            // Do nothing
        }

        try {
            socket.close();
        } catch (Exception e) {
            logger.debug("Exception closing connection: ", e);
        }
        connected = false;
        logger.debug("Connection closed");
    }

    /**
     * {@link Thread} implementation for reading telegrams
     *
     * @author Miika Jukka - Initial contribution
     */
    private class TelegramReader extends Thread {
        boolean interrupted = false;

        public TelegramReader(String name) {
            super(name);
        }

        @Override
        public void interrupt() {
            interrupted = true;
            super.interrupt();
        }

        @Override
        public void run() {
            logger.debug("Data listener started");
            InputStream inputStream = getInputStream();
            while (!interrupted && inputStream != null) {
                try {
                    while (inputStream.available() > 0) {
                        buffer.add((byte) inputStream.read());
                    }
                    handleBuffer();
                    Thread.sleep(200); // Avoid high CPU usage by sleeping between loops
                } catch (IOException e) {
                    sendErrorToListeners(e.getMessage(), e);
                    interrupt();
                } catch (IllegalStateException e) {
                    logger.warn("Read buffer full. Cleaning.");
                    buffer.clear();
                } catch (InterruptedException e) {
                    sendErrorToListeners("Buffer handling interrupted", e);
                    interrupt();
                }
            }
            logger.debug("Telegram listener stopped");
        }
    }
}
