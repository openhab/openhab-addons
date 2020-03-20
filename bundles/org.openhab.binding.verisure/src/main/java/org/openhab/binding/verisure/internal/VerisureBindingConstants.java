/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VerisureBinding} class defines common constants, which are
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
    public static final String CHANNEL_AUTO_RELOCK = "autoRelock";
    public static final String CHANNEL_SMARTPLUG_STATUS = "smartPlugStatus";
    public static final String CHANNEL_ALARM_STATUS = "alarmStatus";
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

    // REST URI constants
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String BASEURL = "https://mypages.verisure.com";
    public static final String LOGON_SUF = BASEURL + "/j_spring_security_check?locale=en_GB";
    public static final String ALARM_COMMAND = BASEURL + "/remotecontrol/armstatechange.cmd";
    public static final String SMARTLOCK_LOCK_COMMAND = BASEURL + "/remotecontrol/lockunlock.cmd";
    public static final String SMARTLOCK_SET_COMMAND = BASEURL + "/overview/setdoorlock.cmd";
    public static final String SMARTLOCK_AUTORELOCK_COMMAND = BASEURL + "/settings/setautorelock.cmd";
    public static final String SMARTLOCK_VOLUME_COMMAND = BASEURL + "/settings/setvolume.cmd";

    public static final String SMARTPLUG_COMMAND = BASEURL + "/settings/smartplug/onoffplug.cmd";
    public static final String START_REDIRECT = "/uk/start.html";
    public static final String START_SUF = BASEURL + START_REDIRECT;

    // GraphQL constants
    public static final String STATUS = BASEURL + "/uk/status";
    public static final String SETTINGS = BASEURL + "/uk/settings.html?giid=";
    public static final String SET_INSTALLATION = BASEURL + "/setinstallation?giid=";
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
}
