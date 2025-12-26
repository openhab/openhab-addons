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

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * TunerHostInfo
 */
@JsonPropertyOrder({ TunerHostInfo.JSON_PROPERTY_ID, TunerHostInfo.JSON_PROPERTY_URL, TunerHostInfo.JSON_PROPERTY_TYPE,
        TunerHostInfo.JSON_PROPERTY_DEVICE_ID, TunerHostInfo.JSON_PROPERTY_FRIENDLY_NAME,
        TunerHostInfo.JSON_PROPERTY_IMPORT_FAVORITES_ONLY, TunerHostInfo.JSON_PROPERTY_ALLOW_H_W_TRANSCODING,
        TunerHostInfo.JSON_PROPERTY_ALLOW_FMP4_TRANSCODING_CONTAINER, TunerHostInfo.JSON_PROPERTY_ALLOW_STREAM_SHARING,
        TunerHostInfo.JSON_PROPERTY_FALLBACK_MAX_STREAMING_BITRATE, TunerHostInfo.JSON_PROPERTY_ENABLE_STREAM_LOOPING,
        TunerHostInfo.JSON_PROPERTY_SOURCE, TunerHostInfo.JSON_PROPERTY_TUNER_COUNT,
        TunerHostInfo.JSON_PROPERTY_USER_AGENT, TunerHostInfo.JSON_PROPERTY_IGNORE_DTS,
        TunerHostInfo.JSON_PROPERTY_READ_AT_NATIVE_FRAMERATE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TunerHostInfo {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public static final String JSON_PROPERTY_URL = "Url";
    @org.eclipse.jdt.annotation.NonNull
    private String url;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private String type;

    public static final String JSON_PROPERTY_DEVICE_ID = "DeviceId";
    @org.eclipse.jdt.annotation.NonNull
    private String deviceId;

    public static final String JSON_PROPERTY_FRIENDLY_NAME = "FriendlyName";
    @org.eclipse.jdt.annotation.NonNull
    private String friendlyName;

    public static final String JSON_PROPERTY_IMPORT_FAVORITES_ONLY = "ImportFavoritesOnly";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean importFavoritesOnly;

    public static final String JSON_PROPERTY_ALLOW_H_W_TRANSCODING = "AllowHWTranscoding";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean allowHWTranscoding;

    public static final String JSON_PROPERTY_ALLOW_FMP4_TRANSCODING_CONTAINER = "AllowFmp4TranscodingContainer";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean allowFmp4TranscodingContainer;

    public static final String JSON_PROPERTY_ALLOW_STREAM_SHARING = "AllowStreamSharing";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean allowStreamSharing;

    public static final String JSON_PROPERTY_FALLBACK_MAX_STREAMING_BITRATE = "FallbackMaxStreamingBitrate";
    @org.eclipse.jdt.annotation.NonNull
    private Integer fallbackMaxStreamingBitrate;

    public static final String JSON_PROPERTY_ENABLE_STREAM_LOOPING = "EnableStreamLooping";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean enableStreamLooping;

    public static final String JSON_PROPERTY_SOURCE = "Source";
    @org.eclipse.jdt.annotation.NonNull
    private String source;

    public static final String JSON_PROPERTY_TUNER_COUNT = "TunerCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer tunerCount;

    public static final String JSON_PROPERTY_USER_AGENT = "UserAgent";
    @org.eclipse.jdt.annotation.NonNull
    private String userAgent;

    public static final String JSON_PROPERTY_IGNORE_DTS = "IgnoreDts";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean ignoreDts;

    public static final String JSON_PROPERTY_READ_AT_NATIVE_FRAMERATE = "ReadAtNativeFramerate";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean readAtNativeFramerate;

    public TunerHostInfo() {
    }

    public TunerHostInfo id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
    }

    public TunerHostInfo url(@org.eclipse.jdt.annotation.NonNull String url) {
        this.url = url;
        return this;
    }

    /**
     * Get url
     * 
     * @return url
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUrl() {
        return url;
    }

    @JsonProperty(value = JSON_PROPERTY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUrl(@org.eclipse.jdt.annotation.NonNull String url) {
        this.url = url;
    }

    public TunerHostInfo type(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
    }

    public TunerHostInfo deviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
        return this;
    }

    /**
     * Get deviceId
     * 
     * @return deviceId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getDeviceId() {
        return deviceId;
    }

    @JsonProperty(value = JSON_PROPERTY_DEVICE_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDeviceId(@org.eclipse.jdt.annotation.NonNull String deviceId) {
        this.deviceId = deviceId;
    }

    public TunerHostInfo friendlyName(@org.eclipse.jdt.annotation.NonNull String friendlyName) {
        this.friendlyName = friendlyName;
        return this;
    }

    /**
     * Get friendlyName
     * 
     * @return friendlyName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_FRIENDLY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getFriendlyName() {
        return friendlyName;
    }

    @JsonProperty(value = JSON_PROPERTY_FRIENDLY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFriendlyName(@org.eclipse.jdt.annotation.NonNull String friendlyName) {
        this.friendlyName = friendlyName;
    }

    public TunerHostInfo importFavoritesOnly(@org.eclipse.jdt.annotation.NonNull Boolean importFavoritesOnly) {
        this.importFavoritesOnly = importFavoritesOnly;
        return this;
    }

    /**
     * Get importFavoritesOnly
     * 
     * @return importFavoritesOnly
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IMPORT_FAVORITES_ONLY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getImportFavoritesOnly() {
        return importFavoritesOnly;
    }

    @JsonProperty(value = JSON_PROPERTY_IMPORT_FAVORITES_ONLY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImportFavoritesOnly(@org.eclipse.jdt.annotation.NonNull Boolean importFavoritesOnly) {
        this.importFavoritesOnly = importFavoritesOnly;
    }

    public TunerHostInfo allowHWTranscoding(@org.eclipse.jdt.annotation.NonNull Boolean allowHWTranscoding) {
        this.allowHWTranscoding = allowHWTranscoding;
        return this;
    }

    /**
     * Get allowHWTranscoding
     * 
     * @return allowHWTranscoding
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALLOW_H_W_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAllowHWTranscoding() {
        return allowHWTranscoding;
    }

    @JsonProperty(value = JSON_PROPERTY_ALLOW_H_W_TRANSCODING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowHWTranscoding(@org.eclipse.jdt.annotation.NonNull Boolean allowHWTranscoding) {
        this.allowHWTranscoding = allowHWTranscoding;
    }

    public TunerHostInfo allowFmp4TranscodingContainer(
            @org.eclipse.jdt.annotation.NonNull Boolean allowFmp4TranscodingContainer) {
        this.allowFmp4TranscodingContainer = allowFmp4TranscodingContainer;
        return this;
    }

    /**
     * Get allowFmp4TranscodingContainer
     * 
     * @return allowFmp4TranscodingContainer
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALLOW_FMP4_TRANSCODING_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAllowFmp4TranscodingContainer() {
        return allowFmp4TranscodingContainer;
    }

    @JsonProperty(value = JSON_PROPERTY_ALLOW_FMP4_TRANSCODING_CONTAINER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowFmp4TranscodingContainer(
            @org.eclipse.jdt.annotation.NonNull Boolean allowFmp4TranscodingContainer) {
        this.allowFmp4TranscodingContainer = allowFmp4TranscodingContainer;
    }

    public TunerHostInfo allowStreamSharing(@org.eclipse.jdt.annotation.NonNull Boolean allowStreamSharing) {
        this.allowStreamSharing = allowStreamSharing;
        return this;
    }

    /**
     * Get allowStreamSharing
     * 
     * @return allowStreamSharing
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ALLOW_STREAM_SHARING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getAllowStreamSharing() {
        return allowStreamSharing;
    }

    @JsonProperty(value = JSON_PROPERTY_ALLOW_STREAM_SHARING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAllowStreamSharing(@org.eclipse.jdt.annotation.NonNull Boolean allowStreamSharing) {
        this.allowStreamSharing = allowStreamSharing;
    }

    public TunerHostInfo fallbackMaxStreamingBitrate(
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
    @JsonProperty(value = JSON_PROPERTY_FALLBACK_MAX_STREAMING_BITRATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getFallbackMaxStreamingBitrate() {
        return fallbackMaxStreamingBitrate;
    }

    @JsonProperty(value = JSON_PROPERTY_FALLBACK_MAX_STREAMING_BITRATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFallbackMaxStreamingBitrate(
            @org.eclipse.jdt.annotation.NonNull Integer fallbackMaxStreamingBitrate) {
        this.fallbackMaxStreamingBitrate = fallbackMaxStreamingBitrate;
    }

    public TunerHostInfo enableStreamLooping(@org.eclipse.jdt.annotation.NonNull Boolean enableStreamLooping) {
        this.enableStreamLooping = enableStreamLooping;
        return this;
    }

    /**
     * Get enableStreamLooping
     * 
     * @return enableStreamLooping
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ENABLE_STREAM_LOOPING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getEnableStreamLooping() {
        return enableStreamLooping;
    }

    @JsonProperty(value = JSON_PROPERTY_ENABLE_STREAM_LOOPING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setEnableStreamLooping(@org.eclipse.jdt.annotation.NonNull Boolean enableStreamLooping) {
        this.enableStreamLooping = enableStreamLooping;
    }

    public TunerHostInfo source(@org.eclipse.jdt.annotation.NonNull String source) {
        this.source = source;
        return this;
    }

    /**
     * Get source
     * 
     * @return source
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SOURCE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getSource() {
        return source;
    }

    @JsonProperty(value = JSON_PROPERTY_SOURCE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSource(@org.eclipse.jdt.annotation.NonNull String source) {
        this.source = source;
    }

    public TunerHostInfo tunerCount(@org.eclipse.jdt.annotation.NonNull Integer tunerCount) {
        this.tunerCount = tunerCount;
        return this;
    }

    /**
     * Get tunerCount
     * 
     * @return tunerCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TUNER_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getTunerCount() {
        return tunerCount;
    }

    @JsonProperty(value = JSON_PROPERTY_TUNER_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTunerCount(@org.eclipse.jdt.annotation.NonNull Integer tunerCount) {
        this.tunerCount = tunerCount;
    }

    public TunerHostInfo userAgent(@org.eclipse.jdt.annotation.NonNull String userAgent) {
        this.userAgent = userAgent;
        return this;
    }

    /**
     * Get userAgent
     * 
     * @return userAgent
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_USER_AGENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUserAgent() {
        return userAgent;
    }

    @JsonProperty(value = JSON_PROPERTY_USER_AGENT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUserAgent(@org.eclipse.jdt.annotation.NonNull String userAgent) {
        this.userAgent = userAgent;
    }

    public TunerHostInfo ignoreDts(@org.eclipse.jdt.annotation.NonNull Boolean ignoreDts) {
        this.ignoreDts = ignoreDts;
        return this;
    }

    /**
     * Get ignoreDts
     * 
     * @return ignoreDts
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IGNORE_DTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIgnoreDts() {
        return ignoreDts;
    }

    @JsonProperty(value = JSON_PROPERTY_IGNORE_DTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIgnoreDts(@org.eclipse.jdt.annotation.NonNull Boolean ignoreDts) {
        this.ignoreDts = ignoreDts;
    }

    public TunerHostInfo readAtNativeFramerate(@org.eclipse.jdt.annotation.NonNull Boolean readAtNativeFramerate) {
        this.readAtNativeFramerate = readAtNativeFramerate;
        return this;
    }

    /**
     * Get readAtNativeFramerate
     * 
     * @return readAtNativeFramerate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_READ_AT_NATIVE_FRAMERATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getReadAtNativeFramerate() {
        return readAtNativeFramerate;
    }

    @JsonProperty(value = JSON_PROPERTY_READ_AT_NATIVE_FRAMERATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setReadAtNativeFramerate(@org.eclipse.jdt.annotation.NonNull Boolean readAtNativeFramerate) {
        this.readAtNativeFramerate = readAtNativeFramerate;
    }

    /**
     * Return true if this TunerHostInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TunerHostInfo tunerHostInfo = (TunerHostInfo) o;
        return Objects.equals(this.id, tunerHostInfo.id) && Objects.equals(this.url, tunerHostInfo.url)
                && Objects.equals(this.type, tunerHostInfo.type)
                && Objects.equals(this.deviceId, tunerHostInfo.deviceId)
                && Objects.equals(this.friendlyName, tunerHostInfo.friendlyName)
                && Objects.equals(this.importFavoritesOnly, tunerHostInfo.importFavoritesOnly)
                && Objects.equals(this.allowHWTranscoding, tunerHostInfo.allowHWTranscoding)
                && Objects.equals(this.allowFmp4TranscodingContainer, tunerHostInfo.allowFmp4TranscodingContainer)
                && Objects.equals(this.allowStreamSharing, tunerHostInfo.allowStreamSharing)
                && Objects.equals(this.fallbackMaxStreamingBitrate, tunerHostInfo.fallbackMaxStreamingBitrate)
                && Objects.equals(this.enableStreamLooping, tunerHostInfo.enableStreamLooping)
                && Objects.equals(this.source, tunerHostInfo.source)
                && Objects.equals(this.tunerCount, tunerHostInfo.tunerCount)
                && Objects.equals(this.userAgent, tunerHostInfo.userAgent)
                && Objects.equals(this.ignoreDts, tunerHostInfo.ignoreDts)
                && Objects.equals(this.readAtNativeFramerate, tunerHostInfo.readAtNativeFramerate);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, url, type, deviceId, friendlyName, importFavoritesOnly, allowHWTranscoding,
                allowFmp4TranscodingContainer, allowStreamSharing, fallbackMaxStreamingBitrate, enableStreamLooping,
                source, tunerCount, userAgent, ignoreDts, readAtNativeFramerate);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TunerHostInfo {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    deviceId: ").append(toIndentedString(deviceId)).append("\n");
        sb.append("    friendlyName: ").append(toIndentedString(friendlyName)).append("\n");
        sb.append("    importFavoritesOnly: ").append(toIndentedString(importFavoritesOnly)).append("\n");
        sb.append("    allowHWTranscoding: ").append(toIndentedString(allowHWTranscoding)).append("\n");
        sb.append("    allowFmp4TranscodingContainer: ").append(toIndentedString(allowFmp4TranscodingContainer))
                .append("\n");
        sb.append("    allowStreamSharing: ").append(toIndentedString(allowStreamSharing)).append("\n");
        sb.append("    fallbackMaxStreamingBitrate: ").append(toIndentedString(fallbackMaxStreamingBitrate))
                .append("\n");
        sb.append("    enableStreamLooping: ").append(toIndentedString(enableStreamLooping)).append("\n");
        sb.append("    source: ").append(toIndentedString(source)).append("\n");
        sb.append("    tunerCount: ").append(toIndentedString(tunerCount)).append("\n");
        sb.append("    userAgent: ").append(toIndentedString(userAgent)).append("\n");
        sb.append("    ignoreDts: ").append(toIndentedString(ignoreDts)).append("\n");
        sb.append("    readAtNativeFramerate: ").append(toIndentedString(readAtNativeFramerate)).append("\n");
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

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `Url` to the URL query string
        if (getUrl() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUrl()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `DeviceId` to the URL query string
        if (getDeviceId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDeviceId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDeviceId()))));
        }

        // add `FriendlyName` to the URL query string
        if (getFriendlyName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sFriendlyName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFriendlyName()))));
        }

        // add `ImportFavoritesOnly` to the URL query string
        if (getImportFavoritesOnly() != null) {
            joiner.add(String.format(Locale.ROOT, "%sImportFavoritesOnly%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImportFavoritesOnly()))));
        }

        // add `AllowHWTranscoding` to the URL query string
        if (getAllowHWTranscoding() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAllowHWTranscoding%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAllowHWTranscoding()))));
        }

        // add `AllowFmp4TranscodingContainer` to the URL query string
        if (getAllowFmp4TranscodingContainer() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAllowFmp4TranscodingContainer%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAllowFmp4TranscodingContainer()))));
        }

        // add `AllowStreamSharing` to the URL query string
        if (getAllowStreamSharing() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAllowStreamSharing%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAllowStreamSharing()))));
        }

        // add `FallbackMaxStreamingBitrate` to the URL query string
        if (getFallbackMaxStreamingBitrate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sFallbackMaxStreamingBitrate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFallbackMaxStreamingBitrate()))));
        }

        // add `EnableStreamLooping` to the URL query string
        if (getEnableStreamLooping() != null) {
            joiner.add(String.format(Locale.ROOT, "%sEnableStreamLooping%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getEnableStreamLooping()))));
        }

        // add `Source` to the URL query string
        if (getSource() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSource%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSource()))));
        }

        // add `TunerCount` to the URL query string
        if (getTunerCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sTunerCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTunerCount()))));
        }

        // add `UserAgent` to the URL query string
        if (getUserAgent() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUserAgent%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUserAgent()))));
        }

        // add `IgnoreDts` to the URL query string
        if (getIgnoreDts() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIgnoreDts%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIgnoreDts()))));
        }

        // add `ReadAtNativeFramerate` to the URL query string
        if (getReadAtNativeFramerate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sReadAtNativeFramerate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getReadAtNativeFramerate()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private TunerHostInfo instance;

        public Builder() {
            this(new TunerHostInfo());
        }

        protected Builder(TunerHostInfo instance) {
            this.instance = instance;
        }

        public TunerHostInfo.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        public TunerHostInfo.Builder url(String url) {
            this.instance.url = url;
            return this;
        }

        public TunerHostInfo.Builder type(String type) {
            this.instance.type = type;
            return this;
        }

        public TunerHostInfo.Builder deviceId(String deviceId) {
            this.instance.deviceId = deviceId;
            return this;
        }

        public TunerHostInfo.Builder friendlyName(String friendlyName) {
            this.instance.friendlyName = friendlyName;
            return this;
        }

        public TunerHostInfo.Builder importFavoritesOnly(Boolean importFavoritesOnly) {
            this.instance.importFavoritesOnly = importFavoritesOnly;
            return this;
        }

        public TunerHostInfo.Builder allowHWTranscoding(Boolean allowHWTranscoding) {
            this.instance.allowHWTranscoding = allowHWTranscoding;
            return this;
        }

        public TunerHostInfo.Builder allowFmp4TranscodingContainer(Boolean allowFmp4TranscodingContainer) {
            this.instance.allowFmp4TranscodingContainer = allowFmp4TranscodingContainer;
            return this;
        }

        public TunerHostInfo.Builder allowStreamSharing(Boolean allowStreamSharing) {
            this.instance.allowStreamSharing = allowStreamSharing;
            return this;
        }

        public TunerHostInfo.Builder fallbackMaxStreamingBitrate(Integer fallbackMaxStreamingBitrate) {
            this.instance.fallbackMaxStreamingBitrate = fallbackMaxStreamingBitrate;
            return this;
        }

        public TunerHostInfo.Builder enableStreamLooping(Boolean enableStreamLooping) {
            this.instance.enableStreamLooping = enableStreamLooping;
            return this;
        }

        public TunerHostInfo.Builder source(String source) {
            this.instance.source = source;
            return this;
        }

        public TunerHostInfo.Builder tunerCount(Integer tunerCount) {
            this.instance.tunerCount = tunerCount;
            return this;
        }

        public TunerHostInfo.Builder userAgent(String userAgent) {
            this.instance.userAgent = userAgent;
            return this;
        }

        public TunerHostInfo.Builder ignoreDts(Boolean ignoreDts) {
            this.instance.ignoreDts = ignoreDts;
            return this;
        }

        public TunerHostInfo.Builder readAtNativeFramerate(Boolean readAtNativeFramerate) {
            this.instance.readAtNativeFramerate = readAtNativeFramerate;
            return this;
        }

        /**
         * returns a built TunerHostInfo instance.
         *
         * The builder is not reusable.
         */
        public TunerHostInfo build() {
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
    public static TunerHostInfo.Builder builder() {
        return new TunerHostInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public TunerHostInfo.Builder toBuilder() {
        return new TunerHostInfo.Builder().id(getId()).url(getUrl()).type(getType()).deviceId(getDeviceId())
                .friendlyName(getFriendlyName()).importFavoritesOnly(getImportFavoritesOnly())
                .allowHWTranscoding(getAllowHWTranscoding())
                .allowFmp4TranscodingContainer(getAllowFmp4TranscodingContainer())
                .allowStreamSharing(getAllowStreamSharing())
                .fallbackMaxStreamingBitrate(getFallbackMaxStreamingBitrate())
                .enableStreamLooping(getEnableStreamLooping()).source(getSource()).tunerCount(getTunerCount())
                .userAgent(getUserAgent()).ignoreDts(getIgnoreDts()).readAtNativeFramerate(getReadAtNativeFramerate());
    }
}
