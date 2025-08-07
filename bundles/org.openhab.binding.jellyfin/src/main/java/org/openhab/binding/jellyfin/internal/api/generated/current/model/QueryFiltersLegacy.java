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
 * QueryFiltersLegacy
 */
@JsonPropertyOrder({ QueryFiltersLegacy.JSON_PROPERTY_GENRES, QueryFiltersLegacy.JSON_PROPERTY_TAGS,
        QueryFiltersLegacy.JSON_PROPERTY_OFFICIAL_RATINGS, QueryFiltersLegacy.JSON_PROPERTY_YEARS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class QueryFiltersLegacy {
    public static final String JSON_PROPERTY_GENRES = "Genres";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> genres;

    public static final String JSON_PROPERTY_TAGS = "Tags";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> tags;

    public static final String JSON_PROPERTY_OFFICIAL_RATINGS = "OfficialRatings";
    @org.eclipse.jdt.annotation.NonNull
    private List<String> officialRatings;

    public static final String JSON_PROPERTY_YEARS = "Years";
    @org.eclipse.jdt.annotation.NonNull
    private List<Integer> years;

    public QueryFiltersLegacy() {
    }

    public QueryFiltersLegacy genres(@org.eclipse.jdt.annotation.NonNull List<String> genres) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_GENRES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getGenres() {
        return genres;
    }

    @JsonProperty(JSON_PROPERTY_GENRES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setGenres(@org.eclipse.jdt.annotation.NonNull List<String> genres) {
        this.genres = genres;
    }

    public QueryFiltersLegacy tags(@org.eclipse.jdt.annotation.NonNull List<String> tags) {
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

    public QueryFiltersLegacy officialRatings(@org.eclipse.jdt.annotation.NonNull List<String> officialRatings) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_OFFICIAL_RATINGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<String> getOfficialRatings() {
        return officialRatings;
    }

    @JsonProperty(JSON_PROPERTY_OFFICIAL_RATINGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOfficialRatings(@org.eclipse.jdt.annotation.NonNull List<String> officialRatings) {
        this.officialRatings = officialRatings;
    }

    public QueryFiltersLegacy years(@org.eclipse.jdt.annotation.NonNull List<Integer> years) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_YEARS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<Integer> getYears() {
        return years;
    }

    @JsonProperty(JSON_PROPERTY_YEARS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setYears(@org.eclipse.jdt.annotation.NonNull List<Integer> years) {
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
}
