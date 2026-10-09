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
package org.openhab.binding.modbus.hager.internal.modbus;

import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Access.READ_ONLY;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.BOOL;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.U16;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.core.library.unit.Units;

/**
 * The {@link HagerFlowStorageRegisterMap} declares the Modbus registers of the Hager Flow Storage.
 * Addresses and data types are transcribed from the Hager Flow XEM470 Modbus table (FR2_2024_03);
 * the same addresses are used for every Modbus unit id of this role.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class HagerFlowStorageRegisterMap {

    private HagerFlowStorageRegisterMap() {
        // constants-only holder, not instantiable
    }

    private static final String GROUP_STATUS = "status";

    public static final Register STORAGE_INDEX = new Register("storage-index", GROUP_STATUS, 4097, 1, U16, READ_ONLY,
            null, Map.of(), List.of());
    public static final Register STORAGE_DCDC_STATUS = new Register("storage-dcdc-status", GROUP_STATUS, 4098, 1, BOOL,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register STORAGE_BAT_STATUS = new Register("storage-bat-status", GROUP_STATUS, 4099, 1, BOOL,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register BATTERY_STATE_OF_CHARGE = new Register("battery-state-of-charge", GROUP_STATUS, 4100,
            1, U16, READ_ONLY, Units.PERCENT, Map.of(), List.of());
    /** All registers of this role, in the order of the source document. */
    public static final List<Register> ALL = List.of(STORAGE_INDEX, STORAGE_DCDC_STATUS, STORAGE_BAT_STATUS,
            BATTERY_STATE_OF_CHARGE);
}
