package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class GatwaySubValueBoolean {
    @SerializedName("settable")
    private boolean settable;
    @SerializedName("value")
    private Boolean value;

    public boolean isSettable() {
        return settable;
    }

    public Boolean getValue() {
        return value;
    }
}
