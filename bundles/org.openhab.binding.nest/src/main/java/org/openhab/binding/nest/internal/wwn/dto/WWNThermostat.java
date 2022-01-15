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
package org.openhab.binding.nest.internal.wwn.dto;

import static org.openhab.core.library.unit.ImperialUnits.FAHRENHEIT;
import static org.openhab.core.library.unit.SIUnits.CELSIUS;

import java.util.Date;

import javax.measure.Unit;
import javax.measure.quantity.Temperature;

import com.google.gson.annotations.SerializedName;

/**
 * Gson class to encapsulate the data for the WWN thermostat.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add equals and hashCode methods
 */
public class WWNThermostat extends BaseWWNDevice {

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
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        WWNThermostat other = (WWNThermostat) obj;
        if (ambientTemperatureC == null) {
            if (other.ambientTemperatureC != null) {
                return false;
            }
        } else if (!ambientTemperatureC.equals(other.ambientTemperatureC)) {
            return false;
        }
        if (ambientTemperatureF == null) {
            if (other.ambientTemperatureF != null) {
                return false;
            }
        } else if (!ambientTemperatureF.equals(other.ambientTemperatureF)) {
            return false;
        }
        if (canCool == null) {
            if (other.canCool != null) {
                return false;
            }
        } else if (!canCool.equals(other.canCool)) {
            return false;
        }
        if (canHeat == null) {
            if (other.canHeat != null) {
                return false;
            }
        } else if (!canHeat.equals(other.canHeat)) {
            return false;
        }
        if (ecoTemperatureHighC == null) {
            if (other.ecoTemperatureHighC != null) {
                return false;
            }
        } else if (!ecoTemperatureHighC.equals(other.ecoTemperatureHighC)) {
            return false;
        }
        if (ecoTemperatureHighF == null) {
            if (other.ecoTemperatureHighF != null) {
                return false;
            }
        } else if (!ecoTemperatureHighF.equals(other.ecoTemperatureHighF)) {
            return false;
        }
        if (ecoTemperatureLowC == null) {
            if (other.ecoTemperatureLowC != null) {
                return false;
            }
        } else if (!ecoTemperatureLowC.equals(other.ecoTemperatureLowC)) {
            return false;
        }
        if (ecoTemperatureLowF == null) {
            if (other.ecoTemperatureLowF != null) {
                return false;
            }
        } else if (!ecoTemperatureLowF.equals(other.ecoTemperatureLowF)) {
            return false;
        }
        if (fanTimerActive == null) {
            if (other.fanTimerActive != null) {
                return false;
            }
        } else if (!fanTimerActive.equals(other.fanTimerActive)) {
            return false;
        }
        if (fanTimerDuration == null) {
            if (other.fanTimerDuration != null) {
                return false;
            }
        } else if (!fanTimerDuration.equals(other.fanTimerDuration)) {
            return false;
        }
        if (fanTimerTimeout == null) {
            if (other.fanTimerTimeout != null) {
                return false;
            }
        } else if (!fanTimerTimeout.equals(other.fanTimerTimeout)) {
            return false;
        }
        if (hasFan == null) {
            if (other.hasFan != null) {
                return false;
            }
        } else if (!hasFan.equals(other.hasFan)) {
            return false;
        }
        if (hasLeaf == null) {
            if (other.hasLeaf != null) {
                return false;
            }
        } else if (!hasLeaf.equals(other.hasLeaf)) {
            return false;
        }
        if (humidity == null) {
            if (other.humidity != null) {
                return false;
            }
        } else if (!humidity.equals(other.humidity)) {
            return false;
        }
        if (hvacMode != other.hvacMode) {
            return false;
        }
        if (hvacState != other.hvacState) {
            return false;
        }
        if (isLocked == null) {
            if (other.isLocked != null) {
                return false;
            }
        } else if (!isLocked.equals(other.isLocked)) {
            return false;
        }
        if (isUsingEmergencyHeat == null) {
            if (other.isUsingEmergencyHeat != null) {
                return false;
            }
        } else if (!isUsingEmergencyHeat.equals(other.isUsingEmergencyHeat)) {
            return false;
        }
        if (lockedTempMaxC == null) {
            if (other.lockedTempMaxC != null) {
                return false;
            }
        } else if (!lockedTempMaxC.equals(other.lockedTempMaxC)) {
            return false;
        }
        if (lockedTempMaxF == null) {
            if (other.lockedTempMaxF != null) {
                return false;
            }
        } else if (!lockedTempMaxF.equals(other.lockedTempMaxF)) {
            return false;
        }
        if (lockedTempMinC == null) {
            if (other.lockedTempMinC != null) {
                return false;
            }
        } else if (!lockedTempMinC.equals(other.lockedTempMinC)) {
            return false;
        }
        if (lockedTempMinF == null) {
            if (other.lockedTempMinF != null) {
                return false;
            }
        } else if (!lockedTempMinF.equals(other.lockedTempMinF)) {
            return false;
        }
        if (previousHvacMode != other.previousHvacMode) {
            return false;
        }
        if (sunlightCorrectionActive == null) {
            if (other.sunlightCorrectionActive != null) {
                return false;
            }
        } else if (!sunlightCorrectionActive.equals(other.sunlightCorrectionActive)) {
            return false;
        }
        if (sunlightCorrectionEnabled == null) {
            if (other.sunlightCorrectionEnabled != null) {
                return false;
            }
        } else if (!sunlightCorrectionEnabled.equals(other.sunlightCorrectionEnabled)) {
            return false;
        }
        if (targetTemperatureC == null) {
            if (other.targetTemperatureC != null) {
                return false;
            }
        } else if (!targetTemperatureC.equals(other.targetTemperatureC)) {
            return false;
        }
        if (targetTemperatureF == null) {
            if (other.targetTemperatureF != null) {
                return false;
            }
        } else if (!targetTemperatureF.equals(other.targetTemperatureF)) {
            return false;
        }
        if (targetTemperatureHighC == null) {
            if (other.targetTemperatureHighC != null) {
                return false;
            }
        } else if (!targetTemperatureHighC.equals(other.targetTemperatureHighC)) {
            return false;
        }
        if (targetTemperatureHighF == null) {
            if (other.targetTemperatureHighF != null) {
                return false;
            }
        } else if (!targetTemperatureHighF.equals(other.targetTemperatureHighF)) {
            return false;
        }
        if (targetTemperatureLowC == null) {
            if (other.targetTemperatureLowC != null) {
                return false;
            }
        } else if (!targetTemperatureLowC.equals(other.targetTemperatureLowC)) {
            return false;
        }
        if (targetTemperatureLowF == null) {
            if (other.targetTemperatureLowF != null) {
                return false;
            }
        } else if (!targetTemperatureLowF.equals(other.targetTemperatureLowF)) {
            return false;
        }
        if (temperatureScale == null) {
            if (other.temperatureScale != null) {
                return false;
            }
        } else if (!temperatureScale.equals(other.temperatureScale)) {
            return false;
        }
        if (timeToTarget == null) {
            if (other.timeToTarget != null) {
                return false;
            }
        } else if (!timeToTarget.equals(other.timeToTarget)) {
            return false;
        }
        if (whereName == null) {
            if (other.whereName != null) {
                return false;
            }
        } else if (!whereName.equals(other.whereName)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((ambientTemperatureC == null) ? 0 : ambientTemperatureC.hashCode());
        result = prime * result + ((ambientTemperatureF == null) ? 0 : ambientTemperatureF.hashCode());
        result = prime * result + ((canCool == null) ? 0 : canCool.hashCode());
        result = prime * result + ((canHeat == null) ? 0 : canHeat.hashCode());
        result = prime * result + ((ecoTemperatureHighC == null) ? 0 : ecoTemperatureHighC.hashCode());
        result = prime * result + ((ecoTemperatureHighF == null) ? 0 : ecoTemperatureHighF.hashCode());
        result = prime * result + ((ecoTemperatureLowC == null) ? 0 : ecoTemperatureLowC.hashCode());
        result = prime * result + ((ecoTemperatureLowF == null) ? 0 : ecoTemperatureLowF.hashCode());
        result = prime * result + ((fanTimerActive == null) ? 0 : fanTimerActive.hashCode());
        result = prime * result + ((fanTimerDuration == null) ? 0 : fanTimerDuration.hashCode());
        result = prime * result + ((fanTimerTimeout == null) ? 0 : fanTimerTimeout.hashCode());
        result = prime * result + ((hasFan == null) ? 0 : hasFan.hashCode());
        result = prime * result + ((hasLeaf == null) ? 0 : hasLeaf.hashCode());
        result = prime * result + ((humidity == null) ? 0 : humidity.hashCode());
        result = prime * result + ((hvacMode == null) ? 0 : hvacMode.hashCode());
        result = prime * result + ((hvacState == null) ? 0 : hvacState.hashCode());
        result = prime * result + ((isLocked == null) ? 0 : isLocked.hashCode());
        result = prime * result + ((isUsingEmergencyHeat == null) ? 0 : isUsingEmergencyHeat.hashCode());
        result = prime * result + ((lockedTempMaxC == null) ? 0 : lockedTempMaxC.hashCode());
        result = prime * result + ((lockedTempMaxF == null) ? 0 : lockedTempMaxF.hashCode());
        result = prime * result + ((lockedTempMinC == null) ? 0 : lockedTempMinC.hashCode());
        result = prime * result + ((lockedTempMinF == null) ? 0 : lockedTempMinF.hashCode());
        result = prime * result + ((previousHvacMode == null) ? 0 : previousHvacMode.hashCode());
        result = prime * result + ((sunlightCorrectionActive == null) ? 0 : sunlightCorrectionActive.hashCode());
        result = prime * result + ((sunlightCorrectionEnabled == null) ? 0 : sunlightCorrectionEnabled.hashCode());
        result = prime * result + ((targetTemperatureC == null) ? 0 : targetTemperatureC.hashCode());
        result = prime * result + ((targetTemperatureF == null) ? 0 : targetTemperatureF.hashCode());
        result = prime * result + ((targetTemperatureHighC == null) ? 0 : targetTemperatureHighC.hashCode());
        result = prime * result + ((targetTemperatureHighF == null) ? 0 : targetTemperatureHighF.hashCode());
        result = prime * result + ((targetTemperatureLowC == null) ? 0 : targetTemperatureLowC.hashCode());
        result = prime * result + ((targetTemperatureLowF == null) ? 0 : targetTemperatureLowF.hashCode());
        result = prime * result + ((temperatureScale == null) ? 0 : temperatureScale.hashCode());
        result = prime * result + ((timeToTarget == null) ? 0 : timeToTarget.hashCode());
        result = prime * result + ((whereName == null) ? 0 : whereName.hashCode());
        return result;
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
