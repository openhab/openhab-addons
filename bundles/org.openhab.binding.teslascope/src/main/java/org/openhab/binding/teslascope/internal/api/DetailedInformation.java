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
public class DetailedInformation {
    public String vin = "";
    public String name = "";
    public String state = "";

    @SerializedName("located_at_home")
    public int locatedAtHome;

    @SerializedName("located_at_work")
    public int locatedAtWork;

    @SerializedName("located_at_favorite")
    public int locatedAtFavorite;

    @SerializedName("vehicle_state")
    public @NonNullByDefault({}) VehicleState vehicleState;

    @SerializedName("climate_state")
    public @NonNullByDefault({}) ClimateState climateState;

    @SerializedName("charge_state")
    public @NonNullByDefault({}) ChargeState chargeState;

    @SerializedName("drive_state")
    public @NonNullByDefault({}) DriveState driveState;

    private DetailedInformation() {
    }
}
