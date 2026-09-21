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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.openhab.binding.caldav.internal.client.CalDavClient;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.client.CalDavUris;
import org.openhab.binding.caldav.internal.client.CalendarCollection;
import org.openhab.binding.caldav.internal.client.CalendarDiscoveryParser;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.binding.caldav.internal.config.CalDavConfiguration;
import org.openhab.binding.caldav.internal.discovery.CalDavDiscoveryService;
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
 */
@NonNullByDefault
public class AccountHandler extends BaseBridgeHandler {
    private static final String PRINCIPAL = "<d:propfind xmlns:d=\"DAV:\"><d:prop><d:current-user-principal/></d:prop></d:propfind>";
    private static final String HOME = "<d:propfind xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\"><d:prop><c:calendar-home-set/></d:prop></d:propfind>";
    private static final String COLLECTIONS = "<d:propfind xmlns:d=\"DAV:\"><d:prop><d:displayname/><d:resourcetype/></d:prop></d:propfind>";
    private final Logger logger = LoggerFactory.getLogger(AccountHandler.class);
    private final HttpClientFactory httpFactory;
    private final Object lifecycle = new Object();
    private @Nullable Session session;
    private @Nullable ScheduledFuture<?> job;
    private final List<Consumer<List<CalendarCollection>>> discovery = new ArrayList<>();

    private static final class Session {
        final AccountConfiguration configuration;
        final HttpClient http;
        final CalDavClient client;
        volatile boolean valid = true;
        boolean running;
        boolean stopping;
        boolean requested;
        int failures;

        Session(AccountConfiguration configuration, HttpClient http) {
            this.configuration = configuration;
            this.http = http;
            client = new CalDavClient(http, configuration);
        }
    }

    public AccountHandler(Bridge bridge, HttpClientFactory httpFactory) {
        super(bridge);
        this.httpFactory = httpFactory;
    }

    @Override
    public void initialize() {
        dispose();
        AccountConfiguration configuration = getConfigAs(AccountConfiguration.class);
        try {
            CalDavConfiguration.validate(configuration);
            HttpClient http = httpFactory.createHttpClient("caldav",
                    new SslContextFactory.Client(!configuration.verifyCertificate));
            http.setFollowRedirects(false);
            http.setMaxConnectionsPerDestination(1);
            Session next = new Session(configuration, http);
            synchronized (lifecycle) {
                session = next;
                job = scheduler.schedule(() -> poll(next), 0, TimeUnit.SECONDS);
            }
        } catch (IllegalArgumentException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "Invalid account configuration");
        }
    }

    public AccountConfiguration configuration() {
        return getConfigAs(AccountConfiguration.class);
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
            job = scheduler.schedule(() -> poll(current), 0, TimeUnit.SECONDS);
        }
    }

    public void discover(Consumer<List<CalendarCollection>> callback) {
        synchronized (lifecycle) {
            if (session == null) {
                return;
            }
            discovery.add(callback);
        }
        requestSync();
    }

    private void poll(Session current) {
        synchronized (lifecycle) {
            if (!current.equals(session) || !current.valid || current.running) {
                return;
            }
            current.running = true;
        }
        try {
            if (!current.http.isStarted()) {
                current.http.start();
            }
            URI base = URI.create(current.configuration.url);
            List<CalendarCollection> collections;
            if ("DIRECT".equals(current.configuration.discoveryMode)) {
                URI home = current.configuration.calendarHome.isBlank() ? base
                        : CalDavUris.resolve(base, current.configuration.calendarHome);
                collections = CalendarDiscoveryParser
                        .collections(current.client.request("PROPFIND", home, COLLECTIONS, "1"), home);
            } else {
                URI principal = CalendarDiscoveryParser
                        .currentUserPrincipal(current.client.request("PROPFIND", base, PRINCIPAL, "0"), base);
                List<URI> homes = CalendarDiscoveryParser
                        .calendarHomes(current.client.request("PROPFIND", principal, HOME, "0"), principal);
                Map<URI, CalendarCollection> discovered = new LinkedHashMap<>();
                for (URI home : homes) {
                    if (!current.valid || Thread.currentThread().isInterrupted()) {
                        return;
                    }
                    for (CalendarCollection collection : CalendarDiscoveryParser
                            .collections(current.client.request("PROPFIND", home, COLLECTIONS, "1"), home)) {
                        discovered.putIfAbsent(collection.uri(), collection);
                    }
                }
                collections = List.copyOf(discovered.values());
            }
            if (!current.valid) {
                return;
            }
            List<Consumer<List<CalendarCollection>>> callbacks;
            synchronized (lifecycle) {
                if (!current.equals(session)) {
                    return;
                }
                updateStatus(ThingStatus.ONLINE);
                current.failures = 0;
                callbacks = List.copyOf(discovery);
                discovery.clear();
            }
            for (var callback : callbacks) {
                if (!current.valid) {
                    return;
                }
                try {
                    callback.accept(collections);
                } catch (RuntimeException e) {
                    logger.debug("CalDAV discovery callback failed ({})", e.getClass().getSimpleName());
                }
            }
            for (var thing : getThing().getThings()) {
                if (!current.valid || Thread.currentThread().isInterrupted()) {
                    return;
                }
                if (thing.getHandler() instanceof CalendarHandler calendar) {
                    calendar.synchronize(current.client, current.configuration, () -> current.valid);
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IllegalArgumentException e) {
            failed(current, ThingStatusDetail.CONFIGURATION_ERROR, e);
        } catch (CalDavHttpException e) {
            failed(current, e.statusCode() == 401 || e.statusCode() == 403 ? ThingStatusDetail.CONFIGURATION_ERROR
                    : ThingStatusDetail.COMMUNICATION_ERROR, e);
        } catch (Exception e) {
            failed(current, ThingStatusDetail.COMMUNICATION_ERROR, e);
        } finally {
            synchronized (lifecycle) {
                current.running = false;
                if (current.equals(session) && current.valid) {
                    long delay = current.requested && current.failures == 0 ? 0
                            : Math.min(3600L, current.configuration.refreshInterval * (1L << current.failures));
                    current.requested = false;
                    job = scheduler.schedule(() -> poll(current), delay, TimeUnit.SECONDS);
                }
            }
            if (!current.valid) {
                close(current);
            }
        }
    }

    private void failed(Session current, ThingStatusDetail detail, Exception error) {
        synchronized (lifecycle) {
            if (!current.equals(session) || !current.valid) {
                return;
            }
            current.failures = Math.min(6, current.failures + 1);
            updateStatus(ThingStatus.OFFLINE, detail, "CalDAV account connection failed");
            discovery.clear();
        }
        for (var thing : getThing().getThings()) {
            if (!current.valid) {
                return;
            }
            if (thing.getHandler() instanceof CalendarHandler calendar) {
                calendar.bridgeConnectionFailed(() -> current.valid);
            }
        }
        logger.debug("CalDAV account synchronization failed ({})", error.getClass().getSimpleName());
    }

    @Override
    public void dispose() {
        Session previous;
        boolean stopIdle;
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
            discovery.clear();
        }
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
        scheduler.execute(() -> stop(current.http));
    }

    private void stop(HttpClient http) {
        try {
            http.stop();
        } catch (Exception e) {
            logger.debug("CalDAV HTTP client shutdown failed ({})", e.getClass().getSimpleName());
        }
    }
}
