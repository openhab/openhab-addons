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
package org.openhab.binding.gce.internal.handler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Optional;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gce.internal.model.M2MMessageParser;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ipx800DeviceConnector} is responsible for connecting,
 * reading, writing and disconnecting from the Ipx800.
 *
 * @author Seebag - Initial Contribution
 * @author Gaël L'hopital - Ported and adapted for OH2
 */
@NonNullByDefault
public class Ipx800DeviceConnector extends Thread {
    private static final int DEFAULT_SOCKET_TIMEOUT_MS = 5000;
    private static final int DEFAULT_RECONNECT_TIMEOUT_MS = 5000;
    private static final int MAX_KEEPALIVE_FAILURE = 3;
    private static final String ENDL = "\r\n";

    private final Logger logger = LoggerFactory.getLogger(Ipx800DeviceConnector.class);

    private final String hostname;
    private final int portNumber;

    private Optional<M2MMessageParser> messageParser = Optional.empty();
    private Optional<Socket> socket = Optional.empty();
    private Optional<BufferedReader> input = Optional.empty();
    private Optional<PrintWriter> output = Optional.empty();

    private int failedKeepalive = 0;
    private boolean waitingKeepaliveResponse = false;

    public Ipx800DeviceConnector(String hostname, int portNumber, ThingUID uid) {
        super("OH-binding-" + uid);
        this.hostname = hostname;
        this.portNumber = portNumber;
        setDaemon(true);
    }

    public synchronized void send(String message) {
        output.ifPresentOrElse(out -> {
            logger.debug("Sending '{}' to Ipx800", message);
            out.write(message + ENDL);
            out.flush();
        }, () -> logger.warn("Trying to send '{}' while the output stream is closed.", message));
    }

    /**
     * Connect to the ipx800
     *
     * @throws IOException
     */
    private void connect() throws IOException {
        disconnect();

        logger.debug("Connecting to {}:{}...", hostname, portNumber);
        Socket socket = new Socket(hostname, portNumber);
        socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT_MS);
        socket.getInputStream().skip(socket.getInputStream().available());
        this.socket = Optional.of(socket);

        input = Optional.of(new BufferedReader(new InputStreamReader(socket.getInputStream())));
        output = Optional.of(new PrintWriter(socket.getOutputStream(), true));
    }

    /**
     * Disconnect the device
     */
    private void disconnect() {
        logger.debug("Disconnecting");

        input.ifPresent(in -> {
            try {
                in.close();
            } catch (IOException ignore) {
            }
            input = Optional.empty();
        });

        output.ifPresent(PrintWriter::close);
        output = Optional.empty();

        socket.ifPresent(client -> {
            try {
                client.close();
            } catch (IOException ignore) {
            }
            socket = Optional.empty();
        });

        logger.debug("Disconnected");
    }

    /**
     * Stop the device thread
     */
    public void dispose() {
        interrupt();
        disconnect();
    }

    /**
     * Send an arbitrary keepalive command which cause the IPX to send an update.
     * If we don't receive the update maxKeepAliveFailure time, the connection is closed and reopened
     */
    private void sendKeepalive() {
        output.ifPresent(out -> {
            if (waitingKeepaliveResponse) {
                failedKeepalive++;
                logger.debug("Sending keepalive, attempt {}", failedKeepalive);
            } else {
                failedKeepalive = 0;
                logger.debug("Sending keepalive");
            }
            out.println("GetIn01");
            out.flush();
            waitingKeepaliveResponse = true;
        });
    }

    @Override
    public void run() {
        try {
            waitingKeepaliveResponse = false;
            failedKeepalive = 0;
            connect();
            while (!interrupted()) {
                if (failedKeepalive > MAX_KEEPALIVE_FAILURE) {
                    throw new IOException("Max keep alive attempts has been reached");
                }
                input.ifPresent(in -> {
                    try {
                        String command = in.readLine();
                        waitingKeepaliveResponse = false;
                        messageParser.ifPresent(parser -> parser.unsolicitedUpdate(command));
                    } catch (IOException e) {
                        handleException(e);
                    }
                });
            }
            disconnect();
        } catch (IOException e) {
            handleException(e);
        }
        try {
            Thread.sleep(DEFAULT_RECONNECT_TIMEOUT_MS);
        } catch (InterruptedException e) {
            dispose();
        }
    }

    private void handleException(Exception e) {
        if (!interrupted()) {
            if (e instanceof SocketTimeoutException) {
                sendKeepalive();
                return;
            } else if (e instanceof IOException) {
                logger.warn("Communication error: '{}'. Will retry in {} ms", e, DEFAULT_RECONNECT_TIMEOUT_MS);
            }
            messageParser.ifPresent(parser -> parser.errorOccurred(e));
        }
    }

    public void setParser(M2MMessageParser parser) {
        this.messageParser = Optional.of(parser);
    }
}
