/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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

package org.openhab.binding.smhi.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.openhab.binding.smhi.internal.SmhiBindingConstants.*;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class SmhiTest {
    private static final ZonedDateTime TIME = ZonedDateTime.parse("2020-12-13T08:15:00Z");
    private @NonNullByDefault({}) TimeSeries timeSeries;

    @BeforeEach
    public void setUp() {
        try {
            InputStream is = SmhiTest.class.getResourceAsStream("forecast.json");
            if (is == null) {
                throw new AssertionError("Couldn't read forecast example");
            }
            String jsonString = new String(is.readAllBytes());
            timeSeries = Parser.parseTimeSeries(jsonString);
        } catch (IOException e) {
            throw new AssertionError("Couldn't read forecast example");
        }
    }

    @Test
    public void parameterTest() {
        assertNotNull(timeSeries);
        Forecast forecast = timeSeries.getForecast(TIME, 0).orElseThrow(AssertionError::new);

        BigDecimal msl = forecast.getParameter(PRESSURE).orElseThrow(AssertionError::new);
        BigDecimal t = forecast.getParameter(TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal vis = forecast.getParameter(VISIBILITY).orElseThrow(AssertionError::new);
        BigDecimal wd = forecast.getParameter(WIND_DIRECTION).orElseThrow(AssertionError::new);
        BigDecimal ws = forecast.getParameter(WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal r = forecast.getParameter(RELATIVE_HUMIDITY).orElseThrow(AssertionError::new);
        BigDecimal tstm = forecast.getParameter(THUNDER_PROBABILITY).orElseThrow(AssertionError::new);
        BigDecimal tcc = forecast.getParameter(TOTAL_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal lcc = forecast.getParameter(LOW_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal mcc = forecast.getParameter(MEDIUM_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal hcc = forecast.getParameter(HIGH_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal gust = forecast.getParameter(GUST).orElseThrow(AssertionError::new);
        BigDecimal pmin = forecast.getParameter(PRECIPITATION_MIN).orElseThrow(AssertionError::new);
        BigDecimal pmax = forecast.getParameter(PRECIPITATION_MAX).orElseThrow(AssertionError::new);
        BigDecimal spp = forecast.getParameter(PERCENT_FROZEN).orElseThrow(AssertionError::new);
        BigDecimal pcat = forecast.getParameter(PRECIPITATION_CATEGORY).orElseThrow(AssertionError::new);
        BigDecimal pmean = forecast.getParameter(PRECIPITATION_MEAN).orElseThrow(AssertionError::new);
        BigDecimal pmedian = forecast.getParameter(PRECIPITATION_MEDIAN).orElseThrow(AssertionError::new);
        BigDecimal wsymb = forecast.getParameter(WEATHER_SYMBOL).orElseThrow(AssertionError::new);

        assertEquals(0, msl.compareTo(BigDecimal.valueOf(1013.7)));
        assertEquals(0, t.compareTo(BigDecimal.valueOf(3.0)));
        assertEquals(0, vis.compareTo(BigDecimal.valueOf(24.3)));
        assertEquals(0, wd.compareTo(BigDecimal.valueOf(110)));
        assertEquals(0, ws.compareTo(BigDecimal.valueOf(1.5)));
        assertEquals(0, r.compareTo(BigDecimal.valueOf(96)));
        assertEquals(0, tstm.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, tcc.compareTo(BigDecimal.valueOf(8)));
        assertEquals(0, lcc.compareTo(BigDecimal.valueOf(8)));
        assertEquals(0, mcc.compareTo(BigDecimal.valueOf(4)));
        assertEquals(0, hcc.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, gust.compareTo(BigDecimal.valueOf(3.0)));
        assertEquals(0, pmin.compareTo(BigDecimal.valueOf(0.0)));
        assertEquals(0, pmax.compareTo(BigDecimal.valueOf(0.0)));
        assertEquals(0, spp.compareTo(BigDecimal.valueOf(-9)));
        assertEquals(0, pcat.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, pmean.compareTo(BigDecimal.valueOf(0.0)));
        assertEquals(0, pmedian.compareTo(BigDecimal.valueOf(0.0)));
        assertEquals(0, wsymb.compareTo(BigDecimal.valueOf(6)));
    }

    @Test
    public void aggregationsTest() {
        assertNotNull(timeSeries);
        BigDecimal maxTemp = ForecastAggregator.max(timeSeries, 5, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal minTemp = ForecastAggregator.min(timeSeries, 5, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal maxWind = ForecastAggregator.max(timeSeries, 5, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal minWind = ForecastAggregator.min(timeSeries, 5, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal totalPrecip = ForecastAggregator.total(timeSeries, 5, PRECIPITATION_MEAN)
                .orElseThrow(AssertionError::new);

        assertEquals(0, maxTemp.compareTo(BigDecimal.valueOf(7.5)));
        assertEquals(0, minTemp.compareTo(BigDecimal.valueOf(4.2)));
        assertEquals(0, maxWind.compareTo(BigDecimal.valueOf(4.4)));
        assertEquals(0, minWind.compareTo(BigDecimal.valueOf(3.7)));
        assertEquals(0, totalPrecip.compareTo(BigDecimal.valueOf(2.4)));
    }
}
