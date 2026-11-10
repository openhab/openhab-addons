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
package org.openhab.binding.tailwind.internal.api.dto;

import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * Answer of a TailWind controller to a request, and the content of its status reports.
 *
 * Only the answer to a status request and the status reports have the device fields. The answer to a command has
 * just a result, and information about the failure if it failed.
 *
 * @author Dave J. Schoepel - Initial contribution
 * @author Christopher Smith - Merged the response classes
 */
@NonNullByDefault
public class TailwindStatus {
    public static final String RESULT_OK = "OK";

    public @Nullable String result;
    public @Nullable String info;
    public @Nullable String product;
    @SerializedName("dev_id")
    public @Nullable String deviceId;
    @SerializedName("fw_ver")
    public @Nullable String firmwareVersion;
    @SerializedName("door_num")
    public @Nullable Integer doorCount;
    @SerializedName("night_mode_en")
    public @Nullable Integer nightModeEnabled;
    @SerializedName("led_brightness")
    public @Nullable Integer ledBrightness;
    @SerializedName("router_rssi")
    public @Nullable Integer routerRssi;
    /** Missing from controllers with firmware older than 10.61. */
    @SerializedName("server_monitor")
    public @Nullable Boolean serverMonitor;
    /** The doors, with the keys door1, door2 and door3. */
    public @Nullable Map<String, @Nullable TailwindDoor> data;
    /** Only in status reports. */
    public @Nullable TailwindNotification notify;

    public boolean isOk() {
        return RESULT_OK.equals(result);
    }

    /**
     * Returns the door with the given index, starting at 0, or null if the controller did not report it.
     */
    public @Nullable TailwindDoor door(int index) {
        Map<String, @Nullable TailwindDoor> doors = data;
        return doors == null ? null : doors.get("door" + (index + 1));
    }
}
