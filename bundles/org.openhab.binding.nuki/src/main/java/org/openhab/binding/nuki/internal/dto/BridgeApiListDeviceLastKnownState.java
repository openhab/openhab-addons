package org.openhab.binding.nuki.internal.dto;

public class BridgeApiListDeviceLastKnownState extends BridgeApiLockStateDto {

    private String timestamp;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }
}
