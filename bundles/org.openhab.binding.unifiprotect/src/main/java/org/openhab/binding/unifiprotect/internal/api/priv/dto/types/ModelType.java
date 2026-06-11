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
package org.openhab.binding.unifiprotect.internal.api.priv.dto.types;

import com.google.gson.annotations.SerializedName;

/**
 * Model types for UniFi Protect devices and objects
 *
 * @author Dan Cunningham - Initial contribution
 */
public enum ModelType {
    @SerializedName("camera")
    CAMERA("camera", "cameras"),

    @SerializedName("light")
    LIGHT("light", "lights"),

    @SerializedName("sensor")
    SENSOR("sensor", "sensors"),

    @SerializedName("doorlock")
    DOORLOCK("doorlock", "doorlocks"),

    @SerializedName("chime")
    CHIME("chime", "chimes"),

    @SerializedName("bridge")
    BRIDGE("bridge", "bridges"),

    @SerializedName("viewer")
    VIEWER("viewer", "viewers"),

    @SerializedName("aiport")
    AIPORT("aiport", "aiports"),

    @SerializedName("liveview")
    LIVEVIEW("liveview", "liveviews"),

    @SerializedName("nvr")
    NVR("nvr", "nvr"),

    @SerializedName("event")
    EVENT("event", "events"),

    @SerializedName("user")
    USER("user", "users"),

    @SerializedName("group")
    GROUP("group", "groups"),

    @SerializedName("schedule")
    SCHEDULE("schedule", "schedules"),

    @SerializedName("recordingSchedule")
    RECORDING_SCHEDULE("recordingSchedule", "recordingSchedules"),

    @SerializedName("ringtone")
    RINGTONE("ringtone", "ringtones"),

    @SerializedName("keyring")
    KEYRING("keyring", "keyrings"),

    @SerializedName("ulpUser")
    ULP_USER("ulpUser", "ulpUsers"),

    @SerializedName("cloudIdentity")
    CLOUD_IDENTITY("cloudIdentity", "cloudIdentities"),

    @SerializedName("userLocation")
    USER_LOCATION("userLocation", "userLocations"),

    @SerializedName("deviceGroup")
    DEVICE_GROUP("deviceGroup", "deviceGroups"),

    @SerializedName("unknown")
    UNKNOWN("unknown", "unknown");

    private final String value;
    private final String devicesKey;

    ModelType(String value, String devicesKey) {
        this.value = value;
        this.devicesKey = devicesKey;
    }

    public String getValue() {
        return value;
    }

    public String getDevicesKey() {
        return devicesKey;
    }

    public static ModelType fromString(String value) {
        if (value == null) {
            return UNKNOWN;
        }
        for (ModelType type : values()) {
            if (type.value.equalsIgnoreCase(value)) {
                return type;
            }
        }
        return UNKNOWN;
    }

    @Override
    public String toString() {
        return value;
    }
}
