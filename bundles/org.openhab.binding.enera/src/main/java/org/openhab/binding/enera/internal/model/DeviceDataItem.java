package org.openhab.binding.enera.internal.model;

import java.util.List;
import java.util.Date;

public class DeviceDataItem {
    private String DeviceId;
    private Date timestamp;
    private List<DeviceValue> values;

    public String getDeviceId() {
        return DeviceId;
    }

    public void setDeviceId(String deviceId) {
        this.DeviceId = deviceId;
    }

    /**
     * @return the timestamp
     */
    public Date getTimestamp() {
        return timestamp;
    }

    /**
     * @param timestamp the timestamp to set
     */
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * @return the values
     */
    public List<DeviceValue> getValues() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(List<DeviceValue> values) {
        this.values = values;
    }

    
}
