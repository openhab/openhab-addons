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

import com.google.gson.annotations.Expose;
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
    @Expose
    private Double uTC; // Time zone offset. This could also be an integer.

    @SerializedName("CityName")
    @Expose
    private String cityName; // City name description given to the BloomSky PWS by device owner

    /*
     * The Storm object sensor data if not null a Storm is associated
     * with the device account.
     */
    @SerializedName("Storm")
    @Expose
    private BloomSkyStormData storm;

    /*
     * Searchable i.e. public can find this PWS on BloomSky map of weather stations,
     * value is true or false.
     */
    @SerializedName("Searchable")
    @Expose
    private String searchable;

    @SerializedName("DeviceName")
    @Expose
    private String deviceName; // Device name - Owner assigned name for the device

    /*
     * Registration time stamp - date and time when device was first registered with BloomSky account
     * Epoch format (Unix time stamp)
     */
    @SerializedName("RegisterTime")
    @Expose
    private Long registerTime;

    @SerializedName("DST")
    @Expose
    private Integer dST; // Daylight savings time on (1) or off (0)

    @SerializedName("BoundedPoint")
    @Expose
    private String boundedPoint; // Bounded point - related to indoor devices no longer supported - should be null

    @SerializedName("LON")
    @Expose
    private Double lON; // Longitude of the PWS

    @SerializedName("Point")
    @Expose
    private BloomSkyPointData point; // Point - related to indoor devices no longer supported - should be null

    /*
     * Video list [Array]- URLs to last five days of video time lapse mp4's in Fahrenheit
     */
    @SerializedName("VideoList")
    @Expose
    private List<String> videoList = null;

    /*
     * Video list [Array]- URLs to last five days of video time lapse mp4's in Celsius
     */
    @SerializedName("VideoList_C")
    @Expose
    private List<String> videoListC = null;

    @SerializedName("DeviceID")
    @Expose
    private String deviceID; // Device unique identifier

    @SerializedName("NumOfFollowers")
    @Expose
    private Integer numOfFollowers; // Number of followers that have this PWS as a favorite

    @SerializedName("LAT")
    @Expose
    private Double lAT; // Latitude of the PWS

    @SerializedName("ALT")
    @Expose
    private Double aLT; // Altitude of the PWS

    @SerializedName("Data")
    @Expose
    private BloomSkySkyData data; // Sensor Data for SKY1/2

    @SerializedName("FullAddress")
    @Expose
    private String fullAddress; // Full location address

    @SerializedName("StreetName")
    @Expose
    private String streetName; // Street name assigned by owner

    /*
     * Image snapshots list [Array]- URLs to last five image snapshots that make up the days video
     */
    @SerializedName("PreviewImageList")
    @Expose
    private List<String> previewImageList = null;

    public Double getuTC() {
        return uTC;
    }

    public void setuTC(Double uTC) {
        this.uTC = uTC;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public BloomSkyStormData getStorm() {
        return storm;
    }

    public void setStorm(BloomSkyStormData storm) {
        this.storm = storm;
    }

    public String getSearchable() {
        return searchable;
    }

    public void setSearchable(String searchable) {
        this.searchable = searchable;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public Long getRegisterTime() {
        return registerTime;
    }

    public void setRegisterTime(Long registerTime) {
        this.registerTime = registerTime;
    }

    public Integer getdST() {
        return dST;
    }

    public void setdST(Integer dST) {
        this.dST = dST;
    }

    public String getBoundedPoint() {
        return boundedPoint;
    }

    public void setBoundedPoint(String boundedPoint) {
        this.boundedPoint = boundedPoint;
    }

    public Double getlON() {
        return lON;
    }

    public void setlON(Double lON) {
        this.lON = lON;
    }

    public BloomSkyPointData getPoint() {
        return point;
    }

    public void setPoint(BloomSkyPointData point) {
        this.point = point;
    }

    public List<String> getVideoList() {
        return videoList;
    }

    public void setVideoList(List<String> videoList) {
        this.videoList = videoList;
    }

    public List<String> getVideoListC() {
        return videoListC;
    }

    public void setVideoListC(List<String> videoListC) {
        this.videoListC = videoListC;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public Integer getNumOfFollowers() {
        return numOfFollowers;
    }

    public void setNumOfFollowers(Integer numOfFollowers) {
        this.numOfFollowers = numOfFollowers;
    }

    public Double getlAT() {
        return lAT;
    }

    public void setlAT(Double lAT) {
        this.lAT = lAT;
    }

    public Double getaLT() {
        return aLT;
    }

    public void setaLT(Double aLT) {
        this.aLT = aLT;
    }

    public BloomSkySkyData getData() {
        return data;
    }

    public void setData(BloomSkySkyData data) {
        this.data = data;
    }

    public String getFullAddress() {
        return fullAddress;
    }

    public void setFullAddress(String fullAddress) {
        this.fullAddress = fullAddress;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public List<String> getPreviewImageList() {
        return previewImageList;
    }

    public void setPreviewImageList(List<String> previewImageList) {
        this.previewImageList = previewImageList;
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
