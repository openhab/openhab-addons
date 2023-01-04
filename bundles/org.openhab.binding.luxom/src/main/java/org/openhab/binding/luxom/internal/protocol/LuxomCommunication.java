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
package org.openhab.binding.luxom.internal.protocol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.luxom.internal.handler.LuxomBridgeHandler;
import org.openhab.binding.luxom.internal.handler.LuxomConnectionException;
import org.openhab.binding.luxom.internal.handler.config.LuxomBridgeConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link LuxomCommunication} class is able to do the following tasks with Luxom IP
 * systems:
 * <ul>
 * <li>Start and stop TCP socket connection with Luxom IP-interface.
 * <li>Read all setup and status information from the Luxom Controller.
 * <li>Execute Luxom commands.
 * <li>Listen to events from Luxom.
 * </ul>
 *
 * @author Kris Jespers - Initial contribution
 */
@NonNullByDefault
public class LuxomCommunication {

    private final Logger logger = LoggerFactory.getLogger(LuxomCommunication.class);

    private final LuxomBridgeHandler bridgeHandler;

    private @Nullable Socket luxomSocket;
    private @Nullable PrintWriter luxomOut;
    private @Nullable BufferedReader luxomIn;

    private volatile boolean listenerStopped;
    private volatile boolean stillListeningToEvents;

    public LuxomCommunication(LuxomBridgeHandler luxomBridgeHandler) {
        super();
        bridgeHandler = luxomBridgeHandler;
    }

    public synchronized void startCommunication() throws LuxomConnectionException {
        try {
            waitForEventListenerThreadToStop();

            initializeSocket();

            // Start Luxom event listener. This listener will act on all messages coming from
            // IP-interface.
            (new Thread(this::runLuxomEvents,
                    "OH-binding-" + bridgeHandler.getThing().getBridgeUID() + "-listen-for-events")).start();

        } catch (IOException | InterruptedException e) {
            throw new LuxomConnectionException(e);
        }
    }

    private void waitForEventListenerThreadToStop() throws InterruptedException, IOException {
        for (int i = 1; stillListeningToEvents && (i <= 5); i++) {
            // the events listener thread did not finish yet, so wait max 5000ms before restarting
            // noinspection BusyWait
            Thread.sleep(1000);
        }
        if (stillListeningToEvents) {
            throw new IOException("starting but previous connection still active after 5000ms");
        }
    }

    private void initializeSocket() throws IOException {
        LuxomBridgeConfig luxomBridgeConfig = bridgeHandler.getIPBridgeConfig();
        if (luxomBridgeConfig != null) {
            InetAddress addr = InetAddress.getByName(luxomBridgeConfig.ipAddress);
            int port = luxomBridgeConfig.port;

            luxomSocket = new Socket(addr, port);
            luxomSocket.setReuseAddress(true);
            luxomSocket.setKeepAlive(true);
            luxomOut = new PrintWriter(luxomSocket.getOutputStream());
            luxomIn = new BufferedReader(new InputStreamReader(luxomSocket.getInputStream()));
            logger.debug("Luxom: connected via local port {}", luxomSocket.getLocalPort());
        } else {
            logger.warn("Luxom: ip bridge not initialized");
        }
    }

    /**
     * Cleanup socket when the communication with Luxom IP-interface is closed.
     */
    public synchronized void stopCommunication() {
        listenerStopped = true;

        closeSocket();
    }

    private void closeSocket() {
        if (luxomSocket != null) {
            try {
                luxomSocket.close();
            } catch (IOException ignore) {
                // ignore IO Error when trying to close the socket if the intention is to close it anyway
            }
        }
        luxomSocket = null;

        logger.debug("Luxom: communication stopped");
    }

    /**
     * Method that handles inbound communication from Luxom, to be called on a separate thread.
     * <p>
     * The thread listens to the TCP socket opened at instantiation of the {@link LuxomCommunication} class
     * and interprets all inbound json messages. It triggers state updates for active channels linked to the Niko Home
     * Control actions. It is started after initialization of the communication.
     */
    private void runLuxomEvents() {
        StringBuilder luxomMessage = new StringBuilder();

        logger.debug("Luxom: listening for events");
        listenerStopped = false;
        stillListeningToEvents = true;

        try {
            boolean mayUseFastReconnect = false;
            boolean mustDoFullReconnect = false;
            while (!listenerStopped && (luxomIn != null)) {
                int nextChar = luxomIn.read();
                if (nextChar == -1) {
                    logger.trace("Luxom: stream ends unexpectedly...");
                    LuxomBridgeConfig luxomBridgeConfig = bridgeHandler.getIPBridgeConfig();
                    if (mayUseFastReconnect && luxomBridgeConfig != null && luxomBridgeConfig.useFastReconnect) {
                        // we stay in the loop and just reinitialize socket
                        mayUseFastReconnect = false; // just once use fast reconnect
                        this.closeSocket();
                        this.initializeSocket();
                        // followed by forced update of status
                        bridgeHandler.forceRefreshThings();
                    } else {
                        listenerStopped = true;
                        mustDoFullReconnect = true;
                    }
                } else {
                    mayUseFastReconnect = true; // reset
                    char c = (char) nextChar;
                    logger.trace("Luxom: read char {}", c);

                    luxomMessage.append(c);

                    if (';' == c) {
                        String message = luxomMessage.toString();
                        bridgeHandler.handleIncomingLuxomMessage(message.substring(0, message.length() - 1));
                        luxomMessage = new StringBuilder();
                    }
                }
            }
            if (mustDoFullReconnect) {
                // I want to do this out of the loop
                bridgeHandler.reconnect();
            }
            logger.trace("Luxom: stopped listening to events");
        } catch (IOException e) {
            logger.warn("Luxom: listening to events - IO exception", e);
            if (!listenerStopped) {
                stillListeningToEvents = false;
                // this is a socket error, not a communication stop triggered from outside this runnable
                // the IO has stopped working, so we need to close cleanly and try to restart
                bridgeHandler.handleCommunicationError(e);
                return;
            }
        } finally {
            stillListeningToEvents = false;
        }

        // this is a stop from outside the runnable, so just log it and stop
        logger.debug("Luxom: event listener thread stopped");
    }

    public synchronized void sendMessage(String message) throws IOException {
        logger.debug("Luxom: send {}", message);
        if (luxomOut != null) {
            luxomOut.print(message + ";");
            luxomOut.flush();
            if (luxomOut.checkError()) {
                throw new IOException(String.format("luxom communication error when sending message: %s", message));
            }
        }
    }

    public boolean isConnected() {
        return luxomSocket != null && luxomSocket.isConnected();
    }
}
