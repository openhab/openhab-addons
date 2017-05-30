package org.openhab.binding.evohome.internal.api.models.v2.response;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class Gateway {

    @SerializedName("gatewayInfo")
    public GatewayInfo gatewayInfo;

    @SerializedName("temperatureControlSystems")
    public List<TemperatureControlSystem> temperatureControlSystems;

}
