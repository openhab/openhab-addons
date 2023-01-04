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

    public final static String testAirPurifierResponseBasedOnCore400S = "{\n" +
            "    \"code\": 0,\n" +
            "    \"msg\": \"request success\",\n" +
            "    \"traceId\": \"1634255391\",\n" +
            "    \"screenStatus\": \"on1\",\n" +
            "    \"airQuality\": 1,\n" +
            "    \"level\": 2,\n" +
            "    \"mode\": \"manual\",\n" +
            "    \"deviceName\": \"Lounge Air Purifier\",\n" +
            "    \"currentFirmVersion\": \"1.0.17\",\n" +
            "    \"childLock\": \"off1\",\n" +
            "    \"deviceStatus\": \"on2\",\n" +
            "    \"deviceImg\": \"https://image.vesync.com/defaultImages/Core_400S_Series/icon_core400s_purifier_160.png\",\n" +
            "    \"connectionStatus\": \"online\"\n" +
            "}";

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
