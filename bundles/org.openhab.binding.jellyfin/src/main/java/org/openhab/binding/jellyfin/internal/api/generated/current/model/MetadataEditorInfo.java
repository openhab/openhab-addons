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
 * MetadataEditorInfo
 */
@JsonPropertyOrder({ MetadataEditorInfo.JSON_PROPERTY_PARENTAL_RATING_OPTIONS,
        MetadataEditorInfo.JSON_PROPERTY_COUNTRIES, MetadataEditorInfo.JSON_PROPERTY_CULTURES,
        MetadataEditorInfo.JSON_PROPERTY_EXTERNAL_ID_INFOS, MetadataEditorInfo.JSON_PROPERTY_CONTENT_TYPE,
        MetadataEditorInfo.JSON_PROPERTY_CONTENT_TYPE_OPTIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MetadataEditorInfo {
    public static final String JSON_PROPERTY_PARENTAL_RATING_OPTIONS = "ParentalRatingOptions";
    @org.eclipse.jdt.annotation.NonNull
    private List<ParentalRating> parentalRatingOptions = new ArrayList<>();

    public static final String JSON_PROPERTY_COUNTRIES = "Countries";
    @org.eclipse.jdt.annotation.NonNull
    private List<CountryInfo> countries = new ArrayList<>();

    public static final String JSON_PROPERTY_CULTURES = "Cultures";
    @org.eclipse.jdt.annotation.NonNull
    private List<CultureDto> cultures = new ArrayList<>();

    public static final String JSON_PROPERTY_EXTERNAL_ID_INFOS = "ExternalIdInfos";
    @org.eclipse.jdt.annotation.NonNull
    private List<ExternalIdInfo> externalIdInfos = new ArrayList<>();

    public static final String JSON_PROPERTY_CONTENT_TYPE = "ContentType";
    @org.eclipse.jdt.annotation.NonNull
    private CollectionType contentType;

    public static final String JSON_PROPERTY_CONTENT_TYPE_OPTIONS = "ContentTypeOptions";
    @org.eclipse.jdt.annotation.NonNull
    private List<NameValuePair> contentTypeOptions = new ArrayList<>();

    public MetadataEditorInfo() {
    }

    public MetadataEditorInfo parentalRatingOptions(
            @org.eclipse.jdt.annotation.NonNull List<ParentalRating> parentalRatingOptions) {
        this.parentalRatingOptions = parentalRatingOptions;
        return this;
    }

    public MetadataEditorInfo addParentalRatingOptionsItem(ParentalRating parentalRatingOptionsItem) {
        if (this.parentalRatingOptions == null) {
            this.parentalRatingOptions = new ArrayList<>();
        }
        this.parentalRatingOptions.add(parentalRatingOptionsItem);
        return this;
    }

    /**
     * Get parentalRatingOptions
     * 
     * @return parentalRatingOptions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PARENTAL_RATING_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ParentalRating> getParentalRatingOptions() {
        return parentalRatingOptions;
    }

    @JsonProperty(JSON_PROPERTY_PARENTAL_RATING_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentalRatingOptions(
            @org.eclipse.jdt.annotation.NonNull List<ParentalRating> parentalRatingOptions) {
        this.parentalRatingOptions = parentalRatingOptions;
    }

    public MetadataEditorInfo countries(@org.eclipse.jdt.annotation.NonNull List<CountryInfo> countries) {
        this.countries = countries;
        return this;
    }

    public MetadataEditorInfo addCountriesItem(CountryInfo countriesItem) {
        if (this.countries == null) {
            this.countries = new ArrayList<>();
        }
        this.countries.add(countriesItem);
        return this;
    }

    /**
     * Get countries
     * 
     * @return countries
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_COUNTRIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<CountryInfo> getCountries() {
        return countries;
    }

    @JsonProperty(JSON_PROPERTY_COUNTRIES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCountries(@org.eclipse.jdt.annotation.NonNull List<CountryInfo> countries) {
        this.countries = countries;
    }

    public MetadataEditorInfo cultures(@org.eclipse.jdt.annotation.NonNull List<CultureDto> cultures) {
        this.cultures = cultures;
        return this;
    }

    public MetadataEditorInfo addCulturesItem(CultureDto culturesItem) {
        if (this.cultures == null) {
            this.cultures = new ArrayList<>();
        }
        this.cultures.add(culturesItem);
        return this;
    }

    /**
     * Get cultures
     * 
     * @return cultures
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CULTURES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<CultureDto> getCultures() {
        return cultures;
    }

    @JsonProperty(JSON_PROPERTY_CULTURES)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCultures(@org.eclipse.jdt.annotation.NonNull List<CultureDto> cultures) {
        this.cultures = cultures;
    }

    public MetadataEditorInfo externalIdInfos(
            @org.eclipse.jdt.annotation.NonNull List<ExternalIdInfo> externalIdInfos) {
        this.externalIdInfos = externalIdInfos;
        return this;
    }

    public MetadataEditorInfo addExternalIdInfosItem(ExternalIdInfo externalIdInfosItem) {
        if (this.externalIdInfos == null) {
            this.externalIdInfos = new ArrayList<>();
        }
        this.externalIdInfos.add(externalIdInfosItem);
        return this;
    }

    /**
     * Get externalIdInfos
     * 
     * @return externalIdInfos
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_EXTERNAL_ID_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<ExternalIdInfo> getExternalIdInfos() {
        return externalIdInfos;
    }

    @JsonProperty(JSON_PROPERTY_EXTERNAL_ID_INFOS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalIdInfos(@org.eclipse.jdt.annotation.NonNull List<ExternalIdInfo> externalIdInfos) {
        this.externalIdInfos = externalIdInfos;
    }

    public MetadataEditorInfo contentType(@org.eclipse.jdt.annotation.NonNull CollectionType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Get contentType
     * 
     * @return contentType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public CollectionType getContentType() {
        return contentType;
    }

    @JsonProperty(JSON_PROPERTY_CONTENT_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContentType(@org.eclipse.jdt.annotation.NonNull CollectionType contentType) {
        this.contentType = contentType;
    }

    public MetadataEditorInfo contentTypeOptions(
            @org.eclipse.jdt.annotation.NonNull List<NameValuePair> contentTypeOptions) {
        this.contentTypeOptions = contentTypeOptions;
        return this;
    }

    public MetadataEditorInfo addContentTypeOptionsItem(NameValuePair contentTypeOptionsItem) {
        if (this.contentTypeOptions == null) {
            this.contentTypeOptions = new ArrayList<>();
        }
        this.contentTypeOptions.add(contentTypeOptionsItem);
        return this;
    }

    /**
     * Get contentTypeOptions
     * 
     * @return contentTypeOptions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_CONTENT_TYPE_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<NameValuePair> getContentTypeOptions() {
        return contentTypeOptions;
    }

    @JsonProperty(JSON_PROPERTY_CONTENT_TYPE_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContentTypeOptions(@org.eclipse.jdt.annotation.NonNull List<NameValuePair> contentTypeOptions) {
        this.contentTypeOptions = contentTypeOptions;
    }

    /**
     * Return true if this MetadataEditorInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MetadataEditorInfo metadataEditorInfo = (MetadataEditorInfo) o;
        return Objects.equals(this.parentalRatingOptions, metadataEditorInfo.parentalRatingOptions)
                && Objects.equals(this.countries, metadataEditorInfo.countries)
                && Objects.equals(this.cultures, metadataEditorInfo.cultures)
                && Objects.equals(this.externalIdInfos, metadataEditorInfo.externalIdInfos)
                && Objects.equals(this.contentType, metadataEditorInfo.contentType)
                && Objects.equals(this.contentTypeOptions, metadataEditorInfo.contentTypeOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parentalRatingOptions, countries, cultures, externalIdInfos, contentType,
                contentTypeOptions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MetadataEditorInfo {\n");
        sb.append("    parentalRatingOptions: ").append(toIndentedString(parentalRatingOptions)).append("\n");
        sb.append("    countries: ").append(toIndentedString(countries)).append("\n");
        sb.append("    cultures: ").append(toIndentedString(cultures)).append("\n");
        sb.append("    externalIdInfos: ").append(toIndentedString(externalIdInfos)).append("\n");
        sb.append("    contentType: ").append(toIndentedString(contentType)).append("\n");
        sb.append("    contentTypeOptions: ").append(toIndentedString(contentTypeOptions)).append("\n");
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
