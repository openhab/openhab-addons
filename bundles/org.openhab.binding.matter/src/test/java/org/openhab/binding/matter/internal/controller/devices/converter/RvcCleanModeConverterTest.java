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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.RvcCleanModeCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateDescription;

/**
 * Tests for {@link RvcCleanModeConverter}
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class RvcCleanModeConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private RvcCleanModeCluster mockCluster;

    @NonNullByDefault({})
    private RvcCleanModeConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        List<RvcCleanModeCluster.ModeOptionStruct> modes = new ArrayList<>();
        modes.add(mockCluster.new ModeOptionStruct("Vacuum", 2, null));
        modes.add(mockCluster.new ModeOptionStruct("Mop", 5, null));
        modes.add(mockCluster.new ModeOptionStruct("Vacuum & Mop", 7, null));
        mockCluster.supportedModes = modes;
        mockCluster.currentMode = 7;
        converter = new RvcCleanModeConverter(mockCluster, mockHandler, 1, "Vacuum");
    }

    @Test
    @SuppressWarnings("null")
    void testCreateChannels() {
        ChannelGroupUID groupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(groupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#rvccleanmode-mode", channel.getUID().toString());
    }

    @Test
    void testHandleCommand() {
        ChannelUID uid = new ChannelUID("matter:node:test:12345:1#rvccleanmode-mode");
        converter.handleCommand(uid, new DecimalType(5));
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(RvcCleanModeCluster.CLUSTER_NAME),
                eq(RvcCleanModeCluster.changeToMode(5)));
    }

    @Test
    void testOnEvent() {
        AttributeChangedMessage msg = new AttributeChangedMessage();
        msg.path = new Path();
        msg.path.attributeName = "currentMode";
        msg.value = 2;
        converter.onEvent(msg);
        verify(mockHandler, times(1)).updateState(eq(1), eq("rvccleanmode-mode"), eq(new DecimalType(2)));
    }

    @Test
    void testInitState() {
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("rvccleanmode-mode"), eq(new DecimalType(7)));
    }
}
