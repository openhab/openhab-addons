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
package org.openhab.binding.echonetlite.internal;

import java.nio.ByteBuffer;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class HexUtil {
    public static String hex(ByteBuffer buffer) {
        return hex(buffer, "[", "]", "0x", ",");
    }

    public static String hex(final ByteBuffer buffer, final String stringPrefix, final String stringSuffix,
            final String bytePrefix, final String delimiter) {
        final StringBuilder sb = new StringBuilder();
        sb.append(stringPrefix);
        for (int i = buffer.position(), n = buffer.limit(); i < n; i++) {
            final int b = buffer.get(i) & 0xFF;
            final String prefix = b < 0x10 ? "0" : "";
            sb.append(bytePrefix).append(prefix).append(Integer.toHexString(b)).append(delimiter);
        }
        sb.setLength(sb.length() - delimiter.length());
        sb.append(stringSuffix);

        return sb.toString();
    }

    public static String hex(int[] array, int offset, int length) {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (int i = offset; i < length; i++) {
            final int b = array[i] & 0xFF;
            hex(sb, b);
            sb.append(',');
        }
        sb.setLength(sb.length() - 1);
        sb.append(']');

        return sb.toString();
    }

    private static void hex(final StringBuilder sb, final int b) {
        final String prefix = b < 0x10 ? "0" : "";
        sb.append("0x").append(prefix).append(Integer.toHexString(b));
    }

    public static String hex(final int b) {
        final StringBuilder sb = new StringBuilder();
        hex(sb, b);
        return sb.toString();
    }

    public static String hex(int[] array) {
        return hex(array, 0, array.length);
    }
}
