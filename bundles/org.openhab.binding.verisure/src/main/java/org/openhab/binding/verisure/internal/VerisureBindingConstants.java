/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.verisure.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link VerisureBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author l3rum - Initial contribution
 * @author Jan Gustafsson - Furher development
 */
@NonNullByDefault
public class VerisureBindingConstants {

    public static final String BINDING_ID = "verisure";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "bridge");
    public static final ThingTypeUID THING_TYPE_ALARM = new ThingTypeUID(BINDING_ID, "alarm");
    public static final ThingTypeUID THING_TYPE_SMARTPLUG = new ThingTypeUID(BINDING_ID, "smartPlug");
    public static final ThingTypeUID THING_TYPE_SMOKEDETECTOR = new ThingTypeUID(BINDING_ID, "smokeDetector");
    public static final ThingTypeUID THING_TYPE_WATERDETECTOR = new ThingTypeUID(BINDING_ID, "waterDetector");
    public static final ThingTypeUID THING_TYPE_SIREN = new ThingTypeUID(BINDING_ID, "siren");
    public static final ThingTypeUID THING_TYPE_DOORWINDOW = new ThingTypeUID(BINDING_ID, "doorWindowSensor");
    public static final ThingTypeUID THING_TYPE_USERPRESENCE = new ThingTypeUID(BINDING_ID, "userPresence");
    public static final ThingTypeUID THING_TYPE_SMARTLOCK = new ThingTypeUID(BINDING_ID, "smartLock");
    public static final ThingTypeUID THING_TYPE_BROADBAND_CONNECTION = new ThingTypeUID(BINDING_ID,
            "broadbandConnection");
    public static final ThingTypeUID THING_TYPE_NIGHT_CONTROL = new ThingTypeUID(BINDING_ID, "nightControl");
    public static final ThingTypeUID THING_TYPE_MICE_DETECTION = new ThingTypeUID(BINDING_ID, "miceDetection");
    public static final ThingTypeUID THING_TYPE_EVENT_LOG = new ThingTypeUID(BINDING_ID, "eventLog");
    public static final ThingTypeUID THING_TYPE_GATEWAY = new ThingTypeUID(BINDING_ID, "gateway");

    // List of all Channel ids
    public static final String CHANNEL_NUMERIC_STATUS = "numericStatus";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_HUMIDITY_ENABLED = "humidityEnabled";
    public static final String CHANNEL_LOCATION = "location";
    public static final String CHANNEL_STATUS = "status";
    public static final String CHANNEL_CONNECTED = "connected";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_LABEL = "label";
    public static final String CHANNEL_USER_NAME = "userName";
    public static final String CHANNEL_WEBACCOUNT = "webAccount";
    public static final String CHANNEL_USER_LOCATION_STATUS = "userLocationStatus";
    public static final String CHANNEL_USER_DEVICE_NAME = "userDeviceName";
    public static final String CHANNEL_SMARTLOCK_VOLUME = "smartLockVolume";
    public static final String CHANNEL_SMARTLOCK_VOICE_LEVEL = "smartLockVoiceLevel";
    public static final String CHANNEL_SMARTLOCK_TRIGGER_CHANNEL = "smartLockTriggerChannel";
    public static final String CHANNEL_AUTO_RELOCK = "autoRelock";
    public static final String CHANNEL_SMARTPLUG_STATUS = "smartPlugStatus";
    public static final String CHANNEL_SMARTPLUG_TRIGGER_CHANNEL = "smartPlugTriggerChannel";
    public static final String CHANNEL_ALARM_STATUS = "alarmStatus";
    public static final String CHANNEL_ALARM_TRIGGER_CHANNEL = "alarmTriggerChannel";
    public static final String CHANNEL_SMARTLOCK_STATUS = "smartLockStatus";
    public static final String CHANNEL_CHANGED_BY_USER = "changedByUser";
    public static final String CHANNEL_CHANGED_VIA = "changedVia";
    public static final String CHANNEL_TIMESTAMP = "timestamp";
    public static final String CHANNEL_TEMPERATURE_TIMESTAMP = "temperatureTimestamp";
    public static final String CHANNEL_HAZARDOUS = "hazardous";
    public static final String CHANNEL_MOTOR_JAM = "motorJam";
    public static final String CHANNEL_INSTALLATION_NAME = "installationName";
    public static final String CHANNEL_INSTALLATION_ID = "installationId";
    public static final String CHANNEL_COUNT_LATEST_DETECTION = "countLatestDetection";
    public static final String CHANNEL_COUNT_LAST_24_HOURS = "countLast24Hours";
    public static final String CHANNEL_DURATION_LATEST_DETECTION = "durationLatestDetection";
    public static final String CHANNEL_DURATION_LAST_24_HOURS = "durationLast24Hours";
    public static final String CHANNEL_LAST_EVENT_LOCATION = "lastEventLocation";
    public static final String CHANNEL_LAST_EVENT_ID = "lastEventId";
    public static final String CHANNEL_LAST_EVENT_DEVICE_ID = "lastEventDeviceId";
    public static final String CHANNEL_LAST_EVENT_DEVICE_TYPE = "lastEventDeviceType";
    public static final String CHANNEL_LAST_EVENT_TYPE = "lastEventType";
    public static final String CHANNEL_LAST_EVENT_CATEGORY = "lastEventCategory";
    public static final String CHANNEL_LAST_EVENT_TIME = "lastEventTime";
    public static final String CHANNEL_LAST_EVENT_USER_NAME = "lastEventUserName";
    public static final String CHANNEL_EVENT_LOG = "eventLog";
    public static final String CHANNEL_STATUS_GSM_OVER_UDP = "statusGSMOverUDP";
    public static final String CHANNEL_STATUS_GSM_OVER_SMS = "statusGSMOverSMS";
    public static final String CHANNEL_STATUS_GPRS_OVER_UDP = "statusGPRSOverUDP";
    public static final String CHANNEL_STATUS_ETH_OVER_UDP = "statusETHOverUDP";
    public static final String CHANNEL_TEST_TIME_GSM_OVER_UDP = "testTimeGSMOverUDP";
    public static final String CHANNEL_TEST_TIME_GSM_OVER_SMS = "testTimeGSMOverSMS";
    public static final String CHANNEL_TEST_TIME_GPRS_OVER_UDP = "testTimeGPRSOverUDP";
    public static final String CHANNEL_TEST_TIME_ETH_OVER_UDP = "testTimeETHOverUDP";
    public static final String CHANNEL_GATEWAY_MODEL = "model";
    public static final String CHANNEL_SMOKE_DETECTION_TRIGGER_CHANNEL = "smokeDetectionTriggerChannel";
    public static final String CHANNEL_MICE_DETECTION_TRIGGER_CHANNEL = "miceDetectionTriggerChannel";
    public static final String CHANNEL_WATER_DETECTION_TRIGGER_CHANNEL = "waterDetectionTriggerChannel";
    public static final String CHANNEL_SIREN_TRIGGER_CHANNEL = "sirenTriggerChannel";
    public static final String CHANNEL_NIGHT_CONTROL_TRIGGER_CHANNEL = "nightControlTriggerChannel";
    public static final String CHANNEL_DOOR_WINDOW_TRIGGER_CHANNEL = "doorWindowTriggerChannel";
    public static final String CHANNEL_GATEWAY_TRIGGER_CHANNEL = "gatewayTriggerChannel";
    public static final String CHANNEL_BATTERY_STATUS = "lowBattery";

    // Trigger channel events
    public static final String TRIGGER_EVENT_LOCK = "LOCK";
    public static final String TRIGGER_EVENT_UNLOCK = "UNLOCK";
    public static final String TRIGGER_EVENT_LOCK_FAILURE = "LOCK_FAILURE";
    public static final String TRIGGER_EVENT_ARM = "ARM";
    public static final String TRIGGER_EVENT_DISARM = "DISARM";
    public static final String TRIGGER_EVENT_FIRE = "FIRE";
    public static final String TRIGGER_EVENT_INSTRUSION = "INTRUSION";
    public static final String TRIGGER_EVENT_WATER = "WATER";
    public static final String TRIGGER_EVENT_MICE = "MICE";
    public static final String TRIGGER_EVENT_BATTERY_LOW = "BATTERY_LOW";
    public static final String TRIGGER_EVENT_BATTERY_RESTORED = "BATTERY_RESTORED";
    public static final String TRIGGER_EVENT_COM_FAILURE = "COM_FAILURE";
    public static final String TRIGGER_EVENT_COM_RESTORED = "COM_RESTORED";
    public static final String TRIGGER_EVENT_COM_TEST = "COM_TEST";
    public static final String TRIGGER_EVENT_SABOTAGE_ALARM = "SABOTAGE_ALARM";
    public static final String TRIGGER_EVENT_SABOTAGE_RESTORED = "SABOTAGE_RESTORED";
    public static final String TRIGGER_EVENT_DOORWINDOW_OPENED = "DOORWINDOW_OPENED";
    public static final String TRIGGER_EVENT_DOORWINDOW_CLOSED = "DOORWINDOW_CLOSED";
    public static final String TRIGGER_EVENT_LOCATION_HOME = "LOCATION_HOME";
    public static final String TRIGGER_EVENT_LOCATION_AWAY = "LOCATION_AWAY";

    // REST URI constants
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String BASE_URL = "https://mypages.verisure.com";
    public static final String LOGON_SUF = BASE_URL + "/j_spring_security_check?locale=sv-SE";
    public static final String ALARM_COMMAND = BASE_URL + "/remotecontrol/armstatechange.cmd";
    public static final String SMARTLOCK_LOCK_COMMAND = BASE_URL + "/remotecontrol/lockunlock.cmd";
    public static final String SMARTLOCK_SET_COMMAND = BASE_URL + "/overview/setdoorlock.cmd";
    public static final String SMARTLOCK_AUTORELOCK_COMMAND = BASE_URL + "/settings/setautorelock.cmd";
    public static final String SMARTLOCK_VOLUME_COMMAND = BASE_URL + "/settings/setvolume.cmd";

    public static final String SMARTPLUG_COMMAND = BASE_URL + "/settings/smartplug/onoffplug.cmd";
    public static final String START_REDIRECT = "/se/start.html";
    public static final String START_SUF = BASE_URL + START_REDIRECT;

    // GraphQL constants
    public static final String STATUS = BASE_URL + "/se/status";
    public static final String EXTEND = BASE_URL + "/session/extend";
    public static final String LOGIN = BASE_URL + "/login.html";
    public static final String SETTINGS = BASE_URL + "/se/settings.html?giid=";
    public static final String SET_INSTALLATION = BASE_URL + "/setinstallation?giid=";
    public static final String BASEURL_API = "https://m-api02.verisure.com";
    public static final String START_GRAPHQL = "/graphql";
    public static final String AUTH_TOKEN = "/auth/token";
    public static final String AUTH_LOGIN = "/auth/login";

    public static final String ALARMSTATUS_PATH = "/remotecontrol";
    public static final String SMARTLOCK_PATH = "/overview/doorlock/";
    public static final String DOORWINDOW_PATH = "/settings/doorwindow";
    public static final String USERTRACKING_PATH = "/overview/usertrackingcontacts";
    public static final String CLIMATEDEVICE_PATH = "/overview/climatedevice";
    public static final String SMARTPLUG_PATH = "/settings/smartplug";
    public static final String ETHERNETSTATUS_PATH = "/overview/ethernetstatus";
    public static final String VACATIONMODE_PATH = "/overview/vacationmode";
    public static final String TEMPERATURE_CONTROL_PATH = "/overview/temperaturecontrol";
    public static final String MOUSEDETECTION_PATH = "/overview/mousedetection";
    public static final String CAMERA_PATH = "/overview/camera";
    public static final String BATTERY_STATUS = "/batterywizard/choose/device?_";
}
