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

import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.caldav.internal.config.CalendarConfiguration;

@NonNullByDefault
class CalendarWindowTest {
    @Test
    void nowAnchorPreservesTimeOfDay() {
        CalendarConfiguration configuration = new CalendarConfiguration();
        configuration.rangeAnchor = "NOW";
        configuration.rangeStartOffset = 0;
        configuration.rangeEndOffset = 2;
        ZonedDateTime now = ZonedDateTime.of(2026, 9, 16, 13, 45, 0, 0, ZoneId.of("UTC"));

        CalendarWindow window = CalendarWindow.from(configuration, now.getZone(), now);

        assertEquals(now, window.start());
        assertEquals(now.plusDays(3), window.end());
    }

    @Test
    void todayAnchorUsesMidnightInConfiguredZone() {
        CalendarConfiguration configuration = new CalendarConfiguration();
        configuration.rangeStartOffset = 0;
        configuration.rangeEndOffset = 0;
        ZonedDateTime now = ZonedDateTime.of(2026, 9, 16, 23, 0, 0, 0, ZoneId.of("Europe/Berlin"));

        CalendarWindow window = CalendarWindow.from(configuration, now.getZone(), now);

        assertEquals("2026-09-16T00:00+02:00[Europe/Berlin]", window.start().toString());
        assertEquals("2026-09-17T00:00+02:00[Europe/Berlin]", window.end().toString());
    }

    @Test
    void appliesNegativeOffsetsAndKeepsExclusiveEnd() {
        CalendarConfiguration configuration = new CalendarConfiguration();
        configuration.rangeStartOffset = -2;
        configuration.rangeEndOffset = 0;
        ZonedDateTime now = ZonedDateTime.of(2026, 9, 16, 13, 0, 0, 0, ZoneId.of("UTC"));

        CalendarWindow window = CalendarWindow.from(configuration, now.getZone(), now);

        assertEquals(ZonedDateTime.parse("2026-09-14T00:00Z[UTC]"), window.start());
        assertEquals(ZonedDateTime.parse("2026-09-17T00:00Z[UTC]"), window.end());
    }

    @Test
    void todayAnchorUsesSuppliedInstantWhenZoneDiffers() {
        CalendarConfiguration configuration = new CalendarConfiguration();
        ZonedDateTime now = ZonedDateTime.of(2026, 9, 16, 23, 30, 0, 0, ZoneId.of("UTC"));

        CalendarWindow window = CalendarWindow.from(configuration, ZoneId.of("Pacific/Kiritimati"), now);

        assertEquals("2026-09-17T00:00+14:00[Pacific/Kiritimati]", window.start().toString());
    }
}
