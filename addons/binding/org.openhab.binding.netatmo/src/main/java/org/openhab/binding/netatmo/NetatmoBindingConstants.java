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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

import io.swagger.client.model.NAWebhookCameraEvent.EventTypeEnum;

/**
 * The {@link NetatmoBinding} class defines common constants, which are used
 * across the whole binding.
 *
 * @author Gaël L'hopital - Initial contribution
 */
public class NetatmoBindingConstants {

    @NonNull
    private static final String BINDING_ID = "netatmo";

    // Configuration keys
    public static final String EQUIPMENT_ID = "id";
    public static final String PARENT_ID = "parentId";
    public static final String REFRESH_INTERVAL = "refreshInterval";
    public static final String SETPOINT_DEFAULT_DURATION = "setpointDefaultDuration";

    public static final String WEBHOOK_APP = "app_security";

    // List of Bridge Type UIDs
    public static final ThingTypeUID APIBRIDGE_THING_TYPE = new ThingTypeUID(BINDING_ID, "netatmoapi");

    // List of Weather Station Things Type UIDs
    public static final ThingTypeUID MAIN_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAMain");
    public static final ThingTypeUID MODULE1_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule1");
    public static final ThingTypeUID MODULE2_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule2");
    public static final ThingTypeUID MODULE3_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule3");
    public static final ThingTypeUID MODULE4_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAModule4");

    // Netatmo Health Coach
    public static final ThingTypeUID HOMECOACH_THING_TYPE = new ThingTypeUID(BINDING_ID, "NHC");

    // List of Thermostat Things Type UIDs
    public static final ThingTypeUID PLUG_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAPlug");
    public static final ThingTypeUID THERM1_THING_TYPE = new ThingTypeUID(BINDING_ID, "NATherm1");

    // List of Welcome Home Things Type UIDs
    public static final ThingTypeUID WELCOME_HOME_THING_TYPE = new ThingTypeUID(BINDING_ID, "NAWelcomeHome");
    public static final ThingTypeUID WELCOME_CAMERA_THING_TYPE = new ThingTypeUID(BINDING_ID, "NACamera");
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
            .of(MAIN_THING_TYPE, MODULE1_THING_TYPE, MODULE2_THING_TYPE, MODULE3_THING_TYPE, MODULE4_THING_TYPE,
                    HOMECOACH_THING_TYPE, PLUG_THING_TYPE, THERM1_THING_TYPE, WELCOME_HOME_THING_TYPE,
                    WELCOME_CAMERA_THING_TYPE, WELCOME_PERSON_THING_TYPE)
            .collect(Collectors.toSet());

    // List of all adressable things in OH = SUPPORTED_DEVICE_THING_TYPES_UIDS + the virtual bridge
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(SUPPORTED_DEVICE_THING_TYPES_UIDS.stream(), Stream.of(APIBRIDGE_THING_TYPE))
            .collect(Collectors.toSet());

    public static final Set<String> MEASURABLE_CHANNELS = Stream.of(CHANNEL_BOILER_ON, CHANNEL_BOILER_OFF)
            .collect(Collectors.toSet());

    public static final Set<EventTypeEnum> HOME_EVENTS = Stream.of(EventTypeEnum.PERSON_AWAY)
            .collect(Collectors.toSet());
    public static final Set<EventTypeEnum> WELCOME_EVENTS = Stream
            .of(EventTypeEnum.PERSON, EventTypeEnum.MOVEMENT, EventTypeEnum.CONNECTION, EventTypeEnum.DISCONNECTION,
                    EventTypeEnum.ON, EventTypeEnum.OFF, EventTypeEnum.BOOT, EventTypeEnum.SD, EventTypeEnum.ALIM,
                    EventTypeEnum.NEW_MODULE, EventTypeEnum.MODULE_CONNECT, EventTypeEnum.MODULE_DISCONNECT,
                    EventTypeEnum.MODULE_LOW_BATTERY, EventTypeEnum.MODULE_END_UPDATE, EventTypeEnum.TAG_BIG_MOVE,
                    EventTypeEnum.TAG_SMALL_MOVE, EventTypeEnum.TAG_UNINSTALLED, EventTypeEnum.TAG_OPEN)
            .collect(Collectors.toSet());
    public static final Set<EventTypeEnum> PERSON_EVENTS = Stream.of(EventTypeEnum.PERSON, EventTypeEnum.PERSON_AWAY)
            .collect(Collectors.toSet());
    public static final Set<EventTypeEnum> PRESENCE_EVENTS = Stream
            .of(EventTypeEnum.OUTDOOR, EventTypeEnum.ALIM, EventTypeEnum.DAILY_SUMMARY).collect(Collectors.toSet());

}
