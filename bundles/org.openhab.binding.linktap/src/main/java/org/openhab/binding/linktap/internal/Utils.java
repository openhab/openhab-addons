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
package org.openhab.binding.linktap.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link Utils} contains static function's for useful functionality.
 *
 * @author David Goodyear - Initial contribution
 */
@NonNullByDefault
public final class Utils {

    private Utils() {
    }

    /**
     * This cleans a string down to characters relevant to a mdns reply
     * '()*+,-./0-9:;<=>?@[\]^_`a-z{|}~ ensuring characters not included
     * in this range are removed.
     *
     * @param chars - The string to be cleansed
     * @return The string with only the relevant characters included
     */
    public static String cleanPrintableChars(final String chars) {
        final StringBuilder stBldr = new StringBuilder(chars.length());
        for (char ch : chars.toCharArray()) {
            final byte chBy = (byte) ch;
            if (chBy >= 32 && chBy <= 126) {
                stBldr.append(ch);
            }
        }
        return stBldr.toString();
    }

    /**
     * Return the localized error message if available otherwise the
     * message. Should they both (either be null or empty) then return the
     * class name of the throwable.
     *
     * @param t - The throwable to extract the data from
     * @return The most representative text for the message
     */
    public static String getMessage(final @Nullable Throwable t) {
        if (t == null) {
            return "?";
        }

        final String localizedMsg = t.getLocalizedMessage();
        if (localizedMsg != null) {
            if (!localizedMsg.isBlank()) {
                return localizedMsg;
            }
        }
        final String msg = t.getMessage();
        if (msg != null) {
            return msg;
        }
        return t.getClass().getName();
    }
}
