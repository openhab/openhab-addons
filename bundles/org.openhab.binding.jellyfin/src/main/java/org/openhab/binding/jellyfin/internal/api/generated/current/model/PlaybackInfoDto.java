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
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Plabyback info dto.
 */
@JsonPropertyOrder({ PlaybackInfoDto.JSON_PROPERTY_USER_ID, PlaybackInfoDto.JSON_PROPERTY_MAX_STREAMING_BITRATE,
        PlaybackInfoDto.JSON_PROPERTY_START_TIME_TICKS, PlaybackInfoDto.JSON_PROPERTY_AUDIO_STREAM_INDEX,
        PlaybackInfoDto.JSON_PROPERTY_SUBTITLE_STREAM_INDEX, PlaybackInfoDto.JSON_PROPERTY_MAX_AUDIO_CHANNELS,
        PlaybackInfoDto.JSON_PROPERTY_MEDIA_SOURCE_ID, PlaybackInfoDto.JSON_PROPERTY_LIVE_STREAM_ID,
        PlaybackInfoDto.JSON_PROPERTY_DEVICE_PROFILE, PlaybackInfoDto.JSON_PROPERTY_ENABLE_DIRECT_PLAY,
        PlaybackInfoDto.JSON_PROPERTY_ENABLE_DIRECT_STREAM, PlaybackInfoDto.JSON_PROPERTY_ENABLE_TRANSCODING,
        PlaybackInfoDto.JSON_PROPERTY_ALLOW_VIDEO_STREAM_COPY, PlaybackInfoDto.JSON_PROPERTY_ALLOW_AUDIO_STREAM_COPY,
        PlaybackInfoDto.JSON_PROPERTY_AUTO_OPEN_LIVE_STREAM,
        PlaybackInfoDto.JSON_PROPERTY_ALWAYS_BURN_IN_SUBTITLE_WHEN_TRANSCODING })

public class PlaybackInfoDto {
    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_MAX_STREAMING_BITRATE = "MaxStreamingBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxStreamingBitrate;

    public static final String JSON_PROPERTY_START_TIME_TICKS = "StartTimeTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long startTimeTicks;

    public static final String JSON_PROPERTY_AUDIO_STREAM_INDEX = "AudioStreamIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer audioStreamIndex;

    public static final String JSON_PROPERTY_SUBTITLE_STREAM_INDEX = "SubtitleStreamIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer subtitleStreamIndex;

    public static final String JSON_PROPERTY_MAX_AUDIO_CHANNELS = "MaxAudioChannels";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxAudioChannels;

    public static final String JSON_PROPERTY_MEDIA_SOURCE_ID = "MediaSourceId";
    @org.eclipse.jdt.annotation.NonNull
    private String mediaSourceId;

    public static final String JSON_PROPERTY_LIVE_STREAM_ID = "LiveStreamId";
    @org.eclipse.jdt.annotation.NonNull
    private String liveStreamId;

    public static final String JSON_PROPERTY_DEVICE_PROFILE = "DeviceProfile";
    @org.eclipse.jdt.annotation.NonNull
    private DeviceProfile deviceProfile;

    public static final String JSON_PROPERTY_ENABLE_DIRECT_PLAY = "EnableDirectPlay";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableDirectPlay;

    public static final String JSON_PROPERTY_ENABLE_DIRECT_STREAM = "EnableDirectStream";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableDirectStream;

    public static final String JSON_PROPERTY_ENABLE_TRANSCODING = "EnableTranscoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableTranscoding;

    public static final String JSON_PROPERTY_ALLOW_VIDEO_STREAM_COPY = "AllowVideoStreamCopy";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean allowVideoStreamCopy;

    public static final String JSON_PROPERTY_ALLOW_AUDIO_STREAM_COPY = "AllowAudioStreamCopy";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean allowAudioStreamCopy;

    public static final String JSON_PROPERTY_AUTO_OPEN_LIVE_STREAM = "AutoOpenLiveStream";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean autoOpenLiveStream;

    public static final String JSON_PROPERTY_ALWAYS_BURN_IN_SUBTITLE_WHEN_TRANSCODING = "AlwaysBurnInSubtitleWhenTranscoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean alwaysBurnInSubtitleWhenTranscoding;

    public PlaybackInfoDto() {
    }

    public PlaybackInfoDto userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets or sets the playback userId.
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
    }

    public PlaybackInfoDto maxStreamingBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate) {
        this.maxStreamingBitrate = maxStreamingBitrate;
        return this;
    }

    /**
     * Gets or sets the max streaming bitrate.
     * 
     * @return maxStreamingBitrate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_STREAMING_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMaxStreamingBitrate() {
        return maxStreamingBitrate;
    }

    @JsonProperty(JSON_PROPERTY_MAX_STREAMING_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxStreamingBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate) {
        this.maxStreamingBitrate = maxStreamingBitrate;
    }

    public PlaybackInfoDto startTimeTicks(@org.eclipse.jdt.annotation.NonNull Long startTimeTicks) {
        this.startTimeTicks = startTimeTicks;
        return this;
    }

    /**
     * Gets or sets the start time in ticks.
     * 
     * @return startTimeTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START_TIME_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getStartTimeTicks() {
        return startTimeTicks;
    }

    @JsonProperty(JSON_PROPERTY_START_TIME_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartTimeTicks(@org.eclipse.jdt.annotation.NonNull Long startTimeTicks) {
        this.startTimeTicks = startTimeTicks;
    }

    public PlaybackInfoDto audioStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex) {
        this.audioStreamIndex = audioStreamIndex;
        return this;
    }

    /**
     * Gets or sets the audio stream index.
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

    public PlaybackInfoDto subtitleStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex) {
        this.subtitleStreamIndex = subtitleStreamIndex;
        return this;
    }

    /**
     * Gets or sets the subtitle stream index.
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

    public PlaybackInfoDto maxAudioChannels(@org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels) {
        this.maxAudioChannels = maxAudioChannels;
        return this;
    }

    /**
     * Gets or sets the max audio channels.
     * 
     * @return maxAudioChannels
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_AUDIO_CHANNELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMaxAudioChannels() {
        return maxAudioChannels;
    }

    @JsonProperty(JSON_PROPERTY_MAX_AUDIO_CHANNELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxAudioChannels(@org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels) {
        this.maxAudioChannels = maxAudioChannels;
    }

    public PlaybackInfoDto mediaSourceId(@org.eclipse.jdt.annotation.NonNull String mediaSourceId) {
        this.mediaSourceId = mediaSourceId;
        return this;
    }

    /**
     * Gets or sets the media source id.
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

    public PlaybackInfoDto liveStreamId(@org.eclipse.jdt.annotation.NonNull String liveStreamId) {
        this.liveStreamId = liveStreamId;
        return this;
    }

    /**
     * Gets or sets the live stream id.
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

    public PlaybackInfoDto deviceProfile(@org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile) {
        this.deviceProfile = deviceProfile;
        return this;
    }

    /**
     * A MediaBrowser.Model.Dlna.DeviceProfile represents a set of metadata which determines which content a certain
     * device is able to play. &lt;br /&gt; Specifically, it defines the supported &lt;see
     * cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.ContainerProfiles\&quot;&gt;containers&lt;/see&gt; and
     * &lt;see cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.CodecProfiles\&quot;&gt;codecs&lt;/see&gt;
     * (video and/or audio, including codec profiles and levels) the device is able to direct play (without transcoding
     * or remuxing), as well as which &lt;see
     * cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.TranscodingProfiles\&quot;&gt;containers/codecs to
     * transcode to&lt;/see&gt; in case it isn&#39;t.
     * 
     * @return deviceProfile
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEVICE_PROFILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public DeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    @JsonProperty(JSON_PROPERTY_DEVICE_PROFILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceProfile(@org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile) {
        this.deviceProfile = deviceProfile;
    }

    public PlaybackInfoDto enableDirectPlay(@org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay) {
        this.enableDirectPlay = enableDirectPlay;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to enable direct play.
     * 
     * @return enableDirectPlay
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_DIRECT_PLAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableDirectPlay() {
        return enableDirectPlay;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_DIRECT_PLAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableDirectPlay(@org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay) {
        this.enableDirectPlay = enableDirectPlay;
    }

    public PlaybackInfoDto enableDirectStream(@org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream) {
        this.enableDirectStream = enableDirectStream;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to enable direct stream.
     * 
     * @return enableDirectStream
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_DIRECT_STREAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableDirectStream() {
        return enableDirectStream;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_DIRECT_STREAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableDirectStream(@org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream) {
        this.enableDirectStream = enableDirectStream;
    }

    public PlaybackInfoDto enableTranscoding(@org.eclipse.jdt.annotation.NonNull Boolean enableTranscoding) {
        this.enableTranscoding = enableTranscoding;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to enable transcoding.
     * 
     * @return enableTranscoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_TRANSCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableTranscoding() {
        return enableTranscoding;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_TRANSCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableTranscoding(@org.eclipse.jdt.annotation.NonNull Boolean enableTranscoding) {
        this.enableTranscoding = enableTranscoding;
    }

    public PlaybackInfoDto allowVideoStreamCopy(@org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy) {
        this.allowVideoStreamCopy = allowVideoStreamCopy;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to enable video stream copy.
     * 
     * @return allowVideoStreamCopy
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ALLOW_VIDEO_STREAM_COPY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getAllowVideoStreamCopy() {
        return allowVideoStreamCopy;
    }

    @JsonProperty(JSON_PROPERTY_ALLOW_VIDEO_STREAM_COPY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowVideoStreamCopy(@org.eclipse.jdt.annotation.NonNull Boolean allowVideoStreamCopy) {
        this.allowVideoStreamCopy = allowVideoStreamCopy;
    }

    public PlaybackInfoDto allowAudioStreamCopy(@org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy) {
        this.allowAudioStreamCopy = allowAudioStreamCopy;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to allow audio stream copy.
     * 
     * @return allowAudioStreamCopy
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ALLOW_AUDIO_STREAM_COPY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getAllowAudioStreamCopy() {
        return allowAudioStreamCopy;
    }

    @JsonProperty(JSON_PROPERTY_ALLOW_AUDIO_STREAM_COPY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowAudioStreamCopy(@org.eclipse.jdt.annotation.NonNull Boolean allowAudioStreamCopy) {
        this.allowAudioStreamCopy = allowAudioStreamCopy;
    }

    public PlaybackInfoDto autoOpenLiveStream(@org.eclipse.jdt.annotation.NonNull Boolean autoOpenLiveStream) {
        this.autoOpenLiveStream = autoOpenLiveStream;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to auto open the live stream.
     * 
     * @return autoOpenLiveStream
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_AUTO_OPEN_LIVE_STREAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getAutoOpenLiveStream() {
        return autoOpenLiveStream;
    }

    @JsonProperty(JSON_PROPERTY_AUTO_OPEN_LIVE_STREAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAutoOpenLiveStream(@org.eclipse.jdt.annotation.NonNull Boolean autoOpenLiveStream) {
        this.autoOpenLiveStream = autoOpenLiveStream;
    }

    public PlaybackInfoDto alwaysBurnInSubtitleWhenTranscoding(
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) {
        this.alwaysBurnInSubtitleWhenTranscoding = alwaysBurnInSubtitleWhenTranscoding;
        return this;
    }

    /**
     * Gets or sets a value indicating whether always burn in subtitles when transcoding.
     * 
     * @return alwaysBurnInSubtitleWhenTranscoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ALWAYS_BURN_IN_SUBTITLE_WHEN_TRANSCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getAlwaysBurnInSubtitleWhenTranscoding() {
        return alwaysBurnInSubtitleWhenTranscoding;
    }

    @JsonProperty(JSON_PROPERTY_ALWAYS_BURN_IN_SUBTITLE_WHEN_TRANSCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlwaysBurnInSubtitleWhenTranscoding(
            @org.eclipse.jdt.annotation.NonNull Boolean alwaysBurnInSubtitleWhenTranscoding) {
        this.alwaysBurnInSubtitleWhenTranscoding = alwaysBurnInSubtitleWhenTranscoding;
    }

    /**
     * Return true if this PlaybackInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PlaybackInfoDto playbackInfoDto = (PlaybackInfoDto) o;
        return Objects.equals(this.userId, playbackInfoDto.userId)
                && Objects.equals(this.maxStreamingBitrate, playbackInfoDto.maxStreamingBitrate)
                && Objects.equals(this.startTimeTicks, playbackInfoDto.startTimeTicks)
                && Objects.equals(this.audioStreamIndex, playbackInfoDto.audioStreamIndex)
                && Objects.equals(this.subtitleStreamIndex, playbackInfoDto.subtitleStreamIndex)
                && Objects.equals(this.maxAudioChannels, playbackInfoDto.maxAudioChannels)
                && Objects.equals(this.mediaSourceId, playbackInfoDto.mediaSourceId)
                && Objects.equals(this.liveStreamId, playbackInfoDto.liveStreamId)
                && Objects.equals(this.deviceProfile, playbackInfoDto.deviceProfile)
                && Objects.equals(this.enableDirectPlay, playbackInfoDto.enableDirectPlay)
                && Objects.equals(this.enableDirectStream, playbackInfoDto.enableDirectStream)
                && Objects.equals(this.enableTranscoding, playbackInfoDto.enableTranscoding)
                && Objects.equals(this.allowVideoStreamCopy, playbackInfoDto.allowVideoStreamCopy)
                && Objects.equals(this.allowAudioStreamCopy, playbackInfoDto.allowAudioStreamCopy)
                && Objects.equals(this.autoOpenLiveStream, playbackInfoDto.autoOpenLiveStream) && Objects.equals(
                        this.alwaysBurnInSubtitleWhenTranscoding, playbackInfoDto.alwaysBurnInSubtitleWhenTranscoding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, maxStreamingBitrate, startTimeTicks, audioStreamIndex, subtitleStreamIndex,
                maxAudioChannels, mediaSourceId, liveStreamId, deviceProfile, enableDirectPlay, enableDirectStream,
                enableTranscoding, allowVideoStreamCopy, allowAudioStreamCopy, autoOpenLiveStream,
                alwaysBurnInSubtitleWhenTranscoding);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class PlaybackInfoDto {\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    maxStreamingBitrate: ").append(toIndentedString(maxStreamingBitrate)).append("\n");
        sb.append("    startTimeTicks: ").append(toIndentedString(startTimeTicks)).append("\n");
        sb.append("    audioStreamIndex: ").append(toIndentedString(audioStreamIndex)).append("\n");
        sb.append("    subtitleStreamIndex: ").append(toIndentedString(subtitleStreamIndex)).append("\n");
        sb.append("    maxAudioChannels: ").append(toIndentedString(maxAudioChannels)).append("\n");
        sb.append("    mediaSourceId: ").append(toIndentedString(mediaSourceId)).append("\n");
        sb.append("    liveStreamId: ").append(toIndentedString(liveStreamId)).append("\n");
        sb.append("    deviceProfile: ").append(toIndentedString(deviceProfile)).append("\n");
        sb.append("    enableDirectPlay: ").append(toIndentedString(enableDirectPlay)).append("\n");
        sb.append("    enableDirectStream: ").append(toIndentedString(enableDirectStream)).append("\n");
        sb.append("    enableTranscoding: ").append(toIndentedString(enableTranscoding)).append("\n");
        sb.append("    allowVideoStreamCopy: ").append(toIndentedString(allowVideoStreamCopy)).append("\n");
        sb.append("    allowAudioStreamCopy: ").append(toIndentedString(allowAudioStreamCopy)).append("\n");
        sb.append("    autoOpenLiveStream: ").append(toIndentedString(autoOpenLiveStream)).append("\n");
        sb.append("    alwaysBurnInSubtitleWhenTranscoding: ")
                .append(toIndentedString(alwaysBurnInSubtitleWhenTranscoding)).append("\n");
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
