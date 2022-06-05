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
package org.openhab.binding.panamaxfurman.internal.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.panamaxfurman.internal.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Wraps the communication to a Panamax/Furman Power Conditioner with a BlueBOLT-CV1 or BlueBOLT-CV2 card via telnet
 *
 * @author Dave Badia - Initial contribution
 */
@NonNullByDefault
public class PanamaxFurmanStreamTelnetTransport extends PanamaxFurmanStreamTransport {
    private static final int CONNECT_TIMEOUT_MILLIS = 2_000;
    private static final int INFINITE_TIMEOUT = 0;

    private final Logger logger = LoggerFactory.getLogger(PanamaxFurmanStreamTelnetTransport.class);

    private final String address;
    private final int port;

    private @Nullable Socket telnetSocket;
    private @Nullable InputStream in;
    private @Nullable OutputStream out;

    public PanamaxFurmanStreamTelnetTransport(String address, int port) {
        this.address = address;
        this.port = port;
    }

    @Override
    protected void openConnection() throws IOException {
        synchronized (ioMutex) {
            logger.debug("Attempting to connect to {}:{}", address, port);

            Socket socket = new Socket();
            socket.connect(new InetSocketAddress(address, port), CONNECT_TIMEOUT_MILLIS);
            socket.setKeepAlive(true);
            // Block forever on reads
            socket.setSoTimeout(INFINITE_TIMEOUT);

            try {
                this.in = socket.getInputStream();
                this.out = socket.getOutputStream();
                this.telnetSocket = socket;
                logger.debug("Connected to {}", getConnectionName());
            } catch (IOException e) {
                // should any errors occur, clean everything up
                shutdown();
                throw e;
            }
        }
    }

    @Override
    protected boolean isConnected() {
        synchronized (ioMutex) {
            return this.telnetSocket != null && this.in != null && this.out != null;
        }
    }

    @Override
    protected void closeConnectionAndStreams() {
        synchronized (ioMutex) {
            Util.closeQuietly(in);
            in = null;
            Util.closeQuietly(out);
            out = null;
            Util.closeQuietly(telnetSocket);
            telnetSocket = null;
        }
    }

    @Override
    protected InputStream getInputStream() throws IOException {
        synchronized (ioMutex) {
            InputStream is = this.in; // Local variable required to avoid @Nullable mismatch error
            if (is == null) {
                throw new IllegalStateException("getInputStream() called but we are not connected");
            }
            return is;
        }
    }

    @Override
    protected OutputStream getOutputStream() throws IOException {
        synchronized (ioMutex) {
            OutputStream os = this.out; // Local variable required to avoid @Nullable mismatch error
            if (os == null) {
                throw new IllegalStateException("getOutputStream() called but we are not connected");
            }
            return os;
        }
    }

    @Override
    public String getConnectionName() {
        return address;
    }
}
