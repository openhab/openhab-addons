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
package org.openhab.binding.bluetooth.util;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This is a n string utility class
 *
 * @author Leo Siepel - Initial contribution
 *
 */
@NonNullByDefault
public class StringUtil {

    public static String randomString(int length, String charset) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            int index = (int) (charset.length() * Math.random());
            sb.append(charset.charAt(index));
        }

        return sb.toString();
    }

    public static String randomAlphabetic(int length) {
        return StringUtil.randomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvxyz");
    }

    public static String randomHex(int length) {
        return StringUtil.randomString(length, "0123456789ABCDEF");
    }

    public static String randomAlphanummeric(int length) {
        return StringUtil.randomString(length, "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvxyz");
    }
}
