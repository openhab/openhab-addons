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
package org.openhab.binding.astro.internal.handler;

import static org.mockito.Mockito.*;
import static org.openhab.binding.astro.internal.AstroBindingConstants.THING_TYPE_SUN;
import static org.openhab.binding.astro.internal.CommonTestConstants.*;

import java.time.Instant;
import java.time.InstantSource;
import java.time.ZoneId;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.i18n.LocaleProvider;
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
@NonNullByDefault
public class ValidConfigurationTest {

    private static final String NULL_LONGITUDE = "51.2,null";
    private static final String NULL_LATITUDE = "null,25.4";

    @Test
    public void testIfGeolocationIsProvidedForASunThingTheThingStatusBecomesONLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE);
        assertThingStatus(thingConfiguration, ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    public void testIfGeolocationIsProvidedForAMoonThingTheThingStatusBecomesONLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE);
        assertThingStatus(thingConfiguration, ThingStatus.ONLINE, ThingStatusDetail.NONE);
    }

    @Test
    public void testIfGeolocationForASunThingIsNullTheThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, null);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfGeolocationForAMoonThingIsNullTheThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, null);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheLatitudeForASunThingIsNullTheThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LATITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheLatitudeForAMoonThingIsNullTheThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LATITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheLongitudeForASunThingIsNullTheThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LONGITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheLongitudeForAMoonThingIsNullTheThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LONGITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheIntervalForASunThingIsLessThan1TheThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, Integer.valueOf(0));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheIntervalForAMoonThingIsLessThan1TheThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, Integer.valueOf(0));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheIntervalForASunThingIsGreaterThan86400TheThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, Integer.valueOf(86401));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR);
    }

    @Test
    public void testIfTheIntervalForAMoonThingIsGreaterThan86400TheThingStatusBecomesOFFLINE() {
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
        LocaleProvider localeProvider = mock(LocaleProvider.class);
        when(timeZoneProvider.getTimeZone()).thenReturn(ZoneId.systemDefault());
        ThingHandler sunHandler = new SunHandler(thing, cronScheduler, timeZoneProvider, localeProvider,
                InstantSource.fixed(Instant.ofEpochMilli(1645671600000L)));
        sunHandler.setCallback(callback);

        sunHandler.initialize();

        ThingStatusInfo expectedThingStatus = new ThingStatusInfo(expectedStatus, expectedStatusDetail, null);
        verify(callback, times(1)).statusUpdated(thing, expectedThingStatus);
    }
}
