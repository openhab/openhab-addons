// package org.openhab.binding.evohome.internal.api;
//
// import org.codehaus.jackson.annotate.JsonIgnoreProperties;
// import org.codehaus.jackson.annotate.JsonProperty;
//
// @JsonIgnoreProperties(ignoreUnknown = true)
// public class DataModelResponse {
//
// @JsonProperty("locationID")
// private String locationId;
//
// @JsonProperty("name")
// private String name;
//
// @JsonProperty("devices")
// private Device[] devices;
//
// @JsonProperty("weather")
// private Weather weather;
//
// public String getLocationId() {
// return locationId;
// }
//
// public String getName() {
// return name;
// }
//
// public Device[] getDevices() {
// return devices;
// }
//
// public Weather getWeather() {
// return weather;
// }
//
// @Override
// public String toString() {
// StringBuilder builder = new StringBuilder();
// builder.append("locationId[" + locationId + "] name[" + name + "]\n");
// builder.append("device[").append("\n");
// for (Device device : devices) {
// builder.append(" ").append(device).append("\n");
// }
// builder.append("]\n");
// builder.append("weather[" + weather + "]");
//
// return builder.toString();
// }
//
// }
