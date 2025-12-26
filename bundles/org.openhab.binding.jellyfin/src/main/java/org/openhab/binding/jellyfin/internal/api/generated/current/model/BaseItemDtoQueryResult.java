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
 * Query result container.
 */
@JsonPropertyOrder({ BaseItemDtoQueryResult.JSON_PROPERTY_ITEMS,
        BaseItemDtoQueryResult.JSON_PROPERTY_TOTAL_RECORD_COUNT, BaseItemDtoQueryResult.JSON_PROPERTY_START_INDEX })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BaseItemDtoQueryResult {
    public static final String JSON_PROPERTY_ITEMS = "Items";
    @org.eclipse.jdt.annotation.NonNull
    private List<BaseItemDto> items = new ArrayList<>();

    public static final String JSON_PROPERTY_TOTAL_RECORD_COUNT = "TotalRecordCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer totalRecordCount;

    public static final String JSON_PROPERTY_START_INDEX = "StartIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer startIndex;

    public BaseItemDtoQueryResult() {
    }

    public BaseItemDtoQueryResult items(@org.eclipse.jdt.annotation.NonNull List<BaseItemDto> items) {
        this.items = items;
        return this;
    }

    public BaseItemDtoQueryResult addItemsItem(BaseItemDto itemsItem) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(itemsItem);
        return this;
    }

    /**
     * Gets or sets the items.
     * 
     * @return items
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<BaseItemDto> getItems() {
        return items;
    }

    @JsonProperty(value = JSON_PROPERTY_ITEMS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItems(@org.eclipse.jdt.annotation.NonNull List<BaseItemDto> items) {
        this.items = items;
    }

    public BaseItemDtoQueryResult totalRecordCount(@org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
        return this;
    }

    /**
     * Gets or sets the total number of records available.
     * 
     * @return totalRecordCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TOTAL_RECORD_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getTotalRecordCount() {
        return totalRecordCount;
    }

    @JsonProperty(value = JSON_PROPERTY_TOTAL_RECORD_COUNT, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTotalRecordCount(@org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    public BaseItemDtoQueryResult startIndex(@org.eclipse.jdt.annotation.NonNull Integer startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Gets or sets the index of the first record in Items.
     * 
     * @return startIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_START_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getStartIndex() {
        return startIndex;
    }

    @JsonProperty(value = JSON_PROPERTY_START_INDEX, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartIndex(@org.eclipse.jdt.annotation.NonNull Integer startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Return true if this BaseItemDtoQueryResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseItemDtoQueryResult baseItemDtoQueryResult = (BaseItemDtoQueryResult) o;
        return Objects.equals(this.items, baseItemDtoQueryResult.items)
                && Objects.equals(this.totalRecordCount, baseItemDtoQueryResult.totalRecordCount)
                && Objects.equals(this.startIndex, baseItemDtoQueryResult.startIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, totalRecordCount, startIndex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BaseItemDtoQueryResult {\n");
        sb.append("    items: ").append(toIndentedString(items)).append("\n");
        sb.append("    totalRecordCount: ").append(toIndentedString(totalRecordCount)).append("\n");
        sb.append("    startIndex: ").append(toIndentedString(startIndex)).append("\n");
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

        // add `Items` to the URL query string
        if (getItems() != null) {
            for (int i = 0; i < getItems().size(); i++) {
                if (getItems().get(i) != null) {
                    joiner.add(getItems().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sItems%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `TotalRecordCount` to the URL query string
        if (getTotalRecordCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sTotalRecordCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTotalRecordCount()))));
        }

        // add `StartIndex` to the URL query string
        if (getStartIndex() != null) {
            joiner.add(String.format(Locale.ROOT, "%sStartIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartIndex()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private BaseItemDtoQueryResult instance;

        public Builder() {
            this(new BaseItemDtoQueryResult());
        }

        protected Builder(BaseItemDtoQueryResult instance) {
            this.instance = instance;
        }

        public BaseItemDtoQueryResult.Builder items(List<BaseItemDto> items) {
            this.instance.items = items;
            return this;
        }

        public BaseItemDtoQueryResult.Builder totalRecordCount(Integer totalRecordCount) {
            this.instance.totalRecordCount = totalRecordCount;
            return this;
        }

        public BaseItemDtoQueryResult.Builder startIndex(Integer startIndex) {
            this.instance.startIndex = startIndex;
            return this;
        }

        /**
         * returns a built BaseItemDtoQueryResult instance.
         *
         * The builder is not reusable.
         */
        public BaseItemDtoQueryResult build() {
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
    public static BaseItemDtoQueryResult.Builder builder() {
        return new BaseItemDtoQueryResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BaseItemDtoQueryResult.Builder toBuilder() {
        return new BaseItemDtoQueryResult.Builder().items(getItems()).totalRecordCount(getTotalRecordCount())
                .startIndex(getStartIndex());
    }
}
