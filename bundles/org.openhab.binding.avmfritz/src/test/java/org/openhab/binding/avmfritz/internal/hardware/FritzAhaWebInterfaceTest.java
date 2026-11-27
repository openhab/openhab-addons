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
package org.openhab.binding.avmfritz.internal.hardware;

import static org.eclipse.jetty.http.HttpStatus.FORBIDDEN_403;
import static org.eclipse.jetty.http.HttpStatus.OK_200;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.avmfritz.internal.config.AVMFritzBoxConfiguration;
import org.openhab.binding.avmfritz.internal.handler.AVMFritzBaseBridgeHandler;
import org.openhab.binding.avmfritz.internal.hardware.callbacks.FritzAhaReauthCallback;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

/**
 * Unit tests for {@link FritzAhaWebInterface}.
 *
 * @author Leo Siepel - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
public class FritzAhaWebInterfaceTest {

    private @Mock @NonNullByDefault({}) AVMFritzBaseBridgeHandler handler;

    @Test
    public void authenticationDoesNotBlock() throws Exception {
        AVMFritzBoxConfiguration config = new AVMFritzBoxConfiguration();
        config.ipAddress = "127.0.0.1";
        config.password = "password";
        config.syncTimeout = TimeUnit.SECONDS.toMillis(10);

        CountDownLatch requestAccepted = new CountDownLatch(1);
        CountDownLatch releaseServer = new CountDownLatch(1);
        ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            config.port = serverSocket.getLocalPort();
            serverExecutor.submit(() -> acceptWithoutResponding(serverSocket, requestAccepted, releaseServer));

            HttpClient httpClient = new HttpClient();
            httpClient.start();
            try {
                FritzAhaWebInterface webInterface = new FritzAhaWebInterface(config, handler, httpClient);
                try {
                    assertTimeoutPreemptively(Duration.ofSeconds(2), webInterface::authenticate);
                    assertTrue(requestAccepted.await(2, TimeUnit.SECONDS));
                } finally {
                    webInterface.dispose();
                }
            } finally {
                releaseServer.countDown();
                httpClient.stop();
            }
        } finally {
            releaseServer.countDown();
            serverExecutor.shutdownNow();
        }
    }

    @Test
    public void authenticationCompletesWhenResponseProcessingFails() throws Exception {
        AVMFritzBoxConfiguration config = new AVMFritzBoxConfiguration();
        config.ipAddress = "127.0.0.1";
        config.password = "password";

        ExecutorService serverExecutor = Executors.newSingleThreadExecutor();
        try (ServerSocket serverSocket = new ServerSocket(0)) {
            config.port = serverSocket.getLocalPort();
            serverExecutor.submit(() -> respondWithChallenge(serverSocket));

            HttpClient httpClient = new HttpClient();
            httpClient.start();
            FritzAhaWebInterface webInterface = new FritzAhaWebInterface(config, handler, httpClient) {
                @Override
                protected String createResponse(String challenge) {
                    throw new IllegalArgumentException("Invalid challenge");
                }
            };
            try {
                CompletableFuture<Boolean> authentication = webInterface.authenticate();
                assertFalse(authentication.get(2, TimeUnit.SECONDS));
            } finally {
                webInterface.dispose();
                httpClient.stop();
            }
        } finally {
            serverExecutor.shutdownNow();
        }
    }

    @Test
    public void authenticationRemainsCurrentUntilStatusUpdateCompletes() {
        AVMFritzBoxConfiguration config = new AVMFritzBoxConfiguration();
        FritzAhaWebInterface webInterface = new FritzAhaWebInterface(config, handler, new HttpClient());
        AtomicBoolean firstStatusUpdate = new AtomicBoolean(true);
        AtomicReference<CompletableFuture<Boolean>> authenticationDuringStatusUpdate = new AtomicReference<>();
        doAnswer(invocation -> {
            if (firstStatusUpdate.getAndSet(false)) {
                authenticationDuringStatusUpdate.set(webInterface.authenticate());
            }
            return null;
        }).when(handler).setStatusInfo(any(), any(), any());

        CompletableFuture<Boolean> authentication = webInterface.authenticate();

        assertSame(authentication, authenticationDuringStatusUpdate.get());
        webInterface.dispose();
    }

    @Test
    public void emptyForbiddenResponseRefreshesSessionWithoutInvalidatingItsReplacement() throws Exception {
        String initialSid = "0123456789abcdef";
        String newSid = "fedcba9876543210";
        AtomicInteger logins = new AtomicInteger();
        CountDownLatch oldRequestReceived = new CountDownLatch(1);
        CountDownLatch releaseOldResponse = new CountDownLatch(1);

        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        ExecutorService serverExecutor = Executors.newFixedThreadPool(2);
        try {
            server.setExecutor(serverExecutor);
            server.createContext("/login_sid.lua", exchange -> {
                try (exchange) {
                    String query = exchange.getRequestURI().getQuery();
                    if (query == null) {
                        sendResponse(exchange, OK_200, "<SessionInfo><SID>0000000000000000</SID>"
                                + "<Challenge>12345678</Challenge></SessionInfo>");
                    } else {
                        String sid = logins.incrementAndGet() == 1 ? initialSid : newSid;
                        sendResponse(exchange, OK_200, "<SessionInfo><SID>" + sid
                                + "</SID><Rights><Name>HomeAuto</Name><Access>2</Access></Rights></SessionInfo>");
                    }
                }
            });
            server.createContext("/" + FritzAhaReauthCallback.WEBSERVICE_PATH, exchange -> {
                try (exchange) {
                    String query = exchange.getRequestURI().getQuery();
                    if (query != null && query.endsWith("sid=" + newSid)) {
                        sendResponse(exchange, OK_200, "ok");
                        return;
                    }
                    if (("switchcmd=delayed&sid=" + initialSid).equals(query)) {
                        oldRequestReceived.countDown();
                        if (!releaseOldResponse.await(10, TimeUnit.SECONDS)) {
                            return;
                        }
                    }
                    sendResponse(exchange, FORBIDDEN_403, "");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            server.start();

            AVMFritzBoxConfiguration config = new AVMFritzBoxConfiguration();
            config.ipAddress = "127.0.0.1";
            config.port = server.getAddress().getPort();
            config.password = "password";
            config.asyncTimeout = TimeUnit.SECONDS.toMillis(20);
            config.syncTimeout = TimeUnit.SECONDS.toMillis(5);

            HttpClient httpClient = new HttpClient();
            httpClient.start();
            FritzAhaWebInterface webInterface = new FritzAhaWebInterface(config, handler, httpClient);
            try {
                CompletableFuture<String> delayedRequest = sendCommand(webInterface, "delayed");
                assertTrue(oldRequestReceived.await(5, TimeUnit.SECONDS));
                assertEquals("ok", sendCommand(webInterface, "getdevicelistinfos").get(5, TimeUnit.SECONDS));

                releaseOldResponse.countDown();
                assertEquals("ok", delayedRequest.get(5, TimeUnit.SECONDS));
                assertEquals(2, logins.get());
            } finally {
                releaseOldResponse.countDown();
                webInterface.dispose();
                httpClient.stop();
            }
        } finally {
            server.stop(0);
            serverExecutor.shutdownNow();
        }
    }

    private CompletableFuture<String> sendCommand(FritzAhaWebInterface webInterface, String command) {
        CompletableFuture<String> responseBody = new CompletableFuture<>();
        webInterface.asyncGet(new FritzAhaReauthCallback(FritzAhaReauthCallback.WEBSERVICE_PATH, "switchcmd=" + command,
                webInterface, HttpMethod.GET, 1) {
            @Override
            public void execute(int status, String response) {
                super.execute(status, response);
                if (isValidRequest()) {
                    responseBody.complete(response);
                }
            }
        });
        return responseBody;
    }

    private void sendResponse(HttpExchange exchange, int status, String response) throws IOException {
        byte[] body = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, body.length == 0 ? -1 : body.length);
        if (body.length > 0) {
            exchange.getResponseBody().write(body);
        }
    }

    private void acceptWithoutResponding(ServerSocket serverSocket, CountDownLatch requestAccepted,
            CountDownLatch releaseServer) {
        try (Socket ignored = serverSocket.accept()) {
            requestAccepted.countDown();
            releaseServer.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // The server socket may be closed during test cleanup.
        }
    }

    private void respondWithChallenge(ServerSocket serverSocket) {
        String content = "<SessionInfo><SID>0000000000000000</SID><Challenge>12345678</Challenge></SessionInfo>";
        try (Socket socket = serverSocket.accept();
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            do {
                line = reader.readLine();
            } while (line != null && !line.isEmpty());
            String response = "HTTP/1.1 200 OK\r\nContent-Length: " + content.getBytes(StandardCharsets.UTF_8).length
                    + "\r\nConnection: close\r\n\r\n" + content;
            socket.getOutputStream().write(response.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            // The server socket may be closed during test cleanup.
        }
    }
}
