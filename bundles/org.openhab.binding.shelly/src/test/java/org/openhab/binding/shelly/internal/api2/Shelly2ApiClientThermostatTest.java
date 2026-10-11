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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSWALLDISPLAY;

import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2ThermostatProfiles;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.QuantityType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.library.unit.SIUnits;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.types.State;

import com.google.gson.Gson;

/**
 * Tests the thermostat status of the Shelly Wall Display.
 *
 * @author Gerhard Braun - Initial contribution
 */
@NonNullByDefault
public class Shelly2ApiClientThermostatTest {
    // NotifyStatus as sent by the Wall Display after the target temperature was changed on the device
    private static final String NOTIFY_STATUS = """
            {"ts":1791749788,"thermostat:0":{"id":0,"enable":true,"target_C":26.5,"target_F":80,
            "current_C":25.4,"current_F":78,"output":false,"errors":[],
            "schedules":{"enable":true,"profile_id":2,"profile_name":"Office, Winter",
            "next":{"rule_id":"1765226856473","enable":true,"target_C":20.5,"target_F":69,"profile_id":2,
            "timespec":"* 1 0 * * MON,TUE,WED,THU,FRI,SAT,SUN"}}}}
            """;

    private final ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSWALLDISPLAY);
    private @NonNullByDefault({}) Shelly2ApiClient client;
    private @NonNullByDefault({}) ShellyThingInterface thing;

    @BeforeEach
    void setUp() {
        profile.hasThermostat = true;
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
    void statusUpdatesAllThermostatChannels() throws ShellyApiException {
        apply(NOTIFY_STATUS);

        assertTemperature(CHANNEL_CONTROL_CURRTEMP, 25.4);
        assertTemperature(CHANNEL_CONTROL_SETTEMP, 26.5);
        verify(thing).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_THERMOSTAT, OnOffType.ON);
        verify(thing).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_HEATING, OnOffType.OFF);
        verify(thing).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_SCHEDULE, OnOffType.ON);
        verify(thing).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PROFILE, new StringType("2"));
    }

    @Test
    void partialStatusUpdatesOnlyReportedValues() throws ShellyApiException {
        apply("{\"thermostat:0\":{\"id\":0,\"output\":true}}");

        verify(thing).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_HEATING, OnOffType.ON);
        verify(thing, times(1)).updateChannel(any(String.class), any(String.class), any(State.class));
    }

    @Test
    void manualTargetWithoutActiveRuleIsAccepted() throws ShellyApiException {
        // active_rule_id is missing as soon as the target was changed manually
        apply("{\"thermostat:0\":{\"id\":0,\"target_C\":21.5,\"schedules\":{\"enable\":true,\"profile_id\":0}}}");

        assertTemperature(CHANNEL_CONTROL_SETTEMP, 21.5);
        verify(thing).updateChannel(CHANNEL_GROUP_CONTROL, CHANNEL_CONTROL_PROFILE, new StringType("0"));
    }

    @Test
    void statusWithoutThermostatIsIgnored() throws ShellyApiException {
        apply("{\"temperature:0\":{\"id\":0,\"tC\":25.5}}");

        verify(thing, never()).updateChannel(eq(CHANNEL_GROUP_CONTROL), any(String.class), any(State.class));
    }

    @Test
    void deviceWithoutThermostatIsIgnored() throws ShellyApiException {
        profile.hasThermostat = false;
        apply(NOTIFY_STATUS);

        verify(thing, never()).updateChannel(eq(CHANNEL_GROUP_CONTROL), any(String.class), any(State.class));
    }

    @Test
    void notifyStatusMessageContainsThermostat() {
        Shelly2DeviceStatusResult message = Objects
                .requireNonNull(new Gson().fromJson(NOTIFY_STATUS, Shelly2DeviceStatusResult.class));

        assertThat(Objects.requireNonNull(message.thermostat0).targetC, is(26.5));
    }

    @Test
    void profileListIsParsed() {
        Shelly2ThermostatProfiles profiles = Objects.requireNonNull(new Gson().fromJson(
                "{\"profiles\":[{\"id\":0,\"name\":\"Office, Winter\"},{\"id\":1,\"name\":\"Away\"}]}",
                Shelly2ThermostatProfiles.class));

        assertThat(Objects.requireNonNull(profiles.profiles).size(), is(2));
        assertThat(profiles.profiles.get(0).name, is("Office, Winter"));
    }

    private void apply(String json) throws ShellyApiException {
        Shelly2DeviceStatusResult result = Objects
                .requireNonNull(new Gson().fromJson(json, Shelly2DeviceStatusResult.class));
        client.updateThermostatStatus(result.thermostat0);
    }

    private void assertTemperature(String channel, double expected) {
        ArgumentCaptor<State> captor = ArgumentCaptor.forClass(State.class);
        verify(thing).updateChannel(eq(CHANNEL_GROUP_CONTROL), eq(channel), captor.capture());
        QuantityType<?> value = (QuantityType<?>) captor.getValue();
        assertThat(value.getUnit(), is(SIUnits.CELSIUS));
        assertThat(value.doubleValue(), is(expected));
    }
}
