/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.insteon.internal.message;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.insteon.internal.device.InsteonAddress;
import org.openhab.binding.insteon.internal.utils.ByteUtils;

/**
 * Represents the data type parser
 *
 * @author Daniel Pfrommer - Initial contribution
 * @author Rob Nielsen - Port to openHAB 2 insteon binding
 */
@NonNullByDefault
public class DataTypeParser {
    public static Object parseDataType(DataType type, String val) {
        switch (type) {
            case BYTE:
                return parseByte(val);
            case INT:
                return parseInt(val);
            case FLOAT:
                return parseFloat(val);
            case ADDRESS:
                return parseAddress(val);
            default:
                throw new IllegalArgumentException("Data Type not implemented in Field Value Parser!");
        }
    }

    public static byte parseByte(@Nullable String val) {
        if (val != null && !val.trim().equals("")) {
            return (byte) ByteUtils.hexStringToInteger(val.trim());
        } else {
            return 0x00;
        }
    }

    public static int parseInt(@Nullable String val) {
        if (val != null && !val.trim().equals("")) {
            return Integer.parseInt(val);
        } else {
            return 0x00;
        }
    }

    public static float parseFloat(@Nullable String val) {
        if (val != null && !val.trim().equals("")) {
            return Float.parseFloat(val.trim());
        } else {
            return 0;
        }
    }

    public static InsteonAddress parseAddress(@Nullable String val) {
        if (val != null && !val.trim().equals("")) {
            return InsteonAddress.parseAddress(val.trim());
        } else {
            return new InsteonAddress();
        }
    }
}
