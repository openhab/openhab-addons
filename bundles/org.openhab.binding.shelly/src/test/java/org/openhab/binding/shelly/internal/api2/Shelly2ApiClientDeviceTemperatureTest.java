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
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.SHELLY_API_INVTEMP;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.types.UnDefType;

import com.google.gson.Gson;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2ApiClientDeviceTemperatureTest {
    private final ShellyDeviceProfile profile = new ShellyDeviceProfile(new ThingTypeUID("shelly", "shellypro4pm"));
    private @NonNullByDefault({}) Shelly2ApiClient client;
    private @NonNullByDefault({}) ShellyThingInterface thing;

    @BeforeEach
    void setUp() {
        thing = mock(ShellyThingInterface.class);
        when(thing.getProfile()).thenReturn(profile);
        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.50"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080,
                mock(NetworkAddressService.class));
        client = new Shelly2ApiClient("test", new ShellyApiConfiguration(bindingConfig, "test", "192.168.1.100"),
                thing);
    }

    @Test
    void deviceTempIsHottestSwitch() throws ShellyApiException {
        fill(35.0, 52.0, 41.0, 38.0);

        assertDeviceTemp(52.0);
    }

    @Test
    void deviceTempFollowsCoolingDevice() throws ShellyApiException {
        fill(35.0, 52.0, 41.0, 38.0);
        fill(33.0, 40.0, 36.0, 34.0);

        assertDeviceTemp(40.0);
    }

    @Test
    void notifyStatusOfCoolerSwitchKeepsHottestSwitch() throws ShellyApiException {
        fill(35.0, 52.0);
        profile.status.temperature = SHELLY_API_INVTEMP;
        fill(30.0);

        assertDeviceTemp(52.0);
    }

    @Test
    void nullReadingDropsStaleHottestSwitch() throws ShellyApiException {
        fill(35.0, 52.0);
        fill(35.0, null);

        assertDeviceTemp(35.0);
    }

    @Test
    void notifyStatusWithoutTemperatureKeepsCachedReading() throws ShellyApiException {
        fill(35.0, 52.0);
        apply("{\"switch:1\":{\"id\":1,\"output\":true}}");

        assertDeviceTemp(52.0);
    }

    @Test
    void allReadingsNullInvalidatesDeviceTemp() throws ShellyApiException {
        fill(35.0, 52.0);
        fill(null, null);

        assertThat(profile.status.temperature, is(SHELLY_API_INVTEMP));
        assertThat(profile.status.tmp.isValid, is(false));
    }

    @Test
    void allReadingsNullSetsDeviceTempChannelUndefOnce() throws ShellyApiException {
        fill(35.0, 52.0);
        fill(null, null);
        fill(null, null);

        verify(thing, times(1)).updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP, UnDefType.UNDEF);
    }

    @Test
    void singleNullReadingKeepsDeviceTempChannel() throws ShellyApiException {
        fill(35.0, 52.0);
        fill(35.0, null);

        verify(thing, never()).updateChannel(CHANNEL_GROUP_DEV_STATUS, CHANNEL_DEVST_ITEMP, UnDefType.UNDEF);
    }

    @Test
    void validReadingAfterNullRestoresDeviceTemp() throws ShellyApiException {
        fill(35.0, 52.0);
        fill(null, null);
        fill(36.0, 48.0);

        assertDeviceTemp(48.0);
    }

    private void fill(Double... temperatures) throws ShellyApiException {
        StringBuilder json = new StringBuilder("{\"sys\":{}");
        for (int i = 0; i < temperatures.length; i++) {
            json.append(",\"switch:%d\":{\"id\":%d,\"output\":false,\"temperature\":{\"tC\":%s}}".formatted(i, i,
                    temperatures[i]));
        }
        apply(json + "}");
    }

    private void apply(String json) throws ShellyApiException {
        Shelly2DeviceStatusResult result = Objects
                .requireNonNull(new Gson().fromJson(json, Shelly2DeviceStatusResult.class));
        client.fillDeviceStatus(profile.status, result, false);
    }

    private void assertDeviceTemp(double expected) {
        assertThat(profile.status.temperature, is(expected));
        assertThat(profile.status.tmp.tC, is(expected));
    }
}
