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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.ALARM_TYPE_FLOOD;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.ALARM_TYPE_NONE;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.ALARM_TYPE_SENSOR_ERROR;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYPLUSFLOOD;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLY2_EVENT_FLOOD_ALARM;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLY2_EVENT_FLOOD_ALARM_OFF;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLY2_EVENT_FLOOD_CABLE_UNPLUGGED;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult.Shelly2DeviceStatusFlood;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

import com.google.gson.Gson;

@NonNullByDefault
public class Shelly2FloodNotifyEventTest {

    @Test
    void floodAlarmPostsFloodAlarm() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent(eventJson(SHELLY2_EVENT_FLOOD_ALARM));
        verify(f.thing).postEvent(ALARM_TYPE_FLOOD, true);
    }

    @Test
    void floodAlarmOffPostsNone() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent(eventJson(SHELLY2_EVENT_FLOOD_ALARM_OFF));
        verify(f.thing).postEvent(ALARM_TYPE_NONE, true);
    }

    @Test
    void floodCableUnpluggedPostsSensorError() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent(eventJson(SHELLY2_EVENT_FLOOD_CABLE_UNPLUGGED));
        verify(f.thing).postEvent(ALARM_TYPE_SENSOR_ERROR, true);
    }

    @Test
    void unknownEventDoesNotPostAlarm() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent(eventJson("some.unknown.event"));
        verify(f.thing, never()).postEvent(any(), anyBoolean());
    }

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
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080,
                mock(NetworkAddressService.class));
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-flood", "");

        Shelly2ApiRpc rpc = new Shelly2ApiRpc("test-flood", mock(ShellyThingTable.class), thing, config,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class));

        return new Fixture(rpc, thing);
    }

    @Test
    void updateFloodStatusMapsAlarmMuteAndErrors() throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Gson().fromJson(
                "{\"flood:0\":{\"alarm\":true,\"mute\":true,\"errors\":[\"cable_unplugged\"]}}",
                Shelly2DeviceStatusResult.class);
        assertNotNull(result);
        ShellyStatusSensor sdata = new ShellyStatusSensor();
        new FloodApiStub(apiConfig()).callUpdateFloodStatus(sdata, Objects.requireNonNull(result.flood0));
        assertThat(sdata.flood, is(true));
        assertThat(sdata.mute, is(true));
        assertThat(sdata.sensorError, is("cable_unplugged"));
    }

    @Test
    void updateFloodStatusNoErrorsLeavesErrorNull() throws ShellyApiException {
        Shelly2DeviceStatusResult result = new Gson().fromJson("{\"flood:0\":{\"alarm\":false,\"mute\":false}}",
                Shelly2DeviceStatusResult.class);
        assertNotNull(result);
        ShellyStatusSensor sdata = new ShellyStatusSensor();
        new FloodApiStub(apiConfig()).callUpdateFloodStatus(sdata, Objects.requireNonNull(result.flood0));
        assertThat(sdata.flood, is(false));
        assertThat(sdata.mute, is(false));
        assertThat(sdata.sensorError, is(nullValue()));
    }

    private ShellyApiConfiguration apiConfig() {
        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080,
                mock(NetworkAddressService.class));
        return new ShellyApiConfiguration(bindingConfig, "test-flood", "");
    }

    private static final class FloodApiStub extends Shelly2ApiClient {
        FloodApiStub(ShellyApiConfiguration config) {
            super("test", config, Mockito.mock(HttpClient.class));
        }

        void callUpdateFloodStatus(ShellyStatusSensor sdata, Shelly2DeviceStatusFlood flood) {
            updateFloodStatus(sdata, flood);
        }

        @Override
        public <T> T apiRequest(String method, @Nullable Object params, Class<T> classOfT) throws ShellyApiException {
            throw new ShellyApiException("Unexpected apiRequest in test: " + method);
        }
    }
}
