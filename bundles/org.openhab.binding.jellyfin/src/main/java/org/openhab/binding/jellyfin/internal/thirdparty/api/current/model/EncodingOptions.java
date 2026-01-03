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
 * Class EncodingOptions.
 */
@JsonPropertyOrder({ EncodingOptions.JSON_PROPERTY_ENCODING_THREAD_COUNT,
        EncodingOptions.JSON_PROPERTY_TRANSCODING_TEMP_PATH, EncodingOptions.JSON_PROPERTY_FALLBACK_FONT_PATH,
        EncodingOptions.JSON_PROPERTY_ENABLE_FALLBACK_FONT, EncodingOptions.JSON_PROPERTY_ENABLE_AUDIO_VBR,
        EncodingOptions.JSON_PROPERTY_DOWN_MIX_AUDIO_BOOST, EncodingOptions.JSON_PROPERTY_DOWN_MIX_STEREO_ALGORITHM,
        EncodingOptions.JSON_PROPERTY_MAX_MUXING_QUEUE_SIZE, EncodingOptions.JSON_PROPERTY_ENABLE_THROTTLING,
        EncodingOptions.JSON_PROPERTY_THROTTLE_DELAY_SECONDS, EncodingOptions.JSON_PROPERTY_ENABLE_SEGMENT_DELETION,
        EncodingOptions.JSON_PROPERTY_SEGMENT_KEEP_SECONDS, EncodingOptions.JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE,
        EncodingOptions.JSON_PROPERTY_ENCODER_APP_PATH, EncodingOptions.JSON_PROPERTY_ENCODER_APP_PATH_DISPLAY,
        EncodingOptions.JSON_PROPERTY_VAAPI_DEVICE, EncodingOptions.JSON_PROPERTY_QSV_DEVICE,
        EncodingOptions.JSON_PROPERTY_ENABLE_TONEMAPPING, EncodingOptions.JSON_PROPERTY_ENABLE_VPP_TONEMAPPING,
        EncodingOptions.JSON_PROPERTY_ENABLE_VIDEO_TOOLBOX_TONEMAPPING,
        EncodingOptions.JSON_PROPERTY_TONEMAPPING_ALGORITHM, EncodingOptions.JSON_PROPERTY_TONEMAPPING_MODE,
        EncodingOptions.JSON_PROPERTY_TONEMAPPING_RANGE, EncodingOptions.JSON_PROPERTY_TONEMAPPING_DESAT,
        EncodingOptions.JSON_PROPERTY_TONEMAPPING_PEAK, EncodingOptions.JSON_PROPERTY_TONEMAPPING_PARAM,
        EncodingOptions.JSON_PROPERTY_VPP_TONEMAPPING_BRIGHTNESS,
        EncodingOptions.JSON_PROPERTY_VPP_TONEMAPPING_CONTRAST, EncodingOptions.JSON_PROPERTY_H264_CRF,
        EncodingOptions.JSON_PROPERTY_H265_CRF, EncodingOptions.JSON_PROPERTY_ENCODER_PRESET,
        EncodingOptions.JSON_PROPERTY_DEINTERLACE_DOUBLE_RATE, EncodingOptions.JSON_PROPERTY_DEINTERLACE_METHOD,
        EncodingOptions.JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC,
        EncodingOptions.JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_VP9,
        EncodingOptions.JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC_REXT,
        EncodingOptions.JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH12_HEVC_REXT,
        EncodingOptions.JSON_PROPERTY_ENABLE_ENHANCED_NVDEC_DECODER,
        EncodingOptions.JSON_PROPERTY_PREFER_SYSTEM_NATIVE_HW_DECODER,
        EncodingOptions.JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_H264_HW_ENCODER,
        EncodingOptions.JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_HEVC_HW_ENCODER,
        EncodingOptions.JSON_PROPERTY_ENABLE_HARDWARE_ENCODING, EncodingOptions.JSON_PROPERTY_ALLOW_HEVC_ENCODING,
        EncodingOptions.JSON_PROPERTY_ALLOW_AV1_ENCODING, EncodingOptions.JSON_PROPERTY_ENABLE_SUBTITLE_EXTRACTION,
        EncodingOptions.JSON_PROPERTY_HARDWARE_DECODING_CODECS,
        EncodingOptions.JSON_PROPERTY_ALLOW_ON_DEMAND_METADATA_BASED_KEYFRAME_EXTRACTION_FOR_EXTENSIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class EncodingOptions {
    public static final String JSON_PROPERTY_ENCODING_THREAD_COUNT = "EncodingThreadCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer encodingThreadCount;

    public static final String JSON_PROPERTY_TRANSCODING_TEMP_PATH = "TranscodingTempPath";
    @org.eclipse.jdt.annotation.Nullable
    private String transcodingTempPath;

    public static final String JSON_PROPERTY_FALLBACK_FONT_PATH = "FallbackFontPath";
    @org.eclipse.jdt.annotation.Nullable
    private String fallbackFontPath;

    public static final String JSON_PROPERTY_ENABLE_FALLBACK_FONT = "EnableFallbackFont";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableFallbackFont;

    public static final String JSON_PROPERTY_ENABLE_AUDIO_VBR = "EnableAudioVbr";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableAudioVbr;

    public static final String JSON_PROPERTY_DOWN_MIX_AUDIO_BOOST = "DownMixAudioBoost";
    @org.eclipse.jdt.annotation.Nullable
    private Double downMixAudioBoost;

    public static final String JSON_PROPERTY_DOWN_MIX_STEREO_ALGORITHM = "DownMixStereoAlgorithm";
    @org.eclipse.jdt.annotation.Nullable
    private DownMixStereoAlgorithms downMixStereoAlgorithm;

    public static final String JSON_PROPERTY_MAX_MUXING_QUEUE_SIZE = "MaxMuxingQueueSize";
    @org.eclipse.jdt.annotation.Nullable
    private Integer maxMuxingQueueSize;

    public static final String JSON_PROPERTY_ENABLE_THROTTLING = "EnableThrottling";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableThrottling;

    public static final String JSON_PROPERTY_THROTTLE_DELAY_SECONDS = "ThrottleDelaySeconds";
    @org.eclipse.jdt.annotation.Nullable
    private Integer throttleDelaySeconds;

    public static final String JSON_PROPERTY_ENABLE_SEGMENT_DELETION = "EnableSegmentDeletion";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableSegmentDeletion;

    public static final String JSON_PROPERTY_SEGMENT_KEEP_SECONDS = "SegmentKeepSeconds";
    @org.eclipse.jdt.annotation.Nullable
    private Integer segmentKeepSeconds;

    public static final String JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE = "HardwareAccelerationType";
    @org.eclipse.jdt.annotation.Nullable
    private HardwareAccelerationType hardwareAccelerationType;

    public static final String JSON_PROPERTY_ENCODER_APP_PATH = "EncoderAppPath";
    @org.eclipse.jdt.annotation.Nullable
    private String encoderAppPath;

    public static final String JSON_PROPERTY_ENCODER_APP_PATH_DISPLAY = "EncoderAppPathDisplay";
    @org.eclipse.jdt.annotation.Nullable
    private String encoderAppPathDisplay;

    public static final String JSON_PROPERTY_VAAPI_DEVICE = "VaapiDevice";
    @org.eclipse.jdt.annotation.Nullable
    private String vaapiDevice;

    public static final String JSON_PROPERTY_QSV_DEVICE = "QsvDevice";
    @org.eclipse.jdt.annotation.Nullable
    private String qsvDevice;

    public static final String JSON_PROPERTY_ENABLE_TONEMAPPING = "EnableTonemapping";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableTonemapping;

    public static final String JSON_PROPERTY_ENABLE_VPP_TONEMAPPING = "EnableVppTonemapping";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableVppTonemapping;

    public static final String JSON_PROPERTY_ENABLE_VIDEO_TOOLBOX_TONEMAPPING = "EnableVideoToolboxTonemapping";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableVideoToolboxTonemapping;

    public static final String JSON_PROPERTY_TONEMAPPING_ALGORITHM = "TonemappingAlgorithm";
    @org.eclipse.jdt.annotation.Nullable
    private TonemappingAlgorithm tonemappingAlgorithm;

    public static final String JSON_PROPERTY_TONEMAPPING_MODE = "TonemappingMode";
    @org.eclipse.jdt.annotation.Nullable
    private TonemappingMode tonemappingMode;

    public static final String JSON_PROPERTY_TONEMAPPING_RANGE = "TonemappingRange";
    @org.eclipse.jdt.annotation.Nullable
    private TonemappingRange tonemappingRange;

    public static final String JSON_PROPERTY_TONEMAPPING_DESAT = "TonemappingDesat";
    @org.eclipse.jdt.annotation.Nullable
    private Double tonemappingDesat;

    public static final String JSON_PROPERTY_TONEMAPPING_PEAK = "TonemappingPeak";
    @org.eclipse.jdt.annotation.Nullable
    private Double tonemappingPeak;

    public static final String JSON_PROPERTY_TONEMAPPING_PARAM = "TonemappingParam";
    @org.eclipse.jdt.annotation.Nullable
    private Double tonemappingParam;

    public static final String JSON_PROPERTY_VPP_TONEMAPPING_BRIGHTNESS = "VppTonemappingBrightness";
    @org.eclipse.jdt.annotation.Nullable
    private Double vppTonemappingBrightness;

    public static final String JSON_PROPERTY_VPP_TONEMAPPING_CONTRAST = "VppTonemappingContrast";
    @org.eclipse.jdt.annotation.Nullable
    private Double vppTonemappingContrast;

    public static final String JSON_PROPERTY_H264_CRF = "H264Crf";
    @org.eclipse.jdt.annotation.Nullable
    private Integer h264Crf;

    public static final String JSON_PROPERTY_H265_CRF = "H265Crf";
    @org.eclipse.jdt.annotation.Nullable
    private Integer h265Crf;

    public static final String JSON_PROPERTY_ENCODER_PRESET = "EncoderPreset";
    @org.eclipse.jdt.annotation.Nullable
    private EncoderPreset encoderPreset;

    public static final String JSON_PROPERTY_DEINTERLACE_DOUBLE_RATE = "DeinterlaceDoubleRate";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean deinterlaceDoubleRate;

    public static final String JSON_PROPERTY_DEINTERLACE_METHOD = "DeinterlaceMethod";
    @org.eclipse.jdt.annotation.Nullable
    private DeinterlaceMethod deinterlaceMethod;

    public static final String JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC = "EnableDecodingColorDepth10Hevc";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableDecodingColorDepth10Hevc;

    public static final String JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_VP9 = "EnableDecodingColorDepth10Vp9";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableDecodingColorDepth10Vp9;

    public static final String JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC_REXT = "EnableDecodingColorDepth10HevcRext";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableDecodingColorDepth10HevcRext;

    public static final String JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH12_HEVC_REXT = "EnableDecodingColorDepth12HevcRext";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableDecodingColorDepth12HevcRext;

    public static final String JSON_PROPERTY_ENABLE_ENHANCED_NVDEC_DECODER = "EnableEnhancedNvdecDecoder";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableEnhancedNvdecDecoder;

    public static final String JSON_PROPERTY_PREFER_SYSTEM_NATIVE_HW_DECODER = "PreferSystemNativeHwDecoder";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean preferSystemNativeHwDecoder;

    public static final String JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_H264_HW_ENCODER = "EnableIntelLowPowerH264HwEncoder";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableIntelLowPowerH264HwEncoder;

    public static final String JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_HEVC_HW_ENCODER = "EnableIntelLowPowerHevcHwEncoder";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableIntelLowPowerHevcHwEncoder;

    public static final String JSON_PROPERTY_ENABLE_HARDWARE_ENCODING = "EnableHardwareEncoding";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableHardwareEncoding;

    public static final String JSON_PROPERTY_ALLOW_HEVC_ENCODING = "AllowHevcEncoding";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean allowHevcEncoding;

    public static final String JSON_PROPERTY_ALLOW_AV1_ENCODING = "AllowAv1Encoding";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean allowAv1Encoding;

    public static final String JSON_PROPERTY_ENABLE_SUBTITLE_EXTRACTION = "EnableSubtitleExtraction";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean enableSubtitleExtraction;

    public static final String JSON_PROPERTY_HARDWARE_DECODING_CODECS = "HardwareDecodingCodecs";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> hardwareDecodingCodecs;

    public static final String JSON_PROPERTY_ALLOW_ON_DEMAND_METADATA_BASED_KEYFRAME_EXTRACTION_FOR_EXTENSIONS = "AllowOnDemandMetadataBasedKeyframeExtractionForExtensions";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> allowOnDemandMetadataBasedKeyframeExtractionForExtensions;

    public EncodingOptions() {
    }

    public EncodingOptions encodingThreadCount(@org.eclipse.jdt.annotation.Nullable Integer encodingThreadCount) {
        this.encodingThreadCount = encodingThreadCount;
        return this;
    }

    /**
     * Gets or sets the thread count used for encoding.
     * 
     * @return encodingThreadCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENCODING_THREAD_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getEncodingThreadCount() {
        return encodingThreadCount;
    }

    @JsonProperty(value = JSON_PROPERTY_ENCODING_THREAD_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncodingThreadCount(@org.eclipse.jdt.annotation.Nullable Integer encodingThreadCount) {
        this.encodingThreadCount = encodingThreadCount;
    }

    public EncodingOptions transcodingTempPath(@org.eclipse.jdt.annotation.Nullable String transcodingTempPath) {
        this.transcodingTempPath = transcodingTempPath;
        return this;
    }

    /**
     * Gets or sets the temporary transcoding path.
     * 
     * @return transcodingTempPath
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TRANSCODING_TEMP_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTranscodingTempPath() {
        return transcodingTempPath;
    }

    @JsonProperty(value = JSON_PROPERTY_TRANSCODING_TEMP_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingTempPath(@org.eclipse.jdt.annotation.Nullable String transcodingTempPath) {
        this.transcodingTempPath = transcodingTempPath;
    }

    public EncodingOptions fallbackFontPath(@org.eclipse.jdt.annotation.Nullable String fallbackFontPath) {
        this.fallbackFontPath = fallbackFontPath;
        return this;
    }

    /**
     * Gets or sets the path to the fallback font.
     * 
     * @return fallbackFontPath
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_FALLBACK_FONT_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getFallbackFontPath() {
        return fallbackFontPath;
    }

    @JsonProperty(value = JSON_PROPERTY_FALLBACK_FONT_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFallbackFontPath(@org.eclipse.jdt.annotation.Nullable String fallbackFontPath) {
        this.fallbackFontPath = fallbackFontPath;
    }

    public EncodingOptions enableFallbackFont(@org.eclipse.jdt.annotation.Nullable Boolean enableFallbackFont) {
        this.enableFallbackFont = enableFallbackFont;
        return this;
    }

    /**
     * Gets or sets a value indicating whether to use the fallback font.
     * 
     * @return enableFallbackFont
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_FALLBACK_FONT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableFallbackFont() {
        return enableFallbackFont;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_FALLBACK_FONT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableFallbackFont(@org.eclipse.jdt.annotation.Nullable Boolean enableFallbackFont) {
        this.enableFallbackFont = enableFallbackFont;
    }

    public EncodingOptions enableAudioVbr(@org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbr) {
        this.enableAudioVbr = enableAudioVbr;
        return this;
    }

    /**
     * Gets or sets a value indicating whether audio VBR is enabled.
     * 
     * @return enableAudioVbr
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUDIO_VBR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableAudioVbr() {
        return enableAudioVbr;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_AUDIO_VBR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAudioVbr(@org.eclipse.jdt.annotation.Nullable Boolean enableAudioVbr) {
        this.enableAudioVbr = enableAudioVbr;
    }

    public EncodingOptions downMixAudioBoost(@org.eclipse.jdt.annotation.Nullable Double downMixAudioBoost) {
        this.downMixAudioBoost = downMixAudioBoost;
        return this;
    }

    /**
     * Gets or sets the audio boost applied when downmixing audio.
     * 
     * @return downMixAudioBoost
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DOWN_MIX_AUDIO_BOOST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getDownMixAudioBoost() {
        return downMixAudioBoost;
    }

    @JsonProperty(value = JSON_PROPERTY_DOWN_MIX_AUDIO_BOOST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDownMixAudioBoost(@org.eclipse.jdt.annotation.Nullable Double downMixAudioBoost) {
        this.downMixAudioBoost = downMixAudioBoost;
    }

    public EncodingOptions downMixStereoAlgorithm(
            @org.eclipse.jdt.annotation.Nullable DownMixStereoAlgorithms downMixStereoAlgorithm) {
        this.downMixStereoAlgorithm = downMixStereoAlgorithm;
        return this;
    }

    /**
     * Gets or sets the algorithm used for downmixing audio to stereo.
     * 
     * @return downMixStereoAlgorithm
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DOWN_MIX_STEREO_ALGORITHM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DownMixStereoAlgorithms getDownMixStereoAlgorithm() {
        return downMixStereoAlgorithm;
    }

    @JsonProperty(value = JSON_PROPERTY_DOWN_MIX_STEREO_ALGORITHM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDownMixStereoAlgorithm(
            @org.eclipse.jdt.annotation.Nullable DownMixStereoAlgorithms downMixStereoAlgorithm) {
        this.downMixStereoAlgorithm = downMixStereoAlgorithm;
    }

    public EncodingOptions maxMuxingQueueSize(@org.eclipse.jdt.annotation.Nullable Integer maxMuxingQueueSize) {
        this.maxMuxingQueueSize = maxMuxingQueueSize;
        return this;
    }

    /**
     * Gets or sets the maximum size of the muxing queue.
     * 
     * @return maxMuxingQueueSize
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MAX_MUXING_QUEUE_SIZE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxMuxingQueueSize() {
        return maxMuxingQueueSize;
    }

    @JsonProperty(value = JSON_PROPERTY_MAX_MUXING_QUEUE_SIZE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxMuxingQueueSize(@org.eclipse.jdt.annotation.Nullable Integer maxMuxingQueueSize) {
        this.maxMuxingQueueSize = maxMuxingQueueSize;
    }

    public EncodingOptions enableThrottling(@org.eclipse.jdt.annotation.Nullable Boolean enableThrottling) {
        this.enableThrottling = enableThrottling;
        return this;
    }

    /**
     * Gets or sets a value indicating whether throttling is enabled.
     * 
     * @return enableThrottling
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_THROTTLING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableThrottling() {
        return enableThrottling;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_THROTTLING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableThrottling(@org.eclipse.jdt.annotation.Nullable Boolean enableThrottling) {
        this.enableThrottling = enableThrottling;
    }

    public EncodingOptions throttleDelaySeconds(@org.eclipse.jdt.annotation.Nullable Integer throttleDelaySeconds) {
        this.throttleDelaySeconds = throttleDelaySeconds;
        return this;
    }

    /**
     * Gets or sets the delay after which throttling happens.
     * 
     * @return throttleDelaySeconds
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_THROTTLE_DELAY_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getThrottleDelaySeconds() {
        return throttleDelaySeconds;
    }

    @JsonProperty(value = JSON_PROPERTY_THROTTLE_DELAY_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThrottleDelaySeconds(@org.eclipse.jdt.annotation.Nullable Integer throttleDelaySeconds) {
        this.throttleDelaySeconds = throttleDelaySeconds;
    }

    public EncodingOptions enableSegmentDeletion(@org.eclipse.jdt.annotation.Nullable Boolean enableSegmentDeletion) {
        this.enableSegmentDeletion = enableSegmentDeletion;
        return this;
    }

    /**
     * Gets or sets a value indicating whether segment deletion is enabled.
     * 
     * @return enableSegmentDeletion
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_SEGMENT_DELETION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableSegmentDeletion() {
        return enableSegmentDeletion;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_SEGMENT_DELETION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSegmentDeletion(@org.eclipse.jdt.annotation.Nullable Boolean enableSegmentDeletion) {
        this.enableSegmentDeletion = enableSegmentDeletion;
    }

    public EncodingOptions segmentKeepSeconds(@org.eclipse.jdt.annotation.Nullable Integer segmentKeepSeconds) {
        this.segmentKeepSeconds = segmentKeepSeconds;
        return this;
    }

    /**
     * Gets or sets seconds for which segments should be kept before being deleted.
     * 
     * @return segmentKeepSeconds
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SEGMENT_KEEP_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSegmentKeepSeconds() {
        return segmentKeepSeconds;
    }

    @JsonProperty(value = JSON_PROPERTY_SEGMENT_KEEP_SECONDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSegmentKeepSeconds(@org.eclipse.jdt.annotation.Nullable Integer segmentKeepSeconds) {
        this.segmentKeepSeconds = segmentKeepSeconds;
    }

    public EncodingOptions hardwareAccelerationType(
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

    public EncodingOptions encoderAppPath(@org.eclipse.jdt.annotation.Nullable String encoderAppPath) {
        this.encoderAppPath = encoderAppPath;
        return this;
    }

    /**
     * Gets or sets the FFmpeg path as set by the user via the UI.
     * 
     * @return encoderAppPath
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENCODER_APP_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEncoderAppPath() {
        return encoderAppPath;
    }

    @JsonProperty(value = JSON_PROPERTY_ENCODER_APP_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncoderAppPath(@org.eclipse.jdt.annotation.Nullable String encoderAppPath) {
        this.encoderAppPath = encoderAppPath;
    }

    public EncodingOptions encoderAppPathDisplay(@org.eclipse.jdt.annotation.Nullable String encoderAppPathDisplay) {
        this.encoderAppPathDisplay = encoderAppPathDisplay;
        return this;
    }

    /**
     * Gets or sets the current FFmpeg path being used by the system and displayed on the transcode page.
     * 
     * @return encoderAppPathDisplay
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENCODER_APP_PATH_DISPLAY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEncoderAppPathDisplay() {
        return encoderAppPathDisplay;
    }

    @JsonProperty(value = JSON_PROPERTY_ENCODER_APP_PATH_DISPLAY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncoderAppPathDisplay(@org.eclipse.jdt.annotation.Nullable String encoderAppPathDisplay) {
        this.encoderAppPathDisplay = encoderAppPathDisplay;
    }

    public EncodingOptions vaapiDevice(@org.eclipse.jdt.annotation.Nullable String vaapiDevice) {
        this.vaapiDevice = vaapiDevice;
        return this;
    }

    /**
     * Gets or sets the VA-API device.
     * 
     * @return vaapiDevice
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VAAPI_DEVICE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVaapiDevice() {
        return vaapiDevice;
    }

    @JsonProperty(value = JSON_PROPERTY_VAAPI_DEVICE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVaapiDevice(@org.eclipse.jdt.annotation.Nullable String vaapiDevice) {
        this.vaapiDevice = vaapiDevice;
    }

    public EncodingOptions qsvDevice(@org.eclipse.jdt.annotation.Nullable String qsvDevice) {
        this.qsvDevice = qsvDevice;
        return this;
    }

    /**
     * Gets or sets the QSV device.
     * 
     * @return qsvDevice
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_QSV_DEVICE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getQsvDevice() {
        return qsvDevice;
    }

    @JsonProperty(value = JSON_PROPERTY_QSV_DEVICE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setQsvDevice(@org.eclipse.jdt.annotation.Nullable String qsvDevice) {
        this.qsvDevice = qsvDevice;
    }

    public EncodingOptions enableTonemapping(@org.eclipse.jdt.annotation.Nullable Boolean enableTonemapping) {
        this.enableTonemapping = enableTonemapping;
        return this;
    }

    /**
     * Gets or sets a value indicating whether tonemapping is enabled.
     * 
     * @return enableTonemapping
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_TONEMAPPING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableTonemapping() {
        return enableTonemapping;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_TONEMAPPING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableTonemapping(@org.eclipse.jdt.annotation.Nullable Boolean enableTonemapping) {
        this.enableTonemapping = enableTonemapping;
    }

    public EncodingOptions enableVppTonemapping(@org.eclipse.jdt.annotation.Nullable Boolean enableVppTonemapping) {
        this.enableVppTonemapping = enableVppTonemapping;
        return this;
    }

    /**
     * Gets or sets a value indicating whether VPP tonemapping is enabled.
     * 
     * @return enableVppTonemapping
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_VPP_TONEMAPPING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableVppTonemapping() {
        return enableVppTonemapping;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_VPP_TONEMAPPING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableVppTonemapping(@org.eclipse.jdt.annotation.Nullable Boolean enableVppTonemapping) {
        this.enableVppTonemapping = enableVppTonemapping;
    }

    public EncodingOptions enableVideoToolboxTonemapping(
            @org.eclipse.jdt.annotation.Nullable Boolean enableVideoToolboxTonemapping) {
        this.enableVideoToolboxTonemapping = enableVideoToolboxTonemapping;
        return this;
    }

    /**
     * Gets or sets a value indicating whether videotoolbox tonemapping is enabled.
     * 
     * @return enableVideoToolboxTonemapping
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_VIDEO_TOOLBOX_TONEMAPPING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableVideoToolboxTonemapping() {
        return enableVideoToolboxTonemapping;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_VIDEO_TOOLBOX_TONEMAPPING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableVideoToolboxTonemapping(
            @org.eclipse.jdt.annotation.Nullable Boolean enableVideoToolboxTonemapping) {
        this.enableVideoToolboxTonemapping = enableVideoToolboxTonemapping;
    }

    public EncodingOptions tonemappingAlgorithm(
            @org.eclipse.jdt.annotation.Nullable TonemappingAlgorithm tonemappingAlgorithm) {
        this.tonemappingAlgorithm = tonemappingAlgorithm;
        return this;
    }

    /**
     * Gets or sets the tone-mapping algorithm.
     * 
     * @return tonemappingAlgorithm
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_ALGORITHM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TonemappingAlgorithm getTonemappingAlgorithm() {
        return tonemappingAlgorithm;
    }

    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_ALGORITHM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingAlgorithm(
            @org.eclipse.jdt.annotation.Nullable TonemappingAlgorithm tonemappingAlgorithm) {
        this.tonemappingAlgorithm = tonemappingAlgorithm;
    }

    public EncodingOptions tonemappingMode(@org.eclipse.jdt.annotation.Nullable TonemappingMode tonemappingMode) {
        this.tonemappingMode = tonemappingMode;
        return this;
    }

    /**
     * Gets or sets the tone-mapping mode.
     * 
     * @return tonemappingMode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TonemappingMode getTonemappingMode() {
        return tonemappingMode;
    }

    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_MODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingMode(@org.eclipse.jdt.annotation.Nullable TonemappingMode tonemappingMode) {
        this.tonemappingMode = tonemappingMode;
    }

    public EncodingOptions tonemappingRange(@org.eclipse.jdt.annotation.Nullable TonemappingRange tonemappingRange) {
        this.tonemappingRange = tonemappingRange;
        return this;
    }

    /**
     * Gets or sets the tone-mapping range.
     * 
     * @return tonemappingRange
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_RANGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TonemappingRange getTonemappingRange() {
        return tonemappingRange;
    }

    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_RANGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingRange(@org.eclipse.jdt.annotation.Nullable TonemappingRange tonemappingRange) {
        this.tonemappingRange = tonemappingRange;
    }

    public EncodingOptions tonemappingDesat(@org.eclipse.jdt.annotation.Nullable Double tonemappingDesat) {
        this.tonemappingDesat = tonemappingDesat;
        return this;
    }

    /**
     * Gets or sets the tone-mapping desaturation.
     * 
     * @return tonemappingDesat
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_DESAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getTonemappingDesat() {
        return tonemappingDesat;
    }

    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_DESAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingDesat(@org.eclipse.jdt.annotation.Nullable Double tonemappingDesat) {
        this.tonemappingDesat = tonemappingDesat;
    }

    public EncodingOptions tonemappingPeak(@org.eclipse.jdt.annotation.Nullable Double tonemappingPeak) {
        this.tonemappingPeak = tonemappingPeak;
        return this;
    }

    /**
     * Gets or sets the tone-mapping peak.
     * 
     * @return tonemappingPeak
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_PEAK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getTonemappingPeak() {
        return tonemappingPeak;
    }

    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_PEAK, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingPeak(@org.eclipse.jdt.annotation.Nullable Double tonemappingPeak) {
        this.tonemappingPeak = tonemappingPeak;
    }

    public EncodingOptions tonemappingParam(@org.eclipse.jdt.annotation.Nullable Double tonemappingParam) {
        this.tonemappingParam = tonemappingParam;
        return this;
    }

    /**
     * Gets or sets the tone-mapping parameters.
     * 
     * @return tonemappingParam
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_PARAM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getTonemappingParam() {
        return tonemappingParam;
    }

    @JsonProperty(value = JSON_PROPERTY_TONEMAPPING_PARAM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingParam(@org.eclipse.jdt.annotation.Nullable Double tonemappingParam) {
        this.tonemappingParam = tonemappingParam;
    }

    public EncodingOptions vppTonemappingBrightness(
            @org.eclipse.jdt.annotation.Nullable Double vppTonemappingBrightness) {
        this.vppTonemappingBrightness = vppTonemappingBrightness;
        return this;
    }

    /**
     * Gets or sets the VPP tone-mapping brightness.
     * 
     * @return vppTonemappingBrightness
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VPP_TONEMAPPING_BRIGHTNESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getVppTonemappingBrightness() {
        return vppTonemappingBrightness;
    }

    @JsonProperty(value = JSON_PROPERTY_VPP_TONEMAPPING_BRIGHTNESS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVppTonemappingBrightness(@org.eclipse.jdt.annotation.Nullable Double vppTonemappingBrightness) {
        this.vppTonemappingBrightness = vppTonemappingBrightness;
    }

    public EncodingOptions vppTonemappingContrast(@org.eclipse.jdt.annotation.Nullable Double vppTonemappingContrast) {
        this.vppTonemappingContrast = vppTonemappingContrast;
        return this;
    }

    /**
     * Gets or sets the VPP tone-mapping contrast.
     * 
     * @return vppTonemappingContrast
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VPP_TONEMAPPING_CONTRAST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getVppTonemappingContrast() {
        return vppTonemappingContrast;
    }

    @JsonProperty(value = JSON_PROPERTY_VPP_TONEMAPPING_CONTRAST, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVppTonemappingContrast(@org.eclipse.jdt.annotation.Nullable Double vppTonemappingContrast) {
        this.vppTonemappingContrast = vppTonemappingContrast;
    }

    public EncodingOptions h264Crf(@org.eclipse.jdt.annotation.Nullable Integer h264Crf) {
        this.h264Crf = h264Crf;
        return this;
    }

    /**
     * Gets or sets the H264 CRF.
     * 
     * @return h264Crf
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_H264_CRF, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getH264Crf() {
        return h264Crf;
    }

    @JsonProperty(value = JSON_PROPERTY_H264_CRF, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setH264Crf(@org.eclipse.jdt.annotation.Nullable Integer h264Crf) {
        this.h264Crf = h264Crf;
    }

    public EncodingOptions h265Crf(@org.eclipse.jdt.annotation.Nullable Integer h265Crf) {
        this.h265Crf = h265Crf;
        return this;
    }

    /**
     * Gets or sets the H265 CRF.
     * 
     * @return h265Crf
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_H265_CRF, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getH265Crf() {
        return h265Crf;
    }

    @JsonProperty(value = JSON_PROPERTY_H265_CRF, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setH265Crf(@org.eclipse.jdt.annotation.Nullable Integer h265Crf) {
        this.h265Crf = h265Crf;
    }

    public EncodingOptions encoderPreset(@org.eclipse.jdt.annotation.Nullable EncoderPreset encoderPreset) {
        this.encoderPreset = encoderPreset;
        return this;
    }

    /**
     * Gets or sets the encoder preset.
     * 
     * @return encoderPreset
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENCODER_PRESET, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public EncoderPreset getEncoderPreset() {
        return encoderPreset;
    }

    @JsonProperty(value = JSON_PROPERTY_ENCODER_PRESET, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncoderPreset(@org.eclipse.jdt.annotation.Nullable EncoderPreset encoderPreset) {
        this.encoderPreset = encoderPreset;
    }

    public EncodingOptions deinterlaceDoubleRate(@org.eclipse.jdt.annotation.Nullable Boolean deinterlaceDoubleRate) {
        this.deinterlaceDoubleRate = deinterlaceDoubleRate;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the framerate is doubled when deinterlacing.
     * 
     * @return deinterlaceDoubleRate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DEINTERLACE_DOUBLE_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getDeinterlaceDoubleRate() {
        return deinterlaceDoubleRate;
    }

    @JsonProperty(value = JSON_PROPERTY_DEINTERLACE_DOUBLE_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeinterlaceDoubleRate(@org.eclipse.jdt.annotation.Nullable Boolean deinterlaceDoubleRate) {
        this.deinterlaceDoubleRate = deinterlaceDoubleRate;
    }

    public EncodingOptions deinterlaceMethod(@org.eclipse.jdt.annotation.Nullable DeinterlaceMethod deinterlaceMethod) {
        this.deinterlaceMethod = deinterlaceMethod;
        return this;
    }

    /**
     * Gets or sets the deinterlace method.
     * 
     * @return deinterlaceMethod
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DEINTERLACE_METHOD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DeinterlaceMethod getDeinterlaceMethod() {
        return deinterlaceMethod;
    }

    @JsonProperty(value = JSON_PROPERTY_DEINTERLACE_METHOD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeinterlaceMethod(@org.eclipse.jdt.annotation.Nullable DeinterlaceMethod deinterlaceMethod) {
        this.deinterlaceMethod = deinterlaceMethod;
    }

    public EncodingOptions enableDecodingColorDepth10Hevc(
            @org.eclipse.jdt.annotation.Nullable Boolean enableDecodingColorDepth10Hevc) {
        this.enableDecodingColorDepth10Hevc = enableDecodingColorDepth10Hevc;
        return this;
    }

    /**
     * Gets or sets a value indicating whether 10bit HEVC decoding is enabled.
     * 
     * @return enableDecodingColorDepth10Hevc
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableDecodingColorDepth10Hevc() {
        return enableDecodingColorDepth10Hevc;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableDecodingColorDepth10Hevc(
            @org.eclipse.jdt.annotation.Nullable Boolean enableDecodingColorDepth10Hevc) {
        this.enableDecodingColorDepth10Hevc = enableDecodingColorDepth10Hevc;
    }

    public EncodingOptions enableDecodingColorDepth10Vp9(
            @org.eclipse.jdt.annotation.Nullable Boolean enableDecodingColorDepth10Vp9) {
        this.enableDecodingColorDepth10Vp9 = enableDecodingColorDepth10Vp9;
        return this;
    }

    /**
     * Gets or sets a value indicating whether 10bit VP9 decoding is enabled.
     * 
     * @return enableDecodingColorDepth10Vp9
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_VP9, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableDecodingColorDepth10Vp9() {
        return enableDecodingColorDepth10Vp9;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_VP9, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableDecodingColorDepth10Vp9(
            @org.eclipse.jdt.annotation.Nullable Boolean enableDecodingColorDepth10Vp9) {
        this.enableDecodingColorDepth10Vp9 = enableDecodingColorDepth10Vp9;
    }

    public EncodingOptions enableDecodingColorDepth10HevcRext(
            @org.eclipse.jdt.annotation.Nullable Boolean enableDecodingColorDepth10HevcRext) {
        this.enableDecodingColorDepth10HevcRext = enableDecodingColorDepth10HevcRext;
        return this;
    }

    /**
     * Gets or sets a value indicating whether 8/10bit HEVC RExt decoding is enabled.
     * 
     * @return enableDecodingColorDepth10HevcRext
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC_REXT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableDecodingColorDepth10HevcRext() {
        return enableDecodingColorDepth10HevcRext;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC_REXT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableDecodingColorDepth10HevcRext(
            @org.eclipse.jdt.annotation.Nullable Boolean enableDecodingColorDepth10HevcRext) {
        this.enableDecodingColorDepth10HevcRext = enableDecodingColorDepth10HevcRext;
    }

    public EncodingOptions enableDecodingColorDepth12HevcRext(
            @org.eclipse.jdt.annotation.Nullable Boolean enableDecodingColorDepth12HevcRext) {
        this.enableDecodingColorDepth12HevcRext = enableDecodingColorDepth12HevcRext;
        return this;
    }

    /**
     * Gets or sets a value indicating whether 12bit HEVC RExt decoding is enabled.
     * 
     * @return enableDecodingColorDepth12HevcRext
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH12_HEVC_REXT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableDecodingColorDepth12HevcRext() {
        return enableDecodingColorDepth12HevcRext;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH12_HEVC_REXT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableDecodingColorDepth12HevcRext(
            @org.eclipse.jdt.annotation.Nullable Boolean enableDecodingColorDepth12HevcRext) {
        this.enableDecodingColorDepth12HevcRext = enableDecodingColorDepth12HevcRext;
    }

    public EncodingOptions enableEnhancedNvdecDecoder(
            @org.eclipse.jdt.annotation.Nullable Boolean enableEnhancedNvdecDecoder) {
        this.enableEnhancedNvdecDecoder = enableEnhancedNvdecDecoder;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the enhanced NVDEC is enabled.
     * 
     * @return enableEnhancedNvdecDecoder
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_ENHANCED_NVDEC_DECODER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableEnhancedNvdecDecoder() {
        return enableEnhancedNvdecDecoder;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_ENHANCED_NVDEC_DECODER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableEnhancedNvdecDecoder(@org.eclipse.jdt.annotation.Nullable Boolean enableEnhancedNvdecDecoder) {
        this.enableEnhancedNvdecDecoder = enableEnhancedNvdecDecoder;
    }

    public EncodingOptions preferSystemNativeHwDecoder(
            @org.eclipse.jdt.annotation.Nullable Boolean preferSystemNativeHwDecoder) {
        this.preferSystemNativeHwDecoder = preferSystemNativeHwDecoder;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the system native hardware decoder should be used.
     * 
     * @return preferSystemNativeHwDecoder
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PREFER_SYSTEM_NATIVE_HW_DECODER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getPreferSystemNativeHwDecoder() {
        return preferSystemNativeHwDecoder;
    }

    @JsonProperty(value = JSON_PROPERTY_PREFER_SYSTEM_NATIVE_HW_DECODER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferSystemNativeHwDecoder(
            @org.eclipse.jdt.annotation.Nullable Boolean preferSystemNativeHwDecoder) {
        this.preferSystemNativeHwDecoder = preferSystemNativeHwDecoder;
    }

    public EncodingOptions enableIntelLowPowerH264HwEncoder(
            @org.eclipse.jdt.annotation.Nullable Boolean enableIntelLowPowerH264HwEncoder) {
        this.enableIntelLowPowerH264HwEncoder = enableIntelLowPowerH264HwEncoder;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the Intel H264 low-power hardware encoder should be used.
     * 
     * @return enableIntelLowPowerH264HwEncoder
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_H264_HW_ENCODER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableIntelLowPowerH264HwEncoder() {
        return enableIntelLowPowerH264HwEncoder;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_H264_HW_ENCODER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableIntelLowPowerH264HwEncoder(
            @org.eclipse.jdt.annotation.Nullable Boolean enableIntelLowPowerH264HwEncoder) {
        this.enableIntelLowPowerH264HwEncoder = enableIntelLowPowerH264HwEncoder;
    }

    public EncodingOptions enableIntelLowPowerHevcHwEncoder(
            @org.eclipse.jdt.annotation.Nullable Boolean enableIntelLowPowerHevcHwEncoder) {
        this.enableIntelLowPowerHevcHwEncoder = enableIntelLowPowerHevcHwEncoder;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the Intel HEVC low-power hardware encoder should be used.
     * 
     * @return enableIntelLowPowerHevcHwEncoder
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_HEVC_HW_ENCODER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableIntelLowPowerHevcHwEncoder() {
        return enableIntelLowPowerHevcHwEncoder;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_HEVC_HW_ENCODER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableIntelLowPowerHevcHwEncoder(
            @org.eclipse.jdt.annotation.Nullable Boolean enableIntelLowPowerHevcHwEncoder) {
        this.enableIntelLowPowerHevcHwEncoder = enableIntelLowPowerHevcHwEncoder;
    }

    public EncodingOptions enableHardwareEncoding(@org.eclipse.jdt.annotation.Nullable Boolean enableHardwareEncoding) {
        this.enableHardwareEncoding = enableHardwareEncoding;
        return this;
    }

    /**
     * Gets or sets a value indicating whether hardware encoding is enabled.
     * 
     * @return enableHardwareEncoding
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_HARDWARE_ENCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableHardwareEncoding() {
        return enableHardwareEncoding;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_HARDWARE_ENCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableHardwareEncoding(@org.eclipse.jdt.annotation.Nullable Boolean enableHardwareEncoding) {
        this.enableHardwareEncoding = enableHardwareEncoding;
    }

    public EncodingOptions allowHevcEncoding(@org.eclipse.jdt.annotation.Nullable Boolean allowHevcEncoding) {
        this.allowHevcEncoding = allowHevcEncoding;
        return this;
    }

    /**
     * Gets or sets a value indicating whether HEVC encoding is enabled.
     * 
     * @return allowHevcEncoding
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ALLOW_HEVC_ENCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAllowHevcEncoding() {
        return allowHevcEncoding;
    }

    @JsonProperty(value = JSON_PROPERTY_ALLOW_HEVC_ENCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowHevcEncoding(@org.eclipse.jdt.annotation.Nullable Boolean allowHevcEncoding) {
        this.allowHevcEncoding = allowHevcEncoding;
    }

    public EncodingOptions allowAv1Encoding(@org.eclipse.jdt.annotation.Nullable Boolean allowAv1Encoding) {
        this.allowAv1Encoding = allowAv1Encoding;
        return this;
    }

    /**
     * Gets or sets a value indicating whether AV1 encoding is enabled.
     * 
     * @return allowAv1Encoding
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ALLOW_AV1_ENCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAllowAv1Encoding() {
        return allowAv1Encoding;
    }

    @JsonProperty(value = JSON_PROPERTY_ALLOW_AV1_ENCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowAv1Encoding(@org.eclipse.jdt.annotation.Nullable Boolean allowAv1Encoding) {
        this.allowAv1Encoding = allowAv1Encoding;
    }

    public EncodingOptions enableSubtitleExtraction(
            @org.eclipse.jdt.annotation.Nullable Boolean enableSubtitleExtraction) {
        this.enableSubtitleExtraction = enableSubtitleExtraction;
        return this;
    }

    /**
     * Gets or sets a value indicating whether subtitle extraction is enabled.
     * 
     * @return enableSubtitleExtraction
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ENABLE_SUBTITLE_EXTRACTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableSubtitleExtraction() {
        return enableSubtitleExtraction;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_SUBTITLE_EXTRACTION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSubtitleExtraction(@org.eclipse.jdt.annotation.Nullable Boolean enableSubtitleExtraction) {
        this.enableSubtitleExtraction = enableSubtitleExtraction;
    }

    public EncodingOptions hardwareDecodingCodecs(
            @org.eclipse.jdt.annotation.Nullable List<String> hardwareDecodingCodecs) {
        this.hardwareDecodingCodecs = hardwareDecodingCodecs;
        return this;
    }

    public EncodingOptions addHardwareDecodingCodecsItem(String hardwareDecodingCodecsItem) {
        if (this.hardwareDecodingCodecs == null) {
            this.hardwareDecodingCodecs = new ArrayList<>();
        }
        this.hardwareDecodingCodecs.add(hardwareDecodingCodecsItem);
        return this;
    }

    /**
     * Gets or sets the codecs hardware encoding is used for.
     * 
     * @return hardwareDecodingCodecs
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_HARDWARE_DECODING_CODECS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getHardwareDecodingCodecs() {
        return hardwareDecodingCodecs;
    }

    @JsonProperty(value = JSON_PROPERTY_HARDWARE_DECODING_CODECS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHardwareDecodingCodecs(@org.eclipse.jdt.annotation.Nullable List<String> hardwareDecodingCodecs) {
        this.hardwareDecodingCodecs = hardwareDecodingCodecs;
    }

    public EncodingOptions allowOnDemandMetadataBasedKeyframeExtractionForExtensions(
            @org.eclipse.jdt.annotation.Nullable List<String> allowOnDemandMetadataBasedKeyframeExtractionForExtensions) {
        this.allowOnDemandMetadataBasedKeyframeExtractionForExtensions = allowOnDemandMetadataBasedKeyframeExtractionForExtensions;
        return this;
    }

    public EncodingOptions addAllowOnDemandMetadataBasedKeyframeExtractionForExtensionsItem(
            String allowOnDemandMetadataBasedKeyframeExtractionForExtensionsItem) {
        if (this.allowOnDemandMetadataBasedKeyframeExtractionForExtensions == null) {
            this.allowOnDemandMetadataBasedKeyframeExtractionForExtensions = new ArrayList<>();
        }
        this.allowOnDemandMetadataBasedKeyframeExtractionForExtensions
                .add(allowOnDemandMetadataBasedKeyframeExtractionForExtensionsItem);
        return this;
    }

    /**
     * Gets or sets the file extensions on-demand metadata based keyframe extraction is enabled for.
     * 
     * @return allowOnDemandMetadataBasedKeyframeExtractionForExtensions
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ALLOW_ON_DEMAND_METADATA_BASED_KEYFRAME_EXTRACTION_FOR_EXTENSIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getAllowOnDemandMetadataBasedKeyframeExtractionForExtensions() {
        return allowOnDemandMetadataBasedKeyframeExtractionForExtensions;
    }

    @JsonProperty(value = JSON_PROPERTY_ALLOW_ON_DEMAND_METADATA_BASED_KEYFRAME_EXTRACTION_FOR_EXTENSIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowOnDemandMetadataBasedKeyframeExtractionForExtensions(
            @org.eclipse.jdt.annotation.Nullable List<String> allowOnDemandMetadataBasedKeyframeExtractionForExtensions) {
        this.allowOnDemandMetadataBasedKeyframeExtractionForExtensions = allowOnDemandMetadataBasedKeyframeExtractionForExtensions;
    }

    /**
     * Return true if this EncodingOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        EncodingOptions encodingOptions = (EncodingOptions) o;
        return Objects.equals(this.encodingThreadCount, encodingOptions.encodingThreadCount)
                && Objects.equals(this.transcodingTempPath, encodingOptions.transcodingTempPath)
                && Objects.equals(this.fallbackFontPath, encodingOptions.fallbackFontPath)
                && Objects.equals(this.enableFallbackFont, encodingOptions.enableFallbackFont)
                && Objects.equals(this.enableAudioVbr, encodingOptions.enableAudioVbr)
                && Objects.equals(this.downMixAudioBoost, encodingOptions.downMixAudioBoost)
                && Objects.equals(this.downMixStereoAlgorithm, encodingOptions.downMixStereoAlgorithm)
                && Objects.equals(this.maxMuxingQueueSize, encodingOptions.maxMuxingQueueSize)
                && Objects.equals(this.enableThrottling, encodingOptions.enableThrottling)
                && Objects.equals(this.throttleDelaySeconds, encodingOptions.throttleDelaySeconds)
                && Objects.equals(this.enableSegmentDeletion, encodingOptions.enableSegmentDeletion)
                && Objects.equals(this.segmentKeepSeconds, encodingOptions.segmentKeepSeconds)
                && Objects.equals(this.hardwareAccelerationType, encodingOptions.hardwareAccelerationType)
                && Objects.equals(this.encoderAppPath, encodingOptions.encoderAppPath)
                && Objects.equals(this.encoderAppPathDisplay, encodingOptions.encoderAppPathDisplay)
                && Objects.equals(this.vaapiDevice, encodingOptions.vaapiDevice)
                && Objects.equals(this.qsvDevice, encodingOptions.qsvDevice)
                && Objects.equals(this.enableTonemapping, encodingOptions.enableTonemapping)
                && Objects.equals(this.enableVppTonemapping, encodingOptions.enableVppTonemapping)
                && Objects.equals(this.enableVideoToolboxTonemapping, encodingOptions.enableVideoToolboxTonemapping)
                && Objects.equals(this.tonemappingAlgorithm, encodingOptions.tonemappingAlgorithm)
                && Objects.equals(this.tonemappingMode, encodingOptions.tonemappingMode)
                && Objects.equals(this.tonemappingRange, encodingOptions.tonemappingRange)
                && Objects.equals(this.tonemappingDesat, encodingOptions.tonemappingDesat)
                && Objects.equals(this.tonemappingPeak, encodingOptions.tonemappingPeak)
                && Objects.equals(this.tonemappingParam, encodingOptions.tonemappingParam)
                && Objects.equals(this.vppTonemappingBrightness, encodingOptions.vppTonemappingBrightness)
                && Objects.equals(this.vppTonemappingContrast, encodingOptions.vppTonemappingContrast)
                && Objects.equals(this.h264Crf, encodingOptions.h264Crf)
                && Objects.equals(this.h265Crf, encodingOptions.h265Crf)
                && Objects.equals(this.encoderPreset, encodingOptions.encoderPreset)
                && Objects.equals(this.deinterlaceDoubleRate, encodingOptions.deinterlaceDoubleRate)
                && Objects.equals(this.deinterlaceMethod, encodingOptions.deinterlaceMethod)
                && Objects.equals(this.enableDecodingColorDepth10Hevc, encodingOptions.enableDecodingColorDepth10Hevc)
                && Objects.equals(this.enableDecodingColorDepth10Vp9, encodingOptions.enableDecodingColorDepth10Vp9)
                && Objects.equals(this.enableDecodingColorDepth10HevcRext,
                        encodingOptions.enableDecodingColorDepth10HevcRext)
                && Objects.equals(this.enableDecodingColorDepth12HevcRext,
                        encodingOptions.enableDecodingColorDepth12HevcRext)
                && Objects.equals(this.enableEnhancedNvdecDecoder, encodingOptions.enableEnhancedNvdecDecoder)
                && Objects.equals(this.preferSystemNativeHwDecoder, encodingOptions.preferSystemNativeHwDecoder)
                && Objects.equals(this.enableIntelLowPowerH264HwEncoder,
                        encodingOptions.enableIntelLowPowerH264HwEncoder)
                && Objects.equals(this.enableIntelLowPowerHevcHwEncoder,
                        encodingOptions.enableIntelLowPowerHevcHwEncoder)
                && Objects.equals(this.enableHardwareEncoding, encodingOptions.enableHardwareEncoding)
                && Objects.equals(this.allowHevcEncoding, encodingOptions.allowHevcEncoding)
                && Objects.equals(this.allowAv1Encoding, encodingOptions.allowAv1Encoding)
                && Objects.equals(this.enableSubtitleExtraction, encodingOptions.enableSubtitleExtraction)
                && Objects.equals(this.hardwareDecodingCodecs, encodingOptions.hardwareDecodingCodecs)
                && Objects.equals(this.allowOnDemandMetadataBasedKeyframeExtractionForExtensions,
                        encodingOptions.allowOnDemandMetadataBasedKeyframeExtractionForExtensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encodingThreadCount, transcodingTempPath, fallbackFontPath, enableFallbackFont,
                enableAudioVbr, downMixAudioBoost, downMixStereoAlgorithm, maxMuxingQueueSize, enableThrottling,
                throttleDelaySeconds, enableSegmentDeletion, segmentKeepSeconds, hardwareAccelerationType,
                encoderAppPath, encoderAppPathDisplay, vaapiDevice, qsvDevice, enableTonemapping, enableVppTonemapping,
                enableVideoToolboxTonemapping, tonemappingAlgorithm, tonemappingMode, tonemappingRange,
                tonemappingDesat, tonemappingPeak, tonemappingParam, vppTonemappingBrightness, vppTonemappingContrast,
                h264Crf, h265Crf, encoderPreset, deinterlaceDoubleRate, deinterlaceMethod,
                enableDecodingColorDepth10Hevc, enableDecodingColorDepth10Vp9, enableDecodingColorDepth10HevcRext,
                enableDecodingColorDepth12HevcRext, enableEnhancedNvdecDecoder, preferSystemNativeHwDecoder,
                enableIntelLowPowerH264HwEncoder, enableIntelLowPowerHevcHwEncoder, enableHardwareEncoding,
                allowHevcEncoding, allowAv1Encoding, enableSubtitleExtraction, hardwareDecodingCodecs,
                allowOnDemandMetadataBasedKeyframeExtractionForExtensions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EncodingOptions {\n");
        sb.append("    encodingThreadCount: ").append(toIndentedString(encodingThreadCount)).append("\n");
        sb.append("    transcodingTempPath: ").append(toIndentedString(transcodingTempPath)).append("\n");
        sb.append("    fallbackFontPath: ").append(toIndentedString(fallbackFontPath)).append("\n");
        sb.append("    enableFallbackFont: ").append(toIndentedString(enableFallbackFont)).append("\n");
        sb.append("    enableAudioVbr: ").append(toIndentedString(enableAudioVbr)).append("\n");
        sb.append("    downMixAudioBoost: ").append(toIndentedString(downMixAudioBoost)).append("\n");
        sb.append("    downMixStereoAlgorithm: ").append(toIndentedString(downMixStereoAlgorithm)).append("\n");
        sb.append("    maxMuxingQueueSize: ").append(toIndentedString(maxMuxingQueueSize)).append("\n");
        sb.append("    enableThrottling: ").append(toIndentedString(enableThrottling)).append("\n");
        sb.append("    throttleDelaySeconds: ").append(toIndentedString(throttleDelaySeconds)).append("\n");
        sb.append("    enableSegmentDeletion: ").append(toIndentedString(enableSegmentDeletion)).append("\n");
        sb.append("    segmentKeepSeconds: ").append(toIndentedString(segmentKeepSeconds)).append("\n");
        sb.append("    hardwareAccelerationType: ").append(toIndentedString(hardwareAccelerationType)).append("\n");
        sb.append("    encoderAppPath: ").append(toIndentedString(encoderAppPath)).append("\n");
        sb.append("    encoderAppPathDisplay: ").append(toIndentedString(encoderAppPathDisplay)).append("\n");
        sb.append("    vaapiDevice: ").append(toIndentedString(vaapiDevice)).append("\n");
        sb.append("    qsvDevice: ").append(toIndentedString(qsvDevice)).append("\n");
        sb.append("    enableTonemapping: ").append(toIndentedString(enableTonemapping)).append("\n");
        sb.append("    enableVppTonemapping: ").append(toIndentedString(enableVppTonemapping)).append("\n");
        sb.append("    enableVideoToolboxTonemapping: ").append(toIndentedString(enableVideoToolboxTonemapping))
                .append("\n");
        sb.append("    tonemappingAlgorithm: ").append(toIndentedString(tonemappingAlgorithm)).append("\n");
        sb.append("    tonemappingMode: ").append(toIndentedString(tonemappingMode)).append("\n");
        sb.append("    tonemappingRange: ").append(toIndentedString(tonemappingRange)).append("\n");
        sb.append("    tonemappingDesat: ").append(toIndentedString(tonemappingDesat)).append("\n");
        sb.append("    tonemappingPeak: ").append(toIndentedString(tonemappingPeak)).append("\n");
        sb.append("    tonemappingParam: ").append(toIndentedString(tonemappingParam)).append("\n");
        sb.append("    vppTonemappingBrightness: ").append(toIndentedString(vppTonemappingBrightness)).append("\n");
        sb.append("    vppTonemappingContrast: ").append(toIndentedString(vppTonemappingContrast)).append("\n");
        sb.append("    h264Crf: ").append(toIndentedString(h264Crf)).append("\n");
        sb.append("    h265Crf: ").append(toIndentedString(h265Crf)).append("\n");
        sb.append("    encoderPreset: ").append(toIndentedString(encoderPreset)).append("\n");
        sb.append("    deinterlaceDoubleRate: ").append(toIndentedString(deinterlaceDoubleRate)).append("\n");
        sb.append("    deinterlaceMethod: ").append(toIndentedString(deinterlaceMethod)).append("\n");
        sb.append("    enableDecodingColorDepth10Hevc: ").append(toIndentedString(enableDecodingColorDepth10Hevc))
                .append("\n");
        sb.append("    enableDecodingColorDepth10Vp9: ").append(toIndentedString(enableDecodingColorDepth10Vp9))
                .append("\n");
        sb.append("    enableDecodingColorDepth10HevcRext: ")
                .append(toIndentedString(enableDecodingColorDepth10HevcRext)).append("\n");
        sb.append("    enableDecodingColorDepth12HevcRext: ")
                .append(toIndentedString(enableDecodingColorDepth12HevcRext)).append("\n");
        sb.append("    enableEnhancedNvdecDecoder: ").append(toIndentedString(enableEnhancedNvdecDecoder)).append("\n");
        sb.append("    preferSystemNativeHwDecoder: ").append(toIndentedString(preferSystemNativeHwDecoder))
                .append("\n");
        sb.append("    enableIntelLowPowerH264HwEncoder: ").append(toIndentedString(enableIntelLowPowerH264HwEncoder))
                .append("\n");
        sb.append("    enableIntelLowPowerHevcHwEncoder: ").append(toIndentedString(enableIntelLowPowerHevcHwEncoder))
                .append("\n");
        sb.append("    enableHardwareEncoding: ").append(toIndentedString(enableHardwareEncoding)).append("\n");
        sb.append("    allowHevcEncoding: ").append(toIndentedString(allowHevcEncoding)).append("\n");
        sb.append("    allowAv1Encoding: ").append(toIndentedString(allowAv1Encoding)).append("\n");
        sb.append("    enableSubtitleExtraction: ").append(toIndentedString(enableSubtitleExtraction)).append("\n");
        sb.append("    hardwareDecodingCodecs: ").append(toIndentedString(hardwareDecodingCodecs)).append("\n");
        sb.append("    allowOnDemandMetadataBasedKeyframeExtractionForExtensions: ")
                .append(toIndentedString(allowOnDemandMetadataBasedKeyframeExtractionForExtensions)).append("\n");
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

        // add `EncodingThreadCount` to the URL query string
        if (getEncodingThreadCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEncodingThreadCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncodingThreadCount()))));
        }

        // add `TranscodingTempPath` to the URL query string
        if (getTranscodingTempPath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTranscodingTempPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTranscodingTempPath()))));
        }

        // add `FallbackFontPath` to the URL query string
        if (getFallbackFontPath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sFallbackFontPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFallbackFontPath()))));
        }

        // add `EnableFallbackFont` to the URL query string
        if (getEnableFallbackFont() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableFallbackFont%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableFallbackFont()))));
        }

        // add `EnableAudioVbr` to the URL query string
        if (getEnableAudioVbr() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableAudioVbr%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAudioVbr()))));
        }

        // add `DownMixAudioBoost` to the URL query string
        if (getDownMixAudioBoost() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDownMixAudioBoost%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDownMixAudioBoost()))));
        }

        // add `DownMixStereoAlgorithm` to the URL query string
        if (getDownMixStereoAlgorithm() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDownMixStereoAlgorithm%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDownMixStereoAlgorithm()))));
        }

        // add `MaxMuxingQueueSize` to the URL query string
        if (getMaxMuxingQueueSize() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMaxMuxingQueueSize%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxMuxingQueueSize()))));
        }

        // add `EnableThrottling` to the URL query string
        if (getEnableThrottling() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableThrottling%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableThrottling()))));
        }

        // add `ThrottleDelaySeconds` to the URL query string
        if (getThrottleDelaySeconds() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sThrottleDelaySeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getThrottleDelaySeconds()))));
        }

        // add `EnableSegmentDeletion` to the URL query string
        if (getEnableSegmentDeletion() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableSegmentDeletion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableSegmentDeletion()))));
        }

        // add `SegmentKeepSeconds` to the URL query string
        if (getSegmentKeepSeconds() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSegmentKeepSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSegmentKeepSeconds()))));
        }

        // add `HardwareAccelerationType` to the URL query string
        if (getHardwareAccelerationType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sHardwareAccelerationType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHardwareAccelerationType()))));
        }

        // add `EncoderAppPath` to the URL query string
        if (getEncoderAppPath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEncoderAppPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncoderAppPath()))));
        }

        // add `EncoderAppPathDisplay` to the URL query string
        if (getEncoderAppPathDisplay() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEncoderAppPathDisplay%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncoderAppPathDisplay()))));
        }

        // add `VaapiDevice` to the URL query string
        if (getVaapiDevice() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVaapiDevice%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVaapiDevice()))));
        }

        // add `QsvDevice` to the URL query string
        if (getQsvDevice() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sQsvDevice%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getQsvDevice()))));
        }

        // add `EnableTonemapping` to the URL query string
        if (getEnableTonemapping() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableTonemapping%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableTonemapping()))));
        }

        // add `EnableVppTonemapping` to the URL query string
        if (getEnableVppTonemapping() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableVppTonemapping%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableVppTonemapping()))));
        }

        // add `EnableVideoToolboxTonemapping` to the URL query string
        if (getEnableVideoToolboxTonemapping() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableVideoToolboxTonemapping%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableVideoToolboxTonemapping()))));
        }

        // add `TonemappingAlgorithm` to the URL query string
        if (getTonemappingAlgorithm() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTonemappingAlgorithm%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingAlgorithm()))));
        }

        // add `TonemappingMode` to the URL query string
        if (getTonemappingMode() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTonemappingMode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingMode()))));
        }

        // add `TonemappingRange` to the URL query string
        if (getTonemappingRange() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTonemappingRange%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingRange()))));
        }

        // add `TonemappingDesat` to the URL query string
        if (getTonemappingDesat() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTonemappingDesat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingDesat()))));
        }

        // add `TonemappingPeak` to the URL query string
        if (getTonemappingPeak() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTonemappingPeak%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingPeak()))));
        }

        // add `TonemappingParam` to the URL query string
        if (getTonemappingParam() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTonemappingParam%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingParam()))));
        }

        // add `VppTonemappingBrightness` to the URL query string
        if (getVppTonemappingBrightness() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVppTonemappingBrightness%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVppTonemappingBrightness()))));
        }

        // add `VppTonemappingContrast` to the URL query string
        if (getVppTonemappingContrast() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVppTonemappingContrast%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVppTonemappingContrast()))));
        }

        // add `H264Crf` to the URL query string
        if (getH264Crf() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sH264Crf%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getH264Crf()))));
        }

        // add `H265Crf` to the URL query string
        if (getH265Crf() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sH265Crf%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getH265Crf()))));
        }

        // add `EncoderPreset` to the URL query string
        if (getEncoderPreset() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEncoderPreset%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncoderPreset()))));
        }

        // add `DeinterlaceDoubleRate` to the URL query string
        if (getDeinterlaceDoubleRate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDeinterlaceDoubleRate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeinterlaceDoubleRate()))));
        }

        // add `DeinterlaceMethod` to the URL query string
        if (getDeinterlaceMethod() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDeinterlaceMethod%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeinterlaceMethod()))));
        }

        // add `EnableDecodingColorDepth10Hevc` to the URL query string
        if (getEnableDecodingColorDepth10Hevc() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableDecodingColorDepth10Hevc%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableDecodingColorDepth10Hevc()))));
        }

        // add `EnableDecodingColorDepth10Vp9` to the URL query string
        if (getEnableDecodingColorDepth10Vp9() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableDecodingColorDepth10Vp9%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableDecodingColorDepth10Vp9()))));
        }

        // add `EnableDecodingColorDepth10HevcRext` to the URL query string
        if (getEnableDecodingColorDepth10HevcRext() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableDecodingColorDepth10HevcRext%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableDecodingColorDepth10HevcRext()))));
        }

        // add `EnableDecodingColorDepth12HevcRext` to the URL query string
        if (getEnableDecodingColorDepth12HevcRext() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableDecodingColorDepth12HevcRext%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableDecodingColorDepth12HevcRext()))));
        }

        // add `EnableEnhancedNvdecDecoder` to the URL query string
        if (getEnableEnhancedNvdecDecoder() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableEnhancedNvdecDecoder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableEnhancedNvdecDecoder()))));
        }

        // add `PreferSystemNativeHwDecoder` to the URL query string
        if (getPreferSystemNativeHwDecoder() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPreferSystemNativeHwDecoder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreferSystemNativeHwDecoder()))));
        }

        // add `EnableIntelLowPowerH264HwEncoder` to the URL query string
        if (getEnableIntelLowPowerH264HwEncoder() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableIntelLowPowerH264HwEncoder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableIntelLowPowerH264HwEncoder()))));
        }

        // add `EnableIntelLowPowerHevcHwEncoder` to the URL query string
        if (getEnableIntelLowPowerHevcHwEncoder() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableIntelLowPowerHevcHwEncoder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableIntelLowPowerHevcHwEncoder()))));
        }

        // add `EnableHardwareEncoding` to the URL query string
        if (getEnableHardwareEncoding() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableHardwareEncoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableHardwareEncoding()))));
        }

        // add `AllowHevcEncoding` to the URL query string
        if (getAllowHevcEncoding() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAllowHevcEncoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAllowHevcEncoding()))));
        }

        // add `AllowAv1Encoding` to the URL query string
        if (getAllowAv1Encoding() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAllowAv1Encoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAllowAv1Encoding()))));
        }

        // add `EnableSubtitleExtraction` to the URL query string
        if (getEnableSubtitleExtraction() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sEnableSubtitleExtraction%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableSubtitleExtraction()))));
        }

        // add `HardwareDecodingCodecs` to the URL query string
        if (getHardwareDecodingCodecs() != null) {
            for (int i = 0; i < getHardwareDecodingCodecs().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sHardwareDecodingCodecs%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getHardwareDecodingCodecs().get(i)))));
            }
        }

        // add `AllowOnDemandMetadataBasedKeyframeExtractionForExtensions` to the URL query string
        if (getAllowOnDemandMetadataBasedKeyframeExtractionForExtensions() != null) {
            for (int i = 0; i < getAllowOnDemandMetadataBasedKeyframeExtractionForExtensions().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT,
                        "%sAllowOnDemandMetadataBasedKeyframeExtractionForExtensions%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(
                                getAllowOnDemandMetadataBasedKeyframeExtractionForExtensions().get(i)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private EncodingOptions instance;

        public Builder() {
            this(new EncodingOptions());
        }

        protected Builder(EncodingOptions instance) {
            this.instance = instance;
        }

        public EncodingOptions.Builder encodingThreadCount(Integer encodingThreadCount) {
            this.instance.encodingThreadCount = encodingThreadCount;
            return this;
        }

        public EncodingOptions.Builder transcodingTempPath(String transcodingTempPath) {
            this.instance.transcodingTempPath = transcodingTempPath;
            return this;
        }

        public EncodingOptions.Builder fallbackFontPath(String fallbackFontPath) {
            this.instance.fallbackFontPath = fallbackFontPath;
            return this;
        }

        public EncodingOptions.Builder enableFallbackFont(Boolean enableFallbackFont) {
            this.instance.enableFallbackFont = enableFallbackFont;
            return this;
        }

        public EncodingOptions.Builder enableAudioVbr(Boolean enableAudioVbr) {
            this.instance.enableAudioVbr = enableAudioVbr;
            return this;
        }

        public EncodingOptions.Builder downMixAudioBoost(Double downMixAudioBoost) {
            this.instance.downMixAudioBoost = downMixAudioBoost;
            return this;
        }

        public EncodingOptions.Builder downMixStereoAlgorithm(DownMixStereoAlgorithms downMixStereoAlgorithm) {
            this.instance.downMixStereoAlgorithm = downMixStereoAlgorithm;
            return this;
        }

        public EncodingOptions.Builder maxMuxingQueueSize(Integer maxMuxingQueueSize) {
            this.instance.maxMuxingQueueSize = maxMuxingQueueSize;
            return this;
        }

        public EncodingOptions.Builder enableThrottling(Boolean enableThrottling) {
            this.instance.enableThrottling = enableThrottling;
            return this;
        }

        public EncodingOptions.Builder throttleDelaySeconds(Integer throttleDelaySeconds) {
            this.instance.throttleDelaySeconds = throttleDelaySeconds;
            return this;
        }

        public EncodingOptions.Builder enableSegmentDeletion(Boolean enableSegmentDeletion) {
            this.instance.enableSegmentDeletion = enableSegmentDeletion;
            return this;
        }

        public EncodingOptions.Builder segmentKeepSeconds(Integer segmentKeepSeconds) {
            this.instance.segmentKeepSeconds = segmentKeepSeconds;
            return this;
        }

        public EncodingOptions.Builder hardwareAccelerationType(HardwareAccelerationType hardwareAccelerationType) {
            this.instance.hardwareAccelerationType = hardwareAccelerationType;
            return this;
        }

        public EncodingOptions.Builder encoderAppPath(String encoderAppPath) {
            this.instance.encoderAppPath = encoderAppPath;
            return this;
        }

        public EncodingOptions.Builder encoderAppPathDisplay(String encoderAppPathDisplay) {
            this.instance.encoderAppPathDisplay = encoderAppPathDisplay;
            return this;
        }

        public EncodingOptions.Builder vaapiDevice(String vaapiDevice) {
            this.instance.vaapiDevice = vaapiDevice;
            return this;
        }

        public EncodingOptions.Builder qsvDevice(String qsvDevice) {
            this.instance.qsvDevice = qsvDevice;
            return this;
        }

        public EncodingOptions.Builder enableTonemapping(Boolean enableTonemapping) {
            this.instance.enableTonemapping = enableTonemapping;
            return this;
        }

        public EncodingOptions.Builder enableVppTonemapping(Boolean enableVppTonemapping) {
            this.instance.enableVppTonemapping = enableVppTonemapping;
            return this;
        }

        public EncodingOptions.Builder enableVideoToolboxTonemapping(Boolean enableVideoToolboxTonemapping) {
            this.instance.enableVideoToolboxTonemapping = enableVideoToolboxTonemapping;
            return this;
        }

        public EncodingOptions.Builder tonemappingAlgorithm(TonemappingAlgorithm tonemappingAlgorithm) {
            this.instance.tonemappingAlgorithm = tonemappingAlgorithm;
            return this;
        }

        public EncodingOptions.Builder tonemappingMode(TonemappingMode tonemappingMode) {
            this.instance.tonemappingMode = tonemappingMode;
            return this;
        }

        public EncodingOptions.Builder tonemappingRange(TonemappingRange tonemappingRange) {
            this.instance.tonemappingRange = tonemappingRange;
            return this;
        }

        public EncodingOptions.Builder tonemappingDesat(Double tonemappingDesat) {
            this.instance.tonemappingDesat = tonemappingDesat;
            return this;
        }

        public EncodingOptions.Builder tonemappingPeak(Double tonemappingPeak) {
            this.instance.tonemappingPeak = tonemappingPeak;
            return this;
        }

        public EncodingOptions.Builder tonemappingParam(Double tonemappingParam) {
            this.instance.tonemappingParam = tonemappingParam;
            return this;
        }

        public EncodingOptions.Builder vppTonemappingBrightness(Double vppTonemappingBrightness) {
            this.instance.vppTonemappingBrightness = vppTonemappingBrightness;
            return this;
        }

        public EncodingOptions.Builder vppTonemappingContrast(Double vppTonemappingContrast) {
            this.instance.vppTonemappingContrast = vppTonemappingContrast;
            return this;
        }

        public EncodingOptions.Builder h264Crf(Integer h264Crf) {
            this.instance.h264Crf = h264Crf;
            return this;
        }

        public EncodingOptions.Builder h265Crf(Integer h265Crf) {
            this.instance.h265Crf = h265Crf;
            return this;
        }

        public EncodingOptions.Builder encoderPreset(EncoderPreset encoderPreset) {
            this.instance.encoderPreset = encoderPreset;
            return this;
        }

        public EncodingOptions.Builder deinterlaceDoubleRate(Boolean deinterlaceDoubleRate) {
            this.instance.deinterlaceDoubleRate = deinterlaceDoubleRate;
            return this;
        }

        public EncodingOptions.Builder deinterlaceMethod(DeinterlaceMethod deinterlaceMethod) {
            this.instance.deinterlaceMethod = deinterlaceMethod;
            return this;
        }

        public EncodingOptions.Builder enableDecodingColorDepth10Hevc(Boolean enableDecodingColorDepth10Hevc) {
            this.instance.enableDecodingColorDepth10Hevc = enableDecodingColorDepth10Hevc;
            return this;
        }

        public EncodingOptions.Builder enableDecodingColorDepth10Vp9(Boolean enableDecodingColorDepth10Vp9) {
            this.instance.enableDecodingColorDepth10Vp9 = enableDecodingColorDepth10Vp9;
            return this;
        }

        public EncodingOptions.Builder enableDecodingColorDepth10HevcRext(Boolean enableDecodingColorDepth10HevcRext) {
            this.instance.enableDecodingColorDepth10HevcRext = enableDecodingColorDepth10HevcRext;
            return this;
        }

        public EncodingOptions.Builder enableDecodingColorDepth12HevcRext(Boolean enableDecodingColorDepth12HevcRext) {
            this.instance.enableDecodingColorDepth12HevcRext = enableDecodingColorDepth12HevcRext;
            return this;
        }

        public EncodingOptions.Builder enableEnhancedNvdecDecoder(Boolean enableEnhancedNvdecDecoder) {
            this.instance.enableEnhancedNvdecDecoder = enableEnhancedNvdecDecoder;
            return this;
        }

        public EncodingOptions.Builder preferSystemNativeHwDecoder(Boolean preferSystemNativeHwDecoder) {
            this.instance.preferSystemNativeHwDecoder = preferSystemNativeHwDecoder;
            return this;
        }

        public EncodingOptions.Builder enableIntelLowPowerH264HwEncoder(Boolean enableIntelLowPowerH264HwEncoder) {
            this.instance.enableIntelLowPowerH264HwEncoder = enableIntelLowPowerH264HwEncoder;
            return this;
        }

        public EncodingOptions.Builder enableIntelLowPowerHevcHwEncoder(Boolean enableIntelLowPowerHevcHwEncoder) {
            this.instance.enableIntelLowPowerHevcHwEncoder = enableIntelLowPowerHevcHwEncoder;
            return this;
        }

        public EncodingOptions.Builder enableHardwareEncoding(Boolean enableHardwareEncoding) {
            this.instance.enableHardwareEncoding = enableHardwareEncoding;
            return this;
        }

        public EncodingOptions.Builder allowHevcEncoding(Boolean allowHevcEncoding) {
            this.instance.allowHevcEncoding = allowHevcEncoding;
            return this;
        }

        public EncodingOptions.Builder allowAv1Encoding(Boolean allowAv1Encoding) {
            this.instance.allowAv1Encoding = allowAv1Encoding;
            return this;
        }

        public EncodingOptions.Builder enableSubtitleExtraction(Boolean enableSubtitleExtraction) {
            this.instance.enableSubtitleExtraction = enableSubtitleExtraction;
            return this;
        }

        public EncodingOptions.Builder hardwareDecodingCodecs(List<String> hardwareDecodingCodecs) {
            this.instance.hardwareDecodingCodecs = hardwareDecodingCodecs;
            return this;
        }

        public EncodingOptions.Builder allowOnDemandMetadataBasedKeyframeExtractionForExtensions(
                List<String> allowOnDemandMetadataBasedKeyframeExtractionForExtensions) {
            this.instance.allowOnDemandMetadataBasedKeyframeExtractionForExtensions = allowOnDemandMetadataBasedKeyframeExtractionForExtensions;
            return this;
        }

        /**
         * returns a built EncodingOptions instance.
         *
         * The builder is not reusable.
         */
        public EncodingOptions build() {
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
    public static EncodingOptions.Builder builder() {
        return new EncodingOptions.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public EncodingOptions.Builder toBuilder() {
        return new EncodingOptions.Builder().encodingThreadCount(getEncodingThreadCount())
                .transcodingTempPath(getTranscodingTempPath()).fallbackFontPath(getFallbackFontPath())
                .enableFallbackFont(getEnableFallbackFont()).enableAudioVbr(getEnableAudioVbr())
                .downMixAudioBoost(getDownMixAudioBoost()).downMixStereoAlgorithm(getDownMixStereoAlgorithm())
                .maxMuxingQueueSize(getMaxMuxingQueueSize()).enableThrottling(getEnableThrottling())
                .throttleDelaySeconds(getThrottleDelaySeconds()).enableSegmentDeletion(getEnableSegmentDeletion())
                .segmentKeepSeconds(getSegmentKeepSeconds()).hardwareAccelerationType(getHardwareAccelerationType())
                .encoderAppPath(getEncoderAppPath()).encoderAppPathDisplay(getEncoderAppPathDisplay())
                .vaapiDevice(getVaapiDevice()).qsvDevice(getQsvDevice()).enableTonemapping(getEnableTonemapping())
                .enableVppTonemapping(getEnableVppTonemapping())
                .enableVideoToolboxTonemapping(getEnableVideoToolboxTonemapping())
                .tonemappingAlgorithm(getTonemappingAlgorithm()).tonemappingMode(getTonemappingMode())
                .tonemappingRange(getTonemappingRange()).tonemappingDesat(getTonemappingDesat())
                .tonemappingPeak(getTonemappingPeak()).tonemappingParam(getTonemappingParam())
                .vppTonemappingBrightness(getVppTonemappingBrightness())
                .vppTonemappingContrast(getVppTonemappingContrast()).h264Crf(getH264Crf()).h265Crf(getH265Crf())
                .encoderPreset(getEncoderPreset()).deinterlaceDoubleRate(getDeinterlaceDoubleRate())
                .deinterlaceMethod(getDeinterlaceMethod())
                .enableDecodingColorDepth10Hevc(getEnableDecodingColorDepth10Hevc())
                .enableDecodingColorDepth10Vp9(getEnableDecodingColorDepth10Vp9())
                .enableDecodingColorDepth10HevcRext(getEnableDecodingColorDepth10HevcRext())
                .enableDecodingColorDepth12HevcRext(getEnableDecodingColorDepth12HevcRext())
                .enableEnhancedNvdecDecoder(getEnableEnhancedNvdecDecoder())
                .preferSystemNativeHwDecoder(getPreferSystemNativeHwDecoder())
                .enableIntelLowPowerH264HwEncoder(getEnableIntelLowPowerH264HwEncoder())
                .enableIntelLowPowerHevcHwEncoder(getEnableIntelLowPowerHevcHwEncoder())
                .enableHardwareEncoding(getEnableHardwareEncoding()).allowHevcEncoding(getAllowHevcEncoding())
                .allowAv1Encoding(getAllowAv1Encoding()).enableSubtitleExtraction(getEnableSubtitleExtraction())
                .hardwareDecodingCodecs(getHardwareDecodingCodecs())
                .allowOnDemandMetadataBasedKeyframeExtractionForExtensions(
                        getAllowOnDemandMetadataBasedKeyframeExtractionForExtensions());
    }
}
