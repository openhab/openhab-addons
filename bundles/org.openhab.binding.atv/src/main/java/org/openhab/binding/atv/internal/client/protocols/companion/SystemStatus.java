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
package org.openhab.binding.atv.internal.client.protocols.companion;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Current system state of the remote device.
 *
 * @author Dan Cunningham - Initial contribution
 */
@NonNullByDefault
public enum SystemStatus {
    /** Not a valid protocol entry; used only locally. */
    Unknown(0x00),

    Asleep(0x01),
    Screensaver(0x02),
    Awake(0x03),
    Idle(0x04);

    private final int value;

    SystemStatus(int value) {
        this.value = value;
    }

    /**
     * Numeric value used on the wire.
     */
    public int value() {
        return value;
    }

    /**
     * Looks up the constant matching an on-wire value.
     *
     * @param value the numeric value
     * @return the matching constant
     * @throws IllegalArgumentException if no constant has the given value
     */
    public static SystemStatus fromValue(int value) {
        for (SystemStatus status : values()) {
            if (status.value == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown SystemStatus value: " + value);
    }
}
