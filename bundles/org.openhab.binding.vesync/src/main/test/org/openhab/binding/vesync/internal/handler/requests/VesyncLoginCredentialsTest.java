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

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.Test;
import org.openhab.binding.vesync.internal.VeSyncConstants;
import org.openhab.binding.vesync.internal.api.VesyncV2ApiHelper;
import org.openhab.binding.vesync.internal.dto.requests.VesyncLoginCredentials;

/**
 * The {@link VesyncLoginCredentials} class implements unit test case for {@link VesyncLoginResponse}
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public class VesyncLoginCredentialsTest {

    @Test
    public void checkMd5Calculation() {
        assertEquals("577441848f056cd02d4c500b25fdd76a",VesyncV2ApiHelper.calculateMd5("TestHashInPythonLib=+"));
    }

    @Test
    public void checkBaseFieldsExist() {
        String content = VeSyncConstants.GSON.toJson(new VesyncLoginCredentials("username", "passmd5"));

        assertEquals(true, content.contains("\"timeZone\": \"America/New_York\""));
        assertEquals(true, content.contains("\"acceptLanguage\": \"en\""));

        assertEquals(true, content.contains("\"appVersion\": \"2.5.1\""));
        assertEquals(true, content.contains("\"phoneBrand\": \"SM N9005\""));
        assertEquals(true, content.contains("\"phoneOS\": \"Android\""));

        Pattern p = Pattern.compile("\"traceId\": \"\\d+\"");
        Matcher m = p.matcher(content);
        assertEquals(true, m.find());
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
