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
package org.openhab.binding.insteon.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.message.DataType;

/**
 * Various utility functions for e.g. hex string parsing
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class Utils {
    public static String getHexString(int b) {
        return String.format("%02X", b & 0xFF);
    }

    public static String getHexString(byte[] b) {
        return getHexString(b, b.length);
    }

    public static String getHexString(byte[] b, int len) {
        String result = "";
        for (int i = 0; i < b.length && i < len; i++) {
            result += String.format("%02X ", b[i] & 0xFF);
        }
        return result;
    }

    public static int strToInt(String s) throws NumberFormatException {
        int ret = -1;
        if (s.startsWith("0x")) {
            ret = Integer.parseInt(s.substring(2), 16);
        } else {
            ret = Integer.parseInt(s);
        }
        return (ret);
    }

    public static int fromHexString(String string) {
        return Integer.parseInt(string, 16);
    }

    public static int from0xHexString(String string) {
        String hex = string.substring(2);
        return fromHexString(hex);
    }

    public static String getHexByte(byte b) {
        return String.format("0x%02X", b & 0xFF);
    }

    public static String getHexByte(int b) {
        return String.format("0x%02X", b);
    }

    public static class DataTypeParser {
        public static Object parseDataType(DataType type, String val) {
            switch (type) {
                case BYTE:
                    return parseByte(val);
                case INT:
                    return parseInt(val);
                case FLOAT:
                    return parseFloat(val);
                case ADDRESS:
                    return parseAddress(val);
                default:
                    throw new IllegalArgumentException("Data Type not implemented in Field Value Parser!");
            }
        }

        public static byte parseByte(@Nullable String val) {
            if (val != null && !"".equals(val.trim())) {
                return (byte) Utils.from0xHexString(val.trim());
            } else {
                return 0x00;
            }
        }

        public static int parseInt(@Nullable String val) {
            if (val != null && !"".equals(val.trim())) {
                return Integer.parseInt(val);
            } else {
                return 0x00;
            }
        }

        public static float parseFloat(@Nullable String val) {
            if (val != null && !"".equals(val.trim())) {
                return Float.parseFloat(val.trim());
            } else {
                return 0;
            }
        }

        public static InsteonAddress parseAddress(@Nullable String val) {
            if (val != null && !"".equals(val.trim())) {
                return InsteonAddress.parseAddress(val.trim());
            } else {
                return new InsteonAddress();
            }
        }
    }

    /**
     * Exception to indicate various xml parsing errors.
     */
    public static class ParsingException extends Exception {
        private static final long serialVersionUID = 3997461423241843949L;

        public ParsingException(String msg) {
            super(msg);
        }

        public ParsingException(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    public static String redactPassword(String port) {
        return !port.startsWith("/hub2/") ? port : port.replaceAll(":\\w+@", ":******@");
    }
}
