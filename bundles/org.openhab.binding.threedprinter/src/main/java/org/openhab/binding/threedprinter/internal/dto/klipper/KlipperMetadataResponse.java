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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * DTO for the Moonraker GET /server/files/metadata response.
 *
 * @author Scott Hanson - Initial contribution
 */
@NonNullByDefault
public class KlipperMetadataResponse {

    @SerializedName("result")
    public @Nullable KlipperMetadataResult result;

    public static class KlipperMetadataResult {
        @SerializedName("thumbnails")
        public @Nullable List<KlipperThumbnail> thumbnails;
    }

    public static class KlipperThumbnail {
        @SerializedName("relative_path")
        public String relativePath = "";

        @SerializedName("width")
        public int width;

        @SerializedName("height")
        public int height;

        @SerializedName("size")
        public int size;
    }
}
