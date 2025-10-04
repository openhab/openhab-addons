/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
 * Enumeration of pairing states used in the HomeKit pairing process.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum PairingState {
    M1(0x01),
    M2(0x02),
    M3(0x03),
    M4(0x04),
    M5(0x05),
    M6(0x06);

    public final byte value;

    PairingState(int value) {
        this.value = (byte) value;
    }

    public static PairingState from(byte value) {
        for (PairingState state : values()) {
            if (state.value == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown pairing state: " + value);
    }
}
