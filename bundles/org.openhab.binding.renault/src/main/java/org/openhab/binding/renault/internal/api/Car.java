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
package org.openhab.binding.renault.internal.api;

import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

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

    public static final String HVAC_STATUS_ON = "ON";
    public static final String HVAC_STATUS_OFF = "OFF";
    public static final String HVAC_STATUS_PENDING = "PENDING";

    private final Logger logger = LoggerFactory.getLogger(Car.class);

    private boolean disableLocation = false;
    private boolean disableBattery = false;
    private boolean disableCockpit = false;
    private boolean disableHvac = false;
    private boolean disableLockStatus = false;

    private boolean pausemode = false;
    private ChargingStatus chargingStatus = ChargingStatus.UNKNOWN;
    private ChargingMode chargingMode = ChargingMode.UNKNOWN;
    private PlugStatus plugStatus = PlugStatus.UNKNOWN;
    private double hvacTargetTemperature = 20.0;
    private @Nullable Double batteryLevel;
    private @Nullable Double batteryAvailableEnergy;
    private @Nullable ZonedDateTime batteryStatusUpdated;
    private @Nullable Integer chargingRemainingTime;
    private @Nullable Boolean hvacstatus;
    private @Nullable Double odometer;
    private @Nullable Double estimatedRange;
    private @Nullable String imageURL;
    private @Nullable ZonedDateTime locationUpdated;
    private LockStatus lockStatus = LockStatus.UNKNOWN;
    private @Nullable Double gpsLatitude;
    private @Nullable Double gpsLongitude;
    private @Nullable Double externalTemperature;

    public enum ChargingMode {
        UNKNOWN,
        SCHEDULE_MODE,
        ALWAYS_CHARGING
    }

    public enum ChargingStatus {
        NOT_IN_CHARGE,
        WAITING_FOR_A_PLANNED_CHARGE,
        CHARGE_ENDED,
        WAITING_FOR_CURRENT_CHARGE,
        ENERGY_FLAP_OPENED,
        CHARGE_IN_PROGRESS,
        CHARGE_ERROR,
        UNAVAILABLE,
        UNKNOWN
    }

    public enum LockStatus {
        LOCKED,
        UNLOCKED,
        UNKNOWN
    }

    public enum PlugStatus {
        UNPLUGGED,
        PLUGGED,
        PLUG_ERROR,
        PLUG_UNKNOWN,
        UNKNOWN
    }

    public void setBatteryStatus(JsonObject responseJson) {
        try {
            JsonObject attributes = getAttributes(responseJson);
            if (attributes != null) {
                if (attributes.get("batteryLevel") != null) {
                    batteryLevel = attributes.get("batteryLevel").getAsDouble();
                }
                if (attributes.get("batteryAutonomy") != null) {
                    estimatedRange = attributes.get("batteryAutonomy").getAsDouble();
                }
                if (attributes.get("plugStatus") != null) {
                    plugStatus = mapPlugStatus(attributes.get("plugStatus").getAsString());
                }
                if (attributes.get("chargingStatus") != null) {
                    chargingStatus = mapChargingStatus(attributes.get("chargingStatus").getAsString());
                }
                if (attributes.get("batteryAvailableEnergy") != null) {
                    batteryAvailableEnergy = attributes.get("batteryAvailableEnergy").getAsDouble();
                }
                if (attributes.get("chargingRemainingTime") != null) {
                    chargingRemainingTime = attributes.get("chargingRemainingTime").getAsInt();
                }
                if (attributes.get("timestamp") != null) {
                    try {
                        batteryStatusUpdated = ZonedDateTime.parse(attributes.get("timestamp").getAsString());
                    } catch (DateTimeParseException e) {
                        batteryStatusUpdated = null;
                        logger.debug("Error updating battery status updated timestamp. {}", e.getMessage());
                    }
                }
            }
        } catch (IllegalStateException | ClassCastException e) {
            logger.warn("Error {} parsing Battery Status: {}", e.getMessage(), responseJson);
        }
    }

    public void resetHVACStatus() {
        this.hvacstatus = null;
    }

    public void setHVACStatus(JsonObject responseJson) {
        try {
            JsonObject attributes = getAttributes(responseJson);
            if (attributes != null) {
                if (attributes.get("hvacStatus") != null) {
                    hvacstatus = attributes.get("hvacStatus").getAsString().equals("on");
                }
                if (attributes.get("externalTemperature") != null) {
                    externalTemperature = attributes.get("externalTemperature").getAsDouble();
                }
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
                if (attributes.get("lastUpdateTime") != null) {
                    try {
                        locationUpdated = ZonedDateTime.parse(attributes.get("lastUpdateTime").getAsString());
                    } catch (DateTimeParseException e) {
                        locationUpdated = null;
                        logger.debug("Error updating location updated timestamp. {}", e.getMessage());
                    }
                }
            }
        } catch (IllegalStateException | ClassCastException e) {
            logger.warn("Error {} parsing Location: {}", e.getMessage(), responseJson);
        }
    }

    public void setLockStatus(JsonObject responseJson) {
        try {
            JsonObject attributes = getAttributes(responseJson);
            if (attributes != null) {
                if (attributes.get("lockStatus") != null) {
                    lockStatus = mapLockStatus(attributes.get("lockStatus").getAsString());
                }
            }
        } catch (IllegalStateException | ClassCastException e) {
            logger.warn("Error {} parsing Lock Status: {}", e.getMessage(), responseJson);
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

    public boolean isDisableBattery() {
        return disableBattery;
    }

    public boolean isDisableCockpit() {
        return disableCockpit;
    }

    public boolean isDisableHvac() {
        return disableHvac;
    }

    public @Nullable Double getBatteryLevel() {
        return batteryLevel;
    }

    public @Nullable ZonedDateTime getBatteryStatusUpdated() {
        return batteryStatusUpdated;
    }

    public @Nullable Boolean getHvacstatus() {
        return hvacstatus;
    }

    public @Nullable Double getOdometer() {
        return odometer;
    }

    public @Nullable String getImageURL() {
        return imageURL;
    }

    public @Nullable Double getGpsLatitude() {
        return gpsLatitude;
    }

    public @Nullable Double getGpsLongitude() {
        return gpsLongitude;
    }

    public @Nullable ZonedDateTime getLocationUpdated() {
        return locationUpdated;
    }

    public @Nullable Double getExternalTemperature() {
        return externalTemperature;
    }

    public @Nullable Double getEstimatedRange() {
        return estimatedRange;
    }

    public PlugStatus getPlugStatus() {
        return plugStatus;
    }

    public ChargingStatus getChargingStatus() {
        return chargingStatus;
    }

    public ChargingMode getChargingMode() {
        return chargingMode;
    }

    public boolean getPauseMode() {
        return pausemode;
    }

    public @Nullable Integer getChargingRemainingTime() {
        return chargingRemainingTime;
    }

    public @Nullable Double getBatteryAvailableEnergy() {
        return batteryAvailableEnergy;
    }

    public double getHvacTargetTemperature() {
        return hvacTargetTemperature;
    }

    public void setHvacTargetTemperature(double hvacTargetTemperature) {
        this.hvacTargetTemperature = hvacTargetTemperature;
    }

    public void setDisableLocation(boolean disableLocation) {
        this.disableLocation = disableLocation;
    }

    public void setDisableBattery(boolean disableBattery) {
        this.disableBattery = disableBattery;
    }

    public void setDisableCockpit(boolean disableCockpit) {
        this.disableCockpit = disableCockpit;
    }

    public void setDisableHvac(boolean disableHvac) {
        this.disableHvac = disableHvac;
    }

    /**
     * Set the charging mode to a known mode.
     * 
     * @param mode
     */
    public void setChargeMode(ChargingMode mode) {
        switch (mode) {
            case SCHEDULE_MODE:
            case ALWAYS_CHARGING:
                chargingMode = mode;
                break;
            default:
                break;
        }
    }

    public void setPauseMode(boolean pausemode) {
        this.pausemode = pausemode;
    }

    private @Nullable JsonObject getAttributes(JsonObject responseJson)
            throws IllegalStateException, ClassCastException {
        if (responseJson.get("data") != null && responseJson.get("data").getAsJsonObject().get("attributes") != null) {
            return responseJson.get("data").getAsJsonObject().get("attributes").getAsJsonObject();
        }
        return null;
    }

    private LockStatus mapLockStatus(final String apiLockStatus) {
        switch (apiLockStatus) {
            case "locked":
                return LockStatus.LOCKED;
            case "unlocked":
                return LockStatus.UNLOCKED;
            default:
                return LockStatus.UNKNOWN;
        }
    }

    private PlugStatus mapPlugStatus(final String apiPlugState) {
        // https://github.com/hacf-fr/renault-api/blob/main/src/renault_api/kamereon/enums.py
        switch (apiPlugState) {
            case "0":
                return PlugStatus.UNPLUGGED;
            case "1":
                return PlugStatus.PLUGGED;
            case "-1":
                return PlugStatus.PLUG_ERROR;
            case "-2147483648":
                return PlugStatus.PLUG_UNKNOWN;
            default:
                return PlugStatus.UNKNOWN;
        }
    }

    private ChargingStatus mapChargingStatus(final String apiChargeState) {
        // https://github.com/hacf-fr/renault-api/blob/main/src/renault_api/kamereon/enums.py
        switch (apiChargeState) {
            case "0.0":
                return ChargingStatus.NOT_IN_CHARGE;
            case "0.1":
                return ChargingStatus.WAITING_FOR_A_PLANNED_CHARGE;
            case "0.2":
                return ChargingStatus.CHARGE_ENDED;
            case "0.3":
                return ChargingStatus.WAITING_FOR_CURRENT_CHARGE;
            case "0.4":
                return ChargingStatus.ENERGY_FLAP_OPENED;
            case "1.0":
                return ChargingStatus.CHARGE_IN_PROGRESS;
            case "-1.0":
                return ChargingStatus.CHARGE_ERROR;
            case "-1.1":
                return ChargingStatus.UNAVAILABLE;
            default:
                return ChargingStatus.UNKNOWN;
        }
    }

    public LockStatus getLockStatus() {
        return lockStatus;
    }

    public boolean isDisableLockStatus() {
        return disableLockStatus;
    }

    public void setDisableLockStatus(boolean disableLockStatus) {
        this.disableLockStatus = disableLockStatus;
    }
}
