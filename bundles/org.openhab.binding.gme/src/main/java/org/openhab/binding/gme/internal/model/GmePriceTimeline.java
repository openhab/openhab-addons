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

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Utility methods for mapping GME market intervals onto a real timeline.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public final class GmePriceTimeline {

    private GmePriceTimeline() {
    }

    public static ZonedDateTime getStartTime(GmePriceEntry entry, ZoneId zoneId) {
        int period = getPeriodIndex(entry);
        long offsetMinutes = (long) (period - 1) * entry.granularity().minutes();
        return entry.flowDate().atStartOfDay(zoneId).plusMinutes(offsetMinutes);
    }

    public static int getExpectedPeriods(LocalDate date, ZoneId zoneId, GmeGranularity granularity) {
        Instant start = date.atStartOfDay(zoneId).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zoneId).toInstant();
        long minutes = Duration.between(start, end).toMinutes();
        return Math.toIntExact(minutes / granularity.minutes());
    }

    public static int getExpectedHours(LocalDate date, ZoneId zoneId) {
        return getExpectedPeriods(date, zoneId, GmeGranularity.PT60);
    }

    public static boolean isCompleteDailySet(List<GmePriceEntry> prices, LocalDate date, ZoneId zoneId) {
        return isCompleteDailySet(prices, date, zoneId, GmeGranularity.PT60);
    }

    public static boolean isCompleteDailySet(List<GmePriceEntry> prices, LocalDate date, ZoneId zoneId,
            GmeGranularity granularity) {
        int expectedPeriods = getExpectedPeriods(date, zoneId, granularity);
        if (prices.size() != expectedPeriods) {
            return false;
        }

        Set<Integer> periods = new HashSet<>();
        for (GmePriceEntry entry : prices) {
            if (!date.equals(entry.flowDate()) || entry.granularity() != granularity) {
                return false;
            }

            int period;
            try {
                period = getPeriodIndex(entry);
            } catch (IllegalArgumentException e) {
                return false;
            }

            if (period < 1 || period > expectedPeriods || !periods.add(period)) {
                return false;
            }
        }

        return periods.size() == expectedPeriods;
    }

    public static Optional<GmePriceEntry> findCurrentPrice(List<GmePriceEntry> prices, Instant now, ZoneId zoneId) {
        List<GmePriceEntry> timeline = prices.stream()
                .sorted(Comparator.comparing(entry -> getStartTime(entry, zoneId))).toList();

        for (int i = 0; i < timeline.size(); i++) {
            GmePriceEntry entry = timeline.get(i);
            Instant start = getStartTime(entry, zoneId).toInstant();
            Instant end = i + 1 < timeline.size() ? getStartTime(timeline.get(i + 1), zoneId).toInstant()
                    : start.plus(entry.granularity().duration());

            if (!now.isBefore(start) && now.isBefore(end)) {
                return Optional.of(entry);
            }
        }

        return Optional.empty();
    }

    public static Optional<GmePriceEntry> findNextPrice(List<GmePriceEntry> prices, Instant now, ZoneId zoneId) {
        return prices.stream().filter(entry -> getStartTime(entry, zoneId).toInstant().isAfter(now))
                .min(Comparator.comparing(entry -> getStartTime(entry, zoneId)));
    }

    private static int getPeriodIndex(GmePriceEntry entry) {
        if (entry.period() > 0) {
            return entry.period();
        }
        if (entry.granularity() == GmeGranularity.PT60) {
            return entry.hour();
        }
        throw new IllegalArgumentException("Missing GME flow period for " + entry.granularity());
    }
}
