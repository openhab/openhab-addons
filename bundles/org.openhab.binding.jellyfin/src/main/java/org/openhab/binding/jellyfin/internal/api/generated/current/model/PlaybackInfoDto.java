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

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Playback info dto.
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
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
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
    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getUserId() {
        return userId;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_ID, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_MAX_STREAMING_BITRATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxStreamingBitrate() {
        return maxStreamingBitrate;
    }

    @JsonProperty(value = JSON_PROPERTY_MAX_STREAMING_BITRATE, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_START_TIME_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getStartTimeTicks() {
        return startTimeTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_START_TIME_TICKS, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_AUDIO_STREAM_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAudioStreamIndex() {
        return audioStreamIndex;
    }

    @JsonProperty(value = JSON_PROPERTY_AUDIO_STREAM_INDEX, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_SUBTITLE_STREAM_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSubtitleStreamIndex() {
        return subtitleStreamIndex;
    }

    @JsonProperty(value = JSON_PROPERTY_SUBTITLE_STREAM_INDEX, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_MAX_AUDIO_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxAudioChannels() {
        return maxAudioChannels;
    }

    @JsonProperty(value = JSON_PROPERTY_MAX_AUDIO_CHANNELS, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMediaSourceId() {
        return mediaSourceId;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_SOURCE_ID, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_LIVE_STREAM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLiveStreamId() {
        return liveStreamId;
    }

    @JsonProperty(value = JSON_PROPERTY_LIVE_STREAM_ID, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_DEVICE_PROFILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DeviceProfile getDeviceProfile() {
        return deviceProfile;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_PROFILE, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ENABLE_DIRECT_PLAY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableDirectPlay() {
        return enableDirectPlay;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_DIRECT_PLAY, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ENABLE_DIRECT_STREAM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableDirectStream() {
        return enableDirectStream;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_DIRECT_STREAM, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ENABLE_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableTranscoding() {
        return enableTranscoding;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_TRANSCODING, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ALLOW_VIDEO_STREAM_COPY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAllowVideoStreamCopy() {
        return allowVideoStreamCopy;
    }

    @JsonProperty(value = JSON_PROPERTY_ALLOW_VIDEO_STREAM_COPY, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ALLOW_AUDIO_STREAM_COPY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAllowAudioStreamCopy() {
        return allowAudioStreamCopy;
    }

    @JsonProperty(value = JSON_PROPERTY_ALLOW_AUDIO_STREAM_COPY, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_AUTO_OPEN_LIVE_STREAM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAutoOpenLiveStream() {
        return autoOpenLiveStream;
    }

    @JsonProperty(value = JSON_PROPERTY_AUTO_OPEN_LIVE_STREAM, required = false)
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
    @JsonProperty(value = JSON_PROPERTY_ALWAYS_BURN_IN_SUBTITLE_WHEN_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAlwaysBurnInSubtitleWhenTranscoding() {
        return alwaysBurnInSubtitleWhenTranscoding;
    }

    @JsonProperty(value = JSON_PROPERTY_ALWAYS_BURN_IN_SUBTITLE_WHEN_TRANSCODING, required = false)
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

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `MaxStreamingBitrate` to the URL query string
        if (getMaxStreamingBitrate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMaxStreamingBitrate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxStreamingBitrate()))));
        }

        // add `StartTimeTicks` to the URL query string
        if (getStartTimeTicks() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStartTimeTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartTimeTicks()))));
        }

        // add `AudioStreamIndex` to the URL query string
        if (getAudioStreamIndex() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAudioStreamIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAudioStreamIndex()))));
        }

        // add `SubtitleStreamIndex` to the URL query string
        if (getSubtitleStreamIndex() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSubtitleStreamIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSubtitleStreamIndex()))));
        }

        // add `MaxAudioChannels` to the URL query string
        if (getMaxAudioChannels() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMaxAudioChannels%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxAudioChannels()))));
        }

        // add `MediaSourceId` to the URL query string
        if (getMediaSourceId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMediaSourceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMediaSourceId()))));
        }

        // add `LiveStreamId` to the URL query string
        if (getLiveStreamId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sLiveStreamId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLiveStreamId()))));
        }

        // add `DeviceProfile` to the URL query string
        if (getDeviceProfile() != null) {
            joiner.add(getDeviceProfile().toUrlQueryString(prefix + "DeviceProfile" + suffix));
        }

        // add `EnableDirectPlay` to the URL query string
        if (getEnableDirectPlay() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableDirectPlay%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableDirectPlay()))));
        }

        // add `EnableDirectStream` to the URL query string
        if (getEnableDirectStream() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableDirectStream%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableDirectStream()))));
        }

        // add `EnableTranscoding` to the URL query string
        if (getEnableTranscoding() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableTranscoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableTranscoding()))));
        }

        // add `AllowVideoStreamCopy` to the URL query string
        if (getAllowVideoStreamCopy() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAllowVideoStreamCopy%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAllowVideoStreamCopy()))));
        }

        // add `AllowAudioStreamCopy` to the URL query string
        if (getAllowAudioStreamCopy() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAllowAudioStreamCopy%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAllowAudioStreamCopy()))));
        }

        // add `AutoOpenLiveStream` to the URL query string
        if (getAutoOpenLiveStream() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAutoOpenLiveStream%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAutoOpenLiveStream()))));
        }

        // add `AlwaysBurnInSubtitleWhenTranscoding` to the URL query string
        if (getAlwaysBurnInSubtitleWhenTranscoding() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlwaysBurnInSubtitleWhenTranscoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlwaysBurnInSubtitleWhenTranscoding()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private PlaybackInfoDto instance;

        public Builder() {
            this(new PlaybackInfoDto());
        }

        protected Builder(PlaybackInfoDto instance) {
            this.instance = instance;
        }

        public PlaybackInfoDto.Builder userId(UUID userId) {
            this.instance.userId = userId;
            return this;
        }

        public PlaybackInfoDto.Builder maxStreamingBitrate(Integer maxStreamingBitrate) {
            this.instance.maxStreamingBitrate = maxStreamingBitrate;
            return this;
        }

        public PlaybackInfoDto.Builder startTimeTicks(Long startTimeTicks) {
            this.instance.startTimeTicks = startTimeTicks;
            return this;
        }

        public PlaybackInfoDto.Builder audioStreamIndex(Integer audioStreamIndex) {
            this.instance.audioStreamIndex = audioStreamIndex;
            return this;
        }

        public PlaybackInfoDto.Builder subtitleStreamIndex(Integer subtitleStreamIndex) {
            this.instance.subtitleStreamIndex = subtitleStreamIndex;
            return this;
        }

        public PlaybackInfoDto.Builder maxAudioChannels(Integer maxAudioChannels) {
            this.instance.maxAudioChannels = maxAudioChannels;
            return this;
        }

        public PlaybackInfoDto.Builder mediaSourceId(String mediaSourceId) {
            this.instance.mediaSourceId = mediaSourceId;
            return this;
        }

        public PlaybackInfoDto.Builder liveStreamId(String liveStreamId) {
            this.instance.liveStreamId = liveStreamId;
            return this;
        }

        public PlaybackInfoDto.Builder deviceProfile(DeviceProfile deviceProfile) {
            this.instance.deviceProfile = deviceProfile;
            return this;
        }

        public PlaybackInfoDto.Builder enableDirectPlay(Boolean enableDirectPlay) {
            this.instance.enableDirectPlay = enableDirectPlay;
            return this;
        }

        public PlaybackInfoDto.Builder enableDirectStream(Boolean enableDirectStream) {
            this.instance.enableDirectStream = enableDirectStream;
            return this;
        }

        public PlaybackInfoDto.Builder enableTranscoding(Boolean enableTranscoding) {
            this.instance.enableTranscoding = enableTranscoding;
            return this;
        }

        public PlaybackInfoDto.Builder allowVideoStreamCopy(Boolean allowVideoStreamCopy) {
            this.instance.allowVideoStreamCopy = allowVideoStreamCopy;
            return this;
        }

        public PlaybackInfoDto.Builder allowAudioStreamCopy(Boolean allowAudioStreamCopy) {
            this.instance.allowAudioStreamCopy = allowAudioStreamCopy;
            return this;
        }

        public PlaybackInfoDto.Builder autoOpenLiveStream(Boolean autoOpenLiveStream) {
            this.instance.autoOpenLiveStream = autoOpenLiveStream;
            return this;
        }

        public PlaybackInfoDto.Builder alwaysBurnInSubtitleWhenTranscoding(
                Boolean alwaysBurnInSubtitleWhenTranscoding) {
            this.instance.alwaysBurnInSubtitleWhenTranscoding = alwaysBurnInSubtitleWhenTranscoding;
            return this;
        }

        /**
         * returns a built PlaybackInfoDto instance.
         *
         * The builder is not reusable.
         */
        public PlaybackInfoDto build() {
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
    public static PlaybackInfoDto.Builder builder() {
        return new PlaybackInfoDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public PlaybackInfoDto.Builder toBuilder() {
        return new PlaybackInfoDto.Builder().userId(getUserId()).maxStreamingBitrate(getMaxStreamingBitrate())
                .startTimeTicks(getStartTimeTicks()).audioStreamIndex(getAudioStreamIndex())
                .subtitleStreamIndex(getSubtitleStreamIndex()).maxAudioChannels(getMaxAudioChannels())
                .mediaSourceId(getMediaSourceId()).liveStreamId(getLiveStreamId()).deviceProfile(getDeviceProfile())
                .enableDirectPlay(getEnableDirectPlay()).enableDirectStream(getEnableDirectStream())
                .enableTranscoding(getEnableTranscoding()).allowVideoStreamCopy(getAllowVideoStreamCopy())
                .allowAudioStreamCopy(getAllowAudioStreamCopy()).autoOpenLiveStream(getAutoOpenLiveStream())
                .alwaysBurnInSubtitleWhenTranscoding(getAlwaysBurnInSubtitleWhenTranscoding());
    }
}
