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
package org.openhab.binding.astro.handler.test;

import static org.mockito.Mockito.*;
import static org.openhab.binding.astro.internal.AstroBindingConstants.THING_TYPE_SUN;
import static org.openhab.binding.astro.test.cases.AstroBindingTestsData.*;

import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.handler.SunHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.TimeZoneProvider;
import org.openhab.core.scheduler.CronScheduler;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerCallback;

/**
 * Tests for the {@link AstroThingHandler}
 * <p>
 * This class tests the required configuration for the astro thing.
 *
 * @author Petar Valchev - Initial contribution
 * @author Svilen Valkanov - Reworked to plain unit tests, removed irrelevant tests
 * @author Christoph Weitkamp - Migrated tests to pure Java
 */
public class AstroValidConfigurationTest {

    private final String NULL_LONGITUDE = "51.2,null";
    private final String NULL_LATITUDE = "null,25.4";

    @Test
    public void testIfGeolocationIsProvidedForASunThing_theThingStatusBecomesONLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE);
        assertThingStatus(thingConfiguration, ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    public void testIfGeolocationIsProvidedForAMoonThing_theThingStatusBecomesONLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE);
        assertThingStatus(thingConfiguration, ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    public void testIfGeolocationForASunThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, null);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfGeolocationForAMoonThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, null);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheLatitudeForASunThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LATITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheLatitudeForAMoonThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LATITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheLongitudeForASunThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LONGITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheLongitudeForAMoonThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LONGITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheIntervalForASunThingIsLessThan1_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, Integer.valueOf(0));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheIntervalForAMoonThingIsLessThan1_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, Integer.valueOf(0));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheIntervalForASunThingIsGreaterThan86400_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, Integer.valueOf(86401));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheIntervalForAMoonThingIsGreaterThan86400_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, Integer.valueOf(86401));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    private void assertThingStatus(Configuration configuration, ThingStatus expectedStatus,
            ThingStatusDetail expectedStatusDetail) {
        ThingUID thingUID = new ThingUID(THING_TYPE_SUN, TEST_SUN_THING_ID);

        Thing thing = mock(Thing.class);
        when(thing.getConfiguration()).thenReturn(configuration);
        when(thing.getUID()).thenReturn(thingUID);

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        CronScheduler cronScheduler = mock(CronScheduler.class);
        TimeZoneProvider timeZoneProvider = mock(TimeZoneProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.systemDefault());
        ThingHandler sunHandler = new SunHandler(thing, cronScheduler, timeZoneProvider);
        sunHandler.setCallback(callback);

        sunHandler.initialize();

        ThingStatusInfo expectedThingStatus = new ThingStatusInfo(expectedStatus, expectedStatusDetail, null);
        verify(callback, times(1)).statusUpdated(thing, expectedThingStatus);
    }
}
