/*
 * Copyright (c) 2010-2025 Contributors to the openHAB project
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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Pitmaster {

    @SerializedName("type")
    @Expose
    private List<String> type = new ArrayList<>();
    @SerializedName("pm")
    @Expose
    private List<Pm> pm = new ArrayList<>();

    /**
     * No args constructor for use in serialization
     * 
     */
    public Pitmaster() {
    }

    /**
     * 
     * @param type
     * @param pm
     */
    public Pitmaster(List<String> type, List<Pm> pm) {
        this.type = type;
        this.pm = pm;
    }

    public List<String> getType() {
        return type;
    }

    public void setType(List<String> type) {
        this.type = type;
    }

    public Pitmaster withType(List<String> type) {
        this.type = type;
        return this;
    }

    public List<Pm> getPm() {
        return pm;
    }

    public void setPm(List<Pm> pm) {
        this.pm = pm;
    }

    public Pitmaster withPm(List<Pm> pm) {
        this.pm = pm;
        return this;
    }
}
