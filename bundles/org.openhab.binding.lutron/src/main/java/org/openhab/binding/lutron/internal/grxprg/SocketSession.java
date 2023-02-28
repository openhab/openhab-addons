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
package org.openhab.binding.lutron.internal.grxprg;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a restartable socket connection to the underlying telnet session with a GRX-PRG/GRX-CI-PRG. Commands can
 * be sent via {@link #sendCommand(String)} and responses will be received on the {@link SocketSessionCallback}
 *
 * @author Tim Roberts - Initial contribution
 */
public class SocketSession {
    private final Logger logger = LoggerFactory.getLogger(SocketSession.class);

    /**
     * The uid of the calling thing
     */
    private final String uid;
    /**
     * The host/ip address to connect to
     */
    private final String host;

    /**
     * The port to connect to
     */
    private final int port;

    /**
     * The actual socket being used. Will be null if not connected
     */
    private Socket client;

    /**
     * The writer to the {@link #client}. Will be null if not connected
     */
    private PrintStream writer;

    /**
     * The reader from the {@link #client}. Will be null if not connected
     */
    private BufferedReader reader;

    /**
     * The {@link ResponseReader} that will be used to read from {@link #reader}
     */
    private final ResponseReader responseReader = new ResponseReader();

    /**
     * The responses read from the {@link #responseReader}
     */
    private final BlockingQueue<Object> responsesQueue = new ArrayBlockingQueue<>(50);

    /**
     * The dispatcher of responses from {@link #responsesQueue}
     */
    private final Dispatcher dispatcher = new Dispatcher();

    /**
     * The {@link SocketSessionCallback} that the {@link #dispatcher} will call
     */
    private AtomicReference<SocketSessionCallback> callback = new AtomicReference<>(null);

    /**
     * Creates the socket session from the given host and port
     *
     * @param uid the thing uid of the calling thing
     * @param host a non-null, non-empty host/ip address
     * @param port the port number between 1 and 65535
     */
    public SocketSession(String uid, String host, int port) {
        if (host == null || host.trim().length() == 0) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.uid = uid;
        this.host = host;
        this.port = port;
    }

    /**
     * Sets the {@link SocketSessionCallback} to use when calling back the
     * responses that have been received.
     *
     * @param callback a non-null {@link SocketSessionCallback} to use
     */
    public void setCallback(SocketSessionCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback cannot be null");
        }
        this.callback.set(callback);
    }

    /**
     * Will attempt to connect to the {@link #host} on port {@link #port}. If we are current connected, will
     * {@link #disconnect()} first. Once connected, the {@link #writer} and {@link #reader} will be created, the
     * {@link #dispatcher} and {@link #responseReader} will be started.
     *
     * @throws java.io.IOException if an exception occurs during the connection attempt
     */
    public void connect() throws IOException {
        disconnect();

        client = new Socket(host, port);
        client.setKeepAlive(true);
        client.setSoTimeout(1000); // allow reader to check to see if it should stop every 1 second

        logger.debug("Connecting to {}:{}", host, port);
        writer = new PrintStream(client.getOutputStream());
        reader = new BufferedReader(new InputStreamReader(client.getInputStream()));

        new Thread(responseReader, "OH-binding-" + uid + "-responseReader").start();
        new Thread(dispatcher, "OH-binding-" + uid + "-dispatcher").start();
    }

    /**
     * Disconnects from the {@link #host} if we are {@link #isConnected()}. The {@link #writer}, {@link #reader} and
     * {@link #client}
     * will be closed and set to null. The {@link #dispatcher} and {@link #responseReader} will be stopped, the
     * {@link #callback} will be nulled and the {@link #responsesQueue} will be cleared.
     *
     * @throws java.io.IOException if an exception occurs during the disconnect attempt
     */
    public void disconnect() throws IOException {
        if (isConnected()) {
            logger.debug("Disconnecting from {}:{}", host, port);

            dispatcher.stopRunning();
            responseReader.stopRunning();

            writer.close();
            writer = null;

            reader.close();
            reader = null;

            client.close();
            client = null;

            callback.set(null);
            responsesQueue.clear();
        }
    }

    /**
     * Returns true if we are connected ({@link #client} is not null and is connected)
     *
     * @return true if connected, false otherwise
     */
    public boolean isConnected() {
        return client != null && client.isConnected();
    }

    /**
     * Sends the specified command to the underlying socket
     *
     * @param command a non-null, non-empty command
     * @throws java.io.IOException an exception that occurred while sending
     */
    public synchronized void sendCommand(String command) throws IOException {
        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }

        if (!isConnected()) {
            throw new IOException("Cannot send message - disconnected");
        }

        logger.debug("Sending Command: '{}'", command);
        writer.println(command + "\n"); // as pre spec - each command must have a newline
        writer.flush();
    }

    /**
     * This is the runnable that will read from the socket and add messages to the responses queue (to be processed by
     * the dispatcher)
     *
     * @author Tim Roberts
     *
     */
    private class ResponseReader implements Runnable {

        /**
         * Whether the reader is currently rRunning
         */
        private final AtomicBoolean isRunning = new AtomicBoolean(false);

        /**
         * Locking to allow proper shutdown of the reader
         */
        private final Lock rLock = new ReentrantLock();
        private final Condition rRunning = rLock.newCondition();

        /**
         * Stops the reader. Will wait 5 seconds for the runnable to stop (should stop within 1 second based on the
         * setSOTimeout)
         */
        public void stopRunning() {
            rLock.lock();
            try {
                if (isRunning.getAndSet(false)) {
                    if (!rRunning.await(5, TimeUnit.SECONDS)) {
                        logger.warn("Waited too long for dispatcher to finish");
                    }
                }
            } catch (InterruptedException e) {
                // shouldn't happen
            } finally {
                rLock.unlock();
            }
        }

        /**
         * Runs the logic to read from the socket until {@link #isRunning} is false. A 'response' is anything that ends
         * with a carriage-return/newline combo. Additionally, the special "login" prompts are
         * treated as responses for purposes of logging in.
         */
        @Override
        public void run() {
            final StringBuilder sb = new StringBuilder(100);
            int c;

            isRunning.set(true);
            responsesQueue.clear();

            while (isRunning.get()) {
                try {
                    // if reader is null, sleep and try again
                    if (reader == null) {
                        Thread.sleep(250);
                        continue;
                    }

                    c = reader.read();
                    if (c == -1) {
                        responsesQueue.put(new IOException("server closed connection"));
                        isRunning.set(false);
                        break;
                    }
                    final char ch = (char) c;
                    sb.append(ch);
                    if (ch == '\n' || ch == ' ') {
                        final String str = sb.toString();
                        if (str.endsWith("\r\n") || str.endsWith("login: ")) {
                            sb.setLength(0);
                            final String response = str.substring(0, str.length() - 2);
                            logger.debug("Received response: {}", response);
                            responsesQueue.put(response);
                        }
                    }
                    // logger.debug(">>> reading: " + sb + ":" + (int) ch);
                } catch (SocketTimeoutException e) {
                    // do nothing - we expect this (setSOTimeout) to check the _isReading
                } catch (InterruptedException e) {
                    // Do nothing - probably shutting down
                } catch (IOException e) {
                    try {
                        isRunning.set(false);
                        responsesQueue.put(e);
                    } catch (InterruptedException e1) {
                        // Do nothing - probably shutting down
                    }
                }
            }

            rLock.lock();
            try {
                rRunning.signalAll();
            } finally {
                rLock.unlock();
            }
        }
    }

    /**
     * The dispatcher runnable is responsible for reading the response queue and dispatching it to the current callable.
     * Since the dispatcher is ONLY started when a callable is set, responses may pile up in the queue and be dispatched
     * when a callable is set. Unlike the socket reader, this can be assigned to another thread (no state outside of the
     * class).
     *
     * @author Tim Roberts
     */
    private class Dispatcher implements Runnable {

        /**
         * Whether the dispatcher is rRunning or not
         */
        private final AtomicBoolean dispatcherRunning = new AtomicBoolean(false);

        /**
         * Locking to allow proper shutdown of the reader
         */
        private final Lock dLock = new ReentrantLock();
        private final Condition dRunning = dLock.newCondition();

        /**
         * Stops the reader. Will wait 5 seconds for the runnable to stop (should stop within 1 second based on the poll
         * timeout below)
         */
        public void stopRunning() {
            dLock.lock();
            try {
                if (dispatcherRunning.getAndSet(false)) {
                    if (!dRunning.await(5, TimeUnit.SECONDS)) {
                        logger.warn("Waited too long for dispatcher to finish");
                    }
                }
            } catch (InterruptedException e) {
                // do nothing
            } finally {
                dLock.unlock();
            }
        }

        /**
         * Runs the logic to dispatch any responses to the current callback until {@link #isRunning} is false.
         */
        @Override
        public void run() {
            dispatcherRunning.set(true);
            while (dispatcherRunning.get()) {
                try {
                    final SocketSessionCallback ssCallback = callback.get();

                    // if callback is null, we don't want to start dispatching yet.
                    if (ssCallback == null) {
                        Thread.sleep(250);
                        continue;
                    }

                    final Object response = responsesQueue.poll(1, TimeUnit.SECONDS);

                    if (response != null) {
                        if (response instanceof String) {
                            try {
                                logger.debug("Dispatching response: {}", response);
                                ssCallback.responseReceived((String) response);
                            } catch (Exception e) {
                                logger.warn("Exception occurred processing the response '{}': ", response, e);
                            }
                        } else if (response instanceof Exception) {
                            logger.debug("Dispatching exception: {}", response);
                            ssCallback.responseException((Exception) response);
                        } else {
                            logger.error("Unknown response class: {}", response);
                        }
                    }
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }

            dLock.lock();
            try {
                // Signal that we are done
                dRunning.signalAll();
            } finally {
                dLock.unlock();
            }
        }
    }
}
