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
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
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
import org.openhab.core.common.registry.AbstractRegistry;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryResultBuilder;
import org.openhab.core.config.discovery.DiscoveryResultFlag;
import org.openhab.core.config.discovery.inbox.Inbox;
import org.openhab.core.config.discovery.internal.AutomaticInboxProcessor;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.storage.Storage;
import org.openhab.core.storage.StorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ManagedThingProvider;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingProvider;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.thing.type.ThingType;
import org.openhab.core.thing.type.ThingTypeRegistry;
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
 * @author Andreas Vilippus - Child calendar identity validation before account discovery
 * @author Andreas Vilippus - Managed handlerless calendars and isolated child publication failures
 * @author Andreas Vilippus - Active calendar representation persistence recovery
 */
@NonNullByDefault
@Timeout(30)
class AccountHandlerTest {
    /** Handler fixture that captures channel or Thing status publications for assertions. */
    private static class Handler extends AccountHandler {
        /** Signals the first observed ONLINE publication. */
        final CountDownLatch online = new CountDownLatch(1);
        /** Signals the first observed OFFLINE publication. */
        final CountDownLatch offline = new CountDownLatch(1);
        /** Number of captured Thing status publications. */
        final AtomicInteger publications = new AtomicInteger();
        /** Ordered Thing status publications available to test assertions. */
        final LinkedBlockingQueue<ThingStatus> statuses = new LinkedBlockingQueue<>();
        /** Most recently captured Thing status. */
        volatile ThingStatus status = ThingStatus.UNKNOWN;
        /** Most recently captured Thing status description. */
        volatile @Nullable String description;
        /** Most recently captured Thing status detail. */
        ThingStatusDetail detail = ThingStatusDetail.NONE;
        /** Test hook invoked before capturing account configuration. */
        volatile Runnable beforeConfiguration = () -> {
        };
        /** Test hook invoked after capturing account configuration. */
        volatile Runnable afterConfiguration = () -> {
        };

        /**
         * Creates a handler fixture that records state and status publications.
         *
         * @param bridge associated account bridge
         * @param factory HTTP client factory used by the fixture
         */
        Handler(Bridge bridge, HttpClientFactory factory) {
            this(bridge, factory, null);
        }

        /**
         * Creates a handler fixture that records state and status publications.
         *
         * @param bridge associated account bridge
         * @param factory HTTP client factory used by the fixture
         * @param scheduler scheduler used by the fixture, or null to use the handler scheduler
         */
        Handler(Bridge bridge, HttpClientFactory factory, @Nullable ScheduledExecutorService scheduler) {
            this(bridge, factory, scheduler, Objects.requireNonNull(mock(ThingRegistry.class)));
        }

        private Handler(Bridge bridge, HttpClientFactory factory, @Nullable ScheduledExecutorService scheduler,
                ThingRegistry registry) {
            super(bridge, factory, registry, () -> ZoneOffset.UTC, scheduler);
            when(registry.getAll()).thenAnswer(invocation -> getThing().getThings());
            when(registry.get(any(ThingUID.class))).thenAnswer(invocation -> getThing().getThings().stream()
                    .filter(thing -> thing.getUID().equals(invocation.getArgument(0))).findFirst().orElse(null));
        }

        /**
         * Runs test hooks around the normal account configuration capture.
         *
         * @return the captured account configuration
         */
        @Override
        public AccountConfiguration configuration() {
            beforeConfiguration.run();
            AccountConfiguration configuration = super.configuration();
            afterConfiguration.run();
            return configuration;
        }

        /**
         * Captures Thing status and detail for the test assertions.
         *
         * @param status new Thing status
         * @param detail status detail
         * @param description status description, if present
         */
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

    /** A captured account poll with its requested delay and cancellable scheduled future. */
    private record Poll(Runnable action, long delay, ScheduledFuture<?> future) {
    }

    /** Scheduler fixture that exposes queued account polls without waiting for wall-clock time. */
    private static final class ControlledScheduler {
        /** Mock scheduler that records delayed polls and executes cleanup tasks immediately. */
        final ScheduledExecutorService executor = Objects.requireNonNull(mock(ScheduledExecutorService.class));
        /** Pending account polls in scheduling order. */
        final Queue<Poll> polls = new ArrayDeque<>();

        /** Captures delayed account work and executes submitted cleanup tasks synchronously. */
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

        /**
         * Removes the next scheduled poll and checks its expected delay.
         *
         * @param expectedDelay expected poll delay in seconds
         * @return the next queued poll after checking its delay
         */
        Poll next(long expectedDelay) {
            Poll poll = Objects.requireNonNull(polls.poll());
            assertEquals(expectedDelay, poll.delay());
            return poll;
        }
    }

    /** HTTP client fixture that supplies discovery responses or a configured transport failure. */
    private static class ScriptedClient extends HttpClient {
        /** Transport failure used for scripted HTTP responses. */
        @Nullable
        Throwable failure;
        /** Number of HTTP requests observed by this fixture. */
        int requests;

        /**
         * Creates a scripted discovery response or configured transport failure.
         *
         * @param uri request target URI
         * @return a scripted HTTP request
         */
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

    /** HTTP client fixture whose startup can be observed or made to fail. */
    private static final class RecoveringClient extends ScriptedClient {
        /** Whether HTTP client startup should fail. */
        boolean fail;
        /** Test hook invoked when HTTP client startup begins. */
        Runnable onStart = () -> {
        };

        /** Runs the startup hook and optionally rejects startup with a simulated connection failure. */
        @Override
        protected void doStart() throws Exception {
            onStart.run();
            if (fail) {
                throw new IOException("Test connection failure");
            }
            super.doStart();
        }
    }

    /** Calendar fixture that captures synchronization results within a supplied account bridge. */
    private static final class Calendar extends CalendarHandler {
        /** Channel states published by the test calendar. */
        final Map<String, State> states = new ConcurrentHashMap<>();
        /** Most recently captured Thing status. */
        volatile ThingStatus status = ThingStatus.UNKNOWN;
        /** Most recently captured Thing status detail. */
        ThingStatusDetail detail = ThingStatusDetail.NONE;
        /** Account bridge returned to the calendar fixture. */
        final Bridge bridge;

        /**
         * Creates a test calendar attached to the supplied account bridge.
         *
         * @param id test calendar identifier
         * @param path configured calendar collection reference
         * @param bridge associated account bridge
         */
        Calendar(String id, String path, Bridge bridge) {
            super(ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), id).withBridge(bridge.getUID())
                    .withConfiguration(new Configuration(Map.of("path", path))).build(), () -> ZoneOffset.UTC,
                    Objects.requireNonNull(mock()));
            this.bridge = bridge;
            setCallback(Objects.requireNonNull(mock(ThingHandlerCallback.class)));
        }

        /**
         * Supplies the account bridge associated with the test calendar.
         *
         * @return the account bridge used by the fixture
         */
        @Override
        protected @Nullable Bridge getBridge() {
            return bridge;
        }

        /**
         * Captures calendar channel state without publishing to the runtime.
         *
         * @param channel channel receiving the command or state
         * @param state published channel state
         */
        @Override
        protected void updateState(ChannelUID channel, State state) {
            states.put(channel.getId(), state);
        }

        /**
         * Captures Thing status and detail for the test assertions.
         *
         * @param status new Thing status
         * @param detail status detail
         * @param description status description, if present
         */
        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            this.status = status;
            this.detail = detail;
        }
    }

    /** Typed storage contract used by calendar test fixtures and mocks. */
    private interface CalendarStorage extends Storage<String> {
    }

    /** Calendar fixture that exposes representation publication and initialization counts. */
    private static final class IdentityCalendar extends CalendarHandler {
        /** Account handler used by the calendar representation fixture. */
        final Handler account;
        /** Number of calendar initializations observed by the fixture. */
        int initializations;
        /** Most recently captured Thing status. */
        ThingStatus status = ThingStatus.UNKNOWN;
        /** Most recently captured Thing status detail. */
        ThingStatusDetail detail = ThingStatusDetail.NONE;

        /**
         * Creates an active calendar fixture for account-scoped representation checks.
         *
         * @param account account handler owning the test calendar
         * @param path configured calendar collection reference
         */
        IdentityCalendar(Handler account, String path) {
            this(account, "identity", path);
        }

        /**
         * Creates an active calendar fixture for account-scoped representation checks.
         *
         * @param account account handler owning the test calendar
         * @param id test calendar identifier
         * @param path configured calendar collection reference
         */
        IdentityCalendar(Handler account, String id, String path) {
            super(ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), id)
                    .withBridge(account.getThing().getUID()).withConfiguration(new Configuration(Map.of("path", path)))
                    .withProperties(Map.of("calendarColor", "#123456", "calendarDescription", "Work",
                            "calendarPrivileges", "read"))
                    .build(), () -> ZoneOffset.UTC, Objects.requireNonNull(mock(CalendarStorage.class)),
                    Clock.fixed(Instant.parse("2026-10-12T09:00:00Z"), ZoneOffset.UTC),
                    Objects.requireNonNull(mock(ScheduledExecutorService.class)));
            this.account = account;
        }

        /** Counts calendar initializations before applying the normal initialization behavior. */
        @Override
        public void initialize() {
            initializations++;
            super.initialize();
        }

        /**
         * Supplies the account bridge associated with the test calendar.
         *
         * @return the account bridge used by the fixture
         */
        @Override
        protected @Nullable Bridge getBridge() {
            return account.getThing();
        }

        /**
         * Ignores event state so the fixture isolates representation publication.
         *
         * @param channel channel receiving the command or state
         * @param state published channel state
         */
        @Override
        protected void updateState(ChannelUID channel, State state) {
        }

        /**
         * Captures Thing status and detail for the test assertions.
         *
         * @param status new Thing status
         * @param detail status detail
         * @param description status description, if present
         */
        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            this.status = status;
            this.detail = detail;
            getThing().setStatusInfo(new ThingStatusInfo(status, detail, description));
        }
    }

    /** Active account and calendar setup for representation changes and discovery ordering. */
    private static final class IdentityFixture implements AutoCloseable {
        /** Controllable scheduler used to inspect and execute pending account work. */
        final ControlledScheduler scheduler = new ControlledScheduler();
        /** Account handler used by the calendar representation fixture. */
        final Handler account;
        /** Active calendar handler whose representation is checked before discovery. */
        final IdentityCalendar calendar;
        /** Scripted HTTP client used to detect unexpected network access. */
        final ScriptedClient http = new ScriptedClient();
        /** Core handler callback used to observe or persist Thing updates. */
        final ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        /** Discovery service mock used to verify scan ordering. */
        final CalDavDiscoveryService discovery = Objects.requireNonNull(mock(CalDavDiscoveryService.class));
        /** Calendar Thing associated with the account fixture. */
        final Thing child;
        /** Calendar representation expected when discovery starts. */
        @Nullable
        String expectedIdentity;
        /** Number of discovery scans requested by the account fixture. */
        int scans;
        /** Expected initialization count when checking representation-only updates. */
        int expectedInitializations = 1;

        /**
         * Initializes an account and calendar with the expected representation before discovery.
         *
         * @param path configured calendar collection reference
         * @param identity calendar representation expected or stored by the fixture
         */
        IdentityFixture(String path, String identity) {
            Bridge bridge = Objects.requireNonNull(
                    spy(configuredBridge(Map.of("url", "https://server-a.example/dav/", "syncMode", "FULL"))));
            HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
            when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class))).thenReturn(http);
            account = new Handler(bridge, factory, scheduler.executor);
            bridge.setHandler(account);
            calendar = new IdentityCalendar(account, path);
            calendar.setCallback(callback);
            child = calendar.getThing();
            child.setHandler(calendar);
            when(bridge.getThings()).thenReturn(List.of(child));
            calendar.initialize();
            expectedIdentity = identity;
            Objects.requireNonNull(doAnswer(invocation -> {
                assertEquals(expectedIdentity, calendar.getThing().getProperties().get("calendarIdentity"),
                        "Child representation must be current before discovery starts");
                assertEquals(expectedInitializations, calendar.initializations);
                scans++;
                return null;
            }).when(discovery)).startScan();
            account.registerDiscoveryService(discovery);
            account.initialize();
            assertEquals(1, scans);
        }

        /**
         * Reconfigures the account and checks calendar representation ordering before discovery.
         *
         * @param values configuration values to apply
         * @param identity calendar representation expected or stored by the fixture
         */
        void change(Map<String, Object> values, @Nullable String identity) {
            expectedIdentity = identity;
            account.thingUpdated(replacement(values));
            assertEquals(expectedInitializations, calendar.initializations);
            assertEquals(identity, calendar.getThing().getProperties().get("calendarIdentity"));
            assertEquals("#123456", calendar.getThing().getProperties().get("calendarColor"));
            assertEquals("Work", calendar.getThing().getProperties().get("calendarDescription"));
            assertEquals("read", calendar.getThing().getProperties().get("calendarPrivileges"));
            assertEquals(0, http.requests);
        }

        /**
         * Builds an account replacement with the existing child and updated settings.
         *
         * @param values configuration values to apply
         * @return an account bridge containing the updated configuration
         */
        Bridge replacement(Map<String, Object> values) {
            Map<String, Object> config = new java.util.HashMap<>(account.getThing().getConfiguration().getProperties());
            config.putAll(values);
            Bridge bridge = Objects.requireNonNull(spy(configuredBridge(config)));
            bridge.setHandler(account);
            when(bridge.getThings()).thenReturn(List.of(child));
            return bridge;
        }

        /** Disposes fixture handlers and releases any local server resources. */
        @Override
        public void close() {
            calendar.dispose();
            account.dispose();
        }
    }

    /** Checks that an account origin change removes incompatible child representations before discovery. */
    @Test
    void accountReinitializationClearsCrossOriginIdentityBeforeDiscovery() {
        try (IdentityFixture fixture = new IdentityFixture("https://server-a.example/calendar/work/",
                "caldav:account:scheduled|/calendar/work/")) {
            fixture.change(Map.of("url", "https://server-b.example/dav/"), null);
            assertEquals(2, fixture.scans);
            assertEquals(ThingStatus.OFFLINE, fixture.calendar.status);
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, fixture.calendar.detail);
            fixture.change(Map.of("url", "https://server-a.example/dav/"), "caldav:account:scheduled|/calendar/work/");
            assertEquals(3, fixture.scans);
            assertEquals(ThingStatus.UNKNOWN, fixture.calendar.status);
        }
    }

    /** Checks that an unchanged origin-relative representation causes no redundant property publication. */
    @Test
    void accountReinitializationKeepsRootRelativeIdentityWithoutPropertyWrites() {
        try (IdentityFixture fixture = new IdentityFixture("/calendar/work/",
                "caldav:account:scheduled|/calendar/work/")) {
            fixture.change(
                    Map.of("url", "https://server-b.example/dav/", "username", "new-user", "password", "new-password"),
                    "caldav:account:scheduled|/calendar/work/");
            assertEquals(2, fixture.scans);
            Objects.requireNonNull(verify(fixture.callback, times(1))).thingUpdated(any(Thing.class));
        }
    }

    /** Checks that relative calendar references are resolved against the new account directory before discovery. */
    @Test
    void accountBaseDirectoryChangeUpdatesRelativeIdentityBeforeDiscovery() {
        try (IdentityFixture fixture = new IdentityFixture("work/", "caldav:account:scheduled|/dav/work/")) {
            fixture.change(Map.of("url", "https://server-a.example/new-base/"),
                    "caldav:account:scheduled|/new-base/work/");
            assertEquals(2, fixture.scans);
            Objects.requireNonNull(verify(fixture.callback, times(2))).thingUpdated(any(Thing.class));
        }
    }

    /** Checks that invalid account settings remove child representations and prevent collection discovery. */
    @Test
    void invalidAccountConfigurationClearsChildIdentityAndPreventsDiscovery() {
        for (Map<String, Object> invalid : List.<Map<String, Object>> of(Map.of("url", "ftp://server-a.example/"),
                Map.of("refreshInterval", new BigDecimal("30.5")), Map.of("username", "user", "password", ""))) {
            try (IdentityFixture fixture = new IdentityFixture("/calendar/work/",
                    "caldav:account:scheduled|/calendar/work/")) {
                fixture.change(invalid, null);
                assertEquals(1, fixture.scans);
                assertEquals(ThingStatus.OFFLINE, fixture.account.status);
                assertEquals(ThingStatus.OFFLINE, fixture.calendar.status);
                assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, fixture.calendar.detail);
            }
        }
    }

    /** Checks that calendars outside a changed account horizon lose their representation before discovery. */
    @Test
    void changedAccountHorizonClearsInvalidCalendarIdentityBeforeDiscovery() {
        try (IdentityFixture fixture = new IdentityFixture("/calendar/work/",
                "caldav:account:scheduled|/calendar/work/")) {
            fixture.change(Map.of("maxFutureDays", 1), null);
            assertEquals(2, fixture.scans);
            assertEquals(ThingStatus.OFFLINE, fixture.calendar.status);
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, fixture.calendar.detail);
        }
    }

    /** Checks that delayed account initialization cannot publish representation data from an obsolete context. */
    @Test
    void obsoleteAccountInitializationCannotReplaceNewerContext() throws Exception {
        CountDownLatch captured = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicBoolean pause = new AtomicBoolean(true);
        var executor = Executors.newSingleThreadExecutor();
        try (IdentityFixture fixture = new IdentityFixture("work/", "caldav:account:scheduled|/dav/work/")) {
            fixture.account.afterConfiguration = () -> {
                if (pause.compareAndSet(true, false)) {
                    captured.countDown();
                    try {
                        assertTrue(release.await(10, TimeUnit.SECONDS));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                }
            };
            Bridge obsolete = fixture.replacement(Map.of("url", "https://server-b.example/old-base/"));
            var initializing = executor.submit(() -> fixture.account.thingUpdated(obsolete));
            assertTrue(captured.await(10, TimeUnit.SECONDS));
            fixture.change(Map.of("url", "https://server-c.example/new-base/"),
                    "caldav:account:scheduled|/new-base/work/");
            release.countDown();
            initializing.get(10, TimeUnit.SECONDS);
            assertEquals("caldav:account:scheduled|/new-base/work/",
                    fixture.calendar.getThing().getProperties().get("calendarIdentity"));
            assertEquals(2, fixture.scans, "Obsolete account context must not start another discovery");
            assertEquals(ThingStatus.UNKNOWN, fixture.account.status);
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    /** Checks that a delayed calendar initialization cannot restore an old account-scoped representation. */
    @Test
    void pendingCalendarInitializationCannotRestoreIdentityFromOldAccount() throws Exception {
        CountDownLatch captured = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicBoolean pause = new AtomicBoolean(true);
        var executor = Executors.newSingleThreadExecutor();
        try (IdentityFixture fixture = new IdentityFixture("https://server-a.example/calendar/work/",
                "caldav:account:scheduled|/calendar/work/")) {
            fixture.expectedInitializations = 2;
            fixture.account.afterConfiguration = () -> {
                if (pause.compareAndSet(true, false)) {
                    captured.countDown();
                    try {
                        assertTrue(release.await(10, TimeUnit.SECONDS));
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new IllegalStateException(e);
                    }
                }
            };
            var initializing = executor.submit(fixture.calendar::initialize);
            assertTrue(captured.await(10, TimeUnit.SECONDS));
            fixture.change(Map.of("url", "https://server-b.example/dav/"), null);
            release.countDown();
            initializing.get(10, TimeUnit.SECONDS);
            assertFalse(fixture.calendar.getThing().getProperties().containsKey("calendarIdentity"));
            assertEquals(ThingStatus.OFFLINE, fixture.calendar.status);
            fixture.calendar.synchronize((method, uri, body, depth) -> {
                fail("Cross-origin calendar must remain inactive after obsolete initialization returns");
                return "";
            }, fixture.account.configuration(), () -> true);
            assertEquals(ThingStatus.OFFLINE, fixture.calendar.status);
            assertEquals(2, fixture.scans);
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    /** In-memory backing storage for actual Core managed Thing persistence. */
    private static final class ManagedStorage implements Storage<Object> {
        private final Map<String, Object> values = new HashMap<>();

        /**
         * Updates the in-memory storage entry used by persistence tests.
         *
         * @param key storage entry key
         * @param value replacement stored value, or null
         * @return the value previously stored under the key, if any
         */
        @Override
        public @Nullable Object put(String key, @Nullable Object value) {
            return value == null ? values.remove(key) : values.put(key, value);
        }

        /**
         * Reads a persisted fixture value without a running storage service.
         *
         * @param key storage entry key
         * @return the stored value, if present
         */
        @Override
        public @Nullable Object get(String key) {
            return values.get(key);
        }

        /**
         * Removes a stored fixture entry.
         *
         * @param key storage entry key
         * @return the removed value, if present
         */
        @Override
        public @Nullable Object remove(String key) {
            return values.remove(key);
        }

        /**
         * Checks whether the in-memory storage contains the entry.
         *
         * @param key storage entry key
         * @return whether the entry is present
         */
        @Override
        public boolean containsKey(String key) {
            return values.containsKey(key);
        }

        /**
         * Returns the keys currently held by the fixture storage.
         *
         * @return the currently stored entry keys
         */
        @Override
        public Collection<String> getKeys() {
            return values.keySet();
        }

        /**
         * Returns the values currently held by the fixture storage.
         *
         * @return the currently stored values
         */
        @Override
        public Collection<@Nullable Object> getValues() {
            return values.values().stream().map(value -> (@Nullable Object) value).toList();
        }
    }

    /** Registry fixture that preserves runtime handler and status state during provider updates. */
    private static final class ManagedRegistry extends AbstractRegistry<Thing, ThingUID, ThingProvider> {
        /** Number of provider updates observed by the registry fixture. */
        int updates;

        /**
         * Registers the managed provider used to observe persisted Thing updates.
         *
         * @param provider managed Thing provider to register
         */
        ManagedRegistry(ManagedThingProvider provider) {
            super(null);
            setManagedProvider(provider);
            addProvider(provider);
        }

        /**
         * Retains the existing runtime handler and status when the provider replaces a Thing.
         *
         * @param oldThing previous registry element
         * @param thing calendar or account Thing to handle
         */
        @Override
        protected void onUpdateElement(Thing oldThing, Thing thing) {
            Thing existing = Objects.requireNonNull(get(thing.getUID()));
            thing.setHandler(existing.getHandler());
            thing.setStatusInfo(existing.getStatusInfo());
            updates++;
        }
    }

    /** Managed provider, registry and account setup for handlerless calendar representation tests. */
    private static final class ManagedChildren implements AutoCloseable {
        /** Core managed provider used to inspect persisted calendar Things. */
        final ManagedThingProvider provider;
        /** Registry view of the managed calendars and their runtime state. */
        final ManagedRegistry registry;
        /** Controllable scheduler used to inspect and execute pending account work. */
        final ControlledScheduler scheduler = new ControlledScheduler();
        /** HTTP factory mock used when the account creates its transport. */
        final HttpClientFactory factory = Objects.requireNonNull(mock(HttpClientFactory.class));
        /** Account handler used by the calendar representation fixture. */
        final Handler account;
        /** Core handler callback used to observe or persist Thing updates. */
        final ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));
        /** Discovery service mock used to verify scan ordering. */
        final CalDavDiscoveryService discovery = Objects.requireNonNull(mock(CalDavDiscoveryService.class));
        /** Number of discovery scans requested by the account fixture. */
        int scans;
        /** Assertion hook executed before a discovery scan is recorded. */
        Runnable beforeScan = () -> {
        };

        /** Creates managed calendar persistence and controlled account discovery fixtures. */
        ManagedChildren() {
            StorageService storageService = Objects.requireNonNull(mock(StorageService.class));
            ManagedStorage storage = new ManagedStorage();
            when(storageService.getStorage(anyString(), any(ClassLoader.class))).thenAnswer(invocation -> storage);
            provider = new ManagedThingProvider(storageService);
            registry = new ManagedRegistry(provider);
            Bridge bridge = bridge(Map.of("url", "https://server-a.example/dav/", "syncMode", "FULL"));
            when(factory.createHttpClient(anyString(), any(SslContextFactory.Client.class)))
                    .thenAnswer(invocation -> new ScriptedClient());
            account = new Handler(bridge, factory, scheduler.executor);
            account.setCallback(callback);
            Objects.requireNonNull(doAnswer(invocation -> {
                Thing updated = invocation.getArgument(0);
                assertNotNull(provider.update(updated));
                return null;
            }).when(callback)).thingUpdated(any(Thing.class));
            Objects.requireNonNull(doAnswer(invocation -> {
                beforeScan.run();
                scans++;
                return null;
            }).when(discovery)).startScan();
            account.registerDiscoveryService(discovery);
        }

        /**
         * Builds an account bridge whose children are read from the managed registry.
         *
         * @param values configuration values to apply
         * @return the configured account bridge fixture
         */
        Bridge bridge(Map<String, Object> values) {
            Bridge bridge = Objects.requireNonNull(spy(configuredBridge(values)));
            when(bridge.getThings()).thenAnswer(invocation -> List.copyOf(registry.getAll()));
            return bridge;
        }

        /**
         * Persists a disabled handlerless calendar with the supplied representation.
         *
         * @param id test calendar identifier
         * @param path configured calendar collection reference
         * @param identity calendar representation expected or stored by the fixture
         * @return the persisted disabled calendar Thing
         */
        Thing calendar(String id, String path, @Nullable String identity) {
            Map<String, String> properties = new HashMap<>(Map.of("calendarColor", "#123456", "calendarDescription",
                    "Work", "calendarPrivileges", "read", "thingTypeVersion", "1"));
            if (identity != null) {
                properties.put("calendarIdentity", identity);
            }
            Thing thing = ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), id)
                    .withBridge(account.getThing().getUID()).withLabel("Disabled calendar").withLocation("Office")
                    .withConfiguration(new Configuration(Map.of("path", path))).withProperties(properties).build();
            thing.setStatusInfo(new ThingStatusInfo(ThingStatus.UNINITIALIZED, ThingStatusDetail.DISABLED, null));
            provider.add(thing);
            return thing;
        }

        /**
         * Reconfigures the account and checks calendar representation ordering before discovery.
         *
         * @param url replacement account endpoint
         */
        void change(String url) {
            account.thingUpdated(bridge(Map.of("url", url, "syncMode", "FULL")));
        }

        /**
         * Checks that a representation update preserves the calendar configuration, metadata and disabled state.
         *
         * @param original calendar Thing whose unrelated data must remain unchanged
         * @param identity calendar representation expected or stored by the fixture
         */
        void unchanged(Thing original, @Nullable String identity) {
            Thing stored = Objects.requireNonNull(provider.get(original.getUID()));
            assertEquals(identity, stored.getProperties().get("calendarIdentity"));
            Map<String, String> before = new HashMap<>(original.getProperties());
            Map<String, String> after = new HashMap<>(stored.getProperties());
            before.remove("calendarIdentity");
            after.remove("calendarIdentity");
            assertEquals(before, after);
            assertEquals(original.getConfiguration(), stored.getConfiguration());
            assertEquals(original.getUID(), stored.getUID());
            assertEquals(original.getBridgeUID(), stored.getBridgeUID());
            assertEquals(original.getLabel(), stored.getLabel());
            assertEquals(original.getLocation(), stored.getLocation());
            assertNull(Objects.requireNonNull(registry.get(original.getUID())).getHandler());
            assertEquals(ThingStatusDetail.DISABLED,
                    Objects.requireNonNull(registry.get(original.getUID())).getStatusInfo().getStatusDetail());
        }

        /** Disposes fixture handlers and releases any local server resources. */
        @Override
        public void close() {
            account.dispose();
            registry.getAll().forEach(thing -> {
                if (thing.getHandler() instanceof CalendarHandler calendar) {
                    calendar.dispose();
                }
            });
        }
    }

    /** Checks that incompatible handlerless representations are removed from persistence before Core Inbox matching. */
    @Test
    void handlerlessCrossOriginIdentityIsPersistentlyRemovedBeforeCoreInboxMatching() {
        try (ManagedChildren fixture = new ManagedChildren()) {
            Thing calendar = fixture.calendar("disabled", "https://server-a.example/calendar/work/",
                    "caldav:account:scheduled|/calendar/work/");
            fixture.account.initialize();
            fixture.beforeScan = () -> assertNull(Objects.requireNonNull(fixture.registry.get(calendar.getUID()))
                    .getProperties().get("calendarIdentity"));
            fixture.change("https://server-b.example/dav/");
            fixture.unchanged(calendar, null);
            assertEquals(1, fixture.registry.updates);
            assertEquals(2, fixture.scans);
            assertEquals("caldav:account:scheduled|/calendar/work/", calendar.getProperties().get("calendarIdentity"),
                    "The old registry object must not be mutated");
            DiscoveryResult result = DiscoveryResultBuilder.create(new ThingUID("caldav:calendar:scheduled:new"))
                    .withBridge(fixture.account.getThing().getUID())
                    .withProperties(Map.of("calendarIdentity", "caldav:account:scheduled|/calendar/work/"))
                    .withRepresentationProperty("calendarIdentity").build();
            ThingType type = Objects.requireNonNull(mock(ThingType.class));
            when(type.getRepresentationProperty()).thenReturn("calendarIdentity");
            ThingTypeRegistry types = Objects.requireNonNull(mock(ThingTypeRegistry.class));
            when(types.getThingType(calendar.getThingTypeUID())).thenReturn(type);
            ThingRegistry things = Objects.requireNonNull(mock(ThingRegistry.class));
            when(things.stream()).thenAnswer(invocation -> fixture.registry.stream());
            Inbox inbox = Objects.requireNonNull(mock(Inbox.class));
            new AutomaticInboxProcessor(types, things, inbox).thingAdded(inbox, result);
            verify(inbox, never()).setFlag(result.getThingUID(), DiscoveryResultFlag.IGNORED);
        }
    }

    /** Checks that stable handlerless collection references survive an origin change without unnecessary writes. */
    @Test
    void handlerlessRootRelativeIdentitySurvivesServerChangeWithoutWrites() {
        try (ManagedChildren fixture = new ManagedChildren()) {
            Thing calendar = fixture.calendar("disabled", "/calendar/work/",
                    "caldav:account:scheduled|/calendar/work/");
            fixture.account.initialize();
            fixture.change("https://server-b.example/dav/");
            fixture.unchanged(calendar, "caldav:account:scheduled|/calendar/work/");
            assertEquals(0, fixture.registry.updates);
            assertEquals(2, fixture.scans);
            Objects.requireNonNull(verify(fixture.callback, never())).thingUpdated(any(Thing.class));
        }
    }

    /** Checks that configured calendars obtain persisted representations even without an active handler. */
    @Test
    void handlerlessCalendarWithoutIdentityGetsPersistentRepresentationWithoutHandler() {
        try (ManagedChildren fixture = new ManagedChildren()) {
            Thing calendar = fixture.calendar("disabled", "/calendar/work/", null);
            fixture.beforeScan = () -> assertEquals("caldav:account:scheduled|/calendar/work/", Objects
                    .requireNonNull(fixture.registry.get(calendar.getUID())).getProperties().get("calendarIdentity"));
            fixture.account.initialize();
            fixture.unchanged(calendar, "caldav:account:scheduled|/calendar/work/");
            assertEquals(1, fixture.registry.updates);
            assertEquals(1, fixture.scans);
        }
    }

    /** Checks that handlerless relative references follow account directory changes in persistent state. */
    @Test
    void handlerlessRelativeReferenceTracksAccountBaseDirectory() {
        try (ManagedChildren fixture = new ManagedChildren()) {
            Thing calendar = fixture.calendar("disabled", "work/", "caldav:account:scheduled|/dav/work/");
            fixture.account.initialize();
            fixture.beforeScan = () -> assertEquals("caldav:account:scheduled|/new-base/work/", Objects
                    .requireNonNull(fixture.registry.get(calendar.getUID())).getProperties().get("calendarIdentity"));
            fixture.change("https://server-b.example/new-base/");
            fixture.unchanged(calendar, "caldav:account:scheduled|/new-base/work/");
            assertEquals(1, fixture.registry.updates);
        }
    }

    /** Checks that a rejected update for a removed child does not invalidate the account or other calendars. */
    @Test
    void removedChildCallbackCannotInvalidateAccountOrOtherCalendar() {
        try (ManagedChildren fixture = new ManagedChildren()) {
            IdentityCalendar first = new IdentityCalendar(fixture.account, "a", "work/");
            IdentityCalendar second = new IdentityCalendar(fixture.account, "b", "family/");
            for (IdentityCalendar calendar : List.of(first, second)) {
                calendar.setCallback(fixture.callback);
                calendar.getThing().setHandler(calendar);
                fixture.provider.add(calendar.getThing());
                calendar.initialize();
            }
            fixture.account.initialize();
            Objects.requireNonNull(doAnswer(invocation -> {
                Thing updated = invocation.getArgument(0);
                if (updated.getUID().equals(first.getThing().getUID())) {
                    first.dispose();
                    fixture.provider.remove(updated.getUID());
                    throw new IllegalArgumentException("Thing is no longer known to the registry");
                }
                assertNotNull(fixture.provider.update(updated));
                return null;
            }).when(fixture.callback)).thingUpdated(any(Thing.class));
            fixture.beforeScan = () -> assertEquals("caldav:account:scheduled|/new-base/family/",
                    Objects.requireNonNull(fixture.registry.get(second.getThing().getUID())).getProperties()
                            .get("calendarIdentity"));
            fixture.change("https://server-b.example/new-base/");
            assertNull(fixture.registry.get(first.getThing().getUID()));
            assertEquals("caldav:account:scheduled|/new-base/family/",
                    Objects.requireNonNull(fixture.provider.get(second.getThing().getUID())).getProperties()
                            .get("calendarIdentity"));
            assertEquals(ThingStatus.UNKNOWN, fixture.account.status);
            assertEquals(ThingStatusDetail.NONE, fixture.account.detail);
            assertEquals(2, fixture.scans);
            verify(fixture.factory, times(2)).createHttpClient(anyString(), any(SslContextFactory.Client.class));
        }
    }

    /**
     * Checks that a live child's publication failure delays discovery while preserving session recovery and other
     * children.
     */
    @Test
    void liveChildPublicationFailureDefersDiscoveryButStartsSessionAndRetainsOtherIdentities() {
        try (ManagedChildren fixture = new ManagedChildren()) {
            Thing first = fixture.calendar("a", "work/", "caldav:account:scheduled|/dav/work/");
            Thing second = fixture.calendar("b", "family/", "caldav:account:scheduled|/dav/family/");
            fixture.account.initialize();
            AtomicBoolean failing = new AtomicBoolean(true);
            Objects.requireNonNull(doAnswer(invocation -> {
                Thing updated = invocation.getArgument(0);
                if (updated.getUID().equals(first.getUID()) && failing.get()) {
                    throw new IllegalStateException("Property persistence unavailable");
                }
                assertNotNull(fixture.provider.update(updated));
                return null;
            }).when(fixture.callback)).thingUpdated(any(Thing.class));
            fixture.change("https://server-b.example/new-base/");
            assertEquals(ThingStatus.UNKNOWN, fixture.account.status);
            assertEquals(ThingStatusDetail.NONE, fixture.account.detail);
            assertEquals("caldav:account:scheduled|/new-base/family/", Objects
                    .requireNonNull(fixture.provider.get(second.getUID())).getProperties().get("calendarIdentity"));
            assertEquals(1, fixture.scans, "Discovery must wait until the remaining live identity is current");
            verify(fixture.factory, times(2)).createHttpClient(anyString(), any(SslContextFactory.Client.class));
            fixture.scheduler.next(0).action().run();
            assertEquals(1, fixture.scans);
            Poll retry = fixture.scheduler.next(300);
            assertTrue(fixture.scheduler.polls.isEmpty(), "No immediate retry storm");
            failing.set(false);
            fixture.beforeScan = () -> assertEquals("caldav:account:scheduled|/new-base/work/", Objects
                    .requireNonNull(fixture.registry.get(first.getUID())).getProperties().get("calendarIdentity"));
            retry.action().run();
            assertEquals(2, fixture.scans);
            fixture.unchanged(first, "caldav:account:scheduled|/new-base/work/");
            fixture.unchanged(second, "caldav:account:scheduled|/new-base/family/");
            assertEquals(ThingStatusDetail.NONE, fixture.account.detail);
        }
    }

    /**
     * Checks that failed active-calendar persistence is retried before discovery despite an already changed in-memory
     * model.
     */
    @Test
    void activeChildPublicationFailureIsRetriedBeforeDiscoveryAndPersistedRecovery() {
        for (String path : List.of("work/", "https://server-a.example/calendar/work/")) {
            try (ManagedChildren fixture = new ManagedChildren()) {
                IdentityCalendar calendar = new IdentityCalendar(fixture.account, "active", path);
                calendar.setCallback(fixture.callback);
                calendar.getThing().setHandler(calendar);
                fixture.provider.add(calendar.getThing());
                calendar.initialize();
                Thing other = fixture.calendar("other", "family/", "caldav:account:scheduled|/dav/family/");
                fixture.account.initialize();
                String oldIdentity = calendar.getThing().getProperties().get("calendarIdentity");
                String expectedIdentity = "work/".equals(path) ? "caldav:account:scheduled|/new-base/work/" : null;
                AtomicBoolean failing = new AtomicBoolean(true);
                Objects.requireNonNull(doAnswer(invocation -> {
                    Thing updated = invocation.getArgument(0);
                    if (updated.getUID().equals(calendar.getThing().getUID()) && failing.get()) {
                        throw new IllegalStateException("Property persistence unavailable");
                    }
                    assertNotNull(fixture.provider.update(updated));
                    return null;
                }).when(fixture.callback)).thingUpdated(any(Thing.class));
                fixture.change("https://server-b.example/new-base/");
                assertEquals(oldIdentity, Objects.requireNonNull(fixture.provider.get(calendar.getThing().getUID()))
                        .getProperties().get("calendarIdentity"));
                assertEquals("caldav:account:scheduled|/new-base/family/", Objects
                        .requireNonNull(fixture.provider.get(other.getUID())).getProperties().get("calendarIdentity"));
                assertEquals(ThingStatus.UNKNOWN, fixture.account.status);
                assertEquals(ThingStatusDetail.NONE, fixture.account.detail);
                assertEquals(1, fixture.scans);
                fixture.scheduler.next(0).action().run();
                assertEquals(1, fixture.scans, "An in-memory property change is not a successful publication");
                assertEquals(oldIdentity, Objects.requireNonNull(fixture.provider.get(calendar.getThing().getUID()))
                        .getProperties().get("calendarIdentity"));
                Poll retry = fixture.scheduler.next(300);
                assertTrue(fixture.scheduler.polls.isEmpty(), "Publication failures must not cause a busy loop");
                failing.set(false);
                fixture.beforeScan = () -> assertEquals(expectedIdentity,
                        Objects.requireNonNull(fixture.provider.get(calendar.getThing().getUID())).getProperties()
                                .get("calendarIdentity"));
                retry.action().run();
                assertEquals(2, fixture.scans);
                assertEquals(expectedIdentity,
                        Objects.requireNonNull(fixture.provider.get(calendar.getThing().getUID())).getProperties()
                                .get("calendarIdentity"));
                assertEquals(expectedIdentity, calendar.getThing().getProperties().get("calendarIdentity"));
                clearInvocations(fixture.callback);
                fixture.account.initialize();
                Objects.requireNonNull(verify(fixture.callback, never())).thingUpdated(any(Thing.class));
            }
        }
    }

    /**
     * Checks that an invalid calendar cannot acknowledge revalidation while its representation removal remains
     * unpersisted.
     */
    @Test
    void invalidRawCalendarPublicationFailureCannotBeAcceptedAsCompletedValidation() {
        try (ManagedChildren fixture = new ManagedChildren()) {
            IdentityCalendar calendar = new IdentityCalendar(fixture.account, "active", "/calendar/work/");
            calendar.setCallback(fixture.callback);
            calendar.getThing().setHandler(calendar);
            fixture.provider.add(calendar.getThing());
            calendar.initialize();
            fixture.account.initialize();
            Thing invalid = ThingBuilder.create(calendar.getThing())
                    .withConfiguration(
                            new Configuration(Map.of("path", "/calendar/work/", "maxEvents", new BigDecimal("1.5"))))
                    .build();
            assertNotNull(fixture.provider.update(invalid));
            AtomicBoolean failing = new AtomicBoolean(true);
            Objects.requireNonNull(doAnswer(invocation -> {
                if (failing.get()) {
                    throw new IllegalStateException("Property persistence unavailable");
                }
                assertNotNull(fixture.provider.update(invocation.getArgument(0)));
                return null;
            }).when(fixture.callback)).thingUpdated(any(Thing.class));
            assertThrows(IllegalStateException.class, () -> calendar.thingUpdated(invalid));
            assertFalse(calendar.getThing().getProperties().containsKey("calendarIdentity"));
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, calendar.detail);
            fixture.account.initialize();
            assertEquals(1, fixture.scans, "A local configuration error does not acknowledge a failed property write");
            fixture.scheduler.next(0).action().run();
            assertEquals(1, fixture.scans);
            Poll retry = fixture.scheduler.next(300);
            assertTrue(fixture.scheduler.polls.isEmpty());
            assertEquals("caldav:account:scheduled|/calendar/work/",
                    Objects.requireNonNull(fixture.provider.get(calendar.getThing().getUID())).getProperties()
                            .get("calendarIdentity"));
            failing.set(false);
            fixture.beforeScan = () -> assertFalse(
                    Objects.requireNonNull(fixture.provider.get(calendar.getThing().getUID())).getProperties()
                            .containsKey("calendarIdentity"));
            retry.action().run();
            assertEquals(2, fixture.scans);
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, calendar.detail);
            assertEquals(ThingStatusDetail.NONE, fixture.account.detail);
        }
    }

    /**
     * Checks that a completed local configuration error does not obstruct other calendar representations or discovery.
     */
    @Test
    void invalidCalendarWithoutParsedConfigurationDoesNotBlockOtherRepresentationsOrDiscovery() {
        try (ManagedChildren fixture = new ManagedChildren()) {
            IdentityCalendar invalid = new IdentityCalendar(fixture.account, "invalid", "/calendar/invalid/");
            invalid.getThing().getConfiguration().put("maxEvents", new BigDecimal("1.5"));
            invalid.getThing().setHandler(invalid);
            invalid.setCallback(fixture.callback);
            fixture.provider.add(invalid.getThing());
            invalid.initialize();
            Thing valid = fixture.calendar("valid", "/calendar/work/", null);
            fixture.account.initialize();
            fixture.unchanged(valid, "caldav:account:scheduled|/calendar/work/");
            assertFalse(invalid.getThing().getProperties().containsKey("calendarIdentity"));
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, invalid.detail);
            assertEquals(ThingStatusDetail.NONE, fixture.account.detail);
            assertEquals(1, fixture.scans);
        }
    }

    /** Checks that delayed old-context persistence completes before a newer account releases discovery. */
    @Test
    void delayedOldContextPublicationCannotOvertakeNewAccountDiscovery() throws Exception {
        CountDownLatch publishing = new CountDownLatch(1), release = new CountDownLatch(1);
        var executor = Executors.newSingleThreadExecutor();
        try (ManagedChildren fixture = new ManagedChildren()) {
            Thing calendar = fixture.calendar("disabled", "work/", "caldav:account:scheduled|/dav/work/");
            fixture.account.initialize();
            AtomicBoolean pause = new AtomicBoolean(true);
            Objects.requireNonNull(doAnswer(invocation -> {
                Thing updated = invocation.getArgument(0);
                if (pause.compareAndSet(true, false)) {
                    publishing.countDown();
                    assertTrue(release.await(10, TimeUnit.SECONDS));
                }
                assertNotNull(fixture.provider.update(updated));
                return null;
            }).when(fixture.callback)).thingUpdated(any(Thing.class));
            var obsolete = executor.submit(() -> fixture.change("https://server-a.example/old-base/"));
            assertTrue(publishing.await(10, TimeUnit.SECONDS));
            fixture.change("https://server-b.example/new-base/");
            assertEquals(ThingStatus.UNKNOWN, fixture.account.status);
            assertEquals(1, fixture.scans, "New discovery must wait for the old property callback to finish");
            release.countDown();
            obsolete.get(10, TimeUnit.SECONDS);
            Poll initial = fixture.scheduler.next(0);
            Objects.requireNonNull(verify(initial.future())).cancel(false);
            fixture.beforeScan = () -> assertEquals("caldav:account:scheduled|/new-base/work/", Objects
                    .requireNonNull(fixture.registry.get(calendar.getUID())).getProperties().get("calendarIdentity"));
            fixture.scheduler.next(0).action().run();
            assertEquals(2, fixture.scans);
            fixture.unchanged(calendar, "caldav:account:scheduled|/new-base/work/");
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    /** Checks that successful account work retains the configured polling interval. */
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

    /** Checks that failure backoff never shortens polling and successful communication restores the normal interval. */
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

    /** Checks that an explicit synchronization request replaces an idle backoff delay with immediate work. */
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

    /** Checks that requests arriving during a failed poll are coalesced without bypassing failure backoff. */
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

    /** Checks that concurrent synchronization requests produce one immediate follow-up after successful work. */
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

    /** Checks that account validation precedes HTTP client creation and corrected settings restore operation. */
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

    /** Checks that invalid raw integer values are rejected before conversion or transport allocation. */
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

    /** Checks that exact integer strings remain valid polling configuration. */
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

    /** Checks that validation and DTO conversion observe the same captured account configuration. */
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
            /**
             * Supplies a controlled configuration snapshot for validation race assertions.
             *
             * @return the configuration snapshot supplied to the test
             */
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

    /** Checks that unusable principal responses stop discovery early and permit a later successful scan. */
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

    /** Checks that one calendar's synchronization failure does not stop other calendars in the poll. */
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
        Bridge bridge = Objects.requireNonNull(spy(configuredBridge(Map.of("url", url, "syncMode", "FULL"))));
        Calendar failed = new Calendar("failed", "/failed/", bridge);
        Calendar healthy = new Calendar("healthy", "/healthy/", bridge);
        Thing failedThing = failed.getThing();
        Thing healthyThing = healthy.getThing();
        failedThing.setHandler(failed);
        healthyThing.setHandler(healthy);
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

    /** Checks that AUTO scans every advertised home set and deterministically deduplicates collections. */
    @Test
    void autoDiscoversBothHomesAndKeepsFirstDuplicateName() throws Exception {
        checkAutoDiscovery(false, false);
    }

    /** Checks that a failed home-set query prevents publication of an incomplete account-wide discovery result. */
    @Test
    void autoDoesNotPublishPartialDiscoveryWhenSecondHomeFails() throws Exception {
        checkAutoDiscovery(true, false);
    }

    /** Checks that normal calendar polling does not repeat completed collection discovery. */
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

    /** Checks that DIRECT scans the configured URL and disposed sessions cannot publish or reschedule work. */
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
            /**
             * Signals completion of HTTP client shutdown to the lifecycle test.
             *
             * @param lifecycle HTTP client lifecycle that has stopped
             */
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

    /** Checks that a discovery request arriving during synchronization is retained for the next poll. */
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
            /** Exposes HTTP startup to the test before completing the client lifecycle transition. */
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

    /** Local HTTP server and controlled account polling for failure and recovery scenarios. */
    private static final class RecoveryFixture implements AutoCloseable {
        /** Local HTTP server supplying controlled DAV responses. */
        final HttpServer server;
        /** HTTP response status returned by the local DAV server. */
        final AtomicInteger status = new AtomicInteger(401);
        /** Number of HTTP requests observed by this fixture. */
        final AtomicInteger requests = new AtomicInteger();
        /** Controllable scheduler used to inspect and execute pending account work. */
        final ControlledScheduler scheduler = new ControlledScheduler();
        /** Handler under test using the fixture dependencies. */
        final Handler handler;
        /** Number of successfully completed collection discovery callbacks. */
        final AtomicInteger callbacks = new AtomicInteger();
        /** Test hook executed when the local server receives a request. */
        volatile Runnable onRequest = () -> {
        };
        /** XML response returned by the local server. */
        volatile String responseBody = "<d:multistatus xmlns:d=\"DAV:\"/>";

        /** Starts a local DAV server and account worker for controlled failure and recovery tests. */
        RecoveryFixture() throws IOException {
            this(300, () -> true);
        }

        /**
         * Starts a local DAV server and account worker for controlled failure and recovery tests.
         *
         * @param interval configured polling interval in seconds
         * @param active check that the discovery request remains current
         */
        RecoveryFixture(int interval, BooleanSupplier active) throws IOException {
            this(interval, active, "BASIC");
        }

        /**
         * Starts a local DAV server and account worker for controlled failure and recovery tests.
         *
         * @param interval configured polling interval in seconds
         * @param active check that the discovery request remains current
         * @param authType authentication mode, or empty for anonymous access
         */
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

        /** Disposes fixture handlers and releases any local server resources. */
        @Override
        public void close() {
            handler.dispose();
            server.stop(0);
        }
    }

    /** Checks that repeated authentication failures cannot falsely recover an account to ONLINE. */
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

    /** Checks that initialization and request-free polling do not establish account connectivity. */
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

    /** Checks that failed discovery work remains queued for the next account retry. */
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

    /** Checks that successful remote communication clears the previous connection failure. */
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

    /** Checks that a poll without a server request cannot clear an OFFLINE account status. */
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

    /** Checks that request-free polling retains the accumulated connection failure backoff. */
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

    /** Checks that each authentication mode preserves bounded retries while credentials remain invalid. */
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

    /** Checks that discovery publication failures retain the work until the consumer completes successfully. */
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

    /** Checks that an obsolete queued scan performs no server access. */
    @Test
    void canceledDiscoveryDoesNotMakeAnyRequest() throws Exception {
        try (RecoveryFixture fixture = new RecoveryFixture(300, () -> false)) {
            fixture.scheduler.next(0).action().run();
            assertEquals(0, fixture.requests.get());
            assertEquals(0, fixture.callbacks.get());
            assertEquals(ThingStatus.UNKNOWN, fixture.handler.status);
        }
    }

    /** Checks that account reinitialization invalidates old retries and their pending consumers. */
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

    /** Checks that disposing an account prevents execution of its pending retry. */
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

    /** Checks that an obsolete request cannot publish success after account reinitialization. */
    @Test
    void reinitializationDuringRequestRejectsOldSuccessAndCallback() throws Exception {
        checkInFlightInvalidation(true);
    }

    /** Checks that an in-flight request cannot publish success after account disposal. */
    @Test
    void disposalDuringRequestRejectsOldSuccessAndCallback() throws Exception {
        checkInFlightInvalidation(false);
    }

    /** Checks that account disposal cancels discovery while its results are still being prepared. */
    @Test
    void accountDisposalDuringDiscoveryPreparationRejectsLateResults() throws Exception {
        checkDiscoveryPreparationInvalidation(true, false);
    }

    /** Checks that account disposal also cancels delayed results from an explicit Inbox scan. */
    @Test
    void accountDisposalDuringManualDiscoveryPreparationRejectsLateResults() throws Exception {
        checkDiscoveryPreparationInvalidation(false, false);
    }

    /** Checks that account reinitialization invalidates delayed results from an explicit Inbox scan. */
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
                /**
                 * Captures an Inbox result for publication and cancellation assertions.
                 *
                 * @param result discovered calendar result
                 */
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

    /** Checks that concurrent poll attempts cannot execute the same discovery work twice. */
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

    /** Checks that HTTP 401 is reported as a specific account authentication error. */
    @Test
    void http401ProducesAuthenticationDetail() throws Exception {
        checkHttpFailure(401, ThingStatusDetail.CONFIGURATION_ERROR, "@text/status.account.authentication");
    }

    /** Checks that HTTP 403 preserves the account access-denied diagnosis. */
    @Test
    void http403ProducesForbiddenDetail() throws Exception {
        checkHttpFailure(403, ThingStatusDetail.CONFIGURATION_ERROR, "@text/status.account.forbidden");
    }

    /** Checks that an absent account endpoint is reported as a configuration error. */
    @Test
    void http404ProducesEndpointConfigurationDetail() throws Exception {
        checkHttpFailure(404, ThingStatusDetail.CONFIGURATION_ERROR, "@text/status.account.not-found");
    }

    /** Checks that a rejected discovery request retains its structured HTTP status. */
    @Test
    void rejectedPropfindPreservesHttpStatus() throws Exception {
        checkHttpFailure(405, ThingStatusDetail.COMMUNICATION_ERROR, "@text/status.account.http-error [\"405\"]");
    }

    /** Checks that a server failure is reported as an account communication error. */
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

    /** Checks that connection failure receives a safe communication diagnosis. */
    @Test
    void connectionFailureProducesCommunicationError() {
        checkTransportFailure(new ConnectException("private-url password"), "@text/status.connection");
    }

    /** Checks that request timeouts receive a specific account status description. */
    @Test
    void timeoutProducesTimeoutDetail() {
        checkTransportFailure(new TimeoutException("private-url password"), "@text/status.timeout");
        checkTransportFailure(new SocketTimeoutException("private-url password"), "@text/status.timeout");
    }

    /** Checks that DNS failures receive a specific account status description. */
    @Test
    void dnsFailureProducesNameResolutionDetail() {
        checkTransportFailure(new UnknownHostException("private-host password"), "@text/status.dns");
    }

    /** Checks that TLS failures receive a specific secure-connection diagnosis. */
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

    /** Checks that observing remote connectivity does not replace the transport or lose incremental calendar state. */
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
        String url = "http://127.0.0.1:" + server.getAddress().getPort() + "/";
        Bridge bridge = Objects.requireNonNull(spy(configuredBridge(Map.of("url", url, "syncMode", "ETAG"))));
        Calendar calendar = new Calendar("incremental", "/calendar/", bridge);
        Thing child = calendar.getThing();
        child.setHandler(calendar);
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
