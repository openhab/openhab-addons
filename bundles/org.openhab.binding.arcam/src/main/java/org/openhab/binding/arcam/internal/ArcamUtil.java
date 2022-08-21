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
package org.openhab.binding.arcam.internal;

import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ArcamUtil} class contains some small utility methods
 *
 * @author Joep Admiraal - Initial contribution
 */
@NonNullByDefault
public class ArcamUtil {
    private static final byte[] HEX_ARRAY = "0123456789ABCDEF".getBytes(StandardCharsets.US_ASCII);

    public static byte[] byteListToArray(List<Byte> bytes) {
        byte[] byteArray = new byte[bytes.size()];
        for (int i = 0; i < bytes.size(); i++) {
            byteArray[i] = bytes.get(i);
        }
        return byteArray;
    }

    public static String bytesToHex(List<Byte> bytes) {
        byte[] byteArray = byteListToArray(bytes);
        for (int i = 0; i < bytes.size(); i++) {
            byteArray[i] = bytes.get(i);
        }

        return bytesToHex(byteArray);
    }

    public static String byteListToUTF(List<Byte> bytes) {
        byte[] byteArray = byteListToArray(bytes);

        return new String(byteArray, StandardCharsets.UTF_8);
    }

    public static String bytesToHex(byte[] bytes) {
        byte[] hexChars = new byte[bytes.length * 3];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 3] = 'x';
            hexChars[j * 3 + 1] = HEX_ARRAY[v >>> 4];
            hexChars[j * 3 + 2] = HEX_ARRAY[v & 0x0F];

        }
        return new String(hexChars, StandardCharsets.UTF_8);
    }

    public static String byteToHex(byte b) {
        int v = b & 0xFF;
        byte[] hexChars = new byte[3];
        hexChars[0] = 'x';
        hexChars[1] = HEX_ARRAY[v >>> 4];
        hexChars[2] = HEX_ARRAY[v & 0x0F];
        return new String(hexChars, StandardCharsets.UTF_8);
    }
}
