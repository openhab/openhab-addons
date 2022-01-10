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
package org.openhab.binding.nest.internal.wwn;

import java.time.Duration;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link WWNBindingConstants} class defines common constants which are used for the WWN implementation in the
 * binding.
 *
 * @author David Bennett - Initial contribution
 */
@NonNullByDefault
public class WWNBindingConstants {

    public static final String BINDING_ID = "nest";

    /** The URL to use to connect to Nest with. */
    public static final String NEST_URL = "https://developer-api.nest.com";

    /** The URL to get the access token when talking to Nest. */
    public static final String NEST_ACCESS_TOKEN_URL = "https://api.home.nest.com/oauth2/access_token";

    /** The path to set values on the thermostat when talking to Nest. */
    public static final String NEST_THERMOSTAT_UPDATE_PATH = "/devices/thermostats/";

    /** The path to set values on the structure when talking to Nest. */
    public static final String NEST_STRUCTURE_UPDATE_PATH = "/structures/";

    /** The path to set values on the camera when talking to Nest. */
    public static final String NEST_CAMERA_UPDATE_PATH = "/devices/cameras/";

    /** The path to set values on the camera when talking to Nest. */
    public static final String NEST_SMOKE_ALARM_UPDATE_PATH = "/devices/smoke_co_alarms/";

    /** The JSON content type used when talking to Nest. */
    public static final String JSON_CONTENT_TYPE = "application/json";

    /** To keep the streaming REST connection alive Nest sends every 30 seconds a message. */
    public static final long KEEP_ALIVE_MILLIS = Duration.ofSeconds(30).toMillis();

    /** To avoid API throttling errors (429 Too Many Requests) Nest recommends making at most one call per minute. */
    public static final int MIN_SECONDS_BETWEEN_API_CALLS = 60;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "wwn_account");
    public static final ThingTypeUID THING_TYPE_CAMERA = new ThingTypeUID(BINDING_ID, "wwn_camera");
    public static final ThingTypeUID THING_TYPE_SMOKE_DETECTOR = new ThingTypeUID(BINDING_ID, "wwn_smoke_detector");
    public static final ThingTypeUID THING_TYPE_STRUCTURE = new ThingTypeUID(BINDING_ID, "wwn_structure");
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "wwn_thermostat");

    public static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(THING_TYPE_ACCOUNT, THING_TYPE_CAMERA,
            THING_TYPE_SMOKE_DETECTOR, THING_TYPE_STRUCTURE, THING_TYPE_THERMOSTAT);

    // List of all channel group prefixes
    public static final String CHANNEL_GROUP_CAMERA_PREFIX = "camera#";
    public static final String CHANNEL_GROUP_LAST_EVENT_PREFIX = "last_event#";

    // List of all Channel IDs
    // read only channels (common)
    public static final String CHANNEL_LAST_CONNECTION = "last_connection";

    // read/write channels (thermostat)
    public static final String CHANNEL_MODE = "mode";
    public static final String CHANNEL_SET_POINT = "set_point";
    public static final String CHANNEL_MAX_SET_POINT = "max_set_point";
    public static final String CHANNEL_MIN_SET_POINT = "min_set_point";
    public static final String CHANNEL_FAN_TIMER_ACTIVE = "fan_timer_active";
    public static final String CHANNEL_FAN_TIMER_DURATION = "fan_timer_duration";

    // read only channels (thermostat)
    public static final String CHANNEL_ECO_MAX_SET_POINT = "eco_max_set_point";
    public static final String CHANNEL_ECO_MIN_SET_POINT = "eco_min_set_point";
    public static final String CHANNEL_LOCKED = "locked";
    public static final String CHANNEL_LOCKED_MAX_SET_POINT = "locked_max_set_point";
    public static final String CHANNEL_LOCKED_MIN_SET_POINT = "locked_min_set_point";
    public static final String CHANNEL_TEMPERATURE = "temperature";
    public static final String CHANNEL_HUMIDITY = "humidity";
    public static final String CHANNEL_PREVIOUS_MODE = "previous_mode";
    public static final String CHANNEL_STATE = "state";
    public static final String CHANNEL_CAN_HEAT = "can_heat";
    public static final String CHANNEL_CAN_COOL = "can_cool";
    public static final String CHANNEL_FAN_TIMER_TIMEOUT = "fan_timer_timeout";
    public static final String CHANNEL_HAS_FAN = "has_fan";
    public static final String CHANNEL_HAS_LEAF = "has_leaf";
    public static final String CHANNEL_SUNLIGHT_CORRECTION_ENABLED = "sunlight_correction_enabled";
    public static final String CHANNEL_SUNLIGHT_CORRECTION_ACTIVE = "sunlight_correction_active";
    public static final String CHANNEL_TIME_TO_TARGET = "time_to_target";
    public static final String CHANNEL_USING_EMERGENCY_HEAT = "using_emergency_heat";

    // read/write channels (camera)
    public static final String CHANNEL_CAMERA_STREAMING = "camera#streaming";

    // read only channels (camera)
    public static final String CHANNEL_CAMERA_AUDIO_INPUT_ENABLED = "camera#audio_input_enabled";
    public static final String CHANNEL_CAMERA_VIDEO_HISTORY_ENABLED = "camera#video_history_enabled";
    public static final String CHANNEL_CAMERA_WEB_URL = "camera#web_url";
    public static final String CHANNEL_CAMERA_APP_URL = "camera#app_url";
    public static final String CHANNEL_CAMERA_PUBLIC_SHARE_ENABLED = "camera#public_share_enabled";
    public static final String CHANNEL_CAMERA_PUBLIC_SHARE_URL = "camera#public_share_url";
    public static final String CHANNEL_CAMERA_SNAPSHOT_URL = "camera#snapshot_url";
    public static final String CHANNEL_CAMERA_LAST_ONLINE_CHANGE = "camera#last_online_change";

    public static final String CHANNEL_LAST_EVENT_HAS_SOUND = "last_event#has_sound";
    public static final String CHANNEL_LAST_EVENT_HAS_MOTION = "last_event#has_motion";
    public static final String CHANNEL_LAST_EVENT_HAS_PERSON = "last_event#has_person";
    public static final String CHANNEL_LAST_EVENT_START_TIME = "last_event#start_time";
    public static final String CHANNEL_LAST_EVENT_END_TIME = "last_event#end_time";
    public static final String CHANNEL_LAST_EVENT_URLS_EXPIRE_TIME = "last_event#urls_expire_time";
    public static final String CHANNEL_LAST_EVENT_WEB_URL = "last_event#web_url";
    public static final String CHANNEL_LAST_EVENT_APP_URL = "last_event#app_url";
    public static final String CHANNEL_LAST_EVENT_IMAGE_URL = "last_event#image_url";
    public static final String CHANNEL_LAST_EVENT_ANIMATED_IMAGE_URL = "last_event#animated_image_url";
    public static final String CHANNEL_LAST_EVENT_ACTIVITY_ZONES = "last_event#activity_zones";

    // read/write channels (smoke detector)

    // read only channels (smoke detector)
    public static final String CHANNEL_UI_COLOR_STATE = "ui_color_state";
    public static final String CHANNEL_LOW_BATTERY = "low_battery";
    public static final String CHANNEL_CO_ALARM_STATE = "co_alarm_state"; // Also in structure
    public static final String CHANNEL_SMOKE_ALARM_STATE = "smoke_alarm_state"; // Also in structure
    public static final String CHANNEL_MANUAL_TEST_ACTIVE = "manual_test_active";
    public static final String CHANNEL_LAST_MANUAL_TEST_TIME = "last_manual_test_time";

    // read/write channel (structure)
    public static final String CHANNEL_AWAY = "away";

    // read only channels (structure)
    public static final String CHANNEL_COUNTRY_CODE = "country_code";
    public static final String CHANNEL_POSTAL_CODE = "postal_code";
    public static final String CHANNEL_PEAK_PERIOD_START_TIME = "peak_period_start_time";
    public static final String CHANNEL_PEAK_PERIOD_END_TIME = "peak_period_end_time";
    public static final String CHANNEL_TIME_ZONE = "time_zone";
    public static final String CHANNEL_ETA_BEGIN = "eta_begin";
    public static final String CHANNEL_RUSH_HOUR_REWARDS_ENROLLMENT = "rush_hour_rewards_enrollment";
    public static final String CHANNEL_SECURITY_STATE = "security_state";
}
