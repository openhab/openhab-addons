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
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.UnknownHostException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLHandshakeException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.client.CalDavXml;
import org.openhab.binding.caldav.internal.client.DavTransport;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.BridgeBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

import com.google.gson.JsonParser;

/**
 * Observable publication, cache recovery and disposal regression tests.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Initial synchronization status tests
 * @author Andreas Vilippus - Lifecycle, cache and publication regression coverage
 * @author Andreas Vilippus - Versioned compact snapshots and canonical cache identity tests
 * @author Andreas Vilippus - Structured failure and retained data regression tests
 * @author Andreas Vilippus - Calendar color channel and metadata lifecycle tests
 * @author Andreas Vilippus - Localized Thing status and plain Item error tests
 * @author Andreas Vilippus - Horizon rollover persistence and restart tests
 * @author Andreas Vilippus - Calendar representation property lifecycle tests
 * @author Andreas Vilippus - Account context and identity publication race regressions
 */
@NonNullByDefault
@Timeout(15)
class CalendarHandlerTest {
    private static final ThingUID DEFAULT_BRIDGE = new ThingUID("caldav:account:test");

    /** In-memory calendar cache used to exercise persistence without a running openHAB instance. */
    private static final class MemoryStorage implements Storage<String> {
        private final Map<String, @Nullable String> values = new HashMap<>();

        /**
         * Updates the in-memory storage entry used by persistence tests.
         *
         * @param key storage entry key
         * @param value replacement stored value, or null
         * @return the value previously stored under the key, if any
         */
        @Override
        public @Nullable String put(String key, @Nullable String value) {
            return values.put(key, value);
        }

        /**
         * Reads a persisted fixture value without a running storage service.
         *
         * @param key storage entry key
         * @return the stored value, if present
         */
        @Override
        public @Nullable String get(String key) {
            return values.get(key);
        }

        /**
         * Removes a stored fixture entry.
         *
         * @param key storage entry key
         * @return the removed value, if present
         */
        @Override
        public @Nullable String remove(String key) {
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
        public Collection<@Nullable String> getValues() {
            return values.values();
        }
    }

    /** Handler fixture that captures channel or Thing status publications for assertions. */
    private static final class Handler extends CalendarHandler {
        /** Latest channel states captured from the calendar handler. */
        final Map<String, State> published = new java.util.concurrent.ConcurrentHashMap<>();
        /** Signals that usable event JSON has been published. */
        final CountDownLatch restored = new CountDownLatch(1);
        /** Calendar representations present at each captured ONLINE publication. */
        final List<String> onlineIdentities = new CopyOnWriteArrayList<>();
        private @Nullable Bridge bridge;
        /** Most recently captured Thing status. */
        volatile ThingStatus status = ThingStatus.UNKNOWN;
        /** Most recently captured Thing status detail. */
        volatile ThingStatusDetail detail = ThingStatusDetail.NONE;
        /** Most recently captured Thing status description. */
        volatile @Nullable String description;

        /** Creates a handler fixture that records state and status publications. */
        Handler() {
            this(new MemoryStorage(), null);
        }

        /**
         * Creates a handler fixture that records state and status publications.
         *
         * @param storage calendar cache storage
         * @param bridge associated account bridge
         */
        Handler(Storage<String> storage, @Nullable Bridge bridge) {
            this(thing(), storage, bridge, () -> ZoneOffset.UTC, Clock.systemUTC(), null);
        }

        /**
         * Creates a handler fixture that records state and status publications.
         *
         * @param thing calendar or account Thing to handle
         * @param storage calendar cache storage
         * @param bridge associated account bridge
         * @param zone time zone used for event projection
         * @param clock clock used for event and range calculations
         * @param scheduler scheduler used by the fixture, or null to use the handler scheduler
         */
        Handler(Thing thing, Storage<String> storage, @Nullable Bridge bridge, TimeZoneProvider zone, Clock clock,
                @Nullable ScheduledExecutorService scheduler) {
            super(thing, zone, storage, clock, scheduler);
            this.bridge = bridge == null ? identityBridge("test") : bridge;
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
            published.put(channel.getId(), state);
            if ("events#json".equals(channel.getId()) && state != UnDefType.UNDEF) {
                restored.countDown();
            }
        }

        /**
         * Captures valid binding statuses and the representation present when ONLINE is published.
         *
         * @param status new Thing status
         * @param detail status detail
         * @param description status description, if present
         */
        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            switch (status) {
                case UNKNOWN, ONLINE, OFFLINE, REMOVED -> {
                    this.status = status;
                    this.detail = detail;
                    this.description = description;
                    if (status == ThingStatus.ONLINE) {
                        onlineIdentities.add(getThing().getProperties().getOrDefault("calendarIdentity", "<missing>"));
                    }
                }
                default -> throw new IllegalArgumentException("Illegal binding status: " + status);
            }
        }
    }

    /** Controllable UTC clock for deterministic calendar range and event transition tests. */
    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-09-18T09:00:00Z");

        /**
         * Supplies UTC for deterministic local time calculations.
         *
         * @return UTC for this controllable test clock
         */
        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        /**
         * Creates a fixed clock in the requested zone without advancing time.
         *
         * @param zone time zone used for event projection
         * @return a fixed clock at the current instant in the requested zone
         */
        @Override
        public Clock withZone(@Nullable ZoneId zone) {
            return Clock.fixed(now, Objects.requireNonNull(zone));
        }

        /**
         * Supplies the current controllable test instant.
         *
         * @return the current test instant
         */
        @Override
        public Instant instant() {
            return now;
        }

        /**
         * Moves the fixture clock forward without waiting for wall-clock time.
         *
         * @param seconds number of seconds to advance the test clock
         */
        void advance(long seconds) {
            now = now.plusSeconds(seconds);
        }
    }

    /** A captured local calendar update with its delay and cancellation handle. */
    private record ClockJob(Runnable task, long delay, ScheduledFuture<?> future) {
    }

    /** Calendar setup with controllable time, persistent cache and captured scheduled jobs. */
    private static final class Fixture implements AutoCloseable {
        /** Controllable clock used for local calendar updates. */
        final MutableClock clock = new MutableClock();
        /** Time zone supplied to calendar range calculations. */
        final AtomicReference<ZoneId> zone = new AtomicReference<>(ZoneOffset.UTC);
        /** Captured local update jobs and their requested delays. */
        final List<ClockJob> jobs = new ArrayList<>();
        /** Persistent calendar cache shared across fixture restarts. */
        final MemoryStorage storage;
        /** Handler under test using the fixture dependencies. */
        final Handler handler;
        /** Core handler callback used to observe or persist Thing updates. */
        final ThingHandlerCallback callback = Objects.requireNonNull(mock(ThingHandlerCallback.class));

        /**
         * Initializes a calendar with controllable time, cache storage and captured update jobs.
         *
         * @param configuration captured configuration used by the handler
         */
        Fixture(Map<String, Object> configuration) {
            this(configuration, new MemoryStorage(), null);
        }

        /**
         * Initializes a calendar with controllable time, cache storage and captured update jobs.
         *
         * @param configuration captured configuration used by the handler
         * @param storage calendar cache storage
         * @param bridge associated account bridge
         */
        Fixture(Map<String, Object> configuration, MemoryStorage storage, @Nullable Bridge bridge) {
            this(configuration, storage, bridge, "Calendar", Map.of());
        }

        /**
         * Initializes a calendar with controllable time, cache storage and captured update jobs.
         *
         * @param configuration captured configuration used by the handler
         * @param storage calendar cache storage
         * @param bridge associated account bridge
         * @param label calendar label
         * @param properties calendar metadata supplied to the fixture
         */
        Fixture(Map<String, Object> configuration, MemoryStorage storage, @Nullable Bridge bridge, String label,
                Map<String, String> properties) {
            this(configuration, storage, bridge, label, properties, Instant.parse("2026-09-18T09:00:00Z"));
        }

        /**
         * Initializes a calendar with controllable time, cache storage and captured update jobs.
         *
         * @param configuration captured configuration used by the handler
         * @param storage calendar cache storage
         * @param bridge associated account bridge
         * @param now initial instant of the controllable clock
         */
        Fixture(Map<String, Object> configuration, MemoryStorage storage, @Nullable Bridge bridge, Instant now) {
            this(configuration, storage, bridge, "Calendar", Map.of(), now);
        }

        /**
         * Initializes a calendar with controllable time, cache storage and captured update jobs.
         *
         * @param configuration captured configuration used by the handler
         * @param storage calendar cache storage
         * @param bridge associated account bridge
         * @param label calendar label
         * @param properties calendar metadata supplied to the fixture
         * @param now initial instant of the controllable clock
         */
        Fixture(Map<String, Object> configuration, MemoryStorage storage, @Nullable Bridge bridge, String label,
                Map<String, String> properties, Instant now) {
            clock.now = now;
            this.storage = storage;
            ScheduledExecutorService scheduler = mock(ScheduledExecutorService.class);
            doAnswer(invocation -> {
                ScheduledFuture<?> future = mock(ScheduledFuture.class);
                jobs.add(new ClockJob(invocation.getArgument(0), invocation.getArgument(1), future));
                return future;
            }).when(scheduler).schedule(any(Runnable.class), anyLong(), eq(TimeUnit.MILLISECONDS));
            doAnswer(invocation -> {
                invocation.<Runnable> getArgument(0).run();
                return null;
            }).when(scheduler).execute(any(Runnable.class));
            Map<String, Object> values = new HashMap<>(
                    Map.of("path", "https://example.org/calendar/", "rangeStartOffset", -1, "rangeEndOffset", 1));
            values.putAll(configuration);
            Thing thing = ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), "test")
                    .withBridge(bridgeUID(bridge)).withConfiguration(new Configuration(values)).withLabel(label)
                    .withProperties(properties).build();
            handler = new Handler(thing, storage, bridge, () -> Objects.requireNonNull(zone.get()), clock, scheduler);
            handler.setCallback(callback);
            handler.initialize();
        }

        /** Disposes fixture handlers and releases any local server resources. */
        @Override
        public void close() {
            handler.dispose();
        }
    }

    private static Bridge identityBridge(String id) {
        return BridgeBuilder.create(new ThingTypeUID("caldav", "account"), id).withConfiguration(
                new Configuration(Map.of("url", "https://example.org/dav/", "username", "user", "password", "secret")))
                .build();
    }

    private static ThingUID bridgeUID(@Nullable Bridge bridge) {
        ThingUID uid = bridge == null ? null : bridge.getUID();
        return uid == null ? DEFAULT_BRIDGE : uid;
    }

    private static Thing reconfigured(Handler handler, @Nullable Bridge bridge, Map<String, Object> changes) {
        Thing current = handler.getThing();
        Configuration values = new Configuration(current.getConfiguration());
        changes.forEach(values::put);
        return ThingBuilder.create(current.getThingTypeUID(), current.getUID())
                .withBridge(bridge == null ? null : bridge.getUID()).withConfiguration(values)
                .withLabel(current.getLabel()).withProperties(current.getProperties()).build();
    }

    /**
     * Checks that live synchronization cannot publish ONLINE before the current calendar representation is established.
     */
    @Test
    @Timeout(30)
    void initializationCannotPublishOnlineBeforeCurrentIdentity() throws Exception {
        Bridge bridge = identityBridge("account-a");
        AccountHandler accountHandler = Objects.requireNonNull(mock(AccountHandler.class));
        AccountConfiguration account = account();
        bridge.setHandler(accountHandler);
        CountDownLatch calculating = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicBoolean pause = new AtomicBoolean();
        when(accountHandler.configuration()).thenAnswer(invocation -> {
            if (pause.compareAndSet(true, false)) {
                calculating.countDown();
                assertTrue(release.await(10, TimeUnit.SECONDS));
            }
            return account;
        });
        var executor = Executors.newFixedThreadPool(2);
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            pause.set(true);
            var initializing = executor.submit(() -> fixture.handler
                    .thingUpdated(reconfigured(fixture.handler, bridge, Map.of("path", "/calendar/family/"))));
            assertTrue(calculating.await(10, TimeUnit.SECONDS));
            AtomicInteger requests = new AtomicInteger();
            DavTransport transport = (method, uri, body, depth) -> {
                requests.incrementAndGet();
                return response(true);
            };
            executor.submit(() -> {
                fixture.handler.synchronize(transport, account, () -> true);
                return null;
            }).get(10, TimeUnit.SECONDS);
            assertTrue(fixture.handler.onlineIdentities.isEmpty(), "ONLINE must follow current identity publication");
            assertEquals(0, requests.get());
            release.countDown();
            initializing.get(10, TimeUnit.SECONDS);
            assertEquals("caldav:account:account-a|/calendar/family/",
                    fixture.handler.getThing().getProperties().get("calendarIdentity"));
            fixture.handler.synchronize(transport, account, () -> true);
            assertEquals(List.of("caldav:account:account-a|/calendar/family/"), fixture.handler.onlineIdentities);
            assertEquals(1, requests.get());
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    /** Checks that delayed initialization cannot overwrite a newer calendar representation. */
    @Test
    @Timeout(30)
    void obsoleteInitializationCannotReplaceNewerGenerationIdentity() throws Exception {
        Bridge bridge = identityBridge("account-a");
        AccountHandler accountHandler = Objects.requireNonNull(mock(AccountHandler.class));
        bridge.setHandler(accountHandler);
        CountDownLatch calculating = new CountDownLatch(1), release = new CountDownLatch(1);
        AtomicBoolean pause = new AtomicBoolean();
        when(accountHandler.configuration()).thenAnswer(invocation -> {
            if (pause.compareAndSet(true, false)) {
                calculating.countDown();
                assertTrue(release.await(10, TimeUnit.SECONDS));
            }
            return account();
        });
        var executor = Executors.newSingleThreadExecutor();
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            pause.set(true);
            var obsolete = executor.submit(() -> fixture.handler
                    .thingUpdated(reconfigured(fixture.handler, bridge, Map.of("path", "/calendar/family/"))));
            assertTrue(calculating.await(10, TimeUnit.SECONDS));
            fixture.handler.thingUpdated(reconfigured(fixture.handler, bridge, Map.of("path", "/calendar/newest/")));
            release.countDown();
            obsolete.get(10, TimeUnit.SECONDS);
            assertEquals("caldav:account:account-a|/calendar/newest/",
                    fixture.handler.getThing().getProperties().get("calendarIdentity"));
            fixture.handler.synchronize((method, uri, body, depth) -> response(true), account(), () -> true);
            assertEquals(List.of("caldav:account:account-a|/calendar/newest/"), fixture.handler.onlineIdentities);
        } finally {
            release.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    /** Checks that an account origin change invalidates an old absolute path without reinitializing the calendar. */
    @Test
    void currentAccountRejectsOldAbsolutePathWithoutCalendarReinitialization() throws Exception {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "https://example.org/calendar/work/"), new MemoryStorage(),
                bridge, "Calendar",
                Map.of("calendarColor", "#123456", "calendarDescription", "Work", "calendarPrivileges", "read"))) {
            fixture.handler.synchronize((method, uri, body, depth) -> response(true), account(), () -> true);
            AccountConfiguration changed = account();
            changed.url = "https://server-b.example/dav/";
            bridge.getConfiguration().put("url", changed.url);
            fixture.handler.synchronize((method, uri, body, depth) -> {
                fail("Invalid cross-origin path must not reach the server");
                return "";
            }, changed, () -> true);
            assertFalse(fixture.handler.getThing().getProperties().containsKey("calendarIdentity"));
            assertEquals(ThingStatus.OFFLINE, fixture.handler.status);
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, fixture.handler.detail);
            assertEquals(List.of("caldav:account:account-a|/calendar/work/"), fixture.handler.onlineIdentities);
            assertEquals("#123456", fixture.handler.getThing().getProperties().get("calendarColor"));
            assertEquals("Work", fixture.handler.getThing().getProperties().get("calendarDescription"));
            assertEquals("read", fixture.handler.getThing().getProperties().get("calendarPrivileges"));
        }
    }

    /** Checks that stable origin-relative paths avoid redundant representation writes after account changes. */
    @Test
    void rootRelativePathSurvivesAccountChangeWithoutAnotherPropertyWrite() throws Exception {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            List<URI> requests = new ArrayList<>();
            DavTransport transport = (method, uri, body, depth) -> {
                requests.add(uri);
                return response(true);
            };
            fixture.handler.synchronize(transport, account(), () -> true);
            AccountConfiguration changed = account();
            changed.url = "https://server-b.example/dav/";
            bridge.getConfiguration().put("url", changed.url);
            fixture.handler.synchronize(transport, changed, () -> true);
            assertEquals(List.of(URI.create("https://example.org/calendar/work/"),
                    URI.create("https://server-b.example/calendar/work/")), requests);
            assertEquals(
                    List.of("caldav:account:account-a|/calendar/work/", "caldav:account:account-a|/calendar/work/"),
                    fixture.handler.onlineIdentities);
            Objects.requireNonNull(verify(fixture.callback, times(1))).thingUpdated(any(Thing.class));
        }
    }

    /** Checks that invalid account settings remove calendar representation without a calendar lifecycle restart. */
    @Test
    void invalidCurrentAccountRemovesIdentityWithoutCalendarReinitialization() throws Exception {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            AccountConfiguration invalid = account();
            invalid.url = "ftp://example.org/";
            fixture.handler.synchronize((method, uri, body, depth) -> {
                fail("Invalid account must not reach the server");
                return "";
            }, invalid, () -> true);
            assertFalse(fixture.handler.getThing().getProperties().containsKey("calendarIdentity"));
            assertEquals(ThingStatus.OFFLINE, fixture.handler.status);
            assertTrue(fixture.handler.onlineIdentities.isEmpty());
        }
    }

    /** Checks that relative calendar paths are re-resolved before successful synchronization publishes ONLINE. */
    @Test
    void changedAccountBaseDirectoryUpdatesRelativeIdentityBeforeOnline() throws Exception {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "work/"), new MemoryStorage(), bridge)) {
            AccountConfiguration changed = account();
            changed.url = "https://example.org/new-base/";
            bridge.getConfiguration().put("url", changed.url);
            fixture.handler.synchronize((method, uri, body, depth) -> response(true), changed, () -> true);
            assertEquals(List.of("caldav:account:account-a|/new-base/work/"), fixture.handler.onlineIdentities);
            Objects.requireNonNull(verify(fixture.callback, times(2))).thingUpdated(any(Thing.class));
        }
    }

    /** Checks that reentrant disposal during property publication cannot reactivate calendar initialization. */
    @Test
    void disposalFromPropertyCallbackCannotReactivateInitialization() throws Exception {
        Bridge bridge = identityBridge("account-a");
        AccountHandler accountHandler = Objects.requireNonNull(mock(AccountHandler.class));
        when(accountHandler.configuration()).thenReturn(account());
        bridge.setHandler(accountHandler);
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            Objects.requireNonNull(doAnswer(invocation -> {
                fixture.handler.dispose();
                return null;
            }).when(fixture.callback)).thingUpdated(any(Thing.class));
            fixture.handler.thingUpdated(reconfigured(fixture.handler, bridge, Map.of("path", "/calendar/family/")));
            fixture.handler.synchronize((method, uri, body, depth) -> {
                fail("Disposed generation must stay inactive");
                return "";
            }, account(), () -> true);
            assertTrue(fixture.handler.onlineIdentities.isEmpty());
            verify(accountHandler, times(1)).requestSync();
        }
    }

    /** Checks that a disposed calendar cannot publish representation data during account revalidation. */
    @Test
    void disposedHandlerCannotPublishAccountContextMetadata() {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            fixture.handler.dispose();
            AccountConfiguration changed = account();
            changed.url = "https://other.example/";
            assertFalse(fixture.handler.accountConfigurationChanged(bridge.getUID(), changed, () -> true));
            Objects.requireNonNull(verify(fixture.callback, times(1))).thingUpdated(any(Thing.class));
            assertTrue(fixture.handler.onlineIdentities.isEmpty());
        }
    }

    /**
     * Checks that valid configuration supplies representation metadata without a running account handler or network
     * access.
     */
    @Test
    void identityIsAvailableWithoutAnAccountHandlerOrLiveSynchronization() {
        Bridge bridge = identityBridge("account-a");
        assertNull(bridge.getHandler());
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            assertEquals("caldav:account:account-a|/calendar/work/",
                    fixture.handler.getThing().getProperties().get("calendarIdentity"));
            assertFalse(fixture.handler.getThing().getProperties().containsKey("calendarUid"));
            assertFalse(fixture.handler.getThing().getConfiguration().containsKey("calendarIdentity"));
            assertEquals(ThingStatus.UNKNOWN, fixture.handler.status);
            assertTrue(fixture.storage.getKeys().isEmpty());
            assertTrue(fixture.jobs.isEmpty());
            Objects.requireNonNull(verify(fixture.callback)).thingUpdated(fixture.handler.getThing());
        }
    }

    /** Checks that successful publication of an unchanged representation is not repeated. */
    @Test
    void unchangedIdentityDoesNotCauseRepeatedPropertyWrites() {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            fixture.handler.initialize();
            fixture.handler.initialize();
            assertEquals("caldav:account:account-a|/calendar/work/",
                    fixture.handler.getThing().getProperties().get("calendarIdentity"));
            Objects.requireNonNull(verify(fixture.callback, times(1))).thingUpdated(fixture.handler.getThing());
        }
    }

    /** Checks that a changed collection path replaces the calendar representation. */
    @Test
    void pathReconfigurationReplacesTheRepresentationProperty() {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            assertEquals("caldav:account:account-a|/calendar/work/",
                    fixture.handler.getThing().getProperties().get("calendarIdentity"));
            fixture.handler.thingUpdated(reconfigured(fixture.handler, bridge, Map.of("path", "/calendar/family/")));
            assertEquals("caldav:account:account-a|/calendar/family/",
                    fixture.handler.getThing().getProperties().get("calendarIdentity"));
        }
    }

    /** Checks that reassociating a calendar changes the owning bridge in its representation. */
    @Test
    void bridgeReassociationChangesTheRepresentationScope() {
        Bridge first = identityBridge("account-a");
        Bridge second = identityBridge("account-b");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), first)) {
            fixture.handler.bridge = second;
            fixture.handler.thingUpdated(reconfigured(fixture.handler, second, Map.of()));
            assertEquals("caldav:account:account-b|/calendar/work/",
                    fixture.handler.getThing().getProperties().get("calendarIdentity"));
        }
    }

    /** Checks that invalid calendar settings remove a previously valid representation. */
    @Test
    void invalidCalendarReconfigurationRemovesTheStaleIdentity() {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            for (Map<String, Object> invalid : List.<Map<String, Object>> of(Map.of("path", ""),
                    Map.of("path", "https://foreign.example/calendar/work/"),
                    Map.of("path", "https://user:secret@example.org/calendar/work/"),
                    Map.of("path", "/calendar/work/#fragment"), Map.of("maxEvents", new BigDecimal("1.5")),
                    Map.of("maxEvents", 0), Map.of("rangeStartOffset", 2, "rangeEndOffset", 1))) {
                assertEquals("caldav:account:account-a|/calendar/work/",
                        fixture.handler.getThing().getProperties().get("calendarIdentity"));
                fixture.handler.thingUpdated(reconfigured(fixture.handler, bridge, invalid));
                assertFalse(fixture.handler.getThing().getProperties().containsKey("calendarIdentity"),
                        invalid.toString());
                fixture.handler.thingUpdated(reconfigured(fixture.handler, bridge, Map.of("path", "/calendar/work/",
                        "maxEvents", 500, "rangeStartOffset", -1, "rangeEndOffset", 1)));
            }
        }
    }

    /** Checks that account endpoint changes preserve an unchanged logical collection reference within the bridge. */
    @Test
    void accountUrlChangeKeepsTheSameLogicalBridgeReference() {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            bridge.getConfiguration().put("url", "https://different.example:8443/other-account/");
            fixture.handler.initialize();
            assertEquals("caldav:account:account-a|/calendar/work/",
                    fixture.handler.getThing().getProperties().get("calendarIdentity"));
            Objects.requireNonNull(verify(fixture.callback, times(1))).thingUpdated(fixture.handler.getThing());
        }
    }

    /** Checks that credentials and unrelated account settings do not enter the calendar representation. */
    @Test
    void credentialsAndOtherAccountSettingsDoNotChangeIdentity() {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            bridge.getConfiguration().put("username", "other-user");
            bridge.getConfiguration().put("password", "other-password");
            bridge.getConfiguration().put("authType", "DIGEST");
            bridge.getConfiguration().put("discoveryMode", "DIRECT");
            fixture.handler.initialize();
            assertEquals("caldav:account:account-a|/calendar/work/",
                    fixture.handler.getThing().getProperties().get("calendarIdentity"));
            Objects.requireNonNull(verify(fixture.callback, times(1))).thingUpdated(fixture.handler.getThing());
        }
    }

    /** Checks that calendar labels and optional server metadata do not affect representation identity. */
    @Test
    void calendarMetadataAndLabelDoNotChangeIdentity() {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            Thing current = fixture.handler.getThing();
            Map<String, String> properties = new HashMap<>(current.getProperties());
            properties.putAll(Map.of("calendarDescription", "Changed", "calendarColor", "#123456", "calendarPrivileges",
                    "read,write"));
            fixture.handler.thingUpdated(ThingBuilder.create(current.getThingTypeUID(), current.getUID())
                    .withBridge(bridge.getUID()).withConfiguration(current.getConfiguration()).withLabel("Renamed")
                    .withProperties(properties).build());
            assertEquals("caldav:account:account-a|/calendar/work/",
                    fixture.handler.getThing().getProperties().get("calendarIdentity"));
            Objects.requireNonNull(verify(fixture.callback, times(1))).thingUpdated(current);
        }
    }

    /** Checks that a calendar without an associated bridge loses its stale representation. */
    @Test
    void removingTheBridgeRemovesTheStaleRepresentationProperty() {
        Bridge bridge = identityBridge("account-a");
        try (Fixture fixture = new Fixture(Map.of("path", "/calendar/work/"), new MemoryStorage(), bridge)) {
            fixture.handler.bridge = null;
            fixture.handler.thingUpdated(reconfigured(fixture.handler, null, Map.of()));
            assertFalse(fixture.handler.getThing().getProperties().containsKey("calendarIdentity"));
        }
    }

    private static String calendar(String uid, String properties) {
        return "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\nUID:" + uid + "\n" + properties
                + "END:VEVENT\nEND:VCALENDAR";
    }

    private static String multistatus(String... resources) {
        StringBuilder result = new StringBuilder(
                "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">");
        for (int i = 0; i < resources.length; i++) {
            result.append("<d:response><d:href>/calendar/").append(i)
                    .append(".ics</d:href><d:propstat><d:prop><c:calendar-data>")
                    .append(resources[i].replace("&", "&amp;").replace("<", "&lt;"))
                    .append("</c:calendar-data></d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>");
        }
        return result.append("</d:multistatus>").toString();
    }

    private static String requestedToken(String body) {
        return assertDoesNotThrow(
                () -> CalDavXml.parse(body).getElementsByTagNameNS("DAV:", "sync-token").item(0).getTextContent());
    }

    private static String syncResponse(String token, String members) {
        return "<d:multistatus xmlns:d=\"DAV:\">" + members + "<d:sync-token>" + token
                + "</d:sync-token></d:multistatus>";
    }

    private static String syncMember(String name, String etag) {
        return "<d:response><d:href>/calendar/" + name + ".ics</d:href><d:propstat><d:prop><d:getetag>" + etag
                + "</d:getetag></d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>";
    }

    private static Thing thing() {
        return ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), "test").withBridge(DEFAULT_BRIDGE)
                .withConfiguration(new Configuration(
                        Map.of("path", "https://example.org/calendar/", "rangeStartOffset", -1, "rangeEndOffset", 1)))
                .build();
    }

    private AccountConfiguration account() {
        AccountConfiguration c = new AccountConfiguration();
        c.url = "https://example.org/";
        c.username = "user";
        c.password = "secret";
        c.syncMode = "FULL";
        return c;
    }

    private String response(boolean empty) {
        ZonedDateTime start = ZonedDateTime.now(ZoneOffset.UTC).minusHours(1), end = start.plusHours(2);
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        String data = "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\nUID:one\nDTSTART:" + format.format(start)
                + "\nDTEND:" + format.format(end) + "\nSUMMARY:Running\nEND:VEVENT\nEND:VCALENDAR";
        return "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">" + (empty ? ""
                : "<d:response><d:href>/calendar/one.ics</d:href><d:propstat><d:prop><c:calendar-data>" + data
                        + "</c:calendar-data></d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>")
                + "</d:multistatus>";
    }

    /** Checks that a valid calendar starts in a waiting state instead of an artificial communication error. */
    @Test
    void initializationWaitsForSynchronizationWithoutError() {
        Handler h = new Handler();
        try {
            h.initialize();
            assertEquals(ThingStatus.UNKNOWN, h.status);
            assertEquals(ThingStatusDetail.NONE, h.detail);
            assertEquals("@text/status.calendar.waiting", h.description);
            assertEquals("SYNCING", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals("", Objects.requireNonNull(h.published.get("sync#error")).toString());
        } finally {
            h.dispose();
        }
    }

    /** Checks that invalid integer configuration is rejected before conversion or scheduled calendar work. */
    @Test
    void invalidRawIntegerValuesAreRejectedBeforeSchedulingOrDtoConversion() throws Exception {
        AccountHandler accountHandler = mock(AccountHandler.class);
        when(accountHandler.configuration()).thenReturn(account());
        Bridge bridge = mock(Bridge.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        for (String name : List.of("rangeStartOffset", "rangeEndOffset", "maxEvents")) {
            for (Object value : List.of(new BigDecimal("1.5"), new BigDecimal("2147483648"),
                    new BigDecimal("-2147483649"), "1.5")) {
                try (Fixture fixture = new Fixture(Map.of(name, value), new MemoryStorage(), bridge)) {
                    Handler handler = fixture.handler;
                    assertEquals(ThingStatus.OFFLINE, handler.status, name + ": " + value);
                    assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, handler.detail);
                    assertEquals("@text/status.calendar.configuration", handler.description);
                    assertEquals("Invalid calendar configuration; check the collection URL and range settings",
                            Objects.requireNonNull(handler.published.get("sync#error")).toString());
                    assertEquals("ERROR", Objects.requireNonNull(handler.published.get("sync#status")).toString());
                    assertEquals(UnDefType.UNDEF, handler.published.get("events#json"));
                    assertTrue(fixture.jobs.isEmpty());
                    AtomicInteger requests = new AtomicInteger();
                    handler.synchronize((method, uri, body, depth) -> {
                        requests.incrementAndGet();
                        return multistatus();
                    }, account(), () -> true);
                    assertEquals(0, requests.get());
                    assertTrue(fixture.storage.getKeys().isEmpty());
                }
            }
        }
        verify(accountHandler, never()).configuration();
        verify(accountHandler, never()).requestSync();
    }

    /** Checks that exact integer strings remain accepted calendar configuration values. */
    @Test
    void exactIntegerStringsRemainValidForCalendarConfiguration() throws Exception {
        try (Fixture fixture = new Fixture(Map.of("rangeStartOffset", "-1", "rangeEndOffset", "1", "maxEvents", "1"))) {
            fixture.handler.synchronize(
                    (method, uri, body,
                            depth) -> multistatus(calendar("one", "DTSTART:20260918T080000Z\nDURATION:PT2H\n")),
                    account(), () -> true);
            assertEquals(ThingStatus.ONLINE, fixture.handler.status);
            assertEquals("OK", Objects.requireNonNull(fixture.handler.published.get("sync#status")).toString());
            assertEquals("1", Objects.requireNonNull(fixture.handler.published.get("events#count")).toString());
        }
    }

    /** Checks that invalid reconfiguration clears old event data and corrected settings permit recovery. */
    @Test
    void invalidIntegerReinitializationClearsDataAndAllowsCorrection() throws Exception {
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler handler = fixture.handler;
            DavTransport transport = (method, uri, body,
                    depth) -> multistatus(calendar("one", "DTSTART:20260918T080000Z\nDURATION:PT2H\n"));
            handler.synchronize(transport, account(), () -> true);
            ClockJob previous = fixture.jobs.getLast();
            int jobs = fixture.jobs.size();
            handler.getThing().getConfiguration().put("maxEvents", new BigDecimal("1.5"));
            handler.thingUpdated(handler.getThing());
            assertEquals(ThingStatus.OFFLINE, handler.status);
            assertEquals(ThingStatusDetail.CONFIGURATION_ERROR, handler.detail);
            assertEquals(UnDefType.UNDEF, handler.published.get("events#json"));
            verify(previous.future()).cancel(true);
            Map<String, State> states = Map.copyOf(handler.published);
            handler.synchronize(transport, account(), () -> true);
            previous.task().run();
            assertEquals(states, handler.published);
            assertEquals(jobs, fixture.jobs.size());
            handler.getThing().getConfiguration().put("maxEvents", "1");
            handler.thingUpdated(handler.getThing());
            handler.synchronize(transport, account(), () -> true);
            assertEquals(ThingStatus.ONLINE, handler.status);
            assertEquals("1", Objects.requireNonNull(handler.published.get("events#count")).toString());
        }
    }

    /** Checks that the first live fetch keeps the calendar UNKNOWN until successful completion. */
    @Test
    @Timeout(30)
    void initialFetchRemainsUnknownUntilSuccess() throws Exception {
        checkStatusDuringFetch(false);
    }

    /** Checks that later synchronization does not take an already confirmed calendar offline while fetching. */
    @Test
    @Timeout(30)
    void subsequentFetchRemainsOnline() throws Exception {
        checkStatusDuringFetch(true);
    }

    private void checkStatusDuringFetch(boolean synchronizedBefore) throws Exception {
        Handler h = new Handler();
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        var worker = Executors.newSingleThreadExecutor();
        try {
            h.initialize();
            if (synchronizedBefore) {
                h.synchronize((m, u, b, d) -> response(false), account(), () -> true);
                assertEquals(ThingStatus.ONLINE, h.status);
            }
            DavTransport transport = (m, u, b, d) -> {
                entered.countDown();
                if (!release.await(10, TimeUnit.SECONDS)) {
                    throw new IOException("timeout");
                }
                return response(false);
            };
            var work = worker.submit(() -> {
                h.synchronize(transport, account(), () -> true);
                return null;
            });
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            assertEquals(synchronizedBefore ? ThingStatus.ONLINE : ThingStatus.UNKNOWN, h.status);
            assertEquals(ThingStatusDetail.NONE, h.detail);
            if (!synchronizedBefore) {
                assertEquals("@text/status.calendar.fetching", h.description);
            }
            assertEquals("SYNCING", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals("", Objects.requireNonNull(h.published.get("sync#error")).toString());
            release.countDown();
            work.get(10, TimeUnit.SECONDS);
            assertEquals(ThingStatus.ONLINE, h.status);
            assertEquals(ThingStatusDetail.NONE, h.detail);
            assertEquals("OK", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals("", Objects.requireNonNull(h.published.get("sync#error")).toString());
            assertNotEquals(UnDefType.UNDEF, h.published.get("sync#last"));
        } finally {
            h.dispose();
            release.countDown();
            worker.shutdownNow();
            assertTrue(worker.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    /** Checks that a failed initial fetch preserves the specific synchronization diagnosis. */
    @Test
    void firstFetchFailureKeepsExistingErrorDetails() throws Exception {
        checkFirstFailure((m, u, b, d) -> {
            throw new IOException("offline");
        }, ThingStatusDetail.COMMUNICATION_ERROR);
        checkFirstFailure((m, u, b, d) -> {
            throw new IllegalArgumentException("invalid calendar data");
        }, ThingStatusDetail.CONFIGURATION_ERROR);
        for (int status : new int[] { 401, 403, 404, 503 }) {
            checkFirstFailure((m, u, b, d) -> {
                throw new CalDavHttpException(m, status);
            }, status == 503 ? ThingStatusDetail.COMMUNICATION_ERROR : ThingStatusDetail.CONFIGURATION_ERROR);
        }
    }

    private void checkFirstFailure(DavTransport transport, ThingStatusDetail expected) throws Exception {
        Handler h = new Handler();
        try {
            h.initialize();
            h.synchronize(transport, account(), () -> true);
            assertEquals(ThingStatus.OFFLINE, h.status);
            assertEquals(expected, h.detail);
            assertEquals("ERROR", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertFalse(Objects.requireNonNull(h.published.get("sync#error")).toString().isEmpty());
        } finally {
            h.dispose();
        }
    }

    /** Checks event-range publication, clearing absent selections and REFRESH without additional server requests. */
    @Test
    void publishesRangeClearsMissingEventsAndRefreshesLocally() throws Exception {
        Handler h = new Handler();
        h.initialize();
        try {
            h.synchronize((m, u, b, d) -> response(false), account(), () -> true);
            assertEquals(ThingStatus.ONLINE, h.status);
            assertEquals(OnOffType.ON, h.published.get("current#active"));
            assertNotNull(h.published.get("events#range-start"));
            assertFalse(h.published.containsKey("next#active"));
            State last = h.published.get("sync#last");
            h.handleCommand(new ChannelUID(h.getThing().getUID(), "sync#last"), RefreshType.REFRESH);
            assertEquals(last, h.published.get("sync#last"));
            h.synchronize((m, u, b, d) -> response(true), account(), () -> true);
            assertEquals(UnDefType.UNDEF, h.published.get("current#start"));
            assertEquals(OnOffType.OFF, h.published.get("current#active"));
            assertEquals("[]", Objects.requireNonNull(h.published.get("events#json")).toString());
        } finally {
            h.dispose();
        }
    }

    /** Checks that failures retain usable events and successful recovery replaces their cached state. */
    @Test
    void communicationFailureKeepsCacheAndRecoveryReplacesIt() throws Exception {
        Handler h = new Handler();
        h.initialize();
        try {
            h.synchronize((m, u, b, d) -> response(false), account(), () -> true);
            State json = h.published.get("events#json"), last = h.published.get("sync#last");
            h.synchronize((m, u, b, d) -> {
                throw new IOException("offline");
            }, account(), () -> true);
            assertEquals(ThingStatus.OFFLINE, h.status);
            assertEquals(json, h.published.get("events#json"));
            assertEquals(last, h.published.get("sync#last"));
            assertEquals("ERROR", Objects.requireNonNull(h.published.get("sync#status")).toString());
            h.synchronize((m, u, b, d) -> response(true), account(), () -> true);
            assertEquals(ThingStatus.ONLINE, h.status);
            h.bridgeConnectionFailed();
            assertEquals("ERROR", Objects.requireNonNull(h.published.get("sync#status")).toString());
        } finally {
            h.dispose();
        }
    }

    /** Checks that a fetch completing after disposal cannot publish calendar state. */
    @Test
    void completionAfterDisposeCannotPublish() throws Exception {
        Handler h = new Handler();
        h.initialize();
        CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
        DavTransport transport = (m, u, b, d) -> {
            entered.countDown();
            if (!release.await(10, TimeUnit.SECONDS)) {
                throw new IOException("timeout");
            }
            return response(false);
        };
        CompletableFuture<Void> work = CompletableFuture.runAsync(() -> {
            try {
                h.synchronize(transport, account(), () -> true);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        try {
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            h.dispose();
            Map<String, State> before = Map.copyOf(h.published);
            release.countDown();
            work.get(10, TimeUnit.SECONDS);
            assertEquals(before, h.published);
        } finally {
            release.countDown();
            h.dispose();
        }
    }

    /** Checks that restart recovery publishes stored events and Thing removal deletes their persistent cache. */
    @Test
    void restoresPersistentCacheBeforeLiveSyncAndRemovesItWithThing() throws Exception {
        MemoryStorage storage = new MemoryStorage();
        Handler first = new Handler(storage, null);
        first.initialize();
        try {
            first.synchronize((m, u, b, d) -> response(false), account(), () -> true);
        } finally {
            first.dispose();
        }
        String raw = Objects.requireNonNull(storage.get(first.getThing().getUID().toString()));
        assertFalse(raw.contains("secret"));
        assertFalse(raw.contains("user"));
        Bridge bridge = mock(Bridge.class);
        AccountHandler accountHandler = mock(AccountHandler.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        when(accountHandler.configuration()).thenReturn(account());
        Handler second = new Handler(storage, bridge);
        second.initialize();
        try {
            assertTrue(second.restored.await(10, TimeUnit.SECONDS));
            assertEquals(first.published.get("events#json"), second.published.get("events#json"));
            assertEquals(first.published.get("sync#last"), second.published.get("sync#last"));
            assertEquals(ThingStatus.UNKNOWN, second.status);
            assertEquals(ThingStatusDetail.NONE, second.detail);
            assertEquals("SYNCING", Objects.requireNonNull(second.published.get("sync#status")).toString());
            assertEquals("", Objects.requireNonNull(second.published.get("sync#error")).toString());
            second.bridgeConnectionFailed();
            assertEquals(ThingStatus.OFFLINE, second.status);
            assertEquals(ThingStatusDetail.BRIDGE_OFFLINE, second.detail);
            assertEquals("ERROR", Objects.requireNonNull(second.published.get("sync#status")).toString());
            second.handleRemoval();
            assertFalse(storage.containsKey(second.getThing().getUID().toString()));
        } finally {
            second.dispose();
        }
    }

    /**
     * Checks that restart into a new day reuses persisted collection state instead of downloading unchanged resources.
     */
    @Test
    void restartReusesPersistedTokenAfterCalendarDayRollover() throws Exception {
        MemoryStorage storage = new MemoryStorage();
        AccountConfiguration configuration = account();
        configuration.syncMode = "SYNC_TOKEN";
        configuration.maxPastDays = 1;
        configuration.maxFutureDays = 1;
        String uid;
        String raw;
        try (Fixture first = new Fixture(Map.of(), storage, null)) {
            first.handler.synchronize((method, uri, body, depth) -> {
                if ("REPORT".equals(method)) {
                    assertEquals("", requestedToken(body));
                    return syncResponse("T1", syncMember("one", "a"));
                }
                assertEquals("GET", method);
                return calendar("future", "DTSTART:20260920T090000Z\nDURATION:PT1H\n");
            }, configuration, () -> true);
            assertEquals("0", Objects.requireNonNull(first.handler.published.get("events#count")).toString());
            uid = first.handler.getThing().getUID().toString();
            raw = Objects.requireNonNull(storage.get(uid));
        }
        var before = JsonParser.parseString(raw).getAsJsonObject().getAsJsonObject("snapshot");
        assertEquals("T1", before.get("token").getAsString());
        assertEquals("2026-09-17T00:00:00Z/2026-09-20T00:00:00Z/Z", before.get("horizon").getAsString());
        try (Fixture restarted = new Fixture(Map.of(), storage, bridge(configuration),
                Instant.parse("2026-09-19T09:00:00Z"))) {
            assertEquals(ThingStatus.UNKNOWN, restarted.handler.status);
            assertEquals("1", Objects.requireNonNull(restarted.handler.published.get("events#count")).toString());
            assertEquals(raw, storage.get(uid));
            List<String> requests = new ArrayList<>();
            restarted.handler.synchronize((method, uri, body, depth) -> {
                requests.add(method);
                assertEquals("REPORT", method);
                assertEquals("T1", requestedToken(body));
                return syncResponse("T2", "");
            }, configuration, () -> true);
            assertEquals(List.of("REPORT"), requests);
            assertEquals(ThingStatus.ONLINE, restarted.handler.status);
            assertEquals("1", Objects.requireNonNull(restarted.handler.published.get("events#count")).toString());
            assertTrue(Objects.requireNonNull(restarted.handler.published.get("events#json")).toString()
                    .contains("future"));
            var after = JsonParser.parseString(Objects.requireNonNull(storage.get(uid))).getAsJsonObject()
                    .getAsJsonObject("snapshot");
            assertEquals("T2", after.get("token").getAsString());
            assertEquals("2026-09-18T00:00:00Z/2026-09-21T00:00:00Z/Z", after.get("horizon").getAsString());
            assertEquals(before.getAsJsonObject("resources"), after.getAsJsonObject("resources"));
        }
    }

    /** Checks that failed synchronization after a horizon change preserves persistence until a complete recovery. */
    @Test
    void rolloverFailureKeepsPersistedStateUntilTransactionalRecovery() throws Exception {
        try (Fixture fixture = new Fixture(Map.of())) {
            AccountConfiguration configuration = account();
            configuration.syncMode = "SYNC_TOKEN";
            configuration.maxPastDays = 1;
            configuration.maxFutureDays = 1;
            AtomicInteger reports = new AtomicInteger();
            DavTransport transport = (method, uri, body, depth) -> {
                if ("REPORT".equals(method)) {
                    int request = reports.incrementAndGet();
                    assertEquals(request == 1 ? "" : "T1", requestedToken(body));
                    return switch (request) {
                        case 1 ->
                            syncResponse("T1", syncMember("a", "a") + syncMember("b", "b") + syncMember("c", "c"));
                        case 2 -> syncResponse("T2", syncMember("b", "changed") + syncMember("d", "d")
                                + "<d:response><d:href>/calendar/c.ics</d:href><d:status>HTTP/1.1 404 Not Found</d:status></d:response>");
                        case 3 -> syncResponse("T3", "");
                        default -> throw new IOException("Unexpected REPORT");
                    };
                }
                assertEquals("GET", method);
                String properties = "DTSTART:20260918T080000Z\nDURATION:PT2H\n";
                return switch (uri.getPath()) {
                    case "/calendar/a.ics" -> calendar("a", properties);
                    case "/calendar/b.ics" -> calendar(reports.get() == 1 ? "b" : "updated-b", properties);
                    case "/calendar/c.ics" -> calendar("c", properties);
                    case "/calendar/d.ics" -> throw new IOException("offline");
                    default -> throw new IOException("Unexpected GET");
                };
            };
            Handler h = fixture.handler;
            h.synchronize(transport, configuration, () -> true);
            String uid = h.getThing().getUID().toString();
            String before = Objects.requireNonNull(fixture.storage.get(uid));
            fixture.clock.advance(24 * 3600);
            h.synchronize(transport, configuration, () -> true);
            assertEquals("ERROR", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals(before, fixture.storage.get(uid));
            h.synchronize(transport, configuration, () -> true);
            assertEquals(3, reports.get());
            assertEquals("OK", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals("3", Objects.requireNonNull(h.published.get("events#count")).toString());
            assertFalse(Objects.requireNonNull(h.published.get("events#json")).toString().contains("updated-b"));
            var original = JsonParser.parseString(before).getAsJsonObject().getAsJsonObject("snapshot");
            var recovered = JsonParser.parseString(Objects.requireNonNull(fixture.storage.get(uid))).getAsJsonObject()
                    .getAsJsonObject("snapshot");
            assertEquals("T3", recovered.get("token").getAsString());
            assertEquals(original.getAsJsonObject("resources"), recovered.getAsJsonObject("resources"));
            assertNotEquals(original.get("horizon"), recovered.get("horizon"));
        }
    }

    /** Checks that partial results publish usable events while retaining the timestamp of the last complete success. */
    @Test
    void partialSnapshotsPublishHealthyResourcesAndOnlyCompleteSyncAdvancesLast() throws Exception {
        String healthy = calendar("healthy", "DTSTART:20260918T080000Z\nDURATION:PT2H\nSUMMARY:Healthy\n");
        String unsupported = calendar("unsupported",
                "DTSTART:20260918T100000Z\nRDATE;VALUE=PERIOD:20260919T100000Z/PT1H\n");
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler h = fixture.handler;
            DavTransport partial = (m, u, b, d) -> multistatus(healthy, unsupported);
            h.synchronize(partial, account(), () -> true);
            assertEquals(ThingStatus.ONLINE, h.status);
            assertEquals("PARTIAL", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals(UnDefType.UNDEF, h.published.get("sync#last"));
            assertEquals("1", Objects.requireNonNull(h.published.get("events#count")).toString());
            assertTrue(Objects.requireNonNull(h.published.get("events#json")).toString().contains("healthy"));
            assertFalse(Objects.requireNonNull(h.published.get("events#json")).toString().contains("unsupported"));
            h.synchronize((m, u, b, d) -> multistatus(healthy), account(), () -> true);
            State last = h.published.get("sync#last");
            fixture.clock.advance(60);
            h.synchronize(partial, account(), () -> true);
            assertEquals(last, h.published.get("sync#last"));
            assertEquals("PARTIAL", Objects.requireNonNull(h.published.get("sync#status")).toString());
            h.synchronize((m, u, b, d) -> multistatus(healthy,
                    calendar("repaired", "DTSTART:20260918T100000Z\nDURATION:PT1H\n")), account(), () -> true);
            assertEquals("OK", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals("", Objects.requireNonNull(h.published.get("sync#error")).toString());
            assertNotEquals(last, h.published.get("sync#last"));
            assertEquals("2", Objects.requireNonNull(h.published.get("events#count")).toString());
        }
    }

    /** Checks that unsupported recurrence modifications cannot publish a misleading unmodified event series. */
    @Test
    void unsupportedRecurrenceRangeDoesNotPublishAnUnmodifiedSeries() throws Exception {
        String unsupported = "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\nUID:series\n"
                + "DTSTART:20260918T100000Z\nDURATION:PT1H\nRRULE:FREQ=DAILY;COUNT=2\nEND:VEVENT\n"
                + "BEGIN:VEVENT\nUID:series\nRECURRENCE-ID;RANGE=THISANDFUTURE:20260919T100000Z\n"
                + "DTSTART:20260919T120000Z\nEND:VEVENT\nEND:VCALENDAR";
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler h = fixture.handler;
            h.synchronize((m, u, b, d) -> multistatus(unsupported,
                    calendar("healthy", "DTSTART:20260918T080000Z\nDURATION:PT2H\n")), account(), () -> true);
            assertEquals("PARTIAL", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals("1", Objects.requireNonNull(h.published.get("events#count")).toString());
            assertFalse(Objects.requireNonNull(h.published.get("events#json")).toString().contains("series"));
            assertEquals(UnDefType.UNDEF, h.published.get("sync#last"));
        }
    }

    /** Checks that local clock updates preserve the waiting or failure state until actual synchronization succeeds. */
    @Test
    @Timeout(30)
    void coldStartRetryWaitsUnknownAndRecoveryAfterLiveSyncStaysOffline() throws Exception {
        for (boolean liveBefore : new boolean[] { false, true }) {
            try (Fixture fixture = new Fixture(Map.of())) {
                Handler h = fixture.handler;
                if (liveBefore) {
                    h.synchronize((m, u, b, d) -> multistatus(), account(), () -> true);
                }
                h.synchronize((m, u, b, d) -> {
                    throw new IOException("offline");
                }, account(), () -> true);
                CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
                var executor = Executors.newSingleThreadExecutor();
                try {
                    var work = executor.submit(() -> {
                        h.synchronize((m, u, b, d) -> {
                            entered.countDown();
                            if (!release.await(10, TimeUnit.SECONDS)) {
                                throw new IOException("timeout");
                            }
                            return multistatus();
                        }, account(), () -> true);
                        return null;
                    });
                    assertTrue(entered.await(10, TimeUnit.SECONDS));
                    assertEquals(liveBefore ? ThingStatus.OFFLINE : ThingStatus.UNKNOWN, h.status);
                    assertEquals("SYNCING", Objects.requireNonNull(h.published.get("sync#status")).toString());
                    release.countDown();
                    work.get(10, TimeUnit.SECONDS);
                    assertEquals(ThingStatus.ONLINE, h.status);
                    assertEquals("OK", Objects.requireNonNull(h.published.get("sync#status")).toString());
                } finally {
                    release.countDown();
                    executor.shutdownNow();
                    assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
                }
            }
        }
    }

    /** Checks that selected event channels use the full event set and preserve empty optional text. */
    @Test
    void currentAndNextUseEventsBeyondJsonLimitAndMissingTextsStayEmpty() throws Exception {
        try (Fixture fixture = new Fixture(Map.of("maxEvents", 1))) {
            Handler h = fixture.handler;
            h.synchronize((m, u, b, d) -> multistatus(calendar("past", "DTSTART:20260918T070000Z\nDURATION:PT1H\n"),
                    calendar("running", "DTSTART:20260918T080000Z\nDURATION:PT2H\n"),
                    calendar("future", "DTSTART:20260918T100000Z\nDURATION:PT1H\n")), account(), () -> true);
            assertEquals("1", Objects.requireNonNull(h.published.get("events#count")).toString());
            assertEquals(OnOffType.ON, h.published.get("events#truncated"));
            assertEquals("running", Objects.requireNonNull(h.published.get("current#uid")).toString());
            assertEquals("future", Objects.requireNonNull(h.published.get("next#uid")).toString());
            for (String group : List.of("current", "next")) {
                for (String field : List.of("title", "description", "location", "organizer", "categories")) {
                    assertEquals("", Objects.requireNonNull(h.published.get(group + "#" + field)).toString());
                }
            }
            h.synchronize((m, u, b, d) -> multistatus(), account(), () -> true);
            assertEquals(UnDefType.UNDEF, h.published.get("current#title"));
            assertEquals(UnDefType.UNDEF, h.published.get("next#title"));
        }
    }

    /** Checks that calendar color metadata is published without altering its server-provided syntax. */
    @Test
    void publishesServerCalendarColorsWithoutConversion() {
        for (String color : List.of("#CEE7FFFF", "#CEE7FF", "#cee7ff", "#cee7ffff", "blue", "DarkGreen", "nonsense")) {
            try (Fixture fixture = new Fixture(Map.of(), new MemoryStorage(), null, "Calendar",
                    Map.of("calendarColor", color))) {
                assertEquals(new StringType(color), fixture.handler.published.get("calendar-color"), color);
                assertEquals(color, fixture.handler.getThing().getProperties().get("calendarColor"));
                assertTrue(fixture.jobs.isEmpty());
                assertTrue(fixture.storage.getKeys().isEmpty());
            }
        }
    }

    /** Checks that color output trims whitespace without mutating the underlying Thing property. */
    @Test
    void trimsCalendarColorWithoutChangingDiscoveryMetadata() {
        String raw = "  #CEE7FFFF  ";
        try (Fixture fixture = new Fixture(Map.of(), new MemoryStorage(), null, "Calendar",
                Map.of("calendarColor", raw, "calendarPrivileges", "read,write", "calendarDescription", "Family"))) {
            assertEquals("#CEE7FFFF",
                    Objects.requireNonNull(fixture.handler.published.get("calendar-color")).toString());
            assertEquals(
                    Map.of("calendarColor", raw, "calendarPrivileges", "read,write", "calendarDescription", "Family",
                            "calendarIdentity", "caldav:account:test|/calendar/"),
                    fixture.handler.getThing().getProperties());
        }
    }

    /** Checks that unavailable or blank calendar colors publish UNDEF. */
    @Test
    void absentOrBlankCalendarColorPublishesUndef() {
        for (Map<String, String> properties : List.of(Map.<String, String> of(), Map.of("calendarColor", ""),
                Map.of("calendarColor", "   "), Map.of("calendarColor", "\u2003"))) {
            try (Fixture fixture = new Fixture(Map.of(), new MemoryStorage(), null, "Calendar", properties)) {
                assertEquals(UnDefType.UNDEF, fixture.handler.published.get("calendar-color"));
            }
        }
    }

    /** Checks that unsafe control characters cause UNDEF rather than a repaired color value. */
    @Test
    void rejectsControlCharactersWithoutRepairingCalendarColor() {
        for (String raw : List.of("#CEE7FF\nblue", "blue\r", "\tblue", "blue\u0000", "blue\u001B", "blue\u007F",
                "blue\u0085")) {
            try (Fixture fixture = new Fixture(Map.of(), new MemoryStorage(), null, "Calendar",
                    Map.of("calendarColor", raw))) {
                assertEquals(UnDefType.UNDEF, fixture.handler.published.get("calendar-color"));
                assertEquals(raw, fixture.handler.getThing().getProperties().get("calendarColor"));
            }
        }
    }

    /** Checks that Thing metadata updates republish changed or removed calendar color. */
    @Test
    void thingUpdatesRepublishChangedAndRemovedCalendarColor() {
        try (Fixture fixture = new Fixture(Map.of(), new MemoryStorage(), null, "Calendar",
                Map.of("calendarColor", "#CEE7FFFF"))) {
            Handler h = fixture.handler;
            for (Map<String, String> properties : List.of(Map.of("calendarColor", "blue"),
                    Map.of("calendarColor", " #cee7ff "), Map.<String, String> of(),
                    Map.of("calendarColor", "DarkGreen"))) {
                Thing current = h.getThing();
                Thing updated = ThingBuilder.create(current.getThingTypeUID(), current.getUID())
                        .withBridge(current.getBridgeUID()).withConfiguration(current.getConfiguration())
                        .withProperties(properties).build();
                h.thingUpdated(updated);
                String raw = properties.get("calendarColor");
                assertEquals(raw == null ? UnDefType.UNDEF : new StringType(raw.trim()),
                        h.published.get("calendar-color"));
                assertEquals(ThingStatus.UNKNOWN, h.status);
                assertTrue(fixture.jobs.isEmpty());
            }
        }
    }

    /** Checks that color REFRESH uses current local metadata without a calendar request. */
    @Test
    void calendarColorRefreshReadsCurrentMetadataWithoutFetchingEvents() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        try (Fixture fixture = new Fixture(Map.of(), new MemoryStorage(), null, "Calendar",
                Map.of("calendarColor", "#CEE7FFFF"))) {
            Handler h = fixture.handler;
            h.synchronize((m, u, b, d) -> {
                requests.incrementAndGet();
                return multistatus(calendar("running", "DTSTART:20260918T080000Z\nDURATION:PT2H\n"));
            }, account(), () -> true);
            Map<String, State> events = new HashMap<>(h.published);
            events.remove("calendar-color");
            ChannelUID channel = new ChannelUID(h.getThing().getUID(), "calendar-color");
            h.getThing().setProperty("calendarColor", " blue ");
            h.handleCommand(channel, RefreshType.REFRESH);
            assertEquals("blue", Objects.requireNonNull(h.published.remove("calendar-color")).toString());
            assertEquals(events, h.published);
            h.getThing().setProperty("calendarColor", null);
            h.handleCommand(channel, RefreshType.REFRESH);
            assertEquals(UnDefType.UNDEF, h.published.remove("calendar-color"));
            assertEquals(events, h.published);
            assertEquals(1, requests.get());
        }
    }

    /** Checks that color REFRESH cannot publish state after disposal. */
    @Test
    void disposedCalendarDoesNotPublishColorOnRefresh() {
        try (Fixture fixture = new Fixture(Map.of(), new MemoryStorage(), null, "Calendar",
                Map.of("calendarColor", "blue"))) {
            Handler h = fixture.handler;
            h.dispose();
            h.published.clear();
            h.getThing().setProperty("calendarColor", "DarkGreen");
            h.handleCommand(new ChannelUID(h.getThing().getUID(), "calendar-color"), RefreshType.REFRESH);
            assertTrue(h.published.isEmpty());
        }
    }

    /** Checks that REFRESH and unsupported write commands never cause calendar network access. */
    @Test
    void allRefreshChannelsAndWriteCommandsAreLocal() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler h = fixture.handler;
            h.synchronize((m, u, b, d) -> {
                requests.incrementAndGet();
                return multistatus(calendar("running", "DTSTART:20260918T080000Z\nDURATION:PT2H\n"));
            }, account(), () -> true);
            Map<String, State> before = Map.copyOf(h.published);
            assertEquals(28, before.size());
            for (String channel : before.keySet()) {
                h.handleCommand(new ChannelUID(h.getThing().getUID(), channel), RefreshType.REFRESH);
                h.handleCommand(new ChannelUID(h.getThing().getUID(), channel), OnOffType.ON);
            }
            assertEquals(before, h.published);
            assertEquals(1, requests.get());
        }
    }

    /** Checks that old fetch results cannot publish, persist or schedule updates after reinitialization. */
    @Test
    @Timeout(30)
    void reinitializationRejectsOldFetchPublicationCacheAndTimer() throws Exception {
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler h = fixture.handler;
            CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
            var executor = Executors.newSingleThreadExecutor();
            try {
                var old = executor.submit(() -> {
                    h.synchronize((m, u, b, d) -> {
                        entered.countDown();
                        if (!release.await(10, TimeUnit.SECONDS)) {
                            throw new IOException("timeout");
                        }
                        return multistatus(calendar("old", "DTSTART:20260918T080000Z\nDURATION:PT2H\n"));
                    }, account(), () -> true);
                    return null;
                });
                assertTrue(entered.await(10, TimeUnit.SECONDS));
                h.getThing().getConfiguration().put("path", "https://example.org/new/");
                h.thingUpdated(h.getThing());
                h.synchronize((method, uri, body, depth) -> {
                    assertEquals(URI.create("https://example.org/new/"), uri);
                    return multistatus(calendar("new", "DTSTART:20260918T080000Z\nDURATION:PT2H\n"))
                            .replace("<d:href>/calendar/", "<d:href>/new/");
                }, account(), () -> true);
                Map<String, State> published = Map.copyOf(h.published);
                String persisted = fixture.storage.get(h.getThing().getUID().toString());
                int jobs = fixture.jobs.size();
                release.countDown();
                old.get(10, TimeUnit.SECONDS);
                assertEquals(published, h.published);
                assertEquals(persisted, fixture.storage.get(h.getThing().getUID().toString()));
                assertEquals(jobs, fixture.jobs.size());
                assertEquals(ThingStatus.ONLINE, h.status);
            } finally {
                release.countDown();
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            }
        }
    }

    /** Checks deterministic local event transitions and rejection of disposed timer work without network access. */
    @Test
    void localClockPublishesStartEndAndRejectsDisposedJobsWithoutNetwork() throws Exception {
        AtomicInteger requests = new AtomicInteger();
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler h = fixture.handler;
            h.synchronize((m, u, b, d) -> {
                requests.incrementAndGet();
                return multistatus(calendar("future", "DTSTART:20260918T090030Z\nDTEND:20260918T090045Z\n"));
            }, account(), () -> true);
            assertEquals(OnOffType.OFF, h.published.get("current#active"));
            ClockJob start = fixture.jobs.getLast();
            assertEquals(30_000L, start.delay());
            fixture.clock.advance(30);
            start.task().run();
            assertEquals(OnOffType.ON, h.published.get("current#active"));
            ClockJob end = fixture.jobs.getLast();
            assertEquals(15_000L, end.delay());
            fixture.clock.advance(15);
            end.task().run();
            assertEquals(OnOffType.OFF, h.published.get("current#active"));
            assertEquals(UnDefType.UNDEF, h.published.get("next#start"));
            ClockJob stale = fixture.jobs.getLast();
            Map<String, State> published = Map.copyOf(h.published);
            h.dispose();
            verify(stale.future()).cancel(true);
            stale.task().run();
            assertEquals(published, h.published);
            assertEquals(1, requests.get());
        }
    }

    /** Checks that local scheduling updates moving output ranges and midnight boundaries. */
    @Test
    void movingRangeAndMidnightAreUpdatedByLocalClock() throws Exception {
        try (Fixture fixture = new Fixture(Map.of("rangeAnchor", "NOW", "rangeStartOffset", 0, "rangeEndOffset", 0))) {
            Handler h = fixture.handler;
            h.synchronize((m, u, b, d) -> multistatus(), account(), () -> true);
            State first = h.published.get("events#range-start");
            fixture.clock.advance(60);
            fixture.jobs.getLast().task().run();
            assertNotEquals(first, h.published.get("events#range-start"));
        }
        try (Fixture fixture = new Fixture(Map.of("rangeStartOffset", 0, "rangeEndOffset", 0))) {
            fixture.clock.now = Instant.parse("2026-03-28T22:59:30Z");
            fixture.zone.set(ZoneId.of("Europe/Berlin"));
            Handler h = fixture.handler;
            h.synchronize((m, u, b, d) -> multistatus(), account(), () -> true);
            State start = h.published.get("events#range-start");
            assertEquals(30_000L, fixture.jobs.getLast().delay());
            fixture.clock.advance(30);
            fixture.jobs.getLast().task().run();
            assertNotEquals(start, h.published.get("events#range-start"));
            assertEquals(
                    new org.openhab.core.library.types.DateTimeType(
                            ZonedDateTime.parse("2026-03-30T00:00+02:00[Europe/Berlin]")),
                    h.published.get("events#range-end"));
        }
    }

    /** Checks that a changed openHAB time zone clears incompatible projections until live synchronization. */
    @Test
    void localZoneChangeClearsOldDataUntilSuccessfulSynchronization() throws Exception {
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler h = fixture.handler;
            DavTransport transport = (m, u, b,
                    d) -> multistatus(calendar("floating", "DTSTART:20260918T080000\nDURATION:PT2H\n"));
            h.synchronize(transport, account(), () -> true);
            fixture.zone.set(ZoneId.of("Europe/Berlin"));
            fixture.jobs.getLast().task().run();
            assertEquals(UnDefType.UNDEF, h.published.get("events#json"));
            assertEquals("ERROR", Objects.requireNonNull(h.published.get("sync#status")).toString());
            h.synchronize(transport, account(), () -> true);
            assertEquals("OK", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertTrue(Objects.requireNonNull(h.published.get("events#json")).toString()
                    .contains("2026-09-18T08:00+02:00"));
        }
    }

    /** Checks that unusable cached data cannot establish a successful live calendar state. */
    @Test
    void corruptForeignAndOversizedCachesDoNotInventLiveSuccess() throws Exception {
        MemoryStorage storage = new MemoryStorage();
        String uid;
        String persisted;
        try (Fixture first = new Fixture(Map.of(), storage, null)) {
            first.handler.synchronize(
                    (m, u, b, d) -> multistatus(calendar("cached", "DTSTART:20260918T080000Z\nDURATION:PT2H\n")),
                    account(), () -> true);
            uid = first.handler.getThing().getUID().toString();
            persisted = Objects.requireNonNull(storage.get(uid));
        }
        Bridge bridge = mock(Bridge.class);
        AccountHandler accountHandler = mock(AccountHandler.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        when(accountHandler.configuration()).thenReturn(account());
        for (String raw : List.of("{broken", "{}",
                persisted.replaceFirst("\"identity\":\"[^\"]*\"", "\"identity\":\"foreign\""),
                "x".repeat(16 * 1024 * 1024 + 1))) {
            storage.put(uid, raw);
            try (Fixture fixture = new Fixture(Map.of(), storage, bridge)) {
                Handler h = fixture.handler;
                assertEquals(ThingStatus.UNKNOWN, h.status);
                assertEquals(UnDefType.UNDEF, h.published.get("events#json"));
                assertEquals(UnDefType.UNDEF, h.published.get("sync#last"));
                h.synchronize((m, u, b, d) -> multistatus(), account(), () -> true);
                assertEquals(ThingStatus.ONLINE, h.status);
                assertEquals("OK", Objects.requireNonNull(h.published.get("sync#status")).toString());
                assertEquals("[]", Objects.requireNonNull(h.published.get("events#json")).toString());
            }
        }
    }

    /** Checks that restored events honor current selection settings while leaving connectivity unconfirmed. */
    @Test
    void restoredCacheAppliesCurrentRangeAndIncludeCancelledWithoutClaimingLiveSuccess() throws Exception {
        MemoryStorage storage = new MemoryStorage();
        try (Fixture first = new Fixture(Map.of(), storage, null)) {
            first.handler.synchronize(
                    (m, u, b, d) -> multistatus(calendar("healthy", "DTSTART:20260918T080000Z\nDURATION:PT2H\n"),
                            calendar("cancelled", "DTSTART:20260919T080000Z\nDURATION:PT2H\nSTATUS:CANCELLED\n")),
                    account(), () -> true);
            assertEquals("1", Objects.requireNonNull(first.handler.published.get("events#count")).toString());
        }
        Bridge bridge = mock(Bridge.class);
        AccountHandler accountHandler = mock(AccountHandler.class);
        when(bridge.getHandler()).thenReturn(accountHandler);
        when(accountHandler.configuration()).thenReturn(account());
        try (Fixture restored = new Fixture(
                Map.of("includeCancelled", true, "rangeStartOffset", 1, "rangeEndOffset", 1), storage, bridge)) {
            Handler h = restored.handler;
            assertEquals(ThingStatus.UNKNOWN, h.status);
            assertEquals("SYNCING", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals("1", Objects.requireNonNull(h.published.get("events#count")).toString());
            String json = Objects.requireNonNull(h.published.get("events#json")).toString();
            assertTrue(json.contains("cancelled"));
            assertFalse(json.contains("healthy"));
            h.synchronize((m, u, b, d) -> multistatus(), account(), () -> true);
            assertEquals(ThingStatus.ONLINE, h.status);
            assertEquals("OK", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals("[]", Objects.requireNonNull(h.published.get("events#json")).toString());
        }
    }

    /** Checks that a concurrent time-zone change prevents publication or persistence of an obsolete projection. */
    @Test
    @Timeout(30)
    void zoneChangeWhileFetchingCannotPublishOrPersistOldZoneData() throws Exception {
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler h = fixture.handler;
            CountDownLatch entered = new CountDownLatch(1), release = new CountDownLatch(1);
            var executor = Executors.newSingleThreadExecutor();
            try {
                var work = executor.submit(() -> {
                    h.synchronize((m, u, b, d) -> {
                        entered.countDown();
                        if (!release.await(10, TimeUnit.SECONDS)) {
                            throw new IOException("timeout");
                        }
                        return multistatus(calendar("floating", "DTSTART:20260918T080000\nDURATION:PT2H\n"));
                    }, account(), () -> true);
                    return null;
                });
                assertTrue(entered.await(10, TimeUnit.SECONDS));
                fixture.zone.set(ZoneId.of("Europe/Berlin"));
                release.countDown();
                work.get(10, TimeUnit.SECONDS);
                assertEquals(UnDefType.UNDEF, h.published.get("events#json"));
                assertFalse(fixture.storage.containsKey(h.getThing().getUID().toString()));
                assertTrue(fixture.jobs.isEmpty());
                h.synchronize(
                        (m, u, b, d) -> multistatus(calendar("floating", "DTSTART:20260918T080000\nDURATION:PT2H\n")),
                        account(), () -> true);
                assertEquals(ThingStatus.ONLINE, h.status);
                assertEquals("OK", Objects.requireNonNull(h.published.get("sync#status")).toString());
                assertTrue(Objects.requireNonNull(h.published.get("events#json")).toString()
                        .contains("2026-09-18T08:00+02:00"));
            } finally {
                release.countDown();
                executor.shutdownNow();
                assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
            }
        }
    }

    /** Checks that exhausted cached coverage is reported locally and repaired by successful synchronization. */
    @Test
    void expiredLocalHorizonIsReportedAndLiveSyncRepairsIt() throws Exception {
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler h = fixture.handler;
            AccountConfiguration account = account();
            account.maxPastDays = 1;
            account.maxFutureDays = 1;
            h.synchronize((m, u, b, d) -> multistatus(), account, () -> true);
            fixture.clock.advance(24 * 3600);
            fixture.jobs.getLast().task().run();
            assertEquals("ERROR", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertTrue(Objects.requireNonNull(h.published.get("sync#error")).toString().contains("cached horizon"));
            h.synchronize((m, u, b, d) -> multistatus(), account, () -> true);
            assertEquals("OK", Objects.requireNonNull(h.published.get("sync#status")).toString());
            assertEquals("", Objects.requireNonNull(h.published.get("sync#error")).toString());
        }
    }

    private static Bridge bridge(AccountConfiguration account) {
        Bridge bridge = Objects.requireNonNull(mock(Bridge.class));
        AccountHandler handler = Objects.requireNonNull(mock(AccountHandler.class));
        when(bridge.getHandler()).thenReturn(handler);
        when(handler.configuration()).thenReturn(account);
        return bridge;
    }

    private static String savedIdentity(AccountConfiguration account, String path, ZoneId zone) throws Exception {
        try (Fixture fixture = new Fixture(Map.of("path", path))) {
            fixture.zone.set(zone);
            fixture.handler.synchronize((m, u, b, d) -> multistatus(), account, () -> true);
            String raw = Objects.requireNonNull(fixture.storage.get(fixture.handler.getThing().getUID().toString()));
            return JsonParser.parseString(raw).getAsJsonObject().get("identity").getAsString();
        }
    }

    /** Checks that equivalent account and collection URI forms share a cache identity. */
    @Test
    void cacheIdentityUsesCanonicalAccountAndCollectionUris() throws Exception {
        String identity = savedIdentity(account(), "https://example.org/calendar/", ZoneOffset.UTC);
        for (String path : List.of("/calendar/", "https://EXAMPLE.org:443/calendar/", "/cal%65ndar/", "/calendar/./",
                "/other/../calendar/")) {
            assertEquals(identity, savedIdentity(account(), path, ZoneOffset.UTC), path);
        }
        AccountConfiguration alias = account();
        alias.url = "https://EXAMPLE.org:443/./";
        assertEquals(identity, savedIdentity(alias, "/calendar/", ZoneOffset.UTC));
        assertNotEquals(identity, savedIdentity(account(), "/other/", ZoneOffset.UTC));
        assertNotEquals(identity, savedIdentity(account(), "/Calendar/", ZoneOffset.UTC));
        assertNotEquals(identity, savedIdentity(account(), "/calendar/?view=2", ZoneOffset.UTC));
    }

    /** Checks that cache identity includes the relevant account and projection context while excluding passwords. */
    @Test
    void cacheIdentityRetainsUserEndpointAndZoneContextButExcludesPassword() throws Exception {
        AccountConfiguration original = account();
        String identity = savedIdentity(original, "/calendar/", ZoneOffset.UTC);
        AccountConfiguration password = account();
        password.password = "replacement-password";
        assertEquals(identity, savedIdentity(password, "/calendar/", ZoneOffset.UTC));
        AccountConfiguration user = account();
        user.username = "another-user";
        assertNotEquals(identity, savedIdentity(user, "/calendar/", ZoneOffset.UTC));
        AccountConfiguration endpoint = account();
        endpoint.url = "https://example.org/caldav/";
        assertNotEquals(identity, savedIdentity(endpoint, "/calendar/", ZoneOffset.UTC));
        assertNotEquals(identity, savedIdentity(original, "/calendar/", ZoneId.of("Europe/Berlin")));
    }

    /** Checks that a versioned compact resource snapshot restores for equivalent manual collection references. */
    @Test
    void versionedCompactSnapshotRestoresAcrossEquivalentManualPaths() throws Exception {
        MemoryStorage storage = new MemoryStorage();
        Map<String, State> published;
        String uid;
        try (Fixture first = new Fixture(Map.of(), storage, null)) {
            first.handler
                    .synchronize(
                            (m, u, b, d) -> multistatus(calendar("one", "DTSTART:20260918T080000Z\nDURATION:PT2H\n"),
                                    calendar("two", "DTSTART:20260919T080000Z\nDURATION:PT2H\n")),
                            account(), () -> true);
            published = Map.copyOf(first.handler.published);
            uid = first.handler.getThing().getUID().toString();
        }
        String raw = Objects.requireNonNull(storage.get(uid));
        var json = JsonParser.parseString(raw).getAsJsonObject();
        assertEquals(1, json.get("version").getAsInt());
        assertEquals(Set.of("0.ics", "1.ics"), json.getAsJsonObject("snapshot").getAsJsonObject("resources").keySet());
        assertFalse(raw.contains("https://"));
        for (String path : List.of("/calendar/", "https://EXAMPLE.org:443/cal%65ndar/")) {
            try (Fixture restored = new Fixture(Map.of("path", path), storage, bridge(account()))) {
                assertEquals(ThingStatus.UNKNOWN, restored.handler.status);
                for (String channel : List.of("events#json", "events#count", "current#uid", "next#uid", "sync#last")) {
                    assertEquals(published.get(channel), restored.handler.published.get(channel), channel);
                }
            }
        }
    }

    /** Checks that incompatible or partly corrupt cache snapshots are discarded transactionally and rebuilt. */
    @Test
    void incompatibleAndPartlyCorruptSnapshotsAreDiscardedInFullThenRebuilt() throws Exception {
        MemoryStorage storage = new MemoryStorage();
        String uid;
        String raw;
        try (Fixture first = new Fixture(Map.of(), storage, null)) {
            first.handler
                    .synchronize(
                            (m, u, b, d) -> multistatus(calendar("valid", "DTSTART:20260918T080000Z\nDURATION:PT2H\n"),
                                    calendar("corrupt", "DTSTART:20260919T080000Z\nDURATION:PT2H\n")),
                            account(), () -> true);
            uid = first.handler.getThing().getUID().toString();
            raw = Objects.requireNonNull(storage.get(uid));
        }
        List<String> invalid = new ArrayList<>();
        var unversioned = JsonParser.parseString(raw).getAsJsonObject();
        unversioned.remove("version");
        invalid.add(unversioned.toString());
        for (String field : List.of("horizon", "token")) {
            var json = JsonParser.parseString(raw).getAsJsonObject();
            json.getAsJsonObject("snapshot").remove(field);
            invalid.add(json.toString());
        }
        for (String field : List.of("etag", "data")) {
            var json = JsonParser.parseString(raw).getAsJsonObject();
            json.getAsJsonObject("snapshot").getAsJsonObject("resources").getAsJsonObject("0.ics").remove(field);
            invalid.add(json.toString());
        }
        for (int version : new int[] { 0, 2 }) {
            var json = JsonParser.parseString(raw).getAsJsonObject();
            json.addProperty("version", version);
            invalid.add(json.toString());
        }
        for (String reference : List.of("https://other.org/1.ics", "https://example.org:444/1.ics",
                "http://example.org/1.ics", "https://user@example.org/1.ics", "1.ics#fragment", "bad%2.ics",
                "bad%zz.ics", "https://example.org/calendar/1.ics", "/calendar/1.ics", "./1.ics", "../1.ics",
                "%2E/../1.ics", "%31.ics", "")) {
            var json = JsonParser.parseString(raw).getAsJsonObject();
            var resources = json.getAsJsonObject("snapshot").getAsJsonObject("resources");
            resources.add(reference, resources.remove("1.ics"));
            invalid.add(json.toString());
        }
        for (String candidate : invalid) {
            storage.put(uid, candidate);
            try (Fixture restored = new Fixture(Map.of(), storage, bridge(account()))) {
                Handler h = restored.handler;
                assertEquals(ThingStatus.UNKNOWN, h.status);
                assertEquals(UnDefType.UNDEF, h.published.get("events#json"));
                assertEquals(UnDefType.UNDEF, h.published.get("sync#last"));
                assertTrue(restored.jobs.isEmpty());
                AtomicInteger calls = new AtomicInteger();
                h.synchronize((method, uri, body, depth) -> {
                    assertEquals("REPORT", method);
                    assertEquals(URI.create("https://example.org/calendar/"), uri);
                    calls.incrementAndGet();
                    return multistatus(calendar("fresh", "DTSTART:20260918T080000Z\nDURATION:PT2H\n"));
                }, account(), () -> true);
                assertEquals(1, calls.get());
                assertEquals(ThingStatus.ONLINE, h.status);
                assertEquals("OK", Objects.requireNonNull(h.published.get("sync#status")).toString());
                assertTrue(Objects.requireNonNull(h.published.get("events#json")).toString().contains("fresh"));
                assertFalse(Objects.requireNonNull(h.published.get("events#json")).toString().contains("valid"));
                assertEquals(Set.of("0.ics"), JsonParser.parseString(Objects.requireNonNull(storage.get(uid)))
                        .getAsJsonObject().getAsJsonObject("snapshot").getAsJsonObject("resources").keySet());
            }
        }
    }

    /** Checks that presentation metadata changes do not prevent restoration of compatible calendar resources. */
    @Test
    void cacheRestoreIgnoresLabelsAndOptionalDiscoveryMetadata() throws Exception {
        MemoryStorage storage = new MemoryStorage();
        State json;
        try (Fixture first = new Fixture(Map.of(), storage, null)) {
            first.handler.synchronize(
                    (m, u, b, d) -> multistatus(calendar("cached", "DTSTART:20260918T080000Z\nDURATION:PT2H\n")),
                    account(), () -> true);
            json = Objects.requireNonNull(first.handler.published.get("events#json"));
        }
        try (Fixture restored = new Fixture(Map.of("path", "/calendar/"), storage, bridge(account()),
                "Changed calendar label", Map.of("calendarDescription", "Changed description", "calendarColor",
                        "#112233", "calendarPrivileges", "read,write"))) {
            assertEquals(json, restored.handler.published.get("events#json"));
            assertEquals(ThingStatus.UNKNOWN, restored.handler.status);
        }
    }

    /** Checks that an unconfirmed account bridge does not produce a fabricated calendar communication error. */
    @Test
    void unknownBridgeDoesNotInventACommunicationFailure() {
        try (Fixture fixture = new Fixture(Map.of())) {
            fixture.handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.UNKNOWN, ThingStatusDetail.NONE,
                    "Waiting for CalDAV server communication"));
            assertEquals(ThingStatus.UNKNOWN, fixture.handler.status);
            assertEquals(ThingStatusDetail.NONE, fixture.handler.detail);
            assertEquals("SYNCING", Objects.requireNonNull(fixture.handler.published.get("sync#status")).toString());
            assertEquals("", Objects.requireNonNull(fixture.handler.published.get("sync#error")).toString());
        }
    }

    /** Checks that calendar authentication errors retain usable events and expose a safe specific diagnosis. */
    @Test
    void http401ExplainsAuthenticationFailureAndPreservesLastUsableData() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 401), ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/status.calendar.authentication", "Authentication failed (HTTP 401)", true);
    }

    /** Checks that forbidden calendar access receives the correct specific diagnosis. */
    @Test
    void http403ExplainsForbiddenCalendarAccess() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 403), ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/status.calendar.forbidden", "Calendar access forbidden (HTTP 403)", true);
    }

    /** Checks that an initially absent calendar collection is reported as a configuration error. */
    @Test
    void http404BeforeFirstSuccessReportsConfigurationError() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 404), ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/status.calendar.not-found", "Calendar collection was not found (HTTP 404)", false);
    }

    /** Checks that a previously working collection reports GONE while retaining its last usable data. */
    @Test
    void http404AfterLiveSuccessReportsGoneAndPreservesLastUsableData() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 404), ThingStatusDetail.GONE, "@text/status.calendar.gone",
                "Calendar collection is no longer available (HTTP 404)", true);
    }

    /** Checks that server failures preserve calendar data and receive a specific safe diagnosis. */
    @Test
    void http500ExplainsServerFailureAndPreservesLastUsableData() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 500), ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/status.calendar.http-error [\"500\"]", "Server returned HTTP 500", true);
    }

    /** Checks that rejected calendar reports retain their structured HTTP status. */
    @Test
    void rejectedReportPreservesHttpStatus() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 405), ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/status.calendar.http-error [\"405\"]", "Server returned HTTP 405", true);
    }

    /** Checks that network causes produce specific diagnostics without revealing raw exception text. */
    @Test
    void structuredNetworkFailuresExplainCauseWithoutExposingExceptionText() throws Exception {
        for (Exception timeout : List.of(new TimeoutException("private-host password"),
                new SocketTimeoutException("private-host password"))) {
            checkFailure(new IOException("private calendar-data", timeout), ThingStatusDetail.COMMUNICATION_ERROR,
                    "@text/status.timeout", "CalDAV request timed out", true);
        }
        checkFailure(new IOException("private calendar-data", new UnknownHostException("private-host password")),
                ThingStatusDetail.COMMUNICATION_ERROR, "@text/status.dns", "CalDAV server name could not be resolved",
                true);
        checkFailure(new IOException("private calendar-data", new ConnectException("private-host password")),
                ThingStatusDetail.COMMUNICATION_ERROR, "@text/status.connection",
                "Unable to connect to the CalDAV server", true);
        checkFailure(
                new IOException("private calendar-data", new SSLHandshakeException("private-certificate password")),
                ThingStatusDetail.COMMUNICATION_ERROR, "@text/status.tls", "TLS connection to the CalDAV server failed",
                true);
    }

    private void checkFailure(IOException failure, ThingStatusDetail detail, String statusDescription,
            String description, boolean synchronizedBefore) throws Exception {
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler handler = fixture.handler;
            if (synchronizedBefore) {
                handler.synchronize(
                        (method, uri, body,
                                depth) -> multistatus(calendar("one",
                                        "DTSTART:20260918T080000Z\nDURATION:PT2H\nSUMMARY:Last usable event\n")),
                        account(), () -> true);
                assertEquals(ThingStatus.ONLINE, handler.status);
            }
            State events = Objects.requireNonNull(handler.published.get("events#json"));
            State lastSync = Objects.requireNonNull(handler.published.get("sync#last"));
            Map<String, String> saved = new HashMap<>();
            fixture.storage.getKeys().forEach(key -> saved.put(key, Objects.requireNonNull(fixture.storage.get(key))));
            handler.synchronize((method, uri, body, depth) -> {
                throw failure;
            }, account(), () -> true);
            assertEquals(ThingStatus.OFFLINE, handler.status);
            assertEquals(detail, handler.detail);
            assertEquals(statusDescription, handler.description);
            assertEquals(description, Objects.requireNonNull(handler.published.get("sync#error")).toString());
            assertEquals("ERROR", Objects.requireNonNull(handler.published.get("sync#status")).toString());
            assertEquals(events, handler.published.get("events#json"));
            assertEquals(lastSync, handler.published.get("sync#last"));
            assertEquals(saved.keySet(), Set.copyOf(fixture.storage.getKeys()));
            saved.forEach((key, value) -> assertEquals(value, fixture.storage.get(key)));
            handler.synchronize((method, uri, body, depth) -> multistatus(), account(), () -> true);
            assertEquals(ThingStatus.ONLINE, handler.status);
            assertEquals("", Objects.requireNonNull(handler.published.get("sync#error")).toString());
        }
    }

    /** Checks that a missing event resource is not misclassified as a missing calendar collection. */
    @Test
    void eventGet404RemainsCommunicationErrorAfterLiveSuccess() throws Exception {
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler handler = fixture.handler;
            AccountConfiguration config = account();
            handler.synchronize(
                    (method, uri, body,
                            depth) -> multistatus(calendar("one", "DTSTART:20260918T080000Z\nDURATION:PT2H\n")),
                    config, () -> true);
            State events = Objects.requireNonNull(handler.published.get("events#json"));
            State lastSync = Objects.requireNonNull(handler.published.get("sync#last"));
            config.syncMode = "ETAG";
            handler.synchronize((method, uri, body, depth) -> {
                if ("GET".equals(method)) {
                    throw new CalDavHttpException("GET", 404);
                }
                return "<d:multistatus xmlns:d=\"DAV:\"><d:response><d:href>/calendar/missing.ics</d:href>"
                        + "<d:propstat><d:prop><d:getetag>new</d:getetag></d:prop>"
                        + "<d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>";
            }, config, () -> true);
            assertEquals(ThingStatus.OFFLINE, handler.status);
            assertEquals(ThingStatusDetail.COMMUNICATION_ERROR, handler.detail);
            assertEquals("@text/status.calendar.resource-changed", handler.description);
            assertEquals("Calendar resource changed during synchronization (HTTP 404)",
                    Objects.requireNonNull(handler.published.get("sync#error")).toString());
            assertEquals(events, handler.published.get("events#json"));
            assertEquals(lastSync, handler.published.get("sync#last"));
        }
    }

    /** Checks that account failure preserves a calendar's own GONE diagnosis until successful calendar recovery. */
    @Test
    void bridgeOfflineDoesNotOverwriteOwnGoneDiagnosisAndRecoveryClearsIt() throws Exception {
        try (Fixture fixture = new Fixture(Map.of())) {
            Handler handler = fixture.handler;
            handler.synchronize((method, uri, body, depth) -> multistatus(), account(), () -> true);
            handler.synchronize((method, uri, body, depth) -> {
                throw new CalDavHttpException("REPORT", 404);
            }, account(), () -> true);
            handler.bridgeStatusChanged(new ThingStatusInfo(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
                    "CalDAV account communication failed"));
            assertEquals(ThingStatusDetail.GONE, handler.detail);
            assertEquals("@text/status.calendar.gone", handler.description);
            assertEquals("Calendar collection is no longer available (HTTP 404)",
                    Objects.requireNonNull(handler.published.get("sync#error")).toString());
            handler.synchronize((method, uri, body, depth) -> multistatus(), account(), () -> true);
            assertEquals(ThingStatus.ONLINE, handler.status);
            assertEquals("", Objects.requireNonNull(handler.published.get("sync#error")).toString());
            handler.bridgeConnectionFailed();
            assertEquals(ThingStatusDetail.BRIDGE_OFFLINE, handler.detail);
            assertEquals("@text/status.calendar.bridge-offline", handler.description);
            assertEquals("Account bridge is offline",
                    Objects.requireNonNull(handler.published.get("sync#error")).toString());
        }
    }
}
