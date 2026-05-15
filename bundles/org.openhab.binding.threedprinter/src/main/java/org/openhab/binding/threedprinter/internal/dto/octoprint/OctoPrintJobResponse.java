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
 * DTO for the OctoPrint GET /api/job response.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class OctoPrintJobResponse {

    @SerializedName("job")
    public @Nullable OctoPrintJob job;

    @SerializedName("progress")
    public @Nullable OctoPrintProgress progress;

    @SerializedName("state")
    public String state = "";

    public static class OctoPrintJob {
        @SerializedName("file")
        public @Nullable OctoPrintFile file;

        @SerializedName("estimatedPrintTime")
        public double estimatedPrintTime;

        public static class OctoPrintFile {
            @SerializedName("name")
            public String name = "";

            @SerializedName("display")
            public String display = "";
        }
    }

    public static class OctoPrintProgress {
        @SerializedName("completion")
        public double completion;

        @SerializedName("printTime")
        public int printTime;

        @SerializedName("printTimeLeft")
        public int printTimeLeft;
    }
}
