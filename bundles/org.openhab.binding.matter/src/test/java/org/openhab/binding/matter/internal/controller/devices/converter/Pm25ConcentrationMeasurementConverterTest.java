/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.Pm25ConcentrationMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.Pm25ConcentrationMeasurementCluster.FeatureMap;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.Pm25ConcentrationMeasurementCluster.LevelValueEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.Pm25ConcentrationMeasurementCluster.MeasurementUnitEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.Units;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;
import org.openhab.core.types.UnDefType;

/**
 * Test class for {@link Pm25ConcentrationMeasurementConverter}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class Pm25ConcentrationMeasurementConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private Pm25ConcentrationMeasurementCluster mockCluster;
    @NonNullByDefault({})
    private Pm25ConcentrationMeasurementConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.measuredValue = 35.0f;
        mockCluster.measurementUnit = MeasurementUnitEnum.UGM3;
        mockCluster.featureMap = new FeatureMap(true, false, false, false, false, false);
        converter = new Pm25ConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#pm25concentrationmeasurement-measuredvalue",
                channel.getUID().toString());
        assertEquals("Number:Dimensionless", channel.getAcceptedItemType());
    }

    @Test
    void testCreateChannelsWithAllFeatures() {
        mockCluster.featureMap = new FeatureMap(true, true, false, false, true, true);
        converter = new Pm25ConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(4, channels.size());

        Channel measuredValueChannel = channels.keySet().stream()
                .filter(c -> c.getUID().getId().equals("1#pm25concentrationmeasurement-measuredvalue")).findFirst()
                .orElse(null);
        assertNotNull(measuredValueChannel);

        Channel levelChannel = channels.keySet().stream()
                .filter(c -> c.getUID().getId().equals("1#pm25concentrationmeasurement-levelvalue")).findFirst()
                .orElse(null);
        assertNotNull(levelChannel);

        Channel peakChannel = channels.keySet().stream()
                .filter(c -> c.getUID().getId().equals("1#pm25concentrationmeasurement-peakmeasuredvalue")).findFirst()
                .orElse(null);
        assertNotNull(peakChannel);

        Channel averageChannel = channels.keySet().stream()
                .filter(c -> c.getUID().getId().equals("1#pm25concentrationmeasurement-averagemeasuredvalue"))
                .findFirst().orElse(null);
        assertNotNull(averageChannel);
    }

    @Test
    void testOnEventWithMeasuredValue() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = Pm25ConcentrationMeasurementCluster.ATTRIBUTE_MEASURED_VALUE;
        message.value = 45.0f;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("pm25concentrationmeasurement-measuredvalue"),
                eq(new QuantityType<>(45.0f, Units.MICROGRAM_PER_CUBICMETRE)));
    }

    @Test
    void testOnEventWithLevelValue() {
        mockCluster.featureMap = new FeatureMap(true, true, false, false, false, false);
        converter = new Pm25ConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = Pm25ConcentrationMeasurementCluster.ATTRIBUTE_LEVEL_VALUE;
        message.value = 2;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("pm25concentrationmeasurement-levelvalue"),
                eq(new DecimalType(2)));
    }

    @Test
    void testOnEventWithPeakMeasuredValue() {
        mockCluster.featureMap = new FeatureMap(true, false, false, false, true, false);
        converter = new Pm25ConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = Pm25ConcentrationMeasurementCluster.ATTRIBUTE_PEAK_MEASURED_VALUE;
        message.value = 65.0f;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("pm25concentrationmeasurement-peakmeasuredvalue"),
                eq(new QuantityType<>(65.0f, Units.MICROGRAM_PER_CUBICMETRE)));
    }

    @Test
    void testOnEventWithAverageMeasuredValue() {
        mockCluster.featureMap = new FeatureMap(true, false, false, false, false, true);
        converter = new Pm25ConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = Pm25ConcentrationMeasurementCluster.ATTRIBUTE_AVERAGE_MEASURED_VALUE;
        message.value = 38.0f;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("pm25concentrationmeasurement-averagemeasuredvalue"),
                eq(new QuantityType<>(38.0f, Units.MICROGRAM_PER_CUBICMETRE)));
    }

    @Test
    void testInitState() {
        mockCluster.measuredValue = 40.0f;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("pm25concentrationmeasurement-measuredvalue"),
                eq(new QuantityType<>(40.0f, Units.MICROGRAM_PER_CUBICMETRE)));
    }

    @Test
    void testInitStateWithAllAttributes() {
        mockCluster.measuredValue = 40.0f;
        mockCluster.levelValue = LevelValueEnum.HIGH;
        mockCluster.peakMeasuredValue = 70.0f;
        mockCluster.averageMeasuredValue = 35.0f;
        mockCluster.featureMap = new FeatureMap(true, true, false, false, true, true);
        converter = new Pm25ConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("pm25concentrationmeasurement-measuredvalue"),
                eq(new QuantityType<>(40.0f, Units.MICROGRAM_PER_CUBICMETRE)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("pm25concentrationmeasurement-levelvalue"),
                eq(new DecimalType(3)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("pm25concentrationmeasurement-peakmeasuredvalue"),
                eq(new QuantityType<>(70.0f, Units.MICROGRAM_PER_CUBICMETRE)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("pm25concentrationmeasurement-averagemeasuredvalue"),
                eq(new QuantityType<>(35.0f, Units.MICROGRAM_PER_CUBICMETRE)));
    }

    @Test
    void testInitStateWithNullValue() {
        mockCluster.measuredValue = null;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("pm25concentrationmeasurement-measuredvalue"),
                eq(UnDefType.NULL));
    }
}
