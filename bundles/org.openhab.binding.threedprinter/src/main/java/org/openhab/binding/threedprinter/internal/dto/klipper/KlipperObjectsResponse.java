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
package org.openhab.binding.threedprinter.internal.dto.klipper;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for the Moonraker GET /printer/objects/query response.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class KlipperObjectsResponse {

    @SerializedName("result")
    public @Nullable KlipperResult result;

    public static class KlipperResult {
        @SerializedName("status")
        public @Nullable KlipperStatus status;
    }

    public static class KlipperStatus {
        @SerializedName("extruder")
        public @Nullable KlipperHeater extruder;

        @SerializedName("heater_bed")
        public @Nullable KlipperHeater heaterBed;

        @SerializedName("print_stats")
        public @Nullable KlipperPrintStats printStats;

        @SerializedName("display_status")
        public @Nullable KlipperDisplayStatus displayStatus;

        @SerializedName("fan")
        public @Nullable KlipperFan fan;

        @SerializedName("gcode_move")
        public @Nullable KlipperGcodeMove gcodeMove;
    }

    public static class KlipperHeater {
        @SerializedName("temperature")
        public double temperature;

        @SerializedName("target")
        public double target;
    }

    public static class KlipperPrintStats {
        @SerializedName("state")
        public String state = "";

        @SerializedName("filename")
        public String filename = "";

        @SerializedName("print_duration")
        public double printDuration;

        @SerializedName("total_duration")
        public double totalDuration;

        @SerializedName("filament_used")
        public double filamentUsed;
    }

    public static class KlipperDisplayStatus {
        @SerializedName("progress")
        public double progress;

        @SerializedName("message")
        public String message = "";
    }

    public static class KlipperFan {
        @SerializedName("speed")
        public double speed;
    }

    public static class KlipperGcodeMove {
        @SerializedName("speed_factor")
        public double speedFactor = 1.0;
    }
}
