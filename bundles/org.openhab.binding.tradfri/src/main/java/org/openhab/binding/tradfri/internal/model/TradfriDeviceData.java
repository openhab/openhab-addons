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
package org.openhab.binding.tradfri.internal.model;

import static org.openhab.binding.tradfri.internal.TradfriBindingConstants.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

/**
 * The {@link TradfriDeviceData} class is a Java wrapper for the raw JSON data about the device state.
 *
 * @author Kai Kreuzer - Initial contribution
 * @author Christoph Weitkamp - Restructuring and refactoring of the binding
 */
public abstract class TradfriDeviceData {

    private final Logger logger = LoggerFactory.getLogger(TradfriDeviceData.class);

    protected JsonObject root;
    protected JsonArray array;
    protected JsonObject attributes;
    protected JsonObject generalInfo;

    public TradfriDeviceData(String attributesNodeName) {
        root = new JsonObject();
        array = new JsonArray();
        attributes = new JsonObject();
        array.add(attributes);
        root.add(attributesNodeName, array);
        generalInfo = new JsonObject();
        root.add(DEVICE, generalInfo);
    }

    public TradfriDeviceData(String attributesNodeName, JsonElement json) {
        try {
            root = json.getAsJsonObject();
            array = root.getAsJsonArray(attributesNodeName);
            attributes = array.get(0).getAsJsonObject();
            generalInfo = root.getAsJsonObject(DEVICE);
        } catch (JsonSyntaxException e) {
            logger.error("JSON error: {}", e.getMessage(), e);
        }
    }

    public Integer getDeviceId() {
        return root.get(INSTANCE_ID).getAsInt();
    }

    public boolean getReachabilityStatus() {
        if (root.get(REACHABILITY_STATE) != null) {
            return root.get(REACHABILITY_STATE).getAsInt() == 1;
        } else {
            return false;
        }
    }

    public String getFirmwareVersion() {
        if (generalInfo.get(DEVICE_FIRMWARE) != null) {
            return generalInfo.get(DEVICE_FIRMWARE).getAsString();
        } else {
            return null;
        }
    }

    public String getModelId() {
        if (generalInfo.get(DEVICE_MODEL) != null) {
            return generalInfo.get(DEVICE_MODEL).getAsString();
        } else {
            return null;
        }
    }

    public String getVendor() {
        if (generalInfo.get(DEVICE_VENDOR) != null) {
            return generalInfo.get(DEVICE_VENDOR).getAsString();
        } else {
            return null;
        }
    }
}
