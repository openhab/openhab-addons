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
package org.openhab.binding.ring.internal;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class ApiConstants {
    public static final int API_VERSION = 11;

    // API resources
    public static final String API_USER_AGENT = "OpenHAB Ring Binding";
    public static final String API_OAUTH_ENDPOINT = "https://oauth.ring.com/oauth/token";
    public static final String API_BASE = "https://api.ring.com";
    public static final String URL_SESSION = API_BASE + "/clients_api/session";
    public static final String URL_DEVICES = API_BASE + "/clients_api/ring_devices";
    public static final String URL_HISTORY = API_BASE + "/clients_api/doorbots/history";
    public static final String URL_RECORDING_START = API_BASE + "/clients_api/dings/";
    public static final String URL_RECORDING_END = "/share/play?disable_redirect=true";
    public static final String URL_DOORBELLS = API_BASE + "/clients_api/doorbots";
    public static final String URL_CHIMES = API_BASE + "/clients_api/chimes";

    public static final String URL_RECORDING = "/clients_api/dings/{0}/recording";

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
    public static final String EVENT_KIND = "kind";
    public static final String EVENT_FAVORITE = "favorite";
    public static final String EVENT_SNAPSHOT_URL = "snapshot_url";
    public static final String EVENT_DOORBOT = "doorbot";

    // JSON names for doorbot
    public static final String DOORBOT_ID = "id";
    public static final String DOORBOT_DESCRIPTION = "description";

    // JSON data names for ring devices
    public static final String DEVICES_DOORBOTS = "doorbots";
    public static final String DEVICES_CHIMES = "chimes";
    public static final String DEVICES_STICKUP_CAMS = "stickup_cams";
    public static final String DEVICES_OTHERDEVICE = "other";

    // JSON data names for generic devices
    public static final String DEVICE_ID = "id";
    public static final String DEVICE_DESCRIPTION = "description";
    public static final String DEVICE_DEVICE_ID = "device_id";
    public static final String DEVICE_FIRMWARE_VERSION = "firmware_version";
    public static final String DEVICE_TIME_ZONE = "time_zone";
    public static final String DEVICE_KIND = "kind";
    public static final String DEVICE_BATTERY = "battery_life";
}
