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
 * Class ImageInfo.
 */
@JsonPropertyOrder({ ImageInfo.JSON_PROPERTY_IMAGE_TYPE, ImageInfo.JSON_PROPERTY_IMAGE_INDEX,
        ImageInfo.JSON_PROPERTY_IMAGE_TAG, ImageInfo.JSON_PROPERTY_PATH, ImageInfo.JSON_PROPERTY_BLUR_HASH,
        ImageInfo.JSON_PROPERTY_HEIGHT, ImageInfo.JSON_PROPERTY_WIDTH, ImageInfo.JSON_PROPERTY_SIZE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ImageInfo {
    public static final String JSON_PROPERTY_IMAGE_TYPE = "ImageType";
    @org.eclipse.jdt.annotation.Nullable
    private ImageType imageType;

    public static final String JSON_PROPERTY_IMAGE_INDEX = "ImageIndex";
    @org.eclipse.jdt.annotation.Nullable
    private Integer imageIndex;

    public static final String JSON_PROPERTY_IMAGE_TAG = "ImageTag";
    @org.eclipse.jdt.annotation.Nullable
    private String imageTag;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.Nullable
    private String path;

    public static final String JSON_PROPERTY_BLUR_HASH = "BlurHash";
    @org.eclipse.jdt.annotation.Nullable
    private String blurHash;

    public static final String JSON_PROPERTY_HEIGHT = "Height";
    @org.eclipse.jdt.annotation.Nullable
    private Integer height;

    public static final String JSON_PROPERTY_WIDTH = "Width";
    @org.eclipse.jdt.annotation.Nullable
    private Integer width;

    public static final String JSON_PROPERTY_SIZE = "Size";
    @org.eclipse.jdt.annotation.Nullable
    private Long size;

    public ImageInfo() {
    }

    public ImageInfo imageType(@org.eclipse.jdt.annotation.Nullable ImageType imageType) {
        this.imageType = imageType;
        return this;
    }

    /**
     * Gets or sets the type of the image.
     * 
     * @return imageType
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ImageType getImageType() {
        return imageType;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageType(@org.eclipse.jdt.annotation.Nullable ImageType imageType) {
        this.imageType = imageType;
    }

    public ImageInfo imageIndex(@org.eclipse.jdt.annotation.Nullable Integer imageIndex) {
        this.imageIndex = imageIndex;
        return this;
    }

    /**
     * Gets or sets the index of the image.
     * 
     * @return imageIndex
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGE_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getImageIndex() {
        return imageIndex;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageIndex(@org.eclipse.jdt.annotation.Nullable Integer imageIndex) {
        this.imageIndex = imageIndex;
    }

    public ImageInfo imageTag(@org.eclipse.jdt.annotation.Nullable String imageTag) {
        this.imageTag = imageTag;
        return this;
    }

    /**
     * Gets or sets the image tag.
     * 
     * @return imageTag
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getImageTag() {
        return imageTag;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_TAG, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageTag(@org.eclipse.jdt.annotation.Nullable String imageTag) {
        this.imageTag = imageTag;
    }

    public ImageInfo path(@org.eclipse.jdt.annotation.Nullable String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path.
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

    public ImageInfo blurHash(@org.eclipse.jdt.annotation.Nullable String blurHash) {
        this.blurHash = blurHash;
        return this;
    }

    /**
     * Gets or sets the blurhash.
     * 
     * @return blurHash
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_BLUR_HASH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getBlurHash() {
        return blurHash;
    }

    @JsonProperty(value = JSON_PROPERTY_BLUR_HASH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBlurHash(@org.eclipse.jdt.annotation.Nullable String blurHash) {
        this.blurHash = blurHash;
    }

    public ImageInfo height(@org.eclipse.jdt.annotation.Nullable Integer height) {
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

    public ImageInfo width(@org.eclipse.jdt.annotation.Nullable Integer width) {
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

    public ImageInfo size(@org.eclipse.jdt.annotation.Nullable Long size) {
        this.size = size;
        return this;
    }

    /**
     * Gets or sets the size.
     * 
     * @return size
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SIZE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getSize() {
        return size;
    }

    @JsonProperty(value = JSON_PROPERTY_SIZE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSize(@org.eclipse.jdt.annotation.Nullable Long size) {
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

        // add `ImageType` to the URL query string
        if (getImageType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sImageType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImageType()))));
        }

        // add `ImageIndex` to the URL query string
        if (getImageIndex() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sImageIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImageIndex()))));
        }

        // add `ImageTag` to the URL query string
        if (getImageTag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImageTag()))));
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `BlurHash` to the URL query string
        if (getBlurHash() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sBlurHash%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBlurHash()))));
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

        // add `Size` to the URL query string
        if (getSize() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sSize%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getSize()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ImageInfo instance;

        public Builder() {
            this(new ImageInfo());
        }

        protected Builder(ImageInfo instance) {
            this.instance = instance;
        }

        public ImageInfo.Builder imageType(ImageType imageType) {
            this.instance.imageType = imageType;
            return this;
        }

        public ImageInfo.Builder imageIndex(Integer imageIndex) {
            this.instance.imageIndex = imageIndex;
            return this;
        }

        public ImageInfo.Builder imageTag(String imageTag) {
            this.instance.imageTag = imageTag;
            return this;
        }

        public ImageInfo.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public ImageInfo.Builder blurHash(String blurHash) {
            this.instance.blurHash = blurHash;
            return this;
        }

        public ImageInfo.Builder height(Integer height) {
            this.instance.height = height;
            return this;
        }

        public ImageInfo.Builder width(Integer width) {
            this.instance.width = width;
            return this;
        }

        public ImageInfo.Builder size(Long size) {
            this.instance.size = size;
            return this;
        }

        /**
         * returns a built ImageInfo instance.
         *
         * The builder is not reusable.
         */
        public ImageInfo build() {
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
    public static ImageInfo.Builder builder() {
        return new ImageInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ImageInfo.Builder toBuilder() {
        return new ImageInfo.Builder().imageType(getImageType()).imageIndex(getImageIndex()).imageTag(getImageTag())
                .path(getPath()).blurHash(getBlurHash()).height(getHeight()).width(getWidth()).size(getSize());
    }
}
