/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.netatmo.internal;

import io.rudolph.netatmo.api.common.model.DeviceType;
import io.rudolph.netatmo.api.presence.model.EventType;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The {@link NetatmoBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class NetatmoBindingConstants {

    private static final String BINDING_ID = "netatmo";

    public static final String VENDOR = "Netatmo";

    // Configuration keys
    public static final String EQUIPMENT_ID = "id";
    public static final String PARENT_ID = "parentId";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String SETPOINT_DEFAULT_DURATION = "setpointDefaultDuration";
    public static final String ROOM_PROPERTY = "Room";
    public static final String HOME_PROPERTY = "Home";

    public static final String WEBHOOK_APP = "app_security";

    // List of Bridge Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "netatmoapi");

    // List of Weather Station Things Type UIDs
    public static final ThingTypeUID BASESTATION = new ThingTypeUID(BINDING_ID, DeviceType.BASESTATION.getValue());
    public static final ThingTypeUID OUTDOORMODULE = new ThingTypeUID(BINDING_ID, DeviceType.OUTDOORMODULE.getValue());
    public static final ThingTypeUID WINDMODULE = new ThingTypeUID(BINDING_ID, DeviceType.WINDMODULE.getValue());
    public static final ThingTypeUID RAINGAUGEMODULE = new ThingTypeUID(BINDING_ID,
            DeviceType.RAINGAUGEMODULE.getValue());
    public static final ThingTypeUID INDOORMODULE = new ThingTypeUID(BINDING_ID, DeviceType.INDOORMODULE.getValue());

    // Netatmo Health Coach
    public static final ThingTypeUID HOMECOACH = new ThingTypeUID(BINDING_ID, DeviceType.HOMECOACH.getValue());

    // List of Thermostat Things Type UIDs
    public static final ThingTypeUID RELAY = new ThingTypeUID(BINDING_ID, DeviceType.RELAY.getValue());
    public static final ThingTypeUID THERMOSTAT = new ThingTypeUID(BINDING_ID, DeviceType.THERMOSTAT.getValue());
    public static final ThingTypeUID VALVE = new ThingTypeUID(BINDING_ID, DeviceType.VALVE.getValue());
    public static final ThingTypeUID ROOM = new ThingTypeUID(BINDING_ID, ROOM_PROPERTY);
    public static final ThingTypeUID HOME = new ThingTypeUID(BINDING_ID, HOME_PROPERTY);
    // List of PresenceHome Things Type UIDs
    public static final ThingTypeUID PRESENCE_CAMERA = new ThingTypeUID(BINDING_ID, DeviceType.PRESENCE_CAMERA.getValue());

    // List of Welcome Home Things Type UIDs
    public static final ThingTypeUID WELCOME_HOME_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAWelcomeHome");
    public static final ThingTypeUID WELCOME_CAMERA = new ThingTypeUID(BINDING_ID,
            DeviceType.WELCOME_CAMERA.getValue());
    public static final ThingTypeUID WELCOME_PERSON_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAWelcomePerson");

    // Weather Station Channel ids
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
    public static final String CHANNEL_MAX_WIND_STRENGTH = "MaxWindStrength";
    public static final String CHANNEL_DATE_MAX_WIND_STRENGTH = "DateMaxWindStrength";
    public static final String CHANNEL_GUST_ANGLE = "GustAngle";
    public static final String CHANNEL_GUST_STRENGTH = "GustStrength";
    public static final String CHANNEL_LOW_BATTERY = "LowBattery";
    public static final String CHANNEL_BATTERY_LEVEL = "BatteryVP";
    public static final String CHANNEL_WIFI_STATUS = "WifiStatus";
    public static final String CHANNEL_RF_STATUS = "RfStatus";
    public static final String CHANNEL_ROOM_WINDOW_OPEN = "WindowOpen";

    // Room specific channels

    public static final String CHANNEL_ROOM_HEATING_POWER_REQUEST = "HeatingPowerRequest";
    public static final String CHANNEL_ROOM_SETPOINT_TEMPERATURE = "SetpointTemperature";
    public static final String CHANNEL_ROOM_SETPOINT_MODE = "SetpointMode";
    public static final String CHANNEL_ROOM_SETPOINT_START_TIME = "SetpointStartTime";
    public static final String CHANNEL_ROOM_SETPOINT_END_TIME = "SetpointEndTime";

    // Healthy Home Coach specific channel
    public static final String CHANNEL_HEALTH_INDEX = "HealthIndex";

    // Thermostat specific channels
    public static final String CHANNEL_THERM_RELAY = "ThermRelayCmd";
    public static final String CHANNEL_THERM_ORIENTATION = "ThermOrientation";
    public static final String CHANNEL_CONNECTED_BOILER = "ConnectedBoiler";
    public static final String CHANNEL_LAST_PLUG_SEEN = "LastPlugSeen";
    public static final String CHANNEL_LAST_BILAN = "LastBilan";

    // Home specific
    public static final String CHANNEL_THERM_MODE = "ThermMode";
    public static final String CHANNEL_THERM_SETPOINT_DURATION = "therm_setpoint_default_duration";
    public static final String CHANNEL_ROOM_COUNT = "room_count";
    public static final String CHANNEL_ACTIVE_SCHEDULE = "therm_schedules";


    // Valve specific channels
    public static final String CHANNEL_ANTICIPATION = "Anticipation";
    public static final String CHANNEL_REACHABLE = "Reachable";

    public static final String CHANNEL_PLANNING = "Planning";

    public static final String CHANNEL_SETPOINT_MODE_MANUAL = "manual";
    public static final String CHANNEL_SETPOINT_MODE_AWAY = "away";
    public static final String CHANNEL_SETPOINT_MODE_HG = "hg";
    public static final String CHANNEL_SETPOINT_MODE_OFF = "off";
    public static final String CHANNEL_SETPOINT_MODE_MAX = "max";
    public static final String CHANNEL_SETPOINT_MODE_PROGRAM = "program";

    // Module Properties
    public static final String PROPERTY_SIGNAL_LEVELS = "signalLevels";
    public static final String PROPERTY_BATTERY_LEVELS = "batteryLevels";
    public static final String PROPERTY_REFRESH_PERIOD = "refreshPeriod";

    // Welcome Home specific channels
    public static final String CHANNEL_WELCOME_HOME_CITY = "welcomeHomeCity";
    public static final String CHANNEL_WELCOME_HOME_COUNTRY = "welcomeHomeCountry";
    public static final String CHANNEL_WELCOME_HOME_TIMEZONE = "welcomeHomeTimezone";
    public static final String CHANNEL_WELCOME_HOME_PERSONCOUNT = "welcomeHomePersonCount";
    public static final String CHANNEL_WELCOME_HOME_UNKNOWNCOUNT = "welcomeHomeUnknownCount";

    public static final String CHANNEL_WELCOME_HOME_EVENT = "welcomeHomeEvent";

    public static final String CHANNEL_WELCOME_PERSON_LASTSEEN = "welcomePersonLastSeen";
    public static final String CHANNEL_WELCOME_PERSON_ATHOME = "welcomePersonAtHome";
    public static final String CHANNEL_WELCOME_PERSON_AVATAR_URL = "welcomePersonAvatarUrl";
    public static final String CHANNEL_WELCOME_PERSON_AVATAR = "welcomePersonAvatar";
    public static final String CHANNEL_WELCOME_PERSON_LASTMESSAGE = "welcomePersonLastEventMessage";
    public static final String CHANNEL_WELCOME_PERSON_LASTTIME = "welcomePersonLastEventTime";
    public static final String CHANNEL_WELCOME_PERSON_LASTEVENT = "welcomePersonLastEvent";
    public static final String CHANNEL_WELCOME_PERSON_LASTEVENT_URL = "welcomePersonLastEventUrl";

    public static final String CHANNEL_WELCOME_CAMERA_STATUS = "welcomeCameraStatus";
    public static final String CHANNEL_WELCOME_CAMERA_SDSTATUS = "welcomeCameraSdStatus";
    public static final String CHANNEL_WELCOME_CAMERA_ALIMSTATUS = "welcomeCameraAlimStatus";
    public static final String CHANNEL_WELCOME_CAMERA_ISLOCAL = "welcomeCameraIsLocal";
    public static final String CHANNEL_WELCOME_CAMERA_LIVEPICTURE = "welcomeCameraLivePicture";
    public static final String CHANNEL_WELCOME_CAMERA_LIVEPICTURE_URL = "welcomeCameraLivePictureUrl";
    public static final String CHANNEL_WELCOME_CAMERA_LIVESTREAM_URL = "welcomeCameraLiveStreamUrl";

    public static final String CHANNEL_WELCOME_EVENT_TYPE = "welcomeEventType";
    public static final String CHANNEL_WELCOME_EVENT_TIME = "welcomeEventTime";
    public static final String CHANNEL_WELCOME_EVENT_CAMERAID = "welcomeEventCameraId";
    public static final String CHANNEL_WELCOME_EVENT_PERSONID = "welcomeEventPersonId";
    public static final String CHANNEL_WELCOME_EVENT_SNAPSHOT = "welcomeEventSnapshot";
    public static final String CHANNEL_WELCOME_EVENT_SNAPSHOT_URL = "welcomeEventSnapshotURL";
    public static final String CHANNEL_WELCOME_EVENT_VIDEO_URL = "welcomeEventVideoURL";
    public static final String CHANNEL_WELCOME_EVENT_VIDEOSTATUS = "welcomeEventVideoStatus";
    public static final String CHANNEL_WELCOME_EVENT_ISARRIVAL = "welcomeEventIsArrival";
    public static final String CHANNEL_WELCOME_EVENT_MESSAGE = "welcomeEventMessage";
    public static final String CHANNEL_WELCOME_EVENT_SUBTYPE = "welcomeEventSubType";

    public static final String WELCOME_PICTURE_URL = "https://api.netatmo.com/api/getcamerapicture";
    public static final String WELCOME_PICTURE_IMAGEID = "image_id";
    public static final String WELCOME_PICTURE_KEY = "key";

    // List of all supported physical devices and modules
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Stream
            .of(BASESTATION, OUTDOORMODULE, WINDMODULE, RAINGAUGEMODULE, INDOORMODULE, HOMECOACH, RELAY, THERMOSTAT,
                    WELCOME_HOME_THING_TYPE, WELCOME_CAMERA, WELCOME_PERSON_THING_TYPE, VALVE, ROOM, HOME, PRESENCE_CAMERA)
            .collect(Collectors.toSet());

    // List of all adressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(SUPPORTED_DEVICE_THING_TYPES_UIDS.stream(), Stream.of(APIBRIDGE_THING_TYPE))
            .collect(Collectors.toSet());

    public static final Set<String> MEASURABLE_CHANNELS = Stream.of(new String[] {}).collect(Collectors.toSet());

    public static final Set<EventType> HOME_EVENTS = Stream.of(EventType.PERSON_AWAY).collect(Collectors.toSet());
    public static final Set<EventType> WELCOME_EVENTS = Stream.of(EventType.PERSON, EventType.MOVEMENT,
            EventType.CONNECTION, EventType.DISCONNECTION, EventType.ON, EventType.OFF, EventType.BOOT, EventType.SD,
            EventType.ALIM, EventType.NEW_MODULE, EventType.MODULE_CONNECT, EventType.MODULE_DISCONNECT,
            EventType.MODULE_LOW_BATTERY, EventType.MODULE_END_UPDATE, EventType.TAG_BIG_MOVE, EventType.TAG_SMALL_MOVE,
            EventType.TAG_UNINSTALLED, EventType.TAG_OPEN).collect(Collectors.toSet());
    public static final Set<EventType> PERSON_EVENTS = Stream.of(EventType.PERSON, EventType.PERSON_AWAY)
            .collect(Collectors.toSet());
    public static final Set<EventType> PRESENCE_EVENTS = Stream
            .of(EventType.OUTDOOR, EventType.ALIM, EventType.DAILY_SUMMARY).collect(Collectors.toSet());

}
