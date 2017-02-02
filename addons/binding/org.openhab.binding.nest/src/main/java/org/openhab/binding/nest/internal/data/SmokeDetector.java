package org.openhab.binding.nest.internal.data;

import java.util.Date;

import com.google.gson.annotations.SerializedName;

/**
 * Data for the nest smoke detector.
 *
 * @author David Bennett
 *
 */
public class SmokeDetector extends BaseNestDevice {
    public String getUiColorState() {
        return uiColorState;
    }

    public String getBatteryHealth() {
        return batteryHealth;
    }

    public String getCoAlarmState() {
        return coAlarmState;
    }

    public String getSmokeAlarmState() {
        return smokeAlarmState;
    }

    public boolean isManualTestActive() {
        return isManualTestActive;
    }

    public Date getLastManualTestTime() {
        return lastManualTestTime;
    }

    @SerializedName("battery_health")
    private String batteryHealth;
    @SerializedName("co_alarm_state")
    private String coAlarmState;
    @SerializedName("smoke_alarm_state")
    private String smokeAlarmState;
    @SerializedName("is_manual_test_active")
    private boolean isManualTestActive;
    @SerializedName("last_manual_test_time")
    private Date lastManualTestTime;
    @SerializedName("ui_color_state")
    private String uiColorState;
}
