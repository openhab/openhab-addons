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
package org.openhab.binding.icalendar.internal.logic;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link CalDavResponseConverter}.
 *
 * @author YOUR FULL NAME - Initial contribution
 */
@NonNullByDefault
public class CalDavResponseConverterTest {
    private static final String TZ = "BEGIN:VTIMEZONE\r\nTZID:Europe/Berlin\r\nEND:VTIMEZONE\r\n";

    private static String response(String... calendars) {
        final StringBuilder sb = new StringBuilder(
                "<?xml version=\"1.0\"?><d:multistatus xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">");
        for (String calendar : calendars) {
            sb.append("<d:response><d:propstat><d:prop><c:calendar-data>").append(calendar)
                    .append("</c:calendar-data></d:prop></d:propstat></d:response>");
        }
        return sb.append("</d:multistatus>").toString();
    }

    private static String convert(String xml) throws CalendarException {
        return CalDavResponseConverter.toICalendar(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    public void mergesEventsAndDeduplicatesTimeZones() throws CalendarException {
        final String result = convert(response(
                "BEGIN:VCALENDAR\r\nVERSION:2.0\r\n" + TZ + "BEGIN:VEVENT\r\nUID:1\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n",
                "BEGIN:VCALENDAR\r\nVERSION:2.0\r\n" + TZ
                        + "BEGIN:VEVENT\r\nUID:2\r\nBEGIN:VALARM\r\nACTION:DISPLAY\r\nEND:VALARM\r\nEND:VEVENT\r\nEND:VCALENDAR\r\n"));
        assertEquals(1, result.split("BEGIN:VTIMEZONE", -1).length - 1);
        assertEquals(2, result.split("BEGIN:VEVENT", -1).length - 1);
        assertEquals(1, result.split("BEGIN:VCALENDAR", -1).length - 1);
        assertTrue(result.contains("BEGIN:VALARM"));
        assertTrue(result.endsWith("END:VCALENDAR\r\n"));
    }

    @Test
    public void unfoldsLines() throws CalendarException {
        final String result = convert(response(
                "BEGIN:VCALENDAR\nBEGIN:VEVENT\nUID:1\nSUMMARY:Very long\n  summary\nEND:VEVENT\nEND:VCALENDAR\n"));
        assertTrue(result.contains("SUMMARY:Very long summary\r\n"));
    }

    @Test
    public void emptyResultIsValidCalendar() throws CalendarException {
        final String result = convert(response());
        assertFalse(result.contains("BEGIN:VEVENT"));
        assertTrue(result.startsWith("BEGIN:VCALENDAR"));
    }

    @Test
    public void rejectsNonMultistatus() {
        assertThrows(CalendarException.class, () -> convert("<html/>"));
        assertThrows(CalendarException.class, () -> convert("no xml"));
    }

    @Test
    public void rejectsDoctype() {
        assertThrows(CalendarException.class, () -> convert(
                "<?xml version=\"1.0\"?><!DOCTYPE d [<!ENTITY x \"y\">]><d:multistatus xmlns:d=\"DAV:\"/>"));
    }
}
