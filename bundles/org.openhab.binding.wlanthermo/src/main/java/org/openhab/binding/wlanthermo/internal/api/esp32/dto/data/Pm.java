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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
    }

    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }

    public Double getSet() {
        return set;
    }

    public void setSet(Double set) {
        this.set = set;
    }

    public String getTyp() {
        return typ;
    }

    public void setTyp(String typ) {
        this.typ = typ;
    }

    public String getSetColor() {
        return setColor;
    }

    public void setSetColor(String setColor) {
        this.setColor = setColor;
    }

    public String getValueColor() {
        return valueColor;
    }

    public void setValueColor(String valueColor) {
        this.valueColor = valueColor;
    }
}
