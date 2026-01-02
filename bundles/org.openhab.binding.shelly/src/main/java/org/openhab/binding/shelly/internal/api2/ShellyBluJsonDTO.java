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
package org.openhab.binding.shelly.internal.api2;

import com.google.gson.annotations.SerializedName;

/**
 * {@link ShellyBluJsonDTO} includes contans and structures used for BluApi's JSON mapping and processing.
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyBluJsonDTO {

    // BLU events
    public static final String SHELLY2_BLU_GWSCRIPT = "oh-blu-scanner.js";
    public static final String SHELLY2_EVENT_BLUPREFIX = "oh-blu.";
    public static final String SHELLY2_EVENT_BLUSCAN = SHELLY2_EVENT_BLUPREFIX + "scan_result";
    public static final String SHELLY2_EVENT_BLUDATA = SHELLY2_EVENT_BLUPREFIX + "data";

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
        public static class Shelly2NotifyBluEventDimmer {
            public Integer direction;
            public Integer steps;
        }

        public String packet;
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
        @SerializedName("Motion")
        public Integer motionState;
        @SerializedName("Temperature")
        public Double[] temperatures;
        @SerializedName("Humidity")
        public Double humidity;
        @SerializedName("Vibration") // BLU Distance
        public Integer vibration;
        @SerializedName("Distance_mm") // BLU Distance
        public Double distance;
        @SerializedName("Channel") // BLU Remote
        public Integer channel;
        @SerializedName("Rotation") // BLU Remote
        public Double[] rotations;
        @SerializedName("Dimmer") // BLU Remote
        public Shelly2NotifyBluEventDimmer dimmer;
        @SerializedName("Moisture") // WS90
        public Double moisture;
        @SerializedName("Speed") // WS90
        public Double[] speeds;
        @SerializedName("UVIndex") // WS90
        public Double uvIndex;
        @SerializedName("Pressure") // WS90
        public Double pressure;
        @SerializedName("Dewpoint") // WS90
        public Double dewPoint;
        @SerializedName("Precipitation") // WS90
        public Double precipitation;
        @SerializedName("Direction") // WS90
        public Double direction;

        @SerializedName("Firmware32")
        public Long firmware32;

        public Integer rssi;
        public Integer tx_power;
    }
}
