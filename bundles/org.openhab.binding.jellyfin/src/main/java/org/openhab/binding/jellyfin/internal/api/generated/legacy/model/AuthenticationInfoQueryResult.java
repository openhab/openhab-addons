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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * AuthenticationInfoQueryResult
 */
@JsonPropertyOrder({ AuthenticationInfoQueryResult.JSON_PROPERTY_ITEMS,
        AuthenticationInfoQueryResult.JSON_PROPERTY_TOTAL_RECORD_COUNT,
        AuthenticationInfoQueryResult.JSON_PROPERTY_START_INDEX })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class AuthenticationInfoQueryResult {
    public static final String JSON_PROPERTY_ITEMS = "Items";
    @org.eclipse.jdt.annotation.NonNull
    private List<AuthenticationInfo> items;

    public static final String JSON_PROPERTY_TOTAL_RECORD_COUNT = "TotalRecordCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer totalRecordCount;

    public static final String JSON_PROPERTY_START_INDEX = "StartIndex";
    @org.eclipse.jdt.annotation.NonNull
    private Integer startIndex;

    public AuthenticationInfoQueryResult() {
    }

    public AuthenticationInfoQueryResult items(@org.eclipse.jdt.annotation.NonNull List<AuthenticationInfo> items) {
        this.items = items;
        return this;
    }

    public AuthenticationInfoQueryResult addItemsItem(AuthenticationInfo itemsItem) {
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
    @JsonProperty(JSON_PROPERTY_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<AuthenticationInfo> getItems() {
        return items;
    }

    @JsonProperty(JSON_PROPERTY_ITEMS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setItems(@org.eclipse.jdt.annotation.NonNull List<AuthenticationInfo> items) {
        this.items = items;
    }

    public AuthenticationInfoQueryResult totalRecordCount(
            @org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
        return this;
    }

    /**
     * Gets or sets the total number of records available.
     * 
     * @return totalRecordCount
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TOTAL_RECORD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getTotalRecordCount() {
        return totalRecordCount;
    }

    @JsonProperty(JSON_PROPERTY_TOTAL_RECORD_COUNT)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTotalRecordCount(@org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
    }

    public AuthenticationInfoQueryResult startIndex(@org.eclipse.jdt.annotation.NonNull Integer startIndex) {
        this.startIndex = startIndex;
        return this;
    }

    /**
     * Gets or sets the index of the first record in Items.
     * 
     * @return startIndex
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_START_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getStartIndex() {
        return startIndex;
    }

    @JsonProperty(JSON_PROPERTY_START_INDEX)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setStartIndex(@org.eclipse.jdt.annotation.NonNull Integer startIndex) {
        this.startIndex = startIndex;
    }

    /**
     * Return true if this AuthenticationInfoQueryResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthenticationInfoQueryResult authenticationInfoQueryResult = (AuthenticationInfoQueryResult) o;
        return Objects.equals(this.items, authenticationInfoQueryResult.items)
                && Objects.equals(this.totalRecordCount, authenticationInfoQueryResult.totalRecordCount)
                && Objects.equals(this.startIndex, authenticationInfoQueryResult.startIndex);
    }

    @Override
    public int hashCode() {
        return Objects.hash(items, totalRecordCount, startIndex);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AuthenticationInfoQueryResult {\n");
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
                    joiner.add(getItems().get(i).toUrlQueryString(String.format("%sItems%s%s", prefix, suffix,
                            "".equals(suffix) ? "" : String.format("%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `TotalRecordCount` to the URL query string
        if (getTotalRecordCount() != null) {
            joiner.add(String.format("%sTotalRecordCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTotalRecordCount()))));
        }

        // add `StartIndex` to the URL query string
        if (getStartIndex() != null) {
            joiner.add(String.format("%sStartIndex%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getStartIndex()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private AuthenticationInfoQueryResult instance;

        public Builder() {
            this(new AuthenticationInfoQueryResult());
        }

        protected Builder(AuthenticationInfoQueryResult instance) {
            this.instance = instance;
        }

        public AuthenticationInfoQueryResult.Builder items(List<AuthenticationInfo> items) {
            this.instance.items = items;
            return this;
        }

        public AuthenticationInfoQueryResult.Builder totalRecordCount(Integer totalRecordCount) {
            this.instance.totalRecordCount = totalRecordCount;
            return this;
        }

        public AuthenticationInfoQueryResult.Builder startIndex(Integer startIndex) {
            this.instance.startIndex = startIndex;
            return this;
        }

        /**
         * returns a built AuthenticationInfoQueryResult instance.
         *
         * The builder is not reusable.
         */
        public AuthenticationInfoQueryResult build() {
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
    public static AuthenticationInfoQueryResult.Builder builder() {
        return new AuthenticationInfoQueryResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public AuthenticationInfoQueryResult.Builder toBuilder() {
        return new AuthenticationInfoQueryResult.Builder().items(getItems()).totalRecordCount(getTotalRecordCount())
                .startIndex(getStartIndex());
    }
}
