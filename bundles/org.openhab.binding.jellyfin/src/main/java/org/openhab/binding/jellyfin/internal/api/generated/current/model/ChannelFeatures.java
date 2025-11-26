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
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * ChannelFeatures
 */
@JsonPropertyOrder({ ChannelFeatures.JSON_PROPERTY_NAME, ChannelFeatures.JSON_PROPERTY_ID,
        ChannelFeatures.JSON_PROPERTY_CAN_SEARCH, ChannelFeatures.JSON_PROPERTY_MEDIA_TYPES,
        ChannelFeatures.JSON_PROPERTY_CONTENT_TYPES, ChannelFeatures.JSON_PROPERTY_MAX_PAGE_SIZE,
        ChannelFeatures.JSON_PROPERTY_AUTO_REFRESH_LEVELS, ChannelFeatures.JSON_PROPERTY_DEFAULT_SORT_FIELDS,
        ChannelFeatures.JSON_PROPERTY_SUPPORTS_SORT_ORDER_TOGGLE, ChannelFeatures.JSON_PROPERTY_SUPPORTS_LATEST_MEDIA,
        ChannelFeatures.JSON_PROPERTY_CAN_FILTER, ChannelFeatures.JSON_PROPERTY_SUPPORTS_CONTENT_DOWNLOADING })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ChannelFeatures {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public static final String JSON_PROPERTY_CAN_SEARCH = "CanSearch";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canSearch;

    public static final String JSON_PROPERTY_MEDIA_TYPES = "MediaTypes";
    @org.eclipse.jdt.annotation.NonNull
    private List<ChannelMediaType> mediaTypes = new ArrayList<>();

    public static final String JSON_PROPERTY_CONTENT_TYPES = "ContentTypes";
    @org.eclipse.jdt.annotation.NonNull
    private List<ChannelMediaContentType> contentTypes = new ArrayList<>();

    public static final String JSON_PROPERTY_MAX_PAGE_SIZE = "MaxPageSize";
    @org.eclipse.jdt.annotation.NonNull
    private Integer maxPageSize;

    public static final String JSON_PROPERTY_AUTO_REFRESH_LEVELS = "AutoRefreshLevels";
    @org.eclipse.jdt.annotation.NonNull
    private Integer autoRefreshLevels;

    public static final String JSON_PROPERTY_DEFAULT_SORT_FIELDS = "DefaultSortFields";
    @org.eclipse.jdt.annotation.NonNull
    private List<ChannelItemSortField> defaultSortFields = new ArrayList<>();

    public static final String JSON_PROPERTY_SUPPORTS_SORT_ORDER_TOGGLE = "SupportsSortOrderToggle";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsSortOrderToggle;

    public static final String JSON_PROPERTY_SUPPORTS_LATEST_MEDIA = "SupportsLatestMedia";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsLatestMedia;

    public static final String JSON_PROPERTY_CAN_FILTER = "CanFilter";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean canFilter;

    public static final String JSON_PROPERTY_SUPPORTS_CONTENT_DOWNLOADING = "SupportsContentDownloading";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean supportsContentDownloading;

    public ChannelFeatures() {
    }

    public ChannelFeatures name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public ChannelFeatures id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the identifier.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
    }

    public ChannelFeatures canSearch(@org.eclipse.jdt.annotation.NonNull Boolean canSearch) {
        this.canSearch = canSearch;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance can search.
     * 
     * @return canSearch
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAN_SEARCH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCanSearch() {
        return canSearch;
    }

    @JsonProperty(value = JSON_PROPERTY_CAN_SEARCH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanSearch(@org.eclipse.jdt.annotation.NonNull Boolean canSearch) {
        this.canSearch = canSearch;
    }

    public ChannelFeatures mediaTypes(@org.eclipse.jdt.annotation.NonNull List<ChannelMediaType> mediaTypes) {
        this.mediaTypes = mediaTypes;
        return this;
    }

    public ChannelFeatures addMediaTypesItem(ChannelMediaType mediaTypesItem) {
        if (this.mediaTypes == null) {
            this.mediaTypes = new ArrayList<>();
        }
        this.mediaTypes.add(mediaTypesItem);
        return this;
    }

    /**
     * Gets or sets the media types.
     * 
     * @return mediaTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MEDIA_TYPES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ChannelMediaType> getMediaTypes() {
        return mediaTypes;
    }

    @JsonProperty(value = JSON_PROPERTY_MEDIA_TYPES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMediaTypes(@org.eclipse.jdt.annotation.NonNull List<ChannelMediaType> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public ChannelFeatures contentTypes(
            @org.eclipse.jdt.annotation.NonNull List<ChannelMediaContentType> contentTypes) {
        this.contentTypes = contentTypes;
        return this;
    }

    public ChannelFeatures addContentTypesItem(ChannelMediaContentType contentTypesItem) {
        if (this.contentTypes == null) {
            this.contentTypes = new ArrayList<>();
        }
        this.contentTypes.add(contentTypesItem);
        return this;
    }

    /**
     * Gets or sets the content types.
     * 
     * @return contentTypes
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CONTENT_TYPES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ChannelMediaContentType> getContentTypes() {
        return contentTypes;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTENT_TYPES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContentTypes(@org.eclipse.jdt.annotation.NonNull List<ChannelMediaContentType> contentTypes) {
        this.contentTypes = contentTypes;
    }

    public ChannelFeatures maxPageSize(@org.eclipse.jdt.annotation.NonNull Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Gets or sets the maximum number of records the channel allows retrieving at a time.
     * 
     * @return maxPageSize
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_MAX_PAGE_SIZE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getMaxPageSize() {
        return maxPageSize;
    }

    @JsonProperty(value = JSON_PROPERTY_MAX_PAGE_SIZE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMaxPageSize(@org.eclipse.jdt.annotation.NonNull Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
    }

    public ChannelFeatures autoRefreshLevels(@org.eclipse.jdt.annotation.NonNull Integer autoRefreshLevels) {
        this.autoRefreshLevels = autoRefreshLevels;
        return this;
    }

    /**
     * Gets or sets the automatic refresh levels.
     * 
     * @return autoRefreshLevels
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_AUTO_REFRESH_LEVELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getAutoRefreshLevels() {
        return autoRefreshLevels;
    }

    @JsonProperty(value = JSON_PROPERTY_AUTO_REFRESH_LEVELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setAutoRefreshLevels(@org.eclipse.jdt.annotation.NonNull Integer autoRefreshLevels) {
        this.autoRefreshLevels = autoRefreshLevels;
    }

    public ChannelFeatures defaultSortFields(
            @org.eclipse.jdt.annotation.NonNull List<ChannelItemSortField> defaultSortFields) {
        this.defaultSortFields = defaultSortFields;
        return this;
    }

    public ChannelFeatures addDefaultSortFieldsItem(ChannelItemSortField defaultSortFieldsItem) {
        if (this.defaultSortFields == null) {
            this.defaultSortFields = new ArrayList<>();
        }
        this.defaultSortFields.add(defaultSortFieldsItem);
        return this;
    }

    /**
     * Gets or sets the default sort orders.
     * 
     * @return defaultSortFields
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DEFAULT_SORT_FIELDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ChannelItemSortField> getDefaultSortFields() {
        return defaultSortFields;
    }

    @JsonProperty(value = JSON_PROPERTY_DEFAULT_SORT_FIELDS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultSortFields(@org.eclipse.jdt.annotation.NonNull List<ChannelItemSortField> defaultSortFields) {
        this.defaultSortFields = defaultSortFields;
    }

    public ChannelFeatures supportsSortOrderToggle(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsSortOrderToggle) {
        this.supportsSortOrderToggle = supportsSortOrderToggle;
        return this;
    }

    /**
     * Gets or sets a value indicating whether a sort ascending/descending toggle is supported.
     * 
     * @return supportsSortOrderToggle
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_SORT_ORDER_TOGGLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsSortOrderToggle() {
        return supportsSortOrderToggle;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_SORT_ORDER_TOGGLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsSortOrderToggle(@org.eclipse.jdt.annotation.NonNull Boolean supportsSortOrderToggle) {
        this.supportsSortOrderToggle = supportsSortOrderToggle;
    }

    public ChannelFeatures supportsLatestMedia(@org.eclipse.jdt.annotation.NonNull Boolean supportsLatestMedia) {
        this.supportsLatestMedia = supportsLatestMedia;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [supports latest media].
     * 
     * @return supportsLatestMedia
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_LATEST_MEDIA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsLatestMedia() {
        return supportsLatestMedia;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_LATEST_MEDIA, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsLatestMedia(@org.eclipse.jdt.annotation.NonNull Boolean supportsLatestMedia) {
        this.supportsLatestMedia = supportsLatestMedia;
    }

    public ChannelFeatures canFilter(@org.eclipse.jdt.annotation.NonNull Boolean canFilter) {
        this.canFilter = canFilter;
        return this;
    }

    /**
     * Gets or sets a value indicating whether this instance can filter.
     * 
     * @return canFilter
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_CAN_FILTER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getCanFilter() {
        return canFilter;
    }

    @JsonProperty(value = JSON_PROPERTY_CAN_FILTER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCanFilter(@org.eclipse.jdt.annotation.NonNull Boolean canFilter) {
        this.canFilter = canFilter;
    }

    public ChannelFeatures supportsContentDownloading(
            @org.eclipse.jdt.annotation.NonNull Boolean supportsContentDownloading) {
        this.supportsContentDownloading = supportsContentDownloading;
        return this;
    }

    /**
     * Gets or sets a value indicating whether [supports content downloading].
     * 
     * @return supportsContentDownloading
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_CONTENT_DOWNLOADING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getSupportsContentDownloading() {
        return supportsContentDownloading;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTS_CONTENT_DOWNLOADING, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportsContentDownloading(@org.eclipse.jdt.annotation.NonNull Boolean supportsContentDownloading) {
        this.supportsContentDownloading = supportsContentDownloading;
    }

    /**
     * Return true if this ChannelFeatures object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelFeatures channelFeatures = (ChannelFeatures) o;
        return Objects.equals(this.name, channelFeatures.name) && Objects.equals(this.id, channelFeatures.id)
                && Objects.equals(this.canSearch, channelFeatures.canSearch)
                && Objects.equals(this.mediaTypes, channelFeatures.mediaTypes)
                && Objects.equals(this.contentTypes, channelFeatures.contentTypes)
                && Objects.equals(this.maxPageSize, channelFeatures.maxPageSize)
                && Objects.equals(this.autoRefreshLevels, channelFeatures.autoRefreshLevels)
                && Objects.equals(this.defaultSortFields, channelFeatures.defaultSortFields)
                && Objects.equals(this.supportsSortOrderToggle, channelFeatures.supportsSortOrderToggle)
                && Objects.equals(this.supportsLatestMedia, channelFeatures.supportsLatestMedia)
                && Objects.equals(this.canFilter, channelFeatures.canFilter)
                && Objects.equals(this.supportsContentDownloading, channelFeatures.supportsContentDownloading);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, id, canSearch, mediaTypes, contentTypes, maxPageSize, autoRefreshLevels,
                defaultSortFields, supportsSortOrderToggle, supportsLatestMedia, canFilter, supportsContentDownloading);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ChannelFeatures {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    canSearch: ").append(toIndentedString(canSearch)).append("\n");
        sb.append("    mediaTypes: ").append(toIndentedString(mediaTypes)).append("\n");
        sb.append("    contentTypes: ").append(toIndentedString(contentTypes)).append("\n");
        sb.append("    maxPageSize: ").append(toIndentedString(maxPageSize)).append("\n");
        sb.append("    autoRefreshLevels: ").append(toIndentedString(autoRefreshLevels)).append("\n");
        sb.append("    defaultSortFields: ").append(toIndentedString(defaultSortFields)).append("\n");
        sb.append("    supportsSortOrderToggle: ").append(toIndentedString(supportsSortOrderToggle)).append("\n");
        sb.append("    supportsLatestMedia: ").append(toIndentedString(supportsLatestMedia)).append("\n");
        sb.append("    canFilter: ").append(toIndentedString(canFilter)).append("\n");
        sb.append("    supportsContentDownloading: ").append(toIndentedString(supportsContentDownloading)).append("\n");
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
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `CanSearch` to the URL query string
        if (getCanSearch() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCanSearch%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanSearch()))));
        }

        // add `MediaTypes` to the URL query string
        if (getMediaTypes() != null) {
            for (int i = 0; i < getMediaTypes().size(); i++) {
                if (getMediaTypes().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sMediaTypes%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getMediaTypes().get(i)))));
                }
            }
        }

        // add `ContentTypes` to the URL query string
        if (getContentTypes() != null) {
            for (int i = 0; i < getContentTypes().size(); i++) {
                if (getContentTypes().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sContentTypes%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getContentTypes().get(i)))));
                }
            }
        }

        // add `MaxPageSize` to the URL query string
        if (getMaxPageSize() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMaxPageSize%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMaxPageSize()))));
        }

        // add `AutoRefreshLevels` to the URL query string
        if (getAutoRefreshLevels() != null) {
            joiner.add(String.format(Locale.ROOT, "%sAutoRefreshLevels%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getAutoRefreshLevels()))));
        }

        // add `DefaultSortFields` to the URL query string
        if (getDefaultSortFields() != null) {
            for (int i = 0; i < getDefaultSortFields().size(); i++) {
                if (getDefaultSortFields().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sDefaultSortFields%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getDefaultSortFields().get(i)))));
                }
            }
        }

        // add `SupportsSortOrderToggle` to the URL query string
        if (getSupportsSortOrderToggle() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSupportsSortOrderToggle%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsSortOrderToggle()))));
        }

        // add `SupportsLatestMedia` to the URL query string
        if (getSupportsLatestMedia() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSupportsLatestMedia%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsLatestMedia()))));
        }

        // add `CanFilter` to the URL query string
        if (getCanFilter() != null) {
            joiner.add(String.format(Locale.ROOT, "%sCanFilter%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getCanFilter()))));
        }

        // add `SupportsContentDownloading` to the URL query string
        if (getSupportsContentDownloading() != null) {
            joiner.add(String.format(Locale.ROOT, "%sSupportsContentDownloading%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSupportsContentDownloading()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ChannelFeatures instance;

        public Builder() {
            this(new ChannelFeatures());
        }

        protected Builder(ChannelFeatures instance) {
            this.instance = instance;
        }

        public ChannelFeatures.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public ChannelFeatures.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public ChannelFeatures.Builder canSearch(Boolean canSearch) {
            this.instance.canSearch = canSearch;
            return this;
        }

        public ChannelFeatures.Builder mediaTypes(List<ChannelMediaType> mediaTypes) {
            this.instance.mediaTypes = mediaTypes;
            return this;
        }

        public ChannelFeatures.Builder contentTypes(List<ChannelMediaContentType> contentTypes) {
            this.instance.contentTypes = contentTypes;
            return this;
        }

        public ChannelFeatures.Builder maxPageSize(Integer maxPageSize) {
            this.instance.maxPageSize = maxPageSize;
            return this;
        }

        public ChannelFeatures.Builder autoRefreshLevels(Integer autoRefreshLevels) {
            this.instance.autoRefreshLevels = autoRefreshLevels;
            return this;
        }

        public ChannelFeatures.Builder defaultSortFields(List<ChannelItemSortField> defaultSortFields) {
            this.instance.defaultSortFields = defaultSortFields;
            return this;
        }

        public ChannelFeatures.Builder supportsSortOrderToggle(Boolean supportsSortOrderToggle) {
            this.instance.supportsSortOrderToggle = supportsSortOrderToggle;
            return this;
        }

        public ChannelFeatures.Builder supportsLatestMedia(Boolean supportsLatestMedia) {
            this.instance.supportsLatestMedia = supportsLatestMedia;
            return this;
        }

        public ChannelFeatures.Builder canFilter(Boolean canFilter) {
            this.instance.canFilter = canFilter;
            return this;
        }

        public ChannelFeatures.Builder supportsContentDownloading(Boolean supportsContentDownloading) {
            this.instance.supportsContentDownloading = supportsContentDownloading;
            return this;
        }

        /**
         * returns a built ChannelFeatures instance.
         *
         * The builder is not reusable.
         */
        public ChannelFeatures build() {
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
    public static ChannelFeatures.Builder builder() {
        return new ChannelFeatures.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ChannelFeatures.Builder toBuilder() {
        return new ChannelFeatures.Builder().name(getName()).id(getId()).canSearch(getCanSearch())
                .mediaTypes(getMediaTypes()).contentTypes(getContentTypes()).maxPageSize(getMaxPageSize())
                .autoRefreshLevels(getAutoRefreshLevels()).defaultSortFields(getDefaultSortFields())
                .supportsSortOrderToggle(getSupportsSortOrderToggle()).supportsLatestMedia(getSupportsLatestMedia())
                .canFilter(getCanFilter()).supportsContentDownloading(getSupportsContentDownloading());
    }
}
