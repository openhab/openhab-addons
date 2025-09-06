/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

package org.openhab.binding.shelly.internal.api2;

import com.google.gson.annotations.SerializedName;

/**
 * {@link ShellyBluEventDataDTO} implements BLU data format
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyBluEventDataDTO {
    // BTHome samples
    // BLU Button 1
    // {"component":"script:2", "id":2, "event":"oh-blu.scan_result",
    // "data":{"addr":"bc:02:6e:c3:a6:c7","rssi":-62,"tx_power":-128}, "ts":1682877414.21}
    // {"component":"script:2", "id":2, "event":"oh-blu.data",
    // "data":{"encryption":false,"BTHome_version":2,"pid":205,"Battery":100,"Button":1,"addr":"b4:35:22:fd:b3:81","rssi":-68},
    // "ts":1682877399.22}
    //
    // BLU Door Window
    // {"component":"script:2", "id":2, "event":"oh-blu.scan_result",
    // "data":{"addr":"bc:02:6e:c3:a6:c7","rssi":-62,"tx_power":-128}, "ts":1682877414.21}
    // {"component":"script:2", "id":2, "event":"oh-blu.data",
    // "data":{"encryption":false,"BTHome_version":2,"pid":38,"Battery":100,"Illuminance":0,"Window":1,"Rotation":0,"addr":"bc:02:6e:c3:a6:c7","rssi":-62},
    // "ts":1682877414.25}

    public static class Shelly2NotifyBluEventData {
        public String addr;
        public String name;
        public Boolean encryption;
        @SerializedName("BTHome_version")
        public Integer bthVersion;
        public Integer pid;
        @SerializedName("Battery")
        public Integer battery;
        @SerializedName("Button")
        public Integer[] buttons;
        @SerializedName("Illuminance")
        public Integer illuminance;
        @SerializedName("Window")
        public Integer windowState;
        @SerializedName("Rotation")
        public Double rotation;
        @SerializedName("Motion")
        public Integer motionState;
        @SerializedName("Temperature")
        public Double[] temperatures;
        @SerializedName("Humidity")
        public Double humidity;
        @SerializedName("Firmware32") // BLU Remote
        public Long firmware;

        public Integer rssi;
        public Integer tx_power;
    }
}
