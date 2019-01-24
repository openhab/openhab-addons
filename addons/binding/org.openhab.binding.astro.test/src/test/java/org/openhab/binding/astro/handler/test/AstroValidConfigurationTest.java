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
package org.openhab.binding.astro.handler.test;

import static org.mockito.Mockito.*;
import static org.openhab.binding.astro.internal.AstroBindingConstants.THING_TYPE_SUN;
import static org.openhab.binding.astro.test.cases.AstroBindingTestsData.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.scheduler.CronScheduler;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.ThingStatusInfo;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerCallback;
import org.junit.Test;
import org.openhab.binding.astro.internal.handler.AstroThingHandler;
import org.openhab.binding.astro.internal.handler.SunHandler;

/**
 * Tests for the {@link AstroThingHandler}
 * <p>
 * This class tests the required configuration for the astro thing.
 *
 * @author Petar Valchev - Initial implementation
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
        assertThingStatus(thingConfiguration, ThingStatus.ONLINE);
    }

    @Test
    public void testIfGeolocationIsProvidedForAMoonThing_theThingStatusBecomesONLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, INTERVAL_DEFAULT_VALUE);
        assertThingStatus(thingConfiguration, ThingStatus.ONLINE);
    }

    @Test
    public void testIfGeolocationForASunThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, null);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE);
    }

    @Test
    public void testIfGeolocationForAMoonThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, null);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE);
    }

    @Test
    public void testIfTheLatitudeForASunThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LATITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE);
    }

    @Test
    public void testIfTheLatitudeForAMoonThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LATITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE);
    }

    @Test
    public void testIfTheLongitudeForASunThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LONGITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE);
    }

    @Test
    public void testIfTheLongitudeForAMoonThingIsNull_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LONGITUDE);
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE);
    }

    @Test
    public void testIfTheIntervalForASunThingIsLessThan1_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(0));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE);
    }

    @Test
    public void testIfTheIntervalForAMoonThingIsLessThan1_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(0));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE);
    }

    @Test
    public void testIfTheIntervalForASunThingIsGreaterThan86400_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(86401));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE);
    }

    @Test
    public void testIfTheIntervalForAMoonThingIsGreaterThan86400_theThingStatusBecomesOFFLINE() {
        Configuration thingConfiguration = new Configuration();
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE);
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(86401));
        assertThingStatus(thingConfiguration, ThingStatus.OFFLINE);
    }

    private void assertThingStatus(Configuration configuration, ThingStatus expectedStatus) {
        ThingUID thingUID = new ThingUID(THING_TYPE_SUN, TEST_SUN_THING_ID);

        Thing thing = mock(Thing.class);
        when(thing.getConfiguration()).thenReturn(configuration);
        when(thing.getUID()).thenReturn(thingUID);

        ThingHandlerCallback callback = mock(ThingHandlerCallback.class);
        CronScheduler cronScheduler = mock(CronScheduler.class);
        ThingHandler sunHandler = new SunHandler(thing, cronScheduler);
        sunHandler.setCallback(callback);

        sunHandler.initialize();

        ThingStatusInfo expectedThingStatus = new ThingStatusInfo(expectedStatus, ThingStatusDetail.NONE, null);
        verify(callback, times(1)).statusUpdated(thing, expectedThingStatus);
    }
}
