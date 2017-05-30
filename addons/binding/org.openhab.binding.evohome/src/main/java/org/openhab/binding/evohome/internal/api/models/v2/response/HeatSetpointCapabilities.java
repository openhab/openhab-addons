package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class HeatSetpointCapabilities {

    @SerializedName("maxHeatSetpoint")
    public double maxHeatSetpoint;

    @SerializedName("minHeatSetpoint")
    public double minHeatSetpoint;

    @SerializedName("valueResolution")
    public double valueResolution;

    @SerializedName("allowedSetpointModes")
    public List<String> allowedSetpointModes;

    //TODO Should be of time time, format: 1.00:00:00
    @SerializedName("maxDuration")
    public String maxDuration;

    //TODO Should be of time time, format: 00:10:00
    @SerializedName("timingResolution")
    public String timingResolution;

}
