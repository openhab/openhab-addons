package org.openhab.binding.nest.internal.data;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * Gson class to encapsulate the data for the nest thermostat.
 *
 * @author David Bennett
 */
public class Thermostat extends BaseNestDevice {
    public String getTempScale() {
        return tempScale;
    }

    public void setTempScale(String tempScale) {
        this.tempScale = tempScale;
    }

    public Number getTargetTemperature() {
        return targetTemperature;
    }

    public void setTargetTemperature(Number targetTemperature) {
        this.targetTemperature = targetTemperature;
    }

    public Number getTargetTemperatureHigh() {
        return targetTemperatureHigh;
    }

    public void setTargetTemperatureHigh(Number targetTemperatureHigh) {
        this.targetTemperatureHigh = targetTemperatureHigh;
    }

    public Number getTargetTemperatureLow() {
        return targetTemperatureLow;
    }

    public void setTargetTemperatureLow(Number targetTemperatureLow) {
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

    public Number getLockedTemperatureHigh() {
        return lockedTemperatureHigh;
    }

    public void setLockedTemperatureHigh(Number lockedTemperatureHigh) {
        this.lockedTemperatureHigh = lockedTemperatureHigh;
    }

    public Number getLockedTemperatureLow() {
        return lockedTemperatureLow;
    }

    public void setLockedTemperatureLow(Number lockedTemperatureLow) {
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

    public Number getFanTimerDuration() {
        return fanTimerDuration;
    }

    public void setFanTimerDuration(Number duration) {
        fanTimerDuration = duration;
    }

    /*
     * Turns the time to target string into a real value.
     */
    public Number getTimeToTarget() {
        if (timeToTarget.startsWith("~")) {
            timeToTarget = timeToTarget.substring(1);
        }
        return Integer.parseInt(timeToTarget);
    }

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
    @SerializedName("target_temperatuire_c")
    private Number targetTemperature;
    @SerializedName("target_temperature_high_c")
    private Number targetTemperatureHigh;
    @SerializedName("target_temperature_low_c")
    private Number targetTemperatureLow;
    @SerializedName("hvac_mode")
    private String mode;
    @SerializedName("hvac_previous_mode")
    private String previousMode;
    @SerializedName("state")
    private String state;
    @SerializedName("is_locked")
    private boolean isLocked;
    @SerializedName("locked_temp_max_c")
    private Number lockedTemperatureHigh;
    @SerializedName("locked_temp_min_c")
    private Number lockedTemperatureLow;
    @SerializedName("sunlight_correction_enabled")
    private boolean sunlightCorrectionEnabled;
    @SerializedName("sunlight_correction_active")
    private boolean sunlightCorrectionActive;
    @SerializedName("fan_timer_durection")
    private Number fanTimerDuration;
    @SerializedName("time_to_target")
    private String timeToTarget;

}
