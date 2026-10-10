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

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Calendar queries carry the same floating-time zone as the local event expansion.
 *
 * @author Andreas Vilippus - Initial contribution
 * @author Andreas Vilippus - CalDAV query time-zone handling
 */
@NonNullByDefault
public final class CalendarReport {
    private CalendarReport() {
    }

    public static String query(ZonedDateTime start, ZonedDateTime end) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
                + "<c:calendar-query xmlns:d=\"DAV:\" xmlns:c=\"urn:ietf:params:xml:ns:caldav\">"
                + "<d:prop><d:getetag/><c:calendar-data/></d:prop><c:filter><c:comp-filter name=\"VCALENDAR\">"
                + "<c:comp-filter name=\"VEVENT\"><c:time-range start=\""
                + DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(java.time.ZoneOffset.UTC).format(start)
                + "\" end=\""
                + DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'").withZone(java.time.ZoneOffset.UTC).format(end)
                + "\"/></c:comp-filter>" + "</c:comp-filter></c:filter><c:timezone>"
                + timezone(start, end).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                + "</c:timezone></c:calendar-query>";
    }

    private static String timezone(ZonedDateTime start, ZonedDateTime end) {
        var rules = start.getZone().getRules();
        Instant first = start.minusDays(2).toInstant();
        // Historical floating masters can determine the exact duration of an occurrence inside this horizon.
        var history = rules.getTransitions();
        if (!history.isEmpty()) {
            first = first.isBefore(history.getFirst().getInstant()) ? first
                    : history.getFirst().getInstant().minus(2, ChronoUnit.DAYS);
        }
        Instant last = end.plusDays(2).toInstant();
        var local = ZonedDateTime.ofInstant(first, start.getZone()).toLocalDateTime();
        StringBuilder result = new StringBuilder("BEGIN:VCALENDAR\r\nVERSION:2.0\r\nPRODID:-//openHAB//CalDAV//EN\r\n"
                + "BEGIN:VTIMEZONE\r\nTZID:" + start.getZone().getId() + "\r\n");
        appendObservance(result, "STANDARD", DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(local),
                rules.getOffset(first), rules.getOffset(first));
        var transition = rules.nextTransition(first);
        while (transition != null && transition.getInstant().isBefore(last)) {
            String kind = rules.getDaylightSavings(transition.getInstant()).isZero() ? "STANDARD" : "DAYLIGHT";
            appendObservance(result, kind,
                    DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss").format(transition.getDateTimeBefore()),
                    transition.getOffsetBefore(), transition.getOffsetAfter());
            transition = rules.nextTransition(transition.getInstant());
        }
        return result.append("END:VTIMEZONE\r\nEND:VCALENDAR\r\n").toString();
    }

    private static void appendObservance(StringBuilder result, String kind, String start, ZoneOffset from,
            ZoneOffset to) {
        result.append("BEGIN:").append(kind).append("\r\nDTSTART:").append(start).append("\r\nTZOFFSETFROM:")
                .append(offset(from)).append("\r\nTZOFFSETTO:").append(offset(to)).append("\r\nEND:").append(kind)
                .append("\r\n");
    }

    private static String offset(ZoneOffset offset) {
        int seconds = offset.getTotalSeconds();
        int magnitude = Math.abs(seconds);
        return String.format(Locale.ROOT, "%s%02d%02d", seconds < 0 ? "-" : "+", magnitude / 3600, magnitude / 60 % 60)
                + (magnitude % 60 == 0 ? "" : String.format(Locale.ROOT, "%02d", magnitude % 60));
    }
}
