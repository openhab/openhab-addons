/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link OrbitBhyveZone} holds information about a B-Hyve
 * zone.
 *
 * @author Ondrej Pecta - Initial contribution
 */
@NonNullByDefault
public class OrbitBhyveZone {
    String name = "";
    int station = 0;

    @SerializedName("catch_cup_run_time")
    int catchCupRunTime = 0;

    @SerializedName("catch_cup_volumes")
    JsonArray catchCupVolumes = new JsonArray();

    @SerializedName("num_sprinklers")
    int numSprinklers = 0;

    @SerializedName("landscape_type")
    @Nullable
    String landscapeType;

    @SerializedName("soil_type")
    @Nullable
    String soilType;

    @SerializedName("sprinkler_type")
    @Nullable
    String sprinklerType;

    @SerializedName("sun_shade")
    @Nullable
    String sunShade;

    @SerializedName("slope_grade")
    int slopeGrade = 0;

    @SerializedName("image_url")
    String imageUrl = "";

    @SerializedName("smart_watering_enabled")
    boolean smartWateringEnabled = false;

    public String getName() {
        return name;
    }

    public int getStation() {
        return station;
    }

    public boolean isSmartWateringEnabled() {
        return smartWateringEnabled;
    }

    public void setSmartWateringEnabled(boolean smartWateringEnabled) {
        this.smartWateringEnabled = smartWateringEnabled;
    }
}
