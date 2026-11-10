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
package org.openhab.binding.tailwind.internal;

import java.util.Locale;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Converts the device id of a TailWind controller.
 *
 * A controller reports its MAC address as a device id such as {@code _8_d1_f9_12_2_ec_}: the six bytes in hexadecimal
 * without leading zeros, separated and surrounded by underscores.
 *
 * @author Dave J. Schoepel - Initial contribution
 * @author Christopher Smith - Extracted from the handler and the discovery participant
 */
@NonNullByDefault
public final class TailwindDeviceId {
    private static final Pattern MAC = Pattern.compile("[0-9a-f]{12}");

    private TailwindDeviceId() {
    }

    /**
     * Returns the MAC address as 12 lower-case hexadecimal digits, such as {@code 08d1f91202ec}, or null if the device
     * id is not one.
     */
    public static @Nullable String toMacAddress(@Nullable String deviceId) {
        if (deviceId == null) {
            return null;
        }
        StringBuilder mac = new StringBuilder();
        for (String part : deviceId.split("_")) {
            if (part.length() == 1) {
                mac.append('0');
            }
            mac.append(part.toLowerCase(Locale.ROOT));
        }
        return MAC.matcher(mac).matches() ? mac.toString() : null;
    }
}
