/**
 * Copyright (c) 2014-2016 by the respective copyright holders.
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
    public final static ThingTypeUID THING_TYPE_IP_REGO6XX = new ThingTypeUID(BINDING_ID, "ipRego6xx");
    public final static ThingTypeUID THING_TYPE_SERIAL_REGO6XX = new ThingTypeUID(BINDING_ID, "serialRego6xx");

    // List of all Channel ids
    public final static String CHANNEL_GROUP_SENSOR_VALUES = "sensorValues#";
    public final static String CHANNEL_GROUP_DEVICE_VALUES = "deviceValues#";
    public final static String CHANNEL_GROUP_REGISTERS = "registers#";
    public final static String CHANNEL_LAST_ERROR = "status#lastError";
    public final static String CHANNEL_LAST_ERROR_TIMESTAMP = CHANNEL_LAST_ERROR + "Timestamp";
    public final static String CHANNEL_LAST_ERROR_TYPE = CHANNEL_LAST_ERROR + "Type";
    public final static String CHANNEL_FRONT_PANEL_POWER_LAMP = "frontPanel#powerLamp";
    public final static String CHANNEL_FRONT_PANEL_PUMP_LAMP = "frontPanel#heatPumpLamp";
    public final static String CHANNEL_FRONT_PANEL_ADDITIONAL_HEAT_LAMP = "frontPanel#additionalHeatLamp";
    public final static String CHANNEL_FRONT_PANEL_WATER_HEATER_LAMP = "frontPanel#hotWaterLamp";
    public final static String CHANNEL_FRONT_PANEL_ALARM_LAMP = "frontPanel#alarmLamp";

    public final static String REFRESH_INTERVAL = "refreshInterval";

    // TCP/IP thing
    public final static String HOST_PARAMETER = "address";
    public final static String TCP_PORT_PARAMETER = "tcpPort";

    // Serial thing
    public final static String PORT_NAME = "portName";
}
