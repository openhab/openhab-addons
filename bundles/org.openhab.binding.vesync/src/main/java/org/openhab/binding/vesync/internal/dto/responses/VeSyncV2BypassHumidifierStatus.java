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
 * The {@link VeSyncV2BypassHumidifierStatus} is a Java class used as a DTO to hold the Vesync's API's common response
 * data, in regards to an Air Humidifier device.
 *
 * @author David Goodyear - Initial contribution
 */
public class VeSyncV2BypassHumidifierStatus extends VeSyncResponse {

    @SerializedName("result")
    public HumidifierrStatus result;

    public class HumidifierrStatus extends VeSyncResponse {

        @SerializedName("result")
        public AirHumidifierStatus result;

        public class AirHumidifierStatus {
            @SerializedName("enabled")
            public boolean enabled;

            @SerializedName("humidity")
            public int humidity;

            @SerializedName("mist_virtual_level")
            public int mistVirtualLevel;

            @SerializedName("mist_level")
            public int mistLevel;

            @SerializedName("mode")
            public String mode;

            @SerializedName("water_lacks")
            public boolean waterLacks;

            @SerializedName("humidity_high")
            public boolean humidityHigh;

            @SerializedName("water_tank_lifted")
            public boolean waterTankLifted;

            @SerializedName("display")
            public boolean display;

            @SerializedName("automatic_stop_reach_target")
            public boolean automaticStopReachTarget;

            @SerializedName("configuration")
            public HumidityPurifierConfig configuration;

            @SerializedName("night_light_brightness")
            public int nightLightBrightness;

            @SerializedName("warm_enabled")
            public boolean warnEnabled;

            @SerializedName("warm_level")
            public int warmLevel;

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
