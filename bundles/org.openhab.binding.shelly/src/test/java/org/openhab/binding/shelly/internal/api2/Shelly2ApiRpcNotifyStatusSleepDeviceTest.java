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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUS1PM;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSHT;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
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
 * Tests for the sleep device handling of {@link Shelly2ApiRpc#onNotifyStatus}: wakeup pushes of an OFFLINE battery
 * device are accepted, the reported wakeup period drives the watchdog timeout and every wakeup restarts the watchdog.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2ApiRpcNotifyStatusSleepDeviceTest {
    private static final String WAKEUP_ONLY = """
            {"src":"test","method":"NotifyStatus","params":{"ts":1.0,"sys":{"uptime":100}}}
            """;

    private static String wakeupWithPeriod(int seconds) {
        return """
                {"src":"test","method":"NotifyStatus","params":{"ts":1.0,"sys":{"uptime":100,"wakeup_period":%d}}}
                """.formatted(seconds);
    }

    @Test
    void offlineSleepDeviceAcceptsWakeupPushAndComesOnline() throws ShellyApiException {
        Fixture f = build(THING_TYPE_SHELLYPLUSHT, false, ThingStatusDetail.COMMUNICATION_ERROR);

        f.rpc.onNotifyStatus(message(WAKEUP_ONLY));

        verify(f.thing).setThingOnline();
        verify(f.thing).restartWatchdog();
    }

    @Test
    void offlineAlwaysOnDeviceIgnoresNotifyStatus() throws ShellyApiException {
        Fixture f = build(THING_TYPE_SHELLYPLUS1PM, false, ThingStatusDetail.COMMUNICATION_ERROR);

        f.rpc.onNotifyStatus(message(WAKEUP_ONLY));

        verify(f.thing, never()).setThingOnline();
        verify(f.thing, never()).restartWatchdog();
    }

    @Test
    void onlineSleepDeviceRestartsWatchdogWithoutChangedValues() throws ShellyApiException {
        Fixture f = build(THING_TYPE_SHELLYPLUSHT, true, ThingStatusDetail.NONE);

        f.rpc.onNotifyStatus(message(WAKEUP_ONLY));

        verify(f.thing).restartWatchdog();
    }

    @Test
    void onlineAlwaysOnDeviceDoesNotRestartWatchdogWithoutChangedValues() throws ShellyApiException {
        Fixture f = build(THING_TYPE_SHELLYPLUS1PM, true, ThingStatusDetail.NONE);

        f.rpc.onNotifyStatus(message(WAKEUP_ONLY));

        verify(f.thing, never()).restartWatchdog();
    }

    @Test
    void reportedWakeupPeriodDrivesWatchdogTimeout() throws ShellyApiException {
        Fixture f = build(THING_TYPE_SHELLYPLUSHT, true, ThingStatusDetail.NONE);
        f.profile.settings.sleepMode = sleepMode(720);
        f.profile.updateWatchdogPeriod();

        f.rpc.onNotifyStatus(message(wakeupWithPeriod(3600)));

        assertThat(f.profile.settings.sleepMode.period, is(equalTo(60)));
        assertThat(f.profile.updatePeriod, is(equalTo((int) Math.round(3600 * 1.1) + 60)));
    }

    @Test
    void reportedWakeupPeriodReplacesLearnedPeriod() throws ShellyApiException {
        Fixture f = build(THING_TYPE_SHELLYPLUSHT, true, ThingStatusDetail.NONE);
        f.profile.settings.sleepMode = sleepMode(60);
        f.profile.updateWatchdogPeriod();
        f.profile.learnWakeupInterval(20 * 3600);

        f.rpc.onNotifyStatus(message(wakeupWithPeriod(3600)));

        assertThat(f.profile.learnedWakeupPeriod, is(equalTo(0)));
        assertThat(f.profile.updatePeriod, is(equalTo((int) Math.round(3600 * 1.1) + 60)));
    }

    @Test
    void reportedWakeupPeriodIsIgnoredWithoutSleepMode() throws ShellyApiException {
        Fixture f = build(THING_TYPE_SHELLYPLUSHT, true, ThingStatusDetail.NONE);
        f.profile.settings.sleepMode = null;
        int before = f.profile.updatePeriod;

        f.rpc.onNotifyStatus(message(wakeupWithPeriod(3600)));

        assertThat(f.profile.updatePeriod, is(equalTo(before)));
    }

    private static ShellySensorSleepMode sleepMode(int minutes) {
        ShellySensorSleepMode sleepMode = new ShellySensorSleepMode();
        sleepMode.unit = "m";
        sleepMode.period = minutes;
        return sleepMode;
    }

    private static Shelly2RpcNotifyStatus message(String json) {
        Shelly2RpcNotifyStatus message = new Gson().fromJson(json, Shelly2RpcNotifyStatus.class);
        if (message == null) {
            throw new IllegalStateException("invalid test message");
        }
        return message;
    }

    private static final class Fixture {
        final Shelly2ApiRpc rpc;
        final ShellyThingInterface thing;
        final ShellyDeviceProfile profile;

        Fixture(Shelly2ApiRpc rpc, ShellyThingInterface thing, ShellyDeviceProfile profile) {
            this.rpc = rpc;
            this.thing = thing;
            this.profile = profile;
        }
    }

    private static Fixture build(ThingTypeUID thingTypeUID, boolean online, ThingStatusDetail detail) {
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
        try {
            when(api.getSensorStatus()).thenReturn(new ShellyStatusSensor());
        } catch (ShellyApiException e) {
            throw new IllegalStateException(e);
        }
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
