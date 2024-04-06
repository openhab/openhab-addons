package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class GatwaySubValueString {
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("value")
    private String value;
    @SerializedName("values")
    private String[] values;

    public boolean isSettable() {
        return settable;
    }

    public String getValue() {
        return value;
    }

    public String[] getValues() {
        return values;
    }
}
