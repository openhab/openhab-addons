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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link DeviceServiceData}.
 *
 * @author David Pace - Initial contribution
 *
 */
public class DeviceServiceDataTest {

    private DeviceServiceData fixture;

    @BeforeEach
    void beforeEach() {
        fixture = new DeviceServiceData();
        fixture.deviceId = "64-da-a0-02-14-9b";
    }

    @Test
    public void testToString() {
        assertEquals("64-da-a0-02-14-9b state: DeviceServiceData", fixture.toString());
    }
}
