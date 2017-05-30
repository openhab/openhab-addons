package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TemperatureControlSystemStatus {

    @SerializedName("systemId")
    public int systemId;

    @SerializedName("systemModeStatus")
    public SystemModeStatus mode;

    @SerializedName("zones")
    public List<ZoneStatus> zones;

    //"activeFaults": [],

}
