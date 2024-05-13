package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class SensoryData {
    @SerializedName("ref")
    private String ref;
    @SerializedName(value = "settable", alternate = "")
    private boolean settable;
    @SerializedName("value")
    private SensoryDataValue value;

    public String getRef() {
        return ref;
    }

    public boolean isSettable() {
        return settable;
    }

    public SensoryDataValue getValue() {
        return value;
    }
}
