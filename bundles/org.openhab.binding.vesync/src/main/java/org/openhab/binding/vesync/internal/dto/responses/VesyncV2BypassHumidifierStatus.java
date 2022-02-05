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
package org.openhab.binding.vesync.internal.dto.responses;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link VesyncV2BypassHumidifierStatus} is a Java class used as a DTO to hold the Vesync's API's common response
 * data, in regards to a Air Humidifier device.
 *
 * @author David Goodyear - Initial contribution
 */
public class VesyncV2BypassHumidifierStatus extends VesyncResponse {

    @SerializedName("result")
    public HumidifierrStatus result;

    public class HumidifierrStatus extends VesyncResponse {

        @SerializedName("result")
        public AirHumidifierStatus result;

        public class AirHumidifierStatus {
            @SerializedName("enabled")
            public boolean enabled;

            @SerializedName("humidity")
            public int humidity;

            @SerializedName("mist_virtual_level")
            public int mist_virtual_level;

            @SerializedName("mist_level")
            public int mist_level;

            @SerializedName("mode")
            public String mode;

            @SerializedName("water_lacks")
            public boolean water_lacks;

            @SerializedName("humidity_high")
            public boolean humidityHigh;

            @SerializedName("water_tank_lifted")
            public boolean water_tank_lifted;

            @SerializedName("display")
            public boolean display;

            @SerializedName("automatic_stop_reach_target")
            public boolean automatic_stop_reach_target;

            @SerializedName("configuration")
            public HumidityPurifierConfig configuration;

            @SerializedName("night_light_brightness")
            public int night_light_brightness;

            @SerializedName("warm_enabled")
            public boolean warn_enabled;

            @SerializedName("warm_level")
            public int warm_level;

            public class HumidityPurifierConfig {
                @SerializedName("auto_target_humidity")
                public int autoTargetHumidity;

                @SerializedName("display")
                public boolean display;

                @SerializedName("automatic_stop")
                public boolean automaticStop;
            }
        }
    }
}
