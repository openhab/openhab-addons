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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ModeSelectCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ModeSelectCluster.ModeOptionStruct;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateDescription;

/**
 * Test class for ModeSelectConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ModeSelectConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private ModeSelectCluster mockCluster;
    @NonNullByDefault({})
    private ModeSelectConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        // Setup supported modes
        List<ModeSelectCluster.ModeOptionStruct> supportedModes = new ArrayList<>();
        supportedModes.add(new ModeOptionStruct("Mode 1", 1, null));
        supportedModes.add(new ModeOptionStruct("Mode 2", 2, null));
        mockCluster.supportedModes = supportedModes;
        mockCluster.description = "Test Mode Select";

        converter = new ModeSelectConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    @SuppressWarnings("null")
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(1, channels.size());

        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#modeselect-mode", channel.getUID().toString());
        assertEquals("Number", channel.getAcceptedItemType());

        StateDescription stateDescription = channels.get(channel);
        assertEquals(2, stateDescription.getOptions().size());
        assertEquals("1", stateDescription.getOptions().get(0).getValue());
        assertEquals("Mode 1", stateDescription.getOptions().get(0).getLabel());
        assertEquals("2", stateDescription.getOptions().get(1).getValue());
        assertEquals("Mode 2", stateDescription.getOptions().get(1).getLabel());
    }

    @Test
    void testHandleCommand() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#modeselect-mode");
        converter.handleCommand(channelUID, new DecimalType(1));
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(ModeSelectCluster.CLUSTER_NAME),
                eq(ModeSelectCluster.changeToMode(1)));
    }

    @Test
    void testOnEventWithCurrentMode() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "currentMode";
        message.value = 1;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("modeselect-mode"), eq(new DecimalType(1)));
    }

    @Test
    void testInitState() {
        mockCluster.currentMode = 1;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("modeselect-mode"), eq(new DecimalType(1)));
    }
}
