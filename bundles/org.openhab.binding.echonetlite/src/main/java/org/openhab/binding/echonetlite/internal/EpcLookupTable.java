/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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

import static org.openhab.binding.echonetlite.internal.HexUtil.hex;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Michael Barker - Initial contribution
 */
@NonNullByDefault
enum EpcLookupTable {
    INSTANCE;

    private static final int MAX_ENTRIES = 256;
    private final Epc[][][] lookupTable = new Epc[MAX_ENTRIES][0][0];

    EpcLookupTable() {
        addLookupTableEntries(lookupTable, EchonetClass.AIRCON_HOMEAC);
        addLookupTableEntries(lookupTable, EchonetClass.MANAGEMENT_CONTROLLER);
        addLookupTableEntries(lookupTable, EchonetClass.NODE_PROFILE);
    }

    public Epc resolve(int groupCode, int classCode, int epcCode) {
        if (MAX_ENTRIES <= groupCode) {
            throw new IllegalArgumentException(MAX_ENTRIES + "<= groupCode (" + groupCode + ")");
        }
        if (MAX_ENTRIES <= classCode) {
            throw new IllegalArgumentException(MAX_ENTRIES + "<= classCode (" + classCode + ")");
        }
        if (MAX_ENTRIES <= epcCode) {
            throw new IllegalArgumentException(MAX_ENTRIES + "<= epcCode (" + epcCode + ")");
        }

        if (0 == lookupTable[groupCode].length) {
            throw new IllegalArgumentException("groupCode (" + hex(groupCode) + ") has no entries");
        }

        if (0 == lookupTable[groupCode][classCode].length) {
            throw new IllegalArgumentException(
                    "groupCode/classCode (" + hex(groupCode) + "/" + hex(classCode) + ") has no entries");
        }

        if (null == lookupTable[groupCode][classCode][epcCode]) {
            throw new IllegalArgumentException("groupCode/classCode (" + hex(groupCode) + "/" + hex(classCode) + "/"
                    + hex(epcCode) + ") has no entry");
        }

        return lookupTable[groupCode][classCode][epcCode];
    }

    private static void addLookupTableEntries(Epc[][][] lookupTable, EchonetClass echonetClass) {
        final int groupCode = echonetClass.groupCode();
        final int classCode = echonetClass.classCode();

        if (null == lookupTable[groupCode] || 0 == lookupTable[groupCode].length) {
            lookupTable[groupCode] = new Epc[MAX_ENTRIES][0];
        }
        if (null == lookupTable[groupCode][classCode] || 0 == lookupTable[groupCode][classCode].length) {
            lookupTable[groupCode][classCode] = new Epc[MAX_ENTRIES];
        }

        for (Epc value : echonetClass.deviceProperties()) {
            lookupTable[groupCode][classCode][value.code()] = value;
        }

        for (Epc value : echonetClass.groupProperties()) {
            lookupTable[groupCode][classCode][value.code()] = value;
        }

        for (Epc value : echonetClass.classProperties()) {
            lookupTable[groupCode][classCode][value.code()] = value;
        }
    }
}
