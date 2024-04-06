package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class FanDirection {
    @SerializedName("vertical")
    private FanMovement vertical;
    @SerializedName("horizontal")
    private FanMovement horizontal;

    public FanMovement getHorizontal() {
        return horizontal;
    }

    public FanMovement getVertical() {
        return vertical;
    }
}
