/**
 * Copyright (c) 2010-2023 Contributors to the openHAB project
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
 * Be careful to not overwrite the getState/getTrigger function mapping the Data to OH channels!
 *
 * @author Christian Schlipp - Initial contribution
 */
public class App {

    @SerializedName("temp_unit")
    @Expose
    private String tempUnit;
    @SerializedName("pit")
    @Expose
    private Pit pit;
    @SerializedName("pit2")
    @Expose
    private Pit pit2;
    @SerializedName("cpu_load")
    @Expose
    private Double cpuLoad;
    @SerializedName("cpu_temp")
    @Expose
    private Double cpuTemp;
    @SerializedName("channel")
    @Expose
    private Channel channel;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;

    /**
     * No args constructor for use in serialization
     * 
     */
    public App() {
    }

    /**
     * 
     * @param cpuLoad
     * @param pit2
     * @param tempUnit
     * @param channel
     * @param pit
     * @param cpuTemp
     * @param timestamp
     */
    public App(String tempUnit, Pit pit, Pit pit2, Double cpuLoad, Double cpuTemp, Channel channel, String timestamp) {
        super();
        this.tempUnit = tempUnit;
        this.pit = pit;
        this.pit2 = pit2;
        this.cpuLoad = cpuLoad;
        this.cpuTemp = cpuTemp;
        this.channel = channel;
        this.timestamp = timestamp;
    }

    public String getTempUnit() {
        return tempUnit;
    }

    public void setTempUnit(String tempUnit) {
        this.tempUnit = tempUnit;
    }

    public App withTempUnit(String tempUnit) {
        this.tempUnit = tempUnit;
        return this;
    }

    public Pit getPit() {
        return pit;
    }

    public void setPit(Pit pit) {
        this.pit = pit;
    }

    public App withPit(Pit pit) {
        this.pit = pit;
        return this;
    }

    public Pit getPit2() {
        return pit2;
    }

    public void setPit2(Pit pit2) {
        this.pit2 = pit2;
    }

    public App withPit2(Pit pit2) {
        this.pit2 = pit2;
        return this;
    }

    public Double getCpuLoad() {
        return cpuLoad;
    }

    public void setCpuLoad(Double cpuLoad) {
        this.cpuLoad = cpuLoad;
    }

    public App withCpuLoad(Double cpuLoad) {
        this.cpuLoad = cpuLoad;
        return this;
    }

    public Double getCpuTemp() {
        return cpuTemp;
    }

    public void setCpuTemp(Double cpuTemp) {
        this.cpuTemp = cpuTemp;
    }

    public App withCpuTemp(Double cpuTemp) {
        this.cpuTemp = cpuTemp;
        return this;
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public App withChannel(Channel channel) {
        this.channel = channel;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public App withTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }
}
