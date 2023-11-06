/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.alarmdecoder.internal.protocol;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Defines keypad device addresses used in an AD keypad address mask.
 *
 * @author Bob Adair - Initial contribution
 */
@NonNullByDefault
public enum ADAddress {
    KEYPAD0(0x01000000, 0),
    KEYPAD1(0x02000000, 1),
    KEYPAD2(0x04000000, 2),
    KEYPAD3(0x08000000, 3),
    KEYPAD4(0x10000000, 4),
    KEYPAD5(0x20000000, 5),
    KEYPAD6(0x40000000, 6),
    KEYPAD7(0x80000000, 7),

    KEYPAD8(0x00010000, 8),
    KEYPAD9(0x00020000, 9),
    KEYPAD10(0x00040000, 10),
    KEYPAD11(0x00080000, 11),
    KEYPAD12(0x00100000, 12),
    KEYPAD13(0x00200000, 13),
    KEYPAD14(0x00400000, 14),
    KEYPAD15(0x00800000, 15),

    KEYPAD16(0x00000100, 16),
    KEYPAD17(0x00000200, 17),
    KEYPAD18(0x00000400, 18),
    KEYPAD19(0x00000800, 19),
    KEYPAD20(0x00001000, 20),
    KEYPAD21(0x00002000, 21),
    KEYPAD22(0x00004000, 22),
    KEYPAD23(0x00008000, 23),

    KEYPAD24(0x00000001, 24),
    KEYPAD25(0x00000002, 25),
    KEYPAD26(0x00000004, 26),
    KEYPAD27(0x00000008, 27),
    KEYPAD28(0x00000010, 28),
    KEYPAD29(0x00000020, 29),
    KEYPAD30(0x00000040, 30),
    KEYPAD31(0x00000080, 31);

    private final long mask;
    private final int device;

    ADAddress(long mask, int device) {
        this.mask = mask;
        this.device = device;
    }

    /** Returns the device bit mask **/
    public long mask() {
        return mask;
    }

    /** Returns the device number (0-31) **/
    public int deviceNum() {
        return device;
    }

    /**
     * Returns the first device address found in addressMask or null if none are found
     *
     * @param addressMask
     */
    public static @Nullable ADAddress getDevice(long addressMask) {
        for (ADAddress address : ADAddress.values()) {
            if ((address.mask() & addressMask) != 0) {
                return address;
            }
        }
        return null;
    }

    /**
     * Returns a Collection of the device addresses found in addressMask.
     * Returns an empty collection if none are found.
     *
     * @param addressMask
     */
    public static Collection<ADAddress> getDevices(long addressMask) {
        Collection<ADAddress> addressCollection = new ArrayList<>();
        for (ADAddress address : ADAddress.values()) {
            if ((address.mask() & addressMask) != 0) {
                addressCollection.add(address);
            }
        }
        return addressCollection;
    }

    /**
     * Return true if 1 and only 1 address bit is set in addressMask
     */
    public static boolean singleAddress(long addressMask) {
        return (Long.bitCount(addressMask) == 1);
    }
}
