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
 * QueryFilters
 */
@JsonPropertyOrder({ QueryFilters.JSON_PROPERTY_GENRES, QueryFilters.JSON_PROPERTY_TAGS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class QueryFilters {
    public static final String JSON_PROPERTY_GENRES = "Genres";
    @org.eclipse.jdt.annotation.Nullable
    private List<NameGuidPair> genres;

    public static final String JSON_PROPERTY_TAGS = "Tags";
    @org.eclipse.jdt.annotation.Nullable
    private List<String> tags;

    public QueryFilters() {
    }

    public QueryFilters genres(@org.eclipse.jdt.annotation.Nullable List<NameGuidPair> genres) {
        this.genres = genres;
        return this;
    }

    public QueryFilters addGenresItem(NameGuidPair genresItem) {
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
    public List<NameGuidPair> getGenres() {
        return genres;
    }

    @JsonProperty(value = JSON_PROPERTY_GENRES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGenres(@org.eclipse.jdt.annotation.Nullable List<NameGuidPair> genres) {
        this.genres = genres;
    }

    public QueryFilters tags(@org.eclipse.jdt.annotation.Nullable List<String> tags) {
        this.tags = tags;
        return this;
    }

    public QueryFilters addTagsItem(String tagsItem) {
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

    /**
     * Return true if this QueryFilters object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        QueryFilters queryFilters = (QueryFilters) o;
        return Objects.equals(this.genres, queryFilters.genres) && Objects.equals(this.tags, queryFilters.tags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genres, tags);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class QueryFilters {\n");
        sb.append("    genres: ").append(toIndentedString(genres)).append("\n");
        sb.append("    tags: ").append(toIndentedString(tags)).append("\n");
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
                if (getGenres().get(i) != null) {
                    joiner.add(getGenres().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sGenres%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
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

        return joiner.toString();
    }

    public static class Builder {

        private QueryFilters instance;

        public Builder() {
            this(new QueryFilters());
        }

        protected Builder(QueryFilters instance) {
            this.instance = instance;
        }

        public QueryFilters.Builder genres(List<NameGuidPair> genres) {
            this.instance.genres = genres;
            return this;
        }

        public QueryFilters.Builder tags(List<String> tags) {
            this.instance.tags = tags;
            return this;
        }

        /**
         * returns a built QueryFilters instance.
         *
         * The builder is not reusable.
         */
        public QueryFilters build() {
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
    public static QueryFilters.Builder builder() {
        return new QueryFilters.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public QueryFilters.Builder toBuilder() {
        return new QueryFilters.Builder().genres(getGenres()).tags(getTags());
    }
}
