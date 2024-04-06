package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class FanOnlyClass {
    @SerializedName("fanSpeed")
    private AutoFanSpeed fanSpeed;
    @SerializedName("fanDirection")
    private FanDirection fanDirection;

    public AutoFanSpeed getFanSpeed() {
        return fanSpeed;
    }

    public FanDirection getFanDirection() {
        return fanDirection;
    }
}
