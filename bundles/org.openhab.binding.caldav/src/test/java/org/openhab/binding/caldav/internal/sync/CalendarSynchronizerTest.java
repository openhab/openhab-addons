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
import java.util.HashMap;
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
 * @author Andreas Vilippus - Cross-horizon synchronization and rollback tests
 */
@NonNullByDefault
@Timeout(30)
class CalendarSynchronizerTest {
    private static final URI URI_CALENDAR = URI.create("https://example.org/calendar/");
    private static final CalendarWindow WINDOW = new CalendarWindow(
            LocalDate.of(2026, 9, 1).atStartOfDay(ZoneOffset.UTC),
            LocalDate.of(2026, 10, 1).atStartOfDay(ZoneOffset.UTC));
    private static final String ICS = "BEGIN:VCALENDAR\nVERSION:2.0\nBEGIN:VEVENT\nUID:one\nDTSTART:20260918T090000Z\nDURATION:PT1H\nEND:VEVENT\nEND:VCALENDAR";
    private static final CalendarWindow SHIFTED_WINDOW = new CalendarWindow(WINDOW.start().plusDays(1),
            WINDOW.end().plusDays(1));

    private static String horizonKey(CalendarWindow window) {
        return window.start().toInstant() + "/" + window.end().toInstant() + "/" + ZoneOffset.UTC;
    }

    private static CalendarSynchronizer.Snapshot cachedSnapshot() {
        return new CalendarSynchronizer.Snapshot(horizonKey(WINDOW), "T1",
                Map.of("a.ics", new CalendarSynchronizer.CachedResource("a", ICS.replace("UID:one", "UID:a")), "b.ics",
                        new CalendarSynchronizer.CachedResource("b", ICS.replace("UID:one", "UID:b")), "c.ics",
                        new CalendarSynchronizer.CachedResource("c", ICS.replace("UID:one", "UID:c"))));
    }

    private static String requestedToken(Request request) throws Exception {
        return CalDavXml.parse(request.body()).getElementsByTagNameNS("DAV:", "sync-token").item(0).getTextContent();
    }

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

    /** Recorded DAV request used to verify resource URLs, report bodies and synchronization depth. */
    private record Request(String method, URI uri, String body, String depth) {
    }

    /** Scripted DAV transport that records requests and consumes queued responses or failures. */
    private static final class Server implements DavTransport {
        /** Scripted DAV responses or failures consumed in request order. */
        final Deque<Object> replies = new ArrayDeque<>();
        /** Recorded DAV requests in execution order. */
        final List<Request> requests = new ArrayList<>();
        /** Number of DAV requests processed by the scripted transport. */
        int calls;

        /**
         * Records the DAV request and consumes the next scripted response or failure.
         *
         * @param method DAV or HTTP request method
         * @param uri request target URI
         * @param body request body
         * @param depth requested WebDAV depth
         * @return the next scripted DAV response
         */
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

    /** Checks that collection synchronization uses the protocol-required report depth and sync level. */
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

    /** Checks interoperability with a server that rejects synchronization reports using the wrong depth. */
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

    /** Checks that the initial collection snapshot is committed only after every response page is processed. */
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

    /** Checks that paged incremental changes publish resource additions and deletions atomically. */
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

    /** Checks that a failed continuation page preserves the original resource map and sync token. */
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

    /** Checks that a non-advancing continuation token fails without committing partial state. */
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

    /** Checks that cyclic continuation tokens are rejected without replacing the previous snapshot. */
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

    /** Checks that the cumulative resource-change limit applies across all response pages. */
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

    /** Checks that changing continuation tokens cannot bypass the maximum page count. */
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

    /** Checks that an incomplete calendar query cannot replace a complete resource cache. */
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

    /** Checks that a sync response without its required token causes no downloads or cache replacement. */
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

    /** Checks that a later resource failure rolls back earlier successful downloads from the same attempt. */
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

    /** Checks that invalid-token recovery cannot repeatedly restart the same synchronization attempt. */
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

    /** Checks that AUTO reaches full synchronization only when both incremental report strategies are unsupported. */
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

    /** Checks that authentication and transient failures cannot select a less efficient synchronization strategy. */
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

    /** Checks that event download errors do not masquerade as unsupported synchronization reports. */
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

    /** Checks that a later report recovers from a resource changing between listing and download. */
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

    /** Checks that incremental collection state applies changed tokens and explicit resource deletion. */
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

    /** Checks that failed resource download preserves the committed token and usable cache. */
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

    /** Checks invalid-token reset and AUTO fallback from unsupported collection synchronization. */
    @Test
    void invalidTokenRestartsAndUnsupportedSyncFallsBack() throws Exception {
        Server server = new Server();
        server.replies.add(response("old", member("stale", "a", "")));
        server.replies.add(ICS.replace("UID:one", "UID:stale"));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        server.replies.add(new CalDavHttpException("REPORT", 403, true));
        server.replies.add(response("new", ""));
        assertEquals("new", sync.synchronize(WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false).snapshot().token());
        assertEquals("old", requestedToken(server.requests.get(2)));
        assertEquals("", requestedToken(server.requests.get(3)));
        assertTrue(sync.snapshot().resources().isEmpty());
        server.replies.add(new CalDavHttpException("REPORT", 405));
        server.replies.add(response("", member("a", "")));
        server.replies.add(ICS);
        assertEquals(1, sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false).events().size());
    }

    /** Checks that authentication errors remain failures instead of triggering another report strategy. */
    @Test
    void authenticationErrorDoesNotTriggerFallback() {
        Server server = new Server();
        server.replies.add(new CalDavHttpException("REPORT", 403));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        assertThrows(CalDavHttpException.class, () -> sync.synchronize(WINDOW, ZoneOffset.UTC, "AUTO", false));
        assertEquals(1, server.calls);
    }

    /** Checks that matching ETags reuse resource data while malformed individual events produce a partial result. */
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

    /** Checks that failed resource properties cannot silently remove previously committed calendar data. */
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

    /** Checks that the structured unsupported-report precondition permits AUTO fallback. */
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

    /** Checks that horizon changes reproject unchanged ETag resources without downloading them again. */
    @Test
    void changedHorizonReusesEtagResourceAndReprojectsEvents() throws Exception {
        Server server = new Server();
        server.replies.add(response("", member("a", "")));
        server.replies.add(ICS.replace("20260918T090000Z", "20261001T090000Z"));
        server.replies.add(response("", member("a", "")));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR);
        assertTrue(sync.synchronize(WINDOW, ZoneOffset.UTC, "ETAG", false).events().isEmpty());
        var result = sync.synchronize(SHIFTED_WINDOW, ZoneOffset.UTC, "ETAG", false);
        assertEquals(List.of("one"), result.events().stream().map(event -> event.uid()).toList());
        assertEquals(List.of("REPORT", "GET", "REPORT"), server.requests.stream().map(Request::method).toList());
        assertEquals(horizonKey(SHIFTED_WINDOW), result.snapshot().horizon());
        assertTrue(server.replies.isEmpty());
    }

    /** Checks that a horizon rollover retains incremental collection state while updating the event projection. */
    @Test
    void syncTokenRolloverKeepsCollectionStateAndReprojectsEvents() throws Exception {
        Server server = new Server();
        var resources = new HashMap<>(cachedSnapshot().resources());
        resources.put("a.ics", new CalendarSynchronizer.CachedResource("a",
                ICS.replace("UID:one", "UID:a").replace("20260918T090000Z", "20260901T090000Z")));
        var before = new CalendarSynchronizer.Snapshot(horizonKey(WINDOW), "T1", resources);
        assertEquals(3, CalendarSynchronizer.expand(before, WINDOW, ZoneOffset.UTC, false).events().size());
        server.replies.add(response("T2", ""));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR, before);
        var result = sync.synchronize(SHIFTED_WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        assertEquals("T1", requestedToken(server.requests.getFirst()));
        assertEquals(List.of("REPORT"), server.requests.stream().map(Request::method).toList());
        assertEquals(before.resources(), result.snapshot().resources());
        assertEquals("T2", result.snapshot().token());
        assertEquals(horizonKey(SHIFTED_WINDOW), result.snapshot().horizon());
        assertEquals(Set.of("b", "c"),
                result.events().stream().map(event -> event.uid()).collect(java.util.stream.Collectors.toSet()));
    }

    /** Checks that a newly visible event is projected from cached resources without another download. */
    @Test
    void syncTokenRolloverRevealsAnUnchangedFutureEvent() throws Exception {
        Server server = new Server();
        String future = ICS.replace("20260918T090000Z", "20261001T090000Z");
        var before = new CalendarSynchronizer.Snapshot(horizonKey(WINDOW), "T1",
                Map.of("one.ics", new CalendarSynchronizer.CachedResource("a", future)));
        assertTrue(CalendarSynchronizer.expand(before, WINDOW, ZoneOffset.UTC, false).events().isEmpty());
        server.replies.add(response("T2", ""));
        var result = new CalendarSynchronizer(server, URI_CALENDAR, before).synchronize(SHIFTED_WINDOW, ZoneOffset.UTC,
                "SYNC_TOKEN", false);
        assertEquals("T1", requestedToken(server.requests.getFirst()));
        assertEquals(List.of("REPORT"), server.requests.stream().map(Request::method).toList());
        assertEquals(List.of("one"), result.events().stream().map(event -> event.uid()).toList());
        assertEquals(before.resources(), result.snapshot().resources());
    }

    /** Checks that incremental synchronization after rollover downloads only changed collection resources. */
    @Test
    void syncTokenRolloverDownloadsOnlyTheChangedResource() throws Exception {
        Server server = new Server();
        var before = cachedSnapshot();
        String changed = ICS.replace("UID:one", "UID:updated-b");
        server.replies.add(response("T2", member("b", "changed", "")));
        server.replies.add(changed);
        var result = new CalendarSynchronizer(server, URI_CALENDAR, before).synchronize(SHIFTED_WINDOW, ZoneOffset.UTC,
                "SYNC_TOKEN", false);
        assertEquals("T1", requestedToken(server.requests.getFirst()));
        assertEquals(List.of("REPORT", "GET"), server.requests.stream().map(Request::method).toList());
        assertEquals(URI_CALENDAR.resolve("b.ics"), server.requests.getLast().uri());
        assertEquals(Set.of("a.ics", "b.ics", "c.ics"), result.snapshot().resources().keySet());
        assertEquals(before.resources().get("a.ics"), result.snapshot().resources().get("a.ics"));
        assertEquals(before.resources().get("c.ics"), result.snapshot().resources().get("c.ics"));
        assertEquals(new CalendarSynchronizer.CachedResource("changed", changed),
                result.snapshot().resources().get("b.ics"));
        assertEquals("T2", result.snapshot().token());
        assertEquals(Set.of("a", "updated-b", "c"),
                result.events().stream().map(event -> event.uid()).collect(java.util.stream.Collectors.toSet()));
    }

    /** Checks that rollover synchronization applies deletions to both cached resources and projected events. */
    @Test
    void syncTokenRolloverRemovesDeletedResourcesAndEvents() throws Exception {
        Server server = new Server();
        server.replies.add(response("T2", "<d:response><d:href>/calendar/c.ics</d:href>"
                + "<d:status>HTTP/1.1 404 Not Found</d:status></d:response>"));
        var result = new CalendarSynchronizer(server, URI_CALENDAR, cachedSnapshot()).synchronize(SHIFTED_WINDOW,
                ZoneOffset.UTC, "SYNC_TOKEN", false);
        assertEquals("T1", requestedToken(server.requests.getFirst()));
        assertEquals(List.of("REPORT"), server.requests.stream().map(Request::method).toList());
        assertEquals(Set.of("a.ics", "b.ics"), result.snapshot().resources().keySet());
        assertEquals(Set.of("a", "b"),
                result.events().stream().map(event -> event.uid()).collect(java.util.stream.Collectors.toSet()));
        assertEquals("T2", result.snapshot().token());
    }

    /** Checks that invalid-token recovery after rollover rebuilds collection membership without stale entries. */
    @Test
    void invalidTokenAfterRolloverRebuildsWithoutStaleResources() throws Exception {
        Server server = new Server();
        server.replies.add(new CalDavHttpException("REPORT", 403, true));
        server.replies.add(response("T2", member("d", "d", "")));
        server.replies.add(ICS.replace("UID:one", "UID:d"));
        var result = new CalendarSynchronizer(server, URI_CALENDAR, cachedSnapshot()).synchronize(SHIFTED_WINDOW,
                ZoneOffset.UTC, "SYNC_TOKEN", false);
        assertEquals("T1", requestedToken(server.requests.getFirst()));
        assertEquals("", requestedToken(server.requests.get(1)));
        assertEquals(List.of("REPORT", "REPORT", "GET"), server.requests.stream().map(Request::method).toList());
        assertEquals(Set.of("d.ics"), result.snapshot().resources().keySet());
        assertEquals(List.of("d"), result.events().stream().map(event -> event.uid()).toList());
        assertEquals("T2", result.snapshot().token());
    }

    /** Checks that ETag reuse after rollover is limited to resources listed by the current horizon query. */
    @Test
    void etagRolloverReusesOnlyResourcesInTheNewQuery() throws Exception {
        Server server = new Server();
        var before = cachedSnapshot();
        server.replies.add(response("", member("b", "b", "") + member("c", "c", "") + member("d", "d", "")));
        server.replies.add(ICS.replace("UID:one", "UID:d"));
        var result = new CalendarSynchronizer(server, URI_CALENDAR, before).synchronize(SHIFTED_WINDOW, ZoneOffset.UTC,
                "ETAG", false);
        assertEquals(List.of("REPORT", "GET"), server.requests.stream().map(Request::method).toList());
        assertEquals(URI_CALENDAR.resolve("d.ics"), server.requests.getLast().uri());
        assertTrue(server.requests.getFirst().body().contains("start=\"20260902T000000Z\""));
        assertTrue(server.requests.getFirst().body().contains("end=\"20261002T000000Z\""));
        assertEquals(Set.of("b.ics", "c.ics", "d.ics"), result.snapshot().resources().keySet());
        assertEquals(before.resources().get("b.ics"), result.snapshot().resources().get("b.ics"));
        assertEquals(before.resources().get("c.ics"), result.snapshot().resources().get("c.ics"));
        assertEquals("", result.snapshot().token());
        assertEquals(horizonKey(SHIFTED_WINDOW), result.snapshot().horizon());
    }

    /** Checks that changed overlapping resources are downloaded while matching ETags remain reusable. */
    @Test
    void etagRolloverDownloadsChangedOverlapOnly() throws Exception {
        Server server = new Server();
        var before = cachedSnapshot();
        String changed = ICS.replace("UID:one", "UID:updated-b");
        server.replies.add(response("", member("b", "changed", "") + member("c", "c", "")));
        server.replies.add(changed);
        var result = new CalendarSynchronizer(server, URI_CALENDAR, before).synchronize(SHIFTED_WINDOW, ZoneOffset.UTC,
                "ETAG", false);
        assertEquals(List.of("REPORT", "GET"), server.requests.stream().map(Request::method).toList());
        assertEquals(URI_CALENDAR.resolve("b.ics"), server.requests.getLast().uri());
        assertEquals(Set.of("b.ics", "c.ics"), result.snapshot().resources().keySet());
        assertEquals(new CalendarSynchronizer.CachedResource("changed", changed),
                result.snapshot().resources().get("b.ics"));
        assertEquals(before.resources().get("c.ics"), result.snapshot().resources().get("c.ics"));
    }

    /** Checks that full synchronization uses current query membership rather than the previous resource map. */
    @Test
    void fullRolloverRebuildsFromTheCurrentQuery() throws Exception {
        Server server = new Server();
        String changed = ICS.replace("UID:one", "UID:updated-b");
        server.replies.add(response("", member("b", "b", changed) + member("d", "d", ICS.replace("UID:one", "UID:d"))));
        var result = new CalendarSynchronizer(server, URI_CALENDAR, cachedSnapshot()).synchronize(SHIFTED_WINDOW,
                ZoneOffset.UTC, "FULL", false);
        assertEquals(List.of("REPORT"), server.requests.stream().map(Request::method).toList());
        assertTrue(server.requests.getFirst().body().contains("<c:calendar-data/>"));
        assertEquals(Set.of("b.ics", "d.ics"), result.snapshot().resources().keySet());
        assertEquals(changed, Objects.requireNonNull(result.snapshot().resources().get("b.ics")).data());
        assertEquals("", result.snapshot().token());
    }

    /**
     * Checks that failed rollover paging preserves the original resources and token despite pending changes and
     * deletions.
     */
    @Test
    void rolloverPagingFailureRollsBackChangesDeletionsAndToken() throws Exception {
        Server server = new Server();
        var before = cachedSnapshot();
        Gson gson = new Gson();
        String persisted = gson.toJson(before);
        server.replies.add(truncated("middle", member("b", "changed", "")
                + "<d:response><d:href>/calendar/c.ics</d:href><d:status>HTTP/1.1 404 Not Found</d:status></d:response>"));
        server.replies.add(ICS.replace("UID:one", "UID:updated-b"));
        server.replies.add(response("T2", member("d", "d", "")));
        server.replies.add(new IOException("offline"));
        var sync = new CalendarSynchronizer(server, URI_CALENDAR, before);
        assertThrows(IOException.class, () -> sync.synchronize(SHIFTED_WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false));
        assertEquals("T1", requestedToken(server.requests.getFirst()));
        assertEquals("middle", requestedToken(server.requests.get(2)));
        assertSame(before, sync.snapshot());
        assertEquals(persisted, gson.toJson(sync.snapshot()));
        server.replies.add(response("recovered", ""));
        var recovered = sync.synchronize(SHIFTED_WINDOW, ZoneOffset.UTC, "SYNC_TOKEN", false);
        assertEquals("T1", requestedToken(server.requests.getLast()));
        assertEquals(before.resources(), recovered.snapshot().resources());
        assertEquals("recovered", recovered.snapshot().token());
        assertEquals(horizonKey(SHIFTED_WINDOW), recovered.snapshot().horizon());
    }

    /** Checks that JSON-restored collection state remains usable for a new event horizon. */
    @Test
    void persistedTokenAndResourcesSurviveARestartIntoANewHorizon() throws Exception {
        Gson gson = new Gson();
        var restored = Objects
                .requireNonNull(gson.fromJson(gson.toJson(cachedSnapshot()), CalendarSynchronizer.Snapshot.class));
        Server server = new Server();
        server.replies.add(response("T2", ""));
        var result = new CalendarSynchronizer(server, URI_CALENDAR, restored).synchronize(SHIFTED_WINDOW,
                ZoneOffset.UTC, "SYNC_TOKEN", false);
        assertEquals("T1", requestedToken(server.requests.getFirst()));
        assertEquals(List.of("REPORT"), server.requests.stream().map(Request::method).toList());
        assertEquals(restored.resources(), result.snapshot().resources());
        assertEquals("T2", result.snapshot().token());
        assertEquals(horizonKey(SHIFTED_WINDOW), result.snapshot().horizon());
        assertEquals(3, result.events().size());
    }

    /** Checks that an empty token rebuilds full collection state instead of inheriting partial query membership. */
    @Test
    void emptyTokenRebuildsInsteadOfInheritingAQueryBoundedCache() throws Exception {
        var old = cachedSnapshot();
        var before = new CalendarSynchronizer.Snapshot(old.horizon(), "", old.resources());
        Server server = new Server();
        server.replies.add(response("T2", member("d", "d", "")));
        server.replies.add(ICS.replace("UID:one", "UID:d"));
        var result = new CalendarSynchronizer(server, URI_CALENDAR, before).synchronize(SHIFTED_WINDOW, ZoneOffset.UTC,
                "SYNC_TOKEN", false);
        assertEquals("", requestedToken(server.requests.getFirst()));
        assertEquals(List.of("REPORT", "GET"), server.requests.stream().map(Request::method).toList());
        assertEquals(Set.of("d.ics"), result.snapshot().resources().keySet());
        assertEquals(List.of("d"), result.events().stream().map(event -> event.uid()).toList());
    }

    /** Checks that AUTO fallback after rollover reuses matching resources from the current ETag query. */
    @Test
    void autoRolloverFallsBackToEtagWithOverlapReuse() throws Exception {
        Server server = new Server();
        server.replies.add(new CalDavHttpException("REPORT", 405));
        server.replies.add(response("", member("b", "b", "") + member("c", "c", "") + member("d", "d", "")));
        server.replies.add(ICS.replace("UID:one", "UID:d"));
        var result = new CalendarSynchronizer(server, URI_CALENDAR, cachedSnapshot()).synchronize(SHIFTED_WINDOW,
                ZoneOffset.UTC, "AUTO", false);
        assertEquals("T1", requestedToken(server.requests.getFirst()));
        assertEquals(List.of("REPORT", "REPORT", "GET"), server.requests.stream().map(Request::method).toList());
        assertEquals(URI_CALENDAR.resolve("d.ics"), server.requests.getLast().uri());
        assertEquals(Set.of("b.ics", "c.ics", "d.ics"), result.snapshot().resources().keySet());
        assertEquals("", result.snapshot().token());
    }

    /** Checks that full fallback after rollover cannot retain resources absent from the new query. */
    @Test
    void autoRolloverFallsBackToFullWithoutOldResources() throws Exception {
        Server server = new Server();
        server.replies.add(new CalDavHttpException("REPORT", 405));
        server.replies.add(new CalDavHttpException("REPORT", 501));
        server.replies.add(response("", member("d", "d", ICS.replace("UID:one", "UID:d"))));
        var result = new CalendarSynchronizer(server, URI_CALENDAR, cachedSnapshot()).synchronize(SHIFTED_WINDOW,
                ZoneOffset.UTC, "AUTO", false);
        assertEquals("T1", requestedToken(server.requests.getFirst()));
        assertEquals(List.of("REPORT", "REPORT", "REPORT"), server.requests.stream().map(Request::method).toList());
        assertFalse(server.requests.get(1).body().contains("<c:calendar-data/>"));
        assertTrue(server.requests.getLast().body().contains("<c:calendar-data/>"));
        assertEquals(Set.of("d.ics"), result.snapshot().resources().keySet());
        assertEquals("", result.snapshot().token());
    }

    /**
     * Checks that an event deleted during download remains a synchronization failure rather than a missing collection.
     */
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

    /** Checks lossless compact resource references across full, ETag and absolute download requests. */
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

    /** Checks that similar path prefixes cannot produce an incorrect collection-relative resource reference. */
    @Test
    void similarCollectionPrefixUsesOriginRelativeFallback() throws Exception {
        Server server = new Server();
        server.replies.add(response("", hrefMember("/cal/abc/event.ics", "a", ICS)));
        var sync = new CalendarSynchronizer(server, URI.create("https://example.org/cal/a/"));
        var result = sync.synchronize(WINDOW, ZoneOffset.UTC, "FULL", false);
        assertEquals(Set.of("/cal/abc/event.ics"), result.snapshot().resources().keySet());
    }

    /** Checks that significant URI path, escape and query distinctions retain separate resource keys. */
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

    /** Checks that full responses reconcile updates and deletions even when resource href spelling changes. */
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

    /** Checks that canonical href aliases preserve ETag reuse and subsequent resource updates or removal. */
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

    /** Checks that identical canonical aliases produce one cached resource and event set. */
    @Test
    void fullDeduplicatesIdenticalAliasesWithoutDuplicatingEvents() throws Exception {
        Server server = new Server();
        server.replies.add(response("", hrefMember("one.ics", "a", ICS) + hrefMember("./one.ics", "a", ICS)
                + hrefMember("https://EXAMPLE.org:443/calendar/one.ics", "a", ICS)));
        var result = new CalendarSynchronizer(server, URI_CALENDAR).synchronize(WINDOW, ZoneOffset.UTC, "FULL", false);
        assertEquals(Set.of("one.ics"), result.snapshot().resources().keySet());
        assertEquals(1, result.events().size());
    }

    /** Checks that conflicting canonical aliases reject the entire response before resource downloads begin. */
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

    /** Checks incremental resources outside the collection path using absolute downloads and canonical deletions. */
    @Test
    void syncTokenHandlesResourcesOutsideCollectionWithAbsoluteGetsAndAliasDeletion() throws Exception {
        checkSyncResourceReferences("SYNC_TOKEN");
    }

    /** Checks AUTO synchronization of external-path resources with lossless references and canonical deletions. */
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

    /** Checks that incremental href aliases consistently add, update and delete the same compact resource key. */
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

    /** Checks that later continuation pages supersede earlier resource state without changing canonical keys. */
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

    /** Checks that unsafe or ambiguous server references leave the committed collection snapshot untouched. */
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

    /** Checks deterministic compact JSON keys and equivalent event expansion after snapshot restoration. */
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
        for (String field : List.of("horizon", "token")) {
            var incomplete = JsonParser.parseString(json).getAsJsonObject();
            incomplete.remove(field);
            var invalid = Objects.requireNonNull(gson.fromJson(incomplete, CalendarSynchronizer.Snapshot.class));
            assertThrows(RuntimeException.class, () -> new CalendarSynchronizer(server, URI_CALENDAR, invalid));
        }
        for (String field : List.of("etag", "data")) {
            var incomplete = JsonParser.parseString(json).getAsJsonObject();
            incomplete.getAsJsonObject("resources").getAsJsonObject("a.ics").remove(field);
            var invalid = Objects.requireNonNull(gson.fromJson(incomplete, CalendarSynchronizer.Snapshot.class));
            assertThrows(RuntimeException.class, () -> new CalendarSynchronizer(server, URI_CALENDAR, invalid));
        }
    }

    /** Checks that persisted snapshots reject noncanonical, unsafe or ambiguous resource references. */
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
            assertThrows(IllegalArgumentException.class,
                    () -> new CalendarSynchronizer(new Server(), URI_CALENDAR, snapshot), reference);
        }
    }
}
