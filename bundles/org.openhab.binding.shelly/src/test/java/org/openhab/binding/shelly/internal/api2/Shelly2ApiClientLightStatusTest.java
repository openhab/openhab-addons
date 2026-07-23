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
import static org.mockito.Mockito.when;

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
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusLight;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2RGBWStatus;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;

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
        status.lights = new ArrayList<>();
        for (int i = 0; i < numChannels; i++) {
            status.lights.add(new ShellySettingsLight());
        }
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
        // Untouched channels stay at their previous (default) state
        assertThat(lights.get(1).ison, is(nullValue()));
        assertThat(lights.get(3).ison, is(nullValue()));
    }

    @Test
    void cctx2ProfileUpdatesBothChannelsFromCctFields() throws ShellyApiException {
        ShellyDeviceProfile profile = lightModeProfile(2);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.cct0 = lightStatus(0, true, 30.0);
        result.cct1 = lightStatus(1, true, 60.0);

        client.fillDeviceStatus(profile.status, result, false);

        List<ShellySettingsLight> lights = profile.status.lights;
        assertThat(lights.get(0).brightness, is(30));
        assertThat(lights.get(1).brightness, is(60));
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
        // Color-mode devices never report light:N, but if the firmware did, it must be ignored
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
