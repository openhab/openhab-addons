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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.URI;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault
class CalendarDiscoveryParserTest {
    private static final URI BASE_URI = URI.create("https://caldav.example.test/dav/");

    @Test
    void resolvesPrincipalAndCalendarHome() throws Exception {
        String principal = "<d:multistatus xmlns:d=\"DAV:\"><d:response><d:propstat><d:prop>"
                + "<d:current-user-principal><d:href>/principals/user/</d:href></d:current-user-principal>"
                + "</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>";
        String home = "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                + "<d:response><d:propstat><d:prop><c:calendar-home-set><d:href>/calendars/user/"
                + "</d:href></c:calendar-home-set></d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>";

        assertEquals(URI.create("https://caldav.example.test/principals/user/"),
                CalendarDiscoveryParser.currentUserPrincipal(principal, BASE_URI));
        assertEquals(List.of(URI.create("https://caldav.example.test/calendars/user/")),
                CalendarDiscoveryParser.calendarHomes(home, BASE_URI));
    }

    @Test
    void resolvesAllHomesInServerOrderAndRemovesDuplicates() throws Exception {
        String xml = homeResponse("<d:href> /calendars/user/ </d:href><d:href>shared/</d:href>"
                + "<d:href>https://caldav.example.test/calendars/user/</d:href><d:href> </d:href>");
        assertEquals(
                List.of(URI.create("https://caldav.example.test/calendars/user/"),
                        URI.create("https://caldav.example.test/dav/shared/")),
                CalendarDiscoveryParser.calendarHomes(xml, BASE_URI));
    }

    @Test
    void rejectsForeignHomeEvenAfterValidHome() {
        for (String href : List.of("https://foreign.example/calendars/", "http://caldav.example.test/calendars/",
                "https://caldav.example.test:8443/calendars/", "http://[invalid")) {
            assertThrows(IllegalArgumentException.class, () -> CalendarDiscoveryParser
                    .calendarHomes(homeResponse("<d:href>/valid/</d:href><d:href>" + href + "</d:href>"), BASE_URI));
        }
    }

    @Test
    void rejectsMissingOrBlankHomes() {
        for (String hrefs : List.of("", "<d:href> </d:href>")) {
            assertThrows(IllegalArgumentException.class,
                    () -> CalendarDiscoveryParser.calendarHomes(homeResponse(hrefs), BASE_URI));
        }
    }

    @Test
    void collectsHomesAcrossSuccessfulPropertiesOnly() throws Exception {
        String xml = homeResponse("<d:href>/first/</d:href>").replace("</d:response>", """
                <d:propstat><d:prop><c:calendar-home-set><d:href>/ignored/</d:href></c:calendar-home-set>
                </d:prop><d:status>HTTP/1.1 404 Not Found</d:status></d:propstat></d:response>
                <d:response><d:propstat><d:prop><c:calendar-home-set><d:href>/second/</d:href>
                </c:calendar-home-set></d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>
                """);
        assertEquals(List.of(BASE_URI.resolve("/first/"), BASE_URI.resolve("/second/")),
                CalendarDiscoveryParser.calendarHomes(xml, BASE_URI));
        assertThrows(IllegalArgumentException.class, () -> CalendarDiscoveryParser.calendarHomes(
                homeResponse("<d:href>/ignored/</d:href>").replace("200 OK", "404 Not Found"), BASE_URI));
    }

    private static String homeResponse(String hrefs) {
        return """
                <d:multistatus xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav">
                <d:response><d:propstat><d:prop><c:calendar-home-set>%s</c:calendar-home-set></d:prop>
                <d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>
                """.formatted(hrefs);
    }

    @Test
    void returnsOnlyCalendarCollections() throws Exception {
        String xml = "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                + "<d:response><d:href>private/</d:href><d:propstat><d:prop><d:displayname>Private</d:displayname>"
                + "<d:resourcetype><d:collection/><c:calendar/></d:resourcetype></d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>"
                + "<d:response><d:href>addressbook/</d:href><d:propstat><d:prop><d:displayname>Contacts</d:displayname>"
                + "<d:resourcetype><d:collection/></d:resourcetype></d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>"
                + "</d:multistatus>";

        var collections = CalendarDiscoveryParser.collections(xml, BASE_URI);

        assertEquals(1, collections.size());
        assertEquals(URI.create("https://caldav.example.test/dav/private/"), collections.get(0).uri());
        assertEquals("Private", collections.get(0).name());
    }

    @Test
    void resolvesAbsoluteHrefAndUsesHrefWhenDisplayNameIsMissing() throws Exception {
        String xml = "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                + "<d:response><d:href>https://caldav.example.test/calendars/work/</d:href><d:propstat><d:prop>"
                + "<d:displayname> </d:displayname><d:resourcetype><c:calendar/></d:resourcetype>"
                + "</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>";

        var collections = CalendarDiscoveryParser.collections(xml, BASE_URI);

        assertEquals(URI.create("https://caldav.example.test/calendars/work/"), collections.get(0).uri());
        assertEquals("https://caldav.example.test/calendars/work/", collections.get(0).name());
    }

    @Test
    void rejectsDiscoveryResponsesWithoutRequiredHref() {
        String xml = "<d:multistatus xmlns:d=\"DAV:\"><d:response><d:propstat><d:prop>"
                + "<d:current-user-principal/></d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>";

        assertThrows(IllegalArgumentException.class, () -> CalendarDiscoveryParser.currentUserPrincipal(xml, BASE_URI));
    }

    @Test
    void rejectsExternalEntities() {
        String xml = "<?xml version=\"1.0\"?><!DOCTYPE foo [<!ENTITY xxe SYSTEM \"file:///etc/passwd\">]>"
                + "<d:multistatus xmlns:d=\"DAV:\"><d:response><d:href>&xxe;</d:href></d:response>"
                + "</d:multistatus>";

        assertThrows(Exception.class, () -> CalendarDiscoveryParser.collections(xml, BASE_URI));
    }

    @Test
    void failedPropertiesAndForeignOriginsCannotBecomeDiscoveredCalendars() throws Exception {
        String xml = "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\"><d:response><d:href>https://foreign.example/calendar/</d:href>"
                + "<d:propstat><d:prop><d:resourcetype><c:calendar/></d:resourcetype></d:prop><d:status>HTTP/1.1 404 Not Found</d:status></d:propstat></d:response></d:multistatus>";
        assertEquals(0, CalendarDiscoveryParser.collections(xml, BASE_URI).size());
        assertThrows(IllegalArgumentException.class,
                () -> CalendarDiscoveryParser.collections(xml.replace("404 Not Found", "200 OK"), BASE_URI));
        assertThrows(java.io.IOException.class, () -> CalendarDiscoveryParser
                .collections(xml.replace("404 Not Found", "500 Internal Server Error"), BASE_URI));
    }
}
