/**
 * Copyright (c) 2010-2022 Contributors to the openHAB project
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

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BloomSkySkyData} is the Java class used to map the JSON response to an BloomSky API request.
 *
 * @author Dave J Schoepel - Initial contribution
 *
 */
public class BloomSkySkyData {

    /*
     * Luminance - the measurable quality of light that most closely
     * corresponds to brightness given in candelas per square meter cd/m2
     */
    @SerializedName("Luminance")
    private int luminance;

    @SerializedName("Temperature")
    private double temperature; // Outside temperature in Fahrenheit or Celsius

    @SerializedName("ImageURL")
    private String imageURL; // Image URL - URL to JPG snapshot image from the Sky camera at Image time stamp

    @SerializedName("TS")
    private long tS; // Observation time stamp in epoch format (Unix time stamp)

    @SerializedName("Rain")
    private String rain; // Rain detected true or false (is it raining)

    @SerializedName("Humidity")
    private double humidity; // Humidity given as a percentage

    @SerializedName("Pressure")
    private double pressure; // Atmospheric pressure inHG or mbar

    /*
     * Device type (model) SKY1 or SKY2. The only difference is that the
     * SKY2 is a newer model that includes bluetooth connection in addition
     * to WiFi.
     */
    @SerializedName("DeviceType")
    private String deviceType;

    /*
     * Battery voltage to indicate level of charge anything over 2600 mv
     * is considered 100% charged. The device is charged by a small solar
     * panel.
     */
    @SerializedName("Voltage")
    private double voltage;

    /*
     * After sunset (night) indicator true or false. The PWS does not take
     * snapshot images during the night.
     */
    @SerializedName("Night")
    private String night;
    /*
     * Calculated UV index (1 - 11+). The Storm UV Index is considered more accurate
     * than SKY. If a Storm is installed, the Storm UV Index should be used.
     */
    @SerializedName("UVIndex")
    private int uVIndex;

    @SerializedName("ImageTS")
    private long imageTS; // Image time stamp in epoch format (Unix time stamp)

    public int getLuminance() {
        return luminance;
    }

    public double getTemperature() {
        return temperature;
    }

    public String getImageURL() {
        return imageURL;
    }

    public long gettS() {
        return tS;
    }

    public String getRain() {
        return rain;
    }

    public double getHumidity() {
        return humidity;
    }

    public double getPressure() {
        return pressure;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public double getVoltage() {
        return voltage;
    }

    public String getNight() {
        return night;
    }

    public int getuVIndex() {
        return uVIndex;
    }

    public long getImageTS() {
        return imageTS;
    }

    @Override
    public String toString() {
        return "Data{" + "luminance=" + luminance + ", temperature=" + temperature + ", imageURL='" + imageURL + '\''
                + ", tS=" + tS + ", rain=" + rain + ", humidity=" + humidity + ", pressure=" + pressure
                + ", deviceType='" + deviceType + '\'' + ", voltage=" + voltage + ", night=" + night + ", uVIndex='"
                + uVIndex + '\'' + ", imageTS=" + imageTS + '}';
    }
}
