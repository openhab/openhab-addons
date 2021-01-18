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

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.octopusenergy.internal.dto.Price;
import org.openhab.binding.octopusenergy.internal.exception.NotEnoughDataException;

/**
 * The {@link PriceOptimiserTest} is a test class for {@link PriceOptimiser}.
 *
 * @author Rene Scherer - Initial contribution
 */
@NonNullByDefault
class PriceOptimiserTest {

    private static final List<Price> PRICE_LIST = new ArrayList<>();

    // 23.00 - before the first slot is available
    private static final ZonedDateTime START_TIME_1 = ZonedDateTime.parse("2020-10-08T23:00Z");

    // 00.00 - start of the first slot
    private static final ZonedDateTime START_TIME_2 = ZonedDateTime.parse("2020-10-09T00:00Z");

    // 00.45 - middle of the second slot
    private static final ZonedDateTime START_TIME_3 = ZonedDateTime.parse("2020-10-09T00:45Z");

    // 01.40
    private static final ZonedDateTime START_TIME_4 = ZonedDateTime.parse("2020-10-09T01:40Z");

    private static final PriceOptimiser PO = PriceOptimiser.getInstance();

    @BeforeAll
    static void setUpBeforeClass() throws Exception {
        // setting up a price profile of 1,1, 1,9, 9,2, 2,2, 2,3, 1,1, 8,8

        PRICE_LIST.add(new Price(1.0, 1.5, ZonedDateTime.parse("2020-10-09T00:00Z"),
                ZonedDateTime.parse("2020-10-09T00:30Z")));
        PRICE_LIST.add(new Price(1.0, 1.5, ZonedDateTime.parse("2020-10-09T00:30Z"),
                ZonedDateTime.parse("2020-10-09T01:00Z")));
        PRICE_LIST.add(new Price(1.0, 1.5, ZonedDateTime.parse("2020-10-09T01:00Z"),
                ZonedDateTime.parse("2020-10-09T01:30Z")));
        PRICE_LIST.add(new Price(9.0, 13.5, ZonedDateTime.parse("2020-10-09T01:30Z"),
                ZonedDateTime.parse("2020-10-09T02:00Z")));
        PRICE_LIST.add(new Price(9.0, 13.5, ZonedDateTime.parse("2020-10-09T02:00Z"),
                ZonedDateTime.parse("2020-10-09T02:30Z")));
        PRICE_LIST.add(new Price(2.0, 3.0, ZonedDateTime.parse("2020-10-09T02:30Z"),
                ZonedDateTime.parse("2020-10-09T03:00Z")));
        PRICE_LIST.add(new Price(2.0, 3.0, ZonedDateTime.parse("2020-10-09T03:00Z"),
                ZonedDateTime.parse("2020-10-09T03:30Z")));
        PRICE_LIST.add(new Price(2.0, 3.0, ZonedDateTime.parse("2020-10-09T03:30Z"),
                ZonedDateTime.parse("2020-10-09T04:00Z")));
        PRICE_LIST.add(new Price(2.0, 3.0, ZonedDateTime.parse("2020-10-09T04:00Z"),
                ZonedDateTime.parse("2020-10-09T04:30Z")));
        PRICE_LIST.add(new Price(3.0, 4.5, ZonedDateTime.parse("2020-10-09T04:30Z"),
                ZonedDateTime.parse("2020-10-09T05:00Z")));
        PRICE_LIST.add(new Price(1.0, 1.5, ZonedDateTime.parse("2020-10-09T05:00Z"),
                ZonedDateTime.parse("2020-10-09T05:30Z")));
        PRICE_LIST.add(new Price(1.0, 1.5, ZonedDateTime.parse("2020-10-09T05:30Z"),
                ZonedDateTime.parse("2020-10-09T06:00Z")));
        PRICE_LIST.add(new Price(8.0, 12.0, ZonedDateTime.parse("2020-10-09T06:00Z"),
                ZonedDateTime.parse("2020-10-09T06:30Z")));
        PRICE_LIST.add(new Price(8.0, 12.0, ZonedDateTime.parse("2020-10-09T06:30Z"),
                ZonedDateTime.parse("2020-10-09T07:00Z")));
    }

    /**
     * Test with a 30 mins (1 slot) duration
     */
    @Test
    void testOptimise30min() {
        try {
            assertEquals(ZonedDateTime.parse("2020-10-09T00:00Z"),
                    PO.optimiseWithAbsoluteStartTime(Duration.ofSeconds(30 * 60 + 30), START_TIME_1,
                            PRICE_LIST).optimisedStartTime);

            assertEquals(ZonedDateTime.parse("2020-10-09T00:00Z"),
                    PO.optimiseWithAbsoluteStartTime(Duration.ofMinutes(30), START_TIME_2,
                            PRICE_LIST).optimisedStartTime);

            assertEquals(ZonedDateTime.parse("2020-10-09T00:45Z"),
                    PO.optimiseWithAbsoluteStartTime(Duration.ofMinutes(30), START_TIME_3,
                            PRICE_LIST).optimisedStartTime);
        } catch (NotEnoughDataException e) {
            fail(e);
        }
    }

    /**
     * Test with a 90 mins duration
     */
    @Test
    void testOptimise90min() {
        try {
            assertEquals(ZonedDateTime.parse("2020-10-09T00:00Z"),
                    PO.optimiseWithAbsoluteStartTime(Duration.ofSeconds(90 * 60 + 30), START_TIME_1,
                            PRICE_LIST).optimisedStartTime);

            assertEquals(ZonedDateTime.parse("2020-10-09T00:00Z"),
                    PO.optimiseWithAbsoluteStartTime(Duration.ofMinutes(90), START_TIME_2,
                            PRICE_LIST).optimisedStartTime);

            assertEquals(ZonedDateTime.parse("2020-10-09T04:30Z"),
                    PO.optimiseWithAbsoluteStartTime(Duration.ofMinutes(90), START_TIME_3,
                            PRICE_LIST).optimisedStartTime);
        } catch (NotEnoughDataException e) {
            fail(e);
        }
    }

    /**
     * Test with a 3 hour duration
     */
    @Test
    void testOptimise3h() {
        try {
            assertEquals(ZonedDateTime.parse("2020-10-09T03:00Z"),
                    PO.optimiseWithAbsoluteStartTime(Duration.ofHours(3), START_TIME_1, PRICE_LIST).optimisedStartTime);

            assertEquals(ZonedDateTime.parse("2020-10-09T03:00Z"),
                    PO.optimiseWithAbsoluteStartTime(Duration.ofHours(3), START_TIME_2, PRICE_LIST).optimisedStartTime);

            assertEquals(ZonedDateTime.parse("2020-10-09T03:00Z"),
                    PO.optimiseWithAbsoluteStartTime(Duration.ofHours(3), START_TIME_3, PRICE_LIST).optimisedStartTime);
        } catch (NotEnoughDataException e) {
            fail(e);
        }
    }

    /**
     * Test with a 90 mins duration
     */
    @Test
    void testCalculateCostForDuration() {
        try {
            // excl. VAT
            // 00.00 - 01.30 => 1 + 1 + 1 = 3
            assertEquals(BigDecimal.valueOf(3),
                    PO.calculateCostForDuration(Duration.ofMinutes(90), START_TIME_2, PRICE_LIST, false)
                            .stripTrailingZeros());
            // 00.45 - 02.15 => 0.5 + 1 + 9 + 4.5 = 15
            assertEquals(BigDecimal.valueOf(15),
                    PO.calculateCostForDuration(Duration.ofMinutes(90), START_TIME_3, PRICE_LIST, false)
                            .stripTrailingZeros());
            // 01.40 - 04.15 => 6 + 9 + 2 + 2 + 2 + 1 = 22
            assertEquals(BigDecimal.valueOf(22),
                    PO.calculateCostForDuration(Duration.ofMinutes(155), START_TIME_4, PRICE_LIST, false)
                            .stripTrailingZeros());

            // incl. VAT
            // 00.00 - 01.30 => 1.5 + 1.5 + 1.5 = 3
            assertEquals(BigDecimal.valueOf(4.5),
                    PO.calculateCostForDuration(Duration.ofMinutes(90), START_TIME_2, PRICE_LIST, true)
                            .stripTrailingZeros());
            // 00.45 - 02.15 => 0.75 + 1.5 + 13.5 + 6.75 = 22.5
            assertEquals(BigDecimal.valueOf(22.5),
                    PO.calculateCostForDuration(Duration.ofMinutes(90), START_TIME_3, PRICE_LIST, true)
                            .stripTrailingZeros());

            // 01.40 - 04.15 => 9 + 13.5 + 3 + 3 + 3 + 1.5 = 33
            assertEquals(BigDecimal.valueOf(33),
                    PO.calculateCostForDuration(Duration.ofMinutes(155), START_TIME_4, PRICE_LIST, true)
                            .stripTrailingZeros());

        } catch (NotEnoughDataException e) {
            fail(e);
        }
    }

    /**
     * Test activities outside of the given price range
     */
    @Test
    void testCalculateCostForDurationExceptions1() {
        // start time is before the price range start
        assertThrows(NotEnoughDataException.class, () -> {
            PO.calculateCostForDuration(Duration.ofHours(2), START_TIME_1, PRICE_LIST, false);
        });
        // during is longer then the price range
        assertThrows(NotEnoughDataException.class, () -> {
            PO.calculateCostForDuration(Duration.ofHours(10), START_TIME_2, PRICE_LIST, false);
        });

        try {
            // within range
            PO.calculateCostForDuration(Duration.ofHours(2), START_TIME_3, PRICE_LIST, false);
            // full range window
            PO.calculateCostForDuration(Duration.ofHours(7), START_TIME_2, PRICE_LIST, false);
        } catch (NotEnoughDataException e) {
            fail(e);
        }
    }

    /**
     * Test activities outside of the given price range
     */
    @Test
    void testCalculateCostForDurationExceptions2() {
        // start time is before the price range start
        assertThrows(NotEnoughDataException.class, () -> {
            PO.calculateCostForDuration(Duration.ofHours(2), START_TIME_1, new ArrayList<>(), false);
        });
    }

    @Test
    void testOptimiseWithDelayedStartAndEarlierFinishTime() {
        try {
            // setting up a price profile of 1,1, 1,9, 9,2, 2,2, 2,3, 1,1, 8,8
            // use window from 1.40 to 4.30
            assertEquals(ZonedDateTime.parse("2020-10-09T02:30Z"),
                    PO.optimise(Duration.ofMinutes(50), 1, 40, 4, 30, START_TIME_1, PRICE_LIST).optimisedStartTime);
        } catch (NotEnoughDataException e) {
            fail(e);
        }
    }

    // start hour/minute only
    @Test
    void testOptimiseWithDelayedStartTime() {
        try {
            // setting up a price profile of 1,1, 1,9, 9,2, 2,2, 2,3, 1,1, 8,8
            // use window from 1.40 to 4.30
            assertEquals(ZonedDateTime.parse("2020-10-09T05:00Z"),
                    PO.optimiseWithRecurringStartTime(Duration.ofMinutes(65), 4, 30, START_TIME_1,
                            PRICE_LIST).optimisedStartTime);
        } catch (NotEnoughDataException e) {
            fail(e);
        }
    }

    // start time & end hour/minute
    @Test
    void testOptimiseWithGivenStartTimeAndEarlierFinishTime() {
        try {
            // setting up a price profile of 1,1, 1,9, 9,2, 2,2, 2,3, 1,1, 8,8
            // use window from 1.40 to 4.30
            assertEquals(ZonedDateTime.parse("2020-10-09T02:30Z"),
                    PO.optimiseWithRecurringEndTime(Duration.ofMinutes(65), START_TIME_4, 4, 30,
                            PRICE_LIST).optimisedStartTime);
        } catch (NotEnoughDataException e) {
            fail(e);
        }
    }

    @Test
    void testGetFutureTimeWithHourAndMinute() {
        ZonedDateTime startTime = ZonedDateTime.parse("2020-10-08T12:30Z");

        assertEquals(ZonedDateTime.parse("2020-10-08T23:15Z"), PO.getFutureTimeWithHourAndMinute(startTime, 23, 15));
        assertEquals(ZonedDateTime.parse("2020-10-08T12:30Z"), PO.getFutureTimeWithHourAndMinute(startTime, 12, 30));
        assertEquals(ZonedDateTime.parse("2020-10-09T09:15Z"), PO.getFutureTimeWithHourAndMinute(startTime, 9, 15));
        assertEquals(ZonedDateTime.parse("2020-10-09T00:00Z"), PO.getFutureTimeWithHourAndMinute(startTime, 0, 0));
    }

    @Test
    void testFindPriceSlotForTime() {
        try {
            // beginning of the list
            assertEquals(0, PO.findPriceSlotForTime(PRICE_LIST, ZonedDateTime.parse("2020-10-09T00:00Z")));
            // beginning of the second slot
            assertEquals(1, PO.findPriceSlotForTime(PRICE_LIST, ZonedDateTime.parse("2020-10-09T00:30Z")));
            // middle of the third slot
            assertEquals(2, PO.findPriceSlotForTime(PRICE_LIST, ZonedDateTime.parse("2020-10-09T01:15Z")));
            // middle of the last slot
            assertEquals(13, PO.findPriceSlotForTime(PRICE_LIST, ZonedDateTime.parse("2020-10-09T06:45Z")));
        } catch (NotEnoughDataException e) {
            fail(e);
        }

        // before the first slot
        assertThrows(NotEnoughDataException.class, () -> {
            PO.findPriceSlotForTime(PRICE_LIST, ZonedDateTime.parse("2020-10-08T22:45Z"));
        });
        // end of the last slot
        assertThrows(NotEnoughDataException.class, () -> {
            PO.findPriceSlotForTime(PRICE_LIST, ZonedDateTime.parse("2020-10-08T07:00Z"));
        });
        // after the last slot
        assertThrows(NotEnoughDataException.class, () -> {
            PO.findPriceSlotForTime(PRICE_LIST, ZonedDateTime.parse("2020-10-08T07:30Z"));
        });
    }
}
