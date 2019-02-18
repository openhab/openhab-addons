package org.openhab.binding.sensibosky.model;

import com.google.gson.annotations.SerializedName;

public class Measurements {
    @SerializedName("batteryVoltage")
    public String batteryVoltage;
    @SerializedName("temperature")
    public float temperature;
    @SerializedName("humidity")
    public float humidity;
}
