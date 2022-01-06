/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.sinope.internal.util;

import java.util.Arrays;

/**
 * The Class ByteUtil.
 *
 * @author Pascal Larin - Initial contribution
 */
public class ByteUtil {

    /**
     * Reverse.
     *
     * @param array to reverse
     * @return the reserved in byte[]
     */
    public static byte[] reverse(byte[] array) {
        if (array == null) {
            return null;
        }
        byte[] r = Arrays.copyOf(array, array.length);
        int i = 0;
        int j = r.length - 1;
        byte tmp;
        while (j > i) {
            tmp = r[j];
            r[j] = r[i];
            r[i] = tmp;
            j--;
            i++;
        }
        return r;
    }

    /**
     * To string.
     *
     * @param buf the buf
     * @return the string
     */
    public static String toString(byte[] buf) {
        StringBuilder sb = new StringBuilder();
        for (byte b : buf) {
            sb.append(String.format("0x%02X ", b));
        }
        return sb.toString();
    }
}
