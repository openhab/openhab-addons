package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Location {

    @SerializedName("locationInfo")
    public LocationInfo locationInfo;

    @SerializedName("gateways")
    public List<Gateway> gateways;
}
