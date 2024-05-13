package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class FanCurrentMode {
    @SerializedName("value")
    private String value;
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("values")
    private String[] values;

    public boolean isSettable() {
        return settable;
    }

    public String[] getValues() {
        return values;
    }

    public String getValue() {
        return value;
    }
}
