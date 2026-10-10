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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.client.CalDavUris;
import org.openhab.binding.caldav.internal.client.CalDavXml;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.binding.caldav.internal.client.CalendarDiscoveryParser;
import org.openhab.binding.caldav.internal.client.DavPrivilege;
import org.openhab.binding.caldav.internal.handler.AccountHandler;
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
 * @author Andreas Vilippus - Canonical discovery identity and reference regressions
 * @author Andreas Vilippus - Metadata and account-wide label regressions
 * @author Andreas Vilippus - Pending scan cancellation regressions
 */
@NonNullByDefault
@Timeout(30)
class CalDavDiscoveryServiceTest {
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
        checkInitialization(true, true, false);
    }

    @Test
    void serviceInitializedAfterAccountDiscoversOnce() throws Exception {
        checkInitialization(false, true, false);
    }

    @Test
    void disabledBackgroundDiscoveryStillAllowsInboxScan() throws Exception {
        checkInitialization(true, false, false);
    }

    @Test
    void directDiscoveryOutsideAccountPathStoresOriginRelativePath() throws Exception {
        checkInitialization(true, true, true);
    }

    private void checkInitialization(boolean serviceFirst, boolean backgroundEnabled, boolean absoluteHref)
            throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger requests = new AtomicInteger();
        LinkedBlockingQueue<String> requestBodies = new LinkedBlockingQueue<>();
        String collectionPath = absoluteHref ? "/calendars/family/" : "/calendar/";
        String href = absoluteHref ? "http://127.0.0.1:" + server.getAddress().getPort() + collectionPath
                : collectionPath;
        server.createContext("/home/", exchange -> {
            try (exchange) {
                requests.incrementAndGet();
                requestBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                byte[] data = ("<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                        + "<d:response><d:href>" + href
                        + "</d:href><d:propstat><d:prop><d:displayname>Calendar</d:displayname>"
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
            assertEquals(collectionPath, result.getProperties().get("path"));
            assertEquals(URI.create(url + collectionPath.substring(1)),
                    CalDavUris.reconstruct(URI.create(url + "home/"), collectionPath));
            assertEquals(1, service.scans.get());
            assertEquals(1, requests.get());
            assertCollectionRequest(Objects.requireNonNull(requestBodies.poll()));
            assertEquals(Map.of("path", collectionPath), result.getProperties());
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
            assertEquals(Map.of("path", "/dav/calendars/shared/"), anonymous.getProperties());
            assertEquals(Map.of("path", "/dav/calendars/shared/"), authenticated.getProperties());
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
    void distinctCollectionsProduceIndependentStableThings() {
        URI first = URI.create("https://example.org/calendar/Aa/");
        URI second = URI.create("https://example.org/calendar/BB/");
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
            assertEquals("calendar-b3cccceb5a7b0b2427fd294c3618afd13de64ed637bc2991d5edf5a1a20e1e88", expected.getId());
            for (String spelling : List.of("https://example.org/calendar/%4A/", "https://example.org:443/calendar/%4A/",
                    "https://example.org/calendar/J/", "https://example.org/calendar/../calendar/J/", "/calendar/J/")) {
                service.startScan();
                Objects.requireNonNull(callbacks.poll())
                        .accept(List.of(new CalendarCollection(URI.create(spelling), "Calendar")));
                DiscoveryResult result = Objects.requireNonNull(service.results.poll());
                assertEquals(expected, result.getThingUID(), spelling);
                assertEquals(Map.of("path", "/calendar/J/"), result.getProperties(), spelling);
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
                "a%2Fb/?x=1", "a%2Fb/?x=2", "a//b/");
        try {
            service.startScan();
            Objects.requireNonNull(callbacks.poll())
                    .accept(paths.stream()
                            .map(path -> new CalendarCollection(URI.create("https://example.org/" + path), "Calendar"))
                            .toList());
            var ids = new java.util.HashSet<ThingUID>();
            for (int i = 0; i < paths.size(); i++) {
                DiscoveryResult result = Objects.requireNonNull(service.results.poll());
                ids.add(result.getThingUID());
                URI canonical = CalDavUris.canonicalize(URI.create("https://example.org/" + paths.get(i)));
                assertEquals(canonical, CalDavUris.reconstruct(URI.create("https://example.org/"),
                        Objects.requireNonNull((String) result.getProperties().get("path"))));
            }
            assertEquals(paths.size(), ids.size());
            assertTrue(service.results.isEmpty());
        } finally {
            service.dispose();
        }
    }

    @Test
    void discoveryStoresOriginRelativePath() {
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "outside")
                .withConfiguration(new Configuration(Map.of("url", "https://example.org/caldav/"))).build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        try {
            service.startScan();
            Objects.requireNonNull(callbacks.poll()).accept(List.of(
                    new CalendarCollection(URI.create("HTTPS://EXAMPLE.ORG:443/calendars/user/9/family/"), "Family")));
            DiscoveryResult result = Objects.requireNonNull(service.results.poll());
            assertEquals(Map.of("path", "/calendars/user/9/family/"), result.getProperties());
            assertEquals("calendar-0971e33ca8a9bf714b0aa7ab6465aef0162403635864457de5406418ac7c7996",
                    result.getThingUID().getId());
            assertEquals("Family", result.getLabel());
            assertEquals(URI.create("https://example.org/calendars/user/9/family/"),
                    CalDavUris.reconstruct(URI.create("https://example.org/caldav/"),
                            Objects.requireNonNull((String) result.getProperties().get("path"))));
            assertTrue(service.results.isEmpty());
        } finally {
            service.dispose();
        }
    }

    @Test
    void discoveredRelativePathsRoundTripToCanonicalCollections() {
        URI account = URI.create("https://example.org/caldav/?old=value");
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "roundtrip")
                .withConfiguration(new Configuration(Map.of("url", account.toString()))).build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        Map<String, String> collections = Map.ofEntries(
                Map.entry("https://EXAMPLE.org:443/calendar/%4a/", "https://example.org/calendar/J/"),
                Map.entry("https://example.org/calendar//family/", "https://example.org/calendar//family/"),
                Map.entry("https://example.org/a%2fb/?next=/%2f", "https://example.org/a%2Fb/?next=/%2F"),
                Map.entry("https://example.org/calendar/?", "https://example.org/calendar/?"),
                Map.entry("https://example.org//calendars/family/", "https://example.org//calendars/family/"),
                Map.entry("/calendar/./family/", "https://example.org/calendar/family/"),
                Map.entry("family/", "https://example.org/caldav/family/"));
        try {
            for (var collection : collections.entrySet()) {
                service.startScan();
                Objects.requireNonNull(callbacks.poll())
                        .accept(List.of(new CalendarCollection(URI.create(collection.getKey()), "Calendar")));
                DiscoveryResult result = Objects.requireNonNull(service.results.poll());
                String path = Objects.requireNonNull((String) result.getProperties().get("path"));
                URI canonical = URI.create(collection.getValue());
                assertEquals(canonical, CalDavUris.reconstruct(account, path), collection.getKey());
                assertTrue(CalDavUris.isRoundTrip(account, canonical, path), collection.getKey());
                URI reference = URI.create(path);
                assertFalse(reference.isAbsolute());
                assertNull(reference.getRawAuthority());
                assertTrue(service.results.isEmpty());
            }
        } finally {
            service.dispose();
        }
    }

    @Test
    void configuredPathRepresentationDoesNotAffectCanonicalDiscoveryId() {
        URI account = URI.create("https://example.org/caldav/");
        URI canonical = URI.create("https://example.org/calendar/J/");
        for (String path : List.of(canonical.toString(), "/calendar/J/", "https://EXAMPLE.org:443/calendar/%4a/")) {
            Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "configured")
                    .withConfiguration(new Configuration(Map.of("url", account.toString()))).build();
            ThingUID uid = new ThingUID(new ThingTypeUID("caldav", "calendar"), bridge.getUID(),
                    "calendar-b3cccceb5a7b0b2427fd294c3618afd13de64ed637bc2991d5edf5a1a20e1e88");
            var calendar = ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), uid).withBridge(bridge.getUID())
                    .withConfiguration(new Configuration(Map.of("path", path))).build();
            ((BridgeImpl) bridge).addThing(calendar);
            LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
            Service service = new Service();
            service.setThingHandler(callbackHandler(bridge, callbacks));
            try {
                service.startScan();
                Objects.requireNonNull(callbacks.poll()).accept(List
                        .of(new CalendarCollection(URI.create("https://EXAMPLE.org:443/calendar/%4a/"), "Calendar")));
                DiscoveryResult result = Objects.requireNonNull(service.results.poll());
                assertEquals(uid, result.getThingUID(), path);
                assertEquals(Map.of("path", "/calendar/J/"), result.getProperties());
                assertEquals(canonical, CalDavUris.canonicalize(CalDavUris.resolve(account, path)));
                assertEquals(path, calendar.getConfiguration().get("path"));
            } finally {
                service.dispose();
            }
        }
    }

    @Test
    void discoveryRejectsUnsafeCollectionUrisBeforePublication() {
        URI account = URI.create("https://localhost/caldav/");
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "unsafe")
                .withConfiguration(new Configuration(Map.of("url", account.toString()))).build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        try {
            for (String reference : List.of("https://other.example/calendar/", "https://localhost:444/calendar/",
                    "http://localhost/calendar/", "https://user:secret@localhost/calendar/",
                    "https://localhost/calendar/#fragment", "//other.example/calendar/")) {
                service.startScan();
                Consumer<List<CalendarCollection>> callback = Objects.requireNonNull(callbacks.poll());
                List<CalendarCollection> collections = List
                        .of(new CalendarCollection(URI.create(reference), "Calendar"));
                assertThrows(IllegalArgumentException.class, () -> callback.accept(collections), reference);
                assertTrue(service.results.isEmpty(), reference);
            }
        } finally {
            service.dispose();
        }
    }

    @Test
    void discoveryRejectsMalformedCollectionHrefsBeforePublication() {
        URI account = URI.create("https://example.org/home/");
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "malformed")
                .withConfiguration(new Configuration(Map.of("url", account.toString()))).build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        try {
            for (String href : List.of("/calendars/bad%/", "/calendars/bad%2/", "/calendars/bad%GG/")) {
                String xml = """
                        <d:multistatus xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav">
                          <d:response><d:href>%s</d:href><d:propstat><d:prop>
                            <d:displayname>Calendar</d:displayname>
                            <d:resourcetype><d:collection/><c:calendar/></d:resourcetype>
                          </d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>
                        </d:multistatus>
                        """.formatted(href);
                service.startScan();
                Consumer<List<CalendarCollection>> callback = Objects.requireNonNull(callbacks.poll());
                assertThrows(IllegalArgumentException.class,
                        () -> callback.accept(CalendarDiscoveryParser.collections(xml, account)), href);
                assertTrue(service.results.isEmpty(), href);
            }
        } finally {
            service.dispose();
        }
    }

    @Test
    void originRelativePathCannotBeReinterpretedAsAnAuthority() {
        URI account = URI.create("https://example.org/caldav/");
        URI collection = URI.create("https://example.org//other.example/calendar/");
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "empty-segment")
                .withConfiguration(new Configuration(Map.of("url", account.toString()))).build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        try {
            service.startScan();
            Objects.requireNonNull(callbacks.poll()).accept(List.of(new CalendarCollection(collection, "Calendar")));
            String path = Objects.requireNonNull(
                    (String) Objects.requireNonNull(service.results.poll()).getProperties().get("path"));
            assertEquals("/.//other.example/calendar/", path);
            assertThrows(IllegalArgumentException.class,
                    () -> CalDavUris.reconstruct(account, collection.getRawPath()));
            assertEquals(collection, CalDavUris.reconstruct(account, path));
        } finally {
            service.dispose();
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
        LinkedBlockingQueue<BooleanSupplier> validity = new LinkedBlockingQueue<>();
        doAnswer(invocation -> {
            callbacks.add(invocation.getArgument(0));
            validity.add(invocation.getArgument(1));
            return null;
        }).when(handler).discover(any(), any(), any());
        Service service = new Service();
        service.setThingHandler(handler);
        List<CalendarCollection> collections = List
                .of(new CalendarCollection(URI.create("https://example.org/calendar/"), "Calendar"));
        try {
            service.startScan();
            Consumer<List<CalendarCollection>> stopped = Objects.requireNonNull(callbacks.poll());
            BooleanSupplier stoppedValid = Objects.requireNonNull(validity.poll());
            assertTrue(stoppedValid.getAsBoolean());
            service.stopScan();
            assertFalse(stoppedValid.getAsBoolean());
            stopped.accept(collections);
            assertTrue(service.results.isEmpty());

            service.startScan();
            Consumer<List<CalendarCollection>> disabled = Objects.requireNonNull(callbacks.poll());
            BooleanSupplier disabledValid = Objects.requireNonNull(validity.poll());
            assertTrue(disabledValid.getAsBoolean());
            service.modified(Map.of(DiscoveryService.CONFIG_PROPERTY_BACKGROUND_DISCOVERY, false));
            assertFalse(disabledValid.getAsBoolean());
            disabled.accept(collections);
            assertTrue(service.results.isEmpty());
            verify(handler).unregisterDiscoveryService(service);

            service.startScan();
            Consumer<List<CalendarCollection>> disposed = Objects.requireNonNull(callbacks.poll());
            BooleanSupplier disposedValid = Objects.requireNonNull(validity.poll());
            assertTrue(disposedValid.getAsBoolean());
            service.dispose();
            assertFalse(disposedValid.getAsBoolean());
            disposed.accept(collections);
            assertTrue(service.results.isEmpty());
        } finally {
            service.dispose();
        }
    }

    @Test
    void stopWaitsForPublicationAlreadyInProgress() throws Exception {
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "publishing")
                .withConfiguration(new Configuration(Map.of("url", "https://example.org/"))).build();
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
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "preparing")
                .withConfiguration(new Configuration(Map.of("url", "https://example.org/"))).build();
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

    @Test
    void uniqueDisplayNamesRemainUnchanged() {
        List<CalendarCollection> collections = List.of(calendar("/calendar/1/", "Privat"),
                calendar("/calendar/2/", "Arbeit"), calendar("/calendar/3/", "Familie"));
        Map<URI, DiscoveryResult> results = discover(collections);
        collections.forEach(collection -> assertEquals(collection.displayName(),
                Objects.requireNonNull(results.get(collection.uri())).getLabel()));
    }

    @Test
    void duplicateDisplayNamesGetUniqueLastSegment() {
        Map<URI, DiscoveryResult> results = discover(
                List.of(calendar("/calendar/123/", "Kalender"), calendar("/calendar/456/", "Kalender")));
        assertEquals("Kalender (123)",
                Objects.requireNonNull(results.get(URI.create("https://example.org/calendar/123/"))).getLabel());
        assertEquals("Kalender (456)",
                Objects.requireNonNull(results.get(URI.create("https://example.org/calendar/456/"))).getLabel());
    }

    @Test
    void duplicateLastSegmentsUseMorePathSegments() {
        Map<URI, DiscoveryResult> results = discover(
                List.of(calendar("/users/a/family/", "Kalender"), calendar("/users/b/family/", "Kalender")));
        assertEquals("Kalender (a/family)",
                Objects.requireNonNull(results.get(URI.create("https://example.org/users/a/family/"))).getLabel());
        assertEquals("Kalender (b/family)",
                Objects.requireNonNull(results.get(URI.create("https://example.org/users/b/family/"))).getLabel());
    }

    @Test
    void duplicatePathsWithDifferentQueriesUseDigestFallback() {
        Map<URI, DiscoveryResult> results = discover(
                List.of(calendar("/calendar/?id=1", "Kalender"), calendar("/calendar/?id=2", "Kalender")));
        Set<String> labels = new HashSet<>();
        results.values().forEach(result -> {
            String digest = result.getThingUID().getId().substring("calendar-".length());
            assertEquals("Kalender (" + digest.substring(0, 8) + ")", result.getLabel());
            labels.add(Objects.requireNonNull(result.getLabel()));
        });
        assertEquals(2, labels.size());
    }

    @Test
    void digestPrefixCollisionExtendsPrefix() {
        Map<URI, DiscoveryResult> results = discover(
                List.of(calendar("/calendar/?id=18792", "Kalender"), calendar("/calendar/?id=73182", "Kalender")));
        Set<String> labels = new HashSet<>();
        results.values().forEach(result -> {
            String digest = result.getThingUID().getId().substring("calendar-".length());
            assertEquals("1e995445", digest.substring(0, 8));
            assertEquals("Kalender (" + digest.substring(0, 9) + ")", result.getLabel());
            labels.add(Objects.requireNonNull(result.getLabel()));
        });
        assertEquals(2, labels.size());
    }

    @Test
    void reservedEscapesAndEmptySegmentsRemainDistinctInSuffixes() {
        Map<URI, DiscoveryResult> results = discover(
                List.of(calendar("/a%2Fb/", "Escaped"), calendar("/a/b/", "Escaped"),
                        calendar("/users//family/", "Kalender"), calendar("/users/b/family/", "Kalender")));
        assertEquals("Escaped (a%2Fb)",
                Objects.requireNonNull(results.get(URI.create("https://example.org/a%2Fb/"))).getLabel());
        assertEquals("Escaped (b)",
                Objects.requireNonNull(results.get(URI.create("https://example.org/a/b/"))).getLabel());
        assertEquals("Kalender (/family)",
                Objects.requireNonNull(results.get(URI.create("https://example.org/users//family/"))).getLabel());
        assertEquals("Kalender (b/family)",
                Objects.requireNonNull(results.get(URI.create("https://example.org/users/b/family/"))).getLabel());
    }

    @Test
    void caseDifferentDisplayNamesRemainUnmodified() {
        Map<URI, DiscoveryResult> results = discover(
                List.of(calendar("/calendar/1/", "Kalender"), calendar("/calendar/2/", "kalender")));
        assertEquals(Set.of("Kalender", "kalender"),
                new HashSet<>(results.values().stream().map(DiscoveryResult::getLabel).toList()));
    }

    @Test
    void missingDisplayNameUsesUniqueCompactFallback() {
        Map<URI, DiscoveryResult> results = discover(List.of(calendar("/users/a/family/", ""),
                calendar("/users/b/family/", ""), calendar("/", ""), calendar("/calendar/other/", "a/family")));
        Set<String> labels = new HashSet<>();
        results.values().forEach(result -> {
            String label = Objects.requireNonNull(result.getLabel());
            assertFalse(label.isBlank());
            assertFalse(label.contains("https://"));
            assertTrue(labels.add(label));
        });
        assertEquals("users/a/family",
                Objects.requireNonNull(results.get(URI.create("https://example.org/users/a/family/"))).getLabel());
        assertEquals("b/family",
                Objects.requireNonNull(results.get(URI.create("https://example.org/users/b/family/"))).getLabel());
        DiscoveryResult root = Objects.requireNonNull(results.get(URI.create("https://example.org/")));
        assertEquals(root.getThingUID().getId().substring(9, 17), root.getLabel());
    }

    @Test
    void longPathSuffixUsesCompactDigestFallback() {
        List<CalendarCollection> collections = List.of(calendar("/" + "a".repeat(81) + "/", "Kalender"),
                calendar("/" + "b".repeat(81) + "/", "Kalender"));
        discover(collections).values()
                .forEach(result -> assertEquals("Kalender (" + result.getThingUID().getId().substring(9, 17) + ")",
                        result.getLabel()));
    }

    @Test
    void labelChangesDoNotChangeThingUidOrPath() {
        CalendarCollection original = calendar("/calendar/123/", "Kalender");
        DiscoveryResult before = Objects.requireNonNull(discover(List.of(original)).get(original.uri()));
        DiscoveryResult after = Objects.requireNonNull(
                discover(List.of(original, calendar("/calendar/456/", "Kalender"))).get(original.uri()));
        assertEquals("Kalender", before.getLabel());
        assertEquals("Kalender (123)", after.getLabel());
        assertEquals(before.getThingUID(), after.getThingUID());
        assertEquals(before.getProperties(), after.getProperties());
    }

    @Test
    void discoveryPublishesOptionalMetadataWithStableSortedPrivileges() {
        CalendarCollection calendar = new CalendarCollection(URI.create("https://example.org/calendar/"), "Family",
                "https://foreign.example/description/", "unusual color text", Set.of(DavPrivilege.values()));
        DiscoveryResult result = Objects.requireNonNull(discover(List.of(calendar)).get(calendar.uri()));
        assertEquals(Map.of("path", "/calendar/", "calendarDescription", "https://foreign.example/description/",
                "calendarColor", "unusual color text", "calendarPrivileges",
                "bind,read,unbind,write,write-content,write-properties"), result.getProperties());
        assertEquals("Family", result.getLabel());
        assertNull(result.getRepresentationProperty());
        assertEquals(new ThingUID("caldav:account:metadata"), result.getBridgeUID());
    }

    @Test
    void absentOptionalMetadataIsNotPublished() {
        CalendarCollection calendar = calendar("/calendar/", "Family");
        assertEquals(Map.of("path", "/calendar/"),
                Objects.requireNonNull(discover(List.of(calendar)).get(calendar.uri())).getProperties());
    }

    @Test
    void changedDescriptionColorAndPrivilegesDoNotChangeIdentityOrPath() {
        CalendarCollection original = calendar("/calendar/", "Family");
        DiscoveryResult before = Objects.requireNonNull(discover(List.of(original)).get(original.uri()));
        for (CalendarCollection changed : List.of(
                new CalendarCollection(original.uri(), "Family", "Description", "", Set.of()),
                new CalendarCollection(original.uri(), "Family", "", "#123456", Set.of()),
                new CalendarCollection(original.uri(), "Family", "", "", Set.of(DavPrivilege.WRITE)))) {
            DiscoveryResult after = Objects.requireNonNull(discover(List.of(changed)).get(changed.uri()));
            assertEquals(before.getThingUID(), after.getThingUID());
            assertEquals(before.getProperties().get("path"), after.getProperties().get("path"));
            assertEquals(before.getLabel(), after.getLabel());
        }
    }

    @Test
    void onlyPathIsDeclaredAsConfigurationAmongDiscoveryProperties() throws Exception {
        try (var input = Objects
                .requireNonNull(getClass().getResourceAsStream("/OH-INF/thing/caldav-thing-types.xml"))) {
            var document = CalDavXml.parse(new String(input.readAllBytes(), StandardCharsets.UTF_8));
            var types = document.getElementsByTagName("thing-type");
            for (int i = 0; i < types.getLength(); i++) {
                var type = (org.w3c.dom.Element) types.item(i);
                if (!"calendar".equals(type.getAttribute("id"))) {
                    continue;
                }
                var parameters = type.getElementsByTagName("parameter");
                Set<String> names = new HashSet<>();
                for (int j = 0; j < parameters.getLength(); j++) {
                    names.add(((org.w3c.dom.Element) parameters.item(j)).getAttribute("name"));
                }
                assertTrue(names.contains("path"));
                assertFalse(names.contains("calendarDescription"));
                assertFalse(names.contains("calendarColor"));
                assertFalse(names.contains("calendarPrivileges"));
                return;
            }
            fail("Calendar Thing type is missing");
        }
    }

    @Test
    void duplicateNamesAcrossDifferentHomesAreResolvedAccountWide() throws Exception {
        checkMultipleHomes(false);
    }

    @Test
    void sameCanonicalCollectionThroughMultipleHomesDoesNotCollideWithItself() throws Exception {
        checkMultipleHomes(true);
    }

    private void checkMultipleHomes(boolean sameCollection) throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        LinkedBlockingQueue<String> requests = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<String> collectionBodies = new LinkedBlockingQueue<>();
        server.createContext("/", exchange -> {
            try (exchange) {
                String path = exchange.getRequestURI().getPath();
                requests.add(exchange.getRequestMethod() + " " + path + " "
                        + exchange.getRequestHeaders().getFirst("Depth"));
                if (path.startsWith("/home-")) {
                    collectionBodies.add(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                }
                String property = switch (path) {
                    case "/" -> "<d:current-user-principal><d:href>/principal/</d:href></d:current-user-principal>";
                    case "/principal/" -> "<c:calendar-home-set><d:href>/home-a/</d:href>"
                            + "<d:href>/home-b/</d:href></c:calendar-home-set>";
                    case "/home-a/", "/home-b/" ->
                        "<d:displayname>Kalender</d:displayname>" + "<d:resourcetype><c:calendar/></d:resourcetype>";
                    default -> throw new IllegalArgumentException("Unexpected discovery path");
                };
                String href = "/home-a/".equals(path) ? "/calendar/123/"
                        : sameCollection ? "/calendar/%31%32%33/" : "/calendar/456/";
                String xml = "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                        + "<d:response><d:href>" + href + "</d:href><d:propstat><d:prop>" + property
                        + "</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>";
                byte[] data = xml.getBytes(StandardCharsets.UTF_8);
                exchange.sendResponseHeaders(207, data.length);
                exchange.getResponseBody().write(data);
            }
        });
        server.start();
        HttpClient http = new HttpClient();
        HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
        when(factory.createHttpClient(Objects.requireNonNullElse(eq("caldav"), "caldav"),
                any(SslContextFactory.Client.class))).thenReturn(http);
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "homes")
                .withConfiguration(
                        new Configuration(Map.of("url", "http://127.0.0.1:" + server.getAddress().getPort() + "/")))
                .build();
        AccountHandler handler = new AccountHandler(bridge, factory) {
            @Override
            protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            }
        };
        Service service = new Service();
        try {
            service.setThingHandler(handler);
            service.initialize();
            handler.initialize();
            DiscoveryResult first = Objects.requireNonNull(service.results.poll(10, TimeUnit.SECONDS));
            assertEquals(sameCollection ? "Kalender" : "Kalender (123)", first.getLabel());
            assertEquals(Map.of("path", "/calendar/123/"), first.getProperties());
            if (!sameCollection) {
                DiscoveryResult second = Objects.requireNonNull(service.results.poll(10, TimeUnit.SECONDS));
                assertEquals("Kalender (456)", second.getLabel());
            }
            assertTrue(service.results.isEmpty());
            assertEquals(
                    List.of("PROPFIND / 0", "PROPFIND /principal/ 0", "PROPFIND /home-a/ 1", "PROPFIND /home-b/ 1"),
                    List.copyOf(requests));
            assertEquals(2, collectionBodies.size());
            collectionBodies.forEach(CalDavDiscoveryServiceTest::assertCollectionRequest);
            assertEquals(1, service.scans.get());
        } finally {
            service.dispose();
            handler.dispose();
            http.stop();
            server.stop(0);
        }
    }

    private static void assertCollectionRequest(String body) {
        try {
            var document = CalDavXml.parse(body);
            assertEquals(1, document.getElementsByTagNameNS("DAV:", "displayname").getLength());
            assertEquals(1, document.getElementsByTagNameNS("DAV:", "resourcetype").getLength());
            assertEquals(1, document.getElementsByTagNameNS("urn:ietf:params:xml:ns:caldav", "calendar-description")
                    .getLength());
            assertEquals(1, document.getElementsByTagNameNS("DAV:", "current-user-privilege-set").getLength());
            assertEquals(1, document.getElementsByTagNameNS("http://apple.com/ns/ical/", "calendar-color").getLength());
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    private static CalendarCollection calendar(String path, String displayName) {
        return new CalendarCollection(URI.create("https://example.org" + path), displayName);
    }

    private Map<URI, DiscoveryResult> discover(List<CalendarCollection> collections) {
        URI account = URI.create("https://example.org/caldav/");
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "metadata")
                .withConfiguration(new Configuration(Map.of("url", account.toString()))).build();
        LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks = new LinkedBlockingQueue<>();
        Service service = new Service();
        service.setThingHandler(callbackHandler(bridge, callbacks));
        try {
            service.startScan();
            Objects.requireNonNull(callbacks.poll()).accept(collections);
            Map<URI, DiscoveryResult> results = new HashMap<>();
            for (DiscoveryResult result : service.results) {
                String path = Objects.requireNonNull((String) result.getProperties().get("path"));
                URI uri = CalDavUris.reconstruct(account, path);
                assertNull(results.put(uri, result));
            }
            return results;
        } finally {
            service.dispose();
        }
    }

    private AccountHandler callbackHandler(Bridge bridge,
            LinkedBlockingQueue<Consumer<List<CalendarCollection>>> callbacks) {
        return new AccountHandler(bridge, Objects.requireNonNull(mock(HttpClientFactory.class))) {
            @Override
            public void discover(Consumer<List<CalendarCollection>> callback, BooleanSupplier valid, Runnable cancel) {
                callbacks.add(callback::accept);
            }
        };
    }

    @Test
    void replacementScanInvalidatesPendingPreviousScan() {
        AccountHandler handler = Objects.requireNonNull(mock(AccountHandler.class));
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "replacement").build();
        when(handler.getThing()).thenReturn(bridge);
        LinkedBlockingQueue<BooleanSupplier> validity = new LinkedBlockingQueue<>();
        doAnswer(invocation -> {
            validity.add(invocation.getArgument(1));
            return null;
        }).when(handler).discover(any(), any(), any());
        Service service = new Service();
        service.setThingHandler(handler);
        try {
            service.startScan();
            BooleanSupplier previous = Objects.requireNonNull(validity.poll());
            assertTrue(previous.getAsBoolean());
            service.startScan();
            assertFalse(previous.getAsBoolean());
            assertTrue(Objects.requireNonNull(validity.poll()).getAsBoolean());
        } finally {
            service.dispose();
        }
    }

    @Test
    void accountCancellationOnlyInvalidatesItsOwnScan() {
        AccountHandler handler = Objects.requireNonNull(mock(AccountHandler.class));
        Bridge bridge = BridgeBuilder.create(new ThingTypeUID("caldav", "account"), "cancellation").build();
        when(handler.getThing()).thenReturn(bridge);
        LinkedBlockingQueue<BooleanSupplier> validity = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<Runnable> cancellations = new LinkedBlockingQueue<>();
        doAnswer(invocation -> {
            validity.add(invocation.getArgument(1));
            cancellations.add(invocation.getArgument(2));
            return null;
        }).when(handler).discover(any(), any(), any());
        Service service = new Service();
        service.setThingHandler(handler);
        try {
            service.startScan();
            BooleanSupplier previous = Objects.requireNonNull(validity.poll());
            Runnable cancelPrevious = Objects.requireNonNull(cancellations.poll());
            service.startScan();
            BooleanSupplier current = Objects.requireNonNull(validity.poll());
            assertFalse(previous.getAsBoolean());
            cancelPrevious.run();
            assertTrue(current.getAsBoolean());
            Objects.requireNonNull(cancellations.poll()).run();
            assertFalse(current.getAsBoolean());
        } finally {
            service.dispose();
        }
    }
}
