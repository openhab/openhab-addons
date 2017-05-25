package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class LocationStatus {

    @SerializedName("locationId")
    public int locationId;

    @SerializedName("gateways")
    public List<GatewayStatus> gateways;

}
