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
package org.openhab.binding.astro.internal.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.model.Circadian;

/**
 * Tests for {@link CircadianCalc}.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class CircadianCalcTest {

    private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    private CircadianCalc circadianCalc;

    @BeforeEach
    public void setup() {
        circadianCalc = new CircadianCalc();
    }

    @Test
    public void calculateUsesPreviousSolarMidnightBeforeSunrise() {
        Calendar noon = newCalendar(2024, Calendar.JANUARY, 1, 13, 0);
        Calendar sunrise = newCalendar(2024, Calendar.JANUARY, 1, 7, 0);
        Calendar sunset = newCalendar(2024, Calendar.JANUARY, 1, 19, 0);

        Circadian beforeSunrise = circadianCalc.calculate(newCalendar(2024, Calendar.JANUARY, 1, 5, 0), sunrise, sunset,
                noon);
        assertEquals(55, beforeSunrise.brightness());
        Circadian afterSunset = circadianCalc.calculate(newCalendar(2024, Calendar.JANUARY, 1, 21, 0), sunrise, sunset,
                noon);
        assertEquals(afterSunset, beforeSunrise);

        assertEquals(2500, beforeSunrise.temperature());
    }

    private static Calendar newCalendar(int year, int month, int day, int hour, int minute) {
        Calendar calendar = new GregorianCalendar(UTC);
        calendar.clear();
        calendar.set(year, month, day, hour, minute, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
