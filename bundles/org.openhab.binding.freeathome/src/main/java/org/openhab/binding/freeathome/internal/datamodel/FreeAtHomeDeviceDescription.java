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
package org.openhab.binding.freeathome.internal.datamodel;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * The {@link FreeAtHomeDeviceDescription} is responsible for determining the device type
 * based on the received json string
 *
 * @author Andras Uhrin - Initial contribution
 *
 */
@NonNullByDefault
public class FreeAtHomeDeviceDescription {

    private final Logger logger = LoggerFactory.getLogger(FreeAtHomeDeviceDescription.class);

    // interface strings
    public static final String DEVICE_INTERFACE_UNKNOWN_TYPE = "unknown";
    public static final String DEVICE_INTERFACE_WIRELESS_TYPE = "wireless";
    public static final String DEVICE_INTERFACE_VIRTUAL_TYPE = "virtual";
    public static final String DEVICE_INTERFACE_WIRED_TYPE = "wired";
    public static final String DEVICE_INTERFACE_HUE_TYPE = "hue";

    public String deviceLabel = "";
    public String deviceId = "";
    public String interfaceType = "";
    public boolean validDevice = false;

    private boolean sceneIsDetected;
    private boolean ruleIsDetected;

    public List<FreeAtHomeDeviceChannel> listOfChannels = new ArrayList<>();

    public FreeAtHomeDeviceDescription() {
        validDevice = false;
    }

    public FreeAtHomeDeviceDescription(JsonObject jsonObject, String id) {
        // set the device ID
        deviceId = id;

        // set the device invalid at first
        validDevice = false;

        sceneIsDetected = id.toLowerCase().startsWith("ffff48");
        ruleIsDetected = id.toLowerCase().startsWith("ffff4a");

        JsonObject jsonObjectOfId = jsonObject.getAsJsonObject(id);

        if (jsonObjectOfId == null) {
            return;
        }

        JsonElement jsonObjectOfInterface = jsonObjectOfId.get("interface");

        if (jsonObjectOfInterface != null) {
            String interfaceString = jsonObjectOfInterface.getAsString();

            if (interfaceString.toLowerCase().startsWith("vdev:")) {
                interfaceType = DEVICE_INTERFACE_VIRTUAL_TYPE;
            } else if (interfaceString.toLowerCase().startsWith("hue")) {
                interfaceType = DEVICE_INTERFACE_HUE_TYPE;
            } else if (interfaceString.toLowerCase().startsWith("rf")) {
                interfaceType = DEVICE_INTERFACE_WIRELESS_TYPE;
            } else if (interfaceString.toLowerCase().startsWith("tp")) {
                interfaceType = DEVICE_INTERFACE_WIRED_TYPE;
            } else {
                interfaceType = DEVICE_INTERFACE_UNKNOWN_TYPE;
            }
        } else {
            interfaceType = DEVICE_INTERFACE_UNKNOWN_TYPE;
        }

        JsonElement jsonObjectOfDeviceLabel = jsonObjectOfId.get("displayName");

        if (jsonObjectOfDeviceLabel == null) {
            this.deviceLabel = "NoName";
        } else {
            this.deviceLabel = jsonObjectOfDeviceLabel.getAsString();
        }

        if (this.deviceLabel.isEmpty()) {
            this.deviceLabel = "NoName";
        }

        JsonObject jsonObjectOfChannels = jsonObjectOfId.getAsJsonObject("channels");

        logger.debug("Detecting device features - device id: {} - device label: {}", this.deviceId, this.deviceLabel);

        if (jsonObjectOfChannels != null) {
            // Scan channels for functions
            for (String nextChannel : jsonObjectOfChannels.keySet()) {
                FreeAtHomeDeviceChannel newChannel = new FreeAtHomeDeviceChannel();

                if (newChannel.createChannelFromJson(deviceLabel, nextChannel, jsonObjectOfChannels, sceneIsDetected,
                        ruleIsDetected)) {
                    if (interfaceType == DEVICE_INTERFACE_VIRTUAL_TYPE) {
                        newChannel.applyChangesForVirtualDevice();
                    }

                    listOfChannels.add(newChannel);
                }
            }
        }
    }

    public boolean isRule() {
        return ruleIsDetected;
    }

    public boolean isScene() {
        return sceneIsDetected;
    }

    public boolean isVirtual() {
        return interfaceType == DEVICE_INTERFACE_VIRTUAL_TYPE;
    }

    public int getNumberOfChannels() {
        return listOfChannels.size();
    }

    public FreeAtHomeDeviceChannel getChannel(int idx) {
        return listOfChannels.get(idx);
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getDeviceLabel() {
        return deviceLabel;
    }
}
