/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.handler.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.ThingStatus
import org.junit.Test
import org.openhab.binding.astro.test.AstroOSGiTest
import org.openhab.binding.astro.test.AstroOSGiTest.AcceptedItemType

/**
 * OSGi tests for the {@link AstroThingHandler}
 *
 * This class tests the required configuration for the astro thing.
 *
 * @author Petar Valchev
 *
 */
class AstroValidConfigurationTest extends AstroOSGiTest {
    private final String NULL_LONGITUDE = "51.2,null"
    private final String NULL_LATITUDE = "null,25.4"

    // refresh interval property for the thing configuration
    private final String INTERVAL_PROPERTY = "interval"

    @Test
    public void 'if geolocation is provided for a sun thing, the thing status becomes ONLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        assertThingStatus(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.ONLINE)
    }

    @Test
    public void 'if geolocation is provided for a moon thing, the thing status becomes ONLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        assertThingStatus(TEST_MOON_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.ONLINE)
    }

    @Test
    public void 'if geolocation for a sun thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, null)
        assertThingStatus(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if geolocation for a moon thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, null)
        assertThingStatus(TEST_MOON_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if no geolocation is provided for a sun thing, the thing status becomes UNINITIALIZED'(){
        Configuration thingConfiguration = new Configuration()
        assertThingStatus(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.UNINITIALIZED)
    }

    @Test
    public void 'if no geolocation is provided for a moon thing, the thing status becomes UNINITIALIZED'(){
        Configuration thingConfiguration = new Configuration()
        assertThingStatus(TEST_MOON_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.UNINITIALIZED)
    }

    @Test
    public void 'if the latitude for a sun thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LATITUDE)
        assertThingStatus(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the latitude for a moon thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LATITUDE)
        assertThingStatus(TEST_MOON_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the longitude for a sun thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LONGITUDE)
        assertThingStatus(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the longitude for a moon thing is null, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, NULL_LONGITUDE)
        assertThingStatus(TEST_MOON_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the interval for a sun thing is less than 1, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(0))
        assertThingStatus(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the interval for a moon thing is less than 1, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(0))
        assertThingStatus(TEST_MOON_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the interval for a sun thing is more than 86400, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(86401))
        assertThingStatus(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the interval for a moon thing is more than 86400, the thing status becomes OFFLINE'(){
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, new Integer(86401))
        assertThingStatus(TEST_MOON_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.OFFLINE)
    }

    @Test
    public void 'if the interval for a sun thing is null, the thing status becomes ONLINE'(){
        // the status becomes online, because the default value for the interval is used
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, null)
        assertThingStatus(TEST_SUN_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.ONLINE)
    }

    @Test
    public void 'if the interval for a moon thing is null, the thing status becomes ONLINE'(){
        // the status becomes online, because the default value for the interval is used
        Configuration thingConfiguration = new Configuration()
        thingConfiguration.put(GEOLOCATION_PROPERTY, GEOLOCATION_VALUE)
        thingConfiguration.put(INTERVAL_PROPERTY, null)
        assertThingStatus(TEST_MOON_THING_ID, DEFAULT_TEST_CHANNEL_ID, AcceptedItemType.DATE_TIME, thingConfiguration, ThingStatus.ONLINE)
    }
}
