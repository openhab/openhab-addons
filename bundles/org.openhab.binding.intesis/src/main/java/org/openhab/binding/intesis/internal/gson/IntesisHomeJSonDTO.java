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
package org.openhab.binding.intesis.internal.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

/**
 * {@link IntesisHomeJSonDTO} is used for the JSon/GSon mapping
 *
 * @author Hans-Jörg Merk - Initial contribution
 */
public class IntesisHomeJSonDTO {

    public static class Response {
        public boolean success;
        public JsonElement data;
    }

    public static class Data {
        public JsonElement id;
        public JsonElement info;
        public JsonElement userinfo;
        public JsonElement config;
        public JsonElement dp;
        public JsonElement dpval;
    }

    public static class Id {
        public String sessionID; // Session ID
    }

    // Device Information used for thing properties
    public static class Info {
        public String wlanSTAMAC; // Device Client MAC Address
        public String wlanAPMAC; // Device Access Point MAC Address
        public String fwVersion; // Device Firmware Version
        public String wlanFwVersion; // Wireless Firmware Version
        public String acStatus; // Air Conditioner Communication Status
        public String wlanLNK; // Connection Status with Wireless Network
        public String ssid; // Wireless Network SSID
        public String rssi; // Wireless Signal Strength
        public String tcpServerLNK; // Cloud Server Connection (Not used for communication here)
        public String localdatetime; // Local Date Time
        public String powerStatus;
        public String lastconfigdatetime; // Last Configuration Date Time
        public String deviceModel; // Device Model
        public String sn; // Serial Number
        public String lastError;
    }

    public static class Userinfo {
        public String username;
        public JsonElement servicelist;
    }

    // List of available services
    public static class Servicelist {
        public String setconfig;
        public String getconfig;
        public String getcurrentconfig;
        public String getinfo;
        public String login;
        public String logout;
        public String passchange;
        public String getavailabledatapoints;
        public String setdatapointvalue;
        public String getdatapointvalue;
        public String getavailableservices;
        public String reboot;
        public String setdefaults;
        public String getdefaultconfig;
    }

    public static class Config {
        public String deviceModel; // Device Model
        public String ip; // Device IP Address
        public String netmask; // Device IP Address
        public String dfltgw; // Default gateway
        public boolean dhcp; // DHCP enabled
        public String ssid; // WLAN Access Point
        public int security; // Security Type
        public int regdomain;
        public int lastconfigdatetime;
    }

    public static class Dp {
        public JsonArray datapoints; // dataPoints
    }

    // Array of UIDs with corresponding description for dynamic channel creation
    public static class Datapoints {
        public int uid; // dataPoint
        public String rw; // read/write status
        public int type;
        public JsonElement descr;
    }

    // Descriptor of dataPoint values
    public static class Descr {
        public int numStates;
        public String[] states;
        public String maxValue;
        public String minValue;
    }

    // Array of UIDs with corresponding values, mapped into channel
    public static class Dpval {
        public int uid; // ID
        public int value;
        public int status;
    }
}
