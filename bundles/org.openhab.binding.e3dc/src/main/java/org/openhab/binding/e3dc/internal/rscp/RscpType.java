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
package org.openhab.binding.e3dc.internal.rscp;

import java.nio.charset.StandardCharsets;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * RSCP data types as used in the type field of an RSCP data item.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public enum RscpType {

    NONE(0x00),
    BOOL(0x01),
    CHAR8(0x02),
    UCHAR8(0x03),
    INT16(0x04),
    UINT16(0x05),
    INT32(0x06),
    UINT32(0x07),
    INT64(0x08),
    UINT64(0x09),
    FLOAT32(0x0A),
    DOUBLE64(0x0B),
    BITFIELD(0x0C),
    CSTRING(0x0D),
    CONTAINER(0x0E),
    TIMESTAMP(0x0F),
    BYTEARRAY(0x10),
    ERROR(0xFF);

    private final int code;

    RscpType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static RscpType fromCode(int code) {
        for (RscpType type : values()) {
            if (type.code == code) {
                return type;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown RSCP type 0x%02x", code));
    }

    /**
     * Renders a decoded value for logging. Passwords are never part of a decoded response, so only
     * the value itself needs conversion.
     */
    static String toStringValue(RscpType type, Object value) {
        if (value instanceof byte[] bytes) {
            if (type == CSTRING) {
                return new String(bytes, StandardCharsets.UTF_8);
            }
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        }
        return String.valueOf(value);
    }
}
