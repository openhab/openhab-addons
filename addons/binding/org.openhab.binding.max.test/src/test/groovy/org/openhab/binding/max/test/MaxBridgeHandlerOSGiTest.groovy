/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.max.test

import static org.hamcrest.CoreMatchers.*
import static org.junit.Assert.*
import static org.junit.matchers.JUnitMatchers.*

import org.eclipse.smarthome.config.core.Configuration
import org.eclipse.smarthome.core.thing.Bridge
import org.eclipse.smarthome.core.thing.ThingRegistry
import org.eclipse.smarthome.core.thing.ThingTypeUID
import org.eclipse.smarthome.core.thing.ThingUID
import org.eclipse.smarthome.core.thing.binding.ThingHandler
import org.eclipse.smarthome.test.OSGiTest
import org.eclipse.smarthome.test.storage.VolatileStorageService
import org.junit.Before
import org.junit.Ignore
import org.junit.Test
import org.openhab.binding.max.MaxBinding
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler

/**
 * Tests for {@link MaxCubeBridgeHandler}.
 *
 * @author Marcel Verpaalen - Initial version
 * @since 2.0
 */
class MaxBridgeHandlerOSGiTest extends OSGiTest {

    final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID("max", "bridge")

    ThingRegistry thingRegistry
    VolatileStorageService volatileStorageService = new VolatileStorageService()

    @Before
    void setUp() {
        registerService(volatileStorageService)
        thingRegistry = getService(ThingRegistry, ThingRegistry)
        assertThat thingRegistry, is(notNullValue())
    }

    @Test
    void maxCubeBridgeHandlerRegisteredAndUnregister() {

        MaxCubeBridgeHandler maxBridgeHandler = getService(ThingHandler, MaxCubeBridgeHandler)
        assertThat maxBridgeHandler, is(nullValue())

        Configuration configuration = new Configuration().with {
            put(MaxBinding.PROPERTY_SERIAL_NUMBER, "KEQ0565026")
            put(MaxBinding.PROPERTY_IP_ADDRESS, "192.168.3.100")
            it
        }


        ThingUID cubeUid = new ThingUID(BRIDGE_THING_TYPE_UID, "testCube");

        Bridge maxBridge = thingRegistry.createThingOfType(
                BRIDGE_THING_TYPE_UID,
                cubeUid,
                null,
                null, configuration)

        assertThat maxBridge, is(notNullValue())
        thingRegistry.add(maxBridge)

        // wait for MaxCubeBridgeHandler to be registered
        waitForAssert({
            assertThat getService(ThingHandler, MaxCubeBridgeHandler), is(notNullValue())
        },  10000)

        thingRegistry.forceRemove(cubeUid)

        // wait for MaxCubeBridgeHandler to be unregistered
        waitForAssert({
            assertThat getService(ThingHandler, MaxCubeBridgeHandler), is(nullValue())
        }, 10000)
    }

}
