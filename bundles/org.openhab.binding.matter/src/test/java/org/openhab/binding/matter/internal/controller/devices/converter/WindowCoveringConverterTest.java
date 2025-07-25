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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.WindowCoveringCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.PercentType;
import org.openhab.core.library.types.StopMoveType;
import org.openhab.core.library.types.UpDownType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateDescription;

/**
 * Test class for WindowCoveringConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class WindowCoveringConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private WindowCoveringCluster mockCluster;
    @NonNullByDefault({})
    private WindowCoveringConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        converter = new WindowCoveringConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#windowcovering-lift", channel.getUID().toString());
        assertEquals("Rollershutter", channel.getAcceptedItemType());
    }

    @Test
    void testHandleCommandUp() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#windowcovering-lift");
        converter.handleCommand(channelUID, UpDownType.UP);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(WindowCoveringCluster.CLUSTER_NAME),
                eq(WindowCoveringCluster.upOrOpen()));
    }

    @Test
    void testHandleCommandDown() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#windowcovering-lift");
        converter.handleCommand(channelUID, UpDownType.DOWN);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(WindowCoveringCluster.CLUSTER_NAME),
                eq(WindowCoveringCluster.downOrClose()));
    }

    @Test
    void testHandleCommandStop() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#windowcovering-lift");
        converter.handleCommand(channelUID, StopMoveType.STOP);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(WindowCoveringCluster.CLUSTER_NAME),
                eq(WindowCoveringCluster.stopMotion()));
    }

    @Test
    void testHandleCommandPercent() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#windowcovering-lift");
        converter.handleCommand(channelUID, new PercentType(50));
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(WindowCoveringCluster.CLUSTER_NAME),
                eq(WindowCoveringCluster.goToLiftPercentage(50)));
    }

    @Test
    void testOnEventWithCurrentPositionLiftPercentage() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "currentPositionLiftPercentage";
        message.value = 75;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("windowcovering-lift"), eq(new PercentType(75)));
    }

    @Test
    void testOnEventWithCurrentPositionLiftPercent100ths() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "currentPositionLiftPercent100ths";
        message.value = 7500; // 75.00%
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("windowcovering-lift"), eq(new PercentType(75)));
    }

    @Test
    void testInitStateWithPercentage() {
        mockCluster.currentPositionLiftPercentage = 25;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("windowcovering-lift"), eq(new PercentType(25)));
    }

    @Test
    void testInitStateWithPercent100ths() {
        mockCluster.currentPositionLiftPercent100ths = 2500; // 25.00%
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("windowcovering-lift"), eq(new PercentType(25)));
    }
}
