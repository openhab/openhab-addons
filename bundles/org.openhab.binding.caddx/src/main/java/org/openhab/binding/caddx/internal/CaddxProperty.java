/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.caddx.internal;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Panel message property class
 *
 * @author Georgios Moutsos - Initial contribution
 */
@NonNullByDefault
public class CaddxProperty {
    // private
    private final String name;
    private final String type; // 'Int', 'String', 'Bit'
    private final int byteFrom;
    private final int byteLength;
    private final int bitFrom;
    private final int bitLength;
    private final boolean external;
    private final String id;

    // Constructor
    public CaddxProperty(String id, int byteFrom, int byteLength, int bitFrom, int bitLength, String type, String name,
            boolean external) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.byteFrom = byteFrom;
        this.byteLength = byteLength;
        this.bitFrom = bitFrom;
        this.bitLength = bitLength;
        this.external = external;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean getExternal() {
        return external;
    }

    public String getId() {
        return id;
    }

    public String getValue(byte[] message) {
        int mask;
        int val;

        if ("Int".equals(type)) {
            if (bitFrom == 0 && bitLength == 0) {
                mask = 255;
                val = message[byteFrom - 1] & mask;
            } else {
                mask = ((1 << ((bitLength - bitFrom))) - 1) << bitFrom;
                val = (message[byteFrom - 1] & mask) >> bitFrom;
            }

            return Integer.toString(val);
        }

        if ("String".equals(type)) {
            byte[] str = Arrays.copyOfRange(message, byteFrom - 1, byteFrom + byteLength);
            return new String(str);
        }

        if ("Bit".equals(type)) {
            return (((message[byteFrom - 1] & (1 << bitFrom)) > 0) ? "true" : "false");
        }

        throw new IllegalArgumentException("type [" + type + "] is unknown.");
    }

    public String toString(byte[] message) {
        int mask;
        int val;

        if ("Int".equals(type)) {
            if (bitFrom == 0 && bitLength == 0) {
                mask = 255;
                val = message[byteFrom - 1];
            } else {
                mask = ((1 << ((bitLength - bitFrom) + 1)) - 1) << bitFrom;
                val = (message[byteFrom - 1] & mask) >> bitFrom;
            }

            return name + ": " + String.format("%2s", Integer.toHexString(val)) + " - " + Integer.toString(val) + " - "
                    + ((val >= 32 && val <= 'z') ? ((char) val) : "-");
        }

        if ("String".contentEquals(type)) {
            StringBuilder sb = new StringBuilder();

            byte[] a = Arrays.copyOfRange(message, byteFrom - 1, byteFrom + byteLength);
            sb.append(name);
            sb.append(": ");
            sb.append(new String(a));
            sb.append("\r\n\r\n");
            for (int i = 0; i < byteLength; i++) {
                sb.append(String.format("%2s", Integer.toHexString(message[byteFrom - 1 + i])));
                sb.append(" - ");
                sb.append((char) message[byteFrom - 1 + i]);
                sb.append("\r\n");
            }

            return sb.toString();
        }

        if ("Bit".contentEquals(type)) {
            return name + ": " + (((message[byteFrom - 1] & (1 << bitFrom)) > 0) ? "true" : "false");
        }

        return "Unknown type: " + type;
    }
}
