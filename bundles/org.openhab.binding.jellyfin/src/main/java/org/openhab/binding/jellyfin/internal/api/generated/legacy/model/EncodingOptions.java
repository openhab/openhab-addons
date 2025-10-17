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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * EncodingOptions
 */
@JsonPropertyOrder({ EncodingOptions.JSON_PROPERTY_ENCODING_THREAD_COUNT,
        EncodingOptions.JSON_PROPERTY_TRANSCODING_TEMP_PATH, EncodingOptions.JSON_PROPERTY_FALLBACK_FONT_PATH,
        EncodingOptions.JSON_PROPERTY_ENABLE_FALLBACK_FONT, EncodingOptions.JSON_PROPERTY_DOWN_MIX_AUDIO_BOOST,
        EncodingOptions.JSON_PROPERTY_MAX_MUXING_QUEUE_SIZE, EncodingOptions.JSON_PROPERTY_ENABLE_THROTTLING,
        EncodingOptions.JSON_PROPERTY_THROTTLE_DELAY_SECONDS, EncodingOptions.JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE,
        EncodingOptions.JSON_PROPERTY_ENCODER_APP_PATH, EncodingOptions.JSON_PROPERTY_ENCODER_APP_PATH_DISPLAY,
        EncodingOptions.JSON_PROPERTY_VAAPI_DEVICE, EncodingOptions.JSON_PROPERTY_ENABLE_TONEMAPPING,
        EncodingOptions.JSON_PROPERTY_ENABLE_VPP_TONEMAPPING, EncodingOptions.JSON_PROPERTY_TONEMAPPING_ALGORITHM,
        EncodingOptions.JSON_PROPERTY_TONEMAPPING_MODE, EncodingOptions.JSON_PROPERTY_TONEMAPPING_RANGE,
        EncodingOptions.JSON_PROPERTY_TONEMAPPING_DESAT, EncodingOptions.JSON_PROPERTY_TONEMAPPING_PEAK,
        EncodingOptions.JSON_PROPERTY_TONEMAPPING_PARAM, EncodingOptions.JSON_PROPERTY_VPP_TONEMAPPING_BRIGHTNESS,
        EncodingOptions.JSON_PROPERTY_VPP_TONEMAPPING_CONTRAST, EncodingOptions.JSON_PROPERTY_H264_CRF,
        EncodingOptions.JSON_PROPERTY_H265_CRF, EncodingOptions.JSON_PROPERTY_ENCODER_PRESET,
        EncodingOptions.JSON_PROPERTY_DEINTERLACE_DOUBLE_RATE, EncodingOptions.JSON_PROPERTY_DEINTERLACE_METHOD,
        EncodingOptions.JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC,
        EncodingOptions.JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_VP9,
        EncodingOptions.JSON_PROPERTY_ENABLE_ENHANCED_NVDEC_DECODER,
        EncodingOptions.JSON_PROPERTY_PREFER_SYSTEM_NATIVE_HW_DECODER,
        EncodingOptions.JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_H264_HW_ENCODER,
        EncodingOptions.JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_HEVC_HW_ENCODER,
        EncodingOptions.JSON_PROPERTY_ENABLE_HARDWARE_ENCODING, EncodingOptions.JSON_PROPERTY_ALLOW_HEVC_ENCODING,
        EncodingOptions.JSON_PROPERTY_ENABLE_SUBTITLE_EXTRACTION,
        EncodingOptions.JSON_PROPERTY_HARDWARE_DECODING_CODECS,
        EncodingOptions.JSON_PROPERTY_ALLOW_ON_DEMAND_METADATA_BASED_KEYFRAME_EXTRACTION_FOR_EXTENSIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class EncodingOptions {
    public static final String JSON_PROPERTY_ENCODING_THREAD_COUNT = "EncodingThreadCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer encodingThreadCount;

    public static final String JSON_PROPERTY_TRANSCODING_TEMP_PATH = "TranscodingTempPath";
    @org.eclipse.jdt.annotation.NonNull
    private String transcodingTempPath;

    public static final String JSON_PROPERTY_FALLBACK_FONT_PATH = "FallbackFontPath";
    @org.eclipse.jdt.annotation.NonNull
    private String fallbackFontPath;

    public static final String JSON_PROPERTY_ENABLE_FALLBACK_FONT = "EnableFallbackFont";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableFallbackFont;

    public static final String JSON_PROPERTY_DOWN_MIX_AUDIO_BOOST = "DownMixAudioBoost";
    @org.eclipse.jdt.annotation.NonNull
    private Double downMixAudioBoost;

    public static final String JSON_PROPERTY_MAX_MUXING_QUEUE_SIZE = "MaxMuxingQueueSize";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxMuxingQueueSize;

    public static final String JSON_PROPERTY_ENABLE_THROTTLING = "EnableThrottling";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableThrottling;

    public static final String JSON_PROPERTY_THROTTLE_DELAY_SECONDS = "ThrottleDelaySeconds";
    @org.eclipse.jdt.annotation.NonNull
    private Integer throttleDelaySeconds;

    public static final String JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE = "HardwareAccelerationType";
    @org.eclipse.jdt.annotation.NonNull
    private String hardwareAccelerationType;

    public static final String JSON_PROPERTY_ENCODER_APP_PATH = "EncoderAppPath";
    @org.eclipse.jdt.annotation.NonNull
    private String encoderAppPath;

    public static final String JSON_PROPERTY_ENCODER_APP_PATH_DISPLAY = "EncoderAppPathDisplay";
    @org.eclipse.jdt.annotation.NonNull
    private String encoderAppPathDisplay;

    public static final String JSON_PROPERTY_VAAPI_DEVICE = "VaapiDevice";
    @org.eclipse.jdt.annotation.NonNull
    private String vaapiDevice;

    public static final String JSON_PROPERTY_ENABLE_TONEMAPPING = "EnableTonemapping";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableTonemapping;

    public static final String JSON_PROPERTY_ENABLE_VPP_TONEMAPPING = "EnableVppTonemapping";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableVppTonemapping;

    public static final String JSON_PROPERTY_TONEMAPPING_ALGORITHM = "TonemappingAlgorithm";
    @org.eclipse.jdt.annotation.NonNull
    private String tonemappingAlgorithm;

    public static final String JSON_PROPERTY_TONEMAPPING_MODE = "TonemappingMode";
    @org.eclipse.jdt.annotation.NonNull
    private String tonemappingMode;

    public static final String JSON_PROPERTY_TONEMAPPING_RANGE = "TonemappingRange";
    @org.eclipse.jdt.annotation.NonNull
    private String tonemappingRange;

    public static final String JSON_PROPERTY_TONEMAPPING_DESAT = "TonemappingDesat";
    @org.eclipse.jdt.annotation.NonNull
    private Double tonemappingDesat;

    public static final String JSON_PROPERTY_TONEMAPPING_PEAK = "TonemappingPeak";
    @org.eclipse.jdt.annotation.NonNull
    private Double tonemappingPeak;

    public static final String JSON_PROPERTY_TONEMAPPING_PARAM = "TonemappingParam";
    @org.eclipse.jdt.annotation.NonNull
    private Double tonemappingParam;

    public static final String JSON_PROPERTY_VPP_TONEMAPPING_BRIGHTNESS = "VppTonemappingBrightness";
    @org.eclipse.jdt.annotation.NonNull
    private Double vppTonemappingBrightness;

    public static final String JSON_PROPERTY_VPP_TONEMAPPING_CONTRAST = "VppTonemappingContrast";
    @org.eclipse.jdt.annotation.NonNull
    private Double vppTonemappingContrast;

    public static final String JSON_PROPERTY_H264_CRF = "H264Crf";
    @org.eclipse.jdt.annotation.NonNull
    private Integer h264Crf;

    public static final String JSON_PROPERTY_H265_CRF = "H265Crf";
    @org.eclipse.jdt.annotation.NonNull
    private Integer h265Crf;

    public static final String JSON_PROPERTY_ENCODER_PRESET = "EncoderPreset";
    @org.eclipse.jdt.annotation.NonNull
    private String encoderPreset;

    public static final String JSON_PROPERTY_DEINTERLACE_DOUBLE_RATE = "DeinterlaceDoubleRate";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean deinterlaceDoubleRate;

    public static final String JSON_PROPERTY_DEINTERLACE_METHOD = "DeinterlaceMethod";
    @org.eclipse.jdt.annotation.NonNull
    private String deinterlaceMethod;

    public static final String JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC = "EnableDecodingColorDepth10Hevc";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableDecodingColorDepth10Hevc;

    public static final String JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_VP9 = "EnableDecodingColorDepth10Vp9";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableDecodingColorDepth10Vp9;

    public static final String JSON_PROPERTY_ENABLE_ENHANCED_NVDEC_DECODER = "EnableEnhancedNvdecDecoder";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableEnhancedNvdecDecoder;

    public static final String JSON_PROPERTY_PREFER_SYSTEM_NATIVE_HW_DECODER = "PreferSystemNativeHwDecoder";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean preferSystemNativeHwDecoder;

    public static final String JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_H264_HW_ENCODER = "EnableIntelLowPowerH264HwEncoder";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableIntelLowPowerH264HwEncoder;

    public static final String JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_HEVC_HW_ENCODER = "EnableIntelLowPowerHevcHwEncoder";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableIntelLowPowerHevcHwEncoder;

    public static final String JSON_PROPERTY_ENABLE_HARDWARE_ENCODING = "EnableHardwareEncoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableHardwareEncoding;

    public static final String JSON_PROPERTY_ALLOW_HEVC_ENCODING = "AllowHevcEncoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean allowHevcEncoding;

    public static final String JSON_PROPERTY_ENABLE_SUBTITLE_EXTRACTION = "EnableSubtitleExtraction";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableSubtitleExtraction;

    public static final String JSON_PROPERTY_HARDWARE_DECODING_CODECS = "HardwareDecodingCodecs";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> hardwareDecodingCodecs;

    public static final String JSON_PROPERTY_ALLOW_ON_DEMAND_METADATA_BASED_KEYFRAME_EXTRACTION_FOR_EXTENSIONS = "AllowOnDemandMetadataBasedKeyframeExtractionForExtensions";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> allowOnDemandMetadataBasedKeyframeExtractionForExtensions;

    public EncodingOptions() {
    }

    public EncodingOptions encodingThreadCount(@org.eclipse.jdt.annotation.NonNull Integer encodingThreadCount) {
        this.encodingThreadCount = encodingThreadCount;
        return this;
    }

    /**
     * Get encodingThreadCount
     * 
     * @return encodingThreadCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENCODING_THREAD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getEncodingThreadCount() {
        return encodingThreadCount;
    }

    @JsonProperty(JSON_PROPERTY_ENCODING_THREAD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncodingThreadCount(@org.eclipse.jdt.annotation.NonNull Integer encodingThreadCount) {
        this.encodingThreadCount = encodingThreadCount;
    }

    public EncodingOptions transcodingTempPath(@org.eclipse.jdt.annotation.NonNull String transcodingTempPath) {
        this.transcodingTempPath = transcodingTempPath;
        return this;
    }

    /**
     * Get transcodingTempPath
     * 
     * @return transcodingTempPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRANSCODING_TEMP_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTranscodingTempPath() {
        return transcodingTempPath;
    }

    @JsonProperty(JSON_PROPERTY_TRANSCODING_TEMP_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingTempPath(@org.eclipse.jdt.annotation.NonNull String transcodingTempPath) {
        this.transcodingTempPath = transcodingTempPath;
    }

    public EncodingOptions fallbackFontPath(@org.eclipse.jdt.annotation.NonNull String fallbackFontPath) {
        this.fallbackFontPath = fallbackFontPath;
        return this;
    }

    /**
     * Get fallbackFontPath
     * 
     * @return fallbackFontPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FALLBACK_FONT_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getFallbackFontPath() {
        return fallbackFontPath;
    }

    @JsonProperty(JSON_PROPERTY_FALLBACK_FONT_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFallbackFontPath(@org.eclipse.jdt.annotation.NonNull String fallbackFontPath) {
        this.fallbackFontPath = fallbackFontPath;
    }

    public EncodingOptions enableFallbackFont(@org.eclipse.jdt.annotation.NonNull Boolean enableFallbackFont) {
        this.enableFallbackFont = enableFallbackFont;
        return this;
    }

    /**
     * Get enableFallbackFont
     * 
     * @return enableFallbackFont
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_FALLBACK_FONT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableFallbackFont() {
        return enableFallbackFont;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_FALLBACK_FONT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableFallbackFont(@org.eclipse.jdt.annotation.NonNull Boolean enableFallbackFont) {
        this.enableFallbackFont = enableFallbackFont;
    }

    public EncodingOptions downMixAudioBoost(@org.eclipse.jdt.annotation.NonNull Double downMixAudioBoost) {
        this.downMixAudioBoost = downMixAudioBoost;
        return this;
    }

    /**
     * Get downMixAudioBoost
     * 
     * @return downMixAudioBoost
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DOWN_MIX_AUDIO_BOOST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getDownMixAudioBoost() {
        return downMixAudioBoost;
    }

    @JsonProperty(JSON_PROPERTY_DOWN_MIX_AUDIO_BOOST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDownMixAudioBoost(@org.eclipse.jdt.annotation.NonNull Double downMixAudioBoost) {
        this.downMixAudioBoost = downMixAudioBoost;
    }

    public EncodingOptions maxMuxingQueueSize(@org.eclipse.jdt.annotation.NonNull Integer maxMuxingQueueSize) {
        this.maxMuxingQueueSize = maxMuxingQueueSize;
        return this;
    }

    /**
     * Get maxMuxingQueueSize
     * 
     * @return maxMuxingQueueSize
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_MUXING_QUEUE_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxMuxingQueueSize() {
        return maxMuxingQueueSize;
    }

    @JsonProperty(JSON_PROPERTY_MAX_MUXING_QUEUE_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxMuxingQueueSize(@org.eclipse.jdt.annotation.NonNull Integer maxMuxingQueueSize) {
        this.maxMuxingQueueSize = maxMuxingQueueSize;
    }

    public EncodingOptions enableThrottling(@org.eclipse.jdt.annotation.NonNull Boolean enableThrottling) {
        this.enableThrottling = enableThrottling;
        return this;
    }

    /**
     * Get enableThrottling
     * 
     * @return enableThrottling
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_THROTTLING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableThrottling() {
        return enableThrottling;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_THROTTLING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableThrottling(@org.eclipse.jdt.annotation.NonNull Boolean enableThrottling) {
        this.enableThrottling = enableThrottling;
    }

    public EncodingOptions throttleDelaySeconds(@org.eclipse.jdt.annotation.NonNull Integer throttleDelaySeconds) {
        this.throttleDelaySeconds = throttleDelaySeconds;
        return this;
    }

    /**
     * Get throttleDelaySeconds
     * 
     * @return throttleDelaySeconds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_THROTTLE_DELAY_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getThrottleDelaySeconds() {
        return throttleDelaySeconds;
    }

    @JsonProperty(JSON_PROPERTY_THROTTLE_DELAY_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThrottleDelaySeconds(@org.eclipse.jdt.annotation.NonNull Integer throttleDelaySeconds) {
        this.throttleDelaySeconds = throttleDelaySeconds;
    }

    public EncodingOptions hardwareAccelerationType(
            @org.eclipse.jdt.annotation.NonNull String hardwareAccelerationType) {
        this.hardwareAccelerationType = hardwareAccelerationType;
        return this;
    }

    /**
     * Get hardwareAccelerationType
     * 
     * @return hardwareAccelerationType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getHardwareAccelerationType() {
        return hardwareAccelerationType;
    }

    @JsonProperty(JSON_PROPERTY_HARDWARE_ACCELERATION_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHardwareAccelerationType(@org.eclipse.jdt.annotation.NonNull String hardwareAccelerationType) {
        this.hardwareAccelerationType = hardwareAccelerationType;
    }

    public EncodingOptions encoderAppPath(@org.eclipse.jdt.annotation.NonNull String encoderAppPath) {
        this.encoderAppPath = encoderAppPath;
        return this;
    }

    /**
     * Gets or sets the FFmpeg path as set by the user via the UI.
     * 
     * @return encoderAppPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENCODER_APP_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEncoderAppPath() {
        return encoderAppPath;
    }

    @JsonProperty(JSON_PROPERTY_ENCODER_APP_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncoderAppPath(@org.eclipse.jdt.annotation.NonNull String encoderAppPath) {
        this.encoderAppPath = encoderAppPath;
    }

    public EncodingOptions encoderAppPathDisplay(@org.eclipse.jdt.annotation.NonNull String encoderAppPathDisplay) {
        this.encoderAppPathDisplay = encoderAppPathDisplay;
        return this;
    }

    /**
     * Gets or sets the current FFmpeg path being used by the system and displayed on the transcode page.
     * 
     * @return encoderAppPathDisplay
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENCODER_APP_PATH_DISPLAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEncoderAppPathDisplay() {
        return encoderAppPathDisplay;
    }

    @JsonProperty(JSON_PROPERTY_ENCODER_APP_PATH_DISPLAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncoderAppPathDisplay(@org.eclipse.jdt.annotation.NonNull String encoderAppPathDisplay) {
        this.encoderAppPathDisplay = encoderAppPathDisplay;
    }

    public EncodingOptions vaapiDevice(@org.eclipse.jdt.annotation.NonNull String vaapiDevice) {
        this.vaapiDevice = vaapiDevice;
        return this;
    }

    /**
     * Get vaapiDevice
     * 
     * @return vaapiDevice
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VAAPI_DEVICE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVaapiDevice() {
        return vaapiDevice;
    }

    @JsonProperty(JSON_PROPERTY_VAAPI_DEVICE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVaapiDevice(@org.eclipse.jdt.annotation.NonNull String vaapiDevice) {
        this.vaapiDevice = vaapiDevice;
    }

    public EncodingOptions enableTonemapping(@org.eclipse.jdt.annotation.NonNull Boolean enableTonemapping) {
        this.enableTonemapping = enableTonemapping;
        return this;
    }

    /**
     * Get enableTonemapping
     * 
     * @return enableTonemapping
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_TONEMAPPING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableTonemapping() {
        return enableTonemapping;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_TONEMAPPING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableTonemapping(@org.eclipse.jdt.annotation.NonNull Boolean enableTonemapping) {
        this.enableTonemapping = enableTonemapping;
    }

    public EncodingOptions enableVppTonemapping(@org.eclipse.jdt.annotation.NonNull Boolean enableVppTonemapping) {
        this.enableVppTonemapping = enableVppTonemapping;
        return this;
    }

    /**
     * Get enableVppTonemapping
     * 
     * @return enableVppTonemapping
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_VPP_TONEMAPPING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableVppTonemapping() {
        return enableVppTonemapping;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_VPP_TONEMAPPING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableVppTonemapping(@org.eclipse.jdt.annotation.NonNull Boolean enableVppTonemapping) {
        this.enableVppTonemapping = enableVppTonemapping;
    }

    public EncodingOptions tonemappingAlgorithm(@org.eclipse.jdt.annotation.NonNull String tonemappingAlgorithm) {
        this.tonemappingAlgorithm = tonemappingAlgorithm;
        return this;
    }

    /**
     * Get tonemappingAlgorithm
     * 
     * @return tonemappingAlgorithm
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TONEMAPPING_ALGORITHM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTonemappingAlgorithm() {
        return tonemappingAlgorithm;
    }

    @JsonProperty(JSON_PROPERTY_TONEMAPPING_ALGORITHM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingAlgorithm(@org.eclipse.jdt.annotation.NonNull String tonemappingAlgorithm) {
        this.tonemappingAlgorithm = tonemappingAlgorithm;
    }

    public EncodingOptions tonemappingMode(@org.eclipse.jdt.annotation.NonNull String tonemappingMode) {
        this.tonemappingMode = tonemappingMode;
        return this;
    }

    /**
     * Get tonemappingMode
     * 
     * @return tonemappingMode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TONEMAPPING_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTonemappingMode() {
        return tonemappingMode;
    }

    @JsonProperty(JSON_PROPERTY_TONEMAPPING_MODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingMode(@org.eclipse.jdt.annotation.NonNull String tonemappingMode) {
        this.tonemappingMode = tonemappingMode;
    }

    public EncodingOptions tonemappingRange(@org.eclipse.jdt.annotation.NonNull String tonemappingRange) {
        this.tonemappingRange = tonemappingRange;
        return this;
    }

    /**
     * Get tonemappingRange
     * 
     * @return tonemappingRange
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TONEMAPPING_RANGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTonemappingRange() {
        return tonemappingRange;
    }

    @JsonProperty(JSON_PROPERTY_TONEMAPPING_RANGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingRange(@org.eclipse.jdt.annotation.NonNull String tonemappingRange) {
        this.tonemappingRange = tonemappingRange;
    }

    public EncodingOptions tonemappingDesat(@org.eclipse.jdt.annotation.NonNull Double tonemappingDesat) {
        this.tonemappingDesat = tonemappingDesat;
        return this;
    }

    /**
     * Get tonemappingDesat
     * 
     * @return tonemappingDesat
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TONEMAPPING_DESAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getTonemappingDesat() {
        return tonemappingDesat;
    }

    @JsonProperty(JSON_PROPERTY_TONEMAPPING_DESAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingDesat(@org.eclipse.jdt.annotation.NonNull Double tonemappingDesat) {
        this.tonemappingDesat = tonemappingDesat;
    }

    public EncodingOptions tonemappingPeak(@org.eclipse.jdt.annotation.NonNull Double tonemappingPeak) {
        this.tonemappingPeak = tonemappingPeak;
        return this;
    }

    /**
     * Get tonemappingPeak
     * 
     * @return tonemappingPeak
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TONEMAPPING_PEAK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getTonemappingPeak() {
        return tonemappingPeak;
    }

    @JsonProperty(JSON_PROPERTY_TONEMAPPING_PEAK)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingPeak(@org.eclipse.jdt.annotation.NonNull Double tonemappingPeak) {
        this.tonemappingPeak = tonemappingPeak;
    }

    public EncodingOptions tonemappingParam(@org.eclipse.jdt.annotation.NonNull Double tonemappingParam) {
        this.tonemappingParam = tonemappingParam;
        return this;
    }

    /**
     * Get tonemappingParam
     * 
     * @return tonemappingParam
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TONEMAPPING_PARAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getTonemappingParam() {
        return tonemappingParam;
    }

    @JsonProperty(JSON_PROPERTY_TONEMAPPING_PARAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTonemappingParam(@org.eclipse.jdt.annotation.NonNull Double tonemappingParam) {
        this.tonemappingParam = tonemappingParam;
    }

    public EncodingOptions vppTonemappingBrightness(
            @org.eclipse.jdt.annotation.NonNull Double vppTonemappingBrightness) {
        this.vppTonemappingBrightness = vppTonemappingBrightness;
        return this;
    }

    /**
     * Get vppTonemappingBrightness
     * 
     * @return vppTonemappingBrightness
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VPP_TONEMAPPING_BRIGHTNESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getVppTonemappingBrightness() {
        return vppTonemappingBrightness;
    }

    @JsonProperty(JSON_PROPERTY_VPP_TONEMAPPING_BRIGHTNESS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVppTonemappingBrightness(@org.eclipse.jdt.annotation.NonNull Double vppTonemappingBrightness) {
        this.vppTonemappingBrightness = vppTonemappingBrightness;
    }

    public EncodingOptions vppTonemappingContrast(@org.eclipse.jdt.annotation.NonNull Double vppTonemappingContrast) {
        this.vppTonemappingContrast = vppTonemappingContrast;
        return this;
    }

    /**
     * Get vppTonemappingContrast
     * 
     * @return vppTonemappingContrast
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VPP_TONEMAPPING_CONTRAST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getVppTonemappingContrast() {
        return vppTonemappingContrast;
    }

    @JsonProperty(JSON_PROPERTY_VPP_TONEMAPPING_CONTRAST)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVppTonemappingContrast(@org.eclipse.jdt.annotation.NonNull Double vppTonemappingContrast) {
        this.vppTonemappingContrast = vppTonemappingContrast;
    }

    public EncodingOptions h264Crf(@org.eclipse.jdt.annotation.NonNull Integer h264Crf) {
        this.h264Crf = h264Crf;
        return this;
    }

    /**
     * Get h264Crf
     * 
     * @return h264Crf
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_H264_CRF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getH264Crf() {
        return h264Crf;
    }

    @JsonProperty(JSON_PROPERTY_H264_CRF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setH264Crf(@org.eclipse.jdt.annotation.NonNull Integer h264Crf) {
        this.h264Crf = h264Crf;
    }

    public EncodingOptions h265Crf(@org.eclipse.jdt.annotation.NonNull Integer h265Crf) {
        this.h265Crf = h265Crf;
        return this;
    }

    /**
     * Get h265Crf
     * 
     * @return h265Crf
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_H265_CRF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getH265Crf() {
        return h265Crf;
    }

    @JsonProperty(JSON_PROPERTY_H265_CRF)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setH265Crf(@org.eclipse.jdt.annotation.NonNull Integer h265Crf) {
        this.h265Crf = h265Crf;
    }

    public EncodingOptions encoderPreset(@org.eclipse.jdt.annotation.NonNull String encoderPreset) {
        this.encoderPreset = encoderPreset;
        return this;
    }

    /**
     * Get encoderPreset
     * 
     * @return encoderPreset
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENCODER_PRESET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEncoderPreset() {
        return encoderPreset;
    }

    @JsonProperty(JSON_PROPERTY_ENCODER_PRESET)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncoderPreset(@org.eclipse.jdt.annotation.NonNull String encoderPreset) {
        this.encoderPreset = encoderPreset;
    }

    public EncodingOptions deinterlaceDoubleRate(@org.eclipse.jdt.annotation.NonNull Boolean deinterlaceDoubleRate) {
        this.deinterlaceDoubleRate = deinterlaceDoubleRate;
        return this;
    }

    /**
     * Get deinterlaceDoubleRate
     * 
     * @return deinterlaceDoubleRate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEINTERLACE_DOUBLE_RATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getDeinterlaceDoubleRate() {
        return deinterlaceDoubleRate;
    }

    @JsonProperty(JSON_PROPERTY_DEINTERLACE_DOUBLE_RATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeinterlaceDoubleRate(@org.eclipse.jdt.annotation.NonNull Boolean deinterlaceDoubleRate) {
        this.deinterlaceDoubleRate = deinterlaceDoubleRate;
    }

    public EncodingOptions deinterlaceMethod(@org.eclipse.jdt.annotation.NonNull String deinterlaceMethod) {
        this.deinterlaceMethod = deinterlaceMethod;
        return this;
    }

    /**
     * Get deinterlaceMethod
     * 
     * @return deinterlaceMethod
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEINTERLACE_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeinterlaceMethod() {
        return deinterlaceMethod;
    }

    @JsonProperty(JSON_PROPERTY_DEINTERLACE_METHOD)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeinterlaceMethod(@org.eclipse.jdt.annotation.NonNull String deinterlaceMethod) {
        this.deinterlaceMethod = deinterlaceMethod;
    }

    public EncodingOptions enableDecodingColorDepth10Hevc(
            @org.eclipse.jdt.annotation.NonNull Boolean enableDecodingColorDepth10Hevc) {
        this.enableDecodingColorDepth10Hevc = enableDecodingColorDepth10Hevc;
        return this;
    }

    /**
     * Get enableDecodingColorDepth10Hevc
     * 
     * @return enableDecodingColorDepth10Hevc
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableDecodingColorDepth10Hevc() {
        return enableDecodingColorDepth10Hevc;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_HEVC)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableDecodingColorDepth10Hevc(
            @org.eclipse.jdt.annotation.NonNull Boolean enableDecodingColorDepth10Hevc) {
        this.enableDecodingColorDepth10Hevc = enableDecodingColorDepth10Hevc;
    }

    public EncodingOptions enableDecodingColorDepth10Vp9(
            @org.eclipse.jdt.annotation.NonNull Boolean enableDecodingColorDepth10Vp9) {
        this.enableDecodingColorDepth10Vp9 = enableDecodingColorDepth10Vp9;
        return this;
    }

    /**
     * Get enableDecodingColorDepth10Vp9
     * 
     * @return enableDecodingColorDepth10Vp9
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_VP9)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableDecodingColorDepth10Vp9() {
        return enableDecodingColorDepth10Vp9;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_DECODING_COLOR_DEPTH10_VP9)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableDecodingColorDepth10Vp9(
            @org.eclipse.jdt.annotation.NonNull Boolean enableDecodingColorDepth10Vp9) {
        this.enableDecodingColorDepth10Vp9 = enableDecodingColorDepth10Vp9;
    }

    public EncodingOptions enableEnhancedNvdecDecoder(
            @org.eclipse.jdt.annotation.NonNull Boolean enableEnhancedNvdecDecoder) {
        this.enableEnhancedNvdecDecoder = enableEnhancedNvdecDecoder;
        return this;
    }

    /**
     * Get enableEnhancedNvdecDecoder
     * 
     * @return enableEnhancedNvdecDecoder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_ENHANCED_NVDEC_DECODER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableEnhancedNvdecDecoder() {
        return enableEnhancedNvdecDecoder;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_ENHANCED_NVDEC_DECODER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableEnhancedNvdecDecoder(@org.eclipse.jdt.annotation.NonNull Boolean enableEnhancedNvdecDecoder) {
        this.enableEnhancedNvdecDecoder = enableEnhancedNvdecDecoder;
    }

    public EncodingOptions preferSystemNativeHwDecoder(
            @org.eclipse.jdt.annotation.NonNull Boolean preferSystemNativeHwDecoder) {
        this.preferSystemNativeHwDecoder = preferSystemNativeHwDecoder;
        return this;
    }

    /**
     * Get preferSystemNativeHwDecoder
     * 
     * @return preferSystemNativeHwDecoder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PREFER_SYSTEM_NATIVE_HW_DECODER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getPreferSystemNativeHwDecoder() {
        return preferSystemNativeHwDecoder;
    }

    @JsonProperty(JSON_PROPERTY_PREFER_SYSTEM_NATIVE_HW_DECODER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferSystemNativeHwDecoder(
            @org.eclipse.jdt.annotation.NonNull Boolean preferSystemNativeHwDecoder) {
        this.preferSystemNativeHwDecoder = preferSystemNativeHwDecoder;
    }

    public EncodingOptions enableIntelLowPowerH264HwEncoder(
            @org.eclipse.jdt.annotation.NonNull Boolean enableIntelLowPowerH264HwEncoder) {
        this.enableIntelLowPowerH264HwEncoder = enableIntelLowPowerH264HwEncoder;
        return this;
    }

    /**
     * Get enableIntelLowPowerH264HwEncoder
     * 
     * @return enableIntelLowPowerH264HwEncoder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_H264_HW_ENCODER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableIntelLowPowerH264HwEncoder() {
        return enableIntelLowPowerH264HwEncoder;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_H264_HW_ENCODER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableIntelLowPowerH264HwEncoder(
            @org.eclipse.jdt.annotation.NonNull Boolean enableIntelLowPowerH264HwEncoder) {
        this.enableIntelLowPowerH264HwEncoder = enableIntelLowPowerH264HwEncoder;
    }

    public EncodingOptions enableIntelLowPowerHevcHwEncoder(
            @org.eclipse.jdt.annotation.NonNull Boolean enableIntelLowPowerHevcHwEncoder) {
        this.enableIntelLowPowerHevcHwEncoder = enableIntelLowPowerHevcHwEncoder;
        return this;
    }

    /**
     * Get enableIntelLowPowerHevcHwEncoder
     * 
     * @return enableIntelLowPowerHevcHwEncoder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_HEVC_HW_ENCODER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableIntelLowPowerHevcHwEncoder() {
        return enableIntelLowPowerHevcHwEncoder;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_INTEL_LOW_POWER_HEVC_HW_ENCODER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableIntelLowPowerHevcHwEncoder(
            @org.eclipse.jdt.annotation.NonNull Boolean enableIntelLowPowerHevcHwEncoder) {
        this.enableIntelLowPowerHevcHwEncoder = enableIntelLowPowerHevcHwEncoder;
    }

    public EncodingOptions enableHardwareEncoding(@org.eclipse.jdt.annotation.NonNull Boolean enableHardwareEncoding) {
        this.enableHardwareEncoding = enableHardwareEncoding;
        return this;
    }

    /**
     * Get enableHardwareEncoding
     * 
     * @return enableHardwareEncoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_HARDWARE_ENCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableHardwareEncoding() {
        return enableHardwareEncoding;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_HARDWARE_ENCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableHardwareEncoding(@org.eclipse.jdt.annotation.NonNull Boolean enableHardwareEncoding) {
        this.enableHardwareEncoding = enableHardwareEncoding;
    }

    public EncodingOptions allowHevcEncoding(@org.eclipse.jdt.annotation.NonNull Boolean allowHevcEncoding) {
        this.allowHevcEncoding = allowHevcEncoding;
        return this;
    }

    /**
     * Get allowHevcEncoding
     * 
     * @return allowHevcEncoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ALLOW_HEVC_ENCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAllowHevcEncoding() {
        return allowHevcEncoding;
    }

    @JsonProperty(JSON_PROPERTY_ALLOW_HEVC_ENCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowHevcEncoding(@org.eclipse.jdt.annotation.NonNull Boolean allowHevcEncoding) {
        this.allowHevcEncoding = allowHevcEncoding;
    }

    public EncodingOptions enableSubtitleExtraction(
            @org.eclipse.jdt.annotation.NonNull Boolean enableSubtitleExtraction) {
        this.enableSubtitleExtraction = enableSubtitleExtraction;
        return this;
    }

    /**
     * Get enableSubtitleExtraction
     * 
     * @return enableSubtitleExtraction
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_SUBTITLE_EXTRACTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableSubtitleExtraction() {
        return enableSubtitleExtraction;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_SUBTITLE_EXTRACTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSubtitleExtraction(@org.eclipse.jdt.annotation.NonNull Boolean enableSubtitleExtraction) {
        this.enableSubtitleExtraction = enableSubtitleExtraction;
    }

    public EncodingOptions hardwareDecodingCodecs(
            @org.eclipse.jdt.annotation.NonNull List<String> hardwareDecodingCodecs) {
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
     * Get hardwareDecodingCodecs
     * 
     * @return hardwareDecodingCodecs
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HARDWARE_DECODING_CODECS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getHardwareDecodingCodecs() {
        return hardwareDecodingCodecs;
    }

    @JsonProperty(JSON_PROPERTY_HARDWARE_DECODING_CODECS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHardwareDecodingCodecs(@org.eclipse.jdt.annotation.NonNull List<String> hardwareDecodingCodecs) {
        this.hardwareDecodingCodecs = hardwareDecodingCodecs;
    }

    public EncodingOptions allowOnDemandMetadataBasedKeyframeExtractionForExtensions(
            @org.eclipse.jdt.annotation.NonNull List<String> allowOnDemandMetadataBasedKeyframeExtractionForExtensions) {
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
     * Get allowOnDemandMetadataBasedKeyframeExtractionForExtensions
     * 
     * @return allowOnDemandMetadataBasedKeyframeExtractionForExtensions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ALLOW_ON_DEMAND_METADATA_BASED_KEYFRAME_EXTRACTION_FOR_EXTENSIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getAllowOnDemandMetadataBasedKeyframeExtractionForExtensions() {
        return allowOnDemandMetadataBasedKeyframeExtractionForExtensions;
    }

    @JsonProperty(JSON_PROPERTY_ALLOW_ON_DEMAND_METADATA_BASED_KEYFRAME_EXTRACTION_FOR_EXTENSIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowOnDemandMetadataBasedKeyframeExtractionForExtensions(
            @org.eclipse.jdt.annotation.NonNull List<String> allowOnDemandMetadataBasedKeyframeExtractionForExtensions) {
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
                && Objects.equals(this.downMixAudioBoost, encodingOptions.downMixAudioBoost)
                && Objects.equals(this.maxMuxingQueueSize, encodingOptions.maxMuxingQueueSize)
                && Objects.equals(this.enableThrottling, encodingOptions.enableThrottling)
                && Objects.equals(this.throttleDelaySeconds, encodingOptions.throttleDelaySeconds)
                && Objects.equals(this.hardwareAccelerationType, encodingOptions.hardwareAccelerationType)
                && Objects.equals(this.encoderAppPath, encodingOptions.encoderAppPath)
                && Objects.equals(this.encoderAppPathDisplay, encodingOptions.encoderAppPathDisplay)
                && Objects.equals(this.vaapiDevice, encodingOptions.vaapiDevice)
                && Objects.equals(this.enableTonemapping, encodingOptions.enableTonemapping)
                && Objects.equals(this.enableVppTonemapping, encodingOptions.enableVppTonemapping)
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
                && Objects.equals(this.enableEnhancedNvdecDecoder, encodingOptions.enableEnhancedNvdecDecoder)
                && Objects.equals(this.preferSystemNativeHwDecoder, encodingOptions.preferSystemNativeHwDecoder)
                && Objects.equals(this.enableIntelLowPowerH264HwEncoder,
                        encodingOptions.enableIntelLowPowerH264HwEncoder)
                && Objects.equals(this.enableIntelLowPowerHevcHwEncoder,
                        encodingOptions.enableIntelLowPowerHevcHwEncoder)
                && Objects.equals(this.enableHardwareEncoding, encodingOptions.enableHardwareEncoding)
                && Objects.equals(this.allowHevcEncoding, encodingOptions.allowHevcEncoding)
                && Objects.equals(this.enableSubtitleExtraction, encodingOptions.enableSubtitleExtraction)
                && Objects.equals(this.hardwareDecodingCodecs, encodingOptions.hardwareDecodingCodecs)
                && Objects.equals(this.allowOnDemandMetadataBasedKeyframeExtractionForExtensions,
                        encodingOptions.allowOnDemandMetadataBasedKeyframeExtractionForExtensions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(encodingThreadCount, transcodingTempPath, fallbackFontPath, enableFallbackFont,
                downMixAudioBoost, maxMuxingQueueSize, enableThrottling, throttleDelaySeconds, hardwareAccelerationType,
                encoderAppPath, encoderAppPathDisplay, vaapiDevice, enableTonemapping, enableVppTonemapping,
                tonemappingAlgorithm, tonemappingMode, tonemappingRange, tonemappingDesat, tonemappingPeak,
                tonemappingParam, vppTonemappingBrightness, vppTonemappingContrast, h264Crf, h265Crf, encoderPreset,
                deinterlaceDoubleRate, deinterlaceMethod, enableDecodingColorDepth10Hevc, enableDecodingColorDepth10Vp9,
                enableEnhancedNvdecDecoder, preferSystemNativeHwDecoder, enableIntelLowPowerH264HwEncoder,
                enableIntelLowPowerHevcHwEncoder, enableHardwareEncoding, allowHevcEncoding, enableSubtitleExtraction,
                hardwareDecodingCodecs, allowOnDemandMetadataBasedKeyframeExtractionForExtensions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class EncodingOptions {\n");
        sb.append("    encodingThreadCount: ").append(toIndentedString(encodingThreadCount)).append("\n");
        sb.append("    transcodingTempPath: ").append(toIndentedString(transcodingTempPath)).append("\n");
        sb.append("    fallbackFontPath: ").append(toIndentedString(fallbackFontPath)).append("\n");
        sb.append("    enableFallbackFont: ").append(toIndentedString(enableFallbackFont)).append("\n");
        sb.append("    downMixAudioBoost: ").append(toIndentedString(downMixAudioBoost)).append("\n");
        sb.append("    maxMuxingQueueSize: ").append(toIndentedString(maxMuxingQueueSize)).append("\n");
        sb.append("    enableThrottling: ").append(toIndentedString(enableThrottling)).append("\n");
        sb.append("    throttleDelaySeconds: ").append(toIndentedString(throttleDelaySeconds)).append("\n");
        sb.append("    hardwareAccelerationType: ").append(toIndentedString(hardwareAccelerationType)).append("\n");
        sb.append("    encoderAppPath: ").append(toIndentedString(encoderAppPath)).append("\n");
        sb.append("    encoderAppPathDisplay: ").append(toIndentedString(encoderAppPathDisplay)).append("\n");
        sb.append("    vaapiDevice: ").append(toIndentedString(vaapiDevice)).append("\n");
        sb.append("    enableTonemapping: ").append(toIndentedString(enableTonemapping)).append("\n");
        sb.append("    enableVppTonemapping: ").append(toIndentedString(enableVppTonemapping)).append("\n");
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
        sb.append("    enableEnhancedNvdecDecoder: ").append(toIndentedString(enableEnhancedNvdecDecoder)).append("\n");
        sb.append("    preferSystemNativeHwDecoder: ").append(toIndentedString(preferSystemNativeHwDecoder))
                .append("\n");
        sb.append("    enableIntelLowPowerH264HwEncoder: ").append(toIndentedString(enableIntelLowPowerH264HwEncoder))
                .append("\n");
        sb.append("    enableIntelLowPowerHevcHwEncoder: ").append(toIndentedString(enableIntelLowPowerHevcHwEncoder))
                .append("\n");
        sb.append("    enableHardwareEncoding: ").append(toIndentedString(enableHardwareEncoding)).append("\n");
        sb.append("    allowHevcEncoding: ").append(toIndentedString(allowHevcEncoding)).append("\n");
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
            joiner.add(String.format("%sEncodingThreadCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncodingThreadCount()))));
        }

        // add `TranscodingTempPath` to the URL query string
        if (getTranscodingTempPath() != null) {
            joiner.add(String.format("%sTranscodingTempPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTranscodingTempPath()))));
        }

        // add `FallbackFontPath` to the URL query string
        if (getFallbackFontPath() != null) {
            joiner.add(String.format("%sFallbackFontPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFallbackFontPath()))));
        }

        // add `EnableFallbackFont` to the URL query string
        if (getEnableFallbackFont() != null) {
            joiner.add(String.format("%sEnableFallbackFont%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableFallbackFont()))));
        }

        // add `DownMixAudioBoost` to the URL query string
        if (getDownMixAudioBoost() != null) {
            joiner.add(String.format("%sDownMixAudioBoost%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDownMixAudioBoost()))));
        }

        // add `MaxMuxingQueueSize` to the URL query string
        if (getMaxMuxingQueueSize() != null) {
            joiner.add(String.format("%sMaxMuxingQueueSize%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxMuxingQueueSize()))));
        }

        // add `EnableThrottling` to the URL query string
        if (getEnableThrottling() != null) {
            joiner.add(String.format("%sEnableThrottling%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableThrottling()))));
        }

        // add `ThrottleDelaySeconds` to the URL query string
        if (getThrottleDelaySeconds() != null) {
            joiner.add(String.format("%sThrottleDelaySeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getThrottleDelaySeconds()))));
        }

        // add `HardwareAccelerationType` to the URL query string
        if (getHardwareAccelerationType() != null) {
            joiner.add(String.format("%sHardwareAccelerationType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHardwareAccelerationType()))));
        }

        // add `EncoderAppPath` to the URL query string
        if (getEncoderAppPath() != null) {
            joiner.add(String.format("%sEncoderAppPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncoderAppPath()))));
        }

        // add `EncoderAppPathDisplay` to the URL query string
        if (getEncoderAppPathDisplay() != null) {
            joiner.add(String.format("%sEncoderAppPathDisplay%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncoderAppPathDisplay()))));
        }

        // add `VaapiDevice` to the URL query string
        if (getVaapiDevice() != null) {
            joiner.add(String.format("%sVaapiDevice%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVaapiDevice()))));
        }

        // add `EnableTonemapping` to the URL query string
        if (getEnableTonemapping() != null) {
            joiner.add(String.format("%sEnableTonemapping%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableTonemapping()))));
        }

        // add `EnableVppTonemapping` to the URL query string
        if (getEnableVppTonemapping() != null) {
            joiner.add(String.format("%sEnableVppTonemapping%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableVppTonemapping()))));
        }

        // add `TonemappingAlgorithm` to the URL query string
        if (getTonemappingAlgorithm() != null) {
            joiner.add(String.format("%sTonemappingAlgorithm%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingAlgorithm()))));
        }

        // add `TonemappingMode` to the URL query string
        if (getTonemappingMode() != null) {
            joiner.add(String.format("%sTonemappingMode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingMode()))));
        }

        // add `TonemappingRange` to the URL query string
        if (getTonemappingRange() != null) {
            joiner.add(String.format("%sTonemappingRange%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingRange()))));
        }

        // add `TonemappingDesat` to the URL query string
        if (getTonemappingDesat() != null) {
            joiner.add(String.format("%sTonemappingDesat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingDesat()))));
        }

        // add `TonemappingPeak` to the URL query string
        if (getTonemappingPeak() != null) {
            joiner.add(String.format("%sTonemappingPeak%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingPeak()))));
        }

        // add `TonemappingParam` to the URL query string
        if (getTonemappingParam() != null) {
            joiner.add(String.format("%sTonemappingParam%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTonemappingParam()))));
        }

        // add `VppTonemappingBrightness` to the URL query string
        if (getVppTonemappingBrightness() != null) {
            joiner.add(String.format("%sVppTonemappingBrightness%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVppTonemappingBrightness()))));
        }

        // add `VppTonemappingContrast` to the URL query string
        if (getVppTonemappingContrast() != null) {
            joiner.add(String.format("%sVppTonemappingContrast%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVppTonemappingContrast()))));
        }

        // add `H264Crf` to the URL query string
        if (getH264Crf() != null) {
            joiner.add(String.format("%sH264Crf%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getH264Crf()))));
        }

        // add `H265Crf` to the URL query string
        if (getH265Crf() != null) {
            joiner.add(String.format("%sH265Crf%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getH265Crf()))));
        }

        // add `EncoderPreset` to the URL query string
        if (getEncoderPreset() != null) {
            joiner.add(String.format("%sEncoderPreset%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncoderPreset()))));
        }

        // add `DeinterlaceDoubleRate` to the URL query string
        if (getDeinterlaceDoubleRate() != null) {
            joiner.add(String.format("%sDeinterlaceDoubleRate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeinterlaceDoubleRate()))));
        }

        // add `DeinterlaceMethod` to the URL query string
        if (getDeinterlaceMethod() != null) {
            joiner.add(String.format("%sDeinterlaceMethod%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeinterlaceMethod()))));
        }

        // add `EnableDecodingColorDepth10Hevc` to the URL query string
        if (getEnableDecodingColorDepth10Hevc() != null) {
            joiner.add(String.format("%sEnableDecodingColorDepth10Hevc%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableDecodingColorDepth10Hevc()))));
        }

        // add `EnableDecodingColorDepth10Vp9` to the URL query string
        if (getEnableDecodingColorDepth10Vp9() != null) {
            joiner.add(String.format("%sEnableDecodingColorDepth10Vp9%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableDecodingColorDepth10Vp9()))));
        }

        // add `EnableEnhancedNvdecDecoder` to the URL query string
        if (getEnableEnhancedNvdecDecoder() != null) {
            joiner.add(String.format("%sEnableEnhancedNvdecDecoder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableEnhancedNvdecDecoder()))));
        }

        // add `PreferSystemNativeHwDecoder` to the URL query string
        if (getPreferSystemNativeHwDecoder() != null) {
            joiner.add(String.format("%sPreferSystemNativeHwDecoder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreferSystemNativeHwDecoder()))));
        }

        // add `EnableIntelLowPowerH264HwEncoder` to the URL query string
        if (getEnableIntelLowPowerH264HwEncoder() != null) {
            joiner.add(String.format("%sEnableIntelLowPowerH264HwEncoder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableIntelLowPowerH264HwEncoder()))));
        }

        // add `EnableIntelLowPowerHevcHwEncoder` to the URL query string
        if (getEnableIntelLowPowerHevcHwEncoder() != null) {
            joiner.add(String.format("%sEnableIntelLowPowerHevcHwEncoder%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableIntelLowPowerHevcHwEncoder()))));
        }

        // add `EnableHardwareEncoding` to the URL query string
        if (getEnableHardwareEncoding() != null) {
            joiner.add(String.format("%sEnableHardwareEncoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableHardwareEncoding()))));
        }

        // add `AllowHevcEncoding` to the URL query string
        if (getAllowHevcEncoding() != null) {
            joiner.add(String.format("%sAllowHevcEncoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAllowHevcEncoding()))));
        }

        // add `EnableSubtitleExtraction` to the URL query string
        if (getEnableSubtitleExtraction() != null) {
            joiner.add(String.format("%sEnableSubtitleExtraction%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableSubtitleExtraction()))));
        }

        // add `HardwareDecodingCodecs` to the URL query string
        if (getHardwareDecodingCodecs() != null) {
            for (int i = 0; i < getHardwareDecodingCodecs().size(); i++) {
                joiner.add(String.format("%sHardwareDecodingCodecs%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getHardwareDecodingCodecs().get(i)))));
            }
        }

        // add `AllowOnDemandMetadataBasedKeyframeExtractionForExtensions` to the URL query string
        if (getAllowOnDemandMetadataBasedKeyframeExtractionForExtensions() != null) {
            for (int i = 0; i < getAllowOnDemandMetadataBasedKeyframeExtractionForExtensions().size(); i++) {
                joiner.add(String.format("%sAllowOnDemandMetadataBasedKeyframeExtractionForExtensions%s%s=%s", prefix,
                        suffix, "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
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

        public EncodingOptions.Builder downMixAudioBoost(Double downMixAudioBoost) {
            this.instance.downMixAudioBoost = downMixAudioBoost;
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

        public EncodingOptions.Builder hardwareAccelerationType(String hardwareAccelerationType) {
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

        public EncodingOptions.Builder enableTonemapping(Boolean enableTonemapping) {
            this.instance.enableTonemapping = enableTonemapping;
            return this;
        }

        public EncodingOptions.Builder enableVppTonemapping(Boolean enableVppTonemapping) {
            this.instance.enableVppTonemapping = enableVppTonemapping;
            return this;
        }

        public EncodingOptions.Builder tonemappingAlgorithm(String tonemappingAlgorithm) {
            this.instance.tonemappingAlgorithm = tonemappingAlgorithm;
            return this;
        }

        public EncodingOptions.Builder tonemappingMode(String tonemappingMode) {
            this.instance.tonemappingMode = tonemappingMode;
            return this;
        }

        public EncodingOptions.Builder tonemappingRange(String tonemappingRange) {
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

        public EncodingOptions.Builder encoderPreset(String encoderPreset) {
            this.instance.encoderPreset = encoderPreset;
            return this;
        }

        public EncodingOptions.Builder deinterlaceDoubleRate(Boolean deinterlaceDoubleRate) {
            this.instance.deinterlaceDoubleRate = deinterlaceDoubleRate;
            return this;
        }

        public EncodingOptions.Builder deinterlaceMethod(String deinterlaceMethod) {
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
                .enableFallbackFont(getEnableFallbackFont()).downMixAudioBoost(getDownMixAudioBoost())
                .maxMuxingQueueSize(getMaxMuxingQueueSize()).enableThrottling(getEnableThrottling())
                .throttleDelaySeconds(getThrottleDelaySeconds()).hardwareAccelerationType(getHardwareAccelerationType())
                .encoderAppPath(getEncoderAppPath()).encoderAppPathDisplay(getEncoderAppPathDisplay())
                .vaapiDevice(getVaapiDevice()).enableTonemapping(getEnableTonemapping())
                .enableVppTonemapping(getEnableVppTonemapping()).tonemappingAlgorithm(getTonemappingAlgorithm())
                .tonemappingMode(getTonemappingMode()).tonemappingRange(getTonemappingRange())
                .tonemappingDesat(getTonemappingDesat()).tonemappingPeak(getTonemappingPeak())
                .tonemappingParam(getTonemappingParam()).vppTonemappingBrightness(getVppTonemappingBrightness())
                .vppTonemappingContrast(getVppTonemappingContrast()).h264Crf(getH264Crf()).h265Crf(getH265Crf())
                .encoderPreset(getEncoderPreset()).deinterlaceDoubleRate(getDeinterlaceDoubleRate())
                .deinterlaceMethod(getDeinterlaceMethod())
                .enableDecodingColorDepth10Hevc(getEnableDecodingColorDepth10Hevc())
                .enableDecodingColorDepth10Vp9(getEnableDecodingColorDepth10Vp9())
                .enableEnhancedNvdecDecoder(getEnableEnhancedNvdecDecoder())
                .preferSystemNativeHwDecoder(getPreferSystemNativeHwDecoder())
                .enableIntelLowPowerH264HwEncoder(getEnableIntelLowPowerH264HwEncoder())
                .enableIntelLowPowerHevcHwEncoder(getEnableIntelLowPowerHevcHwEncoder())
                .enableHardwareEncoding(getEnableHardwareEncoding()).allowHevcEncoding(getAllowHevcEncoding())
                .enableSubtitleExtraction(getEnableSubtitleExtraction())
                .hardwareDecodingCodecs(getHardwareDecodingCodecs())
                .allowOnDemandMetadataBasedKeyframeExtractionForExtensions(
                        getAllowOnDemandMetadataBasedKeyframeExtractionForExtensions());
    }
}
