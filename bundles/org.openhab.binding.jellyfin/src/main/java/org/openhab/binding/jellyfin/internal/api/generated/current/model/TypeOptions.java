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
 * TypeOptions
 */
@JsonPropertyOrder({ TypeOptions.JSON_PROPERTY_TYPE, TypeOptions.JSON_PROPERTY_METADATA_FETCHERS,
        TypeOptions.JSON_PROPERTY_METADATA_FETCHER_ORDER, TypeOptions.JSON_PROPERTY_IMAGE_FETCHERS,
        TypeOptions.JSON_PROPERTY_IMAGE_FETCHER_ORDER, TypeOptions.JSON_PROPERTY_IMAGE_OPTIONS })

public class TypeOptions {
    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private String type;

    public static final String JSON_PROPERTY_METADATA_FETCHERS = "MetadataFetchers";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> metadataFetchers;

    public static final String JSON_PROPERTY_METADATA_FETCHER_ORDER = "MetadataFetcherOrder";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> metadataFetcherOrder;

    public static final String JSON_PROPERTY_IMAGE_FETCHERS = "ImageFetchers";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> imageFetchers;

    public static final String JSON_PROPERTY_IMAGE_FETCHER_ORDER = "ImageFetcherOrder";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> imageFetcherOrder;

    public static final String JSON_PROPERTY_IMAGE_OPTIONS = "ImageOptions";
    @org.eclipse.jdt.annotation.NonNull
    private List<ImageOption> imageOptions;

    public TypeOptions() {
    }

    public TypeOptions type(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
        return this;
    }

    /**
     * Get type
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull String type) {
        this.type = type;
    }

    public TypeOptions metadataFetchers(@org.eclipse.jdt.annotation.NonNull List<String> metadataFetchers) {
        this.metadataFetchers = metadataFetchers;
        return this;
    }

    public TypeOptions addMetadataFetchersItem(String metadataFetchersItem) {
        if (this.metadataFetchers == null) {
            this.metadataFetchers = new ArrayList<>();
        }
        this.metadataFetchers.add(metadataFetchersItem);
        return this;
    }

    /**
     * Get metadataFetchers
     * 
     * @return metadataFetchers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getMetadataFetchers() {
        return metadataFetchers;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataFetchers(@org.eclipse.jdt.annotation.NonNull List<String> metadataFetchers) {
        this.metadataFetchers = metadataFetchers;
    }

    public TypeOptions metadataFetcherOrder(@org.eclipse.jdt.annotation.NonNull List<String> metadataFetcherOrder) {
        this.metadataFetcherOrder = metadataFetcherOrder;
        return this;
    }

    public TypeOptions addMetadataFetcherOrderItem(String metadataFetcherOrderItem) {
        if (this.metadataFetcherOrder == null) {
            this.metadataFetcherOrder = new ArrayList<>();
        }
        this.metadataFetcherOrder.add(metadataFetcherOrderItem);
        return this;
    }

    /**
     * Get metadataFetcherOrder
     * 
     * @return metadataFetcherOrder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_FETCHER_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getMetadataFetcherOrder() {
        return metadataFetcherOrder;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_FETCHER_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataFetcherOrder(@org.eclipse.jdt.annotation.NonNull List<String> metadataFetcherOrder) {
        this.metadataFetcherOrder = metadataFetcherOrder;
    }

    public TypeOptions imageFetchers(@org.eclipse.jdt.annotation.NonNull List<String> imageFetchers) {
        this.imageFetchers = imageFetchers;
        return this;
    }

    public TypeOptions addImageFetchersItem(String imageFetchersItem) {
        if (this.imageFetchers == null) {
            this.imageFetchers = new ArrayList<>();
        }
        this.imageFetchers.add(imageFetchersItem);
        return this;
    }

    /**
     * Get imageFetchers
     * 
     * @return imageFetchers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getImageFetchers() {
        return imageFetchers;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageFetchers(@org.eclipse.jdt.annotation.NonNull List<String> imageFetchers) {
        this.imageFetchers = imageFetchers;
    }

    public TypeOptions imageFetcherOrder(@org.eclipse.jdt.annotation.NonNull List<String> imageFetcherOrder) {
        this.imageFetcherOrder = imageFetcherOrder;
        return this;
    }

    public TypeOptions addImageFetcherOrderItem(String imageFetcherOrderItem) {
        if (this.imageFetcherOrder == null) {
            this.imageFetcherOrder = new ArrayList<>();
        }
        this.imageFetcherOrder.add(imageFetcherOrderItem);
        return this;
    }

    /**
     * Get imageFetcherOrder
     * 
     * @return imageFetcherOrder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_FETCHER_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getImageFetcherOrder() {
        return imageFetcherOrder;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_FETCHER_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageFetcherOrder(@org.eclipse.jdt.annotation.NonNull List<String> imageFetcherOrder) {
        this.imageFetcherOrder = imageFetcherOrder;
    }

    public TypeOptions imageOptions(@org.eclipse.jdt.annotation.NonNull List<ImageOption> imageOptions) {
        this.imageOptions = imageOptions;
        return this;
    }

    public TypeOptions addImageOptionsItem(ImageOption imageOptionsItem) {
        if (this.imageOptions == null) {
            this.imageOptions = new ArrayList<>();
        }
        this.imageOptions.add(imageOptionsItem);
        return this;
    }

    /**
     * Get imageOptions
     * 
     * @return imageOptions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IMAGE_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ImageOption> getImageOptions() {
        return imageOptions;
    }

    @JsonProperty(JSON_PROPERTY_IMAGE_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setImageOptions(@org.eclipse.jdt.annotation.NonNull List<ImageOption> imageOptions) {
        this.imageOptions = imageOptions;
    }

    /**
     * Return true if this TypeOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TypeOptions typeOptions = (TypeOptions) o;
        return Objects.equals(this.type, typeOptions.type)
                && Objects.equals(this.metadataFetchers, typeOptions.metadataFetchers)
                && Objects.equals(this.metadataFetcherOrder, typeOptions.metadataFetcherOrder)
                && Objects.equals(this.imageFetchers, typeOptions.imageFetchers)
                && Objects.equals(this.imageFetcherOrder, typeOptions.imageFetcherOrder)
                && Objects.equals(this.imageOptions, typeOptions.imageOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, metadataFetchers, metadataFetcherOrder, imageFetchers, imageFetcherOrder,
                imageOptions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TypeOptions {\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    metadataFetchers: ").append(toIndentedString(metadataFetchers)).append("\n");
        sb.append("    metadataFetcherOrder: ").append(toIndentedString(metadataFetcherOrder)).append("\n");
        sb.append("    imageFetchers: ").append(toIndentedString(imageFetchers)).append("\n");
        sb.append("    imageFetcherOrder: ").append(toIndentedString(imageFetcherOrder)).append("\n");
        sb.append("    imageOptions: ").append(toIndentedString(imageOptions)).append("\n");
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
