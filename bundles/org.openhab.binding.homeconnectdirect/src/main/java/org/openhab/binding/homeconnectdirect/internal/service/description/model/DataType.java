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
package org.openhab.binding.homeconnectdirect.internal.service.description.model;

import java.util.StringJoiner;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Data Type Description (DTD) identifiers from the device description.
 * Based on HC_INT_BSH_DTD.xml.
 *
 * @author Jonas Brüstel - Initial contribution
 */
@NonNullByDefault
public enum DataType {
    BOOLEAN(0x00, "Boolean"),
    UINT8(0x80, "UInt8"),
    UINT16(0x81, "UInt16"),
    UINT32(0x82, "UInt32"),
    UINT64(0x83, "UInt64"),
    INT8(0x84, "Int8"),
    INT16(0x85, "Int16"),
    INT32(0x86, "Int32"),
    INT64(0x87, "Int64"),
    FLOAT(0x88, "Float"),
    DOUBLE(0x89, "Double"),
    DATE_TIME(0x8A, "DateTime"),
    STRING(0x8B, "String"),
    BYTE_STRING(0x8C, "ByteString"),
    GENERIC_JSON_DATA(0x8D, "GenericJsonData"),

    // Conversion types: COM converts between content transfer type and internal representation
    FLOAT_UINT8(0xA0, "Float_UInt8"),
    FLOAT_UINT16(0xA1, "Float_UInt16"),
    FLOAT_UINT32(0xA2, "Float_UInt32"),
    FLOAT_UINT64(0xA3, "Float_UInt64"),
    FLOAT_INT8(0xA4, "Float_Int8"),
    FLOAT_INT16(0xA5, "Float_Int16"),
    FLOAT_INT32(0xA6, "Float_Int32"),
    FLOAT_INT64(0xA7, "Float_Int64"),
    DATE_DATE_TIME(0xA8, "Date_DateTime"),
    TIME_DATE_TIME(0xA9, "Time_DateTime"),
    MAC_BYTE_ARRAY6(0xAA, "MAC_ByteArray6"),
    RGB(0xAB, "RGB"),

    // Composite types
    POINT2D_UINT16(0x0101, "Point2D_UInt16"),
    POINT2D_FLOAT(0x0108, "Point2D_Float"),
    LINE2D_UINT16(0x0121, "Line2D_UInt16"),
    LINE2D_FLOAT(0x0128, "Line2D_Float"),
    POSE2D_UINT16(0x0141, "Pose2D_UInt16"),
    POSE2D_FLOAT(0x0148, "Pose2D_Float");

    public final int id;
    public final String type;

    DataType(int id, String type) {
        this.id = id;
        this.type = type;
    }

    /**
     * Returns true if this data type represents a raw integer wire encoding (UInt8-Int64).
     * When a contentType has protocolType FLOAT but the wire encoding is a raw integer,
     * the value is a fixed-point integer that needs scaling.
     */
    public boolean isRawInteger() {
        return this == UINT8 || this == UINT16 || this == UINT32 || this == UINT64 || this == INT8 || this == INT16
                || this == INT32 || this == INT64;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", DataType.class.getSimpleName() + "[", "]").add("id=" + id)
                .add("type='" + type + "'").toString();
    }
}
