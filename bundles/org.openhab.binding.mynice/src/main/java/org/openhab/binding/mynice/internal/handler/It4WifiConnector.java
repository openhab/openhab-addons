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
package org.openhab.binding.mynice.internal.handler;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.mynice.internal.xml.dto.CommandType;
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
    private static final int KEEP_ALIVE_DELAY_MIN = 2;
    private static final int SERVER_PORT = 443;
    private static final char ETX = '\u0003';
    private static final char STX = '\u0002';

    private final Logger logger = LoggerFactory.getLogger(It4WifiConnector.class);
    private final It4WifiHandler handler;
    private final ScheduledExecutorService scheduler;
    private final SSLSocket sslsocket;

    private @Nullable InputStreamReader in;
    private @Nullable OutputStreamWriter out;
    private Optional<ScheduledFuture<?>> keepAlive = Optional.empty();

    public It4WifiConnector(String hostname, It4WifiHandler handler, ScheduledExecutorService scheduler) {
        super(It4WifiConnector.class.getName());
        this.handler = handler;
        this.scheduler = scheduler;
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, new TrustManager[] { TrustAllTrustManager.getInstance() }, null);
            sslsocket = (SSLSocket) sslContext.getSocketFactory().createSocket(hostname, SERVER_PORT);
            setDaemon(true);
        } catch (NoSuchAlgorithmException | KeyManagementException | IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private void connect() throws IOException {
        logger.debug("Initiating connection to IT4Wifi on port {}...", SERVER_PORT);
        disconnect();

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

        cancelKeepAlive();

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

    private void cancelKeepAlive() {
        keepAlive.ifPresent(t -> t.cancel(false));
        keepAlive = Optional.empty();
    }

    @Override
    public void run() {
        String buffer = "";
<<<<<<< Upstream, based on main
        try {
            connect();
            handler.handShaked();
            while (!interrupted()) {
                int data;
                while ((data = in.read()) != -1) {
=======
        tryConnect();
        while (!interrupted()) {
            int data;
            try {
                if (pendingMessages > MAX_PENDING_MESSAGES) {
                    throw new IOException("Max keep alive attempts has been reached");
                }
                while (in != null && (data = in.read()) != -1) {
>>>>>>> be8ed80 Removing @NonNullByDefault({}) to better handle NPE
                    cancelKeepAlive();
                    if (data == STX) {
                        buffer = "";
                    } else if (data == ETX) {
                        handler.received(buffer);
                        keepAlive = Optional.of(scheduler.schedule(() -> handler.sendCommand(CommandType.VERIFY),
                                KEEP_ALIVE_DELAY_MIN, TimeUnit.MINUTES));
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
<<<<<<< Upstream, based on main
=======
        handler.connectorInterrupted(message);
    }

    private void tryConnect() {
        String message;
        disconnect();
        connectionAttempts++;
        if (connectionAttempts <= MAX_CONNECTION_ATTEMPTS) {
            try {
                connect();
                handler.handShaked();
                connectionAttempts = 0;
                return;
            } catch (IOException e) {
                message = String.format("Communication error : '%s'.", e.getMessage());
            }
        } else {
            message = "Maximum connection attempts reached.";
        }
        destroyAndExit(message);
>>>>>>> be8ed80 Removing @NonNullByDefault({}) to better handle NPE
    }
}
