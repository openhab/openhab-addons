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
 * Enumeration of pairing feature flags of a HomeKit accessory
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum AccessoryPairingFeature {
    NO(0x00), // no support for HAP Pairing
    YES(0x01), // supports pairing via software, or Apple authentication coprocessor
    SECURE_HTTP_DEPRECATED(0x02); // supports pairing via secure HTTP (deprecated)

    public final byte value;

    AccessoryPairingFeature(int value) {
        this.value = (byte) value;
    }

    public static AccessoryPairingFeature from(int value) throws IllegalArgumentException {
        for (AccessoryPairingFeature state : values()) {
            if (state.value == value) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown pairing feature: " + value);
    }
}
