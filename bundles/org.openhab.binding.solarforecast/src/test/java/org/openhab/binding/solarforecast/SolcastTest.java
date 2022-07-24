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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.solcast.SolcastObject;

/**
 * The {@link SolcastTest} tests responses from forecast solar website
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class SolcastTest {
    public static final String DATE_INPUT_PATTERN_STRING = "yyyy-MM-dd'T'HH:mm:ss";
    public static final DateTimeFormatter DATE_INPUT_PATTERN = DateTimeFormatter.ofPattern(DATE_INPUT_PATTERN_STRING);

    @Test
    void testForecastObject() {
        String dateTime = "2022-07-17T22:00:00.0000000Z";
        System.out.println(dateTime.substring(0, dateTime.lastIndexOf(".")));
        dateTime = dateTime.substring(0, dateTime.lastIndexOf("T"));
        LocalDate ld = LocalDate.parse(dateTime);
        System.out.println(ld);
        String content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        SolcastObject scfo = new SolcastObject(content, now);
        // System.out.println(scfo);
    }

    @Test
    void testForecastTreeMap() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        SolcastObject scfo = new SolcastObject(content, now);
        // System.out.println(scfo);
        System.out.println(scfo.getDayTotal(now, 0));
        System.out.println(scfo.getActualValue(now));
    }

    @Test
    void testJoin() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23);
        SolcastObject scfo = new SolcastObject(content, now);
        System.out.println(scfo.getActualValue(now));
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        System.out.println(scfo.getActualValue(now));
        System.out.println(scfo.getDayTotal(now, 0));
    }

    @Test
    void testOptimisticPessimistic() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23);
        SolcastObject scfo = new SolcastObject(content, now);
        System.out.println(scfo.getActualValue(now));
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        System.out.println("Forecast med " + scfo.getDayTotal(now, 2));
        System.out.println("Forecast pes " + scfo.getPessimisticDayTotal(now, 2));
        System.out.println("Forecast opt " + scfo.getOptimisticDayTotal(now, 2));
    }

    @Test
    void testInavlid() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        LocalDateTime now = LocalDateTime.now();
        SolcastObject scfo = new SolcastObject(content, now);
        System.out.println(scfo.getActualValue(now));
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        System.out.println(scfo.getActualValue(now));
        System.out.println(scfo.getDayTotal(now, 0));
    }

    @Test
    void testRawChannel() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23);
        SolcastObject sco = new SolcastObject(content, now);
        System.out.println(sco.getActualValue(now));
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        sco.join(content);
        System.out.println("SCO Raw " + sco.getRaw());
    }

    @Test
    void testRoofNE() {
        String content = FileReader.readFileInString("src/test/resources/solcast/NE.json");
        LocalDateTime now = LocalDateTime.now();
        JSONObject act = new JSONObject(content);
        JSONObject actual = new JSONObject();
        actual.put("estimated_actuals", act.getJSONArray("estimated_actuals"));
        SolcastObject scfo = new SolcastObject(actual.toString(), now);
        System.out.println(scfo.getActualValue(now));

        JSONObject forecasts = new JSONObject();
        forecasts.put("forecasts", act.getJSONArray("forecasts"));
        scfo.join(forecasts.toString());
        System.out.println(scfo.getActualValue(now));
    }
}
