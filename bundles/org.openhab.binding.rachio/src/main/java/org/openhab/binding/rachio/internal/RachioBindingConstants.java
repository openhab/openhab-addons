/*
 * Copyright (c) 2010-2026 Contributors to the openHAB project
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
package org.openhab.binding.rachio.internal;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link RachioBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Markus Michels - Initial contribution
 */
@NonNullByDefault
public class RachioBindingConstants {

    public static final String BINDING_ID = "rachio";
    public static final String BINDING_VENDOR = "Rachio";

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_CLOUD = new ThingTypeUID(BINDING_ID, "cloud");
    public static final ThingTypeUID THING_TYPE_DEVICE = new ThingTypeUID(BINDING_ID, "device");
    public static final ThingTypeUID THING_TYPE_ZONE = new ThingTypeUID(BINDING_ID, "zone");
    public static final ThingTypeUID THING_TYPE_SCHEDULE = new ThingTypeUID(BINDING_ID, "schedule");
    public static final ThingTypeUID THING_TYPE_FLEX_SCHEDULE = new ThingTypeUID(BINDING_ID, "flex-schedule");
    public static final ThingTypeUID THING_TYPE_BASE_STATION = new ThingTypeUID(BINDING_ID, "base-station");
    public static final ThingTypeUID THING_TYPE_VALVE = new ThingTypeUID(BINDING_ID, "valve");
    public static final ThingTypeUID THING_TYPE_VALVE_PROGRAM = new ThingTypeUID(BINDING_ID, "valve-program");

    public static final Set<ThingTypeUID> SUPPORTED_BRIDGE_THING_TYPES_UIDS = Stream.of(THING_TYPE_CLOUD)
            .collect(Collectors.toSet());
    public static final Set<ThingTypeUID> SUPPORTED_DEVICE_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_DEVICE, THING_TYPE_ZONE, THING_TYPE_SCHEDULE, THING_TYPE_FLEX_SCHEDULE,
                    THING_TYPE_BASE_STATION, THING_TYPE_VALVE, THING_TYPE_VALVE_PROGRAM)
            .collect(Collectors.toSet());
    public static final Set<ThingTypeUID> SUPPORTED_ZONE_THING_TYPES_UIDS = Stream.of(THING_TYPE_ZONE)
            .collect(Collectors.toSet());
    public static final Set<ThingTypeUID> SUPPORTED_SCHEDULE_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_SCHEDULE, THING_TYPE_FLEX_SCHEDULE).collect(Collectors.toSet());
    public static final Set<ThingTypeUID> SUPPORTED_HOSE_TIMER_THING_TYPES_UIDS = Stream
            .of(THING_TYPE_BASE_STATION, THING_TYPE_VALVE, THING_TYPE_VALVE_PROGRAM).collect(Collectors.toSet());
    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Stream
            .concat(SUPPORTED_BRIDGE_THING_TYPES_UIDS.stream(), SUPPORTED_DEVICE_THING_TYPES_UIDS.stream())
            .collect(Collectors.toSet());

    // Rachio Cloud Connector Thing configuration options
    public static final String PARAM_APIKEY = "apikey";
    public static final String PARAM_POLLING_INTERVAL = "pollingInterval";
    public static final String PARAM_DEF_RUNTIME = "defaultRuntime";
    public static final String PARAM_CALLBACK_URL = "callbackUrl";
    public static final String PARAM_CALLBACK_USERNAME = "callbackUsername";
    public static final String PARAM_CALLBACK_PASSWORD = "callbackPassword";
    public static final String PARAM_CLEAR_CALLBACK = "clearAllCallbacks";
    public static final String PARAM_EVENT_HISTORY_LOOKBACK_HOURS = "eventHistoryLookbackHours";
    public static final String PARAM_FORECAST_UNITS = "forecastUnits";
    public static final String PARAM_HOSE_SUMMARY_LOOKBACK_DAYS = "hoseSummaryLookbackDays";
    public static final String PARAM_HOSE_SUMMARY_LOOKAHEAD_DAYS = "hoseSummaryLookaheadDays";

    // List of non-standard Properties
    public static final String PROPERTY_IP_ADDRESS = "ipAddress";
    public static final String PROPERTY_IP_MASK = "ipMask";
    public static final String PROPERTY_IP_GW = "ipGateway";
    public static final String PROPERTY_IP_DNS1 = "ipDNS1";
    public static final String PROPERTY_IP_DNS2 = "ipDNS2";
    public static final String PROPERTY_WIFI_RSSI = "wifiSignal";
    public static final String PROPERTY_NAME = "name";
    public static final String PROPERTY_MODEL = "model";
    public static final String PROPERTY_EXT_ID = "externalId";
    public static final String PROPERTY_DEV_ID = "deviceId";
    public static final String PROPERTY_DEV_LAT = "latitude";
    public static final String PROPERTY_DEV_LONG = "longitude";
    public static final String PROPERTY_ZONE_ID = "zoneId";
    public static final String PROPERTY_SCHEDULE_RULE_ID = "scheduleRuleId";
    public static final String PROPERTY_FLEX_SCHEDULE_RULE_ID = "flexScheduleRuleId";
    public static final String PROPERTY_BASE_STATION_ID = "baseStationId";
    public static final String PROPERTY_VALVE_ID = "valveId";
    public static final String PROPERTY_VALVE_PROGRAM_ID = "programId";
    public static final String PROPERTY_VALVE_PROGRAM_API_VERSION = "programApiVersion";
    public static final String PROPERTY_PERSON_ID = "personId";
    public static final String PROPERTY_PERSON_USER = "accounUserName";
    public static final String PROPERTY_PERSON_NAME = "accountFullName";
    public static final String PROPERTY_PERSON_EMAIL = "accountEMail";

    // List of all Device Channel ids
    public static final String CHANNEL_DEVICE_NAME = "name";
    public static final String CHANNEL_DEVICE_ACTIVE = "active";
    public static final String CHANNEL_DEVICE_ONLINE = "online";
    public static final String CHANNEL_DEVICE_PAUSED = "paused";
    public static final String CHANNEL_DEVICE_PAUSE_TIME = "pauseTime";
    public static final String CHANNEL_DEVICE_SLEEP_MODE = "sleepMode";
    public static final String CHANNEL_DEVICE_RUN = "run";
    public static final String CHANNEL_DEVICE_RUN_ZONES = "runZones";
    public static final String CHANNEL_DEVICE_RUN_TIME = "runTime";
    public static final String CHANNEL_DEVICE_STOP = "stop";
    public static final String CHANNEL_DEVICE_RAIN_DELAY = "rainDelay";
    public static final String CHANNEL_DEVICE_RAIN_STRIPPED = "rainSensorTripped";
    public static final String CHANNEL_DEVICE_ACTIVE_ZONE_NUMBER = "activeZoneNumber";
    public static final String CHANNEL_DEVICE_ACTIVE_ZONE_NAME = "activeZoneName";
    public static final String CHANNEL_DEVICE_ACTIVE_ZONE_ID = "activeZoneId";

    public static final String CHANNEL_CURRENT_SCHEDULE_ID = "currentScheduleId";
    public static final String CHANNEL_CURRENT_SCHEDULE_NAME = "currentScheduleName";
    public static final String CHANNEL_CURRENT_SCHEDULE_TYPE = "currentScheduleType";
    public static final String CHANNEL_CURRENT_SCHEDULE_START = "currentScheduleStartTime";
    public static final String CHANNEL_CURRENT_SCHEDULE_END = "currentScheduleEndTime";
    public static final String CHANNEL_CURRENT_SCHEDULE_DURATION = "currentScheduleDuration";
    public static final String CHANNEL_CURRENT_SCHEDULE_RUNNING = "currentScheduleRunning";
    public static final String CHANNEL_LAST_API_EVENT_TYPE = "lastApiEventType";
    public static final String CHANNEL_LAST_API_EVENT_TIME = "lastApiEventTime";
    public static final String CHANNEL_LAST_API_EVENT_SUMMARY = "lastApiEventSummary";
    public static final String CHANNEL_FORECAST_SUMMARY = "forecastSummary";
    public static final String CHANNEL_FORECAST_TODAY_HIGH = "forecastTodayHigh";
    public static final String CHANNEL_FORECAST_TODAY_LOW = "forecastTodayLow";
    public static final String CHANNEL_FORECAST_PRECIPITATION = "forecastPrecipitation";
    public static final String CHANNEL_FORECAST_PRECIPITATION_PROBABILITY = "forecastPrecipitationProbability";
    public static final String CHANNEL_FORECAST_WIND = "forecastWind";
    public static final String CHANNEL_FORECAST_UPDATED = "forecastUpdated";
    public static final String CHANNEL_LAST_SKIP_TYPE = "lastSkipType";
    public static final String CHANNEL_LAST_SKIP_SCHEDULE_ID = "lastSkipScheduleId";
    public static final String CHANNEL_LAST_SKIP_START = "lastSkipStartTime";
    public static final String CHANNEL_LAST_SKIP_REASON = "lastSkipReason";

    public static final String CHANNEL_SCHED_NAME = "scheduleName";
    public static final String CHANNEL_SCHED_INFO = "scheduleInfo";
    public static final String CHANNEL_SCHED_START = "scheduleStart";
    public static final String CHANNEL_SCHED_END = "scheduleEnd";

    // List of all Zone Channel ids
    public static final String CHANNEL_ZONE_NAME = "name";
    public static final String CHANNEL_ZONE_NUMBER = "number";
    public static final String CHANNEL_ZONE_ENABLED = "enabled";
    public static final String CHANNEL_ZONE_RUN = "run";
    public static final String CHANNEL_ZONE_RUN_TIME = "runTime";
    public static final String CHANNEL_ZONE_RUN_TOTAL = "runTotal";
    public static final String CHANNEL_ZONE_AVAILABLE_WATER = "availableWater";
    public static final String CHANNEL_ZONE_IMAGEURL = "imageUrl";
    public static final String CHANNEL_ZONE_IMAGE = "image";
    public static final String CHANNEL_ZONE_DEPTH_OF_WATER = "depthOfWater";
    public static final String CHANNEL_ZONE_SATURATED_DEPTH_OF_WATER = "saturatedDepthOfWater";
    public static final String CHANNEL_ZONE_MANAGEMENT_ALLOWED_DEPLETION = "managementAllowedDepletion";
    public static final String CHANNEL_ZONE_ROOT_ZONE_DEPTH = "rootZoneDepth";
    public static final String CHANNEL_ZONE_EFFICIENCY = "efficiency";
    public static final String CHANNEL_ZONE_YARD_AREA_SQUARE_FEET = "yardAreaSquareFeet";
    public static final String CHANNEL_ZONE_LAST_WATERED_DATE = "lastWateredDate";
    public static final String CHANNEL_ZONE_FIXED_RUNTIME = "fixedRuntime";
    public static final String CHANNEL_ZONE_MAX_RUNTIME = "maxRuntime";
    public static final String CHANNEL_ZONE_RUNTIME_NO_MULTIPLIER = "runtimeNoMultiplier";
    public static final String CHANNEL_ZONE_SCHEDULE_DATA_MODIFIED = "scheduleDataModified";
    public static final String CHANNEL_ZONE_MOISTURE_LEVEL = "moistureLevel";
    public static final String CHANNEL_ZONE_MOISTURE_PERCENT = "moisturePercent";

    public static final String CHANNEL_SCHEDULE_NAME = "name";
    public static final String CHANNEL_SCHEDULE_ENABLED = "enabled";
    public static final String CHANNEL_SCHEDULE_TYPE = "type";
    public static final String CHANNEL_SCHEDULE_START_TIME = "startTime";
    public static final String CHANNEL_SCHEDULE_LAST_RUN = "lastRun";
    public static final String CHANNEL_SCHEDULE_NEXT_RUN = "nextRun";
    public static final String CHANNEL_SCHEDULE_ZONES = "zones";
    public static final String CHANNEL_SCHEDULE_SEASONAL_ADJUSTMENT = "seasonalAdjustment";
    public static final String CHANNEL_SCHEDULE_START = "start";
    public static final String CHANNEL_SCHEDULE_SKIP = "skip";
    public static final String CHANNEL_SCHEDULE_SKIP_FORWARD_ZONE_RUN = "skipForwardZoneRun";

    public static final String CHANNEL_FLEX_SCHEDULE_NAME = "name";
    public static final String CHANNEL_FLEX_SCHEDULE_ENABLED = "enabled";
    public static final String CHANNEL_FLEX_SCHEDULE_TYPE = "type";
    public static final String CHANNEL_FLEX_SCHEDULE_START_TIME = "start-time";
    public static final String CHANNEL_FLEX_SCHEDULE_LAST_RUN = "last-run";
    public static final String CHANNEL_FLEX_SCHEDULE_NEXT_RUN = "next-run";
    public static final String CHANNEL_FLEX_SCHEDULE_ZONES = "zones";
    public static final String CHANNEL_FLEX_SCHEDULE_SEASONAL_ADJUSTMENT = "seasonal-adjustment";
    public static final String CHANNEL_FLEX_SCHEDULE_START = "start";
    public static final String CHANNEL_FLEX_SCHEDULE_SKIP = "skip";
    public static final String CHANNEL_FLEX_SCHEDULE_SKIP_FORWARD_ZONE_RUN = "skip-forward-zone-run";
    public static final String CHANNEL_FLEX_SCHEDULE_LAST_UPDATE = "last-update";

    public static final String CHANNEL_LAST_UPDATE = "lastUpdate";
    public static final String CHANNEL_LAST_EVENT = "lastEvent";
    public static final String CHANNEL_LAST_EVENTTS = "lastEventTime";
    public static final String CHANNEL_BASE_STATION_NAME = "name";
    public static final String CHANNEL_BASE_STATION_ONLINE = "online";

    public static final String CHANNEL_VALVE_NAME = "name";
    public static final String CHANNEL_VALVE_ONLINE = "online";
    public static final String CHANNEL_VALVE_RUN = "run";
    public static final String CHANNEL_VALVE_RUN_TIME = "runTime";
    public static final String CHANNEL_VALVE_DEFAULT_RUNTIME = "defaultRuntime";
    public static final String CHANNEL_VALVE_STATE_MATCHES = "stateMatches";
    public static final String CHANNEL_VALVE_FLOW_DETECTED = "flowDetected";
    public static final String CHANNEL_VALVE_BATTERY_LEVEL = "batteryLevel";
    public static final String CHANNEL_VALVE_SERIAL_NUMBER = "serialNumber";
    public static final String CHANNEL_VALVE_LAST_RUN_TYPE = "lastRunType";
    public static final String CHANNEL_VALVE_LAST_END_REASON = "lastEndReason";
    public static final String CHANNEL_VALVE_NEXT_PLANNED_RUN_TIME = "nextPlannedRunTime";
    public static final String CHANNEL_VALVE_NEXT_PLANNED_RUN_DURATION = "nextPlannedRunDuration";
    public static final String CHANNEL_VALVE_NEXT_PLANNED_RUN_PROGRAM_ID = "nextPlannedRunProgramId";
    public static final String CHANNEL_VALVE_NEXT_PLANNED_RUN_SKIPPED = "nextPlannedRunSkipped";
    public static final String CHANNEL_VALVE_LAST_COMPLETED_RUN_TIME = "lastCompletedRunTime";
    public static final String CHANNEL_VALVE_LAST_COMPLETED_RUN_DURATION = "lastCompletedRunDuration";
    public static final String CHANNEL_VALVE_LAST_RUN_STATUS = "lastRunStatus";
    public static final String CHANNEL_VALVE_SKIP_NEXT_PLANNED_RUN = "skipNextPlannedRun";
    public static final String CHANNEL_VALVE_CANCEL_NEXT_PLANNED_RUN_SKIP = "cancelNextPlannedRunSkip";

    public static final String CHANNEL_VALVE_PROGRAM_NAME = "name";
    public static final String CHANNEL_VALVE_PROGRAM_ENABLED = "enabled";
    public static final String CHANNEL_VALVE_PROGRAM_TYPE = "programType";
    public static final String CHANNEL_VALVE_PROGRAM_VALVE_ID = "valveId";
    public static final String CHANNEL_VALVE_PROGRAM_START_TIME = "startTime";
    public static final String CHANNEL_VALVE_PROGRAM_NEXT_RUN_TIME = "nextRunTime";
    public static final String CHANNEL_VALVE_PROGRAM_LAST_RUN_TIME = "lastRunTime";
    public static final String CHANNEL_VALVE_PROGRAM_DURATION = "duration";
    public static final String CHANNEL_VALVE_PROGRAM_DAYS_OF_WEEK = "daysOfWeek";
    public static final String CHANNEL_VALVE_PROGRAM_INTERVAL_DAYS = "intervalDays";
    public static final String CHANNEL_VALVE_PROGRAM_SEASONAL_ADJUSTMENT = "seasonalAdjustment";
    public static final String CHANNEL_VALVE_PROGRAM_UPDATED_AT = "updatedAt";
    public static final String CHANNEL_VALVE_PROGRAM_NEXT_RUN_SKIPPED = "nextProgramRunSkipped";
    public static final String CHANNEL_VALVE_PROGRAM_SKIP_NEXT_PLANNED_RUN = "skipNextPlannedRun";
    public static final String CHANNEL_VALVE_PROGRAM_CANCEL_NEXT_PLANNED_RUN_SKIP = "cancelNextPlannedRunSkip";
    public static final String CHANNEL_VALVE_PROGRAM_LAST_RAIN_SKIP_START = "lastRainSkipPlannedRunStartTime";
    public static final String CHANNEL_VALVE_PROGRAM_LAST_RAIN_SKIP_CANCELED_START = "lastRainSkipCanceledPlannedRunStartTime";

    // Default for config options / thing settings
    public static int DEFAULT_POLLING_INTERVAL_SEC = 120;
    public static int DEFAULT_ZONE_RUNTIME_SEC = 300;
    public static int DEFAULT_EVENT_HISTORY_LOOKBACK_HOURS = 24;
    public static int MAX_EVENT_HISTORY_LOOKBACK_HOURS = 168;
    public static int DEFAULT_HOSE_SUMMARY_LOOKBACK_DAYS = 2;
    public static int DEFAULT_HOSE_SUMMARY_LOOKAHEAD_DAYS = 7;
    public static int MAX_HOSE_SUMMARY_WINDOW_DAYS = 31;
    public static final String DEFAULT_FORECAST_UNITS = "METRIC";
    public static final int HTTP_TIMOUT_MS = 15000;
    public static int BINDING_DISCOVERY_TIMEOUT_SEC = 60;

    // --------------- Rachio Cloud API
    public static final String APIURL_BASE = "https://api.rach.io/1/public/";
    public static final String APIURL_CLOUD_REST_BASE = "https://cloud-rest.rach.io";

    public static final String APIURL_GET_PERSON = "person/info"; // obtain personId
    public static final String APIURL_GET_PERSONID = "person"; // obtain personId
    public static final String APIURL_GET_DEVICE = "device"; // get device details, needs /<device id>
    public static final String APIURL_GET_DEVICE_CURRENT_SCHEDULE = "current_schedule";
    public static final String APIURL_GET_DEVICE_EVENT = "event";
    public static final String APIURL_GET_DEVICE_FORECAST = "forecast";

    public static final String APIURL_DEV_PUT_ON = "device/on"; // Enable device / all functions
    public static final String APIURL_DEV_PUT_OFF = "device/off"; // Disable device / all functions
    public static final String APIURL_DEV_PUT_STOP = "device/stop_water"; // stop watering (all zones)
    public static final String APIURL_DEV_PUT_RAIN_DELAY = "device/rain_delay"; // Rain delay device
    public static final String APIURL_DEV_PUT_PAUSE_ZONE_RUN = "device/pause_zone_run"; // Pause active zone run
    public static final String APIURL_DEV_PUT_RESUME_ZONE_RUN = "device/resume_zone_run"; // Resume active zone run
    public static final String APIURL_DEV_POST_WEBHOOK = "notification/webhook"; // deprecated
    public static final String APIURL_DEV_QUERY_WEBHOOK = "notification"; // deprecated
    public static final String APIURL_DEV_DELETE_WEBHOOK = "notification/webhook"; // deprecated

    // New WebhookService endpoints (cloud-rest.rach.io)
    public static final String WEBHOOK_QUERY_CONTROLLER_ID = "resource_id.irrigation_controller_id";
    public static final String WEBHOOK_QUERY_VALVE_ID = "resource_id.valve_id";
    public static final String WEBHOOK_QUERY_PROGRAM_ID = "resource_id.program_id";
    public static final String WEBHOOK_QUERY_LIGHTING_CONTROLLER_ID = "resource_id.lighting_controller_id";
    public static final String WEBHOOK_QUERY_LIGHTING_ZONE_ID = "resource_id.lighting_zone_id";
    public static final String WEBHOOK_QUERY_LIGHTING_SCENE_ID = "resource_id.lighting_scene_id";
    public static final String WEBHOOK_QUERY_LIGHTING_PROGRAM_ID = "resource_id.lighting_program_id";
    public static final String WEBHOOK_CREATE = "/webhook/createWebhook";
    public static final String WEBHOOK_GET = "/webhook/getWebhook/";
    public static final String WEBHOOK_LIST = "/webhook/listWebhooks";
    public static final String WEBHOOK_UPDATE = "/webhook/updateWebhook";
    public static final String WEBHOOK_DELETE = "/webhook/deleteWebhook/";
    public static final String WEBHOOK_DELETE_ALL = "/webhook/deleteAllWebhooks";
    public static final String WEBHOOK_LIST_EVENT_TYPES = "/webhook/listWebhookEventTypes";

    public static final String PROPERTY_GET = "/property/getProperty/";
    public static final String PROPERTY_LIST = "/property/listProperties/";
    public static final String PROPERTY_FIND_BY_ENTITY = "/property/findPropertyByEntity";
    public static final String PROPERTY_QUERY_LOCATION_ID = "resource_id.location_id";
    public static final String PROPERTY_QUERY_BASE_STATION_ID = "resource_id.base_station_id";
    public static final String PROPERTY_QUERY_LIGHTING_AREA_ID = "resource_id.lighting_area_id";

    public static final String VALVE_LIST_BASE_STATIONS = "/valve/listBaseStations/";
    public static final String VALVE_GET_BASE_STATION = "/valve/getBaseStation/";
    public static final String VALVE_LIST_VALVES = "/valve/listValves/";
    public static final String VALVE_GET_VALVE = "/valve/getValve/";
    public static final String VALVE_SET_DEFAULT_RUNTIME = "/valve/setDefaultRuntime";
    public static final String VALVE_START_WATERING = "/valve/startWatering";
    public static final String VALVE_STOP_WATERING = "/valve/stopWatering";

    public static final String PROGRAM_CREATE_SKIP_OVERRIDES = "/program/createSkipOverrides";
    public static final String PROGRAM_DELETE_PROGRAM = "/program/deleteProgram/";
    public static final String PROGRAM_DELETE_SKIP_OVERRIDES = "/program/deleteSkipOverrides";
    public static final String PROGRAM_GET_PROGRAM = "/program/getProgram/";
    public static final String PROGRAM_LIST_PROGRAMS = "/program/listPrograms/";
    public static final String PROGRAM_CREATE_PROGRAM_V2 = "/program/createProgramV2";
    public static final String PROGRAM_GET_PROGRAM_V2 = "/program/getProgramV2/";
    public static final String PROGRAM_LIST_PROGRAMS_V2 = "/program/listProgramsV2";
    public static final String PROGRAM_UPDATE_PROGRAM_V2 = "/program/updateProgramV2";
    public static final String PROGRAM_CREATE_PLANNED_RUN_SKIP_OVERRIDES = "/program/createPlannedRunSkipOverrides";
    public static final String PROGRAM_DELETE_PLANNED_RUN_SKIP_OVERRIDES = "/program/deletePlannedRunSkipOverrides";
    public static final String PROGRAM_QUERY_BASE_STATION_ID = "resourceId.baseStationId";
    public static final String PROGRAM_QUERY_VALVE_ID = "resourceId.valveId";

    public static final String SUMMARY_GET_VALVE_DAY_VIEWS = "/summary/getValveDayViews";

    public static final String APIURL_ZONE_PUT_START = "zone/start"; // start a zone
    public static final String APIURL_ZONE_PUT_MULTIPLE_START = "zone/start_multiple"; // start multiple zones
    public static final String APIURL_ZONE_PUT_ENABLE = "zone/enable"; // enable a zone
    public static final String APIURL_ZONE_PUT_DISABLE = "zone/disable"; // disable a zone
    public static final String APIURL_ZONE_PUT_MOISTURE_LEVEL = "zone/setMoistureLevel";
    public static final String APIURL_ZONE_PUT_MOISTURE_PERCENT = "zone/setMoisturePercent";

    public static final String APIURL_GET_SCHEDULE_RULE = "schedulerule";
    public static final String APIURL_SCHEDULE_RULE_PUT_START = "schedulerule/start";
    public static final String APIURL_SCHEDULE_RULE_PUT_SKIP = "schedulerule/skip";
    public static final String APIURL_SCHEDULE_RULE_PUT_SEASONAL_ADJUSTMENT = "schedulerule/seasonal_adjustment";
    public static final String APIURL_SCHEDULE_RULE_PUT_SKIP_FORWARD_ZONE_RUN = "schedulerule/skip_forward_zone_run";
    public static final String APIURL_GET_FLEX_SCHEDULE_RULE = "flex" + "schedulerule";

    public static final String DEFAULT_IP_FILTER_LIST = "192.168.0.0/16;10.0.0.0/8;172.16.0.0/12";

    // WebHook event types (old numeric IDs - deprecated)
    public static final String WHE_DEVICE_STATUS = "5"; // "Device status event has occurred"
    public static final String WHE_RAIN_DELAY = "6"; // "A rain delay event has occurred"
    public static final String WEATHER_INTELLIGENCE = "7"; // A weather intelligence event has has occurred
    public static final String WHE_WATER_BUDGET = "8"; // A water budget event has occurred
    public static final String WHE_SCHEDULE_STATUS = "9";
    public static final String WHE_ZONE_STATUS = "10";
    public static final String WHE_RAIN_SENSOR_DETECTION = "11"; // physical rain sensor event has coccurred
    public static final String WHE_ZONE_DELTA = "12"; // A physical rain sensor event has occurred
    public static final String WHE_DELTA = "14"; // "An entity has been inserted, updated, or deleted"

    // New Webhook event types (string-based)
    public static final String EVENT_DEVICE_ZONE_RUN_STARTED = "DEVICE_ZONE_RUN_STARTED_EVENT";
    public static final String EVENT_DEVICE_ZONE_RUN_STOPPED = "DEVICE_ZONE_RUN_STOPPED_EVENT";
    public static final String EVENT_DEVICE_ZONE_RUN_COMPLETED = "DEVICE_ZONE_RUN_COMPLETED_EVENT";
    public static final String EVENT_DEVICE_ZONE_RUN_PAUSED = "DEVICE_ZONE_RUN_PAUSED_EVENT";
    public static final String EVENT_SCHEDULE_STARTED = "SCHEDULE_STARTED_EVENT";
    public static final String EVENT_SCHEDULE_STOPPED = "SCHEDULE_STOPPED_EVENT";
    public static final String EVENT_SCHEDULE_COMPLETED = "SCHEDULE_COMPLETED_EVENT";
    public static final String EVENT_RAIN_SKIP = "RAIN_SKIP_NOTIFICATION_EVENT";
    public static final String EVENT_CLIMATE_SKIP = "CLIMATE_SKIP_NOTIFICATION_EVENT";
    public static final String EVENT_FREEZE_SKIP = "FREEZE_SKIP_NOTIFICATION_EVENT";
    public static final String EVENT_WIND_SKIP = "WIND_SKIP_NOTIFICATION_EVENT";
    public static final String EVENT_NO_SKIP = "NO_SKIP_NOTIFICATION_EVENT";
    public static final String EVENT_RAIN_SENSOR_DETECTION_ON = "RAIN_SENSOR_DETECTION_ON_EVENT";
    public static final String EVENT_RAIN_SENSOR_DETECTION_OFF = "RAIN_SENSOR_DETECTION_OFF_EVENT";
    public static final String EVENT_RAIN_DELAY_ON = "RAIN_DELAY_ON_EVENT";
    public static final String EVENT_RAIN_DELAY_OFF = "RAIN_DELAY_OFF_EVENT";
    public static final String EVENT_VALVE_RUN_START = "VALVE_RUN_START_EVENT";
    public static final String EVENT_VALVE_RUN_END = "VALVE_RUN_END_EVENT";
    public static final String EVENT_PROGRAM_RAIN_SKIP_CREATED = "PROGRAM_RAIN_SKIP_CREATED_EVENT";
    public static final String EVENT_PROGRAM_RAIN_SKIP_CANCELED = "PROGRAM_RAIN_SKIP_CANCELED_EVENT";

    public static final String SERVLET_WEBHOOK_PATH = "/rachio/webhook";
    public static final String SERVLET_WEBHOOK_APPLICATION_JSON = "application/json";
    public static final String SERVLET_WEBHOOK_CHARSET = "utf-8";
    public static final String SERVLET_WEBHOOK_USER_AGENT = "Mozilla/5.0";

    public static final String SERVLET_IMAGE_PATH = "/rachio/images";
    public static final String SERVLET_IMAGE_MIME_TYPE = "image/png";
    public static final String SERVLET_IMAGE_URL_BASE = "https://prod-media-photo.rach.io/";

    public static final String RACHIO_JSON_RATE_LIMIT = "X-RateLimit-Limit";
    public static final String RACHIO_JSON_RATE_REMAINING = "X-RateLimit-Remaining";
    public static final String RACHIO_JSON_RATE_RESET = "X-RateLimit-Reset";
    public static final int RACHIO_RATE_LIMIT_WARNING = 200; // slow down polling
    public static final int RACHIO_RATE_LIMIT_CRITICAL = 100; // stop polling
    public static final int RACHIO_RATE_LIMIT_BLOCK = 20; // block api access

    public static final String AWS_IPADDR_DOWNLOAD_URL = "https://ip-ranges.amazonaws.com/ip-ranges.json";
    public static final String AWS_IPADDR_REGION_FILTER = "us-";
}
