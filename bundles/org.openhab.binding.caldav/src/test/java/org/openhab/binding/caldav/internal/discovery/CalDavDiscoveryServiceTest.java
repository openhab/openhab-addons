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
package org.openhab.binding.caldav.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.parallel.ResourceLock;
import org.junit.jupiter.api.parallel.Resources;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.binding.caldav.internal.handler.AccountHandler;
import org.openhab.core.config.core.ConfigUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.internal.BridgeImpl;

import com.sun.net.httpserver.HttpServer;

/**
 * Tests automatic and manual discovery across the bridge and service lifecycles.
 *
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Discovery identity migration regression tests
 */
@NonNullByDefault
@Timeout(30)
class CalDavDiscoveryServiceTest {
    private static final class TestConfigUtil extends ConfigUtil {
        static void useProperties(Set<String> keys) {
            setEnvProvider(name -> keys.contains(name) ? System.getProperty(name) : System.getenv(name));
        }

        static void restoreEnvironment() {
            setEnvProvider(System::getenv);
        }
    }

    private static final class Service extends CalDavDiscoveryService {
        final LinkedBlockingQueue<DiscoveryResult> results = new LinkedBlockingQueue<>();
        final AtomicInteger scans = new AtomicInteger();

        @Override
        public void startScan() {
            scans.incrementAndGet();
            super.startScan();
        }

        @Override
        protected void thingDiscovered(DiscoveryResult result) {
            results.add(result);
        }
    }

    @Test
    void serviceInitializedBeforeAccountStillDiscoversOnce() throws Exception {
        checkInitialization(true, true);
    }

    @Test
    void serviceInitializedAfterAccountDiscoversOnce() throws Exception {
        checkInitialization(false, true);
    }

    @Test
    void disabledBackgroundDiscoveryStillAllowsInboxScan() throws Exception {
        checkInitialization(true, false);
    }

    private void checkInitialization(boolean serviceFirst, boolean backgroundEnabled) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        server.createContext("/home/", exchange -> {
            try (exchange) {
                requests.incrementAndGet();
                byte[] data = ("<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                        + "<d:response><d:href>/calendar/</d:href><d:propstat><d:prop><d:displayname>Calendar</d:displayname>"
                        + "<d:resourcetype><d:collection/><c:calendar/></d:resourcetype></d:prop>"
                        + "<d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>")
                        .getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(207, data.length);
                exchange.getResponseBody().write(data);
            }
        });
        server.start();
        HttpClient http = new HttpClient();
        HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
        when(factory.createHttpClient(eq("caldav"), any(SslContextFactory.Client.class))).thenReturn(http);
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "test")
                .withConfiguration(new Configuration(Map.of("url", url + "home/", "discoveryMode", "DIRECT"))).build();
        AccountHandler handler = new AccountHandler(bridge, factory) {
            @Override
            protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            }
        };
        Service service = new Service();
        try {
            service.activate(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, backgroundEnabled));
            service.setThingHandler(handler);
            if (serviceFirst) {
                service.initialize();
                handler.initialize();
            } else {
                handler.initialize();
                service.initialize();
            }
            if (!backgroundEnabled) {
                assertEquals(0, service.scans.get());
                service.startScan(null);
            }
            DiscoveryResult result = service.results.poll(10, TimeUnit.SECONDS);
            assertNotNull(result);
            assertEquals(url + "calendar/", result.getProperties().get("path"));
            assertEquals(1, service.scans.get());
            assertEquals(1, requests.get());
            assertEquals(Map.of("path", url + "calendar/"), result.getProperties());
            assertNull(result.getRepresentationProperty());
            assertEquals(bridge.getUID(), result.getBridgeUID());
            assertEquals("Calendar", result.getLabel());
            service.startScan(null);
            DiscoveryResult repeated = service.results.poll(10, TimeUnit.SECONDS);
            assertNotNull(repeated);
            assertEquals(result.getThingUID(), repeated.getThingUID());
            assertEquals(result.getBridgeUID(), repeated.getBridgeUID());
            assertEquals(result.getLabel(), repeated.getLabel());
            assertEquals(result.getProperties(), repeated.getProperties());
            assertNull(repeated.getRepresentationProperty());
            assertEquals(2, service.scans.get());
            assertEquals(2, requests.get());
        } finally {
            service.dispose();
            handler.dispose();
            http.stop();
            server.stop(0);
        }
    }

    @Test
    void sameCalendarPathOnDifferentAccountBridgesProducesDistinctThings() {
        String path = "https://example.org/dav/calendars/shared/";
        Bridge anonymousBridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "account-a")
                .withConfiguration(
                        new Configuration(Map.of("url", "https://example.org/dav/", "username", "", "password", "")))
                .build();
        Bridge authenticatedBridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "account-b")
                .withConfiguration(new Configuration(
                        Map.of("url", "https://example.org/dav/", "username", "user", "password", "secret")))
                .build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> anonymousCallbacks = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> authenticatedCallbacks = new LinkedBlockingQueue<>();
        Service anonymousService = new Service();
        Service authenticatedService = new Service();
        anonymousService.setThingHandler(callbackHandler(anonymousBridge, anonymousCallbacks));
        authenticatedService.setThingHandler(callbackHandler(authenticatedBridge, authenticatedCallbacks));
        try {
            List<CalendarCollection> collections = List.of(new CalendarCollection(URI.create(path), "Shared"));
            anonymousService.startScan();
            authenticatedService.startScan();
            Objects.requireNonNull(anonymousCallbacks.poll()).accept(collections);
            Objects.requireNonNull(authenticatedCallbacks.poll()).accept(collections);
            DiscoveryResult anonymous = anonymousService.results.poll();
            DiscoveryResult authenticated = authenticatedService.results.poll();
            assertNotNull(anonymous);
            assertNotNull(authenticated);
            assertEquals(Map.of("path", path), anonymous.getProperties());
            assertEquals(Map.of("path", path), authenticated.getProperties());
            assertNotEquals(anonymous.getThingUID(), authenticated.getThingUID());
            assertEquals(anonymousBridge.getUID(), anonymous.getBridgeUID());
            assertEquals(authenticatedBridge.getUID(), authenticated.getBridgeUID());
            assertNotEquals(anonymous.getBridgeUID(), authenticated.getBridgeUID());
            assertEquals(List.of("account-a"), anonymous.getThingUID().getBridgeIds());
            assertEquals(List.of("account-b"), authenticated.getThingUID().getBridgeIds());
            assertEquals(anonymous.getThingUID().getId(), authenticated.getThingUID().getId());
            assertNull(anonymous.getRepresentationProperty());
            assertNull(authenticated.getRepresentationProperty());
        } finally {
            anonymousService.dispose();
            authenticatedService.dispose();
        }
    }

    @Test
    void collidingLegacyHashesProduceIndependentStableThings() {
        URI first = URI.create("https://example.org/calendar/Aa/");
        URI second = URI.create("https://example.org/calendar/BB/");
        assertEquals(first.toString().hashCode(), second.toString().hashCode());
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "collision")
                .withConfiguration(new Configuration(Map.of("url", "https://example.org/"))).build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        try {
            service.startScan();
            Objects.requireNonNull(callbacks.poll())
                    .accept(List.of(new CalendarCollection(first, "A"), new CalendarCollection(second, "B")));
            DiscoveryResult a = Objects.requireNonNull(service.results.poll());
            DiscoveryResult b = Objects.requireNonNull(service.results.poll());
            assertNotEquals(a.getThingUID(), b.getThingUID());
            service.startScan();
            Objects.requireNonNull(callbacks.poll()).accept(List.of(new CalendarCollection(second, "B")));
            assertEquals(b.getThingUID(), Objects.requireNonNull(service.results.poll()).getThingUID());
            service.startScan();
            Objects.requireNonNull(callbacks.poll())
                    .accept(List.of(new CalendarCollection(second, "B"), new CalendarCollection(first, "A")));
            assertEquals(b.getThingUID(), Objects.requireNonNull(service.results.poll()).getThingUID());
            assertEquals(a.getThingUID(), Objects.requireNonNull(service.results.poll()).getThingUID());
        } finally {
            service.dispose();
        }
    }

    @Test
    void equivalentCollectionUriSpellingsKeepSameThingAcrossScans() {
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "aliases")
                .withConfiguration(new Configuration(Map.of("url", "https://example.org/"))).build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        try {
            service.startScan();
            Objects.requireNonNull(callbacks.poll()).accept(
                    List.of(new CalendarCollection(URI.create("https://EXAMPLE.org/calendar/%4a/"), "Calendar")));
            ThingUID expected = Objects.requireNonNull(service.results.poll()).getThingUID();
            for (String spelling : List.of("https://example.org/calendar/%4A/", "https://example.org:443/calendar/%4A/",
                    "https://example.org/calendar/J/", "https://example.org/calendar/../calendar/J/")) {
                service.startScan();
                Objects.requireNonNull(callbacks.poll())
                        .accept(List.of(new CalendarCollection(URI.create(spelling), "Calendar")));
                assertEquals(expected, Objects.requireNonNull(service.results.poll()).getThingUID(), spelling);
                assertTrue(service.results.isEmpty());
            }
        } finally {
            service.dispose();
        }
    }

    @Test
    void canonicalIdentityPreservesReservedEscapesPathCaseQueryAndTrailingSlash() {
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "distinct")
                .withConfiguration(new Configuration(Map.of("url", "https://example.org/"))).build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        List<String> paths = List.of("a%2Fb/", "a/b/", "a%2Fb", "A%2Fb/", "a%2Fb/?key=Value", "a%2Fb/?key=value",
                "a//b/");
        try {
            service.startScan();
            Objects.requireNonNull(callbacks.poll())
                    .accept(paths.stream()
                            .map(path -> new CalendarCollection(URI.create("https://example.org/" + path), "Calendar"))
                            .toList());
            var ids = new java.util.HashSet<ThingUID>();
            for (int i = 0; i < paths.size(); i++) {
                ids.add(Objects.requireNonNull(service.results.poll()).getThingUID());
            }
            assertEquals(paths.size(), ids.size());
            assertTrue(service.results.isEmpty());
        } finally {
            service.dispose();
        }
    }

    @Test
    void preservesLegacyThingWhenDiscoveredCollectionUriSpellingChanges() {
        URI original = URI.create("https://EXAMPLE.org:443/calendar/%4a/");
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "legacy-alias")
                .withConfiguration(new Configuration(Map.of("url", "https://example.org/"))).build();
        ThingUID legacy = new ThingUID(new ThingTypeUID("caldav", "calendar"), bridge.getUID(),
                "calendar-" + Integer.toUnsignedString(original.toString().hashCode(), 36));
        ((BridgeImpl) bridge).addThing(
                ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), legacy).withBridge(bridge.getUID())
                        .withConfiguration(new Configuration(Map.of("path", original.toString()))).build());
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        try {
            for (String spelling : List.of("https://example.org/calendar/%4A/", "https://example.org/calendar/J/")) {
                service.startScan();
                Objects.requireNonNull(callbacks.poll())
                        .accept(List.of(new CalendarCollection(URI.create(spelling), "Calendar")));
                assertEquals(legacy, Objects.requireNonNull(service.results.poll()).getThingUID(), spelling);
                assertTrue(service.results.isEmpty());
            }
        } finally {
            service.dispose();
        }
    }

    @Test
    void preservesExistingLegacyThingAndDoesNotReuseItsIdForCollidingCollection() {
        URI first = URI.create("https://example.org/calendar/Aa/");
        URI second = URI.create("https://example.org/calendar/BB/");
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "legacy")
                .withConfiguration(new Configuration(Map.of("url", "https://example.org/"))).build();
        ThingUID legacy = new ThingUID(new ThingTypeUID("caldav", "calendar"), bridge.getUID(),
                "calendar-" + Integer.toUnsignedString(first.toString().hashCode(), 36));
        ((BridgeImpl) bridge).addThing(
                ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), legacy).withBridge(bridge.getUID())
                        .withConfiguration(new Configuration(Map.of("path", "/calendar/Aa/"))).build());
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        try {
            service.startScan();
            Objects.requireNonNull(callbacks.poll())
                    .accept(List.of(new CalendarCollection(second, "B"), new CalendarCollection(first, "A")));
            assertNotEquals(legacy, Objects.requireNonNull(service.results.poll()).getThingUID());
            assertEquals(legacy, Objects.requireNonNull(service.results.poll()).getThingUID());
        } finally {
            service.dispose();
        }
    }

    @Test
    @ResourceLock(Resources.SYSTEM_PROPERTIES)
    void preservesLegacyThingWhenAccountUrlAndCalendarPathUseEnvironmentVariables() {
        String urlKey = "openhab.caldav.audit.legacy.url";
        String pathKey = "openhab.caldav.audit.legacy.path";
        @Nullable
        String previousUrl = System.getProperty(urlKey);
        @Nullable
        String previousPath = System.getProperty(pathKey);
        Service service = new Service();
        try {
            System.setProperty(urlKey, "https://example.org/dav/");
            System.setProperty(pathKey, "/calendar/Aa/");
            // Core supports ${ENV:name}; its test hook avoids changing the process environment.
            TestConfigUtil.useProperties(Set.of(urlKey, pathKey));
            URI uri = URI.create("https://example.org/calendar/Aa/");
            Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "variables")
                    .withConfiguration(new Configuration(Map.of("url", "${ENV:" + urlKey + "}"))).build();
            ThingUID legacy = new ThingUID(new ThingTypeUID("caldav", "calendar"), bridge.getUID(),
                    "calendar-" + Integer.toUnsignedString(uri.toString().hashCode(), 36));
            ((BridgeImpl) bridge).addThing(
                    ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), legacy).withBridge(bridge.getUID())
                            .withConfiguration(new Configuration(Map.of("path", "${ENV:" + pathKey + "}"))).build());
            LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
            AccountHandler handler = callbackHandler(bridge, callbacks);
            assertEquals("https://example.org/dav/", handler.configuration().url);
            service.setThingHandler(handler);
            service.startScan();
            Objects.requireNonNull(callbacks.poll()).accept(List.of(new CalendarCollection(uri, "Calendar")));
            DiscoveryResult result = Objects.requireNonNull(service.results.poll());
            assertEquals(legacy, result.getThingUID());
            assertEquals(Map.of("path", uri.toString()), result.getProperties());
            assertEquals("${ENV:" + urlKey + "}", bridge.getConfiguration().get("url"));
        } finally {
            try {
                service.dispose();
            } finally {
                TestConfigUtil.restoreEnvironment();
                restoreProperty(urlKey, previousUrl);
                restoreProperty(pathKey, previousPath);
            }
        }
    }

    private static void restoreProperty(String key, @Nullable String value) {
        if (value == null) {
            System.clearProperty(key);
        } else {
            System.setProperty(key, value);
        }
    }

    @Test
    void manuallyNamedThingDoesNotCreateGlobalPathMatching() {
        URI uri = URI.create("https://example.org/calendar/");
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "manual")
                .withConfiguration(new Configuration(Map.of("url", "https://example.org/"))).build();
        ThingUID manual = new ThingUID(new ThingTypeUID("caldav", "calendar"), bridge.getUID(), "my-calendar");
        ((BridgeImpl) bridge).addThing(
                ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), manual).withBridge(bridge.getUID())
                        .withConfiguration(new Configuration(Map.of("path", uri.toString()))).build());
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        try {
            service.startScan();
            Objects.requireNonNull(callbacks.poll()).accept(List.of(new CalendarCollection(uri, "Calendar")));
            DiscoveryResult result = Objects.requireNonNull(service.results.poll());
            assertNotEquals(manual, result.getThingUID());
            assertNull(result.getRepresentationProperty());
        } finally {
            service.dispose();
        }
    }

    @Test
    void stoppedScansAndDisposedServiceRejectLateResults() throws Exception {
        AccountHandler handler = Objects.requireNonNull(mock(AccountHandler.class));
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "cancelled").build();
        when(handler.getThing()).thenReturn(bridge);
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        doAnswer(invocation -> {
            callbacks.add(invocation.getArgument(0));
            return null;
        }).when(handler).discover(any());
        Service service = new Service();
        service.setThingHandler(handler);
        List<CalendarCollection> collections = List
                .of(new CalendarCollection(URI.create("https://example.org/calendar/"), "Calendar"));
        try {
            service.startScan();
            Consumer<List<CalendarCollection>> stopped = Objects.requireNonNull(callbacks.poll());
            service.stopScan();
            stopped.accept(collections);
            assertTrue(service.results.isEmpty());

            service.startScan();
            Consumer<List<CalendarCollection>> disabled = Objects.requireNonNull(callbacks.poll());
            service.modified(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, false));
            disabled.accept(collections);
            assertTrue(service.results.isEmpty());
            verify(handler).unregisterDiscoveryService(service);

            service.startScan();
            Consumer<List<CalendarCollection>> disposed = Objects.requireNonNull(callbacks.poll());
            service.dispose();
            disposed.accept(collections);
            assertTrue(service.results.isEmpty());
        } finally {
            service.dispose();
        }
    }

    @Test
    void stopWaitsForPublicationAlreadyInProgress() throws Exception {
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "publishing").build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        AccountHandler handler = callbackHandler(bridge, callbacks);
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        LinkedBlockingQueue<String> order = new LinkedBlockingQueue<>();
        CalDavDiscoveryService service = new CalDavDiscoveryService() {
            @Override
            protected void thingDiscovered(DiscoveryResult result) {
                entered.countDown();
                try {
                    assertTrue(release.await(10, TimeUnit.SECONDS), "Publication was not released");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(e);
                }
                super.thingDiscovered(result);
                order.add("published");
            }
        };
        service.setThingHandler(handler);
        var workers = Executors.newFixedThreadPool(2);
        try {
            service.startScan();
            Consumer<List<CalendarCollection>> callback = Objects.requireNonNull(callbacks.poll());
            var publication = workers.submit(() -> callback
                    .accept(List.of(new CalendarCollection(URI.create("https://example.org/calendar/"), "Calendar"))));
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            LinkedBlockingQueue<Thread> stoppingThreads = new LinkedBlockingQueue<>();
            var stop = workers.submit(() -> {
                stoppingThreads.add(Thread.currentThread());
                service.stopScan();
                order.add("stopped");
            });
            Thread stoppingThread = Objects.requireNonNull(stoppingThreads.poll(10, TimeUnit.SECONDS));
            long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(10);
            // Observe actual monitor contention, not merely a stop task that has not been scheduled yet.
            while (stoppingThread.getState() != Thread.State.BLOCKED && !stop.isDone()
                    && System.nanoTime() < deadline) {
                Thread.onSpinWait();
            }
            assertEquals(Thread.State.BLOCKED, stoppingThread.getState(), "Stop must wait for publication");
            assertFalse(stop.isDone());
            release.countDown();
            publication.get(10, TimeUnit.SECONDS);
            stop.get(10, TimeUnit.SECONDS);
            assertEquals(List.of("published", "stopped"), List.copyOf(order));
        } finally {
            release.countDown();
            workers.shutdownNow();
            assertTrue(workers.awaitTermination(10, TimeUnit.SECONDS));
            service.dispose();
        }
    }

    @Test
    void stopDuringResultPreparationPreventsPublication() throws Exception {
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "preparing").build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        AccountHandler handler = Objects.requireNonNull(spy(callbackHandler(bridge, callbacks)));
        CountDownLatch preparing = new CountDownLatch(1), release = new CountDownLatch(1);
        when(handler.getThing()).thenAnswer(invocation -> {
            preparing.countDown();
            assertTrue(release.await(10, TimeUnit.SECONDS), "Result preparation was not released");
            return bridge;
        });
        Service service = new Service();
        service.setThingHandler(handler);
        var worker = Executors.newSingleThreadExecutor();
        try {
            service.startScan();
            Consumer<List<CalendarCollection>> callback = Objects.requireNonNull(callbacks.poll());
            var preparation = worker.submit(() -> callback
                    .accept(List.of(new CalendarCollection(URI.create("https://example.org/calendar/"), "Calendar"))));
            assertTrue(preparing.await(10, TimeUnit.SECONDS));
            service.stopScan();
            release.countDown();
            preparation.get(10, TimeUnit.SECONDS);
            assertTrue(service.results.isEmpty());
        } finally {
            release.countDown();
            worker.shutdownNow();
            assertTrue(worker.awaitTermination(10, TimeUnit.SECONDS));
            service.dispose();
        }
    }

    private AccountHandler callbackHandler(Bridge bridge,
            LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks) {
        return new AccountHandler(bridge, Objects.requireNonNull(mock(HttpClientFactory.class))) {
            @Override
            public void discover(Consumer<List<CalendarCollection>> callback) {
                callbacks.add(callback::accept);
            }
        };
    }
}
