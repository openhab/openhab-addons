package org.openhab.binding.caddx.internal;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;

@NonNullByDefault
public class CaddxProperty {
    // private
    String name;
    String type; // 'Int', 'String', 'Bit'
    int byteFrom;
    int byteLength;
    int bitFrom;
    int bitLength;
    boolean external;
    String id;

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

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

    public String getValue(byte[] message) {
        int mask;
        int val;

        if (type == "Int") {
            if (bitFrom == 0 && bitLength == 0) {
                mask = 255;
                val = message[byteFrom - 1] & mask;
            } else {
                mask = ((1 << ((bitLength - bitFrom))) - 1) << bitFrom;
                val = (message[byteFrom - 1] & mask) >> bitFrom;
            }

            return Integer.toString(val);
        }

        if (type == "String") {
            byte[] str = Arrays.copyOfRange(message, byteFrom - 1, byteFrom + byteLength);
            return new String(str);
        }

        if (type == "Bit") {
            return (((message[byteFrom - 1] & (1 << bitFrom)) > 0) ? "true" : "false");
        }

        throw new IllegalArgumentException("type [" + type + "] is unknown.");
    }

    public String toString(byte[] message) {
        int mask;
        int val;

        if (type == "Int") {
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

        if (type == "String") {
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

        if (type == "Bit") {
            return name + ": " + (((message[byteFrom - 1] & (1 << bitFrom)) > 0) ? "true" : "false");
        }

        return "Unknown type: " + type;
    }
}
