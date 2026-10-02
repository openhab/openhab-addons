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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.caldav.internal.model.CalendarEvent;

@NonNullByDefault
class EventJsonTest {
    @Test
    void serializesTimedEventAndEscapesText() {
        CalendarEvent event = new CalendarEvent("uid-1", null, "Title \"quoted\"", "line 1\nline 2", "Office",
                ZonedDateTime.of(2026, 9, 16, 8, 0, 0, 0, ZoneOffset.ofHours(2)),
                ZonedDateTime.of(2026, 9, 16, 9, 0, 0, 0, ZoneOffset.ofHours(2)), null, null, false, "CONFIRMED",
                List.of("Work", "Important"), "organizer@example.org");

        String json = EventJson.serialize(List.of(event));

        assertEquals(
                "[{\"instanceId\":\"uid-1|2026-09-16T08:00+02:00\",\"uid\":\"uid-1\",\"recurrenceId\":null,\"title\":\"Title \\\"quoted\\\"\",\"description\":\"line 1\\nline 2\",\"location\":\"Office\",\"start\":\"2026-09-16T08:00+02:00\",\"end\":\"2026-09-16T09:00+02:00\",\"allDay\":false,\"status\":\"CONFIRMED\",\"categories\":[\"Work\",\"Important\"],\"organizer\":\"organizer@example.org\"}]",
                json);
    }

    @Test
    void serializesAllDayDatesAndEmptyList() {
        CalendarEvent event = new CalendarEvent("holiday", null, "Holiday", "", "", null, null,
                LocalDate.of(2026, 9, 17), LocalDate.of(2026, 9, 18), true, "CONFIRMED", List.of(), "");

        assertEquals("[]", EventJson.serialize(List.of()));
        assertTrue(EventJson.serialize(List.of(event)).contains("\"instanceId\":\"holiday|2026-09-17\""));
        assertTrue(EventJson.serialize(List.of(event)).contains("\"start\":\"2026-09-17\""));
        assertTrue(EventJson.serialize(List.of(event)).contains("\"end\":\"2026-09-18\""));
    }

    @Test
    void serializesNullTimedDatesAsJsonNull() {
        CalendarEvent event = new CalendarEvent("missing-time", null, "Untimed", "", "", null, null, null, null, false,
                "CONFIRMED", List.of(), "");

        String json = EventJson.serialize(List.of(event));

        assertTrue(json.contains("\"start\":null"));
        assertTrue(json.contains("\"end\":null"));
    }

    @Test
    void roundTripsEveryJsonControlCharacter() {
        StringBuilder text = new StringBuilder("quotes\" slash\\ ä <tag>");
        for (char c = 0; c < 32; c++) {
            text.append(c);
        }
        CalendarEvent event = new CalendarEvent("controls", null, text.toString(), "", "", null, null,
                LocalDate.of(2026, 9, 17), LocalDate.of(2026, 9, 18), true, "CONFIRMED", List.of(), "");
        String json = EventJson.serialize(List.of(event));
        assertEquals(text.toString(), com.google.gson.JsonParser.parseString(json).getAsJsonArray().get(0)
                .getAsJsonObject().get("title").getAsString());
        assertTrue(json.chars().noneMatch(c -> c < 32));
    }
}
