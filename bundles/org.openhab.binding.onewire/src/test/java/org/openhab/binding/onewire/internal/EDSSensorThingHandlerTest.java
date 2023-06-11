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
package org.openhab.binding.onewire.internal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.times;
import static org.openhab.binding.onewire.internal.OwBindingConstants.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.openhab.binding.onewire.internal.handler.EDSSensorThingHandler;
import org.openhab.binding.onewire.internal.handler.OwBaseThingHandler;
import org.openhab.binding.onewire.test.AbstractThingHandlerTest;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.builder.ChannelBuilder;
import org.openhab.core.thing.binding.builder.ThingBuilder;

/**
 * Tests cases for {@link EDSSensorThingHandler}.
 *
 * @author Jan N. Klug - Initial contribution
 */
@NonNullByDefault
public class EDSSensorThingHandlerTest extends AbstractThingHandlerTest {
    private static final String TEST_ID = "00.000000000000";
    private static final ThingUID THING_UID = new ThingUID(THING_TYPE_EDS_ENV, "testthing");
    private static final ChannelUID CHANNEL_UID_TEMPERATURE = new ChannelUID(THING_UID, CHANNEL_TEMPERATURE);
    private static final ChannelUID CHANNEL_UID_HUMIDITY = new ChannelUID(THING_UID, CHANNEL_HUMIDITY);
    private static final ChannelUID CHANNEL_UID_ABSOLUTE_HUMIDITY = new ChannelUID(THING_UID,
            CHANNEL_ABSOLUTE_HUMIDITY);
    private static final ChannelUID CHANNEL_UID_DEWPOINT = new ChannelUID(THING_UID, CHANNEL_DEWPOINT);

    @BeforeEach
    public void setup() throws OwException {
        initializeBridge();

        final Bridge bridge = this.bridge;
        if (bridge == null) {
            fail("bridge is null");
            return;
        }

        thingConfiguration.put(CONFIG_ID, TEST_ID);

        channels.add(ChannelBuilder.create(CHANNEL_UID_TEMPERATURE, "Number:Temperature").build());
        channels.add(ChannelBuilder.create(CHANNEL_UID_HUMIDITY, "Number:Dimensionless").build());
        channels.add(ChannelBuilder.create(CHANNEL_UID_ABSOLUTE_HUMIDITY, "Number:Density").build());
        channels.add(ChannelBuilder.create(CHANNEL_UID_DEWPOINT, "Number:Temperature").build());

        thing = ThingBuilder.create(THING_TYPE_EDS_ENV, "testthing").withLabel("Test thing").withChannels(channels)
                .withConfiguration(new Configuration(thingConfiguration)).withProperties(thingProperties)
                .withBridge(bridge.getUID()).build();

        final Thing thing = this.thing;
        if (thing == null) {
            fail("thing is null");
            return;
        }

        thingHandler = new EDSSensorThingHandler(thing, stateProvider) {
            @Override
            protected @Nullable Bridge getBridge() {
                return bridge;
            }
        };

        initializeHandlerMocks();

        Mockito.doAnswer(answer -> {
            return new OwPageBuffer("EDS0065 ".getBytes());
        }).when(secondBridgeHandler).readPages(any());
    }

    @Test
    public void testInitializationEndsWithUnknown() {
        final ThingHandler thingHandler = this.thingHandler;
        if (thingHandler == null) {
            fail("thingHandler is null");
            return;
        }

        thingHandler.initialize();

        waitForAssert(() -> assertEquals(ThingStatus.UNKNOWN, thingHandler.getThing().getStatusInfo().getStatus()));
    }

    @Test
    public void testRefresh() throws OwException {
        final OwBaseThingHandler thingHandler = this.thingHandler;
        final InOrder inOrder = this.inOrder;
        if (thingHandler == null || inOrder == null) {
            fail("prerequisite is null");
            return;
        }

        thingHandler.initialize();

        // needed to determine initialization is finished
        waitForAssert(() -> assertEquals(ThingStatus.UNKNOWN, thingHandler.getThing().getStatusInfo().getStatus()));

        thingHandler.refresh(bridgeHandler, System.currentTimeMillis());

        inOrder.verify(bridgeHandler, times(1)).checkPresence(new SensorId(TEST_ID));
        inOrder.verify(bridgeHandler, times(2)).readDecimalType(eq(new SensorId(TEST_ID)), any());

        inOrder.verifyNoMoreInteractions();
    }
}
