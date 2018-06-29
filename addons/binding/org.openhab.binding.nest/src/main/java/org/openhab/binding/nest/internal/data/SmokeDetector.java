/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
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
 * Data for the Nest smoke detector.
 *
 * @author David Bennett - Initial contribution
 * @author Wouter Born - Add equals and hashCode methods
 */
public class SmokeDetector extends BaseNestDevice {

    private BatteryHealth batteryHealth;
    private AlarmState coAlarmState;
    private Date lastManualTestTime;
    private AlarmState smokeAlarmState;
    private Boolean isManualTestActive;
    private UiColorState uiColorState;

    public UiColorState getUiColorState() {
        return uiColorState;
    }

    public BatteryHealth getBatteryHealth() {
        return batteryHealth;
    }

    public AlarmState getCoAlarmState() {
        return coAlarmState;
    }

    public Date getLastManualTestTime() {
        return lastManualTestTime;
    }

    public AlarmState getSmokeAlarmState() {
        return smokeAlarmState;
    }

    public Boolean isManualTestActive() {
        return isManualTestActive;
    }

    public enum BatteryHealth {
        @SerializedName("ok")
        OK,
        @SerializedName("replace")
        REPLACE
    }

    public enum AlarmState {
        @SerializedName("ok")
        OK,
        @SerializedName("emergency")
        EMERGENCY,
        @SerializedName("warning")
        WARNING
    }

    public enum UiColorState {
        @SerializedName("gray")
        GRAY,
        @SerializedName("green")
        GREEN,
        @SerializedName("yellow")
        YELLOW,
        @SerializedName("red")
        RED
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SmokeDetector other = (SmokeDetector) obj;
        if (batteryHealth != other.batteryHealth) {
            return false;
        }
        if (coAlarmState != other.coAlarmState) {
            return false;
        }
        if (isManualTestActive == null) {
            if (other.isManualTestActive != null) {
                return false;
            }
        } else if (!isManualTestActive.equals(other.isManualTestActive)) {
            return false;
        }
        if (lastManualTestTime == null) {
            if (other.lastManualTestTime != null) {
                return false;
            }
        } else if (!lastManualTestTime.equals(other.lastManualTestTime)) {
            return false;
        }
        if (smokeAlarmState != other.smokeAlarmState) {
            return false;
        }
        if (uiColorState != other.uiColorState) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((batteryHealth == null) ? 0 : batteryHealth.hashCode());
        result = prime * result + ((coAlarmState == null) ? 0 : coAlarmState.hashCode());
        result = prime * result + ((isManualTestActive == null) ? 0 : isManualTestActive.hashCode());
        result = prime * result + ((lastManualTestTime == null) ? 0 : lastManualTestTime.hashCode());
        result = prime * result + ((smokeAlarmState == null) ? 0 : smokeAlarmState.hashCode());
        result = prime * result + ((uiColorState == null) ? 0 : uiColorState.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SmokeDetector [batteryHealth=").append(batteryHealth).append(", coAlarmState=")
                .append(coAlarmState).append(", lastManualTestTime=").append(lastManualTestTime)
                .append(", smokeAlarmState=").append(smokeAlarmState).append(", isManualTestActive=")
                .append(isManualTestActive).append(", uiColorState=").append(uiColorState).append(", getId()=")
                .append(getId()).append(", getName()=").append(getName()).append(", getDeviceId()=")
                .append(getDeviceId()).append(", getLastConnection()=").append(getLastConnection())
                .append(", isOnline()=").append(isOnline()).append(", getNameLong()=").append(getNameLong())
                .append(", getSoftwareVersion()=").append(getSoftwareVersion()).append(", getStructureId()=")
                .append(getStructureId()).append(", getWhereId()=").append(getWhereId()).append("]");
        return builder.toString();
    }

}
