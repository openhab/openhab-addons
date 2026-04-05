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
package org.openhab.binding.enocean.internal.util;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A general utility class.
 *
 * @author Ravi Nadahar - Initial contribution
 */
@NonNullByDefault
public class EnOceanUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnOceanUtil.class);
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    private EnOceanUtil() {
        // Not to be instantiated
    }

    public static String byteToHex(byte b) {
        int i = b & 0xFF;
        return new StringBuilder(4).append("0x").append(HEX_ARRAY[i >>> 4]).append(HEX_ARRAY[i & 0x0F]).toString();
    }

    public static byte[] subArray(byte[] bytes, int start, int length) {
        if (start == 0 && length == bytes.length) {
            return bytes;
        }
        int len = Math.min(length, bytes.length - start);
        if (len < length) {
            LOGGER.debug("Packet is shorter ({}) than expected: {}", len, length);
        }
        byte[] result = new byte[len];
        System.arraycopy(bytes, start, result, 0, len);
        return result;
    }
}
