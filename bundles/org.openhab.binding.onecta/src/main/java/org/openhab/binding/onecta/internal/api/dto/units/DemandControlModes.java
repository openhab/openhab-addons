package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class DemandControlModes {
    @SerializedName("fixed")
    private DemandControlModesFixed fixedValues;
    // @SerializedName("scheduled")
    // private DemandControlModes scheduled;

    public DemandControlModesFixed getFixedValues() {
        return fixedValues;
    }
}
