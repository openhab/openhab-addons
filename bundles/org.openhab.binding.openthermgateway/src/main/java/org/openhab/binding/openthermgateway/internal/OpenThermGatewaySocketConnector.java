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
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;
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
 * @author Andrew Fiddian-Green - Improve thread interruption, socket timeouts, exception handling, FIFO command queue
 */
@NonNullByDefault
public class OpenThermGatewaySocketConnector implements OpenThermGatewayConnector {
    private static final int COMMAND_RESPONSE_MIN_WAIT_TIME_MILLISECONDS = 100;
    private static final int COMMAND_RESPONSE_MAX_WAIT_TIME_MILLISECONDS = 5000;
    private static final int MAXIMUM_FIFO_BUFFER_SIZE = 20;

    private static final String WDT_RESET_RESPONSE_MESSAGE = "WDT reset";

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

    /**
     * FIFO queue of commands that are pending being sent to the gateway. That is commands that are either not yet sent,
     * or sent but not yet acknowledged and pending possible re-sending.
     *
     * Note: we must use 'synchronized' when accessing this object to ensure proper thread safety.
     */
    private final Queue<PendingCommand> pendingCommands = new ArrayDeque<>(MAXIMUM_FIFO_BUFFER_SIZE);

    /**
     * Wrapper for a command entry in the pending command FIFO queue.
     *
     * @author AndrewFG - initial contribution
     */
    private class PendingCommand {
        protected final GatewayCommand command;
        protected final Instant expiryTime = Instant.now().plusMillis(COMMAND_RESPONSE_MAX_WAIT_TIME_MILLISECONDS);
        protected Instant sentTime = Instant.MIN;

        protected PendingCommand(GatewayCommand command) {
            this.command = command;
        }

        /**
         * Check if the command has been sent to the gateway.
         *
         * @return true if it has been sent
         */
        protected boolean sent() {
            return Instant.MIN.isBefore(sentTime);
        }

        /**
         * Check if the command is ready to send (or re-send) to the gateway.
         *
         * @return true if the command has either not been sent, or sent but not acknowledged within due time i.e. it
         *         needs to be re-sent
         */
        protected boolean readyToSend() {
            return sentTime.isBefore(Instant.now().minusMillis(COMMAND_RESPONSE_MIN_WAIT_TIME_MILLISECONDS));
        }

        /**
         * Check if the command has expired.
         *
         * @return true if the expiry time has expired
         */
        protected boolean expired() {
            return Instant.now().isAfter(expiryTime);
        }
    }

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
                        logger.trace("Read: {}", message);
                        handleMessage(message);
                    } else {
                        logger.debug("Received NULL message from OpenTherm Gateway (EOF)");
                        break;
                    }
                }
                // disable reporting every message (for cleaner re-starting)
                sendCommand(GatewayCommand.parse(GatewayCommandCode.PRINTSUMMARY, "1"));
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
    public void sendCommand(GatewayCommand command) {
        synchronized (pendingCommands) {
            // append the command to the end of the FIFO queue
            if (MAXIMUM_FIFO_BUFFER_SIZE < pendingCommands.size()
                    || !pendingCommands.offer(new PendingCommand(command))) {
                logger.warn("Command refused: FIFO buffer overrun => PLEASE REPORT !!");
            }
            // send the FIFO head command, which may or may not be the one just added
            pendingCommandsSendHeadCommandIfReady();
        } // release the pendingCommands lock
    }

    /**
     * Process the incoming message. Remove any expired commands from the queue. Check if the incoming message is an
     * acknowledgement. If it is the acknowledgement for the FIFO head command, remove it from the queue. Try to send
     * the (next) FIFO head command, if it exists, and is ready to send. And finally if the message is not an
     * acknowledgement, check if it is a valid message, and if so, pass it to the gateway Thing handler for processing.
     *
     * @param message the incoming message received from the gateway
     */
    private void handleMessage(String message) {
        // check if the message is a command acknowledgement e.g. having the form "XX: yyy"
        boolean isCommandAcknowledgement = (message.length() > 2) && (message.charAt(2) == ':');

        synchronized (pendingCommands) {
            // remove all expired commands
            pendingCommandsRemoveAllExpiredCommands();

            // if acknowledgement is for the FIFO head command, remove it from the queue
            if (isCommandAcknowledgement) {
                pendingCommandsRemoveHeadCommandIfAcknowledgement(message);
            }

            // (re-)send the FIFO head command, if it exists and is ready to send
            pendingCommandsSendHeadCommandIfReady();
        } // release the pendingCommands lock

        if (isCommandAcknowledgement) {
            callback.receiveAcknowledgement(message);
        } else if (message.startsWith(WDT_RESET_RESPONSE_MESSAGE)) {
            logger.warn("OpenTherm Gateway was reset by its Watch-Dog Timer!");
        } else {
            Message msg = Message.parse(message);

            // ignore and log bad messages
            if (msg == null) {
                logger.debug("Received message: {}, (unknown)", message);
                return;
            }

            // pass good messages to the Thing handler for processing
            if (msg.getMessageType() == MessageType.READACK || msg.getMessageType() == MessageType.WRITEDATA
                    || msg.getID() == 0 || msg.getID() == 1) {
                callback.receiveMessage(msg);
            }
        }
    }

    /**
     * If there is a FIFO head command that is ready to (re-)send, then send it.
     */
    private void pendingCommandsSendHeadCommandIfReady() {
        // process the command at the head of the queue
        PendingCommand headCommand = pendingCommands.peek();
        if (headCommand != null && headCommand.readyToSend()) {
            String message = headCommand.command.toFullString();

            // transmit the command string
            PrintWriter writer = this.writer;
            if (isConnected() && (writer != null)) {
                writer.print(message + "\r\n");
                writer.flush();
                if (writer.checkError()) {
                    logger.warn("Error sending command to OpenTherm Gateway => PLEASE REPORT !!");
                    stop();
                }
                if (logger.isTraceEnabled()) {
                    logger.trace("Sent: {}{}", message, headCommand.sent() ? " (repeat)" : "");
                }
                headCommand.sentTime = Instant.now();
            } else {
                logger.debug("Unable to send command: {}. OpenThermGatewaySocketConnector is not connected.", message);
            }
        }
    }

    /**
     * If the acknowledgement message corresponds to the FIFO head command then remove it from the queue.
     *
     * @param message must be an acknowledgement message in the form "XX: yyy"
     */
    private void pendingCommandsRemoveHeadCommandIfAcknowledgement(String message) {
        PendingCommand headCommand = pendingCommands.peek();
        if (headCommand != null && headCommand.command.getCode().equals(message.substring(0, 2))) {
            pendingCommands.poll();
        }
    }

    /**
     * Remove all expired commands from the queue.
     */
    private void pendingCommandsRemoveAllExpiredCommands() {
        pendingCommands.removeIf(pendingCommand -> pendingCommand.expired());
    }
}
