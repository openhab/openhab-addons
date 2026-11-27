/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.cloud;

import static org.junit.jupiter.api.Assertions.*;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.binding.miio.internal.Utils;

import com.google.gson.Gson;

/**
 * Test case for {@link CloudDeviceDTO}
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class CloudDeviceDTOTest {

    @Test
    public void toStringDoesNotExposeToken() {
        String token = "17a8da0b48bd12902a495c8608eb8a73";
        CloudDeviceDTO device = new Gson().fromJson(
                "{\"did\":\"238569313\",\"token\":\"" + token + "\",\"name\":\"Plug\",\"localip\":\"192.168.3.200\"}",
                CloudDeviceDTO.class);
        assertNotNull(device);
        String text = device.toString();
        assertFalse(text.contains(token));
        assertTrue(text.contains("token: '" + Utils.obfuscateToken(token) + "'"));
        assertTrue(text.contains("Plug"));
        assertTrue(text.contains("238569313"));
        // the token itself is still available for use
        assertEquals(token, device.getToken());
    }
}
