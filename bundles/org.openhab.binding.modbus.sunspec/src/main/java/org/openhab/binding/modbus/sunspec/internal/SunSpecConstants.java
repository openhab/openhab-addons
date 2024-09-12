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
package org.openhab.binding.modbus.sunspec.internal;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link SunSpecConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Nagy Attila Gábor - Initial contribution
 */
@NonNullByDefault
public class SunSpecConstants {

    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_INVERTER_SINGLE_PHASE = new ThingTypeUID(BINDING_ID,
            "inverter-single-phase");
    public static final ThingTypeUID THING_TYPE_INVERTER_SPLIT_PHASE = new ThingTypeUID(BINDING_ID,
            "inverter-split-phase");
    public static final ThingTypeUID THING_TYPE_INVERTER_THREE_PHASE = new ThingTypeUID(BINDING_ID,
            "inverter-three-phase");
    public static final ThingTypeUID THING_TYPE_METER_SINGLE_PHASE = new ThingTypeUID(BINDING_ID, "meter-single-phase");
    public static final ThingTypeUID THING_TYPE_METER_SPLIT_PHASE = new ThingTypeUID(BINDING_ID, "meter-split-phase");
    public static final ThingTypeUID THING_TYPE_METER_WYE_PHASE = new ThingTypeUID(BINDING_ID, "meter-wye-phase");
    public static final ThingTypeUID THING_TYPE_METER_DELTA_PHASE = new ThingTypeUID(BINDING_ID, "meter-delta-phase");

    // Block types
    public static final int COMMON_BLOCK = 1;
    public static final int INVERTER_SINGLE_PHASE = 101;
    public static final int INVERTER_SPLIT_PHASE = 102;
    public static final int INVERTER_THREE_PHASE = 103;
    public static final int METER_SINGLE_PHASE = 201;
    public static final int METER_SPLIT_PHASE = 202;
    public static final int METER_WYE_PHASE = 203;
    public static final int METER_DELTA_PHASE = 204;
    public static final int FINAL_BLOCK = 0xffff;

    /**
     * Map of the supported thing type uids, with their block type id
     */
    public static final Map<Integer, ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashMap<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.put(INVERTER_SINGLE_PHASE, THING_TYPE_INVERTER_SINGLE_PHASE);
        SUPPORTED_THING_TYPES_UIDS.put(INVERTER_SPLIT_PHASE, THING_TYPE_INVERTER_SPLIT_PHASE);
        SUPPORTED_THING_TYPES_UIDS.put(INVERTER_THREE_PHASE, THING_TYPE_INVERTER_THREE_PHASE);
        SUPPORTED_THING_TYPES_UIDS.put(METER_SINGLE_PHASE, THING_TYPE_METER_SINGLE_PHASE);
        SUPPORTED_THING_TYPES_UIDS.put(METER_SPLIT_PHASE, THING_TYPE_METER_SPLIT_PHASE);
        SUPPORTED_THING_TYPES_UIDS.put(METER_WYE_PHASE, THING_TYPE_METER_WYE_PHASE);
        SUPPORTED_THING_TYPES_UIDS.put(METER_DELTA_PHASE, THING_TYPE_METER_DELTA_PHASE);
    }

    // properties
    public static final String PROPERTY_VENDOR = "vendor";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_VERSION = "version";
    public static final String PROPERTY_PHASE_COUNT = "phaseCount";
    public static final String PROPERTY_SERIAL_NUMBER = "serialNumber";
    public static final String PROPERTY_BLOCK_ADDRESS = "blockAddress";
    public static final String PROPERTY_BLOCK_LENGTH = "blockLength";
    public static final String PROPERTY_UNIQUE_ADDRESS = "uniqueAddress";

    // Channel group ids
    public static final String GROUP_DEVICE_INFO = "deviceInformation";
    public static final String GROUP_AC_GENERAL = "acGeneral";
    public static final String GROUP_AC_PHASE_A = "acPhaseA";
    public static final String GROUP_AC_PHASE_B = "acPhaseB";
    public static final String GROUP_AC_PHASE_C = "acPhaseC";
    public static final String GROUP_DC_GENERAL = "dcGeneral";

    // List of all Channel ids in device information group
    public static final String CHANNEL_PHASE_CONFIGURATION = "phase-configuration";
    public static final String CHANNEL_CABINET_TEMPERATURE = "cabinet-temperature";
    public static final String CHANNEL_HEATSINK_TEMPERATURE = "heatsink-temperature";
    public static final String CHANNEL_TRANSFORMER_TEMPERATURE = "transformer-temperature";
    public static final String CHANNEL_OTHER_TEMPERATURE = "other-temperature";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_STATUS_VENDOR = "status-vendor";

    // List of channel ids in AC general group for inverter
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

    // List of channel ids in DC group for inverter
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

    // Expected SunSpec ID This is a magic constant to distinguish SunSpec compatible
    // devices from other modbus devices
    public static final long SUNSPEC_ID = 0x53756e53;
    // Size of SunSpect ID in words
    public static final int SUNSPEC_ID_SIZE = 2;
    // Size of any block header in words
    public static final int MODEL_HEADER_SIZE = 2;
}
