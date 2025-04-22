/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.matter.internal.controller.devices.converter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openhab.binding.matter.internal.MatterChannelTypeProvider;
import org.openhab.binding.matter.internal.MatterStateDescriptionOptionProvider;
import org.openhab.binding.matter.internal.bridge.MatterBridgeClient;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.BooleanStateCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.types.StateDescription;

/**
 * Test class for BooleanStateConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class BooleanStateConverterTest {

    @Mock
    @NonNullByDefault({})
    private BooleanStateCluster mockCluster;
    @Mock
    @NonNullByDefault({})
    private MatterBridgeClient mockBridgeClient;
    @Mock
    @NonNullByDefault({})
    private BaseThingHandlerFactory mockThingHandlerFactory;
    @Mock
    @NonNullByDefault({})
    private MatterStateDescriptionOptionProvider mockStateDescriptionProvider;
    @Mock
    @NonNullByDefault({})
    private MatterChannelTypeProvider mockChannelTypeProvider;
    @NonNullByDefault({})
    private TestMatterBaseThingHandler mockHandler;
    @NonNullByDefault({})
    private BooleanStateConverter converter;

    @SuppressWarnings("null")
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockHandler = Mockito.spy(new TestMatterBaseThingHandler(mockBridgeClient, mockThingHandlerFactory,
                mockStateDescriptionProvider, mockChannelTypeProvider));
        converter = new BooleanStateConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID thingUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(thingUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#booleanstate-statevalue", channel.getUID().toString());
        assertEquals("Switch", channel.getAcceptedItemType());
    }

    @Test
    void testOnEventWithBooleanValue() throws Exception {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "stateValue";
        message.value = true;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("booleanstate-statevalue"), eq(OnOffType.ON));
    }

    @Test
    void testOnEventWithBooleanValueFalse() throws Exception {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "stateValue";
        message.value = false;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("booleanstate-statevalue"), eq(OnOffType.OFF));
    }

    @Test
    void testInitState() throws Exception {
        mockCluster.stateValue = true;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("booleanstate-statevalue"), eq(OnOffType.ON));
    }
}
