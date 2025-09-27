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
 * Enumeration of error codes used in HomeKit communication.
 *
 * @author Andrew Fiddian-Green - Initial contribution
 */
@NonNullByDefault
public enum ErrorCode {
    RESERVED(0x00),
    UNKNOWN(0x01),
    AUTHENTICATION(0x02),
    BACK_OFF(0x03),
    MAX_PEERS(0x04),
    MAX_TRIES(0x05),
    UNAVAILABLE(0x06),
    BUSY(0x07);

    public final byte value;

    ErrorCode(int value) {
        this.value = (byte) value;
    }

    public static ErrorCode from(byte b) {
        for (ErrorCode state : values()) {
            if (state.value == b) {
                return state;
            }
        }
        throw new IllegalArgumentException("Unknown error code: " + b);
    }
}
