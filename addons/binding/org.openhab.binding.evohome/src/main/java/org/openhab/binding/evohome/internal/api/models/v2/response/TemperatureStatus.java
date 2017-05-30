package org.openhab.binding.evohome.internal.api.models.v2.response;

import com.google.gson.annotations.SerializedName;

public class TemperatureStatus {

    @SerializedName("temperature")
    public double temperature;

    @SerializedName("isAvailable")
    public boolean isAvailable;

}
