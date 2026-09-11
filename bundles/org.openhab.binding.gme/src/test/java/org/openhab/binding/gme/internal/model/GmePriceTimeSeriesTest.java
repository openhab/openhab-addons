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
    void buildsNormalDayTimeSeries() {
        LocalDate date = LocalDate.of(2026, 9, 10);

        TimeSeries series = GmePriceTimeSeries.build(completeDay(date, 24), ROME,
                price -> new QuantityType<>(price.priceKWh(), priceUnit()));

        assertEquals(TimeSeries.Policy.REPLACE, series.getPolicy());
        assertEquals(24, series.size());
        assertEquals(date.atStartOfDay(ROME).toInstant(), series.getBegin());
        assertEquals(date.plusDays(1).atStartOfDay(ROME).minusHours(1).toInstant(), series.getEnd());
    }

    @Test
    void buildsSpringDstTimeSeries() {
        LocalDate date = LocalDate.of(2026, 3, 29);

        TimeSeries series = GmePriceTimeSeries.build(completeDay(date, 23), ROME,
                price -> new QuantityType<>(price.priceKWh(), priceUnit()));

        assertEquals(23, series.size());
        assertEquals(date.atStartOfDay(ROME).toInstant(), series.getBegin());
        assertEquals(date.plusDays(1).atStartOfDay(ROME).minusHours(1).toInstant(), series.getEnd());
    }

    @Test
    void buildsAutumnDstTimeSeries() {
        LocalDate date = LocalDate.of(2026, 10, 25);

        TimeSeries series = GmePriceTimeSeries.build(completeDay(date, 25), ROME,
                price -> new QuantityType<>(price.priceKWh(), priceUnit()));

        assertEquals(25, series.size());
        assertEquals(date.atStartOfDay(ROME).toInstant(), series.getBegin());
        assertEquals(date.plusDays(1).atStartOfDay(ROME).minusHours(1).toInstant(), series.getEnd());
    }

    private static Unit<?> priceUnit() {
        Unit<?> unit = UnitUtils.parseUnit("EUR/kWh");
        return unit != null ? unit : CurrencyUnits.BASE_ENERGY_PRICE;
    }

    private static List<GmePriceEntry> completeDay(LocalDate date, int hours) {
        List<GmePriceEntry> prices = new ArrayList<>();

        for (int hour = 1; hour <= hours; hour++) {
            prices.add(new GmePriceEntry(date, hour, "MGP", "PUN", new BigDecimal("100.000000"), 0, null));
        }

        return prices;
    }
}
