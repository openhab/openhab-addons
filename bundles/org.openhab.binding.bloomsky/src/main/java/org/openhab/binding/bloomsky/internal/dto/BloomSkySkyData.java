/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.bloomsky.internal.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link BloomSkySkyData} is the Java class used to map the JSON response to an BloomSky API request.
 *
 * @author dschoepel - Initial contribution
 *
 */
public class BloomSkySkyData {

    /*
     * Luminance - the measurable quality of light that most closely
     * corresponds to brightness given in candelas per square meter cd/m2
     */
    @SerializedName("Luminance")
    @Expose
    private Integer luminance;

    @SerializedName("Temperature")
    @Expose
    private Double temperature; // Outside temperature in Fahrenheit or Celsius

    @SerializedName("ImageURL")
    @Expose
    private String imageURL; // Image URL - URL to JPG snapshot image from the Sky camera at Image time stamp

    @SerializedName("TS")
    @Expose
    private Long tS; // Observation time stamp in epoch format (Unix time stamp)

    @SerializedName("Rain")
    @Expose
    private String rain; // Rain detected true or false (is it raining)

    @SerializedName("Humidity")
    @Expose
    private Double humidity; // Humidity given as a percentage

    @SerializedName("Pressure")
    @Expose
    private Double pressure; // Atmospheric pressure inHG or mbar

    /*
     * Device type (model) SKY1 or SKY2. The only difference is that the
     * SKY2 is a newer model that includes bluetooth connection in addition
     * to WiFi.
     */
    @SerializedName("DeviceType")
    @Expose
    private String deviceType;

    /*
     * Battery voltage to indicate level of charge anything over 2600 mv
     * is considered 100% charged. The device is charged by a small solar
     * panel.
     */
    @SerializedName("Voltage")
    @Expose
    private Integer voltage;

    /*
     * After sunset (night) indicator true or false. The PWS does not take
     * snapshot images during the night.
     */
    @SerializedName("Night")
    @Expose
    private String night;
    /*
     * Calculated UV index (1 - 11+). The Storm UV Index is considered more accurate
     * than SKY. If a Storm is installed, the Storm UV Index should be used.
     */
    @SerializedName("UVIndex")
    @Expose
    private String uVIndex;

    @SerializedName("ImageTS")
    @Expose
    private Long imageTS; // Image time stamp in epoch format (Unix time stamp)

    public Integer getLuminance() {
        return luminance;
    }

    public void setLuminance(Integer luminance) {
        this.luminance = luminance;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public Long gettS() {
        return tS;
    }

    public void settS(Long tS) {
        this.tS = tS;
    }

    public String getRain() {
        return rain;
    }

    public void setRain(String rain) {
        this.rain = rain;
    }

    public Double getHumidity() {
        return humidity;
    }

    public void setHumidity(Double humidity) {
        this.humidity = humidity;
    }

    public Double getPressure() {
        return pressure;
    }

    public void setPressure(Double pressure) {
        this.pressure = pressure;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public Integer getVoltage() {
        return voltage;
    }

    public void setVoltage(Integer voltage) {
        this.voltage = voltage;
    }

    public String getNight() {
        return night;
    }

    public void setNight(String night) {
        this.night = night;
    }

    public String getuVIndex() {
        return uVIndex;
    }

    public void setuVIndex(String uVIndex) {
        this.uVIndex = uVIndex;
    }

    public Long getImageTS() {
        return imageTS;
    }

    public void setImageTS(Long imageTS) {
        this.imageTS = imageTS;
    }

    @Override
    public String toString() {
        return "Data{" + "luminance=" + luminance + ", temperature=" + temperature + ", imageURL='" + imageURL + '\''
                + ", tS=" + tS + ", rain=" + rain + ", humidity=" + humidity + ", pressure=" + pressure
                + ", deviceType='" + deviceType + '\'' + ", voltage=" + voltage + ", night=" + night + ", uVIndex='"
                + uVIndex + '\'' + ", imageTS=" + imageTS + '}';
    }
}
