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
package org.openhab.binding.tesla.internal.protocol.dto;

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VehicleData} is a data structure to capture
 * variables sent by the Tesla API about a vehicle.
 *
 * @author Kai Kreuzer - Initial contribution
 */
public class VehicleData {

    @SerializedName("id")
    public Long id;
    @SerializedName("user_id")
    public int userId;
    @SerializedName("vehicle_id")
    public String vehicleId;
    @SerializedName("vin")
    public String vin;
    @SerializedName("display_name")
    public String displayName;
    @SerializedName("color")
    public Object color;
    @SerializedName("access_type")
    public String accessType;
    @SerializedName("tokens")
    public List<String> tokens;
    @SerializedName("state")
    public String state;
    @SerializedName("in_service")
    public boolean inService;
    @SerializedName("id_s")
    public String idS;
    @SerializedName("calendar_enabled")
    public boolean calendarEnabled;
    @SerializedName("api_version")
    public int apiVersion;
    @SerializedName("backseat_token")
    public Object backseatToken;
    @SerializedName("backseat_token_updated_at")
    public Object backseatTokenUpdatedAt;
    @SerializedName("charge_state")
    public ChargeState chargeState;
    @SerializedName("climate_state")
    public ClimateState climateState;
    @SerializedName("drive_state")
    public DriveState driveState;
    @SerializedName("gui_settings")
    public GUIState guiSettings;
    @SerializedName("vehicle_config")
    public VehicleConfig vehicleConfig;
    @SerializedName("vehicle_state")
    public VehicleState vehicleState;

    VehicleData() {
    }
}
