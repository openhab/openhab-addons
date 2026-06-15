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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSFLOOD;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.*;

import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Tests for flood {@code NotifyEvent} handling in {@link Shelly2ApiRpc#onNotifyEvent}.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class Shelly2FloodNotifyEventTest {

    // ── flood.alarm ──────────────────────────────────────────────────────────

    @Test
    void floodAlarmPostsFloodAlarm() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent(eventJson(SHELLY2_EVENT_FLOOD_ALARM));
        verify(f.thing).postEvent(ALARM_TYPE_FLOOD, true);
    }

    // ── flood.alarm_off ──────────────────────────────────────────────────────

    @Test
    void floodAlarmOffPostsNone() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent(eventJson(SHELLY2_EVENT_FLOOD_ALARM_OFF));
        verify(f.thing).postEvent(ALARM_TYPE_NONE, true);
    }

    // ── flood.cable_unplugged ────────────────────────────────────────────────

    @Test
    void floodCableUnpluggedPostsSensorError() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent(eventJson(SHELLY2_EVENT_FLOOD_CABLE_UNPLUGGED));
        verify(f.thing).postEvent(ALARM_TYPE_SENSOR_ERROR, true);
    }

    // ── unknown event ────────────────────────────────────────────────────────

    @Test
    void unknownEventDoesNotPostAlarm() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent(eventJson("some.unknown.event"));
        verify(f.thing, never()).postEvent(any(), anyBoolean());
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private static String eventJson(String event) {
        return """
                {"src":"shellyfloodg4-test","params":{"ts":1.0,"events":[{"id":0,"event":"%s"}]}}
                """.formatted(event);
    }

    private static final class Fixture {
        final Shelly2ApiRpc rpc;
        final ShellyThingInterface thing;

        Fixture(Shelly2ApiRpc rpc, ShellyThingInterface thing) {
            this.rpc = rpc;
            this.thing = thing;
        }
    }

    private Fixture build() {
        ThingTypeUID thingTypeUID = THING_TYPE_SHELLYPLUSFLOOD;

        Thing ohThing = mock(Thing.class);
        when(ohThing.getThingTypeUID()).thenReturn(thingTypeUID);

        ShellyDeviceProfile profile = new ShellyDeviceProfile(thingTypeUID);

        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(thing.getProfile()).thenReturn(profile);

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-flood", "");

        Shelly2ApiRpc rpc = new Shelly2ApiRpc("test-flood", mock(ShellyThingTable.class), thing, config,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class));

        return new Fixture(rpc, thing);
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
}
