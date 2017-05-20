package org.openhab.binding.evohome.internal.api.models.v2;

import com.google.gson.annotations.SerializedName;

public class ScheduleCapabilities {

    @SerializedName("maxSwitchpointsPerDay")
    public int MaxSwitchpointsPerDay;

    @SerializedName("minSwitchpointsPerDay")
    public int MinSwitchpointsPerDay;

    @SerializedName("setpointValueResolution")
    public double SetpointValueResolution;

    //TODO Should be of time time, format: 00:10:00
    @SerializedName("timingResolution")
    public String TimingResolution;

}
