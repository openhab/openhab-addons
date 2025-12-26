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

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

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
    @JsonProperty(value = JSON_PROPERTY_SUPPORTED_IMAGES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ImageType> getSupportedImages() {
        return supportedImages;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTED_IMAGES, required = false)
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

        // add `SupportedImages` to the URL query string
        if (getSupportedImages() != null) {
            for (int i = 0; i < getSupportedImages().size(); i++) {
                if (getSupportedImages().get(i) != null) {
                    joiner.add(String.format(Locale.ROOT, "%sSupportedImages%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getSupportedImages().get(i)))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private ImageProviderInfo instance;

        public Builder() {
            this(new ImageProviderInfo());
        }

        protected Builder(ImageProviderInfo instance) {
            this.instance = instance;
        }

        public ImageProviderInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public ImageProviderInfo.Builder supportedImages(List<ImageType> supportedImages) {
            this.instance.supportedImages = supportedImages;
            return this;
        }

        /**
         * returns a built ImageProviderInfo instance.
         *
         * The builder is not reusable.
         */
        public ImageProviderInfo build() {
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
    public static ImageProviderInfo.Builder builder() {
        return new ImageProviderInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ImageProviderInfo.Builder toBuilder() {
        return new ImageProviderInfo.Builder().name(getName()).supportedImages(getSupportedImages());
    }
}
