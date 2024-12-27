/**
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Optional;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.gce.internal.model.M2MMessageParser;
import org.openhab.binding.gce.internal.model.PortDefinition;
import org.openhab.binding.gce.internal.model.StatusFile;
import org.openhab.binding.gce.internal.model.StatusFileAccessor;
import org.openhab.core.thing.ThingUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * The {@link Ipx800DeviceConnector} is responsible for connecting,
 * reading, writing and disconnecting from the Ipx800.
 *
 * @author Seebag - Initial Contribution
 * @author Gaël L'hopital - Ported and adapted for OH2
 */
@NonNullByDefault
public class Ipx800DeviceConnector extends Thread {
    private static final int DEFAULT_SOCKET_TIMEOUT_MS = 10000;
    private static final int DEFAULT_RECONNECT_TIMEOUT_MS = 5000;
    private static final int MAX_KEEPALIVE_FAILURE = 3;

    private final Logger logger = LoggerFactory.getLogger(Ipx800DeviceConnector.class);
    private final Random randomizer = new Random();

    private final String hostname;
    private final int portNumber;
    private final M2MMessageParser parser;
    private final StatusFileAccessor statusAccessor;
    private final Ipx800EventListener listener;

    private Optional<Socket> socket = Optional.empty();
    private Optional<BufferedReader> input = Optional.empty();
    private Optional<PrintWriter> output = Optional.empty();

    private int failedKeepalive = 0;
    private boolean waitingKeepaliveResponse = false;

    public Ipx800DeviceConnector(String hostname, int portNumber, ThingUID uid, Ipx800EventListener listener) {
        super("OH-binding-" + uid);
        this.hostname = hostname;
        this.portNumber = portNumber;
        this.listener = listener;
        this.parser = new M2MMessageParser(listener);
        this.statusAccessor = new StatusFileAccessor(hostname);
        setDaemon(true);
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
        // socket.getInputStream().skip(socket.getInputStream().available());
        this.socket = Optional.of(socket);

        output = Optional.of(new PrintWriter(socket.getOutputStream(), true));
        input = Optional.of(new BufferedReader(new InputStreamReader(socket.getInputStream())));
    }

    /**
     * Disconnect the device
     */
    private void disconnect() {
        socket.ifPresent(client -> {
            try {
                logger.debug("Closing socket");
                client.close();
            } catch (IOException ignore) {
            }
            socket = Optional.empty();
            input = Optional.empty();
            output = Optional.empty();
        });
    }

    /**
     * Stop the device thread
     */
    public void dispose() {
        interrupt();
        disconnect();
    }

    public synchronized void send(String message) {
        output.ifPresentOrElse(out -> {
            logger.debug("Sending '{}' to Ipx800", message);
            out.println(message);
        }, () -> logger.warn("Unable to send '{}' when the output stream is closed.", message));
    }

    /**
     * Send an arbitrary keepalive command which cause the IPX to send an update.
     * If we don't receive the update maxKeepAliveFailure time, the connection is closed and reopened
     */
    private void sendKeepalive() {
        output.ifPresentOrElse(out -> {
            PortDefinition pd = PortDefinition.values()[randomizer.nextInt(PortDefinition.AS_SET.size())];
            String command = "%s%d".formatted(pd.m2mCommand, randomizer.nextInt(pd.quantity) + 1);

            if (waitingKeepaliveResponse) {
                failedKeepalive++;
                logger.debug("Sending keepalive {}, attempt {}", command, failedKeepalive);
            } else {
                failedKeepalive = 0;
                logger.debug("Sending keepalive {}", command);
            }

            out.println(command);
            parser.setExpectedResponse(command);

            waitingKeepaliveResponse = true;
        }, () -> logger.warn("Unable to send keepAlive when the output stream is closed."));
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
                        parser.unsolicitedUpdate(command);
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
            } else if (e instanceof SocketException) {
                logger.debug("SocketException raised by streams while closing socket");
            } else if (e instanceof IOException) {
                logger.warn("Communication error: '{}'. Will retry in {} ms", e, DEFAULT_RECONNECT_TIMEOUT_MS);
            }
            listener.errorOccurred(e);
        }
    }

    public StatusFile readStatusFile() throws SAXException, IOException {
        return statusAccessor.read();
    }

    /**
     * Set output of the device sending the corresponding command
     *
     * @param targetPort
     * @param targetValue
     */
    public void setOutput(String targetPort, int targetValue, boolean pulse) {
        logger.debug("Sending {} to {}", targetValue, targetPort);
        String command = "Set%02d%s%s".formatted(Integer.parseInt(targetPort), targetValue, pulse ? "p" : "");
        send(command);
    }

    /**
     * Resets the counter value to 0
     *
     * @param targetCounter
     */
    public void resetCounter(int targetCounter) {
        logger.debug("Resetting counter {} to 0", targetCounter);
        send("ResetCount%d".formatted(targetCounter));
    }

    public void resetPLC() {
        send("Reset");
    }

}
