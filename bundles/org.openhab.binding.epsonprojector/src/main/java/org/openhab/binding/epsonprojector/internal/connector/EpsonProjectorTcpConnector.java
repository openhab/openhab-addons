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
package org.openhab.binding.epsonprojector.internal.connector;

import static org.openhab.binding.epsonprojector.internal.EpsonProjectorBindingConstants.DEFAULT_PORT;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.epsonprojector.internal.EpsonProjectorException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector for TCP communication.
 *
 * @author Pauli Anttila - Initial contribution
 * @author Michael Lobstein - Improvements for OH3
 */
@NonNullByDefault
public class EpsonProjectorTcpConnector implements EpsonProjectorConnector {
    private static final String ESC_VP_HANDSHAKE = "ESC/VP.net\u0010\u0003\u0000\u0000\u0000\u0000";

    private final Logger logger = LoggerFactory.getLogger(EpsonProjectorTcpConnector.class);
    private final String ip;
    private final int port;

    private @Nullable Socket socket = null;
    private @Nullable InputStream in = null;
    private @Nullable OutputStream out = null;

    public EpsonProjectorTcpConnector(String ip, int port) {
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void connect() throws EpsonProjectorException {
        logger.debug("Open connection to address'{}:{}'", ip, port);

        try {
            Socket socket = new Socket(ip, port);
            this.socket = socket;
            in = socket.getInputStream();
            out = socket.getOutputStream();
        } catch (IOException e) {
            throw new EpsonProjectorException(e);
        }

        // Projectors with built in Ethernet listen on 3629, we must send the handshake to initialize the connection
        if (port == DEFAULT_PORT) {
            try {
                String response = sendMessage(ESC_VP_HANDSHAKE, 5000);
                logger.debug("Response to initialisation of ESC/VP.net is: {}", response);
            } catch (EpsonProjectorException e) {
                logger.debug("Error within initialisation of ESC/VP.net: {}", e.getMessage());
            }
        }
    }

    @Override
    public void disconnect() throws EpsonProjectorException {
        OutputStream out = this.out;

        if (out != null) {
            logger.debug("Close tcp out stream");
            try {
                out.close();
            } catch (IOException e) {
                logger.debug("Error occurred when closing tcp out stream: {}", e.getMessage());
            }
        }

        InputStream in = this.in;
        if (in != null) {
            logger.debug("Close tcp in stream");
            try {
                in.close();
            } catch (IOException e) {
                logger.debug("Error occurred when closing tcp in stream: {}", e.getMessage());
            }
        }

        Socket socket = this.socket;
        if (socket != null) {
            logger.debug("Closing socket");
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("Error occurred when closing tcp socket: {}", e.getMessage());
            }
        }

        this.socket = null;
        this.out = null;
        this.in = null;

        logger.debug("Closed");
    }

    @Override
    public String sendMessage(String data, int timeout) throws EpsonProjectorException {
        InputStream in = this.in;
        OutputStream out = this.out;

        if (in == null || out == null) {
            connect();
            in = this.in;
            out = this.out;
        }

        try {
            if (in != null) {
                // flush input stream
                if (in.markSupported()) {
                    in.reset();
                } else {
                    while (in.available() > 0) {
                        int availableBytes = in.available();

                        if (availableBytes > 0) {
                            byte[] tmpData = new byte[availableBytes];
                            in.read(tmpData, 0, availableBytes);
                        }
                    }
                }
                return sendMmsg(data, timeout);
            } else {
                return "";
            }
        } catch (IOException e) {
            logger.debug("IO error occurred...reconnect and resend once: {}", e.getMessage());
            disconnect();
            connect();

            try {
                return sendMmsg(data, timeout);
            } catch (IOException e1) {
                throw new EpsonProjectorException(e);
            }
        }
    }

    private String sendMmsg(String data, int timeout) throws IOException, EpsonProjectorException {
        String resp = "";

        InputStream in = this.in;
        OutputStream out = this.out;

        if (in != null && out != null) {
            out.write(data.getBytes(StandardCharsets.US_ASCII));
            out.write("\r\n".getBytes(StandardCharsets.US_ASCII));
            out.flush();

            long startTime = System.currentTimeMillis();
            long elapsedTime = 0;

            while (elapsedTime < timeout) {
                int availableBytes = in.available();
                if (availableBytes > 0) {
                    byte[] tmpData = new byte[availableBytes];
                    int readBytes = in.read(tmpData, 0, availableBytes);
                    resp = resp.concat(new String(tmpData, 0, readBytes, StandardCharsets.US_ASCII));

                    if (resp.contains(":")) {
                        return resp;
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        throw new EpsonProjectorException(e);
                    }
                }

                elapsedTime = System.currentTimeMillis() - startTime;
            }
        }
        return resp;
    }
}
