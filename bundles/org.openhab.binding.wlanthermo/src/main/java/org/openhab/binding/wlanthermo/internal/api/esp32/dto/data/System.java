/**
 * Copyright (c) 2010-2024 Contributors to the openHAB project
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
package org.openhab.binding.wlanthermo.internal.api.esp32.dto.data;

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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public Integer getSoc() {
        return soc;
    }

    public void setSoc(Integer soc) {
        this.soc = soc;
    }

    public Boolean getCharge() {
        return charge;
    }

    public void setCharge(Boolean charge) {
        this.charge = charge;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public Integer getOnline() {
        return online;
    }

    public void setOnline(Integer online) {
        this.online = online;
    }
}
