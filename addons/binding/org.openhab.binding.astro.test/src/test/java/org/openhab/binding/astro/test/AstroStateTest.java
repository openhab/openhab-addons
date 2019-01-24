/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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

import static org.junit.Assert.assertEquals;
import static org.openhab.binding.astro.internal.AstroBindingConstants.*;
import static org.openhab.binding.astro.test.cases.AstroBindingTestsData.*;
import static org.openhab.binding.astro.test.cases.AstroParametrizedTestCases.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.smarthome.core.i18n.TimeZoneProvider;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.types.State;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.openhab.binding.astro.internal.calc.MoonCalc;
import org.openhab.binding.astro.internal.calc.SunCalc;
import org.openhab.binding.astro.internal.config.AstroChannelConfig;
import org.openhab.binding.astro.internal.model.Planet;
import org.openhab.binding.astro.internal.util.PropertyUtils;
import org.openhab.binding.astro.test.cases.AstroParametrizedTestCases;

/**
 * Tests for the Astro Channels state
 *
 * @See {@link AstroParametrizedTestCases}
 * @author Petar Valchev - Initial implementation
 * @author Svilen Valkanov - Reworked to plain unit tests
 * @author Erdoan Hadzhiyusein - Adapted the class to work with the new DateTimeType
 * @author Christoph Weitkamp - Migrated tests to pure Java
 */
@RunWith(Parameterized.class)
public class AstroStateTest {

    private String thingID;
    private String channelId;
    private State expectedState;

    // These test result timestamps are adapted for the +03:00 time zone
    private static final ZoneId ZONE_ID = ZoneId.of("+03:00");

    public AstroStateTest(String thingID, String channelId, State expectedState) {
        this.thingID = thingID;
        this.channelId = channelId;
        this.expectedState = expectedState;
    }

    @Parameters
    public static List<Object[]> data() {
        AstroParametrizedTestCases cases = new AstroParametrizedTestCases();
        return cases.getCases();
    }

    @Test
    public void testParametrized() {
        PropertyUtils.unsetTimeZone();

        // Anonymous implementation of the service to adapt the time zone to the tested longitude and latitude
        PropertyUtils.setTimeZone(new TimeZoneProvider() {
            @Override
            public ZoneId getTimeZone() {
                return ZONE_ID;
            }
        });
        try {
            assertStateUpdate(thingID, channelId, expectedState);
        } catch (Exception e) {
            // do nothing
        }
    }

    private void assertStateUpdate(String thingID, String channelId, State expectedState) throws Exception {
        ChannelUID testItemChannelUID = new ChannelUID(getThingUID(thingID), channelId);
        State state = PropertyUtils.getState(testItemChannelUID, new AstroChannelConfig(), getPlanet(thingID));
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
                return sunCalc.getSunInfo(calendar, TEST_LATITUDE, TEST_LONGITUDE, null);
            case (TEST_MOON_THING_ID):
                MoonCalc moonCalc = new MoonCalc();
                return moonCalc.getMoonInfo(calendar, TEST_LATITUDE, TEST_LONGITUDE);
            default:
                return null;
        }
    }
}
