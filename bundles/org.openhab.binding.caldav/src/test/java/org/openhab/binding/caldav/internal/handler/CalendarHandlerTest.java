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
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.client.DavTransport;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * Observable publication, cache recovery and disposal regression tests.
 * 
 * @author Andreas Vilippus - Initial contribution
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

        Handler() {
            this(new MemoryStorage(), null);
        }

        Handler(Storage<String> storage, @Nullable Bridge bridge) {
            super(thing(), () -> ZoneOffset.UTC, storage);
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
            this.status = status;
        }
    }

    private static Thing thing() {
        return ThingBuilder.create(new ThingTypeUID("caldav", "calendar"), "test")
                .withConfiguration(new Configuration(Map.of("path", "https://example.org/calendar/", "calendarId",
                        "calendar", "rangeStartOffset", -1, "rangeEndOffset", 1)))
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
    void restoresPersistentCacheWhileAccountIsOfflineAndRemovesItWithThing() throws Exception {
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
            assertEquals(ThingStatus.OFFLINE, second.status);
            assertEquals("ERROR", Objects.requireNonNull(second.published.get("sync#status")).toString());
            second.handleRemoval();
            assertFalse(storage.containsKey(second.getThing().getUID().toString()));
        } finally {
            second.dispose();
        }
    }
}
