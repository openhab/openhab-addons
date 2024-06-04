/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.BoschSHCHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceServiceData;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.DeviceTest;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Faults;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.LongPollResult;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Message;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.PublicInformation;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Room;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Scenario;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.SubscribeResult;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.UserDefinedState;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.UserDefinedStateTest;
import org.openhab.binding.boschshc.internal.discovery.ThingDiscoveryService;
import org.openhab.binding.boschshc.internal.exceptions.BoschSHCException;
import org.openhab.binding.boschshc.internal.serialization.GsonUtils;
import org.openhab.binding.boschshc.internal.services.binaryswitch.dto.BinarySwitchServiceState;
import org.openhab.binding.boschshc.internal.services.intrusion.actions.arm.dto.ArmActionRequest;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.AlarmState;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.ArmingState;
import org.openhab.binding.boschshc.internal.services.intrusion.dto.IntrusionDetectionSystemState;
import org.openhab.binding.boschshc.internal.services.shuttercontact.ShutterContactState;
import org.openhab.binding.boschshc.internal.services.shuttercontact.dto.ShutterContactServiceState;
import org.openhab.core.config.core.Configuration;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingStatusInfo;
import org.openhab.core.thing.binding.ThingHandlerCallback;
import org.openhab.core.thing.binding.builder.ThingStatusInfoBuilder;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

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
    private @NonNullByDefault({}) Bridge thing;
    private @NonNullByDefault({}) Configuration bridgeConfiguration;

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

        bridgeConfiguration = new Configuration();
        Map<@Nullable String, @Nullable Object> properties = new HashMap<>();
        properties.put("ipAddress", "localhost");
        properties.put("password", "test");
        bridgeConfiguration.setProperties(properties);

        thing = mock(Bridge.class);
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
    void postActionWithoutRequestBody() throws InterruptedException, TimeoutException, ExecutionException {
        String endpoint = "/intrusion/actions/disarm";
        String url = "https://127.0.0.1:8444/smarthome/intrusion/actions/disarm";
        when(httpClient.getBoschSmartHomeUrl(endpoint)).thenReturn(url);
        Request mockRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), any(), any())).thenReturn(mockRequest);

        fixture.postAction(endpoint);
        verify(httpClient).createRequest(eq(url), same(HttpMethod.POST), isNull());
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
        when(devicesResponse.getContentAsString()).thenReturn("""
                [{"@type":"device",
                 "rootDeviceId":"64-da-a0-02-14-9b",
                 "id":"hdm:HomeMaticIP:3014F711A00004953859F31B",
                 "deviceServiceIds":["PowerMeter","PowerSwitch","PowerSwitchProgram","Routing"],
                 "manufacturer":"BOSCH",
                 "roomId":"hz_3",
                 "deviceModel":"PSM",
                 "serial":"3014F711A00004953859F31B",
                 "profile":"GENERIC",
                 "name":"Coffee Machine",
                 "status":"AVAILABLE",
                 "childDeviceIds":[]
                 }]\
                """);
        when(devicesRequest.send()).thenReturn(devicesResponse);
        when(httpClient.createRequest(contains("/devices"), same(HttpMethod.GET))).thenReturn(devicesRequest);

        SubscribeResult subscribeResult = new SubscribeResult();
        when(httpClient.sendRequest(any(), same(SubscribeResult.class), any(), any())).thenReturn(subscribeResult);

        Request longPollRequest = mock(Request.class);
        when(httpClient.createRequest(anyString(), same(HttpMethod.POST),
                argThat((JsonRpcRequest r) -> "RE/longPoll".equals(r.method)))).thenReturn(longPollRequest);

        ThingDiscoveryService thingDiscoveryListener = mock(ThingDiscoveryService.class);
        fixture.registerDiscoveryListener(thingDiscoveryListener);

        fixture.initialAccess(httpClient);

        verify(thingHandlerCallback).statusUpdated(any(),
                eq(ThingStatusInfoBuilder.create(ThingStatus.ONLINE, ThingStatusDetail.NONE).build()));
        verify(thingDiscoveryListener).doScan();
    }

    @Test
    void initialAccessNoBridgeAccess() throws InterruptedException, TimeoutException, ExecutionException {
        when(httpClient.isOnline()).thenReturn(true);
        when(httpClient.isAccessPossible()).thenReturn(true);
        Request request = mock(Request.class);
        when(httpClient.createRequest(any(), same(HttpMethod.GET))).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(request.send()).thenReturn(response);
        when(response.getStatus()).thenReturn(400);

        fixture.initialAccess(httpClient);

        verify(thingHandlerCallback).statusUpdated(same(thing),
                argThat(status -> status.getStatus().equals(ThingStatus.OFFLINE)
                        && status.getStatusDetail().equals(ThingStatusDetail.COMMUNICATION_ERROR)));
    }

    @Test
    void getState() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();
        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(response.getContentAsString()).thenReturn("""
                {
                     "@type": "systemState",
                     "systemAvailability": {
                         "@type": "systemAvailabilityState",
                         "available": true,
                         "deleted": false
                     },
                     "armingState": {
                         "@type": "armingState",
                         "state": "SYSTEM_DISARMED",
                         "deleted": false
                     },
                     "alarmState": {
                         "@type": "alarmState",
                         "value": "ALARM_OFF",
                         "incidents": [],
                         "deleted": false
                     },
                     "activeConfigurationProfile": {
                         "@type": "activeConfigurationProfile",
                         "deleted": false
                     },
                     "securityGapState": {
                         "@type": "securityGapState",
                         "securityGaps": [],
                         "deleted": false
                     },
                     "deleted": false
                 }\
                """);
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
        when(httpClient.getServiceStateUrl(anyString(), anyString(), any())).thenCallRealMethod();
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
        when(response.getContentAsString()).thenReturn("""
                {
                    "@type":"DeviceServiceData",
                    "path":"/devices/hdm:ZigBee:000d6f0004b93361/services/BatteryLevel",
                    "id":"BatteryLevel",
                    "deviceId":"hdm:ZigBee:000d6f0004b93361",
                    "faults":{\s
                        "entries":[
                          {
                            "type":"LOW_BATTERY",
                            "category":"WARNING"
                          }
                        ]
                    }
                }\
                """);
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
        when(httpClient.getServiceStateUrl(anyString(), anyString(), any())).thenCallRealMethod();

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

    @Test
    void getUserStateInfo() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();
        String stateId = UUID.randomUUID().toString();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(request.send()).thenReturn(response);
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);
        when(httpClient.sendRequest(same(request), same(UserDefinedState.class), any(), any()))
                .thenReturn(UserDefinedStateTest.createTestState(stateId));

        UserDefinedState userState = fixture.getUserStateInfo(stateId);
        assertEquals(stateId, userState.getId());
    }

    @Test
    void getUserStateInfoErrorCases()
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

        String stateId = "abcdef";
        when(httpClient.sendRequest(same(request), same(UserDefinedState.class), any(),
                errorResponseHandlerCaptor.capture())).thenReturn(UserDefinedStateTest.createTestState(stateId));

        fixture.getUserStateInfo(stateId);

        BiFunction<Integer, String, BoschSHCException> errorResponseHandler = errorResponseHandlerCaptor.getValue();
        Exception e = errorResponseHandler.apply(500,
                "{\"@type\":\"JsonRestExceptionResponseEntity\",\"errorCode\": \"testErrorCode\",\"statusCode\": 500}");
        assertEquals(
                "Request for info of user-defined state abcdef failed with status code 500 and error code testErrorCode",
                e.getMessage());

        e = errorResponseHandler.apply(404,
                "{\"@type\":\"JsonRestExceptionResponseEntity\",\"errorCode\": \"ENTITY_NOT_FOUND\",\"statusCode\": 404}");
        assertNotNull(e);

        e = errorResponseHandler.apply(500, "");
        assertEquals("Request for info of user-defined state abcdef failed with status code 500", e.getMessage());
    }

    @Test
    void getUserStates() throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();
        String stateId = UUID.randomUUID().toString();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(200);
        when(request.send()).thenReturn(response);
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);
        when(response.getContentAsString()).thenReturn(
                GsonUtils.DEFAULT_GSON_INSTANCE.toJson(List.of(UserDefinedStateTest.createTestState(stateId))));

        List<UserDefinedState> userStates = fixture.getUserStates();
        assertEquals(1, userStates.size());
    }

    @Test
    void getUserStatesReturnsEmptyListIfRequestNotSuccessful()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(401);
        when(request.send()).thenReturn(response);
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);

        List<UserDefinedState> userStates = fixture.getUserStates();
        assertTrue(userStates.isEmpty());
    }

    @Test
    void getUserStatesReturnsEmptyListIfExceptionHappened()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        when(httpClient.getBoschSmartHomeUrl(anyString())).thenCallRealMethod();
        when(httpClient.getBoschShcUrl(anyString())).thenCallRealMethod();

        Request request = mock(Request.class);
        when(request.header(anyString(), anyString())).thenReturn(request);
        ContentResponse response = mock(ContentResponse.class);
        when(response.getStatus()).thenReturn(401);
        when(request.send()).thenThrow(new TimeoutException("text exception"));
        when(httpClient.createRequest(anyString(), same(HttpMethod.GET))).thenReturn(request);

        List<UserDefinedState> userStates = fixture.getUserStates();
        assertTrue(userStates.isEmpty());
    }

    @AfterEach
    void afterEach() throws Exception {
        fixture.dispose();
    }

    @Test
    void handleLongPollResultNoDeviceId() {
        List<Thing> things = new ArrayList<Thing>();
        when(thing.getThings()).thenReturn(things);

        Thing thing = mock(Thing.class);
        things.add(thing);

        BoschSHCHandler thingHandler = mock(BoschSHCHandler.class);
        when(thing.getHandler()).thenReturn(thingHandler);

        String json = """
                {
                  "result": [{
                    "path": "/devices/hdm:HomeMaticIP:3014F711A0001916D859A8A9/services/PowerSwitch",
                    "@type": "DeviceServiceData",
                    "id": "PowerSwitch",
                    "state": {
                       "@type": "powerSwitchState",
                       "switchState": "ON"
                    },
                    "deviceId": "hdm:HomeMaticIP:3014F711A0001916D859A8A9"
                  }],
                  "jsonrpc": "2.0"
                }
                """;
        LongPollResult longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(json, LongPollResult.class);
        assertNotNull(longPollResult);

        fixture.handleLongPollResult(longPollResult);

        verify(thingHandler).getBoschID();
        verifyNoMoreInteractions(thingHandler);
    }

    @Test
    void handleLongPollResult() {
        List<Thing> things = new ArrayList<Thing>();
        when(thing.getThings()).thenReturn(things);

        Thing thing = mock(Thing.class);
        things.add(thing);

        BoschSHCHandler thingHandler = mock(BoschSHCHandler.class);
        when(thing.getHandler()).thenReturn(thingHandler);

        when(thingHandler.getBoschID()).thenReturn("hdm:HomeMaticIP:3014F711A0001916D859A8A9");

        String json = """
                {
                  "result": [{
                    "path": "/devices/hdm:HomeMaticIP:3014F711A0001916D859A8A9/services/PowerSwitch",
                    "@type": "DeviceServiceData",
                    "id": "PowerSwitch",
                    "state": {
                       "@type": "powerSwitchState",
                       "switchState": "ON"
                    },
                    "deviceId": "hdm:HomeMaticIP:3014F711A0001916D859A8A9"
                  }],
                  "jsonrpc": "2.0"
                }
                """;
        LongPollResult longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(json, LongPollResult.class);
        assertNotNull(longPollResult);

        fixture.handleLongPollResult(longPollResult);

        verify(thingHandler).getBoschID();

        JsonElement expectedState = JsonParser.parseString("""
                {
                    "@type": "powerSwitchState",
                    "switchState": "ON"
                }
                """);

        verify(thingHandler).processUpdate("PowerSwitch", expectedState);
    }

    @Test
    void handleLongPollResultHandleChildUpdate() {
        List<Thing> things = new ArrayList<Thing>();
        when(thing.getThings()).thenReturn(things);

        Thing thing = mock(Thing.class);
        things.add(thing);

        BoschSHCHandler thingHandler = mock(BoschSHCHandler.class);
        when(thing.getHandler()).thenReturn(thingHandler);

        when(thingHandler.getBoschID()).thenReturn("hdm:ZigBee:70ac08fffefead2d");

        String json = """
                {
                  "result": [{
                    "path": "/devices/hdm:ZigBee:70ac08fffefead2d#3/services/PowerSwitch",
                    "@type": "DeviceServiceData",
                    "id": "PowerSwitch",
                    "state": {
                       "@type": "powerSwitchState",
                       "switchState": "ON"
                    },
                    "deviceId": "hdm:ZigBee:70ac08fffefead2d#3"
                  }],
                  "jsonrpc": "2.0"
                }
                """;
        LongPollResult longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(json, LongPollResult.class);
        assertNotNull(longPollResult);

        fixture.handleLongPollResult(longPollResult);

        verify(thingHandler).getBoschID();

        JsonElement expectedState = JsonParser.parseString("""
                {
                    "@type": "powerSwitchState",
                    "switchState": "ON"
                }
                """);

        verify(thingHandler).processChildUpdate("hdm:ZigBee:70ac08fffefead2d#3", "PowerSwitch", expectedState);
    }

    @Test
    void handleLongPollResultHandleMessage() {
        List<Thing> things = new ArrayList<Thing>();
        when(thing.getThings()).thenReturn(things);

        Thing thing = mock(Thing.class);
        things.add(thing);

        BoschSHCHandler thingHandler = mock(BoschSHCHandler.class);
        when(thing.getHandler()).thenReturn(thingHandler);

        when(thingHandler.getBoschID()).thenReturn("hdm:ZigBee:5cc7c1fffe1f7967");

        String json = """
                {
                    "result": [{
                        "sourceId": "hdm:ZigBee:5cc7c1fffe1f7967",
                        "sourceType": "DEVICE",
                        "@type": "message",
                        "flags": [],
                        "messageCode": {
                            "name": "TILT_DETECTED",
                            "category": "WARNING"
                        },
                        "location": "Kitchen",
                        "arguments": {
                            "deviceModel": "WLS"
                        },
                        "id": "3499a60e-45b5-4c29-ae1a-202c2182970c",
                        "sourceName": "Bosch_water_detector_1",
                        "timestamp": 1714375556426
                    }],
                    "jsonrpc": "2.0"
                }
                """;
        LongPollResult longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(json, LongPollResult.class);
        assertNotNull(longPollResult);

        fixture.handleLongPollResult(longPollResult);

        Message expectedMessage = (Message) longPollResult.result.get(0);

        verify(thingHandler).processMessage(expectedMessage);
    }

    @Test
    void handleLongPollResultScenarioTriggered() {
        Channel channel = mock(Channel.class);
        when(thing.getChannel(BoschSHCBindingConstants.CHANNEL_SCENARIO_TRIGGERED)).thenReturn(channel);
        when(thingHandlerCallback.isChannelLinked(any())).thenReturn(true);

        String json = """
                {
                  "result": [{
                    "@type": "scenarioTriggered",
                    "name": "My Scenario",
                    "id": "509bd737-eed0-40b7-8caa-e8686a714399",
                    "lastTimeTriggered": "1693758693032"
                  }],
                  "jsonrpc": "2.0"
                }
                """;
        LongPollResult longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(json, LongPollResult.class);
        assertNotNull(longPollResult);

        fixture.handleLongPollResult(longPollResult);

        verify(thingHandlerCallback).stateUpdated(any(), eq(new StringType("My Scenario")));
    }

    @Test
    void handleLongPollResultUserDefinedState() {
        List<Thing> things = new ArrayList<Thing>();
        when(thing.getThings()).thenReturn(things);

        Thing thing = mock(Thing.class);
        things.add(thing);

        BoschSHCHandler thingHandler = mock(BoschSHCHandler.class);
        when(thing.getHandler()).thenReturn(thingHandler);

        when(thingHandler.getBoschID()).thenReturn("3d8023d6-69ca-4e79-89dd-7090295cefbf");

        String json = """
                {
                    "result": [{
                        "deleted": false,
                        "@type": "userDefinedState",
                        "name": "Test State",
                        "id": "3d8023d6-69ca-4e79-89dd-7090295cefbf",
                        "state": true
                    }],
                    "jsonrpc": "2.0"
                }
                """;
        LongPollResult longPollResult = GsonUtils.DEFAULT_GSON_INSTANCE.fromJson(json, LongPollResult.class);
        assertNotNull(longPollResult);

        fixture.handleLongPollResult(longPollResult);

        JsonElement expectedState = new JsonPrimitive(true);

        verify(thingHandler).processUpdate("3d8023d6-69ca-4e79-89dd-7090295cefbf", expectedState);
    }

    @Test
    void handleLongPollFailure() {
        Throwable e = new RuntimeException("Test exception");
        fixture.handleLongPollFailure(e);

        ThingStatusInfo expectedStatus = ThingStatusInfoBuilder
                .create(ThingStatus.UNKNOWN, ThingStatusDetail.UNKNOWN.NONE).build();
        verify(thingHandlerCallback).statusUpdated(thing, expectedStatus);
    }

    @Test
    void getDevices() throws InterruptedException, TimeoutException, ExecutionException {
        Request request = mock(Request.class);
        when(httpClient.createRequest(any(), eq(HttpMethod.GET))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(200);
        String devicesJson = """
                [
                    {
                        "@type": "device",
                        "rootDeviceId": "64-da-a0-3e-81-0c",
                        "id": "hdm:ZigBee:0c4314fffea15de7",
                        "deviceServiceIds": [
                            "CommunicationQuality",
                            "PowerMeter",
                            "PowerSwitch",
                            "PowerSwitchConfiguration",
                            "PowerSwitchProgram"
                        ],
                        "manufacturer": "BOSCH",
                        "roomId": "hz_1",
                        "deviceModel": "PLUG_COMPACT",
                        "serial": "0C4314FFFE802BE2",
                        "profile": "LIGHT",
                        "iconId": "icon_plug_lamp_table",
                        "name": "My Lamp Plug",
                        "status": "AVAILABLE",
                        "childDeviceIds": [],
                        "supportedProfiles": [
                            "LIGHT",
                            "GENERIC",
                            "HEATING_RCC"
                        ]
                    },
                    {
                        "@type": "device",
                        "rootDeviceId": "64-da-a0-3e-81-0c",
                        "id": "hdm:ZigBee:000d6f0012f13bfa",
                        "deviceServiceIds": [
                            "LatestMotion",
                            "CommunicationQuality",
                            "WalkTest",
                            "BatteryLevel",
                            "MultiLevelSensor",
                            "DeviceDefect"
                        ],
                        "manufacturer": "BOSCH",
                        "roomId": "hz_5",
                        "deviceModel": "MD",
                        "serial": "000D6F0012F0da96",
                        "profile": "GENERIC",
                        "name": "My Motion Detector",
                        "status": "AVAILABLE",
                        "childDeviceIds": [],
                        "supportedProfiles": []
                    }
                ]
                """;
        when(contentResponse.getContentAsString()).thenReturn(devicesJson);

        List<Device> devices = fixture.getDevices();

        assertEquals(2, devices.size());

        Device plugDevice = devices.get(0);
        assertEquals("hdm:ZigBee:0c4314fffea15de7", plugDevice.id);
        assertEquals(5, plugDevice.deviceServiceIds.size());
        assertEquals(0, plugDevice.childDeviceIds.size());

        Device motionDetectorDevice = devices.get(1);
        assertEquals("hdm:ZigBee:000d6f0012f13bfa", motionDetectorDevice.id);
        assertEquals(6, motionDetectorDevice.deviceServiceIds.size());
        assertEquals(0, motionDetectorDevice.childDeviceIds.size());
    }

    @Test
    void getDevicesErrorRestResponse() throws InterruptedException, TimeoutException, ExecutionException {
        Request request = mock(Request.class);
        when(httpClient.createRequest(any(), eq(HttpMethod.GET))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(400); // bad request

        List<Device> devices = fixture.getDevices();

        assertThat(devices, hasSize(0));
    }

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getExecutionAndTimeoutExceptionArguments()")
    void getDevicesHandleExceptions() throws InterruptedException, TimeoutException, ExecutionException {
        Request request = mock(Request.class);
        when(httpClient.createRequest(any(), eq(HttpMethod.GET))).thenReturn(request);
        when(request.send()).thenThrow(new ExecutionException(new RuntimeException("Test Exception")));

        List<Device> devices = fixture.getDevices();

        assertThat(devices, hasSize(0));
    }

    @Test
    void getRooms() throws InterruptedException, TimeoutException, ExecutionException {
        Request request = mock(Request.class);
        when(httpClient.createRequest(any(), eq(HttpMethod.GET))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(200);
        String roomsJson = """
                [
                    {
                        "@type": "room",
                        "id": "hz_1",
                        "iconId": "icon_room_living_room",
                        "name": "Living Room"
                    },
                    {
                        "@type": "room",
                        "id": "hz_2",
                        "iconId": "icon_room_dining_room",
                        "name": "Dining Room"
                    }
                ]
                """;
        when(contentResponse.getContentAsString()).thenReturn(roomsJson);

        List<Room> rooms = fixture.getRooms();

        assertEquals(2, rooms.size());

        Room livingRoom = rooms.get(0);
        assertEquals("hz_1", livingRoom.id);
        assertEquals("Living Room", livingRoom.name);

        Room diningRoom = rooms.get(1);
        assertEquals("hz_2", diningRoom.id);
        assertEquals("Dining Room", diningRoom.name);
    }

    @Test
    void getRoomsErrorRestResponse() throws InterruptedException, TimeoutException, ExecutionException {
        Request request = mock(Request.class);
        when(httpClient.createRequest(any(), eq(HttpMethod.GET))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(400); // bad request

        List<Room> rooms = fixture.getRooms();

        assertThat(rooms, hasSize(0));
    }

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getExecutionAndTimeoutExceptionArguments()")
    void getRoomsHandleExceptions() throws InterruptedException, TimeoutException, ExecutionException {
        Request request = mock(Request.class);
        when(httpClient.createRequest(any(), eq(HttpMethod.GET))).thenReturn(request);
        when(request.send()).thenThrow(new ExecutionException(new RuntimeException("Test Exception")));

        List<Room> rooms = fixture.getRooms();

        assertThat(rooms, hasSize(0));
    }

    @Test
    void getServices() {
        assertTrue(fixture.getServices().contains(ThingDiscoveryService.class));
    }

    @Test
    void handleCommandIrrelevantChannel() {
        ChannelUID channelUID = mock(ChannelUID.class);
        when(channelUID.getId()).thenReturn(BoschSHCBindingConstants.CHANNEL_POWER_SWITCH);

        fixture.handleCommand(channelUID, OnOffType.ON);

        verifyNoInteractions(httpClient);
    }

    @Test
    void handleCommandTriggerScenario()
            throws InterruptedException, TimeoutException, ExecutionException, BoschSHCException {
        ChannelUID channelUID = mock(ChannelUID.class);
        when(channelUID.getId()).thenReturn(BoschSHCBindingConstants.CHANNEL_TRIGGER_SCENARIO);

        // required to prevent NPE
        when(httpClient.sendRequest(any(), eq(Scenario[].class), any(), any())).thenReturn(new Scenario[] {});

        fixture.handleCommand(channelUID, OnOffType.ON);

        verify(httpClient).sendRequest(any(), eq(Scenario[].class), any(), any());
    }

    @Test
    void registerDiscoveryListener() {
        ThingDiscoveryService listener = mock(ThingDiscoveryService.class);
        assertTrue(fixture.registerDiscoveryListener(listener));
        assertFalse(fixture.registerDiscoveryListener(listener));
    }

    @Test
    void unregisterDiscoveryListener() {
        assertFalse(fixture.unregisterDiscoveryListener());
        fixture.registerDiscoveryListener(mock(ThingDiscoveryService.class));
        assertTrue(fixture.unregisterDiscoveryListener());
    }

    @Test
    void initializeNoIpAddress() {
        bridgeConfiguration.setProperties(new HashMap<String, Object>());

        fixture.initialize();

        ThingStatusInfo expectedStatus = ThingStatusInfoBuilder
                .create(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR)
                .withDescription("@text/offline.conf-error-empty-ip").build();
        verify(thingHandlerCallback).statusUpdated(thing, expectedStatus);
    }

    @Test
    void initializeNoPassword() {
        HashMap<String, Object> properties = new HashMap<String, Object>();
        properties.put("ipAddress", "localhost");
        bridgeConfiguration.setProperties(properties);

        fixture.initialize();

        ThingStatusInfo expectedStatus = ThingStatusInfoBuilder
                .create(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR)
                .withDescription("@text/offline.conf-error-empty-password").build();
        verify(thingHandlerCallback).statusUpdated(thing, expectedStatus);
    }

    @Test
    void checkBridgeAccess() throws InterruptedException, TimeoutException, ExecutionException {
        Request request = mock(Request.class);
        when(httpClient.createRequest(any(), eq(HttpMethod.GET))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(200);

        assertTrue(fixture.checkBridgeAccess());
    }

    @Test
    void checkBridgeAccessRestResponseError() throws InterruptedException, TimeoutException, ExecutionException {
        Request request = mock(Request.class);
        when(httpClient.createRequest(any(), eq(HttpMethod.GET))).thenReturn(request);
        ContentResponse contentResponse = mock(ContentResponse.class);
        when(request.send()).thenReturn(contentResponse);
        when(contentResponse.getStatus()).thenReturn(400);

        assertFalse(fixture.checkBridgeAccess());
    }

    @ParameterizedTest
    @MethodSource("org.openhab.binding.boschshc.internal.tests.common.CommonTestUtils#getExecutionAndTimeoutExceptionArguments()")
    void checkBridgeAccessRestException(Exception e) throws InterruptedException, TimeoutException, ExecutionException {
        Request request = mock(Request.class);
        when(httpClient.createRequest(any(), eq(HttpMethod.GET))).thenReturn(request);
        when(request.send()).thenThrow(e);

        assertFalse(fixture.checkBridgeAccess());
    }

    @Test
    void getPublicInformation() throws InterruptedException, BoschSHCException, ExecutionException, TimeoutException {
        fixture.getPublicInformation();

        verify(httpClient).createRequest(any(), same(HttpMethod.GET));
        verify(httpClient).sendRequest(any(), same(PublicInformation.class), any(), isNull());
    }
}
