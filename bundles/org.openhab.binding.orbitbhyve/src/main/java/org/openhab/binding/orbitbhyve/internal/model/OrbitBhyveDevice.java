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
package org.openhab.binding.orbitbhyve.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link OrbitBhyveDevice} holds information about a B-Hyve
 * device.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class OrbitBhyveDevice {
    String name = "";
    String type = "";
    String id = "";
    List<OrbitBhyveZone> zones = new ArrayList<>();
    OrbitBhyveDeviceStatus status = new OrbitBhyveDeviceStatus();

    @SerializedName("is_connected")
    boolean isConnected = false;

    @SerializedName("hardware_version")
    String hwVersion = "";

    @SerializedName("firmware_version")
    String fwVersion = "";

    @SerializedName("mac_address")
    String macAddress = "";

    @SerializedName("num_stations")
    int numStations = 0;

    @SerializedName("last_connected_at")
    String lastConnectedAt = "";

    @Nullable
    JsonElement location = null;

    @SerializedName("suggested_start_time")
    String suggestedStartTime = "";

    JsonObject timezone = new JsonObject();

    @SerializedName("water_sense_mode")
    String waterSenseMode = "";

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getHwVersion() {
        return hwVersion;
    }

    public String getFwVersion() {
        return fwVersion;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public int getNumStations() {
        return numStations;
    }

    public List<OrbitBhyveZone> getZones() {
        return zones;
    }

    public String getId() {
        return id;
    }

    public OrbitBhyveDeviceStatus getStatus() {
        return status;
    }

    public String getWaterSenseMode() {
        return waterSenseMode;
    }

    public void setWaterSenseMode(String waterSenseMode) {
        this.waterSenseMode = waterSenseMode;
    }
}
