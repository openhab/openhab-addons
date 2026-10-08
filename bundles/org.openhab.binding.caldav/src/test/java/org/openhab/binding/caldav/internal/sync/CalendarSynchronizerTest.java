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
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openhab.binding.caldav.internal.client.CalDavClient;
import org.openhab.binding.caldav.internal.client.CalDavHttpException;
import org.openhab.binding.caldav.internal.client.CalDavUris;
import org.openhab.binding.caldav.internal.client.CalDavXml;
import org.openhab.binding.caldav.internal.client.DavTransport;
import org.openhab.binding.caldav.internal.config.AccountConfiguration;
import org.openhab.binding.caldav.internal.logic.CalendarWindow;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpServer;

/**
 * Transactional sync and recovery tests without a live CalDAV account.
 * 
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Bounded transactional sync paging
 * @author Andreas Vilippus - Compact resource synchronization and restore regressions
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
        return hrefMember("/calendar/" + name + ".ics", etag, data);
    }

    private static String hrefMember(String href, String etag, String data) {
        return "<d:response><d:href>" + href.replace("&", "&amp;") + "</d:href><d:propstat><d:prop><d:getetag>" + etag
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
        assertEquals(Set.of("one.ics", "two.ics"), sync.snapshot().resources().keySet());
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
        assertEquals(Set.of("two.ics"), result.snapshot().resources().keySet());
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
        assertEquals("new", java.util.Objects.requireNonNull(result.snapshot().resources().get("one.ics")).etag());
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

    @Test
    void resourceReferencesRoundTripThroughFullEtagAndAbsoluteGet() throws Exception {
        for (var entry : Map.ofEntries(Map.entry("event.ics", "event.ics"), Map.entry("sub/event.ics", "sub/event.ics"),
                Map.entry("event%20one.ics", "event%20one.ics"), Map.entry("sub%2fevent.ics", "sub%2Fevent.ics"),
                Map.entry("event%3f.ics", "event%3F.ics"), Map.entry("sub//event.ics", "sub//event.ics"),
                Map.entry(".//event.ics", ".//event.ics"), Map.entry("Event.ics", "Event.ics"),
                Map.entry("event.ics?x=1&y=%2f", "event.ics?x=1&y=%2F"), Map.entry("event.ics?", "event.ics?"),
                Map.entry("./event:one.ics", "./event:one.ics"), Map.entry("sub/", "sub/"), Map.entry("sub", "sub"),
                Map.entry("/other/event.ics", "/other/event.ics"),
                Map.entry("https://example.org//other/event.ics", "/.//other/event.ics")).entrySet()) {
            Server server = new Server();
            server.replies.add(response("", hrefMember(entry.getKey(), "a", ICS)));
            var sync = new CalendarSynchronizer(server, URI_CALENDAR);
            var full = sync.synchronize(WINDOW, ZoneOffset.UTC, "FULL", false);
            assertEquals(Set.of(entry.getValue()), full.snapshot().resources().keySet(), entry.getKey());
            assertEquals(1, full.events().size());
            CalendarSynchronizer.validateSnapshot(full.snapshot(), URI_CALENDAR);
            URI absolute = CalDavUris.canonicalize(CalDavUris.resolve(URI_CALENDAR, entry.getKey()));
            assertEquals(absolute, CalDavUris.reconstruct(URI_CALENDAR, entry.getValue()));
            assertEquals(absolute, CalDavUris.canonicalize(CalDavUris.resolve(URI_CALENDAR, entry.getValue())));
            server.replies.add(response("", hrefMember(entry.getKey(), "b", "")));
            server.replies.add(ICS);
            sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false);
            Request get = server.requests.getLast();
            assertEquals("GET", get.method());
            assertTrue(get.uri().isAbsolute());
            assertEquals(absolute, get.uri(), entry.getKey());
            assertEquals(Set.of(entry.getValue()), sync.snapshot().resources().keySet());
        }
    }

    @Test
    void similarCollectionPrefixUsesOriginRelativeFallback() throws Exception {
        Server server = new Server();
        server.replies.add(response("", hrefMember("/cal/abc/event.ics", "a", ICS)));
        var sync = new CalendarSynchronizer(server, URI.create("https://example.org/cal/a/"));
        var result = sync.synchronize(WINDOW, ZoneOffset.UTC, "FULL", false);
        assertEquals(Set.of("/cal/abc/event.ics"), result.snapshot().resources().keySet());
    }

    @Test
    void distinctPathsEscapesAndQueriesNeverShareAResourceKey() throws Exception {
        List<String> hrefs = List.of("event.ics", "Event.ics", "event.ics?", "event.ics?x=1", "event.ics?x=2",
                "sub/event.ics", "sub%2Fevent.ics", "sub//event.ics", "sub", "sub/");
        Server server = new Server();
        StringBuilder members = new StringBuilder();
        for (int i = 0; i < hrefs.size(); i++) {
            members.append(hrefMember(hrefs.get(i), "a", ICS.replace("UID:one", "UID:event-" + i)));
        }
        server.replies.add(response("", members.toString()));
        var result = new CalendarSynchronizer(server, URI_CALENDAR).synchronize(WINDOW, ZoneOffset.UTC, "FULL", false);
        assertEquals(Set.copyOf(hrefs), result.snapshot().resources().keySet());
        assertEquals(hrefs.size(), result.events().size());
    }

    @Test
    void fullReconcilesUpdatesAndDeletesWithAliasedHrefs() throws Exception {
        Server server = new Server();
        server.replies.add(response("", member("a", ICS) + member("two", "b", ICS.replace("UID:one", "UID:two"))));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        assertEquals(Set.of("one.ics", "two.ics"),
                sync.synchronize(WINDOW, ZoneOffset.UTC, "FULL", false).snapshot().resources().keySet());
        server.replies.add(response("",
                hrefMember("https://EXAMPLE.org:443/calendar/./one.ics", "c", ICS.replace("UID:one", "UID:updated"))));
        var updated = sync.synchronize(WINDOW, ZoneOffset.UTC, "FULL", false);
        assertEquals(Set.of("one.ics"), updated.snapshot().resources().keySet());
        assertEquals("updated", updated.events().getFirst().uid());
        server.replies.add(response("", ""));
        assertTrue(sync.synchronize(WINDOW, ZoneOffset.UTC, "FULL", false).snapshot().resources().isEmpty());
    }

    @Test
    void etagAliasesReuseUnchangedDataThenUpdateAndRemoveResources() throws Exception {
        Server server = new Server();
        server.replies.add(response("", hrefMember("one.ics", "a", "")));
        server.replies.add(ICS);
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false);
        server.replies.add(response("",
                hrefMember("./one.ics", "a", "") + hrefMember("https://EXAMPLE.org:443/calendar/%6Fne.ics", "a", "")));
        assertEquals(Set.of("one.ics"),
                sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false).snapshot().resources().keySet());
        assertEquals(3, server.calls);
        server.replies.add(response("", hrefMember("https://EXAMPLE.org:443/calendar/one.ics", "b", "")));
        server.replies.add(ICS.replace("UID:one", "UID:updated"));
        assertEquals("updated", sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false).events().getFirst().uid());
        assertEquals(URI_CALENDAR.resolve("one.ics"), server.requests.getLast().uri());
        server.replies.add(response("", ""));
        assertTrue(sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false).snapshot().resources().isEmpty());
        assertEquals(6, server.calls);
    }

    @Test
    void fullDeduplicatesIdenticalAliasesWithoutDuplicatingEvents() throws Exception {
        Server server = new Server();
        server.replies.add(response("", hrefMember("one.ics", "a", ICS) + hrefMember("./one.ics", "a", ICS)
                + hrefMember("https://EXAMPLE.org:443/calendar/one.ics", "a", ICS)));
        var result = new CalendarSynchronizer(server, URI_CALENDAR).synchronize(WINDOW, ZoneOffset.UTC, "FULL", false);
        assertEquals(Set.of("one.ics"), result.snapshot().resources().keySet());
        assertEquals(1, result.events().size());
    }

    @Test
    void conflictingAliasesRejectWholeResponseBeforeAnyDownload() throws Exception {
        for (String mode : List.of("FULL", "ETAG", "SYNC_TOKEN", "AUTO")) {
            for (String conflict : List.of(hrefMember("./one.ics", "b", ICS),
                    hrefMember("./one.ics", "a", ICS.replace("UID:one", "UID:other")), deletion("./one.ics"))) {
                Server server = new Server();
                server.replies.add(response("old", hrefMember("one.ics", "a", ICS)));
                if (!"FULL".equals(mode)) {
                    server.replies.add(ICS);
                }
                var sync = new CalendarSynchronizer(server, URI_CALENDAR);
                sync.synchronize(WINDOW, ZoneOffset.UTC, mode, false);
                var before = sync.snapshot();
                int calls = server.calls;
                server.replies.add(response("new", hrefMember("one.ics", "a", ICS) + conflict));
                assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, mode, false));
                assertSame(before, sync.snapshot());
                assertEquals(calls + 1, server.calls);
            }
        }
    }

    private static String deletion(String href) {
        return "<d:response><d:href>" + href + "</d:href><d:status>HTTP/1.1 404 Not Found</d:status></d:response>";
    }

    @Test
    void syncTokenHandlesResourcesOutsideCollectionWithAbsoluteGetsAndAliasDeletion() throws Exception {
        checkSyncResourceReferences("SYNC_TOKEN");
    }

    @Test
    void autoHandlesResourcesOutsideCollectionWithAbsoluteGetsAndAliasDeletion() throws Exception {
        checkSyncResourceReferences("AUTO");
    }

    private void checkSyncResourceReferences(String mode) throws Exception {
        for (var entry : Map.of("/other/Event%2f.ics?x=1",
                List.of("/other/Event%2F.ics?x=1", "https://example.org/other/Event%2F.ics?x=1"),
                "https://EXAMPLE.org:443//other/event.ics",
                List.of("/.//other/event.ics", "https://example.org//other/event.ics"), "/calendar-other/event.ics",
                List.of("/calendar-other/event.ics", "https://example.org/calendar-other/event.ics"), "./event:one.ics",
                List.of("./event:one.ics", "https://example.org/calendar/event:one.ics")).entrySet()) {
            Server server = new Server();
            server.replies.add(response("one", hrefMember(entry.getKey(), "a", "")));
            server.replies.add(ICS);
            var sync = new CalendarSynchronizer(server, URI_CALENDAR);
            var result = sync.synchronize(WINDOW, ZoneOffset.UTC, mode, false);
            assertEquals(Set.of(entry.getValue().getFirst()), result.snapshot().resources().keySet());
            assertEquals("one", result.events().getFirst().uid());
            assertEquals("one", result.snapshot().token());
            Request get = server.requests.getLast();
            assertEquals("GET", get.method());
            assertEquals(URI.create(entry.getValue().get(1)), get.uri());
            server.replies.add(response("two", deletion(entry.getValue().get(1).replace("&", "&amp;"))));
            var deleted = sync.synchronize(WINDOW, ZoneOffset.UTC, mode, false);
            assertTrue(deleted.snapshot().resources().isEmpty());
            assertTrue(deleted.events().isEmpty());
            assertEquals("two", deleted.snapshot().token());
            assertEquals(3, server.calls);
        }
    }

    @Test
    void syncTokenAliasesAddUpdateAndDeleteTheSameCompactKey() throws Exception {
        Server server = new Server();
        server.replies.add(response("one", hrefMember("one.ics", "a", "")));
        server.replies.add(ICS);
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        server.replies.add(response("two", hrefMember("https://EXAMPLE.org:443/calendar/%6fne.ics", "b", "")));
        server.replies.add(ICS.replace("UID:one", "UID:updated"));
        var updated = sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        assertEquals(Set.of("one.ics"), updated.snapshot().resources().keySet());
        assertEquals("updated", updated.events().getFirst().uid());
        assertEquals(URI_CALENDAR.resolve("one.ics"), server.requests.getLast().uri());
        server.replies.add(response("three", deletion("./one.ics")));
        assertTrue(sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false).snapshot().resources().isEmpty());
        assertEquals("three", sync.snapshot().token());
    }

    @Test
    void continuationPagesApplyLaterResourceStateWithStableKeys() throws Exception {
        Server server = new Server();
        server.replies.add(truncated("middle", hrefMember("one.ics", "a", "")));
        server.replies.add(ICS);
        server.replies.add(response("final",
                hrefMember("https://EXAMPLE.org:443/calendar/one.ics", "b", "") + hrefMember("sub/two.ics", "c", "")));
        server.replies.add(ICS.replace("UID:one", "UID:updated"));
        server.replies.add(ICS.replace("UID:one", "UID:two"));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        var result = sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        assertEquals(Set.of("one.ics", "sub/two.ics"), result.snapshot().resources().keySet());
        assertEquals(Set.of("updated", "two"),
                result.events().stream().map(e -> e.uid()).collect(java.util.stream.Collectors.toSet()));
        assertEquals("final", result.snapshot().token());
        server.replies.add(truncated("deleting", deletion("https://EXAMPLE.org:443/calendar/one.ics")));
        server.replies.add(response("deleted", deletion("./sub/two.ics")));
        assertTrue(sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false).snapshot().resources().isEmpty());
        assertEquals("deleted", sync.snapshot().token());
    }

    @Test
    void unsafeOrAmbiguousServerReferencesNeverAlterThePreviousSnapshot() throws Exception {
        for (String href : List.of("https://other.org/calendar/one.ics", "https://example.org:444/calendar/one.ics",
                "http://example.org/calendar/one.ics", "https://user@example.org/calendar/one.ics", "one.ics#fragment",
                "bad%2.ics", "bad%zz.ics", "%2E/../one.ics")) {
            for (String mode : List.of("FULL", "ETAG", "SYNC_TOKEN", "AUTO")) {
                Server server = new Server();
                server.replies.add(response("old", hrefMember("one.ics", "a", ICS)));
                if (!"FULL".equals(mode)) {
                    server.replies.add(ICS);
                }
                var sync = new CalendarSynchronizer(server, URI_CALENDAR);
                sync.synchronize(WINDOW, ZoneOffset.UTC, mode, false);
                var before = sync.snapshot();
                int calls = server.calls;
                server.replies.add(response("new", hrefMember(href, "b", ICS)));
                assertThrows(IOException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, mode, false), href);
                assertSame(before, sync.snapshot());
                assertEquals(calls + 1, server.calls);
            }
        }
    }

    @Test
    void snapshotJsonHasSortedCompactKeysAndRestoresTheSameEvents() throws Exception {
        Server server = new Server();
        server.replies.add(response("",
                hrefMember("b.ics", "b", ICS.replace("UID:one", "UID:two")) + hrefMember("a.ics", "a", ICS)));
        var result = new CalendarSynchronizer(server, URI_CALENDAR).synchronize(WINDOW, ZoneOffset.UTC, "FULL", false);
        Gson gson = new Gson();
        String json = gson.toJson(result.snapshot());
        assertFalse(json.contains("https://"));
        assertEquals(List.of("a.ics", "b.ics"),
                new ArrayList<>(JsonParser.parseString(json).getAsJsonObject().getAsJsonObject("resources").keySet()));
        var restored = Objects.requireNonNull(gson.fromJson(json, CalendarSynchronizer.Snapshot.class));
        CalendarSynchronizer.validateSnapshot(restored, URI_CALENDAR);
        assertEquals(result.events(), CalendarSynchronizer.expand(restored, WINDOW, ZoneOffset.UTC, false).events());
        assertEquals(json, gson.toJson(restored));
    }

    @Test
    void snapshotRejectsInvalidAbsoluteAliasedAndAmbiguousKeys() {
        for (String reference : List.of("https://other.org/one.ics", "https://example.org:444/one.ics",
                "http://example.org/one.ics", "https://user@example.org/one.ics", "one.ics#fragment", "bad%2.ics",
                "bad%zz.ics", "https://example.org/calendar/one.ics", "./one.ics", "../other.ics", "%6fne.ics",
                "/calendar/one.ics", "%2E/../one.ics", "")) {
            var snapshot = new CalendarSynchronizer.Snapshot("", "",
                    Map.of("valid.ics", new CalendarSynchronizer.CachedResource("a", ICS), reference,
                            new CalendarSynchronizer.CachedResource("b", ICS)));
            assertThrows(IllegalArgumentException.class,
                    () -> CalendarSynchronizer.validateSnapshot(snapshot, URI_CALENDAR), reference);
        }
    }
}
