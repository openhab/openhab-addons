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

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * A class for transcoding profile information. Note for client developers: Conditions defined in
 * MediaBrowser.Model.Dlna.CodecProfile has higher priority and can override values defined here.
 */
@JsonPropertyOrder({ TranscodingProfile.JSON_PROPERTY_CONTAINER, TranscodingProfile.JSON_PROPERTY_TYPE,
        TranscodingProfile.JSON_PROPERTY_VIDEO_CODEC, TranscodingProfile.JSON_PROPERTY_AUDIO_CODEC,
        TranscodingProfile.JSON_PROPERTY_PROTOCOL, TranscodingProfile.JSON_PROPERTY_ESTIMATE_CONTENT_LENGTH,
        TranscodingProfile.JSON_PROPERTY_ENABLE_MPEGTS_M2_TS_MODE, TranscodingProfile.JSON_PROPERTY_TRANSCODE_SEEK_INFO,
        TranscodingProfile.JSON_PROPERTY_COPY_TIMESTAMPS, TranscodingProfile.JSON_PROPERTY_CONTEXT,
        TranscodingProfile.JSON_PROPERTY_ENABLE_SUBTITLES_IN_MANIFEST,
        TranscodingProfile.JSON_PROPERTY_MAX_AUDIO_CHANNELS, TranscodingProfile.JSON_PROPERTY_MIN_SEGMENTS,
        TranscodingProfile.JSON_PROPERTY_SEGMENT_LENGTH, TranscodingProfile.JSON_PROPERTY_BREAK_ON_NON_KEY_FRAMES,
        TranscodingProfile.JSON_PROPERTY_CONDITIONS, TranscodingProfile.JSON_PROPERTY_ENABLE_AUDIO_VBR_ENCODING })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TranscodingProfile {
    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.Nullable
    private String container;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.Nullable
    private DlnaProfileType type;

    public static final String JSON_PROPERTY_VIDEO_CODEC = "VideoCodec";
    @org.eclipse.jdt.annotation.Nullable
    private String videoCodec;

    public static final String JSON_PROPERTY_AUDIO_CODEC = "AudioCodec";
    @org.eclipse.jdt.annotation.Nullable
    private String audioCodec;

    public static final String JSON_PROPERTY_PROTOCOL = "Protocol";
    @org.eclipse.jdt.annotation.Nullable
    private MediaStreamProtocol protocol;

    public static final String JSON_PROPERTY_ESTIMATE_CONTENT_LENGTH = "EstimateContentLength";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean estimateContentLength = false;

    public static final String JSON_PROPERTY_ENABLE_MPEGTS_M2_TS_MODE = "EnableMpegtsM2TsMode";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableMpegtsM2TsMode = false;

    public static final String JSON_PROPERTY_TRANSCODE_SEEK_INFO = "TranscodeSeekInfo";
    @org.eclipse.jdt.annotation.Nullable
    private TranscodeSeekInfo transcodeSeekInfo = TranscodeSeekInfo.AUTO;

    public static final String JSON_PROPERTY_COPY_TIMESTAMPS = "CopyTimestamps";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean copyTimestamps = false;

    public static final String JSON_PROPERTY_CONTEXT = "Context";
    @org.eclipse.jdt.annotation.Nullable
    private EncodingContext context = EncodingContext.STREAMING;

    public static final String JSON_PROPERTY_ENABLE_SUBTITLES_IN_MANIFEST = "EnableSubtitlesInManifest";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableSubtitlesInManifest = false;

    public static final String JSON_PROPERTY_MAX_AUDIO_CHANNELS = "MaxAudioChannels";
    @org.eclipse.jdt.annotation.Nullable
    private String maxAudioChannels;

    public static final String JSON_PROPERTY_MIN_SEGMENTS = "MinSegments";
    @org.eclipse.jdt.annotation.Nullable
    private Integer minSegments = 0;

    public static final String JSON_PROPERTY_SEGMENT_LENGTH = "SegmentLength";
    @org.eclipse.jdt.annotation.Nullable
    private Integer segmentLength = 0;

    public static final String JSON_PROPERTY_BREAK_ON_NON_KEY_FRAMES = "BreakOnNonKeyFrames";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean breakOnNonKeyFrames = false;

    public static final String JSON_PROPERTY_CONDITIONS = "Conditions";
    @org.eclipse.jdt.annotation.Nullable
    private List<ProfileCondition> conditions = new ArrayList<>();

    public static final String JSON_PROPERTY_ENABLE_AUDIO_VBR_ENCODING = "EnableAudioVbrEncoding";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableAudioVbrEncoding = true;

    public TranscodingProfile() {
    }

    public TranscodingProfile container(@org.eclipse.jdt.annotation.Nullable String container) {
        this.container = container;
        return this;
    }

    /**
     * Gets or sets the container.
     * 
     * @return container
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getContainer() {
        return container;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainer(@org.eclipse.jdt.annotation.Nullable String container) {
        this.container = container;
    }

    public TranscodingProfile type(@org.eclipse.jdt.annotation.Nullable DlnaProfileType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the DLNA profile type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DlnaProfileType getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.Nullable DlnaProfileType type) {
        this.type = type;
    }

    public TranscodingProfile videoCodec(@org.eclipse.jdt.annotation.Nullable String videoCodec) {
        this.videoCodec = videoCodec;
        return this;
    }

    /**
     * Gets or sets the video codec.
     * 
     * @return videoCodec
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VIDEO_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVideoCodec() {
        return videoCodec;
    }

    @JsonProperty(value = JSON_PROPERTY_VIDEO_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVideoCodec(@org.eclipse.jdt.annotation.Nullable String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public TranscodingProfile audioCodec(@org.eclipse.jdt.annotation.Nullable String audioCodec) {
        this.audioCodec = audioCodec;
        return this;
    }

    /**
     * Gets or sets the audio codec.
     * 
     * @return audioCodec
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_AUDIO_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAudioCodec() {
        return audioCodec;
    }

    @JsonProperty(value = JSON_PROPERTY_AUDIO_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAudioCodec(@org.eclipse.jdt.annotation.Nullable String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public TranscodingProfile protocol(@org.eclipse.jdt.annotation.Nullable MediaStreamProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Gets or sets the protocol.
     * 
     * @return protocol
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROTOCOL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaStreamProtocol getProtocol() {
        return protocol;
    }

    @JsonProperty(value = JSON_PROPERTY_PROTOCOL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProtocol(@org.eclipse.jdt.annotation.Nullable MediaStreamProtocol protocol) {
        this.protocol = protocol;
    }

    public TranscodingProfile estimateContentLength(
            @org.eclipse.jdt.annotation.Nullable Boolean estimateContentLength) {
        this.estimateContentLength = estimateContentLength;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the content length should be estimated.
     * 
     * @return estimateContentLength
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ESTIMATE_CONTENT_LENGTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEstimateContentLength() {
        return estimateContentLength;
    }

    @JsonProperty(value = JSON_PROPERTY_ESTIMATE_CONTENT_LENGTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEstimateContentLength(@org.eclipse.jdt.annotation.Nullable Boolean estimateContentLength) {
        this.estimateContentLength = estimateContentLength;
    }

    public TranscodingProfile enableMpegtsM2TsMode(@org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode) {
        this.enableMpegtsM2TsMode = enableMpegtsM2TsMode;
        return this;
    }

    /**
     * Gets or sets a value indicating whether M2TS mode is enabled.
     * 
     * @return enableMpegtsM2TsMode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_MPEGTS_M2_TS_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableMpegtsM2TsMode() {
        return enableMpegtsM2TsMode;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_MPEGTS_M2_TS_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableMpegtsM2TsMode(@org.eclipse.jdt.annotation.Nullable Boolean enableMpegtsM2TsMode) {
        this.enableMpegtsM2TsMode = enableMpegtsM2TsMode;
    }

    public TranscodingProfile transcodeSeekInfo(
            @org.eclipse.jdt.annotation.Nullable TranscodeSeekInfo transcodeSeekInfo) {
        this.transcodeSeekInfo = transcodeSeekInfo;
        return this;
    }

    /**
     * Gets or sets the transcoding seek info mode.
     * 
     * @return transcodeSeekInfo
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TRANSCODE_SEEK_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TranscodeSeekInfo getTranscodeSeekInfo() {
        return transcodeSeekInfo;
    }

    @JsonProperty(value = JSON_PROPERTY_TRANSCODE_SEEK_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodeSeekInfo(@org.eclipse.jdt.annotation.Nullable TranscodeSeekInfo transcodeSeekInfo) {
        this.transcodeSeekInfo = transcodeSeekInfo;
    }

    public TranscodingProfile copyTimestamps(@org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps) {
        this.copyTimestamps = copyTimestamps;
        return this;
    }

    /**
     * Gets or sets a value indicating whether timestamps should be copied.
     * 
     * @return copyTimestamps
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COPY_TIMESTAMPS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCopyTimestamps() {
        return copyTimestamps;
    }

    @JsonProperty(value = JSON_PROPERTY_COPY_TIMESTAMPS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCopyTimestamps(@org.eclipse.jdt.annotation.Nullable Boolean copyTimestamps) {
        this.copyTimestamps = copyTimestamps;
    }

    public TranscodingProfile context(@org.eclipse.jdt.annotation.Nullable EncodingContext context) {
        this.context = context;
        return this;
    }

    /**
     * Gets or sets the encoding context.
     * 
     * @return context
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CONTEXT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public EncodingContext getContext() {
        return context;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTEXT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContext(@org.eclipse.jdt.annotation.Nullable EncodingContext context) {
        this.context = context;
    }

    public TranscodingProfile enableSubtitlesInManifest(
            @org.eclipse.jdt.annotation.Nullable Boolean enableSubtitlesInManifest) {
        this.enableSubtitlesInManifest = enableSubtitlesInManifest;
        return this;
    }

    /**
     * Gets or sets a value indicating whether subtitles are allowed in the manifest.
     * 
     * @return enableSubtitlesInManifest
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_SUBTITLES_IN_MANIFEST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableSubtitlesInManifest() {
        return enableSubtitlesInManifest;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_SUBTITLES_IN_MANIFEST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSubtitlesInManifest(@org.eclipse.jdt.annotation.Nullable Boolean enableSubtitlesInManifest) {
        this.enableSubtitlesInManifest = enableSubtitlesInManifest;
    }

    public TranscodingProfile maxAudioChannels(@org.eclipse.jdt.annotation.Nullable String maxAudioChannels) {
        this.maxAudioChannels = maxAudioChannels;
        return this;
    }

    /**
     * Gets or sets the maximum audio channels.
     * 
     * @return maxAudioChannels
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MAX_AUDIO_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMaxAudioChannels() {
        return maxAudioChannels;
    }

    @JsonProperty(value = JSON_PROPERTY_MAX_AUDIO_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxAudioChannels(@org.eclipse.jdt.annotation.Nullable String maxAudioChannels) {
        this.maxAudioChannels = maxAudioChannels;
    }

    public TranscodingProfile minSegments(@org.eclipse.jdt.annotation.Nullable Integer minSegments) {
        this.minSegments = minSegments;
        return this;
    }

    /**
     * Gets or sets the minimum amount of segments.
     * 
     * @return minSegments
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MIN_SEGMENTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMinSegments() {
        return minSegments;
    }

    @JsonProperty(value = JSON_PROPERTY_MIN_SEGMENTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMinSegments(@org.eclipse.jdt.annotation.Nullable Integer minSegments) {
        this.minSegments = minSegments;
    }

    public TranscodingProfile segmentLength(@org.eclipse.jdt.annotation.Nullable Integer segmentLength) {
        this.segmentLength = segmentLength;
        return this;
    }

    /**
     * Gets or sets the segment length.
     * 
     * @return segmentLength
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SEGMENT_LENGTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSegmentLength() {
        return segmentLength;
    }

    @JsonProperty(value = JSON_PROPERTY_SEGMENT_LENGTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSegmentLength(@org.eclipse.jdt.annotation.Nullable Integer segmentLength) {
        this.segmentLength = segmentLength;
    }

    public TranscodingProfile breakOnNonKeyFrames(@org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames) {
        this.breakOnNonKeyFrames = breakOnNonKeyFrames;
        return this;
    }

    /**
     * Gets or sets a value indicating whether breaking the video stream on non-keyframes is supported.
     * 
     * @return breakOnNonKeyFrames
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BREAK_ON_NON_KEY_FRAMES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getBreakOnNonKeyFrames() {
        return breakOnNonKeyFrames;
    }

    @JsonProperty(value = JSON_PROPERTY_BREAK_ON_NON_KEY_FRAMES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBreakOnNonKeyFrames(@org.eclipse.jdt.annotation.Nullable Boolean breakOnNonKeyFrames) {
        this.breakOnNonKeyFrames = breakOnNonKeyFrames;
    }

    public TranscodingProfile conditions(@org.eclipse.jdt.annotation.Nullable List<ProfileCondition> conditions) {
        this.conditions = conditions;
        return this;
    }

    public TranscodingProfile addConditionsItem(ProfileCondition conditionsItem) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<>();
        }
        this.conditions.add(conditionsItem);
        return this;
    }

    /**
     * Gets or sets the profile conditions.
     * 
     * @return conditions
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CONDITIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ProfileCondition> getConditions() {
        return conditions;
    }

    @JsonProperty(value = JSON_PROPERTY_CONDITIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConditions(@org.eclipse.jdt.annotation.Nullable List<ProfileCondition> conditions) {
        this.conditions = conditions;
    }

    public TranscodingProfile enableAudioVbrEncoding(
            @org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding) {
        this.enableAudioVbrEncoding = enableAudioVbrEncoding;
        return this;
    }

    /**
     * Gets or sets a value indicating whether variable bitrate encoding is supported.
     * 
     * @return enableAudioVbrEncoding
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUDIO_VBR_ENCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableAudioVbrEncoding() {
        return enableAudioVbrEncoding;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUDIO_VBR_ENCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAudioVbrEncoding(@org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbrEncoding) {
        this.enableAudioVbrEncoding = enableAudioVbrEncoding;
    }

    /**
     * Return true if this TranscodingProfile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TranscodingProfile transcodingProfile = (TranscodingProfile) o;
        return Objects.equals(this.container, transcodingProfile.container)
                && Objects.equals(this.type, transcodingProfile.type)
                && Objects.equals(this.videoCodec, transcodingProfile.videoCodec)
                && Objects.equals(this.audioCodec, transcodingProfile.audioCodec)
                && Objects.equals(this.protocol, transcodingProfile.protocol)
                && Objects.equals(this.estimateContentLength, transcodingProfile.estimateContentLength)
                && Objects.equals(this.enableMpegtsM2TsMode, transcodingProfile.enableMpegtsM2TsMode)
                && Objects.equals(this.transcodeSeekInfo, transcodingProfile.transcodeSeekInfo)
                && Objects.equals(this.copyTimestamps, transcodingProfile.copyTimestamps)
                && Objects.equals(this.context, transcodingProfile.context)
                && Objects.equals(this.enableSubtitlesInManifest, transcodingProfile.enableSubtitlesInManifest)
                && Objects.equals(this.maxAudioChannels, transcodingProfile.maxAudioChannels)
                && Objects.equals(this.minSegments, transcodingProfile.minSegments)
                && Objects.equals(this.segmentLength, transcodingProfile.segmentLength)
                && Objects.equals(this.breakOnNonKeyFrames, transcodingProfile.breakOnNonKeyFrames)
                && Objects.equals(this.conditions, transcodingProfile.conditions)
                && Objects.equals(this.enableAudioVbrEncoding, transcodingProfile.enableAudioVbrEncoding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(container, type, videoCodec, audioCodec, protocol, estimateContentLength,
                enableMpegtsM2TsMode, transcodeSeekInfo, copyTimestamps, context, enableSubtitlesInManifest,
                maxAudioChannels, minSegments, segmentLength, breakOnNonKeyFrames, conditions, enableAudioVbrEncoding);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TranscodingProfile {\n");
        sb.append("    container: ").append(toIndentedString(container)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    videoCodec: ").append(toIndentedString(videoCodec)).append("\n");
        sb.append("    audioCodec: ").append(toIndentedString(audioCodec)).append("\n");
        sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
        sb.append("    estimateContentLength: ").append(toIndentedString(estimateContentLength)).append("\n");
        sb.append("    enableMpegtsM2TsMode: ").append(toIndentedString(enableMpegtsM2TsMode)).append("\n");
        sb.append("    transcodeSeekInfo: ").append(toIndentedString(transcodeSeekInfo)).append("\n");
        sb.append("    copyTimestamps: ").append(toIndentedString(copyTimestamps)).append("\n");
        sb.append("    context: ").append(toIndentedString(context)).append("\n");
        sb.append("    enableSubtitlesInManifest: ").append(toIndentedString(enableSubtitlesInManifest)).append("\n");
        sb.append("    maxAudioChannels: ").append(toIndentedString(maxAudioChannels)).append("\n");
        sb.append("    minSegments: ").append(toIndentedString(minSegments)).append("\n");
        sb.append("    segmentLength: ").append(toIndentedString(segmentLength)).append("\n");
        sb.append("    breakOnNonKeyFrames: ").append(toIndentedString(breakOnNonKeyFrames)).append("\n");
        sb.append("    conditions: ").append(toIndentedString(conditions)).append("\n");
        sb.append("    enableAudioVbrEncoding: ").append(toIndentedString(enableAudioVbrEncoding)).append("\n");
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

        // add `Container` to the URL query string
        if (getContainer() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContainer()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `VideoCodec` to the URL query string
        if (getVideoCodec() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVideoCodec%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideoCodec()))));
        }

        // add `AudioCodec` to the URL query string
        if (getAudioCodec() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAudioCodec%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAudioCodec()))));
        }

        // add `Protocol` to the URL query string
        if (getProtocol() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProtocol%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProtocol()))));
        }

        // add `EstimateContentLength` to the URL query string
        if (getEstimateContentLength() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEstimateContentLength%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEstimateContentLength()))));
        }

        // add `EnableMpegtsM2TsMode` to the URL query string
        if (getEnableMpegtsM2TsMode() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableMpegtsM2TsMode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableMpegtsM2TsMode()))));
        }

        // add `TranscodeSeekInfo` to the URL query string
        if (getTranscodeSeekInfo() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTranscodeSeekInfo%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTranscodeSeekInfo()))));
        }

        // add `CopyTimestamps` to the URL query string
        if (getCopyTimestamps() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCopyTimestamps%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCopyTimestamps()))));
        }

        // add `Context` to the URL query string
        if (getContext() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sContext%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContext()))));
        }

        // add `EnableSubtitlesInManifest` to the URL query string
        if (getEnableSubtitlesInManifest() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableSubtitlesInManifest%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableSubtitlesInManifest()))));
        }

        // add `MaxAudioChannels` to the URL query string
        if (getMaxAudioChannels() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMaxAudioChannels%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxAudioChannels()))));
        }

        // add `MinSegments` to the URL query string
        if (getMinSegments() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMinSegments%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMinSegments()))));
        }

        // add `SegmentLength` to the URL query string
        if (getSegmentLength() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSegmentLength%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSegmentLength()))));
        }

        // add `BreakOnNonKeyFrames` to the URL query string
        if (getBreakOnNonKeyFrames() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBreakOnNonKeyFrames%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBreakOnNonKeyFrames()))));
        }

        // add `Conditions` to the URL query string
        if (getConditions() != null) {
            for (int i = 0; i < getConditions().size(); i++) {
                if (getConditions().get(i) != null) {
                    joiner.add(getConditions().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sConditions%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `EnableAudioVbrEncoding` to the URL query string
        if (getEnableAudioVbrEncoding() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableAudioVbrEncoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAudioVbrEncoding()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private TranscodingProfile instance;

        public Builder() {
            this(new TranscodingProfile());
        }

        protected Builder(TranscodingProfile instance) {
            this.instance = instance;
        }

        public TranscodingProfile.Builder container(String container) {
            this.instance.container = container;
            return this;
        }

        public TranscodingProfile.Builder type(DlnaProfileType type) {
            this.instance.type = type;
            return this;
        }

        public TranscodingProfile.Builder videoCodec(String videoCodec) {
            this.instance.videoCodec = videoCodec;
            return this;
        }

        public TranscodingProfile.Builder audioCodec(String audioCodec) {
            this.instance.audioCodec = audioCodec;
            return this;
        }

        public TranscodingProfile.Builder protocol(MediaStreamProtocol protocol) {
            this.instance.protocol = protocol;
            return this;
        }

        public TranscodingProfile.Builder estimateContentLength(Boolean estimateContentLength) {
            this.instance.estimateContentLength = estimateContentLength;
            return this;
        }

        public TranscodingProfile.Builder enableMpegtsM2TsMode(Boolean enableMpegtsM2TsMode) {
            this.instance.enableMpegtsM2TsMode = enableMpegtsM2TsMode;
            return this;
        }

        public TranscodingProfile.Builder transcodeSeekInfo(TranscodeSeekInfo transcodeSeekInfo) {
            this.instance.transcodeSeekInfo = transcodeSeekInfo;
            return this;
        }

        public TranscodingProfile.Builder copyTimestamps(Boolean copyTimestamps) {
            this.instance.copyTimestamps = copyTimestamps;
            return this;
        }

        public TranscodingProfile.Builder context(EncodingContext context) {
            this.instance.context = context;
            return this;
        }

        public TranscodingProfile.Builder enableSubtitlesInManifest(Boolean enableSubtitlesInManifest) {
            this.instance.enableSubtitlesInManifest = enableSubtitlesInManifest;
            return this;
        }

        public TranscodingProfile.Builder maxAudioChannels(String maxAudioChannels) {
            this.instance.maxAudioChannels = maxAudioChannels;
            return this;
        }

        public TranscodingProfile.Builder minSegments(Integer minSegments) {
            this.instance.minSegments = minSegments;
            return this;
        }

        public TranscodingProfile.Builder segmentLength(Integer segmentLength) {
            this.instance.segmentLength = segmentLength;
            return this;
        }

        public TranscodingProfile.Builder breakOnNonKeyFrames(Boolean breakOnNonKeyFrames) {
            this.instance.breakOnNonKeyFrames = breakOnNonKeyFrames;
            return this;
        }

        public TranscodingProfile.Builder conditions(List<ProfileCondition> conditions) {
            this.instance.conditions = conditions;
            return this;
        }

        public TranscodingProfile.Builder enableAudioVbrEncoding(Boolean enableAudioVbrEncoding) {
            this.instance.enableAudioVbrEncoding = enableAudioVbrEncoding;
            return this;
        }

        /**
         * returns a built TranscodingProfile instance.
         *
         * The builder is not reusable.
         */
        public TranscodingProfile build() {
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
    public static TranscodingProfile.Builder builder() {
        return new TranscodingProfile.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public TranscodingProfile.Builder toBuilder() {
        return new TranscodingProfile.Builder().container(getContainer()).type(getType()).videoCodec(getVideoCodec())
                .audioCodec(getAudioCodec()).protocol(getProtocol()).estimateContentLength(getEstimateContentLength())
                .enableMpegtsM2TsMode(getEnableMpegtsM2TsMode()).transcodeSeekInfo(getTranscodeSeekInfo())
                .copyTimestamps(getCopyTimestamps()).context(getContext())
                .enableSubtitlesInManifest(getEnableSubtitlesInManifest()).maxAudioChannels(getMaxAudioChannels())
                .minSegments(getMinSegments()).segmentLength(getSegmentLength())
                .breakOnNonKeyFrames(getBreakOnNonKeyFrames()).conditions(getConditions())
                .enableAudioVbrEncoding(getEnableAudioVbrEncoding());
    }
}
