package org.openhab.binding.enera.internal.model;

import java.util.List;

public class DeviceDataItem {
    private String DeviceId;
    private RealtimeDataMessageDateTime DateTime;
    private List<DeviceValue> DeviceValues;

    public String getDeviceId() {
        return DeviceId;
    }

    public List<DeviceValue> getDeviceValues() {
        return DeviceValues;
    }

    public void setDeviceValues(List<DeviceValue> deviceValues) {
        this.DeviceValues = deviceValues;
    }

    public RealtimeDataMessageDateTime getDateTime() {
        return DateTime;
    }

    public void setDateTime(RealtimeDataMessageDateTime dateTime) {
        this.DateTime = dateTime;
    }

    public void setDeviceId(String deviceId) {
        this.DeviceId = deviceId;
    }

    
}
