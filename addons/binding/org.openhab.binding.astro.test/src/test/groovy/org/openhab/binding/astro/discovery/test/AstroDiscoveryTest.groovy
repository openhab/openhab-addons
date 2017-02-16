/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.astro.discovery.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.config.discovery.DiscoveryListener
import org.eclipse.smarthome.config.discovery.DiscoveryResult
import org.eclipse.smarthome.config.discovery.DiscoveryService
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.junit.Before
import org.junit.Test
import org.openhab.binding.astro.AstroBindingConstants
import org.openhab.binding.astro.discovery.AstroDiscoveryService
import org.openhab.binding.astro.test.AstroOSGiTest

/**
 * OSGi tests for the {@link AstroDiscoveryService}
 *
 * @author Petar Valchev
 *
 */
class AstroDiscoveryTest extends AstroOSGiTest {
    AstroDiscoveryService discoveryService
    // a listener for discovery events
    DiscoveryListenerMock discoveryListenerMock

    @Before
    public void setUp(){
        discoveryListenerMock = new DiscoveryListenerMock()
        registerService(discoveryListenerMock, DiscoveryListener.class.getName())

        discoveryService = getService(DiscoveryService, AstroDiscoveryService)
        assertThat "Could not get DiscoveryService",
                discoveryService,
                is(notNullValue())

        discoveryService.addDiscoveryListener(discoveryListenerMock)
    }

    @Test
    public void 'sun thing is discovered'(){
        assertDiscoveredThing(TEST_SUN_THING_ID)
    }

    @Test
    public void 'moon thing is discovered'(){
        assertDiscoveredThing(TEST_MOON_THING_ID)
    }

    private void assertDiscoveredThing(String thingID){
        discoveryService.startScan()

        waitForAssert({
            switch(thingID) {
                case (TEST_SUN_THING_ID) :
                    assertThat "Sun thing was not discovered",
                    discoveryListenerMock.isSunThingDiscovered,
                    is(true)
                    break
                case(TEST_MOON_THING_ID) :
                    assertThat "Moon thing was not discovered",
                    discoveryListenerMock.isSunThingDiscovered,
                    is(true)
                    break
            }
        })
    }

    public class DiscoveryListenerMock implements DiscoveryListener{
        public boolean isSunThingDiscovered = false
        public boolean isMoonThingDiscovered = false

        private String sunThingUID = "$AstroBindingConstants.BINDING_ID:$AstroBindingConstants.SUN:$AstroBindingConstants.LOCAL".toString()
        private String moonThingUID = "$AstroBindingConstants.BINDING_ID:$AstroBindingConstants.MOON:$AstroBindingConstants.LOCAL".toString()

        @Override
        public void thingDiscovered(DiscoveryService source, DiscoveryResult result) {
            String discoveryResult = result.getThingUID().toString()
            switch(discoveryResult) {
                case (sunThingUID) :
                    isSunThingDiscovered = true
                    break
                case(moonThingUID) :
                    isMoonThingDiscovered = true
                    break
            }
        }

        @Override
        public void thingRemoved(DiscoveryService source, ThingUID thingUID) {
            // we are obligated to implement this method, but we won't use it, so we will leave it empty
        }

        @Override
        public Collection<ThingUID> removeOlderResults(DiscoveryService source, long timestamp,
                Collection<ThingTypeUID> thingTypeUIDs) {
            // we are obligated to implement this method, but we won't use it, so we will return null
            return null;
        }
    }
}
