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
import org.openhab.binding.shelly.internal.api2.dto.Shelly2StatusJsonDTO.Shelly2DeviceStatus.Shelly2DeviceStatusSysAvlUpdate;

import com.google.gson.annotations.SerializedName;

/**
 * {@link ShellyLoraJsonDTO} contains the JSON structures of the LoRa add-on component.
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyLoraJsonDTO {

    public static class Shelly2DeviceConfigLora {
        public @Nullable Integer id;
        @SerializedName("band_plan")
        public @Nullable String bandPlan;
        public @Nullable Long freq;
        public @Nullable Integer bw;
        public @Nullable Integer dr;
        public @Nullable Integer cr;
        public @Nullable Integer plen;
        public @Nullable Integer txp;
        @SerializedName("rx_enable")
        public @Nullable Boolean rxEnabled;
    }

    public static class Shelly2DeviceStatusLora {
        public @Nullable Integer id;
        @SerializedName("bytes_recd")
        public @Nullable Long rxBytes;
        @SerializedName("bytes_sent")
        public @Nullable Long txBytes;
        @SerializedName("send_fails")
        public @Nullable Long txErrors;
        @SerializedName("air_time_hr_ms")
        public @Nullable Long airtime;
        @SerializedName("fw_version")
        public @Nullable String fw;
        @SerializedName("available_updates")
        public @Nullable Shelly2DeviceStatusSysAvlUpdate availableUpdates;
        public @Nullable ArrayList<String> errors;
        public @Nullable ArrayList<String> flags;
    }

    public static class Shelly2NotifyEventLoraInfo {
        public @Nullable String data;
        public @Nullable String sender; // user_rx only
        public @Nullable Double rssi;
        public @Nullable Double snr;
        public @Nullable Long tsu;
    }
}
