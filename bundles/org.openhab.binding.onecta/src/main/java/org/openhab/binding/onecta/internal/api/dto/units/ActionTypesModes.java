package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class ActionTypesModes {
    @SerializedName("fixed")
    private FanSpeedFixed fixed;

    public FanSpeedFixed getFixed() {
        return fixed;
    }
}
