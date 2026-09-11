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
import static org.mockito.Mockito.when;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatusTemp;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RelayStatus;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@ExtendWith(MockitoExtension.class)
public class Shelly2ApiClientRelayTemperatureTest {

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

    private Shelly2RelayStatus relayWithTemp(int id, double tC) {
        Shelly2RelayStatus rs = new Shelly2RelayStatus();
        rs.id = id;
        rs.output = false;
        Shelly2DeviceStatusTemp temp = new Shelly2DeviceStatusTemp();
        temp.tC = tC;
        rs.temperature = temp;
        return rs;
    }

    @Test
    void deviceTempReflectsHottestSwitchRegardlessOfProcessingOrder() throws ShellyApiException {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellypro4pm"));
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.switch0 = relayWithTemp(0, 35.0);
        result.switch1 = relayWithTemp(1, 52.0); // hottest, but not the last one processed
        result.switch2 = relayWithTemp(2, 41.0);
        result.switch3 = relayWithTemp(3, 38.0);

        client.fillDeviceStatus(profile.status, result, false);

        assertThat(profile.status.temperature, is(52.0));
        assertThat(profile.status.tmp.tC, is(52.0));
    }

    @Test
    void deviceTempReflectsHottestSwitchWhenHottestIsProcessedFirst() throws ShellyApiException {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellypro4pm"));
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.switch0 = relayWithTemp(0, 47.0); // hottest, processed first
        result.switch1 = relayWithTemp(1, 36.0);
        result.switch2 = relayWithTemp(2, 33.0);
        result.switch3 = relayWithTemp(3, 30.0);

        client.fillDeviceStatus(profile.status, result, false);

        assertThat(profile.status.temperature, is(47.0));
        assertThat(profile.status.tmp.tC, is(47.0));
    }

    @Test
    void deviceTempSurvivesNotifyStatusInvalidTempSeeding() throws ShellyApiException {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellypro4pm"));
        Shelly2ApiClient client = newClient(profile);

        Shelly2DeviceStatusResult result = new Shelly2DeviceStatusResult();
        result.switch0 = relayWithTemp(0, 35.0);
        result.switch1 = relayWithTemp(1, 52.0);

        profile.status.temperature = 999.0; // SHELLY_API_INVTEMP, as onNotifyStatus() seeds it
        client.fillDeviceStatus(profile.status, result, true);

        assertThat(profile.status.temperature, is(52.0));
        assertThat(profile.status.tmp.tC, is(52.0));
    }
}
