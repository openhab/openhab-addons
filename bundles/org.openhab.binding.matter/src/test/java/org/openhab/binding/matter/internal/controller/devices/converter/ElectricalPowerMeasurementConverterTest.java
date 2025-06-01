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

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ElectricalPowerMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * Test class for ElectricalPowerMeasurementConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ElectricalPowerMeasurementConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private ElectricalPowerMeasurementCluster mockCluster;
    @NonNullByDefault({})
    private ElectricalPowerMeasurementConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.activeCurrent = BigInteger.valueOf(1000); // 1A
        mockCluster.voltage = BigInteger.valueOf(230000); // 230V
        converter = new ElectricalPowerMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(3, channels.size());

        for (Channel channel : channels.keySet()) {
            String channelId = channel.getUID().getIdWithoutGroup();
            switch (channelId) {
                case "electricalpowermeasurement-activepower":
                    assertEquals("Number:Power", channel.getAcceptedItemType());
                    break;
                case "electricalpowermeasurement-activecurrent":
                    assertEquals("Number:ElectricCurrent", channel.getAcceptedItemType());
                    break;
                case "electricalpowermeasurement-voltage":
                    assertEquals("Number:ElectricPotential", channel.getAcceptedItemType());
                    break;
            }
        }
    }

    @Test
    void testOnEventWithActivePower() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "activePower";
        message.value = 230000; // 230W
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("electricalpowermeasurement-activepower"),
                eq(new QuantityType<>(230.0, Units.WATT)));
    }

    @Test
    void testOnEventWithActiveCurrent() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "activeCurrent";
        message.value = 1000; // 1A
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("electricalpowermeasurement-activecurrent"),
                eq(new QuantityType<>(1.0, Units.AMPERE)));
    }

    @Test
    void testOnEventWithVoltage() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "voltage";
        message.value = 230000; // 230V
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("electricalpowermeasurement-voltage"),
                eq(new QuantityType<>(230.0, Units.VOLT)));
    }

    @Test
    void testOnEventWithInvalidValue() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "activePower";
        message.value = "invalid";
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("electricalpowermeasurement-activepower"),
                eq(UnDefType.UNDEF));
    }

    @Test
    void testInitState() {
        mockCluster.activePower = BigInteger.valueOf(230000); // 230W
        mockCluster.activeCurrent = BigInteger.valueOf(1000); // 1A
        mockCluster.voltage = BigInteger.valueOf(230000); // 230V
        converter.initState();

        verify(mockHandler, times(1)).updateState(eq(1), eq("electricalpowermeasurement-activepower"),
                eq(new QuantityType<>(230.0, Units.WATT)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("electricalpowermeasurement-activecurrent"),
                eq(new QuantityType<>(1.0, Units.AMPERE)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("electricalpowermeasurement-voltage"),
                eq(new QuantityType<>(230.0, Units.VOLT)));
    }

    @Test
    void testInitStateWithNullValues() {
        mockCluster.activePower = null;
        mockCluster.activeCurrent = null;
        mockCluster.voltage = null;
        converter.initState();

        verify(mockHandler, times(1)).updateState(eq(1), eq("electricalpowermeasurement-activepower"),
                eq(UnDefType.NULL));
    }
}
