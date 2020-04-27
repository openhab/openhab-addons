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
import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
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

    private @Nullable Future<?> telegramReaderJob;
    private Socket socket = new Socket();

    public ValloxIpConnector() {
        logger.debug("Ip connector initialized");
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
        socket = new Socket();
        socket.connect(new InetSocketAddress(config.tcpHost, config.tcpPort), ValloxSEConstants.CONNECTION_TIMEOUT);
        socket.setSoTimeout(0); // Don't fail if there's nothing to read. Machine is powered down.
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        logger.debug("Connected to {}:{}", config.tcpHost, config.tcpPort);

        startTelegramReaderThread();
        startProcessorJobs();
        connected = true;
    }

    /**
     * Start thread for receiving telegrams
     */
    private void startTelegramReaderThread() {
        telegramReaderJob = executor.submit(this::telegramReader);
    }

    /**
     * Stop thread for receiving telegrams
     */
    private void stopTelegramReaderThread() {
        if (telegramReaderJob != null) {
            telegramReaderJob.cancel(true);
            telegramReaderJob = null;
        }
    }

    /**
     * Close socket and stop threads.
     */
    @Override
    public void close() {
        super.close();
        connected = false;
        logger.debug("Stopping threads and closing socket");
        stopTelegramReaderThread();
        try {
            socket.close();
        } catch (IOException e) {
            logger.debug("Closing socket failed: ", e);
        }

        logger.debug("Ip connection closed");
    }

    /**
     * Read bytes from input stream to buffer
     */
    private void telegramReader() {
        InputStream inputStream = getInputStream();
        while (connected && inputStream != null) {
            try {
                int data = inputStream.read();
                while (data != -1) {
                    buffer.add((byte) data);
                    data = inputStream.read();
                }
            } catch (IOException e) {
                sendErrorToListeners(e.getMessage(), e);
                break;
            } catch (IllegalStateException e) {
                logger.debug("Read buffer full. Cleaning.");
                buffer.clear();
            }
        }
    }
}
