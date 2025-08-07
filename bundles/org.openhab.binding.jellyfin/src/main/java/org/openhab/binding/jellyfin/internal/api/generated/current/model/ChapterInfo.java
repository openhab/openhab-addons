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

import java.time.OffsetDateTime;
import java.util.Objects;

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
    @org.eclipse.jdt.annotation.NonNull
    private Long startPositionTicks;

    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_IMAGE_PATH = "ImagePath";
    @org.eclipse.jdt.annotation.NonNull
    private String imagePath;

    public static final String JSON_PROPERTY_IMAGE_DATE_MODIFIED = "ImageDateModified";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime imageDateModified;

    public static final String JSON_PROPERTY_IMAGE_TAG = "ImageTag";
    @org.eclipse.jdt.annotation.NonNull
    private String imageTag;

    public ChapterInfo() {
    }

    public ChapterInfo startPositionTicks(@org.eclipse.jdt.annotation.NonNull Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
        return this;
    }

    /**
     * Gets or sets the start position ticks.
     * 
     * @return startPositionTicks
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Long getStartPositionTicks() {
        return startPositionTicks;
    }

    @JsonProperty(JSON_PROPERTY_START_POSITION_TICKS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartPositionTicks(@org.eclipse.jdt.annotation.NonNull Long startPositionTicks) {
        this.startPositionTicks = startPositionTicks;
    }

    public ChapterInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
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

    public ChapterInfo imagePath(@org.eclipse.jdt.annotation.NonNull String imagePath) {
        this.imagePath = imagePath;
        return this;
    }

    /**
     * Gets or sets the image path.
     * 
     * @return imagePath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getImagePath() {
        return imagePath;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImagePath(@org.eclipse.jdt.annotation.NonNull String imagePath) {
        this.imagePath = imagePath;
    }

    public ChapterInfo imageDateModified(@org.eclipse.jdt.annotation.NonNull OffsetDateTime imageDateModified) {
        this.imageDateModified = imageDateModified;
        return this;
    }

    /**
     * Get imageDateModified
     * 
     * @return imageDateModified
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_DATE_MODIFIED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getImageDateModified() {
        return imageDateModified;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_DATE_MODIFIED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageDateModified(@org.eclipse.jdt.annotation.NonNull OffsetDateTime imageDateModified) {
        this.imageDateModified = imageDateModified;
    }

    public ChapterInfo imageTag(@org.eclipse.jdt.annotation.NonNull String imageTag) {
        this.imageTag = imageTag;
        return this;
    }

    /**
     * Get imageTag
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
}
