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

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSHT;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyApiInterface;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySensorSleepMode;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellySettingsStatus;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyStatus;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.Gson;

/**
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2ApiRpcNotifyStatusSleepDeviceTest {
    private static final String WAKEUP_ONLY = """
            {"src":"test","method":"NotifyStatus","params":{"ts":1.0,"sys":{"uptime":100}}}
            """;

    private static Stream<Arguments> provideDevices() {
        return Stream.of(Arguments.of(THING_TYPE_SHELLYPLUSHT, true), Arguments.of(THING_TYPE_SHELLYPLUS1PM, false));
    }

    @ParameterizedTest
    @MethodSource("provideDevices")
    void offlineDeviceComesOnlineOnWakeupPushOnlyWhenSleeping(ThingTypeUID thingTypeUID, boolean sleeping)
            throws ShellyApiException {
        Fixture f = build(thingTypeUID, false, ThingStatusDetail.COMMUNICATION_ERROR);

        f.rpc.onNotifyStatus(message(WAKEUP_ONLY));

        verify(f.thing, times(sleeping ? 1 : 0)).setThingOnline();
    }

    @ParameterizedTest
    @MethodSource("provideDevices")
    void unchangedStatusRestartsWatchdogOnlyForSleepingDevices(ThingTypeUID thingTypeUID, boolean sleeping)
            throws ShellyApiException {
        Fixture f = build(thingTypeUID, true, ThingStatusDetail.NONE);

        f.rpc.onNotifyStatus(message(WAKEUP_ONLY));

        verify(f.thing, times(sleeping ? 1 : 0)).restartWatchdog();
    }

    @Test
    void reportedWakeupPeriodDiscardsLearnedPeriodOnlyWhenChanged() throws ShellyApiException {
        Fixture f = build(THING_TYPE_SHELLYPLUSHT, true, ThingStatusDetail.NONE);
        ShellySensorSleepMode sleepMode = new ShellySensorSleepMode();
        sleepMode.unit = "m";
        sleepMode.period = 720;
        f.profile.settings.sleepMode = sleepMode;

        f.rpc.onNotifyStatus(message(wakeupWithPeriod(3600)));
        f.profile.learnWakeupInterval(6 * 3600);
        f.rpc.onNotifyStatus(message(wakeupWithPeriod(3600)));

        assertThat(f.profile.learnedWakeupPeriod, is(equalTo(6 * 3600)));

        f.rpc.onNotifyStatus(message(wakeupWithPeriod(7200)));

        assertThat(f.profile.learnedWakeupPeriod, is(equalTo(0)));
        assertThat(f.profile.updatePeriod, is(equalTo((int) Math.round(7200 * 1.1) + 60)));
    }

    private static String wakeupWithPeriod(int seconds) {
        return """
                {"src":"test","method":"NotifyStatus","params":{"ts":1.0,"sys":{"uptime":100,"wakeup_period":%d}}}
                """.formatted(seconds);
    }

    private static Shelly2RpcNotifyStatus message(String json) {
        Shelly2RpcNotifyStatus message = new Gson().fromJson(json, Shelly2RpcNotifyStatus.class);
        if (message == null) {
            throw new IllegalStateException("invalid test message");
        }
        return message;
    }

    private record Fixture(Shelly2ApiRpc rpc, ShellyThingInterface thing, ShellyDeviceProfile profile) {
    }

    private static Fixture build(ThingTypeUID thingTypeUID, boolean online, ThingStatusDetail detail)
            throws ShellyApiException {
        Thing ohThing = mock(Thing.class);
        when(ohThing.getThingTypeUID()).thenReturn(thingTypeUID);

        ShellyDeviceProfile profile = new ShellyDeviceProfile(thingTypeUID);
        profile.status = new ShellySettingsStatus();

        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(thing.getProfile()).thenReturn(profile);
        when(thing.isThingOnline()).thenReturn(online);
        when(thing.getThingStatusDetail()).thenReturn(detail);
        when(thing.areChannelsCreated()).thenReturn(true);
        ShellyApiInterface api = mock(ShellyApiInterface.class);
        when(api.getSensorStatus()).thenReturn(new ShellyStatusSensor());
        when(thing.getApi()).thenReturn(api);

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080,
                mock(NetworkAddressService.class));
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-sleep", "");

        Shelly2ApiRpc rpc = new Shelly2ApiRpc("test-sleep", mock(ShellyThingTable.class), thing, config,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class));
        return new Fixture(rpc, thing, profile);
    }
}
