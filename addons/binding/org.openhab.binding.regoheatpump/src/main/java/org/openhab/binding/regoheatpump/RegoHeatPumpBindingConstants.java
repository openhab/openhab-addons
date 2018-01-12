/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.regoheatpump;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link RegoHeatPumpBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Boris Krivonog - Initial contribution
 */
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
