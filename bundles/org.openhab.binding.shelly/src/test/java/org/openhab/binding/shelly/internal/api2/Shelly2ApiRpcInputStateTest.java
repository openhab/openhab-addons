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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSI4;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyInputState;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;

import com.google.gson.Gson;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings("null")
class Shelly2ApiRpcInputStateTest {

    private ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSI4);
    private @Nullable Shelly2ApiRpc rpc;

    @BeforeEach
    void setUp() {
        profile.numInputs = 1;
        ArrayList<ShellyInputState> inputs = new ArrayList<>();
        inputs.add(new ShellyInputState(0));
        profile.status.inputs = inputs;

        Thing ohThing = mock(Thing.class);
        when(ohThing.getThingTypeUID()).thenReturn(THING_TYPE_SHELLYPLUSI4);
        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(thing.getProfile()).thenReturn(profile);

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080,
                mock(NetworkAddressService.class));
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-i4", "");
        rpc = new Shelly2ApiRpc("test-i4", mock(ShellyThingTable.class), thing, config, mock(WebSocketClient.class),
                mock(ScheduledExecutorService.class));
    }

    @Test
    void buttonEventUpdatesTheStatusInput() throws ShellyApiException {
        ShellyInputState input = profile.status.inputs.get(0);

        rpc.onNotifyEvent("""
                {"src":"shellyplusi4-test","params":{"ts":1.0,"events":[{"id":0,"event":"single_push"}]}}
                """);

        assertThat(profile.status.inputs.get(0), is(sameInstance(input)));
        assertThat(input.eventCount, is(1));
        assertThat(input.event, is(notNullValue()));
        assertThat(input.event, is(not("")));
    }

    @Test
    void statusPollKeepsTheEventCountOfAButtonEvent() throws ShellyApiException {
        List<ShellyInputState> inputs = profile.status.inputs;
        rpc.onNotifyEvent("""
                {"src":"shellyplusi4-test","params":{"ts":1.0,"events":[{"id":0,"event":"single_push"}]}}
                """);
        Shelly2DeviceStatusResult result = new Gson().fromJson("{\"input:0\":{\"id\":0,\"state\":true}}",
                Shelly2DeviceStatusResult.class);
        assertNotNull(result);

        rpc.updateInputStatus(profile.status, result, false);

        assertThat(profile.status.inputs, is(sameInstance(inputs)));
        assertThat(inputs.get(0).input, is(1));
        assertThat(inputs.get(0).eventCount, is(1));
    }

    @Test
    void statusPollIgnoresUnknownInputId() throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Gson().fromJson("{\"input:3\":{\"id\":3,\"state\":true}}",
                Shelly2DeviceStatusResult.class);
        assertNotNull(result);

        rpc.updateInputStatus(profile.status, result, false);

        assertThat(profile.status.inputs.size(), is(1));
    }
}
