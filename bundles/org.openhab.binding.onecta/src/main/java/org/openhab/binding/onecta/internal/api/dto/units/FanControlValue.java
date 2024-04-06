package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class FanControlValue {
    @SerializedName("operationModes")
    private FanOperationModes operationModes;

    public FanOperationModes getOperationModes() {
        return operationModes;
    }
}
