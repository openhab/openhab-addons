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
package org.openhab.binding.threedprinter.internal.dto.octoprint;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for the OctoPrint GET /api/printer response.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class OctoPrintPrinterResponse {

    @SerializedName("temperature")
    public @Nullable OctoPrintTemperature temperature;

    @SerializedName("state")
    public @Nullable OctoPrintState state;

    public static class OctoPrintTemperature {
        @SerializedName("tool0")
        public @Nullable OctoPrintTempReading tool0;

        @SerializedName("bed")
        public @Nullable OctoPrintTempReading bed;

        public static class OctoPrintTempReading {
            @SerializedName("actual")
            public double actual;

            @SerializedName("target")
            public double target;
        }
    }

    public static class OctoPrintState {
        @SerializedName("text")
        public String text = "";

        @SerializedName("flags")
        public @Nullable OctoPrintStateFlags flags;

        public static class OctoPrintStateFlags {
            @SerializedName("printing")
            public boolean printing;

            @SerializedName("paused")
            public boolean paused;

            @SerializedName("cancelling")
            public boolean cancelling;

            @SerializedName("error")
            public boolean error;

            @SerializedName("ready")
            public boolean ready;

            @SerializedName("operational")
            public boolean operational;
        }
    }
}
