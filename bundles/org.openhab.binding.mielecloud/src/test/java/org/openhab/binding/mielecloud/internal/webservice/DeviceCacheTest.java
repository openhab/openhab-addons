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
package org.openhab.binding.mielecloud.internal.webservice;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.mielecloud.internal.webservice.api.json.Device;
import org.openhab.binding.mielecloud.internal.webservice.api.json.DeviceCollection;

/**
 * @author Björn Lange - Initial contribution
 */
@NonNullByDefault
public class DeviceCacheTest {
    private static final String FIRST_DEVICE_IDENTIFIER = "000124430016";
    private static final String SECOND_DEVICE_IDENTIFIER = "000124430017";
    private static final String THIRD_DEVICE_IDENTIFIER = "400124430017";

    private final Device firstDevice = mock(Device.class);
    private final Device secondDevice = mock(Device.class);
    private final Device thirdDevice = mock(Device.class);

    private final DeviceCache deviceCache = new DeviceCache();

    @Test
    public void testCacheIsEmptyAfterConstruction() {
        // then:
        assertEquals(0, deviceCache.getDeviceIds().size());
    }

    @Test
    public void testReplaceAllDevicesClearsTheCacheAndPutsAllNewDevicesIntoTheCache() {
        // given:
        DeviceCollection deviceCollection = mock(DeviceCollection.class);
        when(deviceCollection.getDeviceIdentifiers())
                .thenReturn(new HashSet<>(Arrays.asList(FIRST_DEVICE_IDENTIFIER, SECOND_DEVICE_IDENTIFIER)));
        when(deviceCollection.getDevice(FIRST_DEVICE_IDENTIFIER)).thenReturn(firstDevice);
        when(deviceCollection.getDevice(SECOND_DEVICE_IDENTIFIER)).thenReturn(secondDevice);

        // when:
        deviceCache.replaceAllDevices(deviceCollection);

        // then:
        assertEquals(new HashSet<>(Arrays.asList(FIRST_DEVICE_IDENTIFIER, SECOND_DEVICE_IDENTIFIER)),
                deviceCache.getDeviceIds());
        assertEquals(firstDevice, deviceCache.getDevice(FIRST_DEVICE_IDENTIFIER).get());
        assertEquals(secondDevice, deviceCache.getDevice(SECOND_DEVICE_IDENTIFIER).get());
    }

    @Test
    public void testReplaceAllDevicesClearsTheCachePriorToCachingThePassedDevices() {
        // given:
        testReplaceAllDevicesClearsTheCacheAndPutsAllNewDevicesIntoTheCache();

        DeviceCollection deviceCollection = mock(DeviceCollection.class);
        when(deviceCollection.getDeviceIdentifiers()).thenReturn(new HashSet<>(Arrays.asList(THIRD_DEVICE_IDENTIFIER)));
        when(deviceCollection.getDevice(THIRD_DEVICE_IDENTIFIER)).thenReturn(thirdDevice);

        // when:
        deviceCache.replaceAllDevices(deviceCollection);

        // then:
        assertEquals(new HashSet<>(Arrays.asList(THIRD_DEVICE_IDENTIFIER)), deviceCache.getDeviceIds());
        assertEquals(thirdDevice, deviceCache.getDevice(THIRD_DEVICE_IDENTIFIER).get());
    }

    @Test
    public void testClearClearsTheCachedDevices() {
        // given:
        testReplaceAllDevicesClearsTheCacheAndPutsAllNewDevicesIntoTheCache();

        // when:
        deviceCache.clear();

        // then:
        assertEquals(0, deviceCache.getDeviceIds().size());
    }
}
