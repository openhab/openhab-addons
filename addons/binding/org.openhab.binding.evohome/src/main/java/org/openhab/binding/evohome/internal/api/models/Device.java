package org.openhab.binding.evohome.internal.api.models;

public class Device {

    //
    // import org.codehaus.jackson.annotate.JsonIgnoreProperties;
    // import org.codehaus.jackson.annotate.JsonProperty;
    //
    // @JsonIgnoreProperties(ignoreUnknown = true)
    // public class Device {
    //
    // @JsonProperty("deviceID")
    private int deviceId;
    //
    // @JsonProperty("name")
    private String name;
    //
    // @JsonProperty("thermostat")
    private Thermostat thermostat;

    //
    @Override
    public String toString() {
        return "DeviceId[" + deviceId + "] Name[" + name + "] Thermostat[" + thermostat + "]";
    }
    //
    // public int getDeviceId() {
    // return deviceId;
    // }
    //
    // public String getName() {
    // return name;
    // }
    //
    // public Thermostat getThermostat() {
    // return thermostat;
    // }
    //
    // }

}
