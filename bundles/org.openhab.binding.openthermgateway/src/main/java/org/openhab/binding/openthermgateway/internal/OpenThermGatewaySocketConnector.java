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
package org.openhab.binding.openthermgateway.internal;

import static org.openhab.binding.openthermgateway.internal.OpenThermGatewayBindingConstants.BINDING_ID;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.common.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenThermGatewaySocketConnector} is responsible for handling the socket connection
 *
 * @author Arjen Korevaar - Initial contribution
 * @author Arjan Mels - Improved robustness by re-sending commands, handling all message types (not only Boiler)
 */
@NonNullByDefault
public class OpenThermGatewaySocketConnector implements OpenThermGatewayConnector {
    private static final int COMMAND_RESPONSE_MIN_WAIT_TIME_MILLISECONDS = 100;
    private static final int COMMAND_RESPONSE_MAX_WAIT_TIME_MILLISECONDS = 5000;

    private final Logger logger = LoggerFactory.getLogger(OpenThermGatewaySocketConnector.class);

    private final OpenThermGatewayCallback callback;
    private final String ipaddress;
    private final int port;
    private final int connectTimeoutMilliseconds;
    private final int readTimeoutMilliSeconds;

    private @Nullable volatile PrintWriter writer;
    private @Nullable volatile Thread thread;
    private @Nullable Future<Boolean> future;
    private @Nullable ExecutorService executor;

    private Map<String, Entry<Long, GatewayCommand>> pendingCommands = new ConcurrentHashMap<>();

    public OpenThermGatewaySocketConnector(OpenThermGatewayCallback callback, OpenThermGatewayConfiguration config) {
        this.callback = callback;
        ipaddress = config.ipaddress;
        port = config.port;
        connectTimeoutMilliseconds = config.connectTimeoutSeconds * 1000;
        readTimeoutMilliSeconds = config.readTimeoutSeconds * 1000;
    }

    @Override
    public Boolean call() throws Exception {
        thread = Thread.currentThread();
        try (Socket socket = new Socket()) {
            logger.debug("Connecting OpenThermGatewaySocketConnector to {}:{}", this.ipaddress, this.port);
            callback.connectionStateChanged(ConnectionState.CONNECTING);

            socket.connect(new InetSocketAddress(ipaddress, port), connectTimeoutMilliseconds);
            socket.setSoTimeout(readTimeoutMilliSeconds);

            logger.debug("OpenThermGatewaySocketConnector connected");
            callback.connectionStateChanged(ConnectionState.CONNECTED);

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    PrintWriter wrt = new PrintWriter(socket.getOutputStream(), true)) {
                // Make writer accessible on class level
                writer = wrt;

                sendCommand(GatewayCommand.parse(GatewayCommandCode.PRINTREPORT, "A"));
                // Set the OTGW to report every message it receives and transmits
                sendCommand(GatewayCommand.parse(GatewayCommandCode.PRINTSUMMARY, "0"));

                while (!Thread.currentThread().isInterrupted()) {
                    @Nullable
                    String message = reader.readLine();

                    if (message != null) {
                        handleMessage(message);
                    } else {
                        logger.debug("Received NULL message from OpenTherm Gateway (EOF)");
                        break;
                    }
                }
            } catch (IOException ex) {
                logger.warn("Error communicating with OpenTherm Gateway: '{}'", ex.getMessage());
            }
        } catch (IOException ex) {
            logger.warn("Unable to connect to the OpenTherm Gateway: '{}'", ex.getMessage());
        }
        thread = null;
        writer = null;
        logger.debug("OpenThermGatewaySocketConnector disconnected");
        callback.connectionStateChanged(ConnectionState.DISCONNECTED);
        return true;
    }

    @Override
    public void stop() {
        logger.debug("Stopping OpenThermGatewaySocketConnector");

        Thread thread = this.thread;
        Future<Boolean> future = this.future;
        ExecutorService executor = this.executor;

        if (executor != null) {
            executor.shutdown();
        }
        if ((thread != null) && thread.isAlive()) {
            thread.interrupt();
        }
        if (future != null) {
            try {
                future.get(readTimeoutMilliSeconds, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                // expected exception due to e.g. IOException on socket close
            } catch (TimeoutException | InterruptedException e) {
                // unexpected exception
                logger.warn("stop() exception '{}' => PLEASE REPORT !!", e.getMessage());
            }
        }

        this.thread = null;
        this.future = null;
        this.executor = null;
    }

    @Override
    public void start() {
        logger.debug("Starting OpenThermGatewaySocketConnector");
        ExecutorService executor = this.executor = Executors
                .newSingleThreadExecutor(new NamedThreadFactory("binding-" + BINDING_ID));
        future = executor.submit(this);
    }

    @Override
    public synchronized boolean isConnected() {
        Thread thread = this.thread;
        return (thread != null) && thread.isAlive();
    }

    @Override
    public synchronized void sendCommand(GatewayCommand command) {
        PrintWriter wrt = writer;

        pendingCommands.put(command.getCode(),
                new AbstractMap.SimpleImmutableEntry<>(System.currentTimeMillis(), command));

        String msg = command.toFullString();

        if (isConnected() && (wrt != null)) {
            logger.debug("Sending message: {}", msg);
            wrt.print(msg + "\r\n");
            wrt.flush();
            if (wrt.checkError()) {
                logger.warn("sendCommand() error sending message to OpenTherm Gateway => PLEASE REPORT !!");
                stop();
            }
        } else {
            logger.debug("Unable to send message: {}. OpenThermGatewaySocketConnector is not connected.", msg);
        }
    }

    private void handleMessage(String message) {
        if (message.length() > 2 && message.charAt(2) == ':') {
            String code = message.substring(0, 2);
            String value = message.substring(3);

            logger.debug("Received command confirmation: {}: {}", code, value);
            pendingCommands.remove(code);
            return;
        }

        long currentTime = System.currentTimeMillis();

        for (Entry<Long, GatewayCommand> timeAndCommand : pendingCommands.values()) {
            long responseTime = timeAndCommand.getKey() + COMMAND_RESPONSE_MIN_WAIT_TIME_MILLISECONDS;
            long timeoutTime = timeAndCommand.getKey() + COMMAND_RESPONSE_MAX_WAIT_TIME_MILLISECONDS;

            if (currentTime > responseTime && currentTime <= timeoutTime) {
                logger.debug("Resending command: {}", timeAndCommand.getValue());
                sendCommand(timeAndCommand.getValue());
            } else if (currentTime > timeoutTime) {
                pendingCommands.remove(timeAndCommand.getValue().getCode());
            }
        }

        Message msg = Message.parse(message);

        if (msg == null) {
            logger.trace("Received message: {}, (unknown)", message);
            return;
        }
        logger.trace("Received message: {}, {} {} {}", message, msg.getID(), msg.getCodeType(), msg.getMessageType());
        if (msg.getMessageType() == MessageType.READACK || msg.getMessageType() == MessageType.WRITEDATA
                || msg.getID() == 0 || msg.getID() == 1) {
            callback.receiveMessage(msg);
        }
    }
}
