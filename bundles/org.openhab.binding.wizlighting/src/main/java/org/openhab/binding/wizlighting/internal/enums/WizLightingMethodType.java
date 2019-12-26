/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.wizlighting.internal.enums;

/**
 * This enum represents the available WiZ Lighting Request Methods
 *
 * @author Sriram Balakrishnan - Initial contribution
 *
 */
public enum WizLightingMethodType {
    /**
     * Registration - used to "register" with the bulb: This notifies the bult that
     * it you want it to send you heartbeat sync packets.
     * Example Request:
     * {"method": "registration", "id": 1, "params":
     * {"phoneIp": "10.0.0.xxx", "register": true, "homeId": xxxxxx, "phoneMac": "macOfOpenHab"}}
     * Example Response:
     * {"method": "registration", "id": 1, "env": "pro", "result":
     * {"mac":"macOfOpenHab","success":true}}
     */
    registration("registration"),
    /**
     * Pulse - uncertain of purpose
     * Example Request:
     * {"method": "pulse", "id": 22, "params": {"delta": -30, "duration": 900}}
     * Example Response:
     * {"method": "pulse", "id": 22, "env": "pro", "result": {"success": true}}
     */
    pulse("pulse"),
    /**
     * setPilot - used to tell the bulb to change color/temp/state
     * Example Request:
     * {"method": "setPilot", "id": 24, "params": {"state": 1}}
     * Example Response:
     * {"method": "setPilot", "id": 24, "env":"pro", "result": {"success": true}}
     */
    setPilot("setPilot"),
    /**
     * syncPilot - sent by the bulb as heart-beats
     * Example:
     * {"method": "syncPilot", "id": 218, "env": "pro", "params":
     * { "mac": "bulbMac", "rssi": -72, "src":  "udp", "state": true, "sceneId": 0,
     * "temp": 3362, "dimming": 69, "schdPsetId": 5}}
     * Another Example:
     * {"method": "syncPilot", "id": 219, "env": "pro", "params":
     * { "mac": "bulbMac", "rssi": -72, "src": "hb", "mqttCd": 0, "state": true,
     * "sceneId": 0, "temp": 3362, "dimming": 69, "schdPsetId": 5}}
     */
    syncPilot("syncPilot"),
    /**
     * setSystemConfig - no clue, I've never seen this command
     * */
    setSystemConfig("setSystemConfig"),
    /**
     * firstBeat - set by a bulb upon power up
     * Example:
     * {"method": "firstBeat", "id": 0, "env": "pro", "params":
     * {"mac": "bulbMac", "homeId": xxxxxx, "fwVersion": "1.15.2"}}
     */
    firstBeat("firstBeat");

    private String method;

    private WizLightingMethodType(final String method) {
        this.method = method;
    }

    /**
     * Gets the method name for request method
     *
     * @return the method name
     */
    public String getMethod() {
        return method;
    }
}
