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
package org.openhab.binding.sensibo.internal.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link StringUtils} class defines some static string utility methods
 *
 * @author Leo Siepel - Initial contribution
 */
@NonNullByDefault
public class StringUtils {

    public static String capitalizeFully(String input) {
        final String delimiter = "_";
        String capitalizedFully = "";
        for (String str : input.split(delimiter)) {
            String properlyCapitalized = "";
            if (str.length() > 0) {
                properlyCapitalized = str.substring(0, 1).toUpperCase();
            }
            if (str.length() > 1) {
                properlyCapitalized = str.substring(1).toLowerCase();
            }
            capitalizedFully = capitalizedFully + properlyCapitalized;
        }

        return capitalizedFully;
    }

    public static String[] splitByCharacterType(@Nullable String input) {
        if (input == null) {
            return new String[0];
        }
        if (input.isBlank()) {
            return new String[0];
        }
        List<String> cache = new ArrayList<>();
        char[] inputAsCharArray = input.toCharArray();
        int prevType = Character.getType(inputAsCharArray[0]);
        int prevTypeStart = 0;
        for (int i = prevTypeStart + 1; i < inputAsCharArray.length; i++) {
            int curType = Character.getType(inputAsCharArray[i]);
            if (prevType == curType) {
                continue;
            }
            if (curType == Character.LOWERCASE_LETTER && prevType == Character.UPPERCASE_LETTER) {
                int tmpStart = i - 1;
                if (tmpStart != prevTypeStart) {
                    cache.add(new String(inputAsCharArray, prevTypeStart, tmpStart - prevTypeStart));
                    prevTypeStart = tmpStart;
                }
            } else {
                cache.add(new String(inputAsCharArray, prevTypeStart, i - prevTypeStart));
                prevTypeStart = i;
            }
            prevType = curType;
        }
        cache.add(new String(inputAsCharArray, prevTypeStart, inputAsCharArray.length - prevTypeStart));
        return cache.toArray(String[]::new);
    }
}
