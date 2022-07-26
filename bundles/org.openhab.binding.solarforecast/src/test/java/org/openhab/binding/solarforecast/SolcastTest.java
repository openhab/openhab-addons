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

    @Test
    void testForecastObject() {
        String content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 23, 16, 23);
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(19.462, scfo.getActualValue(now), 0.001, "Actual estimation");
    }

    @Test
    void testForecastTreeMap() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 17, 16, 23);
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(25.413, scfo.getDayTotal(now, 0), 0.001, "Day total");
        assertEquals(24.150, scfo.getActualValue(now), 0.001, "Actual estimation");
    }

    @Test
    void testJoin() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23);
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(-1.0, scfo.getActualValue(now), 0.01, "Invalid");
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(22.011, scfo.getActualValue(now), 0.01, "Actual data");
        assertEquals(23.107, scfo.getDayTotal(now, 0), 0.01, "Today data");
    }

    @Test
    void testOptimisticPessimistic() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23);
        SolcastObject scfo = new SolcastObject(content, now);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(19.389, scfo.getDayTotal(now, 2), 0.001, "Estimation");
        assertEquals(7.358, scfo.getPessimisticDayTotal(now, 2), 0.001, "Estimation");
        assertEquals(22.283, scfo.getOptimisticDayTotal(now, 2), 0.001, "Estimation");
    }

    @Test
    void testInavlid() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        LocalDateTime now = LocalDateTime.now();
        SolcastObject scfo = new SolcastObject(content, now);
        assertEquals(-1.0, scfo.getActualValue(now), 0.01, "Data available - day not in");
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        scfo.join(content);
        assertEquals(-1.0, scfo.getActualValue(now), 0.01, "Data available after merge - day not in");
        assertEquals(-1.0, scfo.getDayTotal(now, 0), 0.01, "Data available after merge - day not in");
    }

    @Test
    void testRawChannel() {
        String content = FileReader.readFileInString("src/test/resources/solcast/estimated-actuals.json");
        LocalDateTime now = LocalDateTime.of(2022, 7, 18, 16, 23);
        SolcastObject sco = new SolcastObject(content, now);
        content = FileReader.readFileInString("src/test/resources/solcast/forecasts.json");
        sco.join(content);
        JSONObject joined = new JSONObject(sco.getRaw());
        assertTrue(joined.has("forecasts"), "Forecasts available");
        assertTrue(joined.has("estimated_actuals"), "Actual data available");
    }
}
