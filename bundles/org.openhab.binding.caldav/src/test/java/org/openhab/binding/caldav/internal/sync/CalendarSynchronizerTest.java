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
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.client.CalDavClient;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.client.CalDavXml;
import org.openhab.binding.caldav.internal.client.DavTransport;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.binding.caldav.internal.logic.CalendarWindow;

import com.sun.net.httpserver.HttpServer;

/**
 * Transactional sync and recovery tests without a live CalDAV account.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Bounded transactional sync paging
 */
@NonNullByDefault
@Timeout(30)
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
        return member("one", etag, data);
    }

    private static String member(String name, String etag, String data) {
        return "<d:response><d:href>/calendar/" + name + ".ics</d:href><d:propstat><d:prop><d:getetag>" + etag
                + "</d:getetag>" + (data.isEmpty() ? "" : "<c:calendar-data>" + data + "</c:calendar-data>")
                + "</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>";
    }

    private static String truncated(String token, String members) {
        return response(token,
                members + "<d:response><d:href>/calendar/</d:href>"
                        + "<d:status>HTTP/1.1 507 Insufficient Storage</d:status>"
                        + "<d:error><d:number-of-matches-within-limits/></d:error></d:response>");
    }

    private record Request(String method, URI uri, String body, String depth) {
    }

    private static final class Server implements DavTransport {
        final Deque<Object> replies = new ArrayDeque<>();
        final List<Request> requests = new ArrayList<>();
        int calls;

        @Override
        public String request(String method, URI uri, String body, String depth) throws IOException {
            calls++;
            requests.add(new Request(method, uri, body, depth));
            Object reply = replies.removeFirst();
            if (reply instanceof IOException e) {
                throw e;
            }
            return (String) reply;
        }
    }

    @Test
    void syncReportUsesDepthZeroAndSyncLevelOne() throws Exception {
        Server server = new Server();
        server.replies.add(response("one", ""));
        new CalendarSynchronizer(server, URI_CALENDAR).synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        Request request = server.requests.getFirst();
        assertEquals("REPORT", request.method());
        assertEquals(URI_CALENDAR, request.uri());
        assertEquals("0", request.depth());
        var body = CalDavXml.parse(request.body());
        assertEquals("DAV:", body.getDocumentElement().getNamespaceURI());
        assertEquals("sync-collection", body.getDocumentElement().getLocalName());
        assertEquals("1", body.getElementsByTagNameNS("DAV:", "sync-level").item(0).getTextContent());
        assertEquals("", body.getElementsByTagNameNS("DAV:", "sync-token").item(0).getTextContent());
    }

    @Test
    void syncSucceedsAgainstServerRejectingWrongDepth() throws Exception {
        HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        AtomicInteger rejected = new AtomicInteger();
        server.createContext("/calendar/", exchange -> {
            try (exchange) {
                boolean valid = "REPORT".equals(exchange.getRequestMethod())
                        && "0".equals(exchange.getRequestHeaders().getFirst("Depth"));
                try {
                    var body = CalDavXml
                            .parse(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
                    valid &= "DAV:".equals(body.getDocumentElement().getNamespaceURI())
                            && "sync-collection".equals(body.getDocumentElement().getLocalName())
                            && body.getElementsByTagNameNS("DAV:", "sync-level").getLength() == 1
                            && "1".equals(body.getElementsByTagNameNS("DAV:", "sync-level").item(0).getTextContent());
                } catch (Exception e) {
                    valid = false;
                }
                if (!valid) {
                    rejected.incrementAndGet();
                    exchange.sendResponseHeaders(400, -1);
                    return;
                }
                byte[] result = response("https://example.org/sync/complete", "").getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/xml; charset=UTF-8");
                exchange.sendResponseHeaders(207, result.length);
                exchange.getResponseBody().write(result);
            }
        });
        HttpClient http = new HttpClient();
        try {
            server.start();
            http.start();
            AccountConfiguration configuration = new AccountConfiguration();
            configuration.url = "http://127.0.0.1:" + server.getAddress().getPort() + "/calendar/";
            configuration.requestTimeout = 3;
            URI collection = URI.create(configuration.url);
            CalDavClient client = new CalDavClient(http, configuration);
            assertEquals(400, assertThrows(CalDavHttpException.class, () -> client.request("REPORT", collection,
                    "<d:sync-collection xmlns:d=\"DAV:\">" + "<d:sync-level>1</d:sync-level></d:sync-collection>", "1"))
                    .statusCode());
            var result = new CalendarSynchronizer(client, collection).synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN",
                    false);
            assertEquals("https://example.org/sync/complete", result.snapshot().token());
            assertEquals(1, rejected.get());
        } finally {
            http.stop();
            server.stop(0);
        }
    }

    @Test
    void initialSyncCollectsEveryPageBeforeCommitting() throws Exception {
        Server server = new Server();
        server.replies.add(truncated("middle", member("one", "a", "")));
        server.replies.add(ICS);
        server.replies.add(response("final", member("two", "b", "")));
        server.replies.add(ICS.replace("UID:one", "UID:two"));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        var result = sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        assertEquals(2, result.events().size());
        assertEquals("final", sync.snapshot().token());
        assertEquals(2, sync.snapshot().resources().size());
        var nextRequest = CalDavXml.parse(server.requests.get(2).body());
        assertEquals("middle", nextRequest.getElementsByTagNameNS("DAV:", "sync-token").item(0).getTextContent());
        assertEquals("0", server.requests.get(2).depth());
    }

    @Test
    void incrementalPagingAppliesDeletionAndAdditionTogether() throws Exception {
        Server server = new Server();
        server.replies.add(response("old", member("a", "")));
        server.replies.add(ICS);
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        server.replies.add(truncated("middle", "<d:response><d:href>/calendar/one.ics</d:href>"
                + "<d:status>HTTP/1.1 404 Not Found</d:status></d:response>"));
        server.replies.add(response("final", member("two", "b", "")));
        server.replies.add(ICS.replace("UID:one", "UID:two"));
        var result = sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        assertEquals(List.of("two"), result.events().stream().map(event -> event.uid()).toList());
        assertEquals("final", result.snapshot().token());
        assertFalse(result.snapshot().resources().containsKey(URI_CALENDAR.resolve("one.ics").toString()));
    }

    @Test
    void laterPageFailureRollsBackEarlierDeletionAndToken() throws Exception {
        Server server = new Server();
        server.replies.add(response("old", member("a", "")));
        server.replies.add(ICS);
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        var before = sync.snapshot();
        server.replies.add(truncated("middle", "<d:response><d:href>/calendar/one.ics</d:href>"
                + "<d:status>HTTP/1.1 404 Not Found</d:status></d:response>"));
        CalDavHttpException unavailable = new CalDavHttpException("REPORT", 503);
        server.replies.add(unavailable);
        assertSame(unavailable,
                assertThrows(CalDavHttpException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false)));
        assertSame(before, sync.snapshot());
        assertEquals(4, server.calls);
        server.replies.add(response("recovered", ""));
        assertEquals(1, sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false).events().size());
    }

    @Test
    void repeatedPagingTokenFailsWithoutCommitting() throws Exception {
        Server server = new Server();
        server.replies.add(truncated("middle", ""));
        server.replies.add(truncated("middle", ""));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        var before = sync.snapshot();
        assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false));
        assertEquals(2, server.calls);
        assertSame(before, sync.snapshot());
    }

    @Test
    void cycledPagingTokensFailWithoutCommitting() {
        Server server = new Server();
        server.replies.add(truncated("first", ""));
        server.replies.add(truncated("second", ""));
        server.replies.add(truncated("first", ""));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        var before = sync.snapshot();
        assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false));
        assertEquals(3, server.calls);
        assertSame(before, sync.snapshot());
    }

    @Test
    void cumulativeChangeLimitAcrossPagesKeepsPreviousSnapshot() throws Exception {
        Server server = new Server();
        server.replies.add(response("old", member("a", "")));
        server.replies.add(ICS);
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        var before = sync.snapshot();
        StringBuilder deleted = new StringBuilder();
        for (int member = 0; member < 5000; member++) {
            deleted.append("<d:response><d:href>/calendar/").append(member)
                    .append(".ics</d:href><d:status>HTTP/1.1 404 Not Found</d:status></d:response>");
        }
        server.replies.add(truncated("middle", deleted.toString()));
        server.replies.add(response("final", "<d:response><d:href>/calendar/one.ics</d:href>"
                + "<d:status>HTTP/1.1 404 Not Found</d:status></d:response>"));
        assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false));
        assertSame(before, sync.snapshot());
        assertEquals(4, server.calls);
    }

    @Test
    void endlesslyChangingPagingTokensAreBounded() {
        Server server = new Server();
        for (int page = 0; page < 100; page++) {
            server.replies.add(truncated("page-" + page, ""));
        }
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        var before = sync.snapshot();
        assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false));
        assertEquals(100, server.calls);
        assertSame(before, sync.snapshot());
    }

    @Test
    void truncatedCalendarQueryCannotReplaceCompleteCache() throws Exception {
        for (String mode : List.of("FULL", "ETAG")) {
            Server server = new Server();
            server.replies.add(response("", member("a", ICS)));
            var sync = new CalendarSynchronizer(server, URI_CALENDAR);
            sync.synchronize(WINDOW, ZoneOffset.UTC, "FULL", false);
            var before = sync.snapshot();
            server.replies.add(truncated("middle", ""));
            assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, mode, false));
            assertSame(before, sync.snapshot());
        }
    }

    @Test
    void missingTokenDoesNotTriggerDownloadOrReplaceCache() throws Exception {
        Server server = new Server();
        server.replies.add(response("old", member("a", "")));
        server.replies.add(ICS);
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        var before = sync.snapshot();
        server.replies.add(response("", member("b", "")));
        assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false));
        assertSame(before, sync.snapshot());
        assertEquals(3, server.calls);
    }

    @Test
    void laterDownloadFailureDoesNotCommitSuccessfulEarlierDownload() throws Exception {
        Server server = new Server();
        server.replies.add(response("old", member("a", "")));
        server.replies.add(ICS);
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        var before = sync.snapshot();
        server.replies.add(response("new", member("one", "b", "") + member("two", "c", "")));
        server.replies.add(ICS.replace("UID:one", "UID:changed"));
        server.replies.add(new IOException("offline"));
        assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false));
        assertSame(before, sync.snapshot());
        assertEquals(5, server.calls);
    }

    @Test
    void invalidTokenResetIsOnlyAttemptedOnce() throws Exception {
        Server server = new Server();
        server.replies.add(response("old", member("a", "")));
        server.replies.add(ICS);
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        var before = sync.snapshot();
        server.replies.add(new CalDavHttpException("REPORT", 403, true));
        CalDavHttpException rejectedReset = new CalDavHttpException("REPORT", 403, true);
        server.replies.add(rejectedReset);
        assertSame(rejectedReset,
                assertThrows(CalDavHttpException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false)));
        assertSame(before, sync.snapshot());
        assertEquals(4, server.calls);
        assertEquals("", CalDavXml.parse(server.requests.get(3).body()).getElementsByTagNameNS("DAV:", "sync-token")
                .item(0).getTextContent());
    }

    @Test
    void unsupportedSyncAndEtagReportsFallBackToFull() throws Exception {
        Server server = new Server();
        server.replies.add(new CalDavHttpException("REPORT", 501));
        server.replies.add(new CalDavHttpException("REPORT", 405));
        server.replies.add(response("", member("a", ICS)));
        var result = new CalendarSynchronizer(server, URI_CALENDAR).synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false);
        assertEquals(1, result.events().size());
        assertEquals("", result.snapshot().token());
        assertEquals(3, server.calls);
        assertEquals(0, CalDavXml.parse(server.requests.get(1).body())
                .getElementsByTagNameNS("urn:ietf:params:xml:ns:caldav", "calendar-data").getLength());
        assertEquals(1, CalDavXml.parse(server.requests.get(2).body())
                .getElementsByTagNameNS("urn:ietf:params:xml:ns:caldav", "calendar-data").getLength());
    }

    @Test
    void authenticationAndTemporaryErrorsDoNotTriggerFallbackAtEitherStage() {
        for (int status : List.of(401, 403, 429, 500, 502, 503, 504)) {
            for (boolean etag : List.of(false, true)) {
                Server server = new Server();
                if (etag) {
                    server.replies.add(new CalDavHttpException("REPORT", 405));
                }
                CalDavHttpException failure = new CalDavHttpException("REPORT", status);
                server.replies.add(failure);
                var sync = new CalendarSynchronizer(server, URI_CALENDAR);
                var before = sync.snapshot();
                assertSame(failure, assertThrows(CalDavHttpException.class,
                        () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false)));
                assertSame(before, sync.snapshot());
                assertEquals(etag ? 2 : 1, server.calls);
            }
        }
    }

    @Test
    void unsupportedGetDoesNotTriggerReportFallback() throws Exception {
        for (CalDavHttpException failure : List.of(new CalDavHttpException("GET", 405),
                new CalDavHttpException("GET", 501), new CalDavHttpException("GET", 403, false, true))) {
            Server server = new Server();
            server.replies.add(response("old", member("a", "")));
            server.replies.add(ICS);
            var sync = new CalendarSynchronizer(server, URI_CALENDAR);
            sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false);
            var before = sync.snapshot();
            server.replies.add(response("new", member("b", "")));
            server.replies.add(failure);
            IOException actual = assertThrows(IOException.class,
                    () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false));
            assertSame(failure, actual.getCause());
            assertSame(before, sync.snapshot());
            assertEquals(4, server.calls);
        }
    }

    @Test
    void laterReportReconcilesResourceChangedBetweenReportAndGet() throws Exception {
        Server server = new Server();
        server.replies.add(response("", member("old", "")));
        server.replies.add(ICS.replace("UID:one", "UID:changed"));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        assertEquals("changed", sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false).events().getFirst().uid());
        server.replies.add(response("", member("new", "")));
        server.replies.add(ICS.replace("UID:one", "UID:changed"));
        var result = sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false);
        assertEquals("changed", result.events().getFirst().uid());
        assertEquals("new", java.util.Objects
                .requireNonNull(result.snapshot().resources().get(URI_CALENDAR.resolve("one.ics").toString())).etag());
        assertEquals(4, server.calls);
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
