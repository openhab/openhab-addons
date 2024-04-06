package org.openhab.binding.onecta.internal.api.dto.units;

import com.google.gson.annotations.SerializedName;

public class TemperatureControlValue {
    @SerializedName("operationModes")
    private OperationModes operationModes;

    public OperationModes getOperationModes() {
        return operationModes;
    }
}
