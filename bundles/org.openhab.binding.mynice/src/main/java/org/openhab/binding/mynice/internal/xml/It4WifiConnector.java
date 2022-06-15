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
package org.openhab.binding.mynice.internal.xml;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.mynice.internal.handler.It4WifiHandler;
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
    private final String hostname;
    private final It4WifiHandler handler;
    private final SSLSocketFactory sslsocketfactory;

    private @NonNullByDefault({}) Socket client;
    private @NonNullByDefault({}) InputStreamReader in;
    private @NonNullByDefault({}) OutputStreamWriter out;

    public It4WifiConnector(String hostname, It4WifiHandler handler) {
        super();
        this.hostname = hostname;
        this.handler = handler;

        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { TrustAllTrustManager.getInstance() }, null);
            sslsocketfactory = sslContext.getSocketFactory();
            setDaemon(true);
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void connect() throws IOException {
        disconnect();
        logger.debug("Connecting to {}:{}...", hostname, SERVER_PORT);

        SSLSocket sslsocket = (SSLSocket) sslsocketfactory.createSocket(hostname, SERVER_PORT);
        sslsocket.startHandshake();

        in = new InputStreamReader(sslsocket.getInputStream());
        out = new OutputStreamWriter(sslsocket.getOutputStream());
    }

    public void sendCommand(String command) {
        logger.debug("Sending ItT4Wifi :{}", command);
        try {
            out.write(STX + command + ETX);
            out.flush();
        } catch (IOException e) {
            logger.warn("Exception sending message : {}", e.getMessage());
        }
    }

    private void disconnect() {
        logger.debug("Disconnecting");

        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (client != null) {
                client.close();
            }
        } catch (IOException ignore) {
        }

        in = null;
        out = null;
        client = null;
        logger.debug("Disconnected");
    }

    @Override
    public void run() {
        String buffer = "";
        try {
            connect();
            handler.handShaked();
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
        } catch (IOException e) {
            logger.warn("Communication error : '{}'.", e.getMessage());
            interrupt();
        }
        disconnect();
    }
}
