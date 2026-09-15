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
package org.openhab.binding.shelly.internal.api2.dto;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * {@link ShellyMediaJsonDTO} includes constants and structures used for the Wall Display Media component's JSON
 * mapping and processing.
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyMediaJsonDTO {

    public static class Shelly2DeviceStatusMedia {
        public static class Shelly2DeviceStatusMediaPlayback {
            public static class Shelly2DeviceStatusMediaMeta {
                public @Nullable String title;
                public @Nullable String artist;
                public @Nullable String album;
                public @Nullable Integer duration; // ms
                public @Nullable Integer position; // ms
                public @Nullable String thumb;
            }

            public @Nullable Boolean enable;
            public @Nullable Boolean buffering;
            public @Nullable Integer volume; // 0-10
            @SerializedName("media_type")
            public @Nullable String mediaType; // e.g. "AUDIO", "RADIO"
            @SerializedName("media_meta")
            public @Nullable Shelly2DeviceStatusMediaMeta mediaMeta;
        }

        public @Nullable Shelly2DeviceStatusMediaPlayback playback;
    }
}
