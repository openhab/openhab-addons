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
package org.openhab.binding.myvaillant.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * Constants shared by the myVAILLANT binding.
 *
 * @author Marius Korte - Initial contribution
 */
@NonNullByDefault
public class MyVaillantBindingConstants {

    public static final String BINDING_ID = "myvaillant";

    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_SYSTEM = new ThingTypeUID(BINDING_ID, "system");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_CIRCUIT = new ThingTypeUID(BINDING_ID, "circuit");
    public static final ThingTypeUID THING_TYPE_DHW = new ThingTypeUID(BINDING_ID, "dhw");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_SYSTEM,
            THING_TYPE_ZONE, THING_TYPE_CIRCUIT, THING_TYPE_DHW, THING_TYPE_DEVICE);
    public static final Set<ThingTypeUID> SYSTEM_COMPONENT_THING_TYPES = Set.of(THING_TYPE_ZONE, THING_TYPE_CIRCUIT,
            THING_TYPE_DHW, THING_TYPE_DEVICE);

    // Configuration parameters
    public static final String CONFIG_SYSTEM_ID = "systemId";
    public static final String CONFIG_INDEX = "index";
    public static final String CONFIG_DEVICE_UUID = "deviceUuid";

    // Thing properties
    public static final String PROPERTY_COMPONENT_ID = "componentId";

    // Account channels
    public static final String CHANNEL_API_REQUESTS = "api-requests";

    // System channels
    public static final String CHANNEL_OUTDOOR_TEMPERATURE = "outdoor-temperature";
    public static final String CHANNEL_OUTDOOR_TEMPERATURE_AVERAGE_24H = "outdoor-temperature-average-24h";
    public static final String CHANNEL_WATER_PRESSURE = "water-pressure";
    public static final String CHANNEL_SYSTEM_FLOW_TEMPERATURE = "system-flow-temperature";
    public static final String CHANNEL_CYLINDER_TOP_DHW = "cylinder-temperature-top-dhw";
    public static final String CHANNEL_CYLINDER_BOTTOM_DHW = "cylinder-temperature-bottom-dhw";
    public static final String CHANNEL_CYLINDER_TOP_CH = "cylinder-temperature-top-ch";
    public static final String CHANNEL_CYLINDER_BOTTOM_CH = "cylinder-temperature-bottom-ch";
    public static final String CHANNEL_ENERGY_MANAGER_STATE = "energy-manager-state";
    public static final String CHANNEL_SYSTEM_OFF = "system-off";
    public static final String CHANNEL_FIRMWARE_UPDATE_REQUIRED = "firmware-update-required";
    public static final String CHANNEL_ONLINE = "online";
    public static final String CHANNEL_TROUBLE_CODES = "trouble-codes";
    public static final String CHANNEL_HAS_TROUBLE_CODES = "has-trouble-codes";
    public static final String CHANNEL_HOLIDAY = "holiday";
    public static final String CHANNEL_HOLIDAY_START = "holiday-start";
    public static final String CHANNEL_HOLIDAY_END = "holiday-end";
    public static final String CHANNEL_MANUAL_COOLING = "manual-cooling";
    public static final String CHANNEL_MANUAL_COOLING_START = "manual-cooling-start";
    public static final String CHANNEL_MANUAL_COOLING_END = "manual-cooling-end";
    public static final String CHANNEL_VENTILATION_BOOST = "ventilation-boost";
    public static final String CHANNEL_HEATING_ENERGY_EFFICIENCY = "heating-energy-efficiency";
    public static final String CHANNEL_LAST_UPDATE = "last-update";

    // Zone channels
    public static final String CHANNEL_CURRENT_ROOM_TEMPERATURE = "current-room-temperature";
    public static final String CHANNEL_CURRENT_ROOM_HUMIDITY = "current-room-humidity";
    public static final String CHANNEL_DESIRED_ROOM_TEMPERATURE = "desired-room-temperature-setpoint";
    public static final String CHANNEL_DESIRED_ROOM_TEMPERATURE_HEATING = "desired-room-temperature-setpoint-heating";
    public static final String CHANNEL_DESIRED_ROOM_TEMPERATURE_COOLING = "desired-room-temperature-setpoint-cooling";
    public static final String CHANNEL_OPERATION_MODE_HEATING = "operation-mode-heating";
    public static final String CHANNEL_OPERATION_MODE_COOLING = "operation-mode-cooling";
    public static final String CHANNEL_MANUAL_MODE_SETPOINT_HEATING = "manual-mode-setpoint-heating";
    public static final String CHANNEL_SETPOINT_COOLING = "setpoint-cooling";
    public static final String CHANNEL_SET_BACK_TEMPERATURE = "set-back-temperature";
    public static final String CHANNEL_HEATING_STATE = "heating-state";
    public static final String CHANNEL_CURRENT_SPECIAL_FUNCTION = "current-special-function";
    public static final String CHANNEL_HVAC_ACTION = "hvac-action";
    public static final String CHANNEL_QUICK_VETO = "quick-veto";
    public static final String CHANNEL_QUICK_VETO_END = "quick-veto-end";
    public static final String CHANNEL_QUICK_VETO_DURATION = "quick-veto-duration";
    public static final String CHANNEL_MANUAL_COOLING_ACTIVE = "manual-cooling-active";

    // Circuit channels
    public static final String CHANNEL_CIRCUIT_STATE = "circuit-state";
    public static final String CHANNEL_CURRENT_FLOW_TEMPERATURE = "current-flow-temperature";
    public static final String CHANNEL_FLOW_TEMPERATURE_SETPOINT = "flow-temperature-setpoint";
    public static final String CHANNEL_HEATING_CURVE = "heating-curve";
    public static final String CHANNEL_HEAT_DEMAND_LIMIT = "heat-demand-limited-by-outside-temperature";
    public static final String CHANNEL_MIN_FLOW_TEMPERATURE_SETPOINT = "min-flow-temperature-setpoint";
    public static final String CHANNEL_MAX_FLOW_TEMPERATURE_SETPOINT = "max-flow-temperature-setpoint";
    public static final String CHANNEL_ROOM_TEMPERATURE_CONTROL_MODE = "room-temperature-control-mode";
    public static final String CHANNEL_ENERGY_MANAGER_STATE_CIRCUIT = "calculated-energy-manager-state";
    public static final String CHANNEL_COOLING_ALLOWED = "cooling-allowed";

    // Domestic hot water channels
    public static final String CHANNEL_CURRENT_DHW_TEMPERATURE = "current-temperature";
    public static final String CHANNEL_TAPPING_SETPOINT = "tapping-setpoint";
    public static final String CHANNEL_OPERATION_MODE = "operation-mode";
    public static final String CHANNEL_BOOST = "boost";

    // Device channels (energy channels are created per data type the device reports, see DeviceHandler)
    public static final String CHANNEL_CURRENT_POWER = "current-power";
    public static final String CHANNEL_OPERATION_TIME = "operation-time";
    public static final String CHANNEL_ON_OFF_CYCLES = "on-off-cycles";
    public static final String CHANNEL_TYPE_ENERGY_TODAY = "energy-today";

    // Values used by the API
    public static final String OPERATION_MODE_HEATING = "HEATING";
    public static final String VALUE_TYPE_CONSUMED_ELECTRICAL_ENERGY = "CONSUMED_ELECTRICAL_ENERGY";
    public static final String VALUE_TYPE_HEAT_GENERATED = "HEAT_GENERATED";
}
