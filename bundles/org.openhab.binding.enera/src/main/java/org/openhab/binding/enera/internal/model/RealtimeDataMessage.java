package org.openhab.binding.enera.internal.model;

import java.util.List;

public class RealtimeDataMessage {
    private List<DeviceDataItem> DeviceDataItems;

    /**
     * @return the deviceDataItems
     */
    public List<DeviceDataItem> getDeviceDataItems() {
        return DeviceDataItems;
    }

    /**
     * @param deviceDataItems the deviceDataItems to set
     */
    public void setDeviceDataItems(List<DeviceDataItem> deviceDataItems) {
        this.DeviceDataItems = deviceDataItems;
    }
}
