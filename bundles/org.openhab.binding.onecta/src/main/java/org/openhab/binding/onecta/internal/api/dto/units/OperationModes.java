package org.openhab.binding.onecta.internal.api.dto.units;

import org.openhab.binding.onecta.internal.api.Enums;

import com.google.gson.annotations.SerializedName;

public class OperationModes {
    @SerializedName("heating")
    private OpertationMode heating;
    @SerializedName("cooling")
    private OpertationMode cooling;
    @SerializedName("auto")
    private OpertationMode auto;

    public OpertationMode getOperationMode(Enums.OperationMode operationMode) {
        if (operationMode.getValue() == Enums.OperationMode.HEAT.getValue()) {
            return this.heating;
        } else if (operationMode.getValue() == Enums.OperationMode.COLD.getValue()) {
            return this.cooling;
        } else if (operationMode.getValue() == Enums.OperationMode.AUTO.getValue()) {
            return this.auto;
        } else
            return null;
    }
}
