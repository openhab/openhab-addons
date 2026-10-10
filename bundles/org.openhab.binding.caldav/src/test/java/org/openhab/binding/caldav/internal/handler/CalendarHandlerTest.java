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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import javax.net.ssl.SSLHandshakeException;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
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
 */
@NonNullByDefault
@Timeout(15)
class CalendarHandlerTest {
    private static final class MemoryStorage implements Storage<String> {
        private final Map<String, @Nullable String> values = new HashMap<>();

        @Override
        public @Nullable String put(String key, @Nullable String value) {
            return values.put(key, value);
        }

        @Override
        public @Nullable String get(String key) {
            return values.get(key);
        }

        @Override
        public @Nullable String remove(String key) {
            return values.remove(key);
        }

        @Override
        public boolean containsKey(String key) {
            return values.containsKey(key);
        }

        @Override
        public Collection<String> getKeys() {
            return values.keySet();
        }

        @Override
        public Collection<@Nullable String> getValues() {
            return values.values();
        }
    }

    private static final class Handler extends CalendarHandler {
        final Map<String, State> published = new java.util.concurrent.ConcurrentHashMap<>();
        final CountDownLatch restored = new CountDownLatch(1);
        private final @Nullable Bridge bridge;
        volatile ThingStatus status = ThingStatus.UNKNOWN;
        volatile ThingStatusDetail detail = ThingStatusDetail.NONE;
        volatile @Nullable String description;

        Handler() {
            this(new MemoryStorage(), null);
        }

        Handler(Storage<String> storage, @Nullable Bridge bridge) {
            this(thing(), storage, bridge, () -> ZoneOffset.UTC, Clock.systemUTC(), null);
        }

        Handler(Thing thing, Storage<String> storage, @Nullable Bridge bridge, TimeZoneProvider zone, Clock clock,
                @Nullable ScheduledExecutorService scheduler) {
            super(thing, zone, storage, clock, scheduler);
            this.bridge = bridge;
        }

        @Override
        protected @Nullable Bridge getBridge() {
            return bridge;
        }

        @Override
        protected void updateState(ChannelUID channel, State state) {
            published.put(channel.getId(), state);
            if ("events#json".equals(channel.getId()) && state != UnDefType.UNDEF) {
                restored.countDown();
            }
        }

        @Override
        protected void updateStatus(ThingStatus status, ThingStatusDetail detail, @Nullable String description) {
            switch (status) {
                case UNKNOWN, ONLINE, OFFLINE, REMOVED -> {
                    this.status = status;
                    this.detail = detail;
                    this.description = description;
                }
                default -> throw new IllegalArgumentException("Illegal binding status: " + status);
            }
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now = Instant.parse("2026-09-18T09:00:00Z");

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(@Nullable ZoneId zone) {
            return Clock.fixed(now, Objects.requireNonNull(zone));
        }

        @Override
        public Instant instant() {
            return now;
        }

        void advance(long seconds) {
            now = now.plusSeconds(seconds);
        }
    }

    private record ClockJob(Runnable task, long delay, ScheduledFuture<?> future) {
    }

    private static final class Fixture implements AutoCloseable {
        final MutableClock clock = new MutableClock();
        final AtomicReference<ZoneId> zone = new AtomicReference<>(ZoneOffset.UTC);
        final List<ClockJob> jobs = new ArrayList<>();
        final MemoryStorage storage;
        final Handler handler;

        Fixture(Map<String, Object> configuration) {
            this(configuration, new MemoryStorage(), null);
        }

        Fixture(Map<String, Object> configuration, MemoryStorage storage, @Nullable Bridge bridge) {
            this(configuration, storage, bridge, "Calendar", Map.of());
        }

        Fixture(Map<String, Object> configuration, MemoryStorage storage, @Nullable Bridge bridge, String label,
                Map<String, String> properties) {
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
                    .withConfiguration(new Configuration(values)).withLabel(label).withProperties(properties).build();
            handler = new Handler(thing, storage, bridge, () -> Objects.requireNonNull(zone.get()), clock, scheduler);
            handler.initialize();
        }

        @Override
        public void close() {
            handler.dispose();
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

    private static Thing thing() {
        return ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), "test")
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

    @Test
    @Timeout(30)
    void initialFetchRemainsUnknownUntilSuccess() throws Exception {
        checkStatusDuringFetch(false);
    }

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

    @Test
    void trimsCalendarColorWithoutChangingDiscoveryMetadata() {
        String raw = "  #CEE7FFFF  ";
        try (Fixture fixture = new Fixture(Map.of(), new MemoryStorage(), null, "Calendar",
                Map.of("calendarColor", raw, "calendarPrivileges", "read,write", "calendarDescription", "Family"))) {
            assertEquals("#CEE7FFFF",
                    Objects.requireNonNull(fixture.handler.published.get("calendar-color")).toString());
            assertEquals(
                    Map.of("calendarColor", raw, "calendarPrivileges", "read,write", "calendarDescription", "Family"),
                    fixture.handler.getThing().getProperties());
        }
    }

    @Test
    void absentOrBlankCalendarColorPublishesUndef() {
        for (Map<String, String> properties : List.of(Map.<String, String> of(), Map.of("calendarColor", ""),
                Map.of("calendarColor", "   "), Map.of("calendarColor", "\u2003"))) {
            try (Fixture fixture = new Fixture(Map.of(), new MemoryStorage(), null, "Calendar", properties)) {
                assertEquals(UnDefType.UNDEF, fixture.handler.published.get("calendar-color"));
            }
        }
    }

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
                        .withConfiguration(current.getConfiguration()).withProperties(properties).build();
                h.thingUpdated(updated);
                String raw = properties.get("calendarColor");
                assertEquals(raw == null ? UnDefType.UNDEF : new StringType(raw.trim()),
                        h.published.get("calendar-color"));
                assertEquals(ThingStatus.UNKNOWN, h.status);
                assertTrue(fixture.jobs.isEmpty());
            }
        }
    }

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

    @Test
    void http401ExplainsAuthenticationFailureAndPreservesLastUsableData() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 401), ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/status.calendar.authentication", "Authentication failed (HTTP 401)", true);
    }

    @Test
    void http403ExplainsForbiddenCalendarAccess() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 403), ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/status.calendar.forbidden", "Calendar access forbidden (HTTP 403)", true);
    }

    @Test
    void http404BeforeFirstSuccessReportsConfigurationError() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 404), ThingStatusDetail.CONFIGURATION_ERROR,
                "@text/status.calendar.not-found", "Calendar collection was not found (HTTP 404)", false);
    }

    @Test
    void http404AfterLiveSuccessReportsGoneAndPreservesLastUsableData() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 404), ThingStatusDetail.GONE, "@text/status.calendar.gone",
                "Calendar collection is no longer available (HTTP 404)", true);
    }

    @Test
    void http500ExplainsServerFailureAndPreservesLastUsableData() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 500), ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/status.calendar.http-error [\"500\"]", "Server returned HTTP 500", true);
    }

    @Test
    void rejectedReportPreservesHttpStatus() throws Exception {
        checkFailure(new CalDavHttpException("REPORT", 405), ThingStatusDetail.COMMUNICATION_ERROR,
                "@text/status.calendar.http-error [\"405\"]", "Server returned HTTP 405", true);
    }

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
