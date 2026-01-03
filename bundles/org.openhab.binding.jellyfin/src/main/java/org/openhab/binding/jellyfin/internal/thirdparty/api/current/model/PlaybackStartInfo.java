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
 * Class PlaybackStartInfo.
 */
@JsonPropertyOrder({ PlaybackStartInfo.JSON_PROPERTY_CAN_SEEK, PlaybackStartInfo.JSON_PROPERTY_ITEM,
        PlaybackStartInfo.JSON_PROPERTY_ITEM_ID, PlaybackStartInfo.JSON_PROPERTY_SESSION_ID,
        PlaybackStartInfo.JSON_PROPERTY_MEDIA_SOURCE_ID, PlaybackStartInfo.JSON_PROPERTY_AUDIO_STREAM_INDEX,
        PlaybackStartInfo.JSON_PROPERTY_SUBTITLE_STREAM_INDEX, PlaybackStartInfo.JSON_PROPERTY_IS_PAUSED,
        PlaybackStartInfo.JSON_PROPERTY_IS_MUTED, PlaybackStartInfo.JSON_PROPERTY_POSITION_TICKS,
        PlaybackStartInfo.JSON_PROPERTY_PLAYBACK_START_TIME_TICKS, PlaybackStartInfo.JSON_PROPERTY_VOLUME_LEVEL,
        PlaybackStartInfo.JSON_PROPERTY_BRIGHTNESS, PlaybackStartInfo.JSON_PROPERTY_ASPECT_RATIO,
        PlaybackStartInfo.JSON_PROPERTY_PLAY_METHOD, PlaybackStartInfo.JSON_PROPERTY_LIVE_STREAM_ID,
        PlaybackStartInfo.JSON_PROPERTY_PLAY_SESSION_ID, PlaybackStartInfo.JSON_PROPERTY_REPEAT_MODE,
        PlaybackStartInfo.JSON_PROPERTY_PLAYBACK_ORDER, PlaybackStartInfo.JSON_PROPERTY_NOW_PLAYING_QUEUE,
        PlaybackStartInfo.JSON_PROPERTY_PLAYLIST_ITEM_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaybackStartInfo {
    public static final String JSON_PROPERTY_CAN_SEEK = "CanSeek";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean canSeek;

    public static final String JSON_PROPERTY_ITEM = "Item";
    @org.eclipse.jdt.annotation.Nullable
    private BaseItemDto item;

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.Nullable
    private UUID itemId;

    public static final String JSON_PROPERTY_SESSION_ID = "SessionId";
    @org.eclipse.jdt.annotation.Nullable
    private String sessionId;

    public static final String JSON_PROPERTY_MEDIA_SOURCE_ID = "MediaSourceId";
    @org.eclipse.jdt.annotation.Nullable
    private String mediaSourceId;

    public static final String JSON_PROPERTY_AUDIO_STREAM_INDEX = "AudioStreamIndex";
    @org.eclipse.jdt.annotation.Nullable
    private Integer audioStreamIndex;

    public static final String JSON_PROPERTY_SUBTITLE_STREAM_INDEX = "SubtitleStreamIndex";
    @org.eclipse.jdt.annotation.Nullable
    private Integer subtitleStreamIndex;

    public static final String JSON_PROPERTY_IS_PAUSED = "IsPaused";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isPaused;

    public static final String JSON_PROPERTY_IS_MUTED = "IsMuted";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isMuted;

    public static final String JSON_PROPERTY_POSITION_TICKS = "PositionTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long positionTicks;

    public static final String JSON_PROPERTY_PLAYBACK_START_TIME_TICKS = "PlaybackStartTimeTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long playbackStartTimeTicks;

    public static final String JSON_PROPERTY_VOLUME_LEVEL = "VolumeLevel";
    @org.eclipse.jdt.annotation.Nullable
    private Integer volumeLevel;

    public static final String JSON_PROPERTY_BRIGHTNESS = "Brightness";
    @org.eclipse.jdt.annotation.Nullable
    private Integer brightness;

    public static final String JSON_PROPERTY_ASPECT_RATIO = "AspectRatio";
    @org.eclipse.jdt.annotation.Nullable
    private String aspectRatio;

    public static final String JSON_PROPERTY_PLAY_METHOD = "PlayMethod";
    @org.eclipse.jdt.annotation.Nullable
    private PlayMethod playMethod;

    public static final String JSON_PROPERTY_LIVE_STREAM_ID = "LiveStreamId";
    @org.eclipse.jdt.annotation.Nullable
    private String liveStreamId;

    public static final String JSON_PROPERTY_PLAY_SESSION_ID = "PlaySessionId";
    @org.eclipse.jdt.annotation.Nullable
    private String playSessionId;

    public static final String JSON_PROPERTY_REPEAT_MODE = "RepeatMode";
    @org.eclipse.jdt.annotation.Nullable
    private RepeatMode repeatMode;

    public static final String JSON_PROPERTY_PLAYBACK_ORDER = "PlaybackOrder";
    @org.eclipse.jdt.annotation.Nullable
    private PlaybackOrder playbackOrder;

    public static final String JSON_PROPERTY_NOW_PLAYING_QUEUE = "NowPlayingQueue";
    @org.eclipse.jdt.annotation.Nullable
    private List<QueueItem> nowPlayingQueue;

    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.Nullable
    private String playlistItemId;

    public PlaybackStartInfo() {
    }

    public PlaybackStartInfo canSeek(@org.eclipse.jdt.annotation.Nullable Boolean canSeek) {
        this.canSeek = canSeek;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance can seek.
     * 
     * @return canSeek
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CAN_SEEK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCanSeek() {
        return canSeek;
    }

    @JsonProperty(value = JSON_PROPERTY_CAN_SEEK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanSeek(@org.eclipse.jdt.annotation.Nullable Boolean canSeek) {
        this.canSeek = canSeek;
    }

    public PlaybackStartInfo item(@org.eclipse.jdt.annotation.Nullable BaseItemDto item) {
        this.item = item;
        return this;
    }

    /**
     * Gets or sets the item.
     * 
     * @return item
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BaseItemDto getItem() {
        return item;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItem(@org.eclipse.jdt.annotation.Nullable BaseItemDto item) {
        this.item = item;
    }

    public PlaybackStartInfo itemId(@org.eclipse.jdt.annotation.Nullable UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the item identifier.
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.Nullable UUID itemId) {
        this.itemId = itemId;
    }

    public PlaybackStartInfo sessionId(@org.eclipse.jdt.annotation.Nullable String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Gets or sets the session id.
     * 
     * @return sessionId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSessionId() {
        return sessionId;
    }

    @JsonProperty(value = JSON_PROPERTY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessionId(@org.eclipse.jdt.annotation.Nullable String sessionId) {
        this.sessionId = sessionId;
    }

    public PlaybackStartInfo mediaSourceId(@org.eclipse.jdt.annotation.Nullable String mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
        return this;
    }

    /**
     * Gets or sets the media version identifier.
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

    public PlaybackStartInfo audioStreamIndex(@org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex) {
        this.audioStreamIndex = audioStreamIndex;
        return this;
    }

    /**
     * Gets or sets the index of the audio stream.
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

    public PlaybackStartInfo subtitleStreamIndex(@org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex) {
        this.subtitleStreamIndex = subtitleStreamIndex;
        return this;
    }

    /**
     * Gets or sets the index of the subtitle stream.
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

    public PlaybackStartInfo isPaused(@org.eclipse.jdt.annotation.Nullable Boolean isPaused) {
        this.isPaused = isPaused;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is paused.
     * 
     * @return isPaused
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_PAUSED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsPaused() {
        return isPaused;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_PAUSED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPaused(@org.eclipse.jdt.annotation.Nullable Boolean isPaused) {
        this.isPaused = isPaused;
    }

    public PlaybackStartInfo isMuted(@org.eclipse.jdt.annotation.Nullable Boolean isMuted) {
        this.isMuted = isMuted;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is muted.
     * 
     * @return isMuted
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_MUTED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsMuted() {
        return isMuted;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_MUTED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsMuted(@org.eclipse.jdt.annotation.Nullable Boolean isMuted) {
        this.isMuted = isMuted;
    }

    public PlaybackStartInfo positionTicks(@org.eclipse.jdt.annotation.Nullable Long positionTicks) {
        this.positionTicks = positionTicks;
        return this;
    }

    /**
     * Gets or sets the position ticks.
     * 
     * @return positionTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getPositionTicks() {
        return positionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPositionTicks(@org.eclipse.jdt.annotation.Nullable Long positionTicks) {
        this.positionTicks = positionTicks;
    }

    public PlaybackStartInfo playbackStartTimeTicks(@org.eclipse.jdt.annotation.Nullable Long playbackStartTimeTicks) {
        this.playbackStartTimeTicks = playbackStartTimeTicks;
        return this;
    }

    /**
     * Get playbackStartTimeTicks
     * 
     * @return playbackStartTimeTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYBACK_START_TIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getPlaybackStartTimeTicks() {
        return playbackStartTimeTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYBACK_START_TIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaybackStartTimeTicks(@org.eclipse.jdt.annotation.Nullable Long playbackStartTimeTicks) {
        this.playbackStartTimeTicks = playbackStartTimeTicks;
    }

    public PlaybackStartInfo volumeLevel(@org.eclipse.jdt.annotation.Nullable Integer volumeLevel) {
        this.volumeLevel = volumeLevel;
        return this;
    }

    /**
     * Gets or sets the volume level.
     * 
     * @return volumeLevel
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VOLUME_LEVEL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getVolumeLevel() {
        return volumeLevel;
    }

    @JsonProperty(value = JSON_PROPERTY_VOLUME_LEVEL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVolumeLevel(@org.eclipse.jdt.annotation.Nullable Integer volumeLevel) {
        this.volumeLevel = volumeLevel;
    }

    public PlaybackStartInfo brightness(@org.eclipse.jdt.annotation.Nullable Integer brightness) {
        this.brightness = brightness;
        return this;
    }

    /**
     * Get brightness
     * 
     * @return brightness
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BRIGHTNESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getBrightness() {
        return brightness;
    }

    @JsonProperty(value = JSON_PROPERTY_BRIGHTNESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBrightness(@org.eclipse.jdt.annotation.Nullable Integer brightness) {
        this.brightness = brightness;
    }

    public PlaybackStartInfo aspectRatio(@org.eclipse.jdt.annotation.Nullable String aspectRatio) {
        this.aspectRatio = aspectRatio;
        return this;
    }

    /**
     * Get aspectRatio
     * 
     * @return aspectRatio
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAspectRatio() {
        return aspectRatio;
    }

    @JsonProperty(value = JSON_PROPERTY_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAspectRatio(@org.eclipse.jdt.annotation.Nullable String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public PlaybackStartInfo playMethod(@org.eclipse.jdt.annotation.Nullable PlayMethod playMethod) {
        this.playMethod = playMethod;
        return this;
    }

    /**
     * Gets or sets the play method.
     * 
     * @return playMethod
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAY_METHOD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PlayMethod getPlayMethod() {
        return playMethod;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAY_METHOD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayMethod(@org.eclipse.jdt.annotation.Nullable PlayMethod playMethod) {
        this.playMethod = playMethod;
    }

    public PlaybackStartInfo liveStreamId(@org.eclipse.jdt.annotation.Nullable String liveStreamId) {
        this.liveStreamId = liveStreamId;
        return this;
    }

    /**
     * Gets or sets the live stream identifier.
     * 
     * @return liveStreamId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LIVE_STREAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLiveStreamId() {
        return liveStreamId;
    }

    @JsonProperty(value = JSON_PROPERTY_LIVE_STREAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLiveStreamId(@org.eclipse.jdt.annotation.Nullable String liveStreamId) {
        this.liveStreamId = liveStreamId;
    }

    public PlaybackStartInfo playSessionId(@org.eclipse.jdt.annotation.Nullable String playSessionId) {
        this.playSessionId = playSessionId;
        return this;
    }

    /**
     * Gets or sets the play session identifier.
     * 
     * @return playSessionId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPlaySessionId() {
        return playSessionId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaySessionId(@org.eclipse.jdt.annotation.Nullable String playSessionId) {
        this.playSessionId = playSessionId;
    }

    public PlaybackStartInfo repeatMode(@org.eclipse.jdt.annotation.Nullable RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
        return this;
    }

    /**
     * Gets or sets the repeat mode.
     * 
     * @return repeatMode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REPEAT_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    @JsonProperty(value = JSON_PROPERTY_REPEAT_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRepeatMode(@org.eclipse.jdt.annotation.Nullable RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
    }

    public PlaybackStartInfo playbackOrder(@org.eclipse.jdt.annotation.Nullable PlaybackOrder playbackOrder) {
        this.playbackOrder = playbackOrder;
        return this;
    }

    /**
     * Gets or sets the playback order.
     * 
     * @return playbackOrder
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYBACK_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public PlaybackOrder getPlaybackOrder() {
        return playbackOrder;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYBACK_ORDER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaybackOrder(@org.eclipse.jdt.annotation.Nullable PlaybackOrder playbackOrder) {
        this.playbackOrder = playbackOrder;
    }

    public PlaybackStartInfo nowPlayingQueue(@org.eclipse.jdt.annotation.Nullable List<QueueItem> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
        return this;
    }

    public PlaybackStartInfo addNowPlayingQueueItem(QueueItem nowPlayingQueueItem) {
        if (this.nowPlayingQueue == null) {
            this.nowPlayingQueue = new ArrayList<>();
        }
        this.nowPlayingQueue.add(nowPlayingQueueItem);
        return this;
    }

    /**
     * Get nowPlayingQueue
     * 
     * @return nowPlayingQueue
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NOW_PLAYING_QUEUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<QueueItem> getNowPlayingQueue() {
        return nowPlayingQueue;
    }

    @JsonProperty(value = JSON_PROPERTY_NOW_PLAYING_QUEUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowPlayingQueue(@org.eclipse.jdt.annotation.Nullable List<QueueItem> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
    }

    public PlaybackStartInfo playlistItemId(@org.eclipse.jdt.annotation.Nullable String playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Get playlistItemId
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAYLIST_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.Nullable String playlistItemId) {
        this.playlistItemId = playlistItemId;
    }

    /**
     * Return true if this PlaybackStartInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlaybackStartInfo playbackStartInfo = (PlaybackStartInfo) o;
        return Objects.equals(this.canSeek, playbackStartInfo.canSeek)
                && Objects.equals(this.item, playbackStartInfo.item)
                && Objects.equals(this.itemId, playbackStartInfo.itemId)
                && Objects.equals(this.sessionId, playbackStartInfo.sessionId)
                && Objects.equals(this.mediaSourceId, playbackStartInfo.mediaSourceId)
                && Objects.equals(this.audioStreamIndex, playbackStartInfo.audioStreamIndex)
                && Objects.equals(this.subtitleStreamIndex, playbackStartInfo.subtitleStreamIndex)
                && Objects.equals(this.isPaused, playbackStartInfo.isPaused)
                && Objects.equals(this.isMuted, playbackStartInfo.isMuted)
                && Objects.equals(this.positionTicks, playbackStartInfo.positionTicks)
                && Objects.equals(this.playbackStartTimeTicks, playbackStartInfo.playbackStartTimeTicks)
                && Objects.equals(this.volumeLevel, playbackStartInfo.volumeLevel)
                && Objects.equals(this.brightness, playbackStartInfo.brightness)
                && Objects.equals(this.aspectRatio, playbackStartInfo.aspectRatio)
                && Objects.equals(this.playMethod, playbackStartInfo.playMethod)
                && Objects.equals(this.liveStreamId, playbackStartInfo.liveStreamId)
                && Objects.equals(this.playSessionId, playbackStartInfo.playSessionId)
                && Objects.equals(this.repeatMode, playbackStartInfo.repeatMode)
                && Objects.equals(this.playbackOrder, playbackStartInfo.playbackOrder)
                && Objects.equals(this.nowPlayingQueue, playbackStartInfo.nowPlayingQueue)
                && Objects.equals(this.playlistItemId, playbackStartInfo.playlistItemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(canSeek, item, itemId, sessionId, mediaSourceId, audioStreamIndex, subtitleStreamIndex,
                isPaused, isMuted, positionTicks, playbackStartTimeTicks, volumeLevel, brightness, aspectRatio,
                playMethod, liveStreamId, playSessionId, repeatMode, playbackOrder, nowPlayingQueue, playlistItemId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlaybackStartInfo {\n");
        sb.append("    canSeek: ").append(toIndentedString(canSeek)).append("\n");
        sb.append("    item: ").append(toIndentedString(item)).append("\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
        sb.append("    sessionId: ").append(toIndentedString(sessionId)).append("\n");
        sb.append("    mediaSourceId: ").append(toIndentedString(mediaSourceId)).append("\n");
        sb.append("    audioStreamIndex: ").append(toIndentedString(audioStreamIndex)).append("\n");
        sb.append("    subtitleStreamIndex: ").append(toIndentedString(subtitleStreamIndex)).append("\n");
        sb.append("    isPaused: ").append(toIndentedString(isPaused)).append("\n");
        sb.append("    isMuted: ").append(toIndentedString(isMuted)).append("\n");
        sb.append("    positionTicks: ").append(toIndentedString(positionTicks)).append("\n");
        sb.append("    playbackStartTimeTicks: ").append(toIndentedString(playbackStartTimeTicks)).append("\n");
        sb.append("    volumeLevel: ").append(toIndentedString(volumeLevel)).append("\n");
        sb.append("    brightness: ").append(toIndentedString(brightness)).append("\n");
        sb.append("    aspectRatio: ").append(toIndentedString(aspectRatio)).append("\n");
        sb.append("    playMethod: ").append(toIndentedString(playMethod)).append("\n");
        sb.append("    liveStreamId: ").append(toIndentedString(liveStreamId)).append("\n");
        sb.append("    playSessionId: ").append(toIndentedString(playSessionId)).append("\n");
        sb.append("    repeatMode: ").append(toIndentedString(repeatMode)).append("\n");
        sb.append("    playbackOrder: ").append(toIndentedString(playbackOrder)).append("\n");
        sb.append("    nowPlayingQueue: ").append(toIndentedString(nowPlayingQueue)).append("\n");
        sb.append("    playlistItemId: ").append(toIndentedString(playlistItemId)).append("\n");
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

        // add `CanSeek` to the URL query string
        if (getCanSeek() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCanSeek%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanSeek()))));
        }

        // add `Item` to the URL query string
        if (getItem() != null) {
            joiner.add(getItem().toUrlQueryString(prefix + "Item" + suffix));
        }

        // add `ItemId` to the URL query string
        if (getItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemId()))));
        }

        // add `SessionId` to the URL query string
        if (getSessionId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSessionId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSessionId()))));
        }

        // add `MediaSourceId` to the URL query string
        if (getMediaSourceId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMediaSourceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMediaSourceId()))));
        }

        // add `AudioStreamIndex` to the URL query string
        if (getAudioStreamIndex() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAudioStreamIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAudioStreamIndex()))));
        }

        // add `SubtitleStreamIndex` to the URL query string
        if (getSubtitleStreamIndex() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSubtitleStreamIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSubtitleStreamIndex()))));
        }

        // add `IsPaused` to the URL query string
        if (getIsPaused() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsPaused%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsPaused()))));
        }

        // add `IsMuted` to the URL query string
        if (getIsMuted() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsMuted%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsMuted()))));
        }

        // add `PositionTicks` to the URL query string
        if (getPositionTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPositionTicks()))));
        }

        // add `PlaybackStartTimeTicks` to the URL query string
        if (getPlaybackStartTimeTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlaybackStartTimeTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaybackStartTimeTicks()))));
        }

        // add `VolumeLevel` to the URL query string
        if (getVolumeLevel() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVolumeLevel%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVolumeLevel()))));
        }

        // add `Brightness` to the URL query string
        if (getBrightness() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBrightness%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBrightness()))));
        }

        // add `AspectRatio` to the URL query string
        if (getAspectRatio() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAspectRatio%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAspectRatio()))));
        }

        // add `PlayMethod` to the URL query string
        if (getPlayMethod() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlayMethod%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayMethod()))));
        }

        // add `LiveStreamId` to the URL query string
        if (getLiveStreamId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLiveStreamId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLiveStreamId()))));
        }

        // add `PlaySessionId` to the URL query string
        if (getPlaySessionId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlaySessionId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaySessionId()))));
        }

        // add `RepeatMode` to the URL query string
        if (getRepeatMode() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRepeatMode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRepeatMode()))));
        }

        // add `PlaybackOrder` to the URL query string
        if (getPlaybackOrder() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlaybackOrder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaybackOrder()))));
        }

        // add `NowPlayingQueue` to the URL query string
        if (getNowPlayingQueue() != null) {
            for (int i = 0; i < getNowPlayingQueue().size(); i++) {
                if (getNowPlayingQueue().get(i) != null) {
                    joiner.add(getNowPlayingQueue().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sNowPlayingQueue%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `PlaylistItemId` to the URL query string
        if (getPlaylistItemId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlaylistItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaylistItemId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PlaybackStartInfo instance;

        public Builder() {
            this(new PlaybackStartInfo());
        }

        protected Builder(PlaybackStartInfo instance) {
            this.instance = instance;
        }

        public PlaybackStartInfo.Builder canSeek(Boolean canSeek) {
            this.instance.canSeek = canSeek;
            return this;
        }

        public PlaybackStartInfo.Builder item(BaseItemDto item) {
            this.instance.item = item;
            return this;
        }

        public PlaybackStartInfo.Builder itemId(UUID itemId) {
            this.instance.itemId = itemId;
            return this;
        }

        public PlaybackStartInfo.Builder sessionId(String sessionId) {
            this.instance.sessionId = sessionId;
            return this;
        }

        public PlaybackStartInfo.Builder mediaSourceId(String mediaSourceId) {
            this.instance.mediaSourceId = mediaSourceId;
            return this;
        }

        public PlaybackStartInfo.Builder audioStreamIndex(Integer audioStreamIndex) {
            this.instance.audioStreamIndex = audioStreamIndex;
            return this;
        }

        public PlaybackStartInfo.Builder subtitleStreamIndex(Integer subtitleStreamIndex) {
            this.instance.subtitleStreamIndex = subtitleStreamIndex;
            return this;
        }

        public PlaybackStartInfo.Builder isPaused(Boolean isPaused) {
            this.instance.isPaused = isPaused;
            return this;
        }

        public PlaybackStartInfo.Builder isMuted(Boolean isMuted) {
            this.instance.isMuted = isMuted;
            return this;
        }

        public PlaybackStartInfo.Builder positionTicks(Long positionTicks) {
            this.instance.positionTicks = positionTicks;
            return this;
        }

        public PlaybackStartInfo.Builder playbackStartTimeTicks(Long playbackStartTimeTicks) {
            this.instance.playbackStartTimeTicks = playbackStartTimeTicks;
            return this;
        }

        public PlaybackStartInfo.Builder volumeLevel(Integer volumeLevel) {
            this.instance.volumeLevel = volumeLevel;
            return this;
        }

        public PlaybackStartInfo.Builder brightness(Integer brightness) {
            this.instance.brightness = brightness;
            return this;
        }

        public PlaybackStartInfo.Builder aspectRatio(String aspectRatio) {
            this.instance.aspectRatio = aspectRatio;
            return this;
        }

        public PlaybackStartInfo.Builder playMethod(PlayMethod playMethod) {
            this.instance.playMethod = playMethod;
            return this;
        }

        public PlaybackStartInfo.Builder liveStreamId(String liveStreamId) {
            this.instance.liveStreamId = liveStreamId;
            return this;
        }

        public PlaybackStartInfo.Builder playSessionId(String playSessionId) {
            this.instance.playSessionId = playSessionId;
            return this;
        }

        public PlaybackStartInfo.Builder repeatMode(RepeatMode repeatMode) {
            this.instance.repeatMode = repeatMode;
            return this;
        }

        public PlaybackStartInfo.Builder playbackOrder(PlaybackOrder playbackOrder) {
            this.instance.playbackOrder = playbackOrder;
            return this;
        }

        public PlaybackStartInfo.Builder nowPlayingQueue(List<QueueItem> nowPlayingQueue) {
            this.instance.nowPlayingQueue = nowPlayingQueue;
            return this;
        }

        public PlaybackStartInfo.Builder playlistItemId(String playlistItemId) {
            this.instance.playlistItemId = playlistItemId;
            return this;
        }

        /**
         * returns a built PlaybackStartInfo instance.
         *
         * The builder is not reusable.
         */
        public PlaybackStartInfo build() {
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
    public static PlaybackStartInfo.Builder builder() {
        return new PlaybackStartInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PlaybackStartInfo.Builder toBuilder() {
        return new PlaybackStartInfo.Builder().canSeek(getCanSeek()).item(getItem()).itemId(getItemId())
                .sessionId(getSessionId()).mediaSourceId(getMediaSourceId()).audioStreamIndex(getAudioStreamIndex())
                .subtitleStreamIndex(getSubtitleStreamIndex()).isPaused(getIsPaused()).isMuted(getIsMuted())
                .positionTicks(getPositionTicks()).playbackStartTimeTicks(getPlaybackStartTimeTicks())
                .volumeLevel(getVolumeLevel()).brightness(getBrightness()).aspectRatio(getAspectRatio())
                .playMethod(getPlayMethod()).liveStreamId(getLiveStreamId()).playSessionId(getPlaySessionId())
                .repeatMode(getRepeatMode()).playbackOrder(getPlaybackOrder()).nowPlayingQueue(getNowPlayingQueue())
                .playlistItemId(getPlaylistItemId());
    }
}
