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
package org.openhab.binding.homekit.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Enumeration of paired status flag of a HomeKit accessories.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum AccessoryPairingStatus {
    PAIRED(0x00),
    UNPAIRED(0x01);

    public final byte value;

    AccessoryPairingStatus(int value) {
        this.value = (byte) value;
    }

    public static AccessoryPairingStatus from(int value) throws IllegalArgumentException {
        for (AccessoryPairingStatus state : values()) {
            if (state.value == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown pairing feature: " + value);
    }
}
