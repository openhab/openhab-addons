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
package org.openhab.binding.caldav.internal.client;

import static org.junit.jupiter.api.Assertions.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TimeZone;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.binding.caldav.internal.logic.CalendarWindow;
import org.openhab.binding.caldav.internal.sync.CalendarSynchronizer;

import com.sun.net.httpserver.HttpServer;

import biweekly.Biweekly;
import biweekly.component.Observance;

/**
 * Tests server-side event filtering and the query time-zone contract.
 *
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Calendar-query time-zone regression tests
 */
@NonNullByDefault
@Timeout(30)
class CalendarReportTest {
    @Test
    void createsUtcCalendarQueryWithHalfOpenRange() {
        ZonedDateTime start = ZonedDateTime.of(2026, 9, 16, 0, 0, 0, 0, ZoneOffset.ofHours(2));
        ZonedDateTime end = start.plusDays(1);

        String query = CalendarReport.query(start, end);

        assertTrue(query.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(query.contains("xmlns:d=\"DAV:\""));
        assertTrue(query.contains("xmlns:c=\"urn:ietf:params:xml:ns:caldav\""));
        assertTrue(query.contains("start=\"20260915T220000Z\""));
        assertTrue(query.contains("end=\"20260916T220000Z\""));
        assertTrue(query.contains("<d:getetag/>") && query.contains("<c:calendar-data/>"));
    }

    @Test
    void queryCarriesOpenhabTimezoneAcrossBothDstTransitions() throws Exception {
        ZoneId zone = ZoneId.of("Europe/Berlin");
        var start = LocalDate.of(2026, 1, 1).atStartOfDay(zone);
        var end = start.plusYears(1);
        var root = CalDavXml.parse(CalendarReport.query(start, end)).getDocumentElement();
        var elements = DavResponse.children(root, "urn:ietf:params:xml:ns:caldav", "timezone");
        assertEquals(1, elements.size());
        var calendar = Biweekly.parse(elements.getFirst().getTextContent()).first();
        assertNotNull(calendar);
        var component = Objects.requireNonNull(calendar.getTimezoneInfo().getTimezoneById(zone.getId())).getComponent();
        assertNotNull(component);
        List<Observance> observances = new ArrayList<>(component.getStandardTimes());
        observances.addAll(component.getDaylightSavingsTime());
        var transitions = new TreeMap<Instant, Long>();
        for (Observance observance : observances) {
            var raw = Objects.requireNonNull(observance.getDateStart()).getValue().getRawComponents();
            assertNotNull(raw);
            Instant transition = LocalDateTime
                    .of(raw.getYear(), raw.getMonth(), raw.getDate(), raw.getHour(), raw.getMinute(), raw.getSecond())
                    .toInstant(ZoneOffset.UTC)
                    .minusMillis(Objects.requireNonNull(observance.getTimezoneOffsetFrom()).getValue().getMillis());
            transitions.put(transition,
                    Objects.requireNonNull(observance.getTimezoneOffsetTo()).getValue().getMillis());
        }
        assertEquals(7200000L, transitions.get(Instant.parse("2026-03-29T01:00:00Z")));
        assertEquals(3600000L, transitions.get(Instant.parse("2026-10-25T01:00:00Z")));
        for (String date : List.of("2026-03-29T00:30:00Z", "2026-03-29T01:30:00Z", "2026-10-25T00:30:00Z",
                "2026-10-25T01:30:00Z")) {
            Instant instant = Instant.parse(date);
            assertEquals(zone.getRules().getOffset(instant).getTotalSeconds() * 1000L,
                    Objects.requireNonNull(transitions.floorEntry(instant)).getValue(), date);
        }
    }

    @Test
    void queryTimezonePreservesHistoricalFloatingMasterDuration() throws Exception {
        var start = LocalDate.of(2026, 7, 7).atStartOfDay(ZoneId.of("Europe/Berlin"));
        var root = CalDavXml.parse(CalendarReport.query(start, start.plusDays(1))).getDocumentElement();
        var zone = Biweekly.parse(
                DavResponse.children(root, "urn:ietf:params:xml:ns:caldav", "timezone").getFirst().getTextContent())
                .first().getTimezoneInfo().getTimezoneById("Europe/Berlin").getComponent();
        List<Observance> observances = new ArrayList<>(zone.getStandardTimes());
        observances.addAll(zone.getDaylightSavingsTime());
        var offsets = new TreeMap<Instant, Long>();
        for (Observance observance : observances) {
            var raw = Objects.requireNonNull(observance.getDateStart()).getValue().getRawComponents();
            assertNotNull(raw);
            Instant transition = LocalDateTime
                    .of(raw.getYear(), raw.getMonth(), raw.getDate(), raw.getHour(), raw.getMinute(), raw.getSecond())
                    .toInstant(ZoneOffset.UTC)
                    .minusMillis(Objects.requireNonNull(observance.getTimezoneOffsetFrom()).getValue().getMillis());
            offsets.put(transition, Objects.requireNonNull(observance.getTimezoneOffsetTo()).getValue().getMillis());
        }
        // A weekly floating master crosses the 2025 autumn transition. Its exact DTEND duration stays 49 hours.
        Instant masterStart = Instant.parse("2025-10-25T21:30:00Z");
        Instant masterEnd = Instant.parse("2025-10-27T22:30:00Z");
        var before = offsets.floorEntry(masterStart);
        var after = offsets.floorEntry(masterEnd);
        long fromOffset = (before == null ? offsets.firstEntry() : before).getValue();
        long toOffset = (after == null ? offsets.firstEntry() : after).getValue();
        long duration = java.time.Duration
                .between(LocalDateTime.of(2025, 10, 25, 23, 30), LocalDateTime.of(2025, 10, 27, 23, 30)).toMillis()
                + fromOffset - toOffset;
        assertEquals(java.time.Duration.ofHours(49).toMillis(), duration);
        var occurrence = LocalDateTime.of(2026, 7, 4, 23, 30).atZone(start.getZone());
        assertEquals(start.plusMinutes(30).toInstant(), occurrence.toInstant().plusMillis(duration));
    }

    @Test
    void floatingAndAllDayCandidatesAreConsistentAcrossAllSyncModes() throws Exception {
        ZoneId zone = ZoneOffset.ofHours(-12);
        var start = LocalDate.of(2026, 10, 5).atStartOfDay(zone);
        var window = new CalendarWindow(start, start.plusDays(1));
        String floating = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nBEGIN:VEVENT\r\nUID:floating\r\n"
                + "DTSTART:20261005T001500\r\nDURATION:PT15M\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n";
        String allDay = "BEGIN:VCALENDAR\r\nVERSION:2.0\r\nBEGIN:VEVENT\r\nUID:all-day\r\n"
                + "DTSTART;VALUE=DATE:20261005\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n";
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/calendar/", exchange -> {
            try (exchange) {
                String response;
                if ("GET".equals(exchange.getRequestMethod())) {
                    response = exchange.getRequestURI().getPath().endsWith("floating.ics") ? floating : allDay;
                } else {
                    try {
                        var root = CalDavXml
                                .parse(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8))
                                .getDocumentElement();
                        boolean token = "sync-collection".equals(root.getLocalName());
                        TimeZone effective = TimeZone.getTimeZone("Pacific/Kiritimati");
                        var timezones = DavResponse.children(root, "urn:ietf:params:xml:ns:caldav", "timezone");
                        if (!timezones.isEmpty()) {
                            var calendar = Biweekly.parse(timezones.getFirst().getTextContent()).first();
                            effective = calendar.getTimezoneInfo().getTimezoneById(zone.getId()).getTimeZone();
                        }
                        // The provider's default is UTC+14: both Oct5 values then end before this UTC-12 horizon.
                        boolean intersects = token
                                || effective.getOffset(start.toInstant().toEpochMilli()) == -43200000;
                        boolean includeData = root
                                .getElementsByTagNameNS("urn:ietf:params:xml:ns:caldav", "calendar-data")
                                .getLength() > 0;
                        response = "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                                + (intersects
                                        ? resource("floating", includeData ? floating : "")
                                                + resource("all-day", includeData ? allDay : "")
                                        : "")
                                + (token ? "<d:sync-token>done</d:sync-token>" : "") + "</d:multistatus>";
                    } catch (Exception e) {
                        exchange.sendResponseHeaders(500, -1);
                        return;
                    }
                }
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=UTF-8");
                exchange.sendResponseHeaders(207, bytes.length);
                exchange.getResponseBody().write(bytes);
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            AccountConfiguration account = new AccountConfiguration();
            account.url = "http://127.0.0.1:" + server.getAddress().getPort() + "/calendar/";
            CalDavClient client = new CalDavClient(http, account);
            for (String mode : List.of("FULL", "ETAG", "SYNC_TOKEN")) {
                var result = new CalendarSynchronizer(client, URI.create(account.url)).synchronize(window, zone, mode,
                        false);
                assertEquals(2, result.events().size(), mode);
                assertEquals(0, result.failedResources());
                assertTrue(result.events().stream().anyMatch(e -> e.allDay() && "all-day".equals(e.uid())));
                assertTrue(result.events().stream().anyMatch(e -> "floating".equals(e.uid())
                        && start.plusMinutes(15).toInstant().equals(Objects.requireNonNull(e.start()).toInstant())));
            }
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    private static String resource(String id, String data) {
        return "<d:response><d:href>/calendar/" + id + ".ics</d:href><d:propstat><d:prop>"
                + "<d:getetag>one</d:getetag>"
                + (data.isEmpty() ? "" : "<c:calendar-data><![CDATA[" + data + "]]></c:calendar-data>")
                + "</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>";
    }
}
