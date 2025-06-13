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
package org.openhab.binding.ring.internal.data;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.JsonObject;

/**
 * {"profile":{
 * "id":4445516,
 * "email":"",
 * "first_name":null,
 * "last_name":null,
 * "phone_number":null,
 * "authentication_token":"CUBSmqFr9YE7cofLZKfy",
 * "features":
 * {
 * "remote_logging_format_storing":false,
 * "remote_logging_level":1,
 * "subscriptions_enabled":true,
 * "stickupcam_setup_enabled":true,
 * "vod_enabled":false,
 * "nw_enabled":true,
 * "nw_v2_enabled":true,
 * "nw_user_activated":false,
 * "ringplus_enabled":true,
 * "lpd_enabled":true,
 * "reactive_snoozing_enabled":false,
 * "proactive_snoozing_enabled":false,
 * "owner_proactive_snoozing_enabled":true,
 * "live_view_settings_enabled":true,
 * "delete_all_settings_enabled":false,
 * "power_cable_enabled":false,
 * "device_health_alerts_enabled":true,
 * "chime_pro_enabled":true,
 * "multiple_calls_enabled":true,
 * "ujet_enabled":true,
 * "multiple_delete_enabled":true,
 * "delete_all_enabled":true,
 * "lpd_motion_announcement_enabled":false,
 * "starred_events_enabled":true,
 * "chime_dnd_enabled":false,
 * "video_search_enabled":false,
 * "floodlight_cam_enabled":true,
 * "nw_larger_area_enabled":false,
 * "ring_cam_battery_enabled":true,
 * "elite_cam_enabled":true,
 * "doorbell_v2_enabled":true,
 * "spotlight_battery_dashboard_controls_enabled":false,
 * "bypass_account_verification":false,
 * "legacy_cvr_retention_enabled":false,
 * "new_dashboard_enabled":false,
 * "ring_cam_enabled":true,
 * "ring_search_enabled":false,
 * "ring_cam_mount_enabled":true,
 * "ring_alarm_enabled":false,
 * "in_app_call_notifications":true,
 * "ring_cash_eligible_enabled":true,
 * "new_ring_player_enabled":false,
 * "app_alert_tones_enabled":true,
 * "motion_snoozing_enabled":true
 * },
 * "hardware_id":"80940d0-7285-3366-8c64-6ea91491982b",
 * "explorer_program_terms":null,
 * "user_flow":"ring"
 * }}
 *
 *
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */

@NonNullByDefault
public class Profile {
    private JsonObject jsonProfile = new JsonObject();
    private JsonObject jsonFeatures = new JsonObject();
    private String refreshToken = "";
    private String accessToken = "";

    /**
     * Create Profile instance from JSON String.
     *
     * @param jsonProfile the JSON profile retrieved from the Ring API.
     * @param refreshToken needed for the refresh token so we aren't logging in every time.
     *            Needed as a separate parameter because it's not part of the jsonProfile object.
     * @param accessToken needed for the access token so we aren't logging in every time.
     *            Needed as a separate parameter because it's not part of the jsonProfile object.
     */
    public Profile(JsonObject jsonProfile, String refreshToken, String accessToken) {
        this.jsonProfile = jsonProfile;
        this.jsonFeatures = (JsonObject) jsonProfile.get("features");
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public Profile() {
    }
}
