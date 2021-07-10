/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.octopusenergy.internal.util;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.octopusenergy.internal.dto.Price;
import org.openhab.binding.octopusenergy.internal.dto.PriceOptimiserResult;
import org.openhab.binding.octopusenergy.internal.exception.NotEnoughDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link PriceOptimiser} is a calculator to optimise a schedule for an consumer based on agile pricing.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
public class PriceOptimiser {

    private static final MathContext MATH_CONTEXT = new MathContext(4, RoundingMode.HALF_UP);

    private static final PriceOptimiser INSTANCE = new PriceOptimiser();

    private final Logger logger = LoggerFactory.getLogger(PriceOptimiser.class);

    /**
     * This method will calculate the optimal (price-wise) start time for an activity based on the given duration,
     * start and end times and priceList.
     * Based on hours and minutes specified for start and end time, it will calculate the next window or use all
     * available data.
     *
     * @param requestDuration the requested duration for the activity.
     * @param requestedStartHour the earliest requested hour the activity can start.
     * @param requestedStartMinute the earliest requested minute the activity can start.
     * @param requestedEndHour the latest requested hour the activity can finish.
     * @param requestedEndMinute the latest requested minute the activity can finish.
     * @param priceList the price list the optimisation should be based on.
     * @return the optimisation results.
     * @throws NotEnoughDataException
     */
    public PriceOptimiserResult optimise(Duration requestDuration, int requestedStartHour, int requestedStartMinute,
            int requestedEndHour, int requestedEndMinute, ZonedDateTime now, List<Price> priceList)
            throws NotEnoughDataException {
        ZonedDateTime earliestRequestedStartTime = getFutureTimeWithHourAndMinute(now, requestedStartHour,
                requestedStartMinute);
        ZonedDateTime latestRequestedFinishTime = getFutureTimeWithHourAndMinute(earliestRequestedStartTime,
                requestedEndHour, requestedEndMinute);
        return optimiseWithAbsoluteStartAndEndTime(requestDuration, earliestRequestedStartTime,
                latestRequestedFinishTime, priceList);
    }

    /**
     * This method will calculate the optimal (price-wise) start time for an activity based on the given duration,
     * start time and priceList.
     * Based on hours and minutes specified for start time, it will calculate the next window or use all
     * available data.
     *
     * @param requestDuration the requested duration for the activity.
     * @param requestedStartHour the earliest requested hour the activity can start.
     * @param requestedStartMinute the earliest requested minute the activity can start.
     * @param priceList the price list the optimisation should be based on.
     * @return the optimisation results.
     * @throws NotEnoughDataException
     */
    public PriceOptimiserResult optimiseWithRecurringStartTime(Duration requestDuration, int requestedStartHour,
            int requestedStartMinute, ZonedDateTime now, List<Price> priceList) throws NotEnoughDataException {
        ZonedDateTime earliestRequestedStartTime = getFutureTimeWithHourAndMinute(now, requestedStartHour,
                requestedStartMinute);
        return optimiseWithAbsoluteStartAndEndTime(requestDuration, earliestRequestedStartTime,
                priceList.get(priceList.size() - 1).validTo, priceList);
    }

    /**
     * This method will calculate the optimal (price-wise) start time for an activity based on the given duration,
     * earliest start time and priceList.
     *
     * @param requestDuration the requested duration for the activity.
     * @param earliestRequestedStartTime the earliest time the activity can start.
     * @param priceList the price list the optimisation should be based on.
     * @return the optimisation results.
     * @throws NotEnoughDataException
     */
    public PriceOptimiserResult optimiseWithAbsoluteStartTime(Duration requestDuration,
            ZonedDateTime earliestRequestedStartTime, List<Price> priceList) throws NotEnoughDataException {
        return optimiseWithAbsoluteStartAndEndTime(requestDuration, earliestRequestedStartTime,
                priceList.get(priceList.size() - 1).validTo, priceList);
    }

    /**
     * This method will calculate the optimal (price-wise) start time for an activity based on the given duration,
     * earliest start time, end time hour and minutes and priceList.
     *
     * @param requestDuration the requested duration for the activity.
     * @param requestedEndHour the latest requested hour the activity can finish.
     * @param requestedEndMinute the latest requested minute the activity can finish.
     * @param priceList the price list the optimisation should be based on.
     * @return the optimisation results.
     * @throws NotEnoughDataException
     */
    public PriceOptimiserResult optimiseWithRecurringEndTime(Duration requestDuration, ZonedDateTime earliestStartTime,
            int requestedEndHour, int requestedEndMinute, List<Price> priceList) throws NotEnoughDataException {
        ZonedDateTime latestRequestedFinishTime = getFutureTimeWithHourAndMinute(earliestStartTime, requestedEndHour,
                requestedEndMinute);
        return optimiseWithAbsoluteStartAndEndTime(requestDuration, earliestStartTime, latestRequestedFinishTime,
                priceList);
    }

    /**
     * This method will calculate the optimal (price-wise) start time for an activity based on the given duration,
     * earliest start time, latest end time and priceList.
     *
     * @param requestDuration the requested duration for the activity.
     * @param earliestRequestedStartTime the earliest time the activity can start.
     * @param latestRequestedFinishTime the latest time the activity can finish.
     * @param priceList the price list the optimisation should be based on.
     * @return the optimisation results.
     * @throws NotEnoughDataException
     */
    public PriceOptimiserResult optimiseWithAbsoluteStartAndEndTime(Duration requestDuration,
            ZonedDateTime earliestRequestedStartTime, ZonedDateTime latestRequestedFinishTime, List<Price> priceList)
            throws NotEnoughDataException {
        // round duration down to the next full minute
        logger.trace("calculateOptimalStartTime - duration: {}, start: {}, end: {}", requestDuration,
                earliestRequestedStartTime.toString(), latestRequestedFinishTime.toString());
        Duration duration = Duration.ofMinutes(requestDuration.toMinutes());
        logger.trace("calculateOptimalStartTime - earliest requested start time: {}, duration: {}",
                earliestRequestedStartTime.toString(), duration);

        // preparation and validation
        if (priceList.size() < 1) {
            throw new NotEnoughDataException("No price data available");
        }
        ZonedDateTime possibleStartTime = priceList.get(0).validFrom;
        int possibleStartSlot = 0;
        if (earliestRequestedStartTime.isAfter(possibleStartTime)) {
            // find the correct starting slot based on earliestRequestedStartTime
            possibleStartTime = earliestRequestedStartTime;
            possibleStartSlot = findPriceSlotForTime(priceList, earliestRequestedStartTime);
        }

        ZonedDateTime latestEndTime = latestRequestedFinishTime;
        if (priceList.get(priceList.size() - 1).validTo.isBefore(latestEndTime)) {
            latestEndTime = priceList.get(priceList.size() - 1).validTo;
        }
        if (possibleStartTime.plus(duration).isAfter(latestEndTime)) {
            throw new NotEnoughDataException("Earliest possible slot would finish after the last end time");
        }

        Duration slotSize = Duration.between(priceList.get(0).validFrom, priceList.get(0).validTo);
        long requiredSlots = duration.getSeconds() / slotSize.getSeconds();
        Duration leftOverDuration = duration.minus(slotSize.multipliedBy(requiredSlots));
        logger.trace("slot size: {} mins, required slots: {}, leftover duration: {}", slotSize.toMinutes(),
                requiredSlots, leftOverDuration);

        ZonedDateTime minCostStartTime = possibleStartTime;
        BigDecimal minCost = BigDecimal.valueOf(Integer.MAX_VALUE);
        /*
         * 3 scenarios:
         *
         * R. start activity at requested earliest start time
         * S. align activity with the beginning of a slot
         * E. align activity with the end of a slot.
         */
        if (possibleStartTime.isAfter(priceList.get(0).validFrom)) {
            // Calculate cost for scenario R
            minCost = calculateCostForDuration(duration, possibleStartTime, priceList, true);
            possibleStartSlot++;
            possibleStartTime = priceList.get(possibleStartSlot).validFrom;
            logger.trace("scenario R - startTime: {}, cost: {}, prevMin: {}", possibleStartTime, minCost, minCost);
        }
        boolean withinPriceRange = true;
        while (withinPriceRange) {
            try {
                BigDecimal cost;
                // we align with the beginning of the current slot and test scenario S
                if (possibleStartTime.plus(duration).isAfter(latestEndTime)) {
                    throw new NotEnoughDataException(
                            "Earliest possible slot would finish after the last available price");
                }
                cost = calculateCostForDuration(duration, possibleStartTime, priceList, true);
                logger.trace("scenario S - startTime: {}, cost: {}, prevMin: {}", possibleStartTime, cost, minCost);
                if (cost.compareTo(minCost) < 0) {
                    minCost = cost;
                    minCostStartTime = possibleStartTime;
                }

                // now we align with the end of the slot after the test scenario E
                possibleStartTime = possibleStartTime.plus(slotSize.minus(leftOverDuration));
                if (possibleStartTime.plus(duration).isAfter(latestEndTime)) {
                    throw new NotEnoughDataException(
                            "Earliest possible slot would finish after the last available price");
                }
                cost = calculateCostForDuration(duration, possibleStartTime, priceList, true);
                logger.trace("scenario E - startTime: {}, cost: {}, prevMin: {}", possibleStartTime, cost, minCost);
                if (cost.compareTo(minCost) < 0) {
                    minCost = cost;
                    minCostStartTime = possibleStartTime;
                }

                // and back to the start of the next slot
                possibleStartSlot++;
                if (possibleStartTime.equals(priceList.get(possibleStartSlot).validFrom)) {
                    possibleStartSlot++;
                }
                possibleStartTime = priceList.get(possibleStartSlot).validFrom;
            } catch (NotEnoughDataException | IndexOutOfBoundsException e) {
                // we've either reached the end of the slots or the duration would go beyond the last available slot
                withinPriceRange = false;
            }
        }
        BigDecimal averageUnitCost = minCost.multiply(BigDecimal.valueOf(slotSize.toMinutes()))
                .divide(BigDecimal.valueOf(duration.toMinutes()), MATH_CONTEXT);
        PriceOptimiserResult result = new PriceOptimiserResult(earliestRequestedStartTime, latestRequestedFinishTime,
                duration, minCostStartTime, averageUnitCost, ZonedDateTime.now());
        logger.debug("calculateOptimalStartTime - result: {}", result);
        return result;
    }

    /**
     * Calculates the cost of an 1kW activity based on a given start time and duration. The given is duration is rounded
     * down to the nearest minute.
     *
     * @param duration the duration of the activity
     * @param startTime the time when the activity starts
     * @param includeVat use prices incl. VAT if true, otherwise use prices excl. VAT
     * @return the cost of the activity
     * @throws NotEnoughDataException if any part of the activity is outside the range of prices, an exception is
     *             thrown.
     */
    protected BigDecimal calculateCostForDuration(Duration duration, ZonedDateTime startTime, List<Price> priceList,
            boolean includeVat) throws NotEnoughDataException {
        logger.trace("calculateCostForDuration - duration: {}, start time: {}", duration, startTime);
        try {
            BigDecimal cost = BigDecimal.ZERO;
            // round duration up to the next full minute
            ZonedDateTime endTime = startTime.plus(Duration.ofMinutes(duration.toMinutes()));

            // the price list is always ordered in time order, we can just get the first and last in the list to
            // validate
            // the range
            if (startTime.isBefore(priceList.get(0).validFrom)
                    || endTime.isAfter(priceList.get(priceList.size() - 1).validTo)) {
                throw new NotEnoughDataException("Activity not within price range");
            }
            for (Price price : priceList) {
                // if activity starts before the slot starts, we contribute from the slot start, otherwise activity
                // start
                ZonedDateTime costStart = startTime.isBefore(price.validFrom) ? price.validFrom : startTime;
                // if activity ends after the slot ends, we contribute until the slot end, otherwise activity end
                ZonedDateTime costEnd = endTime.isAfter(price.validTo) ? price.validTo : endTime;

                // if the activity overlaps with the slot, we calculate the contribution
                if (costEnd.isAfter(costStart)) {
                    BigDecimal slotPrice = includeVat ? price.valueIncVat : price.valueExcVat;
                    BigDecimal slotCost = slotPrice
                            .multiply(BigDecimal.valueOf(Duration.between(costStart, costEnd).toMinutes()))
                            .divide(BigDecimal.valueOf(Duration.between(price.validFrom, price.validTo).toMinutes()),
                                    MATH_CONTEXT);
                    cost = cost.add(slotCost);
                    logger.trace("slot : {}, price: {}, slot cost: {}, overall cost: {}", price.validFrom.toString(),
                            slotPrice, slotCost, cost);
                }
            }
            return cost;
        } catch (IndexOutOfBoundsException e) {
            throw new NotEnoughDataException();
        }
    }

    /**
     * Starting at the given time, this method returns a future time with the given hour and minute.
     *
     * @param time
     * @param hour
     * @param minute
     * @return
     */
    protected ZonedDateTime getFutureTimeWithHourAndMinute(ZonedDateTime time, int hour, int minute) {
        ZonedDateTime futureTime = ZonedDateTime.of(time.getYear(), time.getMonthValue(), time.getDayOfMonth(), hour,
                minute, 0, 0, time.getZone());
        if (futureTime.isBefore(time)) {
            // newly calculated time is in the past, we'll add one day
            futureTime = futureTime.plusDays(1);
        }
        return futureTime;
    }

    /**
     * Returns the index of the price slot matching the given time.
     *
     * @param priceList the list of price slots.
     * @param time the time to search for.
     * @return price slot index.
     * @throws NotEnoughDataException time is outside the given price slots.
     */
    protected int findPriceSlotForTime(List<Price> priceList, ZonedDateTime time) throws NotEnoughDataException {
        if (priceList.isEmpty() || time.isBefore(priceList.get(0).validFrom)) {
            throw new NotEnoughDataException("Time is before the first available price slot");
        }
        int slotIndex = 0;
        for (Price p : priceList) {
            // assuming within a slot if validFrom <= time < validTo
            if ((time.isEqual(p.validFrom) || time.isAfter(p.validFrom)) && time.isBefore(p.validTo)) {
                return slotIndex;
            }
            slotIndex++;
        }
        throw new NotEnoughDataException("Time is after the last available price slot");
    }

    public static PriceOptimiser getInstance() {
        return INSTANCE;
    }
}
