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
package org.openhab.binding.modbus.studer.internal;

import static org.openhab.core.library.unit.MetricPrefix.KILO;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;
import static org.openhab.core.library.unit.Units.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.measure.Unit;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link StuderBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Giovanni Mirulla - Initial contribution
 */
@NonNullByDefault
public class StuderBindingConstants {

    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BSP = new ThingTypeUID(BINDING_ID, "bsp");
    public static final ThingTypeUID THING_TYPE_XTENDER = new ThingTypeUID(BINDING_ID, "xtender");
    public static final ThingTypeUID THING_TYPE_VARIOTRACK = new ThingTypeUID(BINDING_ID, "variotrack");
    public static final ThingTypeUID THING_TYPE_VARIOSTRING = new ThingTypeUID(BINDING_ID, "variostring");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = new HashSet<>();
    static {
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_BSP);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_XTENDER);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_VARIOTRACK);
        SUPPORTED_THING_TYPES_UIDS.add(THING_TYPE_VARIOSTRING);
    }

    // List of all Channel ids
    public static final String CHANNEL_POWER = "power";
    public static final String CHANNEL_BATTERY_VOLTAGE = "batteryVoltage";
    public static final String CHANNEL_BATTERY_CURRENT = "batteryCurrent";
    public static final String CHANNEL_STATE_OF_CHARGE = "stateOfCharge";
    public static final String CHANNEL_BATTERY_TEMPERATURE = "batteryTemperature";

    public static final String CHANNEL_INPUT_VOLTAGE = "inputVoltage";
    public static final String CHANNEL_INPUT_CURRENT = "inputCurrent";
    public static final String CHANNEL_INPUT_ACTIVE_POWER = "inputActivePower";
    public static final String CHANNEL_INPUT_FREQUENCY = "inputFrequency";
    public static final String CHANNEL_OUTPUT_VOLTAGE = "outputVoltage";
    public static final String CHANNEL_OUTPUT_CURRENT = "outputCurrent";
    public static final String CHANNEL_OUTPUT_ACTIVE_POWER = "outputActivePower";
    public static final String CHANNEL_OUTPUT_FREQUENCY = "outputFrequency";
    public static final String CHANNEL_OPERATING_STATE = "operatingState";
    public static final String CHANNEL_STATE_INVERTER = "stateInverter";

    public static final String CHANNEL_MODEL_VARIOTRACK = "modelVarioTrack";
    public static final String CHANNEL_VOLTAGE_PV_GENERATOR = "voltagePVGenerator";
    public static final String CHANNEL_POWER_PV_GENERATOR = "powerPVGenerator";
    public static final String CHANNEL_PRODUCTION_CURRENT_DAY = "productionCurrentDay";
    public static final String CHANNEL_OPERATING_MODE = "operatingMode";
    public static final String CHANNEL_STATE_VARIOTRACK = "stateVarioTrack";

    public static final String CHANNEL_PV_VOLTAGE = "PVVoltage";
    public static final String CHANNEL_PV_CURRENT = "PVCurrent";
    public static final String CHANNEL_PV_POWER = "PVPower";
    public static final String CHANNEL_PRODUCTION_PV_CURRENT_DAY = "ProductionPVCurrentDay";
    public static final String CHANNEL_PV_OPERATING_MODE = "PVMode";
    public static final String CHANNEL_PV1_VOLTAGE = "PV1Voltage";
    public static final String CHANNEL_PV1_CURRENT = "PV1Current";
    public static final String CHANNEL_PV1_POWER = "PV1Power";
    public static final String CHANNEL_PRODUCTION_PV1_CURRENT_DAY = "ProductionPV1CurrentDay";
    public static final String CHANNEL_PV1_OPERATING_MODE = "PV1Mode";
    public static final String CHANNEL_PV2_VOLTAGE = "PV2Voltage";
    public static final String CHANNEL_PV2_CURRENT = "PV2Current";
    public static final String CHANNEL_PV2_POWER = "PV2Power";
    public static final String CHANNEL_PRODUCTION_PV2_CURRENT_DAY = "ProductionPV2CurrentDay";
    public static final String CHANNEL_PV2_OPERATING_MODE = "PV2Mode";
    public static final String CHANNEL_STATE_VARIOSTRING = "stateVarioString";

    /**
     * Map of the supported BSP channel with their registers
     */
    public static final Map<Integer, String> CHANNELS_BSP = new HashMap<>();
    static {
        CHANNELS_BSP.put(6, CHANNEL_POWER);
        CHANNELS_BSP.put(0, CHANNEL_BATTERY_VOLTAGE);
        CHANNELS_BSP.put(2, CHANNEL_BATTERY_CURRENT);
        CHANNELS_BSP.put(4, CHANNEL_STATE_OF_CHARGE);
        CHANNELS_BSP.put(58, CHANNEL_BATTERY_TEMPERATURE);
    }

    /**
     * Map of the supported BSP channel with their unit
     */
    public static final Map<Integer, Unit<?>> UNIT_CHANNELS_BSP = new HashMap<>();
    static {
        UNIT_CHANNELS_BSP.put(6, WATT);
        UNIT_CHANNELS_BSP.put(0, VOLT);
        UNIT_CHANNELS_BSP.put(2, AMPERE);
        UNIT_CHANNELS_BSP.put(4, PERCENT);
        UNIT_CHANNELS_BSP.put(58, CELSIUS);
    }

    /**
     * Map of the supported Xtender channel with their registers
     */
    public static final Map<Integer, String> CHANNELS_XTENDER = new HashMap<>();
    static {
        CHANNELS_XTENDER.put(22, CHANNEL_INPUT_VOLTAGE);
        CHANNELS_XTENDER.put(24, CHANNEL_INPUT_CURRENT);
        CHANNELS_XTENDER.put(274, CHANNEL_INPUT_ACTIVE_POWER);
        CHANNELS_XTENDER.put(168, CHANNEL_INPUT_FREQUENCY);
        CHANNELS_XTENDER.put(42, CHANNEL_OUTPUT_VOLTAGE);
        CHANNELS_XTENDER.put(44, CHANNEL_OUTPUT_CURRENT);
        CHANNELS_XTENDER.put(272, CHANNEL_OUTPUT_ACTIVE_POWER);
        CHANNELS_XTENDER.put(170, CHANNEL_OUTPUT_FREQUENCY);
        CHANNELS_XTENDER.put(56, CHANNEL_OPERATING_STATE);
        CHANNELS_XTENDER.put(98, CHANNEL_STATE_INVERTER);
    }

    /**
     * Map of the supported Xtender channel with their unit
     */
    public static final Map<Integer, Unit<?>> UNIT_CHANNELS_XTENDER = new HashMap<>();
    static {
        UNIT_CHANNELS_XTENDER.put(22, VOLT);
        UNIT_CHANNELS_XTENDER.put(24, AMPERE);
        UNIT_CHANNELS_XTENDER.put(274, KILO(WATT));
        UNIT_CHANNELS_XTENDER.put(168, HERTZ);
        UNIT_CHANNELS_XTENDER.put(42, VOLT);
        UNIT_CHANNELS_XTENDER.put(44, AMPERE);
        UNIT_CHANNELS_XTENDER.put(272, KILO(WATT));
        UNIT_CHANNELS_XTENDER.put(170, HERTZ);
    }

    /**
     * Map of the supported VarioTrack channel with their registers
     */
    public static final Map<Integer, String> CHANNELS_VARIOTRACK = new HashMap<>();
    static {
        CHANNELS_VARIOTRACK.put(30, CHANNEL_MODEL_VARIOTRACK);
        CHANNELS_VARIOTRACK.put(4, CHANNEL_VOLTAGE_PV_GENERATOR);
        CHANNELS_VARIOTRACK.put(8, CHANNEL_POWER_PV_GENERATOR);
        CHANNELS_VARIOTRACK.put(14, CHANNEL_PRODUCTION_CURRENT_DAY);
        CHANNELS_VARIOTRACK.put(0, CHANNEL_BATTERY_VOLTAGE);
        CHANNELS_VARIOTRACK.put(2, CHANNEL_BATTERY_CURRENT);
        CHANNELS_VARIOTRACK.put(32, CHANNEL_OPERATING_MODE);
        CHANNELS_VARIOTRACK.put(138, CHANNEL_STATE_VARIOTRACK);
    }

    /**
     * Map of the supported VarioTrack channel with their unit
     */
    public static final Map<Integer, Unit<?>> UNIT_CHANNELS_VARIOTRACK = new HashMap<>();
    static {
        UNIT_CHANNELS_VARIOTRACK.put(4, VOLT);
        UNIT_CHANNELS_VARIOTRACK.put(8, KILO(WATT));
        UNIT_CHANNELS_VARIOTRACK.put(14, KILOWATT_HOUR);
        UNIT_CHANNELS_VARIOTRACK.put(0, VOLT);
        UNIT_CHANNELS_VARIOTRACK.put(2, AMPERE);
    }

    /**
     * Map of the supported VarioString channel with their registers
     */
    public static final Map<Integer, String> CHANNELS_VARIOSTRING = new HashMap<>();
    static {
        CHANNELS_VARIOSTRING.put(0, CHANNEL_BATTERY_VOLTAGE);
        CHANNELS_VARIOSTRING.put(2, CHANNEL_BATTERY_CURRENT);
        CHANNELS_VARIOSTRING.put(8, CHANNEL_PV_VOLTAGE);
        CHANNELS_VARIOSTRING.put(14, CHANNEL_PV_CURRENT);
        CHANNELS_VARIOSTRING.put(20, CHANNEL_PV_POWER);
        CHANNELS_VARIOSTRING.put(34, CHANNEL_PRODUCTION_PV_CURRENT_DAY);
        CHANNELS_VARIOSTRING.put(26, CHANNEL_PV_OPERATING_MODE);
        CHANNELS_VARIOSTRING.put(10, CHANNEL_PV1_VOLTAGE);
        CHANNELS_VARIOSTRING.put(16, CHANNEL_PV1_CURRENT);
        CHANNELS_VARIOSTRING.put(22, CHANNEL_PV1_POWER);
        CHANNELS_VARIOSTRING.put(36, CHANNEL_PRODUCTION_PV1_CURRENT_DAY);
        CHANNELS_VARIOSTRING.put(28, CHANNEL_PV1_OPERATING_MODE);
        CHANNELS_VARIOSTRING.put(12, CHANNEL_PV2_VOLTAGE);
        CHANNELS_VARIOSTRING.put(18, CHANNEL_PV2_CURRENT);
        CHANNELS_VARIOSTRING.put(24, CHANNEL_PV2_POWER);
        CHANNELS_VARIOSTRING.put(38, CHANNEL_PRODUCTION_PV2_CURRENT_DAY);
        CHANNELS_VARIOSTRING.put(30, CHANNEL_PV2_OPERATING_MODE);
        CHANNELS_VARIOSTRING.put(216, CHANNEL_STATE_VARIOSTRING);
    }

    /**
     * Map of the supported VarioString channel with their unit
     */
    public static final Map<Integer, Unit<?>> UNIT_CHANNELS_VARIOSTRING = new HashMap<>();
    static {
        UNIT_CHANNELS_VARIOSTRING.put(0, VOLT);
        UNIT_CHANNELS_VARIOSTRING.put(2, AMPERE);
        UNIT_CHANNELS_VARIOSTRING.put(8, VOLT);
        UNIT_CHANNELS_VARIOSTRING.put(14, AMPERE);
        UNIT_CHANNELS_VARIOSTRING.put(20, KILO(WATT));
        UNIT_CHANNELS_VARIOSTRING.put(34, KILOWATT_HOUR);
        UNIT_CHANNELS_VARIOSTRING.put(10, VOLT);
        UNIT_CHANNELS_VARIOSTRING.put(16, AMPERE);
        UNIT_CHANNELS_VARIOSTRING.put(22, KILO(WATT));
        UNIT_CHANNELS_VARIOSTRING.put(36, KILOWATT_HOUR);
        UNIT_CHANNELS_VARIOSTRING.put(12, VOLT);
        UNIT_CHANNELS_VARIOSTRING.put(18, AMPERE);
        UNIT_CHANNELS_VARIOSTRING.put(24, KILO(WATT));
        UNIT_CHANNELS_VARIOSTRING.put(38, KILOWATT_HOUR);
    }

    // List of all parameters
    public static final String SLAVE_ADDRESS = "slaveAddress";
    public static final String REFRESH = "refresh";
}
