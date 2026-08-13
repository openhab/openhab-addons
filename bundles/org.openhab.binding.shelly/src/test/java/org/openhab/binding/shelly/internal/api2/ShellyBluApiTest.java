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
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYBLUBUTTON1;
import static org.openhab.binding.shelly.internal.ShellyDevices.THING_TYPE_SHELLYBLUWS90;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.junit.jupiter.api.Test;
import org.openhab.binding.shelly.internal.api.ShellyApiException;
import org.openhab.binding.shelly.internal.api.ShellyDeviceProfile;
import org.openhab.binding.shelly.internal.api1.Shelly1ApiJsonDTO.ShellyStatusSensor;
import org.openhab.binding.shelly.internal.config.ShellyApiConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingConfiguration;
import org.openhab.binding.shelly.internal.config.ShellyBindingRuntimeConfig;
import org.openhab.binding.shelly.internal.config.ShellyThingConfiguration;
import org.openhab.binding.shelly.internal.handler.ShellyBluHandler;
import org.openhab.binding.shelly.internal.handler.ShellyThingInterface;
import org.openhab.binding.shelly.internal.handler.ShellyThingTable;
import org.openhab.core.net.NetworkAddressChangeListener;
import org.openhab.core.net.NetworkAddressService;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Unit tests for {@link ShellyBluApi} — connection state guards.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
@SuppressWarnings({ "null" })
public class ShellyBluApiTest {

    private @Nullable ShellyThingInterface thingMock;

    @Test
    void getStatusThrowsWhenNotConnected() throws Exception {
        ShellyBluApi api = buildBluApi();
        ShellyApiException ex = assertThrows(ShellyApiException.class, api::getStatus);
        assertThat(ex.getMessage(), is("offline.status-error-blu-not-connected"));
    }

    @Test
    void getStatusSucceedsWhenConnected() throws Exception {
        ShellyBluApi api = buildBluApi();
        setField(api, "connected", true);
        assertDoesNotThrow(api::getStatus);
    }

    @Test
    void getSensorStatusThrowsWhenNotConnected() throws Exception {
        ShellyBluApi api = buildBluApi();
        ShellyApiException ex = assertThrows(ShellyApiException.class, api::getSensorStatus);
        assertThat(ex.getMessage(), is("offline.status-error-blu-sensor-unavailable"));
    }

    @Test
    void getSensorStatusSucceedsWhenConnected() throws Exception {
        ShellyBluApi api = buildBluApi();
        setField(api, "connected", true);
        assertDoesNotThrow(api::getSensorStatus);
    }

    @Test
    void onNotifyEventAccumulatesFieldsAcrossSequentialWs90Packets() throws Exception {
        ShellyBluApi api = buildBluApi(THING_TYPE_SHELLYBLUWS90);

        String atmosphericPacket = """
                {"src": "shellyblugw-test", "params": {"events": [{"event": "oh-blu.data",
                 "data": {"addr": "aa:bb:cc:dd:ee:ff", "pid": 1,
                          "Temperature": [18.0], "Humidity": 72.0, "Pressure": 1008.5, "Dewpoint": 13.2,
                          "Battery": 85}}]}}
                """;
        String windPacket = """
                {"src": "shellyblugw-test", "params": {"events": [{"event": "oh-blu.data",
                 "data": {"addr": "aa:bb:cc:dd:ee:ff", "pid": 2,
                          "Moisture": 0.0, "Speed": [4.2, 8.1], "Direction": 135.0, "UVIndex": 3.7,
                          "Precipitation": 0.5}}]}}
                """;

        api.onNotifyEvent(atmosphericPacket);
        api.onNotifyEvent(windPacket);

        ShellyStatusSensor sensorData = api.getSensorStatus();
        assertThat("temperature from first packet retained", sensorData.tmp.tC, is(equalTo(18.0)));
        assertThat("humidity from first packet retained", sensorData.hum.value, is(equalTo(72.0)));
        assertThat("pressure from first packet retained", sensorData.pressure, is(equalTo(1008.5)));
        assertThat("dewPoint from first packet retained", sensorData.dewPoint, is(equalTo(13.2)));
        assertThat("windSpeed from second packet added", sensorData.windSpeed, is(equalTo(4.2)));
        assertThat("gustSpeed from second packet added", sensorData.gustSpeed, is(equalTo(8.1)));
        assertThat("windDirection from second packet added", sensorData.windDirection, is(equalTo(135.0)));
        assertThat("uvIndex from second packet added", sensorData.uvIndex, is(equalTo(3.7)));
        assertThat("precipitation from second packet added", sensorData.precipitation, is(equalTo(0.5)));
        assertThat("rain from second packet added", sensorData.rain, is(equalTo(false)));
    }

    @Test
    void onNotifyEventPostsAlarmForEncryptedPayload() throws Exception {
        ShellyBluApi api = buildBluApi();

        String encryptedAlarmPacket = """
                {"src": "shellyblugw-test", "params": {"events": [{"event": "oh-blu.alarm",
                 "data": {"addr": "aa:bb:cc:dd:ee:ff", "code": "BTH_ENCRYPTED"}}]}}
                """;

        api.onNotifyEvent(encryptedAlarmPacket);

        verify(thingMock).postEvent("BTH_ENCRYPTED", false);
    }

    @Test
    void onNotifyEventPostsAlarmForUnknownObjectType() throws Exception {
        ShellyBluApi api = buildBluApi();

        String unknownTypeAlarmPacket = """
                {"src": "shellyblugw-test", "params": {"events": [{"event": "oh-blu.alarm",
                 "data": {"addr": "aa:bb:cc:dd:ee:ff", "code": "BTH_UNKNOWN_TYPE"}}]}}
                """;

        api.onNotifyEvent(unknownTypeAlarmPacket);

        verify(thingMock).postEvent("BTH_UNKNOWN_TYPE", false);
    }

    private ShellyBluApi buildBluApi() {
        return buildBluApi(THING_TYPE_SHELLYBLUBUTTON1);
    }

    private ShellyBluApi buildBluApi(ThingTypeUID thingTypeUID) {
        Thing ohThing = mock(Thing.class);
        when(ohThing.getThingTypeUID()).thenReturn(thingTypeUID);

        ShellyDeviceProfile deviceProfile = new ShellyDeviceProfile(thingTypeUID);

        HttpClient httpClient = mock(HttpClient.class);
        ShellyThingInterface thing = mock(ShellyBluHandler.class);
        when(thing.getThing()).thenReturn(ohThing);
        when(thing.getHttpClient()).thenReturn(httpClient);
        when(thing.getProfile()).thenReturn(deviceProfile);
        when(thing.areChannelsCreated()).thenReturn(true);
        when(thing.updateChannel(anyString(), anyString(), any())).thenReturn(true);
        when(thing.getThingConfig()).thenReturn(new ShellyThingConfiguration());
        thingMock = thing;

        ShellyBindingConfiguration raw = ShellyBindingConfiguration
                .fromProperties(Map.of(ShellyBindingConfiguration.CONFIG_LOCAL_IP, "192.168.1.1"));
        ShellyBindingRuntimeConfig bindingConfig = new ShellyBindingRuntimeConfig(raw, 8080, nullNas());
        ShellyApiConfiguration config = new ShellyApiConfiguration(bindingConfig, "test-blu", "");

        ShellyBluApi api = new ShellyBluApi("test-blu", mock(ShellyThingTable.class), thing, config,
                mock(WebSocketClient.class), mock(ScheduledExecutorService.class));
        when(thing.getApi()).thenReturn(api);
        return api;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
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
