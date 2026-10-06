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

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
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
import org.openhab.binding.caldav.internal.client.CalDavXml;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.State;

import com.sun.net.httpserver.HttpServer;

/**
 * Account mode and in-flight disposal tests using a local server.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - On-demand discovery regression tests
 * @author Andreas Vilippus - Configuration and deterministic account scheduling tests
 */
@NonNullByDefault
@Timeout(30)
class AccountHandlerTest {
    private static class Handler extends AccountHandler {
        final CountDownLatch online = new CountDownLatch(1);
        final CountDownLatch offline = new CountDownLatch(1);
        final AtomicInteger publications = new AtomicInteger();
        final LinkedBlockingQueue<ThingStatus> statuses = new LinkedBlockingQueue<>();
        ThingStatusDetail detail = ThingStatusDetail.NONE;

        Handler(Bridge bridge, HttpClientFactory factory) {
            super(bridge, factory);
        }

        Handler(Bridge bridge, HttpClientFactory factory, ScheduledExecutorService scheduler) {
            super(bridge, factory, scheduler);
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            this.detail = detail;
            publications.incrementAndGet();
            statuses.add(status);
            if (status == ThingStatus.ONLINE) {
                online.countDown();
            } else if (status == ThingStatus.OFFLINE) {
                offline.countDown();
            }
        }
    }

    private record Poll(Runnable action, long delay, ScheduledFuture<?> future) {
    }

    private static final class ControlledScheduler {
        final ScheduledExecutorService executor = Objects.requireNonNull(mock(ScheduledExecutorService.class));
        final Queue<Poll> polls = new ArrayDeque<>();

        ControlledScheduler() {
            when(executor.schedule(any(Runnable.class), anyLong(), eq(TimeUnit.SECONDS))).thenAnswer(invocation -> {
                ScheduledFuture<?> future = Objects.requireNonNull(mock(ScheduledFuture.class));
                polls.add(new Poll(invocation.getArgument(0), invocation.getArgument(1), future));
                return future;
            });
            Objects.requireNonNull(doAnswer(invocation -> {
                invocation.<Runnable> getArgument(0).run();
                return null;
            }).when(executor)).execute(any(Runnable.class));
        }

        Poll next(long expectedDelay) {
            Poll poll = Objects.requireNonNull(polls.poll());
            assertEquals(expectedDelay, poll.delay());
            return poll;
        }
    }

    private static final class RecoveringClient extends HttpClient {
        boolean fail;
        Runnable onStart = () -> {
        };

        @Override
        protected void doStart() throws Exception {
            onStart.run();
            if (fail) {
                throw new IOException("Test connection failure");
            }
            super.doStart();
        }
    }

    private static final class Calendar extends CalendarHandler {
        final Map<String, State> states = new ConcurrentHashMap<>();
        volatile ThingStatus status = ThingStatus.UNKNOWN;

        Calendar(String id, String path) {
            super(ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), id)
                    .withConfiguration(new Configuration(Map.of("path", path))).build(), () -> ZoneOffset.UTC,
                    Objects.requireNonNull(mock()));
        }

        @Override
        protected void updateState(ChannelUID channel, State state) {
            states.put(channel.getId(), state);
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            this.status = status;
        }
    }

    @Test
    void successfulPollPreservesConfiguredInterval() {
        for (int interval : new int[] { 30, 300, 3600, 7200, Integer.MAX_VALUE }) {
            ControlledScheduler scheduler = new ControlledScheduler();
            Handler handler = scheduledHandler(interval, new HttpClient(), scheduler);
            handler.initialize();
            try {
                scheduler.next(0).action().run();
                assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
                Poll next = scheduler.next(interval);
                handler.dispose();
                Objects.requireNonNull(verify(next.future())).cancel(true);
                int publications = handler.publications.get();
                next.action().run();
                assertEquals(publications, handler.publications.get());
                assertTrue(scheduler.polls.isEmpty());
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void failedPollBackoffNeverShortensConfiguredIntervalAndRecoveryResetsIt() {
        for (int[] delays : new int[][] { { 30, 60, 120 }, { 300, 600, 1200 }, { 3600, 3600, 3600 },
                { 7200, 7200, 7200 }, { Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE } }) {
            int interval = delays[0];
            ControlledScheduler scheduler = new ControlledScheduler();
            RecoveringClient http = new RecoveringClient();
            http.fail = true;
            Handler handler = scheduledHandler(interval, http, scheduler);
            handler.initialize();
            try {
                scheduler.next(0).action().run();
                assertEquals(ThingStatus.OFFLINE, handler.statuses.poll());
                assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.detail);
                Poll retry = scheduler.next(delays[1]);
                retry.action().run();
                assertEquals(ThingStatus.OFFLINE, handler.statuses.poll());
                retry = scheduler.next(delays[2]);
                http.fail = false;
                retry.action().run();
                assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
                scheduler.next(interval);
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void idleRequestSyncCancelsBackoffAndRunsImmediately() {
        ControlledScheduler scheduler = new ControlledScheduler();
        RecoveringClient http = new RecoveringClient();
        http.fail = true;
        Handler handler = scheduledHandler(300, http, scheduler);
        handler.initialize();
        try {
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.OFFLINE, handler.statuses.poll());
            Poll retry = scheduler.next(600);
            handler.requestSync();
            Objects.requireNonNull(verify(retry.future())).cancel(false);
            http.fail = false;
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
            scheduler.next(300);
        } finally {
            handler.dispose();
        }
        int publications = handler.publications.get();
        handler.requestSync();
        assertEquals(publications, handler.publications.get());
        assertTrue(scheduler.polls.isEmpty());
    }

    @Test
    void requestsDuringFailedPollAreCoalescedAndKeepBackoff() {
        ControlledScheduler scheduler = new ControlledScheduler();
        RecoveringClient http = new RecoveringClient();
        http.fail = true;
        Handler handler = scheduledHandler(300, http, scheduler);
        http.onStart = () -> {
            handler.requestSync();
            handler.requestSync();
        };
        handler.initialize();
        try {
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.OFFLINE, handler.statuses.poll());
            assertEquals(1, scheduler.polls.size());
            Poll retry = scheduler.next(600);
            http.fail = false;
            http.onStart = () -> {
            };
            retry.action().run();
            assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
            scheduler.next(300);
            assertTrue(scheduler.polls.isEmpty());
        } finally {
            handler.dispose();
        }
    }

    @Test
    void requestsDuringSuccessfulPollAreCoalescedIntoOneImmediateFollowup() {
        ControlledScheduler scheduler = new ControlledScheduler();
        RecoveringClient http = new RecoveringClient();
        Handler handler = scheduledHandler(300, http, scheduler);
        http.onStart = () -> {
            handler.requestSync();
            handler.requestSync();
        };
        handler.initialize();
        try {
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
            assertEquals(1, scheduler.polls.size());
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
            scheduler.next(300);
            assertTrue(scheduler.polls.isEmpty());
        } finally {
            handler.dispose();
        }
    }

    @Test
    void invalidAccountDoesNotCreateClientAndValidReplacementRecovers() {
        ControlledScheduler scheduler = new ControlledScheduler();
        HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
        HttpClient http = new HttpClient();
        when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
        Bridge invalid = configuredBridge(Map.of("url", "https://example.org/", "readOnly", false));
        Handler handler = new Handler(invalid, factory, scheduler.executor);
        handler.initialize();
        try {
            assertEquals(ThingStatus.OFFLINE, handler.statuses.poll());
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, handler.detail);
            verifyNoInteractions(factory);
            assertTrue(scheduler.polls.isEmpty());
            handler.thingUpdated(configuredBridge(Map.of("url", "https://example.org/")));
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
            verify(factory).createHttpClient(eq("caldav"), any(SslContextFactory.Client.class));
            scheduler.next(300);
        } finally {
            handler.dispose();
        }
    }

    @Test
    void invalidRawIntegerValuesAreRejectedBeforeClientCreationAndDtoConversion() {
        Map<String, Integer> defaults = Map.of("refreshInterval", 300, "requestTimeout", 30, "maxPastDays", 30,
                "maxFutureDays", 365);
        for (var parameter : defaults.entrySet()) {
            for (Object raw : List.of(BigDecimal.valueOf(parameter.getValue()).add(new BigDecimal("0.5")),
                    BigDecimal.valueOf(4294967296L + parameter.getValue()), parameter.getValue() + ".5")) {
                ControlledScheduler scheduler = new ControlledScheduler();
                HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
                HttpClient http = new HttpClient();
                when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
                Handler handler = new Handler(
                        configuredBridge(Map.of("url", "https://example.org/", parameter.getKey(), raw)), factory,
                        scheduler.executor);
                try {
                    handler.initialize();
                    assertEquals(ThingStatus.OFFLINE, handler.statuses.poll(), parameter.getKey() + "=" + raw);
                    assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, handler.detail);
                    verifyNoInteractions(factory);
                    assertTrue(scheduler.polls.isEmpty());
                    handler.thingUpdated(configuredBridge(
                            Map.of("url", "https://example.org/", parameter.getKey(), parameter.getValue())));
                    scheduler.next(0).action().run();
                    assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
                    scheduler.next(300);
                } finally {
                    handler.dispose();
                }
            }
        }
    }

    @Test
    void integerStringsAreAcceptedAndUseConfiguredInterval() {
        for (String interval : List.of("7200", "+7200", "07200")) {
            ControlledScheduler scheduler = new ControlledScheduler();
            HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
            HttpClient http = new HttpClient();
            when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
            Handler handler = new Handler(
                    configuredBridge(Map.of("url", "https://example.org/", "refreshInterval", interval)), factory,
                    scheduler.executor);
            try {
                handler.initialize();
                scheduler.next(0).action().run();
                assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
                assertEquals(7200, handler.configuration().refreshInterval);
                scheduler.next(7200);
            } finally {
                handler.dispose();
            }
        }
    }

    @Test
    void validationAndDtoConversionUseSameConfigurationSnapshot() {
        Configuration initial = new Configuration(Map.of("url", "https://example.org/", "refreshInterval", 7200));
        Configuration replacement = new Configuration(
                Map.of("url", "https://example.org/", "refreshInterval", new BigDecimal("300.5")));
        AtomicInteger reads = new AtomicInteger();
        ControlledScheduler scheduler = new ControlledScheduler();
        HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
        HttpClient http = new HttpClient();
        when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
        Handler handler = new Handler(configuredBridge(Map.of("url", "https://example.org/")), factory,
                scheduler.executor) {
            @Override
            protected Configuration getConfig() {
                return reads.getAndIncrement() == 0 ? initial : replacement;
            }
        };
        try {
            handler.initialize();
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
            scheduler.next(7200);
            assertThrows(IllegalArgumentException.class, handler::configuration);
        } finally {
            handler.dispose();
        }
    }

    @Test
    void unauthenticatedOrMissingPrincipalDoesNotRequestHomeAndNextScanRecovers() throws Exception {
        for (String principal : List.of("<d:current-user-principal><d:unauthenticated/></d:current-user-principal>",
                "")) {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            AtomicBoolean valid = new AtomicBoolean();
            Queue<String> requests = new java.util.concurrent.ConcurrentLinkedQueue<>();
            server.createContext("/", exchange -> {
                try (exchange) {
                    String path = exchange.getRequestURI().getPath();
                    requests.add(path);
                    String body = switch (path) {
                        case "/" -> discoveryProperty(valid.get()
                                ? "<d:current-user-principal><d:href>/principal/</d:href></d:current-user-principal>"
                                : principal);
                        case "/principal/" ->
                            discoveryProperty("<c:calendar-home-set><d:href>/home/</d:href></c:calendar-home-set>");
                        case "/home/" -> "";
                        default -> throw new IllegalArgumentException("Unexpected discovery path");
                    };
                    byte[] response = ("<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                            + body + "</d:multistatus>").getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(207, response.length);
                    exchange.getResponseBody().write(response);
                }
            });
            server.start();
            ControlledScheduler scheduler = new ControlledScheduler();
            HttpClient http = new HttpClient();
            HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
            when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            Handler handler = new Handler(configuredBridge(Map.of("url", url)), factory, scheduler.executor);
            Queue<List<CalendarCollection>> results = new ArrayDeque<>();
            handler.initialize();
            try {
                scheduler.next(0).action().run();
                assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
                scheduler.next(300);
                handler.discover(results::add);
                scheduler.next(0).action().run();
                assertEquals(ThingStatus.OFFLINE, handler.statuses.poll());
                assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, handler.detail);
                assertEquals(List.of("/"), List.copyOf(requests));
                assertTrue(results.isEmpty());
                Poll retry = scheduler.next(600);
                valid.set(true);
                requests.clear();
                handler.discover(results::add);
                Objects.requireNonNull(verify(retry.future())).cancel(false);
                scheduler.next(0).action().run();
                assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
                assertEquals(List.of("/", "/principal/", "/home/"), List.copyOf(requests));
                assertEquals(List.of(), results.poll());
                scheduler.next(300);
            } finally {
                handler.dispose();
                http.stop();
                server.stop(0);
            }
        }
    }

    @Test
    void failedCalendarDoesNotStopSuccessfulCalendarInSamePoll() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        Queue<String> requests = new java.util.concurrent.ConcurrentLinkedQueue<>();
        server.createContext("/", exchange -> {
            try (exchange) {
                String path = exchange.getRequestURI().getPath();
                requests.add(path);
                if ("/failed/".equals(path)) {
                    exchange.sendResponseHeaders(503, -1);
                    return;
                }
                byte[] response = "<d:multistatus xmlns:d=\"DAV:\"/>".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(207, response.length);
                exchange.getResponseBody().write(response);
            }
        });
        server.start();
        ControlledScheduler scheduler = new ControlledScheduler();
        HttpClient http = new HttpClient();
        HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
        when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        Calendar failed = new Calendar("failed", "/failed/");
        Calendar healthy = new Calendar("healthy", "/healthy/");
        Thing failedThing = Objects.requireNonNull(mock(Thing.class));
        Thing healthyThing = Objects.requireNonNull(mock(Thing.class));
        when(failedThing.getHandler()).thenReturn(failed);
        when(healthyThing.getHandler()).thenReturn(healthy);
        Bridge bridge = Objects.requireNonNull(spy(configuredBridge(Map.of("url", url, "syncMode", "FULL"))));
        when(bridge.getThings()).thenReturn(List.of(failedThing, healthyThing));
        Handler handler = new Handler(bridge, factory, scheduler.executor);
        failed.initialize();
        healthy.initialize();
        handler.initialize();
        try {
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
            assertEquals(List.of("/failed/", "/healthy/"), List.copyOf(requests));
            assertEquals(ThingStatus.OFFLINE, failed.status);
            assertEquals("ERROR", Objects.requireNonNull(failed.states.get("sync#status")).toString());
            assertEquals(ThingStatus.ONLINE, healthy.status);
            assertEquals("OK", Objects.requireNonNull(healthy.states.get("sync#status")).toString());
            assertEquals("[]", Objects.requireNonNull(healthy.states.get("events#json")).toString());
            scheduler.next(300);
        } finally {
            failed.dispose();
            healthy.dispose();
            handler.dispose();
            http.stop();
            server.stop(0);
        }
    }

    private static Handler scheduledHandler(int interval, HttpClient http, ControlledScheduler scheduler) {
        HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
        when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
        return new Handler(configuredBridge(Map.of("url", "https://example.org/", "refreshInterval", interval)),
                factory, scheduler.executor);
    }

    private static Bridge configuredBridge(Map<String, Object> configuration) {
        Configuration config = new Configuration();
        configuration.forEach(config::put);
        return BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "scheduled").withConfiguration(config)
                .build();
    }

    @Test
    void autoDiscoversBothHomesAndKeepsFirstDuplicateName() throws Exception {
        checkAutoDiscovery(false, false);
    }

    @Test
    void autoDoesNotPublishPartialDiscoveryWhenSecondHomeFails() throws Exception {
        checkAutoDiscovery(true, false);
    }

    @Test
    void normalSyncDoesNotRepeatDiscovery() throws Exception {
        checkAutoDiscovery(false, true);
    }

    private void checkAutoDiscovery(boolean failSecondHome, boolean syncAfterDiscovery) throws Exception {
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
            if (syncAfterDiscovery) {
                requests.clear();
                handler.statuses.clear();
                handler.requestSync();
                assertEquals(ThingStatus.ONLINE, handler.statuses.poll(10, TimeUnit.SECONDS));
                assertTrue(requests.isEmpty());
            }
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
    void directUsesConfiguredUrlAndDisposedSessionCannotPublishOrRestart() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        CountDownLatch secondEntered = new CountDownLatch(1), release = new CountDownLatch(1),
                stopped = new CountDownLatch(1);
        AtomicInteger calls = new AtomicInteger();
        LinkedBlockingQueue<String> requests = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<String> bodies = new LinkedBlockingQueue<>();
        server.createContext("/", exchange -> {
            try (exchange) {
                requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath() + " "
                        + exchange.getRequestHeaders().getFirst("Depth"));
                bodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
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
                .withConfiguration(new Configuration(Map.of("url", url + "home/", "username", "user", "password",
                        "secret", "discoveryMode", "DIRECT")))
                .build();
        Handler handler = new Handler(bridge, factory);
        LinkedBlockingQueue<List<CalendarCollection>> results = new LinkedBlockingQueue<>();
        try {
            handler.initialize();
            assertTrue(handler.online.await(10, TimeUnit.SECONDS));
            assertEquals(0, calls.get());
            handler.discover(results::add);
            assertEquals(List.of(), results.poll(10, TimeUnit.SECONDS));
            assertEquals(1, calls.get());
            assertEquals(List.of("PROPFIND /home/ 1"), List.copyOf(requests));
            var body = CalDavXml.parse(Objects.requireNonNull(bodies.poll()));
            assertEquals(1, body.getElementsByTagNameNS("DAV:", "displayname").getLength());
            assertEquals(1, body.getElementsByTagNameNS("DAV:", "resourcetype").getLength());
            assertEquals(0, body.getElementsByTagNameNS("DAV:", "current-user-principal").getLength());
            assertEquals(0,
                    body.getElementsByTagNameNS("urn:ietf:params:xml:ns:caldav", "calendar-home-set").getLength());
            handler.discover(results::add);
            assertTrue(secondEntered.await(10, TimeUnit.SECONDS));
            handler.dispose();
            int published = handler.publications.get();
            release.countDown();
            assertTrue(stopped.await(10, TimeUnit.SECONDS));
            assertEquals(published, handler.publications.get());
            assertTrue(results.isEmpty());
            handler.requestSync();
            assertEquals(2, calls.get());
        } finally {
            release.countDown();
            handler.dispose();
            http.stop();
            server.stop(0);
        }
    }

    @Test
    void discoveryRequestedDuringNormalPollRunsAfterwards() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        LinkedBlockingQueue<String> requests = new LinkedBlockingQueue<>();
        server.createContext("/home/", exchange -> {
            try (exchange) {
                requests.add(exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath());
                byte[] data = "<d:multistatus xmlns:d=\"DAV:\"/>".getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(207, data.length);
                exchange.getResponseBody().write(data);
            }
        });
        server.start();
        CountDownLatch starting = new CountDownLatch(1), release = new CountDownLatch(1);
        HttpClient http = new HttpClient() {
            @Override
            protected void doStart() throws Exception {
                starting.countDown();
                if (!release.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("Client startup was not released");
                }
                super.doStart();
            }
        };
        HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
        when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "queued")
                .withConfiguration(new Configuration(Map.of("url", url + "home/", "discoveryMode", "DIRECT"))).build();
        Handler handler = new Handler(bridge, factory);
        LinkedBlockingQueue<List<CalendarCollection>> results = new LinkedBlockingQueue<>();
        try {
            handler.initialize();
            assertTrue(starting.await(10, TimeUnit.SECONDS));
            handler.discover(results::add);
            release.countDown();
            assertEquals(List.of(), results.poll(10, TimeUnit.SECONDS));
            assertEquals(List.of("PROPFIND /home/"), List.copyOf(requests));
            assertEquals(2, handler.publications.get());
        } finally {
            release.countDown();
            handler.dispose();
            http.stop();
            server.stop(0);
        }
    }
}
