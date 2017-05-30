package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class GatewayStatus {

    @SerializedName("gatewayId")
    public int gatewayId;

    @SerializedName("temperatureControlSystems")
    public List<TemperatureControlSystemStatus> temperatureControlSystems;

    //"activeFaults": [],

}
