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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.Deque;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.client.DavTransport;
import org.openhab.binding.caldav.internal.logic.CalendarWindow;

/**
 * Transactional sync and recovery tests without a live CalDAV account.
 * 
 * @author Andreas Vilippus - Initial contribution
 */
@NonNullByDefault
class CalendarSynchronizerTest {
    private static final URI URI_CALENDAR = URI.create("https://example.org/calendar/");
    private static final CalendarWindow WINDOW = new CalendarWindow(
            LocalDate.of(2026, 9, 1).atStartOfDay(ZoneOffset.UTC),
            LocalDate.of(2026, 10, 1).atStartOfDay(ZoneOffset.UTC));
    private static final String ICS = "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\nUID:one\nDTSTART:20260918T090000Z\nDURATION:PT1H\nEND:VEVENT\nEND:VCALENDAR";

    private static String response(String token, String members) {
        return "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">" + members
                + (token.isEmpty() ? "" : "<d:sync-token>" + token + "</d:sync-token>") + "</d:multistatus>";
    }

    private static String member(String etag, String data) {
        return "<d:response><d:href>/calendar/one.ics</d:href><d:propstat><d:prop><d:getetag>" + etag + "</d:getetag>"
                + (data.isEmpty() ? "" : "<c:calendar-data>" + data + "</c:calendar-data>")
                + "</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>";
    }

    private static final class Server implements DavTransport {
        final Deque<Object> replies = new ArrayDeque<>();
        int calls;

        @Override
        public String request(String method, URI uri, String body, String depth) throws IOException {
            calls++;
            Object reply = replies.removeFirst();
            if (reply instanceof IOException e) {
                throw e;
            }
            return (String) reply;
        }
    }

    @Test
    void tokenChangesAndExplicitDeletion() throws Exception {
        Server server = new Server();
        server.replies.add(response("one", member("a", "")));
        server.replies.add(ICS);
        server.replies.add(response("two",
                "<d:response><d:href>/calendar/one.ics</d:href><d:status>HTTP/1.1 404 Not Found</d:status></d:response>"));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        assertEquals(1, sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false).events().size());
        assertEquals(0, sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false).events().size());
        assertEquals("two", sync.snapshot().token());
    }

    @Test
    void downloadFailureDoesNotAdvanceTokenOrDeleteCache() throws Exception {
        Server server = new Server();
        server.replies.add(response("one", member("a", "")));
        server.replies.add(ICS);
        server.replies.add(response("two", member("b", "")));
        server.replies.add(new IOException("offline"));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        var before = sync.snapshot();
        assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false));
        assertSame(before, sync.snapshot());
    }

    @Test
    void invalidTokenRestartsAndUnsupportedSyncFallsBack() throws Exception {
        Server server = new Server();
        server.replies.add(new CalDavHttpException("REPORT", 403, true));
        server.replies.add(response("new", ""));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        assertEquals("new", sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false).snapshot().token());
        server.replies.add(new CalDavHttpException("REPORT", 405));
        server.replies.add(response("", member("a", "")));
        server.replies.add(ICS);
        assertEquals(1, sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false).events().size());
    }

    @Test
    void authenticationErrorDoesNotTriggerFallback() {
        Server server = new Server();
        server.replies.add(new CalDavHttpException("REPORT", 403));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        assertThrows(CalDavHttpException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false));
        assertEquals(1, server.calls);
    }

    @Test
    void unchangedEtagAvoidsDownloadAndBrokenResourceIsPartial() throws Exception {
        Server server = new Server();
        server.replies.add(response("", member("a", "")));
        server.replies.add(ICS);
        server.replies.add(response("", member("a", "")));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false);
        assertEquals(1, sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false).events().size());
        assertEquals(3, server.calls);
        server.replies.add(response("", member("b", "invalid")));
        assertEquals(1, sync.synchronize(WINDOW, ZoneOffset.UTC, "FULL", false).failedResources());
    }

    @Test
    void failedPropstatDoesNotDeletePreviouslyPublishedResource() throws Exception {
        Server server = new Server();
        server.replies.add(response("", member("a", ICS)));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "FULL", false);
        var before = sync.snapshot();
        server.replies.add(response("", member("b", ICS).replace("200 OK", "500 Server Error")));
        assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "FULL", false));
        assertSame(before, sync.snapshot());
    }

    @Test
    void explicitUnsupportedReportPreconditionAllowsFallback() throws Exception {
        Server server = new Server();
        server.replies.add(new CalDavHttpException("REPORT", 403, false, true));
        server.replies.add(response("", member("a", "")));
        server.replies.add(ICS);
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        assertEquals(1, sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false).events().size());
        assertEquals(3, server.calls);
    }

    @Test
    void changedHorizonRefetchesEtagResource() throws Exception {
        Server server = new Server();
        server.replies.add(response("", member("a", "")));
        server.replies.add(ICS);
        server.replies.add(response("", member("a", "")));
        server.replies.add(ICS);
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false);
        sync.synchronize(new CalendarWindow(WINDOW.start(), WINDOW.end().plusDays(1)), ZoneOffset.UTC, "ETAG", false);
        assertEquals(4, server.calls);
    }

    @Test
    void resourceDeletedDuringDownloadIsNotReportedAsMissingCollection() {
        Server server = new Server();
        server.replies.add(response("one", member("a", "")));
        server.replies.add(new CalDavHttpException("GET", 404));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        IOException failure = assertThrows(IOException.class,
                () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false));
        assertEquals(IOException.class, failure.getClass());
        assertEquals("", sync.snapshot().token());
    }
}
