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
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private DlnaProfileType type;

    public static final String JSON_PROPERTY_VIDEO_CODEC = "VideoCodec";
    @org.eclipse.jdt.annotation.NonNull
    private String videoCodec;

    public static final String JSON_PROPERTY_AUDIO_CODEC = "AudioCodec";
    @org.eclipse.jdt.annotation.NonNull
    private String audioCodec;

    public static final String JSON_PROPERTY_PROTOCOL = "Protocol";
    @org.eclipse.jdt.annotation.NonNull
    private MediaStreamProtocol protocol;

    public static final String JSON_PROPERTY_ESTIMATE_CONTENT_LENGTH = "EstimateContentLength";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean estimateContentLength = false;

    public static final String JSON_PROPERTY_ENABLE_MPEGTS_M2_TS_MODE = "EnableMpegtsM2TsMode";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableMpegtsM2TsMode = false;

    public static final String JSON_PROPERTY_TRANSCODE_SEEK_INFO = "TranscodeSeekInfo";
    @org.eclipse.jdt.annotation.NonNull
    private TranscodeSeekInfo transcodeSeekInfo = TranscodeSeekInfo.AUTO;

    public static final String JSON_PROPERTY_COPY_TIMESTAMPS = "CopyTimestamps";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean copyTimestamps = false;

    public static final String JSON_PROPERTY_CONTEXT = "Context";
    @org.eclipse.jdt.annotation.NonNull
    private EncodingContext context = EncodingContext.STREAMING;

    public static final String JSON_PROPERTY_ENABLE_SUBTITLES_IN_MANIFEST = "EnableSubtitlesInManifest";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableSubtitlesInManifest = false;

    public static final String JSON_PROPERTY_MAX_AUDIO_CHANNELS = "MaxAudioChannels";
    @org.eclipse.jdt.annotation.NonNull
    private String maxAudioChannels;

    public static final String JSON_PROPERTY_MIN_SEGMENTS = "MinSegments";
    @org.eclipse.jdt.annotation.NonNull
    private Integer minSegments = 0;

    public static final String JSON_PROPERTY_SEGMENT_LENGTH = "SegmentLength";
    @org.eclipse.jdt.annotation.NonNull
    private Integer segmentLength = 0;

    public static final String JSON_PROPERTY_BREAK_ON_NON_KEY_FRAMES = "BreakOnNonKeyFrames";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean breakOnNonKeyFrames = false;

    public static final String JSON_PROPERTY_CONDITIONS = "Conditions";
    @org.eclipse.jdt.annotation.NonNull
    private List<ProfileCondition> conditions = new ArrayList<>();

    public static final String JSON_PROPERTY_ENABLE_AUDIO_VBR_ENCODING = "EnableAudioVbrEncoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableAudioVbrEncoding = true;

    public TranscodingProfile() {
    }

    public TranscodingProfile container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Gets or sets the container.
     * 
     * @return container
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getContainer() {
        return container;
    }

    @JsonProperty(JSON_PROPERTY_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainer(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
    }

    public TranscodingProfile type(@org.eclipse.jdt.annotation.NonNull DlnaProfileType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the DLNA profile type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public DlnaProfileType getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull DlnaProfileType type) {
        this.type = type;
    }

    public TranscodingProfile videoCodec(@org.eclipse.jdt.annotation.NonNull String videoCodec) {
        this.videoCodec = videoCodec;
        return this;
    }

    /**
     * Gets or sets the video codec.
     * 
     * @return videoCodec
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VIDEO_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getVideoCodec() {
        return videoCodec;
    }

    @JsonProperty(JSON_PROPERTY_VIDEO_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVideoCodec(@org.eclipse.jdt.annotation.NonNull String videoCodec) {
        this.videoCodec = videoCodec;
    }

    public TranscodingProfile audioCodec(@org.eclipse.jdt.annotation.NonNull String audioCodec) {
        this.audioCodec = audioCodec;
        return this;
    }

    /**
     * Gets or sets the audio codec.
     * 
     * @return audioCodec
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_AUDIO_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getAudioCodec() {
        return audioCodec;
    }

    @JsonProperty(JSON_PROPERTY_AUDIO_CODEC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAudioCodec(@org.eclipse.jdt.annotation.NonNull String audioCodec) {
        this.audioCodec = audioCodec;
    }

    public TranscodingProfile protocol(@org.eclipse.jdt.annotation.NonNull MediaStreamProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Gets or sets the protocol.
     * 
     * @return protocol
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROTOCOL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public MediaStreamProtocol getProtocol() {
        return protocol;
    }

    @JsonProperty(JSON_PROPERTY_PROTOCOL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProtocol(@org.eclipse.jdt.annotation.NonNull MediaStreamProtocol protocol) {
        this.protocol = protocol;
    }

    public TranscodingProfile estimateContentLength(@org.eclipse.jdt.annotation.NonNull Boolean estimateContentLength) {
        this.estimateContentLength = estimateContentLength;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the content length should be estimated.
     * 
     * @return estimateContentLength
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ESTIMATE_CONTENT_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEstimateContentLength() {
        return estimateContentLength;
    }

    @JsonProperty(JSON_PROPERTY_ESTIMATE_CONTENT_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEstimateContentLength(@org.eclipse.jdt.annotation.NonNull Boolean estimateContentLength) {
        this.estimateContentLength = estimateContentLength;
    }

    public TranscodingProfile enableMpegtsM2TsMode(@org.eclipse.jdt.annotation.NonNull Boolean enableMpegtsM2TsMode) {
        this.enableMpegtsM2TsMode = enableMpegtsM2TsMode;
        return this;
    }

    /**
     * Gets or sets a value indicating whether M2TS mode is enabled.
     * 
     * @return enableMpegtsM2TsMode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_MPEGTS_M2_TS_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableMpegtsM2TsMode() {
        return enableMpegtsM2TsMode;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_MPEGTS_M2_TS_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableMpegtsM2TsMode(@org.eclipse.jdt.annotation.NonNull Boolean enableMpegtsM2TsMode) {
        this.enableMpegtsM2TsMode = enableMpegtsM2TsMode;
    }

    public TranscodingProfile transcodeSeekInfo(
            @org.eclipse.jdt.annotation.NonNull TranscodeSeekInfo transcodeSeekInfo) {
        this.transcodeSeekInfo = transcodeSeekInfo;
        return this;
    }

    /**
     * Gets or sets the transcoding seek info mode.
     * 
     * @return transcodeSeekInfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRANSCODE_SEEK_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TranscodeSeekInfo getTranscodeSeekInfo() {
        return transcodeSeekInfo;
    }

    @JsonProperty(JSON_PROPERTY_TRANSCODE_SEEK_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodeSeekInfo(@org.eclipse.jdt.annotation.NonNull TranscodeSeekInfo transcodeSeekInfo) {
        this.transcodeSeekInfo = transcodeSeekInfo;
    }

    public TranscodingProfile copyTimestamps(@org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps) {
        this.copyTimestamps = copyTimestamps;
        return this;
    }

    /**
     * Gets or sets a value indicating whether timestamps should be copied.
     * 
     * @return copyTimestamps
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COPY_TIMESTAMPS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getCopyTimestamps() {
        return copyTimestamps;
    }

    @JsonProperty(JSON_PROPERTY_COPY_TIMESTAMPS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCopyTimestamps(@org.eclipse.jdt.annotation.NonNull Boolean copyTimestamps) {
        this.copyTimestamps = copyTimestamps;
    }

    public TranscodingProfile context(@org.eclipse.jdt.annotation.NonNull EncodingContext context) {
        this.context = context;
        return this;
    }

    /**
     * Gets or sets the encoding context.
     * 
     * @return context
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTEXT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public EncodingContext getContext() {
        return context;
    }

    @JsonProperty(JSON_PROPERTY_CONTEXT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContext(@org.eclipse.jdt.annotation.NonNull EncodingContext context) {
        this.context = context;
    }

    public TranscodingProfile enableSubtitlesInManifest(
            @org.eclipse.jdt.annotation.NonNull Boolean enableSubtitlesInManifest) {
        this.enableSubtitlesInManifest = enableSubtitlesInManifest;
        return this;
    }

    /**
     * Gets or sets a value indicating whether subtitles are allowed in the manifest.
     * 
     * @return enableSubtitlesInManifest
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_SUBTITLES_IN_MANIFEST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableSubtitlesInManifest() {
        return enableSubtitlesInManifest;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_SUBTITLES_IN_MANIFEST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSubtitlesInManifest(@org.eclipse.jdt.annotation.NonNull Boolean enableSubtitlesInManifest) {
        this.enableSubtitlesInManifest = enableSubtitlesInManifest;
    }

    public TranscodingProfile maxAudioChannels(@org.eclipse.jdt.annotation.NonNull String maxAudioChannels) {
        this.maxAudioChannels = maxAudioChannels;
        return this;
    }

    /**
     * Gets or sets the maximum audio channels.
     * 
     * @return maxAudioChannels
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_AUDIO_CHANNELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMaxAudioChannels() {
        return maxAudioChannels;
    }

    @JsonProperty(JSON_PROPERTY_MAX_AUDIO_CHANNELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxAudioChannels(@org.eclipse.jdt.annotation.NonNull String maxAudioChannels) {
        this.maxAudioChannels = maxAudioChannels;
    }

    public TranscodingProfile minSegments(@org.eclipse.jdt.annotation.NonNull Integer minSegments) {
        this.minSegments = minSegments;
        return this;
    }

    /**
     * Gets or sets the minimum amount of segments.
     * 
     * @return minSegments
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MIN_SEGMENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getMinSegments() {
        return minSegments;
    }

    @JsonProperty(JSON_PROPERTY_MIN_SEGMENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMinSegments(@org.eclipse.jdt.annotation.NonNull Integer minSegments) {
        this.minSegments = minSegments;
    }

    public TranscodingProfile segmentLength(@org.eclipse.jdt.annotation.NonNull Integer segmentLength) {
        this.segmentLength = segmentLength;
        return this;
    }

    /**
     * Gets or sets the segment length.
     * 
     * @return segmentLength
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SEGMENT_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getSegmentLength() {
        return segmentLength;
    }

    @JsonProperty(JSON_PROPERTY_SEGMENT_LENGTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSegmentLength(@org.eclipse.jdt.annotation.NonNull Integer segmentLength) {
        this.segmentLength = segmentLength;
    }

    public TranscodingProfile breakOnNonKeyFrames(@org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames) {
        this.breakOnNonKeyFrames = breakOnNonKeyFrames;
        return this;
    }

    /**
     * Gets or sets a value indicating whether breaking the video stream on non-keyframes is supported.
     * 
     * @return breakOnNonKeyFrames
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BREAK_ON_NON_KEY_FRAMES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getBreakOnNonKeyFrames() {
        return breakOnNonKeyFrames;
    }

    @JsonProperty(JSON_PROPERTY_BREAK_ON_NON_KEY_FRAMES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBreakOnNonKeyFrames(@org.eclipse.jdt.annotation.NonNull Boolean breakOnNonKeyFrames) {
        this.breakOnNonKeyFrames = breakOnNonKeyFrames;
    }

    public TranscodingProfile conditions(@org.eclipse.jdt.annotation.NonNull List<ProfileCondition> conditions) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONDITIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ProfileCondition> getConditions() {
        return conditions;
    }

    @JsonProperty(JSON_PROPERTY_CONDITIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setConditions(@org.eclipse.jdt.annotation.NonNull List<ProfileCondition> conditions) {
        this.conditions = conditions;
    }

    public TranscodingProfile enableAudioVbrEncoding(
            @org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) {
        this.enableAudioVbrEncoding = enableAudioVbrEncoding;
        return this;
    }

    /**
     * Gets or sets a value indicating whether variable bitrate encoding is supported.
     * 
     * @return enableAudioVbrEncoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_AUDIO_VBR_ENCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getEnableAudioVbrEncoding() {
        return enableAudioVbrEncoding;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_AUDIO_VBR_ENCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAudioVbrEncoding(@org.eclipse.jdt.annotation.NonNull Boolean enableAudioVbrEncoding) {
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
}
