/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
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
package org.openhab.binding.miio.internal.basic;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Mapping properties from json
 *
 * @author Marcel Verpaalen - Initial contribution
 */
@NonNullByDefault
public class MiIoBasicChannel {

    @SerializedName("property")
    @Expose
    private @Nullable String property;
    @SerializedName("siid")
    @Expose
    private @Nullable Integer siid;
    @SerializedName("piid")
    @Expose
    private @Nullable Integer piid;
    @SerializedName("friendlyName")
    @Expose
    private @Nullable String friendlyName;
    @SerializedName("channel")
    @Expose
    private @Nullable String channel;
    @SerializedName("channelType")
    @Expose
    private @Nullable String channelType;
    @SerializedName("type")
    @Expose
    private @Nullable String type;
    @SerializedName("refresh")
    @Expose
    private @Nullable Boolean refresh;
    @SerializedName("customRefreshCommand")
    @Expose
    private @Nullable String channelCustomRefreshCommand;
    @SerializedName("transformation")
    @Expose
    private @Nullable String transfortmation;
    @SerializedName("ChannelGroup")
    @Expose
    private @Nullable String channelGroup;
    @SerializedName("actions")
    @Expose
    private @Nullable List<MiIoDeviceAction> miIoDeviceActions = new ArrayList<>();
    @SerializedName("readmeComment")
    @Expose
    private @Nullable String readmeComment;

    public String getProperty() {
        final String property = this.property;
        return (property != null) ? property : "";
    }

    public void setProperty(String property) {
        this.property = property;
    }

    public int getSiid() {
        final Integer siid = this.siid;
        if (siid != null) {
            return siid.intValue();
        } else {
            return 0;
        }
    }

    public void setSiid(Integer siid) {
        this.siid = siid;
    }

    public int getPiid() {
        final Integer piid = this.piid;
        if (piid != null) {
            return piid.intValue();
        } else {
            return 0;
        }
    }

    public void setPiid(Integer piid) {
        this.piid = piid;
    }

    public boolean isMiOt() {
        if (piid != null && siid != null && (getPiid() != 0 || getSiid() != 0)) {
            return true;
        } else {
            return false;
        }
    }

    public String getFriendlyName() {
        final String fn = friendlyName;
        return (fn == null || type == null || fn.isEmpty()) ? getChannel() : fn;
    }

    public void setFriendlyName(String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getChannel() {
        final @Nullable String channel = this.channel;
        return channel != null ? channel : "";
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public String getChannelType() {
        final @Nullable String ct = channelType;
        if (ct == null || ct.isEmpty()) {
            return "";
        } else {
            return (ct.startsWith("system") ? ct : BINDING_ID + ":" + ct);
        }
    }

    public void setChannelType(String channelType) {
        this.channelType = channelType;
    }

    public String getType() {
        final @Nullable String type = this.type;
        return type != null ? type : "";
    }

    public void setType(String type) {
        this.type = type;
    }

    public Boolean getRefresh() {
        final @Nullable Boolean rf = refresh;
        return rf != null && rf.booleanValue() && !getProperty().isEmpty();
    }

    public void setRefresh(Boolean refresh) {
        this.refresh = refresh;
    }

    public String getChannelCustomRefreshCommand() {
        final @Nullable String channelCustomRefreshCommand = this.channelCustomRefreshCommand;
        return channelCustomRefreshCommand != null ? channelCustomRefreshCommand : "";
    }

    public void setChannelCustomRefreshCommand(String channelCustomRefreshCommand) {
        this.channelCustomRefreshCommand = channelCustomRefreshCommand;
    }

    public String getChannelGroup() {
        final @Nullable String channelGroup = this.channelGroup;
        return channelGroup != null ? channelGroup : "";
    }

    public void setChannelGroup(String channelGroup) {
        this.channelGroup = channelGroup;
    }

    public List<MiIoDeviceAction> getActions() {
        final @Nullable List<MiIoDeviceAction> miIoDeviceActions = this.miIoDeviceActions;
        return (miIoDeviceActions != null) ? miIoDeviceActions : Collections.emptyList();
    }

    public void setActions(List<MiIoDeviceAction> miIoDeviceActions) {
        this.miIoDeviceActions = miIoDeviceActions;
    }

    public @Nullable String getTransfortmation() {
        return transfortmation;
    }

    public void setTransfortmation(String transfortmation) {
        this.transfortmation = transfortmation;
    }

    public String getReadmeComment() {
        final String readmeComment = this.readmeComment;
        return (readmeComment != null) ? readmeComment : "";
    }

    public void setReadmeComment(String readmeComment) {
        this.readmeComment = readmeComment;
    }

    @Override
    public String toString() {
        return "[ Channel = " + getChannel() + ", friendlyName = " + getFriendlyName() + ", type = " + getType()
                + ", channelType = " + getChannelType() + ", ChannelGroup = " + getChannelGroup() + ", channel = "
                + getChannel() + ", property = " + getProperty() + ", refresh = " + getRefresh() + "]";
    }
}
