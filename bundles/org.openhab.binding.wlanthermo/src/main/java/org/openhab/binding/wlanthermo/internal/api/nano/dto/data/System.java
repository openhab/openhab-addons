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
package org.openhab.binding.wlanthermo.internal.api.nano.dto.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class System {

    @SerializedName("time")
    @Expose
    private String time;
    @SerializedName("unit")
    @Expose
    private String unit;
    @SerializedName("soc")
    @Expose
    private Integer soc;
    @SerializedName("charge")
    @Expose
    private Boolean charge;
    @SerializedName("rssi")
    @Expose
    private Integer rssi;
    @SerializedName("online")
    @Expose
    private Integer online;

    /**
     * No args constructor for use in serialization
     * 
     */
    public System() {
    }

    /**
     * 
     * @param unit
     * @param rssi
     * @param charge
     * @param soc
     * @param online
     * @param time
     */
    public System(String time, String unit, Integer soc, Boolean charge, Integer rssi, Integer online) {
        super();
        this.time = time;
        this.unit = unit;
        this.soc = soc;
        this.charge = charge;
        this.rssi = rssi;
        this.online = online;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public System withTime(String time) {
        this.time = time;
        return this;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public System withUnit(String unit) {
        this.unit = unit;
        return this;
    }

    public Integer getSoc() {
        return soc;
    }

    public void setSoc(Integer soc) {
        this.soc = soc;
    }

    public System withSoc(Integer soc) {
        this.soc = soc;
        return this;
    }

    public Boolean getCharge() {
        return charge;
    }

    public void setCharge(Boolean charge) {
        this.charge = charge;
    }

    public System withCharge(Boolean charge) {
        this.charge = charge;
        return this;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public System withRssi(Integer rssi) {
        this.rssi = rssi;
        return this;
    }

    public Integer getOnline() {
        return online;
    }

    public void setOnline(Integer online) {
        this.online = online;
    }

    public System withOnline(Integer online) {
        this.online = online;
        return this;
    }
}
