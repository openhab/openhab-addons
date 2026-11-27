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

import com.google.gson.annotations.SerializedName;

/**
 * Room values in their original API units; temperatures are reported in tenths of a degree Celsius.
 *
 * @author Jacob Laursen - Initial contribution
 */
@NonNullByDefault
public class Room {
    public @Nullable String id;
    public @Nullable String name;
    public @Nullable String type;
    @SerializedName("module_ids")
    public @Nullable List<String> moduleIds;
    public @Nullable List<String> modules;
    @SerializedName("air_quality")
    public @Nullable Integer airQuality;
    @SerializedName("algo_status")
    public @Nullable Integer algoStatus;
    @SerializedName("auto_close_ts")
    public @Nullable Long autoCloseTs;
    public @Nullable Integer co2;
    public @Nullable Integer humidity;
    public @Nullable Integer lux;
    @SerializedName("max_comfort_co2")
    public @Nullable Integer maxComfortCo2;
    @SerializedName("max_comfort_humidity")
    public @Nullable Integer maxComfortHumidity;
    @SerializedName("max_comfort_temperature")
    public @Nullable Integer maxComfortTemperature;
    @SerializedName("min_comfort_humidity")
    public @Nullable Integer minComfortHumidity;
    @SerializedName("min_comfort_temperature")
    public @Nullable Integer minComfortTemperature;
    public @Nullable Integer temperature;
}
