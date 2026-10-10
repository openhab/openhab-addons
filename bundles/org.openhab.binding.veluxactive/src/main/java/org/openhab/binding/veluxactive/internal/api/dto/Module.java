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
package org.openhab.binding.veluxactive.internal.api.dto;

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.veluxactive.internal.api.ModuleType;

import com.google.gson.annotations.SerializedName;

/**
 * Module fields reported by VELUX home topology and status.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class Module {
    public @Nullable String id;
    public @Nullable String name;
    public @Nullable ModuleType type;
    public @Nullable String bridge;
    @SerializedName("modules_bridged")
    public @Nullable List<String> modulesBridged;
    @SerializedName("room_id")
    public @Nullable String roomId;
    public @Nullable Boolean reachable;
    public @Nullable Boolean busy;
    public @Nullable Boolean calibrating;
    @SerializedName("firmware_revision_netatmo")
    public @Nullable Integer firmwareRevisionNetatmo;
    @SerializedName("firmware_revision_thirdparty")
    public @Nullable String firmwareRevisionThirdparty;
    @SerializedName("firmware_revision")
    public @Nullable Integer firmwareRevision;
    @SerializedName("hardware_version")
    public @Nullable Integer hardwareVersion;
    @SerializedName("is_raining")
    public @Nullable Boolean isRaining;
    @SerializedName("last_reset_type")
    public @Nullable String lastResetType;
    @SerializedName("last_seen")
    public @Nullable Long lastSeen;
    public @Nullable Boolean locked;
    public @Nullable Boolean locking;
    public @Nullable String pairing;
    public @Nullable Boolean secure;
    @SerializedName("wifi_strength")
    public @Nullable Integer wifiStrength;
    @SerializedName("wifi_state")
    public @Nullable String wifiState;
    @SerializedName("current_position")
    public @Nullable Integer currentPosition;
    @SerializedName("target_position")
    public @Nullable Integer targetPosition;
    public @Nullable String manufacturer;
    public @Nullable String mode;
    @SerializedName("rain_position")
    public @Nullable Integer rainPosition;
    @SerializedName("secure_position")
    public @Nullable Integer securePosition;
    public @Nullable Boolean silent;
    @SerializedName("velux_type")
    public @Nullable String veluxType;
    @SerializedName("battery_level")
    public @Nullable Integer batteryLevel;
    @SerializedName("battery_percent")
    public @Nullable Integer batteryPercent;
    @SerializedName("rf_strength")
    public @Nullable Integer rfStrength;
    @SerializedName("battery_state")
    public @Nullable String batteryState;
    @SerializedName("rf_state")
    public @Nullable String rfState;
}
