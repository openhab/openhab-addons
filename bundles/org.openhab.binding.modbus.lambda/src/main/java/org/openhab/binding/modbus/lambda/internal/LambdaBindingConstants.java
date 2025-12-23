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

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_GENERAL = new ThingTypeUID(BINDING_ID, "general");
    public static final ThingTypeUID THING_TYPE_BOILER = new ThingTypeUID(BINDING_ID, "boiler");
    public static final ThingTypeUID THING_TYPE_BUFFER = new ThingTypeUID(BINDING_ID, "buffer");
    public static final ThingTypeUID THING_TYPE_HEAT_PUMP = new ThingTypeUID(BINDING_ID, "heat-pump");
    public static final ThingTypeUID THING_TYPE_SOLAR = new ThingTypeUID(BINDING_ID, "solar");
    public static final ThingTypeUID THING_TYPE_HEATING_CIRCUIT = new ThingTypeUID(BINDING_ID, "heating-circuit");

    // Channel group ids
    public static final String GROUP_GENERAL_AMBIENT = "ambient-group";
    public static final String GROUP_GENERAL_EMANAGER = "emanager-group";
    public static final String GROUP_HEAT_PUMP = "heat-pump-group";
    public static final String GROUP_BOILER = "boiler-group";
    public static final String GROUP_BOILER_REG50 = "boiler-reg50-group";
    public static final String GROUP_BUFFER = "buffer-group";
    public static final String GROUP_BUFFER_REG50 = "buffer-reg50-group";
    public static final String GROUP_SOLAR = "solar-group";
    public static final String GROUP_SOLAR_REG50 = "solar-reg50-group";
    public static final String GROUP_HEATING_CIRCUIT = "heating-circuit-group";
    public static final String GROUP_HEATING_CIRCUIT_REG50 = "heating-circuit-reg50-group";

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
    public static final String CHANNEL_EMANAGER_ACTUAL_POWER = "emanager-actual-power";
    public static final String CHANNEL_EMANAGER_ACTUAL_POWER_SIGNED = "emanager-actual-power-signed";
    public static final String CHANNEL_EMANAGER_ACTUAL_POWER_CONSUMPTION = "emanager-actual-power-consumption";
    public static final String CHANNEL_POWER_CONSUMPTION_SETPOINT = "emanager-power-consumption-setpoint";

    // Heatpump
    public static final String CHANNEL_HEAT_PUMP_ERROR_STATE = "heat-pump-error-state";
    public static final String CHANNEL_HEAT_PUMP_ERROR_NUMBER = "heat-pump-error-number";
    public static final String CHANNEL_HEAT_PUMP_STATE = "heat-pump-state";
    public static final String CHANNEL_HEAT_PUMP_OPERATING_STATE = "heat-pump-operating-state";
    public static final String CHANNEL_HEAT_PUMP_T_FLOW = "heat-pump-t-flow";
    public static final String CHANNEL_HEAT_PUMP_T_RETURN = "heat-pump-t-return";
    public static final String CHANNEL_HEAT_PUMP_VOL_SINK = "heat-pump-vol-sink";
    public static final String CHANNEL_HEAT_PUMP_T_EQIN = "heat-pump-t-eqin";
    public static final String CHANNEL_HEAT_PUMP_T_EQOUT = "heat-pump-t-eqout";
    public static final String CHANNEL_HEAT_PUMP_VOL_SOURCE = "heat-pump-vol-source";
    public static final String CHANNEL_HEAT_PUMP_COMPRESSOR_RATING = "heat-pump-compressor-rating";
    public static final String CHANNEL_HEAT_PUMP_QP_HEATING = "heat-pump-qp-heating";
    public static final String CHANNEL_HEAT_PUMP_FI_POWER_CONSUMPTION = "heat-pump-fi-power-consumption";
    public static final String CHANNEL_HEAT_PUMP_COP = "heat-pump-cop";
    public static final String CHANNEL_HEAT_PUMP_REQUEST_PASSWORD = "heat-pump-request-password";
    public static final String CHANNEL_HEAT_PUMP_REQUEST_TYPE = "heat-pump-request-type";
    public static final String CHANNEL_HEAT_PUMP_REQUEST_T_FLOW = "heat-pump-request-t-flow";
    public static final String CHANNEL_HEAT_PUMP_REQUEST_T_RETURN = "heat-pump-request-t-return";
    public static final String CHANNEL_HEAT_PUMP_REQUEST_HEAT_SINK = "heat-pump-request-heat-sink";
    public static final String CHANNEL_HEAT_PUMP_RELAIS_STATE = "heat-pump-relais-state";
    public static final String CHANNEL_HEAT_PUMP_VDAE = "heat-pump-vdae";
    public static final String CHANNEL_HEAT_PUMP_VDAQ = "heat-pump-vdaq";
    public static final String CHANNEL_HEAT_PUMP_VDAE_SWAP = "heat-pump-vdae-swap";
    public static final String CHANNEL_HEAT_PUMP_VDAQ_SWAP = "heat-pump-vdaq-swap";

    // Boiler
    public static final String CHANNEL_BOILER_ERROR_NUMBER = "boiler-error-number";
    public static final String CHANNEL_BOILER_OPERATING_STATE = "boiler-operating-state";
    public static final String CHANNEL_BOILER_ACTUAL_HIGH_TEMPERATURE = "boiler-actual-high-temperature";
    public static final String CHANNEL_BOILER_ACTUAL_LOW_TEMPERATURE = "boiler-actual-low-temperature";
    public static final String CHANNEL_BOILER_MAXIMUM_BOILER_TEMPERATURE = "maximum-boiler-temperature";

    // Buffer
    public static final String CHANNEL_BUFFER_ERROR_NUMBER = "buffer-error-number";
    public static final String CHANNEL_BUFFER_OPERATING_STATE = "buffer-operating-state";
    public static final String CHANNEL_BUFFER_ACTUAL_HIGH_TEMPERATURE = "buffer-actual-high-temperature";
    public static final String CHANNEL_BUFFER_ACTUAL_LOW_TEMPERATURE = "buffer-actual-low-temperature";
    public static final String CHANNEL_BUFFER_ACTUAL_MODBUS_TEMPERATURE = "buffer-actual-modbus-temperature";
    public static final String CHANNEL_BUFFER_REQUEST_TYPE = "buffer-request-type";
    public static final String CHANNEL_BUFFER_REQUEST_FLOW_LINE_TEMPERATURE = "buffer-request-flow-line-temperature";
    public static final String CHANNEL_BUFFER_REQUEST_RETURN_LINE_TEMPERATURE = "buffer-request-return-line-temperature";
    public static final String CHANNEL_BUFFER_REQUEST_HEAT_SINK_TEMPERATURE = "buffer-request-heat-sink-temperature-difference";
    public static final String CHANNEL_BUFFER_REQUEST_HEATING_CAPACITY = "buffer-request-heating-capacity";
    public static final String CHANNEL_BUFFER_MAXIMUM_BUFFER_TEMPERATURE = "maximum-buffer-temperature";

    // Solar
    public static final String CHANNEL_SOLAR_ERROR_NUMBER = "solar-error-number";
    public static final String CHANNEL_SOLAR_OPERATING_STATE = "solar-operating-state";
    public static final String CHANNEL_SOLAR_COLLECTOR_TEMPERATURE = "solar-collector-temperature";
    public static final String CHANNEL_SOLAR_BUFFER1_TEMPERATURE = "solar-buffer1-temperature";
    public static final String CHANNEL_SOLAR_BUFFER2_TEMPERATURE = "solar-buffer2-temperature";
    public static final String CHANNEL_SOLAR_MAXIMUM_BUFFER_TEMPERATURE = "solar-maximum-buffer-temperature";
    public static final String CHANNEL_SOLAR_BUFFER_CHANGEOVER_TEMPERATURE = "solar-buffer-changeover-temperature";

    // Heating Circuit
    public static final String CHANNEL_HEATING_CIRCUIT_ERROR_NUMBER = "heating-circuit-error-number";
    public static final String CHANNEL_HEATING_CIRCUIT_OPERATING_STATE = "heating-circuit-operating-state";
    public static final String CHANNEL_HEATING_CIRCUIT_FLOW_LINE_TEMPERATURE = "heating-circuit-flow-line-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_RETURN_LINE_TEMPERATURE = "heating-circuit-return-line-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_ROOM_DEVICE_TEMPERATURE = "heating-circuit-room-device-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_SETPOINT_FLOW_LINE_TEMPERATURE = "heating-circuit-setpoint-flow-line-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_OPERATING_MODE = "heating-circuit-operating-mode";
    public static final String CHANNEL_HEATING_CIRCUIT_TARGET_TEMPERATURE_FLOW_LINE = "heating-circuit-target-temperature-flow-line";
    public static final String CHANNEL_HEATING_CIRCUIT_OFFSET_FLOW_LINE_TEMPERATURE = "heating-circuit-offset-flow-line-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_ROOM_HEATING_TEMPERATURE = "heating-circuit-room-heating-temperature";
    public static final String CHANNEL_HEATING_CIRCUIT_ROOM_COOLING_TEMPERATURE = "heating-circuit-room-cooling-temperature";
}
