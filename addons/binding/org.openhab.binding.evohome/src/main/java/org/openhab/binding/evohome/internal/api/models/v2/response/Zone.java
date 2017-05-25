package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class Zone {

    @SerializedName("zoneId")
    public int zoneId;

    @SerializedName("modelType")
    public String modelType;

    @SerializedName("name")
    public String name;

    @SerializedName("zoneType")
    public String zoneType;

    @SerializedName("heatSetpointCapabilities")
    public HeatSetpointCapabilities heatSetpointCapabilities;

    @SerializedName("scheduleCapabilities")
    public ScheduleCapabilities scheduleCapabilities;

}
