package org.openhab.binding.evohome.internal.api.models.v2;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class HeatSetpointCapabilities {

    @SerializedName("maxHeatSetpoint")
    public double MaxHeatSetpoint;

    @SerializedName("minHeatSetpoint")
    public double MinHeatSetpoint;

    @SerializedName("valueResolution")
    public double ValueResolution;

    @SerializedName("allowedSetpointModes")
    public List<String> AllowedSetpointModes;

    //TODO Should be of time time, format: 1.00:00:00
    @SerializedName("maxDuration")
    public String MaxDuration;

    //TODO Should be of time time, format: 00:10:00
    @SerializedName("timingResolution")
    public String TimingResolution;

}
