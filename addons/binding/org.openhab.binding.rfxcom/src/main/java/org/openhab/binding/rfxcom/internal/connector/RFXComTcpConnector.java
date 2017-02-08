/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.rfxcom.internal.connector;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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

    private static final Logger logger = LoggerFactory.getLogger(RFXComTcpConnector.class);

    InputStream in = null;
    OutputStream out = null;
    Socket socket = null;
    Thread readerThread = null;

    public RFXComTcpConnector() {
    }

    @Override
    public void connect(RFXComBridgeConfiguration device) throws IOException {
        logger.info("Connecting to RFXCOM at {}:{} over TCP/IP", device.host, device.port);
        socket = new Socket(device.host, device.port);
        in = socket.getInputStream();
        out = socket.getOutputStream();

        out.flush();
        if (in.markSupported()) {
            in.reset();
        }

        readerThread = new RFXComStreamReader(this, in);
        readerThread.start();
    }

    @Override
    public void disconnect() {
        logger.debug("Disconnecting");

        if (readerThread != null) {
            logger.debug("Interrupt stream listener");
            readerThread.interrupt();
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
}
