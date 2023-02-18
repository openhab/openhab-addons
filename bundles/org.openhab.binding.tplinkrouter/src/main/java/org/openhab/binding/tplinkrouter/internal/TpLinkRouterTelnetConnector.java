/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.tplinkrouter.internal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link TpLinkRouterTelnetConnector} is responsible for the telnet connection.
 *
 * @author Olivier Marceau - Initial contribution
 */
@NonNullByDefault
public class TpLinkRouterTelnetConnector {

    private static final int TIMEOUT_MS = (int) TimeUnit.MINUTES.toMillis(1);

    private final Logger logger = LoggerFactory.getLogger(TpLinkRouterTelnetConnector.class);

    private @Nullable Thread telnetClientThread;
    private @Nullable Socket socket; // use raw socket since commons net usage seems discouraged
    private @Nullable OutputStreamWriter out;

    public void connect(TpLinkRouterTelenetListener listener, TpLinkRouterConfiguration config, String thingUID)
            throws IOException {
        logger.debug("Connecting to {}", config.hostname);

        Socket socketLocal = new Socket();
        socketLocal.connect(new InetSocketAddress(config.hostname, config.port));
        socketLocal.setSoTimeout(TIMEOUT_MS);
        socketLocal.setKeepAlive(true);

        InputStreamReader inputStream = new InputStreamReader(socketLocal.getInputStream());
        this.out = new OutputStreamWriter(socketLocal.getOutputStream());
        this.socket = socketLocal;
        loginAttempt(listener, inputStream, config);
        Thread clientThread = new Thread(() -> listenInputStream(listener, inputStream, config));
        clientThread.setName("OH-binding-" + thingUID);
        this.telnetClientThread = clientThread;
        clientThread.start();
        logger.debug("TP-Link router telnet client connected to {}", config.hostname);
    }

    public void dispose() {
        logger.debug("disposing connector");
        Thread clientThread = telnetClientThread;
        if (clientThread != null) {
            clientThread.interrupt();
            telnetClientThread = null;
        }
        Socket socketLocal = socket;
        if (socketLocal != null) {
            try {
                socketLocal.close();
            } catch (IOException e) {
                logger.debug("Error while disconnecting telnet client", e);
            }
            socket = null;
        }
    }

    public void sendCommand(String command) {
        logger.debug("sending command: {}", command);
        OutputStreamWriter output = out;
        if (output != null) {
            try {
                output.write(command + '\n');
                output.flush();
            } catch (IOException e) {
                logger.warn("Error sending command", e);
            }
        } else {
            logger.debug("Cannot send command, no telnet connection");
        }
    }

    private void listenInputStream(TpLinkRouterTelenetListener listener, InputStreamReader inputStream,
            TpLinkRouterConfiguration config) {
        listener.onReaderThreadStarted();
        BufferedReader in = new BufferedReader(inputStream);
        try {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    String line = in.readLine();
                    if (line != null && !line.isBlank()) {
                        listener.receivedLine(line);
                        if ("CLI exited after timing out".equals(line)) {
                            OutputStreamWriter output = out;
                            if (output != null) {
                                output.write("\n"); // trigger a "username:" prompt
                                output.flush();
                                loginAttempt(listener, inputStream, config);
                            }
                        }
                    }
                } catch (SocketTimeoutException e) {
                    logger.trace("Socket timeout");
                }
            }
        } catch (InterruptedIOException e) {
            logger.debug("Error in telnet connection ", e);
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) {
                logger.debug("Error in telnet connection ", e);
                listener.onReaderThreadStopped();
            }
        }
        if (Thread.currentThread().isInterrupted()) {
            logger.debug("Interrupted client thread");
            listener.onReaderThreadInterrupted();
        }
    }

    private void loginAttempt(TpLinkRouterTelenetListener listener, InputStreamReader inputStreamReader,
            TpLinkRouterConfiguration config) throws IOException {
        int charInt;
        StringBuilder word = new StringBuilder();
        OutputStreamWriter output = out;
        if (output != null) {
            try {
                while ((charInt = inputStreamReader.read()) != -1) {
                    word.append((char) charInt);
                    logger.trace("received char: {}", (char) charInt);
                    if (word.toString().contains("username:")) {
                        logger.debug("Sending username");
                        output.write(config.username + '\n');
                        output.flush();
                        word = new StringBuilder();
                    }
                    if (word.toString().contains("password:")) {
                        logger.debug("Sending password");
                        output.write(config.password + '\n');
                        output.flush();
                        break;
                    }
                }
            } catch (SocketTimeoutException e) {
                listener.onCommunicationUnavailable();
            }
        }
    }
}
