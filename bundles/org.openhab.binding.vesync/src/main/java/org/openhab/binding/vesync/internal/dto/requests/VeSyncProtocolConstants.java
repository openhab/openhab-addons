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
package org.openhab.binding.vesync.internal.dto.requests;

/**
 * The {@link VeSyncProtocolConstants} contains common Strings used by various elements of the protocol.
 *
 * @author David Goodyear - Initial contribution
 */
public interface VeSyncProtocolConstants {

    // Common Payloads
    String MODE_AUTO = "auto";
    String MODE_MANUAL = "manual";
    String MODE_SLEEP = "sleep";

    String MODE_ON = "on";
    String MODE_DIM = "dim";
    String MODE_OFF = "off";

    // Common Commands
    String DEVICE_SET_SWITCH = "setSwitch";
    String DEVICE_SET_DISPLAY = "setDisplay";
    String DEVICE_SET_LEVEL = "setLevel";

    // Humidifier Commands
    String DEVICE_SET_AUTOMATIC_STOP = "setAutomaticStop";
    String DEVICE_SET_HUMIDITY_MODE = "setHumidityMode";
    String DEVICE_SET_TARGET_HUMIDITY_MODE = "setTargetHumidity";
    String DEVICE_SET_VIRTUAL_LEVEL = "setVirtualLevel";
    String DEVICE_SET_NIGHT_LIGHT_BRIGHTNESS = "setNightLightBrightness";
    String DEVICE_GET_HUMIDIFIER_STATUS = "getHumidifierStatus";

    String DEVICE_LEVEL_TYPE_MIST = "mist";

    // Air Purifier Commands
    String DEVICE_SET_PURIFIER_MODE = "setPurifierMode";
    String DEVICE_SET_CHILD_LOCK = "setChildLock";
    String DEVICE_SET_NIGHT_LIGHT = "setNightLight";
    String DEVICE_GET_PURIFIER_STATUS = "getPurifierStatus";
    String DEVICE_LEVEL_TYPE_WIND = "wind";

    /**
     * Base URL for AUTHENTICATION REQUESTS
     */
    String PROTOCOL = "https";
    String HOST_ENDPOINT = PROTOCOL + "://smartapi.vesync.com/cloud";
    String V1_LOGIN_ENDPOINT = HOST_ENDPOINT + "/v1/user/login";
    String V1_MANAGED_DEVICES_ENDPOINT = HOST_ENDPOINT + "/v1/deviceManaged/devices";
    String V2_BYPASS_ENDPOINT = HOST_ENDPOINT + "/v2/deviceManaged/bypassV2";
}
