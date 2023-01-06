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
package org.openhab.binding.echonetlite.internal;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
public class EchonetPropertyMap {
    private static final int[][] PROPERTY_MAP = { { 0x80, 0x90, 0xA0, 0xB0, 0xC0, 0xD0, 0xE0, 0xF0, },
            { 0x81, 0x91, 0xA1, 0xB1, 0xC1, 0xD1, 0xE1, 0xF1, }, { 0x82, 0x92, 0xA2, 0xB2, 0xC2, 0xD2, 0xE2, 0xF2, },
            { 0x83, 0x93, 0xA3, 0xB3, 0xC3, 0xD3, 0xE3, 0xF3, }, { 0x84, 0x94, 0xA4, 0xB4, 0xC4, 0xD4, 0xE4, 0xF4, },
            { 0x85, 0x95, 0xA5, 0xB5, 0xC5, 0xD5, 0xE5, 0xF5, }, { 0x86, 0x96, 0xA6, 0xB6, 0xC6, 0xD6, 0xE6, 0xF6, },
            { 0x87, 0x97, 0xA7, 0xB7, 0xC7, 0xD7, 0xE7, 0xF7, }, { 0x88, 0x98, 0xA8, 0xB8, 0xC8, 0xD8, 0xE8, 0xF8, },
            { 0x89, 0x99, 0xA9, 0xB9, 0xC9, 0xD9, 0xE9, 0xF9, }, { 0x8A, 0x9A, 0xAA, 0xBA, 0xCA, 0xDA, 0xEA, 0xFA, },
            { 0x8B, 0x9B, 0xAB, 0xBB, 0xCB, 0xDB, 0xEB, 0xFB, }, { 0x8C, 0x9C, 0xAC, 0xBC, 0xCC, 0xDC, 0xEC, 0xFC, },
            { 0x8D, 0x9D, 0xAD, 0xBD, 0xCD, 0xDD, 0xED, 0xFD, }, { 0x8E, 0x9E, 0xAE, 0xBE, 0xCE, 0xDE, 0xEE, 0xFE, },
            { 0x8F, 0x9F, 0xAF, 0xBF, 0xCF, 0xDF, 0xEF, 0xFF, }, };

    private int[] propertyMap = {};
    private final Epc epc;

    public EchonetPropertyMap(final Epc epc) {
        this.epc = epc;
    }

    public Epc epc() {
        return epc;
    }

    public void update(final ByteBuffer edt) {
        propertyMap = parsePropertyMap(edt);
    }

    public void getProperties(int groupCode, int classCode, final Set<Epc> existing, Collection<Epc> toFill) {
        for (int epcCode : propertyMap) {
            final Epc epc = Epc.lookup(groupCode, classCode, epcCode);
            if (!existing.contains(epc)) {
                toFill.add(epc);
            }
        }
    }

    static int[] parsePropertyMap(final ByteBuffer buffer) {
        final int numProperties = buffer.get() & 0xFF;
        final int[] properties = new int[numProperties];
        int propertyIndex = 0;
        if (numProperties < 16) {
            for (int i = 0; i < numProperties; i++) {
                properties[propertyIndex] = (buffer.get() & 0xFF);
                propertyIndex++;
            }
        } else {
            assert 16 == buffer.remaining();

            for (int i = 0; i < 16; i++) {
                int b = buffer.get() & 0xFF;
                for (int j = 0; j < 8; j++) {
                    if (0 != (b & (1 << j))) {
                        assert propertyIndex < properties.length;

                        properties[propertyIndex] = PROPERTY_MAP[i][j];
                        propertyIndex++;
                    }
                }
            }
        }

        assert propertyIndex == properties.length;
        return properties;
    }

    public String toString() {
        return "EnPropertyMap{" + "propertyMap=" + HexUtil.hex(propertyMap) + '}';
    }
}
