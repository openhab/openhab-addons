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
package org.openhab.binding.modbus.lambda.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.binding.modbus.ModbusBindingConstants;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link LambdaBindingConstants} class defines common
 * constants, which are used across the whole binding.
 *
 * @author Paul Frank - Initial contribution
 * @author Christian Koch - modified for lambda heat pump based on stiebeleltron binding for modbus
 */
@NonNullByDefault
public class LambdaBindingConstants {

    private static final String BINDING_ID = ModbusBindingConstants.BINDING_ID;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_LAMBDAHP = new ThingTypeUID(BINDING_ID, "lambdahp");

    // Channel group ids
    public static final String GROUP_GENERAL_AMBIENT = "generalAmbient";
    public static final String GROUP_GENERAL_EMANAGER = "generalEManager";
    public static final String GROUP_HEATPUMP1 = "Heatpump1";
    public static final String GROUP_HEATPUMP1SET = "Heatpump1Set";
    public static final String GROUP_BOILER1 = "Boiler1";
    public static final String GROUP_BOILER1MT = "Boiler1Mt";
    public static final String GROUP_BUFFER1 = "Buffer1";
    public static final String GROUP_BUFFER1MT = "Buffer1Mt";
    public static final String GROUP_HEATINGCIRCUIT1 = "HeatingCircuit1";
    public static final String GROUP_HEATINGCIRCUIT1SETTING = "HeatingCircuit1Setting";

    // List of all Channel ids in device information group
    // General Ambient
    public static final String CHANNEL_AMBIENT_ERROR_NUMBER = "ambient-error-number";
    public static final String CHANNEL_AMBIENT_OPERATING_STATE = "ambient-operating-state";
    public static final String CHANNEL_ACTUAL_AMBIENT_TEMPERATURE = "actual-ambient-temperature";
    public static final String CHANNEL_AVERAGE_AMBIENT_TEMPERATURE = "average-ambient-temperature";
    public static final String CHANNEL_CALCULATED_AMBIENT_TEMPERATURE = "calculated-ambient-temperature";

    // General E-manager
    public static final String CHANNEL_EMANAGER_ERROR_NUMBER = "emanager-error-number";
    public static final String CHANNEL_EMANAGER_OPERATING_STATE = "emanager-operating-state";
    public static final String CHANNEL_ACTUAL_POWER = "actual-power";
    public static final String CHANNEL_ACTUAL_POWER_CONSUMPTION = "actual-power-consumption";
    public static final String CHANNEL_POWER_CONSUMPTION_SETPOINT = "power-consumption-setpoint";

    // Heatpump 1
    public static final String CHANNEL_HEATPUMP1_ERROR_STATE = "heatpump1-error-state";
    public static final String CHANNEL_HEATPUMP1_ERROR_NUMBER = "heatpump1-error-number";
    public static final String CHANNEL_HEATPUMP1_STATE = "heatpump1-state";
    public static final String CHANNEL_HEATPUMP1_OPERATING_STATE = "heatpump1-operating-state";
    public static final String CHANNEL_HEATPUMP1_T_FLOW = "heatpump1-t-flow";
    public static final String CHANNEL_HEATPUMP1_T_RETURN = "heatpump1-t-return";
    public static final String CHANNEL_HEATPUMP1_VOL_SINK = "heatpump1-vol-sink";
    public static final String CHANNEL_HEATPUMP1_T_EQIN = "heatpump1-t-eqin";
    public static final String CHANNEL_HEATPUMP1_T_EQOUT = "heatpump1-t-eqout";
    public static final String CHANNEL_HEATPUMP1_VOL_SOURCE = "heatpump1-vol-source";
    public static final String CHANNEL_HEATPUMP1_COMPRESSOR_RATING = "heatpump1-compressor-rating";
    public static final String CHANNEL_HEATPUMP1_QP_HEATING = "heatpump1-qp-heating";
    public static final String CHANNEL_HEATPUMP1_FI_POWER_CONSUMPTION = "heatpump1-fi-power-consumption";
    public static final String CHANNEL_HEATPUMP1_COP = "heatpump1-cop";
    public static final String CHANNEL_HEATPUMP1_SET_ERROR_QUIT = "heatpump1-set-error-quit";

    // Boiler 1
    public static final String CHANNEL_BOILER1_ERROR_NUMBER = "boiler1-error-number";
    public static final String CHANNEL_BOILER1_OPERATING_STATE = "boiler1-operating-state";
    public static final String CHANNEL_BOILER1_ACTUAL_HIGH_TEMPERATURE = "boiler1-actual-high-temperature";
    public static final String CHANNEL_BOILER1_ACTUAL_LOW_TEMPERATURE = "boiler1-actual-low-temperature";
    public static final String CHANNEL_BOILER1_MAXIMUM_BOILER_TEMPERATURE = "boiler1-maximum-boiler-temperature";

    // Buffer 1
    public static final String CHANNEL_BUFFER1_ERROR_NUMBER = "buffer1-error-number";
    public static final String CHANNEL_BUFFER1_OPERATING_STATE = "buffer1-operating-state";
    public static final String CHANNEL_BUFFER1_ACTUAL_HIGH_TEMPERATURE = "buffer1-actual-high-temperature";
    public static final String CHANNEL_BUFFER1_ACTUAL_LOW_TEMPERATURE = "buffer1-actual-low-temperature";
    public static final String CHANNEL_BUFFER1_MAXIMUM_BOILER_TEMPERATURE = "buffer1-maximum-boiler-temperature";

    // Heating Circuit 1
    public static final String CHANNEL_HEATINGCIRCUIT1_ERROR_NUMBER = "heatingcircuit1-error-number";
    public static final String CHANNEL_HEATINGCIRCUIT1_OPERATING_STATE = "heatingcircuit1-operating-state";
    public static final String CHANNEL_HEATINGCIRCUIT1_FLOW_LINE_TEMPERATURE = "heatingcircuit1-flow-line-temperature";
    public static final String CHANNEL_HEATINGCIRCUIT1_RETURN_LINE_TEMPERATURE = "heatingcircuit1-return-line-temperature";
    public static final String CHANNEL_HEATINGCIRCUIT1_ROOM_DEVICE_TEMPERATURE = "heatingcircuit1-room-device-temperature";
    public static final String CHANNEL_HEATINGCIRCUIT1_SETPOINT_FLOW_LINE_TEMPERATURE = "heatingcircuit1-setpoint-flow-line-temperature";
    public static final String CHANNEL_HEATINGCIRCUIT1_OPERATING_MODE = "heatingcircuit1-operating-mode";
    // Heating Cirucuit 1 Set
    public static final String CHANNEL_HEATINGCIRCUIT1_OFFSET_FLOW_LINE_TEMPERATURE = "heatingcircuit1-offset-flow-line-temperature";
    public static final String CHANNEL_HEATINGCIRCUIT1_ROOM_HEATING_TEMPERATURE = "heatingcircuit1-room-heating-temperature";
    public static final String CHANNEL_HEATINGCIRCUIT1_ROOM_COOLING_TEMPERATURE = "heatingcircuit1-room-cooling-temperature";
}
