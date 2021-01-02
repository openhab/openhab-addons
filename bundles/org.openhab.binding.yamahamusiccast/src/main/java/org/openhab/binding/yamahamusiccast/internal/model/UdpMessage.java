/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.yamahamusiccast.internal.model;

import org.eclipse.jdt.annotation.*;
import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the UDP event received from the Yamaha model/device.
 *
 * @author Lennert Coopman - Initial contribution
 */
@NonNullByDefault
public class UdpMessage {

    @SerializedName("device_id")
    private @Nullable String deviceId;

    public @Nullable String getDeviceId() {
        return deviceId;
    }

    @SerializedName("main")
    private @Nullable Zone main;
    @SerializedName("zone2")
    private @Nullable Zone zone2;
    @SerializedName("zone3")
    private @Nullable Zone zone3;
    @SerializedName("zone4")
    private @Nullable Zone zone4;
    @SerializedName("netusb")
    private @Nullable NetUSB netusb;

    public @Nullable Zone getMain() {
        return main;
    }

    public @Nullable Zone getZone2() {
        return zone2;
    }

    public @Nullable Zone getZone3() {
        return zone3;
    }

    public @Nullable Zone getZone4() {
        return zone4;
    }

    public @Nullable NetUSB getNetUSB() {
        return netusb;
    }

    @NonNullByDefault
    public class Zone {
        @SerializedName("power")
        private @Nullable String power;
        @SerializedName("volume")
        private int volume = 0;
        @SerializedName("mute")
        private @Nullable String mute;
        @SerializedName("input")
        private @Nullable String input;
        @SerializedName("status_updated")
        private @Nullable String statusUpdated;

        public @Nullable String getPower() {
            if (power == null) {
                power = "";
            }
            return power;
        }

        public @Nullable String getMute() {
            if (mute == null) {
                mute = "";
            }
            return mute;
        }

        public @Nullable String getInput() {
            if (input == null) {
                input = "";
            }
            return input;
        }

        public int getVolume() {
            return volume;
        }

        public @Nullable String getstatusUpdated() {
            if (statusUpdated == null) {
                statusUpdated = "";
            }
            return statusUpdated;
        }
    }

    @NonNullByDefault
    public class NetUSB {
        @SerializedName("preset_control")
        private @Nullable PresetControl presetControl;
        @SerializedName("play_info_updated")
        private @Nullable String playInfoUpdated;

        public @Nullable PresetControl getPresetControl() {
            return presetControl;
        }

        public @Nullable String getPlayInfoUpdated() {
            if (playInfoUpdated == null) {
                playInfoUpdated = "";
            }
            return playInfoUpdated;
        }
    }

    @NonNullByDefault
    public class PresetControl {
        @SerializedName("type")
        private @Nullable String type;
        @SerializedName("num")
        private int num = 1;
        @SerializedName("result")
        private @Nullable String result;

        public @Nullable String getType() {
            if (type == null) {
                type = "";
            }
            return type;
        }

        public @Nullable String getResult() {
            if (result == null) {
                result = "";
            }
            return result;
        }

        public int getNum() {
            return num;
        }
    }
}
