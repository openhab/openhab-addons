package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class GatewayInfo {

    @SerializedName("gatewayId")
    public int gatewayId;

    @SerializedName("mac")
    public String macAddress;

    @SerializedName("crc")
    public String crc;

    @SerializedName("isWiFi")
    public boolean isWifi;
}
