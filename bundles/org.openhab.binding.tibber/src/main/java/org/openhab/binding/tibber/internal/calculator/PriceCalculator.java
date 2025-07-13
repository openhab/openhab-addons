/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.tibber.internal.calculator;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.tibber.internal.Utils;
import org.openhab.binding.tibber.internal.dto.CurveEntry;
import org.openhab.binding.tibber.internal.dto.PriceInfo;
import org.openhab.binding.tibber.internal.dto.ScheduleEntry;
import org.openhab.binding.tibber.internal.exception.PriceCalculationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link PriceCalculator} provides price calculations for thing actions.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public class PriceCalculator {
    private static final int AVERAGE_PRICE_INTERVAL = 5;
    private final Logger logger = LoggerFactory.getLogger(PriceCalculator.class);
    private final TreeMap<Instant, PriceInfo> priceMap;

    public PriceCalculator(JsonArray spotPrices) {
        priceMap = new TreeMap<>();
        JsonObject previousEntry = null;
        int previousDuration = 0;
        for (JsonElement entry : spotPrices) {
            JsonObject entryObject = entry.getAsJsonObject();
            if (previousEntry != null) {
                Instant start = Instant.parse(previousEntry.get("startsAt").getAsString());
                Instant end = Instant.parse(entryObject.get("startsAt").getAsString());
                double price = previousEntry.get("total").getAsDouble();
                previousDuration = (int) Duration.between(start, end).getSeconds();
                PriceInfo pi = new PriceInfo(price, previousDuration, start,
                        Utils.mapLevelToInt(previousEntry.get("level").getAsString()));
                priceMap.put(start, pi);
            }
            previousEntry = entryObject;
        }

        if (previousEntry != null) {
            // put last element with previousDuration
            Instant lastElementStart = Instant.parse(previousEntry.get("startsAt").getAsString());
            priceMap.put(lastElementStart, new PriceInfo(previousEntry.get("total").getAsDouble(), previousDuration,
                    lastElementStart, Utils.mapLevelToInt(previousEntry.get("level").getAsString())));

            // put termination element
            Instant terminationInstant = priceMap.lastKey().plus(previousDuration, ChronoUnit.SECONDS);
            priceMap.put(terminationInstant, new PriceInfo(Double.MAX_VALUE, 0, terminationInstant, 0));
        } else {
            logger.warn("Empty spot price update delivered");
        }
    }

    /**
     * Calculate average prices if 24 hours history is available
     *
     * @return Average prices from past 24 h in 5 minutes steps
     */
    public TreeMap<Instant, Double> calculateAveragePrices() {
        TreeMap<Instant, Double> averages = new TreeMap<>();
        Instant startCalculation = priceMap.firstKey();
        Instant iterator = startCalculation;
        // continue loop until iterator current point of calculation
        while (priceMap.higherEntry(iterator) != null) {
            Instant start = iterator.minus(1, ChronoUnit.DAYS);
            if (priceMap.floorEntry(start) != null) {
                if (priceMap.higherEntry(iterator).getKey().isAfter(iterator)) {
                    double price = averagePrice(start, iterator);
                    averages.put(iterator, price);
                }
            }
            iterator = iterator.plus(AVERAGE_PRICE_INTERVAL, ChronoUnit.MINUTES);
        }
        return averages;
    }

    /**
     * Calculate average price between 2 timestamps.
     *
     * @param from start timestamp
     * @param to end timestamp
     * @return average price according to durations
     */
    public double averagePrice(Instant from, Instant to) {
        // calculate average for 24h
        double price = 0;
        Instant iterator = from;
        while (iterator.isBefore(to)) {
            Entry<Instant, PriceInfo> floor = priceMap.floorEntry(iterator);
            Entry<Instant, PriceInfo> ceiling = priceMap.higherEntry(iterator);
            if (floor != null && ceiling != null) {
                long duration = 0;
                if (to.isBefore(ceiling.getKey())) {
                    // if to is before higher entry this is the last calculation and it needs to be cutted
                    duration = Duration.between(iterator, to).toMinutes();
                } else {
                    duration = Duration.between(iterator, ceiling.getKey()).toMinutes();
                }
                price += duration * floor.getValue().price;
                iterator = iterator.plus(duration, ChronoUnit.MINUTES);
            } else {
                logger.warn("Calculation of average price out of range {}", iterator);
                break;
            }
        }
        return price / (24 * 60);
    }

    /**
     * Calculates the price based on start time, power and duration
     *
     * @param start timestamp of calculation
     * @param powerW power in watts
     * @param durationSeconds duration in seconds
     * @return price according to priceMap
     */
    public double calculatePrice(Instant start, int powerW, long durationSeconds) throws PriceCalculationException {
        checkBoundaries(start, start.plus(durationSeconds, ChronoUnit.SECONDS));
        Entry<Instant, PriceInfo> startEntry = priceMap.floorEntry(start);
        Entry<Instant, PriceInfo> nextEntry = priceMap.higherEntry(start);
        if (startEntry != null && nextEntry != null) {
            if (!start.plusSeconds(durationSeconds).isAfter(nextEntry.getKey())) {
                // complete duration is in this price period
                return (powerW / 1000.0) * (durationSeconds / 3600.0) * startEntry.getValue().price;
            } else {
                // calculate price from this time period plus later periods
                int partDuration = (int) Duration.between(start, nextEntry.getKey()).toSeconds();
                double partPrice = powerW / 1000.0 * partDuration / 3600.0 * startEntry.getValue().price;
                long remainingDuration = durationSeconds - partDuration;
                return (partPrice + calculatePrice(nextEntry.getKey(), powerW, remainingDuration));
            }
        } else {
            throw new PriceCalculationException(
                    "Calculation for " + start + " out of range. Respect priceInfoStart and priceInfoEnd boundaries.");
        }
    }

    /**
     * Calculates the best price between 2 timestamps
     *
     * @param earliestStart
     * @param latestEnd
     * @param curve power duration tuples representing a device power curve
     * @return Map with results of cheapest start and price plus most expensive start
     */
    public Map<String, Object> calculateBestPrice(Instant earliestStart, Instant latestEnd, List<CurveEntry> curve)
            throws PriceCalculationException {
        checkBoundaries(earliestStart, latestEnd);
        int totalDuration = 0;
        for (Iterator<CurveEntry> iterator = curve.iterator(); iterator.hasNext();) {
            CurveEntry curveEntry = iterator.next();
            totalDuration += curveEntry.durationSeconds;
        }
        Instant latestStart = latestEnd.minus(totalDuration, ChronoUnit.SECONDS);
        Instant startIterator = earliestStart;
        double highestCost = Double.MIN_VALUE;
        Instant highestStart = Instant.MAX;
        double lowestCost = Double.MAX_VALUE;
        Instant lowestStart = Instant.MAX;
        int iterations = 0;
        double priceAccumulation = 0;
        long calculationStart = System.currentTimeMillis();
        while (startIterator.isBefore(latestStart)) {
            double price = 0;
            for (Iterator<CurveEntry> iterator = curve.iterator(); iterator.hasNext();) {
                CurveEntry curveEntry = iterator.next();
                price += calculatePrice(startIterator, curveEntry.powerWatts, curveEntry.durationSeconds);
            }
            if (price < lowestCost) {
                lowestCost = price;
                lowestStart = startIterator;
            }
            if (price > highestCost) {
                highestCost = price;
                highestStart = startIterator;
            }
            priceAccumulation += price;
            startIterator = startIterator.plus(1, ChronoUnit.MINUTES);
            iterations++;
        }
        logger.trace("Calculation time {} ms for {} iterations", (System.currentTimeMillis() - calculationStart),
                iterations);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("cheapestStart", lowestStart.toString());
        resultMap.put("lowestPrice", lowestCost);
        resultMap.put("mostExpensiveStart", highestStart.toString());
        resultMap.put("highestPrice", highestCost);
        resultMap.put("averagePrice", priceAccumulation / iterations);
        return resultMap;
    }

    /**
     * List prices in ascending or descending order.
     *
     * @param earliestStart start time of list
     * @param latestEnd end time of list
     * @param ascending true for ascending, false for descending order
     * @return list matching exactly between the timestamps with PriceInfo
     */
    public List<PriceInfo> listPrices(Instant earliestStart, Instant latestEnd, boolean ascending)
            throws PriceCalculationException {
        checkBoundaries(earliestStart, latestEnd);
        TreeMap<Instant, PriceInfo> calculationMap = new TreeMap<>();
        for (Entry<Instant, PriceInfo> entry : priceMap.entrySet()) {
            if (!entry.getKey().isBefore(earliestStart) && !entry.getKey().isAfter(latestEnd)
                    && !(Double.MAX_VALUE == entry.getValue().price)) {
                calculationMap.put(entry.getKey(), entry.getValue());
            }
        }
        // assure price before earliest start is available
        calculationMap.put(priceMap.floorKey(earliestStart), priceMap.floorEntry(earliestStart).getValue());

        SortedMap<Double, List<PriceInfo>> reversed = reverseMap(calculationMap, ascending);
        List<PriceInfo> ascendingList = new ArrayList<>();
        reversed.forEach((key, value) -> {
            value.forEach(priceInfo -> {
                priceInfo.adjust(earliestStart, latestEnd);
                ascendingList.add(priceInfo);
            });
        });
        return ascendingList;
    }

    /**
     * Reverse priceMap with sorted timestamps to sorting of prices. If same price is found several times list of
     * timestamp is added.
     *
     * @param input SorteMap with Instant keys
     * @param ascending true ascending sorting, false descending
     * @return SortedMap accoring to price with List of Instant
     */
    public SortedMap<Double, List<PriceInfo>> reverseMap(SortedMap<Instant, PriceInfo> input, boolean ascending) {
        TreeMap<Double, List<PriceInfo>> reversed;
        if (ascending) {
            reversed = new TreeMap<>();
        } else {
            reversed = new TreeMap<>(Comparator.reverseOrder());
        }
        input.forEach((key, value) -> {
            List<PriceInfo> l = reversed.get(value.price);
            if (l == null) {
                l = new ArrayList<>();
            }
            l.add(value);
            reversed.put(value.price, l);
        });
        return reversed;
    }

    /**
     * Calculate non consecutive schedule for fixed duration of power. Due to the fact the listPrices call returns a
     * list exactly matching the earliestStart and latestEnd timestamps with correct duration it's only needed to pick
     * one entry after another to calculate the schedule.
     *
     * @param earliestStart earliest start point
     * @param latestEnd latest end point
     * @param powerW power in watts
     * @param durationS duration in seconds
     * @return List of ScheduleEntries
     */
    public List<ScheduleEntry> calculateNonConsecutive(Instant earliestStart, Instant latestEnd, int powerW,
            int durationS) throws PriceCalculationException {
        checkBoundaries(earliestStart, latestEnd);
        List<PriceInfo> sortedList = listPrices(earliestStart, latestEnd, true);
        List<ScheduleEntry> schedule = new ArrayList<>();
        int remainDuration = durationS;
        for (int i = 0; i < sortedList.size() && remainDuration > 0; i++) {
            PriceInfo priceInfo = sortedList.get(i);
            if (priceInfo.durationSeconds > remainDuration) {
                // request fits in this time window - terminate
                double cost = powerW / 1000.0 * remainDuration / 3600.0 * priceInfo.price;
                ScheduleEntry se = new ScheduleEntry(priceInfo.startsAt,
                        priceInfo.startsAt.plus(remainDuration, ChronoUnit.SECONDS), remainDuration, cost);
                schedule = insertSchedule(se, schedule);
                remainDuration = 0;
            } else {
                double cost = powerW / 1000.0 * priceInfo.durationSeconds / 3600.0 * priceInfo.price;
                ScheduleEntry se = new ScheduleEntry(priceInfo.startsAt,
                        priceInfo.startsAt.plus(priceInfo.durationSeconds, ChronoUnit.SECONDS),
                        priceInfo.durationSeconds, cost);
                schedule = insertSchedule(se, schedule);
                remainDuration -= priceInfo.durationSeconds;
            }
        }
        return schedule;
    }

    /**
     * Insert ScheduleEntry into list to check if a head / tail ScheduleEntry can be found
     *
     * @param entry ScheduleEntry to inserted
     * @param schedule List of currently found ScheduleEntry
     * @return List with compacted ScheduleEntry
     */
    private List<ScheduleEntry> insertSchedule(ScheduleEntry entry, List<ScheduleEntry> schedule) {
        ScheduleEntry newScheduleEntry = entry;
        List<ScheduleEntry> newSchedule = new ArrayList<>();
        while (!schedule.isEmpty()) {
            ScheduleEntry observationEntry = schedule.remove(0);
            if (newScheduleEntry.start.equals(observationEntry.stop)) {
                // found direct predecessor - merge
                newScheduleEntry = new ScheduleEntry(observationEntry.start, newScheduleEntry.stop,
                        observationEntry.duration + newScheduleEntry.duration,
                        observationEntry.cost + newScheduleEntry.cost);
            } else if (newScheduleEntry.stop.equals(observationEntry.start)) {
                // found direct successor - merge
                newScheduleEntry = new ScheduleEntry(newScheduleEntry.start, observationEntry.stop,
                        observationEntry.duration + newScheduleEntry.duration,
                        observationEntry.cost + newScheduleEntry.cost);
            } else {
                newSchedule.add(observationEntry);
            }
        }
        newSchedule.add(newScheduleEntry);
        return newSchedule;
    }

    /**
     * Get earliest price info
     *
     * @return first Instant key of priceMap
     */
    public Instant priceInfoStart() {
        return priceMap.firstKey();
    }

    /**
     * Get latest available price info
     *
     * @return last INstant key of priceMap
     */
    public Instant priceInfoEnd() {
        return priceMap.lastKey().plusSeconds(priceMap.lastEntry().getValue().durationSeconds);
    }

    /**
     * Check start and end boundaries of priceMap
     *
     * @param start
     * @param end
     */
    private void checkBoundaries(Instant start, Instant end) throws PriceCalculationException {
        if (!start.isBefore(end)) {
            throw new PriceCalculationException("Calculation start " + start + " is after end " + end);
        }
        if (start.isBefore(priceInfoStart())) {
            throw new PriceCalculationException(
                    "Calculation start " + start + " too early. Please respect priceInfoStart boundary.");
        }
        if (end.isAfter(priceInfoEnd())) {
            throw new PriceCalculationException(
                    "Calculation end " + end + " too late. Please respect priceInfoEnd boundary.");
        }
    }
}
