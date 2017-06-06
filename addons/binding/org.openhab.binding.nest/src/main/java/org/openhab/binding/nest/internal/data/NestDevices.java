package org.openhab.binding.nest.internal.data;

import java.util.Map;

import com.google.gson.annotations.SerializedName;

/**
 * All the nest devices broken up by type.
 *
 * @author David Bennett
 */
public class NestDevices {
    @SerializedName("thermostats")
    private Map<String, Thermostat> thermostats;
    @SerializedName("smoke_co_alarms")
    private Map<String, SmokeDetector> smokeDetector;
    @SerializedName("cameras")
    private Map<String, Camera> camera;

    /** Id to thermostat mapping */
    public Map<String, Thermostat> getThermostats() {
        return thermostats;
    }

    /** Id to camera mapping */
    public Map<String, Camera> getCameras() {
        return camera;
    }

    /** Id to smoke detector */
    public Map<String, SmokeDetector> getSmokeDetectors() {
        return smokeDetector;
    }
}
