/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wiz.internal.enums;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * This enum represents the available WiZ Request Methods
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
@NonNullByDefault
public enum WizMethodType {
    /**
     * Registration - used to "register" with the bulb: This notifies the bult that
     * it you want it to send you heartbeat sync packets.
     * NOTE: The homeId value is optional, other values are required
     * NOTE: There is no need to register before calling other methods.
     * Example Request:
     * {"method": "registration", "id": 1, "params":
     * {"phoneIp": "10.0.0.xxx", "register": true, "homeId": xxxxxx, "phoneMac": "macOfopenHAB"}}
     * Example Response:
     * {"method": "registration", "id": 1, "env": "pro", "result":
     * {"mac": "macOfopenHAB", "success": true}}
     */
    @SerializedName("registration")
    Registration("registration"),
    /**
     * Pulse - tells the bulb to briely change brightness (by delta % for duration ms)
     * Example Request:
     * {"method": "pulse", "id": 22, "params": {"delta": -30, "duration": 900}}
     * Example Response:
     * {"method": "pulse", "id": 22, "env": "pro", "result": {"success": true}}
     */
    @SerializedName("pulse")
    Pulse("pulse"),
    /**
     * setPilot - used to tell the bulb to change color/temp/state
     * Example Request:
     * {"method": "setPilot", "id": 24, "params": {"state": 1}}
     * Example Response:
     * {"method": "setPilot", "id": 24, "env": "pro", "result": {"success": true}}
     */
    @SerializedName("setPilot")
    SetPilot("setPilot"),
    /**
     * getPilot - gets the current bulb state - no paramters need to be included
     * Example Request:
     * {"method": "getPilot", "id": 24}
     * Example Response:
     * {"method": "getPilot", "id": 22, "env": "pro", "result": {"mac":
     * "a8bb508f570a", "rssi":-76, "state": true, "sceneId": 0, "temp": 2700,
     * "dimming": 42, "schdPsetId": 5}}
     */
    @SerializedName("getPilot")
    GetPilot("getPilot"),
    /**
     * syncPilot - sent by the bulb as heart-beats
     * Example:
     * {"method": "syncPilot", "id": 218, "env": "pro", "params":
     * { "mac": "theBulbMacAddress", "rssi": -72, "src": "udp", "state": true, "sceneId": 0,
     * "temp": 3362, "dimming": 69, "schdPsetId": 5}}
     * Another Example:
     * {"method": "syncPilot", "id": 219, "env": "pro", "params":
     * { "mac": "theBulbMacAddress", "rssi": -72, "src": "hb", "mqttCd": 0, "state": true,
     * "sceneId": 0, "temp": 3362, "dimming": 69, "schdPsetId": 5}}
     */
    @SerializedName("syncPilot")
    SyncPilot("syncPilot"),
    /**
     * getModelConfig - gets more details on the bulb
     */
    @SerializedName("getModelConfig")
    GetModelConfig("getModelConfig"),
    /**
     * getSystemConfig - gets the current system configuration - no paramters need
     * to be included
     * Example Request:
     * {"method": "getSystemConfig", "id": 24}
     * Example Response:
     * {"method": "getSystemConfig", "id": 22, "env": "pro",
     * "result": {"mac": "theBulbMacAddress", "homeId": xxxxxx, "roomId": xxxxxx,
     * "homeLock": false, "pairingLock": false, "typeId": 0, "moduleName":
     * "ESP01_SHRGB1C_31", "fwVersion": "1.15.2", "groupId": 0, "drvConf":[33,1]}}
     */
    @SerializedName("getSystemConfig")
    GetSystemConfig("getSystemConfig"),
    /**
     * setSystemConfig - presumably sets up the system
     * I have NOT attempted to call this method
     */
    @SerializedName("setSystemConfig")
    SetSystemConfig("setSystemConfig"),
    /**
     * getWifiConfig - gets the current wifi configuration - no paramters need to be
     * included
     * Example Request:
     * {"id": 22, "method": "getWifiConfig"}
     * Example Response:
     * {"method": "getWifiConfig", "id": 22, "env": "pro", "result":
     * {:["encryptedString"]}
     */
    @SerializedName("getWifiConfig")
    GetWifiConfig("getWifiConfig"),
    /**
     * setWifiConfig - presumably sets up the system I have NOT attempted to use this method
     */
    @SerializedName("setWifiConfig")
    SetWifiConfig("setWifiConfig"),
    /**
     * firstBeat - set by a bulb upon power up
     * Example:
     * {"method": "firstBeat", "id": 0, "env": "pro", "params":
     * {"mac": "theBulbMacAddress", "homeId": xxxxxx, "fwVersion": "1.15.2"}}
     */
    @SerializedName("firstBeat")
    FirstBeat("firstBeat"),
    /**
     * Unknown - using as a default for inproperly received responses
     */
    UnknownMethod("unknownMethod");

    private final String methodName;

    private WizMethodType(final String methodName) {
        this.methodName = methodName;
    }

    /**
     * Gets the method name for request method
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }
}
