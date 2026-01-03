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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Library type options dto.
 */
@JsonPropertyOrder({ LibraryTypeOptionsDto.JSON_PROPERTY_TYPE, LibraryTypeOptionsDto.JSON_PROPERTY_METADATA_FETCHERS,
        LibraryTypeOptionsDto.JSON_PROPERTY_IMAGE_FETCHERS, LibraryTypeOptionsDto.JSON_PROPERTY_SUPPORTED_IMAGE_TYPES,
        LibraryTypeOptionsDto.JSON_PROPERTY_DEFAULT_IMAGE_OPTIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LibraryTypeOptionsDto {
    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.Nullable
    private String type;

    public static final String JSON_PROPERTY_METADATA_FETCHERS = "MetadataFetchers";
    @org.eclipse.jdt.annotation.Nullable
    private List<LibraryOptionInfoDto> metadataFetchers = new ArrayList<>();

    public static final String JSON_PROPERTY_IMAGE_FETCHERS = "ImageFetchers";
    @org.eclipse.jdt.annotation.Nullable
    private List<LibraryOptionInfoDto> imageFetchers = new ArrayList<>();

    public static final String JSON_PROPERTY_SUPPORTED_IMAGE_TYPES = "SupportedImageTypes";
    @org.eclipse.jdt.annotation.Nullable
    private List<ImageType> supportedImageTypes = new ArrayList<>();

    public static final String JSON_PROPERTY_DEFAULT_IMAGE_OPTIONS = "DefaultImageOptions";
    @org.eclipse.jdt.annotation.Nullable
    private List<ImageOption> defaultImageOptions = new ArrayList<>();

    public LibraryTypeOptionsDto() {
    }

    public LibraryTypeOptionsDto type(@org.eclipse.jdt.annotation.Nullable String type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.Nullable String type) {
        this.type = type;
    }

    public LibraryTypeOptionsDto metadataFetchers(
            @org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> metadataFetchers) {
        this.metadataFetchers = metadataFetchers;
        return this;
    }

    public LibraryTypeOptionsDto addMetadataFetchersItem(LibraryOptionInfoDto metadataFetchersItem) {
        if (this.metadataFetchers == null) {
            this.metadataFetchers = new ArrayList<>();
        }
        this.metadataFetchers.add(metadataFetchersItem);
        return this;
    }

    /**
     * Gets or sets the metadata fetchers.
     * 
     * @return metadataFetchers
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_METADATA_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<LibraryOptionInfoDto> getMetadataFetchers() {
        return metadataFetchers;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataFetchers(@org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> metadataFetchers) {
        this.metadataFetchers = metadataFetchers;
    }

    public LibraryTypeOptionsDto imageFetchers(
            @org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> imageFetchers) {
        this.imageFetchers = imageFetchers;
        return this;
    }

    public LibraryTypeOptionsDto addImageFetchersItem(LibraryOptionInfoDto imageFetchersItem) {
        if (this.imageFetchers == null) {
            this.imageFetchers = new ArrayList<>();
        }
        this.imageFetchers.add(imageFetchersItem);
        return this;
    }

    /**
     * Gets or sets the image fetchers.
     * 
     * @return imageFetchers
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IMAGE_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<LibraryOptionInfoDto> getImageFetchers() {
        return imageFetchers;
    }

    @JsonProperty(value = JSON_PROPERTY_IMAGE_FETCHERS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageFetchers(@org.eclipse.jdt.annotation.Nullable List<LibraryOptionInfoDto> imageFetchers) {
        this.imageFetchers = imageFetchers;
    }

    public LibraryTypeOptionsDto supportedImageTypes(
            @org.eclipse.jdt.annotation.Nullable List<ImageType> supportedImageTypes) {
        this.supportedImageTypes = supportedImageTypes;
        return this;
    }

    public LibraryTypeOptionsDto addSupportedImageTypesItem(ImageType supportedImageTypesItem) {
        if (this.supportedImageTypes == null) {
            this.supportedImageTypes = new ArrayList<>();
        }
        this.supportedImageTypes.add(supportedImageTypesItem);
        return this;
    }

    /**
     * Gets or sets the supported image types.
     * 
     * @return supportedImageTypes
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SUPPORTED_IMAGE_TYPES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ImageType> getSupportedImageTypes() {
        return supportedImageTypes;
    }

    @JsonProperty(value = JSON_PROPERTY_SUPPORTED_IMAGE_TYPES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSupportedImageTypes(@org.eclipse.jdt.annotation.Nullable List<ImageType> supportedImageTypes) {
        this.supportedImageTypes = supportedImageTypes;
    }

    public LibraryTypeOptionsDto defaultImageOptions(
            @org.eclipse.jdt.annotation.Nullable List<ImageOption> defaultImageOptions) {
        this.defaultImageOptions = defaultImageOptions;
        return this;
    }

    public LibraryTypeOptionsDto addDefaultImageOptionsItem(ImageOption defaultImageOptionsItem) {
        if (this.defaultImageOptions == null) {
            this.defaultImageOptions = new ArrayList<>();
        }
        this.defaultImageOptions.add(defaultImageOptionsItem);
        return this;
    }

    /**
     * Gets or sets the default image options.
     * 
     * @return defaultImageOptions
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DEFAULT_IMAGE_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ImageOption> getDefaultImageOptions() {
        return defaultImageOptions;
    }

    @JsonProperty(value = JSON_PROPERTY_DEFAULT_IMAGE_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultImageOptions(@org.eclipse.jdt.annotation.Nullable List<ImageOption> defaultImageOptions) {
        this.defaultImageOptions = defaultImageOptions;
    }

    /**
     * Return true if this LibraryTypeOptionsDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LibraryTypeOptionsDto libraryTypeOptionsDto = (LibraryTypeOptionsDto) o;
        return Objects.equals(this.type, libraryTypeOptionsDto.type)
                && Objects.equals(this.metadataFetchers, libraryTypeOptionsDto.metadataFetchers)
                && Objects.equals(this.imageFetchers, libraryTypeOptionsDto.imageFetchers)
                && Objects.equals(this.supportedImageTypes, libraryTypeOptionsDto.supportedImageTypes)
                && Objects.equals(this.defaultImageOptions, libraryTypeOptionsDto.defaultImageOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, metadataFetchers, imageFetchers, supportedImageTypes, defaultImageOptions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LibraryTypeOptionsDto {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    metadataFetchers: ").append(toIndentedString(metadataFetchers)).append("\n");
        sb.append("    imageFetchers: ").append(toIndentedString(imageFetchers)).append("\n");
        sb.append("    supportedImageTypes: ").append(toIndentedString(supportedImageTypes)).append("\n");
        sb.append("    defaultImageOptions: ").append(toIndentedString(defaultImageOptions)).append("\n");
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

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        // add `MetadataFetchers` to the URL query string
        if (getMetadataFetchers() != null) {
            for (int i = 0; i < getMetadataFetchers().size(); i++) {
                if (getMetadataFetchers().get(i) != null) {
                    joiner.add(getMetadataFetchers().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sMetadataFetchers%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `ImageFetchers` to the URL query string
        if (getImageFetchers() != null) {
            for (int i = 0; i < getImageFetchers().size(); i++) {
                if (getImageFetchers().get(i) != null) {
                    joiner.add(getImageFetchers().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sImageFetchers%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `SupportedImageTypes` to the URL query string
        if (getSupportedImageTypes() != null) {
            for (int i = 0; i < getSupportedImageTypes().size(); i++) {
                if (getSupportedImageTypes().get(i) != null) {
                    joiner.add(String.format(java.util.Locale.ROOT, "%sSupportedImageTypes%s%s=%s", prefix, suffix,
                            "".equals(suffix) ? ""
                                    : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                            containerSuffix),
                            ApiClient.urlEncode(ApiClient.valueToString(getSupportedImageTypes().get(i)))));
                }
            }
        }

        // add `DefaultImageOptions` to the URL query string
        if (getDefaultImageOptions() != null) {
            for (int i = 0; i < getDefaultImageOptions().size(); i++) {
                if (getDefaultImageOptions().get(i) != null) {
                    joiner.add(getDefaultImageOptions().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sDefaultImageOptions%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private LibraryTypeOptionsDto instance;

        public Builder() {
            this(new LibraryTypeOptionsDto());
        }

        protected Builder(LibraryTypeOptionsDto instance) {
            this.instance = instance;
        }

        public LibraryTypeOptionsDto.Builder type(String type) {
            this.instance.type = type;
            return this;
        }

        public LibraryTypeOptionsDto.Builder metadataFetchers(List<LibraryOptionInfoDto> metadataFetchers) {
            this.instance.metadataFetchers = metadataFetchers;
            return this;
        }

        public LibraryTypeOptionsDto.Builder imageFetchers(List<LibraryOptionInfoDto> imageFetchers) {
            this.instance.imageFetchers = imageFetchers;
            return this;
        }

        public LibraryTypeOptionsDto.Builder supportedImageTypes(List<ImageType> supportedImageTypes) {
            this.instance.supportedImageTypes = supportedImageTypes;
            return this;
        }

        public LibraryTypeOptionsDto.Builder defaultImageOptions(List<ImageOption> defaultImageOptions) {
            this.instance.defaultImageOptions = defaultImageOptions;
            return this;
        }

        /**
         * returns a built LibraryTypeOptionsDto instance.
         *
         * The builder is not reusable.
         */
        public LibraryTypeOptionsDto build() {
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
    public static LibraryTypeOptionsDto.Builder builder() {
        return new LibraryTypeOptionsDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LibraryTypeOptionsDto.Builder toBuilder() {
        return new LibraryTypeOptionsDto.Builder().type(getType()).metadataFetchers(getMetadataFetchers())
                .imageFetchers(getImageFetchers()).supportedImageTypes(getSupportedImageTypes())
                .defaultImageOptions(getDefaultImageOptions());
    }
}
