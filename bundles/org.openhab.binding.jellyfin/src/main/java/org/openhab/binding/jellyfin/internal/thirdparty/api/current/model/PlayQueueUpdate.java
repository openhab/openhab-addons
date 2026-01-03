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

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class PlayQueueUpdate.
 */
@JsonPropertyOrder({ PlayQueueUpdate.JSON_PROPERTY_REASON, PlayQueueUpdate.JSON_PROPERTY_LAST_UPDATE,
        PlayQueueUpdate.JSON_PROPERTY_PLAYLIST, PlayQueueUpdate.JSON_PROPERTY_PLAYING_ITEM_INDEX,
        PlayQueueUpdate.JSON_PROPERTY_START_POSITION_TICKS, PlayQueueUpdate.JSON_PROPERTY_IS_PLAYING,
        PlayQueueUpdate.JSON_PROPERTY_SHUFFLE_MODE, PlayQueueUpdate.JSON_PROPERTY_REPEAT_MODE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlayQueueUpdate {
    public static final String JSON_PROPERTY_REASON = "Reason";
    @org.eclipse.jdt.annotation.Nullable
    private PlayQueueUpdateReason reason;

    public static final String JSON_PROPERTY_LAST_UPDATE = "LastUpdate";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime lastUpdate;

    public static final String JSON_PROPERTY_PLAYLIST = "Playlist";
    @org.eclipse.jdt.annotation.Nullable
    private List<SyncPlayQueueItem> playlist = new ArrayList<>();

    public static final String JSON_PROPERTY_PLAYING_ITEM_INDEX = "PlayingItemIndex";
    @org.eclipse.jdt.annotation.Nullable
    private Integer playingItemIndex;

    public static final String JSON_PROPERTY_START_POSITION_TICKS = "StartPositionTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long startPositionTicks;

    public static final String JSON_PROPERTY_IS_PLAYING = "IsPlaying";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isPlaying;

    public static final String JSON_PROPERTY_SHUFFLE_MODE = "ShuffleMode";
    @org.eclipse.jdt.annotation.Nullable
    private GroupShuffleMode shuffleMode;

    public static final String JSON_PROPERTY_REPEAT_MODE = "RepeatMode";
    @org.eclipse.jdt.annotation.Nullable
    private GroupRepeatMode repeatMode;

    public PlayQueueUpdate() {
    }

    public PlayQueueUpdate reason(@org.eclipse.jdt.annotation.Nullable PlayQueueUpdateReason reason) {
        this.reason = reason;
        return this;
    }

    /**
     * Gets the request type that originated this update.
     * 
     * @return reason
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REASON, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PlayQueueUpdateReason getReason() {
        return reason;
    }

    @JsonProperty(value = JSON_PROPERTY_REASON, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReason(@org.eclipse.jdt.annotation.Nullable PlayQueueUpdateReason reason) {
        this.reason = reason;
    }

    public PlayQueueUpdate lastUpdate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    /**
     * Gets the UTC time of the last change to the playing queue.
     * 
     * @return lastUpdate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LAST_UPDATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getLastUpdate() {
        return lastUpdate;
    }

    @JsonProperty(value = JSON_PROPERTY_LAST_UPDATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastUpdate(@org.eclipse.jdt.annotation.Nullable OffsetDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public PlayQueueUpdate playlist(@org.eclipse.jdt.annotation.Nullable List<SyncPlayQueueItem> playlist) {
        this.playlist = playlist;
        return this;
    }

    public PlayQueueUpdate addPlaylistItem(SyncPlayQueueItem playlistItem) {
        if (this.playlist == null) {
            this.playlist = new ArrayList<>();
        }
        this.playlist.add(playlistItem);
        return this;
    }

    /**
     * Gets the playlist.
     * 
     * @return playlist
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYLIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<SyncPlayQueueItem> getPlaylist() {
        return playlist;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYLIST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylist(@org.eclipse.jdt.annotation.Nullable List<SyncPlayQueueItem> playlist) {
        this.playlist = playlist;
    }

    public PlayQueueUpdate playingItemIndex(@org.eclipse.jdt.annotation.Nullable Integer playingItemIndex) {
        this.playingItemIndex = playingItemIndex;
        return this;
    }

    /**
     * Gets the playing item index in the playlist.
     * 
     * @return playingItemIndex
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYING_ITEM_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPlayingItemIndex() {
        return playingItemIndex;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYING_ITEM_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayingItemIndex(@org.eclipse.jdt.annotation.Nullable Integer playingItemIndex) {
        this.playingItemIndex = playingItemIndex;
    }

    public PlayQueueUpdate startPositionTicks(@org.eclipse.jdt.annotation.Nullable Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
        return this;
    }

    /**
     * Gets the start position ticks.
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

    public PlayQueueUpdate isPlaying(@org.eclipse.jdt.annotation.Nullable Boolean isPlaying) {
        this.isPlaying = isPlaying;
        return this;
    }

    /**
     * Gets a value indicating whether the current item is playing.
     * 
     * @return isPlaying
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_PLAYING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPlaying() {
        return isPlaying;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_PLAYING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPlaying(@org.eclipse.jdt.annotation.Nullable Boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public PlayQueueUpdate shuffleMode(@org.eclipse.jdt.annotation.Nullable GroupShuffleMode shuffleMode) {
        this.shuffleMode = shuffleMode;
        return this;
    }

    /**
     * Gets the shuffle mode.
     * 
     * @return shuffleMode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SHUFFLE_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public GroupShuffleMode getShuffleMode() {
        return shuffleMode;
    }

    @JsonProperty(value = JSON_PROPERTY_SHUFFLE_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setShuffleMode(@org.eclipse.jdt.annotation.Nullable GroupShuffleMode shuffleMode) {
        this.shuffleMode = shuffleMode;
    }

    public PlayQueueUpdate repeatMode(@org.eclipse.jdt.annotation.Nullable GroupRepeatMode repeatMode) {
        this.repeatMode = repeatMode;
        return this;
    }

    /**
     * Gets the repeat mode.
     * 
     * @return repeatMode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REPEAT_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public GroupRepeatMode getRepeatMode() {
        return repeatMode;
    }

    @JsonProperty(value = JSON_PROPERTY_REPEAT_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRepeatMode(@org.eclipse.jdt.annotation.Nullable GroupRepeatMode repeatMode) {
        this.repeatMode = repeatMode;
    }

    /**
     * Return true if this PlayQueueUpdate object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayQueueUpdate playQueueUpdate = (PlayQueueUpdate) o;
        return Objects.equals(this.reason, playQueueUpdate.reason)
                && Objects.equals(this.lastUpdate, playQueueUpdate.lastUpdate)
                && Objects.equals(this.playlist, playQueueUpdate.playlist)
                && Objects.equals(this.playingItemIndex, playQueueUpdate.playingItemIndex)
                && Objects.equals(this.startPositionTicks, playQueueUpdate.startPositionTicks)
                && Objects.equals(this.isPlaying, playQueueUpdate.isPlaying)
                && Objects.equals(this.shuffleMode, playQueueUpdate.shuffleMode)
                && Objects.equals(this.repeatMode, playQueueUpdate.repeatMode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reason, lastUpdate, playlist, playingItemIndex, startPositionTicks, isPlaying, shuffleMode,
                repeatMode);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlayQueueUpdate {\n");
        sb.append("    reason: ").append(toIndentedString(reason)).append("\n");
        sb.append("    lastUpdate: ").append(toIndentedString(lastUpdate)).append("\n");
        sb.append("    playlist: ").append(toIndentedString(playlist)).append("\n");
        sb.append("    playingItemIndex: ").append(toIndentedString(playingItemIndex)).append("\n");
        sb.append("    startPositionTicks: ").append(toIndentedString(startPositionTicks)).append("\n");
        sb.append("    isPlaying: ").append(toIndentedString(isPlaying)).append("\n");
        sb.append("    shuffleMode: ").append(toIndentedString(shuffleMode)).append("\n");
        sb.append("    repeatMode: ").append(toIndentedString(repeatMode)).append("\n");
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

        // add `Reason` to the URL query string
        if (getReason() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sReason%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getReason()))));
        }

        // add `LastUpdate` to the URL query string
        if (getLastUpdate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLastUpdate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLastUpdate()))));
        }

        // add `Playlist` to the URL query string
        if (getPlaylist() != null) {
            for (int i = 0; i < getPlaylist().size(); i++) {
                if (getPlaylist().get(i) != null) {
                    joiner.add(getPlaylist().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sPlaylist%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `PlayingItemIndex` to the URL query string
        if (getPlayingItemIndex() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlayingItemIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayingItemIndex()))));
        }

        // add `StartPositionTicks` to the URL query string
        if (getStartPositionTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sStartPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartPositionTicks()))));
        }

        // add `IsPlaying` to the URL query string
        if (getIsPlaying() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsPlaying%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPlaying()))));
        }

        // add `ShuffleMode` to the URL query string
        if (getShuffleMode() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sShuffleMode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getShuffleMode()))));
        }

        // add `RepeatMode` to the URL query string
        if (getRepeatMode() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRepeatMode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRepeatMode()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PlayQueueUpdate instance;

        public Builder() {
            this(new PlayQueueUpdate());
        }

        protected Builder(PlayQueueUpdate instance) {
            this.instance = instance;
        }

        public PlayQueueUpdate.Builder reason(PlayQueueUpdateReason reason) {
            this.instance.reason = reason;
            return this;
        }

        public PlayQueueUpdate.Builder lastUpdate(OffsetDateTime lastUpdate) {
            this.instance.lastUpdate = lastUpdate;
            return this;
        }

        public PlayQueueUpdate.Builder playlist(List<SyncPlayQueueItem> playlist) {
            this.instance.playlist = playlist;
            return this;
        }

        public PlayQueueUpdate.Builder playingItemIndex(Integer playingItemIndex) {
            this.instance.playingItemIndex = playingItemIndex;
            return this;
        }

        public PlayQueueUpdate.Builder startPositionTicks(Long startPositionTicks) {
            this.instance.startPositionTicks = startPositionTicks;
            return this;
        }

        public PlayQueueUpdate.Builder isPlaying(Boolean isPlaying) {
            this.instance.isPlaying = isPlaying;
            return this;
        }

        public PlayQueueUpdate.Builder shuffleMode(GroupShuffleMode shuffleMode) {
            this.instance.shuffleMode = shuffleMode;
            return this;
        }

        public PlayQueueUpdate.Builder repeatMode(GroupRepeatMode repeatMode) {
            this.instance.repeatMode = repeatMode;
            return this;
        }

        /**
         * returns a built PlayQueueUpdate instance.
         *
         * The builder is not reusable.
         */
        public PlayQueueUpdate build() {
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
    public static PlayQueueUpdate.Builder builder() {
        return new PlayQueueUpdate.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PlayQueueUpdate.Builder toBuilder() {
        return new PlayQueueUpdate.Builder().reason(getReason()).lastUpdate(getLastUpdate()).playlist(getPlaylist())
                .playingItemIndex(getPlayingItemIndex()).startPositionTicks(getStartPositionTicks())
                .isPlaying(getIsPlaying()).shuffleMode(getShuffleMode()).repeatMode(getRepeatMode());
    }
}
