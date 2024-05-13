package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class GatwaySubValueInteger {
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("value")
    private Integer value;
    @SerializedName("values")
    private Integer[] values;

    public boolean isSettable() {
        return settable;
    }

    public Integer getValue() {
        return value;
    }

    public Integer[] getValues() {
        return values;
    }
}
