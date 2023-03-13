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
package org.openhab.binding.ring.internal.data;

import org.json.simple.JSONObject;
import org.openhab.binding.ring.internal.ApiConstants;

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
 */

public class Profile {
    // ADD REFRESH TOKEN
    private JSONObject jsonProfile;
    private JSONObject jsonFeatures;
    private String refreshToken;
    private String accessToken;

    /**
     * Create Profile instance from JSON String.
     *
     * @param jsonProfile the JSON profile retrieved from the Ring API.
     * @param refreshToken needed for the refresh token so we aren't logging in every time.
     *            Needed as a separate parameter because it's not part of the jsonProfile object.
     */
    public Profile(JSONObject jsonProfile, String refreshToken, String accessToken) {
        this.jsonProfile = jsonProfile;
        this.jsonFeatures = (JSONObject) jsonProfile.get("features");
        this.refreshToken = refreshToken;
        this.accessToken = accessToken;
    }

    /**
     * The profile id.
     *
     * @return the id.
     */
    @SuppressWarnings("unchecked")
    public String getId() {
        return jsonProfile.getOrDefault(ApiConstants.PROFILE_ID, "?").toString();
    }

    /**
     * The profile id.
     *
     * @return the id.
     */
    public String getRefreshToken() {
        return refreshToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /**
     * Get the authentication token to be used after successful authentication.
     *
     * @return the token.
     */
    @SuppressWarnings("unchecked")
    public String getAuthenticationToken() {
        return jsonProfile.getOrDefault(ApiConstants.PROFILE_AUTHENTICATION_TOKEN, "?").toString();
    }

    /**
     * Get the hardware id. For this implementation, the hardware id is specified by
     * the user when configuring the Ring Account thing. It is not clear from the
     * reverse engineering done whether the hardware id is really used by Ring. It
     * makes sense however, to have unique hardware id's for each device accessing the
     * Ring API.
     *
     * @return the hardware id.
     */
    @SuppressWarnings("unchecked")
    public String getHardwareId() {
        return jsonProfile.getOrDefault(ApiConstants.PROFILE_HARDWARE_ID, "?").toString();
    }

    /**
     * Get the email address of the Ring user account. The email is also used
     * as the username.
     *
     * @return the email address.
     */
    @SuppressWarnings("unchecked")
    public String getEmail() {
        return jsonProfile.getOrDefault(ApiConstants.PROFILE_EMAIL, "?").toString();
    }

    /**
     * Get the first name of the user, or "?" if not available.
     *
     * @return the first name.
     */
    @SuppressWarnings("unchecked")
    public String getFirstName() {
        return jsonProfile.getOrDefault(ApiConstants.PROFILE_FIRST_NAME, "?").toString();
    }

    /**
     * Get the last name of the user, or "?" if not available.
     *
     * @return the last name.
     */
    @SuppressWarnings("unchecked")
    public String getLastName() {
        return jsonProfile.getOrDefault(ApiConstants.PROFILE_LAST_NAME, "?").toString();
    }

    /**
     * Get the phone number of the user, or "?" if not available.
     *
     * @return the phone number.
     */
    @SuppressWarnings("unchecked")
    public String getPhoneNumber() {
        return jsonProfile.getOrDefault(ApiConstants.PROFILE_PHONE_NUMBER, "?").toString();
    }

    /**
     * Get the user flow, or "?" if not available.
     * Not exactly sure what user flow means in this context. In the author's
     * case, this was "ring".
     *
     * @return the user flow.
     */
    @SuppressWarnings("unchecked")
    public String getUserFlow() {
        return jsonProfile.getOrDefault(ApiConstants.PROFILE_USER_FLOW, "?").toString();
    }

    /**
     * Get the explorer program terms.
     *
     * @return the explorer program terms.
     */
    @SuppressWarnings("unchecked")
    public String getExplorerProgramTerms() {
        return jsonProfile.getOrDefault(ApiConstants.PROFILE_EXPLORER_PROGRAM_TERMS, "?").toString();
    }

    /**
     * Return the value retrieved from the Ring API.
     *
     * @param feature the feature enum constant.
     * @return true or false, or IllegalArgumentException when no value found.
     */
    @SuppressWarnings("unchecked")
    public boolean isFeatureEnabled(Feature feature) {
        String result = jsonFeatures.getOrDefault(feature.getJsonName(), "?").toString();
        if (result.equals("?")) {
            throw new IllegalArgumentException("No value found for feature: " + feature);
        }
        return result.equalsIgnoreCase("true");
    }
}
