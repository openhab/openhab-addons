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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault
class DavResponseTest {
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
}
