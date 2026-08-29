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
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;

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
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsEMeter;
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
 * Covers {@link Shelly2ApiClient#fillDeviceStatus} for RGBW2 devices, i.e. the light-mode
 * ({@code light:N}/{@code cct:N}) and color-mode ({@code rgbw:0}/{@code rgb:0}) status dispatch added for
 * Plus RGBW PM / Pro RGBWW PM. Exercises {@code updateLightModeStatus} and {@code updateRGBWStatus} without
 * a real HTTP/WebSocket connection by mocking {@link ShellyThingInterface#getProfile()}.
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
        status.lights = new ArrayList<>();
        status.lights.add(new ShellySettingsLight());
        return profile;
    }

    private Shelly2DeviceStatusLight lightStatus(int id, boolean on, double brightness) {
        Shelly2DeviceStatusLight ls = new Shelly2DeviceStatusLight();
        ls.id = id;
        ls.output = on;
        ls.brightness = brightness;
        return ls;
    }

    private ShellyDeviceProfile proRgbwwPmProfile(String rawProfile, int numLights, int numMeters) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellyprorgbwwpm"));
        profile.isRGBW2 = true;
        profile.inColor = SHELLY2_PROFILE_RGB.equals(rawProfile) || SHELLY2_PROFILE_RGBW.equals(rawProfile)
                || SHELLY2_PROFILE_RGBCCT.equals(rawProfile) || SHELLY2_PROFILE_RGBX2LIGHT.equals(rawProfile);
        profile.device.profile = rawProfile;
        profile.numMeters = numMeters;
        ShellySettingsStatus status = profile.status;
        ArrayList<ShellySettingsLight> lights = new ArrayList<>();
        ArrayList<ShellySettingsRgbwLight> settingsLights = new ArrayList<>();
        for (int i = 0; i < numLights; i++) {
            lights.add(new ShellySettingsLight());
            settingsLights.add(new ShellySettingsRgbwLight());
        }
        status.lights = lights;
        profile.settings.lights = settingsLights;
        ArrayList<ShellySettingsEMeter> emeters = new ArrayList<>();
        for (int i = 0; i < numMeters; i++) {
            emeters.add(new ShellySettingsEMeter());
        }
        status.emeters = emeters;
        return profile;
    }

    private Shelly2RGBWStatus rgbwStatusWithMeter(double apower, double totalWh, double voltage, double current) {
        Shelly2RGBWStatus rgbw = new Shelly2RGBWStatus();
        rgbw.id = 0;
        rgbw.output = true;
        rgbw.apower = apower;
        Shelly2Energy aenergy = new Shelly2Energy();
        aenergy.total = totalWh;
        rgbw.aenergy = aenergy;
        rgbw.voltage = voltage;
        rgbw.current = current;
        return rgbw;
    }

    private Shelly2DeviceStatusLight lightStatusWithMeter(int id, double apower, double totalWh, double voltage,
            double current) {
        Shelly2DeviceStatusLight ls = lightStatus(id, true, 50.0);
        ls.apower = apower;
        Shelly2Energy aenergy = new Shelly2Energy();
        aenergy.total = totalWh;
        ls.aenergy = aenergy;
        ls.voltage = voltage;
        ls.current = current;
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
    void cctx2ProfileUpdatesBothChannelsFromCctFields() throws ShellyApiException {
        ShellyDeviceProfile profile = lightModeProfile(2);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusLight cct0 = lightStatus(0, true, 30.0);
        cct0.ct = 2700;
        Shelly2DeviceStatusLight cct1 = lightStatus(1, true, 60.0);
        cct1.ct = 6500;
        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.cct0 = cct0;
        result.cct1 = cct1;

        client.fillDeviceStatus(profile.status, result, false);

        List<ShellySettingsLight> lights = profile.status.lights;
        assertThat(lights.get(0).brightness, is(30));
        assertThat(lights.get(0).temp, is(2700));
        assertThat(lights.get(1).brightness, is(60));
        assertThat(lights.get(1).temp, is(6500));
    }

    @Test
    void cctx2ProfilePopulatesBothMeterSlotsFromCctFields() throws ShellyApiException {
        ShellyDeviceProfile profile = proRgbwwPmProfile(SHELLY2_PROFILE_CCTX2, 2, 2);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.cct0 = lightStatusWithMeter(0, 12.5, 100.0, 230.0, 0.05);
        result.cct1 = lightStatusWithMeter(1, 7.5, 50.0, 231.0, 0.03);

        client.fillDeviceStatus(profile.status, result, false);

        List<ShellySettingsEMeter> emeters = profile.status.emeters;
        assertThat(emeters.get(0).power, is(12.5));
        assertThat(emeters.get(0).total, is(100.0));
        assertThat(emeters.get(1).power, is(7.5));
        assertThat(emeters.get(1).total, is(50.0));
    }

    @Test
    void rgbcctProfilePopulatesMeterSlotsForBothRgb0AndCct0() throws ShellyApiException {
        // slot 0 = rgb0 (color), slot 1 = cct0 (secondary) - matches fillRgbwSettings()'s flat layout
        ShellyDeviceProfile profile = proRgbwwPmProfile(SHELLY2_PROFILE_RGBCCT, 2, 2);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.rgb0 = rgbwStatusWithMeter(20.0, 200.0, 230.0, 0.1);
        result.cct0 = lightStatusWithMeter(0, 12.5, 100.0, 231.0, 0.05);

        client.fillDeviceStatus(profile.status, result, false);

        List<ShellySettingsEMeter> emeters = profile.status.emeters;
        assertThat(emeters.get(0).power, is(20.0));
        assertThat(emeters.get(0).total, is(200.0));
        assertThat(emeters.get(1).power, is(12.5));
        assertThat(emeters.get(1).total, is(100.0));
        assertThat(profile.status.lights.get(1).ison, is(true));
    }

    @Test
    void rgbx2lightProfilePopulatesMeterSlotsForRgb0AndBothLightChannels() throws ShellyApiException {
        // slot 0 = rgb0 (color), slots 1/2 = light0/light1 (secondary) - matches fillRgbwSettings()'s flat layout
        ShellyDeviceProfile profile = proRgbwwPmProfile(SHELLY2_PROFILE_RGBX2LIGHT, 3, 3);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.rgb0 = rgbwStatusWithMeter(15.0, 150.0, 229.0, 0.08);
        result.light0 = lightStatusWithMeter(0, 5.0, 50.0, 228.0, 0.02);
        result.light1 = lightStatusWithMeter(1, 6.0, 60.0, 227.0, 0.03);

        client.fillDeviceStatus(profile.status, result, false);

        List<ShellySettingsEMeter> emeters = profile.status.emeters;
        assertThat(emeters.get(0).power, is(15.0));
        assertThat(emeters.get(0).total, is(150.0));
        assertThat(emeters.get(1).power, is(5.0));
        assertThat(emeters.get(1).total, is(50.0));
        assertThat(emeters.get(2).power, is(6.0));
        assertThat(emeters.get(2).total, is(60.0));
        assertThat(profile.status.lights.get(1).ison, is(true));
        assertThat(profile.status.lights.get(2).ison, is(true));
    }

    @Test
    void lightProfilePopulatesMeterSlotForEachComponent() throws ShellyApiException {
        // plain "light" profile (no color component): every settings.lights entry starts at slot 0
        ShellyDeviceProfile profile = proRgbwwPmProfile(SHELLY2_PROFILE_LIGHT, 2, 2);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.light0 = lightStatusWithMeter(0, 5.0, 50.0, 228.0, 0.02);
        result.light1 = lightStatusWithMeter(1, 6.0, 60.0, 227.0, 0.03);

        client.fillDeviceStatus(profile.status, result, false);

        List<ShellySettingsEMeter> emeters = profile.status.emeters;
        assertThat(emeters.get(0).power, is(5.0));
        assertThat(emeters.get(0).total, is(50.0));
        assertThat(emeters.get(1).power, is(6.0));
        assertThat(emeters.get(1).total, is(60.0));
    }

    @Test
    void rgbProfilePopulatesMeterSlotForColorComponent() throws ShellyApiException {
        ShellyDeviceProfile profile = proRgbwwPmProfile(SHELLY2_PROFILE_RGB, 1, 1);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.rgb0 = rgbwStatusWithMeter(20.0, 200.0, 230.0, 0.1);

        client.fillDeviceStatus(profile.status, result, false);

        List<ShellySettingsEMeter> emeters = profile.status.emeters;
        assertThat(emeters.get(0).power, is(20.0));
        assertThat(emeters.get(0).total, is(200.0));
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
