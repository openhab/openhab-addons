/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.denonmarantz.internal.connector.telnet;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;

import org.apache.commons.lang.StringUtils;
import org.openhab.binding.denonmarantz.internal.config.DenonMarantzConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manage telnet connection to the Denon/Marantz Receiver
 *
 * @author Jeroen Idserda - Initial contribution (1.x Binding)
 * @author Jan-Willem Veldhuis - Refactored for 2.x
 */
public class DenonMarantzTelnetClient implements Runnable {

    private Logger logger = LoggerFactory.getLogger(DenonMarantzTelnetClient.class);

    private static final Integer RECONNECT_DELAY = 60000; // 1 minute

    private static final Integer TIMEOUT = 60000; // 1 minute

    private DenonMarantzConfiguration config;

    private DenonMarantzTelnetListener listener;

    private boolean running = true;

    private boolean connected = false;

    private Socket socket;

    private OutputStreamWriter out;

    private BufferedReader in;

    public DenonMarantzTelnetClient(DenonMarantzConfiguration config, DenonMarantzTelnetListener listener) {
        logger.debug("Denon listener created");
        this.config = config;
        this.listener = listener;
    }

    @Override
    public void run() {
        while (running) {
            if (!connected) {
                connectTelnetSocket();
            }

            do {
                try {
                    String line = in.readLine();
                    if (line == null) {
                        logger.debug("No more data read from client. Disconnecting..");
                        listener.telnetClientConnected(false);
                        disconnect();
                        break;
                    }
                    logger.trace("Received from {}: {}", config.getHost(), line);
                    if (!StringUtils.isBlank(line)) {
                        listener.receivedLine(line);
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
                    if (!Thread.currentThread().isInterrupted()) {
                        // only log if we don't stop this on purpose causing a SocketClosed
                        logger.debug("Error in telnet connection ", e);
                    }
                    connected = false;
                    listener.telnetClientConnected(false);
                }
            } while (running && connected);
        }
    }

    public void sendCommand(String command) {
        if (out != null) {
            logger.debug("Sending command {}", command);
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
        this.running = false;
        disconnect();
    }

    private void connectTelnetSocket() {
        disconnect();
        int delay = 0;

        while (this.running && (socket == null || !socket.isConnected())) {
            try {
                Thread.sleep(delay);
                logger.debug("Connecting to {}", config.getHost());

                // Use raw socket instead of TelnetClient here because TelnetClient sends an extra newline char
                // after each write which causes the connection to become unresponsive.
                socket = new Socket();
                socket.connect(new InetSocketAddress(config.getHost(), config.getTelnetPort()), TIMEOUT);
                socket.setKeepAlive(true);
                socket.setSoTimeout(TIMEOUT);

                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new OutputStreamWriter(socket.getOutputStream(), "UTF-8");

                connected = true;
                listener.telnetClientConnected(true);
            } catch (IOException e) {
                logger.debug("Cannot connect to {}", config.getHost(), e);
                listener.telnetClientConnected(false);
            } catch (InterruptedException e) {
                logger.debug("Interrupted while connecting to {}", config.getHost(), e);
            }
            delay = RECONNECT_DELAY;
        }

        logger.debug("Denon telnet client connected to {}", config.getHost());
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
