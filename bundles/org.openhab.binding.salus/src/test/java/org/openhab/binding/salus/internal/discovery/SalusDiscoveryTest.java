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
package org.openhab.binding.salus.internal.discovery;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import javax.validation.constraints.NotNull;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openhab.binding.salus.internal.handler.CloudApi;
import org.openhab.binding.salus.internal.rest.Device;
import org.openhab.binding.salus.internal.rest.exceptions.SalusApiException;
import org.openhab.core.config.discovery.DiscoveryListener;
import org.openhab.core.thing.ThingUID;

/**
 * @author Martin Grześlowski - Initial contribution
 */
@NonNullByDefault
public class SalusDiscoveryTest {

    @Test
    @DisplayName("Method filters out disconnected devices and adds connected devices as things using addThing method")
    void testFiltersOutDisconnectedDevicesAndAddsConnectedDevicesAsThings() throws Exception {
        // Given
        var cloudApi = mock(CloudApi.class);
        var bridgeUid = new ThingUID("salus", "salus-device", "boo");
        var discoveryService = new SalusDiscovery(cloudApi, bridgeUid);
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
    void testLogsErrorWhenCloudApiThrowsException() throws Exception {
        // Given
        var cloudApi = mock(CloudApi.class);
        var bridgeUid = mock(ThingUID.class);
        var discoveryService = new SalusDiscovery(cloudApi, bridgeUid);

        given(cloudApi.findDevices()).willThrow(new SalusApiException("API error"));

        // When
        discoveryService.startScan();

        // Then
        // no error is thrown, OK
    }

    private Device randomDevice(boolean connected) {
        var random = new Random();
        var map = new HashMap<@NotNull String, @Nullable Object>();
        if (connected) {
            map.put("connection_status", "online");
        }
        return new Device("dsn-" + random.nextInt(), "name-" + random.nextInt(), connected, map);
    }
}
