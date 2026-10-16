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

import static org.openhab.binding.caldav.internal.CalDavBindingConstants.BINDING_ID;
import static org.openhab.binding.caldav.internal.CalDavBindingConstants.CALENDAR_THING_TYPE;
import static org.openhab.binding.caldav.internal.CalDavBindingConstants.PROPERTY_CALENDAR_IDENTITY;

import java.io.IOException;
import java.net.URI;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.caldav.internal.CalendarIdentity;
import org.openhab.binding.caldav.internal.client.CalDavClient;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.binding.caldav.internal.client.CalendarDiscoveryParser;
import org.openhab.binding.caldav.internal.client.DavTransport;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.binding.caldav.internal.config.CalDavConfiguration;
import org.openhab.binding.caldav.internal.config.CalendarConfiguration;
import org.openhab.binding.caldav.internal.discovery.CalDavDiscoveryService;
import org.openhab.binding.caldav.internal.logic.CalendarWindow;
import org.openhab.core.config.core.ConfigUtil;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
import org.openhab.core.thing.binding.builder.ThingBuilder;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Serial account worker; stale sessions cannot restart polling or publish connection state.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Coordinated lifecycle and recovery
 * @author Andreas Vilippus - On-demand collection discovery
 * @author Andreas Vilippus - Configured polling and retry scheduling
 * @author Andreas Vilippus - Remote-confirmed recovery and retained discovery work
 * @author Andreas Vilippus - Session-bound discovery cancellation
 * @author Andreas Vilippus - Localized Thing status descriptions
 * @author Andreas Vilippus - Calendar identity revalidation before account discovery
 * @author Andreas Vilippus - Handlerless calendar persistence and isolated child update failures
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler {
    private static final ThingTypeUID CALENDAR_TYPE = new ThingTypeUID(BINDING_ID, CALENDAR_THING_TYPE);
    private static final String PRINCIPAL = "<d:propfind xmlns:d=\"DAV:\"><d:prop><d:current-user-principal/></d:prop></d:propfind>";
    private static final String HOME = "<d:propfind xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\"><d:prop><c:calendar-home-set/></d:prop></d:propfind>";
    private static final String COLLECTIONS = """
            <d:propfind xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav" xmlns:i="http://apple.com/ns/ical/">
              <d:prop><d:displayname/><d:resourcetype/><c:calendar-description/>
                <d:current-user-privilege-set/><i:calendar-color/></d:prop>
            </d:propfind>
            """;
    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private final HttpClientFactory httpFactory;
    private final ThingRegistry thingRegistry;
    private final TimeZoneProvider timeZoneProvider;
    private final ScheduledExecutorService accountScheduler;
    private final Object lifecycle = new Object();
    private volatile long contextGeneration;
    private final AtomicBoolean revalidating = new AtomicBoolean();
    private @Nullable Session session;
    private @Nullable CalDavDiscoveryService discoveryService;
    private @Nullable ScheduledFuture<?> job;
    private final List<PendingDiscovery> discovery = new ArrayList<>();

    /** A queued collection scan with its publication validity check and session cancellation hook. */
    private record PendingDiscovery(Consumer<List<CalendarCollection>> callback, BooleanSupplier valid,
            Runnable cancel) {
    }

    /** Connection settings, transport and polling state owned by one account lifecycle context. */
    private static final class Session {
        /** Validated account settings captured for this session. */
        final AccountConfiguration configuration;
        /** HTTP client created for this account context and closed when its work ends. */
        final HttpClient http;
        /** DAV client using the captured account authentication and request settings. */
        final CalDavClient client;
        /** Stable transport that records remote outcomes while preserving incremental calendar workers. */
        final DavTransport transport;
        /** Whether this session still authorizes work and result publication. */
        volatile boolean valid = true;
        /** Whether an account poll is currently executing. */
        boolean running;
        /** Whether HTTP client shutdown has already been scheduled. */
        boolean stopping;
        /** Whether another synchronization was requested during the current poll. */
        boolean requested;
        /** Consecutive failure count used to calculate bounded polling backoff. */
        int failures;
        /** Whether a request in the current poll successfully contacted the server. */
        boolean remoteSucceeded;
        /** Whether associated calendar representations are current before discovery and synchronization. */
        boolean identitiesReady;
        /** Last communication failure observed during the current poll. */
        @Nullable
        IOException remoteFailure;

        /**
         * Captures validated account settings and creates a stable transport for one polling session.
         *
         * @param configuration captured configuration used by the handler
         * @param http account HTTP client
         */
        Session(AccountConfiguration configuration, HttpClient http) {
            this.configuration = configuration;
            this.http = http;
            client = new CalDavClient(http, configuration);
            // Keep this transport stable across polls so CalendarHandler retains its incremental synchronizer.
            transport = (method, uri, body, depth) -> {
                try {
                    String response = client.request(method, uri, body, depth);
                    remoteSucceeded = true;
                    return response;
                } catch (IOException e) {
                    remoteFailure = e;
                    throw e;
                }
            };
        }
    }

    /**
     * Creates an account worker with Core HTTP, registry and time-zone dependencies.
     *
     * @param bridge associated account bridge
     * @param httpFactory Core factory for account HTTP clients
     * @param thingRegistry registry of configured calendar Things
     * @param timeZoneProvider provider of the configured openHAB time zone
     */
    public AccountHandler(Bridge bridge, HttpClientFactory httpFactory, ThingRegistry thingRegistry,
            TimeZoneProvider timeZoneProvider) {
        this(bridge, httpFactory, thingRegistry, timeZoneProvider, null);
    }

    /**
     * Creates an account worker with Core HTTP, registry and time-zone dependencies.
     *
     * @param bridge associated account bridge
     * @param httpFactory Core factory for account HTTP clients
     * @param thingRegistry registry of configured calendar Things
     * @param timeZoneProvider provider of the configured openHAB time zone
     * @param accountScheduler scheduler for account work, or null to use the handler scheduler
     */
    AccountHandler(Bridge bridge, HttpClientFactory httpFactory, ThingRegistry thingRegistry,
            TimeZoneProvider timeZoneProvider, @Nullable ScheduledExecutorService accountScheduler) {
        super(bridge);
        this.httpFactory = httpFactory;
        this.thingRegistry = thingRegistry;
        this.timeZoneProvider = timeZoneProvider;
        this.accountScheduler = accountScheduler == null ? scheduler : accountScheduler;
    }

    /** Validates account settings and calendar representations before scheduling session work. */
    @Override
    public void initialize() {
        dispose();
        long current = contextGeneration;
        AccountConfiguration configuration;
        try {
            configuration = configuration();
            CalDavConfiguration.validate(configuration);
        } catch (IllegalArgumentException e) {
            revalidateCalendars(null, () -> contextGeneration == current);
            synchronized (lifecycle) {
                if (contextGeneration == current) {
                    var failure = CalDavErrors.account(e);
                    updateStatus(ThingStatus.OFFLINE, failure.detail(), failure.statusDescription());
                }
            }
            return;
        }
        if (contextGeneration != current) {
            return;
        }
        HttpClient http = httpFactory.createHttpClient("caldav",
                new SslContextFactory.Client(!configuration.verifyCertificate));
        http.setFollowRedirects(false);
        http.setMaxConnectionsPerDestination(1);
        Session next = new Session(configuration, http);
        next.identitiesReady = revalidateCalendars(configuration, () -> contextGeneration == current);
        synchronized (lifecycle) {
            if (contextGeneration == current) {
                session = next;
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/status.account.waiting");
                CalDavDiscoveryService service = discoveryService;
                if (next.identitiesReady && service != null) {
                    service.startScan();
                } else {
                    job = accountScheduler.schedule(() -> poll(next), 0, TimeUnit.SECONDS);
                }
                return;
            }
        }
        close(next);
    }

    /**
     * Identifies the account lifecycle context captured by calendar initialization.
     */
    long contextGeneration() {
        return contextGeneration;
    }

    private boolean revalidateCalendars(@Nullable AccountConfiguration configuration, BooleanSupplier accountValid) {
        // A delayed old-context callback must finish before a newer context can release discovery.
        // The CAS guard holds no monitor while Core callbacks execute, including reentrant lifecycle calls.
        if (!revalidating.compareAndSet(false, true)) {
            return false;
        }
        try {
            return accountValid.getAsBoolean() && revalidateCurrentCalendars(configuration, accountValid);
        } finally {
            revalidating.set(false);
            if (!accountValid.getAsBoolean()) {
                requestSync();
            }
        }
    }

    private boolean revalidateCurrentCalendars(@Nullable AccountConfiguration configuration,
            BooleanSupplier accountValid) {
        boolean complete = true;
        ThingUID bridgeUID = getThing().getUID();
        // Local child validation may synchronously publish properties; do not hold the account lifecycle lock.
        for (Thing snapshot : thingRegistry.getAll()) {
            Thing thing = thingRegistry.get(snapshot.getUID());
            if (!associatedCalendar(thing, bridgeUID)) {
                continue;
            }
            Thing current = Objects.requireNonNull(thing);
            var handler = current.getHandler();
            BooleanSupplier childValid = () -> accountValid.getAsBoolean()
                    && thingRegistry.get(current.getUID()) == current // NOPMD CompareObjectsWithEquals: UID equality
                                                                      // accepts replacements
                    && associatedCalendar(current, bridgeUID) && current.getHandler() == handler; // NOPMD
                                                                                                  // CompareObjectsWithEquals:
                                                                                                  // require the
                                                                                                  // captured handler
                                                                                                  // instance
            if (!childValid.getAsBoolean()) {
                complete = false;
                continue;
            }
            try {
                if (handler instanceof CalendarHandler calendar) {
                    boolean handled = calendar.getThing() == current // NOPMD CompareObjectsWithEquals: reject a stale
                                                                     // handler model
                            && calendar.accountConfigurationChanged(bridgeUID, configuration, childValid);
                    complete &= handled || !associatedCalendar(thingRegistry.get(current.getUID()), bridgeUID);
                } else if (handler == null) {
                    complete &= updateHandlerlessIdentity(current, bridgeUID, configuration, childValid);
                } else {
                    complete = false;
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                // Core may reject a child update after concurrent removal. A live child's failure defers discovery.
                complete &= !associatedCalendar(thingRegistry.get(current.getUID()), bridgeUID);
                logger.debug("CalDAV calendar identity publication failed for {} ({})", current.getUID(),
                        e.getClass().getSimpleName());
            }
        }
        return complete && accountValid.getAsBoolean();
    }

    private static boolean associatedCalendar(@Nullable Thing thing, ThingUID bridgeUID) {
        return thing != null && bridgeUID.equals(thing.getBridgeUID()) && CALENDAR_TYPE.equals(thing.getThingTypeUID());
    }

    private boolean updateHandlerlessIdentity(Thing thing, ThingUID bridgeUID, @Nullable AccountConfiguration account,
            BooleanSupplier childValid) {
        String identity = null;
        if (account != null) {
            try {
                Configuration captured = ConfigUtil.resolveVariables(thing.getConfiguration());
                CalDavConfiguration.validateIntegerValues(captured, "rangeStartOffset", "rangeEndOffset", "maxEvents");
                CalendarConfiguration calendar = captured.as(CalendarConfiguration.class);
                URI uri = CalDavConfiguration.validate(calendar, account);
                ZonedDateTime now = ZonedDateTime.now(timeZoneProvider.getTimeZone());
                CalDavConfiguration.validate(CalendarWindow.from(calendar, now.getZone(), now),
                        CalDavConfiguration.horizon(account, now.getZone(), now));
                identity = CalendarIdentity.create(bridgeUID, URI.create(account.url), uri);
            } catch (IllegalArgumentException e) {
                // Invalid local calendar configuration removes only its representation property.
            }
        }
        if (!childValid.getAsBoolean()) {
            return !associatedCalendar(thingRegistry.get(thing.getUID()), bridgeUID);
        }
        if (Objects.equals(identity, thing.getProperties().get(PROPERTY_CALENDAR_IDENTITY))) {
            return true;
        }
        var callback = getCallback();
        if (callback == null) {
            return false;
        }
        Map<String, String> properties = new HashMap<>(thing.getProperties());
        if (identity == null) {
            properties.remove(PROPERTY_CALENDAR_IDENTITY);
        } else {
            properties.put(PROPERTY_CALENDAR_IDENTITY, identity);
        }
        Thing updated = ThingBuilder.create(thing).withProperties(properties).build();
        if (!childValid.getAsBoolean()) {
            return !associatedCalendar(thingRegistry.get(thing.getUID()), bridgeUID);
        }
        // The public Core callback persists through the Thing's provider and marks this as a self-update,
        // avoiding the handler registration/reinitialization triggered by an ordinary Registry.update().
        callback.thingUpdated(updated);
        Thing published = thingRegistry.get(thing.getUID());
        return !associatedCalendar(published, bridgeUID)
                || Objects.requireNonNull(published).getConfiguration().equals(thing.getConfiguration())
                        && Objects.equals(identity, published.getProperties().get(PROPERTY_CALENDAR_IDENTITY));
    }

    /**
     * Captures account configuration and validates exact integer values before DTO conversion.
     *
     * @return the captured account configuration
     */
    public AccountConfiguration configuration() {
        Configuration captured = new Configuration(getConfig().getProperties());
        CalDavConfiguration.validateIntegerValues(captured, "refreshInterval", "requestTimeout", "maxPastDays",
                "maxFutureDays");
        AccountConfiguration configuration = captured.as(AccountConfiguration.class);
        return configuration;
    }

    /**
     * Declares the collection discovery service associated with this account handler.
     *
     * @return the account handler service types
     */
    @Override
    public Set<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(CalDavDiscoveryService.class);
    }

    /**
     * Requests account synchronization when a channel receives REFRESH.
     *
     * @param channelUID channel receiving the command
     * @param command command received from the framework
     */
    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            requestSync();
        }
    }

    /** Requests an immediate poll or coalesces the request with work already running in the current session. */
    public void requestSync() {
        synchronized (lifecycle) {
            Session current = session;
            if (current == null || !current.valid) {
                return;
            }
            if (current.running) {
                current.requested = true;
                return;
            }
            ScheduledFuture<?> previous = job;
            if (previous != null) {
                previous.cancel(false);
            }
            job = accountScheduler.schedule(() -> poll(current), 0, TimeUnit.SECONDS);
        }
    }

    /**
     * Registers initialization discovery and starts it when the current calendar identities are ready.
     *
     * @param service account collection discovery service
     */
    public void registerDiscoveryService(CalDavDiscoveryService service) {
        synchronized (lifecycle) {
            discoveryService = service;
            Session current = session;
            if (current != null && current.valid && current.identitiesReady) {
                service.startScan();
            }
        }
    }

    /**
     * Removes the discovery service if it is still the registered instance.
     *
     * @param service account collection discovery service
     */
    public void unregisterDiscoveryService(CalDavDiscoveryService service) {
        synchronized (lifecycle) {
            if (service.equals(discoveryService)) {
                discoveryService = null;
            }
        }
    }

    /**
     * Queues collection discovery on the account worker for the supplied result consumer.
     *
     * @param callback consumer receiving discovered calendar collections
     */
    public void discover(Consumer<List<CalendarCollection>> callback) {
        discover(callback, () -> true);
    }

    /**
     * Queues discovery until its callback succeeds. The nonblocking validity predicate identifies a live scan.
     */
    public void discover(Consumer<List<CalendarCollection>> callback, BooleanSupplier valid) {
        discover(callback, valid, () -> {
        });
    }

    /**
     * Cancels the associated scan when the account session ends, including callbacks already preparing results.
     * The cancellation hook must target only this scan and must not throw.
     */
    public void discover(Consumer<List<CalendarCollection>> callback, BooleanSupplier valid, Runnable cancel) {
        synchronized (lifecycle) {
            if (session == null) {
                return;
            }
            discovery.add(new PendingDiscovery(callback, valid, cancel));
        }
        requestSync();
    }

    private void poll(Session current) {
        List<PendingDiscovery> callbacks;
        synchronized (lifecycle) {
            if (!current.equals(session) || !current.valid || current.running) {
                return;
            }
            current.running = true;
            current.remoteSucceeded = false;
            current.remoteFailure = null;
            // Requests arriving during this poll stay queued for the next run.
            callbacks = new ArrayList<>(discovery);
        }
        try {
            for (var iterator = callbacks.iterator(); iterator.hasNext();) {
                PendingDiscovery request = iterator.next();
                if (!request.valid().getAsBoolean()) {
                    iterator.remove();
                    synchronized (lifecycle) {
                        if (current.equals(session)) {
                            discovery.remove(request);
                        }
                    }
                }
            }
            if (!current.identitiesReady) {
                if (!revalidateCalendars(current.configuration, () -> current.valid)) {
                    return;
                }
                synchronized (lifecycle) {
                    if (!current.equals(session) || !current.valid) {
                        return;
                    }
                    current.identitiesReady = true;
                    CalDavDiscoveryService service = discoveryService;
                    if (service != null) {
                        service.startScan();
                    }
                }
            }
            if (!current.valid) {
                return;
            }
            if (!current.http.isStarted()) {
                current.http.start();
            }
            List<CalendarCollection> collections = callbacks.isEmpty() ? List.of() : discoverCollections(current);
            if (!current.valid) {
                return;
            }
            for (var request : callbacks) {
                if (!current.valid) {
                    return;
                }
                try {
                    if (request.valid().getAsBoolean()) {
                        request.callback().accept(collections);
                    }
                    synchronized (lifecycle) {
                        if (current.equals(session)) {
                            discovery.remove(request);
                        }
                    }
                } catch (RuntimeException e) {
                    logger.debug("CalDAV discovery callback failed ({})", e.getClass().getSimpleName());
                }
            }
            for (var thing : getThing().getThings()) {
                if (!current.valid || Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (thing.getHandler() instanceof CalendarHandler calendar) {
                    calendar.synchronize(current.transport, current.configuration, () -> current.valid);
                }
            }
            synchronized (lifecycle) {
                if (current.equals(session) && current.valid && current.remoteSucceeded) {
                    updateStatus(ThingStatus.ONLINE);
                    current.failures = 0;
                }
            }
            IOException remoteFailure = current.remoteFailure;
            if (!current.remoteSucceeded && remoteFailure != null) {
                // CalendarHandler already published its own failure; retain its specific detail and cached data.
                failed(current, remoteFailure, false);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            failed(current, e, true);
        } finally {
            synchronized (lifecycle) {
                current.running = false;
                if (current.equals(session) && current.valid) {
                    long interval = current.configuration.refreshInterval;
                    long delay = current.requested && current.failures == 0 ? 0
                            : Math.min(Math.max(3600L, interval), interval * (1L << current.failures));
                    current.requested = false;
                    job = accountScheduler.schedule(() -> poll(current), delay, TimeUnit.SECONDS);
                }
            }
            if (!current.valid) {
                close(current);
            }
        }
    }

    private List<CalendarCollection> discoverCollections(Session current) throws Exception {
        URI base = URI.create(current.configuration.url);
        if ("DIRECT".equals(current.configuration.discoveryMode)) {
            return CalendarDiscoveryParser.collections(current.transport.request("PROPFIND", base, COLLECTIONS, "1"),
                    base);
        }
        URI principal = CalendarDiscoveryParser
                .currentUserPrincipal(current.transport.request("PROPFIND", base, PRINCIPAL, "0"), base);
        List<URI> homes = CalendarDiscoveryParser
                .calendarHomes(current.transport.request("PROPFIND", principal, HOME, "0"), principal);
        Map<URI, CalendarCollection> discovered = new LinkedHashMap<>();
        for (URI home : homes) {
            if (!current.valid || Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }
            for (CalendarCollection collection : CalendarDiscoveryParser
                    .collections(current.transport.request("PROPFIND", home, COLLECTIONS, "1"), home)) {
                discovered.putIfAbsent(collection.uri(), collection);
            }
        }
        return List.copyOf(discovered.values());
    }

    private void failed(Session current, Exception error, boolean notifyCalendars) {
        var failure = CalDavErrors.account(error);
        synchronized (lifecycle) {
            if (!current.equals(session) || !current.valid) {
                return;
            }
            current.failures = Math.min(6, current.failures + 1);
            updateStatus(ThingStatus.OFFLINE, failure.detail(), failure.statusDescription());
        }
        if (notifyCalendars) {
            for (var thing : getThing().getThings()) {
                if (!current.valid) {
                    return;
                }
                if (thing.getHandler() instanceof CalendarHandler calendar) {
                    calendar.bridgeConnectionFailed(() -> current.valid);
                }
            }
        }
        if (error instanceof CalDavHttpException http) {
            logger.debug("CalDAV {} failed with HTTP {}", http.operation(), http.statusCode());
        } else {
            logger.debug("CalDAV account synchronization failed: {}", failure.description());
        }
    }

    /** Invalidates the session, cancels pending work and closes its transport after active work finishes. */
    @Override
    public void dispose() {
        Session previous;
        boolean stopIdle;
        List<PendingDiscovery> pending;
        synchronized (lifecycle) {
            contextGeneration++;
            previous = session;
            stopIdle = previous != null && !previous.running;
            session = null;
            if (previous != null) {
                previous.valid = false;
            }
            ScheduledFuture<?> previousJob = job;
            if (previousJob != null) {
                previousJob.cancel(true);
                job = null;
            }
            pending = List.copyOf(discovery);
            discovery.clear();
        }
        // Scan cancellation waits for publication; do not hold the account lifecycle lock here.
        pending.forEach(request -> request.cancel().run());
        if (previous != null) {
            for (var thing : getThing().getThings()) {
                if (thing.getHandler() instanceof CalendarHandler calendar) {
                    calendar.bridgeConnectionFailed();
                }
            }
        }
        if (previous != null && stopIdle) {
            close(previous);
        }
    }

    private void close(Session current) {
        synchronized (lifecycle) {
            if (current.stopping) {
                return;
            }
            current.stopping = true;
        }
        // Disposal may interrupt the account worker; close Jetty on an un-interrupted scheduler task.
        accountScheduler.execute(() -> stop(current.http));
    }

    private void stop(HttpClient http) {
        try {
            http.stop();
        } catch (Exception e) {
            logger.debug("CalDAV HTTP client shutdown failed ({})", e.getClass().getSimpleName());
        }
    }
}
