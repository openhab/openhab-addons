/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.teslascope.internal.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * Class for holding the set of parameters used to read the controller variables.
 *
 * @author Paul Smedley - Initial Contribution
 *
 */
@NonNullByDefault
public class VehicleState {
    // vehicle_state
    public int locked;

    @SerializedName("sentry_mode")
    public int sentryMode;

    @SerializedName("valet_mode")
    public int valetMode;

    @SerializedName("software_update_status")
    public String softwareUpdateStatus = "";

    @SerializedName("software_update_version")
    public String softwareUpdateVersion = "";

    @SerializedName("fd_window")
    public int fdWindow;

    @SerializedName("fp_window")
    public int fpWindow;

    @SerializedName("rd_window")
    public int rdWindow;

    @SerializedName("rp_window")
    public int rpWindow;

    @SerializedName("sun_roof_state")
    public String sunRoofState = "";

    @SerializedName("sunRoofPercentOpen")
    public int sunRoofPercentOpen;

    @SerializedName("homelink_nearby")
    public int homelinkNearby;

    @SerializedName("tpms_pressure_fl")
    public double tpmsPressureFL;

    @SerializedName("tpms_pressure_fr")
    public double tpmsPressureFR;

    @SerializedName("tpms_pressure_rl")
    public double tpmsPressureRL;

    @SerializedName("tpms_pressure_rr")
    public double tpmsPressureRR;

    @SerializedName("tpms_soft_warning_fl")
    public int tpmsSoftWarningFL;

    @SerializedName("tpms_soft_warning_fr")
    public int tpmsSoftWarningFR;

    @SerializedName("tpms_soft_warning_rl")
    public int tpmsSoftWarningRL;

    @SerializedName("tpms_soft_warning_rr")
    public int tpmsSoftWarningRR;

    public int df;
    public int dr;
    public int pf;
    public int pr;
    public int ft;
    public int rt;

    private VehicleState() {
    }
}
