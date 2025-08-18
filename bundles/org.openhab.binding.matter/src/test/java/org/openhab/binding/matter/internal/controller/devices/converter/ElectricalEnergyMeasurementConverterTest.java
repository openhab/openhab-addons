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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ElectricalEnergyMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ElectricalEnergyMeasurementCluster.EnergyMeasurementStruct;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;

/**
 * Test class for ElectricalEnergyMeasurementConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ElectricalEnergyMeasurementConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private ElectricalEnergyMeasurementCluster mockCluster;
    @NonNullByDefault({})
    private ElectricalEnergyMeasurementConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.featureMap = new ElectricalEnergyMeasurementCluster.FeatureMap(true, true, true, true);
        converter = new ElectricalEnergyMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(4, channels.size());

        for (Channel channel : channels.keySet()) {
            assertEquals("Number:Energy", channel.getAcceptedItemType());
        }
    }

    @Test
    void testOnEventWithCumulativeEnergyImported() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "cumulativeEnergyImported";

        ElectricalEnergyMeasurementCluster.EnergyMeasurementStruct energyMeasurement = new EnergyMeasurementStruct(
                BigInteger.valueOf(1000), null, null, null, null);
        message.value = energyMeasurement;

        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1),
                eq("electricalenergymeasurement-cumulativeenergyimported-energy"),
                eq(new QuantityType<>(1.0, Units.WATT_HOUR)));
    }

    @Test
    void testInitState() {
        ElectricalEnergyMeasurementCluster.EnergyMeasurementStruct measurement = new EnergyMeasurementStruct(
                BigInteger.valueOf(1000), null, null, null, null);

        mockCluster.cumulativeEnergyImported = measurement;
        mockCluster.periodicEnergyImported = measurement;

        converter.initState();

        verify(mockHandler, times(1)).updateState(eq(1),
                eq("electricalenergymeasurement-cumulativeenergyimported-energy"),
                eq(new QuantityType<>(1.0, Units.WATT_HOUR)));
        verify(mockHandler, times(1)).updateState(eq(1),
                eq("electricalenergymeasurement-periodicenergyimported-energy"),
                eq(new QuantityType<>(1.0, Units.WATT_HOUR)));
    }
}
