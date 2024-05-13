package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class Electrical {
    @SerializedName("unit")
    private String unit;
    @SerializedName("heating")
    private Ing heating;
    @SerializedName("cooling")
    private Ing cooling;

    public String getUnit() {
        return unit;
    }

    public Ing getHeating() {
        return heating;
    }

    public Ing getCooling() {
        return cooling;
    }
}
