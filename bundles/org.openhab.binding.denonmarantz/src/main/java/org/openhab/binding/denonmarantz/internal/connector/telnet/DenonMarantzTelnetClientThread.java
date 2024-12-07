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
package org.openhab.binding.denonmarantz.internal.connector.telnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.denonmarantz.internal.DenonMarantzBindingConstants;
import org.openhab.binding.denonmarantz.internal.config.DenonMarantzConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage telnet connection to the Denon/Marantz Receiver
 *
 * @author Jeroen Idserda - Initial contribution (1.x Binding)
 * @author Jan-Willem Veldhuis - Refactored for 2.x
 */
@NonNullByDefault
public class DenonMarantzTelnetClientThread extends Thread {

    private Logger logger = LoggerFactory.getLogger(DenonMarantzTelnetClientThread.class);

    private static final Integer RECONNECT_DELAY = 60000; // 1 minute

    private static final Integer TIMEOUT = 60000; // 1 minute

    private DenonMarantzConfiguration config;

    private DenonMarantzTelnetListener listener;

    private boolean connected = false;

    private @Nullable Socket socket;

    private @Nullable OutputStreamWriter out;

    private @Nullable BufferedReader in;

    public DenonMarantzTelnetClientThread(DenonMarantzConfiguration config, DenonMarantzTelnetListener listener) {
        super(String.format("OH-binding-%s-%s", DenonMarantzBindingConstants.BINDING_ID, "TelnetClient"));
        logger.debug("Denon listener created");
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
                    String line = null;
                    BufferedReader in = this.in;
                    if (in != null) {
                        line = in.readLine();
                    }
                    if (line == null) {
                        logger.debug("No more data read from client. Disconnecting..");
                        listener.telnetClientConnected(false);
                        disconnect();
                        break;
                    }
                    logger.trace("Received from {}: {}", config.getHost(), line);
                    if (!line.isBlank()) {
                        listener.receivedLine(line);
                    }
                } catch (SocketTimeoutException e) {
                    logger.trace("Socket timeout");
                    // Disconnects are not always detected unless you write to the socket.
                    OutputStreamWriter out = this.out;
                    if (out != null) {
                        try {
                            out.write('\r');
                            out.flush();
                        } catch (IOException e2) {
                            logger.debug("Error writing to socket");
                            connected = false;
                        }
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
        OutputStreamWriter out = this.out;
        if (out != null) {
            try {
                out.write(command + '\r');
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

        Socket socket = this.socket;
        while (!isInterrupted() && (socket == null || !socket.isConnected())) {
            try {
                Thread.sleep(delay);
                logger.debug("Connecting to {}", config.getHost());

                // Use raw socket instead of TelnetClient here because TelnetClient sends an
                // extra newline char after each write which causes the connection to become
                // unresponsive.
                socket = this.socket = new Socket();
                socket.connect(new InetSocketAddress(config.getHost(), config.getTelnetPort()), TIMEOUT);
                socket.setKeepAlive(true);
                socket.setSoTimeout(TIMEOUT);

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

                connected = true;
                listener.telnetClientConnected(true);
                logger.debug("Denon telnet client connected to {}", config.getHost());
            } catch (IOException e) {
                logger.debug("Cannot connect to {}", config.getHost(), e);
                listener.telnetClientConnected(false);
            } catch (InterruptedException e) {
                logger.debug("Interrupted while connecting to {}", config.getHost(), e);
                Thread.currentThread().interrupt();
            }
            delay = RECONNECT_DELAY;
        }
    }

    public boolean isConnected() {
        return connected;
    }

    private void disconnect() {
        Socket socket = this.socket;
        if (socket != null) {
            logger.debug("Disconnecting socket");
            try {
                socket.close();
            } catch (IOException e) {
                logger.debug("Error while disconnecting telnet client", e);
            } finally {
                this.socket = null;
                out = null;
                in = null;
                listener.telnetClientConnected(false);
            }
        }
    }
}
