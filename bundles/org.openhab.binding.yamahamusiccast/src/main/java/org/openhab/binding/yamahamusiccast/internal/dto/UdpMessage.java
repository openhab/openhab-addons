/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
package org.openhab.binding.yamahamusiccast.internal.dto;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

/**
 * This class represents the UDP event received from the Yamaha model/device.
 *
 * @author Lennert Coopman - Initial contribution
 * @author Florian Hotze - Add volume in decibel
 */

public class UdpMessage {

    @SerializedName("device_id")
    private String deviceId;

    public String getDeviceId() {
        if (deviceId == null) {
            deviceId = "";
        }
        return deviceId;
    }

    @SerializedName("main")
    private Zone main;
    @SerializedName("zone2")
    private Zone zone2;
    @SerializedName("zone3")
    private Zone zone3;
    @SerializedName("zone4")
    private Zone zone4;
    @SerializedName("netusb")
    private NetUSB netusb;
    @SerializedName("dist")
    private Dist dist;

    public Zone getMain() {
        return main;
    }

    public Zone getZone2() {
        return zone2;
    }

    public Zone getZone3() {
        return zone3;
    }

    public Zone getZone4() {
        return zone4;
    }

    public NetUSB getNetUSB() {
        return netusb;
    }

    public Dist getDist() {
        return dist;
    }

    public class Zone {
        @SerializedName("power")
        private String power;
        @SerializedName("volume")
        private int volume = 0;
        @SerializedName("actual_volume")
        private ActualVolume actualVolume;
        @SerializedName("mute")
        private String mute;
        @SerializedName("input")
        private String input;
        @SerializedName("status_updated")
        private String statusUpdated;

        public String getPower() {
            if (power == null) {
                power = "";
            }
            return power;
        }

        public String getMute() {
            if (mute == null) {
                mute = "";
            }
            return mute;
        }

        public String getInput() {
            if (input == null) {
                input = "";
            }
            return input;
        }

        public int getVolume() {
            return volume;
        }

        public @Nullable ActualVolume getActualVolume() {
            return actualVolume;
        }

        public String getstatusUpdated() {
            if (statusUpdated == null) {
                statusUpdated = "";
            }
            return statusUpdated;
        }
    }

    public class NetUSB {
        @SerializedName("preset_control")
        private PresetControl presetControl;
        @SerializedName("play_info_updated")
        private String playInfoUpdated;
        @SerializedName("play_time")
        private int playTime;

        public PresetControl getPresetControl() {
            return presetControl;
        }

        public String getPlayInfoUpdated() {
            if (playInfoUpdated == null) {
                playInfoUpdated = "";
            }
            return playInfoUpdated;
        }

        public int getPlayTime() {
            return playTime;
        }
    }

    public class PresetControl {
        @SerializedName("type")
        private String type;
        @SerializedName("num")
        private int num = 1;
        @SerializedName("result")
        private String result;

        public String getType() {
            if (type == null) {
                type = "";
            }
            return type;
        }

        public String getResult() {
            if (result == null) {
                result = "";
            }
            return result;
        }

        public int getNum() {
            return num;
        }
    }

    public class Dist {
        @SerializedName("dist_info_updated")
        private String distInfoUpdated;

        public String getDistInfoUpdated() {
            if (distInfoUpdated == null) {
                distInfoUpdated = "";
            }
            return distInfoUpdated;
        }
    }
}
