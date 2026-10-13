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

import static org.openhab.binding.caldav.internal.CalDavBindingConstants.PROPERTY_CALENDAR_IDENTITY;

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
import org.openhab.binding.caldav.internal.CalendarIdentity;
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
import org.openhab.core.thing.ThingUID;
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
 * @author Andreas Vilippus - Calendar color metadata channel
 * @author Andreas Vilippus - Localized Thing status descriptions
 * @author Andreas Vilippus - Restored synchronization state reuse
 * @author Andreas Vilippus - Configuration-derived calendar representation
 * @author Andreas Vilippus - Generation-bound identity publication and account revalidation
 * @author Andreas Vilippus - Retry failed calendar representation persistence
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
    private boolean initializing;
    private boolean identityPublicationPending;
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

    /**
     * Creates a calendar handler with persistent cache and local event projection services.
     *
     * @param thing calendar or account Thing to handle
     * @param timeZoneProvider provider of the configured openHAB time zone
     * @param storage calendar cache storage
     */
    public CalendarHandler(Thing thing, TimeZoneProvider timeZoneProvider, Storage<String> storage) {
        this(thing, timeZoneProvider, storage, Clock.systemUTC(), null);
    }

    /**
     * Creates a calendar handler with persistent cache and local event projection services.
     *
     * @param thing calendar or account Thing to handle
     * @param timeZoneProvider provider of the configured openHAB time zone
     * @param storage calendar cache storage
     * @param clock clock used for event and range calculations
     * @param clockScheduler scheduler for local event updates, or null to use the handler scheduler
     */
    CalendarHandler(Thing thing, TimeZoneProvider timeZoneProvider, Storage<String> storage, Clock clock,
            @Nullable ScheduledExecutorService clockScheduler) {
        super(thing);
        this.timeZoneProvider = timeZoneProvider;
        this.storage = storage;
        this.clock = clock;
        this.clockScheduler = clockScheduler == null ? scheduler : clockScheduler;
    }

    /** Validates the calendar representation and prepares local cache recovery before live synchronization. */
    @Override
    public void initialize() {
        dispose();
        CalendarConfiguration configured;
        long current;
        ThingUID bridgeUID;
        synchronized (lifecycle) {
            current = ++generation;
            initializing = true;
            bridgeUID = getThing().getBridgeUID();
            clearEvents();
            publishCalendarColor();
            cacheIdentity = "";
            Configuration captured = new Configuration(getConfig().getProperties());
            try {
                CalDavConfiguration.validateIntegerValues(captured, "rangeStartOffset", "rangeEndOffset", "maxEvents");
                configuration = captured.as(CalendarConfiguration.class);
            } catch (IllegalArgumentException e) {
                var failure = CalDavErrors.calendar(e, false);
                set("sync#status", new StringType("ERROR"));
                set("sync#error", new StringType(failure.description()));
                updateStatus(ThingStatus.OFFLINE, failure.detail(), failure.statusDescription());
            }
            configured = configuration;
        }
        Bridge bridge = getBridge();
        AccountHandler handler = bridge != null && bridge.getHandler() instanceof AccountHandler account ? account
                : null;
        long accountGeneration = handler == null ? 0 : handler.contextGeneration();
        BooleanSupplier accountValid = () -> handler == null || handler.contextGeneration() == accountGeneration;
        AccountConfiguration account = null;
        if (configured != null && bridge != null) {
            try {
                if (handler != null) {
                    account = handler.configuration();
                } else {
                    Configuration captured = new Configuration(bridge.getConfiguration());
                    CalDavConfiguration.validateIntegerValues(captured, "refreshInterval", "requestTimeout",
                            "maxPastDays", "maxFutureDays");
                    account = captured.as(AccountConfiguration.class);
                }
            } catch (IllegalArgumentException e) {
                // An invalid account context must remove the previous representation, too.
            }
        }
        if (prepareIdentity(current, bridgeUID, configured, account, accountValid, configured != null)
                && handler != null && account != null) {
            AccountConfiguration captured = account;
            clockScheduler.execute(() -> {
                if (accountValid.getAsBoolean()) {
                    restoreAtStartup(captured, current);
                }
            });
            handler.requestSync();
        }
    }

    /**
     * Revalidates this calendar's parsed configuration before discovery uses the new account context.
     *
     * @return whether the current context was applied, including a locally invalid calendar with no identity
     */
    boolean accountConfigurationChanged(ThingUID bridgeUID, @Nullable AccountConfiguration account,
            BooleanSupplier accountValid) {
        long current;
        @Nullable
        CalendarConfiguration config;
        synchronized (lifecycle) {
            if (!bridgeUID.equals(getThing().getBridgeUID()) || !accountValid.getAsBoolean()) {
                return false;
            }
            CalendarConfiguration configured = configuration;
            if (configured == null && !(initializing && identityPublicationPending)) {
                return !initializing && !identityPublicationPending && getThing().getStatus() == ThingStatus.OFFLINE
                        && getThing().getStatusInfo().getStatusDetail() == ThingStatusDetail.CONFIGURATION_ERROR
                        && !getThing().getProperties().containsKey(PROPERTY_CALENDAR_IDENTITY);
            }
            current = ++generation;
            config = configured;
            active = false;
            initializing = true;
            ScheduledFuture<?> previous = clockJob;
            if (previous != null) {
                previous.cancel(false);
                clockJob = null;
            }
        }
        prepareIdentity(current, bridgeUID, config, account, accountValid, config != null);
        synchronized (lifecycle) {
            return currentContext(current, bridgeUID, accountValid) && !initializing;
        }
    }

    private boolean prepareIdentity(long current, @Nullable ThingUID bridgeUID, @Nullable CalendarConfiguration config,
            @Nullable AccountConfiguration account, BooleanSupplier accountValid, boolean activate) {
        String identity = null;
        IllegalArgumentException error = null;
        try {
            if (bridgeUID == null || config == null || account == null) {
                throw new IllegalArgumentException("Calendar requires a valid account bridge configuration");
            }
            CalDavConfiguration.validate(account);
            URI uri = CalDavConfiguration.validate(config, account);
            ZoneId zone = timeZoneProvider.getTimeZone();
            ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), zone);
            CalDavConfiguration.validate(CalendarWindow.from(config, zone, now),
                    CalDavConfiguration.horizon(account, zone, now));
            identity = CalendarIdentity.create(bridgeUID, URI.create(account.url), uri);
        } catch (IllegalArgumentException e) {
            error = e;
        }
        synchronized (lifecycle) {
            if (!currentContext(current, bridgeUID, accountValid)) {
                return false;
            }
            updateCalendarIdentity(identity);
            // Core property callbacks are synchronous. Reentrant disposal must not reactivate this generation.
            if (!currentContext(current, bridgeUID, accountValid)) {
                return false;
            }
            initializing = false;
            active = activate && identity != null;
            if (activate) {
                if (error != null) {
                    configurationFailed(error);
                } else {
                    set("sync#status", new StringType("SYNCING"));
                    set("sync#error", new StringType(""));
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/status.calendar.waiting");
                }
            }
            return active && currentContext(current, bridgeUID, accountValid);
        }
    }

    private boolean currentContext(long current, @Nullable ThingUID bridgeUID, BooleanSupplier accountValid) {
        return generation == current && Objects.equals(bridgeUID, getThing().getBridgeUID())
                && accountValid.getAsBoolean();
    }

    private void updateCalendarIdentity(@Nullable String identity) {
        // BaseThingHandler's property callback marks self-updates; Core does not reinitialize this handler for them.
        // Publish under the lifecycle lock so no competing generation can overwrite the property before activation.
        boolean changed = !Objects.equals(identity, getThing().getProperties().get(PROPERTY_CALENDAR_IDENTITY));
        if (!changed && !identityPublicationPending) {
            return;
        }
        long current = generation;
        identityPublicationPending = true;
        var callback = getCallback();
        if (callback == null) {
            throw new IllegalStateException("Calendar handler callback is unavailable");
        }
        if (changed) {
            updateProperty(PROPERTY_CALENDAR_IDENTITY, identity);
        } else {
            // Core changes the in-memory property before invoking the callback; retry a failed provider publication.
            callback.thingUpdated(getThing());
        }
        if (current == generation) {
            identityPublicationPending = false;
        }
    }

    /**
     * Reflects an account bridge failure without replacing a calendar-specific diagnosis.
     *
     * @param info updated account bridge status
     */
    @Override
    public void bridgeStatusChanged(ThingStatusInfo info) {
        if (info.getStatus() != ThingStatus.ONLINE && info.getStatus() != ThingStatus.UNKNOWN) {
            bridgeConnectionFailed();
        }
    }

    /** Publishes bridge-offline state while retaining calendar-specific errors and usable events. */
    public void bridgeConnectionFailed() {
        bridgeConnectionFailed(() -> true);
    }

    /**
     * Publishes bridge-offline state while retaining calendar-specific errors and usable events.
     *
     * @param accountValid nonblocking check that the captured account context still permits publication
     */
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
                updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.BRIDGE_OFFLINE,
                        "@text/status.calendar.bridge-offline");
            } else {
                set("sync#error", new StringType(failure.description()));
                updateStatus(ThingStatus.OFFLINE, failure.detail(), failure.statusDescription());
            }
        }
    }

    /**
     * Republishes local channel state for REFRESH without issuing calendar requests.
     *
     * @param channel channel receiving the command or state
     * @param command command received from the framework
     */
    @Override
    public void handleCommand(ChannelUID channel, Command command) {
        if (command != RefreshType.REFRESH) {
            return;
        }
        synchronized (lifecycle) {
            if (!active) {
                return;
            }
            if ("calendar-color".equals(channel.getId())) {
                publishCalendarColor();
            } else {
                updateState(channel, states.getOrDefault(channel.getId(), UnDefType.UNDEF));
            }
        }
    }

    /**
     * Synchronizes and publishes a calendar snapshot only while its calendar and account contexts remain current.
     *
     * @param client transport belonging to the current account session
     * @param account validated account settings captured for this synchronization
     * @param accountValid nonblocking check that the captured account context still permits publication
     * @throws InterruptedException if the account work is interrupted
     */
    public void synchronize(DavTransport client, AccountConfiguration account, BooleanSupplier accountValid)
            throws InterruptedException {
        long current;
        CalendarConfiguration config;
        ThingUID bridgeUID;
        synchronized (lifecycle) {
            CalendarConfiguration configured = configuration;
            if (initializing || configured == null) {
                return;
            }
            current = generation;
            config = configured;
            bridgeUID = getThing().getBridgeUID();
        }
        boolean configurationValidated = false;
        try {
            CalDavConfiguration.validate(account);
            ZoneId zone = timeZoneProvider.getTimeZone();
            ZonedDateTime now = ZonedDateTime.ofInstant(clock.instant(), zone);
            URI uri = CalDavUris.canonicalize(CalDavConfiguration.validate(config, account));
            CalendarWindow horizon = CalDavConfiguration.horizon(account, zone, now);
            CalDavConfiguration.validate(CalendarWindow.from(config, zone, now), horizon);
            if (bridgeUID == null) {
                throw new IllegalArgumentException("Calendar requires an account bridge");
            }
            String representation = CalendarIdentity.create(bridgeUID, URI.create(account.url), uri);
            synchronized (lifecycle) {
                if (!currentContext(current, bridgeUID, accountValid)) {
                    return;
                }
                updateCalendarIdentity(representation);
                if (!currentContext(current, bridgeUID, accountValid)) {
                    return;
                }
                active = true;
            }
            configurationValidated = true;
            String identity = identity(account, uri, zone);
            boolean restoreNeeded;
            synchronized (lifecycle) {
                restoreNeeded = !identity.equals(cacheIdentity) || synchronizer == null || !client.equals(transport)
                        || !uri.equals(collection);
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
                    synchronizer = restored == null ? new CalendarSynchronizer(client, uri)
                            : new CalendarSynchronizer(client, uri, restored.snapshot());
                    transport = client;
                    collection = uri;
                }
                worker = Objects.requireNonNull(synchronizer);
                set("sync#status", new StringType("SYNCING"));
                set("sync#error", new StringType(""));
                if (!synchronizedOnce) {
                    updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/status.calendar.fetching");
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
            if (!configurationValidated) {
                synchronized (lifecycle) {
                    if (currentContext(current, bridgeUID, accountValid)) {
                        active = false;
                        updateCalendarIdentity(null);
                        if (currentContext(current, bridgeUID, accountValid)) {
                            configurationFailed(e);
                        }
                    }
                }
            } else {
                failed(current, accountValid, e);
            }
        }
    }

    private void configurationFailed(Exception error) {
        var failure = CalDavErrors.calendar(error, synchronizedOnce);
        synchronizationFailure = failure;
        set("sync#status", new StringType("ERROR"));
        set("sync#error", new StringType(failure.description()));
        updateStatus(ThingStatus.OFFLINE, failure.detail(), failure.statusDescription());
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
            updateStatus(ThingStatus.OFFLINE, failure.detail(), failure.statusDescription());
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

    private void publishCalendarColor() {
        String raw = getThing().getProperties().get("calendarColor");
        if (raw == null || raw.codePoints().anyMatch(Character::isISOControl)) {
            set("calendar-color", UnDefType.UNDEF);
            return;
        }
        String color = raw.strip();
        set("calendar-color", color.isEmpty() ? UnDefType.UNDEF : new StringType(color));
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

    /** Versioned calendar resources and last successful synchronization retained across restarts. */
    private record Persisted(int version, String identity, CalendarSynchronizer.Snapshot snapshot, String lastSync) {
    }

    private void save(CalendarSynchronizer.Snapshot snapshot) {
        String last = states.getOrDefault("sync#last", UnDefType.UNDEF).toString();
        storage.put(getThing().getUID().toString(),
                gson.toJson(new Persisted(CACHE_VERSION, cacheIdentity, snapshot, last)));
    }

    /** Validated persisted resources with their event projection, coverage and last successful synchronization. */
    private record Restored(List<CalendarEvent> events, CalendarWindow horizon, State lastSync,
            CalendarSynchronizer.Snapshot snapshot) {
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
            return new Restored(parsed.events(), storedHorizon, lastSync, persisted.snapshot());
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

    /** Disposes the calendar and removes its persistent cache when the Thing is deleted. */
    @Override
    public void handleRemoval() {
        dispose();
        storage.remove(getThing().getUID().toString());
        super.handleRemoval();
    }

    /** Invalidates calendar work and cancels local update jobs before releasing synchronization references. */
    @Override
    public void dispose() {
        synchronized (lifecycle) {
            active = false;
            initializing = false;
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
