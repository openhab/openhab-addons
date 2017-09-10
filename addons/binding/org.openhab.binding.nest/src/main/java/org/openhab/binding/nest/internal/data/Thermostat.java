/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.nest.internal.data;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * Gson class to encapsulate the data for the nest thermostat.
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((ambientTemperature == null) ? 0 : ambientTemperature.hashCode());
        result = prime * result + (canCool ? 1231 : 1237);
        result = prime * result + (canHeat ? 1231 : 1237);
        result = prime * result + (fanTimerActive ? 1231 : 1237);
        result = prime * result + ((fanTimerDuration == null) ? 0 : fanTimerDuration.hashCode());
        result = prime * result + ((fanTimerTimeout == null) ? 0 : fanTimerTimeout.hashCode());
        result = prime * result + (hasFan ? 1231 : 1237);
        result = prime * result + (hasLeaf ? 1231 : 1237);
        result = prime * result + ((humidity == null) ? 0 : humidity.hashCode());
        result = prime * result + (isLocked ? 1231 : 1237);
        result = prime * result + (isUsingEmergencyHeat ? 1231 : 1237);
        result = prime * result + ((lockedTemperatureHigh == null) ? 0 : lockedTemperatureHigh.hashCode());
        result = prime * result + ((lockedTemperatureLow == null) ? 0 : lockedTemperatureLow.hashCode());
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + ((previousMode == null) ? 0 : previousMode.hashCode());
        result = prime * result + ((state == null) ? 0 : state.hashCode());
        result = prime * result + (sunlightCorrectionActive ? 1231 : 1237);
        result = prime * result + (sunlightCorrectionEnabled ? 1231 : 1237);
        result = prime * result + ((targetTemperature == null) ? 0 : targetTemperature.hashCode());
        result = prime * result + ((targetTemperatureHigh == null) ? 0 : targetTemperatureHigh.hashCode());
        result = prime * result + ((targetTemperatureLow == null) ? 0 : targetTemperatureLow.hashCode());
        result = prime * result + ((tempScale == null) ? 0 : tempScale.hashCode());
        result = prime * result + ((timeToTarget == null) ? 0 : timeToTarget.hashCode());
        result = prime * result + ((whereName == null) ? 0 : whereName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Thermostat other = (Thermostat) obj;
        if (ambientTemperature == null) {
            if (other.ambientTemperature != null) {
                return false;
            }
        } else if (!ambientTemperature.equals(other.ambientTemperature)) {
            return false;
        }
        if (canCool != other.canCool) {
            return false;
        }
        if (canHeat != other.canHeat) {
            return false;
        }
        if (fanTimerActive != other.fanTimerActive) {
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
        if (hasFan != other.hasFan) {
            return false;
        }
        if (hasLeaf != other.hasLeaf) {
            return false;
        }
        if (humidity == null) {
            if (other.humidity != null) {
                return false;
            }
        } else if (!humidity.equals(other.humidity)) {
            return false;
        }
        if (isLocked != other.isLocked) {
            return false;
        }
        if (isUsingEmergencyHeat != other.isUsingEmergencyHeat) {
            return false;
        }
        if (lockedTemperatureHigh == null) {
            if (other.lockedTemperatureHigh != null) {
                return false;
            }
        } else if (!lockedTemperatureHigh.equals(other.lockedTemperatureHigh)) {
            return false;
        }
        if (lockedTemperatureLow == null) {
            if (other.lockedTemperatureLow != null) {
                return false;
            }
        } else if (!lockedTemperatureLow.equals(other.lockedTemperatureLow)) {
            return false;
        }
        if (mode == null) {
            if (other.mode != null) {
                return false;
            }
        } else if (!mode.equals(other.mode)) {
            return false;
        }
        if (previousMode == null) {
            if (other.previousMode != null) {
                return false;
            }
        } else if (!previousMode.equals(other.previousMode)) {
            return false;
        }
        if (state == null) {
            if (other.state != null) {
                return false;
            }
        } else if (!state.equals(other.state)) {
            return false;
        }
        if (sunlightCorrectionActive != other.sunlightCorrectionActive) {
            return false;
        }
        if (sunlightCorrectionEnabled != other.sunlightCorrectionEnabled) {
            return false;
        }
        if (targetTemperature == null) {
            if (other.targetTemperature != null) {
                return false;
            }
        } else if (!targetTemperature.equals(other.targetTemperature)) {
            return false;
        }
        if (targetTemperatureHigh == null) {
            if (other.targetTemperatureHigh != null) {
                return false;
            }
        } else if (!targetTemperatureHigh.equals(other.targetTemperatureHigh)) {
            return false;
        }
        if (targetTemperatureLow == null) {
            if (other.targetTemperatureLow != null) {
                return false;
            }
        } else if (!targetTemperatureLow.equals(other.targetTemperatureLow)) {
            return false;
        }
        if (tempScale == null) {
            if (other.tempScale != null) {
                return false;
            }
        } else if (!tempScale.equals(other.tempScale)) {
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
