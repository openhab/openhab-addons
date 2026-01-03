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
 * Class holding information on a running transcode.
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
    @org.eclipse.jdt.annotation.Nullable
    private String audioCodec;

    public static final String JSON_PROPERTY_VIDEO_CODEC = "VideoCodec";
    @org.eclipse.jdt.annotation.Nullable
    private String videoCodec;

    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.Nullable
    private String container;

    public static final String JSON_PROPERTY_IS_VIDEO_DIRECT = "IsVideoDirect";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isVideoDirect;

    public static final String JSON_PROPERTY_IS_AUDIO_DIRECT = "IsAudioDirect";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isAudioDirect;

    public static final String JSON_PROPERTY_BITRATE = "Bitrate";
    @org.eclipse.jdt.annotation.Nullable
    private Integer bitrate;

    public static final String JSON_PROPERTY_FRAMERATE = "Framerate";
    @org.eclipse.jdt.annotation.Nullable
    private Float framerate;

    public static final String JSON_PROPERTY_COMPLETION_PERCENTAGE = "CompletionPercentage";
    @org.eclipse.jdt.annotation.Nullable
    private Double completionPercentage;

    public static final String JSON_PROPERTY_WIDTH = "Width";
    @org.eclipse.jdt.annotation.Nullable
    private Integer width;

    public static final String JSON_PROPERTY_HEIGHT = "Height";
    @org.eclipse.jdt.annotation.Nullable
    private Integer height;

    public static final String JSON_PROPERTY_AUDIO_CHANNELS = "AudioChannels";
    @org.eclipse.jdt.annotation.Nullable
    private Integer audioChannels;

    public static final String JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE = "HardwareAccelerationType";
    @org.eclipse.jdt.annotation.Nullable
    private HardwareAccelerationType hardwareAccelerationType;

    public static final String JSON_PROPERTY_TRANSCODE_REASONS = "TranscodeReasons";
    @org.eclipse.jdt.annotation.Nullable
    private List<TranscodeReason> transcodeReasons = new ArrayList<>();

    public TranscodingInfo() {
    }

    public TranscodingInfo audioCodec(@org.eclipse.jdt.annotation.Nullable String audioCodec) {
        this.audioCodec = audioCodec;
        return this;
    }

    /**
     * Gets or sets the thread count used for encoding.
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

    public TranscodingInfo videoCodec(@org.eclipse.jdt.annotation.Nullable String videoCodec) {
        this.videoCodec = videoCodec;
        return this;
    }

    /**
     * Gets or sets the thread count used for encoding.
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

    public TranscodingInfo container(@org.eclipse.jdt.annotation.Nullable String container) {
        this.container = container;
        return this;
    }

    /**
     * Gets or sets the thread count used for encoding.
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

    public TranscodingInfo isVideoDirect(@org.eclipse.jdt.annotation.Nullable Boolean isVideoDirect) {
        this.isVideoDirect = isVideoDirect;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the video is passed through.
     * 
     * @return isVideoDirect
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_VIDEO_DIRECT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsVideoDirect() {
        return isVideoDirect;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_VIDEO_DIRECT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsVideoDirect(@org.eclipse.jdt.annotation.Nullable Boolean isVideoDirect) {
        this.isVideoDirect = isVideoDirect;
    }

    public TranscodingInfo isAudioDirect(@org.eclipse.jdt.annotation.Nullable Boolean isAudioDirect) {
        this.isAudioDirect = isAudioDirect;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the audio is passed through.
     * 
     * @return isAudioDirect
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_AUDIO_DIRECT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsAudioDirect() {
        return isAudioDirect;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_AUDIO_DIRECT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsAudioDirect(@org.eclipse.jdt.annotation.Nullable Boolean isAudioDirect) {
        this.isAudioDirect = isAudioDirect;
    }

    public TranscodingInfo bitrate(@org.eclipse.jdt.annotation.Nullable Integer bitrate) {
        this.bitrate = bitrate;
        return this;
    }

    /**
     * Gets or sets the bitrate.
     * 
     * @return bitrate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BITRATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getBitrate() {
        return bitrate;
    }

    @JsonProperty(value = JSON_PROPERTY_BITRATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBitrate(@org.eclipse.jdt.annotation.Nullable Integer bitrate) {
        this.bitrate = bitrate;
    }

    public TranscodingInfo framerate(@org.eclipse.jdt.annotation.Nullable Float framerate) {
        this.framerate = framerate;
        return this;
    }

    /**
     * Gets or sets the framerate.
     * 
     * @return framerate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_FRAMERATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Float getFramerate() {
        return framerate;
    }

    @JsonProperty(value = JSON_PROPERTY_FRAMERATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFramerate(@org.eclipse.jdt.annotation.Nullable Float framerate) {
        this.framerate = framerate;
    }

    public TranscodingInfo completionPercentage(@org.eclipse.jdt.annotation.Nullable Double completionPercentage) {
        this.completionPercentage = completionPercentage;
        return this;
    }

    /**
     * Gets or sets the completion percentage.
     * 
     * @return completionPercentage
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COMPLETION_PERCENTAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getCompletionPercentage() {
        return completionPercentage;
    }

    @JsonProperty(value = JSON_PROPERTY_COMPLETION_PERCENTAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCompletionPercentage(@org.eclipse.jdt.annotation.Nullable Double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public TranscodingInfo width(@org.eclipse.jdt.annotation.Nullable Integer width) {
        this.width = width;
        return this;
    }

    /**
     * Gets or sets the video width.
     * 
     * @return width
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getWidth() {
        return width;
    }

    @JsonProperty(value = JSON_PROPERTY_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setWidth(@org.eclipse.jdt.annotation.Nullable Integer width) {
        this.width = width;
    }

    public TranscodingInfo height(@org.eclipse.jdt.annotation.Nullable Integer height) {
        this.height = height;
        return this;
    }

    /**
     * Gets or sets the video height.
     * 
     * @return height
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_HEIGHT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getHeight() {
        return height;
    }

    @JsonProperty(value = JSON_PROPERTY_HEIGHT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHeight(@org.eclipse.jdt.annotation.Nullable Integer height) {
        this.height = height;
    }

    public TranscodingInfo audioChannels(@org.eclipse.jdt.annotation.Nullable Integer audioChannels) {
        this.audioChannels = audioChannels;
        return this;
    }

    /**
     * Gets or sets the audio channels.
     * 
     * @return audioChannels
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_AUDIO_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAudioChannels() {
        return audioChannels;
    }

    @JsonProperty(value = JSON_PROPERTY_AUDIO_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAudioChannels(@org.eclipse.jdt.annotation.Nullable Integer audioChannels) {
        this.audioChannels = audioChannels;
    }

    public TranscodingInfo hardwareAccelerationType(
            @org.eclipse.jdt.annotation.Nullable HardwareAccelerationType hardwareAccelerationType) {
        this.hardwareAccelerationType = hardwareAccelerationType;
        return this;
    }

    /**
     * Gets or sets the hardware acceleration type.
     * 
     * @return hardwareAccelerationType
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public HardwareAccelerationType getHardwareAccelerationType() {
        return hardwareAccelerationType;
    }

    @JsonProperty(value = JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHardwareAccelerationType(
            @org.eclipse.jdt.annotation.Nullable HardwareAccelerationType hardwareAccelerationType) {
        this.hardwareAccelerationType = hardwareAccelerationType;
    }

    public TranscodingInfo transcodeReasons(
            @org.eclipse.jdt.annotation.Nullable List<TranscodeReason> transcodeReasons) {
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
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TRANSCODE_REASONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<TranscodeReason> getTranscodeReasons() {
        return transcodeReasons;
    }

    @JsonProperty(value = JSON_PROPERTY_TRANSCODE_REASONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodeReasons(@org.eclipse.jdt.annotation.Nullable List<TranscodeReason> transcodeReasons) {
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

        // add `AudioCodec` to the URL query string
        if (getAudioCodec() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAudioCodec%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAudioCodec()))));
        }

        // add `VideoCodec` to the URL query string
        if (getVideoCodec() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVideoCodec%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideoCodec()))));
        }

        // add `Container` to the URL query string
        if (getContainer() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContainer()))));
        }

        // add `IsVideoDirect` to the URL query string
        if (getIsVideoDirect() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsVideoDirect%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsVideoDirect()))));
        }

        // add `IsAudioDirect` to the URL query string
        if (getIsAudioDirect() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsAudioDirect%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsAudioDirect()))));
        }

        // add `Bitrate` to the URL query string
        if (getBitrate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBitrate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBitrate()))));
        }

        // add `Framerate` to the URL query string
        if (getFramerate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sFramerate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFramerate()))));
        }

        // add `CompletionPercentage` to the URL query string
        if (getCompletionPercentage() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCompletionPercentage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCompletionPercentage()))));
        }

        // add `Width` to the URL query string
        if (getWidth() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sWidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getWidth()))));
        }

        // add `Height` to the URL query string
        if (getHeight() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sHeight%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHeight()))));
        }

        // add `AudioChannels` to the URL query string
        if (getAudioChannels() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAudioChannels%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAudioChannels()))));
        }

        // add `HardwareAccelerationType` to the URL query string
        if (getHardwareAccelerationType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sHardwareAccelerationType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHardwareAccelerationType()))));
        }

        // add `TranscodeReasons` to the URL query string
        if (getTranscodeReasons() != null) {
            for (int i = 0; i < getTranscodeReasons().size(); i++) {
                if (getTranscodeReasons().get(i) != null) {
                    joiner.add(String.format(java.util.Locale.ROOT, "%sTranscodeReasons%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                            containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getTranscodeReasons().get(i)))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private TranscodingInfo instance;

        public Builder() {
            this(new TranscodingInfo());
        }

        protected Builder(TranscodingInfo instance) {
            this.instance = instance;
        }

        public TranscodingInfo.Builder audioCodec(String audioCodec) {
            this.instance.audioCodec = audioCodec;
            return this;
        }

        public TranscodingInfo.Builder videoCodec(String videoCodec) {
            this.instance.videoCodec = videoCodec;
            return this;
        }

        public TranscodingInfo.Builder container(String container) {
            this.instance.container = container;
            return this;
        }

        public TranscodingInfo.Builder isVideoDirect(Boolean isVideoDirect) {
            this.instance.isVideoDirect = isVideoDirect;
            return this;
        }

        public TranscodingInfo.Builder isAudioDirect(Boolean isAudioDirect) {
            this.instance.isAudioDirect = isAudioDirect;
            return this;
        }

        public TranscodingInfo.Builder bitrate(Integer bitrate) {
            this.instance.bitrate = bitrate;
            return this;
        }

        public TranscodingInfo.Builder framerate(Float framerate) {
            this.instance.framerate = framerate;
            return this;
        }

        public TranscodingInfo.Builder completionPercentage(Double completionPercentage) {
            this.instance.completionPercentage = completionPercentage;
            return this;
        }

        public TranscodingInfo.Builder width(Integer width) {
            this.instance.width = width;
            return this;
        }

        public TranscodingInfo.Builder height(Integer height) {
            this.instance.height = height;
            return this;
        }

        public TranscodingInfo.Builder audioChannels(Integer audioChannels) {
            this.instance.audioChannels = audioChannels;
            return this;
        }

        public TranscodingInfo.Builder hardwareAccelerationType(HardwareAccelerationType hardwareAccelerationType) {
            this.instance.hardwareAccelerationType = hardwareAccelerationType;
            return this;
        }

        public TranscodingInfo.Builder transcodeReasons(List<TranscodeReason> transcodeReasons) {
            this.instance.transcodeReasons = transcodeReasons;
            return this;
        }

        /**
         * returns a built TranscodingInfo instance.
         *
         * The builder is not reusable.
         */
        public TranscodingInfo build() {
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
    public static TranscodingInfo.Builder builder() {
        return new TranscodingInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public TranscodingInfo.Builder toBuilder() {
        return new TranscodingInfo.Builder().audioCodec(getAudioCodec()).videoCodec(getVideoCodec())
                .container(getContainer()).isVideoDirect(getIsVideoDirect()).isAudioDirect(getIsAudioDirect())
                .bitrate(getBitrate()).framerate(getFramerate()).completionPercentage(getCompletionPercentage())
                .width(getWidth()).height(getHeight()).audioChannels(getAudioChannels())
                .hardwareAccelerationType(getHardwareAccelerationType()).transcodeReasons(getTranscodeReasons());
    }
}
