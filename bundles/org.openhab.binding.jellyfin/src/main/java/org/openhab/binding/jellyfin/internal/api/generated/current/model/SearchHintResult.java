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
 * Class SearchHintResult.
 */
@JsonPropertyOrder({ SearchHintResult.JSON_PROPERTY_SEARCH_HINTS, SearchHintResult.JSON_PROPERTY_TOTAL_RECORD_COUNT })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SearchHintResult {
    public static final String JSON_PROPERTY_SEARCH_HINTS = "SearchHints";
    @org.eclipse.jdt.annotation.NonNull
    private List<SearchHint> searchHints = new ArrayList<>();

    public static final String JSON_PROPERTY_TOTAL_RECORD_COUNT = "TotalRecordCount";
    @org.eclipse.jdt.annotation.NonNull
    private Integer totalRecordCount;

    public SearchHintResult() {
    }

    public SearchHintResult searchHints(@org.eclipse.jdt.annotation.NonNull List<SearchHint> searchHints) {
        this.searchHints = searchHints;
        return this;
    }

    public SearchHintResult addSearchHintsItem(SearchHint searchHintsItem) {
        if (this.searchHints == null) {
            this.searchHints = new ArrayList<>();
        }
        this.searchHints.add(searchHintsItem);
        return this;
    }

    /**
     * Gets the search hints.
     * 
     * @return searchHints
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SEARCH_HINTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<SearchHint> getSearchHints() {
        return searchHints;
    }

    @JsonProperty(value = JSON_PROPERTY_SEARCH_HINTS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setSearchHints(@org.eclipse.jdt.annotation.NonNull List<SearchHint> searchHints) {
        this.searchHints = searchHints;
    }

    public SearchHintResult totalRecordCount(@org.eclipse.jdt.annotation.NonNull Integer totalRecordCount) {
        this.totalRecordCount = totalRecordCount;
        return this;
    }

    /**
     * Gets the total record count.
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

    /**
     * Return true if this SearchHintResult object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SearchHintResult searchHintResult = (SearchHintResult) o;
        return Objects.equals(this.searchHints, searchHintResult.searchHints)
                && Objects.equals(this.totalRecordCount, searchHintResult.totalRecordCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(searchHints, totalRecordCount);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SearchHintResult {\n");
        sb.append("    searchHints: ").append(toIndentedString(searchHints)).append("\n");
        sb.append("    totalRecordCount: ").append(toIndentedString(totalRecordCount)).append("\n");
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

        // add `SearchHints` to the URL query string
        if (getSearchHints() != null) {
            for (int i = 0; i < getSearchHints().size(); i++) {
                if (getSearchHints().get(i) != null) {
                    joiner.add(getSearchHints().get(i).toUrlQueryString(
                            String.format(Locale.ROOT, "%sSearchHints%s%s", prefix, suffix, "".equals(suffix) ? ""
                                    : String.format(Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix))));
                }
            }
        }

        // add `TotalRecordCount` to the URL query string
        if (getTotalRecordCount() != null) {
            joiner.add(String.format(Locale.ROOT, "%sTotalRecordCount%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTotalRecordCount()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SearchHintResult instance;

        public Builder() {
            this(new SearchHintResult());
        }

        protected Builder(SearchHintResult instance) {
            this.instance = instance;
        }

        public SearchHintResult.Builder searchHints(List<SearchHint> searchHints) {
            this.instance.searchHints = searchHints;
            return this;
        }

        public SearchHintResult.Builder totalRecordCount(Integer totalRecordCount) {
            this.instance.totalRecordCount = totalRecordCount;
            return this;
        }

        /**
         * returns a built SearchHintResult instance.
         *
         * The builder is not reusable.
         */
        public SearchHintResult build() {
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
    public static SearchHintResult.Builder builder() {
        return new SearchHintResult.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SearchHintResult.Builder toBuilder() {
        return new SearchHintResult.Builder().searchHints(getSearchHints()).totalRecordCount(getTotalRecordCount());
    }
}
