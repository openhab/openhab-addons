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
package org.openhab.binding.boschshc.internal.devices.bridge.dto;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link Device}.
 *
 * @author David Pace - Initial contribution
 *
 */
@NonNullByDefault
public class DeviceTest {

    public static Device createTestDevice() {
        Device device = new Device();
        device.type = "device";
        device.rootDeviceId = "64-da-a0-02-14-9b";
        device.id = "hdm:HomeMaticIP:3014F711A00004953859F31B";
        device.deviceServiceIds = Collections
                .unmodifiableList(List.of("PowerMeter", "PowerSwitch", "PowerSwitchProgram", "Routing"));
        device.manufacturer = "BOSCH";
        device.roomId = "hz_3";
        device.deviceModel = "PSM";
        device.serial = "3014F711A00004953859F31B";
        device.profile = "GENERIC";
        device.name = "Coffee Machine";
        device.status = "AVAILABLE";
        return device;
    }

    private @NonNullByDefault({}) Device fixture;

    @BeforeEach
    void beforeEach() {
        fixture = createTestDevice();
    }

    @Test
    void testIsValid() {
        assertTrue(Device.isValid(fixture));
    }

    @Test
    public void testToString() {
        assertEquals(
                "Type device; RootDeviceId: 64-da-a0-02-14-9b; Id: hdm:HomeMaticIP:3014F711A00004953859F31B; Device Service Ids: PowerMeter, PowerSwitch, PowerSwitchProgram, Routing; Manufacturer: BOSCH; Room Id: hz_3; Device Model: PSM; Serial: 3014F711A00004953859F31B; Profile: GENERIC; Name: Coffee Machine; Status: AVAILABLE; Child Device Ids: null ",
                fixture.toString());
    }
}
