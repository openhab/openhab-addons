package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class ScheduleCapabilities {

    @SerializedName("maxSwitchpointsPerDay")
    public int maxSwitchpointsPerDay;

    @SerializedName("minSwitchpointsPerDay")
    public int minSwitchpointsPerDay;

    @SerializedName("setpointValueResolution")
    public double setpointValueResolution;

    //TODO Should be of time time, format: 00:10:00
    @SerializedName("timingResolution")
    public String timingResolution;

}
