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
package org.openhab.binding.intesis.internal.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.annotations.SerializedName;

/**
 * {@link IntesisHomeJSonDTO} translates Json into Java objects
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
public class IntesisHomeJSonDTO {

    public static class info {
        @SerializedName("wlanSTAMAC")
        public String wlanSTAMAC; // Device Client MAC Address
        @SerializedName("wlanAPMAC")
        public String wlanAPMAC; // Device Access Point MAC Address
        @SerializedName("fwVersion")
        public String fwVersion; // Device Firmware Version
        @SerializedName("wlanFwVersion")
        public String wlanFwVersion; // Wireless Firmware Version
        @SerializedName("acStatus")
        public String acStatus; // Air Conditioner Communication Status
        @SerializedName("wlanLNK")
        public String wlanLNK; // Connection Status with Wireless Network
        @SerializedName("ssid")
        public String ssid; // Wireless Network SSID
        @SerializedName("rssi")
        public String rssi; // Wireless Signal Strength
        @SerializedName("tcpServerLNK")
        public String tcpServerLNK; // Cloud Server Connection (Not used for communication here)
        @SerializedName("localdatetime")
        public String localdatetime; // Local Date Time
        @SerializedName("powerStatus")
        public String powerStatus;
        @SerializedName("lastconfigdatetime")
        public String lastconfigdatetime; // Last Configuration Date Time
        @SerializedName("deviceModel")
        public String deviceModel; // Device Model
        @SerializedName("sn")
        public String sn; // Serial Number
        @SerializedName("lastError")
        public String lastError;
    }

    public static class AuthenticateData {
        @SerializedName("id")
        public String id; // ID
        @SerializedName("sessionID")
        public String sessionID; // Session ID
    }

    public static class dpval {
        @SerializedName("uid")
        public String uid; // ID
        @SerializedName("value")
        public int value;
        @SerializedName("status")
        public int status;
    }

    public static JsonObject getData(String response) {
        JsonParser parser = new JsonParser();
        JsonElement rootNode = parser.parse(response);
        JsonObject details = rootNode.getAsJsonObject();
        // JsonElement successNode = details.get("success");
        JsonElement dataNode = details.get("data");
        JsonObject data = dataNode.getAsJsonObject();
        return data;
    }

    public static boolean getSuccess(String response) {
        JsonParser parser = new JsonParser();
        JsonElement rootNode = parser.parse(response);
        JsonObject details = rootNode.getAsJsonObject();
        boolean success = details.get("success") != null;
        return success;
    }
}
