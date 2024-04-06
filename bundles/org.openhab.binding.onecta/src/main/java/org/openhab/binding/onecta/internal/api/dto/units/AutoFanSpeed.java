package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class AutoFanSpeed {
    @SerializedName("currentMode")
    private FanCurrentMode currentMode;
    @SerializedName("modes")
    private ActionTypesModes modes;

    public ActionTypesModes getModes() {
        return modes;
    }

    public FanCurrentMode getCurrentMode() {
        return currentMode;
    }
}
