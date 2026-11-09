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
package org.openhab.binding.evohome.internal.api;

import static org.junit.jupiter.api.Assertions.*;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.evohome.internal.api.models.v2.dto.response.ZoneSchedule;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * Tests local schedule boundaries and daylight-saving transitions.
 *
 * @author Kovacs Istvan - Initial contribution
 */
@NonNullByDefault
class ZoneScheduleTest {
    private final ZoneId timeZone = ZoneId.of("Europe/Budapest");

    private ZoneSchedule schedule(String first, String second) {
        return parse(EvohomeApiClientTest.scheduleAt(first, second));
    }

    private ZoneSchedule parse(String json) {
        return Objects.requireNonNull(new Gson().fromJson(json, ZoneSchedule.class));
    }

    @Test
    void sundayScheduleRollsOverToMondayInLocationTimezone() {
        Instant next = schedule("06:00:00", "18:00:00").nextSwitchpoint(Instant.parse("2026-10-11T18:00:00Z"),
                timeZone);
        assertEquals(Instant.parse("2026-10-12T04:00:00Z"), next);
    }

    @Test
    void exactSwitchpointUsesTheFollowingFutureSwitchpoint() {
        Instant next = schedule("06:00:00", "18:00:00").nextSwitchpoint(Instant.parse("2026-10-11T16:00:00Z"),
                timeZone);
        assertEquals(Instant.parse("2026-10-12T04:00:00Z"), next);
    }

    @Test
    void springGapMovesNonexistentSwitchpointForward() {
        Instant next = schedule("02:30:00", "18:00:00").nextSwitchpoint(Instant.parse("2026-03-28T23:00:00Z"),
                timeZone);
        assertEquals(Instant.parse("2026-03-29T01:30:00Z"), next);
    }

    @Test
    void autumnOverlapUsesTheFirstOccurrenceOfLocalSwitchpoint() {
        Instant next = schedule("02:30:00", "18:00:00").nextSwitchpoint(Instant.parse("2026-10-24T22:00:00Z"),
                timeZone);
        assertEquals(Instant.parse("2026-10-25T00:30:00Z"), next);
    }

    @Test
    void scheduleTimesNeedNotArriveInChronologicalOrder() {
        Instant next = schedule("18:00:00", "06:00:00").nextSwitchpoint(Instant.parse("2026-10-08T03:00:00Z"),
                timeZone);
        assertEquals(Instant.parse("2026-10-08T04:00:00Z"), next);
    }

    @Test
    void missingOrIncompleteScheduleDoesNotCreateAnExpiry() {
        for (String json : new String[] { "{}", "{\"dailySchedules\":[]}",
                "{\"dailySchedules\":[{\"dayOfWeek\":\"Monday\",\"switchpoints\":[]}]}",
                "{\"dailySchedules\":[null]}" }) {
            ZoneSchedule schedule = Objects.requireNonNull(new Gson().fromJson(json, ZoneSchedule.class));
            assertThrows(IllegalArgumentException.class, () -> schedule.nextSwitchpoint(Instant.now(), timeZone));
        }
    }

    @Test
    void canonicalJsonOrdersDaysAndTimesWithoutRoundingProgrammedTemperatures() {
        JsonObject original = JsonParser.parseString(EvohomeApiClientTest.scheduleAt("18:00:00", "06:00:00")
                .replace("\"heatSetpoint\":18", "\"heatSetpoint\":18.125")
                .replace("\"heatSetpoint\":20", "\"heatSetpoint\":20.375")).getAsJsonObject();
        JsonArray originalDays = original.getAsJsonArray("dailySchedules");
        JsonArray reversed = new JsonArray();
        for (int index = 6; index >= 0; index--) {
            reversed.add(originalDays.get(index));
        }
        original.add("dailySchedules", reversed);
        String canonical = parse(original.toString()).toJson();
        JsonArray days = JsonParser.parseString(canonical).getAsJsonObject().getAsJsonArray("dailySchedules");
        List<String> expectedDays = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday",
                "Sunday");
        for (int index = 0; index < 7; index++) {
            JsonObject day = days.get(index).getAsJsonObject();
            assertEquals(expectedDays.get(index), day.get("dayOfWeek").getAsString());
            JsonArray points = day.getAsJsonArray("switchpoints");
            assertEquals("06:00:00", points.get(0).getAsJsonObject().get("timeOfDay").getAsString());
            assertEquals(20.375, points.get(0).getAsJsonObject().get("heatSetpoint").getAsDouble());
            assertEquals("18:00:00", points.get(1).getAsJsonObject().get("timeOfDay").getAsString());
            assertEquals(18.125, points.get(1).getAsJsonObject().get("heatSetpoint").getAsDouble());
        }
        assertEquals(canonical, parse(canonical).toJson());
    }

    @Test
    void nextScheduledPointReturnsTheTemperatureOfTheSameFutureEntry() {
        ZoneSchedule weekly = schedule("06:00:00", "18:00:00");
        ZoneSchedule.ScheduledSwitchpoint next = weekly.nextScheduledSwitchpoint(Instant.parse("2026-10-11T04:00:00Z"),
                timeZone);
        assertEquals(Instant.parse("2026-10-11T16:00:00Z"), next.time());
        assertEquals(20.0, next.temperature());
        ZoneSchedule.ScheduledSwitchpoint monday = weekly
                .nextScheduledSwitchpoint(Instant.parse("2026-10-11T16:00:00Z"), timeZone);
        assertEquals(Instant.parse("2026-10-12T04:00:00Z"), monday.time());
        assertEquals(18.0, monday.temperature());
    }

    @Test
    void legacyTimeOnlyScheduleStillWorksWithoutInventingTemperatures() {
        ZoneSchedule weekly = parse(EvohomeApiClientTest.scheduleAt("06:00:00", "18:00:00")
                .replace(",\"heatSetpoint\":18", "").replace(",\"heatSetpoint\":20", ""));
        Instant now = Instant.parse("2026-10-12T03:00:00Z");
        assertEquals(Instant.parse("2026-10-12T04:00:00Z"), weekly.nextSwitchpoint(now, timeZone));
        assertThrows(IllegalArgumentException.class, weekly::validate);
        assertThrows(IllegalArgumentException.class, weekly::toJson);
        assertThrows(IllegalArgumentException.class, () -> weekly.nextScheduledSwitchpoint(now, timeZone));
    }

    @Test
    void invalidTemperatureAnywhereInTheWeekRejectsDerivedAndRawReadback() {
        String valid = EvohomeApiClientTest.scheduleAt("06:00:00", "18:00:00");
        for (String value : List.of("null", "\"NaN\"", "\"Infinity\"", "\"-Infinity\"")) {
            ZoneSchedule weekly = parse(valid.replaceFirst("\"heatSetpoint\":18", "\"heatSetpoint\":" + value));
            assertThrows(IllegalArgumentException.class, weekly::validate);
            assertThrows(IllegalArgumentException.class, weekly::toJson);
            assertThrows(IllegalArgumentException.class,
                    () -> weekly.nextScheduledSwitchpoint(Instant.parse("2026-10-14T03:00:00Z"), timeZone));
        }
    }

    @Test
    void duplicateLocalTimesCannotProduceAmbiguousScheduledTemperature() {
        ZoneSchedule weekly = schedule("06:00", "06:00:00");
        Instant now = Instant.parse("2026-10-12T03:00:00Z");
        assertEquals(Instant.parse("2026-10-12T04:00:00Z"), weekly.nextSwitchpoint(now, timeZone));
        assertThrows(IllegalArgumentException.class, weekly::validate);
        assertThrows(IllegalArgumentException.class, weekly::toJson);
        assertThrows(IllegalArgumentException.class, () -> weekly.nextScheduledSwitchpoint(now, timeZone));
    }

    @Test
    void scheduleReadbackRejectsDuplicateDaysAndMissingSwitchpointFields() {
        String valid = EvohomeApiClientTest.scheduleAt("06:00:00", "18:00:00");
        for (String json : List.of(valid.replace("Tuesday", "Monday"), valid.replace("Tuesday", "Funday"),
                valid.replaceFirst("\"dayOfWeek\":\"Monday\"", "\"dayOfWeek\":null"),
                valid.replaceFirst("\"timeOfDay\":\"06:00:00\"", "\"timeOfDay\":null"))) {
            ZoneSchedule weekly = parse(json);
            assertThrows(IllegalArgumentException.class, weekly::validate);
            assertThrows(IllegalArgumentException.class, weekly::toJson);
        }
    }

    @Test
    void invalidClockTimeIsRejectedInsteadOfNormalizingIntoAnotherDay() {
        ZoneSchedule weekly = schedule("24:00:00", "18:00:00");
        assertThrows(DateTimeException.class, weekly::validate);
        assertThrows(DateTimeException.class, weekly::toJson);
        assertThrows(DateTimeException.class,
                () -> weekly.nextScheduledSwitchpoint(Instant.parse("2026-10-12T03:00:00Z"), timeZone));
    }

    @Test
    void scheduledTemperaturesRemainAttachedAcrossDaylightSavingTransitions() {
        ZoneSchedule weekly = schedule("02:30:00", "18:00:00");
        ZoneSchedule.ScheduledSwitchpoint spring = weekly
                .nextScheduledSwitchpoint(Instant.parse("2026-03-28T23:00:00Z"), timeZone);
        assertEquals(Instant.parse("2026-03-29T01:30:00Z"), spring.time());
        assertEquals(18.0, spring.temperature());
        ZoneSchedule.ScheduledSwitchpoint autumn = weekly
                .nextScheduledSwitchpoint(Instant.parse("2026-10-24T22:00:00Z"), timeZone);
        assertEquals(Instant.parse("2026-10-25T00:30:00Z"), autumn.time());
        assertEquals(18.0, autumn.temperature());
    }
}
