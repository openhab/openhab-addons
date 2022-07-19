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

import static org.junit.jupiter.api.Assertions.assertFalse;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.ForecastObject;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;

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
    void test() {
        System.out.println("Test");
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        JSONObject contentJson = new JSONObject(content);
        JSONObject resultJson = contentJson.getJSONObject("result");
        JSONObject wattsJson = resultJson.getJSONObject("watt_hours");
        Iterator<String> iter = wattsJson.keys();
        TreeMap<LocalDateTime, Integer> m = new TreeMap<LocalDateTime, Integer>();
        while (iter.hasNext()) {
            String dateStr = iter.next();
            LocalDateTime ldt = LocalDateTime.parse(dateStr.replace(" ", "T"));
            if (ldt.getDayOfMonth() == 17) {
                m.put(ldt, wattsJson.getInt(dateStr));
            }
            // Date d = new Date(dateStr.replace(" ", "T"));
        }
        System.out.println(m);
        // LocalDateTime now = LocalDateTime.now();
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        System.out.println(now);
        Entry<LocalDateTime, Integer> f = m.floorEntry(now);
        System.out.println(f);
        Entry<LocalDateTime, Integer> c = m.ceilingEntry(now);
        System.out.println(c);
        if (f != null) {
            if (c != null) {
                // we're during suntime!
                System.out.println("Floor " + f + " Ceiling " + c);
                int production = c.getValue() - f.getValue();
                int interpolation = now.getMinute() - f.getKey().getMinute();
                int interpolationProduction = production * interpolation / 60;
                System.out
                        .println("Minutes to interpolate " + interpolation + " Production " + interpolationProduction);
            }
        }
    }

    @Test
    void testForecastObject() {
        System.out.println("Test FO");
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        ForecastObject fo = new ForecastObject(content, now);
        System.out.println(fo.getCurrentValue(now));
    }

    @Test
    void testForecastSum() {
        System.out.println("Test FO");
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        ForecastObject fo = new ForecastObject(content, now);
        System.out.println(fo.getCurrentValue(now));
        QuantityType<Energy> actual = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        System.out.println(actual + " / " + fo.getCurrentValue(now));
        actual = actual.add(fo.getCurrentValue(now));
        System.out.println(actual + " / " + fo.getCurrentValue(now));
        actual = actual.add(fo.getCurrentValue(now));
        System.out.println(actual + " / " + fo.getCurrentValue(now));
    }

    @Test
    void testErrorCases() {
        ForecastObject fo = new ForecastObject();
        assertFalse(fo.isValid());
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        fo.getCurrentValue(now);
        fo.getDayTotal();
        fo.getRemainingProduction(now);
    }
}
