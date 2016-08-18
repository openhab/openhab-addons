/**
 * Copyright (c) 2014-2016 openHAB UG (haftungsbeschraenkt) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.mysensors;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.openhab.binding.mysensors.internal.MySensorsMessage;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * The {@link MySensorsBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Oberföll - Initial contribution
 */
public class MySensorsBindingConstants {

    public static final String BINDING_ID = "mysensors";

    public static final String PARAMETER_NODEID = "nodeId";
    public static final String PARAMETER_CHILDID = "childId";
    public static final String PARAMETER_IPADDRESS = "ipAddress";
    public static final String PRAMETER_TCPPORT = "tcpPort";
    public static final String PARAMETER_SENDDELAY = "sendDelay";
    public static final String PARAMETER_BAUDRATE = "baudRate";
    public static final String PARAMETER_REQUESTACK = "requestack";

    public static final int MYSENSORS_MSG_TYPE_PRESENTATION = 0;
    public static final int MYSENSORS_MSG_TYPE_SET = 1;
    public static final int MYSENSORS_MSG_TYPE_REQ = 2;
    public static final int MYSENSORS_MSG_TYPE_INTERNAL = 3;
    public static final int MYSENSORS_MSG_TYPE_STREAM = 4;

    // Subtypes for presentation
    public static final int MYSENSORS_SUBTYPE_S_DOOR = 0;
    public static final int MYSENSORS_SUBTYPE_S_MOTION = 1;
    public static final int MYSENSORS_SUBTYPE_S_SMOKE = 2;
    public static final int MYSENSORS_SUBTYPE_S_LIGHT = 3;
    public static final int MYSENSORS_SUBTYPE_S_DIMMER = 4;
    public static final int MYSENSORS_SUBTYPE_S_COVER = 5;
    public static final int MYSENSORS_SUBTYPE_S_TEMP = 6;
    public static final int MYSENSORS_SUBTYPE_S_HUM = 7;
    public static final int MYSENSORS_SUBTYPE_S_BARO = 8;
    public static final int MYSENSORS_SUBTYPE_S_WIND = 9;
    public static final int MYSENSORS_SUBTYPE_S_RAIN = 10;
    public static final int MYSENSORS_SUBTYPE_S_UV = 11;
    public static final int MYSENSORS_SUBTYPE_S_WEIGHT = 12;
    public static final int MYSENSORS_SUBTYPE_S_POWER = 13;
    public static final int MYSENSORS_SUBTYPE_S_HEATER = 14;
    public static final int MYSENSORS_SUBTYPE_S_DISTANCE = 15;
    public static final int MYSENSORS_SUBTYPE_S_LIGHT_LEVEL = 16;
    public static final int MYSENSORS_SUBTYPE_S_WATER = 21;
    public static final int MYSENSORS_SUBTYPE_S_CUSTOM = 23;
    public static final int MYSENSORS_SUBTYPE_S_HVAC = 29;
    public static final int MYSENSORS_SUBTYPE_S_MULTIMETER = 30;
    public static final int MYSENSORS_SUBTYPE_S_SPRINKLER = 31;
    public static final int MYSENSORS_SUBTYPE_S_WATER_LEAK = 32;
    public static final int MYSENSORS_SUBTYPE_S_SOUND = 33;
    public static final int MYSENSORS_SUBTYPE_S_VIBRATION = 34;
    public static final int MYSENSORS_SUBTYPE_S_MOISTURE = 35;
    public static final int MYSENSORS_SUBTYPE_S_INFO = 36;
    public static final int MYSENSORS_SUBTYPE_S_GAS = 37;
    public static final int MYSENSORS_SUBTYPE_S_GPS = 38;
    public static final int MYSENSORS_SUBTYPE_S_WATER_QUALITY = 39;

    // Subtypes for set, req
    public static final int MYSENSORS_SUBTYPE_V_TEMP = 0;
    public static final int MYSENSORS_SUBTYPE_V_HUM = 1;
    public static final int MYSENSORS_SUBTYPE_V_STATUS = 2;
    public static final int MYSENSORS_SUBTYPE_V_PERCENTAGE = 3;
    public static final int MYSENSORS_SUBTYPE_V_PRESSURE = 4;
    public static final int MYSENSORS_SUBTYPE_V_FORECAST = 5;
    public static final int MYSENSORS_SUBTYPE_V_RAIN = 6;
    public static final int MYSENSORS_SUBTYPE_V_RAINRATE = 7;
    public static final int MYSENSORS_SUBTYPE_V_WIND = 8;
    public static final int MYSENSORS_SUBTYPE_V_GUST = 9;
    public static final int MYSENSORS_SUBTYPE_V_DIRECTION = 10;
    public static final int MYSENSORS_SUBTYPE_V_UV = 11;
    public static final int MYSENSORS_SUBTYPE_V_WEIGHT = 12;
    public static final int MYSENSORS_SUBTYPE_V_DISTANCE = 13;
    public static final int MYSENSORS_SUBTYPE_V_IMPEDANCE = 14;
    public static final int MYSENSORS_SUBTYPE_V_ARMED = 15;
    public static final int MYSENSORS_SUBTYPE_V_TRIPPED = 16;
    public static final int MYSENSORS_SUBTYPE_V_WATT = 17;
    public static final int MYSENSORS_SUBTYPE_V_KWH = 18;
    public static final int MYSENSORS_SUBTYPE_V_SCENE_ON = 19;
    public static final int MYSENSORS_SUBTYPE_V_SCENE_OFF = 20;
    public static final int MYSENSORS_SUBTYPE_V_HVAC_FLOW_STATE = 21;
    public static final int MYSENSORS_SUBTYPE_V_HVAC_SPEED = 22;
    public static final int MYSENSORS_SUBTYPE_V_LIGHT_LEVEL = 23;
    public static final int MYSENSORS_SUBTYPE_V_VAR1 = 24;
    public static final int MYSENSORS_SUBTYPE_V_VAR2 = 25;
    public static final int MYSENSORS_SUBTYPE_V_VAR3 = 26;
    public static final int MYSENSORS_SUBTYPE_V_VAR4 = 27;
    public static final int MYSENSORS_SUBTYPE_V_VAR5 = 28;
    public static final int MYSENSORS_SUBTYPE_V_UP = 29;
    public static final int MYSENSORS_SUBTYPE_V_DOWN = 30;
    public static final int MYSENSORS_SUBTYPE_V_STOP = 31;
    public static final int MYSENSORS_SUBTYPE_V_IR_SEND = 32;
    public static final int MYSENSORS_SUBTYPE_V_IR_RECEIVE = 33;
    public static final int MYSENSORS_SUBTYPE_V_FLOW = 34;
    public static final int MYSENSORS_SUBTYPE_V_VOLUME = 35;
    public static final int MYSENSORS_SUBTYPE_V_LOCK_STATUS = 36;
    public static final int MYSENSORS_SUBTYPE_V_LEVEL = 37;
    public static final int MYSENSORS_SUBTYPE_V_VOLTAGE = 38;
    public static final int MYSENSORS_SUBTYPE_V_CURRENT = 39;
    public static final int MYSENSORS_SUBTYPE_V_RGB = 40;
    public static final int MYSENSORS_SUBTYPE_V_RGBW = 41;
    public static final int MYSENSORS_SUBTYPE_V_ID = 42;
    public static final int MYSENSORS_SUBTYPE_V_UNIT_PREFIX = 43;
    public static final int MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_COOL = 44;
    public static final int MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_HEAT = 45;
    public static final int MYSENSORS_SUBTYPE_V_HVAC_FLOW_MODE = 46;
    public static final int MYSENSORS_SUBTYPE_V_TEXT = 47;
    public static final int MYSENSORS_SUBTYPE_V_CUSTOM = 48;
    public static final int MYSENSORS_SUBTYPE_V_POSITION = 49;
    public static final int MYSENSORS_SUBTYPE_V_IR_RECORD = 50;
    public static final int MYSENSORS_SUBTYPE_V_PH = 51;
    public static final int MYSENSORS_SUBTYPE_V_ORP = 52;
    public static final int MYSENSORS_SUBTYPE_V_EC = 53;
    public static final int MYSENSORS_SUBTYPE_V_VAR = 54;
    public static final int MYSENSORS_SUBTYPE_V_VA = 55;               
    public static final int MYSENSORS_SUBTYPE_V_POWER_FACTOR = 56;  

    public static final int MYSENSORS_SUBTYPE_I_BATTERY_LEVEL = 0;
    public static final int MYSENSORS_SUBTYPE_I_TIME = 1;
    public static final int MYSENSORS_SUBTYPE_I_VERSION = 2;
    public static final int MYSENSORS_SUBTYPE_I_ID_REQUEST = 3;
    public static final int MYSENSORS_SUBTYPE_I_ID_RESPONSE = 4;
    public static final int MYSENSORS_SUBTYPE_I_INCLUSION_MODE = 5;
    public static final int MYSENSORS_SUBTYPE_I_CONFIG = 6;
    public static final int MYSENSORS_SUBTYPE_I_FIND_PARENT = 7;
    public static final int MYSENSORS_SUBTYPE_I_FIND_PARENT_RESPONSE = 8;
    public static final int MYSENSORS_SUBTYPE_I_LOG_MESSAGE = 9;
    public static final int MYSENSORS_SUBTYPE_I_CHILDREN = 10;
    public static final int MYSENSORS_SUBTYPE_I_SKETCH_NAME = 11;
    public static final int MYSENSORS_SUBTYPE_I_SKETCH_VERSION = 12;
    public static final int MYSENSORS_SUBTYPE_I_REBOOT = 13;
    public static final int MYSENSORS_SUBTYPE_I_GATEWAY_READY = 14;
    public static final int MYSENSORS_SUBTYPE_I_REQUEST_SIGNING = 15;
    public static final int MYSENSORS_SUBTYPE_I_GET_NONCE = 16;
    public static final int MYSENSORS_SUBTYPE_I_GET_NONCE_RESONSE = 17;
    public static final int MYSENSORS_SUBTYPE_I_HEARTBEAT_REQUEST = 18;
    public static final int MYSENSORS_SUBTYPE_I_PRESENTATION = 19;
    public static final int MYSENSORS_SUBTYPE_I_DISCOVER = 20;
    public static final int MYSENSORS_SUBTYPE_I_DISCOVER_RESPONSE = 21;
    public static final int MYSENSORS_SUBTYPE_I_HEARTBEAT_RESPONSE = 22;
    public static final int MYSENSORS_SUBTYPE_I_LOCKED = 23;
    public static final int MYSENSORS_SUBTYPE_I_PING = 24;
    public static final int MYSENSORS_SUBTYPE_I_PONG = 25;
    public static final int MYSENSORS_SUBTYPE_I_REGISTRATION_REQUEST = 26;
    public static final int MYSENSORS_SUBTYPE_I_REGISTRATION_RESPONSE = 27;
    public static final int MYSENSORS_SUBTYPE_I_DEBUG = 28;    

    public static final int MYSENSORS_NUMBER_OF_RETRIES = 5;
    public static final int[] MYSENSORS_RETRY_TIMES = { 0, 100, 500, 1000, 2000 };

    // List of all Thing Type UIDs
    public final static ThingTypeUID THING_TYPE_HUMIDITY = new ThingTypeUID(BINDING_ID, "humidity");
    public final static ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    public final static ThingTypeUID THING_TYPE_MULTIMETER = new ThingTypeUID(BINDING_ID, "multimeter");
    public final static ThingTypeUID THING_TYPE_LIGHT = new ThingTypeUID(BINDING_ID, "light");
    public final static ThingTypeUID THING_TYPE_POWER = new ThingTypeUID(BINDING_ID, "power");
    public final static ThingTypeUID THING_TYPE_BARO = new ThingTypeUID(BINDING_ID, "baro");
    public final static ThingTypeUID THING_TYPE_DOOR = new ThingTypeUID(BINDING_ID, "door");
    public final static ThingTypeUID THING_TYPE_MOTION = new ThingTypeUID(BINDING_ID, "motion");
    public final static ThingTypeUID THING_TYPE_SMOKE = new ThingTypeUID(BINDING_ID, "smoke");
    public final static ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public final static ThingTypeUID THING_TYPE_COVER = new ThingTypeUID(BINDING_ID, "cover");
    public final static ThingTypeUID THING_TYPE_WIND = new ThingTypeUID(BINDING_ID, "wind");
    public final static ThingTypeUID THING_TYPE_RAIN = new ThingTypeUID(BINDING_ID, "rain");
    public final static ThingTypeUID THING_TYPE_UV = new ThingTypeUID(BINDING_ID, "uv");
    public final static ThingTypeUID THING_TYPE_WEIGHT = new ThingTypeUID(BINDING_ID, "weight");
    public final static ThingTypeUID THING_TYPE_DISTANCE = new ThingTypeUID(BINDING_ID, "distance");
    public final static ThingTypeUID THING_TYPE_LIGHT_LEVEL = new ThingTypeUID(BINDING_ID, "light-level");
    public final static ThingTypeUID THING_TYPE_WATER = new ThingTypeUID(BINDING_ID, "waterMeter");
    public final static ThingTypeUID THING_TYPE_CUSTOM = new ThingTypeUID(BINDING_ID, "customSensor");

    public final static ThingTypeUID THING_TYPE_HVAC = new ThingTypeUID(BINDING_ID, "hvacThermostat");
	
    public final static ThingTypeUID THING_TYPE_LOCK = new ThingTypeUID(BINDING_ID, "lock");
    public final static ThingTypeUID THING_TYPE_LEVEL = new ThingTypeUID(BINDING_ID, "level");
    public final static ThingTypeUID THING_TYPE_RGB_LIGHT = new ThingTypeUID(BINDING_ID, "rgbLight");
    public final static ThingTypeUID THING_TYPE_RGBW_LIGHT = new ThingTypeUID(BINDING_ID, "rgbwLight");
    public final static ThingTypeUID THING_TYPE_RGBW_LIGHT = new ThingTypeUID(BINDING_ID, "rgbwLight");
    public final static ThingTypeUID THING_TYPE_PH_METER = new ThingTypeUID(BINDING_ID, "phMeter");

    public final static ThingTypeUID THING_TYPE_BRIDGE_SER = new ThingTypeUID(BINDING_ID, "bridge-ser");
    public final static ThingTypeUID THING_TYPE_BRIDGE_ETH = new ThingTypeUID(BINDING_ID, "bridge-eth");

    // List of all Channel ids
    public final static String CHANNEL_HUM = "hum";
    public final static String CHANNEL_TEMP = "temp";
    public final static String CHANNEL_VOLT = "volt";
    public final static String CHANNEL_WATT = "watt";
    public final static String CHANNEL_KWH = "kwh";
    public final static String CHANNEL_STATUS = "status";
    public final static String CHANNEL_PRESSURE = "pressure";
    public final static String CHANNEL_BARO = "baro";
    public final static String CHANNEL_TRIPPED = "tripped";
    public final static String CHANNEL_ARMED = "armed";
    public final static String CHANNEL_DIMMER = "dimmer";
    public final static String CHANNEL_COVER = "cover";
    public final static String CHANNEL_WIND = "wind";
    public final static String CHANNEL_GUST = "gust";
    public final static String CHANNEL_RAIN = "rain";
    public final static String CHANNEL_RAINRATE = "rainrate";
    public final static String CHANNEL_UV = "uv";
    public final static String CHANNEL_WEIGHT = "weight";
    public final static String CHANNEL_IMPEDANCE = "impedance";
    public final static String CHANNEL_CURRENT = "current";
    public final static String CHANNEL_DISTANCE = "distance";
    public final static String CHANNEL_LIGHT_LEVEL = "light-level";
    public final static String CHANNEL_VERSION = "version";
    public final static String CHANNEL_BATTERY = "battery";
    public final static String CHANNEL_HVAC_FLOW_STATE = "hvac-flow-state";
    public final static String CHANNEL_HVAC_FLOW_MODE = "hvac-flow-mode";
    public final static String CHANNEL_HVAC_SETPOINT_HEAT = "hvac-setPoint-heat";
    public final static String CHANNEL_HVAC_SETPOINT_COOL = "hvac-setPoint-cool";
    public final static String CHANNEL_HVAC_SPEED = "hvac-speed";
    public final static String CHANNEL_VAR1 = "var1";
    public final static String CHANNEL_VAR2 = "var2";
    public final static String CHANNEL_VAR3 = "var3";
    public final static String CHANNEL_VAR4 = "var4";
    public final static String CHANNEL_VAR5 = "var5";
    public final static String CHANNEL_FLOW = "flow";
    public final static String CHANNEL_VOLUME = "volume";
	public final static String CHANNEL_LOCK_STATUS = "lock-status";
	public final static String CHANNEL_LEVEL = "level";
	public final static String CHANNEL_RGB = "rgb";
	public final static String CHANNEL_RGBW = "rgbw";
	public final static String CHANNEL_ID = "id";
	public final static String CHANNEL_UNIT_PREFIX = "unit-prefix";
	public final static String CHANNEL_TEXT = "text";
	public final static String CHANNEL_CUSTOM = "custom";
	public final static String CHANNEL_POSITION = "position";
	public final static String CHANNEL_IR_RECORD = "ir-record";
	public final static String CHANNEL_PH = "ph";
	public final static String CHANNEL_ORP = "orp";
	public final static String CHANNEL_EC = "ec";
	public final static String CHANNEL_VAR = "var";
	public final static String CHANNEL_VA = "va";
	public final static String CHANNEL_POWER_FACTOR = "power-factor";

    public final static String CHANNEL_LAST_UPDATE = "lastupdate";

    // Wait time Arduino reset
    public final static int RESET_TIME = 3000;

    // I version message for startup check
    public static final MySensorsMessage I_VERSION_MESSAGE = new MySensorsMessage(0, 0, 3, 0, false, 2, "");

    public final static Map<Number, String> CHANNEL_MAP = new HashMap<Number, String>() {
        /**
         *
         */
        private static final long serialVersionUID = -7970323220036599380L;

        {
            put(MYSENSORS_SUBTYPE_V_TEMP, CHANNEL_TEMP);
            put(MYSENSORS_SUBTYPE_V_HUM, CHANNEL_HUM);
            put(MYSENSORS_SUBTYPE_V_STATUS, CHANNEL_STATUS);
            put(MYSENSORS_SUBTYPE_V_VOLTAGE, CHANNEL_VOLT);
            put(MYSENSORS_SUBTYPE_V_WATT, CHANNEL_WATT);
            put(MYSENSORS_SUBTYPE_V_KWH, CHANNEL_KWH);
            put(MYSENSORS_SUBTYPE_V_PRESSURE, CHANNEL_PRESSURE);
            put(MYSENSORS_SUBTYPE_V_FORECAST, CHANNEL_BARO);
            put(MYSENSORS_SUBTYPE_V_TRIPPED, CHANNEL_TRIPPED);
            put(MYSENSORS_SUBTYPE_V_ARMED, CHANNEL_ARMED);
            put(MYSENSORS_SUBTYPE_V_PERCENTAGE, CHANNEL_DIMMER);
            put(MYSENSORS_SUBTYPE_V_UP, CHANNEL_COVER);
            put(MYSENSORS_SUBTYPE_V_DOWN, CHANNEL_COVER);
            put(MYSENSORS_SUBTYPE_V_STOP, CHANNEL_COVER);
            put(MYSENSORS_SUBTYPE_V_WIND, CHANNEL_WIND);
            put(MYSENSORS_SUBTYPE_V_GUST, CHANNEL_GUST);
            put(MYSENSORS_SUBTYPE_V_RAIN, CHANNEL_RAIN);
            put(MYSENSORS_SUBTYPE_V_RAINRATE, CHANNEL_RAINRATE);
            put(MYSENSORS_SUBTYPE_V_UV, CHANNEL_UV);
            put(MYSENSORS_SUBTYPE_V_WEIGHT, CHANNEL_WEIGHT);
            put(MYSENSORS_SUBTYPE_V_IMPEDANCE, CHANNEL_IMPEDANCE);
            put(MYSENSORS_SUBTYPE_V_DISTANCE, CHANNEL_DISTANCE);
            put(MYSENSORS_SUBTYPE_V_LIGHT_LEVEL, CHANNEL_LIGHT_LEVEL);
            put(MYSENSORS_SUBTYPE_V_CURRENT, CHANNEL_CURRENT);
            put(MYSENSORS_SUBTYPE_V_HVAC_FLOW_STATE, CHANNEL_HVAC_FLOW_STATE);
            put(MYSENSORS_SUBTYPE_V_HVAC_SPEED, CHANNEL_HVAC_SPEED);
            put(MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_COOL, CHANNEL_HVAC_SETPOINT_COOL);
            put(MYSENSORS_SUBTYPE_V_HVAC_SETPOINT_HEAT, CHANNEL_HVAC_SETPOINT_HEAT);
            put(MYSENSORS_SUBTYPE_V_HVAC_FLOW_MODE, CHANNEL_HVAC_FLOW_MODE);
            put(MYSENSORS_SUBTYPE_V_VAR1, CHANNEL_VAR1);
            put(MYSENSORS_SUBTYPE_V_VAR2, CHANNEL_VAR2);
            put(MYSENSORS_SUBTYPE_V_VAR3, CHANNEL_VAR3);
            put(MYSENSORS_SUBTYPE_V_VAR4, CHANNEL_VAR4);
            put(MYSENSORS_SUBTYPE_V_VAR5, CHANNEL_VAR5);
            put(MYSENSORS_SUBTYPE_V_FLOW, CHANNEL_FLOW);
            put(MYSENSORS_SUBTYPE_V_VOLUME, CHANNEL_VOLUME);
			put(MYSENSORS_SUBTYPE_V_LOCK_STATUS, CHANNEL_LOCK_STATUS);
			put(MYSENSORS_SUBTYPE_V_LEVEL, CHANNEL_LEVEL);
			put(MYSENSORS_SUBTYPE_V_RGB, CHANNEL_RGB);
			put(MYSENSORS_SUBTYPE_V_RGBW, CHANNEL_RGBW);
			put(MYSENSORS_SUBTYPE_V_ID, CHANNEL_ID);
			put(MYSENSORS_SUBTYPE_V_UNIT_PREFIX, CHANNEL_UNIT_PREFIX);
			put(MYSENSORS_SUBTYPE_V_TEXT, CHANNEL_TEXT);
			put(MYSENSORS_SUBTYPE_V_CUSTOM, CHANNEL_CUSTOM);
			put(MYSENSORS_SUBTYPE_V_POSITION, CHANNEL_POSITION);
			put(MYSENSORS_SUBTYPE_V_IR_RECORD, CHANNEL_IR_RECORD);
			put(MYSENSORS_SUBTYPE_V_PH, CHANNEL_PH);
			put(MYSENSORS_SUBTYPE_V_ORP, CHANNEL_ORP);
			put(MYSENSORS_SUBTYPE_V_EC, CHANNEL_EC);
			put(MYSENSORS_SUBTYPE_V_VAR, CHANNEL_VAR);
			put(MYSENSORS_SUBTYPE_V_VA, CHANNEL_VA);
			put(MYSENSORS_SUBTYPE_V_POWER_FACTOR, CHANNEL_POWER_FACTOR);

        }
    };

    public final static Map<Number, String> CHANNEL_MAP_INTERNAL = new HashMap<Number, String>() {
        /**
         *
         */
        private static final long serialVersionUID = 6273187523631143905L;

        {
            put(MYSENSORS_SUBTYPE_I_VERSION, CHANNEL_VERSION);
            put(MYSENSORS_SUBTYPE_I_BATTERY_LEVEL, CHANNEL_BATTERY);
        }
    };

    /** Supported Things without bridge */
    public final static Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_HUMIDITY,
            THING_TYPE_TEMPERATURE, THING_TYPE_LIGHT, THING_TYPE_MULTIMETER, THING_TYPE_POWER, THING_TYPE_BARO,
            THING_TYPE_DOOR, THING_TYPE_MOTION, THING_TYPE_SMOKE, THING_TYPE_DIMMER, THING_TYPE_COVER, THING_TYPE_WIND,
            THING_TYPE_RAIN, THING_TYPE_UV, THING_TYPE_WEIGHT, THING_TYPE_DISTANCE, THING_TYPE_LIGHT_LEVEL,
            THING_TYPE_HVAC, THING_TYPE_WATER, THING_TYPE_CUSTOM, THING_TYPE_LOCK, THING_TYPE_LEVEL, THING_TYPE_RGB_LIGHT,
			THING_TYPE_RGBW_LIGHT, THING_TYPE_RGBW_LIGHT, THING_TYPE_PH_METER);
    /** Supported bridges */
    public final static Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BRIDGE_SER,
            THING_TYPE_BRIDGE_ETH);

    /** Supported devices (things + brdiges) */
    public final static Collection<ThingTypeUID> SUPPORTED_DEVICE_TYPES_UIDS = Lists.newArrayList(THING_TYPE_HUMIDITY,
            THING_TYPE_TEMPERATURE, THING_TYPE_LIGHT, THING_TYPE_MULTIMETER, THING_TYPE_POWER, THING_TYPE_BARO,
            THING_TYPE_DOOR, THING_TYPE_MOTION, THING_TYPE_SMOKE, THING_TYPE_DIMMER, THING_TYPE_COVER, THING_TYPE_WIND,
            THING_TYPE_RAIN, THING_TYPE_UV, THING_TYPE_WEIGHT, THING_TYPE_DISTANCE, THING_TYPE_LIGHT_LEVEL,
            THING_TYPE_HVAC, THING_TYPE_WATER, THING_TYPE_CUSTOM, THING_TYPE_LOCK, THING_TYPE_LEVEL, THING_TYPE_RGB_LIGHT,
			THING_TYPE_RGBW_LIGHT, THING_TYPE_RGBW_LIGHT, THING_TYPE_PH_METER THING_TYPE_BRIDGE_SER, THING_TYPE_BRIDGE_ETH);
}
