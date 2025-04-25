/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.boschshc.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openhab.binding.boschshc.internal.devices.BoschSHCBindingConstants;
import org.openhab.binding.boschshc.internal.devices.bridge.BridgeHandler;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Device;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.Room;
import org.openhab.binding.boschshc.internal.devices.bridge.dto.UserDefinedState;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * Unit tests for {@link ThingDiscoveryService}.
 *
 * @author Gerd Zanker - Initial contribution
 */
@ExtendWith(MockitoExtension.class)
@NonNullByDefault
class ThingDiscoveryServiceTest {

    private @NonNullByDefault({}) ThingDiscoveryService fixture;

    private @Mock @NonNullByDefault({}) BridgeHandler bridgeHandler;
    private @Mock @NonNullByDefault({}) DiscoveryListener discoveryListener;
    private @Captor @NonNullByDefault({}) ArgumentCaptor<DiscoveryService> discoveryServiceCaptor;
    private @Captor @NonNullByDefault({}) ArgumentCaptor<DiscoveryResult> discoveryResultCaptor;

    @BeforeEach
    void beforeEach() {
        fixture = new ThingDiscoveryService();
        fixture.addDiscoveryListener(discoveryListener);
        fixture.setThingHandler(bridgeHandler);
    }

    private void mockBridgeCalls() {
        // Set the Mock Bridge as the ThingHandler
        ThingUID bridgeUID = new ThingUID(BoschSHCBindingConstants.THING_TYPE_SHC, "testSHC");
        Bridge mockBridge = mock(Bridge.class);
        when(mockBridge.getUID()).thenReturn(bridgeUID);
        when(bridgeHandler.getThing()).thenReturn(mockBridge);
    }

    @Test
    void initialize() {
        fixture.initialize();
        verify(bridgeHandler).registerDiscoveryListener(fixture);
    }

    @Test
    void testStartScan() throws InterruptedException {
        mockBridgeCalls();

        Device device = new Device();
        device.name = "My Smart Plug";
        device.deviceModel = "PSM";
        device.id = "hdm:HomeMaticIP:3014F711A00004953859F31B";
        device.deviceServiceIds = List.of("PowerMeter", "PowerSwitch", "PowerSwitchProgram", "Routing");

        List<Device> devices = new ArrayList<>();
        devices.add(device);
        when(bridgeHandler.getDevices()).thenReturn(devices);

        UserDefinedState userDefinedState = new UserDefinedState();
        userDefinedState.setName("My State");
        userDefinedState.setId("23d34fa6-382a-444d-8aae-89c706e22158");
        userDefinedState.setState(true);

        List<UserDefinedState> userDefinedStates = new ArrayList<>();
        userDefinedStates.add(userDefinedState);
        when(bridgeHandler.getUserStates()).thenReturn(userDefinedStates);

        fixture.activate();
        fixture.startScan();

        verify(bridgeHandler).getRooms();
        verify(bridgeHandler).getDevices();

        fixture.stopScan();
        fixture.deactivate();
    }

    @Test
    void testStartScanWithoutBridgeHandler() {
        mockBridgeCalls();

        // No fixture.setThingHandler(bridgeHandler);
        fixture.activate();
        fixture.startScan();

        // bridgeHandler not called, just no exception expected
        fixture.stopScan();
        fixture.deactivate();
    }

    @Test
    void testSetGetThingHandler() {
        fixture.setThingHandler(bridgeHandler);
        assertThat(fixture.getThingHandler(), is(bridgeHandler));
    }

    @Test
    void testAddDevices() {
        mockBridgeCalls();

        ArrayList<Device> devices = new ArrayList<>();
        ArrayList<Room> emptyRooms = new ArrayList<>();

        Device device1 = new Device();
        device1.deviceModel = "TWINGUARD";
        device1.id = "testDevice:ID";
        device1.name = "Test Name";
        devices.add(device1);
        Device device2 = new Device();
        device2.deviceModel = "TWINGUARD";
        device2.id = "testDevice:2";
        device2.name = "Second device";
        devices.add(device2);

        verify(discoveryListener, never()).thingDiscovered(any(), any());

        fixture.addDevices(devices, emptyRooms);

        // two calls for the two devices expected
        verify(discoveryListener, times(2)).thingDiscovered(any(), any());
    }

    @Test
    void testAddDevicesWithNoDevices() {
        ArrayList<Device> emptyDevices = new ArrayList<>();
        ArrayList<Room> emptyRooms = new ArrayList<>();

        verify(discoveryListener, never()).thingDiscovered(any(), any());

        fixture.addDevices(emptyDevices, emptyRooms);

        // nothing shall be discovered, but also no exception shall be thrown
        verify(discoveryListener, never()).thingDiscovered(any(), any());
    }

    @Test
    void testAddDevice() {
        mockBridgeCalls();

        Device device = new Device();
        device.deviceModel = "TWINGUARD";
        device.id = "testDevice:ID";
        device.name = "Test Name";
        fixture.addDevice(device, "TestRoom");

        verify(discoveryListener).thingDiscovered(discoveryServiceCaptor.capture(), discoveryResultCaptor.capture());

        assertThat(discoveryServiceCaptor.getValue().getClass(), is(ThingDiscoveryService.class));
        DiscoveryResult result = discoveryResultCaptor.getValue();
        assertThat(result.getBindingId(), is(BoschSHCBindingConstants.BINDING_ID));
        assertThat(result.getThingTypeUID(), is(BoschSHCBindingConstants.THING_TYPE_TWINGUARD));
        assertThat(result.getThingUID().getId(), is("testDevice_ID"));
        assertThat(result.getBridgeUID().getId(), is("testSHC"));
        assertThat(result.getLabel(), is("Test Name"));
        assertThat(String.valueOf(result.getProperties().get(BoschSHCBindingConstants.PROPERTY_LOCATION)),
                is("TestRoom"));
    }

    @Test
    void testAddDeviceWithNiceNameAndAppendedRoomName() {
        assertDeviceNiceName("-RoomClimateControl-", "TestRoom", "Room Climate Control TestRoom");
    }

    @Test
    void testAddDeviceWithNiceNameWithEmtpyRoomName() {
        assertDeviceNiceName("-RoomClimateControl-", "", "Room Climate Control");
    }

    @Test
    void testAddDeviceWithNiceNameWithoutAppendingRoomName() {
        assertDeviceNiceName("-SmokeDetectionSystem-", "TestRoom", "Smoke Detection System");
    }

    @Test
    void testAddDeviceWithNiceNameWithoutUsualName() {
        assertDeviceNiceName("My other device", "TestRoom", "My other device");
    }

    private void assertDeviceNiceName(String deviceName, String roomName, String expectedNiceName) {
        mockBridgeCalls();

        Device device = new Device();
        device.deviceModel = "TWINGUARD";
        device.id = "testDevice:ID";
        device.name = deviceName;
        fixture.addDevice(device, roomName);
        verify(discoveryListener).thingDiscovered(discoveryServiceCaptor.capture(), discoveryResultCaptor.capture());
        assertThat(discoveryServiceCaptor.getValue().getClass(), is(ThingDiscoveryService.class));
        DiscoveryResult result = discoveryResultCaptor.getValue();
        assertThat(result.getLabel(), is(expectedNiceName));
    }

    @Test
    void testGetRoomForDevice() {
        Device device = new Device();

        ArrayList<Room> rooms = new ArrayList<>();
        Room room1 = new Room();
        room1.id = "r1";
        room1.name = "Room1";
        rooms.add(room1);
        Room room2 = new Room();
        room2.id = "r2";
        room2.name = "Room 2";
        rooms.add(room2);

        device.roomId = "r1";
        assertThat(fixture.getRoomNameForDevice(device, rooms), is("Room1"));

        device.roomId = "r2";
        assertThat(fixture.getRoomNameForDevice(device, rooms), is("Room 2"));

        device.roomId = "unknown";
        assertTrue(fixture.getRoomNameForDevice(device, rooms).isEmpty());
    }

    @Test
    void testGetThingTypeUID() {
        Device device = new Device();

        device.deviceModel = "invalid";
        assertNull(fixture.getThingTypeUID(device));

        // just two spot checks
        device.deviceModel = "BBL";
        assertThat(fixture.getThingTypeUID(device), is(BoschSHCBindingConstants.THING_TYPE_SHUTTER_CONTROL));
        device.deviceModel = "TWINGUARD";
        assertThat(fixture.getThingTypeUID(device), is(BoschSHCBindingConstants.THING_TYPE_TWINGUARD));
    }

    @Test
    void testAddUserDefinedStates() {
        mockBridgeCalls();

        ArrayList<UserDefinedState> userStates = new ArrayList<>();

        UserDefinedState userState1 = new UserDefinedState();
        userState1.setId(UUID.randomUUID().toString());
        userState1.setName("first defined state");
        userState1.setState(true);
        UserDefinedState userState2 = new UserDefinedState();
        userState2.setId(UUID.randomUUID().toString());
        userState2.setName("another defined state");
        userState2.setState(false);
        userStates.add(userState1);
        userStates.add(userState2);

        verify(discoveryListener, never()).thingDiscovered(any(), any());

        fixture.addUserStates(userStates);

        // two calls for the two devices expected
        verify(discoveryListener, times(2)).thingDiscovered(any(), any());
    }

    @Test
    void dispose() {
        Bridge thing = mock(Bridge.class);
        when(thing.getUID()).thenReturn(new ThingUID(BoschSHCBindingConstants.THING_TYPE_SHC, "shc123456"));
        when(bridgeHandler.getThing()).thenReturn(thing);
        fixture.dispose();
        verify(bridgeHandler).unregisterDiscoveryListener();
    }

    @Test
    void getThingTypeUIDLightControl2ChildDevice() {
        Device device = new Device();
        device.deviceModel = ThingDiscoveryService.DEVICE_MODEL_LIGHT_CONTROL_CHILD_DEVICE;

        assertThat(fixture.getThingTypeUID(device), is(nullValue()));
    }
}
