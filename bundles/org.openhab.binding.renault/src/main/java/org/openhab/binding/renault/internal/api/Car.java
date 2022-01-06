/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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
package org.openhab.binding.renault.internal.api;

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

    private boolean disableLocation = false;
    private boolean disableBattery = false;
    private boolean disableCockpit = false;
    private boolean disableHvac = false;

    private @Nullable Double batteryLevel;
    private @Nullable Boolean hvacstatus;
    private @Nullable Double odometer;
    private @Nullable String imageURL;
    private @Nullable Double gpsLatitude;
    private @Nullable Double gpsLongitude;

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
            logger.warn("Error {} parsing Location: {}", e.getMessage(), responseJson);
        }
    }

    public void setDetails(JsonObject responseJson) {
        try {
            if (responseJson.get("assets") != null) {
                JsonArray assetsJson = responseJson.get("assets").getAsJsonArray();
                String url = null;
                for (JsonElement asset : assetsJson) {
                    if (asset.getAsJsonObject().get("assetType") != null
                            && asset.getAsJsonObject().get("assetType").getAsString().equals("PICTURE")) {
                        if (asset.getAsJsonObject().get("renditions") != null) {
                            JsonArray renditions = asset.getAsJsonObject().get("renditions").getAsJsonArray();
                            for (JsonElement rendition : renditions) {
                                if (rendition.getAsJsonObject().get("resolutionType") != null
                                        && rendition.getAsJsonObject().get("resolutionType").getAsString()
                                                .equals("ONE_MYRENAULT_SMALL")) {
                                    url = rendition.getAsJsonObject().get("url").getAsString();
                                    break;
                                }
                            }
                        }
                    }
                    if (url != null && !url.isEmpty()) {
                        imageURL = url;
                        break;
                    }
                }
            }
        } catch (IllegalStateException | ClassCastException e) {
            logger.warn("Error {} parsing Details: {}", e.getMessage(), responseJson);
        }
    }

    public boolean isDisableLocation() {
        return disableLocation;
    }

    public void setDisableLocation(boolean disableLocation) {
        this.disableLocation = disableLocation;
    }

    public boolean isDisableBattery() {
        return disableBattery;
    }

    public void setDisableBattery(boolean disableBattery) {
        this.disableBattery = disableBattery;
    }

    public boolean isDisableCockpit() {
        return disableCockpit;
    }

    public void setDisableCockpit(boolean disableCockpit) {
        this.disableCockpit = disableCockpit;
    }

    public boolean isDisableHvac() {
        return disableHvac;
    }

    public void setDisableHvac(boolean disableHvac) {
        this.disableHvac = disableHvac;
    }

    public @Nullable Double getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Double batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public @Nullable Boolean getHvacstatus() {
        return hvacstatus;
    }

    public void setHvacstatus(Boolean hvacstatus) {
        this.hvacstatus = hvacstatus;
    }

    public @Nullable Double getOdometer() {
        return odometer;
    }

    public void setOdometer(Double odometer) {
        this.odometer = odometer;
    }

    public @Nullable String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public @Nullable Double getGpsLatitude() {
        return gpsLatitude;
    }

    public void setGpsLatitude(Double gpsLatitude) {
        this.gpsLatitude = gpsLatitude;
    }

    public @Nullable Double getGpsLongitude() {
        return gpsLongitude;
    }

    public void setGpsLongitude(Double gpsLongitude) {
        this.gpsLongitude = gpsLongitude;
    }

    private @Nullable JsonObject getAttributes(JsonObject responseJson)
            throws IllegalStateException, ClassCastException {
        if (responseJson.get("data") != null && responseJson.get("data").getAsJsonObject().get("attributes") != null) {
            return responseJson.get("data").getAsJsonObject().get("attributes").getAsJsonObject();
        }
        return null;
    }
}
