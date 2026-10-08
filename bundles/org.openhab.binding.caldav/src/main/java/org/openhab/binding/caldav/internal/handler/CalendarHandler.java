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

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.client.CalDavUris;
import org.openhab.binding.caldav.internal.client.DavTransport;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.binding.caldav.internal.config.CalDavConfiguration;
import org.openhab.binding.caldav.internal.config.CalendarConfiguration;
import org.openhab.binding.caldav.internal.logic.CalendarEventSelection;
import org.openhab.binding.caldav.internal.logic.CalendarWindow;
import org.openhab.binding.caldav.internal.logic.EventJson;
import org.openhab.binding.caldav.internal.model.CalendarEvent;
import org.openhab.binding.caldav.internal.sync.CalendarSynchronizer;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.storage.Storage;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

/**
 * Publishes bounded calendar snapshots and updates clock-dependent channels locally.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Cache, lifecycle and local event transitions
 * @author Andreas Vilippus - Time-zone generation and exact configuration validation
 * @author Andreas Vilippus - Canonical cache identity and versioned resource snapshots
 * @author Andreas Vilippus - Structured synchronization error details
 */
@NonNullByDefault
public class CalendarHandler extends BaseThingHandler {
    private static final int CACHE_VERSION = 1;
    private final Logger logger = LoggerFactory.getLogger(CalendarHandler.class);
    private final TimeZoneProvider timeZoneProvider;
    private final Storage<String> storage;
    private final Clock clock;
    private final ScheduledExecutorService clockScheduler;
    private final Object lifecycle = new Object();
    private final Map<String, State> states = new HashMap<>();
    private @Nullable ScheduledFuture<?> clockJob;
    private long generation;
    private boolean active;
    private boolean loaded;
    private boolean synchronizedOnce;
    private CalDavErrors.@Nullable Failure synchronizationFailure;
    private List<CalendarEvent> events = List.of();
    private @Nullable CalendarSynchronizer synchronizer;
    private @Nullable DavTransport transport;
    private @Nullable CalendarWindow cachedHorizon;
    private @Nullable ZoneId cachedZone;
    private @Nullable CalendarConfiguration configuration;
    private @Nullable URI collection;
    private final Gson gson = new Gson();
    private String cacheIdentity = "";

    public CalendarHandler(Thing thing, TimeZoneProvider timeZoneProvider, Storage<String> storage) {
        this(thing, timeZoneProvider, storage, Clock.systemUTC(), null);
    }

    CalendarHandler(Thing thing, TimeZoneProvider timeZoneProvider, Storage<String> storage, Clock clock,
            @Nullable ScheduledExecutorService clockScheduler) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.storage = storage;
        this.clock = clock;
        this.clockScheduler = clockScheduler == null ? scheduler : clockScheduler;
    }

    @Override
    public void initialize() {
        dispose();
        synchronized (lifecycle) {
            generation++;
            clearEvents();
            cacheIdentity = "";
            Configuration captured = new Configuration(getConfig().getProperties());
            try {
                CalDavConfiguration.validateIntegerValues(captured, "rangeStartOffset", "rangeEndOffset", "maxEvents");
                configuration = captured.as(CalendarConfiguration.class);
            } catch (IllegalArgumentException e) {
                var failure = CalDavErrors.calendar(e, false);
                set("sync#status", new StringType("ERROR"));
                set("sync#error", new StringType(failure.description()));
                updateStatus(ThingStatus.OFFLINE, failure.detail(), failure.description());
                return;
            }
            active = true;
            set("sync#status", new StringType("SYNCING"));
            set("sync#error", new StringType(""));
            updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Waiting for initial calendar synchronization");
        }
        Bridge bridge = getBridge();
        if (bridge != null && bridge.getHandler() instanceof AccountHandler account) {
            long current;
            synchronized (lifecycle) {
                current = generation;
            }
            clockScheduler.execute(() -> restoreAtStartup(account.configuration(), current));
            account.requestSync();
        }
    }

    @Override
    public void bridgeStatusChanged(ThingStatusInfo info) {
        if (info.getStatus() != ThingStatus.ONLINE && info.getStatus() != ThingStatus.UNKNOWN) {
            bridgeConnectionFailed();
        }
    }

    public void bridgeConnectionFailed() {
        bridgeConnectionFailed(() -> true);
    }

    public void bridgeConnectionFailed(BooleanSupplier accountValid) {
        synchronized (lifecycle) {
            CalendarConfiguration configured = configuration;
            if (!active || !accountValid.getAsBoolean() || configured == null) {
                return;
            }
            set("sync#status", new StringType("ERROR"));
            // A bridge failure must not hide the calendar's own diagnosis, including GONE.
            var failure = synchronizationFailure;
            if (failure == null) {
                set("sync#error", new StringType("Account bridge is offline"));
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE, "Account bridge is offline");
            } else {
                set("sync#error", new StringType(failure.description()));
                updateStatus(ThingStatus.OFFLINE, failure.detail(), failure.description());
            }
        }
    }

    @Override
    public void handleCommand(ChannelUID channel, Command command) {
        if (command != RefreshType.REFRESH) {
            return;
        }
        synchronized (lifecycle) {
            if (!active) {
                return;
            }
            updateState(channel, states.getOrDefault(channel.getId(), UnDefType.UNDEF));
        }
    }

    public void synchronize(DavTransport client, AccountConfiguration account, BooleanSupplier accountValid)
            throws InterruptedException {
        long current;
        CalendarConfiguration config;
        synchronized (lifecycle) {
            CalendarConfiguration configured = configuration;
            if (!active || configured == null) {
                return;
            }
            current = generation;
            config = configured;
        }
        try {
            ZoneId zone = timeZoneProvider.getTimeZone();
            ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), zone);
            URI uri = CalDavUris.canonicalize(CalDavConfiguration.validate(config, account));
            CalendarWindow horizon = CalDavConfiguration.horizon(account, zone, now);
            CalDavConfiguration.validate(CalendarWindow.from(config, zone, now), horizon);
            String identity = identity(account, uri, zone);
            boolean restoreNeeded;
            synchronized (lifecycle) {
                restoreNeeded = !identity.equals(cacheIdentity);
            }
            Restored restored = restoreNeeded ? readCache(identity, uri, horizon, zone, config) : null;
            CalendarSynchronizer worker;
            synchronized (lifecycle) {
                if (!valid(current, accountValid)) {
                    return;
                }
                if (!zone.equals(timeZoneProvider.getTimeZone())) {
                    zoneChanged();
                    return;
                }
                if (!identity.equals(cacheIdentity)) {
                    clearEvents();
                    cacheIdentity = identity;
                    synchronizer = null;
                    applyCache(restored, zone, config);
                }
                if (synchronizer == null || !client.equals(transport) || !uri.equals(collection)) {
                    synchronizer = new CalendarSynchronizer(client, uri);
                    transport = client;
                    collection = uri;
                }
                worker = Objects.requireNonNull(synchronizer);
                set("sync#status", new StringType("SYNCING"));
                set("sync#error", new StringType(""));
                if (!synchronizedOnce) {
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "Fetching calendar data");
                }
            }
            CalendarSynchronizer.Result result = worker.synchronize(horizon, zone, account.syncMode,
                    config.includeCancelled);
            synchronized (lifecycle) {
                if (!valid(current, accountValid)) {
                    return;
                }
                if (!zone.equals(timeZoneProvider.getTimeZone())) {
                    zoneChanged();
                    return;
                }
                events = result.events();
                loaded = true;
                synchronizedOnce = true;
                synchronizationFailure = null;
                cachedHorizon = horizon;
                cachedZone = zone;
                publish(config, zone, ZonedDateTime.ofInstant(clock.instant(), zone));
                boolean partial = result.failedResources() > 0;
                set("sync#status", new StringType(partial ? "PARTIAL" : "OK"));
                set("sync#error", new StringType(
                        partial ? result.failedResources() + " calendar resources could not be processed" : ""));
                if (!partial) {
                    set("sync#last", new DateTimeType(ZonedDateTime.ofInstant(clock.instant(), zone)));
                }
                updateStatus(ThingStatus.ONLINE);
                save(result.snapshot());
                scheduleClock(current);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw e;
        } catch (Exception e) {
            failed(current, accountValid, e);
        }
    }

    private void failed(long current, BooleanSupplier accountValid, Exception error) {
        synchronized (lifecycle) {
            if (!valid(current, accountValid)) {
                return;
            }
            var failure = CalDavErrors.calendar(error, synchronizedOnce);
            synchronizationFailure = failure;
            set("sync#status", new StringType("ERROR"));
            set("sync#error", new StringType(failure.description()));
            updateStatus(ThingStatus.OFFLINE, failure.detail(), failure.description());
        }
        if (error instanceof CalDavHttpException http) {
            logger.debug("CalDAV {} failed with HTTP {}", http.operation(), http.statusCode());
        }
    }

    private boolean valid(long expected, BooleanSupplier accountValid) {
        return active && generation == expected && accountValid.getAsBoolean();
    }

    private void publish(CalendarConfiguration config, ZoneId zone, ZonedDateTime now) {
        CalendarWindow window = CalendarWindow.from(config, zone, now);
        List<CalendarEvent> sorted = CalendarEventSelection
                .sort(events.stream().filter(e -> CalendarEventSelection.overlaps(e, window, zone)).toList(), zone);
        var selection = CalendarEventSelection.select(sorted, config.maxEvents, zone);
        set("events#json", new StringType(EventJson.serialize(selection.events())));
        set("events#count", new DecimalType(selection.events().size()));
        set("events#truncated", selection.truncated() ? OnOffType.ON : OnOffType.OFF);
        set("events#range-start", new DateTimeType(window.start()));
        set("events#range-end", new DateTimeType(window.end()));
        CalendarEvent current = CalendarEventSelection.current(sorted, now.toInstant(), zone);
        set("current#active", current == null ? OnOffType.OFF : OnOffType.ON);
        publishEvent("current", current, zone);
        publishEvent("next", CalendarEventSelection.next(sorted, now.toInstant(), zone), zone);
    }

    private void publishEvent(String group, @Nullable CalendarEvent event, ZoneId zone) {
        if (event == null) {
            for (String field : List.of("uid", "title", "description", "location", "organizer", "categories", "all-day",
                    "start", "end")) {
                set(group + "#" + field, UnDefType.UNDEF);
            }
            return;
        }
        set(group + "#uid", new StringType(event.uid()));
        set(group + "#title", new StringType(event.title()));
        set(group + "#description", new StringType(event.description()));
        set(group + "#location", new StringType(event.location()));
        set(group + "#organizer", new StringType(event.organizer()));
        set(group + "#categories", new StringType(String.join(",", event.categories())));
        set(group + "#all-day", event.allDay() ? OnOffType.ON : OnOffType.OFF);
        set(group + "#start", new DateTimeType(CalendarEventSelection.start(event, zone).atZone(zone)));
        set(group + "#end", new DateTimeType(CalendarEventSelection.end(event, zone).atZone(zone)));
    }

    private void scheduleClock(long current) {
        ScheduledFuture<?> previous = clockJob;
        if (previous != null) {
            previous.cancel(false);
        }
        ZoneId zone = timeZoneProvider.getTimeZone();
        Instant now = clock.instant();
        Instant next = now.plusSeconds(60);
        for (CalendarEvent event : events) {
            Instant start = CalendarEventSelection.start(event, zone), end = CalendarEventSelection.end(event, zone);
            if (start.isAfter(now) && start.isBefore(next)) {
                next = start;
            }
            if (end.isAfter(now) && end.isBefore(next)) {
                next = end;
            }
        }
        Instant midnight = now.atZone(zone).toLocalDate().plusDays(1).atStartOfDay(zone).toInstant();
        if (midnight.isBefore(next)) {
            next = midnight;
        }
        long delay = Math.max(1, java.time.Duration.between(now, next).toMillis());
        clockJob = clockScheduler.schedule(() -> tick(current), delay, TimeUnit.MILLISECONDS);
    }

    private void tick(long current) {
        synchronized (lifecycle) {
            CalendarConfiguration config = configuration;
            if (!active || generation != current || config == null || !loaded) {
                return;
            }
            ZoneId zone = timeZoneProvider.getTimeZone();
            ZoneId previousZone = cachedZone;
            CalendarWindow horizon = cachedHorizon;
            ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), zone);
            CalendarWindow window = CalendarWindow.from(config, zone, now);
            if (previousZone != null && !previousZone.equals(zone)) {
                zoneChanged();
                return;
            }
            if (horizon != null && (window.start().isBefore(horizon.start()) || window.end().isAfter(horizon.end()))) {
                set("sync#status", new StringType("ERROR"));
                set("sync#error",
                        new StringType("Requested range exceeds cached horizon; waiting for synchronization"));
            }
            publish(config, zone, now);
            scheduleClock(current);
        }
    }

    private void zoneChanged() {
        clearEvents();
        cacheIdentity = "";
        synchronizer = null;
        ScheduledFuture<?> previous = clockJob;
        if (previous != null) {
            previous.cancel(false);
            clockJob = null;
        }
        set("sync#status", new StringType("ERROR"));
        set("sync#error", new StringType("Time zone changed; waiting for synchronization"));
    }

    private static String identity(AccountConfiguration account, URI uri, ZoneId zone) {
        try {
            String value = CalDavUris.canonicalize(URI.create(account.url)) + "\n" + account.username + "\n"
                    + CalDavUris.canonicalize(uri) + "\n" + zone;
            return HexFormat.of()
                    .formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }

    private void restoreAtStartup(AccountConfiguration account, long current) {
        CalendarConfiguration config;
        synchronized (lifecycle) {
            CalendarConfiguration configured = configuration;
            if (!active || current != generation || configured == null || synchronizedOnce) {
                return;
            }
            config = configured;
        }
        try {
            CalDavConfiguration.validate(account);
            ZoneId zone = timeZoneProvider.getTimeZone();
            URI uri = CalDavUris.canonicalize(CalDavConfiguration.validate(config, account));
            ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), zone);
            CalendarWindow horizon = CalDavConfiguration.horizon(account, zone, now);
            CalDavConfiguration.validate(CalendarWindow.from(config, zone, now), horizon);
            String identity = identity(account, uri, zone);
            Restored restored = readCache(identity, uri, horizon, zone, config);
            synchronized (lifecycle) {
                if (!active || current != generation || synchronizedOnce) {
                    return;
                }
                if (!zone.equals(timeZoneProvider.getTimeZone())) {
                    zoneChanged();
                    return;
                }
                cacheIdentity = identity;
                applyCache(restored, zone, config);
            }
        } catch (IllegalArgumentException e) {
            failed(current, () -> true, e);
        }
    }

    private void set(String channel, State state) {
        states.put(channel, state);
        updateState(channel, state);
    }

    private void clearEvents() {
        events = List.of();
        loaded = false;
        synchronizedOnce = false;
        synchronizationFailure = null;
        for (String field : List.of("json", "count", "truncated", "range-start", "range-end")) {
            set("events#" + field, UnDefType.UNDEF);
        }
        set("sync#last", UnDefType.UNDEF);
        set("current#active", OnOffType.OFF);
        publishEvent("current", null, timeZoneProvider.getTimeZone());
        publishEvent("next", null, timeZoneProvider.getTimeZone());
    }

    private record Persisted(int version, String identity, CalendarSynchronizer.Snapshot snapshot, String lastSync) {
    }

    private void save(CalendarSynchronizer.Snapshot snapshot) {
        String last = states.getOrDefault("sync#last", UnDefType.UNDEF).toString();
        storage.put(getThing().getUID().toString(),
                gson.toJson(new Persisted(CACHE_VERSION, cacheIdentity, snapshot, last)));
    }

    private record Restored(List<CalendarEvent> events, CalendarWindow horizon, State lastSync) {
    }

    private @Nullable Restored readCache(String identity, URI collection, CalendarWindow horizon, ZoneId zone,
            CalendarConfiguration config) {
        try {
            String raw = storage.get(getThing().getUID().toString());
            if (raw == null || raw.length() > 16 * 1024 * 1024) {
                return null;
            }
            Persisted persisted = Objects.requireNonNull(gson.fromJson(raw, Persisted.class));
            if (persisted.version() != CACHE_VERSION || !identity.equals(persisted.identity())) {
                return null;
            }
            CalendarSynchronizer.validateSnapshot(persisted.snapshot(), collection);
            var parsed = CalendarSynchronizer.expand(persisted.snapshot(), horizon, zone, config.includeCancelled);
            String[] boundaries = persisted.snapshot().horizon().split("/", 3);
            CalendarWindow storedHorizon = new CalendarWindow(Instant.parse(boundaries[0]).atZone(zone),
                    Instant.parse(boundaries[1]).atZone(zone));
            State lastSync = "UNDEF".equals(persisted.lastSync()) || "NULL".equals(persisted.lastSync())
                    ? UnDefType.UNDEF
                    : new DateTimeType(persisted.lastSync());
            return new Restored(parsed.events(), storedHorizon, lastSync);
        } catch (RuntimeException e) {
            // Persisted data is an optional recovery source; a server sync replaces an unusable cache.
            return null;
        }
    }

    private void applyCache(@Nullable Restored restored, ZoneId zone, CalendarConfiguration config) {
        if (restored == null) {
            return;
        }
        events = restored.events();
        loaded = true;
        cachedHorizon = restored.horizon();
        cachedZone = zone;
        set("sync#last", restored.lastSync());
        publish(config, zone, ZonedDateTime.ofInstant(clock.instant(), zone));
        scheduleClock(generation);
    }

    @Override
    public void handleRemoval() {
        dispose();
        storage.remove(getThing().getUID().toString());
        super.handleRemoval();
    }

    @Override
    public void dispose() {
        synchronized (lifecycle) {
            active = false;
            generation++;
            ScheduledFuture<?> previous = clockJob;
            if (previous != null) {
                previous.cancel(true);
                clockJob = null;
            }
            synchronizer = null;
            transport = null;
            configuration = null;
        }
    }
}
