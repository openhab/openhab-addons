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
package org.openhab.binding.ecobee.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link StringUtils} class defines static string related methods
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class StringUtils {

    public static String capitalizeWords(@Nullable String input) {
        String output = "";
        if (input != null) {
            String[] splitted = input.split("\\s+");
            String[] processed = new String[splitted.length];
            for (int wordIndex = 0; wordIndex < splitted.length; wordIndex++) {
                if (splitted[wordIndex].length() > 1) {
                    processed[wordIndex] = splitted[wordIndex].substring(0, 1).toUpperCase()
                            + splitted[wordIndex].substring(1);
                } else {
                    processed[wordIndex] = splitted[wordIndex].toUpperCase();
                }
            }
            output = String.join(" ", processed);
        }
        return output;
    }
}
