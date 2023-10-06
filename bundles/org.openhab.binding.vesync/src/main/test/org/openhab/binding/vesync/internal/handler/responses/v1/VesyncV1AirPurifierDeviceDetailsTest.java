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
package org.openhab.binding.vesync.internal.handler.responses.v1;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.responses.VesyncLoginResponse;
import org.openhab.binding.vesync.internal.dto.responses.v1.VesyncV1AirPurifierDeviceDetailsResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * The {@link VesyncV1AirPurifierDeviceDetailsTest} class implements unit test case for {@link VesyncLoginResponse}
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VesyncV1AirPurifierDeviceDetailsTest {

    public static final String testAirPurifierResponseBasedOnCore400S = """
            {
                "code": 0,
                "msg": "request success",
                "traceId": "1634255391",
                "screenStatus": "on1",
                "airQuality": 1,
                "level": 2,
                "mode": "manual",
                "deviceName": "Lounge Air Purifier",
                "currentFirmVersion": "1.0.17",
                "childLock": "off1",
                "deviceStatus": "on2",
                "deviceImg": "https://image.vesync.com/defaultImages/Core_400S_Series/icon_core400s_purifier_160.png",
                "connectionStatus": "online"
            }\
            """;

    @Test
    public void testParseV1AirPurifierDeviceDetailsResponse() {
        VesyncV1AirPurifierDeviceDetailsResponse response = VeSyncConstants.GSON.fromJson(testAirPurifierResponseBasedOnCore400S,
                VesyncV1AirPurifierDeviceDetailsResponse.class);

        if (response != null) {
            assertEquals("on1", response.getScreenStatus());
            assertEquals(1, response.getAirQuality());
            assertEquals(2, response.getLevel());
            assertEquals("manual", response.getMode());
            assertEquals("Lounge Air Purifier", response.getDeviceName());
            assertEquals("1.0.17", response.getCurrentFirmVersion());
            assertEquals("off1", response.getChildLock());
            assertEquals("on2", response.getDeviceStatus());
            assertEquals("https://image.vesync.com/defaultImages/Core_400S_Series/icon_core400s_purifier_160.png", response.getDeviceImgUrl());
            assertEquals("online", response.getConnectionStatus());
        } else {
            fail("GSON returned null");
        }
    }
}
