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
package org.openhab.binding.shelly.internal.api2;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsRgbwLight;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusLight;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2RGBWStatus;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.State;

/**
 * Covers {@link Shelly2ApiClient#fillDeviceStatus} for Plus RGBW PM: the light-mode ({@code light:N}) and
 * color-mode ({@code rgbw:0}/{@code rgb:0}) status dispatch, exercising {@code updateLightModeStatus} and
 * {@code updateRGBWStatus} without a real HTTP/WebSocket connection by mocking
 * {@link ShellyThingInterface#getProfile()}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class Shelly2ApiClientLightStatusTest {

    @Mock
    private @NonNullByDefault({}) ShellyThingInterface thing;

    private Shelly2ApiClient newClient(ShellyDeviceProfile profile) {
        when(thing.getProfile()).thenReturn(profile);
        return new Shelly2ApiClient("test", discoveryConfig(), thing);
    }

    private ShellyApiConfiguration discoveryConfig() {
        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.50"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
        return new ShellyApiConfiguration(bindingConfig, "test-realm", "192.168.1.100");
    }

    private static NetworkAddressService nullNas() {
        return new NetworkAddressService() {
            @Override
            public @Nullable String getPrimaryIpv4HostAddress() {
                return null;
            }

            @Override
            public @Nullable String getConfiguredBroadcastAddress() {
                return null;
            }

            @Override
            public boolean isUseOnlyOneAddress() {
                return false;
            }

            @Override
            public boolean isUseIPv6() {
                return false;
            }

            @Override
            public void addNetworkAddressChangeListener(NetworkAddressChangeListener listener) {
            }

            @Override
            public void removeNetworkAddressChangeListener(NetworkAddressChangeListener listener) {
            }
        };
    }

    private ShellyDeviceProfile lightModeProfile(int numChannels) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellyplusrgbwpm"));
        profile.isRGBW2 = true;
        profile.inColor = false;
        ShellySettingsStatus status = profile.status;
        ArrayList<ShellySettingsLight> lights = new ArrayList<>();
        ArrayList<ShellySettingsRgbwLight> settingsLights = new ArrayList<>();
        for (int i = 0; i < numChannels; i++) {
            lights.add(new ShellySettingsLight());
            settingsLights.add(new ShellySettingsRgbwLight());
        }
        status.lights = lights;
        profile.settings.lights = settingsLights;
        return profile;
    }

    private ShellyDeviceProfile colorModeProfile() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellyplusrgbwpm"));
        profile.isRGBW2 = true;
        profile.inColor = true;
        ShellySettingsStatus status = profile.status;
        ArrayList<ShellySettingsLight> lights = new ArrayList<>();
        lights.add(new ShellySettingsLight());
        status.lights = lights;
        return profile;
    }

    private Shelly2DeviceStatusLight lightStatus(int id, boolean on, double brightness) {
        Shelly2DeviceStatusLight ls = new Shelly2DeviceStatusLight();
        ls.id = id;
        ls.output = on;
        ls.brightness = brightness;
        return ls;
    }

    @Test
    void lightProfileUpdatesBrightnessAndOnStatePerChannel() throws ShellyApiException {
        ShellyDeviceProfile profile = lightModeProfile(4);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.light0 = lightStatus(0, true, 42.0);
        result.light2 = lightStatus(2, false, 7.0);

        client.fillDeviceStatus(profile.status, result, false);

        List<ShellySettingsLight> lights = profile.status.lights;
        assertThat(lights.get(0).ison, is(true));
        assertThat(lights.get(0).brightness, is(42));
        assertThat(lights.get(2).ison, is(false));
        assertThat(lights.get(2).brightness, is(7));
        assertThat(lights.get(1).ison, is(nullValue()));
        assertThat(lights.get(3).ison, is(nullValue()));
    }

    @Test
    void lightModeStatusPushesChannelUpdatesWhenRequested() throws ShellyApiException {
        ShellyDeviceProfile profile = lightModeProfile(2);
        Shelly2ApiClient client = newClient(profile);
        when(thing.areChannelsCreated()).thenReturn(true);
        when(thing.updateChannel(anyString(), anyString(), any(State.class))).thenReturn(true);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.light0 = lightStatus(0, true, 55.0);

        boolean updated = client.fillDeviceStatus(profile.status, result, true);

        assertThat(updated, is(true));
        verify(thing).updateChannel(CHANNEL_GROUP_LIGHT_INDEX + "1", CHANNEL_BRIGHTNESS + "$Switch", OnOffType.ON);
        verify(thing, never()).updateChannel(CHANNEL_GROUP_LIGHT_INDEX + "1", CHANNEL_LIGHT_POWER, OnOffType.ON);
    }

    @Test
    void lightModeStatusSignalsWatchdogEvenWhenNoChannelChanged() throws ShellyApiException {
        ShellyDeviceProfile profile = lightModeProfile(1);
        Shelly2ApiClient client = newClient(profile);
        when(thing.areChannelsCreated()).thenReturn(true);
        when(thing.updateChannel(anyString(), anyString(), any(State.class))).thenReturn(false);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.light0 = lightStatus(0, true, 55.0);

        boolean updated = client.fillDeviceStatus(profile.status, result, true);

        // updateLightModeStatus always signals "processed" for watchdog purposes, independent of
        // whether the channel push itself (verified below) reports a changed value.
        assertThat(updated, is(true));
        verify(thing).updateChannel(CHANNEL_GROUP_LIGHT_CONTROL, CHANNEL_BRIGHTNESS + "$Switch", OnOffType.ON);
    }

    @Test
    void lightModeStatusDoesNotPushChannelsWhenChannelUpdateNotRequested() throws ShellyApiException {
        ShellyDeviceProfile profile = lightModeProfile(1);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.light0 = lightStatus(0, true, 55.0);

        client.fillDeviceStatus(profile.status, result, false);

        verify(thing, never()).updateChannel(anyString(), anyString(), any(State.class));
    }

    @Test
    void colorModeProfileIgnoresLightFieldsAndUsesRgbw() throws ShellyApiException {
        ShellyDeviceProfile profile = colorModeProfile();
        Shelly2ApiClient client = newClient(profile);

        Shelly2RGBWStatus rgbw = new Shelly2RGBWStatus();
        rgbw.id = 0;
        rgbw.output = true;
        rgbw.rgb = new Integer[] { 10, 20, 30 };
        rgbw.white = 40;
        rgbw.brightness = 80.0;

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.rgbw0 = rgbw;
        result.light0 = lightStatus(0, false, 99.0);

        client.fillDeviceStatus(profile.status, result, false);

        ShellySettingsLight light = profile.status.lights.get(0);
        assertThat(light.ison, is(true));
        assertThat(light.red, is(10));
        assertThat(light.green, is(20));
        assertThat(light.blue, is(30));
        assertThat(light.white, is(40));
        assertThat(light.brightness, is(80));
    }
}
