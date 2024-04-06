package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class IconID {
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("value")
    private Float value;
    @SerializedName("maxValue")
    private Float maxValue;
    @SerializedName("minValue")
    private Float minValue;
    @SerializedName("stepValue")
    private Float stepValue;
    @SerializedName("unit")
    private String unit;

    public boolean isSettable() {
        return settable;
    }

    public Float getValue() {
        return value;
    }

    public Float getMaxValue() {
        return maxValue;
    }

    public Float getMinValue() {
        return minValue;
    }

    public Float getStepValue() {
        return stepValue;
    }

    public String getUnit() {
        return unit;
    }
}
