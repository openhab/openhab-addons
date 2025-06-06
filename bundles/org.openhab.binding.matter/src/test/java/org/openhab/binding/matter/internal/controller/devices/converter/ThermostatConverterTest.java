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
import org.openhab.binding.matter.internal.client.dto.cluster.gen.ThermostatCluster;
import org.openhab.binding.matter.internal.client.dto.ws.AttributeChangedMessage;
import org.openhab.binding.matter.internal.client.dto.ws.Path;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelGroupUID;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.types.Command;
import org.openhab.core.types.StateDescription;

/**
 * Test class for ThermostatConverter
 * 
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
class ThermostatConverterTest extends BaseMatterConverterTest {

    @Mock
    @NonNullByDefault({})
    private ThermostatCluster mockCluster;
    @NonNullByDefault({})
    private ThermostatConverter converter;

    @Override
    @BeforeEach
    void setUp() {
        super.setUp();
        mockCluster.featureMap = new ThermostatCluster.FeatureMap(true, true, true, false, false, true, false, false,
                false);
        mockCluster.absMinHeatSetpointLimit = 500; // 5°C
        mockCluster.absMaxHeatSetpointLimit = 3000; // 30°C
        mockCluster.absMinCoolSetpointLimit = 1500; // 15°C
        mockCluster.absMaxCoolSetpointLimit = 3500; // 35°C
        converter = new ThermostatConverter(mockCluster, mockHandler, 1, "TestLabel");
    }

    @Test
    void testCreateChannels() {
        ChannelGroupUID channelGroupUID = new ChannelGroupUID("matter:node:test:12345:1");
        Map<Channel, @Nullable StateDescription> channels = converter.createChannels(channelGroupUID);

        assertEquals(6, channels.size());

        for (Channel channel : channels.keySet()) {
            String channelId = channel.getUID().getIdWithoutGroup();
            switch (channelId) {
                case "thermostat-systemmode":
                    assertEquals("Number", channel.getAcceptedItemType());
                    break;
                case "thermostat-localtemperature":
                case "thermostat-occupiedheating":
                case "thermostat-occupiedcooling":
                case "thermostat-unoccupiedheating":
                case "thermostat-unoccupiedcooling":
                    assertEquals("Number:Temperature", channel.getAcceptedItemType());
                    break;
            }
        }
    }

    @Test
    void testHandleCommandSystemMode() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#thermostat-systemmode");
        Command command = new DecimalType(1); // Heat mode
        converter.handleCommand(channelUID, command);
        verify(mockHandler, times(1)).writeAttribute(eq(1), eq(ThermostatCluster.CLUSTER_NAME), eq("systemMode"),
                eq("1"));
    }

    @Test
    void testHandleCommandHeatingSetpoint() {
        ChannelUID channelUID = new ChannelUID("matter:node:test:12345:1#thermostat-occupiedheating");
        Command command = new QuantityType<>(20, SIUnits.CELSIUS);
        converter.handleCommand(channelUID, command);
        verify(mockHandler, times(1)).writeAttribute(eq(1), eq(ThermostatCluster.CLUSTER_NAME),
                eq("occupiedHeatingSetpoint"), eq("2000"));
    }

    @Test
    void testOnEventWithLocalTemperature() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "localTemperature";
        message.value = 2000; // 20°C
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("thermostat-localtemperature"),
                eq(new QuantityType<>(20.0, SIUnits.CELSIUS)));
    }

    @Test
    void testOnEventWithSystemMode() {
        AttributeChangedMessage message = new AttributeChangedMessage();
        message.path = new Path();
        message.path.attributeName = "systemMode";
        message.value = ThermostatCluster.SystemModeEnum.HEAT;
        converter.onEvent(message);
        verify(mockHandler, times(1)).updateState(eq(1), eq("thermostat-systemmode"),
                eq(new DecimalType(ThermostatCluster.SystemModeEnum.HEAT.value)));
    }

    @Test
    void testInitState() {
        mockCluster.localTemperature = 2000; // 20°C
        mockCluster.systemMode = ThermostatCluster.SystemModeEnum.HEAT;
        mockCluster.occupiedHeatingSetpoint = 2200; // 22°C
        mockCluster.occupiedCoolingSetpoint = 2500; // 25°C

        converter.initState();

        verify(mockHandler, times(1)).updateState(eq(1), eq("thermostat-localtemperature"),
                eq(new QuantityType<>(20.0, SIUnits.CELSIUS)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("thermostat-systemmode"),
                eq(new DecimalType(mockCluster.systemMode.value)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("thermostat-occupiedheating"),
                eq(new QuantityType<>(22.0, SIUnits.CELSIUS)));
        verify(mockHandler, times(1)).updateState(eq(1), eq("thermostat-occupiedcooling"),
                eq(new QuantityType<>(25.0, SIUnits.CELSIUS)));
    }
}
