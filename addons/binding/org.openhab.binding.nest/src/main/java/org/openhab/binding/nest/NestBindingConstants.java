/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link NestBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author David Bennett - Initial contribution
 */
@NonNullByDefault
public class NestBindingConstants {

    public static final String BINDING_ID = "nest";

    /** The URL to use to connect to Nest with. */
    public static final String NEST_URL = "https://developer-api.nest.com";

    /** The URL to get the access token when talking to Nest. */
    public static final String NEST_ACCESS_TOKEN_URL = "https://api.home.nest.com/oauth2/access_token";

    /** The URL to get set values on the thermostat when talking to Nest. */
    public static final String NEST_THERMOSTAT_UPDATE_URL = NEST_URL + "/devices/thermostats/";

    /** The URL to get set values on the structure when talking to Nest. */
    public static final String NEST_STRUCTURE_UPDATE_URL = NEST_URL + "/structures/";

    /** The URL to get set values on the camera when talking to Nest. */
    public static final String NEST_CAMERA_UPDATE_URL = NEST_URL + "/devices/cameras/";

    /** The URL to get set values on the camera when talking to Nest. */
    public static final String NEST_SMOKE_ALARM_UPDATE_URL = NEST_URL + "/devices/smoke_co_alarms/";

    /** The JSON content type used when talking to Nest. */
    public static final String JSON_CONTENT_TYPE = "application/json";

    /** To avoid API throttling errors (429 Too Many Requests) Nest recommends making at most one call per minute. */
    public static final int MIN_SECONDS_BETWEEN_API_CALLS = 60;

    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_THERMOSTAT = new ThingTypeUID(BINDING_ID, "thermostat");
    public static final ThingTypeUID THING_TYPE_CAMERA = new ThingTypeUID(BINDING_ID, "camera");
    public static final ThingTypeUID THING_TYPE_SMOKE_DETECTOR = new ThingTypeUID(BINDING_ID, "smoke_detector");
    public static final ThingTypeUID THING_TYPE_BRIDGE = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_STRUCTURE = new ThingTypeUID(BINDING_ID, "structure");

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
    public static final String CHANNEL_TIME_TO_TARGET_MINS = "time_to_target_mins";
    public static final String CHANNEL_USING_EMERGENCY_HEAT = "using_emergency_heat";

    // read/write channels (camera)
    public static final String CHANNEL_STREAMING = "streaming";

    // read only channels (camera)
    public static final String CHANNEL_AUDIO_INPUT_ENABLED = "audio_input_enabled";
    public static final String CHANNEL_VIDEO_HISTORY_ENABLED = "video_history_enabled";
    public static final String CHANNEL_WEB_URL = "web_url";
    public static final String CHANNEL_APP_URL = "app_url";
    public static final String CHANNEL_PUBLIC_SHARE_ENABLED = "public_share_enabled";
    public static final String CHANNEL_PUBLIC_SHARE_URL = "public_share_url";
    public static final String CHANNEL_SNAPSHOT_URL = "snapshot_url";
    public static final String CHANNEL_LAST_ONLINE_CHANGE = "last_online_change";

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
}
