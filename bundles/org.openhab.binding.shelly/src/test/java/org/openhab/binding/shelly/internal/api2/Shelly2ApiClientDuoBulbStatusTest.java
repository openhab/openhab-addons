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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSCOLORBULB;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSDUOBULB;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.SHELLY_MODE_COLOR;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.SHELLY_MODE_WHITE;

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
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusLight;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2RGBCCTStatus;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2Energy;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;

/**
 * Covers {@link Shelly2ApiClient#fillDeviceStatus} for the Multicolor Bulb G3 ({@code rgbcct:0}) profile, in
 * particular {@code updateDuoBulbStatus}'s handling of {@code NotifyStatus} payloads that omit the {@code mode}
 * field on partial (output/brightness-only) updates.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class Shelly2ApiClientDuoBulbStatusTest {

    @Mock
    private @NonNullByDefault({}) ShellyThingInterface thing;

    private ShellyDeviceProfile multicolorBulbProfile(boolean inColor) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSCOLORBULB);
        profile.isRGBCCT = true;
        profile.inColor = inColor;
        profile.device.mode = inColor ? SHELLY_MODE_COLOR : SHELLY_MODE_WHITE;
        profile.status.lights = new ArrayList<>(List.of(new ShellySettingsLight()));
        return profile;
    }

    private Shelly2ApiClient newClient(ShellyDeviceProfile profile) {
        when(thing.getProfile()).thenReturn(profile);
        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.50"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-realm", "192.168.1.100");
        return new Shelly2ApiClient("test", config, thing);
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

    private ShellyDeviceProfile duoBulbProfile() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSDUOBULB);
        profile.status.lights = new ArrayList<>(List.of(new ShellySettingsLight()));
        return profile;
    }

    private Shelly2RGBCCTStatus rgbcctStatus(@Nullable String mode) {
        Shelly2RGBCCTStatus status = new Shelly2RGBCCTStatus();
        status.id = 0;
        status.mode = mode;
        status.output = true;
        status.brightness = 42.0;
        return status;
    }

    @Test
    void rgbcctPowerAndEnergyAreWiredIntoMeterChannel() throws ShellyApiException {
        ShellyDeviceProfile profile = multicolorBulbProfile(true);
        profile.numMeters = 1;
        profile.status.emeters = new ArrayList<>(List.of(new ShellySettingsEMeter()));
        Shelly2ApiClient client = newClient(profile);

        Shelly2RGBCCTStatus rgbcct = rgbcctStatus("rgb");
        rgbcct.apower = 12.5;
        Shelly2Energy energy = new Shelly2Energy();
        energy.total = 1234.0;
        energy.byMinute = new Double[] { 10.0, 20.0, 30.0 };
        rgbcct.aenergy = energy;

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.rgbcct0 = rgbcct;

        client.fillDeviceStatus(profile.status, result, false);

        ShellySettingsEMeter emeter = profile.status.emeters.get(0);
        assertThat(emeter.power, is(12.5));
        assertThat(emeter.total, is(1234.0));
        Double @Nullable [] byMinute = emeter.energyByMinute;
        assertNotNull(byMinute);
        assertThat(byMinute[0], is(0.01));
        assertThat(byMinute[1], is(0.02));
        assertThat(byMinute[2], is(0.03));
    }

    @Test
    void cctPowerAndEnergyAreWiredIntoMeterChannel() throws ShellyApiException {
        ShellyDeviceProfile profile = duoBulbProfile();
        profile.numMeters = 1;
        profile.status.emeters = new ArrayList<>(List.of(new ShellySettingsEMeter()));
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusLight cct = new Shelly2DeviceStatusLight();
        cct.id = 0;
        cct.output = true;
        cct.brightness = 42.0;
        cct.apower = 5.5;
        Shelly2Energy energy = new Shelly2Energy();
        energy.total = 567.0;
        cct.aenergy = energy;

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.cct0 = cct;

        client.fillDeviceStatus(profile.status, result, false);

        ShellySettingsEMeter emeter = profile.status.emeters.get(0);
        assertThat(emeter.power, is(5.5));
        assertThat(emeter.total, is(567.0));
    }

    @Test
    void rgbModeNotificationSwitchesProfileToColor() throws ShellyApiException {
        ShellyDeviceProfile profile = multicolorBulbProfile(false);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.rgbcct0 = rgbcctStatus("rgb");

        client.fillDeviceStatus(profile.status, result, false);

        assertThat(profile.inColor, is(true));
        assertThat(profile.device.mode, is(SHELLY_MODE_COLOR));
    }

    @Test
    void partialNotificationWithoutModePreservesCurrentRgbMode() throws ShellyApiException {
        ShellyDeviceProfile profile = multicolorBulbProfile(true);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.rgbcct0 = rgbcctStatus(null);

        client.fillDeviceStatus(profile.status, result, false);

        assertThat(profile.inColor, is(true));
        assertThat(profile.device.mode, is(SHELLY_MODE_COLOR));
    }

    @Test
    void partialNotificationWithoutModePreservesCurrentWhiteMode() throws ShellyApiException {
        ShellyDeviceProfile profile = multicolorBulbProfile(false);
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.rgbcct0 = rgbcctStatus(null);

        client.fillDeviceStatus(profile.status, result, false);

        assertThat(profile.inColor, is(false));
        assertThat(profile.device.mode, is(SHELLY_MODE_WHITE));
    }
}
