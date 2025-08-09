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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

public class PlayQueueUpdate {
    public static final String JSON_PROPERTY_REASON = "Reason";
    @org.eclipse.jdt.annotation.NonNull
    private PlayQueueUpdateReason reason;

    public static final String JSON_PROPERTY_LAST_UPDATE = "LastUpdate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime lastUpdate;

    public static final String JSON_PROPERTY_PLAYLIST = "Playlist";
    @org.eclipse.jdt.annotation.NonNull
    private List<SyncPlayQueueItem> playlist = new ArrayList<>();

    public static final String JSON_PROPERTY_PLAYING_ITEM_INDEX = "PlayingItemIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer playingItemIndex;

    public static final String JSON_PROPERTY_START_POSITION_TICKS = "StartPositionTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long startPositionTicks;

    public static final String JSON_PROPERTY_IS_PLAYING = "IsPlaying";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPlaying;

    public static final String JSON_PROPERTY_SHUFFLE_MODE = "ShuffleMode";
    @org.eclipse.jdt.annotation.NonNull
    private GroupShuffleMode shuffleMode;

    public static final String JSON_PROPERTY_REPEAT_MODE = "RepeatMode";
    @org.eclipse.jdt.annotation.NonNull
    private GroupRepeatMode repeatMode;

    public PlayQueueUpdate() {
    }

    public PlayQueueUpdate reason(@org.eclipse.jdt.annotation.NonNull PlayQueueUpdateReason reason) {
        this.reason = reason;
        return this;
    }

    /**
     * Gets the request type that originated this update.
     * 
     * @return reason
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REASON)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlayQueueUpdateReason getReason() {
        return reason;
    }

    @JsonProperty(JSON_PROPERTY_REASON)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReason(@org.eclipse.jdt.annotation.NonNull PlayQueueUpdateReason reason) {
        this.reason = reason;
    }

    public PlayQueueUpdate lastUpdate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
        return this;
    }

    /**
     * Gets the UTC time of the last change to the playing queue.
     * 
     * @return lastUpdate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LAST_UPDATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getLastUpdate() {
        return lastUpdate;
    }

    @JsonProperty(JSON_PROPERTY_LAST_UPDATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLastUpdate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public PlayQueueUpdate playlist(@org.eclipse.jdt.annotation.NonNull List<SyncPlayQueueItem> playlist) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYLIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SyncPlayQueueItem> getPlaylist() {
        return playlist;
    }

    @JsonProperty(JSON_PROPERTY_PLAYLIST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylist(@org.eclipse.jdt.annotation.NonNull List<SyncPlayQueueItem> playlist) {
        this.playlist = playlist;
    }

    public PlayQueueUpdate playingItemIndex(@org.eclipse.jdt.annotation.NonNull Integer playingItemIndex) {
        this.playingItemIndex = playingItemIndex;
        return this;
    }

    /**
     * Gets the playing item index in the playlist.
     * 
     * @return playingItemIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYING_ITEM_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getPlayingItemIndex() {
        return playingItemIndex;
    }

    @JsonProperty(JSON_PROPERTY_PLAYING_ITEM_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayingItemIndex(@org.eclipse.jdt.annotation.NonNull Integer playingItemIndex) {
        this.playingItemIndex = playingItemIndex;
    }

    public PlayQueueUpdate startPositionTicks(@org.eclipse.jdt.annotation.NonNull Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
        return this;
    }

    /**
     * Gets the start position ticks.
     * 
     * @return startPositionTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getStartPositionTicks() {
        return startPositionTicks;
    }

    @JsonProperty(JSON_PROPERTY_START_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartPositionTicks(@org.eclipse.jdt.annotation.NonNull Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
    }

    public PlayQueueUpdate isPlaying(@org.eclipse.jdt.annotation.NonNull Boolean isPlaying) {
        this.isPlaying = isPlaying;
        return this;
    }

    /**
     * Gets a value indicating whether the current item is playing.
     * 
     * @return isPlaying
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_PLAYING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsPlaying() {
        return isPlaying;
    }

    @JsonProperty(JSON_PROPERTY_IS_PLAYING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPlaying(@org.eclipse.jdt.annotation.NonNull Boolean isPlaying) {
        this.isPlaying = isPlaying;
    }

    public PlayQueueUpdate shuffleMode(@org.eclipse.jdt.annotation.NonNull GroupShuffleMode shuffleMode) {
        this.shuffleMode = shuffleMode;
        return this;
    }

    /**
     * Gets the shuffle mode.
     * 
     * @return shuffleMode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SHUFFLE_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public GroupShuffleMode getShuffleMode() {
        return shuffleMode;
    }

    @JsonProperty(JSON_PROPERTY_SHUFFLE_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setShuffleMode(@org.eclipse.jdt.annotation.NonNull GroupShuffleMode shuffleMode) {
        this.shuffleMode = shuffleMode;
    }

    public PlayQueueUpdate repeatMode(@org.eclipse.jdt.annotation.NonNull GroupRepeatMode repeatMode) {
        this.repeatMode = repeatMode;
        return this;
    }

    /**
     * Gets the repeat mode.
     * 
     * @return repeatMode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REPEAT_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public GroupRepeatMode getRepeatMode() {
        return repeatMode;
    }

    @JsonProperty(JSON_PROPERTY_REPEAT_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRepeatMode(@org.eclipse.jdt.annotation.NonNull GroupRepeatMode repeatMode) {
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
}
