package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class GatewayStatus {

    @SerializedName("gatewayId")
    public int GatewayId;

    @SerializedName("temperatureControlSystems")
    public List<TemperatureControlSystemStatus> TemperatureControlSystems;

    //"activeFaults": [],

}
