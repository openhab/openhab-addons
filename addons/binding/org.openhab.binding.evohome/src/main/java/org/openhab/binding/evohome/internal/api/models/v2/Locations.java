package org.openhab.binding.evohome.internal.api.models.v2;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Locations {

    @SerializedName("locationInfo")
    public LocationInfo LocationInfo;

    @SerializedName("gateways")
    public List<Gateway> Gateways;
}
