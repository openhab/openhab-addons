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

import java.util.List;

import com.google.gson.annotations.SerializedName;

/**
 * The {@link BloomSkyJsonSensorData} is the Java class used to map the JSON response to an BloomSky API request.
 *
 * @author dschoepel - Initial contribution
 *
 */
public class BloomSkyJsonSensorData {
    /* --------- Device information follows, then sensor data ------------------ */
    @SerializedName("UTC")
    private double uTC; // Time zone offset. This could also be an integer.

    @SerializedName("CityName")
    private String cityName; // City name description given to the BloomSky PWS by device owner

    /*
     * The Storm object sensor data if not null a Storm is associated
     * with the device account.
     */
    @SerializedName("Storm")
    private BloomSkyStormData storm;

    /*
     * Searchable i.e. public can find this PWS on BloomSky map of weather stations,
     * value is true or false.
     */
    @SerializedName("Searchable")
    private String searchable;

    @SerializedName("DeviceName")
    private String deviceName; // Device name - Owner assigned name for the device

    /*
     * Registration time stamp - date and time when device was first registered with BloomSky account
     * Epoch format (Unix time stamp)
     */
    @SerializedName("RegisterTime")
    private long registerTime;

    @SerializedName("DST")
    private int dST; // Daylight savings time on (1) or off (0)

    @SerializedName("BoundedPoint")
    private String boundedPoint; // Bounded point - related to indoor devices no longer supported - should be null

    @SerializedName("LON")
    private double lON; // Longitude of the PWS

    @SerializedName("Point")
    private BloomSkyPointData point; // Point - related to indoor devices no longer supported - should be null

    /*
     * Video list [Array]- URLs to last five days of video time lapse mp4's in Fahrenheit
     */
    @SerializedName("VideoList")
    private List<String> videoList = null;

    /*
     * Video list [Array]- URLs to last five days of video time lapse mp4's in Celsius
     */
    @SerializedName("VideoList_C")
    private List<String> videoListC = null;

    @SerializedName("DeviceID")
    private String deviceID; // Device unique identifier

    @SerializedName("NumOfFollowers")
    private int numOfFollowers; // Number of followers that have this PWS as a favorite

    @SerializedName("LAT")
    private double lAT; // Latitude of the PWS

    @SerializedName("ALT")
    private double aLT; // Altitude of the PWS

    @SerializedName("Data")
    private BloomSkySkyData data; // Sensor Data for SKY1/2

    @SerializedName("FullAddress")
    private String fullAddress; // Full location address

    @SerializedName("StreetName")
    private String streetName; // Street name assigned by owner

    /*
     * Image snapshots list [Array]- URLs to last five image snapshots that make up the days video
     */
    @SerializedName("PreviewImageList")
    private List<String> previewImageList = null;

    public double getuTC() {
        return uTC;
    }

    public String getCityName() {
        return cityName;
    }

    public BloomSkyStormData getStorm() {
        return storm;
    }

    public String getSearchable() {
        return searchable;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public long getRegisterTime() {
        return registerTime;
    }

    public int getdST() {
        return dST;
    }

    public String getBoundedPoint() {
        return boundedPoint;
    }

    public double getlON() {
        return lON;
    }

    public BloomSkyPointData getPoint() {
        return point;
    }

    public List<String> getVideoList() {
        return videoList;
    }

    public List<String> getVideoListC() {
        return videoListC;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public int getNumOfFollowers() {
        return numOfFollowers;
    }

    public double getlAT() {
        return lAT;
    }

    public double getaLT() {
        return aLT;
    }

    public BloomSkySkyData getData() {
        return data;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public String getStreetName() {
        return streetName;
    }

    public List<String> getPreviewImageList() {
        return previewImageList;
    }

    @Override
    public String toString() {
        return "BloomSkyObservationsDTO{" + "\nuTC=" + uTC + ", \ncityName='" + cityName + '\'' + ", \nstorm=" + storm
                + ", \nsearchable=" + searchable + ", \ndeviceName='" + deviceName + '\'' + ", \nregisterTime="
                + registerTime + ", \ndST=" + dST + ", \nboundedPoint='" + boundedPoint + '\'' + ", \nlON=" + lON
                + ", \npoint=" + point + ", \nvideoList=" + videoList + ", \nvideoListC=" + videoListC
                + ", \ndeviceID='" + deviceID + '\'' + ", \nnumOfFollowers=" + numOfFollowers + ", \nlAT=" + lAT
                + ", \naLT=" + aLT + ", \ndata=" + data + ", \nfullAddress='" + fullAddress + '\'' + ", \nstreetName='"
                + streetName + '\'' + ", \npreviewImageList=" + previewImageList + '}';
    }
}
