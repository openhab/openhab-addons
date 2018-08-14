/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.enocean.internal.transceiver;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.TooManyListenersException;
import java.util.concurrent.ScheduledExecutorService;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;

/**
 *
 * @author Daniel Weber - Initial contribution
 */
public class EnOceanTCPTransceiver extends EnOceanTransceiver {

    private String host;
    private int port;
    private Socket socket;
    private Logger logger = LoggerFactory.getLogger(EnOceanTCPTransceiver.class);

    public EnOceanTCPTransceiver(String host, int port, TransceiverErrorListener errorListener,
            ScheduledExecutorService scheduler) {

        super(errorListener, scheduler);
        this.host = host;
        this.port = port;
    }

    @Override
    public void Initialize() throws UnsupportedCommOperationException, NoSuchPortException, PortInUseException,
            IOException, TooManyListenersException {

        socket = new Socket(host, port);
        socket.setSoTimeout(100); // In ms. Small values mean faster shutdown but more cpu usage.

        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();

        logger.info("EnOceanTCPTransceiver initialized");
    }

    @Override
    public void ShutDown() {

        logger.debug("shutting down TCP transceiver");

        super.ShutDown();

        if (outputStream != null) {
            logger.debug("Closing TCP output stream");
            IOUtils.closeQuietly(outputStream);
        }
        if (inputStream != null) {
            logger.debug("Closeing TCP input stream");
            IOUtils.closeQuietly(inputStream);
        }

        if (socket != null) {
            logger.debug("Closing socket");
            try {
                socket.close();
            } catch (IOException e) {
            }
        }

        socket = null;
        outputStream = null;
        inputStream = null;

        logger.info("TCP Transceiver shutdown");

    }

    @Override
    protected int read(byte[] buffer, int length) {
        try {
            return inputStream.read(buffer, 0, length);
        } catch (SocketTimeoutException e) {
            return 0;
        } catch (IOException e) {
            return 0;
        }
    }
}
