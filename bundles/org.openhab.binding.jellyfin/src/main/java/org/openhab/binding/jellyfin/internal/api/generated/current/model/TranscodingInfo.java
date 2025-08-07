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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Class holding information on a runnning transcode.
 */
@JsonPropertyOrder({ TranscodingInfo.JSON_PROPERTY_AUDIO_CODEC, TranscodingInfo.JSON_PROPERTY_VIDEO_CODEC,
        TranscodingInfo.JSON_PROPERTY_CONTAINER, TranscodingInfo.JSON_PROPERTY_IS_VIDEO_DIRECT,
        TranscodingInfo.JSON_PROPERTY_IS_AUDIO_DIRECT, TranscodingInfo.JSON_PROPERTY_BITRATE,
        TranscodingInfo.JSON_PROPERTY_FRAMERATE, TranscodingInfo.JSON_PROPERTY_COMPLETION_PERCENTAGE,
        TranscodingInfo.JSON_PROPERTY_WIDTH, TranscodingInfo.JSON_PROPERTY_HEIGHT,
        TranscodingInfo.JSON_PROPERTY_AUDIO_CHANNELS, TranscodingInfo.JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE,
        TranscodingInfo.JSON_PROPERTY_TRANSCODE_REASONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TranscodingInfo {
    public static final String JSON_PROPERTY_AUDIO_CODEC = "AudioCodec";
    @org.eclipse.jdt.annotation.NonNull
    private String audioCodec;

    public static final String JSON_PROPERTY_VIDEO_CODEC = "VideoCodec";
    @org.eclipse.jdt.annotation.NonNull
    private String videoCodec;

    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public static final String JSON_PROPERTY_IS_VIDEO_DIRECT = "IsVideoDirect";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isVideoDirect;

    public static final String JSON_PROPERTY_IS_AUDIO_DIRECT = "IsAudioDirect";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isAudioDirect;

    public static final String JSON_PROPERTY_BITRATE = "Bitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer bitrate;

    public static final String JSON_PROPERTY_FRAMERATE = "Framerate";
    @org.eclipse.jdt.annotation.NonNull
    private Float framerate;

    public static final String JSON_PROPERTY_COMPLETION_PERCENTAGE = "CompletionPercentage";
    @org.eclipse.jdt.annotation.NonNull
    private Double completionPercentage;

    public static final String JSON_PROPERTY_WIDTH = "Width";
    @org.eclipse.jdt.annotation.NonNull
    private Integer width;

    public static final String JSON_PROPERTY_HEIGHT = "Height";
    @org.eclipse.jdt.annotation.NonNull
    private Integer height;

    public static final String JSON_PROPERTY_AUDIO_CHANNELS = "AudioChannels";
    @org.eclipse.jdt.annotation.NonNull
    private Integer audioChannels;

    public static final String JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE = "HardwareAccelerationType";
    @org.eclipse.jdt.annotation.NonNull
    private HardwareAccelerationType hardwareAccelerationType;

    /**
     * Gets or Sets transcodeReasons
     */
    public enum TranscodeReason {
        CONTAINER_NOT_SUPPORTED(TranscodeReason.valueOf("ContainerNotSupported")),

        VIDEO_CODEC_NOT_SUPPORTED(TranscodeReason.valueOf("VideoCodecNotSupported")),

        AUDIO_CODEC_NOT_SUPPORTED(TranscodeReason.valueOf("AudioCodecNotSupported")),

        SUBTITLE_CODEC_NOT_SUPPORTED(TranscodeReason.valueOf("SubtitleCodecNotSupported")),

        AUDIO_IS_EXTERNAL(TranscodeReason.valueOf("AudioIsExternal")),

        SECONDARY_AUDIO_NOT_SUPPORTED(TranscodeReason.valueOf("SecondaryAudioNotSupported")),

        VIDEO_PROFILE_NOT_SUPPORTED(TranscodeReason.valueOf("VideoProfileNotSupported")),

        VIDEO_LEVEL_NOT_SUPPORTED(TranscodeReason.valueOf("VideoLevelNotSupported")),

        VIDEO_RESOLUTION_NOT_SUPPORTED(TranscodeReason.valueOf("VideoResolutionNotSupported")),

        VIDEO_BIT_DEPTH_NOT_SUPPORTED(TranscodeReason.valueOf("VideoBitDepthNotSupported")),

        VIDEO_FRAMERATE_NOT_SUPPORTED(TranscodeReason.valueOf("VideoFramerateNotSupported")),

        REF_FRAMES_NOT_SUPPORTED(TranscodeReason.valueOf("RefFramesNotSupported")),

        ANAMORPHIC_VIDEO_NOT_SUPPORTED(TranscodeReason.valueOf("AnamorphicVideoNotSupported")),

        INTERLACED_VIDEO_NOT_SUPPORTED(TranscodeReason.valueOf("InterlacedVideoNotSupported")),

        AUDIO_CHANNELS_NOT_SUPPORTED(TranscodeReason.valueOf("AudioChannelsNotSupported")),

        AUDIO_PROFILE_NOT_SUPPORTED(TranscodeReason.valueOf("AudioProfileNotSupported")),

        AUDIO_SAMPLE_RATE_NOT_SUPPORTED(TranscodeReason.valueOf("AudioSampleRateNotSupported")),

        AUDIO_BIT_DEPTH_NOT_SUPPORTED(TranscodeReason.valueOf("AudioBitDepthNotSupported")),

        CONTAINER_BITRATE_EXCEEDS_LIMIT(TranscodeReason.valueOf("ContainerBitrateExceedsLimit")),

        VIDEO_BITRATE_NOT_SUPPORTED(TranscodeReason.valueOf("VideoBitrateNotSupported")),

        AUDIO_BITRATE_NOT_SUPPORTED(TranscodeReason.valueOf("AudioBitrateNotSupported")),

        UNKNOWN_VIDEO_STREAM_INFO(TranscodeReason.valueOf("UnknownVideoStreamInfo")),

        UNKNOWN_AUDIO_STREAM_INFO(TranscodeReason.valueOf("UnknownAudioStreamInfo")),

        DIRECT_PLAY_ERROR(TranscodeReason.valueOf("DirectPlayError")),

        VIDEO_RANGE_TYPE_NOT_SUPPORTED(TranscodeReason.valueOf("VideoRangeTypeNotSupported")),

        VIDEO_CODEC_TAG_NOT_SUPPORTED(TranscodeReason.valueOf("VideoCodecTagNotSupported"));

        private TranscodeReason value;

        TranscodeReason(TranscodeReason value) {
            this.value = value;
        }

        @JsonValue
        public TranscodeReason getValue() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static TranscodeReason fromValue(TranscodeReason value) {
            for (TranscodeReason b : TranscodeReason.values()) {
                if (b.value.equals(value)) {
                    return b;
                }
            }
            throw new IllegalArgumentException("Unexpected value '" + value + "'");
        }
    }

    public static final String JSON_PROPERTY_TRANSCODE_REASONS = "TranscodeReasons";
    @org.eclipse.jdt.annotation.NonNull
    private TranscodeReasonsEnum transcodeReasons = new ArrayList<>();

    public TranscodingInfo() {
    }

    public TranscodingInfo audioCodec(@org.eclipse.jdt.annotation.NonNull String audioCodec) {
        this.audioCodec = audioCodec;
        return this;
    }

    /**
     * Gets or sets the thread count used for encoding.
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

    public TranscodingInfo videoCodec(@org.eclipse.jdt.annotation.NonNull String videoCodec) {
        this.videoCodec = videoCodec;
        return this;
    }

    /**
     * Gets or sets the thread count used for encoding.
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

    public TranscodingInfo container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Gets or sets the thread count used for encoding.
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

    public TranscodingInfo isVideoDirect(@org.eclipse.jdt.annotation.NonNull Boolean isVideoDirect) {
        this.isVideoDirect = isVideoDirect;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the video is passed through.
     * 
     * @return isVideoDirect
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_VIDEO_DIRECT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsVideoDirect() {
        return isVideoDirect;
    }

    @JsonProperty(JSON_PROPERTY_IS_VIDEO_DIRECT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsVideoDirect(@org.eclipse.jdt.annotation.NonNull Boolean isVideoDirect) {
        this.isVideoDirect = isVideoDirect;
    }

    public TranscodingInfo isAudioDirect(@org.eclipse.jdt.annotation.NonNull Boolean isAudioDirect) {
        this.isAudioDirect = isAudioDirect;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the audio is passed through.
     * 
     * @return isAudioDirect
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_AUDIO_DIRECT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsAudioDirect() {
        return isAudioDirect;
    }

    @JsonProperty(JSON_PROPERTY_IS_AUDIO_DIRECT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsAudioDirect(@org.eclipse.jdt.annotation.NonNull Boolean isAudioDirect) {
        this.isAudioDirect = isAudioDirect;
    }

    public TranscodingInfo bitrate(@org.eclipse.jdt.annotation.NonNull Integer bitrate) {
        this.bitrate = bitrate;
        return this;
    }

    /**
     * Gets or sets the bitrate.
     * 
     * @return bitrate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getBitrate() {
        return bitrate;
    }

    @JsonProperty(JSON_PROPERTY_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBitrate(@org.eclipse.jdt.annotation.NonNull Integer bitrate) {
        this.bitrate = bitrate;
    }

    public TranscodingInfo framerate(@org.eclipse.jdt.annotation.NonNull Float framerate) {
        this.framerate = framerate;
        return this;
    }

    /**
     * Gets or sets the framerate.
     * 
     * @return framerate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FRAMERATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Float getFramerate() {
        return framerate;
    }

    @JsonProperty(JSON_PROPERTY_FRAMERATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFramerate(@org.eclipse.jdt.annotation.NonNull Float framerate) {
        this.framerate = framerate;
    }

    public TranscodingInfo completionPercentage(@org.eclipse.jdt.annotation.NonNull Double completionPercentage) {
        this.completionPercentage = completionPercentage;
        return this;
    }

    /**
     * Gets or sets the completion percentage.
     * 
     * @return completionPercentage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COMPLETION_PERCENTAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Double getCompletionPercentage() {
        return completionPercentage;
    }

    @JsonProperty(JSON_PROPERTY_COMPLETION_PERCENTAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCompletionPercentage(@org.eclipse.jdt.annotation.NonNull Double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public TranscodingInfo width(@org.eclipse.jdt.annotation.NonNull Integer width) {
        this.width = width;
        return this;
    }

    /**
     * Gets or sets the video width.
     * 
     * @return width
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getWidth() {
        return width;
    }

    @JsonProperty(JSON_PROPERTY_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWidth(@org.eclipse.jdt.annotation.NonNull Integer width) {
        this.width = width;
    }

    public TranscodingInfo height(@org.eclipse.jdt.annotation.NonNull Integer height) {
        this.height = height;
        return this;
    }

    /**
     * Gets or sets the video height.
     * 
     * @return height
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getHeight() {
        return height;
    }

    @JsonProperty(JSON_PROPERTY_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHeight(@org.eclipse.jdt.annotation.NonNull Integer height) {
        this.height = height;
    }

    public TranscodingInfo audioChannels(@org.eclipse.jdt.annotation.NonNull Integer audioChannels) {
        this.audioChannels = audioChannels;
        return this;
    }

    /**
     * Gets or sets the audio channels.
     * 
     * @return audioChannels
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_AUDIO_CHANNELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getAudioChannels() {
        return audioChannels;
    }

    @JsonProperty(JSON_PROPERTY_AUDIO_CHANNELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAudioChannels(@org.eclipse.jdt.annotation.NonNull Integer audioChannels) {
        this.audioChannels = audioChannels;
    }

    public TranscodingInfo hardwareAccelerationType(
            @org.eclipse.jdt.annotation.NonNull HardwareAccelerationType hardwareAccelerationType) {
        this.hardwareAccelerationType = hardwareAccelerationType;
        return this;
    }

    /**
     * Gets or sets the hardware acceleration type.
     * 
     * @return hardwareAccelerationType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public HardwareAccelerationType getHardwareAccelerationType() {
        return hardwareAccelerationType;
    }

    @JsonProperty(JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHardwareAccelerationType(
            @org.eclipse.jdt.annotation.NonNull HardwareAccelerationType hardwareAccelerationType) {
        this.hardwareAccelerationType = hardwareAccelerationType;
    }

    public TranscodingInfo transcodeReasons(@org.eclipse.jdt.annotation.NonNull TranscodeReasonsEnum transcodeReasons) {
        this.transcodeReasons = transcodeReasons;
        return this;
    }

    public TranscodingInfo addTranscodeReasonsItem(TranscodeReason transcodeReasonsItem) {
        if (this.transcodeReasons == null) {
            this.transcodeReasons = new ArrayList<>();
        }
        this.transcodeReasons.add(transcodeReasonsItem);
        return this;
    }

    /**
     * Gets or sets the transcode reasons.
     * 
     * @return transcodeReasons
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRANSCODE_REASONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public TranscodeReasonsEnum getTranscodeReasons() {
        return transcodeReasons;
    }

    @JsonProperty(JSON_PROPERTY_TRANSCODE_REASONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodeReasons(@org.eclipse.jdt.annotation.NonNull TranscodeReasonsEnum transcodeReasons) {
        this.transcodeReasons = transcodeReasons;
    }

    /**
     * Return true if this TranscodingInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TranscodingInfo transcodingInfo = (TranscodingInfo) o;
        return Objects.equals(this.audioCodec, transcodingInfo.audioCodec)
                && Objects.equals(this.videoCodec, transcodingInfo.videoCodec)
                && Objects.equals(this.container, transcodingInfo.container)
                && Objects.equals(this.isVideoDirect, transcodingInfo.isVideoDirect)
                && Objects.equals(this.isAudioDirect, transcodingInfo.isAudioDirect)
                && Objects.equals(this.bitrate, transcodingInfo.bitrate)
                && Objects.equals(this.framerate, transcodingInfo.framerate)
                && Objects.equals(this.completionPercentage, transcodingInfo.completionPercentage)
                && Objects.equals(this.width, transcodingInfo.width)
                && Objects.equals(this.height, transcodingInfo.height)
                && Objects.equals(this.audioChannels, transcodingInfo.audioChannels)
                && Objects.equals(this.hardwareAccelerationType, transcodingInfo.hardwareAccelerationType)
                && Objects.equals(this.transcodeReasons, transcodingInfo.transcodeReasons);
    }

    @Override
    public int hashCode() {
        return Objects.hash(audioCodec, videoCodec, container, isVideoDirect, isAudioDirect, bitrate, framerate,
                completionPercentage, width, height, audioChannels, hardwareAccelerationType, transcodeReasons);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TranscodingInfo {\n");
        sb.append("    audioCodec: ").append(toIndentedString(audioCodec)).append("\n");
        sb.append("    videoCodec: ").append(toIndentedString(videoCodec)).append("\n");
        sb.append("    container: ").append(toIndentedString(container)).append("\n");
        sb.append("    isVideoDirect: ").append(toIndentedString(isVideoDirect)).append("\n");
        sb.append("    isAudioDirect: ").append(toIndentedString(isAudioDirect)).append("\n");
        sb.append("    bitrate: ").append(toIndentedString(bitrate)).append("\n");
        sb.append("    framerate: ").append(toIndentedString(framerate)).append("\n");
        sb.append("    completionPercentage: ").append(toIndentedString(completionPercentage)).append("\n");
        sb.append("    width: ").append(toIndentedString(width)).append("\n");
        sb.append("    height: ").append(toIndentedString(height)).append("\n");
        sb.append("    audioChannels: ").append(toIndentedString(audioChannels)).append("\n");
        sb.append("    hardwareAccelerationType: ").append(toIndentedString(hardwareAccelerationType)).append("\n");
        sb.append("    transcodeReasons: ").append(toIndentedString(transcodeReasons)).append("\n");
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
