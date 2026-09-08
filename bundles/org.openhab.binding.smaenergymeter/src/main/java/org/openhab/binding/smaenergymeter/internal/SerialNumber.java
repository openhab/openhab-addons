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
 * @author Osman Basha - Initial contribution
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

        try {
            if (isHexValue(trimmed)) {
                return Integer.toUnsignedString(Integer.parseUnsignedInt(removeHexPrefix(trimmed), 16));
            }
            return Integer.toUnsignedString(Integer.parseUnsignedInt(trimmed, 10));
        } catch (NumberFormatException e) {
            return trimmed;
        }
    }

    private static boolean isHexValue(String value) {
        String candidate = removeHexPrefix(value);
        return candidate.length() == 8 && candidate.chars().allMatch(SerialNumber::isHexDigit);
    }

    private static String removeHexPrefix(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        if (normalized.startsWith("0x")) {
            return value.substring(2);
        }
        return value;
    }

    private static boolean isHexDigit(int character) {
        return character >= '0' && character <= '9' || character >= 'a' && character <= 'f'
                || character >= 'A' && character <= 'F';
    }
}
