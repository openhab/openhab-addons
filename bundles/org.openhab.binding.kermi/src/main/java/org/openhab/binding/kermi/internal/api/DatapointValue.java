package org.openhab.binding.kermi.internal.api;

import com.google.gson.annotations.SerializedName;

public class DatapointValue {

    @SerializedName("Value")
    private Object value;

    @SerializedName("DatapointConfigId")
    private String datapointConfigId;

    @SerializedName("DeviceId")
    private String deviceId;

    @SerializedName("Flags")
    private int flags;

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getDatapointConfigId() {
        return datapointConfigId;
    }

    public void setDatapointConfigId(String datapointConfigId) {
        this.datapointConfigId = datapointConfigId;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

}
