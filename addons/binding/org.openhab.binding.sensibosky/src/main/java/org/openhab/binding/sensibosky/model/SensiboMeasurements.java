package org.openhab.binding.sensibosky.model;

import com.google.gson.annotations.SerializedName;

public class SensiboMeasurements {
    @SerializedName("status")
    public String status;
    @SerializedName("result")
    public MeasurementResult result;
}
