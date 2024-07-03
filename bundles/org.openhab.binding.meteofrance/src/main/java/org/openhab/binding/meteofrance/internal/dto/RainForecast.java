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
package org.openhab.binding.meteofrance.internal.dto;

import java.time.ZonedDateTime;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * @author Gaël L'hopital - Initial contribution
 */
@NonNullByDefault
public class RainForecast {
    public enum RainIntensity {
        @SerializedName("1")
        TEMPS_SEC,
        @SerializedName("2")
        FAIBLE,
        @SerializedName("3")
        MODEREE,
        @SerializedName("4")
        FORTE,
        UNKNOWN;
    }

    public record Forecast(ZonedDateTime time, RainIntensity rainIntensity, String rainIntensityDescription) {
    }

    public record Geometry(String type, List<Double> coordinates) {
    }

    public class Properties {
        public int altitude;
        public String name = "";
        public String country = "";
        public String frenchDepartment = "";
        public int rainProductAvailable;
        public String timezone = "";
        public int confidence;
        public List<Forecast> forecast = List.of();
    }

    public @Nullable ZonedDateTime updateTime;
    public String type = "";
    public @Nullable Geometry geometry;
    public @Nullable Properties properties;
}
