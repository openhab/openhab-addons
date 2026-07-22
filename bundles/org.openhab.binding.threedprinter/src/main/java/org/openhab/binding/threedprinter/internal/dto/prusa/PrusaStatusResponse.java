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
package org.openhab.binding.threedprinter.internal.dto.prusa;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for the PrusaLink GET /api/v1/status response.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class PrusaStatusResponse {

    @SerializedName("printer")
    public @Nullable PrusaPrinterData printer;

    @SerializedName("job")
    public @Nullable PrusaJobData job;

    public static class PrusaPrinterData {
        @SerializedName("state")
        public String state = "";

        @SerializedName("temp_nozzle")
        public double tempNozzle;

        @SerializedName("target_nozzle")
        public double targetNozzle;

        @SerializedName("temp_bed")
        public double tempBed;

        @SerializedName("target_bed")
        public double targetBed;

        @SerializedName("fan_print")
        public int fanPrint;

        @SerializedName("speed")
        public int speed = 100;

        @SerializedName("flow")
        public int flow = 100;
    }

    public static class PrusaJobData {
        @SerializedName("progress")
        public double progress;

        @SerializedName("time_remaining")
        public int timeRemaining;

        @SerializedName("time_printing")
        public int timePrinting;

        @SerializedName("file")
        public @Nullable PrusaFileData file;

        public static class PrusaFileData {
            @SerializedName("display_name")
            public String displayName = "";

            @SerializedName("name")
            public String name = "";

            @SerializedName("path")
            public String path = "";
        }
    }
}
