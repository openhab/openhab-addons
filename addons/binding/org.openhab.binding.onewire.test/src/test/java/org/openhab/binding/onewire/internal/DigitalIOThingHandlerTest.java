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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.builder.ChannelBuilder;
import org.eclipse.smarthome.core.thing.binding.builder.ThingBuilder;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.onewire.internal.OwException;
import org.openhab.binding.onewire.internal.SensorId;
import org.openhab.binding.onewire.internal.device.OwSensorType;
import org.openhab.binding.onewire.internal.handler.DigitalIOThingHandler;
import org.openhab.binding.onewire.test.AbstractThingHandlerTest;

/**
 * Tests cases for {@link DigitalIOThingeHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
public class DigitalIOThingHandlerTest extends AbstractThingHandlerTest {
    private static final String TEST_ID = "00.000000000000";
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_DIGITALIO, "testthing");
    private static final ChannelUID CHANNEL_UID_PRESENT = new ChannelUID(THING_UID, CHANNEL_PRESENT);
    private static final ChannelUID CHANNEL_UID_DIO = new ChannelUID(THING_UID, CHANNEL_DIGITAL + "0");

    @Before
    public void setup() throws OwException {
        MockitoAnnotations.initMocks(this);

        initializeBridge();

        thingConfiguration.put(CONFIG_ID, TEST_ID);

        channels.add(ChannelBuilder.create(CHANNEL_UID_PRESENT, "Switch").build());
        channels.add(ChannelBuilder.create(CHANNEL_UID_DIO, "Switch").build());

        thing = ThingBuilder.create(THING_TYPE_DIGITALIO, "testthing").withLabel("Test thing").withChannels(channels)
                .withConfiguration(new Configuration(thingConfiguration)).withProperties(thingProperties)
                .withBridge(bridge.getUID()).build();

        thingHandler = new DigitalIOThingHandler(thing, stateProvider) {
            @Override
            protected Bridge getBridge() {
                return bridge;
            }
        };

        initializeHandlerMocks();

        Mockito.doAnswer(answer -> {
            return OwSensorType.DS2408;
        }).when(secondBridgeHandler).getType(any());
    }

    @Test
    public void testInitializationEndsWithUnknown() {
        thingHandler.initialize();
        waitForAssert(() -> assertEquals(ThingStatus.UNKNOWN, thing.getStatusInfo().getStatus()));
    }

    @Test
    public void testRefresh() throws OwException {
        thingHandler.initialize();
        thingHandler.refresh(bridgeHandler, System.currentTimeMillis());

        inOrder.verify(bridgeHandler, times(1)).checkPresence(new SensorId(TEST_ID));
        inOrder.verify(bridgeHandler, times(2)).readBitSet(eq(new SensorId(TEST_ID)), any());

        inOrder.verifyNoMoreInteractions();
    }
}
