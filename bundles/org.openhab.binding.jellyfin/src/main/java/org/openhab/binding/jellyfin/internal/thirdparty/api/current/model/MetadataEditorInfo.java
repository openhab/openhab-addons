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
 * A class representing metadata editor information.
 */
@JsonPropertyOrder({ MetadataEditorInfo.JSON_PROPERTY_PARENTAL_RATING_OPTIONS,
        MetadataEditorInfo.JSON_PROPERTY_COUNTRIES, MetadataEditorInfo.JSON_PROPERTY_CULTURES,
        MetadataEditorInfo.JSON_PROPERTY_EXTERNAL_ID_INFOS, MetadataEditorInfo.JSON_PROPERTY_CONTENT_TYPE,
        MetadataEditorInfo.JSON_PROPERTY_CONTENT_TYPE_OPTIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MetadataEditorInfo {
    public static final String JSON_PROPERTY_PARENTAL_RATING_OPTIONS = "ParentalRatingOptions";
    @org.eclipse.jdt.annotation.Nullable
    private List<ParentalRating> parentalRatingOptions = new ArrayList<>();

    public static final String JSON_PROPERTY_COUNTRIES = "Countries";
    @org.eclipse.jdt.annotation.Nullable
    private List<CountryInfo> countries = new ArrayList<>();

    public static final String JSON_PROPERTY_CULTURES = "Cultures";
    @org.eclipse.jdt.annotation.Nullable
    private List<CultureDto> cultures = new ArrayList<>();

    public static final String JSON_PROPERTY_EXTERNAL_ID_INFOS = "ExternalIdInfos";
    @org.eclipse.jdt.annotation.Nullable
    private List<ExternalIdInfo> externalIdInfos = new ArrayList<>();

    public static final String JSON_PROPERTY_CONTENT_TYPE = "ContentType";
    @org.eclipse.jdt.annotation.Nullable
    private CollectionType contentType;

    public static final String JSON_PROPERTY_CONTENT_TYPE_OPTIONS = "ContentTypeOptions";
    @org.eclipse.jdt.annotation.Nullable
    private List<NameValuePair> contentTypeOptions = new ArrayList<>();

    public MetadataEditorInfo() {
    }

    public MetadataEditorInfo parentalRatingOptions(
            @org.eclipse.jdt.annotation.Nullable List<ParentalRating> parentalRatingOptions) {
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
     * Gets or sets the parental rating options.
     * 
     * @return parentalRatingOptions
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PARENTAL_RATING_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ParentalRating> getParentalRatingOptions() {
        return parentalRatingOptions;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENTAL_RATING_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentalRatingOptions(
            @org.eclipse.jdt.annotation.Nullable List<ParentalRating> parentalRatingOptions) {
        this.parentalRatingOptions = parentalRatingOptions;
    }

    public MetadataEditorInfo countries(@org.eclipse.jdt.annotation.Nullable List<CountryInfo> countries) {
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
     * Gets or sets the countries.
     * 
     * @return countries
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_COUNTRIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<CountryInfo> getCountries() {
        return countries;
    }

    @JsonProperty(value = JSON_PROPERTY_COUNTRIES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCountries(@org.eclipse.jdt.annotation.Nullable List<CountryInfo> countries) {
        this.countries = countries;
    }

    public MetadataEditorInfo cultures(@org.eclipse.jdt.annotation.Nullable List<CultureDto> cultures) {
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
     * Gets or sets the cultures.
     * 
     * @return cultures
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CULTURES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<CultureDto> getCultures() {
        return cultures;
    }

    @JsonProperty(value = JSON_PROPERTY_CULTURES, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setCultures(@org.eclipse.jdt.annotation.Nullable List<CultureDto> cultures) {
        this.cultures = cultures;
    }

    public MetadataEditorInfo externalIdInfos(
            @org.eclipse.jdt.annotation.Nullable List<ExternalIdInfo> externalIdInfos) {
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
     * Gets or sets the external id infos.
     * 
     * @return externalIdInfos
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_EXTERNAL_ID_INFOS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<ExternalIdInfo> getExternalIdInfos() {
        return externalIdInfos;
    }

    @JsonProperty(value = JSON_PROPERTY_EXTERNAL_ID_INFOS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setExternalIdInfos(@org.eclipse.jdt.annotation.Nullable List<ExternalIdInfo> externalIdInfos) {
        this.externalIdInfos = externalIdInfos;
    }

    public MetadataEditorInfo contentType(@org.eclipse.jdt.annotation.Nullable CollectionType contentType) {
        this.contentType = contentType;
        return this;
    }

    /**
     * Gets or sets the content type.
     * 
     * @return contentType
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CONTENT_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public CollectionType getContentType() {
        return contentType;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTENT_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContentType(@org.eclipse.jdt.annotation.Nullable CollectionType contentType) {
        this.contentType = contentType;
    }

    public MetadataEditorInfo contentTypeOptions(
            @org.eclipse.jdt.annotation.Nullable List<NameValuePair> contentTypeOptions) {
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
     * Gets or sets the content type options.
     * 
     * @return contentTypeOptions
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_CONTENT_TYPE_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NameValuePair> getContentTypeOptions() {
        return contentTypeOptions;
    }

    @JsonProperty(value = JSON_PROPERTY_CONTENT_TYPE_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setContentTypeOptions(@org.eclipse.jdt.annotation.Nullable List<NameValuePair> contentTypeOptions) {
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

        // add `ParentalRatingOptions` to the URL query string
        if (getParentalRatingOptions() != null) {
            for (int i = 0; i < getParentalRatingOptions().size(); i++) {
                if (getParentalRatingOptions().get(i) != null) {
                    joiner.add(getParentalRatingOptions().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sParentalRatingOptions%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `Countries` to the URL query string
        if (getCountries() != null) {
            for (int i = 0; i < getCountries().size(); i++) {
                if (getCountries().get(i) != null) {
                    joiner.add(getCountries().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sCountries%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `Cultures` to the URL query string
        if (getCultures() != null) {
            for (int i = 0; i < getCultures().size(); i++) {
                if (getCultures().get(i) != null) {
                    joiner.add(getCultures().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sCultures%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `ExternalIdInfos` to the URL query string
        if (getExternalIdInfos() != null) {
            for (int i = 0; i < getExternalIdInfos().size(); i++) {
                if (getExternalIdInfos().get(i) != null) {
                    joiner.add(getExternalIdInfos().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sExternalIdInfos%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `ContentType` to the URL query string
        if (getContentType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sContentType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getContentType()))));
        }

        // add `ContentTypeOptions` to the URL query string
        if (getContentTypeOptions() != null) {
            for (int i = 0; i < getContentTypeOptions().size(); i++) {
                if (getContentTypeOptions().get(i) != null) {
                    joiner.add(getContentTypeOptions().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sContentTypeOptions%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        return joiner.toString();
    }

    public static class Builder {

        private MetadataEditorInfo instance;

        public Builder() {
            this(new MetadataEditorInfo());
        }

        protected Builder(MetadataEditorInfo instance) {
            this.instance = instance;
        }

        public MetadataEditorInfo.Builder parentalRatingOptions(List<ParentalRating> parentalRatingOptions) {
            this.instance.parentalRatingOptions = parentalRatingOptions;
            return this;
        }

        public MetadataEditorInfo.Builder countries(List<CountryInfo> countries) {
            this.instance.countries = countries;
            return this;
        }

        public MetadataEditorInfo.Builder cultures(List<CultureDto> cultures) {
            this.instance.cultures = cultures;
            return this;
        }

        public MetadataEditorInfo.Builder externalIdInfos(List<ExternalIdInfo> externalIdInfos) {
            this.instance.externalIdInfos = externalIdInfos;
            return this;
        }

        public MetadataEditorInfo.Builder contentType(CollectionType contentType) {
            this.instance.contentType = contentType;
            return this;
        }

        public MetadataEditorInfo.Builder contentTypeOptions(List<NameValuePair> contentTypeOptions) {
            this.instance.contentTypeOptions = contentTypeOptions;
            return this;
        }

        /**
         * returns a built MetadataEditorInfo instance.
         *
         * The builder is not reusable.
         */
        public MetadataEditorInfo build() {
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
    public static MetadataEditorInfo.Builder builder() {
        return new MetadataEditorInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MetadataEditorInfo.Builder toBuilder() {
        return new MetadataEditorInfo.Builder().parentalRatingOptions(getParentalRatingOptions())
                .countries(getCountries()).cultures(getCultures()).externalIdInfos(getExternalIdInfos())
                .contentType(getContentType()).contentTypeOptions(getContentTypeOptions());
    }
}
