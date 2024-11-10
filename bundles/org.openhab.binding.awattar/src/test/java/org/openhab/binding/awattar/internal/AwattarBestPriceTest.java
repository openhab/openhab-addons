/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

package org.openhab.binding.awattar.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.junit.jupiter.api.Test;
import org.openhab.binding.awattar.internal.handler.TimeRange;

/**
 * The {@link AwattarBestPriceTest} contains tests for the
 * {@link AwattarConsecutiveBestPriceResult} and {@link AwattarNonConsecutiveBestPriceResult} logic.
 *
 * @author Thomas Leber - Initial contribution
 */
public class AwattarBestPriceTest {

    private ZoneId zoneId = ZoneId.of("GMT");

    public static ZonedDateTime getCalendarForHour(int hour, ZoneId zone) {
        return ZonedDateTime.ofInstant(Instant.ofEpochSecond(1731283200L), zone).truncatedTo(ChronoUnit.HOURS)
                .plusHours(hour);
    }

    public synchronized SortedSet<AwattarPrice> getPrices() {
        SortedSet<AwattarPrice> prices = new TreeSet<>(Comparator.comparing(AwattarPrice::timerange));

        prices.add(new AwattarPrice(103.87, 103.87, 103.87, 103.87, new TimeRange(1731283200000L, 1731286800000L)));
        prices.add(new AwattarPrice(100.06, 100.06, 100.06, 100.06, new TimeRange(1731286800000L, 1731290400000L)));
        prices.add(new AwattarPrice(99.06, 99.06, 99.06, 99.06, new TimeRange(1731290400000L, 1731294000000L)));
        prices.add(new AwattarPrice(99.12, 99.12, 99.12, 99.12, new TimeRange(1731294000000L, 1731297600000L)));
        prices.add(new AwattarPrice(105.16, 105.16, 105.16, 105.16, new TimeRange(1731297600000L, 1731301200000L)));
        prices.add(new AwattarPrice(124.96, 124.96, 124.96, 124.96, new TimeRange(1731301200000L, 1731304800000L)));
        prices.add(new AwattarPrice(143.91, 143.91, 143.91, 143.91, new TimeRange(1731304800000L, 1731308400000L)));
        prices.add(new AwattarPrice(141.95, 141.95, 141.95, 141.95, new TimeRange(1731308400000L, 1731312000000L)));
        prices.add(new AwattarPrice(135.95, 135.95, 135.95, 135.95, new TimeRange(1731312000000L, 1731315600000L)));
        prices.add(new AwattarPrice(130.39, 130.39, 130.39, 130.39, new TimeRange(1731315600000L, 1731319200000L)));
        prices.add(new AwattarPrice(124.5, 124.5, 124.5, 124.5, new TimeRange(1731319200000L, 1731322800000L)));
        prices.add(new AwattarPrice(119.79, 119.79, 119.79, 119.79, new TimeRange(1731322800000L, 1731326400000L)));
        prices.add(new AwattarPrice(131.13, 131.13, 131.13, 131.13, new TimeRange(1731326400000L, 1731330000000L)));
        prices.add(new AwattarPrice(133.72, 133.72, 133.72, 133.72, new TimeRange(1731330000000L, 1731333600000L)));
        prices.add(new AwattarPrice(141.58, 141.58, 141.58, 141.58, new TimeRange(1731333600000L, 1731337200000L)));
        prices.add(new AwattarPrice(146.94, 146.94, 146.94, 146.94, new TimeRange(1731337200000L, 1731340800000L)));
        prices.add(new AwattarPrice(150.08, 150.08, 150.08, 150.08, new TimeRange(1731340800000L, 1731344400000L)));
        prices.add(new AwattarPrice(146.9, 146.9, 146.9, 146.9, new TimeRange(1731344400000L, 1731348000000L)));
        prices.add(new AwattarPrice(139.87, 139.87, 139.87, 139.87, new TimeRange(1731348000000L, 1731351600000L)));
        prices.add(new AwattarPrice(123.78, 123.78, 123.78, 123.78, new TimeRange(1731351600000L, 1731355200000L)));
        prices.add(new AwattarPrice(119.02, 119.02, 119.02, 119.02, new TimeRange(1731355200000L, 1731358800000L)));
        prices.add(new AwattarPrice(116.87, 116.87, 116.87, 116.87, new TimeRange(1731358800000L, 1731362400000L)));
        prices.add(new AwattarPrice(109.72, 109.72, 109.72, 109.72, new TimeRange(1731362400000L, 1731366000000L)));
        prices.add(new AwattarPrice(107.89, 107.89, 107.89, 107.89, new TimeRange(1731366000000L, 1731369600000L)));

        return prices;
    }

    @Test
    void AwattarConsecutiveBestPriceResult() {
        int length = 8;

        List<AwattarPrice> range = new ArrayList<>(getPrices());

        range.sort(Comparator.comparing(AwattarPrice::timerange));
        AwattarConsecutiveBestPriceResult result = new AwattarConsecutiveBestPriceResult(range, length, zoneId);
        assertEquals("00,01,02,03,04,05,06,07", result.getHours());
    }

    @Test
    void AwattarNonConsecutiveBestPriceResult_nonInverted() {
        int length = 6;
        boolean inverted = false;

        List<AwattarPrice> range = new ArrayList<>(getPrices());

        range.sort(Comparator.comparing(AwattarPrice::timerange));
        AwattarNonConsecutiveBestPriceResult result = new AwattarNonConsecutiveBestPriceResult(range, length, inverted,
                zoneId);
        assertEquals("00,01,02,03,04,23", result.getHours());
    }

    @Test
    void AwattarNonConsecutiveBestPriceResult_inverted() {
        int length = 4;
        boolean inverted = true;

        List<AwattarPrice> range = new ArrayList<>(getPrices());

        range.sort(Comparator.comparing(AwattarPrice::timerange));
        AwattarNonConsecutiveBestPriceResult result = new AwattarNonConsecutiveBestPriceResult(range, length, inverted,
                zoneId);
        assertEquals("06,15,16,17", result.getHours());
    }
}
