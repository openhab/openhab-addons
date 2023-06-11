/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.yeelight.internal.lib.device;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openhab.binding.yeelight.internal.lib.enums.ActiveMode;

/**
 * Unit tests for {@link DeviceBase}
 *
 * @author Viktor Koop - Initial contribution
 */
public class DeviceBaseTest {

    private DeviceBase deviceBase;

    @BeforeEach
    public void setUp() {
        deviceBase = new DeviceBase("myid") {
        };
    }

    @Test
    public void testSetColorTemp() {
        String json = "{\"method\":\"props\",\"params\":{\"ct\":4013}}";
        deviceBase.onNotify(json);

        final DeviceStatus deviceStatus = deviceBase.getDeviceStatus();
        assertEquals(4013, deviceStatus.getCt());
    }

    @Test
    public void testSwitchToNightlightMode() {
        deviceBase.onNotify("{\"method\":\"props\",\"params\":{\"nl_br\":96,\"active_mode\":1,\"active_bright\":96}}");
        final DeviceStatus deviceStatus = deviceBase.getDeviceStatus();

        assertEquals(ActiveMode.MOONLIGHT_MODE, deviceStatus.getActiveMode());
    }

    @Test
    public void testNightlightBrightnessUpdate() {
        String json = "{\"method\":\"props\",\"params\":{\"nl_br\":61,\"active_bright\":61}}";
        deviceBase.onNotify(json);

        final DeviceStatus deviceStatus = deviceBase.getDeviceStatus();
        assertEquals(61, deviceStatus.getBrightness());
    }
}
