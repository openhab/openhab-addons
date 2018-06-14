/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.qivicon.internal;

import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * The {@link ESHThing} is a POJO for a response from the Rest API.
 *
 * @author Claudius Ellsel - Initial contribution
 */

public class ESHThing {

    @SerializedName("statusInfo")
    @Expose
    private StatusInfo statusInfo;
    @SerializedName("editable")
    @Expose
    private Boolean editable;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("configuration")
    @Expose
    private Configuration configuration;
    @SerializedName("properties")
    @Expose
    private Properties properties;
    @SerializedName("UID")
    @Expose
    private String uID;
    @SerializedName("thingTypeUID")
    @Expose
    private String thingTypeUID;
    @SerializedName("channels")
    @Expose
    private List<Channel> channels = null;

    public StatusInfo getStatusInfo() {
        return statusInfo;
    }

    public void setStatusInfo(StatusInfo statusInfo) {
        this.statusInfo = statusInfo;
    }

    public Boolean getEditable() {
        return editable;
    }

    public void setEditable(Boolean editable) {
        this.editable = editable;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration configuration) {
        this.configuration = configuration;
    }

    public Properties getProperties() {
        return properties;
    }

    public void setProperties(Properties properties) {
        this.properties = properties;
    }

    public String getUID() {
        return uID;
    }

    public void setUID(String uID) {
        this.uID = uID;
    }

    public String getThingTypeUID() {
        return thingTypeUID;
    }

    public void setThingTypeUID(String thingTypeUID) {
        this.thingTypeUID = thingTypeUID;
    }

    public List<Channel> getChannels() {
        return channels;
    }

    public void setChannels(List<Channel> channels) {
        this.channels = channels;
    }

}
