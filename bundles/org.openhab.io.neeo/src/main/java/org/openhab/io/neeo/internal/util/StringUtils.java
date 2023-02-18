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
package org.openhab.io.neeo.internal.util;

import java.util.ArrayList;
import java.util.Arrays;
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

    public static String[] split(@Nullable String input, String delimiter) {
        if (input == null) {
            return new String[0];
        }
        String[] splitted = input.split(delimiter);
        List<String> list = new ArrayList<String>(Arrays.asList(splitted));
        list.removeAll(Arrays.asList("", null));
        return list.toArray(new String[0]);
    }

    public static String randomString(int length, String charset) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (charset.length() * Math.random());
            sb.append(charset.charAt(index));
        }

        return sb.toString();
    }

    public static String randomAlphanummeric(int length) {
        return StringUtils.randomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz");
    }
}
