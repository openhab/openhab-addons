package org.openhab.binding.kermi.internal.api;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class MenuEntryResponse {

    @SerializedName("DeviceId")
    private String deviceId;

    @SerializedName("ParentMenuEntryId")
    private String parentMenuEntryId;

    @SerializedName("MenuEntries")
    private List<MenuEntry> menuEntries;

    @SerializedName("Bundles")
    private List<Bundle> bundles;

    @SerializedName("Devices")
    private List<Device> devices;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getParentMenuEntryId() {
        return parentMenuEntryId;
    }

    public void setParentMenuEntryId(String parentMenuEntryId) {
        this.parentMenuEntryId = parentMenuEntryId;
    }

    public List<MenuEntry> getMenuEntries() {
        return menuEntries;
    }

    public void setMenuEntries(List<MenuEntry> menuEntries) {
        this.menuEntries = menuEntries;
    }

    public List<Bundle> getBundles() {
        return bundles;
    }

    public void setBundles(List<Bundle> bundles) {
        this.bundles = bundles;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

}
