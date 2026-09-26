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
package org.openhab.binding.tesla.internal.protocol.dto;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VehicleState} is a datastructure to capture
 * variables sent by the Tesla Vehicle
 *
 * @author Karel Goderis - Initial contribution
 */
public class VehicleState {

    @SerializedName("dark_rims")
    public boolean darkRims;
    @SerializedName("has_spoiler")
    public boolean hasSpoiler;
    @SerializedName("homelink_nearby")
    public boolean homelinkNearby;
    @SerializedName("is_user_present")
    public boolean isUserPresent;
    public boolean locked;
    @SerializedName("notifications_supported")
    public boolean notificationsSupported;
    @SerializedName("parsed_calendar_supported")
    public boolean parsedCalendarSupported;
    @SerializedName("remote_start")
    public boolean remoteStart;
    @SerializedName("remote_start_supported")
    public boolean remoteStartSupported;
    public boolean rhd;
    @SerializedName("sentry_mode")
    public boolean sentryMode;
    @SerializedName("valet_mode")
    public boolean valetMode;
    @SerializedName("valet_pin_needed")
    public boolean valetPinNeeded;
    public float odometer;
    @SerializedName("center_display_state")
    public int centerDisplayState;
    public int df;
    public int dr;
    public int ft;
    public int pf;
    public int pr;
    @SerializedName("rear_seat_heaters")
    public int rearSeatHeaters;
    public int rt;
    @SerializedName("seat_type")
    public int seatType;
    @SerializedName("sun_roof_installed")
    public int sunRoofInstalled;
    @SerializedName("sun_roof_percent_open")
    public int sunRoofPercentOpen;
    @SerializedName("autopark_state")
    public String autoparkState;
    @SerializedName("autopark_state_v2")
    public String autoparkStateV2;
    @SerializedName("autopark_style")
    public String autoparkStyle;
    @SerializedName("car_version")
    public String carVersion;
    @SerializedName("exterior_color")
    public String exteriorColor;
    @SerializedName("last_autopark_error")
    public String lastAutoparkError;
    @SerializedName("perf_config")
    public String perfConfig;
    @SerializedName("roof_color")
    public String roofColor;
    @SerializedName("sun_roof_state")
    public String sunRoofState;
    @SerializedName("vehicle_name")
    public String vehicleName;
    @SerializedName("wheel_type")
    public String wheelType;

    @SerializedName("software_update")
    public SoftwareUpdate softwareUpdate;

    // Not reported by every vehicle; null values are left out when the channels are updated
    @SerializedName("fd_window")
    public Integer fdWindow;
    @SerializedName("fp_window")
    public Integer fpWindow;
    @SerializedName("rd_window")
    public Integer rdWindow;
    @SerializedName("rp_window")
    public Integer rpWindow;
    @SerializedName("tpms_pressure_fl")
    public Double tpmsPressureFl;
    @SerializedName("tpms_pressure_fr")
    public Double tpmsPressureFr;
    @SerializedName("tpms_pressure_rl")
    public Double tpmsPressureRl;
    @SerializedName("tpms_pressure_rr")
    public Double tpmsPressureRr;
    @SerializedName("tpms_hard_warning_fl")
    public Boolean tpmsHardWarningFl;
    @SerializedName("tpms_hard_warning_fr")
    public Boolean tpmsHardWarningFr;
    @SerializedName("tpms_hard_warning_rl")
    public Boolean tpmsHardWarningRl;
    @SerializedName("tpms_hard_warning_rr")
    public Boolean tpmsHardWarningRr;
    @SerializedName("tpms_soft_warning_fl")
    public Boolean tpmsSoftWarningFl;
    @SerializedName("tpms_soft_warning_fr")
    public Boolean tpmsSoftWarningFr;
    @SerializedName("tpms_soft_warning_rl")
    public Boolean tpmsSoftWarningRl;
    @SerializedName("tpms_soft_warning_rr")
    public Boolean tpmsSoftWarningRr;

    VehicleState() {
    }
}
