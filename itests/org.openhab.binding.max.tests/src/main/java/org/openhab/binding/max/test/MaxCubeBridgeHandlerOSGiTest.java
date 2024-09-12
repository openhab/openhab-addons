/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.max.test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.openhab.binding.max.internal.MaxBindingConstants.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.max.internal.MaxBindingConstants;
import org.openhab.binding.max.internal.handler.MaxCubeBridgeHandler;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.test.java.JavaOSGiTest;
import org.openhab.core.test.storage.VolatileStorageService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingRegistry;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.BridgeBuilder;

/**
 * Tests for {@link MaxCubeBridgeHandler}.
 *
 * @author Marcel Verpaalen - Initial contribution
 * @author Wouter Born - Migrate Groovy to Java tests
 */
public class MaxCubeBridgeHandlerOSGiTest extends JavaOSGiTest {

    private static final ThingTypeUID BRIDGE_THING_TYPE_UID = new ThingTypeUID(BINDING_ID, BRIDGE_MAXCUBE);

    private ThingRegistry thingRegistry;
    private VolatileStorageService volatileStorageService = new VolatileStorageService();

    private Bridge maxBridge;

    @BeforeEach
    public void setUp() {
        registerService(volatileStorageService);

        thingRegistry = getService(ThingRegistry.class);
        assertThat(thingRegistry, is(notNullValue()));
    }

    @AfterEach
    public void tearDown() {
        if (maxBridge != null) {
            thingRegistry.remove(maxBridge.getUID());

            // TODO: Due to synchronization issues the handler is currently not unset. To be fixed by PR #1789.
            // waitForAssert(() -> assertThat(maxBridge.getHandler(), is(nullValue())));
        }

        unregisterService(volatileStorageService);
    }

    @Test
    public void maxCubeBridgeHandlerIsCreated() {
        MaxCubeBridgeHandler maxBridgeHandler = getService(ThingHandler.class, MaxCubeBridgeHandler.class);
        assertThat(maxBridgeHandler, is(nullValue()));

        Configuration configuration = new Configuration();
        configuration.put(Thing.PROPERTY_SERIAL_NUMBER, "KEQ0565026");
        configuration.put(MaxBindingConstants.PROPERTY_IP_ADDRESS, "192.168.3.100");

        ThingUID cubeUid = new ThingUID(BRIDGE_THING_TYPE_UID, "testCube");

        maxBridge = BridgeBuilder.create(BRIDGE_THING_TYPE_UID, cubeUid).withConfiguration(configuration).build();
        thingRegistry.add(maxBridge);

        // wait for MaxCubeBridgeHandler to be registered
        waitForAssert(() -> assertThat(maxBridge.getHandler(), is(notNullValue())));
    }
}
