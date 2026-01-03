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

import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class ChapterInfo.
 */
@JsonPropertyOrder({ ChapterInfo.JSON_PROPERTY_START_POSITION_TICKS, ChapterInfo.JSON_PROPERTY_NAME,
        ChapterInfo.JSON_PROPERTY_IMAGE_PATH, ChapterInfo.JSON_PROPERTY_IMAGE_DATE_MODIFIED,
        ChapterInfo.JSON_PROPERTY_IMAGE_TAG })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ChapterInfo {
    public static final String JSON_PROPERTY_START_POSITION_TICKS = "StartPositionTicks";
    @org.eclipse.jdt.annotation.Nullable
    private Long startPositionTicks;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_IMAGE_PATH = "ImagePath";
    @org.eclipse.jdt.annotation.Nullable
    private String imagePath;

    public static final String JSON_PROPERTY_IMAGE_DATE_MODIFIED = "ImageDateModified";
    @org.eclipse.jdt.annotation.Nullable
    private OffsetDateTime imageDateModified;

    public static final String JSON_PROPERTY_IMAGE_TAG = "ImageTag";
    @org.eclipse.jdt.annotation.Nullable
    private String imageTag;

    public ChapterInfo() {
    }

    public ChapterInfo startPositionTicks(@org.eclipse.jdt.annotation.Nullable Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
        return this;
    }

    /**
     * Gets or sets the start position ticks.
     * 
     * @return startPositionTicks
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_START_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Long getStartPositionTicks() {
        return startPositionTicks;
    }

    @JsonProperty(value = JSON_PROPERTY_START_POSITION_TICKS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartPositionTicks(@org.eclipse.jdt.annotation.Nullable Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
    }

    public ChapterInfo name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
    }

    public ChapterInfo imagePath(@org.eclipse.jdt.annotation.Nullable String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    /**
     * Gets or sets the image path.
     * 
     * @return imagePath
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGE_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getImagePath() {
        return imagePath;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImagePath(@org.eclipse.jdt.annotation.Nullable String imagePath) {
        this.imagePath = imagePath;
    }

    public ChapterInfo imageDateModified(@org.eclipse.jdt.annotation.Nullable OffsetDateTime imageDateModified) {
        this.imageDateModified = imageDateModified;
        return this;
    }

    /**
     * Get imageDateModified
     * 
     * @return imageDateModified
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGE_DATE_MODIFIED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getImageDateModified() {
        return imageDateModified;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_DATE_MODIFIED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageDateModified(@org.eclipse.jdt.annotation.Nullable OffsetDateTime imageDateModified) {
        this.imageDateModified = imageDateModified;
    }

    public ChapterInfo imageTag(@org.eclipse.jdt.annotation.Nullable String imageTag) {
        this.imageTag = imageTag;
        return this;
    }

    /**
     * Get imageTag
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

    /**
     * Return true if this ChapterInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChapterInfo chapterInfo = (ChapterInfo) o;
        return Objects.equals(this.startPositionTicks, chapterInfo.startPositionTicks)
                && Objects.equals(this.name, chapterInfo.name) && Objects.equals(this.imagePath, chapterInfo.imagePath)
                && Objects.equals(this.imageDateModified, chapterInfo.imageDateModified)
                && Objects.equals(this.imageTag, chapterInfo.imageTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startPositionTicks, name, imagePath, imageDateModified, imageTag);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ChapterInfo {\n");
        sb.append("    startPositionTicks: ").append(toIndentedString(startPositionTicks)).append("\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    imagePath: ").append(toIndentedString(imagePath)).append("\n");
        sb.append("    imageDateModified: ").append(toIndentedString(imageDateModified)).append("\n");
        sb.append("    imageTag: ").append(toIndentedString(imageTag)).append("\n");
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

        // add `StartPositionTicks` to the URL query string
        if (getStartPositionTicks() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sStartPositionTicks%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartPositionTicks()))));
        }

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `ImagePath` to the URL query string
        if (getImagePath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sImagePath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImagePath()))));
        }

        // add `ImageDateModified` to the URL query string
        if (getImageDateModified() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sImageDateModified%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImageDateModified()))));
        }

        // add `ImageTag` to the URL query string
        if (getImageTag() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sImageTag%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getImageTag()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ChapterInfo instance;

        public Builder() {
            this(new ChapterInfo());
        }

        protected Builder(ChapterInfo instance) {
            this.instance = instance;
        }

        public ChapterInfo.Builder startPositionTicks(Long startPositionTicks) {
            this.instance.startPositionTicks = startPositionTicks;
            return this;
        }

        public ChapterInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public ChapterInfo.Builder imagePath(String imagePath) {
            this.instance.imagePath = imagePath;
            return this;
        }

        public ChapterInfo.Builder imageDateModified(OffsetDateTime imageDateModified) {
            this.instance.imageDateModified = imageDateModified;
            return this;
        }

        public ChapterInfo.Builder imageTag(String imageTag) {
            this.instance.imageTag = imageTag;
            return this;
        }

        /**
         * returns a built ChapterInfo instance.
         *
         * The builder is not reusable.
         */
        public ChapterInfo build() {
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
    public static ChapterInfo.Builder builder() {
        return new ChapterInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ChapterInfo.Builder toBuilder() {
        return new ChapterInfo.Builder().startPositionTicks(getStartPositionTicks()).name(getName())
                .imagePath(getImagePath()).imageDateModified(getImageDateModified()).imageTag(getImageTag());
    }
}
