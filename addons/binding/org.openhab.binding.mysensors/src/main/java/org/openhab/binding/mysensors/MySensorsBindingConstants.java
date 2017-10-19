/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
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
import org.openhab.binding.mysensors.converter.MySensorsDecimalTypeConverter;
import org.openhab.binding.mysensors.converter.MySensorsOnOffTypeConverter;
import org.openhab.binding.mysensors.converter.MySensorsOpenCloseTypeConverter;
import org.openhab.binding.mysensors.converter.MySensorsPercentTypeConverter;
import org.openhab.binding.mysensors.converter.MySensorsRGBTypeConverter;
import org.openhab.binding.mysensors.converter.MySensorsRGBWTypeConverter;
import org.openhab.binding.mysensors.converter.MySensorsStringTypeConverter;
import org.openhab.binding.mysensors.converter.MySensorsTypeConverter;
import org.openhab.binding.mysensors.converter.MySensorsUpDownTypeConverter;
import org.openhab.binding.mysensors.internal.MySensorsUtility;
import org.openhab.binding.mysensors.internal.protocol.message.MySensorsMessageSubType;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * The {@link MySensorsBinding} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Tim Oberföll
 */
public class MySensorsBindingConstants {

    public static final String BINDING_ID = "mysensors";

    // parameters / fields of a MySensors message
    public static final String PARAMETER_NODEID = "nodeId";
    public static final String PARAMETER_CHILDID = "childId";
    public static final String PARAMETER_IPADDRESS = "ipAddress";
    public static final String PRAMETER_TCPPORT = "tcpPort";
    public static final String PARAMETER_SENDDELAY = "sendDelay";
    public static final String PARAMETER_BAUDRATE = "baudRate";
    public static final String PARAMETER_REQUESTACK = "requestack";
    public static final String PARAMETER_TOPICSUBSCRIBE = "topicSubscribe";
    public static final String PARAMETER_TOPICPUBLISH = "topicPublish";
    public static final String PARAMETER_BROKERNAME = "brokerName";

    /**
     * All knowing thing. A node with nodeId 999 and childId 999 receives all messages
     * received from the MySensors bridge/gateway. Useful for debugging and for implementation
     * of features not covered by the binding (for example with rules)
     */
    public static final int MYSENSORS_NODE_ID_ALL_KNOWING = 999;
    public static final int MYSENSORS_CHILD_ID_ALL_KNOWING = 999;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_HUMIDITY = new ThingTypeUID(BINDING_ID, "humidity");
    public static final ThingTypeUID THING_TYPE_TEMPERATURE = new ThingTypeUID(BINDING_ID, "temperature");
    public static final ThingTypeUID THING_TYPE_MULTIMETER = new ThingTypeUID(BINDING_ID, "multimeter");
    public static final ThingTypeUID THING_TYPE_BINARY = new ThingTypeUID(BINDING_ID, "light");
    public static final ThingTypeUID THING_TYPE_POWER = new ThingTypeUID(BINDING_ID, "power");
    public static final ThingTypeUID THING_TYPE_BARO = new ThingTypeUID(BINDING_ID, "baro");
    public static final ThingTypeUID THING_TYPE_DOOR = new ThingTypeUID(BINDING_ID, "door");
    public static final ThingTypeUID THING_TYPE_MOTION = new ThingTypeUID(BINDING_ID, "motion");
    public static final ThingTypeUID THING_TYPE_SMOKE = new ThingTypeUID(BINDING_ID, "smoke");
    public static final ThingTypeUID THING_TYPE_DIMMER = new ThingTypeUID(BINDING_ID, "dimmer");
    public static final ThingTypeUID THING_TYPE_COVER = new ThingTypeUID(BINDING_ID, "cover");
    public static final ThingTypeUID THING_TYPE_WIND = new ThingTypeUID(BINDING_ID, "wind");
    public static final ThingTypeUID THING_TYPE_RAIN = new ThingTypeUID(BINDING_ID, "rain");
    public static final ThingTypeUID THING_TYPE_UV = new ThingTypeUID(BINDING_ID, "uv");
    public static final ThingTypeUID THING_TYPE_WEIGHT = new ThingTypeUID(BINDING_ID, "weight");
    public static final ThingTypeUID THING_TYPE_DISTANCE = new ThingTypeUID(BINDING_ID, "distance");
    public static final ThingTypeUID THING_TYPE_LIGHT_LEVEL = new ThingTypeUID(BINDING_ID, "light-level");
    public static final ThingTypeUID THING_TYPE_WATER = new ThingTypeUID(BINDING_ID, "waterMeter");
    public static final ThingTypeUID THING_TYPE_CUSTOM = new ThingTypeUID(BINDING_ID, "customSensor");
    public static final ThingTypeUID THING_TYPE_HVAC = new ThingTypeUID(BINDING_ID, "hvacThermostat");
    public static final ThingTypeUID THING_TYPE_LOCK = new ThingTypeUID(BINDING_ID, "lock");
    public static final ThingTypeUID THING_TYPE_SOUND = new ThingTypeUID(BINDING_ID, "sound");
    public static final ThingTypeUID THING_TYPE_RGB_LIGHT = new ThingTypeUID(BINDING_ID, "rgbLight");
    public static final ThingTypeUID THING_TYPE_RGBW_LIGHT = new ThingTypeUID(BINDING_ID, "rgbwLight");
    public static final ThingTypeUID THING_TYPE_WATER_QUALITY = new ThingTypeUID(BINDING_ID, "waterQuality");
    public static final ThingTypeUID THING_TYPE_MYSENSORS_MESSAGE = new ThingTypeUID(BINDING_ID, "mySensorsMessage");
    public static final ThingTypeUID THING_TYPE_TEXT = new ThingTypeUID(BINDING_ID, "text");
    public static final ThingTypeUID THING_TYPE_IR = new ThingTypeUID(BINDING_ID, "ir");
    public static final ThingTypeUID THING_TYPE_AIR_QUALITY = new ThingTypeUID(BINDING_ID, "airQuality");
    public static final ThingTypeUID THING_TYPE_DUST = new ThingTypeUID(BINDING_ID, "dust");
    public static final ThingTypeUID THING_TYPE_COLOR_SENSOR = new ThingTypeUID(BINDING_ID, "colorSensor");
    public static final ThingTypeUID THING_TYPE_MOISTURE = new ThingTypeUID(BINDING_ID, "moisture");
    public static final ThingTypeUID THING_TYPE_SPRINKLER = new ThingTypeUID(BINDING_ID, "sprinkler");
    public static final ThingTypeUID THING_TYPE_HEATER = new ThingTypeUID(BINDING_ID, "heater");
    public static final ThingTypeUID THING_TYPE_VIBRATION = new ThingTypeUID(BINDING_ID, "vibration");
    public static final ThingTypeUID THING_TYPE_WATER_LEAK = new ThingTypeUID(BINDING_ID, "waterLeak");
    public static final ThingTypeUID THING_TYPE_GAS = new ThingTypeUID(BINDING_ID, "gas");
    public static final ThingTypeUID THING_TYPE_GPS = new ThingTypeUID(BINDING_ID, "gps");
    public static final ThingTypeUID THING_TYPE_SCENE_CONTROLLER = new ThingTypeUID(BINDING_ID, "scene");

    // List of bridges
    public static final ThingTypeUID THING_TYPE_BRIDGE_SER = new ThingTypeUID(BINDING_ID, "bridge-ser");
    public static final ThingTypeUID THING_TYPE_BRIDGE_ETH = new ThingTypeUID(BINDING_ID, "bridge-eth");
    public static final ThingTypeUID THING_TYPE_BRIDGE_MQTT = new ThingTypeUID(BINDING_ID, "bridge-mqtt");

    // List of all Channel ids
    public static final String CHANNEL_HUM = "hum";
    public static final String CHANNEL_TEMP = "temp";
    public static final String CHANNEL_VOLT = "volt";
    public static final String CHANNEL_WATT = "watt";
    public static final String CHANNEL_KWH = "kwh";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_PRESSURE = "pressure";
    public static final String CHANNEL_BARO = "baro";
    public static final String CHANNEL_TRIPPED = "tripped";
    public static final String CHANNEL_ARMED = "armed";
    public static final String CHANNEL_PERCENTAGE = "percentage";
    public static final String CHANNEL_COVER = "cover";
    public static final String CHANNEL_WIND = "wind";
    public static final String CHANNEL_GUST = "gust";
    public static final String CHANNEL_RAIN = "rain";
    public static final String CHANNEL_RAINRATE = "rainrate";
    public static final String CHANNEL_UV = "uv";
    public static final String CHANNEL_WEIGHT = "weight";
    public static final String CHANNEL_IMPEDANCE = "impedance";
    public static final String CHANNEL_CURRENT = "current";
    public static final String CHANNEL_DISTANCE = "distance";
    public static final String CHANNEL_LIGHT_LEVEL = "light-level";
    public static final String CHANNEL_VERSION = "version";
    public static final String CHANNEL_BATTERY = "battery";
    public static final String CHANNEL_HVAC_FLOW_STATE = "hvac-flow-state";
    public static final String CHANNEL_HVAC_FLOW_MODE = "hvac-flow-mode";
    public static final String CHANNEL_HVAC_SETPOINT_HEAT = "hvac-setPoint-heat";
    public static final String CHANNEL_HVAC_SETPOINT_COOL = "hvac-setPoint-cool";
    public static final String CHANNEL_HVAC_SPEED = "hvac-speed";
    public static final String CHANNEL_VAR1 = "var1";
    public static final String CHANNEL_VAR2 = "var2";
    public static final String CHANNEL_VAR3 = "var3";
    public static final String CHANNEL_VAR4 = "var4";
    public static final String CHANNEL_VAR5 = "var5";
    public static final String CHANNEL_FLOW = "flow";
    public static final String CHANNEL_VOLUME = "volume";
    public static final String CHANNEL_LOCK_STATUS = "lock-status";
    public static final String CHANNEL_LEVEL = "level";
    public static final String CHANNEL_RGB = "rgb";
    public static final String CHANNEL_RGBW = "rgbw";
    public static final String CHANNEL_ID = "id";
    public static final String CHANNEL_UNIT_PREFIX = "unit-prefix";
    public static final String CHANNEL_TEXT = "text";
    public static final String CHANNEL_CUSTOM = "custom";
    public static final String CHANNEL_POSITION = "position";
    public static final String CHANNEL_IR_RECORD = "ir-record";
    public static final String CHANNEL_PH = "ph";
    public static final String CHANNEL_ORP = "orp";
    public static final String CHANNEL_EC = "ec";
    public static final String CHANNEL_VAR = "var";
    public static final String CHANNEL_VA = "va";
    public static final String CHANNEL_POWER_FACTOR = "power-factor";
    public static final String CHANNEL_IR_SEND = "irSend";
    public static final String CHANNEL_IR_RECEIVE = "irReceive";
    public static final String CHANNEL_SCENE_ON = "scene-on";
    public static final String CHANNEL_SCENE_OFF = "scene-off";

    // Extra channel names for non-standard MySensors channels
    public static final String CHANNEL_MYSENSORS_MESSAGE = "mySensorsMessage";
    public static final String CHANNEL_LAST_UPDATE = "lastupdate";

    /**
     * Mapping MySensors message type/subtypes to channels.
     */
    public static final Map<MySensorsMessageSubType, String> CHANNEL_MAP = new HashMap<MySensorsMessageSubType, String>() {
        /**
         *
         */
        private static final long serialVersionUID = -7970323220036599380L;

        {
            put(MySensorsMessageSubType.V_TEMP, CHANNEL_TEMP);
            put(MySensorsMessageSubType.V_HUM, CHANNEL_HUM);
            put(MySensorsMessageSubType.V_STATUS, CHANNEL_STATUS);
            put(MySensorsMessageSubType.V_VOLTAGE, CHANNEL_VOLT);
            put(MySensorsMessageSubType.V_WATT, CHANNEL_WATT);
            put(MySensorsMessageSubType.V_KWH, CHANNEL_KWH);
            put(MySensorsMessageSubType.V_PRESSURE, CHANNEL_PRESSURE);
            put(MySensorsMessageSubType.V_FORECAST, CHANNEL_BARO);
            put(MySensorsMessageSubType.V_TRIPPED, CHANNEL_TRIPPED);
            put(MySensorsMessageSubType.V_ARMED, CHANNEL_ARMED);
            put(MySensorsMessageSubType.V_PERCENTAGE, CHANNEL_PERCENTAGE);
            put(MySensorsMessageSubType.V_UP, CHANNEL_COVER);
            put(MySensorsMessageSubType.V_DOWN, CHANNEL_COVER);
            put(MySensorsMessageSubType.V_STOP, CHANNEL_COVER);
            put(MySensorsMessageSubType.V_WIND, CHANNEL_WIND);
            put(MySensorsMessageSubType.V_GUST, CHANNEL_GUST);
            put(MySensorsMessageSubType.V_RAIN, CHANNEL_RAIN);
            put(MySensorsMessageSubType.V_RAINRATE, CHANNEL_RAINRATE);
            put(MySensorsMessageSubType.V_UV, CHANNEL_UV);
            put(MySensorsMessageSubType.V_WEIGHT, CHANNEL_WEIGHT);
            put(MySensorsMessageSubType.V_IMPEDANCE, CHANNEL_IMPEDANCE);
            put(MySensorsMessageSubType.V_DISTANCE, CHANNEL_DISTANCE);
            put(MySensorsMessageSubType.V_LIGHT_LEVEL, CHANNEL_LIGHT_LEVEL);
            put(MySensorsMessageSubType.V_CURRENT, CHANNEL_CURRENT);
            put(MySensorsMessageSubType.V_HVAC_FLOW_STATE, CHANNEL_HVAC_FLOW_STATE);
            put(MySensorsMessageSubType.V_HVAC_SPEED, CHANNEL_HVAC_SPEED);
            put(MySensorsMessageSubType.V_HVAC_SETPOINT_COOL, CHANNEL_HVAC_SETPOINT_COOL);
            put(MySensorsMessageSubType.V_HVAC_SETPOINT_HEAT, CHANNEL_HVAC_SETPOINT_HEAT);
            put(MySensorsMessageSubType.V_HVAC_FLOW_MODE, CHANNEL_HVAC_FLOW_MODE);
            put(MySensorsMessageSubType.V_VAR1, CHANNEL_VAR1);
            put(MySensorsMessageSubType.V_VAR2, CHANNEL_VAR2);
            put(MySensorsMessageSubType.V_VAR3, CHANNEL_VAR3);
            put(MySensorsMessageSubType.V_VAR4, CHANNEL_VAR4);
            put(MySensorsMessageSubType.V_VAR5, CHANNEL_VAR5);
            put(MySensorsMessageSubType.V_FLOW, CHANNEL_FLOW);
            put(MySensorsMessageSubType.V_VOLUME, CHANNEL_VOLUME);
            put(MySensorsMessageSubType.V_LOCK_STATUS, CHANNEL_LOCK_STATUS);
            put(MySensorsMessageSubType.V_LEVEL, CHANNEL_LEVEL);
            put(MySensorsMessageSubType.V_RGB, CHANNEL_RGB);
            put(MySensorsMessageSubType.V_RGBW, CHANNEL_RGBW);
            put(MySensorsMessageSubType.V_ID, CHANNEL_ID);
            put(MySensorsMessageSubType.V_UNIT_PREFIX, CHANNEL_UNIT_PREFIX);
            put(MySensorsMessageSubType.V_TEXT, CHANNEL_TEXT);
            put(MySensorsMessageSubType.V_CUSTOM, CHANNEL_CUSTOM);
            put(MySensorsMessageSubType.V_POSITION, CHANNEL_POSITION);
            put(MySensorsMessageSubType.V_IR_RECORD, CHANNEL_IR_RECORD);
            put(MySensorsMessageSubType.V_PH, CHANNEL_PH);
            put(MySensorsMessageSubType.V_ORP, CHANNEL_ORP);
            put(MySensorsMessageSubType.V_EC, CHANNEL_EC);
            put(MySensorsMessageSubType.V_VAR, CHANNEL_VAR);
            put(MySensorsMessageSubType.V_VA, CHANNEL_VA);
            put(MySensorsMessageSubType.V_POWER_FACTOR, CHANNEL_POWER_FACTOR);
            put(MySensorsMessageSubType.V_TEXT, CHANNEL_TEXT);
            put(MySensorsMessageSubType.V_IR_SEND, CHANNEL_IR_SEND);
            put(MySensorsMessageSubType.V_IR_RECEIVE, CHANNEL_IR_RECEIVE);
            put(MySensorsMessageSubType.V_SCENE_ON, CHANNEL_SCENE_ON);
            put(MySensorsMessageSubType.V_SCENE_OFF, CHANNEL_SCENE_OFF);
        }
    };

    /**
     * Inverse of the CHANNEL_MAP, duplicate allowed (see also Converters here below)
     */
    public static final Map<String, MySensorsMessageSubType> INVERSE_CHANNEL_MAP = MySensorsUtility.invertMap(CHANNEL_MAP, true);

    /**
     * Converters will be used to map values from OH to/from MySensors Variables
     */
    public static final MySensorsDecimalTypeConverter DECIMAL_TYPE_CONVERTER = new MySensorsDecimalTypeConverter();
    public static final MySensorsPercentTypeConverter PERCENT_TYPE_CONVERTER = new MySensorsPercentTypeConverter();
    public static final MySensorsOnOffTypeConverter ONOFF_TYPE_CONVERTER = new MySensorsOnOffTypeConverter();
    public static final MySensorsOpenCloseTypeConverter OPENCLOSE_TYPE_CONVERTER = new MySensorsOpenCloseTypeConverter();
    public static final MySensorsUpDownTypeConverter UPDOWN_TYPE_CONVERTER = new MySensorsUpDownTypeConverter();
    public static final MySensorsStringTypeConverter STRING_TYPE_CONVERTER = new MySensorsStringTypeConverter();
    public static final MySensorsRGBTypeConverter RGB_TYPE_CONVERTER = new MySensorsRGBTypeConverter();
    public static final MySensorsRGBWTypeConverter RGBW_TYPE_CONVERTER = new MySensorsRGBWTypeConverter();

    /**
     * Mappings between ChannelUID and class that represents the type of the channel
     */
    public static final Map<String, MySensorsTypeConverter> TYPE_MAP = new HashMap<String, MySensorsTypeConverter>() {

        /**
         *
         */
        private static final long serialVersionUID = 6273187523631143905L;
        {
            put(CHANNEL_TEMP, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_HUM, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_STATUS, ONOFF_TYPE_CONVERTER);
            put(CHANNEL_VOLT, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_WATT, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_KWH, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_PRESSURE, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_BARO, STRING_TYPE_CONVERTER);
            put(CHANNEL_TRIPPED, OPENCLOSE_TYPE_CONVERTER);
            put(CHANNEL_ARMED, ONOFF_TYPE_CONVERTER);
            put(CHANNEL_PERCENTAGE, PERCENT_TYPE_CONVERTER);
            put(CHANNEL_COVER, UPDOWN_TYPE_CONVERTER);
            put(CHANNEL_COVER, UPDOWN_TYPE_CONVERTER); // !
            put(CHANNEL_COVER, UPDOWN_TYPE_CONVERTER); // !
            put(CHANNEL_WIND, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_GUST, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_RAIN, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_RAINRATE, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_UV, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_WEIGHT, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_IMPEDANCE, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_DISTANCE, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_LIGHT_LEVEL, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_CURRENT, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_HVAC_FLOW_STATE, STRING_TYPE_CONVERTER);
            put(CHANNEL_HVAC_SPEED, STRING_TYPE_CONVERTER);
            put(CHANNEL_HVAC_SETPOINT_COOL, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_HVAC_SETPOINT_HEAT, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_HVAC_FLOW_MODE, STRING_TYPE_CONVERTER);
            put(CHANNEL_VAR1, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VAR2, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VAR3, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VAR4, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VAR5, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_FLOW, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VOLUME, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_LOCK_STATUS, ONOFF_TYPE_CONVERTER);
            put(CHANNEL_LEVEL, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_RGB, RGB_TYPE_CONVERTER);
            put(CHANNEL_RGBW, RGBW_TYPE_CONVERTER);
            put(CHANNEL_ID, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_UNIT_PREFIX, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_TEXT, STRING_TYPE_CONVERTER);
            put(CHANNEL_CUSTOM, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_POSITION, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_IR_RECORD, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_PH, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_ORP, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_EC, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VAR, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_VA, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_POWER_FACTOR, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_TEXT, STRING_TYPE_CONVERTER);
            put(CHANNEL_IR_SEND, STRING_TYPE_CONVERTER);
            put(CHANNEL_IR_RECEIVE, STRING_TYPE_CONVERTER);
            put(CHANNEL_SCENE_ON, DECIMAL_TYPE_CONVERTER);
            put(CHANNEL_SCENE_OFF, DECIMAL_TYPE_CONVERTER);

            // Internal
            put(CHANNEL_VERSION, STRING_TYPE_CONVERTER);
            put(CHANNEL_BATTERY, DECIMAL_TYPE_CONVERTER);
        }
    };

    /**
     * Used in DiscoveryService to map subtype of a presentation message to thing type
     */
    public static final Map<MySensorsMessageSubType, ThingTypeUID> THING_UID_MAP = new HashMap<MySensorsMessageSubType, ThingTypeUID>() {

        /**
         *
         */
        private static final long serialVersionUID = -2042537863671385026L;
        {
            put(MySensorsMessageSubType.S_HUM, THING_TYPE_HUMIDITY);
            put(MySensorsMessageSubType.S_TEMP, THING_TYPE_TEMPERATURE);
            put(MySensorsMessageSubType.S_MULTIMETER, THING_TYPE_MULTIMETER);
            put(MySensorsMessageSubType.S_BINARY, THING_TYPE_BINARY);
            put(MySensorsMessageSubType.S_POWER, THING_TYPE_POWER);
            put(MySensorsMessageSubType.S_BARO, THING_TYPE_BARO);
            put(MySensorsMessageSubType.S_DOOR, THING_TYPE_DOOR);
            put(MySensorsMessageSubType.S_MOTION, THING_TYPE_MOTION);
            put(MySensorsMessageSubType.S_SMOKE, THING_TYPE_SMOKE);
            put(MySensorsMessageSubType.S_DIMMER, THING_TYPE_DIMMER);
            put(MySensorsMessageSubType.S_COVER, THING_TYPE_COVER);
            put(MySensorsMessageSubType.S_WIND, THING_TYPE_WIND);
            put(MySensorsMessageSubType.S_RAIN, THING_TYPE_RAIN);
            put(MySensorsMessageSubType.S_UV, THING_TYPE_UV);
            put(MySensorsMessageSubType.S_WEIGHT, THING_TYPE_WEIGHT);
            put(MySensorsMessageSubType.S_DISTANCE, THING_TYPE_DISTANCE);
            put(MySensorsMessageSubType.S_LIGHT_LEVEL, THING_TYPE_LIGHT_LEVEL);
            put(MySensorsMessageSubType.S_WATER, THING_TYPE_WATER);
            put(MySensorsMessageSubType.S_CUSTOM, THING_TYPE_CUSTOM);
            put(MySensorsMessageSubType.S_HVAC, THING_TYPE_HVAC);
            put(MySensorsMessageSubType.S_LOCK, THING_TYPE_LOCK);
            put(MySensorsMessageSubType.S_SOUND, THING_TYPE_SOUND);
            put(MySensorsMessageSubType.S_RGB_LIGHT, THING_TYPE_RGB_LIGHT);
            put(MySensorsMessageSubType.S_RGBW_LIGHT, THING_TYPE_RGBW_LIGHT);
            put(MySensorsMessageSubType.S_WATER_QUALITY, THING_TYPE_WATER_QUALITY);
            put(MySensorsMessageSubType.S_INFO, THING_TYPE_TEXT);
            put(MySensorsMessageSubType.S_IR, THING_TYPE_IR);
            put(MySensorsMessageSubType.S_AIR_QUALITY, THING_TYPE_AIR_QUALITY);
            put(MySensorsMessageSubType.S_DUST, THING_TYPE_DUST);
            put(MySensorsMessageSubType.S_COLOR_SENSOR, THING_TYPE_COLOR_SENSOR);
            put(MySensorsMessageSubType.S_MOISTURE, THING_TYPE_MOISTURE);
            put(MySensorsMessageSubType.S_SPRINKLER, THING_TYPE_SPRINKLER);
            put(MySensorsMessageSubType.S_HEATER, THING_TYPE_HEATER);
            put(MySensorsMessageSubType.S_VIBRATION, THING_TYPE_VIBRATION);
            put(MySensorsMessageSubType.S_WATER_LEAK, THING_TYPE_WATER_LEAK);
            put(MySensorsMessageSubType.S_GAS, THING_TYPE_GAS);
            put(MySensorsMessageSubType.S_GPS, THING_TYPE_GPS);
            put(MySensorsMessageSubType.S_SCENE_CONTROLLER, THING_TYPE_SCENE_CONTROLLER);
        }

    };

    /**
     * Inverse of the THING_UID_MAP, helps on building child for every thing type
     */
    public static final Map<ThingTypeUID, MySensorsMessageSubType> INVERSE_THING_UID_MAP = MySensorsUtility.invertMap(THING_UID_MAP,
            true);

    /** Supported Things without bridge */
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_HUMIDITY,
            THING_TYPE_TEMPERATURE, THING_TYPE_BINARY, THING_TYPE_MULTIMETER, THING_TYPE_POWER, THING_TYPE_BARO,
            THING_TYPE_DOOR, THING_TYPE_MOTION, THING_TYPE_SMOKE, THING_TYPE_DIMMER, THING_TYPE_COVER, THING_TYPE_WIND,
            THING_TYPE_RAIN, THING_TYPE_UV, THING_TYPE_WEIGHT, THING_TYPE_DISTANCE, THING_TYPE_LIGHT_LEVEL,
            THING_TYPE_HVAC, THING_TYPE_WATER, THING_TYPE_CUSTOM, THING_TYPE_LOCK, THING_TYPE_SOUND,
            THING_TYPE_RGB_LIGHT, THING_TYPE_RGBW_LIGHT, THING_TYPE_WATER_QUALITY, THING_TYPE_MYSENSORS_MESSAGE,
            THING_TYPE_TEXT, THING_TYPE_IR, THING_TYPE_AIR_QUALITY, THING_TYPE_DUST, THING_TYPE_COLOR_SENSOR,
            THING_TYPE_MOISTURE, THING_TYPE_SPRINKLER, THING_TYPE_HEATER, THING_TYPE_VIBRATION, THING_TYPE_WATER_LEAK,
            THING_TYPE_GAS, THING_TYPE_GPS, THING_TYPE_SCENE_CONTROLLER);
    /** Supported bridges */
    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = ImmutableSet.of(THING_TYPE_BRIDGE_SER,
            THING_TYPE_BRIDGE_ETH, THING_TYPE_BRIDGE_MQTT);

    /** Supported devices (things + bridges) */
    public static final Collection<ThingTypeUID> SUPPORTED_DEVICE_TYPES_UIDS = Lists.newArrayList(THING_TYPE_HUMIDITY,
            THING_TYPE_TEMPERATURE, THING_TYPE_BINARY, THING_TYPE_MULTIMETER, THING_TYPE_POWER, THING_TYPE_BARO,
            THING_TYPE_DOOR, THING_TYPE_MOTION, THING_TYPE_SMOKE, THING_TYPE_DIMMER, THING_TYPE_COVER, THING_TYPE_WIND,
            THING_TYPE_RAIN, THING_TYPE_UV, THING_TYPE_WEIGHT, THING_TYPE_DISTANCE, THING_TYPE_LIGHT_LEVEL,
            THING_TYPE_HVAC, THING_TYPE_WATER, THING_TYPE_CUSTOM, THING_TYPE_LOCK, THING_TYPE_SOUND,
            THING_TYPE_RGB_LIGHT, THING_TYPE_RGBW_LIGHT, THING_TYPE_WATER_QUALITY, THING_TYPE_MYSENSORS_MESSAGE,
            THING_TYPE_TEXT, THING_TYPE_IR, THING_TYPE_AIR_QUALITY, THING_TYPE_DUST, THING_TYPE_COLOR_SENSOR,
            THING_TYPE_MOISTURE,THING_TYPE_SPRINKLER, THING_TYPE_HEATER, THING_TYPE_VIBRATION, THING_TYPE_WATER_LEAK,
            THING_TYPE_GAS, THING_TYPE_GPS, THING_TYPE_SCENE_CONTROLLER, 
            THING_TYPE_BRIDGE_SER, THING_TYPE_BRIDGE_ETH, THING_TYPE_BRIDGE_MQTT);
}
