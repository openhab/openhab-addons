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
package org.openhab.binding.rainsoft.internal;

/**
 * @author Ben Rosenblum - Initial contribution
 */

public class ApiConstants {
    // API resources
    public static final String API_USER_AGENT = "OpenHAB RainSoft Binding";
    public static final String API_BASE = "https://remind.rainsoft.com/api/remindapp/v2";
    public static final String URL_LOGIN = API_BASE + "/login";
    public static final String URL_CUSTOMER = API_BASE + "/customer";
    public static final String URL_LOCATIONS = API_BASE + "/locations";
    public static final String URL_DEVICE = API_BASE + "/device";

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

    // JSON data names for generic devices
    public static final String DEVICE_ID = "id";
    public static final String DEVICE_DESCRIPTION = "description";
    public static final String DEVICE_DEVICE_ID = "device_id";
    public static final String DEVICE_FIRMWARE_VERSION = "firmware_version";
    public static final String DEVICE_TIME_ZONE = "time_zone";
    public static final String DEVICE_KIND = "kind";
    public static final String DEVICE_BATTERY = "battery_life";
}
