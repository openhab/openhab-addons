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
package org.openhab.binding.caldav.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

import com.sun.net.httpserver.HttpServer;

/**
 * Account mode and in-flight disposal tests using a local server.
 * 
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
@Timeout(30)
class AccountHandlerTest {
    private static final class Handler extends AccountHandler {
        final CountDownLatch online = new CountDownLatch(1);
        final CountDownLatch offline = new CountDownLatch(1);
        final AtomicInteger publications = new AtomicInteger();

        Handler(Bridge bridge, HttpClientFactory factory) {
            super(bridge, factory);
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            publications.incrementAndGet();
            if (status == ThingStatus.ONLINE) {
                online.countDown();
            } else if (status == ThingStatus.OFFLINE) {
                offline.countDown();
            }
        }
    }

    @Test
    void autoDiscoversBothHomesAndKeepsFirstDuplicateName() throws Exception {
        checkAutoDiscovery(false);
    }

    @Test
    void autoDoesNotPublishPartialDiscoveryWhenSecondHomeFails() throws Exception {
        checkAutoDiscovery(true);
    }

    private void checkAutoDiscovery(boolean failSecondHome) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicBoolean fail = new AtomicBoolean();
        LinkedBlockingQueue<String> requests = new LinkedBlockingQueue<>();
        server.createContext("/", exchange -> {
            try (exchange) {
                String path = exchange.getRequestURI().getPath();
                requests.add(exchange.getRequestMethod() + " " + path + " "
                        + exchange.getRequestHeaders().getFirst("Depth"));
                if (fail.get() && "/home-b/".equals(path)) {
                    exchange.sendResponseHeaders(500, -1);
                    return;
                }
                String response = switch (path) {
                    case "/" -> discoveryProperty(
                            "<d:current-user-principal><d:href>/principal/</d:href></d:current-user-principal>");
                    case "/principal/" -> discoveryProperty(
                            "<c:calendar-home-set><d:href>/home-a/</d:href><d:href>/home-b/</d:href><d:href>/home-a/</d:href></c:calendar-home-set>");
                    case "/home-a/" -> calendarResponse("/calendars/a/", "First");
                    case "/home-b/" ->
                        calendarResponse("/calendars/a/", "Duplicate") + calendarResponse("/calendars/b/", "Second");
                    default -> throw new IllegalArgumentException("Unexpected discovery path");
                };
                byte[] data = ("<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">" + response
                        + "</d:multistatus>").getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(207, data.length);
                exchange.getResponseBody().write(data);
            }
        });
        server.start();
        HttpClient http = new HttpClient();
        HttpClientFactory factory = mock(HttpClientFactory.class);
        when(factory.createHttpClient(eq("caldav"), any(SslContextFactory.Client.class))).thenReturn(http);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "auto")
                .withConfiguration(new Configuration(
                        Map.of("url", url, "username", "user", "password", "secret", "discoveryMode", "AUTO")))
                .build();
        Handler handler = new Handler(bridge, factory);
        LinkedBlockingQueue<List<CalendarCollection>> results = new LinkedBlockingQueue<>();
        try {
            handler.initialize();
            assertTrue(handler.online.await(10, TimeUnit.SECONDS));
            requests.clear();
            fail.set(failSecondHome);
            handler.discover(results::add);
            if (failSecondHome) {
                assertTrue(handler.offline.await(10, TimeUnit.SECONDS));
                assertTrue(results.isEmpty());
            } else {
                assertEquals(
                        List.of(new CalendarCollection(URI.create(url + "calendars/a/"), "First"),
                                new CalendarCollection(URI.create(url + "calendars/b/"), "Second")),
                        results.poll(10, TimeUnit.SECONDS));
            }
            List<String> expected = List.of("PROPFIND / 0", "PROPFIND /principal/ 0", "PROPFIND /home-a/ 1",
                    "PROPFIND /home-b/ 1");
            assertEquals(expected, List.copyOf(requests));
        } finally {
            handler.dispose();
            http.stop();
            server.stop(0);
        }
    }

    private static String discoveryProperty(String property) {
        return "<d:response><d:propstat><d:prop>" + property
                + "</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>";
    }

    private static String calendarResponse(String href, String name) {
        return "<d:response><d:href>" + href + "</d:href><d:propstat><d:prop><d:displayname>" + name
                + "</d:displayname><d:resourcetype><d:collection/><c:calendar/></d:resourcetype>"
                + "</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>";
    }

    @Test
    void directUsesHomeAndDisposedSessionCannotPublishOrRestart() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        CountDownLatch secondEntered = new CountDownLatch(1), release = new CountDownLatch(1),
                stopped = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        server.createContext("/home/", exchange -> {
            try (exchange) {
                if (calls.incrementAndGet() == 2) {
                    secondEntered.countDown();
                    try {
                        if (!release.await(15, TimeUnit.SECONDS)) {
                            return;
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
                byte[] data = "<d:multistatus xmlns:d=\"DAV:\"/>".getBytes(java.nio.charset.StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(207, data.length);
                exchange.getResponseBody().write(data);
            }
        });
        server.start();
        HttpClient http = new HttpClient();
        http.addLifeCycleListener(new org.eclipse.jetty.util.component.LifeCycle.Listener() {
            @Override
            public void lifeCycleStopped(@Nullable LifeCycle lifecycle) {
                stopped.countDown();
            }
        });
        HttpClientFactory factory = mock(HttpClientFactory.class);
        when(factory.createHttpClient(eq("caldav"), any(SslContextFactory.Client.class))).thenReturn(http);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "test")
                .withConfiguration(new Configuration(Map.of("url", url, "username", "user", "password", "secret",
                        "discoveryMode", "DIRECT", "calendarHome", "/home/")))
                .build();
        Handler handler = new Handler(bridge, factory);
        try {
            handler.initialize();
            assertTrue(handler.online.await(10, TimeUnit.SECONDS));
            assertEquals(1, calls.get());
            handler.requestSync();
            assertTrue(secondEntered.await(10, TimeUnit.SECONDS));
            handler.dispose();
            int published = handler.publications.get();
            release.countDown();
            assertTrue(stopped.await(10, TimeUnit.SECONDS));
            assertEquals(published, handler.publications.get());
            handler.requestSync();
            assertEquals(2, calls.get());
        } finally {
            release.countDown();
            handler.dispose();
            http.stop();
            server.stop(0);
        }
    }
}
