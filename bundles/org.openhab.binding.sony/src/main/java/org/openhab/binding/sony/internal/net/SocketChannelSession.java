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
package org.openhab.binding.sony.internal.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.lang.Validate;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represents a restartable socket connection to the underlying telnet session. Commands can be sent via
 * {@link #sendCommand(String)} and responses will be received on any {@link SocketSessionListener}. This implementation
 * of {@link SocketSession} communicates using a {@link SocketChannel} connection.
 *
 * @author Tim Roberts - Initial contribution
 */
@NonNullByDefault
public class SocketChannelSession implements SocketSession {
    /** The logger */
    private final Logger logger = LoggerFactory.getLogger(SocketChannelSession.class);

    /** The host/ip address to connect to */
    private final String host;

    /** The port to connect to */
    private final int port;

    /**
     * The actual socket being used. Will be null if not connected
     */
    private final AtomicReference<@Nullable SocketChannel> socketChannel = new AtomicReference<>();

    /** The responses read from the {@link #responseThread}. */
    private final BlockingQueue<Object> responses = new ArrayBlockingQueue<Object>(50);

    /** The {@link SocketSessionListener} that the {@link #dispatchingThread} will call. */
    private final List<SocketSessionListener> sessionListeners = new CopyOnWriteArrayList<>();

    /** Lock controlling access to dispatching/response threads */
    private final Lock threadLock = new ReentrantLock();

    /** The thread dispatching responses - will be null if not connected. */
    private @Nullable Thread dispatchingThread = null;

    /** The thread processing responses - will be null if not connected. */
    private @Nullable Thread responseThread = null;

    /**
     * Creates the socket session from the given host and port.
     *
     * @param host a non-null, non-empty host/ip address
     * @param port the port number between 1 and 65535
     */
    public SocketChannelSession(final String host, final int port) {
        Validate.notEmpty(host, "host cannot be null");

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.host = host;
        this.port = port;
    }

    @Override
    public void addListener(final SocketSessionListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        sessionListeners.add(listener);
    }

    @Override
    public void clearListeners() {
        sessionListeners.clear();
    }

    @Override
    public boolean removeListener(final SocketSessionListener listener) {
        Objects.requireNonNull(listener, "listener cannot be null");
        return sessionListeners.remove(listener);
    }

    @Override
    public void connect() throws IOException {
        connect(2000);
    }

    @Override
    public void connect(final int timeout) throws IOException {
        threadLock.lock();
        try {
            disconnect();

            final SocketChannel channel = SocketChannel.open();
            channel.configureBlocking(true);

            logger.debug("Connecting to {}:{}", host, port);
            channel.socket().connect(new InetSocketAddress(host, port), timeout);

            socketChannel.set(channel);

            responses.clear();

            dispatchingThread = new Thread(new Dispatcher());
            responseThread = new Thread(new ResponseReader());

            dispatchingThread.setDaemon(true);
            responseThread.setDaemon(true);

            dispatchingThread.start();
            responseThread.start();
        } finally {
            threadLock.unlock();
        }
    }

    @Override
    public void disconnect() throws IOException {
        if (isConnected()) {
            logger.debug("Disconnecting from {}:{}", host, port);

            final SocketChannel channel = socketChannel.getAndSet(null);
            if (channel != null) {
                channel.close();
            }

            threadLock.lock();
            try {
                if (dispatchingThread != null) {
                    dispatchingThread.interrupt();
                    dispatchingThread = null;
                }

                if (responseThread != null) {
                    responseThread.interrupt();
                    responseThread = null;
                }
            } finally {
                threadLock.unlock();
            }

            responses.clear();
        }
    }

    @Override
    public boolean isConnected() {
        final SocketChannel channel = socketChannel.get();
        return channel != null && channel.isConnected();
    }

    @Override
    public synchronized void sendCommand(final String command) throws IOException {
        Objects.requireNonNull(command, "command cannot be empty");

        if (!isConnected()) {
            throw new IOException("Cannot send message - disconnected");
        }

        final ByteBuffer toSend = ByteBuffer.wrap((command + "\r\n").getBytes());

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
     * the dispatcher).
     *
     * @author Tim Roberts
     */
    @NonNullByDefault
    private class ResponseReader implements Runnable {

        /**
         * Runs the logic to read from the socket until interrupted. A 'response' is anything that ends
         * with a carriage-return/newline combo. Additionally, the special "Login: " and "Password: " prompts are
         * treated as responses for purposes of logging in.
         */
        @Override
        public void run() {
            final StringBuilder sb = new StringBuilder(100);
            final ByteBuffer readBuffer = ByteBuffer.allocate(1024);

            responses.clear();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    // if reader is null, sleep and try again
                    if (readBuffer == null) {
                        Thread.sleep(250);
                        continue;
                    }

                    final SocketChannel channel = socketChannel.get();
                    if (channel == null) {
                        // socket was closed
                        Thread.currentThread().interrupt();
                        break;
                    }

                    final int bytesRead = channel.read(readBuffer);
                    if (bytesRead == -1) {
                        responses.put(new IOException("server closed connection"));
                        break;
                    } else if (bytesRead == 0) {
                        readBuffer.clear();
                        continue;
                    }

                    readBuffer.flip();
                    while (readBuffer.hasRemaining()) {
                        final char ch = (char) readBuffer.get();
                        if (ch == '\n') {
                            final String str = sb.toString();
                            sb.setLength(0);
                            responses.put(str.trim());
                        } else {
                            sb.append(ch);
                        }
                    }

                    readBuffer.flip();

                } catch (final InterruptedException e) {
                    // Ending thread execution
                    Thread.currentThread().interrupt(); // sets isInterrupted field
                } catch (final AsynchronousCloseException e) {
                    // socket was closed by another thread but interrupt our loop anyway
                    Thread.currentThread().interrupt();
                } catch (final IOException e) {
                    // set before pushing the response since we'll likely call back our disconnect
                    Thread.currentThread().interrupt();

                    try {
                        responses.put(e);
                        break;
                    } catch (final InterruptedException e1) {
                        // Do nothing - probably shutting down
                        break;
                    }
                }
            }
            logger.debug("Response thread ending");
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
    @NonNullByDefault
    private class Dispatcher implements Runnable {
        /**
         * Runs the logic to dispatch any responses to the current listeners until interrupted
         */
        @Override
        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    final SocketSessionListener[] listeners = sessionListeners.toArray(new SocketSessionListener[0]);

                    // if no listeners, we don't want to start dispatching yet.
                    if (listeners.length == 0) {
                        Thread.sleep(250);
                        continue;
                    }

                    final Object response = responses.poll(1, TimeUnit.SECONDS);

                    if (response != null) {
                        if (response instanceof String) {
                            logger.debug("Dispatching response: {}", response);
                            for (final SocketSessionListener listener : listeners) {
                                listener.responseReceived((String) response);
                            }
                        } else if (response instanceof IOException) {
                            logger.debug("Dispatching exception: {}", response);
                            for (final SocketSessionListener listener : listeners) {
                                listener.responseException((IOException) response);
                            }
                        } else {
                            logger.debug("Unknown response class: {}", response);
                        }
                    }
                } catch (final InterruptedException e) {
                    // Ending thread execution
                    Thread.currentThread().interrupt(); // sets isInterrupted field
                } catch (final Exception e) {
                    logger.debug("Uncaught exception {}", e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
            logger.debug("Dispatch thread ending");
        }
    }
}
