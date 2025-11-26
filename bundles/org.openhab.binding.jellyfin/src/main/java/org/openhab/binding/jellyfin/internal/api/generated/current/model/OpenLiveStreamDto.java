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
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Open live stream dto.
 */
@JsonPropertyOrder({ OpenLiveStreamDto.JSON_PROPERTY_OPEN_TOKEN, OpenLiveStreamDto.JSON_PROPERTY_USER_ID,
        OpenLiveStreamDto.JSON_PROPERTY_PLAY_SESSION_ID, OpenLiveStreamDto.JSON_PROPERTY_MAX_STREAMING_BITRATE,
        OpenLiveStreamDto.JSON_PROPERTY_START_TIME_TICKS, OpenLiveStreamDto.JSON_PROPERTY_AUDIO_STREAM_INDEX,
        OpenLiveStreamDto.JSON_PROPERTY_SUBTITLE_STREAM_INDEX, OpenLiveStreamDto.JSON_PROPERTY_MAX_AUDIO_CHANNELS,
        OpenLiveStreamDto.JSON_PROPERTY_ITEM_ID, OpenLiveStreamDto.JSON_PROPERTY_ENABLE_DIRECT_PLAY,
        OpenLiveStreamDto.JSON_PROPERTY_ENABLE_DIRECT_STREAM,
        OpenLiveStreamDto.JSON_PROPERTY_ALWAYS_BURN_IN_SUBTITLE_WHEN_TRANSCODING,
        OpenLiveStreamDto.JSON_PROPERTY_DEVICE_PROFILE, OpenLiveStreamDto.JSON_PROPERTY_DIRECT_PLAY_PROTOCOLS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class OpenLiveStreamDto {
    public static final String JSON_PROPERTY_OPEN_TOKEN = "OpenToken";
    @org.eclipse.jdt.annotation.NonNull
    private String openToken;

    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID userId;

    public static final String JSON_PROPERTY_PLAY_SESSION_ID = "PlaySessionId";
    @org.eclipse.jdt.annotation.NonNull
    private String playSessionId;

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

    public static final String JSON_PROPERTY_ITEM_ID = "ItemId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID itemId;

    public static final String JSON_PROPERTY_ENABLE_DIRECT_PLAY = "EnableDirectPlay";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableDirectPlay;

    public static final String JSON_PROPERTY_ENABLE_DIRECT_STREAM = "EnableDirectStream";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableDirectStream;

    public static final String JSON_PROPERTY_ALWAYS_BURN_IN_SUBTITLE_WHEN_TRANSCODING = "AlwaysBurnInSubtitleWhenTranscoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean alwaysBurnInSubtitleWhenTranscoding;

    public static final String JSON_PROPERTY_DEVICE_PROFILE = "DeviceProfile";
    @org.eclipse.jdt.annotation.NonNull
    private DeviceProfile deviceProfile;

    public static final String JSON_PROPERTY_DIRECT_PLAY_PROTOCOLS = "DirectPlayProtocols";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaProtocol> directPlayProtocols = new ArrayList<>();

    public OpenLiveStreamDto() {
    }

    public OpenLiveStreamDto openToken(@org.eclipse.jdt.annotation.NonNull String openToken) {
        this.openToken = openToken;
        return this;
    }

    /**
     * Gets or sets the open token.
     * 
     * @return openToken
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OPEN_TOKEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOpenToken() {
        return openToken;
    }

    @JsonProperty(value = JSON_PROPERTY_OPEN_TOKEN, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOpenToken(@org.eclipse.jdt.annotation.NonNull String openToken) {
        this.openToken = openToken;
    }

    public OpenLiveStreamDto userId(@org.eclipse.jdt.annotation.NonNull UUID userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets or sets the user id.
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

    public OpenLiveStreamDto playSessionId(@org.eclipse.jdt.annotation.NonNull String playSessionId) {
        this.playSessionId = playSessionId;
        return this;
    }

    /**
     * Gets or sets the play session id.
     * 
     * @return playSessionId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PLAY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPlaySessionId() {
        return playSessionId;
    }

    @JsonProperty(value = JSON_PROPERTY_PLAY_SESSION_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPlaySessionId(@org.eclipse.jdt.annotation.NonNull String playSessionId) {
        this.playSessionId = playSessionId;
    }

    public OpenLiveStreamDto maxStreamingBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate) {
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

    public OpenLiveStreamDto startTimeTicks(@org.eclipse.jdt.annotation.NonNull Long startTimeTicks) {
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

    public OpenLiveStreamDto audioStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer audioStreamIndex) {
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

    public OpenLiveStreamDto subtitleStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer subtitleStreamIndex) {
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

    public OpenLiveStreamDto maxAudioChannels(@org.eclipse.jdt.annotation.NonNull Integer maxAudioChannels) {
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

    public OpenLiveStreamDto itemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
        return this;
    }

    /**
     * Gets or sets the item id.
     * 
     * @return itemId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getItemId() {
        return itemId;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEM_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemId(@org.eclipse.jdt.annotation.NonNull UUID itemId) {
        this.itemId = itemId;
    }

    public OpenLiveStreamDto enableDirectPlay(@org.eclipse.jdt.annotation.NonNull Boolean enableDirectPlay) {
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

    public OpenLiveStreamDto enableDirectStream(@org.eclipse.jdt.annotation.NonNull Boolean enableDirectStream) {
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

    public OpenLiveStreamDto alwaysBurnInSubtitleWhenTranscoding(
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

    public OpenLiveStreamDto deviceProfile(@org.eclipse.jdt.annotation.NonNull DeviceProfile deviceProfile) {
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

    public OpenLiveStreamDto directPlayProtocols(
            @org.eclipse.jdt.annotation.NonNull List<MediaProtocol> directPlayProtocols) {
        this.directPlayProtocols = directPlayProtocols;
        return this;
    }

    public OpenLiveStreamDto addDirectPlayProtocolsItem(MediaProtocol directPlayProtocolsItem) {
        if (this.directPlayProtocols == null) {
            this.directPlayProtocols = new ArrayList<>();
        }
        this.directPlayProtocols.add(directPlayProtocolsItem);
        return this;
    }

    /**
     * Gets or sets the device play protocols.
     * 
     * @return directPlayProtocols
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DIRECT_PLAY_PROTOCOLS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaProtocol> getDirectPlayProtocols() {
        return directPlayProtocols;
    }

    @JsonProperty(value = JSON_PROPERTY_DIRECT_PLAY_PROTOCOLS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDirectPlayProtocols(@org.eclipse.jdt.annotation.NonNull List<MediaProtocol> directPlayProtocols) {
        this.directPlayProtocols = directPlayProtocols;
    }

    /**
     * Return true if this OpenLiveStreamDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        OpenLiveStreamDto openLiveStreamDto = (OpenLiveStreamDto) o;
        return Objects.equals(this.openToken, openLiveStreamDto.openToken)
                && Objects.equals(this.userId, openLiveStreamDto.userId)
                && Objects.equals(this.playSessionId, openLiveStreamDto.playSessionId)
                && Objects.equals(this.maxStreamingBitrate, openLiveStreamDto.maxStreamingBitrate)
                && Objects.equals(this.startTimeTicks, openLiveStreamDto.startTimeTicks)
                && Objects.equals(this.audioStreamIndex, openLiveStreamDto.audioStreamIndex)
                && Objects.equals(this.subtitleStreamIndex, openLiveStreamDto.subtitleStreamIndex)
                && Objects.equals(this.maxAudioChannels, openLiveStreamDto.maxAudioChannels)
                && Objects.equals(this.itemId, openLiveStreamDto.itemId)
                && Objects.equals(this.enableDirectPlay, openLiveStreamDto.enableDirectPlay)
                && Objects.equals(this.enableDirectStream, openLiveStreamDto.enableDirectStream)
                && Objects.equals(this.alwaysBurnInSubtitleWhenTranscoding,
                        openLiveStreamDto.alwaysBurnInSubtitleWhenTranscoding)
                && Objects.equals(this.deviceProfile, openLiveStreamDto.deviceProfile)
                && Objects.equals(this.directPlayProtocols, openLiveStreamDto.directPlayProtocols);
    }

    @Override
    public int hashCode() {
        return Objects.hash(openToken, userId, playSessionId, maxStreamingBitrate, startTimeTicks, audioStreamIndex,
                subtitleStreamIndex, maxAudioChannels, itemId, enableDirectPlay, enableDirectStream,
                alwaysBurnInSubtitleWhenTranscoding, deviceProfile, directPlayProtocols);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class OpenLiveStreamDto {\n");
        sb.append("    openToken: ").append(toIndentedString(openToken)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    playSessionId: ").append(toIndentedString(playSessionId)).append("\n");
        sb.append("    maxStreamingBitrate: ").append(toIndentedString(maxStreamingBitrate)).append("\n");
        sb.append("    startTimeTicks: ").append(toIndentedString(startTimeTicks)).append("\n");
        sb.append("    audioStreamIndex: ").append(toIndentedString(audioStreamIndex)).append("\n");
        sb.append("    subtitleStreamIndex: ").append(toIndentedString(subtitleStreamIndex)).append("\n");
        sb.append("    maxAudioChannels: ").append(toIndentedString(maxAudioChannels)).append("\n");
        sb.append("    itemId: ").append(toIndentedString(itemId)).append("\n");
        sb.append("    enableDirectPlay: ").append(toIndentedString(enableDirectPlay)).append("\n");
        sb.append("    enableDirectStream: ").append(toIndentedString(enableDirectStream)).append("\n");
        sb.append("    alwaysBurnInSubtitleWhenTranscoding: ")
                .append(toIndentedString(alwaysBurnInSubtitleWhenTranscoding)).append("\n");
        sb.append("    deviceProfile: ").append(toIndentedString(deviceProfile)).append("\n");
        sb.append("    directPlayProtocols: ").append(toIndentedString(directPlayProtocols)).append("\n");
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

        // add `OpenToken` to the URL query string
        if (getOpenToken() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOpenToken%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOpenToken()))));
        }

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `PlaySessionId` to the URL query string
        if (getPlaySessionId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPlaySessionId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPlaySessionId()))));
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

        // add `ItemId` to the URL query string
        if (getItemId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sItemId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getItemId()))));
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

        // add `AlwaysBurnInSubtitleWhenTranscoding` to the URL query string
        if (getAlwaysBurnInSubtitleWhenTranscoding() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAlwaysBurnInSubtitleWhenTranscoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlwaysBurnInSubtitleWhenTranscoding()))));
        }

        // add `DeviceProfile` to the URL query string
        if (getDeviceProfile() != null) {
            joiner.add(getDeviceProfile().toUrlQueryString(prefix + "DeviceProfile" + suffix));
        }

        // add `DirectPlayProtocols` to the URL query string
        if (getDirectPlayProtocols() != null) {
            for (int i = 0; i < getDirectPlayProtocols().size(); i++) {
                if (getDirectPlayProtocols().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sDirectPlayProtocols%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getDirectPlayProtocols().get(i)))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private OpenLiveStreamDto instance;

        public Builder() {
            this(new OpenLiveStreamDto());
        }

        protected Builder(OpenLiveStreamDto instance) {
            this.instance = instance;
        }

        public OpenLiveStreamDto.Builder openToken(String openToken) {
            this.instance.openToken = openToken;
            return this;
        }

        public OpenLiveStreamDto.Builder userId(UUID userId) {
            this.instance.userId = userId;
            return this;
        }

        public OpenLiveStreamDto.Builder playSessionId(String playSessionId) {
            this.instance.playSessionId = playSessionId;
            return this;
        }

        public OpenLiveStreamDto.Builder maxStreamingBitrate(Integer maxStreamingBitrate) {
            this.instance.maxStreamingBitrate = maxStreamingBitrate;
            return this;
        }

        public OpenLiveStreamDto.Builder startTimeTicks(Long startTimeTicks) {
            this.instance.startTimeTicks = startTimeTicks;
            return this;
        }

        public OpenLiveStreamDto.Builder audioStreamIndex(Integer audioStreamIndex) {
            this.instance.audioStreamIndex = audioStreamIndex;
            return this;
        }

        public OpenLiveStreamDto.Builder subtitleStreamIndex(Integer subtitleStreamIndex) {
            this.instance.subtitleStreamIndex = subtitleStreamIndex;
            return this;
        }

        public OpenLiveStreamDto.Builder maxAudioChannels(Integer maxAudioChannels) {
            this.instance.maxAudioChannels = maxAudioChannels;
            return this;
        }

        public OpenLiveStreamDto.Builder itemId(UUID itemId) {
            this.instance.itemId = itemId;
            return this;
        }

        public OpenLiveStreamDto.Builder enableDirectPlay(Boolean enableDirectPlay) {
            this.instance.enableDirectPlay = enableDirectPlay;
            return this;
        }

        public OpenLiveStreamDto.Builder enableDirectStream(Boolean enableDirectStream) {
            this.instance.enableDirectStream = enableDirectStream;
            return this;
        }

        public OpenLiveStreamDto.Builder alwaysBurnInSubtitleWhenTranscoding(
                Boolean alwaysBurnInSubtitleWhenTranscoding) {
            this.instance.alwaysBurnInSubtitleWhenTranscoding = alwaysBurnInSubtitleWhenTranscoding;
            return this;
        }

        public OpenLiveStreamDto.Builder deviceProfile(DeviceProfile deviceProfile) {
            this.instance.deviceProfile = deviceProfile;
            return this;
        }

        public OpenLiveStreamDto.Builder directPlayProtocols(List<MediaProtocol> directPlayProtocols) {
            this.instance.directPlayProtocols = directPlayProtocols;
            return this;
        }

        /**
         * returns a built OpenLiveStreamDto instance.
         *
         * The builder is not reusable.
         */
        public OpenLiveStreamDto build() {
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
    public static OpenLiveStreamDto.Builder builder() {
        return new OpenLiveStreamDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public OpenLiveStreamDto.Builder toBuilder() {
        return new OpenLiveStreamDto.Builder().openToken(getOpenToken()).userId(getUserId())
                .playSessionId(getPlaySessionId()).maxStreamingBitrate(getMaxStreamingBitrate())
                .startTimeTicks(getStartTimeTicks()).audioStreamIndex(getAudioStreamIndex())
                .subtitleStreamIndex(getSubtitleStreamIndex()).maxAudioChannels(getMaxAudioChannels())
                .itemId(getItemId()).enableDirectPlay(getEnableDirectPlay()).enableDirectStream(getEnableDirectStream())
                .alwaysBurnInSubtitleWhenTranscoding(getAlwaysBurnInSubtitleWhenTranscoding())
                .deviceProfile(getDeviceProfile()).directPlayProtocols(getDirectPlayProtocols());
    }
}
