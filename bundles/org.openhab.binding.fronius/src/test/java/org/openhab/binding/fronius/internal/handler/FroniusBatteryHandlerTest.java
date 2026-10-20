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
package org.openhab.binding.fronius.internal.handler;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.ScheduleType;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.TimeOfUseRecord;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.TimeOfUseRecords;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.TimeTableRecord;
import org.openhab.binding.fronius.internal.api.dto.inverter.batterycontrol.WeekdaysRecord;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.UnDefType;

/**
 * Tests for {@link FroniusBatteryHandler}.
 *
 * @author Christian Jonak-Möchel - Initial contribution
 */
@NonNullByDefault
class FroniusBatteryHandlerTest {
    private static final ZoneId ZONE = ZoneId.of("Europe/Vienna");
    private static final WeekdaysRecord ALL_WEEK = new WeekdaysRecord(true, true, true, true, true, true, true);
    private static final TimeOfUseRecords UNTIL_NOON = timeOfUse("10:00", "12:00");

    private static TimeOfUseRecords timeOfUse(String start, String end) {
        return new TimeOfUseRecords(new TimeOfUseRecord[] {
                new TimeOfUseRecord(true, 1000, ScheduleType.CHARGE_MAX, new TimeTableRecord(start, end), ALL_WEEK) });
    }

    private static ZonedDateTime at(int hour, int minute, int second) {
        return ZonedDateTime.of(2026, 10, 18, hour, minute, second, 0, ZONE);
    }

    @Test
    void entryIsInEffectFromItsStartTime() {
        assertEquals(UnDefType.UNDEF,
                FroniusBatteryHandler.effectiveLimit(UNTIL_NOON, ScheduleType.CHARGE_MAX, at(9, 59, 59)));
        assertEquals(new QuantityType<>(1000, Units.WATT),
                FroniusBatteryHandler.effectiveLimit(UNTIL_NOON, ScheduleType.CHARGE_MAX, at(10, 0, 0)));
    }

    @Test
    void entryIsInEffectUntilButExcludingItsEndTime() {
        assertEquals(new QuantityType<>(1000, Units.WATT),
                FroniusBatteryHandler.effectiveLimit(UNTIL_NOON, ScheduleType.CHARGE_MAX, at(11, 59, 59)));
        assertEquals(UnDefType.UNDEF,
                FroniusBatteryHandler.effectiveLimit(UNTIL_NOON, ScheduleType.CHARGE_MAX, at(12, 0, 0)));
    }

    @Test
    void entryEndingAt2359IsNotInEffectDuringTheLastMinuteOfTheDay() {
        assertEquals(UnDefType.UNDEF, FroniusBatteryHandler.effectiveLimit(timeOfUse("00:00", "23:59"),
                ScheduleType.CHARGE_MAX, at(23, 59, 30)));
    }

    @Test
    void entriesWithAnInvalidTimeAreIgnored() {
        // the inverter web UI saves "NaN:NaN" as end time when 24:00 is entered, and the inverter ignores such entries
        TimeOfUseRecords records = new TimeOfUseRecords(new TimeOfUseRecord[] {
                new TimeOfUseRecord(true, 500, ScheduleType.CHARGE_MAX, new TimeTableRecord("00:00", "NaN:NaN"),
                        ALL_WEEK),
                new TimeOfUseRecord(true, 1000, ScheduleType.CHARGE_MAX, new TimeTableRecord("10:00", "12:00"),
                        ALL_WEEK) });

        assertEquals(new QuantityType<>(1000, Units.WATT),
                FroniusBatteryHandler.effectiveLimit(records, ScheduleType.CHARGE_MAX, at(11, 0, 0)));
    }
}
