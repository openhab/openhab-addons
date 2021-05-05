package org.openhab.binding.nuki.internal.dto;

public class BridgeApiListDeviceDto {

    private String nukiId;
    private String firmwareVersion;
    private int deviceType;
    private String name;
    private BridgeApiListDeviceLastKnownState lastKnownState;

    public String getNukiId() {
        return nukiId;
    }

    public void setNukiId(String nukiId) {
        this.nukiId = nukiId;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BridgeApiListDeviceLastKnownState getLastKnownState() {
        return lastKnownState;
    }

    public void setLastKnownState(BridgeApiListDeviceLastKnownState lastKnownState) {
        this.lastKnownState = lastKnownState;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }
}
