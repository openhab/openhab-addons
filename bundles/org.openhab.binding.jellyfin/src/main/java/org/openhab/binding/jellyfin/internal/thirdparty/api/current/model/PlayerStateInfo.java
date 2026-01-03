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

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

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
    @org.eclipse.jdt.annotation.Nullable
    private Long positionTicks;

    public static final String JSON_PROPERTY_CAN_SEEK = "CanSeek";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean canSeek;

    public static final String JSON_PROPERTY_IS_PAUSED = "IsPaused";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isPaused;

    public static final String JSON_PROPERTY_IS_MUTED = "IsMuted";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isMuted;

    public static final String JSON_PROPERTY_VOLUME_LEVEL = "VolumeLevel";
    @org.eclipse.jdt.annotation.Nullable
    private Integer volumeLevel;

    public static final String JSON_PROPERTY_AUDIO_STREAM_INDEX = "AudioStreamIndex";
    @org.eclipse.jdt.annotation.Nullable
    private Integer audioStreamIndex;

    public static final String JSON_PROPERTY_SUBTITLE_STREAM_INDEX = "SubtitleStreamIndex";
    @org.eclipse.jdt.annotation.Nullable
    private Integer subtitleStreamIndex;

    public static final String JSON_PROPERTY_MEDIA_SOURCE_ID = "MediaSourceId";
    @org.eclipse.jdt.annotation.Nullable
    private String mediaSourceId;

    public static final String JSON_PROPERTY_PLAY_METHOD = "PlayMethod";
    @org.eclipse.jdt.annotation.Nullable
    private PlayMethod playMethod;

    public static final String JSON_PROPERTY_REPEAT_MODE = "RepeatMode";
    @org.eclipse.jdt.annotation.Nullable
    private RepeatMode repeatMode;

    public static final String JSON_PROPERTY_PLAYBACK_ORDER = "PlaybackOrder";
    @org.eclipse.jdt.annotation.Nullable
    private PlaybackOrder playbackOrder;

    public static final String JSON_PROPERTY_LIVE_STREAM_ID = "LiveStreamId";
    @org.eclipse.jdt.annotation.Nullable
    private String liveStreamId;

    public PlayerStateInfo() {
    }

    public PlayerStateInfo positionTicks(@org.eclipse.jdt.annotation.Nullable Long positionTicks) {
        this.positionTicks = positionTicks;
        return this;
    }

    /**
     * Gets or sets the now playing position ticks.
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

    public PlayerStateInfo canSeek(@org.eclipse.jdt.annotation.Nullable Boolean canSeek) {
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

    public PlayerStateInfo isPaused(@org.eclipse.jdt.annotation.Nullable Boolean isPaused) {
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

    public PlayerStateInfo isMuted(@org.eclipse.jdt.annotation.Nullable Boolean isMuted) {
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

    public PlayerStateInfo volumeLevel(@org.eclipse.jdt.annotation.Nullable Integer volumeLevel) {
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

    public PlayerStateInfo audioStreamIndex(@org.eclipse.jdt.annotation.Nullable Integer audioStreamIndex) {
        this.audioStreamIndex = audioStreamIndex;
        return this;
    }

    /**
     * Gets or sets the index of the now playing audio stream.
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

    public PlayerStateInfo subtitleStreamIndex(@org.eclipse.jdt.annotation.Nullable Integer subtitleStreamIndex) {
        this.subtitleStreamIndex = subtitleStreamIndex;
        return this;
    }

    /**
     * Gets or sets the index of the now playing subtitle stream.
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

    public PlayerStateInfo mediaSourceId(@org.eclipse.jdt.annotation.Nullable String mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
        return this;
    }

    /**
     * Gets or sets the now playing media version identifier.
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

    public PlayerStateInfo playMethod(@org.eclipse.jdt.annotation.Nullable PlayMethod playMethod) {
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

    public PlayerStateInfo repeatMode(@org.eclipse.jdt.annotation.Nullable RepeatMode repeatMode) {
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

    public PlayerStateInfo playbackOrder(@org.eclipse.jdt.annotation.Nullable PlaybackOrder playbackOrder) {
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

    public PlayerStateInfo liveStreamId(@org.eclipse.jdt.annotation.Nullable String liveStreamId) {
        this.liveStreamId = liveStreamId;
        return this;
    }

    /**
     * Gets or sets the now playing live stream identifier.
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

        // add `PositionTicks` to the URL query string
        if (getPositionTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPositionTicks()))));
        }

        // add `CanSeek` to the URL query string
        if (getCanSeek() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCanSeek%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanSeek()))));
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

        // add `VolumeLevel` to the URL query string
        if (getVolumeLevel() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVolumeLevel%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVolumeLevel()))));
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

        // add `MediaSourceId` to the URL query string
        if (getMediaSourceId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMediaSourceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMediaSourceId()))));
        }

        // add `PlayMethod` to the URL query string
        if (getPlayMethod() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPlayMethod%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlayMethod()))));
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

        // add `LiveStreamId` to the URL query string
        if (getLiveStreamId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLiveStreamId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLiveStreamId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PlayerStateInfo instance;

        public Builder() {
            this(new PlayerStateInfo());
        }

        protected Builder(PlayerStateInfo instance) {
            this.instance = instance;
        }

        public PlayerStateInfo.Builder positionTicks(Long positionTicks) {
            this.instance.positionTicks = positionTicks;
            return this;
        }

        public PlayerStateInfo.Builder canSeek(Boolean canSeek) {
            this.instance.canSeek = canSeek;
            return this;
        }

        public PlayerStateInfo.Builder isPaused(Boolean isPaused) {
            this.instance.isPaused = isPaused;
            return this;
        }

        public PlayerStateInfo.Builder isMuted(Boolean isMuted) {
            this.instance.isMuted = isMuted;
            return this;
        }

        public PlayerStateInfo.Builder volumeLevel(Integer volumeLevel) {
            this.instance.volumeLevel = volumeLevel;
            return this;
        }

        public PlayerStateInfo.Builder audioStreamIndex(Integer audioStreamIndex) {
            this.instance.audioStreamIndex = audioStreamIndex;
            return this;
        }

        public PlayerStateInfo.Builder subtitleStreamIndex(Integer subtitleStreamIndex) {
            this.instance.subtitleStreamIndex = subtitleStreamIndex;
            return this;
        }

        public PlayerStateInfo.Builder mediaSourceId(String mediaSourceId) {
            this.instance.mediaSourceId = mediaSourceId;
            return this;
        }

        public PlayerStateInfo.Builder playMethod(PlayMethod playMethod) {
            this.instance.playMethod = playMethod;
            return this;
        }

        public PlayerStateInfo.Builder repeatMode(RepeatMode repeatMode) {
            this.instance.repeatMode = repeatMode;
            return this;
        }

        public PlayerStateInfo.Builder playbackOrder(PlaybackOrder playbackOrder) {
            this.instance.playbackOrder = playbackOrder;
            return this;
        }

        public PlayerStateInfo.Builder liveStreamId(String liveStreamId) {
            this.instance.liveStreamId = liveStreamId;
            return this;
        }

        /**
         * returns a built PlayerStateInfo instance.
         *
         * The builder is not reusable.
         */
        public PlayerStateInfo build() {
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
    public static PlayerStateInfo.Builder builder() {
        return new PlayerStateInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PlayerStateInfo.Builder toBuilder() {
        return new PlayerStateInfo.Builder().positionTicks(getPositionTicks()).canSeek(getCanSeek())
                .isPaused(getIsPaused()).isMuted(getIsMuted()).volumeLevel(getVolumeLevel())
                .audioStreamIndex(getAudioStreamIndex()).subtitleStreamIndex(getSubtitleStreamIndex())
                .mediaSourceId(getMediaSourceId()).playMethod(getPlayMethod()).repeatMode(getRepeatMode())
                .playbackOrder(getPlaybackOrder()).liveStreamId(getLiveStreamId());
    }
}
