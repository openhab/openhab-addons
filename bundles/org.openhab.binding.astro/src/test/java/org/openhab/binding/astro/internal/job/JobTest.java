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
package org.openhab.binding.astro.internal.job;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Calendar;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.model.Range;
import org.openhab.binding.astro.internal.util.DateTimeUtils;

/**
 * Test class for {@link Job}.
 *
 * @author Leo Siepel - Initial contribution
 */

@NonNullByDefault
public class JobTest {

    @BeforeEach
    public void init() {
    }

    @Test
    public void adjustRangeToConfigForceTest() {
        // arrange
        AstroChannelConfig config = new AstroChannelConfig();
        config.earliest = "08:00";
        config.latest = "22:00";
        config.forceEvent = true;
        Calendar pointInTime = DateTimeUtils.createCalendarForToday(12, 0);
        Range startNull = new Range(null, pointInTime);
        Range endNull = new Range(pointInTime, null);
        Range bothNull = new Range(null, null);
        Range bothNNShouldCorrect = new Range(DateTimeUtils.createCalendarForToday(6, 0),
                DateTimeUtils.createCalendarForToday(22, 0));
        Range bothNNShouldNotCorrect = new Range(pointInTime, pointInTime);

        // act
        Range startNullResult = Job.adjustRangeToConfig(startNull, config);
        Range endNullResult = Job.adjustRangeToConfig(endNull, config);
        Range bothNullResult = Job.adjustRangeToConfig(bothNull, config);
        Range bothNNShouldCorrectResult = Job.adjustRangeToConfig(bothNNShouldCorrect, config);
        Range bothNNSouldNotCorrectResult = Job.adjustRangeToConfig(bothNNShouldNotCorrect, config);

        Calendar fixedStart = DateTimeUtils.getAdjustedEarliest(pointInTime, config);
        Calendar fixdedEnd = DateTimeUtils.getAdjustedLatest(pointInTime, config);

        // assert
        assertEquals(fixedStart.getTime(), startNullResult.getStart().getTime());
        assertEquals(pointInTime.getTime(), startNullResult.getEnd().getTime());
        assertEquals(pointInTime, endNullResult.getStart());
        assertEquals(fixdedEnd, endNullResult.getEnd());
        assertEquals(fixedStart, bothNullResult.getStart());
        assertEquals(fixdedEnd, bothNullResult.getEnd());
        assertEquals(fixedStart, bothNNShouldCorrectResult.getStart());
        assertEquals(fixdedEnd, bothNNShouldCorrectResult.getEnd());
        assertEquals(pointInTime, bothNNSouldNotCorrectResult.getStart());
        assertEquals(pointInTime, bothNNSouldNotCorrectResult.getEnd());
    }

    @Test
    public void adjustRangeToConfigTestSkipForceTest() {
        // arrange
        AstroChannelConfig config = new AstroChannelConfig();
        config.earliest = "08:00";
        config.latest = "22:00";
        config.forceEvent = false;
        Calendar pointInTime = DateTimeUtils.createCalendarForToday(12, 0);
        Range startNull = new Range(null, pointInTime);
        Range endNull = new Range(pointInTime, null);
        Range bothNull = new Range(null, null);
        Range bothNNShouldCorrect = new Range(DateTimeUtils.createCalendarForToday(6, 0),
                DateTimeUtils.createCalendarForToday(22, 0));
        Range bothNNShouldNotCorrect = new Range(pointInTime, pointInTime);

        // act
        Range startNullResult = Job.adjustRangeToConfig(startNull, config);
        Range endNullResult = Job.adjustRangeToConfig(endNull, config);
        Range bothNullResult = Job.adjustRangeToConfig(bothNull, config);
        Range bothNNShouldCorrectResult = Job.adjustRangeToConfig(bothNNShouldCorrect, config);
        Range bothNNSouldNotCorrectResult = Job.adjustRangeToConfig(bothNNShouldNotCorrect, config);

        Calendar fixedStart = DateTimeUtils.getAdjustedEarliest(pointInTime, config);
        Calendar fixdedEnd = DateTimeUtils.getAdjustedLatest(pointInTime, config);

        // assert
        assertEquals(null, startNullResult.getStart());
        assertEquals(pointInTime, startNullResult.getEnd());
        assertEquals(pointInTime, endNullResult.getStart());
        assertEquals(null, endNullResult.getEnd());
        assertEquals(null, bothNullResult.getStart());
        assertEquals(null, bothNullResult.getEnd());
        assertEquals(fixedStart, bothNNShouldCorrectResult.getStart());
        assertEquals(fixdedEnd, bothNNShouldCorrectResult.getEnd());
        assertEquals(pointInTime, bothNNSouldNotCorrectResult.getStart());
        assertEquals(pointInTime, bothNNSouldNotCorrectResult.getEnd());
    }
}
