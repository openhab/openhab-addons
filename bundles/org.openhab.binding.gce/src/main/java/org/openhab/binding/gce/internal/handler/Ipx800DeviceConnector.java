/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link Ipx800DeviceConnector} is responsible for connecting,
 * reading, writing and disconnecting from the Ipx800.
 *
 * @author Seebag - Initial contribution on OH1
 * @author Gaël L'hopital - Ported and adapted for OH2
 */
@NonNullByDefault
public class Ipx800DeviceConnector extends Thread {
    private final Logger logger = LoggerFactory.getLogger(Ipx800DeviceConnector.class);
    private static final int DEFAULT_SOCKET_TIMEOUT = 5000;
    private static final int DEFAULT_RECONNECT_TIMEOUT = 5000;
    private static final int MAX_KEEPALIVE_FAILURE = 3;
    private final static String ENDL = "\r\n";

    private final String hostname;
    public final int portNumber;
    private Optional<Ipx800MessageParser> parser = Optional.empty();

    private boolean interrupted = false;
    private boolean connected = false;

    private @NonNullByDefault({}) Socket client;
    private @NonNullByDefault({}) BufferedReader in;
    private @NonNullByDefault({}) PrintWriter out;

    private int failedKeepalive = 0;
    private boolean waitingKeepaliveResponse = false;

    public Ipx800DeviceConnector(String hostname, int portNumber) {
        this.hostname = hostname;
        this.portNumber = portNumber;
    }

    public synchronized void send(String message) {
        logger.debug("Sending '{}' to Ipx800", message);
        out.write(message + ENDL);
        out.flush();
    }

    /**
     * Connect to the ipx800
     *
     * @throws IOException
     */
    private void connect() throws IOException {
        disconnect();
        logger.debug("Connecting {}:{}...", hostname, portNumber);
        client = new Socket(hostname, portNumber);
        client.setSoTimeout(DEFAULT_SOCKET_TIMEOUT);
        client.getInputStream().skip(client.getInputStream().available());
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        out = new PrintWriter(client.getOutputStream(), true);
        connected = true;
        logger.debug("Connected to {}:{}", hostname, portNumber);
    }

    /**
     * Disconnect the device
     */
    private void disconnect() {
        if (connected) {
            logger.debug("Disconnecting");
            try {
                client.close();
            } catch (IOException e) {
                logger.warn("Unable to disconnect {}", e.getMessage());
            }
            connected = false;
            logger.debug("Disconnected");
        }
    }

    /**
     * Stop the device thread
     */
    public void destroyAndExit() {
        interrupted = true;
        disconnect();
    }

    /**
     * Send an arbitrary keepalive command which cause the IPX to send an update.
     * If we don't receive the update maxKeepAliveFailure time, the connection is closed and reopened
     */
    private void sendKeepalive() {
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
    }

    @Override
    public void run() {
        interrupted = false;
        while (!interrupted) {
            try {
                waitingKeepaliveResponse = false;
                failedKeepalive = 0;
                connect();
                while (!interrupted) {
                    if (failedKeepalive > MAX_KEEPALIVE_FAILURE) {
                        throw new IOException("Max keep alive attempts has been reached");
                    }
                    try {
                        String command = in.readLine();
                        waitingKeepaliveResponse = false;
                        parser.ifPresent(parser -> parser.unsollicitedUpdate(command));
                    } catch (SocketTimeoutException e) {
                        handleException(e);
                    }
                }
                disconnect();
            } catch (IOException e) {
                handleException(e);
            }
            try {
                Thread.sleep(DEFAULT_RECONNECT_TIMEOUT);
            } catch (InterruptedException e) {
                interrupted = true;
                Thread.currentThread().interrupt();
            }
        }
    }

    private void handleException(Exception e) {
        if (e instanceof SocketTimeoutException) {
            sendKeepalive();
            return;
        } else if (e instanceof IOException) {
            logger.info("Communication error : '{}', will retry in {} ms", e, DEFAULT_RECONNECT_TIMEOUT);
        }
        parser.ifPresent(parser -> parser.errorOccurred(e));
        logger.debug(e.getMessage());
    }

    public void setParser(Ipx800MessageParser parser) {
        this.parser = Optional.of(parser);
    }
}
