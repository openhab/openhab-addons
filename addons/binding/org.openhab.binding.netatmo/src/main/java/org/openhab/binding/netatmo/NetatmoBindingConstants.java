/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
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
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "netatmoapi");
    public static final ThingTypeUID MAIN_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAMain");
    public static final ThingTypeUID MODULE1_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule1");
    public static final ThingTypeUID MODULE2_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule2");
    public static final ThingTypeUID MODULE3_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule3");
    public static final ThingTypeUID MODULE4_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule4");
    public static final ThingTypeUID HOMECOACH_THING_TYPE = new ThingTypeUID(BINDING_ID, "NHC");
    public static final ThingTypeUID PLUG_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAPlug");
    public static final ThingTypeUID THERM1_THING_TYPE = new ThingTypeUID(BINDING_ID, "NATherm1");

    // List of all Channel ids
    public static final String CHANNEL_TEMPERATURE = "Temperature";
    public static final String CHANNEL_TEMP_TREND = "TempTrend";
    public static final String CHANNEL_HUMIDITY = "Humidity";
    public static final String CHANNEL_HUMIDEX = "Humidex";
    public static final String CHANNEL_TIMEUTC = "TimeStamp";
    public static final String CHANNEL_DEWPOINT = "Dewpoint";
    public static final String CHANNEL_DEWPOINTDEP = "DewpointDepression";
    public static final String CHANNEL_HEATINDEX = "HeatIndex";
    public static final String CHANNEL_LAST_STATUS_STORE = "LastStatusStore";
    public static final String CHANNEL_LAST_MESSAGE = "LastMessage";
    public static final String CHANNEL_LOCATION = "Location";
    public static final String CHANNEL_BOILER_ON = "BoilerOn";
    public static final String CHANNEL_BOILER_OFF = "BoilerOff";
    public static final String CHANNEL_DATE_MAX_TEMP = "DateMaxTemp";
    public static final String CHANNEL_DATE_MIN_TEMP = "DateMinTemp";
    public static final String CHANNEL_MAX_TEMP = "MaxTemp";
    public static final String CHANNEL_MIN_TEMP = "MinTemp";
    public static final String CHANNEL_ABSOLUTE_PRESSURE = "AbsolutePressure";
    public static final String CHANNEL_CO2 = "Co2";
    public static final String CHANNEL_NOISE = "Noise";
    public static final String CHANNEL_PRESSURE = "Pressure";
    public static final String CHANNEL_PRESS_TREND = "PressTrend";
    public static final String CHANNEL_RAIN = "Rain";
    public static final String CHANNEL_SUM_RAIN1 = "SumRain1";
    public static final String CHANNEL_SUM_RAIN24 = "SumRain24";
    public static final String CHANNEL_WIND_ANGLE = "WindAngle";
    public static final String CHANNEL_WIND_STRENGTH = "WindStrength";
    public static final String CHANNEL_GUST_ANGLE = "GustAngle";
    public static final String CHANNEL_GUST_STRENGTH = "GustStrength";
    public static final String CHANNEL_LOW_BATTERY = "LowBattery";
    public static final String CHANNEL_BATTERY_LEVEL = "BatteryVP";
    public static final String CHANNEL_WIFI_STATUS = "WifiStatus";
    public static final String CHANNEL_RF_STATUS = "RfStatus";
    public static final String CHANNEL_UNIT = "Unit";
    public static final String CHANNEL_WIND_UNIT = "WindUnit";
    public static final String CHANNEL_PRESSURE_UNIT = "PressureUnit";

    // Healthy Home Coach specific channel
    public static final String CHANNEL_HEALTH_INDEX = "HealthIndex";

    // Thermostat specific channels
    public static final String CHANNEL_SETPOINT_MODE = "SetpointMode";
    public static final String CHANNEL_SETPOINT_END_TIME = "SetpointEndTime";
    public static final String CHANNEL_SETPOINT_TEMP = "Sp_Temperature";
    public static final String CHANNEL_THERM_RELAY = "ThermRelayCmd";
    public static final String CHANNEL_THERM_ORIENTATION = "ThermOrientation";
    public static final String CHANNEL_CONNECTED_BOILER = "ConnectedBoiler";
    public static final String CHANNEL_LAST_PLUG_SEEN = "LastPlugSeen";
    public static final String CHANNEL_LAST_BILAN = "LastBilan";

    // Module Properties
    public static final String PROPERTY_BATTERY_MIN = "batteryMin";
    public static final String PROPERTY_BATTERY_MAX = "batteryMax";
    public static final String PROPERTY_BATTERY_LOW = "batteryLow";
    public static final String PROPERTY_SIGNAL_LEVELS = "signalLevels";

    // List of all supported physical devices and modules
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = ImmutableSet.of(MAIN_THING_TYPE,
            MODULE1_THING_TYPE, MODULE2_THING_TYPE, MODULE3_THING_TYPE, MODULE4_THING_TYPE, HOMECOACH_THING_TYPE,
            PLUG_THING_TYPE, THERM1_THING_TYPE);

    // List of all adressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(MAIN_THING_TYPE,
            MODULE1_THING_TYPE, MODULE2_THING_TYPE, MODULE3_THING_TYPE, MODULE4_THING_TYPE, HOMECOACH_THING_TYPE,
            PLUG_THING_TYPE, THERM1_THING_TYPE, APIBRIDGE_THING_TYPE);

    public static final Set<String> MEASURABLE_CHANNELS = ImmutableSet.of(CHANNEL_BOILER_ON, CHANNEL_BOILER_OFF);

}
