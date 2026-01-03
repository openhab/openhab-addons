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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class MediaStream.
 */
@JsonPropertyOrder({ MediaStream.JSON_PROPERTY_CODEC, MediaStream.JSON_PROPERTY_CODEC_TAG,
        MediaStream.JSON_PROPERTY_LANGUAGE, MediaStream.JSON_PROPERTY_COLOR_RANGE,
        MediaStream.JSON_PROPERTY_COLOR_SPACE, MediaStream.JSON_PROPERTY_COLOR_TRANSFER,
        MediaStream.JSON_PROPERTY_COLOR_PRIMARIES, MediaStream.JSON_PROPERTY_DV_VERSION_MAJOR,
        MediaStream.JSON_PROPERTY_DV_VERSION_MINOR, MediaStream.JSON_PROPERTY_DV_PROFILE,
        MediaStream.JSON_PROPERTY_DV_LEVEL, MediaStream.JSON_PROPERTY_RPU_PRESENT_FLAG,
        MediaStream.JSON_PROPERTY_EL_PRESENT_FLAG, MediaStream.JSON_PROPERTY_BL_PRESENT_FLAG,
        MediaStream.JSON_PROPERTY_DV_BL_SIGNAL_COMPATIBILITY_ID, MediaStream.JSON_PROPERTY_ROTATION,
        MediaStream.JSON_PROPERTY_COMMENT, MediaStream.JSON_PROPERTY_TIME_BASE,
        MediaStream.JSON_PROPERTY_CODEC_TIME_BASE, MediaStream.JSON_PROPERTY_TITLE,
        MediaStream.JSON_PROPERTY_HDR10_PLUS_PRESENT_FLAG, MediaStream.JSON_PROPERTY_VIDEO_RANGE,
        MediaStream.JSON_PROPERTY_VIDEO_RANGE_TYPE, MediaStream.JSON_PROPERTY_VIDEO_DO_VI_TITLE,
        MediaStream.JSON_PROPERTY_AUDIO_SPATIAL_FORMAT, MediaStream.JSON_PROPERTY_LOCALIZED_UNDEFINED,
        MediaStream.JSON_PROPERTY_LOCALIZED_DEFAULT, MediaStream.JSON_PROPERTY_LOCALIZED_FORCED,
        MediaStream.JSON_PROPERTY_LOCALIZED_EXTERNAL, MediaStream.JSON_PROPERTY_LOCALIZED_HEARING_IMPAIRED,
        MediaStream.JSON_PROPERTY_DISPLAY_TITLE, MediaStream.JSON_PROPERTY_NAL_LENGTH_SIZE,
        MediaStream.JSON_PROPERTY_IS_INTERLACED, MediaStream.JSON_PROPERTY_IS_A_V_C,
        MediaStream.JSON_PROPERTY_CHANNEL_LAYOUT, MediaStream.JSON_PROPERTY_BIT_RATE,
        MediaStream.JSON_PROPERTY_BIT_DEPTH, MediaStream.JSON_PROPERTY_REF_FRAMES,
        MediaStream.JSON_PROPERTY_PACKET_LENGTH, MediaStream.JSON_PROPERTY_CHANNELS,
        MediaStream.JSON_PROPERTY_SAMPLE_RATE, MediaStream.JSON_PROPERTY_IS_DEFAULT,
        MediaStream.JSON_PROPERTY_IS_FORCED, MediaStream.JSON_PROPERTY_IS_HEARING_IMPAIRED,
        MediaStream.JSON_PROPERTY_HEIGHT, MediaStream.JSON_PROPERTY_WIDTH, MediaStream.JSON_PROPERTY_AVERAGE_FRAME_RATE,
        MediaStream.JSON_PROPERTY_REAL_FRAME_RATE, MediaStream.JSON_PROPERTY_REFERENCE_FRAME_RATE,
        MediaStream.JSON_PROPERTY_PROFILE, MediaStream.JSON_PROPERTY_TYPE, MediaStream.JSON_PROPERTY_ASPECT_RATIO,
        MediaStream.JSON_PROPERTY_INDEX, MediaStream.JSON_PROPERTY_SCORE, MediaStream.JSON_PROPERTY_IS_EXTERNAL,
        MediaStream.JSON_PROPERTY_DELIVERY_METHOD, MediaStream.JSON_PROPERTY_DELIVERY_URL,
        MediaStream.JSON_PROPERTY_IS_EXTERNAL_URL, MediaStream.JSON_PROPERTY_IS_TEXT_SUBTITLE_STREAM,
        MediaStream.JSON_PROPERTY_SUPPORTS_EXTERNAL_STREAM, MediaStream.JSON_PROPERTY_PATH,
        MediaStream.JSON_PROPERTY_PIXEL_FORMAT, MediaStream.JSON_PROPERTY_LEVEL,
        MediaStream.JSON_PROPERTY_IS_ANAMORPHIC })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaStream {
    public static final String JSON_PROPERTY_CODEC = "Codec";
    @org.eclipse.jdt.annotation.Nullable
    private String codec;

    public static final String JSON_PROPERTY_CODEC_TAG = "CodecTag";
    @org.eclipse.jdt.annotation.Nullable
    private String codecTag;

    public static final String JSON_PROPERTY_LANGUAGE = "Language";
    @org.eclipse.jdt.annotation.Nullable
    private String language;

    public static final String JSON_PROPERTY_COLOR_RANGE = "ColorRange";
    @org.eclipse.jdt.annotation.Nullable
    private String colorRange;

    public static final String JSON_PROPERTY_COLOR_SPACE = "ColorSpace";
    @org.eclipse.jdt.annotation.Nullable
    private String colorSpace;

    public static final String JSON_PROPERTY_COLOR_TRANSFER = "ColorTransfer";
    @org.eclipse.jdt.annotation.Nullable
    private String colorTransfer;

    public static final String JSON_PROPERTY_COLOR_PRIMARIES = "ColorPrimaries";
    @org.eclipse.jdt.annotation.Nullable
    private String colorPrimaries;

    public static final String JSON_PROPERTY_DV_VERSION_MAJOR = "DvVersionMajor";
    @org.eclipse.jdt.annotation.Nullable
    private Integer dvVersionMajor;

    public static final String JSON_PROPERTY_DV_VERSION_MINOR = "DvVersionMinor";
    @org.eclipse.jdt.annotation.Nullable
    private Integer dvVersionMinor;

    public static final String JSON_PROPERTY_DV_PROFILE = "DvProfile";
    @org.eclipse.jdt.annotation.Nullable
    private Integer dvProfile;

    public static final String JSON_PROPERTY_DV_LEVEL = "DvLevel";
    @org.eclipse.jdt.annotation.Nullable
    private Integer dvLevel;

    public static final String JSON_PROPERTY_RPU_PRESENT_FLAG = "RpuPresentFlag";
    @org.eclipse.jdt.annotation.Nullable
    private Integer rpuPresentFlag;

    public static final String JSON_PROPERTY_EL_PRESENT_FLAG = "ElPresentFlag";
    @org.eclipse.jdt.annotation.Nullable
    private Integer elPresentFlag;

    public static final String JSON_PROPERTY_BL_PRESENT_FLAG = "BlPresentFlag";
    @org.eclipse.jdt.annotation.Nullable
    private Integer blPresentFlag;

    public static final String JSON_PROPERTY_DV_BL_SIGNAL_COMPATIBILITY_ID = "DvBlSignalCompatibilityId";
    @org.eclipse.jdt.annotation.Nullable
    private Integer dvBlSignalCompatibilityId;

    public static final String JSON_PROPERTY_ROTATION = "Rotation";
    @org.eclipse.jdt.annotation.Nullable
    private Integer rotation;

    public static final String JSON_PROPERTY_COMMENT = "Comment";
    @org.eclipse.jdt.annotation.Nullable
    private String comment;

    public static final String JSON_PROPERTY_TIME_BASE = "TimeBase";
    @org.eclipse.jdt.annotation.Nullable
    private String timeBase;

    public static final String JSON_PROPERTY_CODEC_TIME_BASE = "CodecTimeBase";
    @org.eclipse.jdt.annotation.Nullable
    private String codecTimeBase;

    public static final String JSON_PROPERTY_TITLE = "Title";
    @org.eclipse.jdt.annotation.Nullable
    private String title;

    public static final String JSON_PROPERTY_HDR10_PLUS_PRESENT_FLAG = "Hdr10PlusPresentFlag";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean hdr10PlusPresentFlag;

    public static final String JSON_PROPERTY_VIDEO_RANGE = "VideoRange";
    @org.eclipse.jdt.annotation.Nullable
    private VideoRange videoRange = VideoRange.UNKNOWN;

    public static final String JSON_PROPERTY_VIDEO_RANGE_TYPE = "VideoRangeType";
    @org.eclipse.jdt.annotation.Nullable
    private VideoRangeType videoRangeType = VideoRangeType.UNKNOWN;

    public static final String JSON_PROPERTY_VIDEO_DO_VI_TITLE = "VideoDoViTitle";
    @org.eclipse.jdt.annotation.Nullable
    private String videoDoViTitle;

    public static final String JSON_PROPERTY_AUDIO_SPATIAL_FORMAT = "AudioSpatialFormat";
    @org.eclipse.jdt.annotation.Nullable
    private AudioSpatialFormat audioSpatialFormat = AudioSpatialFormat.NONE;

    public static final String JSON_PROPERTY_LOCALIZED_UNDEFINED = "LocalizedUndefined";
    @org.eclipse.jdt.annotation.Nullable
    private String localizedUndefined;

    public static final String JSON_PROPERTY_LOCALIZED_DEFAULT = "LocalizedDefault";
    @org.eclipse.jdt.annotation.Nullable
    private String localizedDefault;

    public static final String JSON_PROPERTY_LOCALIZED_FORCED = "LocalizedForced";
    @org.eclipse.jdt.annotation.Nullable
    private String localizedForced;

    public static final String JSON_PROPERTY_LOCALIZED_EXTERNAL = "LocalizedExternal";
    @org.eclipse.jdt.annotation.Nullable
    private String localizedExternal;

    public static final String JSON_PROPERTY_LOCALIZED_HEARING_IMPAIRED = "LocalizedHearingImpaired";
    @org.eclipse.jdt.annotation.Nullable
    private String localizedHearingImpaired;

    public static final String JSON_PROPERTY_DISPLAY_TITLE = "DisplayTitle";
    @org.eclipse.jdt.annotation.Nullable
    private String displayTitle;

    public static final String JSON_PROPERTY_NAL_LENGTH_SIZE = "NalLengthSize";
    @org.eclipse.jdt.annotation.Nullable
    private String nalLengthSize;

    public static final String JSON_PROPERTY_IS_INTERLACED = "IsInterlaced";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isInterlaced;

    public static final String JSON_PROPERTY_IS_A_V_C = "IsAVC";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isAVC;

    public static final String JSON_PROPERTY_CHANNEL_LAYOUT = "ChannelLayout";
    @org.eclipse.jdt.annotation.Nullable
    private String channelLayout;

    public static final String JSON_PROPERTY_BIT_RATE = "BitRate";
    @org.eclipse.jdt.annotation.Nullable
    private Integer bitRate;

    public static final String JSON_PROPERTY_BIT_DEPTH = "BitDepth";
    @org.eclipse.jdt.annotation.Nullable
    private Integer bitDepth;

    public static final String JSON_PROPERTY_REF_FRAMES = "RefFrames";
    @org.eclipse.jdt.annotation.Nullable
    private Integer refFrames;

    public static final String JSON_PROPERTY_PACKET_LENGTH = "PacketLength";
    @org.eclipse.jdt.annotation.Nullable
    private Integer packetLength;

    public static final String JSON_PROPERTY_CHANNELS = "Channels";
    @org.eclipse.jdt.annotation.Nullable
    private Integer channels;

    public static final String JSON_PROPERTY_SAMPLE_RATE = "SampleRate";
    @org.eclipse.jdt.annotation.Nullable
    private Integer sampleRate;

    public static final String JSON_PROPERTY_IS_DEFAULT = "IsDefault";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isDefault;

    public static final String JSON_PROPERTY_IS_FORCED = "IsForced";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isForced;

    public static final String JSON_PROPERTY_IS_HEARING_IMPAIRED = "IsHearingImpaired";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isHearingImpaired;

    public static final String JSON_PROPERTY_HEIGHT = "Height";
    @org.eclipse.jdt.annotation.Nullable
    private Integer height;

    public static final String JSON_PROPERTY_WIDTH = "Width";
    @org.eclipse.jdt.annotation.Nullable
    private Integer width;

    public static final String JSON_PROPERTY_AVERAGE_FRAME_RATE = "AverageFrameRate";
    @org.eclipse.jdt.annotation.Nullable
    private Float averageFrameRate;

    public static final String JSON_PROPERTY_REAL_FRAME_RATE = "RealFrameRate";
    @org.eclipse.jdt.annotation.Nullable
    private Float realFrameRate;

    public static final String JSON_PROPERTY_REFERENCE_FRAME_RATE = "ReferenceFrameRate";
    @org.eclipse.jdt.annotation.Nullable
    private Float referenceFrameRate;

    public static final String JSON_PROPERTY_PROFILE = "Profile";
    @org.eclipse.jdt.annotation.Nullable
    private String profile;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.Nullable
    private MediaStreamType type;

    public static final String JSON_PROPERTY_ASPECT_RATIO = "AspectRatio";
    @org.eclipse.jdt.annotation.Nullable
    private String aspectRatio;

    public static final String JSON_PROPERTY_INDEX = "Index";
    @org.eclipse.jdt.annotation.Nullable
    private Integer index;

    public static final String JSON_PROPERTY_SCORE = "Score";
    @org.eclipse.jdt.annotation.Nullable
    private Integer score;

    public static final String JSON_PROPERTY_IS_EXTERNAL = "IsExternal";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isExternal;

    public static final String JSON_PROPERTY_DELIVERY_METHOD = "DeliveryMethod";
    @org.eclipse.jdt.annotation.Nullable
    private SubtitleDeliveryMethod deliveryMethod;

    public static final String JSON_PROPERTY_DELIVERY_URL = "DeliveryUrl";
    @org.eclipse.jdt.annotation.Nullable
    private String deliveryUrl;

    public static final String JSON_PROPERTY_IS_EXTERNAL_URL = "IsExternalUrl";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isExternalUrl;

    public static final String JSON_PROPERTY_IS_TEXT_SUBTITLE_STREAM = "IsTextSubtitleStream";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isTextSubtitleStream;

    public static final String JSON_PROPERTY_SUPPORTS_EXTERNAL_STREAM = "SupportsExternalStream";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean supportsExternalStream;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.Nullable
    private String path;

    public static final String JSON_PROPERTY_PIXEL_FORMAT = "PixelFormat";
    @org.eclipse.jdt.annotation.Nullable
    private String pixelFormat;

    public static final String JSON_PROPERTY_LEVEL = "Level";
    @org.eclipse.jdt.annotation.Nullable
    private Double level;

    public static final String JSON_PROPERTY_IS_ANAMORPHIC = "IsAnamorphic";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isAnamorphic;

    public MediaStream() {
    }

    @JsonCreator
    public MediaStream(@JsonProperty(JSON_PROPERTY_VIDEO_RANGE) VideoRange videoRange,
            @JsonProperty(JSON_PROPERTY_VIDEO_RANGE_TYPE) VideoRangeType videoRangeType,
            @JsonProperty(JSON_PROPERTY_VIDEO_DO_VI_TITLE) String videoDoViTitle,
            @JsonProperty(JSON_PROPERTY_AUDIO_SPATIAL_FORMAT) AudioSpatialFormat audioSpatialFormat,
            @JsonProperty(JSON_PROPERTY_DISPLAY_TITLE) String displayTitle,
            @JsonProperty(JSON_PROPERTY_REFERENCE_FRAME_RATE) Float referenceFrameRate,
            @JsonProperty(JSON_PROPERTY_IS_TEXT_SUBTITLE_STREAM) Boolean isTextSubtitleStream) {
        this();
        this.videoRange = videoRange;
        this.videoRangeType = videoRangeType;
        this.videoDoViTitle = videoDoViTitle;
        this.audioSpatialFormat = audioSpatialFormat;
        this.displayTitle = displayTitle;
        this.referenceFrameRate = referenceFrameRate;
        this.isTextSubtitleStream = isTextSubtitleStream;
    }

    public MediaStream codec(@org.eclipse.jdt.annotation.Nullable String codec) {
        this.codec = codec;
        return this;
    }

    /**
     * Gets or sets the codec.
     * 
     * @return codec
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCodec() {
        return codec;
    }

    @JsonProperty(value = JSON_PROPERTY_CODEC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodec(@org.eclipse.jdt.annotation.Nullable String codec) {
        this.codec = codec;
    }

    public MediaStream codecTag(@org.eclipse.jdt.annotation.Nullable String codecTag) {
        this.codecTag = codecTag;
        return this;
    }

    /**
     * Gets or sets the codec tag.
     * 
     * @return codecTag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CODEC_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCodecTag() {
        return codecTag;
    }

    @JsonProperty(value = JSON_PROPERTY_CODEC_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodecTag(@org.eclipse.jdt.annotation.Nullable String codecTag) {
        this.codecTag = codecTag;
    }

    public MediaStream language(@org.eclipse.jdt.annotation.Nullable String language) {
        this.language = language;
        return this;
    }

    /**
     * Gets or sets the language.
     * 
     * @return language
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLanguage() {
        return language;
    }

    @JsonProperty(value = JSON_PROPERTY_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLanguage(@org.eclipse.jdt.annotation.Nullable String language) {
        this.language = language;
    }

    public MediaStream colorRange(@org.eclipse.jdt.annotation.Nullable String colorRange) {
        this.colorRange = colorRange;
        return this;
    }

    /**
     * Gets or sets the color range.
     * 
     * @return colorRange
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COLOR_RANGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getColorRange() {
        return colorRange;
    }

    @JsonProperty(value = JSON_PROPERTY_COLOR_RANGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setColorRange(@org.eclipse.jdt.annotation.Nullable String colorRange) {
        this.colorRange = colorRange;
    }

    public MediaStream colorSpace(@org.eclipse.jdt.annotation.Nullable String colorSpace) {
        this.colorSpace = colorSpace;
        return this;
    }

    /**
     * Gets or sets the color space.
     * 
     * @return colorSpace
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COLOR_SPACE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getColorSpace() {
        return colorSpace;
    }

    @JsonProperty(value = JSON_PROPERTY_COLOR_SPACE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setColorSpace(@org.eclipse.jdt.annotation.Nullable String colorSpace) {
        this.colorSpace = colorSpace;
    }

    public MediaStream colorTransfer(@org.eclipse.jdt.annotation.Nullable String colorTransfer) {
        this.colorTransfer = colorTransfer;
        return this;
    }

    /**
     * Gets or sets the color transfer.
     * 
     * @return colorTransfer
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COLOR_TRANSFER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getColorTransfer() {
        return colorTransfer;
    }

    @JsonProperty(value = JSON_PROPERTY_COLOR_TRANSFER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setColorTransfer(@org.eclipse.jdt.annotation.Nullable String colorTransfer) {
        this.colorTransfer = colorTransfer;
    }

    public MediaStream colorPrimaries(@org.eclipse.jdt.annotation.Nullable String colorPrimaries) {
        this.colorPrimaries = colorPrimaries;
        return this;
    }

    /**
     * Gets or sets the color primaries.
     * 
     * @return colorPrimaries
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COLOR_PRIMARIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getColorPrimaries() {
        return colorPrimaries;
    }

    @JsonProperty(value = JSON_PROPERTY_COLOR_PRIMARIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setColorPrimaries(@org.eclipse.jdt.annotation.Nullable String colorPrimaries) {
        this.colorPrimaries = colorPrimaries;
    }

    public MediaStream dvVersionMajor(@org.eclipse.jdt.annotation.Nullable Integer dvVersionMajor) {
        this.dvVersionMajor = dvVersionMajor;
        return this;
    }

    /**
     * Gets or sets the Dolby Vision version major.
     * 
     * @return dvVersionMajor
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DV_VERSION_MAJOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getDvVersionMajor() {
        return dvVersionMajor;
    }

    @JsonProperty(value = JSON_PROPERTY_DV_VERSION_MAJOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDvVersionMajor(@org.eclipse.jdt.annotation.Nullable Integer dvVersionMajor) {
        this.dvVersionMajor = dvVersionMajor;
    }

    public MediaStream dvVersionMinor(@org.eclipse.jdt.annotation.Nullable Integer dvVersionMinor) {
        this.dvVersionMinor = dvVersionMinor;
        return this;
    }

    /**
     * Gets or sets the Dolby Vision version minor.
     * 
     * @return dvVersionMinor
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DV_VERSION_MINOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getDvVersionMinor() {
        return dvVersionMinor;
    }

    @JsonProperty(value = JSON_PROPERTY_DV_VERSION_MINOR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDvVersionMinor(@org.eclipse.jdt.annotation.Nullable Integer dvVersionMinor) {
        this.dvVersionMinor = dvVersionMinor;
    }

    public MediaStream dvProfile(@org.eclipse.jdt.annotation.Nullable Integer dvProfile) {
        this.dvProfile = dvProfile;
        return this;
    }

    /**
     * Gets or sets the Dolby Vision profile.
     * 
     * @return dvProfile
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DV_PROFILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getDvProfile() {
        return dvProfile;
    }

    @JsonProperty(value = JSON_PROPERTY_DV_PROFILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDvProfile(@org.eclipse.jdt.annotation.Nullable Integer dvProfile) {
        this.dvProfile = dvProfile;
    }

    public MediaStream dvLevel(@org.eclipse.jdt.annotation.Nullable Integer dvLevel) {
        this.dvLevel = dvLevel;
        return this;
    }

    /**
     * Gets or sets the Dolby Vision level.
     * 
     * @return dvLevel
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DV_LEVEL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getDvLevel() {
        return dvLevel;
    }

    @JsonProperty(value = JSON_PROPERTY_DV_LEVEL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDvLevel(@org.eclipse.jdt.annotation.Nullable Integer dvLevel) {
        this.dvLevel = dvLevel;
    }

    public MediaStream rpuPresentFlag(@org.eclipse.jdt.annotation.Nullable Integer rpuPresentFlag) {
        this.rpuPresentFlag = rpuPresentFlag;
        return this;
    }

    /**
     * Gets or sets the Dolby Vision rpu present flag.
     * 
     * @return rpuPresentFlag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_RPU_PRESENT_FLAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getRpuPresentFlag() {
        return rpuPresentFlag;
    }

    @JsonProperty(value = JSON_PROPERTY_RPU_PRESENT_FLAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRpuPresentFlag(@org.eclipse.jdt.annotation.Nullable Integer rpuPresentFlag) {
        this.rpuPresentFlag = rpuPresentFlag;
    }

    public MediaStream elPresentFlag(@org.eclipse.jdt.annotation.Nullable Integer elPresentFlag) {
        this.elPresentFlag = elPresentFlag;
        return this;
    }

    /**
     * Gets or sets the Dolby Vision el present flag.
     * 
     * @return elPresentFlag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_EL_PRESENT_FLAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getElPresentFlag() {
        return elPresentFlag;
    }

    @JsonProperty(value = JSON_PROPERTY_EL_PRESENT_FLAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setElPresentFlag(@org.eclipse.jdt.annotation.Nullable Integer elPresentFlag) {
        this.elPresentFlag = elPresentFlag;
    }

    public MediaStream blPresentFlag(@org.eclipse.jdt.annotation.Nullable Integer blPresentFlag) {
        this.blPresentFlag = blPresentFlag;
        return this;
    }

    /**
     * Gets or sets the Dolby Vision bl present flag.
     * 
     * @return blPresentFlag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BL_PRESENT_FLAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getBlPresentFlag() {
        return blPresentFlag;
    }

    @JsonProperty(value = JSON_PROPERTY_BL_PRESENT_FLAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBlPresentFlag(@org.eclipse.jdt.annotation.Nullable Integer blPresentFlag) {
        this.blPresentFlag = blPresentFlag;
    }

    public MediaStream dvBlSignalCompatibilityId(
            @org.eclipse.jdt.annotation.Nullable Integer dvBlSignalCompatibilityId) {
        this.dvBlSignalCompatibilityId = dvBlSignalCompatibilityId;
        return this;
    }

    /**
     * Gets or sets the Dolby Vision bl signal compatibility id.
     * 
     * @return dvBlSignalCompatibilityId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DV_BL_SIGNAL_COMPATIBILITY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getDvBlSignalCompatibilityId() {
        return dvBlSignalCompatibilityId;
    }

    @JsonProperty(value = JSON_PROPERTY_DV_BL_SIGNAL_COMPATIBILITY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDvBlSignalCompatibilityId(@org.eclipse.jdt.annotation.Nullable Integer dvBlSignalCompatibilityId) {
        this.dvBlSignalCompatibilityId = dvBlSignalCompatibilityId;
    }

    public MediaStream rotation(@org.eclipse.jdt.annotation.Nullable Integer rotation) {
        this.rotation = rotation;
        return this;
    }

    /**
     * Gets or sets the Rotation in degrees.
     * 
     * @return rotation
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ROTATION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getRotation() {
        return rotation;
    }

    @JsonProperty(value = JSON_PROPERTY_ROTATION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRotation(@org.eclipse.jdt.annotation.Nullable Integer rotation) {
        this.rotation = rotation;
    }

    public MediaStream comment(@org.eclipse.jdt.annotation.Nullable String comment) {
        this.comment = comment;
        return this;
    }

    /**
     * Gets or sets the comment.
     * 
     * @return comment
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COMMENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getComment() {
        return comment;
    }

    @JsonProperty(value = JSON_PROPERTY_COMMENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setComment(@org.eclipse.jdt.annotation.Nullable String comment) {
        this.comment = comment;
    }

    public MediaStream timeBase(@org.eclipse.jdt.annotation.Nullable String timeBase) {
        this.timeBase = timeBase;
        return this;
    }

    /**
     * Gets or sets the time base.
     * 
     * @return timeBase
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TIME_BASE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTimeBase() {
        return timeBase;
    }

    @JsonProperty(value = JSON_PROPERTY_TIME_BASE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimeBase(@org.eclipse.jdt.annotation.Nullable String timeBase) {
        this.timeBase = timeBase;
    }

    public MediaStream codecTimeBase(@org.eclipse.jdt.annotation.Nullable String codecTimeBase) {
        this.codecTimeBase = codecTimeBase;
        return this;
    }

    /**
     * Gets or sets the codec time base.
     * 
     * @return codecTimeBase
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CODEC_TIME_BASE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getCodecTimeBase() {
        return codecTimeBase;
    }

    @JsonProperty(value = JSON_PROPERTY_CODEC_TIME_BASE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodecTimeBase(@org.eclipse.jdt.annotation.Nullable String codecTimeBase) {
        this.codecTimeBase = codecTimeBase;
    }

    public MediaStream title(@org.eclipse.jdt.annotation.Nullable String title) {
        this.title = title;
        return this;
    }

    /**
     * Gets or sets the title.
     * 
     * @return title
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTitle() {
        return title;
    }

    @JsonProperty(value = JSON_PROPERTY_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTitle(@org.eclipse.jdt.annotation.Nullable String title) {
        this.title = title;
    }

    public MediaStream hdr10PlusPresentFlag(@org.eclipse.jdt.annotation.Nullable Boolean hdr10PlusPresentFlag) {
        this.hdr10PlusPresentFlag = hdr10PlusPresentFlag;
        return this;
    }

    /**
     * Get hdr10PlusPresentFlag
     * 
     * @return hdr10PlusPresentFlag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_HDR10_PLUS_PRESENT_FLAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHdr10PlusPresentFlag() {
        return hdr10PlusPresentFlag;
    }

    @JsonProperty(value = JSON_PROPERTY_HDR10_PLUS_PRESENT_FLAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHdr10PlusPresentFlag(@org.eclipse.jdt.annotation.Nullable Boolean hdr10PlusPresentFlag) {
        this.hdr10PlusPresentFlag = hdr10PlusPresentFlag;
    }

    /**
     * Gets the video range.
     * 
     * @return videoRange
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VIDEO_RANGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public VideoRange getVideoRange() {
        return videoRange;
    }

    /**
     * Gets the video range type.
     * 
     * @return videoRangeType
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VIDEO_RANGE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public VideoRangeType getVideoRangeType() {
        return videoRangeType;
    }

    /**
     * Gets the video dovi title.
     * 
     * @return videoDoViTitle
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VIDEO_DO_VI_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getVideoDoViTitle() {
        return videoDoViTitle;
    }

    /**
     * Gets the audio spatial format.
     * 
     * @return audioSpatialFormat
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_AUDIO_SPATIAL_FORMAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public AudioSpatialFormat getAudioSpatialFormat() {
        return audioSpatialFormat;
    }

    public MediaStream localizedUndefined(@org.eclipse.jdt.annotation.Nullable String localizedUndefined) {
        this.localizedUndefined = localizedUndefined;
        return this;
    }

    /**
     * Get localizedUndefined
     * 
     * @return localizedUndefined
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LOCALIZED_UNDEFINED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLocalizedUndefined() {
        return localizedUndefined;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCALIZED_UNDEFINED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalizedUndefined(@org.eclipse.jdt.annotation.Nullable String localizedUndefined) {
        this.localizedUndefined = localizedUndefined;
    }

    public MediaStream localizedDefault(@org.eclipse.jdt.annotation.Nullable String localizedDefault) {
        this.localizedDefault = localizedDefault;
        return this;
    }

    /**
     * Get localizedDefault
     * 
     * @return localizedDefault
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LOCALIZED_DEFAULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLocalizedDefault() {
        return localizedDefault;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCALIZED_DEFAULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalizedDefault(@org.eclipse.jdt.annotation.Nullable String localizedDefault) {
        this.localizedDefault = localizedDefault;
    }

    public MediaStream localizedForced(@org.eclipse.jdt.annotation.Nullable String localizedForced) {
        this.localizedForced = localizedForced;
        return this;
    }

    /**
     * Get localizedForced
     * 
     * @return localizedForced
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LOCALIZED_FORCED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLocalizedForced() {
        return localizedForced;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCALIZED_FORCED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalizedForced(@org.eclipse.jdt.annotation.Nullable String localizedForced) {
        this.localizedForced = localizedForced;
    }

    public MediaStream localizedExternal(@org.eclipse.jdt.annotation.Nullable String localizedExternal) {
        this.localizedExternal = localizedExternal;
        return this;
    }

    /**
     * Get localizedExternal
     * 
     * @return localizedExternal
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LOCALIZED_EXTERNAL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLocalizedExternal() {
        return localizedExternal;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCALIZED_EXTERNAL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalizedExternal(@org.eclipse.jdt.annotation.Nullable String localizedExternal) {
        this.localizedExternal = localizedExternal;
    }

    public MediaStream localizedHearingImpaired(@org.eclipse.jdt.annotation.Nullable String localizedHearingImpaired) {
        this.localizedHearingImpaired = localizedHearingImpaired;
        return this;
    }

    /**
     * Get localizedHearingImpaired
     * 
     * @return localizedHearingImpaired
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LOCALIZED_HEARING_IMPAIRED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLocalizedHearingImpaired() {
        return localizedHearingImpaired;
    }

    @JsonProperty(value = JSON_PROPERTY_LOCALIZED_HEARING_IMPAIRED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalizedHearingImpaired(@org.eclipse.jdt.annotation.Nullable String localizedHearingImpaired) {
        this.localizedHearingImpaired = localizedHearingImpaired;
    }

    /**
     * Get displayTitle
     * 
     * @return displayTitle
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DISPLAY_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDisplayTitle() {
        return displayTitle;
    }

    public MediaStream nalLengthSize(@org.eclipse.jdt.annotation.Nullable String nalLengthSize) {
        this.nalLengthSize = nalLengthSize;
        return this;
    }

    /**
     * Get nalLengthSize
     * 
     * @return nalLengthSize
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NAL_LENGTH_SIZE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getNalLengthSize() {
        return nalLengthSize;
    }

    @JsonProperty(value = JSON_PROPERTY_NAL_LENGTH_SIZE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNalLengthSize(@org.eclipse.jdt.annotation.Nullable String nalLengthSize) {
        this.nalLengthSize = nalLengthSize;
    }

    public MediaStream isInterlaced(@org.eclipse.jdt.annotation.Nullable Boolean isInterlaced) {
        this.isInterlaced = isInterlaced;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is interlaced.
     * 
     * @return isInterlaced
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_INTERLACED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsInterlaced() {
        return isInterlaced;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_INTERLACED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsInterlaced(@org.eclipse.jdt.annotation.Nullable Boolean isInterlaced) {
        this.isInterlaced = isInterlaced;
    }

    public MediaStream isAVC(@org.eclipse.jdt.annotation.Nullable Boolean isAVC) {
        this.isAVC = isAVC;
        return this;
    }

    /**
     * Get isAVC
     * 
     * @return isAVC
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_A_V_C, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsAVC() {
        return isAVC;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_A_V_C, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsAVC(@org.eclipse.jdt.annotation.Nullable Boolean isAVC) {
        this.isAVC = isAVC;
    }

    public MediaStream channelLayout(@org.eclipse.jdt.annotation.Nullable String channelLayout) {
        this.channelLayout = channelLayout;
        return this;
    }

    /**
     * Gets or sets the channel layout.
     * 
     * @return channelLayout
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CHANNEL_LAYOUT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getChannelLayout() {
        return channelLayout;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNEL_LAYOUT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannelLayout(@org.eclipse.jdt.annotation.Nullable String channelLayout) {
        this.channelLayout = channelLayout;
    }

    public MediaStream bitRate(@org.eclipse.jdt.annotation.Nullable Integer bitRate) {
        this.bitRate = bitRate;
        return this;
    }

    /**
     * Gets or sets the bit rate.
     * 
     * @return bitRate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BIT_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getBitRate() {
        return bitRate;
    }

    @JsonProperty(value = JSON_PROPERTY_BIT_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBitRate(@org.eclipse.jdt.annotation.Nullable Integer bitRate) {
        this.bitRate = bitRate;
    }

    public MediaStream bitDepth(@org.eclipse.jdt.annotation.Nullable Integer bitDepth) {
        this.bitDepth = bitDepth;
        return this;
    }

    /**
     * Gets or sets the bit depth.
     * 
     * @return bitDepth
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BIT_DEPTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getBitDepth() {
        return bitDepth;
    }

    @JsonProperty(value = JSON_PROPERTY_BIT_DEPTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBitDepth(@org.eclipse.jdt.annotation.Nullable Integer bitDepth) {
        this.bitDepth = bitDepth;
    }

    public MediaStream refFrames(@org.eclipse.jdt.annotation.Nullable Integer refFrames) {
        this.refFrames = refFrames;
        return this;
    }

    /**
     * Gets or sets the reference frames.
     * 
     * @return refFrames
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REF_FRAMES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getRefFrames() {
        return refFrames;
    }

    @JsonProperty(value = JSON_PROPERTY_REF_FRAMES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRefFrames(@org.eclipse.jdt.annotation.Nullable Integer refFrames) {
        this.refFrames = refFrames;
    }

    public MediaStream packetLength(@org.eclipse.jdt.annotation.Nullable Integer packetLength) {
        this.packetLength = packetLength;
        return this;
    }

    /**
     * Gets or sets the length of the packet.
     * 
     * @return packetLength
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PACKET_LENGTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getPacketLength() {
        return packetLength;
    }

    @JsonProperty(value = JSON_PROPERTY_PACKET_LENGTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPacketLength(@org.eclipse.jdt.annotation.Nullable Integer packetLength) {
        this.packetLength = packetLength;
    }

    public MediaStream channels(@org.eclipse.jdt.annotation.Nullable Integer channels) {
        this.channels = channels;
        return this;
    }

    /**
     * Gets or sets the channels.
     * 
     * @return channels
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getChannels() {
        return channels;
    }

    @JsonProperty(value = JSON_PROPERTY_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setChannels(@org.eclipse.jdt.annotation.Nullable Integer channels) {
        this.channels = channels;
    }

    public MediaStream sampleRate(@org.eclipse.jdt.annotation.Nullable Integer sampleRate) {
        this.sampleRate = sampleRate;
        return this;
    }

    /**
     * Gets or sets the sample rate.
     * 
     * @return sampleRate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SAMPLE_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getSampleRate() {
        return sampleRate;
    }

    @JsonProperty(value = JSON_PROPERTY_SAMPLE_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSampleRate(@org.eclipse.jdt.annotation.Nullable Integer sampleRate) {
        this.sampleRate = sampleRate;
    }

    public MediaStream isDefault(@org.eclipse.jdt.annotation.Nullable Boolean isDefault) {
        this.isDefault = isDefault;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is default.
     * 
     * @return isDefault
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_DEFAULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsDefault() {
        return isDefault;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_DEFAULT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsDefault(@org.eclipse.jdt.annotation.Nullable Boolean isDefault) {
        this.isDefault = isDefault;
    }

    public MediaStream isForced(@org.eclipse.jdt.annotation.Nullable Boolean isForced) {
        this.isForced = isForced;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is forced.
     * 
     * @return isForced
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_FORCED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsForced() {
        return isForced;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_FORCED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsForced(@org.eclipse.jdt.annotation.Nullable Boolean isForced) {
        this.isForced = isForced;
    }

    public MediaStream isHearingImpaired(@org.eclipse.jdt.annotation.Nullable Boolean isHearingImpaired) {
        this.isHearingImpaired = isHearingImpaired;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is for the hearing impaired.
     * 
     * @return isHearingImpaired
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_HEARING_IMPAIRED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsHearingImpaired() {
        return isHearingImpaired;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_HEARING_IMPAIRED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsHearingImpaired(@org.eclipse.jdt.annotation.Nullable Boolean isHearingImpaired) {
        this.isHearingImpaired = isHearingImpaired;
    }

    public MediaStream height(@org.eclipse.jdt.annotation.Nullable Integer height) {
        this.height = height;
        return this;
    }

    /**
     * Gets or sets the height.
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

    public MediaStream width(@org.eclipse.jdt.annotation.Nullable Integer width) {
        this.width = width;
        return this;
    }

    /**
     * Gets or sets the width.
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

    public MediaStream averageFrameRate(@org.eclipse.jdt.annotation.Nullable Float averageFrameRate) {
        this.averageFrameRate = averageFrameRate;
        return this;
    }

    /**
     * Gets or sets the average frame rate.
     * 
     * @return averageFrameRate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_AVERAGE_FRAME_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Float getAverageFrameRate() {
        return averageFrameRate;
    }

    @JsonProperty(value = JSON_PROPERTY_AVERAGE_FRAME_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAverageFrameRate(@org.eclipse.jdt.annotation.Nullable Float averageFrameRate) {
        this.averageFrameRate = averageFrameRate;
    }

    public MediaStream realFrameRate(@org.eclipse.jdt.annotation.Nullable Float realFrameRate) {
        this.realFrameRate = realFrameRate;
        return this;
    }

    /**
     * Gets or sets the real frame rate.
     * 
     * @return realFrameRate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REAL_FRAME_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Float getRealFrameRate() {
        return realFrameRate;
    }

    @JsonProperty(value = JSON_PROPERTY_REAL_FRAME_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRealFrameRate(@org.eclipse.jdt.annotation.Nullable Float realFrameRate) {
        this.realFrameRate = realFrameRate;
    }

    /**
     * Gets the framerate used as reference. Prefer AverageFrameRate, if that is null or an unrealistic value then
     * fallback to RealFrameRate.
     * 
     * @return referenceFrameRate
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_REFERENCE_FRAME_RATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Float getReferenceFrameRate() {
        return referenceFrameRate;
    }

    public MediaStream profile(@org.eclipse.jdt.annotation.Nullable String profile) {
        this.profile = profile;
        return this;
    }

    /**
     * Gets or sets the profile.
     * 
     * @return profile
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROFILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProfile() {
        return profile;
    }

    @JsonProperty(value = JSON_PROPERTY_PROFILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProfile(@org.eclipse.jdt.annotation.Nullable String profile) {
        this.profile = profile;
    }

    public MediaStream type(@org.eclipse.jdt.annotation.Nullable MediaStreamType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaStreamType getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.Nullable MediaStreamType type) {
        this.type = type;
    }

    public MediaStream aspectRatio(@org.eclipse.jdt.annotation.Nullable String aspectRatio) {
        this.aspectRatio = aspectRatio;
        return this;
    }

    /**
     * Gets or sets the aspect ratio.
     * 
     * @return aspectRatio
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAspectRatio() {
        return aspectRatio;
    }

    @JsonProperty(value = JSON_PROPERTY_ASPECT_RATIO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAspectRatio(@org.eclipse.jdt.annotation.Nullable String aspectRatio) {
        this.aspectRatio = aspectRatio;
    }

    public MediaStream index(@org.eclipse.jdt.annotation.Nullable Integer index) {
        this.index = index;
        return this;
    }

    /**
     * Gets or sets the index.
     * 
     * @return index
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getIndex() {
        return index;
    }

    @JsonProperty(value = JSON_PROPERTY_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndex(@org.eclipse.jdt.annotation.Nullable Integer index) {
        this.index = index;
    }

    public MediaStream score(@org.eclipse.jdt.annotation.Nullable Integer score) {
        this.score = score;
        return this;
    }

    /**
     * Gets or sets the score.
     * 
     * @return score
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SCORE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getScore() {
        return score;
    }

    @JsonProperty(value = JSON_PROPERTY_SCORE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setScore(@org.eclipse.jdt.annotation.Nullable Integer score) {
        this.score = score;
    }

    public MediaStream isExternal(@org.eclipse.jdt.annotation.Nullable Boolean isExternal) {
        this.isExternal = isExternal;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is external.
     * 
     * @return isExternal
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_EXTERNAL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsExternal() {
        return isExternal;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_EXTERNAL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsExternal(@org.eclipse.jdt.annotation.Nullable Boolean isExternal) {
        this.isExternal = isExternal;
    }

    public MediaStream deliveryMethod(@org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
        return this;
    }

    /**
     * Gets or sets the method.
     * 
     * @return deliveryMethod
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DELIVERY_METHOD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public SubtitleDeliveryMethod getDeliveryMethod() {
        return deliveryMethod;
    }

    @JsonProperty(value = JSON_PROPERTY_DELIVERY_METHOD, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeliveryMethod(@org.eclipse.jdt.annotation.Nullable SubtitleDeliveryMethod deliveryMethod) {
        this.deliveryMethod = deliveryMethod;
    }

    public MediaStream deliveryUrl(@org.eclipse.jdt.annotation.Nullable String deliveryUrl) {
        this.deliveryUrl = deliveryUrl;
        return this;
    }

    /**
     * Gets or sets the delivery URL.
     * 
     * @return deliveryUrl
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DELIVERY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeliveryUrl() {
        return deliveryUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_DELIVERY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeliveryUrl(@org.eclipse.jdt.annotation.Nullable String deliveryUrl) {
        this.deliveryUrl = deliveryUrl;
    }

    public MediaStream isExternalUrl(@org.eclipse.jdt.annotation.Nullable Boolean isExternalUrl) {
        this.isExternalUrl = isExternalUrl;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance is external URL.
     * 
     * @return isExternalUrl
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_EXTERNAL_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsExternalUrl() {
        return isExternalUrl;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_EXTERNAL_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsExternalUrl(@org.eclipse.jdt.annotation.Nullable Boolean isExternalUrl) {
        this.isExternalUrl = isExternalUrl;
    }

    /**
     * Get isTextSubtitleStream
     * 
     * @return isTextSubtitleStream
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_TEXT_SUBTITLE_STREAM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsTextSubtitleStream() {
        return isTextSubtitleStream;
    }

    public MediaStream supportsExternalStream(@org.eclipse.jdt.annotation.Nullable Boolean supportsExternalStream) {
        this.supportsExternalStream = supportsExternalStream;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [supports external stream].
     * 
     * @return supportsExternalStream
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_EXTERNAL_STREAM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsExternalStream() {
        return supportsExternalStream;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_EXTERNAL_STREAM, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsExternalStream(@org.eclipse.jdt.annotation.Nullable Boolean supportsExternalStream) {
        this.supportsExternalStream = supportsExternalStream;
    }

    public MediaStream path(@org.eclipse.jdt.annotation.Nullable String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the filename.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPath() {
        return path;
    }

    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.Nullable String path) {
        this.path = path;
    }

    public MediaStream pixelFormat(@org.eclipse.jdt.annotation.Nullable String pixelFormat) {
        this.pixelFormat = pixelFormat;
        return this;
    }

    /**
     * Gets or sets the pixel format.
     * 
     * @return pixelFormat
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PIXEL_FORMAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPixelFormat() {
        return pixelFormat;
    }

    @JsonProperty(value = JSON_PROPERTY_PIXEL_FORMAT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPixelFormat(@org.eclipse.jdt.annotation.Nullable String pixelFormat) {
        this.pixelFormat = pixelFormat;
    }

    public MediaStream level(@org.eclipse.jdt.annotation.Nullable Double level) {
        this.level = level;
        return this;
    }

    /**
     * Gets or sets the level.
     * 
     * @return level
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LEVEL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Double getLevel() {
        return level;
    }

    @JsonProperty(value = JSON_PROPERTY_LEVEL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLevel(@org.eclipse.jdt.annotation.Nullable Double level) {
        this.level = level;
    }

    public MediaStream isAnamorphic(@org.eclipse.jdt.annotation.Nullable Boolean isAnamorphic) {
        this.isAnamorphic = isAnamorphic;
        return this;
    }

    /**
     * Gets or sets whether this instance is anamorphic.
     * 
     * @return isAnamorphic
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_ANAMORPHIC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsAnamorphic() {
        return isAnamorphic;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_ANAMORPHIC, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsAnamorphic(@org.eclipse.jdt.annotation.Nullable Boolean isAnamorphic) {
        this.isAnamorphic = isAnamorphic;
    }

    /**
     * Return true if this MediaStream object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaStream mediaStream = (MediaStream) o;
        return Objects.equals(this.codec, mediaStream.codec) && Objects.equals(this.codecTag, mediaStream.codecTag)
                && Objects.equals(this.language, mediaStream.language)
                && Objects.equals(this.colorRange, mediaStream.colorRange)
                && Objects.equals(this.colorSpace, mediaStream.colorSpace)
                && Objects.equals(this.colorTransfer, mediaStream.colorTransfer)
                && Objects.equals(this.colorPrimaries, mediaStream.colorPrimaries)
                && Objects.equals(this.dvVersionMajor, mediaStream.dvVersionMajor)
                && Objects.equals(this.dvVersionMinor, mediaStream.dvVersionMinor)
                && Objects.equals(this.dvProfile, mediaStream.dvProfile)
                && Objects.equals(this.dvLevel, mediaStream.dvLevel)
                && Objects.equals(this.rpuPresentFlag, mediaStream.rpuPresentFlag)
                && Objects.equals(this.elPresentFlag, mediaStream.elPresentFlag)
                && Objects.equals(this.blPresentFlag, mediaStream.blPresentFlag)
                && Objects.equals(this.dvBlSignalCompatibilityId, mediaStream.dvBlSignalCompatibilityId)
                && Objects.equals(this.rotation, mediaStream.rotation)
                && Objects.equals(this.comment, mediaStream.comment)
                && Objects.equals(this.timeBase, mediaStream.timeBase)
                && Objects.equals(this.codecTimeBase, mediaStream.codecTimeBase)
                && Objects.equals(this.title, mediaStream.title)
                && Objects.equals(this.hdr10PlusPresentFlag, mediaStream.hdr10PlusPresentFlag)
                && Objects.equals(this.videoRange, mediaStream.videoRange)
                && Objects.equals(this.videoRangeType, mediaStream.videoRangeType)
                && Objects.equals(this.videoDoViTitle, mediaStream.videoDoViTitle)
                && Objects.equals(this.audioSpatialFormat, mediaStream.audioSpatialFormat)
                && Objects.equals(this.localizedUndefined, mediaStream.localizedUndefined)
                && Objects.equals(this.localizedDefault, mediaStream.localizedDefault)
                && Objects.equals(this.localizedForced, mediaStream.localizedForced)
                && Objects.equals(this.localizedExternal, mediaStream.localizedExternal)
                && Objects.equals(this.localizedHearingImpaired, mediaStream.localizedHearingImpaired)
                && Objects.equals(this.displayTitle, mediaStream.displayTitle)
                && Objects.equals(this.nalLengthSize, mediaStream.nalLengthSize)
                && Objects.equals(this.isInterlaced, mediaStream.isInterlaced)
                && Objects.equals(this.isAVC, mediaStream.isAVC)
                && Objects.equals(this.channelLayout, mediaStream.channelLayout)
                && Objects.equals(this.bitRate, mediaStream.bitRate)
                && Objects.equals(this.bitDepth, mediaStream.bitDepth)
                && Objects.equals(this.refFrames, mediaStream.refFrames)
                && Objects.equals(this.packetLength, mediaStream.packetLength)
                && Objects.equals(this.channels, mediaStream.channels)
                && Objects.equals(this.sampleRate, mediaStream.sampleRate)
                && Objects.equals(this.isDefault, mediaStream.isDefault)
                && Objects.equals(this.isForced, mediaStream.isForced)
                && Objects.equals(this.isHearingImpaired, mediaStream.isHearingImpaired)
                && Objects.equals(this.height, mediaStream.height) && Objects.equals(this.width, mediaStream.width)
                && Objects.equals(this.averageFrameRate, mediaStream.averageFrameRate)
                && Objects.equals(this.realFrameRate, mediaStream.realFrameRate)
                && Objects.equals(this.referenceFrameRate, mediaStream.referenceFrameRate)
                && Objects.equals(this.profile, mediaStream.profile) && Objects.equals(this.type, mediaStream.type)
                && Objects.equals(this.aspectRatio, mediaStream.aspectRatio)
                && Objects.equals(this.index, mediaStream.index) && Objects.equals(this.score, mediaStream.score)
                && Objects.equals(this.isExternal, mediaStream.isExternal)
                && Objects.equals(this.deliveryMethod, mediaStream.deliveryMethod)
                && Objects.equals(this.deliveryUrl, mediaStream.deliveryUrl)
                && Objects.equals(this.isExternalUrl, mediaStream.isExternalUrl)
                && Objects.equals(this.isTextSubtitleStream, mediaStream.isTextSubtitleStream)
                && Objects.equals(this.supportsExternalStream, mediaStream.supportsExternalStream)
                && Objects.equals(this.path, mediaStream.path)
                && Objects.equals(this.pixelFormat, mediaStream.pixelFormat)
                && Objects.equals(this.level, mediaStream.level)
                && Objects.equals(this.isAnamorphic, mediaStream.isAnamorphic);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codec, codecTag, language, colorRange, colorSpace, colorTransfer, colorPrimaries,
                dvVersionMajor, dvVersionMinor, dvProfile, dvLevel, rpuPresentFlag, elPresentFlag, blPresentFlag,
                dvBlSignalCompatibilityId, rotation, comment, timeBase, codecTimeBase, title, hdr10PlusPresentFlag,
                videoRange, videoRangeType, videoDoViTitle, audioSpatialFormat, localizedUndefined, localizedDefault,
                localizedForced, localizedExternal, localizedHearingImpaired, displayTitle, nalLengthSize, isInterlaced,
                isAVC, channelLayout, bitRate, bitDepth, refFrames, packetLength, channels, sampleRate, isDefault,
                isForced, isHearingImpaired, height, width, averageFrameRate, realFrameRate, referenceFrameRate,
                profile, type, aspectRatio, index, score, isExternal, deliveryMethod, deliveryUrl, isExternalUrl,
                isTextSubtitleStream, supportsExternalStream, path, pixelFormat, level, isAnamorphic);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaStream {\n");
        sb.append("    codec: ").append(toIndentedString(codec)).append("\n");
        sb.append("    codecTag: ").append(toIndentedString(codecTag)).append("\n");
        sb.append("    language: ").append(toIndentedString(language)).append("\n");
        sb.append("    colorRange: ").append(toIndentedString(colorRange)).append("\n");
        sb.append("    colorSpace: ").append(toIndentedString(colorSpace)).append("\n");
        sb.append("    colorTransfer: ").append(toIndentedString(colorTransfer)).append("\n");
        sb.append("    colorPrimaries: ").append(toIndentedString(colorPrimaries)).append("\n");
        sb.append("    dvVersionMajor: ").append(toIndentedString(dvVersionMajor)).append("\n");
        sb.append("    dvVersionMinor: ").append(toIndentedString(dvVersionMinor)).append("\n");
        sb.append("    dvProfile: ").append(toIndentedString(dvProfile)).append("\n");
        sb.append("    dvLevel: ").append(toIndentedString(dvLevel)).append("\n");
        sb.append("    rpuPresentFlag: ").append(toIndentedString(rpuPresentFlag)).append("\n");
        sb.append("    elPresentFlag: ").append(toIndentedString(elPresentFlag)).append("\n");
        sb.append("    blPresentFlag: ").append(toIndentedString(blPresentFlag)).append("\n");
        sb.append("    dvBlSignalCompatibilityId: ").append(toIndentedString(dvBlSignalCompatibilityId)).append("\n");
        sb.append("    rotation: ").append(toIndentedString(rotation)).append("\n");
        sb.append("    comment: ").append(toIndentedString(comment)).append("\n");
        sb.append("    timeBase: ").append(toIndentedString(timeBase)).append("\n");
        sb.append("    codecTimeBase: ").append(toIndentedString(codecTimeBase)).append("\n");
        sb.append("    title: ").append(toIndentedString(title)).append("\n");
        sb.append("    hdr10PlusPresentFlag: ").append(toIndentedString(hdr10PlusPresentFlag)).append("\n");
        sb.append("    videoRange: ").append(toIndentedString(videoRange)).append("\n");
        sb.append("    videoRangeType: ").append(toIndentedString(videoRangeType)).append("\n");
        sb.append("    videoDoViTitle: ").append(toIndentedString(videoDoViTitle)).append("\n");
        sb.append("    audioSpatialFormat: ").append(toIndentedString(audioSpatialFormat)).append("\n");
        sb.append("    localizedUndefined: ").append(toIndentedString(localizedUndefined)).append("\n");
        sb.append("    localizedDefault: ").append(toIndentedString(localizedDefault)).append("\n");
        sb.append("    localizedForced: ").append(toIndentedString(localizedForced)).append("\n");
        sb.append("    localizedExternal: ").append(toIndentedString(localizedExternal)).append("\n");
        sb.append("    localizedHearingImpaired: ").append(toIndentedString(localizedHearingImpaired)).append("\n");
        sb.append("    displayTitle: ").append(toIndentedString(displayTitle)).append("\n");
        sb.append("    nalLengthSize: ").append(toIndentedString(nalLengthSize)).append("\n");
        sb.append("    isInterlaced: ").append(toIndentedString(isInterlaced)).append("\n");
        sb.append("    isAVC: ").append(toIndentedString(isAVC)).append("\n");
        sb.append("    channelLayout: ").append(toIndentedString(channelLayout)).append("\n");
        sb.append("    bitRate: ").append(toIndentedString(bitRate)).append("\n");
        sb.append("    bitDepth: ").append(toIndentedString(bitDepth)).append("\n");
        sb.append("    refFrames: ").append(toIndentedString(refFrames)).append("\n");
        sb.append("    packetLength: ").append(toIndentedString(packetLength)).append("\n");
        sb.append("    channels: ").append(toIndentedString(channels)).append("\n");
        sb.append("    sampleRate: ").append(toIndentedString(sampleRate)).append("\n");
        sb.append("    isDefault: ").append(toIndentedString(isDefault)).append("\n");
        sb.append("    isForced: ").append(toIndentedString(isForced)).append("\n");
        sb.append("    isHearingImpaired: ").append(toIndentedString(isHearingImpaired)).append("\n");
        sb.append("    height: ").append(toIndentedString(height)).append("\n");
        sb.append("    width: ").append(toIndentedString(width)).append("\n");
        sb.append("    averageFrameRate: ").append(toIndentedString(averageFrameRate)).append("\n");
        sb.append("    realFrameRate: ").append(toIndentedString(realFrameRate)).append("\n");
        sb.append("    referenceFrameRate: ").append(toIndentedString(referenceFrameRate)).append("\n");
        sb.append("    profile: ").append(toIndentedString(profile)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    aspectRatio: ").append(toIndentedString(aspectRatio)).append("\n");
        sb.append("    index: ").append(toIndentedString(index)).append("\n");
        sb.append("    score: ").append(toIndentedString(score)).append("\n");
        sb.append("    isExternal: ").append(toIndentedString(isExternal)).append("\n");
        sb.append("    deliveryMethod: ").append(toIndentedString(deliveryMethod)).append("\n");
        sb.append("    deliveryUrl: ").append(toIndentedString(deliveryUrl)).append("\n");
        sb.append("    isExternalUrl: ").append(toIndentedString(isExternalUrl)).append("\n");
        sb.append("    isTextSubtitleStream: ").append(toIndentedString(isTextSubtitleStream)).append("\n");
        sb.append("    supportsExternalStream: ").append(toIndentedString(supportsExternalStream)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    pixelFormat: ").append(toIndentedString(pixelFormat)).append("\n");
        sb.append("    level: ").append(toIndentedString(level)).append("\n");
        sb.append("    isAnamorphic: ").append(toIndentedString(isAnamorphic)).append("\n");
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

        // add `Codec` to the URL query string
        if (getCodec() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCodec%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCodec()))));
        }

        // add `CodecTag` to the URL query string
        if (getCodecTag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCodecTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCodecTag()))));
        }

        // add `Language` to the URL query string
        if (getLanguage() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLanguage()))));
        }

        // add `ColorRange` to the URL query string
        if (getColorRange() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sColorRange%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getColorRange()))));
        }

        // add `ColorSpace` to the URL query string
        if (getColorSpace() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sColorSpace%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getColorSpace()))));
        }

        // add `ColorTransfer` to the URL query string
        if (getColorTransfer() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sColorTransfer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getColorTransfer()))));
        }

        // add `ColorPrimaries` to the URL query string
        if (getColorPrimaries() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sColorPrimaries%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getColorPrimaries()))));
        }

        // add `DvVersionMajor` to the URL query string
        if (getDvVersionMajor() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDvVersionMajor%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDvVersionMajor()))));
        }

        // add `DvVersionMinor` to the URL query string
        if (getDvVersionMinor() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDvVersionMinor%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDvVersionMinor()))));
        }

        // add `DvProfile` to the URL query string
        if (getDvProfile() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDvProfile%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDvProfile()))));
        }

        // add `DvLevel` to the URL query string
        if (getDvLevel() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDvLevel%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDvLevel()))));
        }

        // add `RpuPresentFlag` to the URL query string
        if (getRpuPresentFlag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRpuPresentFlag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRpuPresentFlag()))));
        }

        // add `ElPresentFlag` to the URL query string
        if (getElPresentFlag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sElPresentFlag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getElPresentFlag()))));
        }

        // add `BlPresentFlag` to the URL query string
        if (getBlPresentFlag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBlPresentFlag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBlPresentFlag()))));
        }

        // add `DvBlSignalCompatibilityId` to the URL query string
        if (getDvBlSignalCompatibilityId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDvBlSignalCompatibilityId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDvBlSignalCompatibilityId()))));
        }

        // add `Rotation` to the URL query string
        if (getRotation() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRotation%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRotation()))));
        }

        // add `Comment` to the URL query string
        if (getComment() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sComment%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getComment()))));
        }

        // add `TimeBase` to the URL query string
        if (getTimeBase() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTimeBase%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTimeBase()))));
        }

        // add `CodecTimeBase` to the URL query string
        if (getCodecTimeBase() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sCodecTimeBase%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCodecTimeBase()))));
        }

        // add `Title` to the URL query string
        if (getTitle() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTitle%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTitle()))));
        }

        // add `Hdr10PlusPresentFlag` to the URL query string
        if (getHdr10PlusPresentFlag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sHdr10PlusPresentFlag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHdr10PlusPresentFlag()))));
        }

        // add `VideoRange` to the URL query string
        if (getVideoRange() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVideoRange%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideoRange()))));
        }

        // add `VideoRangeType` to the URL query string
        if (getVideoRangeType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVideoRangeType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideoRangeType()))));
        }

        // add `VideoDoViTitle` to the URL query string
        if (getVideoDoViTitle() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sVideoDoViTitle%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideoDoViTitle()))));
        }

        // add `AudioSpatialFormat` to the URL query string
        if (getAudioSpatialFormat() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAudioSpatialFormat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAudioSpatialFormat()))));
        }

        // add `LocalizedUndefined` to the URL query string
        if (getLocalizedUndefined() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLocalizedUndefined%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLocalizedUndefined()))));
        }

        // add `LocalizedDefault` to the URL query string
        if (getLocalizedDefault() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLocalizedDefault%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLocalizedDefault()))));
        }

        // add `LocalizedForced` to the URL query string
        if (getLocalizedForced() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLocalizedForced%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLocalizedForced()))));
        }

        // add `LocalizedExternal` to the URL query string
        if (getLocalizedExternal() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLocalizedExternal%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLocalizedExternal()))));
        }

        // add `LocalizedHearingImpaired` to the URL query string
        if (getLocalizedHearingImpaired() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLocalizedHearingImpaired%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLocalizedHearingImpaired()))));
        }

        // add `DisplayTitle` to the URL query string
        if (getDisplayTitle() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDisplayTitle%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDisplayTitle()))));
        }

        // add `NalLengthSize` to the URL query string
        if (getNalLengthSize() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sNalLengthSize%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getNalLengthSize()))));
        }

        // add `IsInterlaced` to the URL query string
        if (getIsInterlaced() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsInterlaced%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsInterlaced()))));
        }

        // add `IsAVC` to the URL query string
        if (getIsAVC() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsAVC%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsAVC()))));
        }

        // add `ChannelLayout` to the URL query string
        if (getChannelLayout() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sChannelLayout%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannelLayout()))));
        }

        // add `BitRate` to the URL query string
        if (getBitRate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBitRate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBitRate()))));
        }

        // add `BitDepth` to the URL query string
        if (getBitDepth() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBitDepth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBitDepth()))));
        }

        // add `RefFrames` to the URL query string
        if (getRefFrames() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRefFrames%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRefFrames()))));
        }

        // add `PacketLength` to the URL query string
        if (getPacketLength() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPacketLength%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPacketLength()))));
        }

        // add `Channels` to the URL query string
        if (getChannels() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sChannels%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getChannels()))));
        }

        // add `SampleRate` to the URL query string
        if (getSampleRate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSampleRate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSampleRate()))));
        }

        // add `IsDefault` to the URL query string
        if (getIsDefault() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsDefault%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsDefault()))));
        }

        // add `IsForced` to the URL query string
        if (getIsForced() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsForced%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsForced()))));
        }

        // add `IsHearingImpaired` to the URL query string
        if (getIsHearingImpaired() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsHearingImpaired%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsHearingImpaired()))));
        }

        // add `Height` to the URL query string
        if (getHeight() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sHeight%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHeight()))));
        }

        // add `Width` to the URL query string
        if (getWidth() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sWidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getWidth()))));
        }

        // add `AverageFrameRate` to the URL query string
        if (getAverageFrameRate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAverageFrameRate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAverageFrameRate()))));
        }

        // add `RealFrameRate` to the URL query string
        if (getRealFrameRate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sRealFrameRate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRealFrameRate()))));
        }

        // add `ReferenceFrameRate` to the URL query string
        if (getReferenceFrameRate() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sReferenceFrameRate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getReferenceFrameRate()))));
        }

        // add `Profile` to the URL query string
        if (getProfile() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProfile%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProfile()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `AspectRatio` to the URL query string
        if (getAspectRatio() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sAspectRatio%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAspectRatio()))));
        }

        // add `Index` to the URL query string
        if (getIndex() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIndex()))));
        }

        // add `Score` to the URL query string
        if (getScore() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sScore%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getScore()))));
        }

        // add `IsExternal` to the URL query string
        if (getIsExternal() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsExternal%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsExternal()))));
        }

        // add `DeliveryMethod` to the URL query string
        if (getDeliveryMethod() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDeliveryMethod%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeliveryMethod()))));
        }

        // add `DeliveryUrl` to the URL query string
        if (getDeliveryUrl() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDeliveryUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeliveryUrl()))));
        }

        // add `IsExternalUrl` to the URL query string
        if (getIsExternalUrl() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsExternalUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsExternalUrl()))));
        }

        // add `IsTextSubtitleStream` to the URL query string
        if (getIsTextSubtitleStream() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsTextSubtitleStream%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsTextSubtitleStream()))));
        }

        // add `SupportsExternalStream` to the URL query string
        if (getSupportsExternalStream() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSupportsExternalStream%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsExternalStream()))));
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `PixelFormat` to the URL query string
        if (getPixelFormat() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPixelFormat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPixelFormat()))));
        }

        // add `Level` to the URL query string
        if (getLevel() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sLevel%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLevel()))));
        }

        // add `IsAnamorphic` to the URL query string
        if (getIsAnamorphic() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsAnamorphic%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsAnamorphic()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MediaStream instance;

        public Builder() {
            this(new MediaStream());
        }

        protected Builder(MediaStream instance) {
            this.instance = instance;
        }

        public MediaStream.Builder codec(String codec) {
            this.instance.codec = codec;
            return this;
        }

        public MediaStream.Builder codecTag(String codecTag) {
            this.instance.codecTag = codecTag;
            return this;
        }

        public MediaStream.Builder language(String language) {
            this.instance.language = language;
            return this;
        }

        public MediaStream.Builder colorRange(String colorRange) {
            this.instance.colorRange = colorRange;
            return this;
        }

        public MediaStream.Builder colorSpace(String colorSpace) {
            this.instance.colorSpace = colorSpace;
            return this;
        }

        public MediaStream.Builder colorTransfer(String colorTransfer) {
            this.instance.colorTransfer = colorTransfer;
            return this;
        }

        public MediaStream.Builder colorPrimaries(String colorPrimaries) {
            this.instance.colorPrimaries = colorPrimaries;
            return this;
        }

        public MediaStream.Builder dvVersionMajor(Integer dvVersionMajor) {
            this.instance.dvVersionMajor = dvVersionMajor;
            return this;
        }

        public MediaStream.Builder dvVersionMinor(Integer dvVersionMinor) {
            this.instance.dvVersionMinor = dvVersionMinor;
            return this;
        }

        public MediaStream.Builder dvProfile(Integer dvProfile) {
            this.instance.dvProfile = dvProfile;
            return this;
        }

        public MediaStream.Builder dvLevel(Integer dvLevel) {
            this.instance.dvLevel = dvLevel;
            return this;
        }

        public MediaStream.Builder rpuPresentFlag(Integer rpuPresentFlag) {
            this.instance.rpuPresentFlag = rpuPresentFlag;
            return this;
        }

        public MediaStream.Builder elPresentFlag(Integer elPresentFlag) {
            this.instance.elPresentFlag = elPresentFlag;
            return this;
        }

        public MediaStream.Builder blPresentFlag(Integer blPresentFlag) {
            this.instance.blPresentFlag = blPresentFlag;
            return this;
        }

        public MediaStream.Builder dvBlSignalCompatibilityId(Integer dvBlSignalCompatibilityId) {
            this.instance.dvBlSignalCompatibilityId = dvBlSignalCompatibilityId;
            return this;
        }

        public MediaStream.Builder rotation(Integer rotation) {
            this.instance.rotation = rotation;
            return this;
        }

        public MediaStream.Builder comment(String comment) {
            this.instance.comment = comment;
            return this;
        }

        public MediaStream.Builder timeBase(String timeBase) {
            this.instance.timeBase = timeBase;
            return this;
        }

        public MediaStream.Builder codecTimeBase(String codecTimeBase) {
            this.instance.codecTimeBase = codecTimeBase;
            return this;
        }

        public MediaStream.Builder title(String title) {
            this.instance.title = title;
            return this;
        }

        public MediaStream.Builder hdr10PlusPresentFlag(Boolean hdr10PlusPresentFlag) {
            this.instance.hdr10PlusPresentFlag = hdr10PlusPresentFlag;
            return this;
        }

        public MediaStream.Builder videoRange(VideoRange videoRange) {
            this.instance.videoRange = videoRange;
            return this;
        }

        public MediaStream.Builder videoRangeType(VideoRangeType videoRangeType) {
            this.instance.videoRangeType = videoRangeType;
            return this;
        }

        public MediaStream.Builder videoDoViTitle(String videoDoViTitle) {
            this.instance.videoDoViTitle = videoDoViTitle;
            return this;
        }

        public MediaStream.Builder audioSpatialFormat(AudioSpatialFormat audioSpatialFormat) {
            this.instance.audioSpatialFormat = audioSpatialFormat;
            return this;
        }

        public MediaStream.Builder localizedUndefined(String localizedUndefined) {
            this.instance.localizedUndefined = localizedUndefined;
            return this;
        }

        public MediaStream.Builder localizedDefault(String localizedDefault) {
            this.instance.localizedDefault = localizedDefault;
            return this;
        }

        public MediaStream.Builder localizedForced(String localizedForced) {
            this.instance.localizedForced = localizedForced;
            return this;
        }

        public MediaStream.Builder localizedExternal(String localizedExternal) {
            this.instance.localizedExternal = localizedExternal;
            return this;
        }

        public MediaStream.Builder localizedHearingImpaired(String localizedHearingImpaired) {
            this.instance.localizedHearingImpaired = localizedHearingImpaired;
            return this;
        }

        public MediaStream.Builder displayTitle(String displayTitle) {
            this.instance.displayTitle = displayTitle;
            return this;
        }

        public MediaStream.Builder nalLengthSize(String nalLengthSize) {
            this.instance.nalLengthSize = nalLengthSize;
            return this;
        }

        public MediaStream.Builder isInterlaced(Boolean isInterlaced) {
            this.instance.isInterlaced = isInterlaced;
            return this;
        }

        public MediaStream.Builder isAVC(Boolean isAVC) {
            this.instance.isAVC = isAVC;
            return this;
        }

        public MediaStream.Builder channelLayout(String channelLayout) {
            this.instance.channelLayout = channelLayout;
            return this;
        }

        public MediaStream.Builder bitRate(Integer bitRate) {
            this.instance.bitRate = bitRate;
            return this;
        }

        public MediaStream.Builder bitDepth(Integer bitDepth) {
            this.instance.bitDepth = bitDepth;
            return this;
        }

        public MediaStream.Builder refFrames(Integer refFrames) {
            this.instance.refFrames = refFrames;
            return this;
        }

        public MediaStream.Builder packetLength(Integer packetLength) {
            this.instance.packetLength = packetLength;
            return this;
        }

        public MediaStream.Builder channels(Integer channels) {
            this.instance.channels = channels;
            return this;
        }

        public MediaStream.Builder sampleRate(Integer sampleRate) {
            this.instance.sampleRate = sampleRate;
            return this;
        }

        public MediaStream.Builder isDefault(Boolean isDefault) {
            this.instance.isDefault = isDefault;
            return this;
        }

        public MediaStream.Builder isForced(Boolean isForced) {
            this.instance.isForced = isForced;
            return this;
        }

        public MediaStream.Builder isHearingImpaired(Boolean isHearingImpaired) {
            this.instance.isHearingImpaired = isHearingImpaired;
            return this;
        }

        public MediaStream.Builder height(Integer height) {
            this.instance.height = height;
            return this;
        }

        public MediaStream.Builder width(Integer width) {
            this.instance.width = width;
            return this;
        }

        public MediaStream.Builder averageFrameRate(Float averageFrameRate) {
            this.instance.averageFrameRate = averageFrameRate;
            return this;
        }

        public MediaStream.Builder realFrameRate(Float realFrameRate) {
            this.instance.realFrameRate = realFrameRate;
            return this;
        }

        public MediaStream.Builder referenceFrameRate(Float referenceFrameRate) {
            this.instance.referenceFrameRate = referenceFrameRate;
            return this;
        }

        public MediaStream.Builder profile(String profile) {
            this.instance.profile = profile;
            return this;
        }

        public MediaStream.Builder type(MediaStreamType type) {
            this.instance.type = type;
            return this;
        }

        public MediaStream.Builder aspectRatio(String aspectRatio) {
            this.instance.aspectRatio = aspectRatio;
            return this;
        }

        public MediaStream.Builder index(Integer index) {
            this.instance.index = index;
            return this;
        }

        public MediaStream.Builder score(Integer score) {
            this.instance.score = score;
            return this;
        }

        public MediaStream.Builder isExternal(Boolean isExternal) {
            this.instance.isExternal = isExternal;
            return this;
        }

        public MediaStream.Builder deliveryMethod(SubtitleDeliveryMethod deliveryMethod) {
            this.instance.deliveryMethod = deliveryMethod;
            return this;
        }

        public MediaStream.Builder deliveryUrl(String deliveryUrl) {
            this.instance.deliveryUrl = deliveryUrl;
            return this;
        }

        public MediaStream.Builder isExternalUrl(Boolean isExternalUrl) {
            this.instance.isExternalUrl = isExternalUrl;
            return this;
        }

        public MediaStream.Builder isTextSubtitleStream(Boolean isTextSubtitleStream) {
            this.instance.isTextSubtitleStream = isTextSubtitleStream;
            return this;
        }

        public MediaStream.Builder supportsExternalStream(Boolean supportsExternalStream) {
            this.instance.supportsExternalStream = supportsExternalStream;
            return this;
        }

        public MediaStream.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public MediaStream.Builder pixelFormat(String pixelFormat) {
            this.instance.pixelFormat = pixelFormat;
            return this;
        }

        public MediaStream.Builder level(Double level) {
            this.instance.level = level;
            return this;
        }

        public MediaStream.Builder isAnamorphic(Boolean isAnamorphic) {
            this.instance.isAnamorphic = isAnamorphic;
            return this;
        }

        /**
         * returns a built MediaStream instance.
         *
         * The builder is not reusable.
         */
        public MediaStream build() {
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
    public static MediaStream.Builder builder() {
        return new MediaStream.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MediaStream.Builder toBuilder() {
        return new MediaStream.Builder().codec(getCodec()).codecTag(getCodecTag()).language(getLanguage())
                .colorRange(getColorRange()).colorSpace(getColorSpace()).colorTransfer(getColorTransfer())
                .colorPrimaries(getColorPrimaries()).dvVersionMajor(getDvVersionMajor())
                .dvVersionMinor(getDvVersionMinor()).dvProfile(getDvProfile()).dvLevel(getDvLevel())
                .rpuPresentFlag(getRpuPresentFlag()).elPresentFlag(getElPresentFlag()).blPresentFlag(getBlPresentFlag())
                .dvBlSignalCompatibilityId(getDvBlSignalCompatibilityId()).rotation(getRotation()).comment(getComment())
                .timeBase(getTimeBase()).codecTimeBase(getCodecTimeBase()).title(getTitle())
                .hdr10PlusPresentFlag(getHdr10PlusPresentFlag()).videoRange(getVideoRange())
                .videoRangeType(getVideoRangeType()).videoDoViTitle(getVideoDoViTitle())
                .audioSpatialFormat(getAudioSpatialFormat()).localizedUndefined(getLocalizedUndefined())
                .localizedDefault(getLocalizedDefault()).localizedForced(getLocalizedForced())
                .localizedExternal(getLocalizedExternal()).localizedHearingImpaired(getLocalizedHearingImpaired())
                .displayTitle(getDisplayTitle()).nalLengthSize(getNalLengthSize()).isInterlaced(getIsInterlaced())
                .isAVC(getIsAVC()).channelLayout(getChannelLayout()).bitRate(getBitRate()).bitDepth(getBitDepth())
                .refFrames(getRefFrames()).packetLength(getPacketLength()).channels(getChannels())
                .sampleRate(getSampleRate()).isDefault(getIsDefault()).isForced(getIsForced())
                .isHearingImpaired(getIsHearingImpaired()).height(getHeight()).width(getWidth())
                .averageFrameRate(getAverageFrameRate()).realFrameRate(getRealFrameRate())
                .referenceFrameRate(getReferenceFrameRate()).profile(getProfile()).type(getType())
                .aspectRatio(getAspectRatio()).index(getIndex()).score(getScore()).isExternal(getIsExternal())
                .deliveryMethod(getDeliveryMethod()).deliveryUrl(getDeliveryUrl()).isExternalUrl(getIsExternalUrl())
                .isTextSubtitleStream(getIsTextSubtitleStream()).supportsExternalStream(getSupportsExternalStream())
                .path(getPath()).pixelFormat(getPixelFormat()).level(getLevel()).isAnamorphic(getIsAnamorphic());
    }
}
