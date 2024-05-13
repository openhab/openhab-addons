package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class FanSpeedFixed {
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("value")
    private Integer value;
    @SerializedName("maxValue")
    private Integer maxValue;
    @SerializedName("minValue")
    private Integer minValue;
    @SerializedName("stepValue")
    private Integer stepValue;
    @SerializedName("unit")
    private String unit;

    public boolean isSettable() {
        return settable;
    }

    public Integer getValue() {
        return value;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public Integer getStepValue() {
        return stepValue;
    }

    public String getUnit() {
        return unit;
    }
}
