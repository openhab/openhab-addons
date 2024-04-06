package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class TemperatureControl {
    @SerializedName("ref")
    private String ref;
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("value")
    private TemperatureControlValue value;

    public boolean isSettable() {
        return settable;
    }

    public String getRef() {
        return ref;
    }

    public TemperatureControlValue getValue() {
        return value;
    }
}
