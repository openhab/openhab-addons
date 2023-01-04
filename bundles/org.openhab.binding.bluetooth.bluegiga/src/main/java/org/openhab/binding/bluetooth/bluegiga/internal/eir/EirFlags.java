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
package org.openhab.binding.bluetooth.bluegiga.internal.eir;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Definition of the EIR Flags field
 *
 * @author Chris Jackson - Initial contribution
 *
 */
@NonNullByDefault
public enum EirFlags {
    UNKNOWN(-1),
    LE_LIMITED_DISCOVERABLE_MODE(0),
    LE_GENERAL_DISCOVERABLE_MODE(1),
    BR_EDR_NOT_SUPPORTED(2),
    SIMULTANEOUS_LE_BDR_CONTROLLER(3),
    SIMULTANEOUS_LE_BDR_HOST(4),
    BIT5(5),
    BIT6(6),
    BIT7(7),
    BIT8(8),
    BIT9(9),
    BIT10(10),
    BIT11(11),
    BIT12(12),
    BIT13(13),
    BIT14(14),
    BIT15(15);

    /**
     * A mapping between the integer code and its corresponding type to
     * facilitate lookup by code.
     */
    private static Map<Integer, EirFlags> codeMapping = new HashMap<>();

    private int key;

    private EirFlags(int key) {
        this.key = key;
    }

    private static void initMapping() {
        codeMapping = new HashMap<>();
        for (EirFlags s : values()) {
            codeMapping.put(s.key, s);
        }
    }

    /**
     * Lookup function based on the type code. Returns null if the code does not exist.
     *
     * @param bluetoothAddressType
     *            the code to lookup
     * @return enumeration value.
     */
    @SuppressWarnings({ "null", "unused" })
    public static EirFlags getEirFlag(int eirFlag) {
        if (codeMapping.isEmpty()) {
            initMapping();
        }

        return codeMapping.getOrDefault(eirFlag, UNKNOWN);
    }

    /**
     * Returns the Bluetooth protocol defined value for this enum
     *
     * @return the EIR Data type key
     */
    public int getKey() {
        return key;
    }
}
