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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;

@NonNullByDefault
class CalendarReportTest {
    @Test
    void createsUtcCalendarQueryWithHalfOpenRange() {
        ZonedDateTime start = ZonedDateTime.of(2026, 9, 16, 0, 0, 0, 0, ZoneOffset.ofHours(2));
        ZonedDateTime end = start.plusDays(1);

        String query = CalendarReport.query(start, end);

        assertTrue(query.startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"));
        assertTrue(query.contains("xmlns:d=\"DAV:\""));
        assertTrue(query.contains("xmlns:c=\"urn:ietf:params:xml:ns:caldav\""));
        assertTrue(query.contains("start=\"20260915T220000Z\""));
        assertTrue(query.contains("end=\"20260916T220000Z\""));
        assertTrue(query.contains("<d:getetag/>") && query.contains("<c:calendar-data/>"));
    }
}
