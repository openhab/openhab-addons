/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.modbus.sungrow.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SungrowConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nagy Attila Gábor - Initial contribution
 * @author Ferdinand Schwenk - reused for sungrow bundle
 */
@NonNullByDefault
public class SungrowConstants {

    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_INVERTER_SHRT = new ThingTypeUID(BINDING_ID, "inverter-sungrow-shrt");

    // Block types
    public static final int COMMON_BLOCK = 1;
    public static final String INVERTER_SHRT = "Hybrid Inverter";
    public static final int FINAL_BLOCK = 0xffff;

    /**
     * Map of the supported thing type uids, with their block type id
     */
    public static final Map<String, ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashMap<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.put(INVERTER_SHRT, THING_TYPE_INVERTER_SHRT);
    }

    // properties
    public static final String PROPERTY_VENDOR = "vendor";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_DEVICE_TYPE = "deviceType";
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_PHASE_COUNT = "phaseCount";
    public static final String PROPERTY_SERIAL_NUMBER = "serialNumber";

    // Channel group ids
    public static final String GROUP_DEVICE_INFO = "deviceInformation";
    public static final String GROUP_AC_GENERAL = "acGeneral";
    public static final String GROUP_AC_PHASE_A = "acPhaseA";
    public static final String GROUP_AC_PHASE_B = "acPhaseB";
    public static final String GROUP_AC_PHASE_C = "acPhaseC";
    public static final String GROUP_POWER_INFO = "powerInfo";
    public static final String GROUP_MPPT1 = "mppt1";
    public static final String GROUP_MPPT2 = "mppt2";
    public static final String GROUP_BATTERY = "battery";

    // List of all Channel ids in device information group
    public static final String CHANNEL_DEVICE_TYPE = "device-type";
    public static final String CHANNEL_OUTPUT_TYPE = "output-type";
    public static final String CHANNEL_PHASE_CONFIGURATION = "phase-configuration";
    public static final String CHANNEL_INSIDE_TEMPERATURE = "inside-temperature";
    public static final String CHANNEL_NOMINAL_OUTPUT_POWER = "nominal-output-power";
    public static final String CHANNEL_SYSTEM_STATE = "system-state";
    public static final String CHANNEL_RUNNING_STATE = "running-state";

    // List of channel ids in AC general group for inverter
    public static final String CHANNEL_GRID_STATE = "ac-grid-state";
    public static final String CHANNEL_AC_TOTAL_CURRENT = "ac-total-current";
    public static final String CHANNEL_AC_POWER = "ac-power";
    public static final String CHANNEL_AC_FREQUENCY = "ac-frequency";
    public static final String CHANNEL_AC_APPARENT_POWER = "ac-apparent-power";
    public static final String CHANNEL_AC_REACTIVE_POWER = "ac-reactive-power";
    public static final String CHANNEL_AC_POWER_FACTOR = "ac-power-factor";
    public static final String CHANNEL_AC_LIFETIME_ENERGY = "ac-lifetime-energy";

    // List of channels ids in AC general group for meter
    public static final String CHANNEL_AC_AVERAGE_VOLTAGE_TO_N = "ac-average-voltage-to-n";
    public static final String CHANNEL_AC_AVERAGE_VOLTAGE_TO_NEXT = "ac-average-voltage-to-next";
    public static final String CHANNEL_AC_TOTAL_REAL_POWER = "ac-total-real-power";
    public static final String CHANNEL_AC_TOTAL_APPARENT_POWER = "ac-total-apparent-power";
    public static final String CHANNEL_AC_TOTAL_REACTIVE_POWER = "ac-total-reactive-power";
    public static final String CHANNEL_AC_AVERAGE_POWER_FACTOR = "ac-average-power-factor";
    public static final String CHANNEL_AC_TOTAL_EXPORTED_REAL_ENERGY = "ac-total-exported-real-energy";
    public static final String CHANNEL_AC_TOTAL_IMPORTED_REAL_ENERGY = "ac-total-imported-real-energy";
    public static final String CHANNEL_AC_TOTAL_EXPORTED_APPARENT_ENERGY = "ac-total-exported-apparent-energy";
    public static final String CHANNEL_AC_TOTAL_IMPORTED_APPARENT_ENERGY = "ac-total-imported-apparent-energy";
    public static final String CHANNEL_AC_TOTAL_IMPORTED_REACTIVE_ENERGY_Q1 = "ac-total-imported-reactive-energy-q1";
    public static final String CHANNEL_AC_TOTAL_IMPORTED_REACTIVE_ENERGY_Q2 = "ac-total-imported-reactive-energy-q2";
    public static final String CHANNEL_AC_TOTAL_EXPORTED_REACTIVE_ENERGY_Q3 = "ac-total-exported-reactive-energy-q3";
    public static final String CHANNEL_AC_TOTAL_EXPORTED_REACTIVE_ENERGY_Q4 = "ac-total-exported-reactive-energy-q4";

    // List of channel ids in AC phase group for inverter
    public static final String CHANNEL_AC_PHASE_CURRENT = "ac-phase-current";
    public static final String CHANNEL_AC_VOLTAGE_TO_NEXT = "ac-voltage-to-next";
    public static final String CHANNEL_AC_VOLTAGE_TO_N = "ac-voltage-to-n";

    // List of channel ids in Power Info group for inverter
    public static final String CHANNEL_DC_TOTAL_POWER = "dc-total-power";
    public static final String CHANNEL_LOAD_POWER = "load-power";
    public static final String CHANNEL_GRID_POWER = "grid-power";

    // List of channel ids in MPPT group for inverter
    public static final String CHANNEL_DC_CURRENT = "dc-current";
    public static final String CHANNEL_DC_VOLTAGE = "dc-voltage";
    public static final String CHANNEL_DC_POWER = "dc-power";

    // List of channel ids in AC phase group for meter
    public static final String CHANNEL_AC_REAL_POWER = "ac-real-power";
    public static final String CHANNEL_AC_EXPORTED_REAL_ENERGY = "ac-exported-real-energy";
    public static final String CHANNEL_AC_IMPORTED_REAL_ENERGY = "ac-imported-real-energy";
    public static final String CHANNEL_AC_EXPORTED_APPARENT_ENERGY = "ac-exported-apparent-energy";
    public static final String CHANNEL_AC_IMPORTED_APPARENT_ENERGY = "ac-imported-apparent-energy";
    public static final String CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q1 = "ac-imported-reactive-energy-q1";
    public static final String CHANNEL_AC_IMPORTED_REACTIVE_ENERGY_Q2 = "ac-imported-reactive-energy-q2";
    public static final String CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q3 = "ac-exported-reactive-energy-q3";
    public static final String CHANNEL_AC_EXPORTED_REACTIVE_ENERGY_Q4 = "ac-exported-reactive-energy-q4";

    // List of channel ids in Battery group for inverter
    public static final String CHANNEL_BATTERY_VOLTAGE = "battery-voltage";
    public static final String CHANNEL_BATTERY_CURRENT = "battery-current";
    public static final String CHANNEL_BATTERY_POWER = "battery-power";
    public static final String CHANNEL_BATTERY_LEVEL = "battery-level";
    public static final String CHANNEL_BATTERY_HEALTH = "battery-health";
    public static final String CHANNEL_BATTERY_TEMPERATURE = "battery-temperature";

    // Running State Bits
    public static final Integer RS_PV_POWER_GENERATING = (1 << 0);
    public static final Integer RS_BATTERY_CHARGING = (1 << 1);
    public static final Integer RS_BATTERY_DISCHARGING = (1 << 2);
    public static final Integer RS_POSITIV_POWER_LOAD = (1 << 3);
    public static final Integer RS_POWER_FEED_IN_GRID = (1 << 4);
    public static final Integer RS_IMPORT_POWER_FROM_GRID = (1 << 5);
    public static final Integer RS_NEGATIV_LOAD_POWER = (1 << 7);

    // Expected Sungrow ID This is a magic constant to distinguish Sungrow compatible
    // devices from other modbus devices
    public static final long SUNGROW_ID = 0x53756e53;
    // Size of SUNGROW ID in words
    public static final int SUNGROW_ID_SIZE = 2;
    // Size of any block header in words
    public static final int MODEL_HEADER_SIZE = 2;
}
