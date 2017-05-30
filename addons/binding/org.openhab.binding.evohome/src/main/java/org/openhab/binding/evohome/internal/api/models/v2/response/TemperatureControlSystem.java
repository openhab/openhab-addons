package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class TemperatureControlSystem {

    @SerializedName("systemId")
    public int systemId;

    @SerializedName("modelType")
    public String modelType;

    @SerializedName("zones")
    public List<Zone> zones;

    @SerializedName("allowedSystemModes")
    public List<Mode> allowedSystemModes;

}
