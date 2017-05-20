package org.openhab.binding.evohome.internal.api.models.v2;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TemperatureControlSystem {

    @SerializedName("systemId")
    public int SystemId;

    @SerializedName("modelType")
    public String ModelType;

    @SerializedName("zones")
    public List<Zone> Zones;

    @SerializedName("allowedSystemModes")
    public List<Mode> AllowedSystemModes;

}
