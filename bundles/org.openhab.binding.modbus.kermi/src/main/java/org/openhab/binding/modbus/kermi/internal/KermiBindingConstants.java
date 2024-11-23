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
package org.openhab.binding.modbus.kermi.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link KermiBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kai Neuhaus - Initial contribution
 */
@NonNullByDefault
public class KermiBindingConstants {

    public static final String STATE_AND_ALARM_READ_ERRORS = "Status And Alarm Modbus Read Errors";
    public static final String STATE_READ_ERROR = "Information Modbus Read Error";
    public static final String DATA_READ_ERROR = "Data Modbus Read Error";
    static final String PV_READ_ERROR = "PV Modbus Read Error";
    public static final String ALARM_GROUP = "xcenter-alarm";
    public static final String STATE_GROUP = "xcenter-state";
    public static final String ENERGY_SOURCE_GROUP = "xcenter-energy-source";
    public static final String CHARGING_CIRCUIT_GROUP = "xcenter-charging-circuit";
    public static final String POWER_GROUP = "xcenter-power";
    public static final String WORKHOURS_GROUP = "xcenter-workhours";
    public static final String PV_GROUP = "xcenter-pv-modulation";
    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    // Supported Thing Types
    public static final ThingTypeUID THING_TYPE_KERMI_XCENTER = new ThingTypeUID(BINDING_ID, "kermi-xcenter");

    // Channels for State
    public static final String GLOBAL_STATE_ID_CHANNEL = "global-state-id";

    // Alarm State
    public static final String ALARM_STATE_CHANNEL = "alarm-state";

    // Energy Source
    public static final String FLOW_TEMPERATURE_CHANNEL = "flow-temperature";
    public static final String RETURN_TEMPERATURE_CHANNEL = "return-temperature";
    public static final String FLOW_SPEED_CHANNEL = "flow-speed";

    // Charging Circuit
    public static final String EXIT_TEMPERATURE_CHANNEL = "exit-temperature";
    public static final String INCOMING_TEMPERATURE_CHANNEL = "incoming-temperature";
    public static final String TEMPERATURE_SENSOR_OUTSIDE_CHANNEL = "temperature-sensor-outside";

    // Power
    public static final String COP_CHANNEL = "cop";
    public static final String COP_HEATING_CHANNEL = "cop-heating";
    public static final String COP_DRINKINGWATER_CHANNEL = "cop-drinkingwater";
    public static final String COP_COOLING_CHANNEL = "cop-cooling";

    public static final String POWER_CHANNEL = "power";
    public static final String POWER_HEATING_CHANNEL = "power-heating";
    public static final String POWER_DRINKINGWATER_CHANNEL = "power-drinkingwater";
    public static final String POWER_COOLING_CHANNEL = "power-cooling";

    public static final String ELECTRIC_POWER_CHANNEL = "electric-power";
    public static final String ELECTRIC_POWER_HEATING_CHANNEL = "electric-power-heating";
    public static final String ELECTRIC_POWER_DRINKINGWATER_CHANNEL = "electric-power-drinkingwater";
    public static final String ELECTRIC_POWER_COOLING_CHANNEL = "electric-power-cooling";

    // Work hours
    public static final String WORKHOURS_FAN_CHANNEL = "workhours-fan";
    public static final String WORKHOURS_STORAGE_LOADING_PUMP_CHANNEL = "workhours-storage-loading-pump";
    public static final String WORKHOURS_COMPRESSOR_CHANNEL = "workhours-compressor";

    // PV
    public static final String PV_STATE_CHANNEL = "pv-state";
    public static final String PV_POWER_CHANNEL = "pv-power";
    public static final String PV_TARGET_TEMPERATURE_HEATING_CHANNEL = "pv-target-temperature-heating";
    public static final String PV_TARGET_TEMPERATURE_DRINKINGWATER_CHANNEL = "pv-target-temperature-drinkingwater";
}
