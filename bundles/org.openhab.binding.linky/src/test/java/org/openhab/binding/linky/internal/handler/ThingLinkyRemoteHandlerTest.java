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
package org.openhab.binding.linky.internal.handler;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.linky.internal.config.LinkyThingRemoteConfiguration;
import org.openhab.binding.linky.internal.dto.IntervalReading;
import org.openhab.binding.linky.internal.dto.MeterReading;
import org.openhab.core.i18n.LocaleProvider;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.thing.Thing;

/**
 * The {@link ThingLinkyRemoteHandler} is responsible for extra validation for Raw things.
 *
 * @author Laurent Arnal - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
public class ThingLinkyRemoteHandlerTest {

    private ThingLinkyRemoteHandler handler;

    @Mock
    LocaleProvider localProvider;

    @Mock
    Thing thing;

    @Mock
    TimeZoneProvider tzProvider;

    @Mock
    LinkyThingRemoteConfiguration config;

    @BeforeEach
    public void setUp() {
        when(thing.getConfiguration()).thenReturn(new LinkyThingRemoteConfiguration());
    }

    /*
     * @AfterAll
     * public void tearDown() {
     * // handler.dispose();
     * }
     */

    public @Nullable MeterReading getMeterReadingAfterChecks(ThingLinkyRemoteHandler handler,
            @Nullable MeterReading meterReading) {
        return handler.getMeterReadingAfterChecks(meterReading);
    }

    @Test
    public void testBase() {
        handler = new ThingLinkyRemoteHandler(thing, localProvider, tzProvider);

        MeterReading mr = getMeterReadingAfterChecks(handler, null);
        assertEquals(mr, null);
    }

    @Test
    public void testValidRange1() {
        handler = new ThingLinkyRemoteHandler(thing, localProvider, tzProvider);

        MeterReading mr = new MeterReading();
        mr.baseValue = new IntervalReading[75];
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0, 0);
        for (int idx = 0; idx < 75; idx++) {
            mr.baseValue[idx] = new IntervalReading();
            mr.baseValue[idx].value = Double.valueOf(idx);
            mr.baseValue[idx].date = startDate.plusDays(idx);
        }

        mr = getMeterReadingAfterChecks(handler, mr);
        assertNotEquals(mr, null);
        if (mr == null) {
            return;
        }
        assertNotEquals(mr.weekValue, null);
        assertNotEquals(mr.monthValue, null);
        assertNotEquals(mr.yearValue, null);

        assertEquals(mr.weekValue.length, 11);
        assertEquals(mr.monthValue.length, 3);
        assertEquals(mr.yearValue.length, 1);

        assertEquals(mr.weekValue[0].value, 10);

        assertEquals(mr.weekValue[1].value, 56);
        assertEquals(mr.weekValue[1].date, LocalDateTime.of(2025, 1, 6, 0, 0, 0));

        assertEquals(mr.weekValue[4].value, 203);
        assertEquals(mr.weekValue[4].date, LocalDateTime.of(2025, 1, 27, 0, 0, 0));

        assertEquals(mr.weekValue[10].value, 497);
        assertEquals(mr.weekValue[10].date, LocalDateTime.of(2025, 3, 10, 0, 0, 0));

        assertEquals(mr.monthValue[0].value, 465);
        assertEquals(mr.monthValue[0].date, LocalDateTime.of(2025, 1, 1, 0, 0, 0));

        assertEquals(mr.monthValue[1].value, 1246);
        assertEquals(mr.monthValue[1].date, LocalDateTime.of(2025, 2, 1, 0, 0, 0));

        assertEquals(mr.monthValue[2].value, 1064);
        assertEquals(mr.monthValue[2].date, LocalDateTime.of(2025, 3, 1, 0, 0, 0));

        assertEquals(mr.yearValue[0].value, 2775);
        assertEquals(mr.yearValue[0].date, LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }

    @Test
    public void testValidRange2() {
        handler = new ThingLinkyRemoteHandler(thing, localProvider, tzProvider);

        MeterReading mr = new MeterReading();
        mr.baseValue = new IntervalReading[128];
        LocalDateTime startDate = LocalDateTime.of(2024, 11, 6, 0, 0, 0);
        for (int idx = 0; idx < 128; idx++) {
            mr.baseValue[idx] = new IntervalReading();
            mr.baseValue[idx].value = Double.valueOf(idx);
            mr.baseValue[idx].date = startDate.plusDays(idx);
        }

        mr = getMeterReadingAfterChecks(handler, mr);
        assertNotEquals(mr, null);
        if (mr == null) {
            return;
        }
        assertNotEquals(mr.weekValue, null);
        assertNotEquals(mr.monthValue, null);
        assertNotEquals(mr.yearValue, null);

        assertEquals(mr.weekValue.length, 19);
        assertEquals(mr.monthValue.length, 5);
        assertEquals(mr.yearValue.length, 2);

        assertEquals(mr.weekValue[0].value, 10);
        assertEquals(mr.weekValue[0].date, LocalDateTime.of(2024, 11, 6, 0, 0, 0));

        assertEquals(mr.weekValue[2].value, 105);
        assertEquals(mr.weekValue[2].date, LocalDateTime.of(2024, 11, 18, 0, 0, 0));

        assertEquals(mr.weekValue[6].value, 301);
        assertEquals(mr.weekValue[6].date, LocalDateTime.of(2024, 12, 16, 0, 0, 0));

        assertEquals(mr.weekValue[8].value, 399);
        assertEquals(mr.weekValue[8].date, LocalDateTime.of(2024, 12, 30, 0, 0, 0));

        assertEquals(mr.weekValue[12].value, 595);
        assertEquals(mr.weekValue[12].date, LocalDateTime.of(2025, 01, 27, 0, 0, 0));

        assertEquals(mr.weekValue[18].value, 502);
        assertEquals(mr.weekValue[18].date, LocalDateTime.of(2025, 03, 10, 0, 0, 0));

        assertEquals(mr.monthValue[0].value, 300);
        assertEquals(mr.monthValue[0].date, LocalDateTime.of(2024, 11, 1, 0, 0, 0));

        assertEquals(mr.monthValue[1].value, 1240);
        assertEquals(mr.monthValue[1].date, LocalDateTime.of(2024, 12, 1, 0, 0, 0));

        assertEquals(mr.monthValue[2].value, 2201);
        assertEquals(mr.monthValue[2].date, LocalDateTime.of(2025, 1, 1, 0, 0, 0));

        assertEquals(mr.monthValue[3].value, 2814);
        assertEquals(mr.monthValue[3].date, LocalDateTime.of(2025, 2, 1, 0, 0, 0));

        assertEquals(mr.monthValue[4].value, 1573);
        assertEquals(mr.monthValue[4].date, LocalDateTime.of(2025, 3, 1, 0, 0, 0));

        assertEquals(mr.yearValue[0].value, 1540);
        assertEquals(mr.yearValue[0].date, LocalDateTime.of(2024, 1, 1, 0, 0, 0));

        assertEquals(mr.yearValue[1].value, 6588);
        assertEquals(mr.yearValue[1].date, LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }

    @Test
    public void testValidRange3() {
        handler = new ThingLinkyRemoteHandler(thing, localProvider, tzProvider);

        MeterReading mr = new MeterReading();
        mr.baseValue = new IntervalReading[716];
        LocalDateTime startDate = LocalDateTime.of(2023, 03, 29, 0, 0, 0);
        for (int idx = 0; idx < 716; idx++) {
            mr.baseValue[idx] = new IntervalReading();
            mr.baseValue[idx].value = Double.valueOf(idx);
            mr.baseValue[idx].date = startDate.plusDays(idx);
        }

        mr = getMeterReadingAfterChecks(handler, mr);
        assertNotEquals(mr, null);
        if (mr == null) {
            return;
        }

        assertNotEquals(mr.weekValue, null);
        assertNotEquals(mr.monthValue, null);
        assertNotEquals(mr.yearValue, null);

        assertEquals(mr.weekValue.length, 103);
        assertEquals(mr.monthValue.length, 25);
        assertEquals(mr.yearValue.length, 3);

        assertEquals(mr.weekValue[0].value, 10);
        assertEquals(mr.weekValue[0].date, LocalDateTime.of(2023, 03, 29, 0, 0, 0));

        assertEquals(mr.weekValue[2].value, 105);
        assertEquals(mr.weekValue[2].date, LocalDateTime.of(2023, 04, 10, 0, 0, 0));

        assertEquals(mr.weekValue[15].value, 742);
        assertEquals(mr.weekValue[15].date, LocalDateTime.of(2023, 07, 10, 0, 0, 0));

        assertEquals(mr.weekValue[39].value, 1918);
        assertEquals(mr.weekValue[39].date, LocalDateTime.of(2023, 12, 25, 0, 0, 0));

        assertEquals(mr.weekValue[56].value, 2751);
        assertEquals(mr.weekValue[56].date, LocalDateTime.of(2024, 04, 22, 0, 0, 0));

        assertEquals(mr.weekValue[90].value, 4417);
        assertEquals(mr.weekValue[90].date, LocalDateTime.of(2024, 12, 16, 0, 0, 0));

        assertEquals(mr.weekValue[97].value, 4760);
        assertEquals(mr.weekValue[97].date, LocalDateTime.of(2025, 02, 03, 0, 0, 0));

        assertEquals(mr.weekValue[102].value, 2854);
        assertEquals(mr.weekValue[102].date, LocalDateTime.of(2025, 03, 10, 0, 0, 0));

        assertEquals(mr.monthValue[0].value, 3);
        assertEquals(mr.monthValue[0].date, LocalDateTime.of(2023, 03, 01, 0, 0, 0));

        assertEquals(mr.monthValue[5].value, 4340);
        assertEquals(mr.monthValue[5].date, LocalDateTime.of(2023, 8, 01, 0, 0, 0));

        assertEquals(mr.monthValue[9].value, 8122);
        assertEquals(mr.monthValue[9].date, LocalDateTime.of(2023, 12, 1, 0, 0, 0));

        assertEquals(mr.monthValue[10].value, 9083);
        assertEquals(mr.monthValue[10].date, LocalDateTime.of(2024, 01, 01, 0, 0, 0));

        assertEquals(mr.monthValue[17].value, 15686);
        assertEquals(mr.monthValue[17].date, LocalDateTime.of(2024, 8, 01, 0, 0, 0));

        assertEquals(mr.monthValue[22].value, 20429);
        assertEquals(mr.monthValue[22].date, LocalDateTime.of(2025, 01, 01, 0, 0, 0));

        assertEquals(mr.monthValue[23].value, 19278);
        assertEquals(mr.monthValue[23].date, LocalDateTime.of(2025, 02, 01, 0, 0, 0));

        assertEquals(mr.monthValue[24].value, 9217);
        assertEquals(mr.monthValue[24].date, LocalDateTime.of(2025, 03, 01, 0, 0, 0));

        assertEquals(mr.yearValue[0].value, 38503);
        assertEquals(mr.yearValue[0].date, LocalDateTime.of(2023, 1, 1, 0, 0, 0));

        assertEquals(mr.yearValue[1].value, 168543);
        assertEquals(mr.yearValue[1].date, LocalDateTime.of(2024, 1, 1, 0, 0, 0));

        assertEquals(mr.yearValue[2].value, 48924);
        assertEquals(mr.yearValue[2].date, LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }

    @Test
    public void testValidRange4() {
        handler = new ThingLinkyRemoteHandler(thing, localProvider, tzProvider);

        MeterReading mr = new MeterReading();
        mr.baseValue = new IntervalReading[35];
        LocalDateTime startDate = LocalDateTime.of(2025, 02, 10, 0, 0, 0);
        for (int idx = 0; idx < 35; idx++) {
            mr.baseValue[idx] = new IntervalReading();
            mr.baseValue[idx].value = Double.valueOf(idx);
            mr.baseValue[idx].date = startDate.plusDays(idx);
        }

        mr = getMeterReadingAfterChecks(handler, mr);
        assertNotEquals(mr, null);
        if (mr == null) {
            return;
        }

        assertNotEquals(mr.weekValue, null);
        assertNotEquals(mr.monthValue, null);
        assertNotEquals(mr.yearValue, null);

        assertEquals(mr.weekValue.length, 5);
        assertEquals(mr.monthValue.length, 2);
        assertEquals(mr.yearValue.length, 1);

        assertEquals(mr.weekValue[0].value, 21);
        assertEquals(mr.weekValue[0].date, LocalDateTime.of(2025, 2, 10, 0, 0, 0));

        assertEquals(mr.weekValue[1].value, 70);
        assertEquals(mr.weekValue[1].date, LocalDateTime.of(2025, 2, 17, 0, 0, 0));

        assertEquals(mr.weekValue[2].value, 119);
        assertEquals(mr.weekValue[2].date, LocalDateTime.of(2025, 2, 24, 0, 0, 0));

        assertEquals(mr.weekValue[3].value, 168);
        assertEquals(mr.weekValue[3].date, LocalDateTime.of(2025, 3, 3, 0, 0, 0));

        assertEquals(mr.weekValue[4].value, 217);
        assertEquals(mr.weekValue[4].date, LocalDateTime.of(2025, 3, 10, 0, 0, 0));

        assertEquals(mr.monthValue[0].value, 171);
        assertEquals(mr.monthValue[0].date, LocalDateTime.of(2025, 2, 1, 0, 0, 0));

        assertEquals(mr.monthValue[1].value, 424);
        assertEquals(mr.monthValue[1].date, LocalDateTime.of(2025, 3, 1, 0, 0, 0));

        assertEquals(mr.yearValue[0].value, 595);
        assertEquals(mr.yearValue[0].date, LocalDateTime.of(2025, 1, 1, 0, 0, 0));
    }
}
