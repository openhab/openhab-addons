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
package org.openhab.binding.insteon.internal.device;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * This class has utilities related to the X10 protocol.
 *
 * @author Bernd Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 * @author Jeremy Setton - Improvements for openHAB 3 insteon binding
 */
@NonNullByDefault
public class X10 {
    public static final char HOUSE_CODE_MIN = 'A';
    public static final char HOUSE_CODE_MAX = 'P';
    public static final int UNIT_CODE_MIN = 1;
    public static final int UNIT_CODE_MAX = 16;

    /**
     * Enumerates the X10 command codes
     */
    public enum Command {
        ALL_LIGHTS_OFF(0x6),
        STATUS_OFF(0xE),
        ON(0x2),
        PRESET_DIM_1(0xA),
        ALL_LIGHTS_ON(0x1),
        HAIL_ACKNOWLEDGE(0x9),
        BRIGHT(0x5),
        STATUS_ON(0xD),
        EXTENDED_CODE(0x9),
        STATUS_REQUEST(0xF),
        OFF(0x3),
        PRESET_DIM_2(0xB),
        ALL_UNITS_OFF(0x0),
        HAIL_REQUEST(0x8),
        DIM(0x4),
        EXTENDED_DATA(0xC);

        private final byte code;

        Command(int code) {
            this.code = (byte) code;
        }

        public byte code() {
            return code;
        }
    }

    /**
     * Enumerates the X10 flag codes
     */
    public enum Flag {
        ADDRESS(0x00),
        COMMAND(0x80);

        private byte code;

        Flag(int code) {
            this.code = (byte) code;
        }

        public byte code() {
            return code;
        }
    }

    /**
     * Returns house code as string
     *
     * @param c house code as per X10 spec
     * @return clear text house code, i.e letter A-P
     */
    public static String houseToString(byte c) {
        String s = houseCodeToString.get(c & 0xff);
        return s == null ? "X" : s;
    }

    /**
     * Returns unit code as integer
     *
     * @param c unit code per X10 spec
     * @return decoded integer, i.e. number 0-16
     */
    public static int unitToInt(byte c) {
        Integer i = unitCodeToInt.get(c & 0xff);
        return i == null ? -1 : i;
    }

    /**
     * Returns if an X10 address is valid
     *
     * @param s string to test
     * @return true if valid X10 address
     */
    public static boolean isValidAddress(String s) {
        String[] parts = s.split("\\.");
        if (parts.length != 2) {
            return false;
        }
        return isValidHouseCode(parts[0]) && isValidUnitCode(parts[1]);
    }

    /**
     * Returns if a house code is valid
     *
     * @param s house code to test
     * @return true if valid house code
     */
    public static boolean isValidHouseCode(String s) {
        return s.length() == 1 && s.charAt(0) >= HOUSE_CODE_MIN && s.charAt(0) <= HOUSE_CODE_MAX;
    }

    /**
     * Returns if a unit code as an integer is valid
     *
     * @param i unit code to test
     * @return true if valid unit code
     */
    public static boolean isValidUnitCode(int i) {
        return i >= UNIT_CODE_MIN && i <= UNIT_CODE_MAX;
    }

    /**
     * Returns if a unit code as a string is valid
     *
     * @param s unit code to test
     * @return true if valid unit code
     */
    public static boolean isValidUnitCode(String s) {
        try {
            int i = Integer.parseInt(s);
            return isValidUnitCode(i);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Returns X10 address to byte code
     *
     * @param address clear text address
     * @return byte that encodes house + unit code
     */
    public static byte addressToByte(String address) {
        String[] parts = address.split("\\.");
        int ih = houseStringToCode(parts[0]);
        int iu = unitStringToCode(parts[1]);
        int itot = ih << 4 | iu;
        return (byte) (itot & 0xff);
    }

    /**
     * Returns house string as code
     *
     * @param s clear text house string
     * @return coded house byte
     */
    public static int houseStringToCode(String s) {
        for (Entry<Integer, String> entry : houseCodeToString.entrySet()) {
            if (s.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return 0xf;
    }

    /**
     * Returns unit string as code
     *
     * @param s string with clear text integer inside
     * @return encoded unit byte
     */
    public static int unitStringToCode(String s) {
        try {
            int i = Integer.parseInt(s);
            for (Entry<Integer, Integer> entry : unitCodeToInt.entrySet()) {
                if (i == entry.getValue()) {
                    return entry.getKey();
                }
            }
        } catch (NumberFormatException e) {
        }
        return 0xf;
    }

    /**
     * Map between 4-bit X10 code and the house code.
     */
    private static Map<Integer, String> houseCodeToString = new HashMap<>();
    /**
     * Map between 4-bit X10 code and the unit code.
     */
    private static Map<Integer, Integer> unitCodeToInt = new HashMap<>();

    static {
        houseCodeToString.put(0x6, "A");
        unitCodeToInt.put(0x6, 1);
        houseCodeToString.put(0xe, "B");
        unitCodeToInt.put(0xe, 2);
        houseCodeToString.put(0x2, "C");
        unitCodeToInt.put(0x2, 3);
        houseCodeToString.put(0xa, "D");
        unitCodeToInt.put(0xa, 4);
        houseCodeToString.put(0x1, "E");
        unitCodeToInt.put(0x1, 5);
        houseCodeToString.put(0x9, "F");
        unitCodeToInt.put(0x9, 6);
        houseCodeToString.put(0x5, "G");
        unitCodeToInt.put(0x5, 7);
        houseCodeToString.put(0xd, "H");
        unitCodeToInt.put(0xd, 8);
        houseCodeToString.put(0x7, "I");
        unitCodeToInt.put(0x7, 9);
        houseCodeToString.put(0xf, "J");
        unitCodeToInt.put(0xf, 10);
        houseCodeToString.put(0x3, "K");
        unitCodeToInt.put(0x3, 11);
        houseCodeToString.put(0xb, "L");
        unitCodeToInt.put(0xb, 12);
        houseCodeToString.put(0x0, "M");
        unitCodeToInt.put(0x0, 13);
        houseCodeToString.put(0x8, "N");
        unitCodeToInt.put(0x8, 14);
        houseCodeToString.put(0x4, "O");
        unitCodeToInt.put(0x4, 15);
        houseCodeToString.put(0xc, "P");
        unitCodeToInt.put(0xc, 16);
    }
}
