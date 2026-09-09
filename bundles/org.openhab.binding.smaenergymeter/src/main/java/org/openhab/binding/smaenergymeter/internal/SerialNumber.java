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
package org.openhab.binding.smaenergymeter.internal;

import java.util.Locale;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Helper methods for working with SMA meter serial numbers.
 *
 * @author Marcel Goerentz - Initial contribution
 */
@NonNullByDefault
public final class SerialNumber {

    private SerialNumber() {
    }

    public static String fromRaw(int rawSerialNumber) {
        return Integer.toUnsignedString(rawSerialNumber);
    }

    public static boolean matches(String configuredValue, String actualDecimalValue) {
        String normalizedConfiguredValue = normalize(configuredValue);
        return !normalizedConfiguredValue.isEmpty() && normalizedConfiguredValue.equals(actualDecimalValue);
    }

    public static String normalize(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        String candidate = removeHexPrefix(trimmed);

        try {
            // If value starts with 0x, it's definitely hexadecimal
            if (trimmed.toLowerCase(Locale.ROOT).startsWith("0x")) {
                return Integer.toUnsignedString(Integer.parseUnsignedInt(candidate, 16));
            }

            // If the value contains hex letters (a-f), it's legacy hexadecimal
            if (candidate.chars().anyMatch(c -> (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return Integer.toUnsignedString(Integer.parseUnsignedInt(candidate, 16));
            }

            // Otherwise, parse as decimal (new format)
            return Integer.toUnsignedString(Integer.parseUnsignedInt(candidate, 10));
        } catch (NumberFormatException e) {
            return trimmed;
        }
    }

    private static String removeHexPrefix(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("0x")) {
            return value.substring(2);
        }
        return value;
    }
}
