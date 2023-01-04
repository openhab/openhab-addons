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
package org.openhab.binding.vesync.internal.handler.requests;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.api.VesyncV2ApiHelper;
import org.openhab.binding.vesync.internal.dto.requests.VesyncLoginCredentials;
import org.openhab.binding.vesync.internal.dto.requests.VesyncRequestManagedDeviceBypassV2;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The {@link VesyncLoginCredentials} class implements unit test case for {@link VesyncLoginResponse}
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VesyncRequestManagedDeviceBypassV2Test {

    @Test
    public void checkMd5Calculation() {
        assertEquals("577441848f056cd02d4c500b25fdd76a",VesyncV2ApiHelper.calculateMd5("TestHashInPythonLib=+"));
    }

    @Test
    public void checkBaseFieldsExist() {
        String content = VeSyncConstants.GSON.toJson(new VesyncRequestManagedDeviceBypassV2());

        assertEquals(true, content.contains("\"method\": \"bypassV2\""));
        assertEquals(true, content.contains("\"data\": {}"));
    }

    @Test
    public void checkEmptyPayload() {
        final VesyncRequestManagedDeviceBypassV2.EmptyPayload testPaylaod = new VesyncRequestManagedDeviceBypassV2.EmptyPayload();
        final String contentTest1 = VeSyncConstants.GSON.toJson(testPaylaod);
        assertEquals(true, contentTest1.equals("{}"));
    }

    @Test
    public void checkSetLevelPayload() {
        final VesyncRequestManagedDeviceBypassV2.SetLevelPayload testPaylaod = new VesyncRequestManagedDeviceBypassV2.SetLevelPayload(1,"stringval",2);
        final String contentTest1 = VeSyncConstants.GSON.toJson(testPaylaod);
        assertEquals(true, contentTest1.contains("\"id\": 1"));
        assertEquals(true,contentTest1.contains("\"type\": \"stringval\""));
        assertEquals(true,contentTest1.contains("\"level\": 2"));
    }

    @Test
    public void checkSetChildLockPayload() {
        final VesyncRequestManagedDeviceBypassV2.SetChildLock testPaylaod = new VesyncRequestManagedDeviceBypassV2.SetChildLock(false);
        final String contentTest1 = VeSyncConstants.GSON.toJson(testPaylaod);
        assertEquals(true,contentTest1.contains("\"child_lock\": false"));

        testPaylaod.childLock = true;
        final String contentTest2 = VeSyncConstants.GSON.toJson(testPaylaod);
        assertEquals(true,contentTest2.contains("\"child_lock\": true"));
    }

    @Test
    public void checkSetSwitchPayload() {
        final VesyncRequestManagedDeviceBypassV2.SetSwitchPayload testPaylaod = new VesyncRequestManagedDeviceBypassV2.SetSwitchPayload(true,0);
        final String contentTest1 = VeSyncConstants.GSON.toJson(testPaylaod);
        assertEquals(true, contentTest1.contains("\"enabled\": true"));
        assertEquals(true, contentTest1.contains("\"id\": 0"));

        testPaylaod.enabled = false;
        testPaylaod.id = 100;
        final String contentTest2 = VeSyncConstants.GSON.toJson(testPaylaod);
        assertEquals(true, contentTest2.contains("\"enabled\": false"));
        assertEquals(true, contentTest2.contains("\"id\": 100"));
    }

    @Test
    public void checkSetNightLightPayload() {
        final VesyncRequestManagedDeviceBypassV2.SetNightLight testPaylaod = new VesyncRequestManagedDeviceBypassV2.SetNightLight("myValue");
        final String contentTest1 = VeSyncConstants.GSON.toJson(testPaylaod);
        assertEquals(true, contentTest1.contains("\"night_light\": \"myValue\""));
    }

    @Test
    public void checkSetTargetHumidityPayload() {
        final VesyncRequestManagedDeviceBypassV2.SetTargetHumidity test0Paylaod = new VesyncRequestManagedDeviceBypassV2.SetTargetHumidity(0);
        final String contentTest1 = VeSyncConstants.GSON.toJson(test0Paylaod);
        assertEquals(true, contentTest1.contains("\"target_humidity\": 0"));

        final VesyncRequestManagedDeviceBypassV2.SetTargetHumidity test100Paylaod = new VesyncRequestManagedDeviceBypassV2.SetTargetHumidity(100);
        final String contentTest2 = VeSyncConstants.GSON.toJson(test100Paylaod);
        assertEquals(true, contentTest2.contains("\"target_humidity\": 100"));
    }

    @Test
    public void checkSetNightLightBrightnessPayload() {
        final VesyncRequestManagedDeviceBypassV2.SetNightLightBrightness test0Paylaod = new VesyncRequestManagedDeviceBypassV2.SetNightLightBrightness(0);
        final String contentTest1 = VeSyncConstants.GSON.toJson(test0Paylaod);
        assertEquals(true, contentTest1.contains("\"night_light_brightness\": 0"));

        final VesyncRequestManagedDeviceBypassV2.SetNightLightBrightness test100Paylaod = new VesyncRequestManagedDeviceBypassV2.SetNightLightBrightness(100);
        final String contentTest2 = VeSyncConstants.GSON.toJson(test100Paylaod);
        assertEquals(true, contentTest2.contains("\"night_light_brightness\": 100"));
    }

    @Test
    public void checkEnabledPayload() {
        final VesyncRequestManagedDeviceBypassV2.EnabledPayload enabledOn = new VesyncRequestManagedDeviceBypassV2.EnabledPayload(true);
        final String contentTest1 = VeSyncConstants.GSON.toJson(enabledOn);
        assertEquals(true, contentTest1.contains("\"enabled\": true"));

        final VesyncRequestManagedDeviceBypassV2.EnabledPayload enabledOff = new VesyncRequestManagedDeviceBypassV2.EnabledPayload(false);
        final String contentTest2 = VeSyncConstants.GSON.toJson(enabledOff);
        assertEquals(true, contentTest2.contains("\"enabled\": false"));
    }

    @Test
    public void checkLoginMethodJson() {

        String content = VeSyncConstants.GSON.toJson(new VesyncLoginCredentials("username", "passmd5"));

        assertEquals(true, content.contains("\"method\": \"login\""));
        assertEquals(true, content.contains("\"email\": \"username\""));
        assertEquals(true, content.contains("\"password\": \"passmd5\""));
        assertEquals(true, content.contains("\"userType\": \"1\""));
        assertEquals(true, content.contains("\"devToken\": \"\""));
    }
}
