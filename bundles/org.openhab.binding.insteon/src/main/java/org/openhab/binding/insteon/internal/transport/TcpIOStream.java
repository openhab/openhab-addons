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
package org.openhab.binding.insteon.internal.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements IOStream for an Insteon Legacy Hub
 * Also works for serial ports exposed via tcp, eg. ser2net
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Rewrite insteon binding
 */
@NonNullByDefault
public class TcpIOStream extends IOStream {
    private final Logger logger = LoggerFactory.getLogger(TcpIOStream.class);

    private String host;
    private int port;
    private @Nullable Socket socket;

    /**
     * Constructor
     *
     * @param host host name of hub device
     * @param port port to connect to
     */
    public TcpIOStream(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public boolean isOpen() {
        return socket != null;
    }

    @Override
    public boolean open() {
        if (isOpen()) {
            logger.warn("socket is already open");
            return false;
        }

        try {
            Socket socket = new Socket(host, port);
            this.in = socket.getInputStream();
            this.out = socket.getOutputStream();
            this.socket = socket;
            return true;
        } catch (UnknownHostException e) {
            logger.warn("unknown host name: {}", host);
        } catch (IOException e) {
            logger.warn("cannot open connection to {} port {}: {}", host, port, e.getMessage());
        }

        return false;
    }

    @Override
    public void close() {
        InputStream in = this.in;
        if (in != null) {
            try {
                in.close();
            } catch (IOException e) {
                logger.debug("failed to close input stream", e);
            }
            this.in = null;
        }

        OutputStream out = this.out;
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                logger.debug("failed to close output stream", e);
            }
            this.out = null;
        }

        Socket socket = this.socket;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("failed to close the socket", e);
            }
            this.socket = null;
        }
    }
}
