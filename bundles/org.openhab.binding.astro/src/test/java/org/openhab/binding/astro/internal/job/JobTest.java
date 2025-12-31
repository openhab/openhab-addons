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
package org.openhab.binding.astro.internal.job;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

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

    private static final TimeZone TIME_ZONE = TimeZone.getTimeZone("Asia/Tbilisi");

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
        Calendar pointInTime = DateTimeUtils.createCalendarForToday(12, 0, TIME_ZONE, Locale.ROOT);
        Range startNull = new Range(null, pointInTime);
        Range endNull = new Range(pointInTime, null);
        Range bothNull = new Range(null, null);
        Range bothNNShouldCorrect = new Range(DateTimeUtils.createCalendarForToday(6, 0, TIME_ZONE, Locale.ROOT),
                DateTimeUtils.createCalendarForToday(23, 10, TIME_ZONE, Locale.ROOT));
        Range bothNNShouldNotCorrect = new Range(pointInTime, pointInTime);

        // act
        Range startNullResult = Job.adjustRangeToConfig(startNull, config, TIME_ZONE, Locale.ROOT);
        Range endNullResult = Job.adjustRangeToConfig(endNull, config, TIME_ZONE, Locale.ROOT);
        Range bothNullResult = Job.adjustRangeToConfig(bothNull, config, TIME_ZONE, Locale.ROOT);
        Range bothNNShouldCorrectResult = Job.adjustRangeToConfig(bothNNShouldCorrect, config, TIME_ZONE, Locale.ROOT);
        Range bothNNSouldNotCorrectResult = Job.adjustRangeToConfig(bothNNShouldNotCorrect, config, TIME_ZONE,
                Locale.ROOT);

        Calendar fixedStart = DateTimeUtils.getAdjustedEarliest(pointInTime, config);
        Calendar fixedEnd = DateTimeUtils.getAdjustedLatest(pointInTime, config);

        // assert
        Calendar startNullResultStart = startNullResult.getStart();
        Calendar startNullResultEnd = startNullResult.getEnd();
        assertNotNull(startNullResultStart);
        assertNotNull(startNullResultEnd);
        assertEquals(fixedStart.getTime(), startNullResultStart.getTime());
        assertEquals(pointInTime.getTime(), startNullResultEnd.getTime());
        assertEquals(pointInTime, endNullResult.getStart());
        assertEquals(fixedEnd, endNullResult.getEnd());
        assertEquals(fixedStart, bothNullResult.getStart());
        assertEquals(fixedEnd, bothNullResult.getEnd());
        assertEquals(fixedStart, bothNNShouldCorrectResult.getStart());
        assertEquals(fixedEnd, bothNNShouldCorrectResult.getEnd());
        assertEquals(pointInTime, bothNNSouldNotCorrectResult.getStart());
        assertEquals(pointInTime, bothNNSouldNotCorrectResult.getEnd());

        // arrange more (add negative offset)
        config.offset = -49;
        Calendar newPointInTime = (Calendar) pointInTime.clone();
        newPointInTime.add(Calendar.MINUTE, -49);
        Calendar outerFixedPoint = (Calendar) fixedEnd.clone();
        fixedEnd.add(Calendar.MINUTE, -49);

        // act again
        startNullResult = Job.adjustRangeToConfig(startNull, config, TIME_ZONE, Locale.ROOT);
        endNullResult = Job.adjustRangeToConfig(endNull, config, TIME_ZONE, Locale.ROOT);
        bothNullResult = Job.adjustRangeToConfig(bothNull, config, TIME_ZONE, Locale.ROOT);
        bothNNShouldCorrectResult = Job.adjustRangeToConfig(bothNNShouldCorrect, config, TIME_ZONE, Locale.ROOT);
        bothNNSouldNotCorrectResult = Job.adjustRangeToConfig(bothNNShouldNotCorrect, config, TIME_ZONE, Locale.ROOT);

        // assert again
        startNullResultStart = startNullResult.getStart();
        startNullResultEnd = startNullResult.getEnd();
        assertNotNull(startNullResultStart);
        assertNotNull(startNullResultEnd);
        assertEquals(fixedStart.getTime(), startNullResultStart.getTime());
        assertEquals(newPointInTime.getTime(), startNullResultEnd.getTime());
        assertEquals(newPointInTime, endNullResult.getStart());
        assertEquals(fixedEnd, endNullResult.getEnd());
        assertEquals(fixedStart, bothNullResult.getStart());
        assertEquals(fixedEnd, bothNullResult.getEnd());
        assertEquals(fixedStart, bothNNShouldCorrectResult.getStart());
        assertEquals(outerFixedPoint, bothNNShouldCorrectResult.getEnd());
        assertEquals(newPointInTime, bothNNSouldNotCorrectResult.getStart());
        assertEquals(newPointInTime, bothNNSouldNotCorrectResult.getEnd());

        // arrange even more (add negative offset)
        config.offset = 93;
        newPointInTime = (Calendar) pointInTime.clone();
        newPointInTime.add(Calendar.MINUTE, 93);
        fixedEnd.add(Calendar.MINUTE, 49);
        outerFixedPoint = (Calendar) fixedStart.clone();
        fixedStart.add(Calendar.MINUTE, 93);

        // act yet again
        startNullResult = Job.adjustRangeToConfig(startNull, config, TIME_ZONE, Locale.ROOT);
        endNullResult = Job.adjustRangeToConfig(endNull, config, TIME_ZONE, Locale.ROOT);
        bothNullResult = Job.adjustRangeToConfig(bothNull, config, TIME_ZONE, Locale.ROOT);
        bothNNShouldCorrectResult = Job.adjustRangeToConfig(bothNNShouldCorrect, config, TIME_ZONE, Locale.ROOT);
        bothNNSouldNotCorrectResult = Job.adjustRangeToConfig(bothNNShouldNotCorrect, config, TIME_ZONE, Locale.ROOT);

        // assert yet again
        startNullResultStart = startNullResult.getStart();
        startNullResultEnd = startNullResult.getEnd();
        assertNotNull(startNullResultStart);
        assertNotNull(startNullResultEnd);
        assertEquals(fixedStart.getTime(), startNullResultStart.getTime());
        assertEquals(newPointInTime.getTime(), startNullResultEnd.getTime());
        assertEquals(newPointInTime, endNullResult.getStart());
        assertEquals(fixedEnd, endNullResult.getEnd());
        assertEquals(fixedStart, bothNullResult.getStart());
        assertEquals(fixedEnd, bothNullResult.getEnd());
        assertEquals(outerFixedPoint, bothNNShouldCorrectResult.getStart());
        assertEquals(fixedEnd, bothNNShouldCorrectResult.getEnd());
        assertEquals(newPointInTime, bothNNSouldNotCorrectResult.getStart());
        assertEquals(newPointInTime, bothNNSouldNotCorrectResult.getEnd());
    }

    @Test
    public void adjustRangeToConfigTestSkipForceTest() {
        // arrange
        AstroChannelConfig config = new AstroChannelConfig();
        config.earliest = "08:00";
        config.latest = "22:00";
        config.forceEvent = false;
        Calendar pointInTime = DateTimeUtils.createCalendarForToday(12, 0, TIME_ZONE, Locale.ROOT);
        Range startNull = new Range(null, pointInTime);
        Range endNull = new Range(pointInTime, null);
        Range bothNull = new Range(null, null);
        Range bothNNShouldCorrect = new Range(DateTimeUtils.createCalendarForToday(6, 0, TIME_ZONE, Locale.ROOT),
                DateTimeUtils.createCalendarForToday(23, 10, TIME_ZONE, Locale.ROOT));
        Range bothNNShouldNotCorrect = new Range(pointInTime, pointInTime);

        // act
        Range startNullResult = Job.adjustRangeToConfig(startNull, config, TIME_ZONE, Locale.ROOT);
        Range endNullResult = Job.adjustRangeToConfig(endNull, config, TIME_ZONE, Locale.ROOT);
        Range bothNullResult = Job.adjustRangeToConfig(bothNull, config, TIME_ZONE, Locale.ROOT);
        Range bothNNShouldCorrectResult = Job.adjustRangeToConfig(bothNNShouldCorrect, config, TIME_ZONE, Locale.ROOT);
        Range bothNNSouldNotCorrectResult = Job.adjustRangeToConfig(bothNNShouldNotCorrect, config, TIME_ZONE,
                Locale.ROOT);

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
