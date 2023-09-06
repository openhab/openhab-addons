/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.devices.bridge;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpMethod;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceTest;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Faults;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.SubscribeResult;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.services.binaryswitch.dto.BinarySwitchServiceState;
import org.openhab.binding.boschshc.internal.services.intrusion.actions.arm.dto.ArmActionRequest;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.AlarmState;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.ArmingState;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.IntrusionDetectionSystemState;
import org.openhab.binding.boschshc.internal.services.shuttercontact.ShutterContactState;
import org.openhab.binding.boschshc.internal.services.shuttercontact.dto.ShutterContactServiceState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

/**
 * Unit tests for the {@link BridgeHandler}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
class BridgeHandlerTest {

    private @NonNullByDefault({}) BridgeHandler fixture;

    private @NonNullByDefault({}) BoschHttpClient httpClient;

    private @NonNullByDefault({}) ThingHandlerCallback thingHandlerCallback;

    @BeforeAll
    static void beforeAll() throws IOException {
        Path mavenTargetFolder = Paths.get("target");
        assertTrue(Files.exists(mavenTargetFolder), "Maven target folder does not exist.");
        System.setProperty("openhab.userdata", mavenTargetFolder.toFile().getAbsolutePath());
        Path etc = mavenTargetFolder.resolve("etc");
        if (!Files.exists(etc)) {
            Files.createDirectory(etc);
        }
    }

    @BeforeEach
    void beforeEach() throws Exception {
        Bridge bridge = mock(Bridge.class);
        fixture = new BridgeHandler(bridge);

        thingHandlerCallback = mock(ThingHandlerCallback.class);
        fixture.setCallback(thingHandlerCallback);

        Configuration bridgeConfiguration = new Configuration();
        Map<@Nullable String, @Nullable Object> properties = new HashMap<>();
        properties.put("ipAddress", "localhost");
        properties.put("password", "test");
        bridgeConfiguration.setProperties(properties);

        Thing thing = mock(Bridge.class);
        when(thing.getConfiguration()).thenReturn(bridgeConfiguration);
        // this calls initialize() as well
        fixture.thingUpdated(thing);

        // shut down the real HTTP client
        if (fixture.httpClient != null) {
            fixture.httpClient.stop();
        }

        // use a mocked HTTP client
        httpClient = mock(BoschHttpClient.class);
        fixture.httpClient = httpClient;
    }

    @Test
    void postAction() throws InterruptedException, TimeoutException, ExecutionException {
        String endpoint = "/intrusion/actions/arm";
        String url = "https://127.0.0.1:8444/smarthome/intrusion/actions/arm";
        when(httpClient.getBoschSmartHomeUrl(endpoint)).thenReturn(url);
        Request mockRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), any(), any())).thenReturn(mockRequest);
        ArmActionRequest request = new ArmActionRequest();
        request.profileId = "0";

        fixture.postAction(endpoint, request);
        verify(httpClient).createRequest(eq(url), same(HttpMethod.POST), same(request));
        verify(mockRequest).send();
    }

    @Test
    void initialAccessHttpClientOffline() {
        fixture.initialAccess(httpClient);
    }

    @Test
    void initialAccessHttpClientOnline() throws InterruptedException {
        when(httpClient.isOnline()).thenReturn(true);
        fixture.initialAccess(httpClient);
    }

    @Test
    void initialAccessAccessPossible()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.isOnline()).thenReturn(true);
        when(httpClient.isAccessPossible()).thenReturn(true);
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        // mock a request and response to obtain rooms
        Request roomsRequest = mock(Request.class);
        ContentResponse roomsResponse = mock(ContentResponse.class);
        when(roomsResponse.getStatus()).thenReturn(200);
        when(roomsResponse.getContentAsString()).thenReturn(
                "[{\"@type\":\"room\",\"id\":\"hz_1\",\"iconId\":\"icon_room_bedroom\",\"name\":\"Bedroom\"}]");
        when(roomsRequest.send()).thenReturn(roomsResponse);
        when(httpClient.createRequest(contains("/rooms"), same(HttpMethod.GET))).thenReturn(roomsRequest);

        // mock a request and response to obtain devices
        Request devicesRequest = mock(Request.class);
        ContentResponse devicesResponse = mock(ContentResponse.class);
        when(devicesResponse.getStatus()).thenReturn(200);
        when(devicesResponse.getContentAsString()).thenReturn("[{\"@type\":\"device\",\r\n"
                + " \"rootDeviceId\":\"64-da-a0-02-14-9b\",\r\n"
                + " \"id\":\"hdm:HomeMaticIP:3014F711A00004953859F31B\",\r\n"
                + " \"deviceServiceIds\":[\"PowerMeter\",\"PowerSwitch\",\"PowerSwitchProgram\",\"Routing\"],\r\n"
                + " \"manufacturer\":\"BOSCH\",\r\n" + " \"roomId\":\"hz_3\",\r\n" + " \"deviceModel\":\"PSM\",\r\n"
                + " \"serial\":\"3014F711A00004953859F31B\",\r\n" + " \"profile\":\"GENERIC\",\r\n"
                + " \"name\":\"Coffee Machine\",\r\n" + " \"status\":\"AVAILABLE\",\r\n" + " \"childDeviceIds\":[]\r\n"
                + " }]");
        when(devicesRequest.send()).thenReturn(devicesResponse);
        when(httpClient.createRequest(contains("/devices"), same(HttpMethod.GET))).thenReturn(devicesRequest);

        SubscribeResult subscribeResult = new SubscribeResult();
        when(httpClient.sendRequest(any(), same(SubscribeResult.class), any(), any())).thenReturn(subscribeResult);

        Request longPollRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> r.method.equals("RE/longPoll")))).thenReturn(longPollRequest);

        fixture.initialAccess(httpClient);
        verify(thingHandlerCallback).statusUpdated(any(),
                eq(ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()));
    }

    @Test
    void getState() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();
        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{\r\n" + "     \"@type\": \"systemState\",\r\n"
                + "     \"systemAvailability\": {\r\n" + "         \"@type\": \"systemAvailabilityState\",\r\n"
                + "         \"available\": true,\r\n" + "         \"deleted\": false\r\n" + "     },\r\n"
                + "     \"armingState\": {\r\n" + "         \"@type\": \"armingState\",\r\n"
                + "         \"state\": \"SYSTEM_DISARMED\",\r\n" + "         \"deleted\": false\r\n" + "     },\r\n"
                + "     \"alarmState\": {\r\n" + "         \"@type\": \"alarmState\",\r\n"
                + "         \"value\": \"ALARM_OFF\",\r\n" + "         \"incidents\": [],\r\n"
                + "         \"deleted\": false\r\n" + "     },\r\n" + "     \"activeConfigurationProfile\": {\r\n"
                + "         \"@type\": \"activeConfigurationProfile\",\r\n" + "         \"deleted\": false\r\n"
                + "     },\r\n" + "     \"securityGapState\": {\r\n" + "         \"@type\": \"securityGapState\",\r\n"
                + "         \"securityGaps\": [],\r\n" + "         \"deleted\": false\r\n" + "     },\r\n"
                + "     \"deleted\": false\r\n" + " }");
        when(request.send()).thenReturn(response);
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);

        IntrusionDetectionSystemState state = fixture.getState("intrusion/states/system",
                IntrusionDetectionSystemState.class);
        assertNotNull(state);
        assertTrue(state.systemAvailability.available);
        assertSame(AlarmState.ALARM_OFF, state.alarmState.value);
        assertSame(ArmingState.SYSTEM_DISARMED, state.armingState.state);
    }

    @Test
    void getDeviceState() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();
        when(httpClient.getServiceStateUrl(anyString(), anyString())).thenCallRealMethod();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString())
                .thenReturn("{\n" + "   \"@type\": \"shutterContactState\",\n" + "   \"value\": \"OPEN\"\n" + " }");
        when(request.send()).thenReturn(response);
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);

        ShutterContactServiceState state = fixture.getState("hdm:HomeMaticIP:3014D711A000009D545DEB39D",
                "ShutterContact", ShutterContactServiceState.class);
        assertNotNull(state);
        assertSame(ShutterContactState.OPEN, state.value);
    }

    @Test
    void getDeviceInfo() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(request.send()).thenReturn(response);
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);
        when(httpClient.sendRequest(same(request), same(Device.class), any(), any()))
                .thenReturn(DeviceTest.createTestDevice());

        String deviceId = "hdm:HomeMaticIP:3014F711A00004953859F31B";
        Device device = fixture.getDeviceInfo(deviceId);
        assertEquals(deviceId, device.id);
    }

    @Test
    void getDeviceInfoErrorCases()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(request.send()).thenReturn(response);
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<BiFunction<Integer, String, BoschSHCException>> errorResponseHandlerCaptor = ArgumentCaptor
                .forClass(BiFunction.class);

        when(httpClient.sendRequest(same(request), same(Device.class), any(), errorResponseHandlerCaptor.capture()))
                .thenReturn(DeviceTest.createTestDevice());

        String deviceId = "hdm:HomeMaticIP:3014F711A00004953859F31B";
        fixture.getDeviceInfo(deviceId);

        BiFunction<Integer, String, BoschSHCException> errorResponseHandler = errorResponseHandlerCaptor.getValue();
        Exception e = errorResponseHandler.apply(500,
                "{\"@type\":\"JsonRestExceptionResponseEntity\",\"errorCode\": \"testErrorCode\",\"statusCode\": 500}");
        assertEquals(
                "Request for info of device hdm:HomeMaticIP:3014F711A00004953859F31B failed with status code 500 and error code testErrorCode",
                e.getMessage());

        e = errorResponseHandler.apply(404,
                "{\"@type\":\"JsonRestExceptionResponseEntity\",\"errorCode\": \"ENTITY_NOT_FOUND\",\"statusCode\": 404}");
        assertNotNull(e);

        e = errorResponseHandler.apply(500, "");
        assertEquals("Request for info of device hdm:HomeMaticIP:3014F711A00004953859F31B failed with status code 500",
                e.getMessage());
    }

    @Test
    void getServiceData() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();
        when(httpClient.getServiceUrl(anyString(), anyString())).thenCallRealMethod();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("{ \n" + "    \"@type\":\"DeviceServiceData\",\n"
                + "    \"path\":\"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel\",\n"
                + "    \"id\":\"BatteryLevel\",\n" + "    \"deviceId\":\"hdm:ZigBee:000d6f0004b93361\",\n"
                + "    \"faults\":{ \n" + "        \"entries\":[\n" + "          {\n"
                + "            \"type\":\"LOW_BATTERY\",\n" + "            \"category\":\"WARNING\"\n" + "          }\n"
                + "        ]\n" + "    }\n" + "}");
        when(request.send()).thenReturn(response);
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);

        DeviceServiceData serviceData = fixture.getServiceData("hdm:ZigBee:000d6f0004b93361", "BatteryLevel");
        assertNotNull(serviceData);
        Faults faults = serviceData.faults;
        assertNotNull(faults);
        assertEquals("LOW_BATTERY", faults.entries.get(0).type);
    }

    @Test
    void getServiceDataError() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();
        when(httpClient.getServiceUrl(anyString(), anyString())).thenCallRealMethod();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(500);
        when(response.getContentAsString()).thenReturn(
                "{\"@type\":\"JsonRestExceptionResponseEntity\",\"errorCode\": \"testErrorCode\",\"statusCode\": 500}");
        when(request.send()).thenReturn(response);
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);
        when(httpClient.sendRequest(same(request), same(Device.class), any(), any()))
                .thenReturn(DeviceTest.createTestDevice());

        BoschSHCException e = assertThrows(BoschSHCException.class,
                () -> fixture.getServiceData("hdm:ZigBee:000d6f0004b93361", "BatteryLevel"));
        assertEquals(
                "State request with URL https://null:8444/smarthome/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel failed with status code 500 and error code testErrorCode",
                e.getMessage());
    }

    @Test
    void getServiceDataErrorNoRestExceptionResponse()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();
        when(httpClient.getServiceUrl(anyString(), anyString())).thenCallRealMethod();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(500);
        when(response.getContentAsString()).thenReturn("");
        when(request.send()).thenReturn(response);
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);

        BoschSHCException e = assertThrows(BoschSHCException.class,
                () -> fixture.getServiceData("hdm:ZigBee:000d6f0004b93361", "BatteryLevel"));
        assertEquals(
                "State request with URL https://null:8444/smarthome/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel failed with status code 500",
                e.getMessage());
    }

    @Test
    void putState() throws InterruptedException, TimeoutException, ExecutionException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();
        when(httpClient.getServiceStateUrl(anyString(), anyString())).thenCallRealMethod();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);

        when(httpClient.createRequest(anyString(), same(HttpMethod.PUT), any(BinarySwitchServiceState.class)))
                .thenReturn(request);
        when(request.send()).thenReturn(response);

        BinarySwitchServiceState binarySwitchState = new BinarySwitchServiceState();
        binarySwitchState.on = true;
        fixture.putState("hdm:ZigBee:f0d1b80000f2a3e9", "BinarySwitch", binarySwitchState);
    }

    @AfterEach
    void afterEach() throws Exception {
        fixture.dispose();
    }
}
