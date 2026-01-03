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

package org.openhab.binding.jellyfin.internal.thirdparty.api.current.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class PlayRequest.
 */
@JsonPropertyOrder({ PlayRequest.JSON_PROPERTY_ITEM_IDS, PlayRequest.JSON_PROPERTY_START_POSITION_TICKS,
        PlayRequest.JSON_PROPERTY_PLAY_COMMAND, PlayRequest.JSON_PROPERTY_CONTROLLING_USER_ID,
        PlayRequest.JSON_PROPERTY_SUBTITLE_STREAM_INDEX, PlayRequest.JSON_PROPERTY_AUDIO_STREAM_INDEX,
        PlayRequest.JSON_PROPERTY_MEDIA_SOURCE_ID, PlayRequest.JSON_PROPERTY_START_INDEX })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlayRequest {
    public static final String JSON_PROPERTY_ITEM_IDS = "ItemIds";
    @org.eclipse.jdt.annotation.Nullable
    private List<UUID> itemIds;

    public static final String JSON_PROPERTY_START_POSITION_TICKS = "StartPositionTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long startPositionTicks;

    public static final String JSON_PROPERTY_PLAY_COMMAND = "PlayCommand";
    @org.eclipse.jdt.annotation.Nullable
    private PlayCommand playCommand;

    public static final String JSON_PROPERTY_CONTROLLING_USER_ID = "ControllingUserId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID controllingUserId;

    public static final String JSON_PROPERTY_SUBTITLE_STREAM_INDEX = "SubtitleStreamIndex";
    @org.eclipse.jdt.annotation.Nullable
    private Integer subtitleStreamIndex;

    public static final String JSON_PROPERTY_AUDIO_STREAM_INDEX = "AudioStreamIndex";
    @org.eclipse.jdt.annotation.Nullable
    private Integer audioStreamIndex;

    public static final String JSON_PROPERTY_MEDIA_SOURCE_ID = "MediaSourceId";
    @org.eclipse.jdt.annotation.Nullable
    private String mediaSourceId;

    public static final String JSON_PROPERTY_START_INDEX = "StartIndex";
    @org.eclipse.jdt.annotation.Nullable
    private Integer startIndex;

    public PlayRequest() {
    }

    public PlayRequest itemIds(@org.eclipse.jdt.annotation.Nullable List<UUID> itemIds) {
        this.itemIds = itemIds;
        return this;
    }

    public PlayRequest addItemIdsItem(UUID itemIdsItem) {
        if (this.itemIds == null) {
            this.itemIds = new ArrayList<>();
        }
        this.itemIds.add(itemIdsItem);
        return this;
    }

    /**
     * Gets or sets the item ids.
     * 
     * @return itemIds
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEM_IDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<UUID> getItemIds() {
        return itemIds;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_IDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemIds(@org.eclipse.jdt.annotation.Nullable List<UUID> itemIds) {
        this.itemIds = itemIds;
    }

    public PlayRequest startPositionTicks(@org.eclipse.jdt.annotation.Nullable Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
        return this;
    }

    /**
     * Gets or sets the start position ticks that the first item should be played at.
     * 
     * @return startPositionTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_START_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getStartPositionTicks() {
        return startPositionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_START_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartPositionTicks(@org.eclipse.jdt.annotation.Nullable Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
    }

    public PlayRequest playCommand(@org.eclipse.jdt.annotation.Nullable PlayCommand playCommand) {
        this.playCommand = playCommand;
        return this;
    }

    /**
     * Gets or sets the play command.
     * 
     * @return playCommand
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAY_COMMAND, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PlayCommand getPlayCommand() {
        return playCommand;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAY_COMMAND, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayCommand(@org.eclipse.jdt.annotation.Nullable PlayCommand playCommand) {
        this.playCommand = playCommand;
    }

    public PlayRequest controllingUserId(@org.eclipse.jdt.annotation.Nullable UUID controllingUserId) {
        this.controllingUserId = controllingUserId;
        return this;
    }

    /**
     * Gets or sets the controlling user identifier.
     * 
     * @return controllingUserId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CONTROLLING_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getControllingUserId() {
        return controllingUserId;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTROLLING_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setControllingUserId(@org.eclipse.jdt.annotation.Nullable UUID controllingUserId) {
        this.controllingUserId = controllingUserId;
    }

    public PlayRequest subtitleStreamIndex(@org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex) {
        this.subtitleStreamIndex = subtitleStreamIndex;
        return this;
    }

    /**
     * Get subtitleStreamIndex
     * 
     * @return subtitleStreamIndex
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SUBTITLE_STREAM_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSubtitleStreamIndex() {
        return subtitleStreamIndex;
    }

    @JsonProperty(value = JSON_PROPERTY_SUBTITLE_STREAM_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitleStreamIndex(@org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex) {
        this.subtitleStreamIndex = subtitleStreamIndex;
    }

    public PlayRequest audioStreamIndex(@org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex) {
        this.audioStreamIndex = audioStreamIndex;
        return this;
    }

    /**
     * Get audioStreamIndex
     * 
     * @return audioStreamIndex
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_AUDIO_STREAM_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAudioStreamIndex() {
        return audioStreamIndex;
    }

    @JsonProperty(value = JSON_PROPERTY_AUDIO_STREAM_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAudioStreamIndex(@org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex) {
        this.audioStreamIndex = audioStreamIndex;
    }

    public PlayRequest mediaSourceId(@org.eclipse.jdt.annotation.Nullable String mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
        return this;
    }

    /**
     * Get mediaSourceId
     * 
     * @return mediaSourceId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMediaSourceId() {
        return mediaSourceId;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaSourceId(@org.eclipse.jdt.annotation.Nullable String mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
    }

    public PlayRequest startIndex(@org.eclipse.jdt.annotation.Nullable Integer startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Get startIndex
     * 
     * @return startIndex
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_START_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getStartIndex() {
        return startIndex;
    }

    @JsonProperty(value = JSON_PROPERTY_START_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartIndex(@org.eclipse.jdt.annotation.Nullable Integer startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Return true if this PlayRequest object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayRequest playRequest = (PlayRequest) o;
        return Objects.equals(this.itemIds, playRequest.itemIds)
                && Objects.equals(this.startPositionTicks, playRequest.startPositionTicks)
                && Objects.equals(this.playCommand, playRequest.playCommand)
                && Objects.equals(this.controllingUserId, playRequest.controllingUserId)
                && Objects.equals(this.subtitleStreamIndex, playRequest.subtitleStreamIndex)
                && Objects.equals(this.audioStreamIndex, playRequest.audioStreamIndex)
                && Objects.equals(this.mediaSourceId, playRequest.mediaSourceId)
                && Objects.equals(this.startIndex, playRequest.startIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemIds, startPositionTicks, playCommand, controllingUserId, subtitleStreamIndex,
                audioStreamIndex, mediaSourceId, startIndex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlayRequest {\n");
        sb.append("    itemIds: ").append(toIndentedString(itemIds)).append("\n");
        sb.append("    startPositionTicks: ").append(toIndentedString(startPositionTicks)).append("\n");
        sb.append("    playCommand: ").append(toIndentedString(playCommand)).append("\n");
        sb.append("    controllingUserId: ").append(toIndentedString(controllingUserId)).append("\n");
        sb.append("    subtitleStreamIndex: ").append(toIndentedString(subtitleStreamIndex)).append("\n");
        sb.append("    audioStreamIndex: ").append(toIndentedString(audioStreamIndex)).append("\n");
        sb.append("    mediaSourceId: ").append(toIndentedString(mediaSourceId)).append("\n");
        sb.append("    startIndex: ").append(toIndentedString(startIndex)).append("\n");
        sb.append("}");
        return sb.toString();
    }

    /**
     * Convert the given object to string with each line indented by 4 spaces
     * (except the first line).
     */
    private String toIndentedString(Object o) {
        if (o == null) {
            return "null";
        }
        return o.toString().replace("\n", "\n    ");
    }

    /**
     * Convert the instance into URL query string.
     *
     * @return URL query string
     */
    public String toUrlQueryString() {
        return toUrlQueryString(null);
    }

    /**
     * Convert the instance into URL query string.
     *
     * @param prefix prefix of the query string
     * @return URL query string
     */
    public String toUrlQueryString(String prefix) {
        String suffix = "";
        String containerSuffix = "";
        String containerPrefix = "";
        if (prefix == null) {
            // style=form, explode=true, e.g. /pet?name=cat&type=manx
            prefix = "";
        } else {
            // deepObject style e.g. /pet?id[name]=cat&id[type]=manx
            prefix = prefix + "[";
            suffix = "]";
            containerSuffix = "]";
            containerPrefix = "[";
        }

        StringJoiner joiner = new StringJoiner("&");

        // add `ItemIds` to the URL query string
        if (getItemIds() != null) {
            for (int i = 0; i < getItemIds().size(); i++) {
                if (getItemIds().get(i) != null) {
                    joiner.add(String.format(java.util.Locale.ROOT, "%sItemIds%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                            containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getItemIds().get(i)))));
                }
            }
        }

        // add `StartPositionTicks` to the URL query string
        if (getStartPositionTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sStartPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartPositionTicks()))));
        }

        // add `PlayCommand` to the URL query string
        if (getPlayCommand() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlayCommand%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayCommand()))));
        }

        // add `ControllingUserId` to the URL query string
        if (getControllingUserId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sControllingUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getControllingUserId()))));
        }

        // add `SubtitleStreamIndex` to the URL query string
        if (getSubtitleStreamIndex() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSubtitleStreamIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSubtitleStreamIndex()))));
        }

        // add `AudioStreamIndex` to the URL query string
        if (getAudioStreamIndex() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAudioStreamIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAudioStreamIndex()))));
        }

        // add `MediaSourceId` to the URL query string
        if (getMediaSourceId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMediaSourceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMediaSourceId()))));
        }

        // add `StartIndex` to the URL query string
        if (getStartIndex() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sStartIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartIndex()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PlayRequest instance;

        public Builder() {
            this(new PlayRequest());
        }

        protected Builder(PlayRequest instance) {
            this.instance = instance;
        }

        public PlayRequest.Builder itemIds(List<UUID> itemIds) {
            this.instance.itemIds = itemIds;
            return this;
        }

        public PlayRequest.Builder startPositionTicks(Long startPositionTicks) {
            this.instance.startPositionTicks = startPositionTicks;
            return this;
        }

        public PlayRequest.Builder playCommand(PlayCommand playCommand) {
            this.instance.playCommand = playCommand;
            return this;
        }

        public PlayRequest.Builder controllingUserId(UUID controllingUserId) {
            this.instance.controllingUserId = controllingUserId;
            return this;
        }

        public PlayRequest.Builder subtitleStreamIndex(Integer subtitleStreamIndex) {
            this.instance.subtitleStreamIndex = subtitleStreamIndex;
            return this;
        }

        public PlayRequest.Builder audioStreamIndex(Integer audioStreamIndex) {
            this.instance.audioStreamIndex = audioStreamIndex;
            return this;
        }

        public PlayRequest.Builder mediaSourceId(String mediaSourceId) {
            this.instance.mediaSourceId = mediaSourceId;
            return this;
        }

        public PlayRequest.Builder startIndex(Integer startIndex) {
            this.instance.startIndex = startIndex;
            return this;
        }

        /**
         * returns a built PlayRequest instance.
         *
         * The builder is not reusable.
         */
        public PlayRequest build() {
            try {
                return this.instance;
            } finally {
                // ensure that this.instance is not reused
                this.instance = null;
            }
        }

        @Override
        public String toString() {
            return getClass() + "=(" + instance + ")";
        }
    }

    /**
     * Create a builder with no initialized field.
     */
    public static PlayRequest.Builder builder() {
        return new PlayRequest.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PlayRequest.Builder toBuilder() {
        return new PlayRequest.Builder().itemIds(getItemIds()).startPositionTicks(getStartPositionTicks())
                .playCommand(getPlayCommand()).controllingUserId(getControllingUserId())
                .subtitleStreamIndex(getSubtitleStreamIndex()).audioStreamIndex(getAudioStreamIndex())
                .mediaSourceId(getMediaSourceId()).startIndex(getStartIndex());
    }
}
