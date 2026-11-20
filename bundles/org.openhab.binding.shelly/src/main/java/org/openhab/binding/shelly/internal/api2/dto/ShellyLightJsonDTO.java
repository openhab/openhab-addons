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

import java.util.ArrayList;

import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2DeviceStatusTemp;
import org.openhab.binding.shelly.internal.api2.Shelly2ApiJsonDTO.Shelly2Energy;

import com.google.gson.annotations.SerializedName;

/**
 * {@link ShellyLightJsonDTO} contains the JSON structures of the light, RGB(W), CCT and DALI components.
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyLightJsonDTO {

    public static class Shelly2GetConfigLight {
        public static class Shelly2GetConfigLightDefault {
            public @Nullable Integer brightness;
        }

        public static class Shelly2GetConfigLightNightMode {
            public @Nullable Boolean enable;
            public @Nullable Integer brightness;
            public @Nullable Double[] rgb;
            public @Nullable Double white;
        }

        public static class Shelly2ConfigLightPresets {
            public static class Shelly2ConfigLightButtonPreset {
                public @Nullable Double brightness;
                public @Nullable Double[] rgb;
            }

            @SerializedName("button_doublepush")
            public @Nullable Shelly2ConfigLightButtonPreset buttonDoublePush;
        }

        public @Nullable Integer id;
        public @Nullable String name;
        @SerializedName("in_mode")
        public @Nullable String inMode;
        @SerializedName("initial_state")
        public @Nullable String initialState;
        @SerializedName("auto_on")
        public @Nullable Boolean autoOn;
        @SerializedName("auto_off")
        public @Nullable Boolean autoOff;
        @SerializedName("auto_on_delay")
        public @Nullable Double autoOnDelay;
        @SerializedName("auto_off_delay")
        public @Nullable Double autoOffDelay;
        @SerializedName("transition_duration")
        public @Nullable Double transitionDuration;
        @SerializedName("min_brightness_on_toggle")
        public @Nullable Double minBrightnessOnToggle;
        @SerializedName("button_fade_rate")
        public @Nullable Integer buttonFadeRate;
        @SerializedName("button_presets")
        public @Nullable Shelly2ConfigLightPresets buttonPresets;
        @SerializedName("default")
        public @Nullable Shelly2GetConfigLightDefault defaultCfg;
        @SerializedName("night_mode")
        public @Nullable Shelly2GetConfigLightNightMode nightMode;
        @SerializedName("range_map")
        public @Nullable Double[] rangeMap;
        @SerializedName("ct_range")
        public @Nullable Integer[] ctRange; // CCT component only: [min, max] color temperature in Kelvin
    }

    public static class Shelly2DeviceStatusLight {
        public @Nullable Integer id;
        public @Nullable String source;
        public @Nullable Boolean output;
        public @Nullable Double[] rgb;
        public @Nullable Double brightness;
        public @Nullable Shelly2Energy aenergy;
        public @Nullable Double apower;
        public @Nullable Double current;
        public @Nullable Double voltage;

        public @Nullable Shelly2DeviceStatusTemp temperature;
        @SerializedName("timer_started_at")
        public @Nullable Double timerStartedAt;
        @SerializedName("timer_duration")
        public @Nullable Double timerDuration;
        public @Nullable String[] flags;
        public @Nullable Integer ct; // color temperature in Kelvin (CCT component)
    }

    // Devices with combined RGB + CCT support - component "rgbcct:0"
    // mode="cct": ct field valid; mode="rgb": rgb array valid
    public static class Shelly2RGBCCTStatus {
        public @Nullable Integer id;
        public @Nullable String source;
        public @Nullable String mode; // "cct" or "rgb"
        public @Nullable Boolean output;
        public @Nullable Double brightness;
        public @Nullable Integer[] rgb; // [R, G, B] 0-255, valid when mode="rgb"
        public @Nullable Integer ct; // color temperature K, valid when mode="cct"
        public @Nullable Shelly2Energy aenergy;
        public @Nullable Double apower;
        @SerializedName("timer_started_at")
        public @Nullable Double timerStartedAt;
        @SerializedName("timer_duration")
        public @Nullable Double timerDuration;
    }

    public static class Shelly2RGBWStatus {
        public @Nullable Integer id;
        public @Nullable String source;
        public @Nullable Boolean output;
        public @Nullable Integer[] rgb;
        public @Nullable Double brightness;
        public @Nullable Integer white;
        public @Nullable Shelly2DeviceStatusTemp temperature;
        public @Nullable Shelly2Energy aenergy;
        public @Nullable Double apower;
        public @Nullable Double voltage;
        public @Nullable Double current;
    }

    public static class Shelly2DaliScanStatus {
        @SerializedName("cg_count")
        public @Nullable Integer cgCount;
        @SerializedName("started_at")
        public @Nullable Double startedAt;
        public @Nullable ArrayList<String> errors;
    }

    public static class Shelly2DaliStatus {
        @SerializedName("cg_count")
        public @Nullable Integer cgCount;
        public @Nullable Shelly2DaliScanStatus scan;
    }
}
