/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.ring.internal;

/**
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

public class ApiConstants {
    public static final int API_VERSION = 11;

    // API resources
    public static final String API_USER_AGENT = "OpenHAB Ring Binding";
    public static final String API_OAUTH_ENDPOINT = "https://oauth.ring.com/oauth/token";
    public static final String API_BASE = "https://api.ring.com";
    public static final String URL_SESSION = API_BASE + "/clients_api/session";
    public static final String URL_DINGS = API_BASE + "/clients_api/dings/active";
    public static final String URL_DEVICES = API_BASE + "/clients_api/ring_devices";
    public static final String URL_HISTORY = API_BASE + "/clients_api/doorbots/history";
    public static final String URL_RECORDING_START = API_BASE + "/clients_api/dings/";
    public static final String URL_RECORDING_END = "/share/play?disable_redirect=true";
    public static final String URL_DOORBELLS = API_BASE + "/clients_api/doorbots";
    public static final String URL_CHIMES = API_BASE + "/clients_api/chimes";

    public static final String HEALTH_DOORBELL_ENDPOINT = URL_DOORBELLS + "/health";
    public static final String HEALTH_CHIMES_ENDPOINT = URL_CHIMES + "/health";
    public static final String LIGHTS_ENDPOINT = URL_DOORBELLS + "/floodlight_light_{1}";
    public static final String LINKED_CHIMES_ENDPOINT = URL_CHIMES + "/linked_doorbots";
    public static final String LIVE_STREAMING_ENDPOINT = URL_DOORBELLS + "/vod";
    public static final String NEW_SESSION_ENDPOINT = "/clients_api/session";
    public static final String RINGTONES_ENDPOINT = "/ringtones";
    public static final String SIREN_ENDPOINT = URL_DOORBELLS + "/siren_{1}";
    public static final String TESTSOUND_CHIME_ENDPOINT = URL_CHIMES + "/play_sound";
    public static final String URL_DOORBELL_HISTORY = URL_DOORBELLS + "/history";
    public static final String URL_RECORDING = "/clients_api/dings/{0}/recording";

    public static final String KIND_DING = "ding";
    public static final String KIND_MOTION = "motion";
    // public static final String CHIME_TEST_SOUND_KINDS = (KIND_DING, KIND_MOTION);

    // default values
    public static final int CHIME_VOL_MIN = 0;
    public static final int CHIME_VOL_MAX = 10;

    public static final int DOORBELL_VOL_MIN = 0;
    public static final int DOORBELL_VOL_MAX = 11;

    public static final int SIREN_DURATION_MIN = 0;
    public static final int SIREN_DURATION_MAX = 120;

    // error strings
    public static final String MSG_BOOLEAN_REQUIRED = "Boolean value is required.";
    public static final String MSG_EXISTING_TYPE = "Integer value where {0}.";// .format(DOORBELL_EXISTING_TYPE);
    public static final String MSG_GENERIC_FAIL = "Sorry.. Something went wrong...";
    public static final String FILE_EXISTS = "The file {0} already exists.";
    public static final String MSG_VOL_OUTBOUND = "Must be within the {0}-{1}.";
    public static final String MSG_ALLOWED_VALUES = "Only the following values are allowed: {0}.";

    // JSON data names for profile
    public static final String PROFILE_AUTHENTICATION_TOKEN = "authentication_token";
    public static final String PROFILE_ID = "id";
    public static final String PROFILE_EMAIL = "email";
    public static final String PROFILE_HARDWARE_ID = "hardware_id";
    public static final String PROFILE_FIRST_NAME = "first_name";
    public static final String PROFILE_LAST_NAME = "last_name";
    public static final String PROFILE_PHONE_NUMBER = "phone_number";
    public static final String PROFILE_USER_FLOW = "user_flow";
    public static final String PROFILE_EXPLORER_PROGRAM_TERMS = "explorer_program_terms";

    // JSON names for events
    public static final String EVENT_ID = "id";
    public static final String EVENT_CREATED_AT = "created_at";
    public static final String EVENT_ANSWERED = "answered";
    public static final String EVENT_EVENTS = "events";
    public static final String EVENT_KIND = "kind";
    public static final String EVENT_FAVORITE = "favorite";
    public static final String EVENT_SNAPSHOT_URL = "snapshot_url";
    public static final String EVENT_RECORDING = "recording";
    public static final String EVENT_DOORBOT = "doorbot";

    // JSON names for doorbot
    public static final String DOORBOT_ID = "id";
    public static final String DOORBOT_DESCRIPTION = "description";

    // JSON data names for ring devices
    public static final String DEVICES_DOORBOTS = "doorbots";
    public static final String DEVICES_AUTHORIZED_DOORBOTS = "authorized_doorbots";
    public static final String DEVICES_CHIMES = "chimes";
    public static final String DEVICES_STICKUP_CAMS = "stickup_cams";
    public static final String DEVICES_BASE_STATIONS = "base_stations";
    public static final String DEVICES_OTHER = "other";

    // JSON data names for generic devices
    public static final String DEVICE_ID = "id";
    public static final String DEVICE_DESCRIPTION = "description";
    public static final String DEVICE_DEVICE_ID = "device_id";
    public static final String DEVICE_FIRMWARE_VERSION = "firmware_version";
    public static final String DEVICE_TIME_ZONE = "time_zone";
    public static final String DEVICE_KIND = "kind";
    public static final String DEVICE_BATTERY = "battery_life";
}
