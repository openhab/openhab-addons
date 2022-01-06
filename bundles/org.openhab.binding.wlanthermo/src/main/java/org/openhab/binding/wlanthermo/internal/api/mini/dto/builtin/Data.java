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
package org.openhab.binding.wlanthermo.internal.api.mini.dto.builtin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Data {

    @SerializedName("temp")
    @Expose
    private Double temp;
    @SerializedName("color")
    @Expose
    private String color;
    @SerializedName("state")
    @Expose
    private String state;
    @SerializedName("temp_min")
    @Expose
    private Double tempMin;
    @SerializedName("temp_max")
    @Expose
    private Double tempMax;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("alert")
    @Expose
    private Boolean alert;
    @SerializedName("show")
    @Expose
    private Boolean show;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Data() {
    }

    /**
     * 
     * @param tempMax
     * @param temp
     * @param color
     * @param alert
     * @param name
     * @param show
     * @param state
     * @param tempMin
     */
    public Data(Double temp, String color, String state, Double tempMin, Double tempMax, String name, Boolean alert,
            Boolean show) {
        super();
        this.temp = temp;
        this.color = color;
        this.state = state;
        this.tempMin = tempMin;
        this.tempMax = tempMax;
        this.name = name;
        this.alert = alert;
        this.show = show;
    }

    public Double getTemp() {
        return temp;
    }

    public void setTemp(Double temp) {
        this.temp = temp;
    }

    public Data withTemp(Double temp) {
        this.temp = temp;
        return this;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public Data withColor(String color) {
        this.color = color;
        return this;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Data withState(String state) {
        this.state = state;
        return this;
    }

    public Double getTempMin() {
        return tempMin;
    }

    public void setTempMin(Double tempMin) {
        this.tempMin = tempMin;
    }

    public Data withTempMin(Double tempMin) {
        this.tempMin = tempMin;
        return this;
    }

    public Double getTempMax() {
        return tempMax;
    }

    public void setTempMax(Double tempMax) {
        this.tempMax = tempMax;
    }

    public Data withTempMax(Double tempMax) {
        this.tempMax = tempMax;
        return this;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Data withName(String name) {
        this.name = name;
        return this;
    }

    public Boolean getAlert() {
        return alert;
    }

    public void setAlert(Boolean alert) {
        this.alert = alert;
    }

    public Data withAlert(Boolean alert) {
        this.alert = alert;
        return this;
    }

    public Boolean getShow() {
        return show;
    }

    public void setShow(Boolean show) {
        this.show = show;
    }

    public Data withShow(Boolean show) {
        this.show = show;
        return this;
    }
}
