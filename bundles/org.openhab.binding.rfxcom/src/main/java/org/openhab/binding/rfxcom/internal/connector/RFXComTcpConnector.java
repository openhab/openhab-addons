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
package org.openhab.binding.rfxcom.internal.connector;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.openhab.binding.rfxcom.internal.config.RFXComBridgeConfiguration;
import org.openhab.core.util.HexUtils;
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

    private final String readerThreadName;
    private Thread readerThread;

    public RFXComTcpConnector(String readerThreadName) {
        super();
        this.readerThreadName = readerThreadName;
    }

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

        readerThread = new RFXComStreamReader(this, readerThreadName);
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

        if (socket != null) {
            logger.debug("Close socket");
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("Error while closing the socket: {}", e.getMessage());
            }
        }

        readerThread = null;
        socket = null;
        out = null;
        in = null;

        logger.debug("Closed");
    }

    @Override
    public void sendMessage(byte[] data) throws IOException {
        if (logger.isTraceEnabled()) {
            logger.trace("Send data (len={}): {}", data.length, HexUtils.bytesToHex(data));
        }
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
