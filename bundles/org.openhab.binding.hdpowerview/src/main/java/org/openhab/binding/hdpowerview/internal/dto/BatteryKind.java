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
package org.openhab.binding.hdpowerview.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * An enum for the type of power supply in a shade.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum BatteryKind {
    ERROR_UNKNOWN(-1),
    HARDWIRED_POWER_SUPPLY(1),
    BATTERY_WAND(2),
    RECHARGEABLE_BATTERY_WAND(3);

    private int batteryKind;

    private BatteryKind(int i) {
        this.batteryKind = i;
    }

    /**
     * Determine the BatteryKind by parsing the given string value.
     *
     * @param value the string to parse, or null.
     * @return the BatteryKind or ERROR_UNKNOWN in case of error.
     */
    public static BatteryKind fromString(@Nullable String value) {
        if (value != null) {
            try {
                int intValue = Integer.parseInt(value);
                for (BatteryKind e : values()) {
                    if (e.batteryKind == intValue) {
                        return e;
                    }
                }
            } catch (NumberFormatException e) {
                // fall through
            }
        }
        return ERROR_UNKNOWN;
    }
}
