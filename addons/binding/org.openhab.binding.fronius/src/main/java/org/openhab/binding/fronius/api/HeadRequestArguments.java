package org.openhab.binding.fronius.api;

import com.google.gson.annotations.SerializedName;

public class HeadRequestArguments {
    @SerializedName("DataCollection")
    private String dataCollection;
    @SerializedName("DeviceClass")
    private String deviceClass;
    @SerializedName("DeviceId")
    private String deviceId;
    @SerializedName("Scope")
    private String scope;

    public String getDataCollection() {
        return dataCollection;
    }

    public void setDataCollection(String dataCollection) {
        this.dataCollection = dataCollection;
    }

    public String getDeviceClass() {
        return deviceClass;
    }

    public void setDeviceClass(String deviceClass) {
        this.deviceClass = deviceClass;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

}
