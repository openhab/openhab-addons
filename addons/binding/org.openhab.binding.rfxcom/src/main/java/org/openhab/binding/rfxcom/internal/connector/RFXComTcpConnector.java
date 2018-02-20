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
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * RFXCOM connector for TCP/IP communication.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Ivan F. Martinez, James Hewitt-Thomas - Implementation
 */
public class RFXComTcpConnector extends RFXComBaseConnector {
    private final Logger logger = LoggerFactory.getLogger(RFXComTcpConnector.class);

    private OutputStream out;
    private Socket socket;

    private Thread readerThread;

    @Override
    public void connect(RFXComBridgeConfiguration device) throws IOException {
        logger.info("Connecting to RFXCOM at {}:{} over TCP/IP", device.host, device.port);
        socket = new Socket(device.host, device.port);
        socket.setSoTimeout(100); // In ms. Small values mean faster shutdown but more cpu usage.
        in = socket.getInputStream();
        out = socket.getOutputStream();

        out.flush();
        if (in.markSupported()) {
            in.reset();
        }

        readerThread = new RFXComStreamReader(this);
        readerThread.start();
    }

    @Override
    public void disconnect() {
        logger.debug("Disconnecting");

        if (readerThread != null) {
            logger.debug("Interrupt stream listener");
            readerThread.interrupt();
            try {
                readerThread.join();
            } catch (InterruptedException e) {
            }
        }

        if (out != null) {
            logger.debug("Close tcp out stream");
            IOUtils.closeQuietly(out);
        }
        if (in != null) {
            logger.debug("Close tcp in stream");
            IOUtils.closeQuietly(in);
        }

        if (socket != null) {
            logger.debug("Close socket");
            IOUtils.closeQuietly(socket);
        }

        readerThread = null;
        socket = null;
        out = null;
        in = null;

        logger.debug("Closed");
    }

    @Override
    public void sendMessage(byte[] data) throws IOException {
        logger.trace("Send data (len={}): {}", data.length, DatatypeConverter.printHexBinary(data));
        out.write(data);
        out.flush();
    }

    @Override
    int read(byte[] buffer, int offset, int length) throws IOException {
        try {
            return super.read(buffer, offset, length);
        } catch (SocketTimeoutException ignore) {
            // ignore this exception, instead return 0 to behave like the serial read
            return 0;
        }
    }
}
