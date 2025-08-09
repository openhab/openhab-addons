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
 * Class SearchHintResult.
 */
@JsonPropertyOrder({ SearchHintResult.JSON_PROPERTY_SEARCH_HINTS, SearchHintResult.JSON_PROPERTY_TOTAL_RECORD_COUNT })

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
    @JsonProperty(JSON_PROPERTY_SEARCH_HINTS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<SearchHint> getSearchHints() {
        return searchHints;
    }

    @JsonProperty(JSON_PROPERTY_SEARCH_HINTS)
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
}
