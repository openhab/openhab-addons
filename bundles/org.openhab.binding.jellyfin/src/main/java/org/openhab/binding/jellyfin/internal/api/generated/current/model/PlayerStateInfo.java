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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * PlayerStateInfo
 */
@JsonPropertyOrder({ PlayerStateInfo.JSON_PROPERTY_POSITION_TICKS, PlayerStateInfo.JSON_PROPERTY_CAN_SEEK,
        PlayerStateInfo.JSON_PROPERTY_IS_PAUSED, PlayerStateInfo.JSON_PROPERTY_IS_MUTED,
        PlayerStateInfo.JSON_PROPERTY_VOLUME_LEVEL, PlayerStateInfo.JSON_PROPERTY_AUDIO_STREAM_INDEX,
        PlayerStateInfo.JSON_PROPERTY_SUBTITLE_STREAM_INDEX, PlayerStateInfo.JSON_PROPERTY_MEDIA_SOURCE_ID,
        PlayerStateInfo.JSON_PROPERTY_PLAY_METHOD, PlayerStateInfo.JSON_PROPERTY_REPEAT_MODE,
        PlayerStateInfo.JSON_PROPERTY_PLAYBACK_ORDER, PlayerStateInfo.JSON_PROPERTY_LIVE_STREAM_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class PlayerStateInfo {
    public static final String JSON_PROPERTY_POSITION_TICKS = "PositionTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long positionTicks;

    public static final String JSON_PROPERTY_CAN_SEEK = "CanSeek";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canSeek;

    public static final String JSON_PROPERTY_IS_PAUSED = "IsPaused";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isPaused;

    public static final String JSON_PROPERTY_IS_MUTED = "IsMuted";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isMuted;

    public static final String JSON_PROPERTY_VOLUME_LEVEL = "VolumeLevel";
    @org.eclipse.jdt.annotation.NonNull
    private Integer volumeLevel;

    public static final String JSON_PROPERTY_AUDIO_STREAM_INDEX = "AudioStreamIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer audioStreamIndex;

    public static final String JSON_PROPERTY_SUBTITLE_STREAM_INDEX = "SubtitleStreamIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer subtitleStreamIndex;

    public static final String JSON_PROPERTY_MEDIA_SOURCE_ID = "MediaSourceId";
    @org.eclipse.jdt.annotation.NonNull
    private String mediaSourceId;

    public static final String JSON_PROPERTY_PLAY_METHOD = "PlayMethod";
    @org.eclipse.jdt.annotation.NonNull
    private PlayMethod playMethod;

    public static final String JSON_PROPERTY_REPEAT_MODE = "RepeatMode";
    @org.eclipse.jdt.annotation.NonNull
    private RepeatMode repeatMode;

    public static final String JSON_PROPERTY_PLAYBACK_ORDER = "PlaybackOrder";
    @org.eclipse.jdt.annotation.NonNull
    private PlaybackOrder playbackOrder;

    public static final String JSON_PROPERTY_LIVE_STREAM_ID = "LiveStreamId";
    @org.eclipse.jdt.annotation.NonNull
    private String liveStreamId;

    public PlayerStateInfo() {
    }

    public PlayerStateInfo positionTicks(@org.eclipse.jdt.annotation.NonNull Long positionTicks) {
        this.positionTicks = positionTicks;
        return this;
    }

    /**
     * Gets or sets the now playing position ticks.
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

    public PlayerStateInfo canSeek(@org.eclipse.jdt.annotation.NonNull Boolean canSeek) {
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

    public PlayerStateInfo isPaused(@org.eclipse.jdt.annotation.NonNull Boolean isPaused) {
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

    public PlayerStateInfo isMuted(@org.eclipse.jdt.annotation.NonNull Boolean isMuted) {
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

    public PlayerStateInfo volumeLevel(@org.eclipse.jdt.annotation.NonNull Integer volumeLevel) {
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

    public PlayerStateInfo audioStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex) {
        this.audioStreamIndex = audioStreamIndex;
        return this;
    }

    /**
     * Gets or sets the index of the now playing audio stream.
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

    public PlayerStateInfo subtitleStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex) {
        this.subtitleStreamIndex = subtitleStreamIndex;
        return this;
    }

    /**
     * Gets or sets the index of the now playing subtitle stream.
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

    public PlayerStateInfo mediaSourceId(@org.eclipse.jdt.annotation.NonNull String mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
        return this;
    }

    /**
     * Gets or sets the now playing media version identifier.
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

    public PlayerStateInfo playMethod(@org.eclipse.jdt.annotation.NonNull PlayMethod playMethod) {
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

    public PlayerStateInfo repeatMode(@org.eclipse.jdt.annotation.NonNull RepeatMode repeatMode) {
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

    public PlayerStateInfo playbackOrder(@org.eclipse.jdt.annotation.NonNull PlaybackOrder playbackOrder) {
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

    public PlayerStateInfo liveStreamId(@org.eclipse.jdt.annotation.NonNull String liveStreamId) {
        this.liveStreamId = liveStreamId;
        return this;
    }

    /**
     * Gets or sets the now playing live stream identifier.
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

    /**
     * Return true if this PlayerStateInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlayerStateInfo playerStateInfo = (PlayerStateInfo) o;
        return Objects.equals(this.positionTicks, playerStateInfo.positionTicks)
                && Objects.equals(this.canSeek, playerStateInfo.canSeek)
                && Objects.equals(this.isPaused, playerStateInfo.isPaused)
                && Objects.equals(this.isMuted, playerStateInfo.isMuted)
                && Objects.equals(this.volumeLevel, playerStateInfo.volumeLevel)
                && Objects.equals(this.audioStreamIndex, playerStateInfo.audioStreamIndex)
                && Objects.equals(this.subtitleStreamIndex, playerStateInfo.subtitleStreamIndex)
                && Objects.equals(this.mediaSourceId, playerStateInfo.mediaSourceId)
                && Objects.equals(this.playMethod, playerStateInfo.playMethod)
                && Objects.equals(this.repeatMode, playerStateInfo.repeatMode)
                && Objects.equals(this.playbackOrder, playerStateInfo.playbackOrder)
                && Objects.equals(this.liveStreamId, playerStateInfo.liveStreamId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(positionTicks, canSeek, isPaused, isMuted, volumeLevel, audioStreamIndex,
                subtitleStreamIndex, mediaSourceId, playMethod, repeatMode, playbackOrder, liveStreamId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlayerStateInfo {\n");
        sb.append("    positionTicks: ").append(toIndentedString(positionTicks)).append("\n");
        sb.append("    canSeek: ").append(toIndentedString(canSeek)).append("\n");
        sb.append("    isPaused: ").append(toIndentedString(isPaused)).append("\n");
        sb.append("    isMuted: ").append(toIndentedString(isMuted)).append("\n");
        sb.append("    volumeLevel: ").append(toIndentedString(volumeLevel)).append("\n");
        sb.append("    audioStreamIndex: ").append(toIndentedString(audioStreamIndex)).append("\n");
        sb.append("    subtitleStreamIndex: ").append(toIndentedString(subtitleStreamIndex)).append("\n");
        sb.append("    mediaSourceId: ").append(toIndentedString(mediaSourceId)).append("\n");
        sb.append("    playMethod: ").append(toIndentedString(playMethod)).append("\n");
        sb.append("    repeatMode: ").append(toIndentedString(repeatMode)).append("\n");
        sb.append("    playbackOrder: ").append(toIndentedString(playbackOrder)).append("\n");
        sb.append("    liveStreamId: ").append(toIndentedString(liveStreamId)).append("\n");
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
