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
package org.openhab.binding.emby.internal.model;

import org.eclipse.jdt.annotation.NonNullByDefault;

import com.google.gson.annotations.SerializedName;

/**
 * embyPlayState - part of the model for the json object received from the server
 *
 * @author Zachary Christiansen - Initial Contribution
 *
 */
@NonNullByDefault
public class EmbyPlayingPostJsonModel {

    @SerializedName("ItemsIds")
    private String itemsIds = "";

    @SerializedName("PlayCommand")
    private String playCommand = "";

    @SerializedName("StartPositionTicks")
    private String startPositionTicks = "";

    @SerializedName("MediaSourceId")
    private String mediaSourceId = "";

    @SerializedName("AudioStreamIndex")
    private String audioStreamIndex = "";

    @SerializedName("SubtitleStreamIndex")
    private String subtitleStreamIndex = "";

    @SerializedName("StartIndex")
    private String startIndex = "";

    public String getItemsIds() {
        return this.itemsIds;
    }

    public void setItemsIds(String settingItemsIds) {
        this.itemsIds = settingItemsIds;
    }

    public String getPlayCommand() {
        return this.playCommand;
    }

    public void setPlayCommand(String settingPlayCommand) {
        this.playCommand = settingPlayCommand;
    }

    public String getStartPositionTicks() {
        return this.startPositionTicks;
    }

    public void setStartPositionTicks(String settingStartPositionTicks) {
        this.startPositionTicks = settingStartPositionTicks;
    }

    public String getMediaSourceIds() {
        return this.mediaSourceId;
    }

    public void setMediaSourceId0(String settingMediaSourceId) {
        this.mediaSourceId = settingMediaSourceId;
    }

    public String getSubtitleStreamIndex() {
        return this.subtitleStreamIndex;
    }

    public void setSubtitleStreamIndex(String settingSubtitleStreamIndex) {
        this.subtitleStreamIndex = settingSubtitleStreamIndex;
    }

    public String getStartIndex() {
        return this.startIndex;
    }

    public void setStartIndex(String settingStartIndex) {
        this.startIndex = settingStartIndex;
    }
}
