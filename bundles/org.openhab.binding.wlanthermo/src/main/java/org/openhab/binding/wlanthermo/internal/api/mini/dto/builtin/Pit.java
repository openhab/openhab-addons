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
package org.openhab.binding.wlanthermo.internal.api.mini.dto.builtin;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Pit {

    @SerializedName("enabled")
    @Expose
    private Boolean enabled;
    @SerializedName("timestamp")
    @Expose
    private String timestamp;
    @SerializedName("setpoint")
    @Expose
    private Double setpoint;
    @SerializedName("current")
    @Expose
    private Double current;
    @SerializedName("control_out")
    @Expose
    private Integer controlOut;
    @SerializedName("ch")
    @Expose
    private Integer ch;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("open_lid")
    @Expose
    private String openLid;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Pit() {
    }

    /**
     * 
     * @param current
     * @param setpoint
     * @param ch
     * @param openLid
     * @param controlOut
     * @param type
     * @param enabled
     * @param timestamp
     */
    public Pit(Boolean enabled, String timestamp, Double setpoint, Double current, Integer controlOut, Integer ch,
            String type, String openLid) {
        super();
        this.enabled = enabled;
        this.timestamp = timestamp;
        this.setpoint = setpoint;
        this.current = current;
        this.controlOut = controlOut;
        this.ch = ch;
        this.type = type;
        this.openLid = openLid;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Pit withEnabled(Boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Pit withTimestamp(String timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Double getSetpoint() {
        return setpoint;
    }

    public void setSetpoint(Double setpoint) {
        this.setpoint = setpoint;
    }

    public Pit withSetpoint(Double setpoint) {
        this.setpoint = setpoint;
        return this;
    }

    public Double getCurrent() {
        return current;
    }

    public void setCurrent(Double current) {
        this.current = current;
    }

    public Pit withCurrent(Double current) {
        this.current = current;
        return this;
    }

    public Integer getControlOut() {
        return controlOut;
    }

    public void setControlOut(Integer controlOut) {
        this.controlOut = controlOut;
    }

    public Pit withControlOut(Integer controlOut) {
        this.controlOut = controlOut;
        return this;
    }

    public Integer getCh() {
        return ch;
    }

    public void setCh(Integer ch) {
        this.ch = ch;
    }

    public Pit withCh(Integer ch) {
        this.ch = ch;
        return this;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Pit withType(String type) {
        this.type = type;
        return this;
    }

    public String getOpenLid() {
        return openLid;
    }

    public void setOpenLid(String openLid) {
        this.openLid = openLid;
    }

    public Pit withOpenLid(String openLid) {
        this.openLid = openLid;
        return this;
    }
}
