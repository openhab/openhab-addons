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

import javax.measure.quantity.Dimensionless;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.RelativeHumidityMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;

/**
 * Test class for RelativeHumidityMeasurementConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class RelativeHumidityMeasurementConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private RelativeHumidityMeasurementCluster mockCluster;
    @NonNullByDefault({})
    private RelativeHumidityMeasurementConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.measuredValue = 5000; // 50.00%
        converter = new RelativeHumidityMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#relativehumiditymeasurement-measuredvalue", channel.getUID().toString());
        assertEquals("Number:Dimensionless", channel.getAcceptedItemType());
    }

    @Test
    void testOnEventWithMeasuredValue() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "measuredValue";
        message.value = 5000; // 50.00%
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("relativehumiditymeasurement-measuredvalue"),
                eq(new QuantityType<Dimensionless>(50.00, Units.PERCENT)));
    }

    @Test
    void testInitState() {
        mockCluster.measuredValue = 5000; // 50.00%
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("relativehumiditymeasurement-measuredvalue"),
                eq(new QuantityType<Dimensionless>(50.00, Units.PERCENT)));
    }
}
