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
package org.openhab.binding.intesis.internal.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.thing.ThingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class handling the Socket connections.
 *
 * @author Cody Cutrer - Initial contribution
 * @author Hans-Jörg Merk - Moved Socket to it's own class
 */
@NonNullByDefault
public class IntesisBoxSocketApi {

    private final Logger logger = LoggerFactory.getLogger(IntesisBoxSocketApi.class);

    private final String ipAddress;
    private final int port;

    private @Nullable IntesisSocket tcpSocket = null;
    private @Nullable OutputStreamWriter tcpOutput = null;
    private @Nullable BufferedReader tcpInput = null;

    private @Nullable IntesisBoxChangeListener changeListener;

    private boolean connected = false;

    public IntesisBoxSocketApi(final String ipAddress, final int port) {
        this.ipAddress = ipAddress;
        this.port = port;
    }

    private class IntesisSocket {
        final Socket socket;

        public IntesisSocket() throws UnknownHostException, IOException {
            socket = new Socket();
            SocketAddress tcpSocketAddress = new InetSocketAddress(ipAddress, port);
            socket.connect(tcpSocketAddress);
        }

        public void close() throws IOException {
            socket.close();
        }
    }

    @SuppressWarnings("null")
    public void openConnection() throws IOException {
        closeConnection();

        logger.debug("openConnection(): Connecting to IntesisBox ");

        IntesisBoxChangeListener listener = this.changeListener;
        if (listener != null) {
            logger.debug("IntesisBox listener added");
        }

        tcpSocket = new IntesisSocket();
        tcpOutput = new OutputStreamWriter(tcpSocket.socket.getOutputStream(), "US-ASCII");
        tcpInput = new BufferedReader(new InputStreamReader(tcpSocket.socket.getInputStream()));

        Thread tcpListener = new Thread(new TCPListener());
        tcpListener.start();

        setConnected(true);
        if (listener != null) {
            listener.connectionStatusChanged(ThingStatus.ONLINE);
        }
    }

    @SuppressWarnings("null")
    public void closeConnection() {
        try {
            if (tcpSocket != null) {
                logger.debug("closeConnection(): Closing Socket!");
                tcpSocket.close();
                tcpSocket = null;
            }
            if (tcpInput != null) {
                logger.debug("closeConnection(): Closing Output Writer!");
                tcpInput.close();
                tcpInput = null;
            }
            if (tcpOutput != null) {
                logger.debug("closeConnection(): Closing Input Reader!");
                tcpOutput.close();
                tcpOutput = null;
            }

            setConnected(false);
            logger.debug("closeConnection(): Closed TCP Connection!");
        } catch (IOException ioException) {
            logger.debug("closeConnection(): Unable to close connection - {}", ioException.getMessage());
        } catch (Exception exception) {
            logger.debug("closeConnection(): Error closing connection - {}", exception.getMessage());
        }
    }

    private class TCPListener implements Runnable {
        private final Logger logger = LoggerFactory.getLogger(TCPListener.class);

        /**
         * Run method. Runs the MessageListener thread
         */
        @Override
        public void run() {
            try {
                while (isConnected()) {
                    String message = read();
                    try {
                        logger.trace("Calling readMessage()");
                        readMessage(message);
                    } catch (Exception e) {
                        logger.debug("TCPListener(): Message not handled by thing: {}", e.getMessage());
                        closeConnection();
                    }
                }
            } catch (Exception e) {
                logger.debug("TCPListener(): Unable to read message: {} ", e.getMessage(), e);
                closeConnection();
            }
        }
    }

    public void addIntesisBoxChangeListener(IntesisBoxChangeListener listener) {
        if (this.changeListener == null) {
            this.changeListener = listener;
        }
    }

    private void write(String data) {
        try {
            if (tcpOutput != null) {
                tcpOutput.write(data);
            }
            if (tcpOutput != null) {
                tcpOutput.flush();
            }
        } catch (IOException ioException) {
            logger.debug("write(): {}", ioException.getMessage());
            setConnected(false);
        } catch (Exception exception) {
            logger.debug("write(): Unable to write to socket: {} ", exception.getMessage(), exception);
            setConnected(false);
        }
    }

    public String read() {
        String message = "";
        try {
            if (tcpInput != null) {
                message = tcpInput.readLine();
                logger.debug("read(): Message Received: {}", message);
            }
        } catch (IOException ioException) {
            logger.debug("read(): IO Exception: {}", ioException.getMessage());
            setConnected(false);
        } catch (Exception exception) {
            logger.debug("read(): Exception: {} ", exception.getMessage(), exception);
            setConnected(false);
        }
        return message;
    }

    public void readMessage(String message) {
        IntesisBoxChangeListener listener = this.changeListener;

        if (listener != null && !message.isEmpty()) {
            logger.trace("readMessage(): Inform listener with message: {}", message);
            listener.messageReceived(message);
        }
    }

    public void sendAlive() {
        write("GET,1:*\r\n");
        logger.trace("Keep alive sent");
    }

    public void sendId() {
        write("ID\r\n");
        logger.trace("Id request sent");
    }

    public void sendLimitsQuery() {
        write("LIMITS:*\r\n");
        logger.trace("Limits request sent");
    }

    public void sendCommand(String function, String value) {
        String data = String.format("SET,1:%s,%s\r\n", function, value);
        write(data);
        logger.trace("sendCommand(): '{}' Command Sent - {}", function, value);
    }

    public void sendQuery(String function) {
        String data = String.format("GET,1:%s\r\n", function);
        write(data);
        logger.trace("sendQuery(): '{}' Command Sent", function);
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }
}
