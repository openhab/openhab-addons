/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
    private static final String LAMBDA_GENERAL = "lambda-general";
    private static final String LAMBDA_BOILER = "lambda-boiler";
    private static final String LAMBDA_BUFFER = "lambda-buffer";
    private static final String LAMBDA_HEATPUMP = "lambda-heatpump";
    private static final String LAMBDA_HEATING_CIRCUIT = "lambda-heating_circuit";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GENERAL = new ThingTypeUID(BINDING_ID, LAMBDA_GENERAL);
    public static final ThingTypeUID THING_TYPE_BOILER = new ThingTypeUID(BINDING_ID, LAMBDA_BOILER);
    public static final ThingTypeUID THING_TYPE_BUFFER = new ThingTypeUID(BINDING_ID, LAMBDA_BUFFER);
    public static final ThingTypeUID THING_TYPE_HEATPUMP = new ThingTypeUID(BINDING_ID, LAMBDA_HEATPUMP);
    public static final ThingTypeUID THING_TYPE_HEATING_CIRCUIT = new ThingTypeUID(BINDING_ID, LAMBDA_HEATING_CIRCUIT);

    // Channel group ids
    public static final String GROUP_GENERAL_AMBIENT = "ambient-group";
    public static final String GROUP_GENERAL_EMANAGER = "emanager-group";
    public static final String GROUP_HEATPUMP = "heatpump-group";
    public static final String GROUP_HEATPUMP_REG50 = "heatpump-reg50-group";
    public static final String GROUP_BOILER = "boiler-group";
    public static final String GROUP_BOILER_REG50 = "boiler-reg50-group";
    public static final String GROUP_BUFFER = "buffer-group";
    public static final String GROUP_BUFFER_REG50 = "buffer-reg50-group";
    public static final String GROUP_HEATING_CIRCUIT = "heating_circuit-group";
    public static final String GROUP_HEATING_CIRCUIT_REG50 = "heating_circuit-reg50-group";

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
    public static final String CHANNEL_ACTUAL_POWER_SIGNED = "actual-power-signed";
    public static final String CHANNEL_ACTUAL_POWER_CONSUMPTION = "actual-power-consumption";
    public static final String CHANNEL_POWER_CONSUMPTION_SETPOINT = "power-consumption-setpoint";

    // Heatpump
    public static final String CHANNEL_HEATPUMP_ERROR_STATE = "heatpump-error-state";
    public static final String CHANNEL_HEATPUMP_ERROR_NUMBER = "heatpump-error-number";
    public static final String CHANNEL_HEATPUMP_STATE = "heatpump-state";
    public static final String CHANNEL_HEATPUMP_OPERATING_STATE = "heatpump-operating-state";
    public static final String CHANNEL_HEATPUMP_T_FLOW = "heatpump-t-flow";
    public static final String CHANNEL_HEATPUMP_T_RETURN = "heatpump-t-return";
    public static final String CHANNEL_HEATPUMP_VOL_SINK = "heatpump-vol-sink";
    public static final String CHANNEL_HEATPUMP_T_EQIN = "heatpump-t-eqin";
    public static final String CHANNEL_HEATPUMP_T_EQOUT = "heatpump-t-eqout";
    public static final String CHANNEL_HEATPUMP_VOL_SOURCE = "heatpump-vol-source";
    public static final String CHANNEL_HEATPUMP_COMPRESSOR_RATING = "heatpump-compressor-rating";
    public static final String CHANNEL_HEATPUMP_QP_HEATING = "heatpump-qp-heating";
    public static final String CHANNEL_HEATPUMP_FI_POWER_CONSUMPTION = "heatpump-fi-power-consumption";
    public static final String CHANNEL_HEATPUMP_COP = "heatpump-cop";
    public static final String CHANNEL_HEATPUMP_REQUEST_T_FLOW = "heatpump-request-t-flow";
    public static final String CHANNEL_HEATPUMP_REQUEST_T_RETURN = "heatpump-request-t-return";
    public static final String CHANNEL_HEATPUMP_REQUEST_HEAT_SINK = "heatpump-request-heat-sink";
    public static final String CHANNEL_HEATPUMP_VDAE = "heatpump-vdae";
    public static final String CHANNEL_HEATPUMP_VDAQ = "heatpump-vdaq";
    public static final String CHANNEL_HEATPUMP_SET_ERROR_QUIT = "heatpump-set-error-quit";

    // Boiler
    public static final String CHANNEL_BOILER_ERROR_NUMBER = "boiler-error-number";
    public static final String CHANNEL_BOILER_OPERATING_STATE = "boiler-operating-state";
    public static final String CHANNEL_BOILER_ACTUAL_HIGH_TEMPERATURE = "boiler-actual-high-temperature";
    public static final String CHANNEL_BOILER_ACTUAL_LOW_TEMPERATURE = "boiler-actual-low-temperature";
    public static final String CHANNEL_BOILER_MAXIMUM_BOILER_TEMPERATURE = "boiler-maximum-boiler-temperature";

    // Buffer
    public static final String CHANNEL_BUFFER_ERROR_NUMBER = "buffer-error-number";
    public static final String CHANNEL_BUFFER_OPERATING_STATE = "buffer-operating-state";
    public static final String CHANNEL_BUFFER_ACTUAL_HIGH_TEMPERATURE = "buffer-actual-high-temperature";
    public static final String CHANNEL_BUFFER_ACTUAL_LOW_TEMPERATURE = "buffer-actual-low-temperature";
    public static final String CHANNEL_BUFFER_MAXIMUM_BUFFER_TEMPERATURE = "buffer-maximum-buffer-temperature";

    // Heating Circuit
    public static final String CHANNEL_HEATING_CIRCUIT_ERROR_NUMBER = "heating_circuit-error-number";
    public static final String CHANNEL_HEATING_CIRCUIT_OPERATING_STATE = "heating_circuit-operating-state";
    public static final String CHANNEL_HEATING_CIRCUIT_FLOW_LINE_TEMPERATURE = "heating_circuit-flow-line-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_RETURN_LINE_TEMPERATURE = "heating_circuit-return-line-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_ROOM_DEVICE_TEMPERATURE = "heating_circuit-room-device-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_SETPOINT_FLOW_LINE_TEMPERATURE = "heating_circuit-setpoint-flow-line-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_OPERATING_MODE = "heating_circuit-operating-mode";
    // Heating Circuit Set
    public static final String CHANNEL_HEATING_CIRCUIT_OFFSET_FLOW_LINE_TEMPERATURE = "heating_circuit-offset-flow-line-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_ROOM_HEATING_TEMPERATURE = "heating_circuit-room-heating-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_ROOM_COOLING_TEMPERATURE = "heating_circuit-room-cooling-temperature";
}
