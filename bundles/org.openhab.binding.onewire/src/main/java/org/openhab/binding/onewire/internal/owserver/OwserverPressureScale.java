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
package org.openhab.binding.onewire.internal.owserver;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OwserverPressureScale} provides the owserver protocol pressure scale flags
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public enum OwserverPressureScale {
    MILLIBAR(0x00000000),
    ATM(0x00040000),
    MMHG(0x00080000),
    INHG(0x000C0000),
    PSI(0x00100000),
    PASCAL(0x00140000);

    private static final int CLEAR_MASK = 0x001C0000;
    private final int flag;

    OwserverPressureScale(int flag) {
        this.flag = flag;
    }

    /**
     * get numeric value of this flag
     *
     * @return
     */
    public int getValue() {
        return flag;
    }

    /**
     * set this flag in a set of given flags
     *
     * @param flags aggregated flags
     * @return parameter with this flag set
     */
    public int setFlag(int flags) {
        return (flags & ~CLEAR_MASK) | this.getValue();
    }

    /**
     * get the pressure scale flag from a given set of flags
     *
     * @param flags set of flags
     * @return pressure scale flag
     * @throws IllegalArgumentException
     */
    public static OwserverPressureScale getFlag(int flags) throws IllegalArgumentException {
        for (OwserverPressureScale value : values()) {
            if (value.getValue() == (flags & CLEAR_MASK)) {
                return value;
            }
        }
        throw new IllegalArgumentException("Pressure scale flag not found");
    }
}
