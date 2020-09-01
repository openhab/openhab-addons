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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.annotations.SerializedName;

/**
 * {@link IntesisHomeJSonDTO} is used for the JSon/GSon mapping
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
public class IntesisHomeJSonDTO {

    public static class response {
        @SerializedName("success")
        public boolean success;
        @SerializedName("data")
        public JsonElement data;
    }

    public static class data {
        @SerializedName("id")
        public JsonElement id;
        @SerializedName("info")
        public JsonElement info;
        @SerializedName("userinfo")
        public JsonElement userinfo;
        @SerializedName("config")
        public JsonElement config;
        @SerializedName("dp")
        public JsonElement dp;
        @SerializedName("dpval")
        public JsonElement dpval;
    }

    public static class id {
        @SerializedName("sessionID")
        public String sessionID; // Session ID
    }

    // Device Information used for thing properties
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

    public static class userinfo {
        @SerializedName("username")
        public String username;
        @SerializedName("servicelist")
        public JsonElement servicelist;
    }

    // List of available services
    public static class servicelist {
        @SerializedName("setconfig")
        public String setconfig;
        @SerializedName("getconfig")
        public String getconfig;
        @SerializedName("getcurrentconfig")
        public String getcurrentconfig;
        @SerializedName("getinfo")
        public String getinfo;
        @SerializedName("login")
        public String login;
        @SerializedName("logout")
        public String logout;
        @SerializedName("passchange")
        public String passchange;
        @SerializedName("getavailabledatapoints")
        public String getavailabledatapoints;
        @SerializedName("setdatapointvalue")
        public String setdatapointvalue;
        @SerializedName("getdatapointvalue")
        public String getdatapointvalue;
        @SerializedName("getavailableservices")
        public String getavailableservices;
        @SerializedName("reboot")
        public String reboot;
        @SerializedName("setdefaults")
        public String setdefaults;
        @SerializedName("getdefaultconfig")
        public String getdefaultconfig;
    }

    public static class config {
        @SerializedName("deviceModel")
        public String deviceModel; // Device Model
        @SerializedName("ip")
        public String ip; // Device IP Address
        @SerializedName("netmask")
        public String netmask; // Device IP Address
        @SerializedName("dfltgw")
        public String dfltgw; // Default gateway
        @SerializedName("dhcp")
        public boolean dhcp; // DHCP enabled
        @SerializedName("ssid")
        public String ssid; // WLAN Access Point
        @SerializedName("security")
        public int security; // Security Type
        @SerializedName("regdomain")
        public int regdomain;
        @SerializedName("lastconfigdatetime")
        public int lastconfigdatetime;
    }

    public static class dp {
        @SerializedName("datapoints")
        public JsonArray datapoints; // dataPoints
    }

    // Array of UIDs with corresponding description for dynamic channel creation
    public static class datapoints {
        @SerializedName("uid")
        public int uid; // dataPoint
        @SerializedName("rw")
        public String rw; // read/write status
        @SerializedName("type")
        public int type;
        @SerializedName("descr")
        public JsonElement descr;
    }

    // Descriptor of dataPoint values
    public static class descr {
        @SerializedName("numStates")
        public int numStates;
        @SerializedName("states")
        public String[] states;
        @SerializedName("maxValue")
        public String maxValue;
        @SerializedName("minValue")
        public String minValue;
    }

    // Array of UIDs with corresponding values, mapped into channel
    public static class dpval {
        @SerializedName("uid")
        public int uid; // ID
        @SerializedName("value")
        public int value;
        @SerializedName("status")
        public int status;
    }
}
