/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.mynice.internal.handler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.io.net.http.TrustAllTrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link It4WifiConnector} is responsible for connecting reading, writing and disconnecting from the It4Wifi.
 *
 * @author Gaël L'hopital - Initial Contribution
 */
@NonNullByDefault
public class It4WifiConnector extends Thread {
    private static final int SERVER_PORT = 443;
    private static final char ETX = '\u0003';
    private static final char STX = '\u0002';

    private final Logger logger = LoggerFactory.getLogger(It4WifiConnector.class);
    private final It4WifiHandler handler;
    private final SSLSocket sslsocket;

    private @NonNullByDefault({}) InputStreamReader in;
    private @NonNullByDefault({}) OutputStreamWriter out;

    public It4WifiConnector(String hostname, It4WifiHandler handler) {
        super(It4WifiConnector.class.getName());
        this.handler = handler;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { TrustAllTrustManager.getInstance() }, null);
            sslsocket = (SSLSocket) sslContext.getSocketFactory().createSocket(hostname, SERVER_PORT);
            setDaemon(true);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public void run() {
        String buffer = "";
        try {
            connect();
            while (!interrupted()) {
                int data;
                while ((data = in.read()) != -1) {
                    if (data == STX) {
                        buffer = "";
                    } else if (data == ETX) {
                        handler.received(buffer);
                    } else {
                        buffer += (char) data;
                    }
                }
            }
            handler.connectorInterrupted("IT4WifiConnector interrupted");
            dispose();
        } catch (IOException e) {
            handler.connectorInterrupted(e.getMessage());
        }
    }

    public synchronized void sendCommand(String command) {
        logger.debug("Sending ItT4Wifi :{}", command);
        try {
            out.write(STX + command + ETX);
            out.flush();
        } catch (IOException e) {
            handler.connectorInterrupted(e.getMessage());
        }
    }

    private void disconnect() {
        logger.debug("Disconnecting");

        if (in != null) {
            try {
                in.close();
            } catch (IOException ignore) {
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (IOException ignore) {
            }
        }

        in = null;
        out = null;

        logger.debug("Disconnected");
    }

    /**
     * Stop the device thread
     *
     * @throws IOException
     */
    public void dispose() {
        interrupt();
        disconnect();
        try {
            sslsocket.close();
        } catch (IOException e) {
            logger.warn("Error closing sslsocket : {}", e.getMessage());
        }
    }

    private void connect() throws IOException {
        disconnect();
        logger.debug("Initiating connection to IT4Wifi on port {}...", SERVER_PORT);

        sslsocket.startHandshake();
        in = new InputStreamReader(sslsocket.getInputStream());
        out = new OutputStreamWriter(sslsocket.getOutputStream());
        handler.handShaked();
    }
}
