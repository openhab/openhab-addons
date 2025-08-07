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
 * Class MetadataOptions.
 */
@JsonPropertyOrder({ MetadataOptions.JSON_PROPERTY_ITEM_TYPE, MetadataOptions.JSON_PROPERTY_DISABLED_METADATA_SAVERS,
        MetadataOptions.JSON_PROPERTY_LOCAL_METADATA_READER_ORDER,
        MetadataOptions.JSON_PROPERTY_DISABLED_METADATA_FETCHERS, MetadataOptions.JSON_PROPERTY_METADATA_FETCHER_ORDER,
        MetadataOptions.JSON_PROPERTY_DISABLED_IMAGE_FETCHERS, MetadataOptions.JSON_PROPERTY_IMAGE_FETCHER_ORDER })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MetadataOptions {
    public static final String JSON_PROPERTY_ITEM_TYPE = "ItemType";
    @org.eclipse.jdt.annotation.NonNull
    private String itemType;

    public static final String JSON_PROPERTY_DISABLED_METADATA_SAVERS = "DisabledMetadataSavers";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> disabledMetadataSavers;

    public static final String JSON_PROPERTY_LOCAL_METADATA_READER_ORDER = "LocalMetadataReaderOrder";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> localMetadataReaderOrder;

    public static final String JSON_PROPERTY_DISABLED_METADATA_FETCHERS = "DisabledMetadataFetchers";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> disabledMetadataFetchers;

    public static final String JSON_PROPERTY_METADATA_FETCHER_ORDER = "MetadataFetcherOrder";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> metadataFetcherOrder;

    public static final String JSON_PROPERTY_DISABLED_IMAGE_FETCHERS = "DisabledImageFetchers";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> disabledImageFetchers;

    public static final String JSON_PROPERTY_IMAGE_FETCHER_ORDER = "ImageFetcherOrder";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> imageFetcherOrder;

    public MetadataOptions() {
    }

    public MetadataOptions itemType(@org.eclipse.jdt.annotation.NonNull String itemType) {
        this.itemType = itemType;
        return this;
    }

    /**
     * Get itemType
     * 
     * @return itemType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ITEM_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getItemType() {
        return itemType;
    }

    @JsonProperty(JSON_PROPERTY_ITEM_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItemType(@org.eclipse.jdt.annotation.NonNull String itemType) {
        this.itemType = itemType;
    }

    public MetadataOptions disabledMetadataSavers(
            @org.eclipse.jdt.annotation.NonNull List<String> disabledMetadataSavers) {
        this.disabledMetadataSavers = disabledMetadataSavers;
        return this;
    }

    public MetadataOptions addDisabledMetadataSaversItem(String disabledMetadataSaversItem) {
        if (this.disabledMetadataSavers == null) {
            this.disabledMetadataSavers = new ArrayList<>();
        }
        this.disabledMetadataSavers.add(disabledMetadataSaversItem);
        return this;
    }

    /**
     * Get disabledMetadataSavers
     * 
     * @return disabledMetadataSavers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISABLED_METADATA_SAVERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDisabledMetadataSavers() {
        return disabledMetadataSavers;
    }

    @JsonProperty(JSON_PROPERTY_DISABLED_METADATA_SAVERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisabledMetadataSavers(@org.eclipse.jdt.annotation.NonNull List<String> disabledMetadataSavers) {
        this.disabledMetadataSavers = disabledMetadataSavers;
    }

    public MetadataOptions localMetadataReaderOrder(
            @org.eclipse.jdt.annotation.NonNull List<String> localMetadataReaderOrder) {
        this.localMetadataReaderOrder = localMetadataReaderOrder;
        return this;
    }

    public MetadataOptions addLocalMetadataReaderOrderItem(String localMetadataReaderOrderItem) {
        if (this.localMetadataReaderOrder == null) {
            this.localMetadataReaderOrder = new ArrayList<>();
        }
        this.localMetadataReaderOrder.add(localMetadataReaderOrderItem);
        return this;
    }

    /**
     * Get localMetadataReaderOrder
     * 
     * @return localMetadataReaderOrder
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_LOCAL_METADATA_READER_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getLocalMetadataReaderOrder() {
        return localMetadataReaderOrder;
    }

    @JsonProperty(JSON_PROPERTY_LOCAL_METADATA_READER_ORDER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLocalMetadataReaderOrder(@org.eclipse.jdt.annotation.NonNull List<String> localMetadataReaderOrder) {
        this.localMetadataReaderOrder = localMetadataReaderOrder;
    }

    public MetadataOptions disabledMetadataFetchers(
            @org.eclipse.jdt.annotation.NonNull List<String> disabledMetadataFetchers) {
        this.disabledMetadataFetchers = disabledMetadataFetchers;
        return this;
    }

    public MetadataOptions addDisabledMetadataFetchersItem(String disabledMetadataFetchersItem) {
        if (this.disabledMetadataFetchers == null) {
            this.disabledMetadataFetchers = new ArrayList<>();
        }
        this.disabledMetadataFetchers.add(disabledMetadataFetchersItem);
        return this;
    }

    /**
     * Get disabledMetadataFetchers
     * 
     * @return disabledMetadataFetchers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISABLED_METADATA_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDisabledMetadataFetchers() {
        return disabledMetadataFetchers;
    }

    @JsonProperty(JSON_PROPERTY_DISABLED_METADATA_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisabledMetadataFetchers(@org.eclipse.jdt.annotation.NonNull List<String> disabledMetadataFetchers) {
        this.disabledMetadataFetchers = disabledMetadataFetchers;
    }

    public MetadataOptions metadataFetcherOrder(@org.eclipse.jdt.annotation.NonNull List<String> metadataFetcherOrder) {
        this.metadataFetcherOrder = metadataFetcherOrder;
        return this;
    }

    public MetadataOptions addMetadataFetcherOrderItem(String metadataFetcherOrderItem) {
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

    public MetadataOptions disabledImageFetchers(
            @org.eclipse.jdt.annotation.NonNull List<String> disabledImageFetchers) {
        this.disabledImageFetchers = disabledImageFetchers;
        return this;
    }

    public MetadataOptions addDisabledImageFetchersItem(String disabledImageFetchersItem) {
        if (this.disabledImageFetchers == null) {
            this.disabledImageFetchers = new ArrayList<>();
        }
        this.disabledImageFetchers.add(disabledImageFetchersItem);
        return this;
    }

    /**
     * Get disabledImageFetchers
     * 
     * @return disabledImageFetchers
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DISABLED_IMAGE_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getDisabledImageFetchers() {
        return disabledImageFetchers;
    }

    @JsonProperty(JSON_PROPERTY_DISABLED_IMAGE_FETCHERS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDisabledImageFetchers(@org.eclipse.jdt.annotation.NonNull List<String> disabledImageFetchers) {
        this.disabledImageFetchers = disabledImageFetchers;
    }

    public MetadataOptions imageFetcherOrder(@org.eclipse.jdt.annotation.NonNull List<String> imageFetcherOrder) {
        this.imageFetcherOrder = imageFetcherOrder;
        return this;
    }

    public MetadataOptions addImageFetcherOrderItem(String imageFetcherOrderItem) {
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

    /**
     * Return true if this MetadataOptions object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetadataOptions metadataOptions = (MetadataOptions) o;
        return Objects.equals(this.itemType, metadataOptions.itemType)
                && Objects.equals(this.disabledMetadataSavers, metadataOptions.disabledMetadataSavers)
                && Objects.equals(this.localMetadataReaderOrder, metadataOptions.localMetadataReaderOrder)
                && Objects.equals(this.disabledMetadataFetchers, metadataOptions.disabledMetadataFetchers)
                && Objects.equals(this.metadataFetcherOrder, metadataOptions.metadataFetcherOrder)
                && Objects.equals(this.disabledImageFetchers, metadataOptions.disabledImageFetchers)
                && Objects.equals(this.imageFetcherOrder, metadataOptions.imageFetcherOrder);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemType, disabledMetadataSavers, localMetadataReaderOrder, disabledMetadataFetchers,
                metadataFetcherOrder, disabledImageFetchers, imageFetcherOrder);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MetadataOptions {\n");
        sb.append("    itemType: ").append(toIndentedString(itemType)).append("\n");
        sb.append("    disabledMetadataSavers: ").append(toIndentedString(disabledMetadataSavers)).append("\n");
        sb.append("    localMetadataReaderOrder: ").append(toIndentedString(localMetadataReaderOrder)).append("\n");
        sb.append("    disabledMetadataFetchers: ").append(toIndentedString(disabledMetadataFetchers)).append("\n");
        sb.append("    metadataFetcherOrder: ").append(toIndentedString(metadataFetcherOrder)).append("\n");
        sb.append("    disabledImageFetchers: ").append(toIndentedString(disabledImageFetchers)).append("\n");
        sb.append("    imageFetcherOrder: ").append(toIndentedString(imageFetcherOrder)).append("\n");
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
