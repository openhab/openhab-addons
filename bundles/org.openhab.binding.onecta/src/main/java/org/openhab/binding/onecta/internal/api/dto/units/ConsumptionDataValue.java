package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class ConsumptionDataValue {
    @SerializedName("electrical")
    private Electrical electrical;

    public Electrical getElectrical() {
        return electrical;
    }
}
