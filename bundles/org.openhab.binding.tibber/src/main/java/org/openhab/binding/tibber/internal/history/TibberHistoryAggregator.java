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
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.SortedMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

/**
 * The {@link TibberHistoryAggregator} fills the gap in coarser time-window series
 * caused by the Tibber API only returning <em>completed</em> periods.
 * <p>
 * For example, an ANNUAL query executed in May 2026 returns data only up to end-2025.
 * This class uses already-fetched finer-resolution data (e.g. MONTHLY) to compute
 * a synthetic "year-to-date" entry for the coarser window (ANNUAL) and injects it
 * into a copy of the coarser series.
 * <p>
 * Aggregation rules:
 * <ul>
 * <li>MONTHLY → ANNUAL: sum all monthly entries whose timestamp falls in the current year</li>
 * <li>WEEKLY → MONTHLY: sum all weekly entries whose timestamp falls in the current month</li>
 * <li>DAILY → WEEKLY: sum all daily entries whose timestamp falls in the current ISO week</li>
 * </ul>
 * Synthetic entries are <strong>not</strong> written back to the persistent store —
 * they are transient and will be superseded by real API data once the period closes.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class TibberHistoryAggregator {

    private final Logger logger = LoggerFactory.getLogger(TibberHistoryAggregator.class);
    private final ZoneId zoneId;

    /**
     * @param zoneId timezone used to determine period boundaries (year/month/week start)
     */
    public TibberHistoryAggregator(ZoneId zoneId) {
        this.zoneId = zoneId;
    }

    /**
     * Computes a synthetic entry for the current (incomplete) period of the coarser window
     * by summing consumption, cost and production values from the finer-resolution series.
     * The result is injected into a copy of the coarser series — the original is not mutated.
     *
     * @param finer the finer resolution (data source)
     * @param coarser the coarser resolution (target — the one with the gap)
     * @param finerSeries fetched series for the finer window
     * @param coarserSeries fetched series for the coarser window
     * @return enriched coarser series with a synthetic entry for the current period,
     *         or the original coarser series unchanged if no finer data is available
     */
    public TibberHistorySeries fillCurrentPeriod(TibberHistory.TimeWindow finer, TibberHistory.TimeWindow coarser,
            TibberHistorySeries finerSeries, TibberHistorySeries coarserSeries) {
        if (finerSeries.isEmpty()) {
            logger.debug("fillCurrentPeriod: no {} data available — skipping aggregation for {}", finer, coarser);
            return coarserSeries;
        }

        Instant now = Instant.now();
        ZonedDateTime nowZdt = now.atZone(zoneId);

        // Determine the start of the current (incomplete) coarser period
        Instant periodStart = currentPeriodStart(coarser, nowZdt);

        // Collect all finer entries that belong to the current coarser period
        SortedMap<Instant, JsonObject> candidates = finerSeries.tailMap(periodStart);
        if (candidates.isEmpty()) {
            logger.debug("fillCurrentPeriod: no {} entries found after {} — gap stays unfilled", finer, periodStart);
            return coarserSeries;
        }

        // Aggregate: sum consumption, cost, production
        BigDecimal totalConsumption = BigDecimal.ZERO;
        BigDecimal totalCost = BigDecimal.ZERO;
        BigDecimal totalProduction = BigDecimal.ZERO;
        @Nullable
        String consumptionUnit = null;
        @Nullable
        String costUnit = null;
        @Nullable
        String productionUnit = null;

        for (Map.Entry<Instant, JsonObject> entry : candidates.entrySet()) {
            // Only include entries that are strictly before now (exclude the current, possibly
            // incomplete sub-period — e.g. today's partial day when aggregating DAILY→WEEKLY)
            if (!entry.getKey().isBefore(now.truncatedTo(ChronoUnit.HOURS))) {
                continue;
            }
            JsonObject data = entry.getValue();
            if (data.has(TibberHistorySeries.PURPOSE_CONSUMPTION)) {
                String raw = data.get(TibberHistorySeries.PURPOSE_CONSUMPTION).getAsString();
                ParsedQuantity pq = parseQuantity(raw);
                if (pq != null) {
                    totalConsumption = totalConsumption.add(pq.value);
                    consumptionUnit = pq.unit;
                }
            }
            if (data.has(TibberHistorySeries.PURPOSE_COST)) {
                String raw = data.get(TibberHistorySeries.PURPOSE_COST).getAsString();
                ParsedQuantity pq = parseQuantity(raw);
                if (pq != null) {
                    totalCost = totalCost.add(pq.value);
                    costUnit = pq.unit;
                }
            }
            if (data.has(TibberHistorySeries.PURPOSE_PRODUCTION)) {
                String raw = data.get(TibberHistorySeries.PURPOSE_PRODUCTION).getAsString();
                ParsedQuantity pq = parseQuantity(raw);
                if (pq != null) {
                    totalProduction = totalProduction.add(pq.value);
                    productionUnit = pq.unit;
                }
            }
        }

        // Build synthetic JsonObject (same format as TibberHistorySeries stores)
        JsonObject synthetic = new JsonObject();
        if (consumptionUnit != null) {
            synthetic.addProperty(TibberHistorySeries.PURPOSE_CONSUMPTION,
                    totalConsumption.setScale(4, RoundingMode.HALF_UP) + " " + consumptionUnit);
        }
        if (costUnit != null) {
            synthetic.addProperty(TibberHistorySeries.PURPOSE_COST,
                    totalCost.setScale(4, RoundingMode.HALF_UP) + " " + costUnit);
        }
        if (productionUnit != null) {
            synthetic.addProperty(TibberHistorySeries.PURPOSE_PRODUCTION,
                    totalProduction.setScale(4, RoundingMode.HALF_UP) + " " + productionUnit);
        }

        if (synthetic.size() == 0) {
            logger.debug("fillCurrentPeriod: aggregation yielded no data for {} → {}", finer, coarser);
            return coarserSeries;
        }

        // Create enriched copy (do not mutate the stored series)
        TibberHistorySeries enriched = new TibberHistorySeries(coarserSeries.toString());
        enriched.put(periodStart, synthetic);
        logger.debug("fillCurrentPeriod: injected synthetic {} entry at {} (consumption={} cost={} production={})",
                coarser, periodStart, consumptionUnit != null ? totalConsumption + " " + consumptionUnit : "n/a",
                costUnit != null ? totalCost + " " + costUnit : "n/a",
                productionUnit != null ? totalProduction + " " + productionUnit : "n/a");
        return enriched;
    }

    /**
     * Returns the start {@link Instant} of the current incomplete period for the given coarser window.
     * <ul>
     * <li>ANNUAL → first instant of the current year</li>
     * <li>MONTHLY → first instant of the current month</li>
     * <li>WEEKLY → first instant of the current ISO week (Monday 00:00)</li>
     * <li>DAILY → first instant of the current day (00:00)</li>
     * </ul>
     */
    private Instant currentPeriodStart(TibberHistory.TimeWindow coarser, ZonedDateTime now) {
        return switch (coarser) {
            case ANNUAL -> now.withDayOfYear(1).truncatedTo(ChronoUnit.DAYS).toInstant();
            case MONTHLY -> now.withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS).toInstant();
            case WEEKLY -> {
                int dayOfWeek = now.getDayOfWeek().getValue(); // 1=Mon … 7=Sun
                yield now.minusDays(dayOfWeek - 1).truncatedTo(ChronoUnit.DAYS).toInstant();
            }
            case DAILY -> now.truncatedTo(ChronoUnit.DAYS).toInstant();
        };
    }

    /**
     * Parses a openHAB QuantityType string such as {@code "12.345 kWh"} or {@code "3.21 EUR"}.
     * Returns null if the string cannot be parsed.
     */
    private @Nullable ParsedQuantity parseQuantity(String raw) {
        int lastSpace = raw.lastIndexOf(' ');
        if (lastSpace <= 0 || lastSpace >= raw.length() - 1) {
            logger.warn("Cannot parse quantity string: '{}'", raw);
            return null;
        }
        try {
            BigDecimal value = new BigDecimal(raw.substring(0, lastSpace).trim());
            String unit = raw.substring(lastSpace + 1).trim();
            return new ParsedQuantity(value, unit);
        } catch (NumberFormatException e) {
            logger.warn("Cannot parse numeric part of quantity '{}': {}", raw, e.getMessage());
            return null;
        }
    }

    /** Simple value holder for a parsed quantity. */
    private static final class ParsedQuantity {
        final BigDecimal value;
        final String unit;

        ParsedQuantity(BigDecimal value, String unit) {
            this.value = value;
            this.unit = unit;
        }
    }
}
