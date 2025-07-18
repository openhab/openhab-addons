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
 * A MediaBrowser.Model.Dlna.DeviceProfile represents a set of metadata which determines which content a certain device
 * is able to play. &lt;br /&gt; Specifically, it defines the supported &lt;see
 * cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.ContainerProfiles\&quot;&gt;containers&lt;/see&gt; and
 * &lt;see cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.CodecProfiles\&quot;&gt;codecs&lt;/see&gt; (video
 * and/or audio, including codec profiles and levels) the device is able to direct play (without transcoding or
 * remuxing), as well as which &lt;see
 * cref&#x3D;\&quot;P:MediaBrowser.Model.Dlna.DeviceProfile.TranscodingProfiles\&quot;&gt;containers/codecs to transcode
 * to&lt;/see&gt; in case it isn&#39;t.
 */
@JsonPropertyOrder({ DeviceProfile.JSON_PROPERTY_NAME, DeviceProfile.JSON_PROPERTY_ID,
        DeviceProfile.JSON_PROPERTY_IDENTIFICATION, DeviceProfile.JSON_PROPERTY_FRIENDLY_NAME,
        DeviceProfile.JSON_PROPERTY_MANUFACTURER, DeviceProfile.JSON_PROPERTY_MANUFACTURER_URL,
        DeviceProfile.JSON_PROPERTY_MODEL_NAME, DeviceProfile.JSON_PROPERTY_MODEL_DESCRIPTION,
        DeviceProfile.JSON_PROPERTY_MODEL_NUMBER, DeviceProfile.JSON_PROPERTY_MODEL_URL,
        DeviceProfile.JSON_PROPERTY_SERIAL_NUMBER, DeviceProfile.JSON_PROPERTY_ENABLE_ALBUM_ART_IN_DIDL,
        DeviceProfile.JSON_PROPERTY_ENABLE_SINGLE_ALBUM_ART_LIMIT,
        DeviceProfile.JSON_PROPERTY_ENABLE_SINGLE_SUBTITLE_LIMIT, DeviceProfile.JSON_PROPERTY_SUPPORTED_MEDIA_TYPES,
        DeviceProfile.JSON_PROPERTY_USER_ID, DeviceProfile.JSON_PROPERTY_ALBUM_ART_PN,
        DeviceProfile.JSON_PROPERTY_MAX_ALBUM_ART_WIDTH, DeviceProfile.JSON_PROPERTY_MAX_ALBUM_ART_HEIGHT,
        DeviceProfile.JSON_PROPERTY_MAX_ICON_WIDTH, DeviceProfile.JSON_PROPERTY_MAX_ICON_HEIGHT,
        DeviceProfile.JSON_PROPERTY_MAX_STREAMING_BITRATE, DeviceProfile.JSON_PROPERTY_MAX_STATIC_BITRATE,
        DeviceProfile.JSON_PROPERTY_MUSIC_STREAMING_TRANSCODING_BITRATE,
        DeviceProfile.JSON_PROPERTY_MAX_STATIC_MUSIC_BITRATE, DeviceProfile.JSON_PROPERTY_SONY_AGGREGATION_FLAGS,
        DeviceProfile.JSON_PROPERTY_PROTOCOL_INFO, DeviceProfile.JSON_PROPERTY_TIMELINE_OFFSET_SECONDS,
        DeviceProfile.JSON_PROPERTY_REQUIRES_PLAIN_VIDEO_ITEMS, DeviceProfile.JSON_PROPERTY_REQUIRES_PLAIN_FOLDERS,
        DeviceProfile.JSON_PROPERTY_ENABLE_M_S_MEDIA_RECEIVER_REGISTRAR,
        DeviceProfile.JSON_PROPERTY_IGNORE_TRANSCODE_BYTE_RANGE_REQUESTS,
        DeviceProfile.JSON_PROPERTY_XML_ROOT_ATTRIBUTES, DeviceProfile.JSON_PROPERTY_DIRECT_PLAY_PROFILES,
        DeviceProfile.JSON_PROPERTY_TRANSCODING_PROFILES, DeviceProfile.JSON_PROPERTY_CONTAINER_PROFILES,
        DeviceProfile.JSON_PROPERTY_CODEC_PROFILES, DeviceProfile.JSON_PROPERTY_RESPONSE_PROFILES,
        DeviceProfile.JSON_PROPERTY_SUBTITLE_PROFILES })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class DeviceProfile {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_IDENTIFICATION = "Identification";
    @org.eclipse.jdt.annotation.NonNull
    private DeviceIdentification identification;

    public static final String JSON_PROPERTY_FRIENDLY_NAME = "FriendlyName";
    @org.eclipse.jdt.annotation.NonNull
    private String friendlyName;

    public static final String JSON_PROPERTY_MANUFACTURER = "Manufacturer";
    @org.eclipse.jdt.annotation.NonNull
    private String manufacturer;

    public static final String JSON_PROPERTY_MANUFACTURER_URL = "ManufacturerUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String manufacturerUrl;

    public static final String JSON_PROPERTY_MODEL_NAME = "ModelName";
    @org.eclipse.jdt.annotation.NonNull
    private String modelName;

    public static final String JSON_PROPERTY_MODEL_DESCRIPTION = "ModelDescription";
    @org.eclipse.jdt.annotation.NonNull
    private String modelDescription;

    public static final String JSON_PROPERTY_MODEL_NUMBER = "ModelNumber";
    @org.eclipse.jdt.annotation.NonNull
    private String modelNumber;

    public static final String JSON_PROPERTY_MODEL_URL = "ModelUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String modelUrl;

    public static final String JSON_PROPERTY_SERIAL_NUMBER = "SerialNumber";
    @org.eclipse.jdt.annotation.NonNull
    private String serialNumber;

    public static final String JSON_PROPERTY_ENABLE_ALBUM_ART_IN_DIDL = "EnableAlbumArtInDidl";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableAlbumArtInDidl = false;

    public static final String JSON_PROPERTY_ENABLE_SINGLE_ALBUM_ART_LIMIT = "EnableSingleAlbumArtLimit";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableSingleAlbumArtLimit = false;

    public static final String JSON_PROPERTY_ENABLE_SINGLE_SUBTITLE_LIMIT = "EnableSingleSubtitleLimit";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableSingleSubtitleLimit = false;

    public static final String JSON_PROPERTY_SUPPORTED_MEDIA_TYPES = "SupportedMediaTypes";
    @org.eclipse.jdt.annotation.NonNull
    private String supportedMediaTypes;

    public static final String JSON_PROPERTY_USER_ID = "UserId";
    @org.eclipse.jdt.annotation.NonNull
    private String userId;

    public static final String JSON_PROPERTY_ALBUM_ART_PN = "AlbumArtPn";
    @org.eclipse.jdt.annotation.NonNull
    private String albumArtPn;

    public static final String JSON_PROPERTY_MAX_ALBUM_ART_WIDTH = "MaxAlbumArtWidth";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxAlbumArtWidth;

    public static final String JSON_PROPERTY_MAX_ALBUM_ART_HEIGHT = "MaxAlbumArtHeight";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxAlbumArtHeight;

    public static final String JSON_PROPERTY_MAX_ICON_WIDTH = "MaxIconWidth";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxIconWidth;

    public static final String JSON_PROPERTY_MAX_ICON_HEIGHT = "MaxIconHeight";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxIconHeight;

    public static final String JSON_PROPERTY_MAX_STREAMING_BITRATE = "MaxStreamingBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxStreamingBitrate;

    public static final String JSON_PROPERTY_MAX_STATIC_BITRATE = "MaxStaticBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxStaticBitrate;

    public static final String JSON_PROPERTY_MUSIC_STREAMING_TRANSCODING_BITRATE = "MusicStreamingTranscodingBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer musicStreamingTranscodingBitrate;

    public static final String JSON_PROPERTY_MAX_STATIC_MUSIC_BITRATE = "MaxStaticMusicBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxStaticMusicBitrate;

    public static final String JSON_PROPERTY_SONY_AGGREGATION_FLAGS = "SonyAggregationFlags";
    @org.eclipse.jdt.annotation.NonNull
    private String sonyAggregationFlags;

    public static final String JSON_PROPERTY_PROTOCOL_INFO = "ProtocolInfo";
    @org.eclipse.jdt.annotation.NonNull
    private String protocolInfo;

    public static final String JSON_PROPERTY_TIMELINE_OFFSET_SECONDS = "TimelineOffsetSeconds";
    @org.eclipse.jdt.annotation.NonNull
    private Integer timelineOffsetSeconds = 0;

    public static final String JSON_PROPERTY_REQUIRES_PLAIN_VIDEO_ITEMS = "RequiresPlainVideoItems";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean requiresPlainVideoItems = false;

    public static final String JSON_PROPERTY_REQUIRES_PLAIN_FOLDERS = "RequiresPlainFolders";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean requiresPlainFolders = false;

    public static final String JSON_PROPERTY_ENABLE_M_S_MEDIA_RECEIVER_REGISTRAR = "EnableMSMediaReceiverRegistrar";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableMSMediaReceiverRegistrar = false;

    public static final String JSON_PROPERTY_IGNORE_TRANSCODE_BYTE_RANGE_REQUESTS = "IgnoreTranscodeByteRangeRequests";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean ignoreTranscodeByteRangeRequests = false;

    public static final String JSON_PROPERTY_XML_ROOT_ATTRIBUTES = "XmlRootAttributes";
    @org.eclipse.jdt.annotation.NonNull
    private List<XmlAttribute> xmlRootAttributes = new ArrayList<>();

    public static final String JSON_PROPERTY_DIRECT_PLAY_PROFILES = "DirectPlayProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<DirectPlayProfile> directPlayProfiles = new ArrayList<>();

    public static final String JSON_PROPERTY_TRANSCODING_PROFILES = "TranscodingProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<TranscodingProfile> transcodingProfiles = new ArrayList<>();

    public static final String JSON_PROPERTY_CONTAINER_PROFILES = "ContainerProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<ContainerProfile> containerProfiles = new ArrayList<>();

    public static final String JSON_PROPERTY_CODEC_PROFILES = "CodecProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<CodecProfile> codecProfiles = new ArrayList<>();

    public static final String JSON_PROPERTY_RESPONSE_PROFILES = "ResponseProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<ResponseProfile> responseProfiles = new ArrayList<>();

    public static final String JSON_PROPERTY_SUBTITLE_PROFILES = "SubtitleProfiles";
    @org.eclipse.jdt.annotation.NonNull
    private List<SubtitleProfile> subtitleProfiles = new ArrayList<>();

    public DeviceProfile() {
    }

    public DeviceProfile name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name of this device profile.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public DeviceProfile id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the Id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public DeviceProfile identification(@org.eclipse.jdt.annotation.NonNull DeviceIdentification identification) {
        this.identification = identification;
        return this;
    }

    /**
     * Gets or sets the Identification.
     * 
     * @return identification
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IDENTIFICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public DeviceIdentification getIdentification() {
        return identification;
    }

    @JsonProperty(JSON_PROPERTY_IDENTIFICATION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIdentification(@org.eclipse.jdt.annotation.NonNull DeviceIdentification identification) {
        this.identification = identification;
    }

    public DeviceProfile friendlyName(@org.eclipse.jdt.annotation.NonNull String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    /**
     * Gets or sets the friendly name of the device profile, which can be shown to users.
     * 
     * @return friendlyName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FRIENDLY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getFriendlyName() {
        return friendlyName;
    }

    @JsonProperty(JSON_PROPERTY_FRIENDLY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFriendlyName(@org.eclipse.jdt.annotation.NonNull String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public DeviceProfile manufacturer(@org.eclipse.jdt.annotation.NonNull String manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    /**
     * Gets or sets the manufacturer of the device which this profile represents.
     * 
     * @return manufacturer
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MANUFACTURER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getManufacturer() {
        return manufacturer;
    }

    @JsonProperty(JSON_PROPERTY_MANUFACTURER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setManufacturer(@org.eclipse.jdt.annotation.NonNull String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public DeviceProfile manufacturerUrl(@org.eclipse.jdt.annotation.NonNull String manufacturerUrl) {
        this.manufacturerUrl = manufacturerUrl;
        return this;
    }

    /**
     * Gets or sets an url for the manufacturer of the device which this profile represents.
     * 
     * @return manufacturerUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MANUFACTURER_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getManufacturerUrl() {
        return manufacturerUrl;
    }

    @JsonProperty(JSON_PROPERTY_MANUFACTURER_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setManufacturerUrl(@org.eclipse.jdt.annotation.NonNull String manufacturerUrl) {
        this.manufacturerUrl = manufacturerUrl;
    }

    public DeviceProfile modelName(@org.eclipse.jdt.annotation.NonNull String modelName) {
        this.modelName = modelName;
        return this;
    }

    /**
     * Gets or sets the model name of the device which this profile represents.
     * 
     * @return modelName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MODEL_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getModelName() {
        return modelName;
    }

    @JsonProperty(JSON_PROPERTY_MODEL_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setModelName(@org.eclipse.jdt.annotation.NonNull String modelName) {
        this.modelName = modelName;
    }

    public DeviceProfile modelDescription(@org.eclipse.jdt.annotation.NonNull String modelDescription) {
        this.modelDescription = modelDescription;
        return this;
    }

    /**
     * Gets or sets the model description of the device which this profile represents.
     * 
     * @return modelDescription
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MODEL_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getModelDescription() {
        return modelDescription;
    }

    @JsonProperty(JSON_PROPERTY_MODEL_DESCRIPTION)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setModelDescription(@org.eclipse.jdt.annotation.NonNull String modelDescription) {
        this.modelDescription = modelDescription;
    }

    public DeviceProfile modelNumber(@org.eclipse.jdt.annotation.NonNull String modelNumber) {
        this.modelNumber = modelNumber;
        return this;
    }

    /**
     * Gets or sets the model number of the device which this profile represents.
     * 
     * @return modelNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MODEL_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getModelNumber() {
        return modelNumber;
    }

    @JsonProperty(JSON_PROPERTY_MODEL_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setModelNumber(@org.eclipse.jdt.annotation.NonNull String modelNumber) {
        this.modelNumber = modelNumber;
    }

    public DeviceProfile modelUrl(@org.eclipse.jdt.annotation.NonNull String modelUrl) {
        this.modelUrl = modelUrl;
        return this;
    }

    /**
     * Gets or sets the ModelUrl.
     * 
     * @return modelUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MODEL_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getModelUrl() {
        return modelUrl;
    }

    @JsonProperty(JSON_PROPERTY_MODEL_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setModelUrl(@org.eclipse.jdt.annotation.NonNull String modelUrl) {
        this.modelUrl = modelUrl;
    }

    public DeviceProfile serialNumber(@org.eclipse.jdt.annotation.NonNull String serialNumber) {
        this.serialNumber = serialNumber;
        return this;
    }

    /**
     * Gets or sets the serial number of the device which this profile represents.
     * 
     * @return serialNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SERIAL_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSerialNumber() {
        return serialNumber;
    }

    @JsonProperty(JSON_PROPERTY_SERIAL_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSerialNumber(@org.eclipse.jdt.annotation.NonNull String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public DeviceProfile enableAlbumArtInDidl(@org.eclipse.jdt.annotation.NonNull Boolean enableAlbumArtInDidl) {
        this.enableAlbumArtInDidl = enableAlbumArtInDidl;
        return this;
    }

    /**
     * Gets or sets a value indicating whether EnableAlbumArtInDidl.
     * 
     * @return enableAlbumArtInDidl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_ALBUM_ART_IN_DIDL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableAlbumArtInDidl() {
        return enableAlbumArtInDidl;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_ALBUM_ART_IN_DIDL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableAlbumArtInDidl(@org.eclipse.jdt.annotation.NonNull Boolean enableAlbumArtInDidl) {
        this.enableAlbumArtInDidl = enableAlbumArtInDidl;
    }

    public DeviceProfile enableSingleAlbumArtLimit(
            @org.eclipse.jdt.annotation.NonNull Boolean enableSingleAlbumArtLimit) {
        this.enableSingleAlbumArtLimit = enableSingleAlbumArtLimit;
        return this;
    }

    /**
     * Gets or sets a value indicating whether EnableSingleAlbumArtLimit.
     * 
     * @return enableSingleAlbumArtLimit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_SINGLE_ALBUM_ART_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableSingleAlbumArtLimit() {
        return enableSingleAlbumArtLimit;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_SINGLE_ALBUM_ART_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSingleAlbumArtLimit(@org.eclipse.jdt.annotation.NonNull Boolean enableSingleAlbumArtLimit) {
        this.enableSingleAlbumArtLimit = enableSingleAlbumArtLimit;
    }

    public DeviceProfile enableSingleSubtitleLimit(
            @org.eclipse.jdt.annotation.NonNull Boolean enableSingleSubtitleLimit) {
        this.enableSingleSubtitleLimit = enableSingleSubtitleLimit;
        return this;
    }

    /**
     * Gets or sets a value indicating whether EnableSingleSubtitleLimit.
     * 
     * @return enableSingleSubtitleLimit
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_SINGLE_SUBTITLE_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableSingleSubtitleLimit() {
        return enableSingleSubtitleLimit;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_SINGLE_SUBTITLE_LIMIT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableSingleSubtitleLimit(@org.eclipse.jdt.annotation.NonNull Boolean enableSingleSubtitleLimit) {
        this.enableSingleSubtitleLimit = enableSingleSubtitleLimit;
    }

    public DeviceProfile supportedMediaTypes(@org.eclipse.jdt.annotation.NonNull String supportedMediaTypes) {
        this.supportedMediaTypes = supportedMediaTypes;
        return this;
    }

    /**
     * Gets or sets the SupportedMediaTypes.
     * 
     * @return supportedMediaTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTED_MEDIA_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSupportedMediaTypes() {
        return supportedMediaTypes;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTED_MEDIA_TYPES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportedMediaTypes(@org.eclipse.jdt.annotation.NonNull String supportedMediaTypes) {
        this.supportedMediaTypes = supportedMediaTypes;
    }

    public DeviceProfile userId(@org.eclipse.jdt.annotation.NonNull String userId) {
        this.userId = userId;
        return this;
    }

    /**
     * Gets or sets the UserId.
     * 
     * @return userId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserId() {
        return userId;
    }

    @JsonProperty(JSON_PROPERTY_USER_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserId(@org.eclipse.jdt.annotation.NonNull String userId) {
        this.userId = userId;
    }

    public DeviceProfile albumArtPn(@org.eclipse.jdt.annotation.NonNull String albumArtPn) {
        this.albumArtPn = albumArtPn;
        return this;
    }

    /**
     * Gets or sets the AlbumArtPn.
     * 
     * @return albumArtPn
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ALBUM_ART_PN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getAlbumArtPn() {
        return albumArtPn;
    }

    @JsonProperty(JSON_PROPERTY_ALBUM_ART_PN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAlbumArtPn(@org.eclipse.jdt.annotation.NonNull String albumArtPn) {
        this.albumArtPn = albumArtPn;
    }

    public DeviceProfile maxAlbumArtWidth(@org.eclipse.jdt.annotation.NonNull Integer maxAlbumArtWidth) {
        this.maxAlbumArtWidth = maxAlbumArtWidth;
        return this;
    }

    /**
     * Gets or sets the MaxAlbumArtWidth.
     * 
     * @return maxAlbumArtWidth
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_ALBUM_ART_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxAlbumArtWidth() {
        return maxAlbumArtWidth;
    }

    @JsonProperty(JSON_PROPERTY_MAX_ALBUM_ART_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxAlbumArtWidth(@org.eclipse.jdt.annotation.NonNull Integer maxAlbumArtWidth) {
        this.maxAlbumArtWidth = maxAlbumArtWidth;
    }

    public DeviceProfile maxAlbumArtHeight(@org.eclipse.jdt.annotation.NonNull Integer maxAlbumArtHeight) {
        this.maxAlbumArtHeight = maxAlbumArtHeight;
        return this;
    }

    /**
     * Gets or sets the MaxAlbumArtHeight.
     * 
     * @return maxAlbumArtHeight
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_ALBUM_ART_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxAlbumArtHeight() {
        return maxAlbumArtHeight;
    }

    @JsonProperty(JSON_PROPERTY_MAX_ALBUM_ART_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxAlbumArtHeight(@org.eclipse.jdt.annotation.NonNull Integer maxAlbumArtHeight) {
        this.maxAlbumArtHeight = maxAlbumArtHeight;
    }

    public DeviceProfile maxIconWidth(@org.eclipse.jdt.annotation.NonNull Integer maxIconWidth) {
        this.maxIconWidth = maxIconWidth;
        return this;
    }

    /**
     * Gets or sets the maximum allowed width of embedded icons.
     * 
     * @return maxIconWidth
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_ICON_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxIconWidth() {
        return maxIconWidth;
    }

    @JsonProperty(JSON_PROPERTY_MAX_ICON_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxIconWidth(@org.eclipse.jdt.annotation.NonNull Integer maxIconWidth) {
        this.maxIconWidth = maxIconWidth;
    }

    public DeviceProfile maxIconHeight(@org.eclipse.jdt.annotation.NonNull Integer maxIconHeight) {
        this.maxIconHeight = maxIconHeight;
        return this;
    }

    /**
     * Gets or sets the maximum allowed height of embedded icons.
     * 
     * @return maxIconHeight
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_ICON_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxIconHeight() {
        return maxIconHeight;
    }

    @JsonProperty(JSON_PROPERTY_MAX_ICON_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxIconHeight(@org.eclipse.jdt.annotation.NonNull Integer maxIconHeight) {
        this.maxIconHeight = maxIconHeight;
    }

    public DeviceProfile maxStreamingBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStreamingBitrate) {
        this.maxStreamingBitrate = maxStreamingBitrate;
        return this;
    }

    /**
     * Gets or sets the maximum allowed bitrate for all streamed content.
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

    public DeviceProfile maxStaticBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStaticBitrate) {
        this.maxStaticBitrate = maxStaticBitrate;
        return this;
    }

    /**
     * Gets or sets the maximum allowed bitrate for statically streamed content (&#x3D; direct played files).
     * 
     * @return maxStaticBitrate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_STATIC_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxStaticBitrate() {
        return maxStaticBitrate;
    }

    @JsonProperty(JSON_PROPERTY_MAX_STATIC_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxStaticBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStaticBitrate) {
        this.maxStaticBitrate = maxStaticBitrate;
    }

    public DeviceProfile musicStreamingTranscodingBitrate(
            @org.eclipse.jdt.annotation.NonNull Integer musicStreamingTranscodingBitrate) {
        this.musicStreamingTranscodingBitrate = musicStreamingTranscodingBitrate;
        return this;
    }

    /**
     * Gets or sets the maximum allowed bitrate for transcoded music streams.
     * 
     * @return musicStreamingTranscodingBitrate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MUSIC_STREAMING_TRANSCODING_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMusicStreamingTranscodingBitrate() {
        return musicStreamingTranscodingBitrate;
    }

    @JsonProperty(JSON_PROPERTY_MUSIC_STREAMING_TRANSCODING_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMusicStreamingTranscodingBitrate(
            @org.eclipse.jdt.annotation.NonNull Integer musicStreamingTranscodingBitrate) {
        this.musicStreamingTranscodingBitrate = musicStreamingTranscodingBitrate;
    }

    public DeviceProfile maxStaticMusicBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStaticMusicBitrate) {
        this.maxStaticMusicBitrate = maxStaticMusicBitrate;
        return this;
    }

    /**
     * Gets or sets the maximum allowed bitrate for statically streamed (&#x3D; direct played) music files.
     * 
     * @return maxStaticMusicBitrate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAX_STATIC_MUSIC_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxStaticMusicBitrate() {
        return maxStaticMusicBitrate;
    }

    @JsonProperty(JSON_PROPERTY_MAX_STATIC_MUSIC_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxStaticMusicBitrate(@org.eclipse.jdt.annotation.NonNull Integer maxStaticMusicBitrate) {
        this.maxStaticMusicBitrate = maxStaticMusicBitrate;
    }

    public DeviceProfile sonyAggregationFlags(@org.eclipse.jdt.annotation.NonNull String sonyAggregationFlags) {
        this.sonyAggregationFlags = sonyAggregationFlags;
        return this;
    }

    /**
     * Gets or sets the content of the aggregationFlags element in the urn:schemas-sonycom:av namespace.
     * 
     * @return sonyAggregationFlags
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SONY_AGGREGATION_FLAGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSonyAggregationFlags() {
        return sonyAggregationFlags;
    }

    @JsonProperty(JSON_PROPERTY_SONY_AGGREGATION_FLAGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSonyAggregationFlags(@org.eclipse.jdt.annotation.NonNull String sonyAggregationFlags) {
        this.sonyAggregationFlags = sonyAggregationFlags;
    }

    public DeviceProfile protocolInfo(@org.eclipse.jdt.annotation.NonNull String protocolInfo) {
        this.protocolInfo = protocolInfo;
        return this;
    }

    /**
     * Gets or sets the ProtocolInfo.
     * 
     * @return protocolInfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROTOCOL_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProtocolInfo() {
        return protocolInfo;
    }

    @JsonProperty(JSON_PROPERTY_PROTOCOL_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProtocolInfo(@org.eclipse.jdt.annotation.NonNull String protocolInfo) {
        this.protocolInfo = protocolInfo;
    }

    public DeviceProfile timelineOffsetSeconds(@org.eclipse.jdt.annotation.NonNull Integer timelineOffsetSeconds) {
        this.timelineOffsetSeconds = timelineOffsetSeconds;
        return this;
    }

    /**
     * Gets or sets the TimelineOffsetSeconds.
     * 
     * @return timelineOffsetSeconds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TIMELINE_OFFSET_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getTimelineOffsetSeconds() {
        return timelineOffsetSeconds;
    }

    @JsonProperty(JSON_PROPERTY_TIMELINE_OFFSET_SECONDS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimelineOffsetSeconds(@org.eclipse.jdt.annotation.NonNull Integer timelineOffsetSeconds) {
        this.timelineOffsetSeconds = timelineOffsetSeconds;
    }

    public DeviceProfile requiresPlainVideoItems(@org.eclipse.jdt.annotation.NonNull Boolean requiresPlainVideoItems) {
        this.requiresPlainVideoItems = requiresPlainVideoItems;
        return this;
    }

    /**
     * Gets or sets a value indicating whether RequiresPlainVideoItems.
     * 
     * @return requiresPlainVideoItems
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REQUIRES_PLAIN_VIDEO_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRequiresPlainVideoItems() {
        return requiresPlainVideoItems;
    }

    @JsonProperty(JSON_PROPERTY_REQUIRES_PLAIN_VIDEO_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequiresPlainVideoItems(@org.eclipse.jdt.annotation.NonNull Boolean requiresPlainVideoItems) {
        this.requiresPlainVideoItems = requiresPlainVideoItems;
    }

    public DeviceProfile requiresPlainFolders(@org.eclipse.jdt.annotation.NonNull Boolean requiresPlainFolders) {
        this.requiresPlainFolders = requiresPlainFolders;
        return this;
    }

    /**
     * Gets or sets a value indicating whether RequiresPlainFolders.
     * 
     * @return requiresPlainFolders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REQUIRES_PLAIN_FOLDERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRequiresPlainFolders() {
        return requiresPlainFolders;
    }

    @JsonProperty(JSON_PROPERTY_REQUIRES_PLAIN_FOLDERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequiresPlainFolders(@org.eclipse.jdt.annotation.NonNull Boolean requiresPlainFolders) {
        this.requiresPlainFolders = requiresPlainFolders;
    }

    public DeviceProfile enableMSMediaReceiverRegistrar(
            @org.eclipse.jdt.annotation.NonNull Boolean enableMSMediaReceiverRegistrar) {
        this.enableMSMediaReceiverRegistrar = enableMSMediaReceiverRegistrar;
        return this;
    }

    /**
     * Gets or sets a value indicating whether EnableMSMediaReceiverRegistrar.
     * 
     * @return enableMSMediaReceiverRegistrar
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENABLE_M_S_MEDIA_RECEIVER_REGISTRAR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableMSMediaReceiverRegistrar() {
        return enableMSMediaReceiverRegistrar;
    }

    @JsonProperty(JSON_PROPERTY_ENABLE_M_S_MEDIA_RECEIVER_REGISTRAR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableMSMediaReceiverRegistrar(
            @org.eclipse.jdt.annotation.NonNull Boolean enableMSMediaReceiverRegistrar) {
        this.enableMSMediaReceiverRegistrar = enableMSMediaReceiverRegistrar;
    }

    public DeviceProfile ignoreTranscodeByteRangeRequests(
            @org.eclipse.jdt.annotation.NonNull Boolean ignoreTranscodeByteRangeRequests) {
        this.ignoreTranscodeByteRangeRequests = ignoreTranscodeByteRangeRequests;
        return this;
    }

    /**
     * Gets or sets a value indicating whether IgnoreTranscodeByteRangeRequests.
     * 
     * @return ignoreTranscodeByteRangeRequests
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IGNORE_TRANSCODE_BYTE_RANGE_REQUESTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIgnoreTranscodeByteRangeRequests() {
        return ignoreTranscodeByteRangeRequests;
    }

    @JsonProperty(JSON_PROPERTY_IGNORE_TRANSCODE_BYTE_RANGE_REQUESTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIgnoreTranscodeByteRangeRequests(
            @org.eclipse.jdt.annotation.NonNull Boolean ignoreTranscodeByteRangeRequests) {
        this.ignoreTranscodeByteRangeRequests = ignoreTranscodeByteRangeRequests;
    }

    public DeviceProfile xmlRootAttributes(@org.eclipse.jdt.annotation.NonNull List<XmlAttribute> xmlRootAttributes) {
        this.xmlRootAttributes = xmlRootAttributes;
        return this;
    }

    public DeviceProfile addXmlRootAttributesItem(XmlAttribute xmlRootAttributesItem) {
        if (this.xmlRootAttributes == null) {
            this.xmlRootAttributes = new ArrayList<>();
        }
        this.xmlRootAttributes.add(xmlRootAttributesItem);
        return this;
    }

    /**
     * Gets or sets the XmlRootAttributes.
     * 
     * @return xmlRootAttributes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_XML_ROOT_ATTRIBUTES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<XmlAttribute> getXmlRootAttributes() {
        return xmlRootAttributes;
    }

    @JsonProperty(JSON_PROPERTY_XML_ROOT_ATTRIBUTES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setXmlRootAttributes(@org.eclipse.jdt.annotation.NonNull List<XmlAttribute> xmlRootAttributes) {
        this.xmlRootAttributes = xmlRootAttributes;
    }

    public DeviceProfile directPlayProfiles(
            @org.eclipse.jdt.annotation.NonNull List<DirectPlayProfile> directPlayProfiles) {
        this.directPlayProfiles = directPlayProfiles;
        return this;
    }

    public DeviceProfile addDirectPlayProfilesItem(DirectPlayProfile directPlayProfilesItem) {
        if (this.directPlayProfiles == null) {
            this.directPlayProfiles = new ArrayList<>();
        }
        this.directPlayProfiles.add(directPlayProfilesItem);
        return this;
    }

    /**
     * Gets or sets the direct play profiles.
     * 
     * @return directPlayProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DIRECT_PLAY_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<DirectPlayProfile> getDirectPlayProfiles() {
        return directPlayProfiles;
    }

    @JsonProperty(JSON_PROPERTY_DIRECT_PLAY_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDirectPlayProfiles(@org.eclipse.jdt.annotation.NonNull List<DirectPlayProfile> directPlayProfiles) {
        this.directPlayProfiles = directPlayProfiles;
    }

    public DeviceProfile transcodingProfiles(
            @org.eclipse.jdt.annotation.NonNull List<TranscodingProfile> transcodingProfiles) {
        this.transcodingProfiles = transcodingProfiles;
        return this;
    }

    public DeviceProfile addTranscodingProfilesItem(TranscodingProfile transcodingProfilesItem) {
        if (this.transcodingProfiles == null) {
            this.transcodingProfiles = new ArrayList<>();
        }
        this.transcodingProfiles.add(transcodingProfilesItem);
        return this;
    }

    /**
     * Gets or sets the transcoding profiles.
     * 
     * @return transcodingProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRANSCODING_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<TranscodingProfile> getTranscodingProfiles() {
        return transcodingProfiles;
    }

    @JsonProperty(JSON_PROPERTY_TRANSCODING_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingProfiles(
            @org.eclipse.jdt.annotation.NonNull List<TranscodingProfile> transcodingProfiles) {
        this.transcodingProfiles = transcodingProfiles;
    }

    public DeviceProfile containerProfiles(
            @org.eclipse.jdt.annotation.NonNull List<ContainerProfile> containerProfiles) {
        this.containerProfiles = containerProfiles;
        return this;
    }

    public DeviceProfile addContainerProfilesItem(ContainerProfile containerProfilesItem) {
        if (this.containerProfiles == null) {
            this.containerProfiles = new ArrayList<>();
        }
        this.containerProfiles.add(containerProfilesItem);
        return this;
    }

    /**
     * Gets or sets the container profiles.
     * 
     * @return containerProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTAINER_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ContainerProfile> getContainerProfiles() {
        return containerProfiles;
    }

    @JsonProperty(JSON_PROPERTY_CONTAINER_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContainerProfiles(@org.eclipse.jdt.annotation.NonNull List<ContainerProfile> containerProfiles) {
        this.containerProfiles = containerProfiles;
    }

    public DeviceProfile codecProfiles(@org.eclipse.jdt.annotation.NonNull List<CodecProfile> codecProfiles) {
        this.codecProfiles = codecProfiles;
        return this;
    }

    public DeviceProfile addCodecProfilesItem(CodecProfile codecProfilesItem) {
        if (this.codecProfiles == null) {
            this.codecProfiles = new ArrayList<>();
        }
        this.codecProfiles.add(codecProfilesItem);
        return this;
    }

    /**
     * Gets or sets the codec profiles.
     * 
     * @return codecProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CODEC_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<CodecProfile> getCodecProfiles() {
        return codecProfiles;
    }

    @JsonProperty(JSON_PROPERTY_CODEC_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCodecProfiles(@org.eclipse.jdt.annotation.NonNull List<CodecProfile> codecProfiles) {
        this.codecProfiles = codecProfiles;
    }

    public DeviceProfile responseProfiles(@org.eclipse.jdt.annotation.NonNull List<ResponseProfile> responseProfiles) {
        this.responseProfiles = responseProfiles;
        return this;
    }

    public DeviceProfile addResponseProfilesItem(ResponseProfile responseProfilesItem) {
        if (this.responseProfiles == null) {
            this.responseProfiles = new ArrayList<>();
        }
        this.responseProfiles.add(responseProfilesItem);
        return this;
    }

    /**
     * Gets or sets the ResponseProfiles.
     * 
     * @return responseProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RESPONSE_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ResponseProfile> getResponseProfiles() {
        return responseProfiles;
    }

    @JsonProperty(JSON_PROPERTY_RESPONSE_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setResponseProfiles(@org.eclipse.jdt.annotation.NonNull List<ResponseProfile> responseProfiles) {
        this.responseProfiles = responseProfiles;
    }

    public DeviceProfile subtitleProfiles(@org.eclipse.jdt.annotation.NonNull List<SubtitleProfile> subtitleProfiles) {
        this.subtitleProfiles = subtitleProfiles;
        return this;
    }

    public DeviceProfile addSubtitleProfilesItem(SubtitleProfile subtitleProfilesItem) {
        if (this.subtitleProfiles == null) {
            this.subtitleProfiles = new ArrayList<>();
        }
        this.subtitleProfiles.add(subtitleProfilesItem);
        return this;
    }

    /**
     * Gets or sets the subtitle profiles.
     * 
     * @return subtitleProfiles
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUBTITLE_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<SubtitleProfile> getSubtitleProfiles() {
        return subtitleProfiles;
    }

    @JsonProperty(JSON_PROPERTY_SUBTITLE_PROFILES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSubtitleProfiles(@org.eclipse.jdt.annotation.NonNull List<SubtitleProfile> subtitleProfiles) {
        this.subtitleProfiles = subtitleProfiles;
    }

    /**
     * Return true if this DeviceProfile object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        DeviceProfile deviceProfile = (DeviceProfile) o;
        return Objects.equals(this.name, deviceProfile.name) && Objects.equals(this.id, deviceProfile.id)
                && Objects.equals(this.identification, deviceProfile.identification)
                && Objects.equals(this.friendlyName, deviceProfile.friendlyName)
                && Objects.equals(this.manufacturer, deviceProfile.manufacturer)
                && Objects.equals(this.manufacturerUrl, deviceProfile.manufacturerUrl)
                && Objects.equals(this.modelName, deviceProfile.modelName)
                && Objects.equals(this.modelDescription, deviceProfile.modelDescription)
                && Objects.equals(this.modelNumber, deviceProfile.modelNumber)
                && Objects.equals(this.modelUrl, deviceProfile.modelUrl)
                && Objects.equals(this.serialNumber, deviceProfile.serialNumber)
                && Objects.equals(this.enableAlbumArtInDidl, deviceProfile.enableAlbumArtInDidl)
                && Objects.equals(this.enableSingleAlbumArtLimit, deviceProfile.enableSingleAlbumArtLimit)
                && Objects.equals(this.enableSingleSubtitleLimit, deviceProfile.enableSingleSubtitleLimit)
                && Objects.equals(this.supportedMediaTypes, deviceProfile.supportedMediaTypes)
                && Objects.equals(this.userId, deviceProfile.userId)
                && Objects.equals(this.albumArtPn, deviceProfile.albumArtPn)
                && Objects.equals(this.maxAlbumArtWidth, deviceProfile.maxAlbumArtWidth)
                && Objects.equals(this.maxAlbumArtHeight, deviceProfile.maxAlbumArtHeight)
                && Objects.equals(this.maxIconWidth, deviceProfile.maxIconWidth)
                && Objects.equals(this.maxIconHeight, deviceProfile.maxIconHeight)
                && Objects.equals(this.maxStreamingBitrate, deviceProfile.maxStreamingBitrate)
                && Objects.equals(this.maxStaticBitrate, deviceProfile.maxStaticBitrate)
                && Objects.equals(this.musicStreamingTranscodingBitrate, deviceProfile.musicStreamingTranscodingBitrate)
                && Objects.equals(this.maxStaticMusicBitrate, deviceProfile.maxStaticMusicBitrate)
                && Objects.equals(this.sonyAggregationFlags, deviceProfile.sonyAggregationFlags)
                && Objects.equals(this.protocolInfo, deviceProfile.protocolInfo)
                && Objects.equals(this.timelineOffsetSeconds, deviceProfile.timelineOffsetSeconds)
                && Objects.equals(this.requiresPlainVideoItems, deviceProfile.requiresPlainVideoItems)
                && Objects.equals(this.requiresPlainFolders, deviceProfile.requiresPlainFolders)
                && Objects.equals(this.enableMSMediaReceiverRegistrar, deviceProfile.enableMSMediaReceiverRegistrar)
                && Objects.equals(this.ignoreTranscodeByteRangeRequests, deviceProfile.ignoreTranscodeByteRangeRequests)
                && Objects.equals(this.xmlRootAttributes, deviceProfile.xmlRootAttributes)
                && Objects.equals(this.directPlayProfiles, deviceProfile.directPlayProfiles)
                && Objects.equals(this.transcodingProfiles, deviceProfile.transcodingProfiles)
                && Objects.equals(this.containerProfiles, deviceProfile.containerProfiles)
                && Objects.equals(this.codecProfiles, deviceProfile.codecProfiles)
                && Objects.equals(this.responseProfiles, deviceProfile.responseProfiles)
                && Objects.equals(this.subtitleProfiles, deviceProfile.subtitleProfiles);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, identification, friendlyName, manufacturer, manufacturerUrl, modelName,
                modelDescription, modelNumber, modelUrl, serialNumber, enableAlbumArtInDidl, enableSingleAlbumArtLimit,
                enableSingleSubtitleLimit, supportedMediaTypes, userId, albumArtPn, maxAlbumArtWidth, maxAlbumArtHeight,
                maxIconWidth, maxIconHeight, maxStreamingBitrate, maxStaticBitrate, musicStreamingTranscodingBitrate,
                maxStaticMusicBitrate, sonyAggregationFlags, protocolInfo, timelineOffsetSeconds,
                requiresPlainVideoItems, requiresPlainFolders, enableMSMediaReceiverRegistrar,
                ignoreTranscodeByteRangeRequests, xmlRootAttributes, directPlayProfiles, transcodingProfiles,
                containerProfiles, codecProfiles, responseProfiles, subtitleProfiles);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class DeviceProfile {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    identification: ").append(toIndentedString(identification)).append("\n");
        sb.append("    friendlyName: ").append(toIndentedString(friendlyName)).append("\n");
        sb.append("    manufacturer: ").append(toIndentedString(manufacturer)).append("\n");
        sb.append("    manufacturerUrl: ").append(toIndentedString(manufacturerUrl)).append("\n");
        sb.append("    modelName: ").append(toIndentedString(modelName)).append("\n");
        sb.append("    modelDescription: ").append(toIndentedString(modelDescription)).append("\n");
        sb.append("    modelNumber: ").append(toIndentedString(modelNumber)).append("\n");
        sb.append("    modelUrl: ").append(toIndentedString(modelUrl)).append("\n");
        sb.append("    serialNumber: ").append(toIndentedString(serialNumber)).append("\n");
        sb.append("    enableAlbumArtInDidl: ").append(toIndentedString(enableAlbumArtInDidl)).append("\n");
        sb.append("    enableSingleAlbumArtLimit: ").append(toIndentedString(enableSingleAlbumArtLimit)).append("\n");
        sb.append("    enableSingleSubtitleLimit: ").append(toIndentedString(enableSingleSubtitleLimit)).append("\n");
        sb.append("    supportedMediaTypes: ").append(toIndentedString(supportedMediaTypes)).append("\n");
        sb.append("    userId: ").append(toIndentedString(userId)).append("\n");
        sb.append("    albumArtPn: ").append(toIndentedString(albumArtPn)).append("\n");
        sb.append("    maxAlbumArtWidth: ").append(toIndentedString(maxAlbumArtWidth)).append("\n");
        sb.append("    maxAlbumArtHeight: ").append(toIndentedString(maxAlbumArtHeight)).append("\n");
        sb.append("    maxIconWidth: ").append(toIndentedString(maxIconWidth)).append("\n");
        sb.append("    maxIconHeight: ").append(toIndentedString(maxIconHeight)).append("\n");
        sb.append("    maxStreamingBitrate: ").append(toIndentedString(maxStreamingBitrate)).append("\n");
        sb.append("    maxStaticBitrate: ").append(toIndentedString(maxStaticBitrate)).append("\n");
        sb.append("    musicStreamingTranscodingBitrate: ").append(toIndentedString(musicStreamingTranscodingBitrate))
                .append("\n");
        sb.append("    maxStaticMusicBitrate: ").append(toIndentedString(maxStaticMusicBitrate)).append("\n");
        sb.append("    sonyAggregationFlags: ").append(toIndentedString(sonyAggregationFlags)).append("\n");
        sb.append("    protocolInfo: ").append(toIndentedString(protocolInfo)).append("\n");
        sb.append("    timelineOffsetSeconds: ").append(toIndentedString(timelineOffsetSeconds)).append("\n");
        sb.append("    requiresPlainVideoItems: ").append(toIndentedString(requiresPlainVideoItems)).append("\n");
        sb.append("    requiresPlainFolders: ").append(toIndentedString(requiresPlainFolders)).append("\n");
        sb.append("    enableMSMediaReceiverRegistrar: ").append(toIndentedString(enableMSMediaReceiverRegistrar))
                .append("\n");
        sb.append("    ignoreTranscodeByteRangeRequests: ").append(toIndentedString(ignoreTranscodeByteRangeRequests))
                .append("\n");
        sb.append("    xmlRootAttributes: ").append(toIndentedString(xmlRootAttributes)).append("\n");
        sb.append("    directPlayProfiles: ").append(toIndentedString(directPlayProfiles)).append("\n");
        sb.append("    transcodingProfiles: ").append(toIndentedString(transcodingProfiles)).append("\n");
        sb.append("    containerProfiles: ").append(toIndentedString(containerProfiles)).append("\n");
        sb.append("    codecProfiles: ").append(toIndentedString(codecProfiles)).append("\n");
        sb.append("    responseProfiles: ").append(toIndentedString(responseProfiles)).append("\n");
        sb.append("    subtitleProfiles: ").append(toIndentedString(subtitleProfiles)).append("\n");
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format("%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(
                    String.format("%sId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Identification` to the URL query string
        if (getIdentification() != null) {
            joiner.add(getIdentification().toUrlQueryString(prefix + "Identification" + suffix));
        }

        // add `FriendlyName` to the URL query string
        if (getFriendlyName() != null) {
            joiner.add(String.format("%sFriendlyName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFriendlyName()))));
        }

        // add `Manufacturer` to the URL query string
        if (getManufacturer() != null) {
            joiner.add(String.format("%sManufacturer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getManufacturer()))));
        }

        // add `ManufacturerUrl` to the URL query string
        if (getManufacturerUrl() != null) {
            joiner.add(String.format("%sManufacturerUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getManufacturerUrl()))));
        }

        // add `ModelName` to the URL query string
        if (getModelName() != null) {
            joiner.add(String.format("%sModelName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getModelName()))));
        }

        // add `ModelDescription` to the URL query string
        if (getModelDescription() != null) {
            joiner.add(String.format("%sModelDescription%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getModelDescription()))));
        }

        // add `ModelNumber` to the URL query string
        if (getModelNumber() != null) {
            joiner.add(String.format("%sModelNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getModelNumber()))));
        }

        // add `ModelUrl` to the URL query string
        if (getModelUrl() != null) {
            joiner.add(String.format("%sModelUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getModelUrl()))));
        }

        // add `SerialNumber` to the URL query string
        if (getSerialNumber() != null) {
            joiner.add(String.format("%sSerialNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSerialNumber()))));
        }

        // add `EnableAlbumArtInDidl` to the URL query string
        if (getEnableAlbumArtInDidl() != null) {
            joiner.add(String.format("%sEnableAlbumArtInDidl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableAlbumArtInDidl()))));
        }

        // add `EnableSingleAlbumArtLimit` to the URL query string
        if (getEnableSingleAlbumArtLimit() != null) {
            joiner.add(String.format("%sEnableSingleAlbumArtLimit%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableSingleAlbumArtLimit()))));
        }

        // add `EnableSingleSubtitleLimit` to the URL query string
        if (getEnableSingleSubtitleLimit() != null) {
            joiner.add(String.format("%sEnableSingleSubtitleLimit%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableSingleSubtitleLimit()))));
        }

        // add `SupportedMediaTypes` to the URL query string
        if (getSupportedMediaTypes() != null) {
            joiner.add(String.format("%sSupportedMediaTypes%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportedMediaTypes()))));
        }

        // add `UserId` to the URL query string
        if (getUserId() != null) {
            joiner.add(String.format("%sUserId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserId()))));
        }

        // add `AlbumArtPn` to the URL query string
        if (getAlbumArtPn() != null) {
            joiner.add(String.format("%sAlbumArtPn%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAlbumArtPn()))));
        }

        // add `MaxAlbumArtWidth` to the URL query string
        if (getMaxAlbumArtWidth() != null) {
            joiner.add(String.format("%sMaxAlbumArtWidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxAlbumArtWidth()))));
        }

        // add `MaxAlbumArtHeight` to the URL query string
        if (getMaxAlbumArtHeight() != null) {
            joiner.add(String.format("%sMaxAlbumArtHeight%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxAlbumArtHeight()))));
        }

        // add `MaxIconWidth` to the URL query string
        if (getMaxIconWidth() != null) {
            joiner.add(String.format("%sMaxIconWidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxIconWidth()))));
        }

        // add `MaxIconHeight` to the URL query string
        if (getMaxIconHeight() != null) {
            joiner.add(String.format("%sMaxIconHeight%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxIconHeight()))));
        }

        // add `MaxStreamingBitrate` to the URL query string
        if (getMaxStreamingBitrate() != null) {
            joiner.add(String.format("%sMaxStreamingBitrate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxStreamingBitrate()))));
        }

        // add `MaxStaticBitrate` to the URL query string
        if (getMaxStaticBitrate() != null) {
            joiner.add(String.format("%sMaxStaticBitrate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxStaticBitrate()))));
        }

        // add `MusicStreamingTranscodingBitrate` to the URL query string
        if (getMusicStreamingTranscodingBitrate() != null) {
            joiner.add(String.format("%sMusicStreamingTranscodingBitrate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMusicStreamingTranscodingBitrate()))));
        }

        // add `MaxStaticMusicBitrate` to the URL query string
        if (getMaxStaticMusicBitrate() != null) {
            joiner.add(String.format("%sMaxStaticMusicBitrate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxStaticMusicBitrate()))));
        }

        // add `SonyAggregationFlags` to the URL query string
        if (getSonyAggregationFlags() != null) {
            joiner.add(String.format("%sSonyAggregationFlags%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSonyAggregationFlags()))));
        }

        // add `ProtocolInfo` to the URL query string
        if (getProtocolInfo() != null) {
            joiner.add(String.format("%sProtocolInfo%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProtocolInfo()))));
        }

        // add `TimelineOffsetSeconds` to the URL query string
        if (getTimelineOffsetSeconds() != null) {
            joiner.add(String.format("%sTimelineOffsetSeconds%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTimelineOffsetSeconds()))));
        }

        // add `RequiresPlainVideoItems` to the URL query string
        if (getRequiresPlainVideoItems() != null) {
            joiner.add(String.format("%sRequiresPlainVideoItems%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRequiresPlainVideoItems()))));
        }

        // add `RequiresPlainFolders` to the URL query string
        if (getRequiresPlainFolders() != null) {
            joiner.add(String.format("%sRequiresPlainFolders%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRequiresPlainFolders()))));
        }

        // add `EnableMSMediaReceiverRegistrar` to the URL query string
        if (getEnableMSMediaReceiverRegistrar() != null) {
            joiner.add(String.format("%sEnableMSMediaReceiverRegistrar%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableMSMediaReceiverRegistrar()))));
        }

        // add `IgnoreTranscodeByteRangeRequests` to the URL query string
        if (getIgnoreTranscodeByteRangeRequests() != null) {
            joiner.add(String.format("%sIgnoreTranscodeByteRangeRequests%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIgnoreTranscodeByteRangeRequests()))));
        }

        // add `XmlRootAttributes` to the URL query string
        if (getXmlRootAttributes() != null) {
            for (int i = 0; i < getXmlRootAttributes().size(); i++) {
                if (getXmlRootAttributes().get(i) != null) {
                    joiner.add(getXmlRootAttributes().get(i).toUrlQueryString(String.format("%sXmlRootAttributes%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `DirectPlayProfiles` to the URL query string
        if (getDirectPlayProfiles() != null) {
            for (int i = 0; i < getDirectPlayProfiles().size(); i++) {
                if (getDirectPlayProfiles().get(i) != null) {
                    joiner.add(getDirectPlayProfiles().get(i).toUrlQueryString(String.format("%sDirectPlayProfiles%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `TranscodingProfiles` to the URL query string
        if (getTranscodingProfiles() != null) {
            for (int i = 0; i < getTranscodingProfiles().size(); i++) {
                if (getTranscodingProfiles().get(i) != null) {
                    joiner.add(getTranscodingProfiles().get(i).toUrlQueryString(String.format(
                            "%sTranscodingProfiles%s%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `ContainerProfiles` to the URL query string
        if (getContainerProfiles() != null) {
            for (int i = 0; i < getContainerProfiles().size(); i++) {
                if (getContainerProfiles().get(i) != null) {
                    joiner.add(getContainerProfiles().get(i).toUrlQueryString(String.format("%sContainerProfiles%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `CodecProfiles` to the URL query string
        if (getCodecProfiles() != null) {
            for (int i = 0; i < getCodecProfiles().size(); i++) {
                if (getCodecProfiles().get(i) != null) {
                    joiner.add(getCodecProfiles().get(i).toUrlQueryString(String.format("%sCodecProfiles%s%s", prefix,
                            suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `ResponseProfiles` to the URL query string
        if (getResponseProfiles() != null) {
            for (int i = 0; i < getResponseProfiles().size(); i++) {
                if (getResponseProfiles().get(i) != null) {
                    joiner.add(getResponseProfiles().get(i).toUrlQueryString(String.format("%sResponseProfiles%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `SubtitleProfiles` to the URL query string
        if (getSubtitleProfiles() != null) {
            for (int i = 0; i < getSubtitleProfiles().size(); i++) {
                if (getSubtitleProfiles().get(i) != null) {
                    joiner.add(getSubtitleProfiles().get(i).toUrlQueryString(String.format("%sSubtitleProfiles%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private DeviceProfile instance;

        public Builder() {
            this(new DeviceProfile());
        }

        protected Builder(DeviceProfile instance) {
            this.instance = instance;
        }

        public DeviceProfile.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public DeviceProfile.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public DeviceProfile.Builder identification(DeviceIdentification identification) {
            this.instance.identification = identification;
            return this;
        }

        public DeviceProfile.Builder friendlyName(String friendlyName) {
            this.instance.friendlyName = friendlyName;
            return this;
        }

        public DeviceProfile.Builder manufacturer(String manufacturer) {
            this.instance.manufacturer = manufacturer;
            return this;
        }

        public DeviceProfile.Builder manufacturerUrl(String manufacturerUrl) {
            this.instance.manufacturerUrl = manufacturerUrl;
            return this;
        }

        public DeviceProfile.Builder modelName(String modelName) {
            this.instance.modelName = modelName;
            return this;
        }

        public DeviceProfile.Builder modelDescription(String modelDescription) {
            this.instance.modelDescription = modelDescription;
            return this;
        }

        public DeviceProfile.Builder modelNumber(String modelNumber) {
            this.instance.modelNumber = modelNumber;
            return this;
        }

        public DeviceProfile.Builder modelUrl(String modelUrl) {
            this.instance.modelUrl = modelUrl;
            return this;
        }

        public DeviceProfile.Builder serialNumber(String serialNumber) {
            this.instance.serialNumber = serialNumber;
            return this;
        }

        public DeviceProfile.Builder enableAlbumArtInDidl(Boolean enableAlbumArtInDidl) {
            this.instance.enableAlbumArtInDidl = enableAlbumArtInDidl;
            return this;
        }

        public DeviceProfile.Builder enableSingleAlbumArtLimit(Boolean enableSingleAlbumArtLimit) {
            this.instance.enableSingleAlbumArtLimit = enableSingleAlbumArtLimit;
            return this;
        }

        public DeviceProfile.Builder enableSingleSubtitleLimit(Boolean enableSingleSubtitleLimit) {
            this.instance.enableSingleSubtitleLimit = enableSingleSubtitleLimit;
            return this;
        }

        public DeviceProfile.Builder supportedMediaTypes(String supportedMediaTypes) {
            this.instance.supportedMediaTypes = supportedMediaTypes;
            return this;
        }

        public DeviceProfile.Builder userId(String userId) {
            this.instance.userId = userId;
            return this;
        }

        public DeviceProfile.Builder albumArtPn(String albumArtPn) {
            this.instance.albumArtPn = albumArtPn;
            return this;
        }

        public DeviceProfile.Builder maxAlbumArtWidth(Integer maxAlbumArtWidth) {
            this.instance.maxAlbumArtWidth = maxAlbumArtWidth;
            return this;
        }

        public DeviceProfile.Builder maxAlbumArtHeight(Integer maxAlbumArtHeight) {
            this.instance.maxAlbumArtHeight = maxAlbumArtHeight;
            return this;
        }

        public DeviceProfile.Builder maxIconWidth(Integer maxIconWidth) {
            this.instance.maxIconWidth = maxIconWidth;
            return this;
        }

        public DeviceProfile.Builder maxIconHeight(Integer maxIconHeight) {
            this.instance.maxIconHeight = maxIconHeight;
            return this;
        }

        public DeviceProfile.Builder maxStreamingBitrate(Integer maxStreamingBitrate) {
            this.instance.maxStreamingBitrate = maxStreamingBitrate;
            return this;
        }

        public DeviceProfile.Builder maxStaticBitrate(Integer maxStaticBitrate) {
            this.instance.maxStaticBitrate = maxStaticBitrate;
            return this;
        }

        public DeviceProfile.Builder musicStreamingTranscodingBitrate(Integer musicStreamingTranscodingBitrate) {
            this.instance.musicStreamingTranscodingBitrate = musicStreamingTranscodingBitrate;
            return this;
        }

        public DeviceProfile.Builder maxStaticMusicBitrate(Integer maxStaticMusicBitrate) {
            this.instance.maxStaticMusicBitrate = maxStaticMusicBitrate;
            return this;
        }

        public DeviceProfile.Builder sonyAggregationFlags(String sonyAggregationFlags) {
            this.instance.sonyAggregationFlags = sonyAggregationFlags;
            return this;
        }

        public DeviceProfile.Builder protocolInfo(String protocolInfo) {
            this.instance.protocolInfo = protocolInfo;
            return this;
        }

        public DeviceProfile.Builder timelineOffsetSeconds(Integer timelineOffsetSeconds) {
            this.instance.timelineOffsetSeconds = timelineOffsetSeconds;
            return this;
        }

        public DeviceProfile.Builder requiresPlainVideoItems(Boolean requiresPlainVideoItems) {
            this.instance.requiresPlainVideoItems = requiresPlainVideoItems;
            return this;
        }

        public DeviceProfile.Builder requiresPlainFolders(Boolean requiresPlainFolders) {
            this.instance.requiresPlainFolders = requiresPlainFolders;
            return this;
        }

        public DeviceProfile.Builder enableMSMediaReceiverRegistrar(Boolean enableMSMediaReceiverRegistrar) {
            this.instance.enableMSMediaReceiverRegistrar = enableMSMediaReceiverRegistrar;
            return this;
        }

        public DeviceProfile.Builder ignoreTranscodeByteRangeRequests(Boolean ignoreTranscodeByteRangeRequests) {
            this.instance.ignoreTranscodeByteRangeRequests = ignoreTranscodeByteRangeRequests;
            return this;
        }

        public DeviceProfile.Builder xmlRootAttributes(List<XmlAttribute> xmlRootAttributes) {
            this.instance.xmlRootAttributes = xmlRootAttributes;
            return this;
        }

        public DeviceProfile.Builder directPlayProfiles(List<DirectPlayProfile> directPlayProfiles) {
            this.instance.directPlayProfiles = directPlayProfiles;
            return this;
        }

        public DeviceProfile.Builder transcodingProfiles(List<TranscodingProfile> transcodingProfiles) {
            this.instance.transcodingProfiles = transcodingProfiles;
            return this;
        }

        public DeviceProfile.Builder containerProfiles(List<ContainerProfile> containerProfiles) {
            this.instance.containerProfiles = containerProfiles;
            return this;
        }

        public DeviceProfile.Builder codecProfiles(List<CodecProfile> codecProfiles) {
            this.instance.codecProfiles = codecProfiles;
            return this;
        }

        public DeviceProfile.Builder responseProfiles(List<ResponseProfile> responseProfiles) {
            this.instance.responseProfiles = responseProfiles;
            return this;
        }

        public DeviceProfile.Builder subtitleProfiles(List<SubtitleProfile> subtitleProfiles) {
            this.instance.subtitleProfiles = subtitleProfiles;
            return this;
        }

        /**
         * returns a built DeviceProfile instance.
         *
         * The builder is not reusable.
         */
        public DeviceProfile build() {
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
    public static DeviceProfile.Builder builder() {
        return new DeviceProfile.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public DeviceProfile.Builder toBuilder() {
        return new DeviceProfile.Builder().name(getName()).id(getId()).identification(getIdentification())
                .friendlyName(getFriendlyName()).manufacturer(getManufacturer()).manufacturerUrl(getManufacturerUrl())
                .modelName(getModelName()).modelDescription(getModelDescription()).modelNumber(getModelNumber())
                .modelUrl(getModelUrl()).serialNumber(getSerialNumber()).enableAlbumArtInDidl(getEnableAlbumArtInDidl())
                .enableSingleAlbumArtLimit(getEnableSingleAlbumArtLimit())
                .enableSingleSubtitleLimit(getEnableSingleSubtitleLimit()).supportedMediaTypes(getSupportedMediaTypes())
                .userId(getUserId()).albumArtPn(getAlbumArtPn()).maxAlbumArtWidth(getMaxAlbumArtWidth())
                .maxAlbumArtHeight(getMaxAlbumArtHeight()).maxIconWidth(getMaxIconWidth())
                .maxIconHeight(getMaxIconHeight()).maxStreamingBitrate(getMaxStreamingBitrate())
                .maxStaticBitrate(getMaxStaticBitrate())
                .musicStreamingTranscodingBitrate(getMusicStreamingTranscodingBitrate())
                .maxStaticMusicBitrate(getMaxStaticMusicBitrate()).sonyAggregationFlags(getSonyAggregationFlags())
                .protocolInfo(getProtocolInfo()).timelineOffsetSeconds(getTimelineOffsetSeconds())
                .requiresPlainVideoItems(getRequiresPlainVideoItems()).requiresPlainFolders(getRequiresPlainFolders())
                .enableMSMediaReceiverRegistrar(getEnableMSMediaReceiverRegistrar())
                .ignoreTranscodeByteRangeRequests(getIgnoreTranscodeByteRangeRequests())
                .xmlRootAttributes(getXmlRootAttributes()).directPlayProfiles(getDirectPlayProfiles())
                .transcodingProfiles(getTranscodingProfiles()).containerProfiles(getContainerProfiles())
                .codecProfiles(getCodecProfiles()).responseProfiles(getResponseProfiles())
                .subtitleProfiles(getSubtitleProfiles());
    }
}
