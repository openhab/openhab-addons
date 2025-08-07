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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class PlaybackProgressInfo.
 */
@JsonPropertyOrder({ PlaybackProgressInfo.JSON_PROPERTY_CAN_SEEK, PlaybackProgressInfo.JSON_PROPERTY_ITEM,
        PlaybackProgressInfo.JSON_PROPERTY_ITEM_ID, PlaybackProgressInfo.JSON_PROPERTY_SESSION_ID,
        PlaybackProgressInfo.JSON_PROPERTY_MEDIA_SOURCE_ID, PlaybackProgressInfo.JSON_PROPERTY_AUDIO_STREAM_INDEX,
        PlaybackProgressInfo.JSON_PROPERTY_SUBTITLE_STREAM_INDEX, PlaybackProgressInfo.JSON_PROPERTY_IS_PAUSED,
        PlaybackProgressInfo.JSON_PROPERTY_IS_MUTED, PlaybackProgressInfo.JSON_PROPERTY_POSITION_TICKS,
        PlaybackProgressInfo.JSON_PROPERTY_PLAYBACK_START_TIME_TICKS, PlaybackProgressInfo.JSON_PROPERTY_VOLUME_LEVEL,
        PlaybackProgressInfo.JSON_PROPERTY_BRIGHTNESS, PlaybackProgressInfo.JSON_PROPERTY_ASPECT_RATIO,
        PlaybackProgressInfo.JSON_PROPERTY_PLAY_METHOD, PlaybackProgressInfo.JSON_PROPERTY_LIVE_STREAM_ID,
        PlaybackProgressInfo.JSON_PROPERTY_PLAY_SESSION_ID, PlaybackProgressInfo.JSON_PROPERTY_REPEAT_MODE,
        PlaybackProgressInfo.JSON_PROPERTY_PLAYBACK_ORDER, PlaybackProgressInfo.JSON_PROPERTY_NOW_PLAYING_QUEUE,
        PlaybackProgressInfo.JSON_PROPERTY_PLAYLIST_ITEM_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlaybackProgressInfo {
    public static final String JSON_PROPERTY_CAN_SEEK = "CanSeek";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canSeek;

    public static final String JSON_PROPERTY_ITEM = "Item";
    @org.eclipse.jdt.annotation.NonNull
    private BaseItemDto item;

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID itemId;

    public static final String JSON_PROPERTY_SESSION_ID = "SessionId";
    @org.eclipse.jdt.annotation.NonNull
    private String sessionId;

    public static final String JSON_PROPERTY_MEDIA_SOURCE_ID = "MediaSourceId";
    @org.eclipse.jdt.annotation.NonNull
    private String mediaSourceId;

    public static final String JSON_PROPERTY_AUDIO_STREAM_INDEX = "AudioStreamIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer audioStreamIndex;

    public static final String JSON_PROPERTY_SUBTITLE_STREAM_INDEX = "SubtitleStreamIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer subtitleStreamIndex;

    public static final String JSON_PROPERTY_IS_PAUSED = "IsPaused";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPaused;

    public static final String JSON_PROPERTY_IS_MUTED = "IsMuted";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isMuted;

    public static final String JSON_PROPERTY_POSITION_TICKS = "PositionTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long positionTicks;

    public static final String JSON_PROPERTY_PLAYBACK_START_TIME_TICKS = "PlaybackStartTimeTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long playbackStartTimeTicks;

    public static final String JSON_PROPERTY_VOLUME_LEVEL = "VolumeLevel";
    @org.eclipse.jdt.annotation.NonNull
    private Integer volumeLevel;

    public static final String JSON_PROPERTY_BRIGHTNESS = "Brightness";
    @org.eclipse.jdt.annotation.NonNull
    private Integer brightness;

    public static final String JSON_PROPERTY_ASPECT_RATIO = "AspectRatio";
    @org.eclipse.jdt.annotation.NonNull
    private String aspectRatio;

    public static final String JSON_PROPERTY_PLAY_METHOD = "PlayMethod";
    @org.eclipse.jdt.annotation.NonNull
    private PlayMethod playMethod;

    public static final String JSON_PROPERTY_LIVE_STREAM_ID = "LiveStreamId";
    @org.eclipse.jdt.annotation.NonNull
    private String liveStreamId;

    public static final String JSON_PROPERTY_PLAY_SESSION_ID = "PlaySessionId";
    @org.eclipse.jdt.annotation.NonNull
    private String playSessionId;

    public static final String JSON_PROPERTY_REPEAT_MODE = "RepeatMode";
    @org.eclipse.jdt.annotation.NonNull
    private RepeatMode repeatMode;

    public static final String JSON_PROPERTY_PLAYBACK_ORDER = "PlaybackOrder";
    @org.eclipse.jdt.annotation.NonNull
    private PlaybackOrder playbackOrder;

    public static final String JSON_PROPERTY_NOW_PLAYING_QUEUE = "NowPlayingQueue";
    @org.eclipse.jdt.annotation.NonNull
    private List<QueueItem> nowPlayingQueue;

    public static final String JSON_PROPERTY_PLAYLIST_ITEM_ID = "PlaylistItemId";
    @org.eclipse.jdt.annotation.NonNull
    private String playlistItemId;

    public PlaybackProgressInfo() {
    }

    public PlaybackProgressInfo canSeek(@org.eclipse.jdt.annotation.NonNull Boolean canSeek) {
        this.canSeek = canSeek;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance can seek.
     * 
     * @return canSeek
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CAN_SEEK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getCanSeek() {
        return canSeek;
    }

    @JsonProperty(JSON_PROPERTY_CAN_SEEK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanSeek(@org.eclipse.jdt.annotation.NonNull Boolean canSeek) {
        this.canSeek = canSeek;
    }

    public PlaybackProgressInfo item(@org.eclipse.jdt.annotation.NonNull BaseItemDto item) {
        this.item = item;
        return this;
    }

    /**
     * Gets or sets the item.
     * 
     * @return item
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public BaseItemDto getItem() {
        return item;
    }

    @JsonProperty(JSON_PROPERTY_ITEM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItem(@org.eclipse.jdt.annotation.NonNull BaseItemDto item) {
        this.item = item;
    }

    public PlaybackProgressInfo itemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the item identifier.
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(JSON_PROPERTY_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
    }

    public PlaybackProgressInfo sessionId(@org.eclipse.jdt.annotation.NonNull String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Gets or sets the session id.
     * 
     * @return sessionId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getSessionId() {
        return sessionId;
    }

    @JsonProperty(JSON_PROPERTY_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSessionId(@org.eclipse.jdt.annotation.NonNull String sessionId) {
        this.sessionId = sessionId;
    }

    public PlaybackProgressInfo mediaSourceId(@org.eclipse.jdt.annotation.NonNull String mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
        return this;
    }

    /**
     * Gets or sets the media version identifier.
     * 
     * @return mediaSourceId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MEDIA_SOURCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMediaSourceId() {
        return mediaSourceId;
    }

    @JsonProperty(JSON_PROPERTY_MEDIA_SOURCE_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaSourceId(@org.eclipse.jdt.annotation.NonNull String mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
    }

    public PlaybackProgressInfo audioStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex) {
        this.audioStreamIndex = audioStreamIndex;
        return this;
    }

    /**
     * Gets or sets the index of the audio stream.
     * 
     * @return audioStreamIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_AUDIO_STREAM_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getAudioStreamIndex() {
        return audioStreamIndex;
    }

    @JsonProperty(JSON_PROPERTY_AUDIO_STREAM_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAudioStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex) {
        this.audioStreamIndex = audioStreamIndex;
    }

    public PlaybackProgressInfo subtitleStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex) {
        this.subtitleStreamIndex = subtitleStreamIndex;
        return this;
    }

    /**
     * Gets or sets the index of the subtitle stream.
     * 
     * @return subtitleStreamIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUBTITLE_STREAM_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getSubtitleStreamIndex() {
        return subtitleStreamIndex;
    }

    @JsonProperty(JSON_PROPERTY_SUBTITLE_STREAM_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitleStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex) {
        this.subtitleStreamIndex = subtitleStreamIndex;
    }

    public PlaybackProgressInfo isPaused(@org.eclipse.jdt.annotation.NonNull Boolean isPaused) {
        this.isPaused = isPaused;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is paused.
     * 
     * @return isPaused
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_PAUSED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsPaused() {
        return isPaused;
    }

    @JsonProperty(JSON_PROPERTY_IS_PAUSED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsPaused(@org.eclipse.jdt.annotation.NonNull Boolean isPaused) {
        this.isPaused = isPaused;
    }

    public PlaybackProgressInfo isMuted(@org.eclipse.jdt.annotation.NonNull Boolean isMuted) {
        this.isMuted = isMuted;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is muted.
     * 
     * @return isMuted
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_MUTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsMuted() {
        return isMuted;
    }

    @JsonProperty(JSON_PROPERTY_IS_MUTED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsMuted(@org.eclipse.jdt.annotation.NonNull Boolean isMuted) {
        this.isMuted = isMuted;
    }

    public PlaybackProgressInfo positionTicks(@org.eclipse.jdt.annotation.NonNull Long positionTicks) {
        this.positionTicks = positionTicks;
        return this;
    }

    /**
     * Gets or sets the position ticks.
     * 
     * @return positionTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getPositionTicks() {
        return positionTicks;
    }

    @JsonProperty(JSON_PROPERTY_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPositionTicks(@org.eclipse.jdt.annotation.NonNull Long positionTicks) {
        this.positionTicks = positionTicks;
    }

    public PlaybackProgressInfo playbackStartTimeTicks(
            @org.eclipse.jdt.annotation.NonNull Long playbackStartTimeTicks) {
        this.playbackStartTimeTicks = playbackStartTimeTicks;
        return this;
    }

    /**
     * Get playbackStartTimeTicks
     * 
     * @return playbackStartTimeTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYBACK_START_TIME_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getPlaybackStartTimeTicks() {
        return playbackStartTimeTicks;
    }

    @JsonProperty(JSON_PROPERTY_PLAYBACK_START_TIME_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaybackStartTimeTicks(@org.eclipse.jdt.annotation.NonNull Long playbackStartTimeTicks) {
        this.playbackStartTimeTicks = playbackStartTimeTicks;
    }

    public PlaybackProgressInfo volumeLevel(@org.eclipse.jdt.annotation.NonNull Integer volumeLevel) {
        this.volumeLevel = volumeLevel;
        return this;
    }

    /**
     * Gets or sets the volume level.
     * 
     * @return volumeLevel
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VOLUME_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getVolumeLevel() {
        return volumeLevel;
    }

    @JsonProperty(JSON_PROPERTY_VOLUME_LEVEL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVolumeLevel(@org.eclipse.jdt.annotation.NonNull Integer volumeLevel) {
        this.volumeLevel = volumeLevel;
    }

    public PlaybackProgressInfo brightness(@org.eclipse.jdt.annotation.NonNull Integer brightness) {
        this.brightness = brightness;
        return this;
    }

    /**
     * Get brightness
     * 
     * @return brightness
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BRIGHTNESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getBrightness() {
        return brightness;
    }

    @JsonProperty(JSON_PROPERTY_BRIGHTNESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBrightness(@org.eclipse.jdt.annotation.NonNull Integer brightness) {
        this.brightness = brightness;
    }

    public PlaybackProgressInfo aspectRatio(@org.eclipse.jdt.annotation.NonNull String aspectRatio) {
        this.aspectRatio = aspectRatio;
        return this;
    }

    /**
     * Get aspectRatio
     * 
     * @return aspectRatio
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ASPECT_RATIO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAspectRatio() {
        return aspectRatio;
    }

    @JsonProperty(JSON_PROPERTY_ASPECT_RATIO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAspectRatio(@org.eclipse.jdt.annotation.NonNull String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public PlaybackProgressInfo playMethod(@org.eclipse.jdt.annotation.NonNull PlayMethod playMethod) {
        this.playMethod = playMethod;
        return this;
    }

    /**
     * Gets or sets the play method.
     * 
     * @return playMethod
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAY_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlayMethod getPlayMethod() {
        return playMethod;
    }

    @JsonProperty(JSON_PROPERTY_PLAY_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlayMethod(@org.eclipse.jdt.annotation.NonNull PlayMethod playMethod) {
        this.playMethod = playMethod;
    }

    public PlaybackProgressInfo liveStreamId(@org.eclipse.jdt.annotation.NonNull String liveStreamId) {
        this.liveStreamId = liveStreamId;
        return this;
    }

    /**
     * Gets or sets the live stream identifier.
     * 
     * @return liveStreamId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LIVE_STREAM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getLiveStreamId() {
        return liveStreamId;
    }

    @JsonProperty(JSON_PROPERTY_LIVE_STREAM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLiveStreamId(@org.eclipse.jdt.annotation.NonNull String liveStreamId) {
        this.liveStreamId = liveStreamId;
    }

    public PlaybackProgressInfo playSessionId(@org.eclipse.jdt.annotation.NonNull String playSessionId) {
        this.playSessionId = playSessionId;
        return this;
    }

    /**
     * Gets or sets the play session identifier.
     * 
     * @return playSessionId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAY_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPlaySessionId() {
        return playSessionId;
    }

    @JsonProperty(JSON_PROPERTY_PLAY_SESSION_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaySessionId(@org.eclipse.jdt.annotation.NonNull String playSessionId) {
        this.playSessionId = playSessionId;
    }

    public PlaybackProgressInfo repeatMode(@org.eclipse.jdt.annotation.NonNull RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
        return this;
    }

    /**
     * Gets or sets the repeat mode.
     * 
     * @return repeatMode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REPEAT_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public RepeatMode getRepeatMode() {
        return repeatMode;
    }

    @JsonProperty(JSON_PROPERTY_REPEAT_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRepeatMode(@org.eclipse.jdt.annotation.NonNull RepeatMode repeatMode) {
        this.repeatMode = repeatMode;
    }

    public PlaybackProgressInfo playbackOrder(@org.eclipse.jdt.annotation.NonNull PlaybackOrder playbackOrder) {
        this.playbackOrder = playbackOrder;
        return this;
    }

    /**
     * Gets or sets the playback order.
     * 
     * @return playbackOrder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYBACK_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public PlaybackOrder getPlaybackOrder() {
        return playbackOrder;
    }

    @JsonProperty(JSON_PROPERTY_PLAYBACK_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaybackOrder(@org.eclipse.jdt.annotation.NonNull PlaybackOrder playbackOrder) {
        this.playbackOrder = playbackOrder;
    }

    public PlaybackProgressInfo nowPlayingQueue(@org.eclipse.jdt.annotation.NonNull List<QueueItem> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
        return this;
    }

    public PlaybackProgressInfo addNowPlayingQueueItem(QueueItem nowPlayingQueueItem) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NOW_PLAYING_QUEUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<QueueItem> getNowPlayingQueue() {
        return nowPlayingQueue;
    }

    @JsonProperty(JSON_PROPERTY_NOW_PLAYING_QUEUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNowPlayingQueue(@org.eclipse.jdt.annotation.NonNull List<QueueItem> nowPlayingQueue) {
        this.nowPlayingQueue = nowPlayingQueue;
    }

    public PlaybackProgressInfo playlistItemId(@org.eclipse.jdt.annotation.NonNull String playlistItemId) {
        this.playlistItemId = playlistItemId;
        return this;
    }

    /**
     * Get playlistItemId
     * 
     * @return playlistItemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPlaylistItemId() {
        return playlistItemId;
    }

    @JsonProperty(JSON_PROPERTY_PLAYLIST_ITEM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaylistItemId(@org.eclipse.jdt.annotation.NonNull String playlistItemId) {
        this.playlistItemId = playlistItemId;
    }

    /**
     * Return true if this PlaybackProgressInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlaybackProgressInfo playbackProgressInfo = (PlaybackProgressInfo) o;
        return Objects.equals(this.canSeek, playbackProgressInfo.canSeek)
                && Objects.equals(this.item, playbackProgressInfo.item)
                && Objects.equals(this.itemId, playbackProgressInfo.itemId)
                && Objects.equals(this.sessionId, playbackProgressInfo.sessionId)
                && Objects.equals(this.mediaSourceId, playbackProgressInfo.mediaSourceId)
                && Objects.equals(this.audioStreamIndex, playbackProgressInfo.audioStreamIndex)
                && Objects.equals(this.subtitleStreamIndex, playbackProgressInfo.subtitleStreamIndex)
                && Objects.equals(this.isPaused, playbackProgressInfo.isPaused)
                && Objects.equals(this.isMuted, playbackProgressInfo.isMuted)
                && Objects.equals(this.positionTicks, playbackProgressInfo.positionTicks)
                && Objects.equals(this.playbackStartTimeTicks, playbackProgressInfo.playbackStartTimeTicks)
                && Objects.equals(this.volumeLevel, playbackProgressInfo.volumeLevel)
                && Objects.equals(this.brightness, playbackProgressInfo.brightness)
                && Objects.equals(this.aspectRatio, playbackProgressInfo.aspectRatio)
                && Objects.equals(this.playMethod, playbackProgressInfo.playMethod)
                && Objects.equals(this.liveStreamId, playbackProgressInfo.liveStreamId)
                && Objects.equals(this.playSessionId, playbackProgressInfo.playSessionId)
                && Objects.equals(this.repeatMode, playbackProgressInfo.repeatMode)
                && Objects.equals(this.playbackOrder, playbackProgressInfo.playbackOrder)
                && Objects.equals(this.nowPlayingQueue, playbackProgressInfo.nowPlayingQueue)
                && Objects.equals(this.playlistItemId, playbackProgressInfo.playlistItemId);
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
        sb.append("class PlaybackProgressInfo {\n");
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
}
