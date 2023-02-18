/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.sleepiq.api.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.sleepiq.internal.api.dto.TimeSince;

/**
 * The {@link TimeSinceText} tests TimeSince.
 *
 * @author Gregory Moyer - Initial contribution
 */
@NonNullByDefault
public class TimeSinceTest {
    @Test
    public void testWithDuration() {
        assertEquals(new TimeSince().withDuration(0, 0, 0, 0).getDuration(),
                new TimeSince().withDuration(Duration.parse("PT00H00M00S")).getDuration());
        assertEquals(new TimeSince().withDuration(0, 2, 3, 4).getDuration(),
                new TimeSince().withDuration(Duration.parse("PT02H03M04S")).getDuration());
        assertEquals(new TimeSince().withDuration(0, 12, 34, 56).getDuration(),
                new TimeSince().withDuration(Duration.parse("PT12H34M56S")).getDuration());
        assertEquals(new TimeSince().withDuration(1, 2, 3, 4).getDuration(),
                new TimeSince().withDuration(Duration.parse("P1DT02H03M04S")).getDuration());
        assertEquals(new TimeSince().withDuration(12, 23, 34, 45).getDuration(),
                new TimeSince().withDuration(Duration.parse("P12DT23H34M45S")).getDuration());
    }

    @Test
    public void testToString() {
        assertEquals("00:00:00", new TimeSince().withDuration(Duration.parse("PT00H00M00S")).toString());
        assertEquals("02:03:04", new TimeSince().withDuration(Duration.parse("PT02H03M04S")).toString());
        assertEquals("12:34:56", new TimeSince().withDuration(Duration.parse("PT12H34M56S")).toString());
        assertEquals("1 d 02:03:04", new TimeSince().withDuration(Duration.parse("P1DT02H03M04S")).toString());
        assertEquals("12 d 23:34:45", new TimeSince().withDuration(Duration.parse("P12DT23H34M45S")).toString());
    }

    @Test
    public void testParse() {
        assertEquals(Duration.parse("PT00H00M00S"), TimeSince.parse("00:00:00").getDuration());
        assertEquals(Duration.parse("PT2H3M4S"), TimeSince.parse("02:03:04").getDuration());
        assertEquals(Duration.parse("PT12H34M56S"), TimeSince.parse("12:34:56").getDuration());
        assertEquals(Duration.parse("P1DT2H3M4S"), TimeSince.parse("1 d 02:03:04").getDuration());
        assertEquals(Duration.parse("P12DT23H34M45S"), TimeSince.parse("12 d 23:34:45").getDuration());
    }
}
