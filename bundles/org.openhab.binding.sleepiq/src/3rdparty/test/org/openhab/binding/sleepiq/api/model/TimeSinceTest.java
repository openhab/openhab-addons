/*
 * Copyright 2017 Gregory Moyer
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.openhab.binding.sleepiq.api.model;

import static org.junit.Assert.assertEquals;

import java.time.Duration;

import org.junit.Test;
import org.openhab.binding.sleepiq.api.model.TimeSince;

public class TimeSinceTest
{
    @Test
    public void testWithDuration()
    {
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
    public void testToString()
    {
        assertEquals("00:00:00",
                     new TimeSince().withDuration(Duration.parse("PT00H00M00S")).toString());
        assertEquals("02:03:04",
                     new TimeSince().withDuration(Duration.parse("PT02H03M04S")).toString());
        assertEquals("12:34:56",
                     new TimeSince().withDuration(Duration.parse("PT12H34M56S")).toString());
        assertEquals("1 d 02:03:04",
                     new TimeSince().withDuration(Duration.parse("P1DT02H03M04S")).toString());
        assertEquals("12 d 23:34:45",
                     new TimeSince().withDuration(Duration.parse("P12DT23H34M45S")).toString());
    }

    @Test
    public void testParse()
    {
        assertEquals(Duration.parse("PT00H00M00S"), TimeSince.parse("00:00:00").getDuration());
        assertEquals(Duration.parse("PT2H3M4S"), TimeSince.parse("02:03:04").getDuration());
        assertEquals(Duration.parse("PT12H34M56S"), TimeSince.parse("12:34:56").getDuration());
        assertEquals(Duration.parse("P1DT2H3M4S"), TimeSince.parse("1 d 02:03:04").getDuration());
        assertEquals(Duration.parse("P12DT23H34M45S"),
                     TimeSince.parse("12 d 23:34:45").getDuration());
    }
}
