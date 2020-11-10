/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.withings.internal.api;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.openhab.binding.withings.internal.api.device.DevicesResponse;

/**
 * @author Sven Strohschein - Initial contribution
 */
public class WithingsDataModelTest {

    @Test
    public void testGetDevice() {
        DevicesResponse.Device device = new DevicesResponse.Device("deviceId");

        WithingsDataModel model = new WithingsDataModel(Collections.singletonList(device), Optional.empty());
        assertEquals(Optional.of(device), model.getDevice("deviceId"));
    }

    @Test
    public void testGetDevice_MultipleDevices() {
        DevicesResponse.Device device1 = new DevicesResponse.Device("deviceId1");
        DevicesResponse.Device device2 = new DevicesResponse.Device("deviceId");
        DevicesResponse.Device device3 = new DevicesResponse.Device("deviceId4");

        WithingsDataModel model = new WithingsDataModel(Arrays.asList(device1, device2, device3), Optional.empty());
        assertEquals(Optional.of(device2), model.getDevice("deviceId"));
    }

    @Test
    public void testGetDevice_UnknownDeviceId() {
        DevicesResponse.Device device = new DevicesResponse.Device("deviceId");

        WithingsDataModel model = new WithingsDataModel(Collections.singletonList(device), Optional.empty());
        assertEquals(Optional.empty(), model.getDevice("deviceId2"));
    }

    @Test
    public void testGetDevice_NoDevices() {
        WithingsDataModel model = new WithingsDataModel(Collections.emptyList(), Optional.empty());
        assertEquals(Optional.empty(), model.getDevice("deviceId"));
    }
}
