package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class HeatSetpointStatus {

    @SerializedName("TargetTemperature")
    public double TargetTemperature;

    @SerializedName("setpointMode")
    public String SetpointMode;

}
