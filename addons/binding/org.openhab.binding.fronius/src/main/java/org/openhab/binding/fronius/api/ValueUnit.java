package org.openhab.binding.fronius.api;

import com.google.gson.annotations.SerializedName;

public class ValueUnit {
    @SerializedName("Value")
    private float value;
    @SerializedName("Unit")
    private String unit;

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

}
