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
package org.openhab.binding.boschshc.internal.discovery;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;

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
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.config.discovery.DiscoveryResult;
import org.openhab.core.config.discovery.DiscoveryService;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.ThingUID;

/**
 * ThingDiscoveryService Tester.
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
    void testStartScan() throws InterruptedException {
        mockBridgeCalls();

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
        assertThat(String.valueOf(result.getProperties().get("Location")), is("TestRoom"));
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
}
