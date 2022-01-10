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

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link BloomSkyStormData} is the Java class used to map the JSON response to a BloomSky API request.
 *
 * @author dschoepel - Initial contribution
 *
 */
public class BloomSkyStormData {

    @SerializedName("UVIndex")
    private int uVIndex; // Calculated UV index (1-11+), this is more accurate than index from SKY device

    @SerializedName("WindDirection")
    private String windDirection; // Converted Wind direction from degrees to compass headings (S,SW,W,NW,N,NE,E,SE)

    @SerializedName("RainDaily")
    private double rainDaily; // Daily rain total for the past 24 hour period in inches/millimeters

    /*
     * Wind gust highest wind speed (peak speed in
     * a rolling 10 minute window) miles/hour or meters/second
     */
    @SerializedName("WindGust")
    private double windGust;

    /*
     * Sustained wind speed rolling two minute average (miles/hour or meters/second)
     */
    @SerializedName("SustainedWindSpeed")
    private double sustainedWindSpeed;

    @SerializedName("RainRate")
    private double rainRate; // Rain Rate 10 minutes of rainfall inches/millimeters

    /*
     * 24 hour rainfall (a rolling 24 hour window) inches/millimeters
     * The name for this key field was modified as the original JSON response
     * used an invalid name starting with a number "24hRain"
     */
    @SerializedName("24hRain")
    @Expose(deserialize = true)
    private double rain24h;

    public int getuVIndex() {
        return uVIndex;
    }

    public String getWindDirection() {
        return windDirection;
    }

    public double getRainDaily() {
        return rainDaily;
    }

    public double getWindGust() {
        return windGust;
    }

    public double getSustainedWindSpeed() {
        return sustainedWindSpeed;
    }

    public double getRainRate() {
        return rainRate;
    }

    public double getRain24h() {
        return rain24h;
    }

    @Override
    public String toString() {
        return "Storm{" + "uVIndex='" + uVIndex + '\'' + ", windDirection='" + windDirection + '\'' + ", rainDaily="
                + rainDaily + ", windGust=" + windGust + ", sustainedWindSpeed=" + sustainedWindSpeed + ", rainRate="
                + rainRate + ", rain24h=" + rain24h + '}';
    }
}
