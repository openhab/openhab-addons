package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class OpertationMode {
    @SerializedName("setpoints")
    private Setpoints setpoints;

    public Setpoints getSetpoints() {
        return setpoints;
    }
}
