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
package org.openhab.binding.atv.internal.client.dto;

/**
 * Pairing requirement for a service.
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum PairingRequirement {
    /**
     * Not supported by protocol or not implemented.
     */
    Unsupported(1),

    /**
     * Pairing is disabled by protocol.
     */
    Disabled(2),

    /**
     * Pairing is not needed.
     */
    NotNeeded(3),

    /**
     * Pairing is supported but not required.
     */
    Optional(4),

    /**
     * Pairing must be performed.
     */
    Mandatory(5);

    private final int value;

    PairingRequirement(int value) {
        this.value = value;
    }

    /**
     * Returns the numeric value stored for this constant.
     */
    public int value() {
        return value;
    }

    /**
     * Looks up the constant matching a stored numeric value.
     *
     * @throws IllegalArgumentException if no constant has the given value
     */
    public static PairingRequirement fromValue(int value) {
        for (PairingRequirement v : values()) {
            if (v.value == value) {
                return v;
            }
        }
        throw new IllegalArgumentException("Unknown PairingRequirement value: " + value);
    }
}
