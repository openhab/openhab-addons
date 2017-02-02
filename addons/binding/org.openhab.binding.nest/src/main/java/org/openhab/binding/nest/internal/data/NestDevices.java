package org.openhab.binding.nest.internal.data;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class NestDevices {
    @SerializedName("thermostate")
    private List<Thermostat> thermostats;
    @SerializedName("smoke_co_alarms")
    private List<SmokeDetector> smokeDetector;
    @SerializedName("cameras")
    private List<Camera> camera;

    public List<Thermostat> getThermostats() {
        return thermostats;
    }
}
