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
package org.openhab.binding.onewire.internal;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.handler.BasicThingHandler;
import org.openhab.binding.onewire.test.AbstractThingHandlerTest;

/**
 * Tests cases for {@link BasicThingeHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class BasicThingHandlerTest extends AbstractThingHandlerTest {
    private static final String TEST_ID = "00.000000000000";

    @Before
    public void setup() throws OwException {
        MockitoAnnotations.initMocks(this);

        initializeBridge();

        thingConfiguration.put(CONFIG_ID, TEST_ID);

        thing = ThingBuilder.create(THING_TYPE_BASIC, "testthing").withLabel("Test thing")
                .withConfiguration(new Configuration(thingConfiguration)).withProperties(thingProperties)
                .withBridge(bridge.getUID()).build();

        thingHandler = new BasicThingHandler(thing, stateProvider) {
            @Override
            protected Bridge getBridge() {
                return bridge;
            }
        };

        initializeHandlerMocks();

    }

    @Test
    public void testInitializationEndsWithUnknown() throws OwException {
        Mockito.doAnswer(answer -> {
            return OwSensorType.DS2401;
        }).when(secondBridgeHandler).getType(any());

        thingHandler.initialize();

        waitForAssert(() -> assertEquals(ThingStatus.UNKNOWN, thingHandler.getThing().getStatusInfo().getStatus()));
    }

    @Test
    public void testRefreshAnalog() throws OwException {
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
