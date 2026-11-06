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
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.I32;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.STRING;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.U16;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;

/**
 * The {@link HagerFlowSgReadyRegisterMap} declares the Modbus registers of the Hager Flow SG-Ready Device.
 * Addresses and data types are transcribed from the Hager Flow XEM470 Modbus table (FR2_2024_03);
 * the same addresses are used for every Modbus unit id of this role.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class HagerFlowSgReadyRegisterMap {

    private HagerFlowSgReadyRegisterMap() {
        // constants-only holder, not instantiable
    }

    private static final String GROUP_IDENTITY = "identity";
    private static final String GROUP_STATUS = "status";

    public static final Register SGREADY_INDEX = new Register("sgready-index", GROUP_IDENTITY, 4097, 1, U16, READ_ONLY,
            null, Map.of(), List.of());
    public static final Register SGREADY_NAME = new Register("sgready-name", GROUP_IDENTITY, 4098, 50, STRING,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register SGREADY_ACTIVE = new Register("sgready-active", GROUP_STATUS, 4150, 1, U16, READ_ONLY,
            null, Map.of(0, "Inactive", 1, "active"), List.of());
    public static final Register SGREADY_WORKING = new Register("sgready-working", GROUP_STATUS, 4151, 1, BOOL,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register SGREADY_STATUS = new Register(
            "sgready-status", GROUP_STATUS, 4152, 2, I32, READ_ONLY, null, Map.ofEntries(Map.entry(1, "STATE_1_BLOCK"),
                    Map.entry(2, "STATE_2_NORMAL"), Map.entry(3, "STATE_3_GO"), Map.entry(4, "STATE_4_FORCE_GO")),
            List.of());
    /** All registers of this role, in the order of the source document. */
    public static final List<Register> ALL = List.of(SGREADY_INDEX, SGREADY_NAME, SGREADY_ACTIVE, SGREADY_WORKING,
            SGREADY_STATUS);
}
