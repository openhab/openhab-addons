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
 * {@link ShellyCoverJsonDTO} includes constants and structures used for the Gen2+ cover/roller component's JSON
 * mapping and processing.
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyCoverJsonDTO {

    public static class Shelly2DevConfigCover {
        public static class Shelly2DeviceConfigCoverMotor {
            @SerializedName("idle_power_thr")
            public @Nullable Double idlePowerThr;
        }

        public static class Shelly2DeviceConfigCoverSafetySwitch {
            public @Nullable Boolean enable;
            public @Nullable String direction;
            public @Nullable String action;
            @SerializedName("allowed_move")
            public @Nullable String allowedMove;
        }

        public static class Shelly2DeviceConfigCoverObstructionDetection {
            public @Nullable Boolean enable;
            public @Nullable String direction;
            public @Nullable String action;
            @SerializedName("power_thr")
            public @Nullable Integer powerThr;
            public @Nullable Double holdoff;
        }

        public @Nullable String id;
        public @Nullable String name;
        public @Nullable Shelly2DeviceConfigCoverMotor motor;
        @SerializedName("maxtime_open")
        public @Nullable Double maxtimeOpen;
        @SerializedName("maxtime_close")
        public @Nullable Double maxtimeClose;
        @SerializedName("initial_state")
        public @Nullable String initialState;
        @SerializedName("invert_directions")
        public @Nullable Boolean invertDirections;
        @SerializedName("in_mode")
        public @Nullable String inMode;
        @SerializedName("swap_inputs")
        public @Nullable Boolean swapInputs;
        @SerializedName("safety_switch")
        public @Nullable Shelly2DeviceConfigCoverSafetySwitch safetySwitch;
        @SerializedName("power_limit")
        public @Nullable Integer powerLimit;
        @SerializedName("voltage_limit")
        public @Nullable Integer voltageLimit;
        @SerializedName("current_limit")
        public @Nullable Double currentLimit;
        @SerializedName("obstruction_detection")
        public @Nullable Shelly2DeviceConfigCoverObstructionDetection obstructionDetection;
    }

    public static class Shelly2CoverStatus {
        public @Nullable Integer id;
        public @Nullable String source;
        public @Nullable String state;
        public @Nullable Double apower;
        public @Nullable Double voltage;
        public @Nullable Double current;
        public @Nullable Double pf;
        public @Nullable Shelly2Energy aenergy;
        @SerializedName("current_pos")
        public @Nullable Integer currentPos;
        @SerializedName("target_pos")
        public @Nullable Integer targetPos;
        @SerializedName("move_timeout")
        public @Nullable Double moveTimeout;
        @SerializedName("move_started_at")
        public @Nullable Double moveStartedAt;
        @SerializedName("pos_control")
        public @Nullable Boolean posControl;
        public @Nullable Shelly2DeviceStatusTemp temperature;
        public @Nullable ArrayList<String> errors;
    }
}
