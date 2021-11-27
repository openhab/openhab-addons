/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
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
package org.openhab.binding.renault.internal.renault.api;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * MyRenault registered car for parsing HTTP responses and collecting data and
 * information.
 * 
 * @author Doug Culnane - Initial contribution
 */
@NonNullByDefault
public class Car {

    private final Logger logger = LoggerFactory.getLogger(Car.class);

    public Double batteryLevel = Double.valueOf(-1);
    public Boolean hvacstatus = false;
    public Double odometer = Double.valueOf(0);
    public String imageURL = "";
    public Double gpsLatitude = Double.valueOf(0);
    public Double gpsLongitude = Double.valueOf(0);

    public void setBatteryStatus(JsonObject responseJson) {
        try {
            JsonObject attributes = getAttributes(responseJson);
            if (attributes != null && attributes.get("batteryLevel") != null) {
                batteryLevel = attributes.get("batteryLevel").getAsDouble();
            }
        } catch (IllegalStateException | ClassCastException e) {
            logger.warn("Error {} parsing Battery Status: {}", e.getMessage(), responseJson);
        }
    }

    public void setHVACStatus(JsonObject responseJson) {
        try {
            JsonObject attributes = getAttributes(responseJson);
            if (attributes != null && attributes.get("hvacStatus") != null) {
                hvacstatus = attributes.get("hvacStatus").getAsString().equals("on");
            }
        } catch (IllegalStateException | ClassCastException e) {
            logger.warn("Error {} parsing HVAC Status: {}", e.getMessage(), responseJson);
        }
    }

    public void setCockpit(JsonObject responseJson) {
        try {
            JsonObject attributes = getAttributes(responseJson);
            if (attributes != null && attributes.get("totalMileage") != null) {
                odometer = attributes.get("totalMileage").getAsDouble();
            }
        } catch (IllegalStateException | ClassCastException e) {
            logger.warn("Error {} parsing Cockpit: {}", e.getMessage(), responseJson);
        }
    }

    public void setLocation(JsonObject responseJson) {
        try {
            JsonObject attributes = getAttributes(responseJson);
            if (attributes != null) {
                if (attributes.get("gpsLatitude") != null) {
                    gpsLatitude = attributes.get("gpsLatitude").getAsDouble();
                }
                if (attributes.get("gpsLongitude") != null) {
                    gpsLongitude = attributes.get("gpsLongitude").getAsDouble();
                }
            }
        } catch (IllegalStateException | ClassCastException e) {
            logger.warn("Error {} parsing Cockpit: {}", e.getMessage(), responseJson);
        }
    }

    public void setDetails(JsonObject responseJson) {
        try {
            if (responseJson.get("assets") != null) {
                JsonArray assetsJson = responseJson.get("assets").getAsJsonArray();
                for (JsonElement asset : assetsJson) {
                    if (asset.getAsJsonObject().get("assetType") != null
                            && asset.getAsJsonObject().get("assetType").getAsString().equals("PICTURE")) {
                        if (asset.getAsJsonObject().get("renditions") != null) {
                            JsonArray renditions = asset.getAsJsonObject().get("renditions").getAsJsonArray();
                            for (JsonElement rendition : renditions) {
                                if (rendition.getAsJsonObject().get("resolutionType") != null
                                        && rendition.getAsJsonObject().get("resolutionType").getAsString()
                                                .equals("ONE_MYRENAULT_SMALL")) {
                                    imageURL = rendition.getAsJsonObject().get("url").getAsString();
                                    break;
                                }
                            }
                        }
                    }
                    if (imageURL.length() > 0) {
                        break;
                    }
                }
            }
        } catch (IllegalStateException | ClassCastException e) {
            logger.warn("Error {} parsing Details: {}", e.getMessage(), responseJson);
        }
    }

    private @Nullable JsonObject getAttributes(JsonObject responseJson)
            throws IllegalStateException, ClassCastException {
        if (responseJson.get("data") != null && responseJson.get("data").getAsJsonObject().get("attributes") != null) {
            return responseJson.get("data").getAsJsonObject().get("attributes").getAsJsonObject();
        }
        return null;
    }
}
