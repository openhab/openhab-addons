/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.myenergi.internal.model;

import static java.time.DayOfWeek.MONDAY;
import static java.time.DayOfWeek.SATURDAY;
import static java.time.DayOfWeek.SUNDAY;
import static java.time.DayOfWeek.TUESDAY;
import static java.time.DayOfWeek.WEDNESDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.DayOfWeek;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.myenergi.internal.exception.InvalidDataException;

/**
 * The {@link DaysOfWeekMapTest} is a test class for {@link DaysOfWeekMap}.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
class DaysOfWeekMapTest {

    @Test
    void testDaysOfWeekMap() {
        DaysOfWeekMap map = new DaysOfWeekMap();
        assertEquals("00000000", map.getMapAsString());
    }

    @Test
    void testDaysOfWeekMapDaysOfTheWeek() {
        DaysOfWeekMap map = new DaysOfWeekMap(TUESDAY);
        assertEquals("00100000", map.getMapAsString());
    }

    @Test
    void testDaysOfWeekMapDaysOfTheWeekArray() {
        DayOfWeek[] dow = { TUESDAY, SATURDAY };
        DaysOfWeekMap map = new DaysOfWeekMap(dow);
        assertEquals("00100010", map.getMapAsString());
    }

    @Test
    void testSetMap() {
        DaysOfWeekMap map = new DaysOfWeekMap();
        try {
            map.setMap("01010101");
            assertEquals("01010101", map.getMapAsString());
        } catch (InvalidDataException e) {
            fail(e);
        }

        assertThrows(InvalidDataException.class, () -> {
            map.setMap("01010101110");
        });

        assertThrows(InvalidDataException.class, () -> {
            map.setMap("01010102");
        });

        assertThrows(InvalidDataException.class, () -> {
            map.setMap("0101010");
        });
    }

    @Test
    void testSetDay() {
        DaysOfWeekMap map = new DaysOfWeekMap();
        assertEquals("00000000", map.getMapAsString());
        map.setDay(TUESDAY, true);
        assertEquals("00100000", map.getMapAsString());
        map.setDay(WEDNESDAY, true);
        assertEquals("00110000", map.getMapAsString());
        map.setDay(SATURDAY, true);
        assertEquals("00110010", map.getMapAsString());
        map.setDay(WEDNESDAY, false);
        assertEquals("00100010", map.getMapAsString());
    }

    @Test
    void testSetDays() {
        DayOfWeek[] dow1 = { MONDAY, WEDNESDAY, SATURDAY, SUNDAY };
        DayOfWeek[] dow2 = { MONDAY, SATURDAY };

        DaysOfWeekMap map = new DaysOfWeekMap();
        assertEquals("00000000", map.getMapAsString());

        map.setDays(dow1, true);
        assertEquals("01010011", map.getMapAsString());

        map.setDays(dow2, false);
        assertEquals("00010001", map.getMapAsString());
    }
}
