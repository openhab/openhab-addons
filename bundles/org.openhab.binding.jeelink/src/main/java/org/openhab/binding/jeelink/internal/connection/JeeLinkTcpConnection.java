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
package org.openhab.binding.jeelink.internal.connection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ScheduledExecutorService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Reads lines from TCP port and propagates them to registered InputListeners.
 *
 * @author Volker Bier - Initial contribution
 */
public class JeeLinkTcpConnection extends AbstractJeeLinkConnection {
    private static final Pattern IP_PORT_PATTERN = Pattern.compile("([0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+):([0-9]+)");

    private final Logger logger = LoggerFactory.getLogger(JeeLinkTcpConnection.class);

    private ScheduledExecutorService scheduler;
    private Reader reader;
    private Socket socket;

    public JeeLinkTcpConnection(String port, ScheduledExecutorService scheduler, ConnectionListener l) {
        super(port, l);
        this.scheduler = scheduler;
    }

    @Override
    public synchronized void closeConnection() {
        if (reader != null) {
            logger.debug("Closing TCP connection to port {}...", port);
            reader.close();
            reader = null;
            closeSocketSilently();
            socket = null;
            notifyClosed();
        }
    }

    @Override
    public synchronized void openConnection() {
        if (reader != null) {
            logger.debug("TCP connection to port {} is already open!", port);
            return;
        }

        Matcher ipm = IP_PORT_PATTERN.matcher(port);
        if (!ipm.matches()) {
            notifyAbort("Invalid TCP port specification: " + port);
        }

        String hostName = ipm.group(1);
        int portNumber = Integer.parseInt(ipm.group(2));

        logger.debug("Opening TCP connection to host {} port {}...", hostName, portNumber);
        try {
            logger.debug("Creating TCP socket to {}...", port);
            socket = new Socket(hostName, portNumber);
            socket.setKeepAlive(true);
            logger.debug("TCP socket created.");

            reader = new Reader(socket);
            scheduler.execute(reader);

            notifyOpen();
        } catch (IOException ex) {
            if (socket != null) {
                closeSocketSilently();
            }

            notifyAbort(ex.getMessage());
        }
    }

    private void closeSocketSilently() {
        try {
            socket.close();
        } catch (IOException e) {
            logger.debug("Failed to close socket.", e);
        }
    }

    @Override
    public OutputStream getInitStream() throws IOException {
        return socket == null ? null : socket.getOutputStream();
    }

    private class Reader implements Runnable {
        private Socket socket;
        private BufferedReader inputReader;
        private volatile boolean isRunning = true;

        private Reader(Socket socket) throws IOException {
            this.socket = socket;
            inputReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        }

        @Override
        public void run() {
            String line;
            logger.debug("Reader for TCP port {} starting...", port);
            try {
                while (isRunning) {
                    line = inputReader.readLine();

                    if (line == null) {
                        throw new IOException("Got EOF on port " + port);
                    }

                    propagateLine(line);
                }
            } catch (IOException ex) {
                if (isRunning) {
                    closeConnection();
                    notifyAbort(ex.getMessage());
                }
            } finally {
                logger.debug("Reader for TCP port {} finished...", port);
            }
        }

        public void close() {
            logger.debug("Shutting down reader for TCP port {}...", port);
            try {
                isRunning = false;
                socket.close();
                inputReader.close();
            } catch (IOException ex) {
                logger.debug("Failed to close TCP port {}!", port, ex);
            }
        }
    }
}
