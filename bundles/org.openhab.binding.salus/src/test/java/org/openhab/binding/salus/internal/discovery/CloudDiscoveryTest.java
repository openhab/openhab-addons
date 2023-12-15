package org.openhab.binding.salus.internal.discovery;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openhab.binding.salus.internal.handler.CloudApi;
import org.openhab.binding.salus.internal.handler.CloudBridgeHandler;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.thing.ThingUID;

public class CloudDiscoveryTest {

    @Test
    @DisplayName("Method filters out disconnected devices and adds connected devices as things using addThing method")
    void test_filters_out_disconnected_devices_and_adds_connected_devices_as_things() {
        // Given
        var cloudApi = mock(CloudApi.class);
        var bridgeHandler = mock(CloudBridgeHandler.class);
        var bridgeUid = new ThingUID("salus", "salus-device", "boo");
        var discoveryService = new CloudDiscovery(bridgeHandler, cloudApi, bridgeUid);
        var discoveryListener = mock(DiscoveryListener.class);
        discoveryService.addDiscoveryListener(discoveryListener);
        var device1 = randomDevice(true);
        var device2 = randomDevice(true);
        var device3 = randomDevice(false);
        var device4 = randomDevice(false);
        var devices = new TreeSet<>(List.of(device1, device2, device3, device4));

        given(cloudApi.findDevices()).willReturn(devices);

        // When
        discoveryService.startScan();

        // Then
        verify(cloudApi).findDevices();
        verify(discoveryListener).thingDiscovered(eq(discoveryService),
                argThat(discoveryResult -> discoveryResult.getLabel().equals(device1.name())));
        verify(discoveryListener).thingDiscovered(eq(discoveryService),
                argThat(discoveryResult -> discoveryResult.getLabel().equals(device2.name())));
        verify(discoveryListener, never()).thingDiscovered(eq(discoveryService),
                argThat(discoveryResult -> discoveryResult.getLabel().equals(device3.name())));
        verify(discoveryListener, never()).thingDiscovered(eq(discoveryService),
                argThat(discoveryResult -> discoveryResult.getLabel().equals(device4.name())));
    }

    @Test
    @DisplayName("Cloud API throws an exception during device retrieval, method logs the error")
    void test_logs_error_when_cloud_api_throws_exception() {
        // Given
        var cloudApi = mock(CloudApi.class);
        var bridgeHandler = mock(CloudBridgeHandler.class);
        var bridgeUid = mock(ThingUID.class);
        var discoveryService = new CloudDiscovery(bridgeHandler, cloudApi, bridgeUid);

        given(cloudApi.findDevices()).willThrow(new RuntimeException("API error"));

        // When
        discoveryService.startScan();

        // Then
        // no error is thrown, OK
    }

    private Device randomDevice(boolean connected) {
        var random = new Random();
        var map = new HashMap<String, Object>();
        if (connected) {
            map.put("connection_status", "online");
        }
        return new Device("dsn-" + random.nextInt(), "name-" + random.nextInt(), map);
    }
}
