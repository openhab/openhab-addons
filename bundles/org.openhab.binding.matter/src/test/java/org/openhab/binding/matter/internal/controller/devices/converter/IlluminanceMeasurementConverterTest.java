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

import javax.measure.quantity.Illuminance;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.IlluminanceMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * Test class for IlluminanceMeasurementConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class IlluminanceMeasurementConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private IlluminanceMeasurementCluster mockCluster;
    @NonNullByDefault({})
    private IlluminanceMeasurementConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        converter = new IlluminanceMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#illuminancemeasurement-measuredvalue", channel.getUID().toString());
        assertEquals("Number:Illuminance", channel.getAcceptedItemType());
    }

    @Test
    void testOnEventWithMeasuredValue() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "measuredValue";
        message.value = 100;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("illuminancemeasurement-measuredvalue"),
                eq(new QuantityType<Illuminance>(100, Units.LUX)));
    }

    @Test
    void testInitState() {
        mockCluster.measuredValue = 100;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("illuminancemeasurement-measuredvalue"),
                eq(new QuantityType<Illuminance>(100, Units.LUX)));
    }

    @Test
    void testInitStateWithNullValue() {
        mockCluster.measuredValue = null;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("illuminancemeasurement-measuredvalue"),
                eq(UnDefType.NULL));
    }
}
