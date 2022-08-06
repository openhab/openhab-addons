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
 */
package org.openhab.binding.solarforecast;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.Utils;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;

/**
 * The {@link ForecastSolarTest} tests responses from forecast solar website
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ForecastSolarTest {
    public static final String DATE_INPUT_PATTERN_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter DATE_INPUT_PATTERN = DateTimeFormatter.ofPattern(DATE_INPUT_PATTERN_STRING);

    @Test
    void testForecastObject() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        ForecastSolarObject fo = new ForecastSolarObject(content, now, now);
        assertEquals(49.431, fo.getActualValue(now), 0.001, "Current Production");
        assertEquals(14.152, fo.getRemainingProduction(now), 0.001, "Current Production");
        assertEquals(fo.getDayTotal(now, 0), fo.getActualValue(now) + fo.getRemainingProduction(now), 0.001,
                "Total production");
        assertEquals(fo.getDayTotal(now, 0), fo.getActualValue(now) + fo.getRemainingProduction(now), 0.001,
                "Total production");
    }

    @Test
    void testActualPower() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        ForecastSolarObject fo = new ForecastSolarObject(content, now, now);
        assertEquals(5.704, fo.getActualPowerValue(now), 0.001, "Actual estimation");

        // todo: test this - date out of scope shall return undef
        // LocalDateTime ldt = LocalDateTime.of(2022, 7, 23, 0, 5);
        LocalDateTime ldt = LocalDateTime.of(2022, 7, 17, 0, 5);
        for (int i = 0; i < 96; i++) {
            ldt = ldt.plusMinutes(15);
            System.out.println(ldt + " " + fo.getActualPowerValue(ldt));
        }
    }

    @Test
    void testInterpolation() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 0);
        ForecastSolarObject fo = new ForecastSolarObject(content, now, now);
        double previousValue = 0;
        for (int i = 0; i < 60; i++) {
            now = now.plusMinutes(1);
            assertTrue(previousValue < fo.getActualValue(now));
            previousValue = fo.getActualValue(now);
        }
    }

    @Test
    void testForecastSum() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        ForecastSolarObject fo = new ForecastSolarObject(content, now, now);
        QuantityType<Energy> actual = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        State st = Utils.getEnergyState(fo.getActualValue(now));
        assertTrue(st instanceof QuantityType);
        actual = actual.add((QuantityType<Energy>) st);
        assertEquals(49.431, actual.floatValue(), 0.001, "Current Production");
        actual = actual.add((QuantityType<Energy>) st);
        assertEquals(98.862, actual.floatValue(), 0.001, "Doubled Current Production");
    }

    @Test
    void testErrorCases() {
        ForecastSolarObject fo = new ForecastSolarObject();
        assertFalse(fo.isValid());
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        assertEquals(-1.0, fo.getActualValue(now), 0.001, "Actual Production");
        assertEquals(-1.0, fo.getDayTotal(now, 0), 0.001, "Today Production");
        assertEquals(-1.0, fo.getRemainingProduction(now), 0.001, "Remaining Production");
        assertEquals(-1.0, fo.getDayTotal(now, 1), 0.001, "Tomorrow Production");
    }

    @Test
    void testActions() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        ForecastSolarObject fo = new ForecastSolarObject(content, now, now);
        System.out.println(fo.getForecastBegin());
        System.out.println(fo.getForecastEnd());
        System.out.println(fo.getDay(now.toLocalDate()));
        System.out.println(fo.getDay(now.toLocalDate()));
        System.out.println(fo.getPower(now));
        System.out.println(fo.getEnergy(now, now.plusDays(2)));
        System.out.println(fo.getEnergy(now, now.plusMinutes(120)));
        System.out.println(fo.getEnergy(now, now.plusDays(20)));
        System.out.println(fo.getEnergy(now, now.plusDays(20)));
    }
}
