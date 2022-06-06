/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Note to tests:
 * There is a chance, that one test will fail, if started right before midnight,
 * as {@code LocalDate.now} will return two different days between the calls.
 */

package org.openhab.binding.publicholiday.internal.publicholiday;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.openhab.binding.publicholiday.internal.PublicHolidayHandler;

class HolidayJobTest {
    @ParameterizedTest
    @MethodSource
    void run(List<Holiday> holidays, boolean expCurDay, boolean expNextDay, String expName) throws Exception {
        PublicHolidayHandler handlerMock = mock(PublicHolidayHandler.class);

        HolidayJob testUnit = new HolidayJob(handlerMock, holidays);
        testUnit.run();
        Mockito.verify(handlerMock).updateValue(eq(expCurDay), eq(expNextDay), eq(expName));
    }

    static Stream<Arguments> run() {
        return List.of(
                // no holidays configured
                Arguments.of(List.of(), false, false, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.NO_HOLIDAY, "h1")), false, false, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.HOLIDAY, "h1")), true, false, "h1"),
                Arguments.of(List.of(genHoliday(HolidaySpec.DAY_BEFORE, "h1")), false, true, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.HOLIDAY_BEFORE, "h1")), true, true, "h1")).stream();
    }

    @ParameterizedTest
    @MethodSource
    void refreshValues(List<Holiday> holidays, boolean updCurDay, boolean updNextDay, boolean expCurDay,
            boolean expNextDay, String expName) throws Exception {
        PublicHolidayHandler handlerMock = mock(PublicHolidayHandler.class);

        HolidayJob testUnit = new HolidayJob(handlerMock, holidays);
        testUnit.refreshValues(updCurDay, updNextDay);
        Mockito.verify(handlerMock).updateValue(eq(expCurDay), eq(expNextDay), eq(expName));
    }

    static Stream<Arguments> refreshValues() {
        return List.of(Arguments.of(List.of(), true, true, false, false, "none"),
                Arguments.of(List.of(), true, false, false, false, "none"),
                Arguments.of(List.of(), false, true, false, false, "none"),
                Arguments.of(List.of(), false, false, false, false, "none"),

                Arguments.of(List.of(genHoliday(HolidaySpec.NO_HOLIDAY, "h1")), true, true, false, false, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.NO_HOLIDAY, "h1")), false, true, false, false, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.NO_HOLIDAY, "h1")), true, false, false, false, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.NO_HOLIDAY, "h1")), false, false, false, false, "none"),

                Arguments.of(List.of(genHoliday(HolidaySpec.HOLIDAY, "h1")), true, true, true, false, "h1"),
                Arguments.of(List.of(genHoliday(HolidaySpec.HOLIDAY, "h1")), false, true, false, false, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.HOLIDAY, "h1")), true, false, true, false, "h1"),
                Arguments.of(List.of(genHoliday(HolidaySpec.HOLIDAY, "h1")), false, false, false, false, "none"),

                Arguments.of(List.of(genHoliday(HolidaySpec.DAY_BEFORE, "h1")), true, true, false, true, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.DAY_BEFORE, "h1")), false, true, false, true, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.DAY_BEFORE, "h1")), true, false, false, false, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.DAY_BEFORE, "h1")), false, false, false, false, "none"),

                Arguments.of(List.of(genHoliday(HolidaySpec.HOLIDAY_BEFORE, "h1")), true, true, true, true, "h1"),
                Arguments.of(List.of(genHoliday(HolidaySpec.HOLIDAY_BEFORE, "h1")), false, true, false, true, "none"),
                Arguments.of(List.of(genHoliday(HolidaySpec.HOLIDAY_BEFORE, "h1")), true, false, true, false, "h1"),
                Arguments.of(List.of(genHoliday(HolidaySpec.HOLIDAY_BEFORE, "h1")), false, false, false, false, "none"))
                .stream();
    }

    private enum HolidaySpec {
        NO_HOLIDAY,
        HOLIDAY,
        DAY_BEFORE,
        HOLIDAY_BEFORE
    }

    private static Holiday genHoliday(HolidaySpec holidaySpec, String name) {
        return new Holiday(name) {
            @Override
            public boolean isHoliday(LocalDate cmpDate) {
                int delta = LocalDate.now().until(cmpDate).getDays();
                switch (holidaySpec) {
                    case HOLIDAY:
                        return delta == 0;
                    case DAY_BEFORE:
                        return delta == 1;
                    case HOLIDAY_BEFORE:
                        return delta == 0 || delta == 1;
                }
                return false;
            }
        };
    }
}
