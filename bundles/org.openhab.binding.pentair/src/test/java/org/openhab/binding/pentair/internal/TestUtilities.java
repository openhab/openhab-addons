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
package org.openhab.binding.pentair.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * TestUtilities
 *
 * @author Jeff James - Initial contribution
 *
 */
@NonNullByDefault
public class TestUtilities {

    public static byte[] parsehex(String in) {
        String out = in.replaceAll("\\s", "");

        return javax.xml.bind.DatatypeConverter.parseHexBinary(out);
    }

    private static int hexToBin(byte in) {
        if ('0' <= in && in <= '9') {
            return in - '0';
        }
        if ('A' <= in && in <= 'F') {
            return in - 'A' + 10;
        }
        if ('a' <= in && in <= 'f') {
            return in - 'a' + 10;
        }
        return -1;
    }

    public static byte[] parsehex(byte[] in) {
        byte[] out = new byte[in.length / 2 + 1];

        int i = 0;
        int length = 0;
        while (i < in.length) {
            int h = hexToBin(in[i]);
            i++;
            if (h == -1) {
                continue;
            }

            if (i >= in.length) {
                break;
            }
            int l = hexToBin(in[i]);
            i++;
            if (l == -1) {
                continue;
            }

            out[length++] = (byte) (h * 16 + l);
        }
        return out;
    }
}
