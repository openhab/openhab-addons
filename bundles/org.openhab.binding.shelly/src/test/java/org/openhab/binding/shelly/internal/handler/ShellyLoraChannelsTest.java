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
package org.openhab.binding.shelly.internal.handler;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatusLora;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

@NonNullByDefault
public class ShellyLoraChannelsTest {

    private static final ThingUID THING_UID = new ThingUID("shelly", "shellyplus1", "test");

    @BeforeAll
    static void initChannelDefinitions() {
        ShellyTranslationProvider messages = mock(ShellyTranslationProvider.class);
        when(messages.get(anyString(), any(Object[].class))).thenReturn("mocked");
        new ShellyChannelDefinitions(messages);
    }

    private static Thing thing() {
        Thing thing = mock(Thing.class);
        when(thing.getUID()).thenReturn(THING_UID);
        return thing;
    }

    private static ShellyDeviceProfile loraProfile(boolean rxEnabled) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1);
        profile.settings.loraDetected = true;
        profile.settings.loraRxEnabled = rxEnabled;
        return profile;
    }

    @Test
    void createLoraChannelsDetectedCreatesAllChannelsWithEmptyStatus() {
        // Channel creation is driven by the profile flags only — the status carries no LoRa data at
        // initialization time
        Map<String, Channel> channels = ShellyChannelDefinitions.createLoraChannels(thing(), loraProfile(true),
                new ShellySettingsStatus());

        assertThat(channels.size(), is(10));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RXDATA), is(true));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RXDATARAW), is(true));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RXBYTES), is(true));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_TXDATA), is(true));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_TXDATARAW), is(true));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_TXBYTES), is(true));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_TXERRORS), is(true));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RSSI), is(true));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_SNR), is(true));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_AIRTIME), is(true));
    }

    @Test
    void createLoraChannelsRxDisabledSkipsRxChannels() {
        Map<String, Channel> channels = ShellyChannelDefinitions.createLoraChannels(thing(), loraProfile(false),
                new ShellySettingsStatus());

        assertThat(channels.size(), is(7));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RXDATA), is(false));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RXDATARAW), is(false));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RXBYTES), is(false));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_TXDATA), is(true));
    }

    @Test
    void createLoraChannelsNotDetectedCreatesNothing() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1);

        Map<String, Channel> channels = ShellyChannelDefinitions.createLoraChannels(thing(), profile,
                new ShellySettingsStatus());

        assertThat(channels.isEmpty(), is(true));
    }

    @Test
    void updateLoraStatusUpdatesCounterChannels() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(loraProfile(true));
        Shelly2DeviceStatusLora status = new Shelly2DeviceStatusLora();
        status.rxBytes = 44L;
        status.txBytes = 69280L;
        status.txErrors = 2L;
        status.airtime = 893342L;

        ShellyComponents.updateLoraStatus(handler, status);

        verify(handler).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_RXBYTES),
                argThat(s -> s instanceof QuantityType<?> && ((QuantityType<?>) s).longValue() == 44));
        verify(handler).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_TXBYTES),
                argThat(s -> s instanceof QuantityType<?> && ((QuantityType<?>) s).longValue() == 69280));
        verify(handler).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_AIRTIME),
                argThat(s -> s instanceof QuantityType<?> && ((QuantityType<?>) s).longValue() == 893342));
    }

    @Test
    void updateLoraStatusSetsAddonFirmwareProperty() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(loraProfile(true));
        Shelly2DeviceStatusLora status = new Shelly2DeviceStatusLora();
        status.fw = "1.11.1";

        ShellyComponents.updateLoraStatus(handler, status);

        verify(handler).updateProperties(PROPERTY_ADDON_FIRMWARE, "1.11.1");
    }

    @Test
    void updateLoraStatusUnchangedFirmwareWritesPropertyOnce() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(loraProfile(true));
        Shelly2DeviceStatusLora status = new Shelly2DeviceStatusLora();
        status.fw = "1.11.1";

        ShellyComponents.updateLoraStatus(handler, status);
        ShellyComponents.updateLoraStatus(handler, status);

        verify(handler, times(1)).updateProperties(PROPERTY_ADDON_FIRMWARE, "1.11.1");
    }

    @Test
    void updateLoraStatusNoFirmwareNoPropertyWrite() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(loraProfile(true));

        ShellyComponents.updateLoraStatus(handler, new Shelly2DeviceStatusLora());

        verify(handler, never()).updateProperties(anyString(), anyString());
    }

    @Test
    void updateLoraStatusNotDetectedNoChannelUpdates() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1));
        Shelly2DeviceStatusLora status = new Shelly2DeviceStatusLora();
        status.rxBytes = 44L;

        ShellyComponents.updateLoraStatus(handler, status);

        verify(handler, never()).updateChannel(anyString(), anyString(), any());
    }
}
