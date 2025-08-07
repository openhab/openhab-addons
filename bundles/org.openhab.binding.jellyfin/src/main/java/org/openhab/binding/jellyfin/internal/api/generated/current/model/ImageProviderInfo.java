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
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class ImageProviderInfo.
 */
@JsonPropertyOrder({ ImageProviderInfo.JSON_PROPERTY_NAME, ImageProviderInfo.JSON_PROPERTY_SUPPORTED_IMAGES })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ImageProviderInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_SUPPORTED_IMAGES = "SupportedImages";
    @org.eclipse.jdt.annotation.NonNull
    private List<ImageType> supportedImages = new ArrayList<>();

    public ImageProviderInfo() {
    }

    public ImageProviderInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the name.
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

    public ImageProviderInfo supportedImages(@org.eclipse.jdt.annotation.NonNull List<ImageType> supportedImages) {
        this.supportedImages = supportedImages;
        return this;
    }

    public ImageProviderInfo addSupportedImagesItem(ImageType supportedImagesItem) {
        if (this.supportedImages == null) {
            this.supportedImages = new ArrayList<>();
        }
        this.supportedImages.add(supportedImagesItem);
        return this;
    }

    /**
     * Gets the supported image types.
     * 
     * @return supportedImages
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_SUPPORTED_IMAGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ImageType> getSupportedImages() {
        return supportedImages;
    }

    @JsonProperty(JSON_PROPERTY_SUPPORTED_IMAGES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportedImages(@org.eclipse.jdt.annotation.NonNull List<ImageType> supportedImages) {
        this.supportedImages = supportedImages;
    }

    /**
     * Return true if this ImageProviderInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ImageProviderInfo imageProviderInfo = (ImageProviderInfo) o;
        return Objects.equals(this.name, imageProviderInfo.name)
                && Objects.equals(this.supportedImages, imageProviderInfo.supportedImages);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, supportedImages);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ImageProviderInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    supportedImages: ").append(toIndentedString(supportedImages)).append("\n");
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
