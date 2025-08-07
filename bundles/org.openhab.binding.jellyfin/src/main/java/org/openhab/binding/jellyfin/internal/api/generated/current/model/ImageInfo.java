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
 * Class ImageInfo.
 */
@JsonPropertyOrder({ ImageInfo.JSON_PROPERTY_IMAGE_TYPE, ImageInfo.JSON_PROPERTY_IMAGE_INDEX,
        ImageInfo.JSON_PROPERTY_IMAGE_TAG, ImageInfo.JSON_PROPERTY_PATH, ImageInfo.JSON_PROPERTY_BLUR_HASH,
        ImageInfo.JSON_PROPERTY_HEIGHT, ImageInfo.JSON_PROPERTY_WIDTH, ImageInfo.JSON_PROPERTY_SIZE })

public class ImageInfo {
    public static final String JSON_PROPERTY_IMAGE_TYPE = "ImageType";
    @org.eclipse.jdt.annotation.NonNull
    private ImageType imageType;

    public static final String JSON_PROPERTY_IMAGE_INDEX = "ImageIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer imageIndex;

    public static final String JSON_PROPERTY_IMAGE_TAG = "ImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String imageTag;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_BLUR_HASH = "BlurHash";
    @org.eclipse.jdt.annotation.NonNull
    private String blurHash;

    public static final String JSON_PROPERTY_HEIGHT = "Height";
    @org.eclipse.jdt.annotation.NonNull
    private Integer height;

    public static final String JSON_PROPERTY_WIDTH = "Width";
    @org.eclipse.jdt.annotation.NonNull
    private Integer width;

    public static final String JSON_PROPERTY_SIZE = "Size";
    @org.eclipse.jdt.annotation.NonNull
    private Long size;

    public ImageInfo() {
    }

    public ImageInfo imageType(@org.eclipse.jdt.annotation.NonNull ImageType imageType) {
        this.imageType = imageType;
        return this;
    }

    /**
     * Gets or sets the type of the image.
     * 
     * @return imageType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ImageType getImageType() {
        return imageType;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageType(@org.eclipse.jdt.annotation.NonNull ImageType imageType) {
        this.imageType = imageType;
    }

    public ImageInfo imageIndex(@org.eclipse.jdt.annotation.NonNull Integer imageIndex) {
        this.imageIndex = imageIndex;
        return this;
    }

    /**
     * Gets or sets the index of the image.
     * 
     * @return imageIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getImageIndex() {
        return imageIndex;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageIndex(@org.eclipse.jdt.annotation.NonNull Integer imageIndex) {
        this.imageIndex = imageIndex;
    }

    public ImageInfo imageTag(@org.eclipse.jdt.annotation.NonNull String imageTag) {
        this.imageTag = imageTag;
        return this;
    }

    /**
     * Gets or sets the image tag.
     * 
     * @return imageTag
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getImageTag() {
        return imageTag;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_TAG)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageTag(@org.eclipse.jdt.annotation.NonNull String imageTag) {
        this.imageTag = imageTag;
    }

    public ImageInfo path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path.
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

    public ImageInfo blurHash(@org.eclipse.jdt.annotation.NonNull String blurHash) {
        this.blurHash = blurHash;
        return this;
    }

    /**
     * Gets or sets the blurhash.
     * 
     * @return blurHash
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_BLUR_HASH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getBlurHash() {
        return blurHash;
    }

    @JsonProperty(JSON_PROPERTY_BLUR_HASH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBlurHash(@org.eclipse.jdt.annotation.NonNull String blurHash) {
        this.blurHash = blurHash;
    }

    public ImageInfo height(@org.eclipse.jdt.annotation.NonNull Integer height) {
        this.height = height;
        return this;
    }

    /**
     * Gets or sets the height.
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

    public ImageInfo width(@org.eclipse.jdt.annotation.NonNull Integer width) {
        this.width = width;
        return this;
    }

    /**
     * Gets or sets the width.
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

    public ImageInfo size(@org.eclipse.jdt.annotation.NonNull Long size) {
        this.size = size;
        return this;
    }

    /**
     * Gets or sets the size.
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

    /**
     * Return true if this ImageInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImageInfo imageInfo = (ImageInfo) o;
        return Objects.equals(this.imageType, imageInfo.imageType)
                && Objects.equals(this.imageIndex, imageInfo.imageIndex)
                && Objects.equals(this.imageTag, imageInfo.imageTag) && Objects.equals(this.path, imageInfo.path)
                && Objects.equals(this.blurHash, imageInfo.blurHash) && Objects.equals(this.height, imageInfo.height)
                && Objects.equals(this.width, imageInfo.width) && Objects.equals(this.size, imageInfo.size);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imageType, imageIndex, imageTag, path, blurHash, height, width, size);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ImageInfo {\n");
        sb.append("    imageType: ").append(toIndentedString(imageType)).append("\n");
        sb.append("    imageIndex: ").append(toIndentedString(imageIndex)).append("\n");
        sb.append("    imageTag: ").append(toIndentedString(imageTag)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    blurHash: ").append(toIndentedString(blurHash)).append("\n");
        sb.append("    height: ").append(toIndentedString(height)).append("\n");
        sb.append("    width: ").append(toIndentedString(width)).append("\n");
        sb.append("    size: ").append(toIndentedString(size)).append("\n");
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
