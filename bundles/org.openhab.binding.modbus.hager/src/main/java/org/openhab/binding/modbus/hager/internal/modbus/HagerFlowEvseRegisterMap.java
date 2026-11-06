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
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Access.READ_WRITE;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.BITFIELD;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.BOOL;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.STRING;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.U16;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.U32;

import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.BitFlag;
import org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.Register;
import org.openhab.core.library.unit.Units;

/**
 * The {@link HagerFlowEvseRegisterMap} declares the Modbus registers of the Hager Flow EVSE.
 * Addresses and data types are transcribed from the Hager Flow XEM470 Modbus table (FR2_2024_03);
 * the same addresses are used for every Modbus unit id of this role.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class HagerFlowEvseRegisterMap {

    private HagerFlowEvseRegisterMap() {
        // constants-only holder, not instantiable
    }

    private static final String GROUP_IDENTITY = "identity";
    private static final String GROUP_GENERAL_STATUS_AND_CONFIGURATION = "general-status-and-configuration";
    private static final String GROUP_CHARGING_SESSION_INFORMATION = "charging-session-information";
    private static final String GROUP_INSTANTANEOUS_MEASURES = "instantaneous-measures";

    public static final Register EVCS_NUMBER = new Register("evcs-number", GROUP_IDENTITY, 4097, 1, U16, READ_ONLY,
            null, Map.of(), List.of());
    public static final Register EVCS_TYPE = new Register("evcs-type", GROUP_IDENTITY, 4098, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "virtual"), Map.entry(1, "E3DC wallbox"), Map.entry(2, "Easy Connect wallbox"),
                    Map.entry(4, "wallbox RSCP group (farming)"), Map.entry(5, "Edison Connect wallbox"),
                    Map.entry(6, "Multi Connect wallbox"), Map.entry(7, "Eebus Wallbox")),
            List.of());
    public static final Register EVCS_NAME = new Register("evcs-name", GROUP_IDENTITY, 4099, 50, STRING, READ_ONLY,
            null, Map.of(), List.of());
    public static final Register EVCS_SERIAL = new Register("evcs-serial", GROUP_IDENTITY, 4149, 16, STRING, READ_ONLY,
            null, Map.of(), List.of());
    public static final Register EVCS_FIRMWARE_VERSION = new Register("evcs-firmware-version", GROUP_IDENTITY, 4165, 32,
            STRING, READ_ONLY, null, Map.of(), List.of());
    public static final Register EVCS_STATE = new Register("evcs-state", GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4608,
            1, U16, READ_ONLY, null, Map.of(),
            List.of(new BitFlag(0, "evcs-state-normal-mode", "normal mode", "not in normal mode"),
                    new BitFlag(1, "evcs-state-default-mode", "default mode (CRC wrong parameter value range)",
                            "not in default mode"),
                    new BitFlag(2, "evcs-state-sun-mode-active", "sun mode active", "sun mode inactive"),
                    new BitFlag(3, "evcs-state-temperature-derating-active", "temperature derating active",
                            "temperature derating inactive"),
                    new BitFlag(4, "evcs-state-schuko-active", "Schuko active", "Schuko inactive"),
                    new BitFlag(5, "evcs-state-crc-wrong-meter-value-range", "CRC wrong meter value range",
                            "meter value range OK"),
                    new BitFlag(6, "evcs-state-limited-operation", "limited operation (not all modes active)",
                            "full operation")));
    public static final Register EVCS_ENERGY_ALL = new Register("evcs-energy-all",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4609, 2, U32, READ_ONLY, Units.WATT_HOUR, Map.of(), List.of());
    public static final Register EVCS_SOLAR_ENERGY = new Register("evcs-solar-energy",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4611, 2, U32, READ_ONLY, Units.WATT_HOUR, Map.of(), List.of());
    public static final Register EVCS_CONNECTION_STATE = new Register("evcs-connection-state",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4613, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "EVCS disconnected"), Map.entry(1, "EVCS connected")), List.of());
    public static final Register EVCS_WORKING_STATE = new Register("evcs-working-state",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4614, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "EVCS not working properly"), Map.entry(1, "EVCS working normally")), List.of());
    public static final Register EVCS_SERVICE_STATE = new Register("evcs-service-state",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4615, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "no upgrade ongoing"), Map.entry(1, "firmware upgrade ongoing")), List.of());
    public static final Register EVCS_POWER_METER_CONNECTION_STATE = new Register("evcs-power-meter-connection-state",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4616, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "power meter disconnected"), Map.entry(1, "power meter connected")), List.of());
    public static final Register EVCS_POWER_METER_WORKING_STATE = new Register("evcs-power-meter-working-state",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4617, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "power meter not working properly"),
                    Map.entry(1, "power meter working normally")),
            List.of());
    public static final Register EVCS_POWER_METER_SERVICE_STATE = new Register("evcs-power-meter-service-state",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4618, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "no upgrade ongoing"), Map.entry(1, "firmware upgrade ongoing")), List.of());
    public static final Register EVCS_AVAILABILITY = new Register("evcs-availability",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4619, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "Unavailable (charge is forbidden)"),
                    Map.entry(1, "Available (charge might be authorized)")),
            List.of());
    public static final Register EVCS_STATION_ENABLED = new Register("evcs-station-enabled",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4620, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "Disabled"), Map.entry(1, "Enabled")), List.of());
    public static final Register EVCS_MAX_CHARGING_POWER = new Register("evcs-max-charging-power",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4621, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register EVCS_MAX_CHARGING_POWER_PER_PHASE = new Register("evcs-max-charging-power-per-phase",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4623, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register EVCS_MAX_CHARGING_CURRENT = new Register("evcs-max-charging-current",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4625, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register EVCS_MIN_CHARGING_POWER = new Register("evcs-min-charging-power",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4626, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register EVCS_MIN_CHARGING_POWER_PER_PHASE = new Register("evcs-min-charging-power-per-phase",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4628, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register EVCS_MIN_CHARGING_CURRENT = new Register("evcs-min-charging-current",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4630, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register EVCS_BOOST_MODE = new Register("evcs-boost-mode",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4631, 1, BOOL, READ_WRITE, null, Map.of(), List.of());
    public static final Register EVCS_PHASE_MANAGEMENT = new Register("evcs-phase-management",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4632, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(1, "mono"), Map.entry(3, "tri"), Map.entry(4, "auto")), List.of());
    public static final Register EVCS_CHARGE_STRATEGY = new Register("evcs-charge-strategy",
            GROUP_GENERAL_STATUS_AND_CONFIGURATION, 4633, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "Boost (Immediate)"), Map.entry(1, "Delayed"), Map.entry(2, "disabled")),
            List.of());
    public static final Register EVCS_STATE_SESSION = new Register("evcs-state-session",
            GROUP_CHARGING_SESSION_INFORMATION, 4864, 1, U16, READ_ONLY, null, Map.of(),
            List.of(new BitFlag(2, "evcs-state-session-sun-mode-active", "sun mode active", "sun mode inactive"),
                    new BitFlag(3, "evcs-state-session-temperature-derating-active", "temperature derating active",
                            "temperature derating inactive")));
    public static final Register CHARGING_SESSION_ID = new Register("charging-session-id",
            GROUP_CHARGING_SESSION_INFORMATION, 4865, 64, STRING, READ_ONLY, null, Map.of(), List.of());
    public static final Register CHARGING_SESSION_BADGE_ID = new Register("charging-session-badge-id",
            GROUP_CHARGING_SESSION_INFORMATION, 4929, 16, STRING, READ_ONLY, null, Map.of(), List.of());
    public static final Register STATUS = new Register("status", GROUP_CHARGING_SESSION_INFORMATION, 4945, 4, BITFIELD,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register ACTIVE_PHASE = new Register("active-phase", GROUP_CHARGING_SESSION_INFORMATION, 4949,
            1, U16, READ_ONLY, null, Map.of(), List.of());
    public static final Register ENERGY_SESSION = new Register("energy-session", GROUP_CHARGING_SESSION_INFORMATION,
            4950, 2, U32, READ_ONLY, Units.WATT_HOUR, Map.of(), List.of());
    public static final Register GRID_ENERGY_SESSION = new Register("grid-energy-session",
            GROUP_CHARGING_SESSION_INFORMATION, 4952, 2, U32, READ_ONLY, Units.WATT_HOUR, Map.of(), List.of());
    public static final Register SOLAR_ENERGY_SESSION = new Register("solar-energy-session",
            GROUP_CHARGING_SESSION_INFORMATION, 4954, 2, U32, READ_ONLY, Units.WATT_HOUR, Map.of(), List.of());
    public static final Register TOTAL_CHARGING_CURRENT = new Register("total-charging-current",
            GROUP_INSTANTANEOUS_MEASURES, 5120, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register CHARGING_CURRENT_L1 = new Register("charging-current-l1", GROUP_INSTANTANEOUS_MEASURES,
            5121, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register CHARGING_CURRENT_L2 = new Register("charging-current-l2", GROUP_INSTANTANEOUS_MEASURES,
            5122, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register CHARGING_CURRENT_L3 = new Register("charging-current-l3", GROUP_INSTANTANEOUS_MEASURES,
            5123, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register CHARGING_POWER_TOTAL = new Register("charging-power-total",
            GROUP_INSTANTANEOUS_MEASURES, 5124, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register CHARGING_POWER_L1 = new Register("charging-power-l1", GROUP_INSTANTANEOUS_MEASURES,
            5126, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register CHARGING_POWER_L2 = new Register("charging-power-l2", GROUP_INSTANTANEOUS_MEASURES,
            5128, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register CHARGING_POWER_L3 = new Register("charging-power-l3", GROUP_INSTANTANEOUS_MEASURES,
            5130, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register ASSIGN_POWER_TOTAL = new Register("assign-power-total", GROUP_INSTANTANEOUS_MEASURES,
            5132, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register ASSIGNED_POWER_L1 = new Register("assigned-power-l1", GROUP_INSTANTANEOUS_MEASURES,
            5134, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register ASSIGNED_POWER_L2 = new Register("assigned-power-l2", GROUP_INSTANTANEOUS_MEASURES,
            5136, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register ASSIGNED_POWER_L3 = new Register("assigned-power-l3", GROUP_INSTANTANEOUS_MEASURES,
            5138, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    /** All registers of this role, in the order of the source document. */
    public static final List<Register> ALL = List.of(EVCS_NUMBER, EVCS_TYPE, EVCS_NAME, EVCS_SERIAL,
            EVCS_FIRMWARE_VERSION, EVCS_STATE, EVCS_ENERGY_ALL, EVCS_SOLAR_ENERGY, EVCS_CONNECTION_STATE,
            EVCS_WORKING_STATE, EVCS_SERVICE_STATE, EVCS_POWER_METER_CONNECTION_STATE, EVCS_POWER_METER_WORKING_STATE,
            EVCS_POWER_METER_SERVICE_STATE, EVCS_AVAILABILITY, EVCS_STATION_ENABLED, EVCS_MAX_CHARGING_POWER,
            EVCS_MAX_CHARGING_POWER_PER_PHASE, EVCS_MAX_CHARGING_CURRENT, EVCS_MIN_CHARGING_POWER,
            EVCS_MIN_CHARGING_POWER_PER_PHASE, EVCS_MIN_CHARGING_CURRENT, EVCS_BOOST_MODE, EVCS_PHASE_MANAGEMENT,
            EVCS_CHARGE_STRATEGY, EVCS_STATE_SESSION, CHARGING_SESSION_ID, CHARGING_SESSION_BADGE_ID, STATUS,
            ACTIVE_PHASE, ENERGY_SESSION, GRID_ENERGY_SESSION, SOLAR_ENERGY_SESSION, TOTAL_CHARGING_CURRENT,
            CHARGING_CURRENT_L1, CHARGING_CURRENT_L2, CHARGING_CURRENT_L3, CHARGING_POWER_TOTAL, CHARGING_POWER_L1,
            CHARGING_POWER_L2, CHARGING_POWER_L3, ASSIGN_POWER_TOTAL, ASSIGNED_POWER_L1, ASSIGNED_POWER_L2,
            ASSIGNED_POWER_L3);
}
