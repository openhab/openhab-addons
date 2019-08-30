/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
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
package org.openhab.binding.gpstracker.internal.message.life360;

import com.google.gson.annotations.SerializedName;

import java.math.BigDecimal;

/**
 * The {@link Location} is a Life360 message POJO
 *
 * @author Gabor Bicskei - Initial contribution
 */
public class Location {

    @SerializedName("sourceId")
    private String sourceId;

    @SerializedName("charge")
    private String charge;

    @SerializedName("latitude")
    private String latitude;

    @SerializedName("wifiState")
    private String wifiState;

    @SerializedName("accuracy")
    private String accuracy;

    @SerializedName("battery")
    private String battery;

    @SerializedName("speed")
    private float speed;

    @SerializedName("name")
    private String name;

    @SerializedName("isDriving")
    private String isDriving;

    @SerializedName("longitude")
    private String longitude;

    @SerializedName("since")
    private int since;

    @SerializedName("timestamp")
    private String timestamp;

    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    public String getSourceId() {
        return sourceId;
    }

    public void setCharge(String charge) {
        this.charge = charge;
    }

    public String getCharge() {
        return charge;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public BigDecimal getLatitude() {
        return new BigDecimal(latitude);
    }

    public void setWifiState(String wifiState) {
        this.wifiState = wifiState;
    }

    public String getWifiState() {
        return wifiState;
    }

    public void setAccuracy(String accuracy) {
        this.accuracy = accuracy;
    }

    public BigDecimal getAccuracy() {
        return new BigDecimal(accuracy);
    }

    public void setBattery(String battery) {
        this.battery = battery;
    }

    public Integer getBattery() {
        return new Integer(battery);
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getSpeed() {
        return speed;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setIsDriving(String isDriving) {
        this.isDriving = isDriving;
    }

    public String getIsDriving() {
        return isDriving;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLongitude() {
        return new BigDecimal(longitude);
    }

    public void setSince(int since) {
        this.since = since;
    }

    public int getSince() {
        return since;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return new Long(timestamp);
    }

    @Override
    public String toString() {
        return
                "Location{" +
                        "sourceId = '" + sourceId + '\'' +
                        ",charge = '" + charge + '\'' +
                        ",latitude = '" + latitude + '\'' +
                        ",wifiState = '" + wifiState + '\'' +
                        ",accuracy = '" + accuracy + '\'' +
                        ",battery = '" + battery + '\'' +
                        ",speed = '" + speed + '\'' +
                        ",name = '" + name + '\'' +
                        ",isDriving = '" + isDriving + '\'' +
                        ",longitude = '" + longitude + '\'' +
                        ",since = '" + since + '\'' +
                        ",timestamp = '" + timestamp + '\'' +
                        "}";
    }
}
