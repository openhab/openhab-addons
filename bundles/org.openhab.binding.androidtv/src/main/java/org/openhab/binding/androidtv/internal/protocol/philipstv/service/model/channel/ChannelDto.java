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
package org.openhab.binding.androidtv.internal.protocol.philipstv.internal.service.model.channel;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Part of {@link TvChannelDto} and {@link AvailableTvChannelsDto}
 *
 * @author Benjamin Meyer - Initial contribution
 */
public class ChannelDto {

    @JsonProperty
    private String serviceType;

    @JsonProperty
    private int logoVersion;

    @JsonProperty
    private String ccid;

    @JsonProperty
    private String name;

    @JsonProperty
    private String preset;

    @JsonProperty
    private int tsid;

    @JsonProperty
    private String type;

    @JsonProperty
    private int onid;

    @JsonProperty
    private int sid;

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setLogoVersion(int logoVersion) {
        this.logoVersion = logoVersion;
    }

    public int getLogoVersion() {
        return logoVersion;
    }

    public void setCcid(String ccid) {
        this.ccid = ccid;
    }

    public String getCcid() {
        return ccid;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setPreset(String preset) {
        this.preset = preset;
    }

    public String getPreset() {
        return preset;
    }

    public void setTsid(int tsid) {
        this.tsid = tsid;
    }

    public int getTsid() {
        return tsid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setOnid(int onid) {
        this.onid = onid;
    }

    public int getOnid() {
        return onid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public int getSid() {
        return sid;
    }

    @Override
    public String toString() {
        return "ChannelItem{" + "serviceType = '" + serviceType + '\'' + ",logoVersion = '" + logoVersion + '\''
                + ",ccid = '" + ccid + '\'' + ",name = '" + name + '\'' + ",preset = '" + preset + '\'' + ",tsid = '"
                + tsid + '\'' + ",type = '" + type + '\'' + ",onid = '" + onid + '\'' + ",sid = '" + sid + '\'' + "}";
    }
}
