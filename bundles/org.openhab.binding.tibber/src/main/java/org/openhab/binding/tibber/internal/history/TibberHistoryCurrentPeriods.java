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
package org.openhab.binding.tibber.internal.history;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.measure.Quantity;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.types.State;

import com.google.gson.JsonObject;

/**
 * The {@link TibberHistoryCurrentPeriods} computes the completed base of the current (running) periods from the
 * DAILY history series.
 * <p>
 * Live values delivered by the Tibber websocket only describe today. To publish the current week, month and year,
 * the values of the completed days are summed per purpose and used as base which the live values are added to.
 * This class is a pure helper: it has no OSGi dependencies and does not read the clock on its own - the reference
 * time is passed in.
 * <p>
 * Values are stored as {@code "<number> <unit>"} or as {@code "<number>"} if no unit is known (e.g. cost in an
 * unknown currency). The unit of the last parsed entry is used for the resulting state; entries which cannot be
 * parsed are ignored.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class TibberHistoryCurrentPeriods {

    private static final List<TibberHistory.TimeWindow> CURRENT_PERIOD_WINDOWS = List
            .of(TibberHistory.TimeWindow.WEEKLY, TibberHistory.TimeWindow.MONTHLY, TibberHistory.TimeWindow.ANNUAL);

    private static final List<String> PURPOSES = List.of(TibberHistorySeries.PURPOSE_CONSUMPTION,
            TibberHistorySeries.PURPOSE_COST, TibberHistorySeries.PURPOSE_PRODUCTION);

    private TibberHistoryCurrentPeriods() {
    }

    /**
     * Immutable result of {@link #compute(TibberHistorySeries, ZoneId, Instant)}: the summed values per window and
     * purpose plus the timestamp of the newest DAILY entry used as freshness marker.
     */
    public static final class Values {
        private final @Nullable Instant newestDailyEntry;
        private final Map<TibberHistory.TimeWindow, Map<String, State>> values;

        Values(@Nullable Instant newestDailyEntry, Map<TibberHistory.TimeWindow, Map<String, State>> values) {
            this.newestDailyEntry = newestDailyEntry;
            Map<TibberHistory.TimeWindow, Map<String, State>> copy = new EnumMap<>(TibberHistory.TimeWindow.class);
            values.forEach((window, windowValues) -> copy.put(window, Map.copyOf(windowValues)));
            this.values = Map.copyOf(copy);
        }

        /**
         * @return timestamp of the newest DAILY entry or {@code null} if the series is empty
         */
        public @Nullable Instant newestDailyEntry() {
            return newestDailyEntry;
        }

        /**
         * Checks if the DAILY series contains an entry for yesterday or later.
         *
         * @param zone time zone of the binding
         * @param now reference time
         * @return {@code true} if the completed base covers at least all days before today
         */
        public boolean isFresh(ZoneId zone, Instant now) {
            Instant newest = newestDailyEntry;
            return newest != null
                    && !newest.isBefore(now.atZone(zone).truncatedTo(ChronoUnit.DAYS).minusDays(1).toInstant());
        }

        /**
         * @param window time window
         * @param purpose one of {@link TibberHistorySeries#PURPOSE_CONSUMPTION},
         *            {@link TibberHistorySeries#PURPOSE_COST} or {@link TibberHistorySeries#PURPOSE_PRODUCTION}
         * @return summed state of the completed days of the running period or {@code null} if there is no data
         */
        public @Nullable State get(TibberHistory.TimeWindow window, String purpose) {
            Map<String, State> windowValues = values.get(window);
            return windowValues == null ? null : windowValues.get(purpose);
        }
    }

    /**
     * Sums the DAILY entries from the start of the running week, month and year up to (but excluding) today.
     *
     * @param daily DAILY history series
     * @param zone time zone of the binding
     * @param now reference time
     * @return immutable values of the completed days per window and purpose
     */
    public static Values compute(TibberHistorySeries daily, ZoneId zone, Instant now) {
        ZonedDateTime nowZdt = now.atZone(zone);
        Instant startOfToday = nowZdt.truncatedTo(ChronoUnit.DAYS).toInstant();
        Map<TibberHistory.TimeWindow, Map<String, State>> values = new EnumMap<>(TibberHistory.TimeWindow.class);

        for (TibberHistory.TimeWindow window : CURRENT_PERIOD_WINDOWS) {
            Instant periodStart = TibberHistoryAggregator.currentPeriodStart(window, nowZdt);
            // Defensive copy of the live-backed tailMap view to avoid ConcurrentModificationException
            // if another thread merges new data into the series while we iterate.
            SortedMap<Instant, JsonObject> candidates = new TreeMap<>(daily.tailMap(periodStart));
            Map<String, State> windowValues = new HashMap<>();
            for (String purpose : PURPOSES) {
                @Nullable
                BigDecimal sum = null;
                @Nullable
                String unit = null;
                for (Map.Entry<Instant, JsonObject> candidate : candidates.entrySet()) {
                    if (!candidate.getKey().isBefore(startOfToday)) {
                        continue;
                    }
                    JsonObject data = candidate.getValue();
                    if (!data.has(purpose)) {
                        continue;
                    }
                    @Nullable
                    ParsedQuantity parsed = parseQuantity(data.get(purpose).getAsString());
                    if (parsed == null) {
                        continue;
                    }
                    sum = sum == null ? parsed.value() : sum.add(parsed.value());
                    unit = parsed.unit();
                }
                if (sum != null) {
                    windowValues.put(purpose, createState(sum, unit));
                }
            }
            values.put(window, windowValues);
        }

        Instant newestDailyEntry = daily.isEmpty() ? null : daily.lastKey();
        return new Values(newestDailyEntry, values);
    }

    /**
     * Adds the live value of today to the completed base of the running period.
     *
     * @param completed summed value of the completed days of the running period
     * @param today live value of today
     * @return sum of both values, or {@code null} if the states are of an unsupported type or cannot be converted
     *         to a common unit
     */
    public static @Nullable State add(@Nullable State completed, @Nullable State today) {
        if (completed instanceof QuantityType<?> completedQuantity && today instanceof QuantityType<?> todayQuantity) {
            return addQuantities(completedQuantity, todayQuantity);
        }
        if (completed instanceof DecimalType completedDecimal && today instanceof DecimalType todayDecimal) {
            BigDecimal sum = completedDecimal.toBigDecimal().add(todayDecimal.toBigDecimal());
            return DecimalType.valueOf(sum.toPlainString());
        }
        return null;
    }

    private static <T extends Quantity<T>> @Nullable State addQuantities(QuantityType<T> completed,
            QuantityType<?> today) {
        QuantityType<?> converted = today.toUnit(completed.getUnit());
        if (converted == null) {
            return null;
        }
        return new QuantityType<>(completed.toBigDecimal().add(converted.toBigDecimal()), completed.getUnit());
    }

    private static State createState(BigDecimal sum, @Nullable String unit) {
        String value = sum.toPlainString();
        return unit == null ? DecimalType.valueOf(value) : QuantityType.valueOf(value + " " + unit);
    }

    private static @Nullable ParsedQuantity parseQuantity(String raw) {
        int lastSpace = raw.lastIndexOf(' ');
        String valuePart;
        @Nullable
        String unitPart;
        if (lastSpace >= 0) {
            valuePart = raw.substring(0, lastSpace);
            String candidate = raw.substring(lastSpace + 1).trim();
            unitPart = candidate.isEmpty() ? null : candidate;
        } else {
            valuePart = raw;
            unitPart = null;
        }
        try {
            return new ParsedQuantity(new BigDecimal(valuePart.trim()), unitPart);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private record ParsedQuantity(BigDecimal value, @Nullable String unit) {
    }
}
