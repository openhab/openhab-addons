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

    private Boolean canCool;
    private Boolean canHeat;
    private Boolean isUsingEmergencyHeat;
    private Boolean hasFan;
    private Boolean fanTimerActive;
    private Date fanTimerTimeout;
    private Boolean hasLeaf;
    private String temperatureScale;
    private Double ambientTemperatureC;
    private Double ambientTemperatureF;
    private Integer humidity;
    private Double targetTemperatureC;
    private Double targetTemperatureF;
    private Double targetTemperatureHighC;
    private Double targetTemperatureHighF;
    private Double targetTemperatureLowC;
    private Double targetTemperatureLowF;
    private Mode hvacMode;
    private Mode previousHvacMode;
    private State hvacState;
    private Double ecoTemperatureHighC;
    private Double ecoTemperatureHighF;
    private Double ecoTemperatureLowC;
    private Double ecoTemperatureLowF;
    private Boolean isLocked;
    private Double lockedTempMaxC;
    private Double lockedTempMaxF;
    private Double lockedTempMinC;
    private Double lockedTempMinF;
    private Boolean sunlightCorrectionEnabled;
    private Boolean sunlightCorrectionActive;
    private Integer fanTimerDuration;
    private String timeToTarget;
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
        return hvacMode;
    }

    public Double getEcoTemperatureHigh() {
        if (getTemperatureUnit() == CELSIUS) {
            return ecoTemperatureHighC;
        } else if (getTemperatureUnit() == FAHRENHEIT) {
            return ecoTemperatureHighF;
        } else {
            return null;
        }
    }

    public Double getEcoTemperatureLow() {
        if (getTemperatureUnit() == CELSIUS) {
            return ecoTemperatureLowC;
        } else if (getTemperatureUnit() == FAHRENHEIT) {
            return ecoTemperatureLowF;
        } else {
            return null;
        }
    }

    public Boolean isLocked() {
        return isLocked;
    }

    public Double getLockedTempMax() {
        if (getTemperatureUnit() == CELSIUS) {
            return lockedTempMaxC;
        } else if (getTemperatureUnit() == FAHRENHEIT) {
            return lockedTempMaxF;
        } else {
            return null;
        }
    }

    public Double getLockedTempMin() {
        if (getTemperatureUnit() == CELSIUS) {
            return lockedTempMinC;
        } else if (getTemperatureUnit() == FAHRENHEIT) {
            return lockedTempMinF;
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

    public Mode getPreviousHvacMode() {
        return previousHvacMode;
    }

    public State getHvacState() {
        return hvacState;
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
                .append(", targetTemperatureLowF=").append(targetTemperatureLowF).append(", hvacMode=").append(hvacMode)
                .append(", previousHvacMode=").append(previousHvacMode).append(", hvacState=").append(hvacState)
                .append(", ecoTemperatureHighC=").append(ecoTemperatureHighC).append(", ecoTemperatureHighF=")
                .append(ecoTemperatureHighF).append(", ecoTemperatureLowC=").append(ecoTemperatureLowC)
                .append(", ecoTemperatureLowF=").append(ecoTemperatureLowF).append(", isLocked=").append(isLocked)
                .append(", lockedTempMaxC=").append(lockedTempMaxC).append(", lockedTempMaxF=").append(lockedTempMaxF)
                .append(", lockedTempMinC=").append(lockedTempMinC).append(", lockedTempMinF=").append(lockedTempMinF)
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
