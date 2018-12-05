/**
 * Copyright (c) 2010-2018 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.miio.internal.basic;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping properties from json
 *
 * @author Marcel Verpaalen - Initial contribution
 */
public class MiIoBasicChannel {

    @SerializedName("property")
    @Expose
    private String property;
    @SerializedName("friendlyName")
    @Expose
    private String friendlyName;
    @SerializedName("channel")
    @Expose
    private String channel;
    @SerializedName("channelType")
    @Expose
    private String channelType;
    @SerializedName("type")
    @Expose
    private String type;
    @SerializedName("refresh")
    @Expose
    private Boolean refresh;
    @SerializedName("transformation")
    @Expose
    private String transfortmation;
    @SerializedName("ChannelGroup")
    @Expose
    private String channelGroup;
    @SerializedName("actions")
    @Expose
    private List<MiIoDeviceAction> miIoDeviceActions = new ArrayList<MiIoDeviceAction>();;

    public String getProperty() {
        return property;
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public String getFriendlyName() {
        return type == null || friendlyName.isEmpty() ? channel : friendlyName;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelType() {
        return channelType == null || channelType.isEmpty() ? channel : channelType;
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getType() {
        return type == null ? "" : type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRefresh() {
        return refresh && !property.isEmpty();
    }

    public void setRefresh(Boolean refresh) {
        this.refresh = refresh;
    }

    public String getChannelGroup() {
        return channelGroup;
    }

    public void setChannelGroup(String channelGroup) {
        this.channelGroup = channelGroup;
    }

    public List<MiIoDeviceAction> getActions() {
        return miIoDeviceActions;
    }

    public void setActions(List<MiIoDeviceAction> miIoDeviceActions) {
        this.miIoDeviceActions = miIoDeviceActions;
    }

    public String getTransfortmation() {
        return transfortmation;
    }

    public void setTransfortmation(String transfortmation) {
        this.transfortmation = transfortmation;
    }

    @Override
    public String toString() {
        return "[ Channel = " + channel + ", friendlyName = " + friendlyName + ", type = " + type + ", channelType = "
                + getChannelType() + ", ChannelGroup = " + channelGroup + ", channel = " + channel + ", property = "
                + property + ", refresh = " + refresh + "]";
    }
}
