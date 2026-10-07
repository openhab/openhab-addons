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
package org.openhab.binding.shelly.internal.handler.component;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSWALLDISPLAY;

import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor.ShellySensorBat;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusPower;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusPower.Shelly2DeviceStatusBattery;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusPower.Shelly2DeviceStatusCharger;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null", "unchecked" })
public class ShellyComponentBatteryTest {
    private static final Set<String> EXT_CHANNEL_IDS = Set.of(
            CHANNEL_GROUP_SENSOR + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_SENSOR_BAT_LEVEL,
            CHANNEL_GROUP_SENSOR + ChannelUID.CHANNEL_GROUP_SEPARATOR + CHANNEL_SENSOR_BAT_LOW);

    @BeforeAll
    static void initChannelDefinitions() {
        ShellyTranslationProvider messages = mock(ShellyTranslationProvider.class);
        when(messages.get(anyString(), any(Object[].class))).thenAnswer(i -> i.getArgument(0));
        new ShellyChannelDefinitions(messages);
    }

    private ShellyThingInterface newHandler() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(new ThingUID(THING_TYPE_SHELLYPLUSWALLDISPLAY, "test"));
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getThing()).thenReturn(thing);
        when(handler.getThingConfig()).thenReturn(new ShellyThingConfiguration());
        when(handler.updateChannel(anyString(), anyString(), any())).thenReturn(true);
        return handler;
    }

    @Test
    void extBatteryFillsAttachedSensorBattery() {
        ShellyStatusSensor sdata = new ShellyStatusSensor();
        Shelly2DeviceStatusPower power = new Shelly2DeviceStatusPower();
        power.battery = new Shelly2DeviceStatusBattery();
        power.battery.percent = 74.0;
        power.battery.volt = 2.9;

        ShellyComponentBattery.fillExtStatus(sdata, power, false);

        assertEquals(74.0, sdata.bat1.value, 0.0001);
        assertEquals(2.9, sdata.bat1.voltage, 0.0001);
        assertNull(sdata.bat);
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    void onlyFullStatusClearsMissingExtBattery(boolean fullStatus) {
        ShellyStatusSensor sdata = new ShellyStatusSensor();
        sdata.bat1 = new ShellySensorBat();
        Shelly2DeviceStatusPower powerWithoutBattery = new Shelly2DeviceStatusPower();
        powerWithoutBattery.external = new Shelly2DeviceStatusCharger();

        ShellyComponentBattery.fillExtStatus(sdata, powerWithoutBattery, fullStatus);

        assertEquals(fullStatus, sdata.bat1 == null);
    }

    @Test
    void extBatteryCreatesChannelsAndPublishesLevelAndLow() {
        ShellyStatusSensor sdata = new ShellyStatusSensor();
        sdata.bat1 = new ShellySensorBat();
        sdata.bat1.value = 10.0;
        ShellyThingInterface handler = newHandler();

        assertTrue(ShellyComponentBattery.updateExtChannels(handler, sdata));

        ArgumentCaptor<Map<String, Channel>> channels = ArgumentCaptor.forClass(Map.class);
        verify(handler).updateThingChannels(eq(Map.of()), channels.capture());
        assertEquals(EXT_CHANNEL_IDS, channels.getValue().keySet());
        verify(handler).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_SENSOR_BAT_LEVEL),
                argThat(s -> s instanceof QuantityType<?> q && q.doubleValue() == 10.0));
        verify(handler).updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_BAT_LOW, OnOffType.ON);
        verify(handler).postEvent(ALARM_TYPE_LOW_BATTERY, false);
    }

    @Test
    void missingExtBatteryRemovesChannels() {
        ShellyThingInterface handler = newHandler();

        assertFalse(ShellyComponentBattery.updateExtChannels(handler, new ShellyStatusSensor()));

        verify(handler).removeChannels(EXT_CHANNEL_IDS);
        verify(handler, never()).updateThingChannels(any(), any());
    }

    @Test
    void chargingSuppressesLowThresholdButNotDeviceFlag() {
        ShellySensorBat bat = new ShellySensorBat();
        bat.value = 5.0;
        ShellyThingInterface handler = newHandler();

        ShellyComponentBattery.updateChannels(handler, CHANNEL_GROUP_BATTERY, bat, true);
        bat.batteryLow = true;
        ShellyComponentBattery.updateChannels(handler, CHANNEL_GROUP_BATTERY, bat, true);

        verify(handler).updateChannel(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LOW, OnOffType.OFF);
        verify(handler).updateChannel(CHANNEL_GROUP_BATTERY, CHANNEL_SENSOR_BAT_LOW, OnOffType.ON);
    }
}
