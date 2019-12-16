/**
 * Copyright (c) 2018,2018 by the respective copyright holders.
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.openthermgateway;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link OpenThermGatewayBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Arjen Korevaar
 */
@NonNullByDefault
public class OpenThermGatewayBindingConstants {

    private static final String BINDING_ID = "openthermgateway";

    // List of all Thing Type UID's
    public static final ThingTypeUID MAIN_THING_TYPE = new ThingTypeUID(BINDING_ID, "otgw");

    // List of all Channel id's
    public static final String CHANNEL_SEND_COMMAND = "sendcommand";
    public static final String CHANNEL_OVERRIDE_SETPOINT_TEMPORARY = "temperaturetemporary";
    public static final String CHANNEL_OVERRIDE_SETPOINT_CONSTANT = "temperatureconstant";
    public static final String CHANNEL_OVERRIDE_DHW_SETPOINT = "overridedhwsetpoint";

    public static final String CHANNEL_ROOM_TEMPERATURE = "roomtemp";
    public static final String CHANNEL_ROOM_SETPOINT = "roomsetpoint";
    public static final String CHANNEL_FLOW_TEMPERATURE = "flowtemp";
    public static final String CHANNEL_RETURN_TEMPERATURE = "returntemp";
    public static final String CHANNEL_OUTSIDE_TEMPERATURE = "outsidetemp";
    public static final String CHANNEL_CENTRAL_HEATING_WATER_SETPOINT = "controlsetpoint";
    public static final String CHANNEL_CENTRAL_HEATING_WATER_PRESSURE = "waterpressure";
    public static final String CHANNEL_CENTRAL_HEATING_ENABLED = "ch_enable";
    public static final String CHANNEL_CENTRAL_HEATING_MODE = "ch_mode";
    public static final String CHANNEL_DOMESTIC_HOT_WATER_TEMPERATURE = "dhwtemp";
    public static final String CHANNEL_DOMESTIC_HOT_WATER_ENABLED = "dhw_enable";
    public static final String CHANNEL_DOMESTIC_HOT_WATER_MODE = "dhw_mode";
    public static final String CHANNEL_DOMESTIC_HOT_WATER_SETPOINT = "tdhwset";
    public static final String CHANNEL_FLAME = "flame";
    public static final String CHANNEL_RELATIVE_MODULATION_LEVEL = "modulevel";
    public static final String CHANNEL_MAXIMUM_MODULATION_LEVEL = "maxrelmdulevel";

    public static final Set<String> SUPPORTED_CHANNEL_IDS = ImmutableSet.of(CHANNEL_ROOM_TEMPERATURE,
            CHANNEL_ROOM_SETPOINT, CHANNEL_FLOW_TEMPERATURE, CHANNEL_RETURN_TEMPERATURE, CHANNEL_OUTSIDE_TEMPERATURE,
            CHANNEL_CENTRAL_HEATING_WATER_PRESSURE, CHANNEL_CENTRAL_HEATING_ENABLED, CHANNEL_CENTRAL_HEATING_MODE,
            CHANNEL_CENTRAL_HEATING_WATER_SETPOINT, CHANNEL_DOMESTIC_HOT_WATER_TEMPERATURE,
            CHANNEL_DOMESTIC_HOT_WATER_ENABLED, CHANNEL_DOMESTIC_HOT_WATER_MODE, CHANNEL_DOMESTIC_HOT_WATER_SETPOINT,
            CHANNEL_FLAME, CHANNEL_RELATIVE_MODULATION_LEVEL, CHANNEL_MAXIMUM_MODULATION_LEVEL);
}