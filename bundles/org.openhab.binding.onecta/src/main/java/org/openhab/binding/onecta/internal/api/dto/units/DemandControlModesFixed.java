package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class DemandControlModesFixed {
    @SerializedName("stepValue")
    private Integer stepValue;
    @SerializedName("value")
    private Integer value;
    @SerializedName("minValue")
    private Integer minValue;
    @SerializedName("maxValue")
    private Integer maxValue;
    @SerializedName("settable")
    private Boolean settable;

    public Integer getStepValue() {
        return stepValue;
    }

    public Integer getValue() {
        return value;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public Boolean getSettable() {
        return settable;
    }
}
