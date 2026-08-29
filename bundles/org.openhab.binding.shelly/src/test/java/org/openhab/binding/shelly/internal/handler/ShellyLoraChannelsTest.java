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
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatusLora;
import org.openhab.binding.shelly.internal.provider.ShellyChannelDefinitions;
import org.openhab.binding.shelly.internal.provider.ShellyTranslationProvider;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingUID;

/**
 * Tests for the LoRa Add-On channel lifecycle in {@link ShellyChannelDefinitions} and {@link ShellyComponents}:
 * channel creation and reconciliation, status counter updates and TX command handling.
 *
 * @author Markus Michels - Initial contribution
 */
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
        Map<String, Channel> channels = ShellyChannelDefinitions.createLoraChannels(thing(), loraProfile(true));

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
        Map<String, Channel> channels = ShellyChannelDefinitions.createLoraChannels(thing(), loraProfile(false));

        assertThat(channels.size(), is(5));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RXDATA), is(false));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RXDATARAW), is(false));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RXBYTES), is(false));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_RSSI), is(false));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_SNR), is(false));
        assertThat(channels.containsKey("lora#" + CHANNEL_LORA_TXDATA), is(true));
    }

    @Test
    void createLoraChannelsNotDetectedCreatesNothing() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1);

        Map<String, Channel> channels = ShellyChannelDefinitions.createLoraChannels(thing(), profile);

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
        verify(handler).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_TXERRORS), eq(new DecimalType(2)));
        verify(handler).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_AIRTIME),
                argThat(s -> s instanceof QuantityType<?> && ((QuantityType<?>) s).longValue() == 893342));
    }

    @Test
    void updateLoraStatusPartialUpdateDoesNotClobberOtherChannels() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(loraProfile(true));
        Shelly2DeviceStatusLora full = new Shelly2DeviceStatusLora();
        full.rxBytes = 44L;
        full.txBytes = 69280L;
        full.txErrors = 2L;
        full.airtime = 893342L;
        ShellyComponents.updateLoraStatus(handler, full);

        Shelly2DeviceStatusLora delta = new Shelly2DeviceStatusLora();
        delta.rxBytes = 50L;
        ShellyComponents.updateLoraStatus(handler, delta);

        verify(handler, times(2)).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_RXBYTES), any());
        verify(handler, times(1)).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_TXBYTES), any());
        verify(handler, times(1)).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_TXERRORS), any());
        verify(handler, times(1)).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_AIRTIME), any());
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

        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_LORA), anyString(), any());
    }

    @Test
    void getObsoleteLoraChannelIdsDetectedAndRxEnabledReturnsEmpty() {
        assertThat(ShellyChannelDefinitions.getObsoleteLoraChannelIds(loraProfile(true)).isEmpty(), is(true));
    }

    @Test
    void getObsoleteLoraChannelIdsRxDisabledReturnsRxOnlyChannels() {
        Set<String> obsolete = ShellyChannelDefinitions.getObsoleteLoraChannelIds(loraProfile(false));

        assertThat(obsolete.contains("lora#" + CHANNEL_LORA_RXDATA), is(true));
        assertThat(obsolete.contains("lora#" + CHANNEL_LORA_TXDATA), is(false));
    }

    @Test
    void getObsoleteLoraChannelIdsNotDetectedReturnsAllChannels() {
        Set<String> obsolete = ShellyChannelDefinitions
                .getObsoleteLoraChannelIds(new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1));

        assertThat(obsolete.contains("lora#" + CHANNEL_LORA_RXDATA), is(true));
        assertThat(obsolete.contains("lora#" + CHANNEL_LORA_TXDATA), is(true));
    }

    @Test
    void updateDeviceStatusRemovesLoraChannelsAndFirmwarePropertyWhenAddonRemoved() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1);
        profile.addOnFw = "1.11.1";
        when(handler.getProfile()).thenReturn(profile);
        Thing thing = thing();
        when(handler.getThing()).thenReturn(thing);
        when(handler.areChannelsCreated()).thenReturn(true);

        ShellyComponents.updateDeviceStatus(handler, new ShellySettingsStatus());

        verify(handler).removeChannels(argThat(ids -> ids.contains("lora#" + CHANNEL_LORA_TXDATA)));
        verify(handler).removeProperty(PROPERTY_ADDON_FIRMWARE);
    }

    @Test
    void updateDeviceStatusRemovesRxChannelsWhenRxDisabled() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(loraProfile(false));
        Thing thing = thing();
        when(handler.getThing()).thenReturn(thing);
        when(handler.areChannelsCreated()).thenReturn(true);

        ShellyComponents.updateDeviceStatus(handler, new ShellySettingsStatus());

        verify(handler).removeChannels(argThat(ids -> ids.contains("lora#" + CHANNEL_LORA_RXDATA)));
        verify(handler, never()).removeProperty(PROPERTY_ADDON_FIRMWARE);
    }

    @Test
    void updateDeviceStatusAddsLoraChannelsAfterChannelsCreated() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(loraProfile(true));
        Thing thing = thing();
        when(handler.getThing()).thenReturn(thing);
        when(handler.areChannelsCreated()).thenReturn(true);

        ShellyComponents.updateDeviceStatus(handler, new ShellySettingsStatus());

        verify(handler, never()).updateChannelDefinitions(any());
        verify(handler).updateThingChannels(eq(Map.of()),
                argThat(channels -> channels.size() == 10 && channels.containsKey("lora#" + CHANNEL_LORA_TXDATA)
                        && channels.containsKey("lora#" + CHANNEL_LORA_RXDATA)));
    }

    @Test
    void updateDeviceStatusAddsNoLoraChannelsWhenNotDetected() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(new ShellyDeviceProfile(THING_TYPE_SHELLYPLUS1));
        Thing thing = thing();
        when(handler.getThing()).thenReturn(thing);
        when(handler.areChannelsCreated()).thenReturn(true);

        ShellyComponents.updateDeviceStatus(handler, new ShellySettingsStatus());

        verify(handler).updateThingChannels(eq(Map.of()), argThat(Map::isEmpty));
    }

    @Test
    void updateDeviceStatusKeepsLoraChannelsWhenDetectedAndRxEnabled() {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getProfile()).thenReturn(loraProfile(true));
        Thing thing = thing();
        when(handler.getThing()).thenReturn(thing);
        when(handler.areChannelsCreated()).thenReturn(true);

        ShellyComponents.updateDeviceStatus(handler, new ShellySettingsStatus());

        verify(handler, never()).removeChannels(any());
    }

    @Test
    void handleLoraCommandTxDataEncodesTextAsBase64AndSends() throws ShellyApiException {
        ShellyThingInterface handler = loraCommandHandler();

        ShellyComponents.handleLoraCommand(handler, CHANNEL_LORA_TXDATA, new StringType("Hello"));

        verify(handler.getApi()).loraSendData(0, "SGVsbG8=");
        verify(handler).updateChannel(CHANNEL_GROUP_LORA, CHANNEL_LORA_TXDATARAW, new StringType("SGVsbG8="));
    }

    @Test
    void handleLoraCommandTxDataRawSendsAndDecodesValidUtf8Payload() throws ShellyApiException {
        ShellyThingInterface handler = loraCommandHandler();

        ShellyComponents.handleLoraCommand(handler, CHANNEL_LORA_TXDATARAW, new StringType("SGVsbG8="));

        verify(handler.getApi()).loraSendData(0, "SGVsbG8=");
        verify(handler).updateChannel(CHANNEL_GROUP_LORA, CHANNEL_LORA_TXDATA, new StringType("Hello"));
    }

    @Test
    void handleLoraCommandTxDataRawSendsRawButSkipsTextChannelOnNonUtf8Payload() throws ShellyApiException {
        ShellyThingInterface handler = loraCommandHandler();

        ShellyComponents.handleLoraCommand(handler, CHANNEL_LORA_TXDATARAW, new StringType("//4="));

        verify(handler.getApi()).loraSendData(0, "//4=");
        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_LORA), eq(CHANNEL_LORA_TXDATA), any());
    }

    @Test
    void handleLoraCommandTxDataRawSkipsSendOnInvalidBase64() throws ShellyApiException {
        ShellyThingInterface handler = loraCommandHandler();

        ShellyComponents.handleLoraCommand(handler, CHANNEL_LORA_TXDATARAW, new StringType("not base64!!"));

        verify(handler.getApi(), never()).loraSendData(anyInt(), anyString());
        verify(handler, never()).updateChannel(eq(CHANNEL_GROUP_LORA), anyString(), any());
    }

    private static ShellyThingInterface loraCommandHandler() throws ShellyApiException {
        ShellyThingInterface handler = mock(ShellyThingInterface.class);
        when(handler.getThingName()).thenReturn("test-lora");
        when(handler.getApi()).thenReturn(mock(ShellyApiInterface.class));
        return handler;
    }
}
