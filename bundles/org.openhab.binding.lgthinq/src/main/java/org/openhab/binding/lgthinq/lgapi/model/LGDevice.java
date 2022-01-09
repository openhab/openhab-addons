package org.openhab.binding.lgthinq.lgapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LGDevice {
    private String modelName;
    @JsonProperty("deviceType")
    private int deviceTypeId;
    private String deviceCode;
    private String alias;
    private String deviceId;
    private String platformType;

    public String getModelName() {
        return modelName;
    }

    @JsonIgnore
    public DeviceTypes getDeviceType() {
        return DeviceTypes.fromDeviceTypeId(deviceTypeId);
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public int getDeviceTypeId() {
        return deviceTypeId;
    }

    public void setDeviceTypeId(int deviceTypeId) {
        this.deviceTypeId = deviceTypeId;
    }

    public String getDeviceCode() {
        return deviceCode;
    }

    public void setDeviceCode(String deviceCode) {
        this.deviceCode = deviceCode;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPlatformType() {
        return platformType;
    }

    public void setPlatformType(String platformType) {
        this.platformType = platformType;
    }
}
