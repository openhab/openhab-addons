package org.openhab.binding.sensibosky.model;

import com.google.gson.annotations.SerializedName;

public class AcState {
    @SerializedName("on")
    public boolean on;
    @SerializedName("mode")
    public String mode;
    @SerializedName("fanLevel")
    public String fanLevel;
    @SerializedName("targetTemperature")
    public int targetTemperature;
    @SerializedName("temperatureUnit")
    public String temperatureUnit;
    @SerializedName("swing")
    public String swing;
}
