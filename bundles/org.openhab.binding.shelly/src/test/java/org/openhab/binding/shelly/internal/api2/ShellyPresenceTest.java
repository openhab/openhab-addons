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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CHANNEL_GROUP_SENSOR;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CHANNEL_SENSOR_OBJECT_COUNT;
import static org.openhab.binding.shelly.internal.ShellyBindingConstants.CHANNEL_SENSOR_PRESENCE;
import static org.openhab.binding.shelly.internal.ShellyDevices.*;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLY2_EVENT_COUNTER;
import static org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.SHELLY2_EVENT_PRESENCE;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2SettingsPresence;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2StatusPresence;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;

import com.google.gson.Gson;

/**
 * Unit tests for Shelly Presence Gen4 JSON parsing — TypeAdapterFactory and NotifyEvent handling.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyPresenceTest {

    private final Gson gson = new Gson();

    @Test
    void configAdapterCollectsSinglePresenceZone() {
        String json = "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{},"
                + "\"presencezone:200\":{\"id\":200,\"name\":\"Zone 1\",\"enable\":true}}";
        Shelly2GetConfigResult dc = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));

        assertThat(dc.presence, is(notNullValue()));
        assertThat(dc.presence.size(), is(1));
        Shelly2SettingsPresence zone = dc.presence.get(0);
        assertThat(zone.id, is(200));
        assertThat(zone.name, is("Zone 1"));
        assertThat(zone.enable, is(true));
    }

    @Test
    void configAdapterCollectsMultiplePresenceZones() {
        String json = "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{},"
                + "\"presencezone:200\":{\"id\":200,\"name\":\"Main\",\"enable\":true},"
                + "\"presencezone:201\":{\"id\":201,\"name\":\"Side\",\"enable\":false}}";
        Shelly2GetConfigResult dc = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));

        assertThat(dc.presence, is(notNullValue()));
        assertThat(dc.presence.size(), is(2));
    }

    @Test
    void configAdapterPresenceNullWhenNoZoneKeys() {
        String json = "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}}";
        Shelly2GetConfigResult dc = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));

        assertThat(dc.presence, is(nullValue()));
    }

    @Test
    void configAdapterSkipsNullPresenceZoneEntry() {
        String json = "{\"sys\":{\"device\":{},\"location\":{}},\"wifi\":{}," + "\"presencezone:200\":null,"
                + "\"presencezone:201\":{\"id\":201,\"name\":\"Side\",\"enable\":true}}";
        Shelly2GetConfigResult dc = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));

        assertThat("null zone entry must be skipped", dc.presence, is(notNullValue()));
        assertThat(dc.presence.size(), is(1));
        assertThat(dc.presence.get(0).id, is(201));
    }

    @Test
    void configAdapterPreservesSiblingFields() {
        String json = "{\"sys\":{\"device\":{\"name\":\"mydevice\"},\"location\":{}},\"wifi\":{},"
                + "\"presence\":{\"enable\":true,\"main_zone\":\"presencezone:200\"},"
                + "\"presencezone:200\":{\"id\":200,\"name\":\"Main\",\"enable\":true}}";
        Shelly2GetConfigResult dc = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));

        assertThat(dc.presence0, is(notNullValue()));
        assertThat(dc.presence0.enable, is(true));
        assertThat(dc.presence0.mainZone, is("presencezone:200"));
        assertThat(dc.sys.device.name, is("mydevice"));
        assertThat(dc.presence, is(notNullValue()));
        assertThat(dc.presence.size(), is(1));
    }

    @Test
    void statusAdapterCollectsZoneAndInjectsId() {
        String json = "{\"sys\":{\"available_updates\":{}},\"presencezone:200\":{\"value\":true,\"num_objects\":2}}";
        Shelly2DeviceStatusResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));

        assertThat(result.presence, is(notNullValue()));
        assertThat(result.presence.size(), is(1));
        Shelly2StatusPresence zone = result.presence.get(0);
        assertThat("id injected from key", zone.id, is(200));
        assertThat(zone.value, is(true));
        assertThat(zone.numObjects, is(2));
    }

    @Test
    void statusAdapterHandlesPresenceOff() {
        String json = "{\"sys\":{\"available_updates\":{}},\"presencezone:200\":{\"value\":false,\"num_objects\":0}}";
        Shelly2DeviceStatusResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));

        assertThat(result.presence, is(notNullValue()));
        Shelly2StatusPresence zone = result.presence.get(0);
        assertThat(zone.value, is(false));
        assertThat(zone.numObjects, is(0));
    }

    @Test
    void statusAdapterPresenceNullWhenNoZoneKeys() {
        String json = "{\"sys\":{\"available_updates\":{}}}";
        Shelly2DeviceStatusResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));

        assertThat(result.presence, is(nullValue()));
    }

    @Test
    void statusAdapterSkipsNullPresenceZoneEntry() {
        String json = "{\"sys\":{\"available_updates\":{}}," + "\"presencezone:200\":null,"
                + "\"presencezone:201\":{\"value\":true,\"num_objects\":1}}";
        Shelly2DeviceStatusResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));

        assertThat("null zone entry must be skipped", result.presence, is(notNullValue()));
        assertThat(result.presence.size(), is(1));
        assertThat(result.presence.get(0).id, is(201));
        assertThat(result.presence.get(0).value, is(true));
    }

    @Test
    void statusAdapterCollectsMultipleZones() {
        String json = "{\"sys\":{\"available_updates\":{}},"
                + "\"presencezone:200\":{\"value\":true,\"num_objects\":1},"
                + "\"presencezone:201\":{\"value\":false,\"num_objects\":0}}";
        Shelly2DeviceStatusResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));

        assertThat(result.presence, is(notNullValue()));
        assertThat(result.presence.size(), is(2));
        boolean hasZone200 = result.presence.stream().anyMatch(z -> Integer.valueOf(200).equals(z.id));
        boolean hasZone201 = result.presence.stream().anyMatch(z -> Integer.valueOf(201).equals(z.id));
        assertThat("zone 200 present", hasZone200, is(true));
        assertThat("zone 201 present", hasZone201, is(true));
    }

    @Test
    void notifyEventPresenceDeserializes() {
        String json = "{\"src\":\"shellypresence-aabb\",\"ts\":1731931521.19,"
                + "\"params\":{\"ts\":1731931521.19,\"events\":["
                + "{\"component\":\"presencezone:200\",\"id\":200,\"event\":\"presence\","
                + "\"value\":true,\"ts\":1731931521.19}]}}";
        Shelly2RpcNotifyEvent msg = Objects.requireNonNull(gson.fromJson(json, Shelly2RpcNotifyEvent.class));

        assertThat(msg.params.events.size(), is(1));
        var e = msg.params.events.get(0);
        assertThat(e.event, is("presence"));
        assertThat(e.component, is("presencezone:200"));
        assertThat(e.value, is(true));
        assertThat(e.numObjects, is(nullValue()));
    }

    @Test
    void notifyEventCounterDeserializes() {
        String json = "{\"src\":\"shellypresence-aabb\",\"ts\":1731931521.19,"
                + "\"params\":{\"ts\":1731931521.19,\"events\":["
                + "{\"component\":\"presencezone:200\",\"id\":200,\"event\":\"counter\","
                + "\"num_objects\":2,\"ts\":1731931521.19}]}}";
        Shelly2RpcNotifyEvent msg = Objects.requireNonNull(gson.fromJson(json, Shelly2RpcNotifyEvent.class));

        assertThat(msg.params.events.size(), is(1));
        var e = msg.params.events.get(0);
        assertThat(e.event, is("counter"));
        assertThat(e.numObjects, is(2));
        assertThat(e.value, is(nullValue()));
    }

    @Test
    void notifyEventPresenceWithAbsentValueFieldDeserializesToNull() {
        String json = "{\"src\":\"shellypresence-aabb\",\"ts\":1731931521.19,"
                + "\"params\":{\"ts\":1731931521.19,\"events\":["
                + "{\"component\":\"presencezone:200\",\"id\":200,\"event\":\"presence\"," + "\"ts\":1731931521.19}]}}";
        Shelly2RpcNotifyEvent msg = Objects.requireNonNull(gson.fromJson(json, Shelly2RpcNotifyEvent.class));

        var e = msg.params.events.get(0);
        assertThat("absent value field must deserialize to null — handler must not write spurious OFF", e.value,
                is(nullValue()));
    }

    @Test
    void presenceProfileSetsIsSensorAndIsPresence() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSPRESENCE);

        assertTrue(profile.isPresence, "isPresence must be true for shellypluspresence");
        assertTrue(profile.isSensor, "isSensor must be true so updateSensors() runs");
        assertFalse(profile.hasBattery, "Presence is mains-powered — hasBattery must be false");
        assertTrue(profile.alwaysOn, "mains-powered device must be alwaysOn");
        assertThat(profile.presenceMainZoneKey, is("presencezone:200"));
    }

    @Test
    void presenceProfileIsNotSetForOtherThingTypes() {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSSMOKE);

        assertFalse(profile.isPresence);
    }

    @Test
    void presenceEventFromMainZoneUpdatesChannelAndCache() throws ShellyApiException {
        Fixture f = build();

        f.rpc.onNotifyEvent(presenceEventJson("presencezone:200", true));

        verify(f.thing).updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_PRESENCE, OnOffType.ON);
        assertThat(f.rpc.getSensorStatus().presence, is(true));
    }

    @Test
    void presenceEventFromOtherZoneIsIgnored() throws ShellyApiException {
        Fixture f = build();

        f.rpc.onNotifyEvent(presenceEventJson("presencezone:201", true));

        verify(f.thing, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_SENSOR_PRESENCE), any());
        assertThat(f.rpc.getSensorStatus().presence, is(nullValue()));
    }

    @Test
    void counterEventFromMainZoneUpdatesChannelAndCache() throws ShellyApiException {
        Fixture f = build();

        f.rpc.onNotifyEvent(counterEventJson("presencezone:200", 3));

        verify(f.thing).updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_OBJECT_COUNT, new DecimalType(3));
        assertThat(f.rpc.getSensorStatus().objectCount, is(3));
    }

    @Test
    void counterEventFromOtherZoneIsIgnored() throws ShellyApiException {
        Fixture f = build();

        f.rpc.onNotifyEvent(counterEventJson("presencezone:201", 3));

        verify(f.thing, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_SENSOR_OBJECT_COUNT), any());
        assertThat(f.rpc.getSensorStatus().objectCount, is(nullValue()));
    }

    @Test
    void statusRefreshAfterZoneEventKeepsEventState() throws ShellyApiException {
        Fixture f = build();
        f.rpc.onNotifyEvent(presenceEventJson("presencezone:200", true));
        f.rpc.onNotifyEvent(counterEventJson("presencezone:200", 2));

        ShellyStatusSensor sdata = f.rpc.getSensorStatus();

        assertThat(sdata.presence, is(true));
        assertThat(sdata.objectCount, is(2));
    }

    private static String presenceEventJson(String component, boolean value) {
        return """
                {"src":"shellypresenceg4-test","params":{"ts":1.0,"events":[\
                {"component":"%s","id":200,"event":"%s","value":%s,"ts":1.0}]}}
                """.formatted(component, SHELLY2_EVENT_PRESENCE, value);
    }

    private static String counterEventJson(String component, int numObjects) {
        return """
                {"src":"shellypresenceg4-test","params":{"ts":1.0,"events":[\
                {"component":"%s","id":200,"event":"%s","num_objects":%d,"ts":1.0}]}}
                """.formatted(component, SHELLY2_EVENT_COUNTER, numObjects);
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
        Thing ohThing = mock(Thing.class);
        when(ohThing.getThingTypeUID()).thenReturn(THING_TYPE_SHELLYPLUSPRESENCE);

        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(thing.getProfile()).thenReturn(new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSPRESENCE));

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080,
                mock(NetworkAddressService.class));
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-presence", "");

        Shelly2ApiRpc rpc = new Shelly2ApiRpc("test-presence", mock(ShellyThingTable.class), thing, config,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class));

        return new Fixture(rpc, thing);
    }
}
