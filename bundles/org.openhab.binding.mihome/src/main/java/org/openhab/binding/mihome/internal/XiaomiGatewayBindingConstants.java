/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.mihome.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link XiaomiGatewayBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Patrick Boos - Initial contribution
 * @author Dieter Schmidt - added cube, gateway sound channels, window sensor alarm
 * @author Daniel Walters - Added Aqara Door/Window sensor and Aqara temperature, humidity and pressure sensor
 * @author Kuba Wolanin - Added Water Leak sensor
 */
@NonNullByDefault
public class XiaomiGatewayBindingConstants {

    public static final String BINDING_ID = "mihome";

    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");
    public static final ThingTypeUID THING_TYPE_BASIC = new ThingTypeUID(BINDING_ID, "basic");
    // sensors
    public static final ThingTypeUID THING_TYPE_SENSOR_HT = new ThingTypeUID(BINDING_ID, "sensor_ht");
    public static final ThingTypeUID THING_TYPE_SENSOR_AQARA_WEATHER_V1 = new ThingTypeUID(BINDING_ID,
            "sensor_weather_v1");
    public static final ThingTypeUID THING_TYPE_SENSOR_MOTION = new ThingTypeUID(BINDING_ID, "sensor_motion");
    public static final ThingTypeUID THING_TYPE_SENSOR_AQARA_MOTION = new ThingTypeUID(BINDING_ID, "sensor_motion_aq2");
    public static final ThingTypeUID THING_TYPE_SENSOR_SWITCH = new ThingTypeUID(BINDING_ID, "sensor_switch");
    public static final ThingTypeUID THING_TYPE_SENSOR_AQARA_SWITCH = new ThingTypeUID(BINDING_ID, "sensor_switch_aq2");
    public static final ThingTypeUID THING_TYPE_SENSOR_MAGNET = new ThingTypeUID(BINDING_ID, "sensor_magnet");
    public static final ThingTypeUID THING_TYPE_SENSOR_AQARA_MAGNET = new ThingTypeUID(BINDING_ID, "sensor_magnet_aq2");
    public static final ThingTypeUID THING_TYPE_SENSOR_AQARA_LOCK = new ThingTypeUID(BINDING_ID, "sensor_lock_aq1");
    public static final ThingTypeUID THING_TYPE_SENSOR_CUBE = new ThingTypeUID(BINDING_ID, "sensor_cube");
    public static final ThingTypeUID THING_TYPE_SENSOR_AQARA_VIBRATION = new ThingTypeUID(BINDING_ID,
            "sensor_vibration");
    public static final ThingTypeUID THING_TYPE_SENSOR_AQARA1 = new ThingTypeUID(BINDING_ID, "86sw1");
    public static final ThingTypeUID THING_TYPE_SENSOR_AQARA2 = new ThingTypeUID(BINDING_ID, "86sw2");
    public static final ThingTypeUID THING_TYPE_SENSOR_GAS = new ThingTypeUID(BINDING_ID, "natgas");
    public static final ThingTypeUID THING_TYPE_SENSOR_SMOKE = new ThingTypeUID(BINDING_ID, "smoke");
    public static final ThingTypeUID THING_TYPE_SENSOR_WATER = new ThingTypeUID(BINDING_ID, "sensor_wleak_aq1");

    // actors
    public static final ThingTypeUID THING_TYPE_ACTOR_PLUG = new ThingTypeUID(BINDING_ID, "sensor_plug");
    public static final ThingTypeUID THING_TYPE_ACTOR_AQARA1 = new ThingTypeUID(BINDING_ID, "ctrl_neutral1");
    public static final ThingTypeUID THING_TYPE_ACTOR_AQARA2 = new ThingTypeUID(BINDING_ID, "ctrl_neutral2");
    public static final ThingTypeUID THING_TYPE_ACTOR_AQARA_ZERO1 = new ThingTypeUID(BINDING_ID, "ctrl_ln1");
    public static final ThingTypeUID THING_TYPE_ACTOR_AQARA_ZERO2 = new ThingTypeUID(BINDING_ID, "ctrl_ln2");
    public static final ThingTypeUID THING_TYPE_ACTOR_CURTAIN = new ThingTypeUID(BINDING_ID, "curtain");

    // List of all Channel ids
    public static final String CHANNEL_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_LOW_BATTERY = "lowBattery";
    // HT sensor
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_PRESSURE = "pressure";
    // motion sensor
    public static final String CHANNEL_MOTION = "motion";
    public static final String CHANNEL_MOTION_OFF_TIMER = "motionOffTimer";
    public static final String CHANNEL_LAST_MOTION = "lastMotion";
    // window sensor
    public static final String CHANNEL_IS_OPEN = "isOpen";
    public static final String CHANNEL_LAST_OPENED = "lastOpened";
    public static final String CHANNEL_OPEN_ALARM = "isOpenAlarm";
    public static final String CHANNEL_OPEN_ALARM_TIMER = "isOpenAlarmTimer";
    // plug
    public static final String CHANNEL_POWER_ON = "power";
    public static final String CHANNEL_IN_USE = "inUse";
    public static final String CHANNEL_LOAD_POWER = "loadPower";
    public static final String CHANNEL_POWER_CONSUMED = "powerConsumed";
    // switch
    public static final String CHANNEL_BUTTON = "button";
    // cube
    public static final String CHANNEL_ACTION = "action";
    public static final String CHANNEL_LAST_ACTION = "lastAction";
    public static final String CHANNEL_CUBE_ROTATION_ANGLE = "rotationAngle";
    public static final String CHANNEL_CUBE_ROTATION_TIME = "rotationTime";
    // vibration
    public static final String CHANNEL_TILT_ANGLE = "tiltAngle";
    public static final String CHANNEL_ORIENTATION_X = "orientationX";
    public static final String CHANNEL_ORIENTATION_Y = "orientationY";
    public static final String CHANNEL_ORIENTATION_Z = "orientationZ";
    public static final String CHANNEL_BED_ACTIVITY = "bedActivity";
    // gateway sound
    public static final String CHANNEL_GATEWAY_SOUND_SWITCH = "enableSound";
    public static final String CHANNEL_GATEWAY_SOUND = "sound";
    public static final String CHANNEL_GATEWAY_VOLUME = "volume";
    // gateway light
    public static final String CHANNEL_BRIGHTNESS = "brightness";
    public static final String CHANNEL_ILLUMINATION = "illumination";
    public static final String CHANNEL_COLOR = "color";
    public static final String CHANNEL_COLOR_TEMPERATURE = "colorTemperature";
    // aqara switches
    public static final String CHANNEL_SWITCH_CH0 = "ch1";
    public static final String CHANNEL_SWITCH_CH1 = "ch2";
    public static final String CHANNEL_SWITCH_DUAL_CH = "dual_ch";
    // curtain
    public static final String CHANNEL_CURTAIN_CONTROL = "curtainControl";
    // gas & smoke sensor
    public static final String CHANNEL_ALARM = "alarm";
    public static final String CHANNEL_STATUS = "status";
    // smoke sensor
    public static final String CHANNEL_DENSITY = "density";
    // water leak sensor
    public static final String CHANNEL_LEAK = "leak";
    // aqara lock
    public static final String CHANNEL_ID = "id";
    public static final String CHANNEL_WRONG_ACCESS = "wrongAccess";
    // Bridge config properties
    public static final String SERIAL_NUMBER = "serialNumber";
    public static final String HOST = "ipAddress";
    public static final String PORT = "port";
    public static final String INTERFACE = "interface";
    public static final String TOKEN = "token";

    // Item config properties
    public static final String ITEM_ID = "itemId";

    // Basic Device channels
    public static final String CHANNEL_REPORT_MSG = "reportMessage";
    public static final String CHANNEL_HEARTBEAT_MSG = "heartbeatMessage";
    public static final String CHANNEL_READ_ACK_MSG = "readAckMessage";
    public static final String CHANNEL_WRITE_ACK_MSG = "writeAckMessage";
    public static final String CHANNEL_LAST_MSG = "lastMessage";
    public static final String CHANNEL_WRITE_MSG = "writeMessage";
}
