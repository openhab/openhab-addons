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
package org.openhab.binding.lcn.internal.common;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Helper to bitwise reverse numbers.
 *
 * @author Tobias Jüttner - Initial Contribution
 */
@NonNullByDefault
final class ReverseNumber {
    /** Cache with all reversed 8 bit values. */
    private static final int[] REVERSED_UINT8 = new int[256];

    /** Initializes static data once this class is first used. */
    static {
        for (int i = 0; i < 256; ++i) {
            int reversed = 0;
            for (int j = 0; j < 8; ++j) {
                if ((i & (1 << j)) != 0) {
                    reversed |= (0x80 >> j);
                }
            }
            REVERSED_UINT8[i] = reversed;
        }
    }

    /**
     * Reverses the given 8 bit value bitwise.
     *
     * @param value the value to reverse bitwise (treated as unsigned 8 bit value)
     * @return the reversed value
     * @throws LcnException if value is out of range (not unsigned 8 bit)
     */
    static int reverseUInt8(int value) throws LcnException {
        if (value < 0 || value > 255) {
            throw new LcnException();
        }
        return REVERSED_UINT8[value];
    }
}
