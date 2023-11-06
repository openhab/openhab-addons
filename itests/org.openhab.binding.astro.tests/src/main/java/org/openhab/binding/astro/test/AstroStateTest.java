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
package org.openhab.binding.astro.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.openhab.binding.astro.internal.AstroBindingConstants.*;
import static org.openhab.binding.astro.test.cases.AstroBindingTestsData.*;
import static org.openhab.binding.astro.test.cases.AstroParametrizedTestCases.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.astro.internal.calc.MoonCalc;
import org.openhab.binding.astro.internal.calc.SunCalc;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.util.PropertyUtils;
import org.openhab.binding.astro.test.cases.AstroParametrizedTestCases;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.types.State;

/**
 * Tests for the Astro Channels state
 *
 * @see AstroParametrizedTestCases
 * @author Petar Valchev - Initial contribution
 * @author Svilen Valkanov - Reworked to plain unit tests
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 * @author Christoph Weitkamp - Migrated tests to pure Java
 */
public class AstroStateTest {

    // These test result timestamps are adapted for the +03:00 time zone
    private static final ZoneId ZONE_ID = ZoneId.of("+03:00");

    public static List<Object[]> data() {
        AstroParametrizedTestCases cases = new AstroParametrizedTestCases();
        return cases.getCases();
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testParametrized(String thingID, String channelId, State expectedState) {
        try {
            assertStateUpdate(thingID, channelId, expectedState);
        } catch (Exception e) {
            // do nothing
        }
    }

    private void assertStateUpdate(String thingID, String channelId, State expectedState) throws Exception {
        ChannelUID testItemChannelUID = new ChannelUID(getThingUID(thingID), channelId);
        State state = PropertyUtils.getState(testItemChannelUID, new AstroChannelConfig(), getPlanet(thingID), ZONE_ID);
        assertEquals(expectedState, state);
    }

    private ThingUID getThingUID(String thingID) {
        switch (thingID) {
            case (TEST_SUN_THING_ID):
                return new ThingUID(THING_TYPE_SUN, thingID);
            case (TEST_MOON_THING_ID):
                return new ThingUID(THING_TYPE_MOON, thingID);
            default:
                return null;
        }
    }

    private Planet getPlanet(String thingID) {
        LocalDateTime time = LocalDateTime.of(TEST_YEAR, TEST_MONTH, TEST_DAY, 0, 0);
        ZonedDateTime zonedTime = ZonedDateTime.ofLocal(time, ZONE_ID, null);
        Calendar calendar = GregorianCalendar.from(zonedTime);
        switch (thingID) {
            case (TEST_SUN_THING_ID):
                SunCalc sunCalc = new SunCalc();
                return sunCalc.getSunInfo(calendar, TEST_LATITUDE, TEST_LONGITUDE, null, false);
            case (TEST_MOON_THING_ID):
                MoonCalc moonCalc = new MoonCalc();
                return moonCalc.getMoonInfo(calendar, TEST_LATITUDE, TEST_LONGITUDE);
            default:
                return null;
        }
    }
}
