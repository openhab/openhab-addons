package org.openhab.binding.enera.internal.model;

import java.util.List;

public class RealtimeDataMessage {
    private String meterSha;
    private List<DeviceDataItem> items;

    /**
     * @return the meterSha
     */
    public String getMeterSha() {
        return meterSha;
    }

    /**
     * @param meterSha the meterSha to set
     */
    public void setMeterSha(String meterSha) {
        this.meterSha = meterSha;
    }

    /**
     * @return the items
     */
    public List<DeviceDataItem> getItems() {
        return items;
    }

    /**
     * @param items the items to set
     */
    public void setItems(List<DeviceDataItem> items) {
        this.items = items;
    }

}
