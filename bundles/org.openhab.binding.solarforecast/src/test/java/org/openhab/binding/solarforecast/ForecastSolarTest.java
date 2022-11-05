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

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.measure.quantity.Energy;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.solarforecast.internal.forecastsolar.ForecastSolarObject;
import org.openhab.binding.solarforecast.internal.utils.Utils;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;

/**
 * The {@link ForecastSolarTest} tests responses from forecast solar object
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
class ForecastSolarTest {
    // double comparison tolerance = 1 Watt
    private static final double TOLERANCE = 0.001;

    @Test
    void testForecastObject() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 17, 00);
        ForecastSolarObject fo = new ForecastSolarObject(content, queryDateTime);
        // "2022-07-17 21:32:00": 63583,
        assertEquals(63.583, fo.getDayTotal(queryDateTime.toLocalDate()), TOLERANCE, "Total production");
        // "2022-07-17 17:00:00": 52896,
        assertEquals(52.896, fo.getActualValue(queryDateTime), TOLERANCE, "Current Production");
        // 63583 - 52896 = 10687
        assertEquals(10.687, fo.getRemainingProduction(queryDateTime), TOLERANCE, "Current Production");
        // sum cross check
        assertEquals(fo.getDayTotal(queryDateTime.toLocalDate()),
                fo.getActualValue(queryDateTime) + fo.getRemainingProduction(queryDateTime), TOLERANCE,
                "actual + remain = total");

        queryDateTime = LocalDateTime.of(2022, 7, 18, 19, 00);
        // "2022-07-18 19:00:00": 63067,
        assertEquals(63.067, fo.getActualValue(queryDateTime), TOLERANCE, "Actual production");
        // "2022-07-18 21:31:00": 65554
        assertEquals(65.554, fo.getDayTotal(queryDateTime.toLocalDate()), TOLERANCE, "Total production");
    }

    @Test
    void testActualPower() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 10, 00);
        ForecastSolarObject fo = new ForecastSolarObject(content, queryDateTime);
        // "2022-07-17 10:00:00": 4874,
        assertEquals(4.874, fo.getActualPowerValue(queryDateTime), TOLERANCE, "Actual estimation");

        queryDateTime = LocalDateTime.of(2022, 7, 18, 14, 00);
        // "2022-07-18 14:00:00": 7054,
        assertEquals(7.054, fo.getActualPowerValue(queryDateTime), TOLERANCE, "Actual estimation");
    }

    @Test
    void testInterpolation() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 16, 0);
        ForecastSolarObject fo = new ForecastSolarObject(content, queryDateTime);

        // test steady value increase
        double previousValue = 0;
        for (int i = 0; i < 60; i++) {
            queryDateTime = queryDateTime.plusMinutes(1);
            assertTrue(previousValue < fo.getActualValue(queryDateTime));
            previousValue = fo.getActualValue(queryDateTime);
        }

        queryDateTime = LocalDateTime.of(2022, 7, 18, 6, 23);
        // "2022-07-18 06:00:00": 132,
        // "2022-07-18 07:00:00": 1188,
        // 1188 - 132 = 1056 | 1056 * 23 / 60 = 404 | 404 + 131 = 535
        assertEquals(0.535, fo.getActualValue(queryDateTime), 0.002, "Actual estimation");
    }

    @Test
    void testForecastSum() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime queryDateTime = LocalDateTime.of(2022, 7, 17, 16, 23);
        ForecastSolarObject fo = new ForecastSolarObject(content, queryDateTime);
        QuantityType<Energy> actual = QuantityType.valueOf(0, Units.KILOWATT_HOUR);
        State st = Utils.getEnergyState(fo.getActualValue(queryDateTime));
        assertTrue(st instanceof QuantityType);
        actual = actual.add((QuantityType<Energy>) st);
        assertEquals(49.431, actual.floatValue(), TOLERANCE, "Current Production");
        actual = actual.add((QuantityType<Energy>) st);
        assertEquals(98.862, actual.floatValue(), TOLERANCE, "Doubled Current Production");
    }

    @Test
    void testCornerCases() {
        // invalid object
        ForecastSolarObject fo = new ForecastSolarObject();
        assertFalse(fo.isValid());
        LocalDateTime query = LocalDateTime.of(2022, 7, 17, 16, 23);
        assertEquals(-1.0, fo.getActualValue(query), TOLERANCE, "Actual Production");
        assertEquals(-1.0, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Today Production");
        assertEquals(-1.0, fo.getRemainingProduction(query), TOLERANCE, "Remaining Production");
        assertEquals(-1.0, fo.getDayTotal(query.plusDays(1).toLocalDate()), TOLERANCE, "Tomorrow Production");

        // valid object - query date one day too early
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        query = LocalDateTime.of(2022, 7, 16, 23, 59);
        fo = new ForecastSolarObject(content, query);
        assertEquals(-1.0, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(-1.0, fo.getActualValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(-1.0, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(-1.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");

        // one minute later we reach a valid date
        query = query.plusMinutes(1);
        assertEquals(63.583, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(0.0, fo.getActualValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(63.583, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(0.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");

        // valid object - query date one day too late
        query = LocalDateTime.of(2022, 7, 19, 0, 0);
        assertEquals(-1.0, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(-1.0, fo.getActualValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(-1.0, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(-1.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");

        // one minute earlier we reach a valid date
        query = query.minusMinutes(1);
        assertEquals(65.554, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(65.554, fo.getActualValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(0.0, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(0.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");

        // test times between 2 dates
        query = LocalDateTime.of(2022, 7, 17, 23, 59);
        assertEquals(63.583, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(63.583, fo.getActualValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(0.0, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(0.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");
        query = query.plusMinutes(1);
        assertEquals(65.554, fo.getDayTotal(query.toLocalDate()), TOLERANCE, "Actual out of scope");
        assertEquals(0.0, fo.getActualValue(query), TOLERANCE, "Actual out of scope");
        assertEquals(65.554, fo.getRemainingProduction(query), TOLERANCE, "Remain out of scope");
        assertEquals(0.0, fo.getActualPowerValue(query), TOLERANCE, "Remain out of scope");
    }

    @Test
    void testActions() {
        String content = FileReader.readFileInString("src/test/resources/forecastsolar/result.json");
        LocalDateTime query = LocalDateTime.of(2022, 7, 17, 16, 23);
        ForecastSolarObject fo = new ForecastSolarObject(content, query);
        assertEquals("2022-07-17T05:31:00", fo.getForecastBegin().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "Forecast begin");
        assertEquals("2022-07-18T21:31:00", fo.getForecastEnd().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                "Forecast end");
        assertEquals(QuantityType.valueOf(63.583, Units.KILOWATT_HOUR).toString(),
                fo.getDay(query.toLocalDate()).toFullString(), "Actual out of scope");

        query = LocalDateTime.of(2022, 7, 17, 0, 0);
        // "watt_hours_day": {
        // "2022-07-17": 63583,
        // "2022-07-18": 65554
        // }
        assertEquals(QuantityType.valueOf(129.137, Units.KILOWATT_HOUR).toString(),
                fo.getEnergy(query, query.plusDays(2)).toFullString(), "Actual out of scope");

        assertEquals(UnDefType.UNDEF, fo.getDay(query.toLocalDate(), "optimistic"));
        assertEquals(UnDefType.UNDEF, fo.getDay(query.toLocalDate(), "pessimistic"));
        assertEquals(UnDefType.UNDEF, fo.getDay(query.toLocalDate(), "total", "rubbish"));
    }

    @Test
    public void testHorizon() throws URISyntaxException, IOException, InterruptedException {
        String url = "https://api.forecast.solar/estimate/50.55598767987004/8.49558522179684/12/-40/5.525";
        String horizon = "2,2,2,2,1,1,3,3,4,3,3,4,3,3,3,3,4,5,7,5,4,2,2,2,2,1,1,1,1,1,2,2,2,2,1,2";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
        HttpResponse<String> response = client.send(request, BodyHandlers.ofString());
        System.out.println(url);
        System.out.println(response.body());
        url += "?horizon=" + horizon;
        request = HttpRequest.newBuilder().uri(new URI(url)).GET().build();
        response = client.send(request, BodyHandlers.ofString());
        System.out.println(url);
        System.out.println(response.body());

    }
}
