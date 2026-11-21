/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.tibberpulselocal.internal.stream;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A scripted test double of the bridge. It listens on the loopback address with an ephemeral port, reads the request
 * of each connection, records it, and then runs the behaviour for that connection. The behaviours are used in order;
 * the last one repeats. All threads are daemons and every wait is bounded.
 *
 * @author Simon Spielmann - Initial contribution
 */
@NonNullByDefault
final class FakeBridge implements AutoCloseable {

    /** What the bridge does with one connection after it has read the request. */
    @FunctionalInterface
    interface Behaviour {
        void serve(Connection connection) throws Exception;
    }

    /** One accepted connection. */
    static final class Connection {
        private final Socket socket;
        private final String request;

        Connection(Socket socket, String request) {
            this.socket = socket;
            this.request = request;
        }

        String request() {
            return request;
        }

        void send(byte[] data) throws IOException {
            OutputStream out = socket.getOutputStream();
            out.write(data);
            out.flush();
        }

        /** Sends the data in chunks, each in its own TCP segment (no delay, no sleep). */
        void sendChunked(byte[] data, int chunk) throws IOException {
            socket.setTcpNoDelay(true);
            for (int i = 0; i < data.length; i += chunk) {
                send(Arrays.copyOfRange(data, i, Math.min(data.length, i + chunk)));
            }
        }

        /** Reads what the client sends until it closes the connection or the bridge shuts down. */
        byte[] readUntilClosed() {
            ByteArrayOutputStream received = new ByteArrayOutputStream();
            try {
                InputStream in = socket.getInputStream();
                byte[] buffer = new byte[256];
                int n;
                while ((n = in.read(buffer)) >= 0) {
                    received.write(buffer, 0, n);
                }
            } catch (IOException e) {
                // The client or the bridge closed the connection.
            }
            return received.toByteArray();
        }

        /** Reads exactly {@code count} bytes sent by the client. */
        byte[] readExactly(int count) throws IOException {
            byte[] data = socket.getInputStream().readNBytes(count);
            if (data.length < count) {
                throw new EOFException();
            }
            return data;
        }

        void reset() throws IOException {
            socket.setSoLinger(true, 0);
            socket.close();
        }
    }

    private final ServerSocket server;
    private final Behaviour[] behaviours;
    private final List<Socket> sockets = new CopyOnWriteArrayList<>();
    private final BlockingQueue<String> requests = new LinkedBlockingQueue<>();
    private final AtomicInteger accepted = new AtomicInteger();
    private final AtomicInteger open = new AtomicInteger();
    private final AtomicInteger maxOpen = new AtomicInteger();
    private final AtomicInteger requestsSeen = new AtomicInteger();
    private final Object openLock = new Object();

    FakeBridge(Behaviour... behaviours) throws IOException {
        this.behaviours = behaviours.length == 0 ? new Behaviour[] { silent() } : behaviours;
        this.server = new ServerSocket(0, 50, InetAddress.getLoopbackAddress());
        Thread acceptor = new Thread(this::acceptLoop, "fake-bridge-accept");
        acceptor.setDaemon(true);
        acceptor.start();
    }

    int port() {
        return server.getLocalPort();
    }

    int accepted() {
        return accepted.get();
    }

    int maxOpen() {
        return maxOpen.get();
    }

    /** @return the head of the next unseen request, or null after the timeout */
    @Nullable
    String nextRequest(long timeoutMillis) throws InterruptedException {
        return requests.poll(timeoutMillis, TimeUnit.MILLISECONDS);
    }

    /** Waits until the given number of connections was accepted and their requests were read. */
    boolean awaitRequests(int count, long timeoutMillis) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        synchronized (openLock) {
            while (requestsSeen.get() < count) {
                long left = deadline - System.nanoTime();
                if (left <= 0) {
                    return false;
                }
                TimeUnit.NANOSECONDS.timedWait(openLock, left);
            }
        }
        return true;
    }

    /** Waits until no connection is open any more, that is, the client closed all of them. */
    boolean awaitNoOpenConnections(long timeoutMillis) throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        synchronized (openLock) {
            while (open.get() > 0) {
                long left = deadline - System.nanoTime();
                if (left <= 0) {
                    return false;
                }
                TimeUnit.NANOSECONDS.timedWait(openLock, left);
            }
        }
        return true;
    }

    private void acceptLoop() {
        while (!server.isClosed()) {
            try {
                Socket socket = server.accept();
                int index = accepted.getAndIncrement();
                synchronized (openLock) {
                    int now = open.incrementAndGet();
                    maxOpen.accumulateAndGet(now, Math::max);
                }
                sockets.add(socket);
                Thread handler = new Thread(() -> handle(socket, index), "fake-bridge-connection-" + index);
                handler.setDaemon(true);
                handler.start();
            } catch (IOException e) {
                return;
            }
        }
    }

    private void handle(Socket socket, int index) {
        try {
            String request = readRequest(socket);
            requests.add(request);
            synchronized (openLock) {
                requestsSeen.incrementAndGet();
                openLock.notifyAll();
            }
            Behaviour behaviour = behaviours[Math.min(index, behaviours.length - 1)];
            behaviour.serve(new Connection(socket, request));
        } catch (Exception e) {
            // The client went away or the bridge was closed; the connection is finished either way.
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
            sockets.remove(socket);
            synchronized (openLock) {
                open.decrementAndGet();
                openLock.notifyAll();
            }
        }
    }

    private static String readRequest(Socket socket) throws IOException {
        InputStream in = socket.getInputStream();
        ByteArrayOutputStream head = new ByteArrayOutputStream();
        int state = 0;
        while (state < 4) {
            int b = in.read();
            if (b < 0) {
                break;
            }
            head.write(b);
            if (b == ((state & 1) == 0 ? '\r' : '\n')) {
                state++;
            } else {
                state = b == '\r' ? 1 : 0;
            }
        }
        return head.toString(StandardCharsets.ISO_8859_1);
    }

    @Override
    public void close() {
        try {
            server.close();
        } catch (IOException e) {
            // ignore
        }
        for (Socket socket : sockets) {
            try {
                socket.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }

    // Ready-made behaviours

    /** Accepts the request, sends nothing, and keeps the connection until the client closes it. */
    static Behaviour silent() {
        return Connection::readUntilClosed;
    }

    /** Sends the data, then keeps the connection until the client closes it. */
    static Behaviour sending(byte[]... chunks) {
        return connection -> {
            for (byte[] chunk : chunks) {
                connection.send(chunk);
            }
            connection.readUntilClosed();
        };
    }

    /** Sends the data, then closes the connection (a plain close, no close frame). */
    static Behaviour sendingThenClosing(byte[]... chunks) {
        return connection -> {
            for (byte[] chunk : chunks) {
                connection.send(chunk);
            }
        };
    }

    /** Closes the connection right after the request. */
    static Behaviour closing() {
        return connection -> {
        };
    }

    /** Resets the connection right after the request. */
    static Behaviour resetting() {
        return Connection::reset;
    }

    /** Answers with the given raw HTTP text and then keeps the connection until the client closes it. */
    static Behaviour answering(String http, byte[]... thenFrames) {
        return connection -> {
            connection.send(http.getBytes(StandardCharsets.ISO_8859_1));
            for (byte[] frame : thenFrames) {
                connection.send(frame);
            }
            connection.readUntilClosed();
        };
    }

    /** Answers with the given raw HTTP text and closes the connection. */
    static Behaviour answeringThenClosing(String http) {
        return connection -> connection.send(http.getBytes(StandardCharsets.ISO_8859_1));
    }
}
