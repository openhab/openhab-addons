/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

/**
 * Gson class to encapsulate the data for the Nest thermostat.
 *
 * @author David Bennett - Initial Contribution
 */
public class Thermostat extends BaseNestDevice {
    @SerializedName("can_cool")
    private boolean canCool;
    @SerializedName("can_heat")
    private boolean canHeat;
    @SerializedName("is_using_emergency_heat")
    private boolean isUsingEmergencyHeat;
    @SerializedName("has_fan")
    private boolean hasFan;
    @SerializedName("fan_timer_active")
    private boolean fanTimerActive;
    @SerializedName("fan_timer_timeout")
    private Date fanTimerTimeout;
    @SerializedName("has_leaf")
    private boolean hasLeaf;
    @SerializedName("temperature_scale")
    private String tempScale;
    @SerializedName("ambient_temperature_c")
    private Double ambientTemperature;
    @SerializedName("humidity")
    private Integer humidity;
    @SerializedName("target_temperature_c")
    private Double targetTemperature;
    @SerializedName("target_temperature_high_c")
    private Double targetTemperatureHigh;
    @SerializedName("target_temperature_low_c")
    private Double targetTemperatureLow;
    @SerializedName("hvac_mode")
    private String mode;
    @SerializedName("previous_hvac_mode")
    private String previousMode;
    @SerializedName("hvac_state")
    private String state;
    @SerializedName("is_locked")
    private boolean isLocked;
    @SerializedName("locked_temp_max_c")
    private Double lockedTemperatureHigh;
    @SerializedName("locked_temp_min_c")
    private Double lockedTemperatureLow;
    @SerializedName("sunlight_correction_enabled")
    private boolean sunlightCorrectionEnabled;
    @SerializedName("sunlight_correction_active")
    private boolean sunlightCorrectionActive;
    @SerializedName("fan_timer_duration")
    private Integer fanTimerDuration;
    @SerializedName("time_to_target")
    private String timeToTarget;
    @SerializedName("where_name")
    private String whereName;

    public String getTempScale() {
        return tempScale;
    }

    public void setTempScale(String tempScale) {
        this.tempScale = tempScale;
    }

    public Double getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(Double targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public Double getTargetTemperatureHigh() {
        return targetTemperatureHigh;
    }

    public void setTargetTemperatureHigh(Double targetTemperatureHigh) {
        this.targetTemperatureHigh = targetTemperatureHigh;
    }

    public Double getTargetTemperatureLow() {
        return targetTemperatureLow;
    }

    public void setTargetTemperatureLow(Double targetTemperatureLow) {
        this.targetTemperatureLow = targetTemperatureLow;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public boolean isLocked() {
        return isLocked;
    }

    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public Double getLockedTemperatureHigh() {
        return lockedTemperatureHigh;
    }

    public void setLockedTemperatureHigh(Double lockedTemperatureHigh) {
        this.lockedTemperatureHigh = lockedTemperatureHigh;
    }

    public Double getLockedTemperatureLow() {
        return lockedTemperatureLow;
    }

    public void setLockedTemperatureLow(Double lockedTemperatureLow) {
        this.lockedTemperatureLow = lockedTemperatureLow;
    }

    public boolean isCanCool() {
        return canCool;
    }

    public boolean isCanHeat() {
        return canHeat;
    }

    public boolean isUsingEmergencyHeat() {
        return isUsingEmergencyHeat;
    }

    public boolean isHasFan() {
        return hasFan;
    }

    public boolean isFanTimerActive() {
        return fanTimerActive;
    }

    public Date getFanTimerTimeout() {
        return fanTimerTimeout;
    }

    public boolean isHasLeaf() {
        return hasLeaf;
    }

    public String getPreviousMode() {
        return previousMode;
    }

    public String getState() {
        return state;
    }

    public boolean isSunlightCorrectionEnabled() {
        return sunlightCorrectionEnabled;
    }

    public boolean isSunlightCorrectionActive() {
        return sunlightCorrectionActive;
    }

    public Integer getFanTimerDuration() {
        return fanTimerDuration;
    }

    public void setFanTimerDuration(Integer duration) {
        fanTimerDuration = duration;
    }

    /*
     * Turns the time to target string into a real value.
     */
    public Integer getTimeToTarget() {
        if (timeToTarget.startsWith("~")) {
            timeToTarget = timeToTarget.substring(1);
        }
        return Integer.parseInt(timeToTarget);
    }

    public String getWhereName() {
        return whereName;
    }

    public Double getAmbientTemperature() {
        return ambientTemperature;
    }

    public Integer getHumidity() {
        return humidity;
    }

    @Override
    public String toString() {
        return "Thermostat [canCool=" + canCool + ", canHeat=" + canHeat + ", isUsingEmergencyHeat="
                + isUsingEmergencyHeat + ", hasFan=" + hasFan + ", fanTimerActive=" + fanTimerActive
                + ", fanTimerTimeout=" + fanTimerTimeout + ", hasLeaf=" + hasLeaf + ", tempScale=" + tempScale
                + ", ambientTemperature=" + ambientTemperature + ", humidity=" + humidity + ", targetTemperature="
                + targetTemperature + ", targetTemperatureHigh=" + targetTemperatureHigh + ", targetTemperatureLow="
                + targetTemperatureLow + ", mode=" + mode + ", previousMode=" + previousMode + ", state=" + state
                + ", isLocked=" + isLocked + ", lockedTemperatureHigh=" + lockedTemperatureHigh
                + ", lockedTemperatureLow=" + lockedTemperatureLow + ", sunlightCorrectionEnabled="
                + sunlightCorrectionEnabled + ", sunlightCorrectionActive=" + sunlightCorrectionActive
                + ", fanTimerDuration=" + fanTimerDuration + ", timeToTarget=" + timeToTarget + ", whereName="
                + whereName + ", getName()=" + getName() + ", getDeviceId()=" + getDeviceId() + ", getLastConnection()="
                + getLastConnection() + ", isOnline()=" + isOnline() + ", getNameLong()=" + getNameLong()
                + ", getSoftwareVersion()=" + getSoftwareVersion() + ", getStructureId()=" + getStructureId()
                + ", getWhereId()=" + getWhereId() + "]";
    }
}
