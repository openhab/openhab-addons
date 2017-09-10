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

/**
 * Data for the nest smoke detector.
 *
 * @author David Bennett - Initial Contribution
 *
 */
public class SmokeDetector extends BaseNestDevice {
    @SerializedName("battery_health")
    private BatteryHealth batteryHealth;
    @SerializedName("co_alarm_state")
    private AlarmState coAlarmState;
    @SerializedName("smoke_alarm_state")
    private AlarmState smokeAlarmState;
    @SerializedName("is_manual_test_active")
    private boolean isManualTestActive;
    @SerializedName("ui_color_state")
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

    public AlarmState getSmokeAlarmState() {
        return smokeAlarmState;
    }

    public boolean isManualTestActive() {
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
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((batteryHealth == null) ? 0 : batteryHealth.hashCode());
        result = prime * result + ((coAlarmState == null) ? 0 : coAlarmState.hashCode());
        result = prime * result + (isManualTestActive ? 1231 : 1237);
        result = prime * result + ((smokeAlarmState == null) ? 0 : smokeAlarmState.hashCode());
        result = prime * result + ((uiColorState == null) ? 0 : uiColorState.hashCode());
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
        if (isManualTestActive != other.isManualTestActive) {
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
    public String toString() {
        return "SmokeDetector [batteryHealth=" + batteryHealth + ", coAlarmState=" + coAlarmState + ", smokeAlarmState="
                + smokeAlarmState + ", isManualTestActive=" + isManualTestActive + ", uiColorState=" + uiColorState
                + ", getName()=" + getName() + ", getDeviceId()=" + getDeviceId() + ", getLastConnection()="
                + getLastConnection() + ", getNameLong()=" + getNameLong() + ", getSoftwareVersion()="
                + getSoftwareVersion() + ", getStructureId()=" + getStructureId() + ", isOnline()=" + isOnline()
                + ", getWhereId()=" + getWhereId() + "]";
    }
}
