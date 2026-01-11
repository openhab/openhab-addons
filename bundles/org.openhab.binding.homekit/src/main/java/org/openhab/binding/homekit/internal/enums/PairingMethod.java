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
 * Enumeration of pairing methods used in HomeKit communication.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum PairingMethod {
    SETUP(0x00),
    SETUP_AUTH(0x01),
    VERIFY(0x02),
    ADD(0x03),
    REMOVE(0x04),
    LIST(0x05);

    public final byte value;

    PairingMethod(int value) {
        this.value = (byte) value;
    }

    public static PairingMethod from(byte value) throws IllegalArgumentException {
        for (PairingMethod state : values()) {
            if (state.value == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown pairing method: " + value);
    }
}
