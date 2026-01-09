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
package org.openhab.binding.energidataservice.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

import javax.measure.quantity.Energy;
import javax.measure.quantity.Power;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.energidataservice.internal.exception.MissingPriceException;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides calculations based on price maps.
 * This is the current stage of evolution.
 * Ideally this binding would simply provide data in a well-defined format for
 * openHAB core. Operations on this data could then be implemented in core.
 * This way there would be a unified interface from rules, and the calculations
 * could be reused between different data providers (bindings).
 * 
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class PriceCalculator {

    private final Logger logger = LoggerFactory.getLogger(PriceCalculator.class);

    private final NavigableMap<Instant, BigDecimal> priceMap;

    public PriceCalculator(Map<Instant, BigDecimal> priceMap) {
        this.priceMap = new ConcurrentSkipListMap<>(priceMap);
    }

    /**
     * Calculate cheapest period from list of durations with specified amount of energy
     * used per phase.
     *
     * @param earliestStart Earliest allowed start time.
     * @param latestEnd Latest allowed end time.
     * @param totalDuration Total duration to fit.
     * @param durationPhases List of {@link Duration}'s representing different phases of using power.
     * @param energyUsedPerPhase Amount of energy used per phase.
     *
     * @return Map containing resulting values
     */
    public Map<String, Object> calculateCheapestPeriod(Instant earliestStart, Instant latestEnd, Duration totalDuration,
            Collection<Duration> durationPhases, QuantityType<Energy> energyUsedPerPhase) throws MissingPriceException {
        QuantityType<Energy> energyInWattHour = energyUsedPerPhase.toUnit(Units.WATT_HOUR);
        if (energyInWattHour == null) {
            throw new IllegalArgumentException(
                    "Invalid unit " + energyUsedPerPhase.getUnit() + ", expected energy unit");
        }
        // watts = (kWh × 1,000) ÷ hrs
        int numerator = energyInWattHour.intValue() * 3600;
        List<QuantityType<Power>> consumptionPhases = new ArrayList<>();
        Duration remainingDuration = totalDuration;
        for (Duration phase : durationPhases) {
            consumptionPhases.add(QuantityType.valueOf(numerator / phase.getSeconds(), Units.WATT));
            remainingDuration = remainingDuration.minus(phase);
        }
        if (remainingDuration.isNegative()) {
            throw new IllegalArgumentException("totalDuration must be equal to or greater than sum of phases");
        }
        if (!remainingDuration.isZero()) {
            List<Duration> durationsWithTermination = new ArrayList<>(durationPhases);
            durationsWithTermination.add(remainingDuration);
            consumptionPhases.add(QuantityType.valueOf(0, Units.WATT));
            return calculateCheapestPeriod(earliestStart, latestEnd, durationsWithTermination, consumptionPhases);
        }
        return calculateCheapestPeriod(earliestStart, latestEnd, durationPhases, consumptionPhases);
    }

    /**
     * Calculate cheapest period from duration with linear power usage.
     *
     * @param earliestStart Earliest allowed start time.
     * @param latestEnd Latest allowed end time.
     * @param duration Duration to fit.
     * @param power Power consumption for the duration of time.
     *
     * @return Map containing resulting values
     */
    public Map<String, Object> calculateCheapestPeriod(Instant earliestStart, Instant latestEnd, Duration duration,
            QuantityType<Power> power) throws MissingPriceException {
        return calculateCheapestPeriod(earliestStart, latestEnd, List.of(duration), List.of(power));
    }

    /**
     * Calculate cheapest period from list of durations with corresponding list of consumption
     * per duration.
     *
     * @param earliestStart Earliest allowed start time.
     * @param latestEnd Latest allowed end time.
     * @param durationPhases List of {@link Duration}'s representing different phases of using power.
     * @param consumptionPhases Corresponding List of power consumption for the duration of time.
     *
     * @return Map containing resulting values
     */
    public Map<String, Object> calculateCheapestPeriod(Instant earliestStart, Instant latestEnd,
            Collection<Duration> durationPhases, Collection<QuantityType<Power>> consumptionPhases)
            throws MissingPriceException {
        if (durationPhases.size() != consumptionPhases.size()) {
            throw new IllegalArgumentException("Number of phases do not match");
        }
        Map<String, Object> result = new HashMap<>();
        Duration totalDuration = durationPhases.stream().reduce(Duration.ZERO, Duration::plus);
        Instant calculationStart = earliestStart;
        Instant calculationEnd = earliestStart.plus(totalDuration);
        BigDecimal lowestPrice = BigDecimal.valueOf(Double.MAX_VALUE);
        BigDecimal highestPrice = BigDecimal.ZERO;
        Instant cheapestStart = Instant.MIN;
        Instant mostExpensiveStart = Instant.MIN;
        Duration resolution = determineResolution();

        while (calculationEnd.compareTo(latestEnd) <= 0) {
            BigDecimal currentPrice = BigDecimal.ZERO;
            Duration minDurationUntilNextPrice = resolution;
            Instant atomStart = calculationStart;

            Iterator<Duration> durationIterator = durationPhases.iterator();
            Iterator<QuantityType<Power>> consumptionIterator = consumptionPhases.iterator();
            while (durationIterator.hasNext()) {
                Duration atomDuration = durationIterator.next();
                QuantityType<Power> atomConsumption = consumptionIterator.next();

                Instant atomEnd = atomStart.plus(atomDuration);
                Instant priceStart = truncateTo(atomStart, resolution);
                Instant priceEnd = priceStart.plus(resolution);

                // Get next intersection with rate change.
                Duration durationUntilNextPrice = Duration.between(atomStart, priceEnd);
                if (durationUntilNextPrice.compareTo(minDurationUntilNextPrice) < 0) {
                    minDurationUntilNextPrice = durationUntilNextPrice;
                }

                BigDecimal atomPrice = calculatePrice(atomStart, atomEnd, atomConsumption);
                currentPrice = currentPrice.add(atomPrice);
                atomStart = atomEnd;
            }

            if (currentPrice.compareTo(lowestPrice) < 0) {
                lowestPrice = currentPrice;
                cheapestStart = calculationStart;
            }
            if (currentPrice.compareTo(highestPrice) > 0) {
                highestPrice = currentPrice;
                mostExpensiveStart = calculationStart;
            }

            // Now fast forward to next price interval intersection.
            calculationStart = calculationStart.plus(minDurationUntilNextPrice);
            calculationEnd = calculationStart.plus(totalDuration);
        }

        if (!cheapestStart.equals(Instant.MIN)) {
            result.put("CheapestStart", cheapestStart);
            result.put("LowestPrice", lowestPrice);
            result.put("MostExpensiveStart", mostExpensiveStart);
            result.put("HighestPrice", highestPrice);
        }

        return result;
    }

    /**
     * Calculate total price from 'start' to 'end' given linear power consumption.
     *
     * @param start Start time
     * @param end End time
     * @param power The current power consumption.
     */
    public BigDecimal calculatePrice(Instant start, Instant end, QuantityType<Power> power)
            throws MissingPriceException {
        QuantityType<Power> quantityInWatt = power.toUnit(Units.WATT);
        if (quantityInWatt == null) {
            throw new IllegalArgumentException("Invalid unit " + power.getUnit() + ", expected power unit");
        }
        BigDecimal watt = quantityInWatt.toBigDecimal();
        if (watt.equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }

        Duration resolution = determineResolution();
        Instant current = start;
        BigDecimal result = BigDecimal.ZERO;
        while (current.isBefore(end)) {
            Instant priceStart = truncateTo(current, resolution);
            Instant priceEnd = priceStart.plus(resolution);

            BigDecimal currentPrice = get(current);
            if (currentPrice == null) {
                throw new MissingPriceException("Price missing at " + priceStart.toString());
            }

            Instant currentStart = priceStart;
            if (start.isAfter(priceStart)) {
                currentStart = start;
            }
            Instant currentEnd = priceEnd;
            if (end.isBefore(priceEnd)) {
                currentEnd = end;
            }

            // E(kWh) = P(W) × t(hr) / 1000
            Duration duration = Duration.between(currentStart, currentEnd);
            BigDecimal contribution = currentPrice.multiply(watt).multiply(
                    new BigDecimal(duration.getSeconds()).divide(new BigDecimal(3600000), 9, RoundingMode.HALF_UP));
            result = result.add(contribution);
            logger.trace("Period {}-{}: {} @ {}", currentStart, currentEnd, contribution, currentPrice);

            current = priceEnd;
        }

        return result;
    }

    private @Nullable BigDecimal get(Instant time) {
        Map.Entry<Instant, BigDecimal> entry = priceMap.floorEntry(time);
        if (entry == null) {
            return null;
        }

        Instant validUntil = getValidUntil(entry.getKey());
        if (validUntil != null && time.isBefore(validUntil)) {
            return entry.getValue();
        }

        return null;
    }

    private @Nullable Instant getValidUntil(Instant current) {
        Instant next = getPriceMapHigherKey(current);
        if (next != null) {
            return next;
        }

        Instant previous = getPriceMapLowerKey(current);
        if (previous != null) {
            return current.plus(Duration.between(previous, current));
        }

        return null;
    }

    private @Nullable Instant getPriceMapHigherKey(Instant current) {
        return priceMap.higherKey(current);
    }

    private @Nullable Instant getPriceMapLowerKey(Instant current) {
        return priceMap.lowerKey(current);
    }

    private Instant getPriceMapFirstKey() {
        return priceMap.firstKey();
    }

    private Duration determineResolution() {
        if (priceMap.size() < 2) {
            throw new IllegalStateException("Cannot determine price resolution (only one price point available)");
        }

        Instant first = getPriceMapFirstKey();
        Instant second = getPriceMapHigherKey(first);
        if (second == null) {
            throw new IllegalStateException("Cannot determine price resolution (only one price point available)");
        }
        return Duration.between(first, second);
    }

    private static Instant truncateTo(Instant instant, Duration resolution) {
        long resolutionSeconds = resolution.getSeconds();
        long truncatedSeconds = (instant.getEpochSecond() / resolutionSeconds) * resolutionSeconds;
        return Instant.ofEpochSecond(truncatedSeconds);
    }
}
