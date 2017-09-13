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
 * Data for the Nest smoke detector.
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
    public String toString() {
        return "SmokeDetector [batteryHealth=" + batteryHealth + ", coAlarmState=" + coAlarmState + ", smokeAlarmState="
                + smokeAlarmState + ", isManualTestActive=" + isManualTestActive + ", uiColorState=" + uiColorState
                + ", getName()=" + getName() + ", getDeviceId()=" + getDeviceId() + ", getLastConnection()="
                + getLastConnection() + ", getNameLong()=" + getNameLong() + ", getSoftwareVersion()="
                + getSoftwareVersion() + ", getStructureId()=" + getStructureId() + ", isOnline()=" + isOnline()
                + ", getWhereId()=" + getWhereId() + "]";
    }
}
