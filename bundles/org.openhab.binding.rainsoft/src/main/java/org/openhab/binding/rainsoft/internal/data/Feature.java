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
package org.openhab.binding.rainsoft.internal.data;

/**
 * @author Wim Vissers - Initial contribution
 * @author Ben Rosenblum - Updated for OH4 / New Maintainer
 */
public enum Feature {

    REMOTE_LOGGING_FORMAT_STORING,
    REMOTE_LOGGING_LEVEL,
    SUBSCRIPTIONS_ENABLED,
    STICKUPCAM_SETUP_ENABLED,
    VOD_ENABLED,
    NW_ENABLED,
    NW_V2_ENABLED,
    NW_USER_ACTIVATED,
    RINGPLUS_ENABLED,
    LPD_ENABLED,
    REACTIVE_SNOOZING_ENABLED,
    PROACTIVE_SNOOZING_ENABLED,
    OWNER_PROACTIVE_SNOOZING_ENABLED,
    LIVE_VIEW_SETTINGS_ENABLED,
    DELETE_ALL_SETTINGS_ENABLED,
    POWER_CABLE_ENABLED,
    DEVICE_HEALTH_ALERTS_ENABLED,
    CHIME_PRO_ENABLED,
    MULTIPLE_CALLS_ENABLED,
    UJET_ENABLED,
    MULTIPLE_DELETE_ENABLED,
    DELETE_ALL_ENABLED,
    LPD_MOTION_ANNOUNCEMENT_ENABLED,
    STARRED_EVENTS_ENABLED,
    CHIME_DND_ENABLED,
    VIDEO_SEARCH_ENABLED,
    FLOODLIGHT_CAM_ENABLED,
    NW_LARGER_AREA_ENABLED,
    RING_CAM_BATTERY_ENABLED,
    ELITE_CAM_ENABLED,
    DOORBELL_V2_ENABLED,
    SPOTLIGHT_BATTERY_DASHBOARD_CONTROLS_ENABLED,
    BYPASS_ACCOUNT_VERIFICATION,
    LEGACY_CVR_RETENTION_ENABLED,
    NEW_DASHBOARD_ENABLED,
    RING_CAM_ENABLED,
    RING_SEARCH_ENABLED,
    RING_CAM_MOUNT_ENABLED,
    RING_ALARM_ENABLED,
    IN_APP_CALL_NOTIFICATIONS,
    RING_CASH_ELIGIBLE_ENABLED,
    NEW_RING_PLAYER_ENABLED,
    APP_ALERT_TONES_ENABLED,
    MOTION_SNOOZING_ENABLED;

    /**
     * The enum is named according to the json names retrieved from
     * the RainSoft API, but in upper case.
     *
     * @return the json name.
     */
    public String getJsonName() {
        return this.toString().toLowerCase();
    }
}
