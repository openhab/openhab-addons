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
package org.openhab.binding.russound.internal.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
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
     * The responses read from the {@link #responseReader}
     */
    private final BlockingQueue<Object> responses = new ArrayBlockingQueue<>(50);

    /**
     * The {@link SocketSessionListener} that the {@link #dispatcher} will call
     */
    private List<SocketSessionListener> sessionListeners = new CopyOnWriteArrayList<>();

    /**
     * The thread dispatching responses - will be null if not connected
     */
    private Thread dispatchingThread = null;

    /**
     * The thread processing responses - will be null if not connected
     */
    private Thread responseThread = null;

    /**
     * Creates the socket session from the given host and port
     *
     * @param host a non-null, non-empty host/ip address
     * @param port the port number between 1 and 65535
     */
    public SocketChannelSession(String host, int port) {
        if (host == null || host.trim().length() == 0) {
            throw new IllegalArgumentException("Host cannot be null or empty");
        }

        if (port < 1 || port > 65535) {
            throw new IllegalArgumentException("Port must be between 1 and 65535");
        }
        this.host = host;
        this.port = port;
    }

    @Override
    public void addListener(SocketSessionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener cannot be null");
        }
        sessionListeners.add(listener);
    }

    @Override
    public void clearListeners() {
        sessionListeners.clear();
    }

    @Override
    public boolean removeListener(SocketSessionListener listener) {
        return sessionListeners.remove(listener);
    }

    @Override
    public void connect() throws IOException {
        connect(2000);
    }

    @Override
    public void connect(int timeout) throws IOException {
        disconnect();

        final SocketChannel channel = SocketChannel.open();
        channel.configureBlocking(true);

        logger.debug("Connecting to {}:{}", host, port);
        channel.socket().connect(new InetSocketAddress(host, port), timeout);

        socketChannel.set(channel);

        responses.clear();

        dispatchingThread = new Thread(new Dispatcher());
        responseThread = new Thread(new ResponseReader());

        dispatchingThread.start();
        responseThread.start();
    }

    @Override
    public void disconnect() throws IOException {
        if (isConnected()) {
            logger.debug("Disconnecting from {}:{}", host, port);

            final SocketChannel channel = socketChannel.getAndSet(null);
            channel.close();

            dispatchingThread.interrupt();
            dispatchingThread = null;

            responseThread.interrupt();
            responseThread = null;

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
        if (command == null) {
            throw new IllegalArgumentException("command cannot be null");
        }

        // if (command.trim().length() == 0) {
        // throw new IllegalArgumentException("Command cannot be empty");
        // }

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
         * Runs the logic to read from the socket until {@link #isRunning} is false. A 'response' is anything that ends
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

                    int bytesRead = channel.read(readBuffer);
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
                    // Ending thread execution
                    Thread.currentThread().interrupt();
                } catch (AsynchronousCloseException e) {
                    // socket was closed by another thread but interrupt our loop anyway
                    Thread.currentThread().interrupt();
                } catch (IOException e) {
                    // set before pushing the response since we'll likely call back our stop
                    Thread.currentThread().interrupt();

                    try {
                        responses.put(e);
                        break;
                    } catch (InterruptedException e1) {
                        // Do nothing - probably shutting down
                        // Since we set isRunning to false, will drop out of loop and end the thread
                    }
                }
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
         * Runs the logic to dispatch any responses to the current listeners until {@link #isRunning} is false.
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
                        if (response instanceof String stringCommand) {
                            logger.debug("Dispatching response: {}", response);
                            for (SocketSessionListener listener : listeners) {
                                listener.responseReceived(stringCommand);
                            }
                        } else if (response instanceof IOException ioException) {
                            logger.debug("Dispatching exception: {}", response);
                            for (SocketSessionListener listener : listeners) {
                                listener.responseException(ioException);
                            }
                        } else {
                            logger.warn("Unknown response class: {}", response);
                        }
                    }
                } catch (InterruptedException e) {
                    // Ending thread execution
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.debug("Uncaught exception {}: ", e.getMessage(), e);
                    Thread.currentThread().interrupt();
                }
            }
        }
    }
}
