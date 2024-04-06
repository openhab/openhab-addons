package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class FanControl {
    @SerializedName("ref")
    private String ref;
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("value")
    private FanControlValue value;

    public String getRef() {
        return ref;
    }

    public boolean isSettable() {
        return settable;
    }

    public FanControlValue getValue() {
        return value;
    }
}
