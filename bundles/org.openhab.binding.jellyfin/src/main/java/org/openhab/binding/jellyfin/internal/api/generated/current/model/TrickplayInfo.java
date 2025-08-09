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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * An entity representing the metadata for a group of trickplay tiles.
 */
@JsonPropertyOrder({ TrickplayInfo.JSON_PROPERTY_WIDTH, TrickplayInfo.JSON_PROPERTY_HEIGHT,
        TrickplayInfo.JSON_PROPERTY_TILE_WIDTH, TrickplayInfo.JSON_PROPERTY_TILE_HEIGHT,
        TrickplayInfo.JSON_PROPERTY_THUMBNAIL_COUNT, TrickplayInfo.JSON_PROPERTY_INTERVAL,
        TrickplayInfo.JSON_PROPERTY_BANDWIDTH })

public class TrickplayInfo {
    public static final String JSON_PROPERTY_WIDTH = "Width";
    @org.eclipse.jdt.annotation.NonNull
    private Integer width;

    public static final String JSON_PROPERTY_HEIGHT = "Height";
    @org.eclipse.jdt.annotation.NonNull
    private Integer height;

    public static final String JSON_PROPERTY_TILE_WIDTH = "TileWidth";
    @org.eclipse.jdt.annotation.NonNull
    private Integer tileWidth;

    public static final String JSON_PROPERTY_TILE_HEIGHT = "TileHeight";
    @org.eclipse.jdt.annotation.NonNull
    private Integer tileHeight;

    public static final String JSON_PROPERTY_THUMBNAIL_COUNT = "ThumbnailCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer thumbnailCount;

    public static final String JSON_PROPERTY_INTERVAL = "Interval";
    @org.eclipse.jdt.annotation.NonNull
    private Integer interval;

    public static final String JSON_PROPERTY_BANDWIDTH = "Bandwidth";
    @org.eclipse.jdt.annotation.NonNull
    private Integer bandwidth;

    public TrickplayInfo() {
    }

    public TrickplayInfo width(@org.eclipse.jdt.annotation.NonNull Integer width) {
        this.width = width;
        return this;
    }

    /**
     * Gets or sets width of an individual thumbnail.
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

    public TrickplayInfo height(@org.eclipse.jdt.annotation.NonNull Integer height) {
        this.height = height;
        return this;
    }

    /**
     * Gets or sets height of an individual thumbnail.
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

    public TrickplayInfo tileWidth(@org.eclipse.jdt.annotation.NonNull Integer tileWidth) {
        this.tileWidth = tileWidth;
        return this;
    }

    /**
     * Gets or sets amount of thumbnails per row.
     * 
     * @return tileWidth
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TILE_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTileWidth() {
        return tileWidth;
    }

    @JsonProperty(JSON_PROPERTY_TILE_WIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTileWidth(@org.eclipse.jdt.annotation.NonNull Integer tileWidth) {
        this.tileWidth = tileWidth;
    }

    public TrickplayInfo tileHeight(@org.eclipse.jdt.annotation.NonNull Integer tileHeight) {
        this.tileHeight = tileHeight;
        return this;
    }

    /**
     * Gets or sets amount of thumbnails per column.
     * 
     * @return tileHeight
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TILE_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getTileHeight() {
        return tileHeight;
    }

    @JsonProperty(JSON_PROPERTY_TILE_HEIGHT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTileHeight(@org.eclipse.jdt.annotation.NonNull Integer tileHeight) {
        this.tileHeight = tileHeight;
    }

    public TrickplayInfo thumbnailCount(@org.eclipse.jdt.annotation.NonNull Integer thumbnailCount) {
        this.thumbnailCount = thumbnailCount;
        return this;
    }

    /**
     * Gets or sets total amount of non-black thumbnails.
     * 
     * @return thumbnailCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_THUMBNAIL_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getThumbnailCount() {
        return thumbnailCount;
    }

    @JsonProperty(JSON_PROPERTY_THUMBNAIL_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setThumbnailCount(@org.eclipse.jdt.annotation.NonNull Integer thumbnailCount) {
        this.thumbnailCount = thumbnailCount;
    }

    public TrickplayInfo interval(@org.eclipse.jdt.annotation.NonNull Integer interval) {
        this.interval = interval;
        return this;
    }

    /**
     * Gets or sets interval in milliseconds between each trickplay thumbnail.
     * 
     * @return interval
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_INTERVAL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getInterval() {
        return interval;
    }

    @JsonProperty(JSON_PROPERTY_INTERVAL)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setInterval(@org.eclipse.jdt.annotation.NonNull Integer interval) {
        this.interval = interval;
    }

    public TrickplayInfo bandwidth(@org.eclipse.jdt.annotation.NonNull Integer bandwidth) {
        this.bandwidth = bandwidth;
        return this;
    }

    /**
     * Gets or sets peak bandwith usage in bits per second.
     * 
     * @return bandwidth
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BANDWIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getBandwidth() {
        return bandwidth;
    }

    @JsonProperty(JSON_PROPERTY_BANDWIDTH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBandwidth(@org.eclipse.jdt.annotation.NonNull Integer bandwidth) {
        this.bandwidth = bandwidth;
    }

    /**
     * Return true if this TrickplayInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TrickplayInfo trickplayInfo = (TrickplayInfo) o;
        return Objects.equals(this.width, trickplayInfo.width) && Objects.equals(this.height, trickplayInfo.height)
                && Objects.equals(this.tileWidth, trickplayInfo.tileWidth)
                && Objects.equals(this.tileHeight, trickplayInfo.tileHeight)
                && Objects.equals(this.thumbnailCount, trickplayInfo.thumbnailCount)
                && Objects.equals(this.interval, trickplayInfo.interval)
                && Objects.equals(this.bandwidth, trickplayInfo.bandwidth);
    }

    @Override
    public int hashCode() {
        return Objects.hash(width, height, tileWidth, tileHeight, thumbnailCount, interval, bandwidth);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TrickplayInfo {\n");
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
}
