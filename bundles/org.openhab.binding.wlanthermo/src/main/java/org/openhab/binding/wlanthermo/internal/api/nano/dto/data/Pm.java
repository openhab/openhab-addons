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
package org.openhab.binding.wlanthermo.internal.api.nano.dto.data;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Pm {

    @SerializedName("id")
    @Expose
    private Integer id;
    @SerializedName("channel")
    @Expose
    private Integer channel;
    @SerializedName("pid")
    @Expose
    private Integer pid;
    @SerializedName("value")
    @Expose
    private Integer value;
    @SerializedName("set")
    @Expose
    private Double set;
    @SerializedName("typ")
    @Expose
    private String typ;
    @SerializedName("set_color")
    @Expose
    private String setColor;
    @SerializedName("value_color")
    @Expose
    private String valueColor;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Pm() {
    }

    /**
     * 
     * @param set
     * @param setColor
     * @param channel
     * @param pid
     * @param typ
     * @param id
     * @param value
     * @param valueColor
     */
    public Pm(Integer id, Integer channel, Integer pid, Integer value, Double set, String typ, String setColor,
            String valueColor) {
        super();
        this.id = id;
        this.channel = channel;
        this.pid = pid;
        this.value = value;
        this.set = set;
        this.typ = typ;
        this.setColor = setColor;
        this.valueColor = valueColor;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Pm withId(Integer id) {
        this.id = id;
        return this;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Pm withChannel(Integer channel) {
        this.channel = channel;
        return this;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Pm withPid(Integer pid) {
        this.pid = pid;
        return this;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Pm withValue(Integer value) {
        this.value = value;
        return this;
    }

    public Double getSet() {
        return set;
    }

    public void setSet(Double set) {
        this.set = set;
    }

    public Pm withSet(Double set) {
        this.set = set;
        return this;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public Pm withTyp(String typ) {
        this.typ = typ;
        return this;
    }

    public String getSetColor() {
        return setColor;
    }

    public void setSetColor(String setColor) {
        this.setColor = setColor;
    }

    public Pm withSetColor(String setColor) {
        this.setColor = setColor;
        return this;
    }

    public String getValueColor() {
        return valueColor;
    }

    public void setValueColor(String valueColor) {
        this.valueColor = valueColor;
    }

    public Pm withValueColor(String valueColor) {
        this.valueColor = valueColor;
        return this;
    }
}
