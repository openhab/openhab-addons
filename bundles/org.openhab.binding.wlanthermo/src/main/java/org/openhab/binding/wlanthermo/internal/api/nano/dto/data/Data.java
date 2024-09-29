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

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * This DTO is used to parse the JSON
 * Class is auto-generated from JSON using http://www.jsonschema2pojo.org/
 * Be careful to not overwrite the setState/getState/getTrigger function mapping the Data to OH channels!
 *
 * @author Christian Schlipp - Initial contribution
 */
public class Data {

    @SerializedName("system")
    @Expose
    private System system;
    @SerializedName("channel")
    @Expose
    private List<Channel> channel = new ArrayList<>();
    @SerializedName("pitmaster")
    @Expose
    private Pitmaster pitmaster;

    /**
     * No args constructor for use in serialization
     * 
     */
    public Data() {
    }

    public Data(System system, List<Channel> channel, Pitmaster pitmaster) {
        super();
        this.system = system;
        this.channel = channel;
        this.pitmaster = pitmaster;
    }

    public System getSystem() {
        return system;
    }

    public void setSystem(System system) {
        this.system = system;
    }

    public Data withSystem(System system) {
        this.system = system;
        return this;
    }

    public List<Channel> getChannel() {
        return channel;
    }

    public void setChannel(List<Channel> channel) {
        this.channel = channel;
    }

    public Data withChannel(List<Channel> channel) {
        this.channel = channel;
        return this;
    }

    public Pitmaster getPitmaster() {
        return pitmaster;
    }

    public void setPitmaster(Pitmaster pitmaster) {
        this.pitmaster = pitmaster;
    }

    public Data withPitmaster(Pitmaster pitmaster) {
        this.pitmaster = pitmaster;
        return this;
    }
}
