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
 * DTO for the PrusaLink GET /api/v1/job response.
 *
 * <p>
 * Unlike /api/v1/status, this endpoint includes the file name and thumbnail reference links for the
 * currently loaded job.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class PrusaJobResponse {

    @SerializedName("file")
    public @Nullable PrusaJobFile file;

    public static class PrusaJobFile {
        @SerializedName("name")
        public String name = "";

        @SerializedName("display_name")
        public String displayName = "";

        @SerializedName("refs")
        public @Nullable PrusaJobFileRefs refs;
    }

    public static class PrusaJobFileRefs {
        @SerializedName("thumbnail")
        public String thumbnail = "";
    }
}
