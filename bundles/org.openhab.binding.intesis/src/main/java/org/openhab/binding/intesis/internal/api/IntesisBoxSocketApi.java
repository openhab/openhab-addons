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
package org.openhab.binding.intesis.internal.api;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.intesis.internal.handler.IntesisBoxHandler;
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
    private final String readerThreadName;

    private @Nullable IntesisSocket tcpSocket = null;
    private @Nullable OutputStreamWriter tcpOutput = null;
    private @Nullable BufferedReader tcpInput = null;

    private @Nullable IntesisBoxChangeListener changeListener;

    private boolean connected = false;

    public IntesisBoxSocketApi(final String ipAddress, final int port, final String readerThreadName) {
        this.ipAddress = ipAddress;
        this.port = port;
        this.readerThreadName = readerThreadName;
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

    public void openConnection() throws IOException {
        closeConnection();

        IntesisBoxChangeListener listener = this.changeListener;
        IntesisSocket localSocket = tcpSocket = new IntesisSocket();
        tcpOutput = new OutputStreamWriter(localSocket.socket.getOutputStream(), StandardCharsets.US_ASCII);
        tcpInput = new BufferedReader(
                new InputStreamReader(localSocket.socket.getInputStream(), StandardCharsets.US_ASCII));

        Thread tcpListener = new Thread(new TCPListener());
        tcpListener.setName(readerThreadName);
        tcpListener.setDaemon(true);
        tcpListener.start();

        setConnected(true);
        if (listener != null) {
            listener.connectionStatusChanged(ThingStatus.ONLINE, null);
        }
    }

    public void closeConnection() {
        try {
            IntesisSocket localSocket = tcpSocket;
            OutputStreamWriter localOutput = tcpOutput;
            BufferedReader localInput = tcpInput;

            if (localSocket != null) {
                localSocket.close();
                localSocket = null;
            }
            if (localInput != null) {
                localInput.close();
                localInput = null;
            }
            if (localOutput != null) {
                localOutput.close();
                localOutput = null;
            }
            setConnected(false);
        } catch (IOException ioException) {
            logger.debug("closeConnection(): Unable to close connection - {}", ioException.getMessage());
        } catch (Exception exception) {
            logger.debug("closeConnection(): Error closing connection - {}", exception.getMessage());
        }
    }

    private class TCPListener implements Runnable {

        /**
         * Run method. Runs the MessageListener thread
         */
        @Override
        public void run() {
            while (isConnected()) {
                String message = read();
                readMessage(message);
            }
        }
    }

    public void addIntesisBoxChangeListener(IntesisBoxChangeListener listener) {
        if (this.changeListener == null) {
            this.changeListener = listener;
        }
    }

    private void write(String data) {
        IntesisBoxChangeListener listener = this.changeListener;
        try {
            OutputStreamWriter localOutput = tcpOutput;

            if (localOutput != null) {
                localOutput.write(data);
                localOutput.flush();
            }
        } catch (IOException ioException) {
            setConnected(false);
            if (listener != null) {
                listener.connectionStatusChanged(ThingStatus.OFFLINE, ioException.getMessage());
            }
        }
    }

    public String read() {
        String message = "";
        try {
            BufferedReader localInput = tcpInput;
            if (localInput != null) {
                message = localInput.readLine();
            }
        } catch (IOException ioException) {
            setConnected(false);
        }
        return message;
    }

    public void readMessage(String message) {
        IntesisBoxChangeListener listener = this.changeListener;

        if (listener != null && !message.isEmpty()) {
            listener.messageReceived(message);
        }
    }

    public void sendAlive() {
        write("GET,1:*\r\n");
    }

    public void sendId() {
        write("ID\r\n");
    }

    public void sendLimitsQuery() {
        write("LIMITS:*\r\n");
    }

    public void sendCommand(String function, String value) {
        String data = String.format("SET,1:%s,%s\r\n", function, value);
        write(data);
    }

    public void sendQuery(String function) {
        String data = String.format("GET,1:%s\r\n", function);
        write(data);
    }

    public boolean isConnected() {
        return this.connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public void removeIntesisBoxChangeListener(IntesisBoxHandler intesisBoxHandler) {
        if (this.changeListener != null) {
            this.changeListener = null;
        }
    }
}
