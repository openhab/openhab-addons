/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.hdpowerview.internal.dto.gen3;

/**
 * Enum for Generation 3 shade power types.
 *
 * The gateway's API documentation lists only the values 0 to 2, but gateways also report 11 and 12 for shades with
 * a built-in (fixed) rechargeable battery or a fixed hardwired supply respectively.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 * @author Kai Holthaus - Add fixed power types
 */
public enum PowerType {
    BATTERY(0),
    HARDWIRED(1),
    RECHARGEABLE(2),
    FIXED_RECHARGEABLE(11),
    FIXED_HARDWIRED(12),
    UNKNOWN(-1);

    private final int value;

    PowerType(int value) {
        this.value = value;
    }

    /**
     * Get the power type for the given value reported by the gateway.
     *
     * @param value the value reported by the gateway.
     * @return the matching power type, or UNKNOWN if the value is not recognized.
     */
    public static PowerType fromValue(int value) {
        for (PowerType powerType : values()) {
            if (powerType.value == value) {
                return powerType;
            }
        }
        return UNKNOWN;
    }
}
