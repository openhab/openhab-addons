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
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.F64;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.I32;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.STRING;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.U16;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.BitFlag;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.core.library.unit.Units;

/**
 * The {@link HagerFlowPowermeterRegisterMap} declares the Modbus registers of the Hager Flow PowerMeter.
 * Addresses and data types are transcribed from the Hager Flow XEM470 Modbus table (FR2_2024_03);
 * the same addresses are used for every Modbus unit id of this role.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class HagerFlowPowermeterRegisterMap {

    private HagerFlowPowermeterRegisterMap() {
        // constants-only holder, not instantiable
    }

    private static final String GROUP_IDENTITY = "identity";
    private static final String GROUP_STATUS = "status";
    private static final String GROUP_MEASUREMENTS = "measurements";

    public static final Register POWERMETER_ID = new Register("powermeter-id", GROUP_IDENTITY, 4097, 1, U16, READ_ONLY,
            null, Map.of(), List.of());
    public static final Register POWERMETER_NAME = new Register("powermeter-name", GROUP_IDENTITY, 4098, 50, STRING,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register POWERMETER_TYPE = new Register("powermeter-type", GROUP_IDENTITY, 4148, 1, U16,
            READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "UNDEFINED"), Map.entry(1, "ROOT"), Map.entry(2, "ADDITIONAL"),
                    Map.entry(3, "ADDITIONAL PRODUCTION"), Map.entry(4, "ADDITIONAL CONSUMPTION"), Map.entry(5, "FARM"),
                    Map.entry(6, "UNUSED"), Map.entry(7, "WALLBOX"), Map.entry(8, "FARM ADDITIONAL")),
            List.of());
    public static final Register POWERMETER_CONNECTION_STATUS = new Register("powermeter-connection-status",
            GROUP_STATUS, 4149, 1, BOOL, READ_ONLY, null, Map.of(), List.of());
    public static final Register POWERMETER_WORKING_STATUS = new Register("powermeter-working-status", GROUP_STATUS,
            4150, 1, BOOL, READ_ONLY, null, Map.of(), List.of());
    public static final Register POWERMETER_IN_SERVICE_STATUS = new Register("powermeter-in-service-status",
            GROUP_STATUS, 4151, 1, BOOL, READ_ONLY, null, Map.of(), List.of());
    public static final Register POWERMETER_ACTIVE_PHASE = new Register("powermeter-active-phase", GROUP_STATUS, 4152,
            1, U16, READ_ONLY, null, Map.of(),
            List.of(new BitFlag(0, "powermeter-active-phase-l1-active", "L1 active", "L1 inactive"),
                    new BitFlag(1, "powermeter-active-phase-l2-active", "L2 active", "L2 inactive"),
                    new BitFlag(2, "powermeter-active-phase-l3-active", "L3 active", "L3 inactive")));
    public static final Register POWERMETER_POWER_SUM = new Register("powermeter-power-sum", GROUP_MEASUREMENTS, 4153,
            2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register POWERMETER_POWER_L1 = new Register("powermeter-power-l1", GROUP_MEASUREMENTS, 4155, 2,
            I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register POWERMETER_POWER_L2 = new Register("powermeter-power-l2", GROUP_MEASUREMENTS, 4157, 2,
            I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register POWERMETER_POWER_L3 = new Register("powermeter-power-l3", GROUP_MEASUREMENTS, 4159, 2,
            I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register POWERMETER_VOLTAGE_L1 = new Register("powermeter-voltage-l1", GROUP_MEASUREMENTS, 4161,
            1, U16, READ_ONLY, Units.VOLT, Map.of(), List.of());
    public static final Register POWERMETER_VOLTAGE_L2 = new Register("powermeter-voltage-l2", GROUP_MEASUREMENTS, 4162,
            1, U16, READ_ONLY, Units.VOLT, Map.of(), List.of());
    public static final Register POWERMETER_VOLTAGE_L3 = new Register("powermeter-voltage-l3", GROUP_MEASUREMENTS, 4163,
            1, U16, READ_ONLY, Units.VOLT, Map.of(), List.of());
    public static final Register POWERMETER_ENERGY_SUM = new Register("powermeter-energy-sum", GROUP_MEASUREMENTS, 4164,
            4, F64, READ_ONLY, Units.WATT_HOUR, Map.of(), List.of());
    public static final Register POWERMETER_ENERGY_L1 = new Register("powermeter-energy-l1", GROUP_MEASUREMENTS, 4168,
            4, F64, READ_ONLY, Units.WATT_HOUR, Map.of(), List.of());
    public static final Register POWERMETER_ENERGY_L2 = new Register("powermeter-energy-l2", GROUP_MEASUREMENTS, 4172,
            4, F64, READ_ONLY, Units.WATT_HOUR, Map.of(), List.of());
    public static final Register POWERMETER_ENERGY_L3 = new Register("powermeter-energy-l3", GROUP_MEASUREMENTS, 4176,
            4, F64, READ_ONLY, Units.WATT_HOUR, Map.of(), List.of());
    /** All registers of this role, in the order of the source document. */
    public static final List<Register> ALL = List.of(POWERMETER_ID, POWERMETER_NAME, POWERMETER_TYPE,
            POWERMETER_CONNECTION_STATUS, POWERMETER_WORKING_STATUS, POWERMETER_IN_SERVICE_STATUS,
            POWERMETER_ACTIVE_PHASE, POWERMETER_POWER_SUM, POWERMETER_POWER_L1, POWERMETER_POWER_L2,
            POWERMETER_POWER_L3, POWERMETER_VOLTAGE_L1, POWERMETER_VOLTAGE_L2, POWERMETER_VOLTAGE_L3,
            POWERMETER_ENERGY_SUM, POWERMETER_ENERGY_L1, POWERMETER_ENERGY_L2, POWERMETER_ENERGY_L3);
}
