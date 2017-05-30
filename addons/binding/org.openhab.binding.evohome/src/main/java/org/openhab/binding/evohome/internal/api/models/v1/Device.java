package org.openhab.binding.evohome.internal.api.models.v1;

public class Device {

    private int deviceId;
    private String name;
    private Thermostat thermostat;

    @Override
    public String toString() {
        return "DeviceId[" + deviceId + "] Name[" + name + "] Thermostat[" + thermostat + "]";
    }

    public int getDeviceId() {
        return deviceId;
    }

    public String getName() {
        return name;
    }

    public Thermostat getThermostat() {
        return thermostat;
    }

}
