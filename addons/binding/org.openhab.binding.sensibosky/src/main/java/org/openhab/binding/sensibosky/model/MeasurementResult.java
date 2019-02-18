package org.openhab.binding.sensibosky.model;

import com.google.gson.annotations.SerializedName;

public class MeasurementResult {
    @SerializedName("temperatureUnit")
    public String temperatureUnit;
    @SerializedName("measurements")
    public Measurements measurements;
}
