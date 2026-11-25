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
package org.openhab.binding.bydbatterybox.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A battery stand-in on the loopback interface. It accepts one connection and answers each received request with the
 * next scripted reply. A reply of {@link #CLOSE} closes the connection instead, {@link #SILENCE} sends nothing.
 *
 * @author Michael Weger - Initial contribution
 */
@NonNullByDefault
public class FakeBattery implements AutoCloseable {

    public static final byte[] CLOSE = new byte[0];
    public static final byte[] SILENCE = new byte[] { 0 };

    private final ServerSocket server;
    private final BlockingQueue<byte[]> replies = new LinkedBlockingQueue<>();
    private final List<byte[]> requests = Collections.synchronizedList(new ArrayList<>());
    private final int segmentSize;
    private final Thread thread;
    private volatile @Nullable Socket client;

    private FakeBattery(int port, int segmentSize) throws IOException {
        this.segmentSize = segmentSize;
        server = new ServerSocket(port, 1, InetAddress.getLoopbackAddress());
        thread = new Thread(this::serve, "fake-battery");
        thread.setDaemon(true);
        thread.start();
    }

    /**
     * @param segmentSize replies are written in pieces of this size to exercise frame reassembly
     * @param replies the replies, in the order of the expected requests
     */
    public static FakeBattery start(int segmentSize, byte[]... replies) throws IOException {
        return startOnPort(0, segmentSize, replies);
    }

    /**
     * Starts the fake battery on a given port, e.g. one a client has already failed to connect to.
     *
     * @param port the port, or 0 for any free port
     */
    public static FakeBattery startOnPort(int port, int segmentSize, byte[]... replies) throws IOException {
        FakeBattery battery = new FakeBattery(port, segmentSize);
        battery.replies.addAll(Arrays.asList(replies));
        return battery;
    }

    public int port() {
        return server.getLocalPort();
    }

    public List<byte[]> requests() {
        return List.copyOf(requests);
    }

    private void serve() {
        try (Socket socket = server.accept()) {
            client = socket;
            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();
            byte[] buffer = new byte[64];
            while (true) {
                int n = in.read(buffer);
                if (n < 0) {
                    return;
                }
                requests.add(Arrays.copyOf(buffer, n));
                byte[] reply = replies.poll();
                if (reply == null || reply == CLOSE) {
                    return;
                }
                if (reply == SILENCE) {
                    continue;
                }
                for (int offset = 0; offset < reply.length; offset += segmentSize) {
                    out.write(reply, offset, Math.min(segmentSize, reply.length - offset));
                    out.flush();
                }
            }
        } catch (IOException e) {
            // connection closed by the test
        }
    }

    @Override
    public void close() throws IOException {
        Socket socket = client;
        if (socket != null) {
            socket.close();
        }
        server.close();
    }
}
