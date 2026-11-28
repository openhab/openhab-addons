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
package org.openhab.binding.atv.internal.client.support.http;

import static org.junit.jupiter.api.Assertions.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.atv.internal.client.exceptions.OperationTimeoutError;

/**
 * Tests for {@link HttpConnection} against a local socket.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class HttpConnectionTest {

    private final ServerSocket server;
    private @Nullable HttpConnection connection;

    HttpConnectionTest() throws Exception {
        server = new ServerSocket(0, 1, InetAddress.getLoopbackAddress());
    }

    @AfterEach
    void tearDown() throws Exception {
        HttpConnection local = connection;
        if (local != null) {
            local.close();
        }
        server.close();
    }

    @Test
    void lateReplyToATimedOutRequestIsNotGivenToTheNextRequest() throws Exception {
        CountDownLatch bothReceived = new CountDownLatch(1);
        CompletableFuture<Void> device = CompletableFuture.runAsync(() -> {
            try (Socket socket = server.accept()) {
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.US_ASCII));
                awaitRequest(in);
                awaitRequest(in);
                bothReceived.await(10, TimeUnit.SECONDS);
                OutputStream out = socket.getOutputStream();
                out.write((response("first") + response("second")).getBytes(StandardCharsets.US_ASCII));
                out.flush();
                // Keep the socket open until the client has read both replies
                socket.getInputStream().read();
            } catch (Exception e) {
                throw new CompletionException(e);
            }
        });

        HttpConnection local = HttpConnection.connect(server.getInetAddress().getHostAddress(), server.getLocalPort())
                .get(10, TimeUnit.SECONDS);
        connection = local;

        CompletableFuture<HttpResponse> first = request(local, "/first", Duration.ofMillis(100));
        CompletionException timeout = assertThrows(CompletionException.class, first::join);
        assertInstanceOf(OperationTimeoutError.class, timeout.getCause());

        CompletableFuture<HttpResponse> second = request(local, "/second", Duration.ofSeconds(10));
        bothReceived.countDown();

        assertEquals("second", new String(second.get(10, TimeUnit.SECONDS).bodyBytes(), StandardCharsets.UTF_8));
        local.close();
        device.get(10, TimeUnit.SECONDS);
    }

    private static CompletableFuture<HttpResponse> request(HttpConnection connection, String path, Duration timeout) {
        return connection.sendAndReceive("GET", path, "HTTP/1.1", HttpParser.USER_AGENT, null, null, null, false,
                timeout);
    }

    /** Reads one request without a body, up to its blank line. */
    private static void awaitRequest(BufferedReader in) throws Exception {
        String line;
        do {
            line = in.readLine();
        } while (line != null && !line.isEmpty());
    }

    private static String response(String body) {
        return "HTTP/1.1 200 OK\r\nContent-Length: " + body.length() + "\r\n\r\n" + body;
    }
}
