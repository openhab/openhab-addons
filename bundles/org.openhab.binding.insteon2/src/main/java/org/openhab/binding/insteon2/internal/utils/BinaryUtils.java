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
package org.openhab.binding.insteon2.internal.utils;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link BinaryUtils} represents binary utility functions
 *
 * @author Jeremy Setton - Initial contribution
 */
@NonNullByDefault
public class BinaryUtils {
    public static String getBinaryString(byte b) {
        return getBinaryString(b & 0xFF);
    }

    public static String getBinaryString(int b) {
        String binary = Integer.toBinaryString(b);
        return String.format("%8s", binary).replace(" ", "0");
    }

    public static int getBit(int bitmask, int bit) {
        return (bitmask >> bit) & 0x1;
    }

    public static boolean isBitSet(int bitmask, int bit) {
        return getBit(bitmask, bit) == 0x1;
    }

    public static int setBit(int bitmask, int bit, boolean shouldSet) {
        return shouldSet ? bitmask | (0x1 << bit) : bitmask & ~(0x1 << bit);
    }

    public static int setBit(int bitmask, int bit) {
        return setBit(bitmask, bit, true);
    }

    public static int clearBit(int bitmask, int bit) {
        return setBit(bitmask, bit, false);
    }
}
