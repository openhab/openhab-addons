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
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.BITSTRING;
import static org.openhab.binding.modbus.hager.internal.modbus.ModbusRegisterTypes.DataType.I32;
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
 * The {@link HagerFlowEmsRegisterMap} declares the Modbus registers of the Hager Flow EMS.
 * Addresses and data types are transcribed from the Hager Flow XEM470 Modbus table (FR2_2024_03);
 * the same addresses are used for every Modbus unit id of this role.
 *
 * @author Bernd Weymann - Initial contribution
 */
@NonNullByDefault
public final class HagerFlowEmsRegisterMap {

    private HagerFlowEmsRegisterMap() {
        // constants-only holder, not instantiable
    }

    private static final String GROUP_DEVICE_IDENTIFICATION = "device-identification";
    private static final String GROUP_GENERAL_INFORMATION = "general-information";
    private static final String GROUP_SLAVE_ID_IDENTITY = "slave-id-identity";
    private static final String GROUP_ROOT_MAIN_MEASUREMENTS = "root-main-measurements";

    public static final Register VENDOR_NAME = new Register("vendor-name", GROUP_DEVICE_IDENTIFICATION, 0, 16, STRING,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register PRODUCT_CODE = new Register("product-code", GROUP_DEVICE_IDENTIFICATION, 16, 16,
            STRING, READ_ONLY, null, Map.of(), List.of());
    public static final Register VENDOR_URL = new Register("vendor-url", GROUP_DEVICE_IDENTIFICATION, 32, 16, STRING,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register PRODUCT_SERIAL_NUMBER = new Register("product-serial-number",
            GROUP_DEVICE_IDENTIFICATION, 48, 16, STRING, READ_ONLY, null, Map.of(), List.of());
    public static final Register SW_VERSION = new Register("sw-version", GROUP_DEVICE_IDENTIFICATION, 64, 16, STRING,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register MAX_CURRENT_PHASE = new Register("max-current-phase", GROUP_GENERAL_INFORMATION, 272,
            1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register MAX_CURRENT_PHASE_DERATED = new Register("max-current-phase-derated",
            GROUP_GENERAL_INFORMATION, 273, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register MAX_POWER_PHASE = new Register("max-power-phase", GROUP_GENERAL_INFORMATION, 274, 2,
            U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register MAX_POWER_PHASE_DERATED = new Register("max-power-phase-derated",
            GROUP_GENERAL_INFORMATION, 276, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register MAX_POWER = new Register("max-power", GROUP_GENERAL_INFORMATION, 278, 2, U32,
            READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register MAX_POWER_DERATED = new Register("max-power-derated", GROUP_GENERAL_INFORMATION, 280,
            2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register EVCS_COUNT = new Register("evcs-count", GROUP_GENERAL_INFORMATION, 306, 1, U16,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register EVCS_LIST = new Register("evcs-list", GROUP_GENERAL_INFORMATION, 307, 13, BITSTRING,
            READ_ONLY, null, Map.of(),
            List.of(new BitFlag(0, "evcs-list-evcs-1", "connected", "not connected"),
                    new BitFlag(1, "evcs-list-evcs-2", "connected", "not connected"),
                    new BitFlag(2, "evcs-list-evcs-3", "connected", "not connected"),
                    new BitFlag(3, "evcs-list-evcs-4", "connected", "not connected"),
                    new BitFlag(4, "evcs-list-evcs-5", "connected", "not connected"),
                    new BitFlag(5, "evcs-list-evcs-6", "connected", "not connected"),
                    new BitFlag(6, "evcs-list-evcs-7", "connected", "not connected"),
                    new BitFlag(7, "evcs-list-evcs-8", "connected", "not connected")));
    public static final Register PHASE_NUMBER = new Register("phase-number", GROUP_GENERAL_INFORMATION, 512, 1, U16,
            READ_ONLY, null, Map.of(), List.of());
    public static final Register SUN_POWER_PRIORITY = new Register("sun-power-priority", GROUP_GENERAL_INFORMATION, 513,
            1, U16, READ_ONLY, null, Map.of(),
            List.of(new BitFlag(0, "sun-power-priority-battery-first", "Battery First", "Car first"),
                    new BitFlag(1, "sun-power-priority-allow-battery-discharge-into-car",
                            "allow battery discharge into car", "forbid battery discharge into car")));
    public static final Register BLACKOUT_ACTIVE = new Register("blackout-active", GROUP_GENERAL_INFORMATION, 514, 1,
            U16, READ_ONLY, null, Map.ofEntries(Map.entry(0, "NOT_POSSIBLE"), Map.entry(1, "ACTIVE"),
                    Map.entry(2, "NOT_ACTIVE"), Map.entry(3, "NOT_AVAILABLE"), Map.entry(4, "SWITCH_IN_ISLAND_STATE")),
            List.of());
    public static final Register SGREADY_BRAIN_DISABLE = new Register("sgready-brain-disable",
            GROUP_GENERAL_INFORMATION, 515, 1, U16, READ_ONLY, null,
            Map.ofEntries(Map.entry(0, "SGReady Brain enabled"), Map.entry(1, "SGReady Brain disabled")), List.of());
    public static final Register SGREADY_POWER = new Register("sgready-power", GROUP_GENERAL_INFORMATION, 516, 2, U32,
            READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register SLAVEID_TYPE = new Register("slaveid-type", GROUP_SLAVE_ID_IDENTITY, 4096, 1, U16,
            READ_ONLY, null, Map.ofEntries(Map.entry(0, "Main system (EMS)"), Map.entry(1, "EVSE"),
                    Map.entry(2, "Storage"), Map.entry(3, "Power Meter"), Map.entry(4, "SG-Ready")),
            List.of());
    public static final Register SLAVE_ID_INDEX_BY_TYPE = new Register("slave-id-index-by-type",
            GROUP_SLAVE_ID_IDENTITY, 4097, 1, U16, READ_ONLY, null, Map.of(), List.of());
    public static final Register ROOT_MAIN_CURRENT_SUM = new Register("root-main-current-sum",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4098, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register ROOT_MAIN_CURRENT_L1 = new Register("root-main-current-l1",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4099, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register ROOT_MAIN_CURRENT_L2 = new Register("root-main-current-l2",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4100, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register ROOT_MAIN_CURRENT_L3 = new Register("root-main-current-l3",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4101, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    // The Modbus table documents U32, but the value can become negative and is therefore read as
    // I32, see https://github.com/openhab/openhab-addons/issues/18711
    public static final Register ROOT_MAIN_POWER_SUM = new Register("root-main-power-sum", GROUP_ROOT_MAIN_MEASUREMENTS,
            4102, 2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    // The Modbus table documents U32, but the value can become negative and is therefore read as
    // I32, see https://github.com/openhab/openhab-addons/issues/18711
    public static final Register ROOT_MAIN_POWER_L1 = new Register("root-main-power-l1", GROUP_ROOT_MAIN_MEASUREMENTS,
            4104, 2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    // The Modbus table documents U32, but the value can become negative and is therefore read as
    // I32, see https://github.com/openhab/openhab-addons/issues/18711
    public static final Register ROOT_MAIN_POWER_L2 = new Register("root-main-power-l2", GROUP_ROOT_MAIN_MEASUREMENTS,
            4106, 2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    // The Modbus table documents U32, but the value can become negative and is therefore read as
    // I32, see https://github.com/openhab/openhab-addons/issues/18711
    public static final Register ROOT_MAIN_POWER_L3 = new Register("root-main-power-l3", GROUP_ROOT_MAIN_MEASUREMENTS,
            4108, 2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register ALL_EVCS_CURRENT_SUM = new Register("all-evcs-current-sum",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4110, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register ALL_EVCS_POWER_SUM = new Register("all-evcs-power-sum", GROUP_ROOT_MAIN_MEASUREMENTS,
            4114, 2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register ALL_PV_CURRENT_SUM = new Register("all-pv-current-sum", GROUP_ROOT_MAIN_MEASUREMENTS,
            4122, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register ALL_PV_POWER_SUM = new Register("all-pv-power-sum", GROUP_ROOT_MAIN_MEASUREMENTS, 4126,
            2, U32, READ_ONLY, Units.WATT, Map.of(), List.of());
    // The Modbus table documents address 4134, but the register is at 4133, see
    // https://github.com/openhab/openhab-addons/issues/18711#issuecomment-6082009683
    public static final Register ALL_BATTERY_CURRENT_SUM = new Register("all-battery-current-sum",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4133, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register ALL_BATTERY_POWER_SUM = new Register("all-battery-power-sum",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4138, 2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register BATTERY_STATE_OF_CHARGE = new Register("battery-state-of-charge",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4146, 1, U16, READ_ONLY, Units.PERCENT, Map.of(), List.of());
    public static final Register ALL_HOUSE_CONSUMPTION_CURRENT_SUM = new Register("all-house-consumption-current-sum",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4147, 1, U16, READ_ONLY, Units.AMPERE, Map.of(), List.of());
    public static final Register ALL_HOUSE_CONSUMPTION_POWER_SUM = new Register("all-house-consumption-power-sum",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4151, 2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register AUTARKY_LAST_HOUR = new Register("autarky-last-hour", GROUP_ROOT_MAIN_MEASUREMENTS,
            4224, 1, U16, READ_ONLY, Units.PERCENT, Map.of(), List.of());
    public static final Register SELF_CONSUMPTION_LAST_HOUR = new Register("self-consumption-last-hour",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4225, 1, U16, READ_ONLY, Units.PERCENT, Map.of(), List.of());
    public static final Register MARGIN_TO_MAX_POWER_TOTAL = new Register("margin-to-max-power-total",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4240, 2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register MARGIN_TO_MAX_POWER_L1 = new Register("margin-to-max-power-l1",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4242, 2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register MARGIN_TO_MAX_POWER_L2 = new Register("margin-to-max-power-l2",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4244, 2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    public static final Register MARGIN_TO_MAX_POWER_L3 = new Register("margin-to-max-power-l3",
            GROUP_ROOT_MAIN_MEASUREMENTS, 4246, 2, I32, READ_ONLY, Units.WATT, Map.of(), List.of());
    /** All registers of this role, in the order of the source document. */
    public static final List<Register> ALL = List.of(VENDOR_NAME, PRODUCT_CODE, VENDOR_URL, PRODUCT_SERIAL_NUMBER,
            SW_VERSION, MAX_CURRENT_PHASE, MAX_CURRENT_PHASE_DERATED, MAX_POWER_PHASE, MAX_POWER_PHASE_DERATED,
            MAX_POWER, MAX_POWER_DERATED, EVCS_COUNT, EVCS_LIST, PHASE_NUMBER, SUN_POWER_PRIORITY, BLACKOUT_ACTIVE,
            SGREADY_BRAIN_DISABLE, SGREADY_POWER, SLAVEID_TYPE, SLAVE_ID_INDEX_BY_TYPE, ROOT_MAIN_CURRENT_SUM,
            ROOT_MAIN_CURRENT_L1, ROOT_MAIN_CURRENT_L2, ROOT_MAIN_CURRENT_L3, ROOT_MAIN_POWER_SUM, ROOT_MAIN_POWER_L1,
            ROOT_MAIN_POWER_L2, ROOT_MAIN_POWER_L3, ALL_EVCS_CURRENT_SUM, ALL_EVCS_POWER_SUM, ALL_PV_CURRENT_SUM,
            ALL_PV_POWER_SUM, ALL_BATTERY_CURRENT_SUM, ALL_BATTERY_POWER_SUM, BATTERY_STATE_OF_CHARGE,
            ALL_HOUSE_CONSUMPTION_CURRENT_SUM, ALL_HOUSE_CONSUMPTION_POWER_SUM, AUTARKY_LAST_HOUR,
            SELF_CONSUMPTION_LAST_HOUR, MARGIN_TO_MAX_POWER_TOTAL, MARGIN_TO_MAX_POWER_L1, MARGIN_TO_MAX_POWER_L2,
            MARGIN_TO_MAX_POWER_L3);
}
