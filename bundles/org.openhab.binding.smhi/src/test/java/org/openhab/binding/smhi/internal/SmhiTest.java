/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
import java.math.RoundingMode;
import java.time.ZonedDateTime;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.DecimalType;

/**
 * @author Anders Alfredsson - Initial contribution
 */
@NonNullByDefault
public class SmhiTest {
    private static final ZonedDateTime TIME = ZonedDateTime.parse("2021-06-13T07:00:00Z");
    private @NonNullByDefault({}) TimeSeries timeSeries1;
    private @NonNullByDefault({}) TimeSeries timeSeries2;
    private @NonNullByDefault({}) TimeSeries timeSeries3;

    @BeforeEach
    public void setUp() {
        try {
            @Nullable
            InputStream is1 = SmhiTest.class.getResourceAsStream("forecast1.json");
            @Nullable
            InputStream is2 = SmhiTest.class.getResourceAsStream("forecast2.json");
            @Nullable
            InputStream is3 = SmhiTest.class.getResourceAsStream("forecast3.json");
            if (is1 == null || is2 == null || is3 == null) {
                throw new AssertionError("Couldn't read forecast example");
            }
            String str1 = new String(is1.readAllBytes());
            String str2 = new String(is2.readAllBytes());
            String str3 = new String(is3.readAllBytes());
            timeSeries1 = Parser.parseTimeSeries(str1);
            timeSeries2 = Parser.parseTimeSeries(str2);
            timeSeries3 = Parser.parseTimeSeries(str3);
        } catch (IOException e) {
            throw new AssertionError("Couldn't read forecast example");
        }
    }

    @Test
    public void parameterTest() {
        assertNotNull(timeSeries1);
        Forecast forecast0 = timeSeries1.getForecast(TIME, 0).orElseThrow(AssertionError::new);

        BigDecimal msl0 = forecast0.getParameter(PRESSURE).orElseThrow(AssertionError::new);
        BigDecimal t0 = forecast0.getParameter(TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal vis0 = forecast0.getParameter(VISIBILITY).orElseThrow(AssertionError::new);
        BigDecimal wd0 = forecast0.getParameter(WIND_DIRECTION).orElseThrow(AssertionError::new);
        BigDecimal ws0 = forecast0.getParameter(WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal r0 = forecast0.getParameter(RELATIVE_HUMIDITY).orElseThrow(AssertionError::new);
        BigDecimal tstm0 = forecast0.getParameter(THUNDER_PROBABILITY).orElseThrow(AssertionError::new);
        BigDecimal tcc0 = forecast0.getParameter(TOTAL_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal lcc0 = forecast0.getParameter(LOW_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal mcc0 = forecast0.getParameter(MEDIUM_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal hcc0 = forecast0.getParameter(HIGH_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal gust0 = forecast0.getParameter(GUST).orElseThrow(AssertionError::new);
        BigDecimal pmin0 = forecast0.getParameter(PRECIPITATION_MIN).orElseThrow(AssertionError::new);
        BigDecimal pmax0 = forecast0.getParameter(PRECIPITATION_MAX).orElseThrow(AssertionError::new);
        BigDecimal spp0 = forecast0.getParameter(PERCENT_FROZEN).orElseThrow(AssertionError::new);
        BigDecimal pcat0 = forecast0.getParameter(PRECIPITATION_CATEGORY).orElseThrow(AssertionError::new);
        BigDecimal pmean0 = forecast0.getParameter(PRECIPITATION_MEAN).orElseThrow(AssertionError::new);
        BigDecimal pmedian0 = forecast0.getParameter(PRECIPITATION_MEDIAN).orElseThrow(AssertionError::new);
        BigDecimal wsymb0 = forecast0.getParameter(WEATHER_SYMBOL).orElseThrow(AssertionError::new);

        assertEquals(0, msl0.compareTo(BigDecimal.valueOf(1014.1)));
        assertEquals(0, t0.compareTo(BigDecimal.valueOf(18.2)));
        assertEquals(0, vis0.compareTo(BigDecimal.valueOf(28.4)));
        assertEquals(0, wd0.compareTo(BigDecimal.valueOf(313)));
        assertEquals(0, ws0.compareTo(BigDecimal.valueOf(2)));
        assertEquals(0, r0.compareTo(BigDecimal.valueOf(52)));
        assertEquals(0, tstm0.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, tcc0.compareTo(BigDecimal.valueOf(3)));
        assertEquals(0, lcc0.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, mcc0.compareTo(BigDecimal.valueOf(2)));
        assertEquals(0, hcc0.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, gust0.compareTo(BigDecimal.valueOf(6.9)));
        assertEquals(0, pmin0.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, pmax0.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, spp0.compareTo(BigDecimal.valueOf(-9)));
        assertEquals(0, pcat0.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, pmean0.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, pmedian0.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, wsymb0.compareTo(BigDecimal.valueOf(20)));
        assertEquals("20", new DecimalType(wsymb0.setScale(0, RoundingMode.DOWN)).toString());

        Forecast forecast1 = timeSeries1.getForecast(TIME.plusHours(1), 0).orElseThrow(AssertionError::new);

        BigDecimal msl1 = forecast1.getParameter(PRESSURE).orElseThrow(AssertionError::new);
        BigDecimal t1 = forecast1.getParameter(TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal vis1 = forecast1.getParameter(VISIBILITY).orElseThrow(AssertionError::new);
        BigDecimal wd1 = forecast1.getParameter(WIND_DIRECTION).orElseThrow(AssertionError::new);
        BigDecimal ws1 = forecast1.getParameter(WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal r1 = forecast1.getParameter(RELATIVE_HUMIDITY).orElseThrow(AssertionError::new);
        BigDecimal tstm1 = forecast1.getParameter(THUNDER_PROBABILITY).orElseThrow(AssertionError::new);
        BigDecimal tcc1 = forecast1.getParameter(TOTAL_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal lcc1 = forecast1.getParameter(LOW_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal mcc1 = forecast1.getParameter(MEDIUM_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal hcc1 = forecast1.getParameter(HIGH_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal gust1 = forecast1.getParameter(GUST).orElseThrow(AssertionError::new);
        BigDecimal pmin1 = forecast1.getParameter(PRECIPITATION_MIN).orElseThrow(AssertionError::new);
        BigDecimal pmax1 = forecast1.getParameter(PRECIPITATION_MAX).orElseThrow(AssertionError::new);
        BigDecimal spp1 = forecast1.getParameter(PERCENT_FROZEN).orElseThrow(AssertionError::new);
        BigDecimal pcat1 = forecast1.getParameter(PRECIPITATION_CATEGORY).orElseThrow(AssertionError::new);
        BigDecimal pmean1 = forecast1.getParameter(PRECIPITATION_MEAN).orElseThrow(AssertionError::new);
        BigDecimal pmedian1 = forecast1.getParameter(PRECIPITATION_MEDIAN).orElseThrow(AssertionError::new);
        BigDecimal wsymb1 = forecast1.getParameter(WEATHER_SYMBOL).orElseThrow(AssertionError::new);

        assertEquals(0, msl1.compareTo(BigDecimal.valueOf(1014.5)));
        assertEquals(0, t1.compareTo(BigDecimal.valueOf(19.2)));
        assertEquals(0, vis1.compareTo(BigDecimal.valueOf(30.3)));
        assertEquals(0, wd1.compareTo(BigDecimal.valueOf(307)));
        assertEquals(0, ws1.compareTo(BigDecimal.valueOf(2.5)));
        assertEquals(0, r1.compareTo(BigDecimal.valueOf(48)));
        assertEquals(0, tstm1.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, tcc1.compareTo(BigDecimal.valueOf(4)));
        assertEquals(0, lcc1.compareTo(BigDecimal.valueOf(1)));
        assertEquals(0, mcc1.compareTo(BigDecimal.valueOf(3)));
        assertEquals(0, hcc1.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, gust1.compareTo(BigDecimal.valueOf(7.9)));
        assertEquals(0, pmin1.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, pmax1.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, spp1.compareTo(BigDecimal.valueOf(-9)));
        assertEquals(0, pcat1.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, pmean1.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, pmedian1.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, wsymb1.compareTo(BigDecimal.valueOf(3)));

        Forecast forecast2 = timeSeries1.getForecast(TIME, 1).orElseThrow(AssertionError::new);

        BigDecimal msl2 = forecast2.getParameter(PRESSURE).orElseThrow(AssertionError::new);
        BigDecimal t2 = forecast2.getParameter(TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal vis2 = forecast2.getParameter(VISIBILITY).orElseThrow(AssertionError::new);
        BigDecimal wd2 = forecast2.getParameter(WIND_DIRECTION).orElseThrow(AssertionError::new);
        BigDecimal ws2 = forecast2.getParameter(WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal r2 = forecast2.getParameter(RELATIVE_HUMIDITY).orElseThrow(AssertionError::new);
        BigDecimal tstm2 = forecast2.getParameter(THUNDER_PROBABILITY).orElseThrow(AssertionError::new);
        BigDecimal tcc2 = forecast2.getParameter(TOTAL_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal lcc2 = forecast2.getParameter(LOW_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal mcc2 = forecast2.getParameter(MEDIUM_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal hcc2 = forecast2.getParameter(HIGH_CLOUD_COVER).orElseThrow(AssertionError::new);
        BigDecimal gust2 = forecast2.getParameter(GUST).orElseThrow(AssertionError::new);
        BigDecimal pmin2 = forecast2.getParameter(PRECIPITATION_MIN).orElseThrow(AssertionError::new);
        BigDecimal pmax2 = forecast2.getParameter(PRECIPITATION_MAX).orElseThrow(AssertionError::new);
        BigDecimal spp2 = forecast2.getParameter(PERCENT_FROZEN).orElseThrow(AssertionError::new);
        BigDecimal pcat2 = forecast2.getParameter(PRECIPITATION_CATEGORY).orElseThrow(AssertionError::new);
        BigDecimal pmean2 = forecast2.getParameter(PRECIPITATION_MEAN).orElseThrow(AssertionError::new);
        BigDecimal pmedian2 = forecast2.getParameter(PRECIPITATION_MEDIAN).orElseThrow(AssertionError::new);
        BigDecimal wsymb2 = forecast2.getParameter(WEATHER_SYMBOL).orElseThrow(AssertionError::new);

        assertEquals(0, msl2.compareTo(BigDecimal.valueOf(1014.5)));
        assertEquals(0, t2.compareTo(BigDecimal.valueOf(19.2)));
        assertEquals(0, vis2.compareTo(BigDecimal.valueOf(30.3)));
        assertEquals(0, wd2.compareTo(BigDecimal.valueOf(307)));
        assertEquals(0, ws2.compareTo(BigDecimal.valueOf(2.5)));
        assertEquals(0, r2.compareTo(BigDecimal.valueOf(48)));
        assertEquals(0, tstm2.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, tcc2.compareTo(BigDecimal.valueOf(4)));
        assertEquals(0, lcc2.compareTo(BigDecimal.valueOf(1)));
        assertEquals(0, mcc2.compareTo(BigDecimal.valueOf(3)));
        assertEquals(0, hcc2.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, gust2.compareTo(BigDecimal.valueOf(7.9)));
        assertEquals(0, pmin2.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, pmax2.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, spp2.compareTo(BigDecimal.valueOf(-9)));
        assertEquals(0, pcat2.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, pmean2.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, pmedian2.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, wsymb2.compareTo(BigDecimal.valueOf(3)));
    }

    @Test
    public void forecastAggregatorTest() {
        assertNotNull(timeSeries1);
        assertNotNull(timeSeries2);
        BigDecimal maxTempT1F0 = ForecastAggregator.max(timeSeries1, 0, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal minTempT1F0 = ForecastAggregator.min(timeSeries1, 0, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal maxWindT1F0 = ForecastAggregator.max(timeSeries1, 0, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal minWindT1F0 = ForecastAggregator.min(timeSeries1, 0, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal totalPrecipT1F0 = ForecastAggregator.total(timeSeries1, 0, PRECIPITATION_MEAN)
                .orElseThrow(AssertionError::new);
        BigDecimal noonPressureT1F0 = ForecastAggregator.noonOrFirst(timeSeries1, 0, PRESSURE)
                .orElseThrow(AssertionError::new);

        assertEquals(0, maxTempT1F0.compareTo(BigDecimal.valueOf(20.8)));
        assertEquals(0, minTempT1F0.compareTo(BigDecimal.valueOf(12.2)));
        assertEquals(0, maxWindT1F0.compareTo(BigDecimal.valueOf(4)));
        assertEquals(0, minWindT1F0.compareTo(BigDecimal.valueOf(0.6)));
        assertEquals(0, totalPrecipT1F0.compareTo(BigDecimal.valueOf(0)));
        assertEquals(0, noonPressureT1F0.compareTo(BigDecimal.valueOf(1015.6)));

        BigDecimal maxTempT1F9 = ForecastAggregator.max(timeSeries1, 9, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal minTempT1F9 = ForecastAggregator.min(timeSeries1, 9, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal maxWindT1F9 = ForecastAggregator.max(timeSeries1, 9, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal minWindT1F9 = ForecastAggregator.min(timeSeries1, 9, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal totalPrecipT1F9 = ForecastAggregator.total(timeSeries1, 9, PRECIPITATION_MEAN)
                .orElseThrow(AssertionError::new);
        BigDecimal noonPressureT1F9 = ForecastAggregator.noonOrFirst(timeSeries1, 9, PRESSURE)
                .orElseThrow(AssertionError::new);

        assertEquals(0, maxTempT1F9.compareTo(BigDecimal.valueOf(21.5)));
        assertEquals(0, minTempT1F9.compareTo(BigDecimal.valueOf(14.6)));
        assertEquals(0, maxWindT1F9.compareTo(BigDecimal.valueOf(1.6)));
        assertEquals(0, minWindT1F9.compareTo(BigDecimal.valueOf(1.4)));
        assertEquals(0, totalPrecipT1F9.compareTo(BigDecimal.valueOf(3.6)));
        assertEquals(0, noonPressureT1F9.compareTo(BigDecimal.valueOf(1016.2)));

        BigDecimal maxTempT2F0 = ForecastAggregator.max(timeSeries2, 0, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal minTempT2F0 = ForecastAggregator.min(timeSeries2, 0, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal maxWindT2F0 = ForecastAggregator.max(timeSeries2, 0, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal minWindT2F0 = ForecastAggregator.min(timeSeries2, 0, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal totalPrecipT2F0 = ForecastAggregator.total(timeSeries2, 0, PRECIPITATION_MEAN)
                .orElseThrow(AssertionError::new);
        BigDecimal noonPressureT2F0 = ForecastAggregator.noonOrFirst(timeSeries2, 0, PRESSURE)
                .orElseThrow(AssertionError::new);

        assertEquals(0, maxTempT2F0.compareTo(BigDecimal.valueOf(22.5)));
        assertEquals(0, minTempT2F0.compareTo(BigDecimal.valueOf(9.7)));
        assertEquals(0, maxWindT2F0.compareTo(BigDecimal.valueOf(5.7)));
        assertEquals(0, minWindT2F0.compareTo(BigDecimal.valueOf(2)));
        assertEquals(0, totalPrecipT2F0.compareTo(BigDecimal.valueOf(0.3)));
        assertEquals(0, noonPressureT2F0.compareTo(BigDecimal.valueOf(1013.7)));

        BigDecimal maxTempT2F9 = ForecastAggregator.max(timeSeries2, 9, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal minTempT2F9 = ForecastAggregator.min(timeSeries2, 9, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal maxWindT2F9 = ForecastAggregator.max(timeSeries2, 9, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal minWindT2F9 = ForecastAggregator.min(timeSeries2, 9, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal totalPrecipT2F9 = ForecastAggregator.total(timeSeries2, 9, PRECIPITATION_MEAN)
                .orElseThrow(AssertionError::new);
        BigDecimal noonPressureT2F9 = ForecastAggregator.noonOrFirst(timeSeries2, 9, PRESSURE)
                .orElseThrow(AssertionError::new);

        assertEquals(0, maxTempT2F9.compareTo(BigDecimal.valueOf(22.4)));
        assertEquals(0, minTempT2F9.compareTo(BigDecimal.valueOf(15.2)));
        assertEquals(0, maxWindT2F9.compareTo(BigDecimal.valueOf(1.3)));
        assertEquals(0, minWindT2F9.compareTo(BigDecimal.valueOf(0.7)));
        assertEquals(0, totalPrecipT2F9.compareTo(BigDecimal.valueOf(2.4)));
        assertEquals(0, noonPressureT2F9.compareTo(BigDecimal.valueOf(1014.6)));

        BigDecimal maxTempT3F0 = ForecastAggregator.max(timeSeries3, 0, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal minTempT3F0 = ForecastAggregator.min(timeSeries3, 0, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal maxWindT3F0 = ForecastAggregator.max(timeSeries3, 0, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal minWindT3F0 = ForecastAggregator.min(timeSeries3, 0, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal totalPrecipT3F0 = ForecastAggregator.total(timeSeries3, 0, PRECIPITATION_MEAN)
                .orElseThrow(AssertionError::new);
        BigDecimal noonPressureT3F0 = ForecastAggregator.noonOrFirst(timeSeries3, 0, PRESSURE)
                .orElseThrow(AssertionError::new);

        assertEquals(0, maxTempT3F0.compareTo(BigDecimal.valueOf(18.6)));
        assertEquals(0, minTempT3F0.compareTo(BigDecimal.valueOf(14.1)));
        assertEquals(0, maxWindT3F0.compareTo(BigDecimal.valueOf(6)));
        assertEquals(0, minWindT3F0.compareTo(BigDecimal.valueOf(4.9)));
        assertEquals(0, totalPrecipT3F0.compareTo(BigDecimal.valueOf(0.5)));
        assertEquals(0, noonPressureT3F0.compareTo(BigDecimal.valueOf(1012.6)));

        BigDecimal maxTempT3F9 = ForecastAggregator.max(timeSeries3, 9, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal minTempT3F9 = ForecastAggregator.min(timeSeries3, 9, TEMPERATURE).orElseThrow(AssertionError::new);
        BigDecimal maxWindT3F9 = ForecastAggregator.max(timeSeries3, 9, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal minWindT3F9 = ForecastAggregator.min(timeSeries3, 9, WIND_SPEED).orElseThrow(AssertionError::new);
        BigDecimal totalPrecipT3F9 = ForecastAggregator.total(timeSeries3, 9, PRECIPITATION_MEAN)
                .orElseThrow(AssertionError::new);
        BigDecimal noonPressureT3F9 = ForecastAggregator.noonOrFirst(timeSeries3, 9, PRESSURE)
                .orElseThrow(AssertionError::new);

        assertEquals(0, maxTempT3F9.compareTo(BigDecimal.valueOf(22.6)));
        assertEquals(0, minTempT3F9.compareTo(BigDecimal.valueOf(15.5)));
        assertEquals(0, maxWindT3F9.compareTo(BigDecimal.valueOf(1)));
        assertEquals(0, minWindT3F9.compareTo(BigDecimal.valueOf(0.9)));
        assertEquals(0, totalPrecipT3F9.compareTo(BigDecimal.valueOf(3.6)));
        assertEquals(0, noonPressureT3F9.compareTo(BigDecimal.valueOf(1016.6)));
    }
}
