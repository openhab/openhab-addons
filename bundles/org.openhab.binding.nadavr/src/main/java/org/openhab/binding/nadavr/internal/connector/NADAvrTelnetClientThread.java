/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.nadavr.internal.connector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.openhab.binding.nadavr.internal.NADAvrConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link NADAvrTelnetClientThread} manages the connection to the NAD A/V Receiver.
 *
 * @author Dave J Schoepel - Initial contribution
 */
public class NADAvrTelnetClientThread extends Thread {

    private Logger logger = LoggerFactory.getLogger(NADAvrTelnetClientThread.class);

    private static final Integer RECONNECT_DELAY = 60000; // 1 minute

    private static final Integer TIMEOUT = 60000; // 1 minute

    private NADAvrConfiguration config;

    private NADAvrTelnetListener listener;

    private boolean connected = false;

    private Socket socket;

    private OutputStreamWriter out;

    private BufferedReader in;

    /**
     * @param config
     * @param listener
     */
    public NADAvrTelnetClientThread(NADAvrConfiguration config, NADAvrTelnetListener listener) {
        logger.debug("NAD Avr listener created.");
        this.config = config;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            if (!connected) {
                connectTelnetSocket();
            }

            do {
                try {
                    String line = in.readLine();
                    // logger.debug("Received from {} : {}", config.getHostname(), line);
                    if (line == null) {
                        logger.debug("No more data read from client. Disconnecting..");
                        listener.telnetClientConnected(false);
                        disconnect();
                        break;
                    }
                    // logger.debug("Received from {} : {}", config.getHostname(), line);
                    if (!line.isBlank()) {
                        listener.receivedLine(line);
                        // TODO: add code to capture settings in hash table
                    }
                } catch (SocketTimeoutException e) {
                    logger.trace("Socket timeout");
                    // Disconnects are not always detected unless you write to the socket.
                    try {
                        out.write('\r');
                        out.flush();
                    } catch (IOException e2) {
                        logger.debug("Error writing to socket");
                        connected = false;
                    }
                } catch (IOException e) {
                    if (!isInterrupted()) {
                        // only log if we don't stop this on purpose causing a SocketClosed
                        logger.debug("Error in telnet connection ", e);
                    }
                    connected = false;
                    listener.telnetClientConnected(false);
                }
            } while (!isInterrupted() && connected);
        }
        disconnect();
        logger.debug("Stopped client thread");
    }

    public void sendCommand(String command) {
        if (out != null) {
            try {
                out.write('\r' + command + '\r'); // Precede and follow command with carriage return
                out.flush();
            } catch (IOException e) {
                logger.debug("Error sending command", e);
            }
        } else {
            logger.debug("Cannot send command, no telnet connection");
        }
    }

    public void shutdown() {
        disconnect();
    }

    private void connectTelnetSocket() {
        disconnect();
        int delay = 0;

        while (!isInterrupted() && (socket == null || !socket.isConnected())) {
            try {
                Thread.sleep(delay);
                logger.debug("Connecting to {} {}", config.getHostname(), config.ipAddress);

                // Use raw socket instead of TelnetClient here because TelnetClient sends an
                // extra newline char after each write which causes the connection to become
                // unresponsive.
                socket = new Socket();
                socket.connect(new InetSocketAddress(config.getHostname(), config.getTelnetPort()), TIMEOUT);
                socket.setKeepAlive(true);
                socket.setSoTimeout(TIMEOUT);

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

                connected = true;
                listener.telnetClientConnected(true);
                logger.debug("NAD telnet client connected to {}", config.getHostname());
            } catch (IOException e) {
                logger.debug("Cannot connect to {}", config.getHostname(), e);
                listener.telnetClientConnected(false);
            } catch (InterruptedException e) {
                logger.debug("Interrupted while connecting to {}", config.getHostname(), e);
                Thread.currentThread().interrupt();
            }
            delay = RECONNECT_DELAY;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private void disconnect() {
        if (socket != null) {
            logger.debug("Disconnecting socket");
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("Error while disconnecting telnet client", e);
            } finally {
                socket = null;
                out = null;
                in = null;
                listener.telnetClientConnected(false);
            }
        }
    }

}
