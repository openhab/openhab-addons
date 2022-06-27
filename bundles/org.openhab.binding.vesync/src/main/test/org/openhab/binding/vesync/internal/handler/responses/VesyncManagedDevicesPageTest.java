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
package org.openhab.binding.vesync.internal.handler.responses;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.dto.responses.VesyncLoginResponse;
import org.openhab.binding.vesync.internal.dto.responses.VesyncManagedDevicesPage;

import static org.junit.jupiter.api.Assertions.*;

/**
 * The {@link VesyncManagedDevicesPageTest} class implements unit test case for {@link VesyncLoginResponse}
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VesyncManagedDevicesPageTest {

    public final static String testGoodSearchResponsePageBody = "{\n" +
            "    \"traceId\": \"1634387642\",\n" +
            "    \"code\": 0,\n" +
            "    \"msg\": \"request success\",\n" +
            "    \"result\": {\n" +
            "        \"total\": 2,\n" +
            "        \"pageSize\": 100,\n" +
            "        \"pageNo\": 1,\n" +
            "        \"list\": [\n" +
            "            {\n" +
            "                \"deviceRegion\": \"EU\",\n" +
            "                \"isOwner\": true,\n" +
            "                \"authKey\": null,\n" +
            "                \"deviceName\": \"Air Filter\",\n" +
            "                \"deviceImg\": \"https://image.vesync.com/defaultImages/Core_400S_Series/icon_core400s_purifier_160.png\",\n" +
            "                \"cid\": \"cidValue1\",\n" +
            "                \"deviceStatus\": \"on\",\n" +
            "                \"connectionStatus\": \"online\",\n" +
            "                \"connectionType\": \"WiFi+BTOnboarding+BTNotify\",\n" +
            "                \"deviceType\": \"Core400S\",\n" +
            "                \"type\": \"wifi-air\",\n" +
            "                \"uuid\": \"abcdefab-1234-1234-abcd-123498761234\",\n" +
            "                \"configModule\": \"WiFiBTOnboardingNotify_AirPurifier_Core400S_EU\",\n" +
            "                \"macID\": \"ab:cd:ef:12:34:56\",\n" +
            "                \"mode\": \"simModeData\",\n" +
            "                \"speed\": 4,\n" +
            "                \"extension\": {\n" +
            "                    \"airQuality\": -1,\n" +
            "                    \"airQualityLevel\": 1,\n" +
            "                    \"mode\": \"auto\",\n" +
            "                    \"fanSpeedLevel\": \"1\"\n" +
            "                },\n" +
            "                \"currentFirmVersion\": null,\n" +
            "                \"subDeviceNo\": \"simSubDevice\",\n" +
            "                \"subDeviceType\": \"simSubDeviceType\",\n" +
            "                \"deviceFirstSetupTime\": \"Oct 15, 2021 3:43:02 PM\"\n" +
            "            }\n" +
            "        ]\n" +
            "    }\n" +
            "}";

    @Test
    public void testParseManagedDevicesSearchGoodResponse() {
        VesyncManagedDevicesPage response = VeSyncConstants.GSON.fromJson(testGoodSearchResponsePageBody,
                VesyncManagedDevicesPage.class);
        if (response != null) {
            assertEquals("1634387642", response.getTraceId());
            assertEquals("1", response.result.getPageNo());
            assertEquals("100", response.result.getPageSize());
            assertEquals("2", response.result.getTotal());
            assertEquals("1", String.valueOf(response.result.list.length));

            assertEquals("EU", response.result.list[0].getDeviceRegion());
            assertEquals("Air Filter", response.result.list[0].getDeviceName());
            assertEquals("https://image.vesync.com/defaultImages/Core_400S_Series/icon_core400s_purifier_160.png", response.result.list[0].getDeviceImg());
            assertEquals("on", response.result.list[0].getDeviceStatus());
            assertEquals("online", response.result.list[0].getConnectionStatus());
            assertEquals("WiFi+BTOnboarding+BTNotify", response.result.list[0].getConnectionType());
            assertEquals("Core400S", response.result.list[0].getDeviceType());
            assertEquals("wifi-air", response.result.list[0].getType());
            assertEquals("abcdefab-1234-1234-abcd-123498761234", response.result.list[0].getUuid());
            assertEquals("WiFiBTOnboardingNotify_AirPurifier_Core400S_EU", response.result.list[0].getConfigModule());
            assertEquals("simModeData",response.result.list[0].getMode());
            assertEquals("simSubDevice", response.result.list[0].getSubDeviceNo());
            assertEquals("simSubDeviceType", response.result.list[0].getSubDeviceType());
            assertEquals( "4", response.result.list[0].getSpeed());
            assertEquals("cidValue1",response.result.list[0].getCid());
            assertEquals("ab:cd:ef:12:34:56", response.result.list[0].getMacId());
        } else {
            fail("GSON returned null");
        }
    }
}
