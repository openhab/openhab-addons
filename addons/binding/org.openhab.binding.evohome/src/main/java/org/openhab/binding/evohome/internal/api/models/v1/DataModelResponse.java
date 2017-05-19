package org.openhab.binding.evohome.internal.api.models.v1;

import org.openhab.binding.evohome.internal.api.models.v1.Device;
import org.openhab.binding.evohome.internal.api.models.v1.Weather;

public class DataModelResponse {

    private String locationId;
    private String name;
    private Device[] devices;
    private Weather weather;

    public String getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public Device[] getDevices() {
        return devices;
    }

    public Weather getWeather() {
        return weather;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("locationId[" + locationId + "] name[" + name + "]\n");
        builder.append("device[").append("\n");
        for (Device device : devices) {
            builder.append(" ").append(device).append("\n");
        }
        builder.append("]\n");
        builder.append("weather[" + weather + "]");

        return builder.toString();
    }
}
