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
import static org.openhab.binding.shelly.internal.api2.dto.ShellyPresenceJsonDTO.SHELLY2_EVENT_COUNTER;
import static org.openhab.binding.shelly.internal.api2.dto.ShellyPresenceJsonDTO.SHELLY2_EVENT_PRESENCE;
import static org.openhab.binding.shelly.internal.api2.dto.ShellyPresenceJsonDTO.SHELLY2_PRESENCE_DEFAULT_ZONE_ID;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceConfig.Shelly2GetConfigResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusResult;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2RpcNotifyEvent;
import org.openhab.binding.shelly.internal.api2.dto.ShellyPresenceJsonDTO.Shelly2StatusPresence;
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
 * Unit tests for Shelly Presence Gen4 JSON parsing, status polling and NotifyEvent handling.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyPresenceTest {

    private final Gson gson = new Gson();

    @Test
    void configParsesPresenceBlockAlongsideDynamicZoneKeys() {
        String json = "{\"sys\":{\"device\":{\"name\":\"mydevice\"},\"location\":{}},\"wifi\":{},"
                + "\"presence\":{\"enable\":true,\"main_zone\":\"presencezone:200\"},"
                + "\"presencezone:200\":{\"id\":200,\"name\":\"Main\",\"enable\":true}}";
        Shelly2GetConfigResult dc = Objects.requireNonNull(gson.fromJson(json, Shelly2GetConfigResult.class));

        var presence = Objects.requireNonNull(dc.presence);
        assertThat(presence.enable, is(true));
        assertThat(presence.mainZone, is("presencezone:200"));
        assertThat(dc.sys.device.name, is("mydevice"));
    }

    @Test
    void presenceZoneGetStatusResponseDeserializes() {
        String json = "{\"id\":200,\"value\":true,\"num_objects\":2}";
        Shelly2StatusPresence zone = Objects.requireNonNull(gson.fromJson(json, Shelly2StatusPresence.class));

        assertThat(zone.id, is(200));
        assertThat(zone.value, is(true));
        assertThat(zone.numObjects, is(2));
    }

    @Test
    void illuminationPublishesWithoutANumericLuxValue() throws ShellyApiException {
        // real Presence Gen4 hardware never reports a numeric lux value, only the illumination category
        String json = "{\"sys\":{\"available_updates\":{}},\"illuminance:0\":{\"id\":0,\"illumination\":\"dark\"}}";
        Shelly2DeviceStatusResult result = Objects.requireNonNull(gson.fromJson(json, Shelly2DeviceStatusResult.class));
        Fixture f = build();
        ShellyStatusSensor sdata = new ShellyStatusSensor();

        f.rpc.updateIlluminanceStatus(sdata, result.illuminance0);

        var lux = Objects.requireNonNull(sdata.lux);
        assertThat(lux.isValid, is(true));
        assertThat(lux.value, is(nullValue()));
        assertThat(lux.illumination, is("dark"));
    }

    @Test
    void notifyEventPresenceDeserializes() {
        String json = "{\"src\":\"shellypresence-aabb\",\"ts\":1731931521.19,"
                + "\"params\":{\"ts\":1731931521.19,\"events\":["
                + "{\"component\":\"presencezone:200\",\"id\":200,\"event\":\"presence\","
                + "\"value\":true,\"ts\":1731931521.19}]}}";
        Shelly2RpcNotifyEvent msg = Objects.requireNonNull(gson.fromJson(json, Shelly2RpcNotifyEvent.class));

        var events = Objects.requireNonNull(Objects.requireNonNull(msg.params).events);
        assertThat(events.size(), is(1));
        var e = events.get(0);
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

        var events = Objects.requireNonNull(Objects.requireNonNull(msg.params).events);
        assertThat(events.size(), is(1));
        var e = events.get(0);
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

        var e = Objects.requireNonNull(Objects.requireNonNull(msg.params).events).get(0);
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

    @Test
    void configuredMainZoneReplacesDefaultZoneForEventFiltering() throws ShellyApiException {
        Fixture f = build("presencezone:201");

        f.rpc.onNotifyEvent(presenceEventJson("presencezone:201", true));
        f.rpc.onNotifyEvent(counterEventJson("presencezone:201", 4));

        verify(f.thing).updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_PRESENCE, OnOffType.ON);
        verify(f.thing).updateChannel(CHANNEL_GROUP_SENSOR, CHANNEL_SENSOR_OBJECT_COUNT, new DecimalType(4));
        assertThat(f.rpc.getSensorStatus().presence, is(true));
        assertThat(f.rpc.getSensorStatus().objectCount, is(4));
    }

    @Test
    void defaultZoneIsIgnoredWhenAnotherMainZoneIsConfigured() throws ShellyApiException {
        Fixture f = build("presencezone:201");

        f.rpc.onNotifyEvent(presenceEventJson("presencezone:200", true));
        f.rpc.onNotifyEvent(counterEventJson("presencezone:200", 4));

        verify(f.thing, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_SENSOR_PRESENCE), any());
        verify(f.thing, never()).updateChannel(eq(CHANNEL_GROUP_SENSOR), eq(CHANNEL_SENSOR_OBJECT_COUNT), any());
        assertThat(f.rpc.getSensorStatus().presence, is(nullValue()));
        assertThat(f.rpc.getSensorStatus().objectCount, is(nullValue()));
    }

    @Test
    void presenceStatusUpdatesSensorDataFromTheFetchedZone() throws ShellyApiException {
        Fixture f = build();
        ShellyStatusSensor sdata = new ShellyStatusSensor();

        f.rpc.updatePresenceStatus(sdata, statusZone(200, true, 3));

        assertThat(sdata.presence, is(true));
        assertThat(sdata.objectCount, is(3));
    }

    @Test
    void presenceStatusWithNoZoneLeavesSensorDataUnchanged() throws ShellyApiException {
        Fixture f = build();
        ShellyStatusSensor sdata = new ShellyStatusSensor();

        f.rpc.updatePresenceStatus(sdata, null);

        assertThat(sdata.presence, is(nullValue()));
        assertThat(sdata.objectCount, is(nullValue()));
    }

    @Test
    void presenceStatusZoneWithoutValuesKeepsThePreviousReadings() throws ShellyApiException {
        Fixture f = build();
        ShellyStatusSensor sdata = new ShellyStatusSensor();
        sdata.presence = true;
        sdata.objectCount = 2;

        f.rpc.updatePresenceStatus(sdata, statusZone(200, null, null));

        assertThat(sdata.presence, is(true));
        assertThat(sdata.objectCount, is(2));
    }

    @Test
    void mainZoneIdIsParsedFromTheConfiguredZoneKey() {
        assertThat(Shelly2ApiClient.getPresenceMainZoneId("presencezone:201"), is(201));
        assertThat(Shelly2ApiClient.getPresenceMainZoneId("presencezone:0"), is(0));
    }

    @Test
    void malformedMainZoneKeyFallsBackToTheDefaultZone() {
        assertThat(Shelly2ApiClient.getPresenceMainZoneId("presencezone:not-a-number"),
                is(SHELLY2_PRESENCE_DEFAULT_ZONE_ID));
        assertThat(Shelly2ApiClient.getPresenceMainZoneId("presencezone"), is(SHELLY2_PRESENCE_DEFAULT_ZONE_ID));
        assertThat(Shelly2ApiClient.getPresenceMainZoneId("presencezone:"), is(SHELLY2_PRESENCE_DEFAULT_ZONE_ID));
    }

    private static Shelly2StatusPresence statusZone(int id, @Nullable Boolean value, @Nullable Integer numObjects) {
        Shelly2StatusPresence zone = new Shelly2StatusPresence();
        zone.id = id;
        zone.value = value;
        zone.numObjects = numObjects;
        return zone;
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
        return build(new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSPRESENCE));
    }

    private Fixture build(String mainZoneKey) {
        ShellyDeviceProfile profile = new ShellyDeviceProfile(THING_TYPE_SHELLYPLUSPRESENCE);
        profile.presenceMainZoneKey = mainZoneKey;
        return build(profile);
    }

    private Fixture build(ShellyDeviceProfile profile) {
        Thing ohThing = mock(Thing.class);
        when(ohThing.getThingTypeUID()).thenReturn(THING_TYPE_SHELLYPLUSPRESENCE);

        ShellyThingInterface thing = mock(ShellyThingInterface.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(mock(HttpClient.class));
        when(thing.getProfile()).thenReturn(profile);

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
