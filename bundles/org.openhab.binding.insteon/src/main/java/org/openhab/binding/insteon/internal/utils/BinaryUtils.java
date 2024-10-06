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

/**
 * The {@link BinaryUtils} represents binary utility functions
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class BinaryUtils {
    /**
     * Returns a binary string for a given byte
     *
     * @param b the byte
     * @return the formatted binary string
     */
    public static String getBinaryString(byte b) {
        return getBinaryString(b & 0xFF);
    }

    /**
     * Returns a binary string for a given integer
     *
     * @param i the integer
     * @return the formatted binary string
     */
    public static String getBinaryString(int i) {
        String binary = Integer.toBinaryString(i);
        return String.format("%8s", binary).replace(" ", "0");
    }

    /**
     * Returns a bit value
     *
     * @param bitmask the bitmask
     * @param bit the bit to extract
     * @return the bit value
     */
    public static int getBit(int bitmask, int bit) {
        return (bitmask >> bit) & 0x1;
    }

    /**
     * Returns if a bit is set
     *
     * @param bitmask the bitmask
     * @param bit the bit to check
     * @return true if bit is set, otherwise false
     */
    public static boolean isBitSet(int bitmask, int bit) {
        return getBit(bitmask, bit) == 0x1;
    }

    /**
     * Updates a bit in a bitmask
     *
     * @param bitmask the bitmask to update
     * @param bit the bit to update
     * @param shouldSet if bit should be set
     * @return the updated bitmask
     */
    public static int updateBit(int bitmask, int bit, boolean shouldSet) {
        return shouldSet ? bitmask | (0x1 << bit) : bitmask & ~(0x1 << bit);
    }

    /**
     * Sets a bit in a bitmask
     *
     * @param bitmask the bitmask to update
     * @param bit the bit to set
     * @return the updated bitmask
     */
    public static int setBit(int bitmask, int bit) {
        return updateBit(bitmask, bit, true);
    }

    /**
     * Clears a bit in a bitmask
     *
     * @param bitmask the bitmask to update
     * @param bit the bit to clear
     * @return the updated bitmask
     */
    public static int clearBit(int bitmask, int bit) {
        return updateBit(bitmask, bit, false);
    }
}
