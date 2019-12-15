/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.onewire.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.handler.BasicThingHandler;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.test.AbstractThingHandlerTest;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.openhab.binding.onewire.internal.OwBindingConstants.CONFIG_ID;
import static org.openhab.binding.onewire.internal.OwBindingConstants.THING_TYPE_BASIC;

/**
 * Tests cases for {@link BasicThingHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class BasicThingHandlerTest extends AbstractThingHandlerTest {
    private static final String TEST_ID = "00.000000000000";

    @Before
    public void setup() throws OwException {
        MockitoAnnotations.initMocks(this);

        initializeBridge();

        final Bridge bridge = this.bridge;
        if (bridge == null) {
            Assert.fail("bridge is null");
            return;
        }

        thingConfiguration.put(CONFIG_ID, TEST_ID);

        thing = ThingBuilder.create(THING_TYPE_BASIC, "testthing").withLabel("Test thing")
                .withConfiguration(new Configuration(thingConfiguration)).withProperties(thingProperties)
                .withBridge(bridge.getUID()).build();

        final Thing thing = this.thing;
        if (thing == null) {
            Assert.fail("thing is null");
            return;
        }

        thingHandler = new BasicThingHandler(thing, stateProvider) {
            @Override
            protected @Nullable Bridge getBridge() {
                return bridge;
            }
        };

        initializeHandlerMocks();
    }

    @Test
    public void testInitializationEndsWithUnknown() throws OwException {
        final ThingHandler thingHandler = this.thingHandler;
        if (thingHandler == null) {
            Assert.fail("thingHandler is null");
            return;
        }

        Mockito.doAnswer(answer -> {
            return OwSensorType.DS2401;
        }).when(secondBridgeHandler).getType(any());

        thingHandler.initialize();

        waitForAssert(() -> assertEquals(ThingStatus.UNKNOWN, thingHandler.getThing().getStatusInfo().getStatus()));
    }

    @Test
    public void testRefreshAnalog() throws OwException {
        final OwBaseThingHandler thingHandler = this.thingHandler;
        final InOrder inOrder = this.inOrder;
        if (thingHandler == null || inOrder == null) {
            Assert.fail("prerequisite is null");
            return;
        }

        Mockito.doAnswer(answer -> {
            return OwSensorType.DS18B20;
        }).when(secondBridgeHandler).getType(any());

        thingHandler.initialize();
        waitForAssert(() -> assertEquals(ThingStatus.UNKNOWN, thingHandler.getThing().getStatusInfo().getStatus()));

        thingHandler.refresh(bridgeHandler, System.currentTimeMillis());

        inOrder.verify(bridgeHandler, times(1)).checkPresence(new SensorId(TEST_ID));
        inOrder.verify(bridgeHandler, times(1)).readDecimalType(eq(new SensorId(TEST_ID)), any());

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void testRefreshDigital() throws OwException {
        final OwBaseThingHandler thingHandler = this.thingHandler;
        final InOrder inOrder = this.inOrder;
        if (thingHandler == null || inOrder == null) {
            Assert.fail("prerequisite is null");
            return;
        }

        Mockito.doAnswer(answer -> {
            return OwSensorType.DS2408;
        }).when(secondBridgeHandler).getType(any());

        thingHandler.initialize();
        waitForAssert(() -> assertEquals(ThingStatus.UNKNOWN, thingHandler.getThing().getStatusInfo().getStatus()));

        thingHandler.refresh(bridgeHandler, System.currentTimeMillis());

        inOrder.verify(bridgeHandler, times(1)).checkPresence(new SensorId(TEST_ID));
        inOrder.verify(bridgeHandler, times(2)).readBitSet(eq(new SensorId(TEST_ID)), any());

        inOrder.verifyNoMoreInteractions();
    }
}
