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
package org.openhab.binding.caldav.internal.sync;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.client.CalendarReport;
import org.openhab.binding.caldav.internal.client.DavResponse;
import org.openhab.binding.caldav.internal.client.DavTransport;
import org.openhab.binding.caldav.internal.logic.CalendarLimitException;
import org.openhab.binding.caldav.internal.logic.CalendarWindow;
import org.openhab.binding.caldav.internal.logic.ICalendarParser;
import org.openhab.binding.caldav.internal.model.CalendarEvent;

/**
 * Transactional resource synchronization, confined to the account worker.
 * 
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
public final class CalendarSynchronizer {
    private final DavTransport transport;
    private final URI collection;
    private Snapshot snapshot = new Snapshot("", "", Map.of());

    public CalendarSynchronizer(DavTransport transport, URI collection) {
        this.transport = transport;
        this.collection = collection;
    }

    public record CachedResource(String etag, String data) {
    }

    public record Snapshot(String horizon, String token, Map<String, CachedResource> resources) {
    }

    public record Result(List<CalendarEvent> events, int failedResources, Snapshot snapshot) {
    }

    public Snapshot snapshot() {
        return snapshot;
    }

    public Result synchronize(CalendarWindow horizon, ZoneId zone, String mode, boolean includeCancelled)
            throws Exception {
        String key = horizon.start().toInstant() + "/" + horizon.end().toInstant() + "/" + zone;
        Snapshot previous = snapshot;
        boolean sameHorizon = previous.horizon().equals(key);
        Snapshot candidate;
        if ("FULL".equals(mode)) {
            candidate = full(horizon, key);
        } else if ("ETAG".equals(mode)) {
            candidate = etag(horizon, key, sameHorizon ? previous.resources() : Map.of());
        } else {
            try {
                candidate = token(key, sameHorizon ? previous : new Snapshot(key, "", Map.of()));
            } catch (CalDavHttpException e) {
                if (e.invalidSyncToken()) {
                    candidate = token(key, new Snapshot(key, "", Map.of()));
                } else if ("AUTO".equals(mode) && unsupported(e)) {
                    try {
                        candidate = etag(horizon, key, sameHorizon ? previous.resources() : Map.of());
                    } catch (CalDavHttpException fallback) {
                        if (!unsupported(fallback)) {
                            throw fallback;
                        }
                        candidate = full(horizon, key);
                    }
                } else {
                    throw e;
                }
            }
        }
        Result parsed = expand(candidate, horizon, zone, includeCancelled);
        snapshot = candidate;
        return parsed;
    }

    private static boolean unsupported(CalDavHttpException e) {
        return e.unsupportedReport();
    }

    private Snapshot full(CalendarWindow horizon, String key) throws Exception {
        DavResponse response = DavResponse.parse(
                transport.request("REPORT", collection, CalendarReport.query(horizon.start(), horizon.end()), "1"),
                collection);
        Map<String, CachedResource> resources = new HashMap<>();
        for (var resource : response.resources()) {
            requireSuccess(resource);
            if (resource.data().isEmpty()) {
                throw new IOException("Calendar data missing from full response");
            }
            put(resources, resource.href(), new CachedResource(resource.etag(), resource.data()));
        }
        return new Snapshot(key, "", Map.copyOf(resources));
    }

    private Snapshot etag(CalendarWindow horizon, String key, Map<String, CachedResource> previous) throws Exception {
        String query = CalendarReport.query(horizon.start(), horizon.end()).replace("<c:calendar-data/>", "");
        DavResponse response = DavResponse.parse(transport.request("REPORT", collection, query, "1"), collection);
        Map<String, CachedResource> resources = new HashMap<>();
        for (var resource : response.resources()) {
            requireSuccess(resource);
            CachedResource old = previous.get(resource.href());
            CachedResource value = old != null && !resource.etag().isEmpty() && resource.etag().equals(old.etag()) ? old
                    : new CachedResource(resource.etag(), download(resource.href()));
            put(resources, resource.href(), value);
        }
        return new Snapshot(key, "", Map.copyOf(resources));
    }

    private Snapshot token(String key, Snapshot previous) throws Exception {
        String body = "<d:sync-collection xmlns:d=\"DAV:\"><d:sync-token>" + escape(previous.token())
                + "</d:sync-token><d:sync-level>1</d:sync-level><d:prop><d:getetag/></d:prop></d:sync-collection>";
        DavResponse response = DavResponse.parse(transport.request("REPORT", collection, body, "1"), collection);
        if (response.token().isEmpty()) {
            throw new IOException("Sync response has no token");
        }
        Map<String, CachedResource> resources = new HashMap<>(previous.resources());
        for (var resource : response.resources()) {
            if (resource.status() == 404) {
                resources.remove(resource.href());
                continue;
            }
            requireSuccess(resource);
            String data = download(resource.href());
            put(resources, resource.href(), new CachedResource(resource.etag(), data));
        }
        return new Snapshot(key, response.token(), Map.copyOf(resources));
    }

    private String download(String href) throws IOException, InterruptedException {
        try {
            return transport.request("GET", URI.create(href), "", "0");
        } catch (CalDavHttpException e) {
            if (e.statusCode() == 404) {
                throw new IOException("Calendar resource changed during synchronization", e);
            }
            throw e;
        }
    }

    public static Result expand(Snapshot snapshot, CalendarWindow horizon, ZoneId zone, boolean includeCancelled) {
        validateCache(snapshot);
        List<CalendarEvent> events = new ArrayList<>();
        int failures = 0;
        for (CachedResource resource : snapshot.resources().values()) {
            try {
                events.addAll(ICalendarParser.parse(resource.data(), horizon, zone, includeCancelled));
                if (events.size() > ICalendarParser.MAX_INSTANCES) {
                    throw new CalendarLimitException("Too many calendar instances");
                }
            } catch (CalendarLimitException e) {
                throw e;
            } catch (IllegalArgumentException e) {
                failures++;
            }
        }
        return new Result(List.copyOf(events), failures, snapshot);
    }

    private static void put(Map<String, CachedResource> resources, String href, CachedResource resource)
            throws IOException {
        if (resource.data().getBytes(StandardCharsets.UTF_8).length > ICalendarParser.MAX_RESOURCE_SIZE) {
            throw new IOException("Calendar resource exceeds limit");
        }
        resources.put(href, resource);
        long bytes = resources.values().stream().mapToLong(value -> value.data().length()).sum();
        if (resources.size() > DavResponse.MAX_RESOURCES || bytes > 8 * 1024 * 1024) {
            throw new IOException("Calendar cache exceeds limit");
        }
    }

    private static void validateCache(Snapshot candidate) {
        long bytes = 0;
        if (candidate.resources().size() > DavResponse.MAX_RESOURCES) {
            throw new CalendarLimitException("Too many calendar resources");
        }
        for (CachedResource resource : candidate.resources().values()) {
            int size = resource.data().getBytes(StandardCharsets.UTF_8).length;
            bytes += size;
            if (size > ICalendarParser.MAX_RESOURCE_SIZE || bytes > 8 * 1024 * 1024) {
                throw new CalendarLimitException("Calendar cache exceeds limit");
            }
        }
    }

    private static void requireSuccess(DavResponse.Resource resource) throws IOException {
        if (resource.status() != 200) {
            throw new IOException("Incomplete calendar resource listing");
        }
    }

    private static String escape(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }
}
