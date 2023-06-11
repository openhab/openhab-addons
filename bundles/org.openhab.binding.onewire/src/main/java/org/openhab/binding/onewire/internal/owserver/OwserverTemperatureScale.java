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
package org.openhab.binding.onewire.internal.owserver;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link OwserverTemperatureScale} provides the owserver protocol temperature scale flags
 *
 * @author Jan N. Klug - Initial contribution
 */

@NonNullByDefault
public enum OwserverTemperatureScale {
    CENTIGRADE(0x00000000),
    FAHRENHEIT(0x00010000),
    KELVIN(0x00020000),
    RANKINE(0x00030000);

    private static final int CLEAR_MASK = 0x00030000;
    private final int flag;

    OwserverTemperatureScale(int flag) {
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
        int tempFlags = flags;
        tempFlags &= ~CLEAR_MASK;
        tempFlags |= this.getValue();
        return tempFlags;
    }

    /**
     * get the temperature scale flag from a given set of flags
     *
     * @param flags set of flags
     * @return temperature scale flag
     * @throws IllegalArgumentException
     */
    public static OwserverTemperatureScale getFlag(int flags) {
        int tempFlags = flags;
        tempFlags &= CLEAR_MASK;
        for (OwserverTemperatureScale value : values()) {
            if (value.getValue() == tempFlags) {
                return value;
            }
        }
        return CENTIGRADE;
    }
}
