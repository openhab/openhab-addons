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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests discovery properties, optional enrichment and XML trust boundaries.
 *
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - Optional metadata and privilege regressions
 */
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
        assertEquals("Private", collections.get(0).displayName());
    }

    @Test
    void missingDisplayNameRemainsSemanticallyMissing() throws Exception {
        String xml = "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                + "<d:response><d:href>https://caldav.example.test/calendars/work/</d:href><d:propstat><d:prop>"
                + "<d:displayname> </d:displayname><d:resourcetype><c:calendar/></d:resourcetype>"
                + "</d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response></d:multistatus>";

        var collections = CalendarDiscoveryParser.collections(xml, BASE_URI);

        assertEquals(URI.create("https://caldav.example.test/calendars/work/"), collections.get(0).uri());
        assertEquals("", collections.get(0).displayName());
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

    @Test
    void parsesDescription() throws Exception {
        assertEquals("Family & friends",
                collection("<c:calendar-description> Family &amp; friends </c:calendar-description>", "")
                        .description());
    }

    @Test
    void missingDescriptionIsAllowed() throws Exception {
        assertEquals("", collection("", "").description());
    }

    @Test
    void descriptionIn404PropstatIsAllowed() throws Exception {
        assertEquals("", collection("", failedProperty("<c:calendar-description>Ignored</c:calendar-description>", 404))
                .description());
    }

    @Test
    void parsesCalendarColor() throws Exception {
        assertEquals("#11223388", collection("<i:calendar-color> #11223388 </i:calendar-color>", "").color());
    }

    @Test
    void missingCalendarColorIsAllowed() throws Exception {
        assertEquals("", collection("", "").color());
    }

    @Test
    void colorIn404PropstatIsAllowed() throws Exception {
        assertEquals("", collection("", failedProperty("<i:calendar-color>#112233</i:calendar-color>", 404)).color());
    }

    @Test
    void parsesKnownPrivilegesWithoutExpandingWrite() throws Exception {
        CalendarCollection calendar = collection("""
                <d:current-user-privilege-set>
                  <d:privilege><d:read/></d:privilege>
                  <d:privilege><d:write/></d:privilege>
                  <d:privilege><d:bind/></d:privilege>
                  <d:privilege><d:read/></d:privilege>
                </d:current-user-privilege-set>
                """, "");
        assertEquals(Set.of(DavPrivilege.READ, DavPrivilege.WRITE, DavPrivilege.BIND), calendar.privileges());
        assertEquals(Set.of(DavPrivilege.WRITE), collection(
                "<d:current-user-privilege-set><d:privilege><d:write/></d:privilege></d:current-user-privilege-set>",
                "").privileges());
    }

    @Test
    void parsesAllKnownPrivilegeNames() throws Exception {
        String properties = "<d:current-user-privilege-set>"
                + "<d:privilege><d:read/><d:write/><d:write-content/><d:write-properties/><d:bind/><d:unbind/></d:privilege>"
                + "</d:current-user-privilege-set>";
        assertEquals(Set.of(DavPrivilege.values()), collection(properties, "").privileges());
    }

    @Test
    void unknownPrivilegeIsIgnored() throws Exception {
        assertEquals(Set.of(DavPrivilege.READ), collection("""
                <d:current-user-privilege-set>
                  <d:privilege><x:custom-privilege/><x:write/><d:all/></d:privilege>
                  <d:privilege><d:read/></d:privilege>
                  <x:privilege><d:write/></x:privilege>
                </d:current-user-privilege-set>
                """, "").privileges());
    }

    @Test
    void missingPrivilegeSetIsAllowed() throws Exception {
        assertEquals(Set.of(), collection("", "").privileges());
    }

    @Test
    void privilegeSetIn404PropstatIsAllowed() throws Exception {
        assertEquals(Set.of(), collection("", failedProperty(
                "<d:current-user-privilege-set><d:privilege><d:write/></d:privilege></d:current-user-privilege-set>",
                404)).privileges());
    }

    @Test
    void mixed200And404PropstatsStillParseCollection() throws Exception {
        CalendarCollection calendar = collection("<c:calendar-description>Visible</c:calendar-description>",
                failedProperty("<c:calendar-description>Ignored</c:calendar-description><i:calendar-color/>"
                        + "<d:current-user-privilege-set/>", 404));
        assertEquals("Visible", calendar.description());
        assertEquals("", calendar.color());
        assertEquals(Set.of(), calendar.privileges());
        assertEquals("Family", calendar.displayName());
    }

    @Test
    void malformedOrUnusualMetadataDoesNotAffectCollectionUri() throws Exception {
        CalendarCollection calendar = collection("""
                <c:calendar-description><d:href>https://foreign.example/wrong/</d:href></c:calendar-description>
                <i:calendar-color>unusual: not a CSS color</i:calendar-color>
                <d:current-user-privilege-set><d:privilege>write</d:privilege></d:current-user-privilege-set>
                """, "");
        assertEquals(URI.create("https://caldav.example.test/dav/family/"), calendar.uri());
        assertEquals("https://foreign.example/wrong/", calendar.description());
        assertEquals("unusual: not a CSS color", calendar.color());
        assertEquals(Set.of(), calendar.privileges());
    }

    @Test
    void oversizedOptionalMetadataIsOmittedWithoutLosingCollection() throws Exception {
        String atLimit = "a".repeat(4096);
        assertEquals(atLimit, collection("<c:calendar-description>" + atLimit
                + "</c:calendar-description><i:calendar-color>" + atLimit + "</i:calendar-color>", "").description());
        assertEquals(atLimit, collection("<i:calendar-color>" + atLimit + "</i:calendar-color>", "").color());
        CalendarCollection calendar = collection("<c:calendar-description>" + atLimit
                + "x</c:calendar-description><i:calendar-color>" + atLimit + "x</i:calendar-color>", "");
        assertEquals("", calendar.description());
        assertEquals("", calendar.color());
        assertEquals(BASE_URI.resolve("family/"), calendar.uri());
    }

    @Test
    void optionalMetadataFailuresDoNotMaskRequiredPropertyFailures() throws Exception {
        for (int status : List.of(403, 500)) {
            CalendarCollection calendar = collection("", failedProperty(
                    "<c:calendar-description/><i:calendar-color/><d:current-user-privilege-set/>", status));
            assertEquals("Family", calendar.displayName());
            assertEquals("", calendar.description());
            assertEquals("", calendar.color());
            assertEquals(Set.of(), calendar.privileges());
            assertThrows(java.io.IOException.class,
                    () -> collection("", failedProperty("<c:calendar-description/><d:resourcetype/>", status)));
        }
    }

    @Test
    void discoveryResourceLimitStillRejectsOversizedLists() {
        String response = "<d:response><d:href>family/</d:href><d:propstat><d:prop><d:resourcetype>"
                + "<c:calendar/></d:resourcetype></d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat></d:response>";
        String xml = "<d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                + response.repeat(5001) + "</d:multistatus>";
        assertThrows(java.io.IOException.class, () -> CalendarDiscoveryParser.collections(xml, BASE_URI));
    }

    private static CalendarCollection collection(String metadata, String extraPropstats) throws Exception {
        String xml = """
                <d:multistatus xmlns:d="DAV:" xmlns:c="urn:ietf:params:xml:ns:caldav"
                    xmlns:i="http://apple.com/ns/ical/" xmlns:x="urn:example:custom">
                  <d:response><d:href>family/</d:href><d:propstat><d:prop>
                    <d:displayname> Family </d:displayname><d:resourcetype><c:calendar/></d:resourcetype>
                    %s
                  </d:prop><d:status>HTTP/1.1 200 OK</d:status></d:propstat>%s</d:response>
                </d:multistatus>
                """.formatted(metadata, extraPropstats);
        List<CalendarCollection> collections = CalendarDiscoveryParser.collections(xml, BASE_URI);
        assertEquals(1, collections.size());
        return collections.getFirst();
    }

    private static String failedProperty(String property, int status) {
        return "<d:propstat><d:prop>" + property + "</d:prop><d:status>HTTP/1.1 " + status
                + " Failed</d:status></d:propstat>";
    }
}
