package org.openhab.binding.nest.internal.data;

import com.google.gson.annotations.SerializedName;

/**
 * Data for the nest smoke detector.
 *
 * @author David Bennett
 *
 */
public class SmokeDetector extends BaseNestDevice {
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
        ok,
        replace
    }

    public enum AlarmState {
        ok,
        emergency,
        warning
    }

    public enum UiColorState {
        gray,
        green,
        yellow,
        red
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
}
