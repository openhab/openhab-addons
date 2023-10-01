package org.openhab.binding.kermi.internal.model;

import java.util.List;

import org.openhab.binding.kermi.internal.api.Datapoint;

import com.google.gson.annotations.SerializedName;

public class ListDatapointCacheFile {

    @SerializedName("DeviceId")
    private String deviceId;

    @SerializedName("Serial")
    private String serial;

    @SerializedName("Datapoints")
    private List<Datapoint> datapoints;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public List<Datapoint> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<Datapoint> datapoints) {
        this.datapoints = datapoints;
    }

}
