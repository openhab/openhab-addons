package org.openhab.binding.kermi.internal.api;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Bundle {

    @SerializedName("DatapointBundleId")
    private String datapointBundleId;

    @SerializedName("Datapoints")
    private List<Datapoint> datapoints;

    @SerializedName("DisplayName")
    private String displayName;

    public String getDatapointBundleId() {
        return datapointBundleId;
    }

    public void setDatapointBundleId(String datapointBundleId) {
        this.datapointBundleId = datapointBundleId;
    }

    public List<Datapoint> getDatapoints() {
        return datapoints;
    }

    public void setDatapoints(List<Datapoint> datapoints) {
        this.datapoints = datapoints;
    }

}
