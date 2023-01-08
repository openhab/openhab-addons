/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.regoheatpump.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RegoHeatPumpBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Boris Krivonog - Initial contribution
 */
@NonNullByDefault
public class RegoHeatPumpBindingConstants {

    public static final String BINDING_ID = "regoheatpump";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_IP_REGO6XX = new ThingTypeUID(BINDING_ID, "ipRego6xx");
    public static final ThingTypeUID THING_TYPE_SERIAL_REGO6XX = new ThingTypeUID(BINDING_ID, "serialRego6xx");
    public static final ThingTypeUID THING_TYPE_IP_HUSDATA = new ThingTypeUID(BINDING_ID, "ipHusdata");
    public static final ThingTypeUID THING_TYPE_SERIAL_HUSDATA = new ThingTypeUID(BINDING_ID, "serialHusdata");

    // List of all Channel ids
    public static final String CHANNEL_GROUP_SENSOR_VALUES = "sensorValues#";
    public static final String CHANNEL_GROUP_CONTROL_DATA = "controlData#";
    public static final String CHANNEL_GROUP_DEVICE_VALUES = "deviceValues#";
    public static final String CHANNEL_GROUP_SETTINGS = "settings#";
    public static final String CHANNEL_GROUP_OPERATING_TIMES = "operatingTimes#";
    public static final String CHANNEL_LAST_ERROR = "status#lastError";
    public static final String CHANNEL_LAST_ERROR_TIMESTAMP = CHANNEL_LAST_ERROR + "Timestamp";
    public static final String CHANNEL_LAST_ERROR_TYPE = CHANNEL_LAST_ERROR + "Type";
    public static final String CHANNEL_FRONT_PANEL_POWER_LAMP = "frontPanel#powerLamp";
    public static final String CHANNEL_FRONT_PANEL_PUMP_LAMP = "frontPanel#heatPumpLamp";
    public static final String CHANNEL_FRONT_PANEL_ADDITIONAL_HEAT_LAMP = "frontPanel#additionalHeatLamp";
    public static final String CHANNEL_FRONT_PANEL_WATER_HEATER_LAMP = "frontPanel#hotWaterLamp";
    public static final String CHANNEL_FRONT_PANEL_ALARM_LAMP = "frontPanel#alarmLamp";

    public static final String REFRESH_INTERVAL = "refreshInterval";

    // TCP/IP thing
    public static final String HOST_PARAMETER = "address";
    public static final String TCP_PORT_PARAMETER = "tcpPort";

    // Serial thing
    public static final String PORT_NAME = "portName";
}
