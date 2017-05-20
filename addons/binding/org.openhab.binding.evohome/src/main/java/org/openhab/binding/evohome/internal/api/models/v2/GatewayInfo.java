package org.openhab.binding.evohome.internal.api.models.v2;

import com.google.gson.annotations.SerializedName;

public class GatewayInfo {

    @SerializedName("gatewayId")
    public int GatewayId;

    @SerializedName("mac")
    public String MacAddress;

    @SerializedName("crc")
    public String Crc;

    @SerializedName("isWiFi")
    public boolean IsWiFi;
}
