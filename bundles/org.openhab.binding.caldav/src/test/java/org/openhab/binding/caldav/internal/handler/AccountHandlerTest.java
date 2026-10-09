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
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;

import javax.net.ssl.SSLHandshakeException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.client.util.BufferingResponseListener;
import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.client.CalDavXml;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.binding.caldav.internal.discovery.CalDavDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
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
 * @author Andreas Vilippus - Remote-confirmed recovery and pending discovery regression tests
 * @author Andreas Vilippus - Discovery callback disposal regressions
 * @author Andreas Vilippus - Localized Thing status regression coverage
 */
@NonNullByDefault
@Timeout(30)
class AccountHandlerTest {
    private static class Handler extends AccountHandler {
        final CountDownLatch online = new CountDownLatch(1);
        final CountDownLatch offline = new CountDownLatch(1);
        final AtomicInteger publications = new AtomicInteger();
        final LinkedBlockingQueue<ThingStatus> statuses = new LinkedBlockingQueue<>();
        volatile ThingStatus status = ThingStatus.UNKNOWN;
        volatile @Nullable String description;
        ThingStatusDetail detail = ThingStatusDetail.NONE;
        volatile Runnable beforeConfiguration = () -> {
        };

        Handler(Bridge bridge, HttpClientFactory factory) {
            super(bridge, factory);
        }

        Handler(Bridge bridge, HttpClientFactory factory, ScheduledExecutorService scheduler) {
            super(bridge, factory, scheduler);
        }

        @Override
        public AccountConfiguration configuration() {
            beforeConfiguration.run();
            return super.configuration();
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            this.status = status;
            this.description = description;
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

    private static class ScriptedClient extends HttpClient {
        @Nullable
        Throwable failure;
        int requests;

        @Override
        public Request newRequest(@Nullable URI uri) {
            Request request = Objects.requireNonNull(mock(Request.class, RETURNS_SELF));
            Response response = Objects.requireNonNull(mock(Response.class));
            when(response.getStatus()).thenReturn(failure == null ? 207 : 0);
            doAnswer(invocation -> {
                requests++;
                BufferingResponseListener listener = invocation.getArgument(0);
                Throwable cause = failure;
                if (cause == null) {
                    String xml = "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                            + discoveryProperty(
                                    "<d:current-user-principal><d:href>/</d:href></d:current-user-principal>"
                                            + "<c:calendar-home-set><d:href>/</d:href></c:calendar-home-set>")
                            + "</d:multistatus>";
                    listener.onContent(response, ByteBuffer.wrap(xml.getBytes(StandardCharsets.UTF_8)));
                }
                listener.onComplete(new Result(request, response, cause));
                return null;
            }).when(request).send(any(Response.CompleteListener.class));
            return request;
        }
    }

    private static final class RecoveringClient extends ScriptedClient {
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
        ThingStatusDetail detail = ThingStatusDetail.NONE;

        Calendar(String id, String path) {
            super(ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), id)
                    .withConfiguration(new Configuration(Map.of("path", path))).build(), () -> ZoneOffset.UTC,
                    Objects.requireNonNull(mock()));
        }

        @Override
        protected @Nullable Bridge getBridge() {
            return null;
        }

        @Override
        protected void updateState(ChannelUID channel, State state) {
            states.put(channel.getId(), state);
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            this.status = status;
            this.detail = detail;
        }
    }

    @Test
    void successfulPollPreservesConfiguredInterval() {
        for (int interval : new int[] { 30, 300, 3600, 7200, Integer.MAX_VALUE }) {
            ControlledScheduler scheduler = new ControlledScheduler();
            Handler handler = scheduledHandler(interval, new ScriptedClient(), scheduler);
            handler.initialize();
            queueDiscovery(handler, scheduler);
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
            queueDiscovery(handler, scheduler);
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
        queueDiscovery(handler, scheduler);
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
        queueDiscovery(handler, scheduler);
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
        queueDiscovery(handler, scheduler);
        try {
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.ONLINE, handler.statuses.poll());
            assertEquals(1, scheduler.polls.size());
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.ONLINE, handler.status);
            assertTrue(handler.statuses.isEmpty());
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
        HttpClient http = new ScriptedClient();
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
            queueDiscovery(handler, scheduler);
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
                HttpClient http = new ScriptedClient();
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
                    queueDiscovery(handler, scheduler);
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
            HttpClient http = new ScriptedClient();
            when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
            Handler handler = new Handler(
                    configuredBridge(Map.of("url", "https://example.org/", "refreshInterval", interval)), factory,
                    scheduler.executor);
            try {
                handler.initialize();
                queueDiscovery(handler, scheduler);
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
        HttpClient http = new ScriptedClient();
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
            queueDiscovery(handler, scheduler);
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
                assertEquals(ThingStatus.UNKNOWN, handler.statuses.poll());
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
                assertEquals(List.of(), results.poll());
                assertTrue(results.isEmpty());
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
        assertEquals(ThingStatus.UNKNOWN, handler.statuses.poll());
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

    private static void queueDiscovery(Handler handler, ControlledScheduler scheduler) {
        assertEquals(ThingStatus.UNKNOWN, handler.statuses.poll());
        handler.discover(collections -> {
        });
        scheduler.next(0); // Remove the initialization job cancelled by discover().
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
        ControlledScheduler controlled = new ControlledScheduler();
        Handler handler = new Handler(bridge, factory, controlled.executor);
        LinkedBlockingQueue<List<CalendarCollection>> results = new LinkedBlockingQueue<>();
        try {
            handler.initialize();
            controlled.next(0).action().run();
            assertEquals(ThingStatus.UNKNOWN, handler.status);
            controlled.next(300);
            handler.statuses.clear();
            requests.clear();
            fail.set(failSecondHome);
            handler.discover(results::add);
            controlled.next(0).action().run();
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
                controlled.next(300);
                handler.requestSync();
                controlled.next(0).action().run();
                assertEquals(ThingStatus.ONLINE, handler.status);
                assertTrue(handler.statuses.isEmpty());
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
            assertEquals(ThingStatus.UNKNOWN, handler.status);
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
            assertTrue(handler.online.await(10, TimeUnit.SECONDS));
            assertEquals(2, handler.publications.get());
        } finally {
            release.countDown();
            handler.dispose();
            http.stop();
            server.stop(0);
        }
    }

    private static final class RecoveryFixture implements AutoCloseable {
        final HttpServer server;
        final AtomicInteger status = new AtomicInteger(401);
        final AtomicInteger requests = new AtomicInteger();
        final ControlledScheduler scheduler = new ControlledScheduler();
        final Handler handler;
        final AtomicInteger callbacks = new AtomicInteger();
        volatile Runnable onRequest = () -> {
        };
        volatile String responseBody = "<d:multistatus xmlns:d=\"DAV:\"/>";

        RecoveryFixture() throws IOException {
            this(300, () -> true);
        }

        RecoveryFixture(int interval, BooleanSupplier active) throws IOException {
            this(interval, active, "BASIC");
        }

        RecoveryFixture(int interval, BooleanSupplier active, String authType) throws IOException {
            server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/", exchange -> {
                try (exchange) {
                    requests.incrementAndGet();
                    onRequest.run();
                    byte[] body = responseBody.getBytes(StandardCharsets.UTF_8);
                    exchange.sendResponseHeaders(status.get(), body.length);
                    exchange.getResponseBody().write(body);
                }
            });
            server.start();
            HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
            when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class)))
                    .thenAnswer(invocation -> new HttpClient());
            String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
            handler = new Handler(
                    configuredBridge(Map.of("url", url, "discoveryMode", "DIRECT", "username",
                            authType.isEmpty() ? "" : "user", "password", authType.isEmpty() ? "" : "wrong-password",
                            "authType", authType.isEmpty() ? "AUTO" : authType, "refreshInterval", interval)),
                    factory, scheduler.executor);
            handler.initialize();
            scheduler.next(0);
            handler.discover(result -> callbacks.incrementAndGet(), active);
        }

        @Override
        public void close() {
            handler.dispose();
            server.stop(0);
        }
    }

    @Test
    void wrongPasswordStaysOfflineAcrossRetries() throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture()) {
            fixture.scheduler.next(0).action().run();
            assertEquals(ThingStatus.OFFLINE, fixture.handler.status);
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, fixture.handler.detail);
            assertEquals("@text/status.account.authentication", fixture.handler.description);
            assertEquals(1, fixture.requests.get());
            fixture.scheduler.next(600).action().run();
            assertEquals(ThingStatus.OFFLINE, fixture.handler.status,
                    "A retry without a successful remote request must not restore ONLINE");
            assertEquals(2, fixture.requests.get(), "Failed discovery must be requested again");
            assertEquals(0, fixture.callbacks.get());
            assertFalse(fixture.handler.statuses.contains(ThingStatus.ONLINE));
            fixture.scheduler.next(1200);
        }
    }

    @Test
    void initializationAndNoOpPollLeaveConnectionUnconfirmed() {
        ControlledScheduler scheduler = new ControlledScheduler();
        ScriptedClient client = new ScriptedClient();
        Handler handler = scheduledHandler(300, client, scheduler);
        try {
            handler.initialize();
            assertEquals(ThingStatus.UNKNOWN, handler.status);
            assertEquals("@text/status.account.waiting", handler.description);
            int publications = handler.publications.get();
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.UNKNOWN, handler.status);
            assertEquals(publications, handler.publications.get());
            assertEquals(0, client.requests);
            scheduler.next(300);
        } finally {
            handler.dispose();
        }
    }

    @Test
    void failedDiscoveryRemainsPendingForRetry() throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture()) {
            fixture.status.set(500);
            fixture.scheduler.next(0).action().run();
            assertEquals(0, fixture.callbacks.get());
            fixture.status.set(207);
            fixture.scheduler.next(600).action().run();
            assertEquals(2, fixture.requests.get());
            assertEquals(1, fixture.callbacks.get());
            int publications = fixture.handler.publications.get();
            fixture.scheduler.next(300).action().run();
            assertEquals(2, fixture.requests.get());
            assertEquals(1, fixture.callbacks.get());
            assertEquals(publications, fixture.handler.publications.get());
        }
    }

    @Test
    void successfulRetryRestoresOnline() throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture()) {
            fixture.scheduler.next(0).action().run();
            fixture.scheduler.next(600).action().run();
            assertEquals(ThingStatus.OFFLINE, fixture.handler.status);
            fixture.status.set(207);
            fixture.scheduler.next(1200).action().run();
            assertEquals(3, fixture.requests.get());
            assertEquals(ThingStatus.ONLINE, fixture.handler.status);
            Poll regular = fixture.scheduler.next(300);
            fixture.status.set(401);
            fixture.handler.discover(result -> fail("An authentication failure must not complete discovery"));
            Objects.requireNonNull(verify(regular.future())).cancel(false);
            fixture.scheduler.next(0).action().run();
            assertEquals(ThingStatus.OFFLINE, fixture.handler.status);
            fixture.scheduler.next(600);
        }
    }

    @Test
    void noOpPollDoesNotSetOfflineAccountOnline() throws Exception {
        AtomicBoolean active = new AtomicBoolean(true);
        try (RecoveryFixture fixture = new RecoveryFixture(300, active::get)) {
            fixture.scheduler.next(0).action().run();
            active.set(false);
            int publications = fixture.handler.publications.get();
            fixture.scheduler.next(600).action().run();
            assertEquals(1, fixture.requests.get());
            assertEquals(0, fixture.callbacks.get());
            assertEquals(ThingStatus.OFFLINE, fixture.handler.status);
            assertEquals(publications, fixture.handler.publications.get());
            assertFalse(fixture.handler.statuses.contains(ThingStatus.ONLINE));
        }
    }

    @Test
    void noOpPollDoesNotResetFailureBackoff() throws Exception {
        AtomicBoolean active = new AtomicBoolean(true);
        try (RecoveryFixture fixture = new RecoveryFixture(300, active::get)) {
            fixture.scheduler.next(0).action().run();
            active.set(false);
            fixture.scheduler.next(600).action().run();
            Poll delayed = fixture.scheduler.next(600);
            fixture.handler.discover(result -> fail("Expected another authentication failure"));
            Objects.requireNonNull(verify(delayed.future())).cancel(false);
            fixture.scheduler.next(0).action().run();
            assertEquals(2, fixture.requests.get());
            fixture.scheduler.next(1200);
        }
    }

    @Test
    void repeatedAuthenticationFailuresHaveBoundedBackoffInEveryAuthMode() throws Exception {
        for (String authType : List.of("", "BASIC", "DIGEST", "AUTO")) {
            try (RecoveryFixture fixture = new RecoveryFixture(300, () -> true, authType)) {
                for (long delay : new long[] { 0, 600, 1200, 2400, 3600, 3600, 3600, 3600 }) {
                    fixture.scheduler.next(delay).action().run();
                    assertEquals(ThingStatus.OFFLINE, fixture.handler.status);
                    assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, fixture.handler.detail);
                    assertFalse(fixture.handler.statuses.contains(ThingStatus.ONLINE));
                }
                assertEquals(8, fixture.requests.get());
                assertEquals(0, fixture.callbacks.get());
            }
        }
    }

    @Test
    void callbackFailureKeepsDiscoveryPendingUntilCallbackCompletes() throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture()) {
            fixture.status.set(207);
            AtomicInteger calls = new AtomicInteger();
            fixture.handler.discover(result -> {
                if (calls.incrementAndGet() == 1) {
                    throw new IllegalStateException("Test callback failure");
                }
            });
            fixture.scheduler.next(0); // Replaced by the second discovery request.
            fixture.scheduler.next(0).action().run();
            assertEquals(1, fixture.callbacks.get());
            assertEquals(1, calls.get());
            fixture.scheduler.next(300).action().run();
            assertEquals(1, fixture.callbacks.get());
            assertEquals(2, calls.get());
            fixture.scheduler.next(300).action().run();
            assertEquals(2, calls.get());
            assertEquals(2, fixture.requests.get());
        }
    }

    @Test
    void canceledDiscoveryDoesNotMakeAnyRequest() throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture(300, () -> false)) {
            fixture.scheduler.next(0).action().run();
            assertEquals(0, fixture.requests.get());
            assertEquals(0, fixture.callbacks.get());
            assertEquals(ThingStatus.UNKNOWN, fixture.handler.status);
        }
    }

    @Test
    void reinitializeInvalidatesOldRetryAndCallbacks() throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture()) {
            fixture.scheduler.next(0).action().run();
            Poll old = fixture.scheduler.next(600);
            fixture.handler.initialize();
            Objects.requireNonNull(verify(old.future())).cancel(true);
            int publications = fixture.handler.publications.get();
            old.action().run();
            assertEquals(publications, fixture.handler.publications.get());
            assertEquals(1, fixture.requests.get());
            fixture.status.set(207);
            AtomicInteger freshCallbacks = new AtomicInteger();
            fixture.scheduler.next(0);
            fixture.handler.discover(result -> freshCallbacks.incrementAndGet());
            fixture.scheduler.next(0).action().run();
            assertEquals(1, freshCallbacks.get());
            assertEquals(0, fixture.callbacks.get());
            assertEquals(ThingStatus.ONLINE, fixture.handler.status);
        }
    }

    @Test
    void disposeInvalidatesPendingRetry() throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture()) {
            fixture.scheduler.next(0).action().run();
            Poll old = fixture.scheduler.next(600);
            fixture.handler.dispose();
            Objects.requireNonNull(verify(old.future())).cancel(true);
            int publications = fixture.handler.publications.get();
            old.action().run();
            assertEquals(publications, fixture.handler.publications.get());
            assertEquals(1, fixture.requests.get());
            assertEquals(0, fixture.callbacks.get());
            assertTrue(fixture.scheduler.polls.isEmpty());
        }
    }

    @Test
    void reinitializationDuringRequestRejectsOldSuccessAndCallback() throws Exception {
        checkInFlightInvalidation(true);
    }

    @Test
    void disposalDuringRequestRejectsOldSuccessAndCallback() throws Exception {
        checkInFlightInvalidation(false);
    }

    @Test
    void accountDisposalDuringDiscoveryPreparationRejectsLateResults() throws Exception {
        checkDiscoveryPreparationInvalidation(true, false);
    }

    @Test
    void accountDisposalDuringManualDiscoveryPreparationRejectsLateResults() throws Exception {
        checkDiscoveryPreparationInvalidation(false, false);
    }

    @Test
    void accountReinitializationDuringManualDiscoveryPreparationRejectsLateResults() throws Exception {
        checkDiscoveryPreparationInvalidation(false, true);
    }

    private void checkDiscoveryPreparationInvalidation(boolean background, boolean reinitialize) throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture()) {
            fixture.status.set(207);
            fixture.responseBody = "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                    + calendarResponse("/calendar/", "Calendar") + "</d:multistatus>";
            AtomicInteger results = new AtomicInteger();
            CalDavDiscoveryService service = new CalDavDiscoveryService() {
                @Override
                protected void thingDiscovered(DiscoveryResult result) {
                    results.incrementAndGet();
                }
            };
            service.setThingHandler(fixture.handler);
            fixture.scheduler.next(0);
            if (background) {
                service.initialize();
            } else {
                service.startScan();
            }
            CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
            fixture.handler.beforeConfiguration = () -> awaitRequestRelease(entered, release);
            var executor = Executors.newSingleThreadExecutor(Thread.ofPlatform().daemon(true).factory());
            try {
                var running = executor.submit(fixture.scheduler.next(0).action());
                assertTrue(entered.await(10, TimeUnit.SECONDS));
                fixture.handler.beforeConfiguration = () -> {
                };
                if (reinitialize) {
                    fixture.handler.initialize();
                } else {
                    fixture.handler.dispose();
                }
                int publications = fixture.handler.publications.get();
                release.countDown();
                running.get(10, TimeUnit.SECONDS);
                assertEquals(0, results.get(), "An old account session must not publish discovery results");
                assertEquals(publications, fixture.handler.publications.get());
                if (reinitialize) {
                    fixture.scheduler.next(0);
                    service.startScan();
                    fixture.scheduler.next(0).action().run();
                    assertEquals(1, results.get());
                    assertEquals(ThingStatus.ONLINE, fixture.handler.status);
                } else {
                    assertTrue(fixture.scheduler.polls.isEmpty());
                }
            } finally {
                release.countDown();
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
                service.dispose();
            }
        }
    }

    private void checkInFlightInvalidation(boolean reinitialize) throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture()) {
            fixture.status.set(207);
            CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
            fixture.onRequest = () -> awaitRequestRelease(entered, release);
            var executor = Executors.newSingleThreadExecutor(Thread.ofPlatform().daemon(true).factory());
            try {
                var running = executor.submit(fixture.scheduler.next(0).action());
                assertTrue(entered.await(10, TimeUnit.SECONDS));
                if (reinitialize) {
                    fixture.handler.initialize();
                } else {
                    fixture.handler.dispose();
                }
                int publications = fixture.handler.publications.get();
                release.countDown();
                running.get(10, TimeUnit.SECONDS);
                assertEquals(publications, fixture.handler.publications.get());
                assertEquals(0, fixture.callbacks.get());
                assertFalse(fixture.handler.statuses.contains(ThingStatus.ONLINE));
                if (reinitialize) {
                    fixture.scheduler.next(0).action().run();
                    assertEquals(ThingStatus.UNKNOWN, fixture.handler.status);
                    fixture.scheduler.next(300);
                    fixture.handler.discover(result -> fixture.callbacks.incrementAndGet());
                    fixture.scheduler.next(0).action().run();
                    assertEquals(ThingStatus.ONLINE, fixture.handler.status);
                    assertEquals(1, fixture.callbacks.get());
                    assertEquals(2, fixture.requests.get());
                } else {
                    assertTrue(fixture.scheduler.polls.isEmpty());
                }
            } finally {
                release.countDown();
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            }
        }
    }

    @Test
    void overlappingPollsDoNotDuplicateDiscovery() throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture()) {
            fixture.status.set(207);
            CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
            fixture.onRequest = () -> awaitRequestRelease(entered, release);
            var executor = Executors.newSingleThreadExecutor(Thread.ofPlatform().daemon(true).factory());
            try {
                Poll poll = fixture.scheduler.next(0);
                var running = executor.submit(poll.action());
                assertTrue(entered.await(10, TimeUnit.SECONDS));
                poll.action().run();
                assertEquals(1, fixture.requests.get());
                release.countDown();
                running.get(10, TimeUnit.SECONDS);
                assertEquals(1, fixture.callbacks.get());
                assertEquals(1, fixture.requests.get());
                fixture.scheduler.next(300);
                assertTrue(fixture.scheduler.polls.isEmpty());
            } finally {
                release.countDown();
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            }
        }
    }

    private static void awaitRequestRelease(CountDownLatch entered, CountDownLatch release) {
        entered.countDown();
        try {
            assertTrue(release.await(10, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException(e);
        }
    }

    @Test
    void http401ProducesAuthenticationDetail() throws Exception {
        checkHttpFailure(401, ThingStatusDetail.CONFIGURATION_ERROR, "@text/status.account.authentication");
    }

    @Test
    void http403ProducesForbiddenDetail() throws Exception {
        checkHttpFailure(403, ThingStatusDetail.CONFIGURATION_ERROR, "@text/status.account.forbidden");
    }

    @Test
    void http404ProducesEndpointConfigurationDetail() throws Exception {
        checkHttpFailure(404, ThingStatusDetail.CONFIGURATION_ERROR, "@text/status.account.not-found");
    }

    @Test
    void rejectedPropfindPreservesHttpStatus() throws Exception {
        checkHttpFailure(405, ThingStatusDetail.COMMUNICATION_ERROR, "@text/status.account.http-error [\"405\"]");
    }

    @Test
    void http500ProducesCommunicationError() throws Exception {
        checkHttpFailure(500, ThingStatusDetail.COMMUNICATION_ERROR, "@text/status.account.http-error [\"500\"]");
    }

    private void checkHttpFailure(int status, ThingStatusDetail detail, String description) throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture()) {
            fixture.status.set(status);
            fixture.responseBody = "<html>private-calendar-data wrong-password Authorization secret-cookie</html>";
            fixture.scheduler.next(0).action().run();
            assertEquals(ThingStatus.OFFLINE, fixture.handler.status);
            assertEquals(detail, fixture.handler.detail);
            assertEquals(description, fixture.handler.description);
            assertEquals(1, fixture.requests.get());
            assertEquals(0, fixture.callbacks.get());
            assertFalse(fixture.handler.statuses.contains(ThingStatus.ONLINE));
        }
    }

    @Test
    void connectionFailureProducesCommunicationError() {
        checkTransportFailure(new ConnectException("private-url password"), "@text/status.connection");
    }

    @Test
    void timeoutProducesTimeoutDetail() {
        checkTransportFailure(new TimeoutException("private-url password"), "@text/status.timeout");
        checkTransportFailure(new SocketTimeoutException("private-url password"), "@text/status.timeout");
    }

    @Test
    void dnsFailureProducesNameResolutionDetail() {
        checkTransportFailure(new UnknownHostException("private-host password"), "@text/status.dns");
    }

    @Test
    void tlsFailureProducesSecureConnectionDetail() {
        checkTransportFailure(new SSLHandshakeException("private-certificate password"), "@text/status.tls");
    }

    private void checkTransportFailure(Throwable failure, String description) {
        ControlledScheduler scheduler = new ControlledScheduler();
        ScriptedClient client = new ScriptedClient();
        client.failure = failure;
        Handler handler = scheduledHandler(300, client, scheduler);
        try {
            handler.initialize();
            queueDiscovery(handler, scheduler);
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.OFFLINE, handler.status);
            assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.detail);
            assertEquals(description, handler.description);
            assertEquals(1, client.requests);
            assertFalse(handler.statuses.contains(ThingStatus.ONLINE));
            client.failure = null;
            scheduler.next(600).action().run();
            assertEquals(ThingStatus.ONLINE, handler.status);
            assertTrue(client.requests > 1);
            scheduler.next(300);
        } finally {
            handler.dispose();
        }
    }

    @Test
    void connectivityTrackingPreservesIncrementalCalendarWorkerAcrossPolls() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger downloads = new AtomicInteger(), reports = new AtomicInteger();
        AtomicInteger remoteStatus = new AtomicInteger(207);
        server.createContext("/", exchange -> {
            try (exchange) {
                String response;
                if ("GET".equals(exchange.getRequestMethod())) {
                    downloads.incrementAndGet();
                    response = "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\nUID:one\n"
                            + "DTSTART:20261007T080000Z\nDURATION:PT1H\nEND:VEVENT\nEND:VCALENDAR";
                } else {
                    reports.incrementAndGet();
                    response = "<d:multistatus xmlns:d=\"DAV:\"><d:response><d:href>/calendar/one.ics</d:href>"
                            + "<d:propstat><d:prop><d:getetag>unchanged</d:getetag></d:prop>"
                            + "<d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>";
                }
                byte[] body = response.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(remoteStatus.get(), body.length);
                exchange.getResponseBody().write(body);
            }
        });
        server.start();
        ControlledScheduler scheduler = new ControlledScheduler();
        HttpClient http = new HttpClient();
        HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
        when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
        Calendar calendar = new Calendar("incremental", "/calendar/");
        Thing child = Objects.requireNonNull(mock(Thing.class));
        when(child.getHandler()).thenReturn(calendar);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        Bridge bridge = Objects.requireNonNull(spy(configuredBridge(Map.of("url", url, "syncMode", "ETAG"))));
        when(bridge.getThings()).thenReturn(List.of(child));
        Handler handler = new Handler(bridge, factory, scheduler.executor);
        try {
            calendar.initialize();
            handler.initialize();
            assertEquals(ThingStatus.UNKNOWN, handler.statuses.poll());
            scheduler.next(0).action().run();
            assertEquals(ThingStatus.ONLINE, handler.status);
            assertEquals(ThingStatus.ONLINE, calendar.status);
            assertEquals(1, downloads.get());
            remoteStatus.set(503);
            scheduler.next(300).action().run();
            assertEquals(ThingStatus.OFFLINE, handler.status);
            assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.detail);
            calendar.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, handler.detail, handler.description));
            assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, calendar.detail);
            assertEquals("Server returned HTTP 503",
                    Objects.requireNonNull(calendar.states.get("sync#error")).toString());
            remoteStatus.set(207);
            scheduler.next(600).action().run();
            assertEquals(ThingStatus.ONLINE, handler.status);
            assertEquals(3, reports.get());
            assertEquals(1, downloads.get(), "An unchanged ETag must reuse the preceding poll's resource");
            assertEquals(ThingStatus.ONLINE, calendar.status);
        } finally {
            calendar.dispose();
            handler.dispose();
            http.stop();
            server.stop(0);
        }
    }
}
