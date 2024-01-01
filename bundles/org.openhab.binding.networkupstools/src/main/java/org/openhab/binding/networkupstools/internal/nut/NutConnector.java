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
package org.openhab.binding.networkupstools.internal.nut;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Connector class manages the socket connection to the NUT server.
 *
 * @author Hilbrand Bouwkamp - Initial contribution
 */
@NonNullByDefault
class NutConnector {

    private static final String USERNAME = "USERNAME %s";
    private static final String PASSWORD = "PASSWORD %s";
    private static final String OK = "OK";
    private static final String ERR = "ERR";
    private static final int TIME_OUT_MILLISECONDS = 10_000;
    private static final int MAX_RETRIES = 3;

    private final Logger logger = LoggerFactory.getLogger(NutConnector.class);

    private final String login;
    private final String password;
    private final InetSocketAddress inetSocketAddress;
    private @Nullable Socket socket;
    private @Nullable BufferedReader reader;
    private @Nullable PrintWriter writer;

    /**
     * Constructor.
     *
     * @param host host
     * @param port port
     * @param username username
     * @param password password
     */
    NutConnector(final String host, final int port, final String username, final String password) {
        this.login = username.isEmpty() ? "" : String.format(USERNAME, username);
        this.password = password.isEmpty() ? "" : String.format(PASSWORD, password);
        inetSocketAddress = new InetSocketAddress(host, port);
    }

    /**
     * Communicates to read the data to the NUT server. It handles the connection and authentication. Sends the command
     * to the NUT server and passes the reading of the values to the readFunction argument.
     *
     * @param <R> The type of the returned data
     * @param command The command to send to the NUT server
     * @param readFunction Function called to handle the lines read from the NUT server
     * @return the data read from the NUT server
     * @throws NutException Exception thrown related to the NUT server connection and/or data.
     */
    public synchronized <R> R read(final String command,
            final NutFunction<NutSupplier<String>, @Nullable R> readFunction) throws NutException {
        int retry = 0;

        while (true) {
            try {
                connectIfClosed();
                final PrintWriter localWriter = writer;
                final BufferedReader localReader = reader;

                if (localWriter == null) {
                    throw new NutException("Writer closed.");
                } else {
                    localWriter.println(command);
                }
                if (localReader == null) {
                    throw new NutException("Reader closed.");
                } else {
                    return readFunction.apply(() -> readLine(localReader));
                }
            } catch (final NutException | RuntimeException e) {
                retry++;
                close();
                if (retry < MAX_RETRIES) {
                    logger.debug("Error during command retry {}:", retry, e);
                } else {
                    throw e;
                }
            }
        }
    }

    /**
     * Opens a Socket connection if there is no connection or if the connection is closed. Authenticates if
     * username/password is provided.
     *
     * @throws NutException Exception thrown if no connection to NUT server could be made successfully.
     */
    public void connectIfClosed() throws NutException {
        if (socket == null || socket.isClosed() || !socket.isConnected()) {
            try {
                closeStreams();
                socket = newSocket();
                socket.connect(inetSocketAddress, TIME_OUT_MILLISECONDS);
                final BufferedReader localReader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                reader = localReader;
                final PrintWriter localWriter = new PrintWriter(socket.getOutputStream(), true);
                writer = localWriter;
                writeCommand(login, localReader, localWriter);
                writeCommand(password, localReader, localWriter);
            } catch (final IOException e) {
                throw new NutException(e);
            }
        }
    }

    /**
     * Closes the socket.
     */
    public synchronized void close() {
        try {
            if (socket != null) {
                socket.close();
                socket = null;
            }
        } catch (final IOException e) {
            logger.debug("Closing socket failed", e);
        }
    }

    private void closeStreams() {
        try {
            if (reader != null && socket != null && !socket.isInputShutdown()) {
                reader.close();
            }
            if (writer != null && socket != null && !socket.isOutputShutdown()) {
                writer.close();
            }
        } catch (final IOException e) {
            logger.debug("Closing streams failed", e);
        }
    }

    /**
     * Protected method to be able to mock the Socket connection in unit tests.
     *
     * @return a new Socket object
     */
    protected Socket newSocket() {
        return new Socket();
    }

    private @Nullable String readLine(final BufferedReader reader) throws NutException {
        try {
            final String line = reader.readLine();

            if (line != null && line.startsWith(ERR)) {
                throw new NutException(line);
            }
            return line;
        } catch (final IOException e) {
            throw new NutException(e);
        }
    }

    private void writeCommand(final String argument, final BufferedReader reader, final PrintWriter writer)
            throws IOException, NutException {
        if (!argument.isEmpty()) {
            writer.println(argument);
            final String result = reader.readLine();

            logger.trace("Command result: {}", result);
            if (result == null) {
                throw new NutException("No data read after sending command");
            } else if (!result.startsWith(OK)) {
                throw new NutException(result);
            }
        }
    }
}
