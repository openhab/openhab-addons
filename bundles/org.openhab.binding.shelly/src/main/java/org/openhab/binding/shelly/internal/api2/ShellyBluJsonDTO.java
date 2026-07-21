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

import java.lang.reflect.Type;

import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

/**
 * {@link ShellyBluJsonDTO} includes constants and structures used for BluApi's JSON mapping and processing.
 *
 * @author Markus Michels - Initial contribution
 */
public class ShellyBluJsonDTO {

    // BLU events
    public static final String SHELLY2_BLU_GWSCRIPT = "oh-blu-scanner.js";
    public static final String SHELLY2_EVENT_BLUPREFIX = "oh-blu.";
    public static final String SHELLY2_EVENT_BLUSCAN = SHELLY2_EVENT_BLUPREFIX + "scan_result";
    public static final String SHELLY2_EVENT_BLUDATA = SHELLY2_EVENT_BLUPREFIX + "data";
    public static final String SHELLY2_EVENT_BLUALARM = SHELLY2_EVENT_BLUPREFIX + "alarm";

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

    // Handles BTHome fields that a single-button device sends as a scalar but a multi-button device sends as an array.
    // Without this adapter, a plain Gson instance would throw JsonSyntaxException on scalar payloads for T[] fields.
    static class IntegerArrayAdapter implements JsonDeserializer<Integer[]> {
        @Override
        public Integer[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            if (json.isJsonArray()) {
                return ctx.deserialize(json, Integer[].class);
            }
            return new Integer[] { json.getAsInt() };
        }
    }

    static class DoubleArrayAdapter implements JsonDeserializer<Double[]> {
        @Override
        public Double[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext ctx) {
            if (json.isJsonArray()) {
                return ctx.deserialize(json, Double[].class);
            }
            return new Double[] { json.getAsDouble() };
        }
    }

    public static class Shelly2NotifyBluEventData {
        public static class Shelly2NotifyBluEventDimmer {
            public @Nullable Integer direction;
            public @Nullable Integer steps;
        }

        public @Nullable String packet;
        public @Nullable String addr;
        public @Nullable String name;
        public @Nullable Boolean encryption;
        @SerializedName("code") // oh-blu.alarm: BTH_ENCRYPTED, BTH_UNKNOWN_TYPE
        public @Nullable String alarmCode;
        @SerializedName("BTHome_version")
        public @Nullable Integer bthVersion;
        public @Nullable Integer pid;
        @SerializedName("Battery")
        public @Nullable Integer battery;
        @JsonAdapter(IntegerArrayAdapter.class)
        @SerializedName("Button")
        public @Nullable Integer[] buttons;
        @SerializedName("Illuminance")
        public @Nullable Integer illuminance;
        @SerializedName("Window")
        public @Nullable Integer windowState;
        @SerializedName("Motion")
        public @Nullable Integer motionState;
        @JsonAdapter(DoubleArrayAdapter.class)
        @SerializedName("Temperature")
        public @Nullable Double[] temperatures;
        @SerializedName("Humidity")
        public @Nullable Double humidity;
        @SerializedName("Vibration") // BLU Distance
        public @Nullable Integer vibration;
        @SerializedName("Distance_mm") // BLU Distance
        public @Nullable Double distance;
        @SerializedName("Channel") // BLU Remote
        public @Nullable Integer channel;
        @JsonAdapter(DoubleArrayAdapter.class)
        @SerializedName("Rotation") // BLU Remote
        public @Nullable Double[] rotations;
        @SerializedName("Dimmer") // BLU Remote
        public @Nullable Shelly2NotifyBluEventDimmer dimmer;
        @SerializedName("Moisture") // WS90 rain detection (BTHome 0x20)
        public @Nullable Double rain;
        @SerializedName("Speed") // WS90
        public @Nullable Double[] speeds;
        @SerializedName("UVIndex") // WS90
        public @Nullable Double uvIndex;
        @SerializedName("Pressure") // WS90
        public @Nullable Double pressure;
        @SerializedName("Dewpoint") // WS90
        public @Nullable Double dewPoint;
        @SerializedName("Precipitation") // WS90
        public @Nullable Double precipitation;
        @SerializedName("Direction") // WS90
        public @Nullable Double direction;

        @SerializedName("Firmware32")
        public @Nullable Long firmware32;
        @SerializedName("LightLevel") // BLU ZB: 0=dark, 1=twilight, 2=bright
        public @Nullable Integer lightLevel;
        @SerializedName("BatteryLow") // BLU H&T Display ZB: 1=battery below 15%
        public @Nullable Integer batteryLow;

        public @Nullable Integer rssi;
        @SerializedName("tx_power")
        public @Nullable Integer txPower;
    }
}
