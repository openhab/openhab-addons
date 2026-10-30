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
package org.openhab.binding.caldav.internal.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.openhab.binding.caldav.internal.model.CalendarEvent;

@NonNullByDefault
class CalendarEventSelectionTest {
    @Test
    void sortsBeforeApplyingLimitAndMarksTruncation() {
        CalendarEvent later = timed("later", 10, null);
        CalendarEvent earlier = timed("earlier", 8, null);

        CalendarEventSelection.Result result = CalendarEventSelection.select(List.of(later, earlier), 1);

        assertEquals(List.of(earlier), result.events());
        assertTrue(result.truncated());
    }

    @Test
    void leavesCompleteListUntruncatedAndUsesStableInstanceIdsAsTieBreaker() {
        CalendarEvent second = timed("same", 8, "2026-09-17T08:00:00Z");
        CalendarEvent first = timed("same", 8, "2026-09-16T08:00:00Z");

        CalendarEventSelection.Result result = CalendarEventSelection.select(List.of(second, first), 10);

        assertEquals(List.of(first, second), result.events());
        assertTrue(!result.truncated());
    }

    private static CalendarEvent timed(String uid, int hour, @Nullable String recurrenceId) {
        ZonedDateTime start = ZonedDateTime.of(2026, 9, 17, hour, 0, 0, 0, ZoneOffset.UTC);
        return new CalendarEvent(uid, recurrenceId, uid, "", "", start, start.plusHours(1), null, null, false,
                "CONFIRMED", List.of(), "");
    }

    @Test
    void currentIncludesStartExcludesEndAndNextSkipsPastAndRunning() {
        var past = timed("past", 7, null);
        var running = timed("running", 8, null);
        var future = timed("future", 9, null);
        var now = ZonedDateTime.of(2026, 9, 17, 8, 0, 0, 0, ZoneOffset.UTC).toInstant();
        var sorted = CalendarEventSelection.sort(List.of(future, running, past), ZoneOffset.UTC);
        assertEquals(running, CalendarEventSelection.current(sorted, now, ZoneOffset.UTC));
        assertEquals(future, CalendarEventSelection.next(sorted, now, ZoneOffset.UTC));
        assertEquals(future, CalendarEventSelection.current(sorted, now.plusSeconds(3600), ZoneOffset.UTC));
    }
}
