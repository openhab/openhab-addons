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
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLYRPC_METHOD_GETSTATUS;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.Gson;

/**
 * Tests for the device temperature of a multi-switch Gen2+ device across consecutive
 * {@link Shelly2ApiRpc#getStatus()} poll cycles.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2ApiRpcGetStatusTemperatureTest {
    private static final ThingTypeUID PRO4PM = new ThingTypeUID("shelly", "shellypro4pm");

    private static String status(Double... temperatures) {
        StringBuilder switches = new StringBuilder();
        for (int i = 0; i < temperatures.length; i++) {
            switches.append(",\"switch:%d\":{\"id\":%d,\"output\":false,\"temperature\":{\"tC\":%s}}".formatted(i, i,
                    temperatures[i]));
        }
        return "{\"sys\":{\"uptime\":100},\"cloud\":{},\"mqtt\":{},\"wifi\":{}" + switches + "}";
    }

    private static String statusWithoutTemperature() {
        return "{\"sys\":{\"uptime\":100},\"cloud\":{},\"mqtt\":{},\"wifi\":{},\"switch:0\":{\"id\":0,\"output\":false}}";
    }

    @Test
    void deviceTempFollowsCoolingDeviceAcrossPollCycles() throws ShellyApiException {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(PRO4PM);
        Shelly2ApiRpc rpc = build(profile);

        poll(rpc, status(35.0, 52.0, 41.0, 38.0));
        assertThat(profile.status.tmp.tC, is(52.0));

        poll(rpc, status(33.0, 40.0, 36.0, 34.0));

        assertThat(profile.status.temperature, is(40.0));
        assertThat(profile.status.tmp.tC, is(40.0));
    }

    @Test
    void deviceTempRisesAgainAfterCooling() throws ShellyApiException {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(PRO4PM);
        Shelly2ApiRpc rpc = build(profile);
        poll(rpc, status(50.0, 45.0));
        poll(rpc, status(30.0, 28.0));

        poll(rpc, status(44.0, 31.0));

        assertThat(profile.status.tmp.tC, is(44.0));
    }

    @Test
    void deviceTempIsClearedWhenNoSwitchReportsTemperatureAnymore() throws ShellyApiException {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(PRO4PM);
        Shelly2ApiRpc rpc = build(profile);
        poll(rpc, status(50.0));

        poll(rpc, statusWithoutTemperature());

        assertThat(profile.status.temperature, is(nullValue()));
    }

    private static void poll(Shelly2ApiRpc rpc, String json) throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Gson().fromJson(json, Shelly2DeviceStatusResult.class);
        doReturn(result).when(rpc).apiRequest(eq(SHELLYRPC_METHOD_GETSTATUS), any(),
                eq(Shelly2DeviceStatusResult.class));
        rpc.getStatus();
    }

    private static Shelly2ApiRpc build(ShellyDeviceProfile profile) {
        Thing ohThing = mock(Thing.class);
        when(ohThing.getThingTypeUID()).thenReturn(PRO4PM);

        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(thing.getProfile()).thenReturn(profile);

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080,
                mock(NetworkAddressService.class));
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-temp", "");

        return spy(new Shelly2ApiRpc("test-temp", mock(ShellyThingTable.class), thing, config,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class)));
    }
}
