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
package org.openhab.binding.atlona.internal.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a restartable socket connection to the underlying telnet session. Commands can be sent via
 * {@link #sendCommand(String)} and responses will be received on any {@link SocketSessionListener}. This implementation
 * of {@link SocketSession} communicates using a {@link SocketChannel} connection.
 *
 * @author Tim Roberts - Initial contribution
 */
public class SocketChannelSession implements SocketSession {
    private final Logger logger = LoggerFactory.getLogger(SocketChannelSession.class);

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
    private final AtomicReference<SocketChannel> socketChannel = new AtomicReference<>();

    /**
     * The {@link ResponseReader} that will be used to read from {@link #_readBuffer}
     */
    private final ResponseReader responseReader = new ResponseReader();

    /**
     * The responses read from the {@link #responseReader}
     */
    private final BlockingQueue<Object> responses = new ArrayBlockingQueue<>(50);

    /**
     * The dispatcher of responses from {@link #responses}
     */
    private final Dispatcher dispatcher = new Dispatcher();

    /**
     * The {@link SocketSessionListener} that the {@link #dispatcher} will call
     */
    private List<SocketSessionListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * Creates the socket session from the given host and port
     *
     * @param uid the thing uid of the calling thing
     * @param host a non-null, non-empty host/ip address
     * @param port the port number between 1 and 65535
     */
    public SocketChannelSession(String uid, String host, int port) {
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

    @Override
    public void addListener(SocketSessionListener listener) {
        listeners.add(listener);
    }

    @Override
    public void clearListeners() {
        listeners.clear();
    }

    @Override
    public boolean removeListener(SocketSessionListener listener) {
        return listeners.remove(listener);
    }

    @Override
    public void connect() throws IOException {
        disconnect();

        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(true);

        logger.debug("Connecting to {}:{}", host, port);
        channel.connect(new InetSocketAddress(host, port));

        logger.debug("Waiting for connect");
        while (!channel.finishConnect()) {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
            }
        }

        socketChannel.set(channel);
        Thread dispatcherThread = new Thread(dispatcher, "OH-binding-" + uid + "-dispatcher");
        dispatcherThread.setDaemon(true);
        dispatcherThread.start();
        Thread responseReaderThread = new Thread(responseReader, "OH-binding-" + uid + "-responseReader");
        responseReaderThread.setDaemon(true);
        responseReaderThread.start();
    }

    @Override
    public void disconnect() throws IOException {
        if (isConnected()) {
            logger.debug("Disconnecting from {}:{}", host, port);

            final SocketChannel channel = socketChannel.getAndSet(null);
            channel.close();

            dispatcher.stopRunning();
            responseReader.stopRunning();

            responses.clear();
        }
    }

    @Override
    public boolean isConnected() {
        final SocketChannel channel = socketChannel.get();
        return channel != null && channel.isConnected();
    }

    @Override
    public synchronized void sendCommand(String command) throws IOException {
        if (!isConnected()) {
            throw new IOException("Cannot send message - disconnected");
        }

        ByteBuffer toSend = ByteBuffer.wrap((command + "\r\n").getBytes());

        final SocketChannel channel = socketChannel.get();
        if (channel == null) {
            logger.debug("Cannot send command '{}' - socket channel was closed", command);
        } else {
            logger.debug("Sending Command: '{}'", command);
            channel.write(toSend);
        }
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
         * Whether the reader is currently running
         */
        private final AtomicBoolean isRunning = new AtomicBoolean(false);

        /**
         * Locking to allow proper shutdown of the reader
         */
        private final CountDownLatch running = new CountDownLatch(1);

        /**
         * Stops the reader. Will wait 5 seconds for the runnable to stop
         */
        public void stopRunning() {
            if (isRunning.getAndSet(false)) {
                try {
                    if (!running.await(5, TimeUnit.SECONDS)) {
                        logger.warn("Waited too long for response reader to finish");
                    }
                } catch (InterruptedException e) {
                    // Do nothing
                }
            }
        }

        /**
         * Runs the logic to read from the socket until {@link #isRunning} is false. A 'response' is anything that ends
         * with a carriage-return/newline combo. Additionally, the special "Login: " and "Password: " prompts are
         * treated as responses for purposes of logging in.
         */
        @Override
        public void run() {
            final StringBuilder sb = new StringBuilder(100);
            final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

            isRunning.set(true);
            responses.clear();

            while (isRunning.get()) {
                try {
                    // if reader is null, sleep and try again
                    if (readBuffer == null) {
                        Thread.sleep(250);
                        continue;
                    }

                    final SocketChannel channel = socketChannel.get();
                    if (channel == null) {
                        // socket was closed
                        isRunning.set(false);
                        break;
                    }

                    int bytesRead = channel.read(readBuffer);
                    if (bytesRead == -1) {
                        responses.put(new IOException("server closed connection"));
                        isRunning.set(false);
                        break;
                    } else if (bytesRead == 0) {
                        readBuffer.clear();
                        continue;
                    }

                    readBuffer.flip();
                    while (readBuffer.hasRemaining()) {
                        final char ch = (char) readBuffer.get();
                        sb.append(ch);
                        if (ch == '\n' || ch == ' ') {
                            final String str = sb.toString();
                            if (str.endsWith("\r\n") || str.endsWith("Login: ") || str.endsWith("Password: ")) {
                                sb.setLength(0);
                                final String response = str.substring(0, str.length() - 2);
                                responses.put(response);
                            }
                        }
                    }

                    readBuffer.flip();
                } catch (InterruptedException e) {
                    // Do nothing - probably shutting down
                } catch (AsynchronousCloseException e) {
                    // socket was definitely closed by another thread
                } catch (IOException e) {
                    try {
                        isRunning.set(false);
                        responses.put(e);
                    } catch (InterruptedException e1) {
                        // Do nothing - probably shutting down
                    }
                }
            }

            running.countDown();
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
         * Whether the dispatcher is running or not
         */
        private final AtomicBoolean isRunning = new AtomicBoolean(false);

        /**
         * Locking to allow proper shutdown of the reader
         */
        private final CountDownLatch running = new CountDownLatch(1);

        /**
         * Whether the dispatcher is currently processing a message
         */
        private final AtomicReference<Thread> processingThread = new AtomicReference<>();

        /**
         * Stops the reader. Will wait 5 seconds for the runnable to stop (should stop within 1 second based on the poll
         * timeout below)
         */
        @SuppressWarnings("PMD.CompareObjectsWithEquals")
        public void stopRunning() {
            if (isRunning.getAndSet(false)) {
                // only wait if stopRunning didn't get called as part of processing a message
                // (which would happen if we are processing an exception that forced a session close)
                final Thread processingThread = this.processingThread.get();
                if (processingThread != null && Thread.currentThread() != processingThread) {
                    try {
                        if (!running.await(5, TimeUnit.SECONDS)) {
                            logger.warn("Waited too long for dispatcher to finish");
                        }
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                }
            }
        }

        /**
         * Runs the logic to dispatch any responses to the current listeners until {@link #isRunning} is false.
         */
        @Override
        public void run() {
            processingThread.set(Thread.currentThread());

            isRunning.set(true);
            while (isRunning.get()) {
                try {
                    // if no listeners, we don't want to start dispatching yet.
                    if (listeners.isEmpty()) {
                        Thread.sleep(250);
                        continue;
                    }

                    final Object response = responses.poll(1, TimeUnit.SECONDS);

                    if (response != null) {
                        if (response instanceof String stringResponse) {
                            try {
                                logger.debug("Dispatching response: {}", response);
                                final SocketSessionListener[] listeners = SocketChannelSession.this.listeners
                                        .toArray(new SocketSessionListener[0]);
                                for (SocketSessionListener listener : listeners) {
                                    listener.responseReceived(stringResponse);
                                }
                            } catch (Exception e) {
                                logger.warn("Exception occurred processing the response '{}': ", response, e);
                            }
                        } else if (response instanceof Exception exceptionResponse) {
                            logger.debug("Dispatching exception: {}", response);
                            final SocketSessionListener[] listeners = SocketChannelSession.this.listeners
                                    .toArray(new SocketSessionListener[0]);
                            for (SocketSessionListener listener : listeners) {
                                listener.responseException(exceptionResponse);
                            }
                        } else {
                            logger.warn("Unknown response class: {}", response);
                        }
                    }
                } catch (InterruptedException e) {
                    // Do nothing
                } catch (Exception e) {
                    logger.debug("Uncaught exception {}", e.getMessage(), e);
                    break;
                }
            }
            isRunning.set(false);
            processingThread.set(null);
            running.countDown();
        }
    }
}
