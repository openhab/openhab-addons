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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.AirQualityCluster;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.AirQualityCluster.AirQualityEnum;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.types.StateDescription;

/**
 * Test class for {@link AirQualityConverter}
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class AirQualityConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private AirQualityCluster mockCluster;
    @NonNullByDefault({})
    private AirQualityConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.airQuality = AirQualityEnum.UNKNOWN;
        converter = new AirQualityConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(1, channels.size());
        Channel channel = channels.keySet().iterator().next();
        assertEquals("matter:node:test:12345:1#airquality-airquality", channel.getUID().toString());
        assertEquals("Number", channel.getAcceptedItemType());
    }

    @Test
    void testOnEventWithAirQuality() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = AirQualityCluster.ATTRIBUTE_AIR_QUALITY;
        message.value = AirQualityEnum.GOOD;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("airquality-airquality"), eq(new DecimalType(1)));
    }

    @Test
    void testInitState() {
        mockCluster.airQuality = AirQualityEnum.POOR;
        converter.initState();
        verify(mockHandler, times(1)).updateState(eq(1), eq("airquality-airquality"), eq(new DecimalType(4)));
    }
}
