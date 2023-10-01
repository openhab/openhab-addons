package org.openhab.binding.kermi.internal.api;

import com.google.gson.annotations.SerializedName;

public class Datapoint {

    @SerializedName("Config")
    private Config config;

    @SerializedName("DatapointValue")
    private DatapointValue datapointValue;

    public Config getConfig() {
        return config;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public DatapointValue getDatapointValue() {
        return datapointValue;
    }

    public void setDatapointValue(DatapointValue datapointValue) {
        this.datapointValue = datapointValue;
    }

}
