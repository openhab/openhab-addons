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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.RvcOperationalStateCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.StateDescription;

/**
 * Tests for {@link RvcOperationalStateConverter}
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class RvcOperationalStateConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private RvcOperationalStateCluster mockCluster;
    @NonNullByDefault({})
    private RvcOperationalStateConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.operationalState = RvcOperationalStateCluster.OperationalStateEnum.ERROR;
        converter = new RvcOperationalStateConverter(mockCluster, mockHandler, 1, "Vacuum");
    }

    @Test
    @SuppressWarnings("null")
    void testCreateChannels() {
        ChannelGroupUID groupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(groupUID);
        assertEquals(2, channels.size());
        assertEquals(2, channels.keySet().size());
    }

    @Test
    void testHandleGoHomeCommand() {
        ChannelUID goHomeUID = new ChannelUID("matter:node:test:12345:1#rvcoperationalstate-gohome");
        converter.handleCommand(goHomeUID, OnOffType.ON);
        verify(mockHandler, times(1)).sendClusterCommand(eq(1), eq(RvcOperationalStateCluster.CLUSTER_NAME),
                eq(RvcOperationalStateCluster.goHome()));
    }

    @Test
    void testOnEvent() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "operationalState";
        message.value = 1; // RUNNING
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("rvcoperationalstate-state"), eq(new DecimalType(1)));
    }

    @Test
    void testInitState() {
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("rvcoperationalstate-state"),
                eq(new DecimalType(RvcOperationalStateCluster.OperationalStateEnum.ERROR.value)));
    }
}
