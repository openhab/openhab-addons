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
 * QueryFilters
 */
@JsonPropertyOrder({ QueryFilters.JSON_PROPERTY_GENRES, QueryFilters.JSON_PROPERTY_TAGS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class QueryFilters {
    public static final String JSON_PROPERTY_GENRES = "Genres";
    @org.eclipse.jdt.annotation.NonNull
    private List<NameGuidPair> genres;

    public static final String JSON_PROPERTY_TAGS = "Tags";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> tags;

    public QueryFilters() {
    }

    public QueryFilters genres(@org.eclipse.jdt.annotation.NonNull List<NameGuidPair> genres) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_GENRES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<NameGuidPair> getGenres() {
        return genres;
    }

    @JsonProperty(JSON_PROPERTY_GENRES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGenres(@org.eclipse.jdt.annotation.NonNull List<NameGuidPair> genres) {
        this.genres = genres;
    }

    public QueryFilters tags(@org.eclipse.jdt.annotation.NonNull List<String> tags) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TAGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getTags() {
        return tags;
    }

    @JsonProperty(JSON_PROPERTY_TAGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTags(@org.eclipse.jdt.annotation.NonNull List<String> tags) {
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
}
