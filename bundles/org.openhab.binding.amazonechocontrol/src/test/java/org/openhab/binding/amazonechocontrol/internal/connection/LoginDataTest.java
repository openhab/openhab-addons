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
package org.openhab.binding.amazonechocontrol.internal.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.CookieManager;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.junit.jupiter.api.Test;
import org.openhab.core.util.HexUtils;

/**
 * The {@link LoginDataTest} verifies that the device id is always derived from the serial.
 *
 * @author Jens Erat - Initial contribution
 */
@NonNullByDefault
class LoginDataTest {
    private static final String DEVICE_TYPE = "A2IVLV5VM2W81";

    @Test
    void deserializeDerivesDeviceIdFromSerialAndIgnoresStoredValue() {
        String serial = "0123456789ABCDEF0123456789ABCDEF";
        String staleDeviceId = "not-the-derived-device-id";
        String data = String.join("\n", "8", "frc", serial, staleDeviceId, "refresh", "amazon.com",
                "https://www.amazon.com", "https://alexa.amazon.com", "name", "customer", "1234567890", "0") + "\n";

        LoginData loginData = new LoginData(new CookieManager());
        assertTrue(loginData.deserialize(data));

        String expected = HexUtils.bytesToHex((serial + "#" + DEVICE_TYPE).getBytes()).toLowerCase();
        assertEquals(expected, loginData.getDeviceId());
        assertNotEquals(staleDeviceId, loginData.getDeviceId());
    }
}
