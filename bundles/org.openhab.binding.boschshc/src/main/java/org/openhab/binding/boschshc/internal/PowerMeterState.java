package org.openhab.binding.boschshc.internal;

import com.google.gson.annotations.SerializedName;

public class PowerMeterState {

    @SerializedName("@type")
    String type;

    double energyConsumption;
    double powerConsumption;
}
