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
package org.openhab.binding.wlanthermo.internal.api.esp32.dto.settings;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Pid {

    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("aktor")
    @Expose
    private Integer aktor;
    @SerializedName("Kp")
    @Expose
    private Double kp;
    @SerializedName("Ki")
    @Expose
    private Double ki;
    @SerializedName("Kd")
    @Expose
    private Double kd;
    @SerializedName("DCmmin")
    @Expose
    private Double dCmmin;
    @SerializedName("DCmmax")
    @Expose
    private Double dCmmax;
    @SerializedName("opl")
    @Expose
    private Integer opl;
    @SerializedName("SPmin")
    @Expose
    private Double sPmin;
    @SerializedName("SPmax")
    @Expose
    private Double sPmax;
    @SerializedName("link")
    @Expose
    private Integer link;
    @SerializedName("tune")
    @Expose
    private Integer tune;
    @SerializedName("jp")
    @Expose
    private Integer jp;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getAktor() {
        return aktor;
    }

    public void setAktor(Integer aktor) {
        this.aktor = aktor;
    }

    public Double getKp() {
        return kp;
    }

    public void setKp(Double kp) {
        this.kp = kp;
    }

    public Double getKi() {
        return ki;
    }

    public void setKi(Double ki) {
        this.ki = ki;
    }

    public Double getKd() {
        return kd;
    }

    public void setKd(Double kd) {
        this.kd = kd;
    }

    public Double getDCmmin() {
        return dCmmin;
    }

    public void setDCmmin(Double dCmmin) {
        this.dCmmin = dCmmin;
    }

    public Double getDCmmax() {
        return dCmmax;
    }

    public void setDCmmax(Double dCmmax) {
        this.dCmmax = dCmmax;
    }

    public Integer getOpl() {
        return opl;
    }

    public void setOpl(Integer opl) {
        this.opl = opl;
    }

    public Double getSPmin() {
        return sPmin;
    }

    public void setSPmin(Double sPmin) {
        this.sPmin = sPmin;
    }

    public Double getSPmax() {
        return sPmax;
    }

    public void setSPmax(Double sPmax) {
        this.sPmax = sPmax;
    }

    public Integer getLink() {
        return link;
    }

    public void setLink(Integer link) {
        this.link = link;
    }

    public Integer getTune() {
        return tune;
    }

    public void setTune(Integer tune) {
        this.tune = tune;
    }

    public Integer getJp() {
        return jp;
    }

    public void setJp(Integer jp) {
        this.jp = jp;
    }
}
