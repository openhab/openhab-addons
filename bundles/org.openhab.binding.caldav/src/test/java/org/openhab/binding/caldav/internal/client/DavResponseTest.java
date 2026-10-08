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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Structured DAV response and collection truncation tests.
 *
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Collection synchronization truncation tests
 * @author Andreas Vilippus - Canonical collection marker regressions
 */
@NonNullByDefault
@Timeout(30)
class DavResponseTest {
    @Test
    void collectionTruncationMarkerIsNotAMemberResource() throws Exception {
        String response = "<d:multistatus xmlns:d=\"DAV:\"><d:response><d:href>/calendar/</d:href>"
                + "<d:status>HTTP/1.1 507 Insufficient Storage</d:status>"
                + "<d:error><d:number-of-matches-within-limits/></d:error></d:response>"
                + "<d:sync-token>middle</d:sync-token></d:multistatus>";
        var parsed = DavResponse.parse(response, URI.create("https://example.org/calendar/"));
        assertEquals(0, parsed.resources().size());
        assertEquals("middle", parsed.token());
        assertTrue(parsed.truncated());
    }

    @Test
    void collectionTruncationDoesNotRequireOptionalErrorElement() throws Exception {
        String response = "<d:multistatus xmlns:d=\"DAV:\"><d:response><d:href>/calendar/</d:href>"
                + "<d:status>HTTP/1.1 507 Insufficient Storage</d:status></d:response>"
                + "<d:sync-token>middle</d:sync-token></d:multistatus>";
        var parsed = DavResponse.parse(response, URI.create("https://example.org/calendar/"));
        assertEquals(0, parsed.resources().size());
        assertTrue(parsed.truncated());
    }

    @Test
    void failedMemberIsNotMistakenForCollectionTruncation() throws Exception {
        String response = "<d:multistatus xmlns:d=\"DAV:\"><d:response><d:href>/calendar/one.ics</d:href>"
                + "<d:status>HTTP/1.1 507 Insufficient Storage</d:status>"
                + "<d:error><d:number-of-matches-within-limits/></d:error></d:response></d:multistatus>";
        var parsed = DavResponse.parse(response, URI.create("https://example.org/calendar/"));
        assertEquals(1, parsed.resources().size());
        assertEquals(507, parsed.resources().getFirst().status());
        assertFalse(parsed.truncated());
    }

    @Test
    void resourceLimitExcludesCollectionTruncationMarker() throws Exception {
        StringBuilder response = new StringBuilder("<d:multistatus xmlns:d=\"DAV:\">");
        for (int member = 0; member < DavResponse.MAX_RESOURCES; member++) {
            response.append("<d:response><d:href>/calendar/").append(member)
                    .append(".ics</d:href><d:status>HTTP/1.1 404 Not Found</d:status></d:response>");
        }
        String marker = "<d:response><d:href>/calendar/</d:href>"
                + "<d:status>HTTP/1.1 507 Insufficient Storage</d:status></d:response>";
        var parsed = DavResponse.parse(response + marker + "</d:multistatus>",
                URI.create("https://example.org/calendar/"));
        assertEquals(DavResponse.MAX_RESOURCES, parsed.resources().size());
        assertTrue(parsed.truncated());
        response.append("<d:response><d:href>/calendar/extra.ics</d:href>"
                + "<d:status>HTTP/1.1 404 Not Found</d:status></d:response>");
        assertThrows(IOException.class,
                () -> DavResponse.parse(response + "</d:multistatus>", URI.create("https://example.org/calendar/")));
    }

    @Test
    void extractsEventsFromNamespacedMultistatusResponse() throws Exception {
        String xml = "<?xml version=\"1.0\"?><d:multistatus xmlns:d=\"DAV:\" "
                + "xmlns:c=\"urn:ietf:params:xml:ns:caldav\"><d:response><d:href>/one.ics</d:href><d:propstat><d:prop>"
                + "<c:calendar-data>BEGIN:VCALENDAR\nBEGIN:VEVENT\nUID:one\n"
                + "DTSTART:20260916T080000Z\nDTEND:20260916T090000Z\nSUMMARY:One\n"
                + "END:VEVENT\nEND:VCALENDAR</c:calendar-data></d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>";

        var events = DavResponse.parse(xml, URI.create("https://example.org/calendar/"));

        assertEquals(1, events.resources().size());
        assertEquals("https://example.org/one.ics", events.resources().getFirst().href());
    }

    @Test
    void rejectsExternalEntities() {
        String xml = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                + "<d:response><c:calendar-data>&xxe;</c:calendar-data></d:response></d:multistatus>";

        assertThrows(Exception.class, () -> DavResponse.parse(xml, URI.create("https://example.org/calendar/")));
    }

    @Test
    void collectionAliasesAreRecognizedAsTruncationAndEmptyCollectionEntries() throws Exception {
        for (String status : new String[] { "200 OK", "507 Insufficient Storage" }) {
            String xml = "<d:multistatus xmlns:d=\"DAV:\"><d:response>"
                    + "<d:href>https://EXAMPLE.org:443/cal%65ndar/./</d:href>" + "<d:status>HTTP/1.1 " + status
                    + "</d:status></d:response>" + "<d:sync-token>middle</d:sync-token></d:multistatus>";
            var parsed = DavResponse.parse(xml, URI.create("https://example.org/calendar/"));
            assertTrue(parsed.resources().isEmpty());
            assertEquals(status.startsWith("507"), parsed.truncated());
        }
    }
}
