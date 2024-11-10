/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.nikobus.internal.utils;

import java.util.concurrent.Future;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Utils} class defines commonly used utility functions.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class Utils {
    public static void cancel(@Nullable Future<?> future) {
        if (future != null) {
            future.cancel(true);
        }
    }

    /**
     * Convert bus address to push button's address as seen in Nikobus
     * PC software.
     *
     * @param addressString
     *            String representing a bus Push Button's address.
     * @return Push button's address as seen in Nikobus PC software.
     */
    public static String convertToHumanReadableNikobusAddress(String addressString) {
        try {
            int address = Integer.parseInt(addressString, 16);
            int nikobusAddress = 0;

            for (int i = 0; i < 21; ++i) {
                nikobusAddress = (nikobusAddress << 1) | ((address >> i) & 1);
            }

            nikobusAddress = (nikobusAddress << 1);
            int button = (address >> 21) & 0x07;

            return leftPadWithZeros(Integer.toHexString(nikobusAddress), 6) + ":" + mapButton(button);
        } catch (NumberFormatException e) {
            return "[" + addressString + "]";
        }
    }

    private static String mapButton(int buttonIndex) {
        switch (buttonIndex) {
            case 0:
                return "1";
            case 1:
                return "5";
            case 2:
                return "2";
            case 3:
                return "6";
            case 4:
                return "3";
            case 5:
                return "7";
            case 6:
                return "4";
            case 7:
                return "8";
            default:
                return "?";
        }
    }

    public static String leftPadWithZeros(String text, int size) {
        StringBuilder builder = new StringBuilder(text);
        while (builder.length() < size) {
            builder.insert(0, '0');
        }
        return builder.toString();
    }
}
