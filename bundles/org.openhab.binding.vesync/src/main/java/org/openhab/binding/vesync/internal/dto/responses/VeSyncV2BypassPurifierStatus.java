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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VeSyncV2BypassPurifierStatus} is a Java class used as a DTO to hold the Vesync's API's common response
 * data, in regards to an Air Purifier device.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncV2BypassPurifierStatus extends VeSyncResponse {

    @SerializedName("result")
    public PurifierStatus result;

    public class PurifierStatus extends VeSyncResponse {

        @SerializedName("result")
        public AirPurifierStatus result;

        public class AirPurifierStatus {
            @SerializedName("enabled")
            public boolean enabled;

            @SerializedName("filter_life")
            public int filterLife;

            @SerializedName("mode")
            public String mode;

            @SerializedName("level")
            public int level;

            @SerializedName("air_quality")
            public int airQuality;

            @SerializedName("air_quality_value")
            public int airQualityValue;

            @SerializedName("display")
            public boolean display;

            @SerializedName("child_lock")
            public boolean childLock;

            @SerializedName("night_light")
            public String nightLight;

            @SerializedName("configuration")
            public AirPurifierConfig configuration;

            public class AirPurifierConfig {
                @SerializedName("display")
                public boolean display;

                @SerializedName("display_forever")
                public boolean displayForever;

                @SerializedName("auto_preference")
                public AirPurifierConfigAutoPref autoPreference;

                public class AirPurifierConfigAutoPref {
                    @SerializedName("type")
                    public String autoType;

                    @SerializedName("room_size")
                    public int roomSize;
                }
            }

            @SerializedName("extension")
            public AirPurifierExtension extension;

            public class AirPurifierExtension {
                @SerializedName("schedule_count")
                public int scheduleCount;

                @SerializedName("timer_remain")
                public int timerRemain;
            }

            @SerializedName("device_error_code")
            public int deviceErrorCode;
        }
    }
}
