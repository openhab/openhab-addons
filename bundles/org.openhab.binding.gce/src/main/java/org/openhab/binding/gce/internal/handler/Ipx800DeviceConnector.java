/*
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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
    private static final int MAX_KEEPALIVE_FAILURE = 3;

    private final Logger logger = LoggerFactory.getLogger(Ipx800DeviceConnector.class);
    private final Random randomizer = new Random();

    private final M2MMessageParser parser;
    private final StatusFileAccessor statusAccessor;
    private final Ipx800EventListener listener;
    private final Socket socket;
    private final BufferedReader input;
    private final PrintWriter output;

    private int failedKeepalive = 0;
    private boolean waitingKeepaliveResponse = false;
    private boolean interrupted = false;

    public Ipx800DeviceConnector(String hostname, int portNumber, ThingUID uid, Ipx800EventListener listener)
            throws UnknownHostException, IOException {
        super("OH-binding-" + uid);
        this.listener = listener;

        logger.debug("Connecting to {}:{}...", hostname, portNumber);
        Socket socket = new Socket(hostname, portNumber);
        socket.setSoTimeout(DEFAULT_SOCKET_TIMEOUT_MS);
        this.socket = socket;

        output = new PrintWriter(socket.getOutputStream(), true);
        input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        parser = new M2MMessageParser(listener);
        statusAccessor = new StatusFileAccessor(hostname);
        setDaemon(true);
    }

    /**
     *
     * Stop the device thread
     */

    public void dispose() {
        interrupted = true;
    }

    public synchronized void send(String message) {
        logger.debug("Sending '{}' to Ipx800", message);
        output.println(message);
    }

    /**
     *
     * Send a random keepalive command which cause the IPX to send an update.
     * If we don't receive the update maxKeepAliveFailure time, the connection
     * is closed
     */

    private void sendKeepalive() {
        PortDefinition pd = PortDefinition.values()[randomizer.nextInt(PortDefinition.AS_SET.size())];
        String command = "%s%d".formatted(pd.m2mCommand, randomizer.nextInt(pd.quantity) + 1);

        if (waitingKeepaliveResponse) {
            failedKeepalive++;
            logger.debug("Sending keepalive {}, attempt {}", command, failedKeepalive);
        } else {
            failedKeepalive = 0;
            logger.debug("Sending keepalive {}", command);
        }

        output.println(command);
        parser.setExpectedResponse(command);

        waitingKeepaliveResponse = true;
    }

    @Override
    public void run() {
        while (!interrupted) {
            if (failedKeepalive > MAX_KEEPALIVE_FAILURE) {
                interrupted = true;
                listener.errorOccurred(new IOException("Max keep alive attempts has been reached"));
            }
            try {
                String command = input.readLine();
                waitingKeepaliveResponse = false;
                parser.unsolicitedUpdate(command);
            } catch (SocketTimeoutException e) {
                sendKeepalive();
            } catch (IOException e) {
                interrupted = true;
                listener.errorOccurred(e);
            }
        }
        if (output instanceof PrintWriter out) {
            out.close();
        }

        if (input instanceof BufferedReader in) {
            try {
                in.close();
            } catch (IOException e) {
                logger.warn("Exception input stream: {}", e.getMessage());
            }
        }

        if (socket instanceof Socket client) {
            try {
                logger.debug("Closing socket");
                client.close();
            } catch (IOException e) {
                logger.warn("Exception closing socket: {}", e.getMessage());
            }
        }
    }

    public StatusFile readStatusFile() throws SAXException, IOException {
        return statusAccessor.read();
    }

    /**
     *
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
     *
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
