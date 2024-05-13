package org.openhab.binding.onecta.internal.api.dto.units;

import org.openhab.binding.onecta.internal.api.Enums;

import com.google.gson.annotations.SerializedName;

public class FanOperationModes {

    @SerializedName("heating")
    private FanOnlyClass heating;
    @SerializedName("cooling")
    private FanOnlyClass cooling;
    @SerializedName("auto")
    private FanOnlyClass auto;
    @SerializedName("dry")
    private FanOnlyClass dry;
    @SerializedName("fanOnly")
    private FanOnlyClass fanOnly;

    public FanOnlyClass getFanOperationMode(Enums.OperationMode operationMode) {
        if (operationMode.getValue() == Enums.OperationMode.HEAT.getValue()) {
            return this.heating;
        } else if (operationMode.getValue() == Enums.OperationMode.COLD.getValue()) {
            return this.cooling;
        } else if (operationMode.getValue() == Enums.OperationMode.AUTO.getValue()) {
            return this.auto;
        } else if (operationMode.getValue() == Enums.OperationMode.FAN.getValue()) {
            return this.fanOnly;
        } else if (operationMode.getValue() == Enums.OperationMode.DEHUMIDIFIER.getValue()) {
            return this.dry;
        } else
            return null;
    }

    public FanOnlyClass getHeating() {
        return heating;
    }

    public FanOnlyClass getCooling() {
        return cooling;
    }

    public FanOnlyClass getAuto() {
        return auto;
    }

    public FanOnlyClass getDry() {
        return dry;
    }

    public FanOnlyClass getFanOnly() {
        return fanOnly;
    }
}
