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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * MediaSourceInfo
 */
@JsonPropertyOrder({ MediaSourceInfo.JSON_PROPERTY_PROTOCOL, MediaSourceInfo.JSON_PROPERTY_ID,
        MediaSourceInfo.JSON_PROPERTY_PATH, MediaSourceInfo.JSON_PROPERTY_ENCODER_PATH,
        MediaSourceInfo.JSON_PROPERTY_ENCODER_PROTOCOL, MediaSourceInfo.JSON_PROPERTY_TYPE,
        MediaSourceInfo.JSON_PROPERTY_CONTAINER, MediaSourceInfo.JSON_PROPERTY_SIZE, MediaSourceInfo.JSON_PROPERTY_NAME,
        MediaSourceInfo.JSON_PROPERTY_IS_REMOTE, MediaSourceInfo.JSON_PROPERTY_ETAG,
        MediaSourceInfo.JSON_PROPERTY_RUN_TIME_TICKS, MediaSourceInfo.JSON_PROPERTY_READ_AT_NATIVE_FRAMERATE,
        MediaSourceInfo.JSON_PROPERTY_IGNORE_DTS, MediaSourceInfo.JSON_PROPERTY_IGNORE_INDEX,
        MediaSourceInfo.JSON_PROPERTY_GEN_PTS_INPUT, MediaSourceInfo.JSON_PROPERTY_SUPPORTS_TRANSCODING,
        MediaSourceInfo.JSON_PROPERTY_SUPPORTS_DIRECT_STREAM, MediaSourceInfo.JSON_PROPERTY_SUPPORTS_DIRECT_PLAY,
        MediaSourceInfo.JSON_PROPERTY_IS_INFINITE_STREAM,
        MediaSourceInfo.JSON_PROPERTY_USE_MOST_COMPATIBLE_TRANSCODING_PROFILE,
        MediaSourceInfo.JSON_PROPERTY_REQUIRES_OPENING, MediaSourceInfo.JSON_PROPERTY_OPEN_TOKEN,
        MediaSourceInfo.JSON_PROPERTY_REQUIRES_CLOSING, MediaSourceInfo.JSON_PROPERTY_LIVE_STREAM_ID,
        MediaSourceInfo.JSON_PROPERTY_BUFFER_MS, MediaSourceInfo.JSON_PROPERTY_REQUIRES_LOOPING,
        MediaSourceInfo.JSON_PROPERTY_SUPPORTS_PROBING, MediaSourceInfo.JSON_PROPERTY_VIDEO_TYPE,
        MediaSourceInfo.JSON_PROPERTY_ISO_TYPE, MediaSourceInfo.JSON_PROPERTY_VIDEO3_D_FORMAT,
        MediaSourceInfo.JSON_PROPERTY_MEDIA_STREAMS, MediaSourceInfo.JSON_PROPERTY_MEDIA_ATTACHMENTS,
        MediaSourceInfo.JSON_PROPERTY_FORMATS, MediaSourceInfo.JSON_PROPERTY_BITRATE,
        MediaSourceInfo.JSON_PROPERTY_FALLBACK_MAX_STREAMING_BITRATE, MediaSourceInfo.JSON_PROPERTY_TIMESTAMP,
        MediaSourceInfo.JSON_PROPERTY_REQUIRED_HTTP_HEADERS, MediaSourceInfo.JSON_PROPERTY_TRANSCODING_URL,
        MediaSourceInfo.JSON_PROPERTY_TRANSCODING_SUB_PROTOCOL, MediaSourceInfo.JSON_PROPERTY_TRANSCODING_CONTAINER,
        MediaSourceInfo.JSON_PROPERTY_ANALYZE_DURATION_MS, MediaSourceInfo.JSON_PROPERTY_DEFAULT_AUDIO_STREAM_INDEX,
        MediaSourceInfo.JSON_PROPERTY_DEFAULT_SUBTITLE_STREAM_INDEX, MediaSourceInfo.JSON_PROPERTY_HAS_SEGMENTS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaSourceInfo {
    public static final String JSON_PROPERTY_PROTOCOL = "Protocol";
    @org.eclipse.jdt.annotation.NonNull
    private MediaProtocol protocol;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_ENCODER_PATH = "EncoderPath";
    @org.eclipse.jdt.annotation.NonNull
    private String encoderPath;

    public static final String JSON_PROPERTY_ENCODER_PROTOCOL = "EncoderProtocol";
    @org.eclipse.jdt.annotation.NonNull
    private MediaProtocol encoderProtocol;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private MediaSourceType type;

    public static final String JSON_PROPERTY_CONTAINER = "Container";
    @org.eclipse.jdt.annotation.NonNull
    private String container;

    public static final String JSON_PROPERTY_SIZE = "Size";
    @org.eclipse.jdt.annotation.NonNull
    private Long size;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_IS_REMOTE = "IsRemote";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isRemote;

    public static final String JSON_PROPERTY_ETAG = "ETag";
    @org.eclipse.jdt.annotation.NonNull
    private String etag;

    public static final String JSON_PROPERTY_RUN_TIME_TICKS = "RunTimeTicks";
    @org.eclipse.jdt.annotation.NonNull
    private Long runTimeTicks;

    public static final String JSON_PROPERTY_READ_AT_NATIVE_FRAMERATE = "ReadAtNativeFramerate";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean readAtNativeFramerate;

    public static final String JSON_PROPERTY_IGNORE_DTS = "IgnoreDts";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean ignoreDts;

    public static final String JSON_PROPERTY_IGNORE_INDEX = "IgnoreIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean ignoreIndex;

    public static final String JSON_PROPERTY_GEN_PTS_INPUT = "GenPtsInput";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean genPtsInput;

    public static final String JSON_PROPERTY_SUPPORTS_TRANSCODING = "SupportsTranscoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsTranscoding;

    public static final String JSON_PROPERTY_SUPPORTS_DIRECT_STREAM = "SupportsDirectStream";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsDirectStream;

    public static final String JSON_PROPERTY_SUPPORTS_DIRECT_PLAY = "SupportsDirectPlay";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsDirectPlay;

    public static final String JSON_PROPERTY_IS_INFINITE_STREAM = "IsInfiniteStream";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isInfiniteStream;

    public static final String JSON_PROPERTY_USE_MOST_COMPATIBLE_TRANSCODING_PROFILE = "UseMostCompatibleTranscodingProfile";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean useMostCompatibleTranscodingProfile = false;

    public static final String JSON_PROPERTY_REQUIRES_OPENING = "RequiresOpening";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean requiresOpening;

    public static final String JSON_PROPERTY_OPEN_TOKEN = "OpenToken";
    @org.eclipse.jdt.annotation.NonNull
    private String openToken;

    public static final String JSON_PROPERTY_REQUIRES_CLOSING = "RequiresClosing";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean requiresClosing;

    public static final String JSON_PROPERTY_LIVE_STREAM_ID = "LiveStreamId";
    @org.eclipse.jdt.annotation.NonNull
    private String liveStreamId;

    public static final String JSON_PROPERTY_BUFFER_MS = "BufferMs";
    @org.eclipse.jdt.annotation.NonNull
    private Integer bufferMs;

    public static final String JSON_PROPERTY_REQUIRES_LOOPING = "RequiresLooping";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean requiresLooping;

    public static final String JSON_PROPERTY_SUPPORTS_PROBING = "SupportsProbing";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsProbing;

    public static final String JSON_PROPERTY_VIDEO_TYPE = "VideoType";
    @org.eclipse.jdt.annotation.NonNull
    private VideoType videoType;

    public static final String JSON_PROPERTY_ISO_TYPE = "IsoType";
    @org.eclipse.jdt.annotation.NonNull
    private IsoType isoType;

    public static final String JSON_PROPERTY_VIDEO3_D_FORMAT = "Video3DFormat";
    @org.eclipse.jdt.annotation.NonNull
    private Video3DFormat video3DFormat;

    public static final String JSON_PROPERTY_MEDIA_STREAMS = "MediaStreams";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaStream> mediaStreams;

    public static final String JSON_PROPERTY_MEDIA_ATTACHMENTS = "MediaAttachments";
    @org.eclipse.jdt.annotation.NonNull
    private List<MediaAttachment> mediaAttachments;

    public static final String JSON_PROPERTY_FORMATS = "Formats";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> formats;

    public static final String JSON_PROPERTY_BITRATE = "Bitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer bitrate;

    public static final String JSON_PROPERTY_FALLBACK_MAX_STREAMING_BITRATE = "FallbackMaxStreamingBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer fallbackMaxStreamingBitrate;

    public static final String JSON_PROPERTY_TIMESTAMP = "Timestamp";
    @org.eclipse.jdt.annotation.NonNull
    private TransportStreamTimestamp timestamp;

    public static final String JSON_PROPERTY_REQUIRED_HTTP_HEADERS = "RequiredHttpHeaders";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> requiredHttpHeaders;

    public static final String JSON_PROPERTY_TRANSCODING_URL = "TranscodingUrl";
    @org.eclipse.jdt.annotation.NonNull
    private String transcodingUrl;

    public static final String JSON_PROPERTY_TRANSCODING_SUB_PROTOCOL = "TranscodingSubProtocol";
    @org.eclipse.jdt.annotation.NonNull
    private MediaStreamProtocol transcodingSubProtocol;

    public static final String JSON_PROPERTY_TRANSCODING_CONTAINER = "TranscodingContainer";
    @org.eclipse.jdt.annotation.NonNull
    private String transcodingContainer;

    public static final String JSON_PROPERTY_ANALYZE_DURATION_MS = "AnalyzeDurationMs";
    @org.eclipse.jdt.annotation.NonNull
    private Integer analyzeDurationMs;

    public static final String JSON_PROPERTY_DEFAULT_AUDIO_STREAM_INDEX = "DefaultAudioStreamIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer defaultAudioStreamIndex;

    public static final String JSON_PROPERTY_DEFAULT_SUBTITLE_STREAM_INDEX = "DefaultSubtitleStreamIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer defaultSubtitleStreamIndex;

    public static final String JSON_PROPERTY_HAS_SEGMENTS = "HasSegments";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean hasSegments;

    public MediaSourceInfo() {
    }

    public MediaSourceInfo protocol(@org.eclipse.jdt.annotation.NonNull MediaProtocol protocol) {
        this.protocol = protocol;
        return this;
    }

    /**
     * Get protocol
     * 
     * @return protocol
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROTOCOL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaProtocol getProtocol() {
        return protocol;
    }

    @JsonProperty(JSON_PROPERTY_PROTOCOL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProtocol(@org.eclipse.jdt.annotation.NonNull MediaProtocol protocol) {
        this.protocol = protocol;
    }

    public MediaSourceInfo id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
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

    public MediaSourceInfo path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Get path
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPath() {
        return path;
    }

    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public MediaSourceInfo encoderPath(@org.eclipse.jdt.annotation.NonNull String encoderPath) {
        this.encoderPath = encoderPath;
        return this;
    }

    /**
     * Get encoderPath
     * 
     * @return encoderPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENCODER_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEncoderPath() {
        return encoderPath;
    }

    @JsonProperty(JSON_PROPERTY_ENCODER_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncoderPath(@org.eclipse.jdt.annotation.NonNull String encoderPath) {
        this.encoderPath = encoderPath;
    }

    public MediaSourceInfo encoderProtocol(@org.eclipse.jdt.annotation.NonNull MediaProtocol encoderProtocol) {
        this.encoderProtocol = encoderProtocol;
        return this;
    }

    /**
     * Get encoderProtocol
     * 
     * @return encoderProtocol
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ENCODER_PROTOCOL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaProtocol getEncoderProtocol() {
        return encoderProtocol;
    }

    @JsonProperty(JSON_PROPERTY_ENCODER_PROTOCOL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEncoderProtocol(@org.eclipse.jdt.annotation.NonNull MediaProtocol encoderProtocol) {
        this.encoderProtocol = encoderProtocol;
    }

    public MediaSourceInfo type(@org.eclipse.jdt.annotation.NonNull MediaSourceType type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaSourceType getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull MediaSourceType type) {
        this.type = type;
    }

    public MediaSourceInfo container(@org.eclipse.jdt.annotation.NonNull String container) {
        this.container = container;
        return this;
    }

    /**
     * Get container
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

    public MediaSourceInfo size(@org.eclipse.jdt.annotation.NonNull Long size) {
        this.size = size;
        return this;
    }

    /**
     * Get size
     * 
     * @return size
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getSize() {
        return size;
    }

    @JsonProperty(JSON_PROPERTY_SIZE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSize(@org.eclipse.jdt.annotation.NonNull Long size) {
        this.size = size;
    }

    public MediaSourceInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
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

    public MediaSourceInfo isRemote(@org.eclipse.jdt.annotation.NonNull Boolean isRemote) {
        this.isRemote = isRemote;
        return this;
    }

    /**
     * Gets or sets a value indicating whether the media is remote. Differentiate internet url vs local network.
     * 
     * @return isRemote
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_REMOTE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsRemote() {
        return isRemote;
    }

    @JsonProperty(JSON_PROPERTY_IS_REMOTE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsRemote(@org.eclipse.jdt.annotation.NonNull Boolean isRemote) {
        this.isRemote = isRemote;
    }

    public MediaSourceInfo etag(@org.eclipse.jdt.annotation.NonNull String etag) {
        this.etag = etag;
        return this;
    }

    /**
     * Get etag
     * 
     * @return etag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ETAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getEtag() {
        return etag;
    }

    @JsonProperty(JSON_PROPERTY_ETAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEtag(@org.eclipse.jdt.annotation.NonNull String etag) {
        this.etag = etag;
    }

    public MediaSourceInfo runTimeTicks(@org.eclipse.jdt.annotation.NonNull Long runTimeTicks) {
        this.runTimeTicks = runTimeTicks;
        return this;
    }

    /**
     * Get runTimeTicks
     * 
     * @return runTimeTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_RUN_TIME_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getRunTimeTicks() {
        return runTimeTicks;
    }

    @JsonProperty(JSON_PROPERTY_RUN_TIME_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRunTimeTicks(@org.eclipse.jdt.annotation.NonNull Long runTimeTicks) {
        this.runTimeTicks = runTimeTicks;
    }

    public MediaSourceInfo readAtNativeFramerate(@org.eclipse.jdt.annotation.NonNull Boolean readAtNativeFramerate) {
        this.readAtNativeFramerate = readAtNativeFramerate;
        return this;
    }

    /**
     * Get readAtNativeFramerate
     * 
     * @return readAtNativeFramerate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_READ_AT_NATIVE_FRAMERATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getReadAtNativeFramerate() {
        return readAtNativeFramerate;
    }

    @JsonProperty(JSON_PROPERTY_READ_AT_NATIVE_FRAMERATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReadAtNativeFramerate(@org.eclipse.jdt.annotation.NonNull Boolean readAtNativeFramerate) {
        this.readAtNativeFramerate = readAtNativeFramerate;
    }

    public MediaSourceInfo ignoreDts(@org.eclipse.jdt.annotation.NonNull Boolean ignoreDts) {
        this.ignoreDts = ignoreDts;
        return this;
    }

    /**
     * Get ignoreDts
     * 
     * @return ignoreDts
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IGNORE_DTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIgnoreDts() {
        return ignoreDts;
    }

    @JsonProperty(JSON_PROPERTY_IGNORE_DTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIgnoreDts(@org.eclipse.jdt.annotation.NonNull Boolean ignoreDts) {
        this.ignoreDts = ignoreDts;
    }

    public MediaSourceInfo ignoreIndex(@org.eclipse.jdt.annotation.NonNull Boolean ignoreIndex) {
        this.ignoreIndex = ignoreIndex;
        return this;
    }

    /**
     * Get ignoreIndex
     * 
     * @return ignoreIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IGNORE_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIgnoreIndex() {
        return ignoreIndex;
    }

    @JsonProperty(JSON_PROPERTY_IGNORE_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIgnoreIndex(@org.eclipse.jdt.annotation.NonNull Boolean ignoreIndex) {
        this.ignoreIndex = ignoreIndex;
    }

    public MediaSourceInfo genPtsInput(@org.eclipse.jdt.annotation.NonNull Boolean genPtsInput) {
        this.genPtsInput = genPtsInput;
        return this;
    }

    /**
     * Get genPtsInput
     * 
     * @return genPtsInput
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_GEN_PTS_INPUT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getGenPtsInput() {
        return genPtsInput;
    }

    @JsonProperty(JSON_PROPERTY_GEN_PTS_INPUT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGenPtsInput(@org.eclipse.jdt.annotation.NonNull Boolean genPtsInput) {
        this.genPtsInput = genPtsInput;
    }

    public MediaSourceInfo supportsTranscoding(@org.eclipse.jdt.annotation.NonNull Boolean supportsTranscoding) {
        this.supportsTranscoding = supportsTranscoding;
        return this;
    }

    /**
     * Get supportsTranscoding
     * 
     * @return supportsTranscoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_TRANSCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsTranscoding() {
        return supportsTranscoding;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_TRANSCODING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsTranscoding(@org.eclipse.jdt.annotation.NonNull Boolean supportsTranscoding) {
        this.supportsTranscoding = supportsTranscoding;
    }

    public MediaSourceInfo supportsDirectStream(@org.eclipse.jdt.annotation.NonNull Boolean supportsDirectStream) {
        this.supportsDirectStream = supportsDirectStream;
        return this;
    }

    /**
     * Get supportsDirectStream
     * 
     * @return supportsDirectStream
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_DIRECT_STREAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsDirectStream() {
        return supportsDirectStream;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_DIRECT_STREAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsDirectStream(@org.eclipse.jdt.annotation.NonNull Boolean supportsDirectStream) {
        this.supportsDirectStream = supportsDirectStream;
    }

    public MediaSourceInfo supportsDirectPlay(@org.eclipse.jdt.annotation.NonNull Boolean supportsDirectPlay) {
        this.supportsDirectPlay = supportsDirectPlay;
        return this;
    }

    /**
     * Get supportsDirectPlay
     * 
     * @return supportsDirectPlay
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_DIRECT_PLAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsDirectPlay() {
        return supportsDirectPlay;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_DIRECT_PLAY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsDirectPlay(@org.eclipse.jdt.annotation.NonNull Boolean supportsDirectPlay) {
        this.supportsDirectPlay = supportsDirectPlay;
    }

    public MediaSourceInfo isInfiniteStream(@org.eclipse.jdt.annotation.NonNull Boolean isInfiniteStream) {
        this.isInfiniteStream = isInfiniteStream;
        return this;
    }

    /**
     * Get isInfiniteStream
     * 
     * @return isInfiniteStream
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_INFINITE_STREAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsInfiniteStream() {
        return isInfiniteStream;
    }

    @JsonProperty(JSON_PROPERTY_IS_INFINITE_STREAM)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsInfiniteStream(@org.eclipse.jdt.annotation.NonNull Boolean isInfiniteStream) {
        this.isInfiniteStream = isInfiniteStream;
    }

    public MediaSourceInfo useMostCompatibleTranscodingProfile(
            @org.eclipse.jdt.annotation.NonNull Boolean useMostCompatibleTranscodingProfile) {
        this.useMostCompatibleTranscodingProfile = useMostCompatibleTranscodingProfile;
        return this;
    }

    /**
     * Get useMostCompatibleTranscodingProfile
     * 
     * @return useMostCompatibleTranscodingProfile
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_USE_MOST_COMPATIBLE_TRANSCODING_PROFILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getUseMostCompatibleTranscodingProfile() {
        return useMostCompatibleTranscodingProfile;
    }

    @JsonProperty(JSON_PROPERTY_USE_MOST_COMPATIBLE_TRANSCODING_PROFILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUseMostCompatibleTranscodingProfile(
            @org.eclipse.jdt.annotation.NonNull Boolean useMostCompatibleTranscodingProfile) {
        this.useMostCompatibleTranscodingProfile = useMostCompatibleTranscodingProfile;
    }

    public MediaSourceInfo requiresOpening(@org.eclipse.jdt.annotation.NonNull Boolean requiresOpening) {
        this.requiresOpening = requiresOpening;
        return this;
    }

    /**
     * Get requiresOpening
     * 
     * @return requiresOpening
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REQUIRES_OPENING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRequiresOpening() {
        return requiresOpening;
    }

    @JsonProperty(JSON_PROPERTY_REQUIRES_OPENING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequiresOpening(@org.eclipse.jdt.annotation.NonNull Boolean requiresOpening) {
        this.requiresOpening = requiresOpening;
    }

    public MediaSourceInfo openToken(@org.eclipse.jdt.annotation.NonNull String openToken) {
        this.openToken = openToken;
        return this;
    }

    /**
     * Get openToken
     * 
     * @return openToken
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_OPEN_TOKEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOpenToken() {
        return openToken;
    }

    @JsonProperty(JSON_PROPERTY_OPEN_TOKEN)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOpenToken(@org.eclipse.jdt.annotation.NonNull String openToken) {
        this.openToken = openToken;
    }

    public MediaSourceInfo requiresClosing(@org.eclipse.jdt.annotation.NonNull Boolean requiresClosing) {
        this.requiresClosing = requiresClosing;
        return this;
    }

    /**
     * Get requiresClosing
     * 
     * @return requiresClosing
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REQUIRES_CLOSING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRequiresClosing() {
        return requiresClosing;
    }

    @JsonProperty(JSON_PROPERTY_REQUIRES_CLOSING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequiresClosing(@org.eclipse.jdt.annotation.NonNull Boolean requiresClosing) {
        this.requiresClosing = requiresClosing;
    }

    public MediaSourceInfo liveStreamId(@org.eclipse.jdt.annotation.NonNull String liveStreamId) {
        this.liveStreamId = liveStreamId;
        return this;
    }

    /**
     * Get liveStreamId
     * 
     * @return liveStreamId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LIVE_STREAM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getLiveStreamId() {
        return liveStreamId;
    }

    @JsonProperty(JSON_PROPERTY_LIVE_STREAM_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLiveStreamId(@org.eclipse.jdt.annotation.NonNull String liveStreamId) {
        this.liveStreamId = liveStreamId;
    }

    public MediaSourceInfo bufferMs(@org.eclipse.jdt.annotation.NonNull Integer bufferMs) {
        this.bufferMs = bufferMs;
        return this;
    }

    /**
     * Get bufferMs
     * 
     * @return bufferMs
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BUFFER_MS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getBufferMs() {
        return bufferMs;
    }

    @JsonProperty(JSON_PROPERTY_BUFFER_MS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBufferMs(@org.eclipse.jdt.annotation.NonNull Integer bufferMs) {
        this.bufferMs = bufferMs;
    }

    public MediaSourceInfo requiresLooping(@org.eclipse.jdt.annotation.NonNull Boolean requiresLooping) {
        this.requiresLooping = requiresLooping;
        return this;
    }

    /**
     * Get requiresLooping
     * 
     * @return requiresLooping
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REQUIRES_LOOPING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getRequiresLooping() {
        return requiresLooping;
    }

    @JsonProperty(JSON_PROPERTY_REQUIRES_LOOPING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequiresLooping(@org.eclipse.jdt.annotation.NonNull Boolean requiresLooping) {
        this.requiresLooping = requiresLooping;
    }

    public MediaSourceInfo supportsProbing(@org.eclipse.jdt.annotation.NonNull Boolean supportsProbing) {
        this.supportsProbing = supportsProbing;
        return this;
    }

    /**
     * Get supportsProbing
     * 
     * @return supportsProbing
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTS_PROBING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsProbing() {
        return supportsProbing;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTS_PROBING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsProbing(@org.eclipse.jdt.annotation.NonNull Boolean supportsProbing) {
        this.supportsProbing = supportsProbing;
    }

    public MediaSourceInfo videoType(@org.eclipse.jdt.annotation.NonNull VideoType videoType) {
        this.videoType = videoType;
        return this;
    }

    /**
     * Get videoType
     * 
     * @return videoType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VIDEO_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public VideoType getVideoType() {
        return videoType;
    }

    @JsonProperty(JSON_PROPERTY_VIDEO_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVideoType(@org.eclipse.jdt.annotation.NonNull VideoType videoType) {
        this.videoType = videoType;
    }

    public MediaSourceInfo isoType(@org.eclipse.jdt.annotation.NonNull IsoType isoType) {
        this.isoType = isoType;
        return this;
    }

    /**
     * Get isoType
     * 
     * @return isoType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ISO_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public IsoType getIsoType() {
        return isoType;
    }

    @JsonProperty(JSON_PROPERTY_ISO_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsoType(@org.eclipse.jdt.annotation.NonNull IsoType isoType) {
        this.isoType = isoType;
    }

    public MediaSourceInfo video3DFormat(@org.eclipse.jdt.annotation.NonNull Video3DFormat video3DFormat) {
        this.video3DFormat = video3DFormat;
        return this;
    }

    /**
     * Get video3DFormat
     * 
     * @return video3DFormat
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VIDEO3_D_FORMAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Video3DFormat getVideo3DFormat() {
        return video3DFormat;
    }

    @JsonProperty(JSON_PROPERTY_VIDEO3_D_FORMAT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setVideo3DFormat(@org.eclipse.jdt.annotation.NonNull Video3DFormat video3DFormat) {
        this.video3DFormat = video3DFormat;
    }

    public MediaSourceInfo mediaStreams(@org.eclipse.jdt.annotation.NonNull List<MediaStream> mediaStreams) {
        this.mediaStreams = mediaStreams;
        return this;
    }

    public MediaSourceInfo addMediaStreamsItem(MediaStream mediaStreamsItem) {
        if (this.mediaStreams == null) {
            this.mediaStreams = new ArrayList<>();
        }
        this.mediaStreams.add(mediaStreamsItem);
        return this;
    }

    /**
     * Get mediaStreams
     * 
     * @return mediaStreams
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MEDIA_STREAMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaStream> getMediaStreams() {
        return mediaStreams;
    }

    @JsonProperty(JSON_PROPERTY_MEDIA_STREAMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaStreams(@org.eclipse.jdt.annotation.NonNull List<MediaStream> mediaStreams) {
        this.mediaStreams = mediaStreams;
    }

    public MediaSourceInfo mediaAttachments(
            @org.eclipse.jdt.annotation.NonNull List<MediaAttachment> mediaAttachments) {
        this.mediaAttachments = mediaAttachments;
        return this;
    }

    public MediaSourceInfo addMediaAttachmentsItem(MediaAttachment mediaAttachmentsItem) {
        if (this.mediaAttachments == null) {
            this.mediaAttachments = new ArrayList<>();
        }
        this.mediaAttachments.add(mediaAttachmentsItem);
        return this;
    }

    /**
     * Get mediaAttachments
     * 
     * @return mediaAttachments
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MEDIA_ATTACHMENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<MediaAttachment> getMediaAttachments() {
        return mediaAttachments;
    }

    @JsonProperty(JSON_PROPERTY_MEDIA_ATTACHMENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaAttachments(@org.eclipse.jdt.annotation.NonNull List<MediaAttachment> mediaAttachments) {
        this.mediaAttachments = mediaAttachments;
    }

    public MediaSourceInfo formats(@org.eclipse.jdt.annotation.NonNull List<String> formats) {
        this.formats = formats;
        return this;
    }

    public MediaSourceInfo addFormatsItem(String formatsItem) {
        if (this.formats == null) {
            this.formats = new ArrayList<>();
        }
        this.formats.add(formatsItem);
        return this;
    }

    /**
     * Get formats
     * 
     * @return formats
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FORMATS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getFormats() {
        return formats;
    }

    @JsonProperty(JSON_PROPERTY_FORMATS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFormats(@org.eclipse.jdt.annotation.NonNull List<String> formats) {
        this.formats = formats;
    }

    public MediaSourceInfo bitrate(@org.eclipse.jdt.annotation.NonNull Integer bitrate) {
        this.bitrate = bitrate;
        return this;
    }

    /**
     * Get bitrate
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

    public MediaSourceInfo fallbackMaxStreamingBitrate(
            @org.eclipse.jdt.annotation.NonNull Integer fallbackMaxStreamingBitrate) {
        this.fallbackMaxStreamingBitrate = fallbackMaxStreamingBitrate;
        return this;
    }

    /**
     * Get fallbackMaxStreamingBitrate
     * 
     * @return fallbackMaxStreamingBitrate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_FALLBACK_MAX_STREAMING_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getFallbackMaxStreamingBitrate() {
        return fallbackMaxStreamingBitrate;
    }

    @JsonProperty(JSON_PROPERTY_FALLBACK_MAX_STREAMING_BITRATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFallbackMaxStreamingBitrate(
            @org.eclipse.jdt.annotation.NonNull Integer fallbackMaxStreamingBitrate) {
        this.fallbackMaxStreamingBitrate = fallbackMaxStreamingBitrate;
    }

    public MediaSourceInfo timestamp(@org.eclipse.jdt.annotation.NonNull TransportStreamTimestamp timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    /**
     * Get timestamp
     * 
     * @return timestamp
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TIMESTAMP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public TransportStreamTimestamp getTimestamp() {
        return timestamp;
    }

    @JsonProperty(JSON_PROPERTY_TIMESTAMP)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTimestamp(@org.eclipse.jdt.annotation.NonNull TransportStreamTimestamp timestamp) {
        this.timestamp = timestamp;
    }

    public MediaSourceInfo requiredHttpHeaders(
            @org.eclipse.jdt.annotation.NonNull Map<String, String> requiredHttpHeaders) {
        this.requiredHttpHeaders = requiredHttpHeaders;
        return this;
    }

    public MediaSourceInfo putRequiredHttpHeadersItem(String key, String requiredHttpHeadersItem) {
        if (this.requiredHttpHeaders == null) {
            this.requiredHttpHeaders = new HashMap<>();
        }
        this.requiredHttpHeaders.put(key, requiredHttpHeadersItem);
        return this;
    }

    /**
     * Get requiredHttpHeaders
     * 
     * @return requiredHttpHeaders
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_REQUIRED_HTTP_HEADERS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getRequiredHttpHeaders() {
        return requiredHttpHeaders;
    }

    @JsonProperty(JSON_PROPERTY_REQUIRED_HTTP_HEADERS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setRequiredHttpHeaders(@org.eclipse.jdt.annotation.NonNull Map<String, String> requiredHttpHeaders) {
        this.requiredHttpHeaders = requiredHttpHeaders;
    }

    public MediaSourceInfo transcodingUrl(@org.eclipse.jdt.annotation.NonNull String transcodingUrl) {
        this.transcodingUrl = transcodingUrl;
        return this;
    }

    /**
     * Get transcodingUrl
     * 
     * @return transcodingUrl
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRANSCODING_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTranscodingUrl() {
        return transcodingUrl;
    }

    @JsonProperty(JSON_PROPERTY_TRANSCODING_URL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingUrl(@org.eclipse.jdt.annotation.NonNull String transcodingUrl) {
        this.transcodingUrl = transcodingUrl;
    }

    public MediaSourceInfo transcodingSubProtocol(
            @org.eclipse.jdt.annotation.NonNull MediaStreamProtocol transcodingSubProtocol) {
        this.transcodingSubProtocol = transcodingSubProtocol;
        return this;
    }

    /**
     * Media streaming protocol. Lowercase for backwards compatibility.
     * 
     * @return transcodingSubProtocol
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRANSCODING_SUB_PROTOCOL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaStreamProtocol getTranscodingSubProtocol() {
        return transcodingSubProtocol;
    }

    @JsonProperty(JSON_PROPERTY_TRANSCODING_SUB_PROTOCOL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingSubProtocol(
            @org.eclipse.jdt.annotation.NonNull MediaStreamProtocol transcodingSubProtocol) {
        this.transcodingSubProtocol = transcodingSubProtocol;
    }

    public MediaSourceInfo transcodingContainer(@org.eclipse.jdt.annotation.NonNull String transcodingContainer) {
        this.transcodingContainer = transcodingContainer;
        return this;
    }

    /**
     * Get transcodingContainer
     * 
     * @return transcodingContainer
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TRANSCODING_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getTranscodingContainer() {
        return transcodingContainer;
    }

    @JsonProperty(JSON_PROPERTY_TRANSCODING_CONTAINER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTranscodingContainer(@org.eclipse.jdt.annotation.NonNull String transcodingContainer) {
        this.transcodingContainer = transcodingContainer;
    }

    public MediaSourceInfo analyzeDurationMs(@org.eclipse.jdt.annotation.NonNull Integer analyzeDurationMs) {
        this.analyzeDurationMs = analyzeDurationMs;
        return this;
    }

    /**
     * Get analyzeDurationMs
     * 
     * @return analyzeDurationMs
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ANALYZE_DURATION_MS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAnalyzeDurationMs() {
        return analyzeDurationMs;
    }

    @JsonProperty(JSON_PROPERTY_ANALYZE_DURATION_MS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAnalyzeDurationMs(@org.eclipse.jdt.annotation.NonNull Integer analyzeDurationMs) {
        this.analyzeDurationMs = analyzeDurationMs;
    }

    public MediaSourceInfo defaultAudioStreamIndex(
            @org.eclipse.jdt.annotation.NonNull Integer defaultAudioStreamIndex) {
        this.defaultAudioStreamIndex = defaultAudioStreamIndex;
        return this;
    }

    /**
     * Get defaultAudioStreamIndex
     * 
     * @return defaultAudioStreamIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEFAULT_AUDIO_STREAM_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getDefaultAudioStreamIndex() {
        return defaultAudioStreamIndex;
    }

    @JsonProperty(JSON_PROPERTY_DEFAULT_AUDIO_STREAM_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultAudioStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer defaultAudioStreamIndex) {
        this.defaultAudioStreamIndex = defaultAudioStreamIndex;
    }

    public MediaSourceInfo defaultSubtitleStreamIndex(
            @org.eclipse.jdt.annotation.NonNull Integer defaultSubtitleStreamIndex) {
        this.defaultSubtitleStreamIndex = defaultSubtitleStreamIndex;
        return this;
    }

    /**
     * Get defaultSubtitleStreamIndex
     * 
     * @return defaultSubtitleStreamIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEFAULT_SUBTITLE_STREAM_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getDefaultSubtitleStreamIndex() {
        return defaultSubtitleStreamIndex;
    }

    @JsonProperty(JSON_PROPERTY_DEFAULT_SUBTITLE_STREAM_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultSubtitleStreamIndex(@org.eclipse.jdt.annotation.NonNull Integer defaultSubtitleStreamIndex) {
        this.defaultSubtitleStreamIndex = defaultSubtitleStreamIndex;
    }

    public MediaSourceInfo hasSegments(@org.eclipse.jdt.annotation.NonNull Boolean hasSegments) {
        this.hasSegments = hasSegments;
        return this;
    }

    /**
     * Get hasSegments
     * 
     * @return hasSegments
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_HAS_SEGMENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getHasSegments() {
        return hasSegments;
    }

    @JsonProperty(JSON_PROPERTY_HAS_SEGMENTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setHasSegments(@org.eclipse.jdt.annotation.NonNull Boolean hasSegments) {
        this.hasSegments = hasSegments;
    }

    /**
     * Return true if this MediaSourceInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaSourceInfo mediaSourceInfo = (MediaSourceInfo) o;
        return Objects.equals(this.protocol, mediaSourceInfo.protocol) && Objects.equals(this.id, mediaSourceInfo.id)
                && Objects.equals(this.path, mediaSourceInfo.path)
                && Objects.equals(this.encoderPath, mediaSourceInfo.encoderPath)
                && Objects.equals(this.encoderProtocol, mediaSourceInfo.encoderProtocol)
                && Objects.equals(this.type, mediaSourceInfo.type)
                && Objects.equals(this.container, mediaSourceInfo.container)
                && Objects.equals(this.size, mediaSourceInfo.size) && Objects.equals(this.name, mediaSourceInfo.name)
                && Objects.equals(this.isRemote, mediaSourceInfo.isRemote)
                && Objects.equals(this.etag, mediaSourceInfo.etag)
                && Objects.equals(this.runTimeTicks, mediaSourceInfo.runTimeTicks)
                && Objects.equals(this.readAtNativeFramerate, mediaSourceInfo.readAtNativeFramerate)
                && Objects.equals(this.ignoreDts, mediaSourceInfo.ignoreDts)
                && Objects.equals(this.ignoreIndex, mediaSourceInfo.ignoreIndex)
                && Objects.equals(this.genPtsInput, mediaSourceInfo.genPtsInput)
                && Objects.equals(this.supportsTranscoding, mediaSourceInfo.supportsTranscoding)
                && Objects.equals(this.supportsDirectStream, mediaSourceInfo.supportsDirectStream)
                && Objects.equals(this.supportsDirectPlay, mediaSourceInfo.supportsDirectPlay)
                && Objects.equals(this.isInfiniteStream, mediaSourceInfo.isInfiniteStream)
                && Objects.equals(this.useMostCompatibleTranscodingProfile,
                        mediaSourceInfo.useMostCompatibleTranscodingProfile)
                && Objects.equals(this.requiresOpening, mediaSourceInfo.requiresOpening)
                && Objects.equals(this.openToken, mediaSourceInfo.openToken)
                && Objects.equals(this.requiresClosing, mediaSourceInfo.requiresClosing)
                && Objects.equals(this.liveStreamId, mediaSourceInfo.liveStreamId)
                && Objects.equals(this.bufferMs, mediaSourceInfo.bufferMs)
                && Objects.equals(this.requiresLooping, mediaSourceInfo.requiresLooping)
                && Objects.equals(this.supportsProbing, mediaSourceInfo.supportsProbing)
                && Objects.equals(this.videoType, mediaSourceInfo.videoType)
                && Objects.equals(this.isoType, mediaSourceInfo.isoType)
                && Objects.equals(this.video3DFormat, mediaSourceInfo.video3DFormat)
                && Objects.equals(this.mediaStreams, mediaSourceInfo.mediaStreams)
                && Objects.equals(this.mediaAttachments, mediaSourceInfo.mediaAttachments)
                && Objects.equals(this.formats, mediaSourceInfo.formats)
                && Objects.equals(this.bitrate, mediaSourceInfo.bitrate)
                && Objects.equals(this.fallbackMaxStreamingBitrate, mediaSourceInfo.fallbackMaxStreamingBitrate)
                && Objects.equals(this.timestamp, mediaSourceInfo.timestamp)
                && Objects.equals(this.requiredHttpHeaders, mediaSourceInfo.requiredHttpHeaders)
                && Objects.equals(this.transcodingUrl, mediaSourceInfo.transcodingUrl)
                && Objects.equals(this.transcodingSubProtocol, mediaSourceInfo.transcodingSubProtocol)
                && Objects.equals(this.transcodingContainer, mediaSourceInfo.transcodingContainer)
                && Objects.equals(this.analyzeDurationMs, mediaSourceInfo.analyzeDurationMs)
                && Objects.equals(this.defaultAudioStreamIndex, mediaSourceInfo.defaultAudioStreamIndex)
                && Objects.equals(this.defaultSubtitleStreamIndex, mediaSourceInfo.defaultSubtitleStreamIndex)
                && Objects.equals(this.hasSegments, mediaSourceInfo.hasSegments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(protocol, id, path, encoderPath, encoderProtocol, type, container, size, name, isRemote,
                etag, runTimeTicks, readAtNativeFramerate, ignoreDts, ignoreIndex, genPtsInput, supportsTranscoding,
                supportsDirectStream, supportsDirectPlay, isInfiniteStream, useMostCompatibleTranscodingProfile,
                requiresOpening, openToken, requiresClosing, liveStreamId, bufferMs, requiresLooping, supportsProbing,
                videoType, isoType, video3DFormat, mediaStreams, mediaAttachments, formats, bitrate,
                fallbackMaxStreamingBitrate, timestamp, requiredHttpHeaders, transcodingUrl, transcodingSubProtocol,
                transcodingContainer, analyzeDurationMs, defaultAudioStreamIndex, defaultSubtitleStreamIndex,
                hasSegments);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaSourceInfo {\n");
        sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    encoderPath: ").append(toIndentedString(encoderPath)).append("\n");
        sb.append("    encoderProtocol: ").append(toIndentedString(encoderProtocol)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    container: ").append(toIndentedString(container)).append("\n");
        sb.append("    size: ").append(toIndentedString(size)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    isRemote: ").append(toIndentedString(isRemote)).append("\n");
        sb.append("    etag: ").append(toIndentedString(etag)).append("\n");
        sb.append("    runTimeTicks: ").append(toIndentedString(runTimeTicks)).append("\n");
        sb.append("    readAtNativeFramerate: ").append(toIndentedString(readAtNativeFramerate)).append("\n");
        sb.append("    ignoreDts: ").append(toIndentedString(ignoreDts)).append("\n");
        sb.append("    ignoreIndex: ").append(toIndentedString(ignoreIndex)).append("\n");
        sb.append("    genPtsInput: ").append(toIndentedString(genPtsInput)).append("\n");
        sb.append("    supportsTranscoding: ").append(toIndentedString(supportsTranscoding)).append("\n");
        sb.append("    supportsDirectStream: ").append(toIndentedString(supportsDirectStream)).append("\n");
        sb.append("    supportsDirectPlay: ").append(toIndentedString(supportsDirectPlay)).append("\n");
        sb.append("    isInfiniteStream: ").append(toIndentedString(isInfiniteStream)).append("\n");
        sb.append("    useMostCompatibleTranscodingProfile: ")
                .append(toIndentedString(useMostCompatibleTranscodingProfile)).append("\n");
        sb.append("    requiresOpening: ").append(toIndentedString(requiresOpening)).append("\n");
        sb.append("    openToken: ").append(toIndentedString(openToken)).append("\n");
        sb.append("    requiresClosing: ").append(toIndentedString(requiresClosing)).append("\n");
        sb.append("    liveStreamId: ").append(toIndentedString(liveStreamId)).append("\n");
        sb.append("    bufferMs: ").append(toIndentedString(bufferMs)).append("\n");
        sb.append("    requiresLooping: ").append(toIndentedString(requiresLooping)).append("\n");
        sb.append("    supportsProbing: ").append(toIndentedString(supportsProbing)).append("\n");
        sb.append("    videoType: ").append(toIndentedString(videoType)).append("\n");
        sb.append("    isoType: ").append(toIndentedString(isoType)).append("\n");
        sb.append("    video3DFormat: ").append(toIndentedString(video3DFormat)).append("\n");
        sb.append("    mediaStreams: ").append(toIndentedString(mediaStreams)).append("\n");
        sb.append("    mediaAttachments: ").append(toIndentedString(mediaAttachments)).append("\n");
        sb.append("    formats: ").append(toIndentedString(formats)).append("\n");
        sb.append("    bitrate: ").append(toIndentedString(bitrate)).append("\n");
        sb.append("    fallbackMaxStreamingBitrate: ").append(toIndentedString(fallbackMaxStreamingBitrate))
                .append("\n");
        sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
        sb.append("    requiredHttpHeaders: ").append(toIndentedString(requiredHttpHeaders)).append("\n");
        sb.append("    transcodingUrl: ").append(toIndentedString(transcodingUrl)).append("\n");
        sb.append("    transcodingSubProtocol: ").append(toIndentedString(transcodingSubProtocol)).append("\n");
        sb.append("    transcodingContainer: ").append(toIndentedString(transcodingContainer)).append("\n");
        sb.append("    analyzeDurationMs: ").append(toIndentedString(analyzeDurationMs)).append("\n");
        sb.append("    defaultAudioStreamIndex: ").append(toIndentedString(defaultAudioStreamIndex)).append("\n");
        sb.append("    defaultSubtitleStreamIndex: ").append(toIndentedString(defaultSubtitleStreamIndex)).append("\n");
        sb.append("    hasSegments: ").append(toIndentedString(hasSegments)).append("\n");
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

        // add `Protocol` to the URL query string
        if (getProtocol() != null) {
            joiner.add(String.format("%sProtocol%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProtocol()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(
                    String.format("%sId%s=%s", prefix, suffix, ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format("%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `EncoderPath` to the URL query string
        if (getEncoderPath() != null) {
            joiner.add(String.format("%sEncoderPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncoderPath()))));
        }

        // add `EncoderProtocol` to the URL query string
        if (getEncoderProtocol() != null) {
            joiner.add(String.format("%sEncoderProtocol%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEncoderProtocol()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format("%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `Container` to the URL query string
        if (getContainer() != null) {
            joiner.add(String.format("%sContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContainer()))));
        }

        // add `Size` to the URL query string
        if (getSize() != null) {
            joiner.add(String.format("%sSize%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSize()))));
        }

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format("%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `IsRemote` to the URL query string
        if (getIsRemote() != null) {
            joiner.add(String.format("%sIsRemote%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsRemote()))));
        }

        // add `ETag` to the URL query string
        if (getEtag() != null) {
            joiner.add(String.format("%sETag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEtag()))));
        }

        // add `RunTimeTicks` to the URL query string
        if (getRunTimeTicks() != null) {
            joiner.add(String.format("%sRunTimeTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRunTimeTicks()))));
        }

        // add `ReadAtNativeFramerate` to the URL query string
        if (getReadAtNativeFramerate() != null) {
            joiner.add(String.format("%sReadAtNativeFramerate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getReadAtNativeFramerate()))));
        }

        // add `IgnoreDts` to the URL query string
        if (getIgnoreDts() != null) {
            joiner.add(String.format("%sIgnoreDts%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIgnoreDts()))));
        }

        // add `IgnoreIndex` to the URL query string
        if (getIgnoreIndex() != null) {
            joiner.add(String.format("%sIgnoreIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIgnoreIndex()))));
        }

        // add `GenPtsInput` to the URL query string
        if (getGenPtsInput() != null) {
            joiner.add(String.format("%sGenPtsInput%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getGenPtsInput()))));
        }

        // add `SupportsTranscoding` to the URL query string
        if (getSupportsTranscoding() != null) {
            joiner.add(String.format("%sSupportsTranscoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsTranscoding()))));
        }

        // add `SupportsDirectStream` to the URL query string
        if (getSupportsDirectStream() != null) {
            joiner.add(String.format("%sSupportsDirectStream%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsDirectStream()))));
        }

        // add `SupportsDirectPlay` to the URL query string
        if (getSupportsDirectPlay() != null) {
            joiner.add(String.format("%sSupportsDirectPlay%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsDirectPlay()))));
        }

        // add `IsInfiniteStream` to the URL query string
        if (getIsInfiniteStream() != null) {
            joiner.add(String.format("%sIsInfiniteStream%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsInfiniteStream()))));
        }

        // add `UseMostCompatibleTranscodingProfile` to the URL query string
        if (getUseMostCompatibleTranscodingProfile() != null) {
            joiner.add(String.format("%sUseMostCompatibleTranscodingProfile%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUseMostCompatibleTranscodingProfile()))));
        }

        // add `RequiresOpening` to the URL query string
        if (getRequiresOpening() != null) {
            joiner.add(String.format("%sRequiresOpening%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRequiresOpening()))));
        }

        // add `OpenToken` to the URL query string
        if (getOpenToken() != null) {
            joiner.add(String.format("%sOpenToken%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOpenToken()))));
        }

        // add `RequiresClosing` to the URL query string
        if (getRequiresClosing() != null) {
            joiner.add(String.format("%sRequiresClosing%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRequiresClosing()))));
        }

        // add `LiveStreamId` to the URL query string
        if (getLiveStreamId() != null) {
            joiner.add(String.format("%sLiveStreamId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getLiveStreamId()))));
        }

        // add `BufferMs` to the URL query string
        if (getBufferMs() != null) {
            joiner.add(String.format("%sBufferMs%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBufferMs()))));
        }

        // add `RequiresLooping` to the URL query string
        if (getRequiresLooping() != null) {
            joiner.add(String.format("%sRequiresLooping%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getRequiresLooping()))));
        }

        // add `SupportsProbing` to the URL query string
        if (getSupportsProbing() != null) {
            joiner.add(String.format("%sSupportsProbing%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsProbing()))));
        }

        // add `VideoType` to the URL query string
        if (getVideoType() != null) {
            joiner.add(String.format("%sVideoType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideoType()))));
        }

        // add `IsoType` to the URL query string
        if (getIsoType() != null) {
            joiner.add(String.format("%sIsoType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsoType()))));
        }

        // add `Video3DFormat` to the URL query string
        if (getVideo3DFormat() != null) {
            joiner.add(String.format("%sVideo3DFormat%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getVideo3DFormat()))));
        }

        // add `MediaStreams` to the URL query string
        if (getMediaStreams() != null) {
            for (int i = 0; i < getMediaStreams().size(); i++) {
                if (getMediaStreams().get(i) != null) {
                    joiner.add(getMediaStreams().get(i).toUrlQueryString(String.format("%sMediaStreams%s%s", prefix,
                            suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `MediaAttachments` to the URL query string
        if (getMediaAttachments() != null) {
            for (int i = 0; i < getMediaAttachments().size(); i++) {
                if (getMediaAttachments().get(i) != null) {
                    joiner.add(getMediaAttachments().get(i).toUrlQueryString(String.format("%sMediaAttachments%s%s",
                            prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `Formats` to the URL query string
        if (getFormats() != null) {
            for (int i = 0; i < getFormats().size(); i++) {
                joiner.add(String.format("%sFormats%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getFormats().get(i)))));
            }
        }

        // add `Bitrate` to the URL query string
        if (getBitrate() != null) {
            joiner.add(String.format("%sBitrate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBitrate()))));
        }

        // add `FallbackMaxStreamingBitrate` to the URL query string
        if (getFallbackMaxStreamingBitrate() != null) {
            joiner.add(String.format("%sFallbackMaxStreamingBitrate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFallbackMaxStreamingBitrate()))));
        }

        // add `Timestamp` to the URL query string
        if (getTimestamp() != null) {
            joiner.add(String.format("%sTimestamp%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTimestamp()))));
        }

        // add `RequiredHttpHeaders` to the URL query string
        if (getRequiredHttpHeaders() != null) {
            for (String _key : getRequiredHttpHeaders().keySet()) {
                joiner.add(String.format("%sRequiredHttpHeaders%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, _key, containerSuffix),
                        getRequiredHttpHeaders().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getRequiredHttpHeaders().get(_key)))));
            }
        }

        // add `TranscodingUrl` to the URL query string
        if (getTranscodingUrl() != null) {
            joiner.add(String.format("%sTranscodingUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTranscodingUrl()))));
        }

        // add `TranscodingSubProtocol` to the URL query string
        if (getTranscodingSubProtocol() != null) {
            joiner.add(String.format("%sTranscodingSubProtocol%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTranscodingSubProtocol()))));
        }

        // add `TranscodingContainer` to the URL query string
        if (getTranscodingContainer() != null) {
            joiner.add(String.format("%sTranscodingContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTranscodingContainer()))));
        }

        // add `AnalyzeDurationMs` to the URL query string
        if (getAnalyzeDurationMs() != null) {
            joiner.add(String.format("%sAnalyzeDurationMs%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAnalyzeDurationMs()))));
        }

        // add `DefaultAudioStreamIndex` to the URL query string
        if (getDefaultAudioStreamIndex() != null) {
            joiner.add(String.format("%sDefaultAudioStreamIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDefaultAudioStreamIndex()))));
        }

        // add `DefaultSubtitleStreamIndex` to the URL query string
        if (getDefaultSubtitleStreamIndex() != null) {
            joiner.add(String.format("%sDefaultSubtitleStreamIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDefaultSubtitleStreamIndex()))));
        }

        // add `HasSegments` to the URL query string
        if (getHasSegments() != null) {
            joiner.add(String.format("%sHasSegments%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getHasSegments()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MediaSourceInfo instance;

        public Builder() {
            this(new MediaSourceInfo());
        }

        protected Builder(MediaSourceInfo instance) {
            this.instance = instance;
        }

        public MediaSourceInfo.Builder protocol(MediaProtocol protocol) {
            this.instance.protocol = protocol;
            return this;
        }

        public MediaSourceInfo.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public MediaSourceInfo.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public MediaSourceInfo.Builder encoderPath(String encoderPath) {
            this.instance.encoderPath = encoderPath;
            return this;
        }

        public MediaSourceInfo.Builder encoderProtocol(MediaProtocol encoderProtocol) {
            this.instance.encoderProtocol = encoderProtocol;
            return this;
        }

        public MediaSourceInfo.Builder type(MediaSourceType type) {
            this.instance.type = type;
            return this;
        }

        public MediaSourceInfo.Builder container(String container) {
            this.instance.container = container;
            return this;
        }

        public MediaSourceInfo.Builder size(Long size) {
            this.instance.size = size;
            return this;
        }

        public MediaSourceInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public MediaSourceInfo.Builder isRemote(Boolean isRemote) {
            this.instance.isRemote = isRemote;
            return this;
        }

        public MediaSourceInfo.Builder etag(String etag) {
            this.instance.etag = etag;
            return this;
        }

        public MediaSourceInfo.Builder runTimeTicks(Long runTimeTicks) {
            this.instance.runTimeTicks = runTimeTicks;
            return this;
        }

        public MediaSourceInfo.Builder readAtNativeFramerate(Boolean readAtNativeFramerate) {
            this.instance.readAtNativeFramerate = readAtNativeFramerate;
            return this;
        }

        public MediaSourceInfo.Builder ignoreDts(Boolean ignoreDts) {
            this.instance.ignoreDts = ignoreDts;
            return this;
        }

        public MediaSourceInfo.Builder ignoreIndex(Boolean ignoreIndex) {
            this.instance.ignoreIndex = ignoreIndex;
            return this;
        }

        public MediaSourceInfo.Builder genPtsInput(Boolean genPtsInput) {
            this.instance.genPtsInput = genPtsInput;
            return this;
        }

        public MediaSourceInfo.Builder supportsTranscoding(Boolean supportsTranscoding) {
            this.instance.supportsTranscoding = supportsTranscoding;
            return this;
        }

        public MediaSourceInfo.Builder supportsDirectStream(Boolean supportsDirectStream) {
            this.instance.supportsDirectStream = supportsDirectStream;
            return this;
        }

        public MediaSourceInfo.Builder supportsDirectPlay(Boolean supportsDirectPlay) {
            this.instance.supportsDirectPlay = supportsDirectPlay;
            return this;
        }

        public MediaSourceInfo.Builder isInfiniteStream(Boolean isInfiniteStream) {
            this.instance.isInfiniteStream = isInfiniteStream;
            return this;
        }

        public MediaSourceInfo.Builder useMostCompatibleTranscodingProfile(
                Boolean useMostCompatibleTranscodingProfile) {
            this.instance.useMostCompatibleTranscodingProfile = useMostCompatibleTranscodingProfile;
            return this;
        }

        public MediaSourceInfo.Builder requiresOpening(Boolean requiresOpening) {
            this.instance.requiresOpening = requiresOpening;
            return this;
        }

        public MediaSourceInfo.Builder openToken(String openToken) {
            this.instance.openToken = openToken;
            return this;
        }

        public MediaSourceInfo.Builder requiresClosing(Boolean requiresClosing) {
            this.instance.requiresClosing = requiresClosing;
            return this;
        }

        public MediaSourceInfo.Builder liveStreamId(String liveStreamId) {
            this.instance.liveStreamId = liveStreamId;
            return this;
        }

        public MediaSourceInfo.Builder bufferMs(Integer bufferMs) {
            this.instance.bufferMs = bufferMs;
            return this;
        }

        public MediaSourceInfo.Builder requiresLooping(Boolean requiresLooping) {
            this.instance.requiresLooping = requiresLooping;
            return this;
        }

        public MediaSourceInfo.Builder supportsProbing(Boolean supportsProbing) {
            this.instance.supportsProbing = supportsProbing;
            return this;
        }

        public MediaSourceInfo.Builder videoType(VideoType videoType) {
            this.instance.videoType = videoType;
            return this;
        }

        public MediaSourceInfo.Builder isoType(IsoType isoType) {
            this.instance.isoType = isoType;
            return this;
        }

        public MediaSourceInfo.Builder video3DFormat(Video3DFormat video3DFormat) {
            this.instance.video3DFormat = video3DFormat;
            return this;
        }

        public MediaSourceInfo.Builder mediaStreams(List<MediaStream> mediaStreams) {
            this.instance.mediaStreams = mediaStreams;
            return this;
        }

        public MediaSourceInfo.Builder mediaAttachments(List<MediaAttachment> mediaAttachments) {
            this.instance.mediaAttachments = mediaAttachments;
            return this;
        }

        public MediaSourceInfo.Builder formats(List<String> formats) {
            this.instance.formats = formats;
            return this;
        }

        public MediaSourceInfo.Builder bitrate(Integer bitrate) {
            this.instance.bitrate = bitrate;
            return this;
        }

        public MediaSourceInfo.Builder fallbackMaxStreamingBitrate(Integer fallbackMaxStreamingBitrate) {
            this.instance.fallbackMaxStreamingBitrate = fallbackMaxStreamingBitrate;
            return this;
        }

        public MediaSourceInfo.Builder timestamp(TransportStreamTimestamp timestamp) {
            this.instance.timestamp = timestamp;
            return this;
        }

        public MediaSourceInfo.Builder requiredHttpHeaders(Map<String, String> requiredHttpHeaders) {
            this.instance.requiredHttpHeaders = requiredHttpHeaders;
            return this;
        }

        public MediaSourceInfo.Builder transcodingUrl(String transcodingUrl) {
            this.instance.transcodingUrl = transcodingUrl;
            return this;
        }

        public MediaSourceInfo.Builder transcodingSubProtocol(MediaStreamProtocol transcodingSubProtocol) {
            this.instance.transcodingSubProtocol = transcodingSubProtocol;
            return this;
        }

        public MediaSourceInfo.Builder transcodingContainer(String transcodingContainer) {
            this.instance.transcodingContainer = transcodingContainer;
            return this;
        }

        public MediaSourceInfo.Builder analyzeDurationMs(Integer analyzeDurationMs) {
            this.instance.analyzeDurationMs = analyzeDurationMs;
            return this;
        }

        public MediaSourceInfo.Builder defaultAudioStreamIndex(Integer defaultAudioStreamIndex) {
            this.instance.defaultAudioStreamIndex = defaultAudioStreamIndex;
            return this;
        }

        public MediaSourceInfo.Builder defaultSubtitleStreamIndex(Integer defaultSubtitleStreamIndex) {
            this.instance.defaultSubtitleStreamIndex = defaultSubtitleStreamIndex;
            return this;
        }

        public MediaSourceInfo.Builder hasSegments(Boolean hasSegments) {
            this.instance.hasSegments = hasSegments;
            return this;
        }

        /**
         * returns a built MediaSourceInfo instance.
         *
         * The builder is not reusable.
         */
        public MediaSourceInfo build() {
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
    public static MediaSourceInfo.Builder builder() {
        return new MediaSourceInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MediaSourceInfo.Builder toBuilder() {
        return new MediaSourceInfo.Builder().protocol(getProtocol()).id(getId()).path(getPath())
                .encoderPath(getEncoderPath()).encoderProtocol(getEncoderProtocol()).type(getType())
                .container(getContainer()).size(getSize()).name(getName()).isRemote(getIsRemote()).etag(getEtag())
                .runTimeTicks(getRunTimeTicks()).readAtNativeFramerate(getReadAtNativeFramerate())
                .ignoreDts(getIgnoreDts()).ignoreIndex(getIgnoreIndex()).genPtsInput(getGenPtsInput())
                .supportsTranscoding(getSupportsTranscoding()).supportsDirectStream(getSupportsDirectStream())
                .supportsDirectPlay(getSupportsDirectPlay()).isInfiniteStream(getIsInfiniteStream())
                .useMostCompatibleTranscodingProfile(getUseMostCompatibleTranscodingProfile())
                .requiresOpening(getRequiresOpening()).openToken(getOpenToken()).requiresClosing(getRequiresClosing())
                .liveStreamId(getLiveStreamId()).bufferMs(getBufferMs()).requiresLooping(getRequiresLooping())
                .supportsProbing(getSupportsProbing()).videoType(getVideoType()).isoType(getIsoType())
                .video3DFormat(getVideo3DFormat()).mediaStreams(getMediaStreams())
                .mediaAttachments(getMediaAttachments()).formats(getFormats()).bitrate(getBitrate())
                .fallbackMaxStreamingBitrate(getFallbackMaxStreamingBitrate()).timestamp(getTimestamp())
                .requiredHttpHeaders(getRequiredHttpHeaders()).transcodingUrl(getTranscodingUrl())
                .transcodingSubProtocol(getTranscodingSubProtocol()).transcodingContainer(getTranscodingContainer())
                .analyzeDurationMs(getAnalyzeDurationMs()).defaultAudioStreamIndex(getDefaultAudioStreamIndex())
                .defaultSubtitleStreamIndex(getDefaultSubtitleStreamIndex()).hasSegments(getHasSegments());
    }
}
