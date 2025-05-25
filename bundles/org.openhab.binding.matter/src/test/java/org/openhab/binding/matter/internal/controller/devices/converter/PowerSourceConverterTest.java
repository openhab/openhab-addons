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
import org.mockito.Mockito;
import org.openhab.binding.matter.internal.client.dto.cluster.gen.PowerSourceCluster;
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
 * Test class for PowerSourceConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class PowerSourceConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private PowerSourceCluster mockCluster;
    @NonNullByDefault({})
    private PowerSourceConverter converter;

    @Override
    @BeforeEach
    @SuppressWarnings("null")
    void setUp() {
        super.setUp();
        mockCluster.featureMap = Mockito.mock(PowerSourceCluster.FeatureMap.class);
        mockCluster.featureMap.battery = true;
        mockCluster.batPercentRemaining = 100; // 50%
        mockCluster.batChargeLevel = PowerSourceCluster.BatChargeLevelEnum.OK;
        converter = new PowerSourceConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    @SuppressWarnings("null")
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);
        assertEquals(2, channels.size());

        for (Channel channel : channels.keySet()) {
            String channelId = channel.getUID().getIdWithoutGroup();
            switch (channelId) {
                case "powersource-batpercentremaining":
                    assertEquals("Number:Dimensionless", channel.getAcceptedItemType());
                    break;
                case "powersource-batchargelevel":
                    assertEquals("Number", channel.getAcceptedItemType());
                    StateDescription stateDescription = channels.get(channel);
                    assertEquals(3, stateDescription.getOptions().size()); // Number of BatChargeLevelEnum values
                    break;
            }
        }
    }

    @Test
    void testOnEventWithBatteryPercent() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "batPercentRemaining";
        message.value = 100; // 50%
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("powersource-batpercentremaining"),
                eq(new QuantityType<Dimensionless>(50, Units.PERCENT)));
    }

    @Test
    void testOnEventWithChargeLevel() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "batChargeLevel";
        message.value = PowerSourceCluster.BatChargeLevelEnum.OK;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("powersource-batchargelevel"),
                eq(new DecimalType(PowerSourceCluster.BatChargeLevelEnum.OK.getValue())));
    }

    @Test
    void testOnEventWithInvalidBatteryPercent() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "batPercentRemaining";
        message.value = 201; // Invalid value
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("powersource-batpercentremaining"), eq(UnDefType.UNDEF));
    }

    @Test
    void testInitState() {
        mockCluster.batPercentRemaining = 100; // 50%
        mockCluster.batChargeLevel = PowerSourceCluster.BatChargeLevelEnum.OK;
        converter.initState();

        verify(mockHandler, times(1)).updateState(eq(1), eq("powersource-batpercentremaining"),
                eq(new QuantityType<Dimensionless>(50, Units.PERCENT)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("powersource-batchargelevel"),
                eq(new DecimalType(PowerSourceCluster.BatChargeLevelEnum.OK.getValue())));
    }
}
