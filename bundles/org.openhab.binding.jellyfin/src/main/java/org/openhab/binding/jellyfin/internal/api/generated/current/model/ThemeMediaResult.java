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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Class ThemeMediaResult.
 */
@JsonPropertyOrder({ ThemeMediaResult.JSON_PROPERTY_ITEMS, ThemeMediaResult.JSON_PROPERTY_TOTAL_RECORD_COUNT,
        ThemeMediaResult.JSON_PROPERTY_START_INDEX, ThemeMediaResult.JSON_PROPERTY_OWNER_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ThemeMediaResult {
    public static final String JSON_PROPERTY_ITEMS = "Items";
    @org.eclipse.jdt.annotation.NonNull
    private List<BaseItemDto> items = new ArrayList<>();

    public static final String JSON_PROPERTY_TOTAL_RECORD_COUNT = "TotalRecordCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer totalRecordCount;

    public static final String JSON_PROPERTY_START_INDEX = "StartIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer startIndex;

    public static final String JSON_PROPERTY_OWNER_ID = "OwnerId";
    @org.eclipse.jdt.annotation.NonNull
    private UUID ownerId;

    public ThemeMediaResult() {
    }

    public ThemeMediaResult items(@org.eclipse.jdt.annotation.NonNull List<BaseItemDto> items) {
        this.items = items;
        return this;
    }

    public ThemeMediaResult addItemsItem(BaseItemDto itemsItem) {
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

    public ThemeMediaResult totalRecordCount(@org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
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

    public ThemeMediaResult startIndex(@org.eclipse.jdt.annotation.NonNull Integer startIndex) {
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

    public ThemeMediaResult ownerId(@org.eclipse.jdt.annotation.NonNull UUID ownerId) {
        this.ownerId = ownerId;
        return this;
    }

    /**
     * Gets or sets the owner id.
     * 
     * @return ownerId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OWNER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getOwnerId() {
        return ownerId;
    }

    @JsonProperty(value = JSON_PROPERTY_OWNER_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOwnerId(@org.eclipse.jdt.annotation.NonNull UUID ownerId) {
        this.ownerId = ownerId;
    }

    /**
     * Return true if this ThemeMediaResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ThemeMediaResult themeMediaResult = (ThemeMediaResult) o;
        return Objects.equals(this.items, themeMediaResult.items)
                && Objects.equals(this.totalRecordCount, themeMediaResult.totalRecordCount)
                && Objects.equals(this.startIndex, themeMediaResult.startIndex)
                && Objects.equals(this.ownerId, themeMediaResult.ownerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, totalRecordCount, startIndex, ownerId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ThemeMediaResult {\n");
        sb.append("    items: ").append(toIndentedString(items)).append("\n");
        sb.append("    totalRecordCount: ").append(toIndentedString(totalRecordCount)).append("\n");
        sb.append("    startIndex: ").append(toIndentedString(startIndex)).append("\n");
        sb.append("    ownerId: ").append(toIndentedString(ownerId)).append("\n");
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

        // add `OwnerId` to the URL query string
        if (getOwnerId() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOwnerId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOwnerId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ThemeMediaResult instance;

        public Builder() {
            this(new ThemeMediaResult());
        }

        protected Builder(ThemeMediaResult instance) {
            this.instance = instance;
        }

        public ThemeMediaResult.Builder items(List<BaseItemDto> items) {
            this.instance.items = items;
            return this;
        }

        public ThemeMediaResult.Builder totalRecordCount(Integer totalRecordCount) {
            this.instance.totalRecordCount = totalRecordCount;
            return this;
        }

        public ThemeMediaResult.Builder startIndex(Integer startIndex) {
            this.instance.startIndex = startIndex;
            return this;
        }

        public ThemeMediaResult.Builder ownerId(UUID ownerId) {
            this.instance.ownerId = ownerId;
            return this;
        }

        /**
         * returns a built ThemeMediaResult instance.
         *
         * The builder is not reusable.
         */
        public ThemeMediaResult build() {
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
    public static ThemeMediaResult.Builder builder() {
        return new ThemeMediaResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ThemeMediaResult.Builder toBuilder() {
        return new ThemeMediaResult.Builder().items(getItems()).totalRecordCount(getTotalRecordCount())
                .startIndex(getStartIndex()).ownerId(getOwnerId());
    }
}
