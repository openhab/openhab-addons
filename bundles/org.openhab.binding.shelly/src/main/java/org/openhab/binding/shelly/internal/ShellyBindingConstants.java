/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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
package org.openhab.binding.shelly.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * The {@link ShellyBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class ShellyBindingConstants {

    public static final String VENDOR = "Shelly";
    public static final String BINDING_ID = "shelly";
    public static final String SYSTEM_ID = "system";

    // Thing Configuration Properties
    public static final String CONFIG_DEVICEIP = "deviceIp";
    public static final String CONFIG_DEVICEADDRESS = "deviceAddress";
    public static final String CONFIG_HTTP_USERID = "userId";
    public static final String CONFIG_HTTP_PASSWORD = "password";
    public static final String CONFIG_UPDATE_INTERVAL = "updateInterval";

    public static final String PROPERTY_SERVICE_NAME = "serviceName";
    public static final String PROPERTY_DEV_NAME = "deviceName";
    public static final String PROPERTY_DEV_TYPE = "deviceType";
    public static final String PROPERTY_DEV_MODE = "deviceMode";
    public static final String PROPERTY_DEV_GEN = "deviceGeneration";
    public static final String PROPERTY_DEV_AUTH = "deviceAuth";
    public static final String PROPERTY_GW_DEVICE = "gatewayDevice";
    public static final String PROPERTY_HWREV = "deviceHwRev";
    public static final String PROPERTY_HWBATCH = "deviceHwBatch";
    public static final String PROPERTY_UPDATE_PERIOD = "devUpdatePeriod";
    public static final String PROPERTY_NUM_RELAYS = "numberRelays";
    public static final String PROPERTY_NUM_ROLLERS = "numberRollers";
    public static final String PROPERTY_NUM_METER = "numberMeters";
    public static final String PROPERTY_LAST_ACTIVE = "lastActive";
    public static final String PROPERTY_WIFI_NETW = "wifiNetwork";
    public static final String PROPERTY_UPDATE_STATUS = "updateStatus";
    public static final String PROPERTY_UPDATE_AVAILABLE = "updateAvailable";
    public static final String PROPERTY_UPDATE_CURR_VERS = "updateCurrentVersion";
    public static final String PROPERTY_UPDATE_NEW_VERS = "updateNewVersion";
    public static final String PROPERTY_COAP_DESCR = "coapDeviceDescr";
    public static final String PROPERTY_COAP_VERSION = "coapVersion";
    public static final String PROPERTY_COIOTAUTO = "coiotAutoEnable";

    // Relay
    public static final String CHANNEL_GROUP_RELAY_CONTROL = "relay";
    public static final String CHANNEL_OUTPUT_NAME = "outputName";
    public static final String CHANNEL_OUTPUT = "output";
    public static final String CHANNEL_INPUT = "input";
    public static final String CHANNEL_INPUT1 = "input1";
    public static final String CHANNEL_INPUT2 = "input2";
    public static final String CHANNEL_BRIGHTNESS = "brightness";

    public static final String CHANNEL_TIMER_AUTOON = "autoOn";
    public static final String CHANNEL_TIMER_AUTOOFF = "autoOff";
    public static final String CHANNEL_TIMER_ACTIVE = "timerActive";

    // Roller
    public static final String CHANNEL_GROUP_ROL_CONTROL = "roller";
    public static final String CHANNEL_ROL_CONTROL_CONTROL = "control";
    public static final String CHANNEL_ROL_CONTROL_POS = "rollerpos";
    public static final String CHANNEL_ROL_CONTROL_FAV = "rollerFav";
    public static final String CHANNEL_ROL_CONTROL_TIMER = "timer";
    public static final String CHANNEL_ROL_CONTROL_STATE = "state";
    public static final String CHANNEL_ROL_CONTROL_STOPR = "stopReason";
    public static final String CHANNEL_ROL_CONTROL_SAFETY = "safety";

    // Dimmer
    public static final String CHANNEL_GROUP_DIMMER_CONTROL = CHANNEL_GROUP_RELAY_CONTROL;

    // Power meter
    public static final String CHANNEL_GROUP_METER = "meter";
    public static final String CHANNEL_METER_CURRENTWATTS = "currentWatts";
    public static final String CHANNEL_METER_LASTMIN = "lastPower";
    public static final String CHANNEL_METER_LASTMIN1 = CHANNEL_METER_LASTMIN + "1";
    public static final String CHANNEL_METER_TOTALKWH = "totalKWH";
    public static final String CHANNEL_EMETER_TOTALRET = "returnedKWH";
    public static final String CHANNEL_EMETER_REACTWATTS = "reactiveWatts";
    public static final String CHANNEL_EMETER_VOLTAGE = "voltage";
    public static final String CHANNEL_EMETER_CURRENT = "current";
    public static final String CHANNEL_EMETER_FREQUENCY = "frequency";
    public static final String CHANNEL_EMETER_PFACTOR = "powerFactor";
    public static final String CHANNEL_EMETER_RESETTOTAL = "resetTotals";
    public static final String CHANNEL_GROUP_NMETER = "nmeter";
    public static final String CHANNEL_NMETER_CURRENT = "ncurrent";
    public static final String CHANNEL_NMETER_IXSUM = "ixsum";
    public static final String CHANNEL_NMETER_MISMATCH = "nmismatch";
    public static final String CHANNEL_NMETER_MTRESHHOLD = "nmTreshhold";

    public static final String CHANNEL_GROUP_SENSOR = "sensors";
    public static final String CHANNEL_SENSOR_TEMP = "temperature";
    public static final String CHANNEL_SENSOR_HUM = "humidity";
    public static final String CHANNEL_SENSOR_LUX = "lux";
    public static final String CHANNEL_SENSOR_PPM = "ppm";
    public static final String CHANNEL_SENSOR_VOLTAGE = "voltage";
    public static final String CHANNEL_SENSOR_ILLUM = "illumination";
    public static final String CHANNEL_SENSOR_VIBRATION = "vibration";
    public static final String CHANNEL_SENSOR_TILT = "tilt";
    public static final String CHANNEL_SENSOR_FLOOD = "flood";
    public static final String CHANNEL_SENSOR_SMOKE = "smoke";
    public static final String CHANNEL_SENSOR_MUTE = "mute";
    public static final String CHANNEL_SENSOR_STATE = "state";
    public static final String CHANNEL_SENSOR_VALVE = "valve";
    public static final String CHANNEL_SENSOR_SSTATE = "status"; // Shelly Gas
    public static final String CHANNEL_SENSOR_MOTION_ACT = "motionActive";
    public static final String CHANNEL_SENSOR_MOTION = "motion";
    public static final String CHANNEL_SENSOR_MOTION_TS = "motionTimestamp";
    public static final String CHANNEL_SENSOR_SLEEPTIME = "sensorSleepTime";
    public static final String CHANNEL_SENSOR_ALARM_STATE = "alarmState";
    public static final String CHANNEL_SENSOR_ERROR = "lastError";

    // TRV
    public static final String CHANNEL_CONTROL_SETTEMP = "targetTemp";
    public static final String CHANNEL_CONTROL_POSITION = "position";
    public static final String CHANNEL_CONTROL_MODE = "mode";
    public static final String CHANNEL_CONTROL_BCONTROL = "boost";
    public static final String CHANNEL_CONTROL_BTIMER = "boostTimer";
    public static final String CHANNEL_CONTROL_SCHEDULE = "schedule";
    public static final String CHANNEL_CONTROL_PROFILE = "selectedProfile";

    // External sensors for Shelly1/1PM
    public static final String CHANNEL_ESENSOR_TEMP1 = CHANNEL_SENSOR_TEMP + "1";
    public static final String CHANNEL_ESENSOR_TEMP2 = CHANNEL_SENSOR_TEMP + "2";
    public static final String CHANNEL_ESENSOR_TEMP3 = CHANNEL_SENSOR_TEMP + "3";
    public static final String CHANNEL_ESENSOR_TEMP4 = CHANNEL_SENSOR_TEMP + "4";
    public static final String CHANNEL_ESENSOR_TEMP5 = CHANNEL_SENSOR_TEMP + "5";
    public static final String CHANNEL_ESENSOR_HUMIDITY = CHANNEL_SENSOR_HUM;
    public static final String CHANNEL_ESENSOR_VOLTAGE = CHANNEL_SENSOR_VOLTAGE;
    public static final String CHANNEL_ESENSOR_DIGITALINPUT = "digitalInput";
    public static final String CHANNEL_ESENSOR_ANALOGINPUT = "analogInput";
    public static final String CHANNEL_ESENSOR_INPUT = "input";
    public static final String CHANNEL_ESENSOR_INPUT1 = CHANNEL_ESENSOR_INPUT + "1";

    public static final String CHANNEL_GROUP_CONTROL = "control";
    public static final String CHANNEL_SENSE_KEY = "key";

    public static final String CHANNEL_GROUP_BATTERY = "battery";
    public static final String CHANNEL_SENSOR_BAT_LEVEL = "batteryLevel";
    public static final String CHANNEL_SENSOR_BAT_LOW = "lowBattery";

    public static final String CHANNEL_GROUP_LIGHT_CONTROL = "control";
    public static final String CHANNEL_LIGHT_COLOR_MODE = "mode";
    public static final String CHANNEL_LIGHT_POWER = "power";
    public static final String CHANNEL_LIGHT_DEFSTATE = "defaultState";
    public static final String CHANNEL_GROUP_LIGHT_CHANNEL = "channel";

    // Bulb/RGBW2 in color mode
    public static final String CHANNEL_GROUP_COLOR_CONTROL = "color";
    public static final String CHANNEL_COLOR_PICKER = "hsb";
    public static final String CHANNEL_COLOR_FULL = "full";
    public static final String CHANNEL_COLOR_RED = "red";
    public static final String CHANNEL_COLOR_GREEN = "green";
    public static final String CHANNEL_COLOR_BLUE = "blue";
    public static final String CHANNEL_COLOR_WHITE = "white";
    public static final String CHANNEL_COLOR_GAIN = "gain";
    public static final String CHANNEL_COLOR_EFFECT = "effect";

    // Bulb/RGBW2/Dup in White Mode
    public static final String CHANNEL_GROUP_WHITE_CONTROL = "white";
    public static final String CHANNEL_COLOR_TEMP = "temperature";

    // Device Status
    public static final String CHANNEL_GROUP_DEV_STATUS = "device";
    public static final String CHANNEL_DEVST_NAME = "deviceName";
    public static final String CHANNEL_DEVST_GATEWAY = "gatewayDevice";
    public static final String CHANNEL_DEVST_UPTIME = "uptime";
    public static final String CHANNEL_DEVST_HEARTBEAT = "heartBeat";
    public static final String CHANNEL_DEVST_RSSI = "wifiSignal";
    public static final String CHANNEL_DEVST_ITEMP = "internalTemp";
    public static final String CHANNEL_DEVST_WAKEUP = "wakeupReason";
    public static final String CHANNEL_DEVST_ALARM = "alarm";
    public static final String CHANNEL_DEVST_ACCUWATTS = "accumulatedWatts";
    public static final String CHANNEL_DEVST_ACCUTOTAL = "accumulatedWTotal";
    public static final String CHANNEL_DEVST_ACCURETURNED = "accumulatedReturned";
    public static final String CHANNEL_DEVST_TOTALKWH = "totalKWH";
    public static final String CHANNEL_DEVST_RESETTOTAL = CHANNEL_EMETER_RESETTOTAL;

    public static final String CHANNEL_DEVST_CHARGER = "charger";
    public static final String CHANNEL_DEVST_UPDATE = "updateAvailable";
    public static final String CHANNEL_DEVST_SELFTTEST = "selfTest";
    public static final String CHANNEL_DEVST_VOLTAGE = "supplyVoltage";
    public static final String CHANNEL_DEVST_CALIBRATED = "calibrated";

    public static final String CHANNEL_LED_STATUS_DISABLE = "statusLed";
    public static final String CHANNEL_LED_POWER_DISABLE = "powerLed";
    // Button/xi3
    public static final String CHANNEL_GROUP_STATUS = "status";
    public static final String CHANNEL_STATUS_EVENTTYPE = "lastEvent";
    public static final String CHANNEL_STATUS_EVENTTYPE1 = CHANNEL_STATUS_EVENTTYPE + "1";
    public static final String CHANNEL_STATUS_EVENTTYPE2 = CHANNEL_STATUS_EVENTTYPE + "2";
    public static final String CHANNEL_STATUS_EVENTCOUNT = "eventCount";
    public static final String CHANNEL_STATUS_EVENTCOUNT1 = CHANNEL_STATUS_EVENTCOUNT + "1";
    public static final String CHANNEL_STATUS_EVENTCOUNT2 = CHANNEL_STATUS_EVENTCOUNT + "2";

    // General
    public static final String CHANNEL_LAST_UPDATE = "lastUpdate";
    public static final String CHANNEL_EVENT_TRIGGER = "event";
    public static final String CHANNEL_BUTTON_TRIGGER = "button";
    public static final String CHANNEL_BUTTON_TRIGGER1 = CHANNEL_BUTTON_TRIGGER + "1";
    public static final String CHANNEL_BUTTON_TRIGGER2 = CHANNEL_BUTTON_TRIGGER + "2";

    public static final String SHELLY_API_MIN_FWVERSION = "v1.8.2";
    public static final String SHELLY_API_MIN_FWCOIOT = "v1.6";// v1.6.0+
    public static final String SHELLY_API_FWCOIOT2 = "v1.8";// CoAP 2 with FW 1.8+
    public static final String SHELLY_API_FW_110 = "v1.10"; // FW 1.10 or newer detected, activates some add feature
    public static final String SHELLY2_API_MIN_FWVERSION = "v0.10.1"; // Gen 2 minimum FW

    // Alarm types/messages
    public static final String ALARM_TYPE_NONE = "NONE";
    public static final String ALARM_TYPE_RESTARTED = "RESTARTED";
    public static final String ALARM_TYPE_OVERTEMP = "OVERTEMP";
    public static final String ALARM_TYPE_OVERPOWER = "OVERPOWER";
    public static final String ALARM_TYPE_OVERLOAD = "OVERLOAD";
    public static final String ALARM_TYPE_LOADERR = "LOAD_ERROR";
    public static final String ALARM_TYPE_SENSOR_ERROR = "SENSOR_ERROR";
    public static final String ALARM_TYPE_LOW_BATTERY = "LOW_BATTERY";
    public static final String ALARM_TYPE_VALVE_ERROR = "VALVE_ERROR";
    public static final String EVENT_TYPE_VIBRATION = "VIBRATION";

    // Event types
    public static final String EVENT_TYPE_RELAY = "relay";
    public static final String EVENT_TYPE_ROLLER = "roller";
    public static final String EVENT_TYPE_LIGHT = "light";
    public static final String EVENT_TYPE_SENSORDATA = "report";

    // URI for the EventServlet
    public static final String SHELLY1_CALLBACK_URI = "/shelly/event";
    public static final String SHELLY2_CALLBACK_URI = "/shelly/wsevent";

    public static final int DIM_STEPSIZE = 5;

    // Formatting: Number of scaling digits
    public static final int DIGITS_NONE = 0;
    public static final int DIGITS_WATT = 2;
    public static final int DIGITS_KWH = 3;
    public static final int DIGITS_VOLT = 1;
    public static final int DIGITS_AMPERE = 3;
    public static final int DIGITS_FREQUENCY = 1;
    public static final int DIGITS_TEMP = 1;
    public static final int DIGITS_LUX = 0;
    public static final int DIGITS_PERCENT = 1;

    public static final int SHELLY_API_TIMEOUT_MS = 10000;
    public static final int UPDATE_STATUS_INTERVAL_SECONDS = 3; // check for updates every x sec
    public static final int UPDATE_SKIP_COUNT = 20; // update every x triggers or when a key was pressed
    public static final int UPDATE_MIN_DELAY = 15;// update every x triggers or when a key was pressed
    public static final int UPDATE_SETTINGS_INTERVAL_SECONDS = 60; // check for updates every x sec
    public static final int HEALTH_CHECK_INTERVAL_SEC = 300; // Health check interval, 5min
    public static final int VIBRATION_FILTER_SEC = 5; // Absorb duplicate vibration events for xx sec

    public static final String BUNDLE_RESOURCE_SNIPLETS = "sniplets"; // where to find code sniplets in the bundle
    public static final String BUNDLE_RESOURCE_SCRIPTS = "scripts"; // where to find scrips in the bundle
}
