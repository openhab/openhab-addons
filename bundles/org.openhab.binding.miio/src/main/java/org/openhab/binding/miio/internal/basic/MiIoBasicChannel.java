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
package org.openhab.binding.miio.internal.basic;

import static org.openhab.binding.miio.internal.MiIoBindingConstants.BINDING_ID;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

import com.google.gson.JsonElement;
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
    @SerializedName("description")
    @Expose
    private @Nullable String description;
    @SerializedName("channelType")
    @Expose
    private @Nullable String channelType;
    @SerializedName("type")
    @Expose
    private @Nullable String type;
    @SerializedName("unit")
    @Expose
    private @Nullable String unit;
    @SerializedName("stateDescription")
    @Expose
    private @Nullable StateDescriptionDTO stateDescription;
    @SerializedName("refresh")
    @Expose
    private @Nullable Boolean refresh;
    @SerializedName("refreshInterval")
    @Expose
    private @Nullable Integer refreshInterval;
    @SerializedName("customRefreshCommand")
    @Expose
    private @Nullable String channelCustomRefreshCommand;
    @SerializedName("customRefreshParameters")
    @Expose
    private @Nullable JsonElement customRefreshParameters;
    @SerializedName("transformation")
    @Expose
    private @Nullable String transformation;
    @SerializedName("transformations")
    @Expose
    private @Nullable List<String> transformations;
    @SerializedName("ChannelGroup")
    @Expose
    private @Nullable String channelGroup;
    @SerializedName("actions")
    @Expose
    private @Nullable List<MiIoDeviceAction> miIoDeviceActions = new ArrayList<>();
    @SerializedName("category")
    @Expose
    private @Nullable String category;
    @SerializedName("tags")
    @Expose
    private @Nullable LinkedHashSet<String> tags;
    @SerializedName("readmeComment")
    @Expose
    private @Nullable String readmeComment;

    public String getProperty() {
        final String property = this.property;
        return (property != null) ? property : "";
    }

    public void setProperty(@Nullable String property) {
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

    public void setSiid(@Nullable Integer siid) {
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

    public void setPiid(@Nullable Integer piid) {
        this.piid = piid;
    }

    public boolean isMiOt() {
        return (piid != null && siid != null && (getPiid() != 0 || getSiid() != 0));
    }

    public String getFriendlyName() {
        final String fn = friendlyName;
        return (fn == null || type == null || fn.isEmpty()) ? getChannel() : fn;
    }

    public void setFriendlyName(@Nullable String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public String getChannel() {
        final @Nullable String channel = this.channel;
        return channel != null ? channel : "";
    }

    public void setChannel(@Nullable String channel) {
        this.channel = channel;
    }

    public String getDescription() {
        final String description = this.description;
        return description != null ? description : "";
    }

    public void setDescription(@Nullable String description) {
        this.description = description;
    }

    public String getChannelType() {
        final @Nullable String ct = channelType;
        if (ct == null || ct.isEmpty()) {
            return "";
        } else {
            return (ct.startsWith("system") ? ct : BINDING_ID + ":" + ct);
        }
    }

    public void setChannelType(@Nullable String channelType) {
        this.channelType = channelType;
    }

    public String getType() {
        final @Nullable String type = this.type;
        return type != null ? type : "";
    }

    public void setType(@Nullable String type) {
        this.type = type;
    }

    public String getUnit() {
        final @Nullable String unit = this.unit;
        return unit != null ? unit : "";
    }

    public void setUnit(@Nullable String unit) {
        this.unit = unit;
    }

    public @Nullable StateDescriptionDTO getStateDescription() {
        return stateDescription;
    }

    public void setStateDescription(@Nullable StateDescriptionDTO stateDescription) {
        this.stateDescription = stateDescription;
    }

    public Boolean getRefresh() {
        final @Nullable Boolean rf = refresh;
        return rf != null && rf.booleanValue();
    }

    public void setRefresh(@Nullable Boolean refresh) {
        this.refresh = refresh;
    }

    public Integer getRefreshInterval() {
        Integer refreshInterval = this.refreshInterval;
        if (refreshInterval != null) {
            return refreshInterval;
        }
        return 1;
    }

    public void setRefresh(@Nullable final Integer interval) {
        final Integer refreshInterval = interval;
        if (refreshInterval != null && refreshInterval.intValue() != 1) {
            this.refreshInterval = refreshInterval;
        } else {
            this.refreshInterval = null;
        }
    }

    public String getChannelCustomRefreshCommand() {
        final @Nullable String channelCustomRefreshCommand = this.channelCustomRefreshCommand;
        return channelCustomRefreshCommand != null ? channelCustomRefreshCommand : "";
    }

    public void setChannelCustomRefreshCommand(@Nullable String channelCustomRefreshCommand) {
        this.channelCustomRefreshCommand = channelCustomRefreshCommand;
    }

    public @Nullable final JsonElement getCustomRefreshParameters() {
        return customRefreshParameters;
    }

    public final void setCustomRefreshParameters(@Nullable JsonElement customRefreshParameters) {
        this.customRefreshParameters = customRefreshParameters;
    }

    public String getChannelGroup() {
        final @Nullable String channelGroup = this.channelGroup;
        return channelGroup != null ? channelGroup : "";
    }

    public void setChannelGroup(@Nullable String channelGroup) {
        this.channelGroup = channelGroup;
    }

    public List<MiIoDeviceAction> getActions() {
        final @Nullable List<MiIoDeviceAction> miIoDeviceActions = this.miIoDeviceActions;
        return (miIoDeviceActions != null) ? miIoDeviceActions : Collections.emptyList();
    }

    public void setActions(List<MiIoDeviceAction> miIoDeviceActions) {
        this.miIoDeviceActions = miIoDeviceActions;
    }

    public @Nullable String getTransformation() {
        return transformation;
    }

    public void setTransformation(@Nullable String transformation) {
        this.transformation = transformation;
    }

    public final List<String> getTransformations() {
        List<String> transformations = this.transformations;
        if (transformations == null) {
            transformations = new ArrayList<>();
        }
        String transformation = this.transformation;
        if (transformation != null) {
            List<String> allTransformation = new ArrayList<>(List.of(transformation));
            allTransformation.addAll(transformations);
            return allTransformation;
        }
        return transformations;
    }

    public final void setTransformations(@Nullable List<String> transformations) {
        if (transformations != null && !transformations.isEmpty()) {
            this.transformations = transformations;
        } else {
            this.transformations = null;
        }
    }

    public @Nullable String getCategory() {
        return category;
    }

    public void setCategory(@Nullable String category) {
        this.category = category;
    }

    public @Nullable LinkedHashSet<String> getTags() {
        return tags;
    }

    public void setTags(@Nullable LinkedHashSet<String> tags) {
        this.tags = tags;
    }

    public String getReadmeComment() {
        final String readmeComment = this.readmeComment;
        return (readmeComment != null) ? readmeComment : "";
    }

    public void setReadmeComment(@Nullable String readmeComment) {
        this.readmeComment = readmeComment;
    }

    @Override
    public String toString() {
        return "[ Channel = " + getChannel() + ", friendlyName = " + getFriendlyName() + ", type = " + getType()
                + ", channelType = " + getChannelType() + ", ChannelGroup = " + getChannelGroup() + ", channel = "
                + getChannel() + ", property = " + getProperty() + ", refresh = " + getRefresh() + "]";
    }
}
