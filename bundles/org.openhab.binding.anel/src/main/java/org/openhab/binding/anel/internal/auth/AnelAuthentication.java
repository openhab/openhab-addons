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
package org.openhab.binding.anel.internal.auth;

import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * This class determines the authentication method from a status response of an ANEL device.
 *
 * @author Patrick Koenemann - Initial contribution
 */
@NonNullByDefault
public class AnelAuthentication {

    public enum AuthMethod {
        PLAIN,
        BASE64,
        XORBASE64;

        private static final Pattern NAME_AND_FIRMWARE_PATTERN = Pattern.compile(":NET-PWRCTRL_0?(\\d+\\.\\d)");
        private static final Pattern LAST_SEGMENT_FIRMWARE_PATTERN = Pattern.compile(":(\\d+\\.\\d)$");

        private static final String MIN_FIRMWARE_BASE64 = "6.0";
        private static final String MIN_FIRMWARE_XOR_BASE64 = "6.1";

        public static AuthMethod of(String status) {
            if (status.isEmpty()) {
                return PLAIN; // fallback
            }
            if (status.trim().endsWith(":xor") || status.contains(":xor:")) {
                return XORBASE64;
            }
            final String firmwareVersion = getFirmwareVersion(status);
            if (firmwareVersion == null) {
                return PLAIN;
            }
            if (firmwareVersion.compareTo(MIN_FIRMWARE_XOR_BASE64) >= 0) {
                return XORBASE64; // >= 6.1
            }
            if (firmwareVersion.compareTo(MIN_FIRMWARE_BASE64) >= 0) {
                return BASE64; // exactly 6.0
            }
            return PLAIN; // fallback
        }

        private static @Nullable String getFirmwareVersion(String fullStatusStringOrFirmwareVersion) {
            final Matcher matcher1 = NAME_AND_FIRMWARE_PATTERN.matcher(fullStatusStringOrFirmwareVersion);
            if (matcher1.find()) {
                return matcher1.group(1);
            }
            final Matcher matcher2 = LAST_SEGMENT_FIRMWARE_PATTERN.matcher(fullStatusStringOrFirmwareVersion.trim());
            if (matcher2.find()) {
                return matcher2.group(1);
            }
            return null;
        }
    }

    public static String getUserPasswordString(@Nullable String user, @Nullable String password,
            @Nullable AuthMethod authMethod) {
        final String userPassword = (user == null ? "" : user) + (password == null ? "" : password);
        if (authMethod == null || authMethod == AuthMethod.PLAIN) {
            return userPassword;
        }

        if (authMethod == AuthMethod.BASE64 || password == null || password.isEmpty()) {
            return Base64.getEncoder().encodeToString(userPassword.getBytes());
        }

        if (authMethod == AuthMethod.XORBASE64) {
            final StringBuilder result = new StringBuilder();

            // XOR
            for (int c = 0; c < userPassword.length(); c++) {
                result.append((char) (userPassword.charAt(c) ^ password.charAt(c % password.length())));
            }

            return Base64.getEncoder().encodeToString(result.toString().getBytes());
        }

        throw new UnsupportedOperationException("Unknown auth method: " + authMethod);
    }
}
