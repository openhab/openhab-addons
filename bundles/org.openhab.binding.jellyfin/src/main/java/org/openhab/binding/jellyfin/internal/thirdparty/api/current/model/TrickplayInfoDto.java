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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The trickplay api model.
 */
@JsonPropertyOrder({ TrickplayInfoDto.JSON_PROPERTY_WIDTH, TrickplayInfoDto.JSON_PROPERTY_HEIGHT,
        TrickplayInfoDto.JSON_PROPERTY_TILE_WIDTH, TrickplayInfoDto.JSON_PROPERTY_TILE_HEIGHT,
        TrickplayInfoDto.JSON_PROPERTY_THUMBNAIL_COUNT, TrickplayInfoDto.JSON_PROPERTY_INTERVAL,
        TrickplayInfoDto.JSON_PROPERTY_BANDWIDTH })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TrickplayInfoDto {
    public static final String JSON_PROPERTY_WIDTH = "Width";
    @org.eclipse.jdt.annotation.Nullable
    private Integer width;

    public static final String JSON_PROPERTY_HEIGHT = "Height";
    @org.eclipse.jdt.annotation.Nullable
    private Integer height;

    public static final String JSON_PROPERTY_TILE_WIDTH = "TileWidth";
    @org.eclipse.jdt.annotation.Nullable
    private Integer tileWidth;

    public static final String JSON_PROPERTY_TILE_HEIGHT = "TileHeight";
    @org.eclipse.jdt.annotation.Nullable
    private Integer tileHeight;

    public static final String JSON_PROPERTY_THUMBNAIL_COUNT = "ThumbnailCount";
    @org.eclipse.jdt.annotation.Nullable
    private Integer thumbnailCount;

    public static final String JSON_PROPERTY_INTERVAL = "Interval";
    @org.eclipse.jdt.annotation.Nullable
    private Integer interval;

    public static final String JSON_PROPERTY_BANDWIDTH = "Bandwidth";
    @org.eclipse.jdt.annotation.Nullable
    private Integer bandwidth;

    public TrickplayInfoDto() {
    }

    public TrickplayInfoDto width(@org.eclipse.jdt.annotation.Nullable Integer width) {
        this.width = width;
        return this;
    }

    /**
     * Gets the width of an individual thumbnail.
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

    public TrickplayInfoDto height(@org.eclipse.jdt.annotation.Nullable Integer height) {
        this.height = height;
        return this;
    }

    /**
     * Gets the height of an individual thumbnail.
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

    public TrickplayInfoDto tileWidth(@org.eclipse.jdt.annotation.Nullable Integer tileWidth) {
        this.tileWidth = tileWidth;
        return this;
    }

    /**
     * Gets the amount of thumbnails per row.
     * 
     * @return tileWidth
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TILE_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getTileWidth() {
        return tileWidth;
    }

    @JsonProperty(value = JSON_PROPERTY_TILE_WIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTileWidth(@org.eclipse.jdt.annotation.Nullable Integer tileWidth) {
        this.tileWidth = tileWidth;
    }

    public TrickplayInfoDto tileHeight(@org.eclipse.jdt.annotation.Nullable Integer tileHeight) {
        this.tileHeight = tileHeight;
        return this;
    }

    /**
     * Gets the amount of thumbnails per column.
     * 
     * @return tileHeight
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TILE_HEIGHT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getTileHeight() {
        return tileHeight;
    }

    @JsonProperty(value = JSON_PROPERTY_TILE_HEIGHT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTileHeight(@org.eclipse.jdt.annotation.Nullable Integer tileHeight) {
        this.tileHeight = tileHeight;
    }

    public TrickplayInfoDto thumbnailCount(@org.eclipse.jdt.annotation.Nullable Integer thumbnailCount) {
        this.thumbnailCount = thumbnailCount;
        return this;
    }

    /**
     * Gets the total amount of non-black thumbnails.
     * 
     * @return thumbnailCount
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_THUMBNAIL_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getThumbnailCount() {
        return thumbnailCount;
    }

    @JsonProperty(value = JSON_PROPERTY_THUMBNAIL_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThumbnailCount(@org.eclipse.jdt.annotation.Nullable Integer thumbnailCount) {
        this.thumbnailCount = thumbnailCount;
    }

    public TrickplayInfoDto interval(@org.eclipse.jdt.annotation.Nullable Integer interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Gets the interval in milliseconds between each trickplay thumbnail.
     * 
     * @return interval
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_INTERVAL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getInterval() {
        return interval;
    }

    @JsonProperty(value = JSON_PROPERTY_INTERVAL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInterval(@org.eclipse.jdt.annotation.Nullable Integer interval) {
        this.interval = interval;
    }

    public TrickplayInfoDto bandwidth(@org.eclipse.jdt.annotation.Nullable Integer bandwidth) {
        this.bandwidth = bandwidth;
        return this;
    }

    /**
     * Gets the peak bandwidth usage in bits per second.
     * 
     * @return bandwidth
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BANDWIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getBandwidth() {
        return bandwidth;
    }

    @JsonProperty(value = JSON_PROPERTY_BANDWIDTH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBandwidth(@org.eclipse.jdt.annotation.Nullable Integer bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * Return true if this TrickplayInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TrickplayInfoDto trickplayInfoDto = (TrickplayInfoDto) o;
        return Objects.equals(this.width, trickplayInfoDto.width)
                && Objects.equals(this.height, trickplayInfoDto.height)
                && Objects.equals(this.tileWidth, trickplayInfoDto.tileWidth)
                && Objects.equals(this.tileHeight, trickplayInfoDto.tileHeight)
                && Objects.equals(this.thumbnailCount, trickplayInfoDto.thumbnailCount)
                && Objects.equals(this.interval, trickplayInfoDto.interval)
                && Objects.equals(this.bandwidth, trickplayInfoDto.bandwidth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, tileWidth, tileHeight, thumbnailCount, interval, bandwidth);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TrickplayInfoDto {\n");
        sb.append("    width: ").append(toIndentedString(width)).append("\n");
        sb.append("    height: ").append(toIndentedString(height)).append("\n");
        sb.append("    tileWidth: ").append(toIndentedString(tileWidth)).append("\n");
        sb.append("    tileHeight: ").append(toIndentedString(tileHeight)).append("\n");
        sb.append("    thumbnailCount: ").append(toIndentedString(thumbnailCount)).append("\n");
        sb.append("    interval: ").append(toIndentedString(interval)).append("\n");
        sb.append("    bandwidth: ").append(toIndentedString(bandwidth)).append("\n");
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

        // add `TileWidth` to the URL query string
        if (getTileWidth() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTileWidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTileWidth()))));
        }

        // add `TileHeight` to the URL query string
        if (getTileHeight() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTileHeight%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTileHeight()))));
        }

        // add `ThumbnailCount` to the URL query string
        if (getThumbnailCount() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sThumbnailCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getThumbnailCount()))));
        }

        // add `Interval` to the URL query string
        if (getInterval() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sInterval%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getInterval()))));
        }

        // add `Bandwidth` to the URL query string
        if (getBandwidth() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBandwidth%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBandwidth()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private TrickplayInfoDto instance;

        public Builder() {
            this(new TrickplayInfoDto());
        }

        protected Builder(TrickplayInfoDto instance) {
            this.instance = instance;
        }

        public TrickplayInfoDto.Builder width(Integer width) {
            this.instance.width = width;
            return this;
        }

        public TrickplayInfoDto.Builder height(Integer height) {
            this.instance.height = height;
            return this;
        }

        public TrickplayInfoDto.Builder tileWidth(Integer tileWidth) {
            this.instance.tileWidth = tileWidth;
            return this;
        }

        public TrickplayInfoDto.Builder tileHeight(Integer tileHeight) {
            this.instance.tileHeight = tileHeight;
            return this;
        }

        public TrickplayInfoDto.Builder thumbnailCount(Integer thumbnailCount) {
            this.instance.thumbnailCount = thumbnailCount;
            return this;
        }

        public TrickplayInfoDto.Builder interval(Integer interval) {
            this.instance.interval = interval;
            return this;
        }

        public TrickplayInfoDto.Builder bandwidth(Integer bandwidth) {
            this.instance.bandwidth = bandwidth;
            return this;
        }

        /**
         * returns a built TrickplayInfoDto instance.
         *
         * The builder is not reusable.
         */
        public TrickplayInfoDto build() {
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
    public static TrickplayInfoDto.Builder builder() {
        return new TrickplayInfoDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public TrickplayInfoDto.Builder toBuilder() {
        return new TrickplayInfoDto.Builder().width(getWidth()).height(getHeight()).tileWidth(getTileWidth())
                .tileHeight(getTileHeight()).thumbnailCount(getThumbnailCount()).interval(getInterval())
                .bandwidth(getBandwidth());
    }
}
