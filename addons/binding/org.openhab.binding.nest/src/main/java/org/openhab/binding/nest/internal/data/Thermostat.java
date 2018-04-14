/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import static org.eclipse.smarthome.core.library.unit.ImperialUnits.FAHRENHEIT;
import static org.eclipse.smarthome.core.library.unit.SIUnits.CELSIUS;

import java.util.Date;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import com.google.gson.annotations.SerializedName;

/**
 * Gson class to encapsulate the data for the Nest thermostat.
 *
 * @author David Bennett - Initial Contribution
 */
public class Thermostat extends BaseNestDevice {
    @SerializedName("can_cool")
    private Boolean canCool;
    @SerializedName("can_heat")
    private Boolean canHeat;
    @SerializedName("is_using_emergency_heat")
    private Boolean isUsingEmergencyHeat;
    @SerializedName("has_fan")
    private Boolean hasFan;
    @SerializedName("fan_timer_active")
    private Boolean fanTimerActive;
    @SerializedName("fan_timer_timeout")
    private Date fanTimerTimeout;
    @SerializedName("has_leaf")
    private Boolean hasLeaf;
    @SerializedName("temperature_scale")
    private String temperatureScale;
    @SerializedName("ambient_temperature_c")
    private Double ambientTemperatureC;
    @SerializedName("ambient_temperature_f")
    private Double ambientTemperatureF;
    @SerializedName("humidity")
    private Integer humidity;
    @SerializedName("target_temperature_c")
    private Double targetTemperatureC;
    @SerializedName("target_temperature_f")
    private Double targetTemperatureF;
    @SerializedName("target_temperature_high_c")
    private Double targetTemperatureHighC;
    @SerializedName("target_temperature_high_f")
    private Double targetTemperatureHighF;
    @SerializedName("target_temperature_low_c")
    private Double targetTemperatureLowC;
    @SerializedName("target_temperature_low_f")
    private Double targetTemperatureLowF;
    @SerializedName("hvac_mode")
    private Mode mode;
    @SerializedName("previous_hvac_mode")
    private Mode previousMode;
    @SerializedName("hvac_state")
    private State state;
    @SerializedName("is_locked")
    private Boolean isLocked;
    @SerializedName("locked_temp_max_c")
    private Double lockedTemperatureHighC;
    @SerializedName("locked_temp_max_f")
    private Double lockedTemperatureHighF;
    @SerializedName("locked_temp_min_c")
    private Double lockedTemperatureLowC;
    @SerializedName("locked_temp_min_f")
    private Double lockedTemperatureLowF;
    @SerializedName("sunlight_correction_enabled")
    private Boolean sunlightCorrectionEnabled;
    @SerializedName("sunlight_correction_active")
    private Boolean sunlightCorrectionActive;
    @SerializedName("fan_timer_duration")
    private Integer fanTimerDuration;
    @SerializedName("time_to_target")
    private String timeToTarget;
    @SerializedName("where_name")
    private String whereName;

    public Unit<Temperature> getTemperatureUnit() {
        if ("C".equals(temperatureScale)) {
            return CELSIUS;
        } else if ("F".equals(temperatureScale)) {
            return FAHRENHEIT;
        } else {
            return null;
        }
    }

    public Double getTargetTemperature() {
        if (getTemperatureUnit() == CELSIUS) {
            return targetTemperatureC;
        } else if (getTemperatureUnit() == FAHRENHEIT) {
            return targetTemperatureF;
        } else {
            return null;
        }
    }

    public Double getTargetTemperatureHigh() {
        if (getTemperatureUnit() == CELSIUS) {
            return targetTemperatureHighC;
        } else if (getTemperatureUnit() == FAHRENHEIT) {
            return targetTemperatureHighF;
        } else {
            return null;
        }
    }

    public Double getTargetTemperatureLow() {
        if (getTemperatureUnit() == CELSIUS) {
            return targetTemperatureLowC;
        } else if (getTemperatureUnit() == FAHRENHEIT) {
            return targetTemperatureLowF;
        } else {
            return null;
        }
    }

    public Mode getMode() {
        return mode;
    }

    public Boolean isLocked() {
        return isLocked;
    }

    public Double getLockedTemperatureHigh() {
        if (getTemperatureUnit() == CELSIUS) {
            return lockedTemperatureHighC;
        } else if (getTemperatureUnit() == FAHRENHEIT) {
            return lockedTemperatureHighF;
        } else {
            return null;
        }
    }

    public Double getLockedTemperatureLow() {
        if (getTemperatureUnit() == CELSIUS) {
            return lockedTemperatureLowC;
        } else if (getTemperatureUnit() == FAHRENHEIT) {
            return lockedTemperatureLowF;
        } else {
            return null;
        }
    }

    public Boolean isCanCool() {
        return canCool;
    }

    public Boolean isCanHeat() {
        return canHeat;
    }

    public Boolean isUsingEmergencyHeat() {
        return isUsingEmergencyHeat;
    }

    public Boolean isHasFan() {
        return hasFan;
    }

    public Boolean isFanTimerActive() {
        return fanTimerActive;
    }

    public Date getFanTimerTimeout() {
        return fanTimerTimeout;
    }

    public Boolean isHasLeaf() {
        return hasLeaf;
    }

    public Mode getPreviousMode() {
        return previousMode;
    }

    public State getState() {
        return state;
    }

    public Boolean isSunlightCorrectionEnabled() {
        return sunlightCorrectionEnabled;
    }

    public Boolean isSunlightCorrectionActive() {
        return sunlightCorrectionActive;
    }

    public Integer getFanTimerDuration() {
        return fanTimerDuration;
    }

    public Integer getTimeToTarget() {
        return parseTimeToTarget(timeToTarget);
    }

    /*
     * Turns the time to target string into a real value.
     */
    static Integer parseTimeToTarget(String timeToTarget) {
        if (timeToTarget == null) {
            return null;
        } else if (timeToTarget.startsWith("~") || timeToTarget.startsWith("<") || timeToTarget.startsWith(">")) {
            return Integer.valueOf(timeToTarget.substring(1));
        }
        return Integer.valueOf(timeToTarget);
    }

    public String getWhereName() {
        return whereName;
    }

    public Double getAmbientTemperature() {
        if (getTemperatureUnit() == CELSIUS) {
            return ambientTemperatureC;
        } else if (getTemperatureUnit() == FAHRENHEIT) {
            return ambientTemperatureF;
        } else {
            return null;
        }
    }

    public Integer getHumidity() {
        return humidity;
    }

    public enum Mode {
        @SerializedName("heat")
        HEAT,
        @SerializedName("cool")
        COOL,
        @SerializedName("heat-cool")
        HEAT_COOL,
        @SerializedName("eco")
        ECO,
        @SerializedName("off")
        OFF
    }

    public enum State {
        @SerializedName("heating")
        HEATING,
        @SerializedName("cooling")
        COOLING,
        @SerializedName("off")
        OFF
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Thermostat [canCool=").append(canCool).append(", canHeat=").append(canHeat)
                .append(", isUsingEmergencyHeat=").append(isUsingEmergencyHeat).append(", hasFan=").append(hasFan)
                .append(", fanTimerActive=").append(fanTimerActive).append(", fanTimerTimeout=").append(fanTimerTimeout)
                .append(", hasLeaf=").append(hasLeaf).append(", temperatureScale=").append(temperatureScale)
                .append(", ambientTemperatureC=").append(ambientTemperatureC).append(", ambientTemperatureF=")
                .append(ambientTemperatureF).append(", humidity=").append(humidity).append(", targetTemperatureC=")
                .append(targetTemperatureC).append(", targetTemperatureF=").append(targetTemperatureF)
                .append(", targetTemperatureHighC=").append(targetTemperatureHighC).append(", targetTemperatureHighF=")
                .append(targetTemperatureHighF).append(", targetTemperatureLowC=").append(targetTemperatureLowC)
                .append(", targetTemperatureLowF=").append(targetTemperatureLowF).append(", mode=").append(mode)
                .append(", previousMode=").append(previousMode).append(", state=").append(state).append(", isLocked=")
                .append(isLocked).append(", lockedTemperatureHighC=").append(lockedTemperatureHighC)
                .append(", lockedTemperatureHighF=").append(lockedTemperatureHighF).append(", lockedTemperatureLowC=")
                .append(lockedTemperatureLowC).append(", lockedTemperatureLowF=").append(lockedTemperatureLowF)
                .append(", sunlightCorrectionEnabled=").append(sunlightCorrectionEnabled)
                .append(", sunlightCorrectionActive=").append(sunlightCorrectionActive).append(", fanTimerDuration=")
                .append(fanTimerDuration).append(", timeToTarget=").append(timeToTarget).append(", whereName=")
                .append(whereName).append(", getId()=").append(getId()).append(", getName()=").append(getName())
                .append(", getDeviceId()=").append(getDeviceId()).append(", getLastConnection()=")
                .append(getLastConnection()).append(", isOnline()=").append(isOnline()).append(", getNameLong()=")
                .append(getNameLong()).append(", getSoftwareVersion()=").append(getSoftwareVersion())
                .append(", getStructureId()=").append(getStructureId()).append(", getWhereId()=").append(getWhereId())
                .append("]");
        return builder.toString();
    }

}
