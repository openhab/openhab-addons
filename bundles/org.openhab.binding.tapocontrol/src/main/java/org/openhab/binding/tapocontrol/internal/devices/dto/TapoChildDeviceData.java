/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.tapocontrol.internal.devices.dto;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Tapo Child Device Information class
 *
 * @author Gaël L'hopital - Initial contribution
 * @author Christian Wild - Integrating TapoHub
 */
@NonNullByDefault
public class TapoChildDeviceData extends TapoBaseDeviceData {
    @SerializedName("at_low_battery")
    @Expose(serialize = false, deserialize = true)
    private boolean atLowBattery = false;

    @SerializedName("battery_percentage")
    @Expose(serialize = false, deserialize = true)
    private int batteryPercentage = 0;

    @SerializedName("bind_count")
    @Expose(serialize = false, deserialize = true)
    private int bindCount = 0;

    @SerializedName("temp_unit")
    @Expose(serialize = false, deserialize = true)
    private String tempUnit = "celsius";

    @SerializedName("current_temp")
    @Expose(serialize = false, deserialize = true)
    private double currentTemp = 0.0;

    @SerializedName("current_humidity")
    @Expose(serialize = false, deserialize = true)
    private int currentHumidity = 0;

    @Expose(serialize = false, deserialize = true)
    private String category = "";

    @SerializedName("device_on")
    @Expose(serialize = true, deserialize = true)
    private boolean deviceOn = false;

    @SerializedName("jamming_rssi")
    @Expose(serialize = false, deserialize = true)
    private int jammingRssi = 0;

    @SerializedName("jamming_signal_level")
    @Expose(serialize = false, deserialize = true)
    private int jammingSignalLevel = 0;

    @SerializedName("lastOnboardingTimestamp")
    @Expose(serialize = false, deserialize = true)
    private long lastOnboardingTimestamp = 0;

    @SerializedName("on_time")
    @Expose(serialize = false, deserialize = true)
    private long onTime = 0;

    @Expose(serialize = false, deserialize = true)
    private boolean open = false;

    @SerializedName("parent_device_id")
    @Expose(serialize = false, deserialize = true)
    private String parentDeviceId = "";

    @Expose(serialize = false, deserialize = true)
    private int position = 0;

    @SerializedName("report_interval")
    @Expose(serialize = false, deserialize = true)
    private int reportInterval = 0;

    @SerializedName("slot_number")
    @Expose(serialize = false, deserialize = true)
    private int slotNumber = 0;

    @Expose(serialize = false, deserialize = true)
    private String status = "";

    @SerializedName("status_follow_edge")
    @Expose(serialize = false, deserialize = true)
    private boolean statusFollowEedge = false;

    /***********************************
     *
     * GET VALUES
     *
     ************************************/

    /* get boolean values */
    public boolean batteryIsLow() {
        return atLowBattery;
    }

    public boolean getStatusFollowEedge() {
        return statusFollowEedge;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public boolean isOff() {
        return !deviceOn;
    }

    public boolean isOn() {
        return deviceOn;
    }

    public double getTemperature() {
        return currentTemp;
    }

    public String getTempUnit() {
        return tempUnit;
    }

    public int getHumidity() {
        return currentHumidity;
    }

    public boolean isOnline() {
        return "online".equals(status);
    }

    public boolean isOpen() {
        return open;
    }

    /* get numeric values */
    public int getBindCount() {
        return bindCount;
    }

    public int getJammingRssi() {
        return jammingRssi;
    }

    public int getJammingSignalLevel() {
        return jammingSignalLevel;
    }

    public int getPosition() {
        return position;
    }

    public int getReportInterval() {
        return reportInterval;
    }

    public int getSlotNumber() {
        return slotNumber;
    }

    public long getLastOnboardingTimestamp() {
        return lastOnboardingTimestamp;
    }

    public Number getOnTime() {
        return onTime;
    }

    /* get string values */
    public String getCategory() {
        return category;
    }

    public String getParentDeviceId() {
        return parentDeviceId;
    }

    @Override
    public String getRepresentationProperty() {
        return getDeviceId();
    }

    /***********************************
     *
     * SET VALUES
     *
     ************************************/
    public void setDeviceOn(boolean deviceOn) {
        this.deviceOn = deviceOn;
    }
}
