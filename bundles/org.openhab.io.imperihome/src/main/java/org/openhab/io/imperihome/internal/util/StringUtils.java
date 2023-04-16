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
package org.openhab.io.imperihome.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link StringUtils} class defines some static string utility methods
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class StringUtils {

    /**
     * Simple method to create boolean from string.
     * 'true', 'on', 'y', 't' or 'yes' (case insensitive) will return true. Otherwise, false is returned.
     */
    public static boolean toBoolean(@Nullable String input) {
        if (input != null) {
            input = input.toLowerCase();
        }
        return "true".equals(input) || "on".equals(input) || "y".equals(input) || "t".equals(input)
                || "yes".equals(input);
    }

    public static String padLeft(@Nullable String input, int minSize, String padString) {
        if (input == null) {
            input = "";
        }
        return String.format("%" + minSize + "s", input).replace(" ", padString);
    }
}
