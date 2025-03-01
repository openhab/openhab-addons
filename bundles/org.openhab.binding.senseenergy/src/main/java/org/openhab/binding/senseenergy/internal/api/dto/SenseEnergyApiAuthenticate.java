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
package org.openhab.binding.senseenergy.internal.api.dto;

import java.time.Instant;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link SenseEnergyApiAuthenticate}
 *
 * @author Jeff James - Initial contribution
 */
public class SenseEnergyApiAuthenticate {
    public boolean authorized;
    @SerializedName("account_id")
    public long accountID;
    @SerializedName("user_id")
    public long userID;
    @SerializedName("access_token")
    public String accessToken;
    @SerializedName("refresh_token")
    public String refreshToken;
    public SenseEnergyApiMonitor[] monitors;
    @SerializedName("bridge_server")
    public String bridgeServer;
    @SerializedName("date_created")
    public Instant dateCreated;
    @SerializedName("totp_enabled")
    public transient boolean totpEnabled;
    @SerializedName("ab_cohort")
    public transient String abCohort;
}

/* @formatter:off
 * {
    "authorized": true,
    "account_id": xxxx,
    "user_id": xxxx,
    "access_token": "t1.v2.xxx",
    "settings": {
        "user_id": xxxx,
        "settings": {
            "notifications": {
                "xxxx": {
                    "new_named_device_push": true,
                    "new_named_device_email": true,
                    "monitor_offline_push": true,
                    "monitor_offline_email": true,
                    "monitor_monthly_email": true,
                    "always_on_change_push": true,
                    "comparison_change_push": true,
                    "new_peak_push": true,
                    "new_peak_email": false,
                    "monthly_change_push": true,
                    "weekly_change_push": false,
                    "daily_change_push": false,
                    "generator_on_push": true,
                    "generator_off_push": true,
                    "time_of_use": true,
                    "grid_outage_push": true,
                    "grid_restored_push": true,
                    "relay_update_available_push": true,
                    "relay_update_installed_push": true,
                    "new_features_and_offers_push": true
                }
            },
            "labs_enabled": true,
            "hide_trends_carbon_card": false,
            "ohm_connect_status": "eligible"
        },
        "version": 1
    },
    "refresh_token": "xxxx",
    "monitors": [
        {
            "id": xxxx,
            "date_created": "2024-04-01T17:12:45.000Z",
            "serial_number": "N327004101",
            "time_zone": "America/Los_Angeles",
            "solar_connected": true,
            "solar_configured": true,
            "online": false,
            "attributes": {
                "id": xxxx,
                "name": "",
                "state": "CA",
                "cost": 20.77,
                "sell_back_rate": 20.77,
                "user_set_cost": false,
                "cycle_start": null,
                "basement_type": "No basement",
                "home_size_type": "3250 sq. ft",
                "home_type": "Single family",
                "number_of_occupants": "3",
                "occupancy_type": "Full-time",
                "year_built_type": "1980s",
                "basement_type_key": null,
                "home_size_type_key": null,
                "home_type_key": null,
                "occupancy_type_key": null,
                "year_built_type_key": null,
                "address": null,
                "city": null,
                "postal_code": "xxxx",
                "electricity_cost": null,
                "show_cost": true,
                "tou_enabled": false,
                "solar_tou_enabled": false,
                "power_region": null,
                "to_grid_threshold": null,
                "panel": null,
                "home_info_survey_progress": "COMPLETED",
                "device_survey_progress": "COMPLETED",
                "user_set_sell_back_rate": true
            },
            "signal_check_completed_time": "2024-04-01T17:31:55.000Z",
            "data_sharing": [],
            "ethernet_supported": false,
            "power_over_ethernet_supported": false,
            "aux_ignore": false,
            "aux_port": "solar",
            "hardware_type": "monitor",
            "zigbee_supported": false
        }
    ],
    "bridge_server": "wss://mb1.home.sense.com",
    "date_created": "2024-04-01T17:15:13.000Z",
    "totp_enabled": false,
    "ab_cohort": "energyhog_2"
}
@formatter:on
 */
