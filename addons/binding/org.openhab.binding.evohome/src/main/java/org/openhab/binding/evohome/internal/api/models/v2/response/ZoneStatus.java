package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class ZoneStatus {

    @SerializedName("zoneId")
    public int zoneId;

    @SerializedName("name")
    public String name;

    @SerializedName("temperatureStatus")
    public TemperatureStatus temperature;

    @SerializedName("heatSetpointStatus")
    public HeatSetpointStatus heatSetpoint;

  //"activeFaults": [],

}
