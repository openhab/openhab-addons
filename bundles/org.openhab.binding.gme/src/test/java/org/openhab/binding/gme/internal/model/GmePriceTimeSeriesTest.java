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
package org.openhab.binding.gme.internal.model;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.measure.Unit;

import org.junit.jupiter.api.Test;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.CurrencyUnits;
import org.openhab.core.types.TimeSeries;
import org.openhab.core.types.util.UnitUtils;

class GmePriceTimeSeriesTest {

    private static final ZoneId ROME = ZoneId.of("Europe/Rome");

    @Test
    void buildsHourlyTimeSeries() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        TimeSeries series = build(date, GmeGranularity.PT60);

        assertEquals(24, series.size());
        assertEquals(date.atStartOfDay(ROME).toInstant(), series.getBegin());
        assertEquals(date.plusDays(1).atStartOfDay(ROME).minusHours(1).toInstant(), series.getEnd());
    }

    @Test
    void buildsHalfHourlyTimeSeries() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        TimeSeries series = build(date, GmeGranularity.PT30);

        assertEquals(48, series.size());
        assertEquals(date.atStartOfDay(ROME).toInstant(), series.getBegin());
        assertEquals(date.plusDays(1).atStartOfDay(ROME).minusMinutes(30).toInstant(), series.getEnd());
    }

    @Test
    void buildsQuarterHourlyTimeSeries() {
        LocalDate date = LocalDate.of(2026, 9, 10);
        TimeSeries series = build(date, GmeGranularity.PT15);

        assertEquals(96, series.size());
        assertEquals(date.atStartOfDay(ROME).toInstant(), series.getBegin());
        assertEquals(date.plusDays(1).atStartOfDay(ROME).minusMinutes(15).toInstant(), series.getEnd());
    }

    @Test
    void buildsQuarterHourlyDstTimeSeries() {
        LocalDate spring = LocalDate.of(2026, 3, 29);
        LocalDate autumn = LocalDate.of(2026, 10, 25);

        assertEquals(92, build(spring, GmeGranularity.PT15).size());
        assertEquals(100, build(autumn, GmeGranularity.PT15).size());
    }

    private static TimeSeries build(LocalDate date, GmeGranularity granularity) {
        return GmePriceTimeSeries.build(completeDay(date, granularity), ROME,
                price -> new QuantityType<>(price.priceKWh(), priceUnit()));
    }

    private static Unit<?> priceUnit() {
        Unit<?> unit = UnitUtils.parseUnit("EUR/kWh");
        return unit != null ? unit : CurrencyUnits.BASE_ENERGY_PRICE;
    }

    private static List<GmePriceEntry> completeDay(LocalDate date, GmeGranularity granularity) {
        int periods = GmePriceTimeline.getExpectedPeriods(date, ROME, granularity);
        List<GmePriceEntry> prices = new ArrayList<>();

        for (int period = 1; period <= periods; period++) {
            int hour = ((period - 1) * granularity.minutes()) / 60 + 1;
            prices.add(new GmePriceEntry(date, hour, "MGP", "PUN", new BigDecimal("100.000000"), period, granularity,
                    null));
        }

        return prices;
    }
}
