/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
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
    private final CaddxPropertyType type; // 'Int', 'String', 'Bit'
    private final int byteFrom;
    private final int byteLength;
    private final int bitFrom;
    private final int bitLength;
    private final boolean external;
    private final String id;

    public CaddxProperty(String id, int byteFrom, int byteLength, int bitFrom, int bitLength, CaddxPropertyType type,
            String name, boolean external) {
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

    public CaddxPropertyType getType() {
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

        switch (type) {
            case INT:
                if (bitFrom == 0 && bitLength == 0) {
                    mask = 255;
                    val = message[byteFrom - 1] & mask;
                } else {
                    mask = ((1 << ((bitLength - bitFrom))) - 1) << bitFrom;
                    val = (message[byteFrom - 1] & mask) >> bitFrom;
                }

                return Integer.toString(val);
            case STRING:
                byte[] str = Arrays.copyOfRange(message, byteFrom - 1, byteFrom + byteLength - 1);
                return mapCaddxString(new String(str, StandardCharsets.US_ASCII));
            case BIT:
                return (((message[byteFrom - 1] & (1 << bitFrom)) > 0) ? "true" : "false");
            default:
                throw new IllegalArgumentException("type is unknown.");
        }
    }

    public String toString(byte[] message) {
        int mask;
        int val;
        StringWriter sWriter = new StringWriter();
        PrintWriter pWriter = new PrintWriter(sWriter);

        switch (type) {
            case INT:
                if (bitFrom == 0 && bitLength == 0) {
                    mask = 255;
                    val = message[byteFrom - 1];
                } else {
                    mask = ((1 << ((bitLength - bitFrom) + 1)) - 1) << bitFrom;
                    val = (message[byteFrom - 1] & mask) >> bitFrom;
                }

                pWriter.printf("%s: %02x - %d - %c", name, val, val, Character.isValidCodePoint(val) ? val : 32);
                pWriter.flush();

                return sWriter.toString();
            case STRING:
                pWriter.print(name);
                pWriter.print(": ");

                byte[] a = Arrays.copyOfRange(message, byteFrom - 1, byteFrom + byteLength);
                pWriter.println(mapCaddxString(new String(a, StandardCharsets.US_ASCII)));
                pWriter.println();
                for (int i = 0; i < byteLength; i++) {
                    pWriter.printf("%02x", message[byteFrom - 1 + i]);
                    pWriter.print(" - ");
                    pWriter.println((char) message[byteFrom - 1 + i]);
                }
                pWriter.flush();

                return sWriter.toString();
            case BIT:
                pWriter.print(name);
                pWriter.print(": ");
                pWriter.print(((message[byteFrom - 1] & (1 << bitFrom)) > 0));
                pWriter.flush();

                return sWriter.toString();
            default:
                pWriter.print("Unknown type: ");
                pWriter.print(type.toString());
                pWriter.flush();

                return sWriter.toString();
        }
    }

    private String mapCaddxString(String str) {
        StringBuilder s = new StringBuilder(str.length());

        CharacterIterator it = new StringCharacterIterator(str);
        for (char ch = it.first(); ch != CharacterIterator.DONE; ch = it.next()) {
            switch (ch) {
                case 0xb7:
                    s.append('Γ');
                    break;
                case 0x10:
                    s.append('Δ');
                    break;
                case 0x13:
                    s.append('Θ');
                    break;
                case 0x14:
                    s.append('Λ');
                    break;
                case 0x12:
                    s.append('Ξ');
                    break;
                case 0xc8:
                    s.append('Π');
                    break;
                case 0x16:
                    s.append('Σ');
                    break;
                case 0xcc:
                    s.append('Φ');
                    break;
                case 0x17:
                    s.append('Ψ');
                    break;
                case 0x15:
                    s.append('Ω');
                    break;
                default:
                    s.append(ch);
                    break;
            }
        }

        return s.toString();
    }
}
