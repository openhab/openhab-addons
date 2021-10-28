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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * @author Doug Culnane - Initial contribution
 */
public class Car {

    private final Logger logger = LoggerFactory.getLogger(Car.class);

    public Double batteryLevel;
    public Boolean hvacstatus;
    public Double odometer;
    public String imageURL;
    public Double gpsLatitude;
    public Double gpsLongitude;

    public void setBatteryStatus(JsonObject responseJson) {
        try {
            batteryLevel = responseJson.get("data").getAsJsonObject().get("attributes").getAsJsonObject()
                    .get("batteryLevel").getAsDouble();
        } catch (Exception e) {
            logger.error("Error {} parsing Battery Status: {}", e, responseJson);
        }
    }

    public void setHVACStatus(JsonObject responseJson) {
        try {
            hvacstatus = responseJson.get("data").getAsJsonObject().get("attributes").getAsJsonObject()
                    .get("hvacStatus").getAsString().equals("on");
        } catch (Exception e) {
            logger.error("Error {} parsing HVAC Status: {}", e, responseJson);
        }
    }

    public void setCockpit(JsonObject responseJson) {
        try {
            odometer = responseJson.get("data").getAsJsonObject().get("attributes").getAsJsonObject()
                    .get("totalMileage").getAsDouble();
        } catch (Exception e) {
            logger.error("Error {} parsing Cockpit: {}", e, responseJson);
        }
    }

    public void setLocation(JsonObject responseJson) {
        try {
            gpsLatitude = responseJson.get("data").getAsJsonObject().get("attributes").getAsJsonObject()
                    .get("gpsLatitude").getAsDouble();
            gpsLongitude = responseJson.get("data").getAsJsonObject().get("attributes").getAsJsonObject()
                    .get("gpsLongitude").getAsDouble();
        } catch (Exception e) {
            logger.error("Error {} parsing Cockpit: {}", e, responseJson);
        }
    }

    public void setDetails(JsonObject responseJson) {
        try {
            JsonArray assetsJson = responseJson.get("assets").getAsJsonArray();
            for (JsonElement asset : assetsJson) {
                if (asset.getAsJsonObject().get("assetType").getAsString().equals("PICTURE")) {
                    JsonArray renditions = asset.getAsJsonObject().get("renditions").getAsJsonArray();
                    for (JsonElement rendition : renditions) {
                        if (rendition.getAsJsonObject().get("resolutionType").getAsString()
                                .equals("ONE_MYRENAULT_SMALL")) {
                            imageURL = rendition.getAsJsonObject().get("url").getAsString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error {} parsing Details: {}", e, responseJson);
        }
    }
}
