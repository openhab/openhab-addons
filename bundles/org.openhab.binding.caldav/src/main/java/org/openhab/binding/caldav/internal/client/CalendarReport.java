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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;

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
                + "\"/></c:comp-filter>" + "</c:comp-filter></c:filter></c:calendar-query>";
    }
}
