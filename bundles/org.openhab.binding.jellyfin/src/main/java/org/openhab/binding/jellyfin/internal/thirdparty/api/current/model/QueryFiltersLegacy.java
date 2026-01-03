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
 * QueryFiltersLegacy
 */
@JsonPropertyOrder({ QueryFiltersLegacy.JSON_PROPERTY_GENRES, QueryFiltersLegacy.JSON_PROPERTY_TAGS,
        QueryFiltersLegacy.JSON_PROPERTY_OFFICIAL_RATINGS, QueryFiltersLegacy.JSON_PROPERTY_YEARS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class QueryFiltersLegacy {
    public static final String JSON_PROPERTY_GENRES = "Genres";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> genres;

    public static final String JSON_PROPERTY_TAGS = "Tags";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> tags;

    public static final String JSON_PROPERTY_OFFICIAL_RATINGS = "OfficialRatings";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> officialRatings;

    public static final String JSON_PROPERTY_YEARS = "Years";
    @org.eclipse.jdt.annotation.Nullable
    private List<Integer> years;

    public QueryFiltersLegacy() {
    }

    public QueryFiltersLegacy genres(@org.eclipse.jdt.annotation.Nullable List<String> genres) {
        this.genres = genres;
        return this;
    }

    public QueryFiltersLegacy addGenresItem(String genresItem) {
        if (this.genres == null) {
            this.genres = new ArrayList<>();
        }
        this.genres.add(genresItem);
        return this;
    }

    /**
     * Get genres
     * 
     * @return genres
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_GENRES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getGenres() {
        return genres;
    }

    @JsonProperty(value = JSON_PROPERTY_GENRES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGenres(@org.eclipse.jdt.annotation.Nullable List<String> genres) {
        this.genres = genres;
    }

    public QueryFiltersLegacy tags(@org.eclipse.jdt.annotation.Nullable List<String> tags) {
        this.tags = tags;
        return this;
    }

    public QueryFiltersLegacy addTagsItem(String tagsItem) {
        if (this.tags == null) {
            this.tags = new ArrayList<>();
        }
        this.tags.add(tagsItem);
        return this;
    }

    /**
     * Get tags
     * 
     * @return tags
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getTags() {
        return tags;
    }

    @JsonProperty(value = JSON_PROPERTY_TAGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTags(@org.eclipse.jdt.annotation.Nullable List<String> tags) {
        this.tags = tags;
    }

    public QueryFiltersLegacy officialRatings(@org.eclipse.jdt.annotation.Nullable List<String> officialRatings) {
        this.officialRatings = officialRatings;
        return this;
    }

    public QueryFiltersLegacy addOfficialRatingsItem(String officialRatingsItem) {
        if (this.officialRatings == null) {
            this.officialRatings = new ArrayList<>();
        }
        this.officialRatings.add(officialRatingsItem);
        return this;
    }

    /**
     * Get officialRatings
     * 
     * @return officialRatings
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_OFFICIAL_RATINGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<String> getOfficialRatings() {
        return officialRatings;
    }

    @JsonProperty(value = JSON_PROPERTY_OFFICIAL_RATINGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOfficialRatings(@org.eclipse.jdt.annotation.Nullable List<String> officialRatings) {
        this.officialRatings = officialRatings;
    }

    public QueryFiltersLegacy years(@org.eclipse.jdt.annotation.Nullable List<Integer> years) {
        this.years = years;
        return this;
    }

    public QueryFiltersLegacy addYearsItem(Integer yearsItem) {
        if (this.years == null) {
            this.years = new ArrayList<>();
        }
        this.years.add(yearsItem);
        return this;
    }

    /**
     * Get years
     * 
     * @return years
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_YEARS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<Integer> getYears() {
        return years;
    }

    @JsonProperty(value = JSON_PROPERTY_YEARS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setYears(@org.eclipse.jdt.annotation.Nullable List<Integer> years) {
        this.years = years;
    }

    /**
     * Return true if this QueryFiltersLegacy object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryFiltersLegacy queryFiltersLegacy = (QueryFiltersLegacy) o;
        return Objects.equals(this.genres, queryFiltersLegacy.genres)
                && Objects.equals(this.tags, queryFiltersLegacy.tags)
                && Objects.equals(this.officialRatings, queryFiltersLegacy.officialRatings)
                && Objects.equals(this.years, queryFiltersLegacy.years);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genres, tags, officialRatings, years);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QueryFiltersLegacy {\n");
        sb.append("    genres: ").append(toIndentedString(genres)).append("\n");
        sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
        sb.append("    officialRatings: ").append(toIndentedString(officialRatings)).append("\n");
        sb.append("    years: ").append(toIndentedString(years)).append("\n");
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

        // add `Genres` to the URL query string
        if (getGenres() != null) {
            for (int i = 0; i < getGenres().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sGenres%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getGenres().get(i)))));
            }
        }

        // add `Tags` to the URL query string
        if (getTags() != null) {
            for (int i = 0; i < getTags().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sTags%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getTags().get(i)))));
            }
        }

        // add `OfficialRatings` to the URL query string
        if (getOfficialRatings() != null) {
            for (int i = 0; i < getOfficialRatings().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sOfficialRatings%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getOfficialRatings().get(i)))));
            }
        }

        // add `Years` to the URL query string
        if (getYears() != null) {
            for (int i = 0; i < getYears().size(); i++) {
                joiner.add(String.format(java.util.Locale.ROOT, "%sYears%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i, containerSuffix),
                        ApiClient.urlEncode(ApiClient.valueToString(getYears().get(i)))));
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private QueryFiltersLegacy instance;

        public Builder() {
            this(new QueryFiltersLegacy());
        }

        protected Builder(QueryFiltersLegacy instance) {
            this.instance = instance;
        }

        public QueryFiltersLegacy.Builder genres(List<String> genres) {
            this.instance.genres = genres;
            return this;
        }

        public QueryFiltersLegacy.Builder tags(List<String> tags) {
            this.instance.tags = tags;
            return this;
        }

        public QueryFiltersLegacy.Builder officialRatings(List<String> officialRatings) {
            this.instance.officialRatings = officialRatings;
            return this;
        }

        public QueryFiltersLegacy.Builder years(List<Integer> years) {
            this.instance.years = years;
            return this;
        }

        /**
         * returns a built QueryFiltersLegacy instance.
         *
         * The builder is not reusable.
         */
        public QueryFiltersLegacy build() {
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
    public static QueryFiltersLegacy.Builder builder() {
        return new QueryFiltersLegacy.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public QueryFiltersLegacy.Builder toBuilder() {
        return new QueryFiltersLegacy.Builder().genres(getGenres()).tags(getTags())
                .officialRatings(getOfficialRatings()).years(getYears());
    }
}
