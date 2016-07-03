/**
 * Copyright (c) 2014-2015 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.netatmo;

import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;

import com.google.common.collect.ImmutableSet;

/**
 * The {@link NetatmoBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class NetatmoBindingConstants {

    private static final String BINDING_ID = "netatmo";

    // Configuration keys
    public static final String EQUIPMENT_ID = "equipmentId";
    public static final String PARENT_ID = "parentId";

    // List of all Thing Type UIDs
    public final static ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "netatmoapi");
    public final static ThingTypeUID MAIN_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAMain");
    public final static ThingTypeUID MODULE1_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule1");
    public final static ThingTypeUID MODULE2_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule2");
    public final static ThingTypeUID MODULE3_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule3");
    public final static ThingTypeUID MODULE4_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule4");
    public final static ThingTypeUID PLUG_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAPlug");
    public final static ThingTypeUID THERM1_THING_TYPE = new ThingTypeUID(BINDING_ID, "NATherm1");

    // List of all Channel ids
    public final static String CHANNEL_TEMPERATURE = "Temperature";
    public final static String CHANNEL_HUMIDITY = "Humidity";
    public final static String CHANNEL_HUMIDEX = "Humidex";
    public final static String CHANNEL_TIMEUTC = "TimeStamp";
    public final static String CHANNEL_DEWPOINT = "Dewpoint";
    public final static String CHANNEL_DEWPOINTDEP = "DewpointDepression";
    public final static String CHANNEL_HEATINDEX = "HeatIndex";
    public final static String CHANNEL_LAST_STATUS_STORE = "LastStatusStore";
    public final static String CHANNEL_LAST_MESSAGE = "LastMessage";
    public final static String CHANNEL_LOCATION = "Location";
    public final static String CHANNEL_BOILER_ON = "BoilerOn";
    public final static String CHANNEL_BOILER_OFF = "BoilerOff";
    public final static String CHANNEL_DATE_MAX_TEMP = "date_max_temp";
    public final static String CHANNEL_DATE_MIN_TEMP = "date_min_temp";
    public final static String CHANNEL_MAX_TEMP = "min_temp";
    public final static String CHANNEL_MIN_TEMP = "max_temp";
    public final static String CHANNEL_ABSOLUTE_PRESSURE = "AbsolutePressure";
    public final static String CHANNEL_CO2 = "Co2";
    public final static String CHANNEL_NOISE = "Noise";
    public final static String CHANNEL_PRESSURE = "Pressure";
    public final static String CHANNEL_RAIN = "Rain";
    public final static String CHANNEL_SUM_RAIN1 = "sum_rain_1";
    public final static String CHANNEL_SUM_RAIN24 = "sum_rain_24";
    public final static String CHANNEL_WIND_ANGLE = "WindAngle";
    public final static String CHANNEL_WIND_STRENGTH = "WindStrength";
    public final static String CHANNEL_GUST_ANGLE = "GustAngle";
    public final static String CHANNEL_GUST_STRENGTH = "GustStrength";
    public final static String CHANNEL_LOW_BATTERY = "LowBattery";
    public final static String CHANNEL_BATTERY_LEVEL = "BatteryVP";
    public final static String CHANNEL_WIFI_STATUS = "WifiStatus";
    public final static String CHANNEL_RF_STATUS = "RfStatus";
    public final static String CHANNEL_UNIT = "Unit";
    public final static String CHANNEL_WIND_UNIT = "WindUnit";
    public final static String CHANNEL_PRESSURE_UNIT = "PressureUnit";

    // Thermostat specific channels
    public final static String CHANNEL_SETPOINT_MODE = "SetpointMode";
    public final static String CHANNEL_SETPOINT_TEMP = "SetpointTemperature";
    // public final static String CHANNEL_SETPOINT_END_TIME = "setpoint_endtime";

    // Module Properties
    public final static String PROPERTY_BATTERY_MIN = "batteryMin";
    public final static String PROPERTY_BATTERY_MAX = "batteryMax";
    public final static String PROPERTY_BATTERY_LOW = "batteryLow";
    public final static String PROPERTY_SIGNAL_LEVELS = "signalLevels";
    public final static String PROPERTY_ACTUAL_APP = "actualApp";

    // List of all supported physical devices and modules
    public final static Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(MAIN_THING_TYPE,
            MODULE1_THING_TYPE, MODULE2_THING_TYPE, MODULE3_THING_TYPE, MODULE4_THING_TYPE, PLUG_THING_TYPE,
            THERM1_THING_TYPE);

    // List of all adressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(MAIN_THING_TYPE,
            MODULE1_THING_TYPE, MODULE2_THING_TYPE, MODULE3_THING_TYPE, MODULE4_THING_TYPE, PLUG_THING_TYPE,
            THERM1_THING_TYPE, APIBRIDGE_THING_TYPE);

}
