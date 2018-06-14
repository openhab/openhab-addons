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
 * The {@link Channel} is a POJO for a response from the Rest API.
 *
 * @author Claudius Ellsel - Initial contribution
 */
public class Channel {

    @SerializedName("linkedItems")
    @Expose
    private List<String> linkedItems = null;
    @SerializedName("uid")
    @Expose
    private String uid;
    @SerializedName("id")
    @Expose
    private String id;
    @SerializedName("channelTypeUID")
    @Expose
    private String channelTypeUID;
    @SerializedName("itemType")
    @Expose
    private String itemType;
    @SerializedName("kind")
    @Expose
    private String kind;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("description")
    @Expose
    private String description;
    @SerializedName("defaultTags")
    @Expose
    private List<String> defaultTags = null;
    @SerializedName("properties")
    @Expose
    private Properties_ properties;
    @SerializedName("configuration")
    @Expose
    private Configuration_ configuration;

    public List<String> getLinkedItems() {
        return linkedItems;
    }

    public void setLinkedItems(List<String> linkedItems) {
        this.linkedItems = linkedItems;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getChannelTypeUID() {
        return channelTypeUID;
    }

    public void setChannelTypeUID(String channelTypeUID) {
        this.channelTypeUID = channelTypeUID;
    }

    public String getItemType() {
        return itemType;
    }

    public void setItemType(String itemType) {
        this.itemType = itemType;
    }

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getDefaultTags() {
        return defaultTags;
    }

    public void setDefaultTags(List<String> defaultTags) {
        this.defaultTags = defaultTags;
    }

    public Properties_ getProperties() {
        return properties;
    }

    public void setProperties(Properties_ properties) {
        this.properties = properties;
    }

    public Configuration_ getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Configuration_ configuration) {
        this.configuration = configuration;
    }

}
