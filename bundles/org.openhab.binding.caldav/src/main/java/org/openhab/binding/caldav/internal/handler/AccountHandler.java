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

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.caldav.internal.client.CalDavClient;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.binding.caldav.internal.client.CalendarDiscoveryParser;
import org.openhab.binding.caldav.internal.client.DavTransport;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.binding.caldav.internal.config.CalDavConfiguration;
import org.openhab.binding.caldav.internal.discovery.CalDavDiscoveryService;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.io.net.http.HttpClientFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.thing.binding.ThingHandlerService;
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
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler {
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
    private final ScheduledExecutorService accountScheduler;
    private final Object lifecycle = new Object();
    private @Nullable Session session;
    private @Nullable CalDavDiscoveryService discoveryService;
    private @Nullable ScheduledFuture<?> job;
    private final List<PendingDiscovery> discovery = new ArrayList<>();

    private record PendingDiscovery(Consumer<List<CalendarCollection>> callback, BooleanSupplier valid,
            Runnable cancel) {
    }

    private static final class Session {
        final AccountConfiguration configuration;
        final HttpClient http;
        final CalDavClient client;
        final DavTransport transport;
        volatile boolean valid = true;
        boolean running;
        boolean stopping;
        boolean requested;
        int failures;
        boolean remoteSucceeded;
        @Nullable
        IOException remoteFailure;

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

    public AccountHandler(Bridge bridge, HttpClientFactory httpFactory) {
        super(bridge);
        this.httpFactory = httpFactory;
        this.accountScheduler = scheduler;
    }

    AccountHandler(Bridge bridge, HttpClientFactory httpFactory, ScheduledExecutorService accountScheduler) {
        super(bridge);
        this.httpFactory = httpFactory;
        this.accountScheduler = accountScheduler;
    }

    @Override
    public void initialize() {
        dispose();
        try {
            AccountConfiguration configuration = configuration();
            CalDavConfiguration.validate(configuration);
            HttpClient http = httpFactory.createHttpClient("caldav",
                    new SslContextFactory.Client(!configuration.verifyCertificate));
            http.setFollowRedirects(false);
            http.setMaxConnectionsPerDestination(1);
            Session next = new Session(configuration, http);
            synchronized (lifecycle) {
                session = next;
                updateStatus(ThingStatus.UNKNOWN, ThingStatusDetail.NONE, "@text/status.account.waiting");
                CalDavDiscoveryService service = discoveryService;
                if (service != null) {
                    service.startScan();
                } else {
                    job = accountScheduler.schedule(() -> poll(next), 0, TimeUnit.SECONDS);
                }
            }
        } catch (IllegalArgumentException e) {
            var failure = CalDavErrors.account(e);
            updateStatus(ThingStatus.OFFLINE, failure.detail(), failure.statusDescription());
        }
    }

    public AccountConfiguration configuration() {
        Configuration captured = new Configuration(getConfig().getProperties());
        CalDavConfiguration.validateIntegerValues(captured, "refreshInterval", "requestTimeout", "maxPastDays",
                "maxFutureDays");
        AccountConfiguration configuration = captured.as(AccountConfiguration.class);
        return configuration;
    }

    @Override
    public Set<Class<? extends ThingHandlerService>> getServices() {
        return Set.of(CalDavDiscoveryService.class);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command == RefreshType.REFRESH) {
            requestSync();
        }
    }

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

    public void registerDiscoveryService(CalDavDiscoveryService service) {
        synchronized (lifecycle) {
            discoveryService = service;
            Session current = session;
            if (current != null && current.valid) {
                service.startScan();
            }
        }
    }

    public void unregisterDiscoveryService(CalDavDiscoveryService service) {
        synchronized (lifecycle) {
            if (service.equals(discoveryService)) {
                discoveryService = null;
            }
        }
    }

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

    @Override
    public void dispose() {
        Session previous;
        boolean stopIdle;
        List<PendingDiscovery> pending;
        synchronized (lifecycle) {
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
