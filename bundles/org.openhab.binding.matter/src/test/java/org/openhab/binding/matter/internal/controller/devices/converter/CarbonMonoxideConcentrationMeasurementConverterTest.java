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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.CarbonMonoxideConcentrationMeasurementCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.CarbonMonoxideConcentrationMeasurementCluster.FeatureMap;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.CarbonMonoxideConcentrationMeasurementCluster.LevelValueEnum;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.CarbonMonoxideConcentrationMeasurementCluster.MeasurementUnitEnum;
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
 * Test class for {@link CarbonMonoxideConcentrationMeasurementConverter}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class CarbonMonoxideConcentrationMeasurementConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private CarbonMonoxideConcentrationMeasurementCluster mockCluster;
    @NonNullByDefault({})
    private CarbonMonoxideConcentrationMeasurementConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.measuredValue = 0.0f;
        mockCluster.measurementUnit = MeasurementUnitEnum.PPM;
        mockCluster.featureMap = new FeatureMap(true, false, false, false, false, false);
        converter = new CarbonMonoxideConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#carbonmonoxideconcentrationmeasurement-measuredvalue",
                channel.getUID().toString());
        assertEquals("Number:Dimensionless", channel.getAcceptedItemType());
    }

    @Test
    void testCreateChannelsWithAllFeatures() {
        mockCluster.featureMap = new FeatureMap(true, true, false, false, true, true);
        converter = new CarbonMonoxideConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(4, channels.size());

        Channel measuredValueChannel = channels.keySet().stream()
                .filter(c -> c.getUID().getId().equals("1#carbonmonoxideconcentrationmeasurement-measuredvalue"))
                .findFirst().orElse(null);
        assertNotNull(measuredValueChannel);

        Channel levelChannel = channels.keySet().stream()
                .filter(c -> c.getUID().getId().equals("1#carbonmonoxideconcentrationmeasurement-levelvalue"))
                .findFirst().orElse(null);
        assertNotNull(levelChannel);

        Channel peakChannel = channels.keySet().stream()
                .filter(c -> c.getUID().getId().equals("1#carbonmonoxideconcentrationmeasurement-peakmeasuredvalue"))
                .findFirst().orElse(null);
        assertNotNull(peakChannel);

        Channel averageChannel = channels.keySet().stream()
                .filter(c -> c.getUID().getId().equals("1#carbonmonoxideconcentrationmeasurement-averagemeasuredvalue"))
                .findFirst().orElse(null);
        assertNotNull(averageChannel);
    }

    @Test
    void testOnEventWithMeasuredValue() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = CarbonMonoxideConcentrationMeasurementCluster.ATTRIBUTE_MEASURED_VALUE;
        message.value = 50.0f;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("carbonmonoxideconcentrationmeasurement-measuredvalue"),
                eq(new QuantityType<>(50.0f, Units.PARTS_PER_MILLION)));
    }

    @Test
    void testOnEventWithLevelValue() {
        mockCluster.featureMap = new FeatureMap(true, true, false, false, false, false);
        converter = new CarbonMonoxideConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = CarbonMonoxideConcentrationMeasurementCluster.ATTRIBUTE_LEVEL_VALUE;
        message.value = 3;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("carbonmonoxideconcentrationmeasurement-levelvalue"),
                eq(new DecimalType(3)));
    }

    @Test
    void testOnEventWithPeakMeasuredValue() {
        mockCluster.featureMap = new FeatureMap(true, false, false, false, true, false);
        converter = new CarbonMonoxideConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = CarbonMonoxideConcentrationMeasurementCluster.ATTRIBUTE_PEAK_MEASURED_VALUE;
        message.value = 100.0f;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("carbonmonoxideconcentrationmeasurement-peakmeasuredvalue"),
                eq(new QuantityType<>(100.0f, Units.PARTS_PER_MILLION)));
    }

    @Test
    void testOnEventWithAverageMeasuredValue() {
        mockCluster.featureMap = new FeatureMap(true, false, false, false, false, true);
        converter = new CarbonMonoxideConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = CarbonMonoxideConcentrationMeasurementCluster.ATTRIBUTE_AVERAGE_MEASURED_VALUE;
        message.value = 30.0f;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1),
                eq("carbonmonoxideconcentrationmeasurement-averagemeasuredvalue"),
                eq(new QuantityType<>(30.0f, Units.PARTS_PER_MILLION)));
    }

    @Test
    void testInitState() {
        mockCluster.measuredValue = 10.0f;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("carbonmonoxideconcentrationmeasurement-measuredvalue"),
                eq(new QuantityType<>(10.0f, Units.PARTS_PER_MILLION)));
    }

    @Test
    void testInitStateWithAllAttributes() {
        mockCluster.measuredValue = 10.0f;
        mockCluster.levelValue = LevelValueEnum.CRITICAL;
        mockCluster.peakMeasuredValue = 150.0f;
        mockCluster.averageMeasuredValue = 25.0f;
        mockCluster.featureMap = new FeatureMap(true, true, false, false, true, true);
        converter = new CarbonMonoxideConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("carbonmonoxideconcentrationmeasurement-measuredvalue"),
                eq(new QuantityType<>(10.0f, Units.PARTS_PER_MILLION)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("carbonmonoxideconcentrationmeasurement-levelvalue"),
                eq(new DecimalType(4)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("carbonmonoxideconcentrationmeasurement-peakmeasuredvalue"),
                eq(new QuantityType<>(150.0f, Units.PARTS_PER_MILLION)));
        verify(mockHandler, times(1)).updateState(eq(1),
                eq("carbonmonoxideconcentrationmeasurement-averagemeasuredvalue"),
                eq(new QuantityType<>(25.0f, Units.PARTS_PER_MILLION)));
    }

    @Test
    void testInitStateWithNullValue() {
        mockCluster.measuredValue = null;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("carbonmonoxideconcentrationmeasurement-measuredvalue"),
                eq(UnDefType.NULL));
    }

    @Test
    void testMeasuredValueWithMicrogramUnit() {
        mockCluster.measurementUnit = MeasurementUnitEnum.UGM3;
        converter = new CarbonMonoxideConcentrationMeasurementConverter(mockCluster, mockHandler, 1, "TestLabel");

        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = CarbonMonoxideConcentrationMeasurementCluster.ATTRIBUTE_MEASURED_VALUE;
        message.value = 75.0f;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("carbonmonoxideconcentrationmeasurement-measuredvalue"),
                eq(new QuantityType<>(75.0f, Units.MICROGRAM_PER_CUBICMETRE)));
    }
}
