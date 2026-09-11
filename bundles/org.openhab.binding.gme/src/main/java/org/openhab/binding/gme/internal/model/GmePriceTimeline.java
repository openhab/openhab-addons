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
 * Utility methods for mapping GME hourly entries onto a real timeline.
 *
 * @author Andrea Riela - Initial contribution
 */
@NonNullByDefault
public final class GmePriceTimeline {

    private GmePriceTimeline() {
    }

    public static ZonedDateTime getStartTime(GmePriceEntry entry, ZoneId zoneId) {
        return entry.flowDate().atStartOfDay(zoneId).plusHours(entry.hour() - 1L);
    }

    public static int getExpectedHours(LocalDate date, ZoneId zoneId) {
        Instant start = date.atStartOfDay(zoneId).toInstant();
        Instant end = date.plusDays(1).atStartOfDay(zoneId).toInstant();
        return Math.toIntExact(Duration.between(start, end).toHours());
    }

    public static boolean isCompleteDailySet(List<GmePriceEntry> prices, LocalDate date, ZoneId zoneId) {
        int expectedHours = getExpectedHours(date, zoneId);

        if (prices.size() != expectedHours) {
            return false;
        }

        Set<Integer> hours = new HashSet<>();

        for (GmePriceEntry entry : prices) {
            if (!date.equals(entry.flowDate()) || entry.hour() < 1 || entry.hour() > expectedHours
                    || !hours.add(entry.hour())) {
                return false;
            }
        }

        return hours.size() == expectedHours;
    }

    public static Optional<GmePriceEntry> findCurrentPrice(List<GmePriceEntry> prices, Instant now, ZoneId zoneId) {
        List<GmePriceEntry> timeline = prices.stream()
                .sorted(Comparator.comparing(entry -> getStartTime(entry, zoneId))).toList();

        for (int i = 0; i < timeline.size(); i++) {
            GmePriceEntry entry = timeline.get(i);
            Instant start = getStartTime(entry, zoneId).toInstant();

            Instant end;
            if (i + 1 < timeline.size()) {
                end = getStartTime(timeline.get(i + 1), zoneId).toInstant();
            } else {
                end = start.plusSeconds(3600);
            }

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
}
