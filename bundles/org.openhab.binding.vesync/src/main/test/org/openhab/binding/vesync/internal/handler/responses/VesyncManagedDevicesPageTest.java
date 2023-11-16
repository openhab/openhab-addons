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

    public static final String testGoodSearchResponsePageBody = """
            {
                "traceId": "1634387642",
                "code": 0,
                "msg": "request success",
                "result": {
                    "total": 2,
                    "pageSize": 100,
                    "pageNo": 1,
                    "list": [
                        {
                            "deviceRegion": "EU",
                            "isOwner": true,
                            "authKey": null,
                            "deviceName": "Air Filter",
                            "deviceImg": "https://image.vesync.com/defaultImages/Core_400S_Series/icon_core400s_purifier_160.png",
                            "cid": "cidValue1",
                            "deviceStatus": "on",
                            "connectionStatus": "online",
                            "connectionType": "WiFi+BTOnboarding+BTNotify",
                            "deviceType": "Core400S",
                            "type": "wifi-air",
                            "uuid": "abcdefab-1234-1234-abcd-123498761234",
                            "configModule": "WiFiBTOnboardingNotify_AirPurifier_Core400S_EU",
                            "macID": "ab:cd:ef:12:34:56",
                            "mode": "simModeData",
                            "speed": 4,
                            "extension": {
                                "airQuality": -1,
                                "airQualityLevel": 1,
                                "mode": "auto",
                                "fanSpeedLevel": "1"
                            },
                            "currentFirmVersion": null,
                            "subDeviceNo": "simSubDevice",
                            "subDeviceType": "simSubDeviceType",
                            "deviceFirstSetupTime": "Oct 15, 2021 3:43:02 PM"
                        }
                    ]
                }
            }\
            """;

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
