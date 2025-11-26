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

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * BoxSetInfo
 */
@JsonPropertyOrder({ BoxSetInfo.JSON_PROPERTY_NAME, BoxSetInfo.JSON_PROPERTY_ORIGINAL_TITLE,
        BoxSetInfo.JSON_PROPERTY_PATH, BoxSetInfo.JSON_PROPERTY_METADATA_LANGUAGE,
        BoxSetInfo.JSON_PROPERTY_METADATA_COUNTRY_CODE, BoxSetInfo.JSON_PROPERTY_PROVIDER_IDS,
        BoxSetInfo.JSON_PROPERTY_YEAR, BoxSetInfo.JSON_PROPERTY_INDEX_NUMBER,
        BoxSetInfo.JSON_PROPERTY_PARENT_INDEX_NUMBER, BoxSetInfo.JSON_PROPERTY_PREMIERE_DATE,
        BoxSetInfo.JSON_PROPERTY_IS_AUTOMATED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BoxSetInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_ORIGINAL_TITLE = "OriginalTitle";
    @org.eclipse.jdt.annotation.NonNull
    private String originalTitle;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_METADATA_LANGUAGE = "MetadataLanguage";
    @org.eclipse.jdt.annotation.NonNull
    private String metadataLanguage;

    public static final String JSON_PROPERTY_METADATA_COUNTRY_CODE = "MetadataCountryCode";
    @org.eclipse.jdt.annotation.NonNull
    private String metadataCountryCode;

    public static final String JSON_PROPERTY_PROVIDER_IDS = "ProviderIds";
    @org.eclipse.jdt.annotation.NonNull
    private Map<String, String> providerIds;

    public static final String JSON_PROPERTY_YEAR = "Year";
    @org.eclipse.jdt.annotation.NonNull
    private Integer year;

    public static final String JSON_PROPERTY_INDEX_NUMBER = "IndexNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer indexNumber;

    public static final String JSON_PROPERTY_PARENT_INDEX_NUMBER = "ParentIndexNumber";
    @org.eclipse.jdt.annotation.NonNull
    private Integer parentIndexNumber;

    public static final String JSON_PROPERTY_PREMIERE_DATE = "PremiereDate";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime premiereDate;

    public static final String JSON_PROPERTY_IS_AUTOMATED = "IsAutomated";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isAutomated;

    public BoxSetInfo() {
    }

    public BoxSetInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public BoxSetInfo originalTitle(@org.eclipse.jdt.annotation.NonNull String originalTitle) {
        this.originalTitle = originalTitle;
        return this;
    }

    /**
     * Gets or sets the original title.
     * 
     * @return originalTitle
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_ORIGINAL_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getOriginalTitle() {
        return originalTitle;
    }

    @JsonProperty(value = JSON_PROPERTY_ORIGINAL_TITLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOriginalTitle(@org.eclipse.jdt.annotation.NonNull String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public BoxSetInfo path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPath() {
        return path;
    }

    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public BoxSetInfo metadataLanguage(@org.eclipse.jdt.annotation.NonNull String metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
        return this;
    }

    /**
     * Gets or sets the metadata language.
     * 
     * @return metadataLanguage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_METADATA_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMetadataLanguage() {
        return metadataLanguage;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataLanguage(@org.eclipse.jdt.annotation.NonNull String metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
    }

    public BoxSetInfo metadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
        return this;
    }

    /**
     * Gets or sets the metadata country code.
     * 
     * @return metadataCountryCode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_METADATA_COUNTRY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMetadataCountryCode() {
        return metadataCountryCode;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA_COUNTRY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
    }

    public BoxSetInfo providerIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> providerIds) {
        this.providerIds = providerIds;
        return this;
    }

    public BoxSetInfo putProviderIdsItem(String key, String providerIdsItem) {
        if (this.providerIds == null) {
            this.providerIds = new HashMap<>();
        }
        this.providerIds.put(key, providerIdsItem);
        return this;
    }

    /**
     * Gets or sets the provider ids.
     * 
     * @return providerIds
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_IDS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public Map<String, String> getProviderIds() {
        return providerIds;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_IDS, required = false)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> providerIds) {
        this.providerIds = providerIds;
    }

    public BoxSetInfo year(@org.eclipse.jdt.annotation.NonNull Integer year) {
        this.year = year;
        return this;
    }

    /**
     * Gets or sets the year.
     * 
     * @return year
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_YEAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getYear() {
        return year;
    }

    @JsonProperty(value = JSON_PROPERTY_YEAR, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setYear(@org.eclipse.jdt.annotation.NonNull Integer year) {
        this.year = year;
    }

    public BoxSetInfo indexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
        return this;
    }

    /**
     * Get indexNumber
     * 
     * @return indexNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getIndexNumber() {
        return indexNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
    }

    public BoxSetInfo parentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
        return this;
    }

    /**
     * Get parentIndexNumber
     * 
     * @return parentIndexNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PARENT_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Integer getParentIndexNumber() {
        return parentIndexNumber;
    }

    @JsonProperty(value = JSON_PROPERTY_PARENT_INDEX_NUMBER, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
    }

    public BoxSetInfo premiereDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
        return this;
    }

    /**
     * Get premiereDate
     * 
     * @return premiereDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PREMIERE_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getPremiereDate() {
        return premiereDate;
    }

    @JsonProperty(value = JSON_PROPERTY_PREMIERE_DATE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPremiereDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
    }

    public BoxSetInfo isAutomated(@org.eclipse.jdt.annotation.NonNull Boolean isAutomated) {
        this.isAutomated = isAutomated;
        return this;
    }

    /**
     * Get isAutomated
     * 
     * @return isAutomated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_IS_AUTOMATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsAutomated() {
        return isAutomated;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_AUTOMATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsAutomated(@org.eclipse.jdt.annotation.NonNull Boolean isAutomated) {
        this.isAutomated = isAutomated;
    }

    /**
     * Return true if this BoxSetInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BoxSetInfo boxSetInfo = (BoxSetInfo) o;
        return Objects.equals(this.name, boxSetInfo.name)
                && Objects.equals(this.originalTitle, boxSetInfo.originalTitle)
                && Objects.equals(this.path, boxSetInfo.path)
                && Objects.equals(this.metadataLanguage, boxSetInfo.metadataLanguage)
                && Objects.equals(this.metadataCountryCode, boxSetInfo.metadataCountryCode)
                && Objects.equals(this.providerIds, boxSetInfo.providerIds)
                && Objects.equals(this.year, boxSetInfo.year)
                && Objects.equals(this.indexNumber, boxSetInfo.indexNumber)
                && Objects.equals(this.parentIndexNumber, boxSetInfo.parentIndexNumber)
                && Objects.equals(this.premiereDate, boxSetInfo.premiereDate)
                && Objects.equals(this.isAutomated, boxSetInfo.isAutomated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, originalTitle, path, metadataLanguage, metadataCountryCode, providerIds, year,
                indexNumber, parentIndexNumber, premiereDate, isAutomated);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BoxSetInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    originalTitle: ").append(toIndentedString(originalTitle)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    metadataLanguage: ").append(toIndentedString(metadataLanguage)).append("\n");
        sb.append("    metadataCountryCode: ").append(toIndentedString(metadataCountryCode)).append("\n");
        sb.append("    providerIds: ").append(toIndentedString(providerIds)).append("\n");
        sb.append("    year: ").append(toIndentedString(year)).append("\n");
        sb.append("    indexNumber: ").append(toIndentedString(indexNumber)).append("\n");
        sb.append("    parentIndexNumber: ").append(toIndentedString(parentIndexNumber)).append("\n");
        sb.append("    premiereDate: ").append(toIndentedString(premiereDate)).append("\n");
        sb.append("    isAutomated: ").append(toIndentedString(isAutomated)).append("\n");
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `OriginalTitle` to the URL query string
        if (getOriginalTitle() != null) {
            joiner.add(String.format(Locale.ROOT, "%sOriginalTitle%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getOriginalTitle()))));
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `MetadataLanguage` to the URL query string
        if (getMetadataLanguage() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMetadataLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMetadataLanguage()))));
        }

        // add `MetadataCountryCode` to the URL query string
        if (getMetadataCountryCode() != null) {
            joiner.add(String.format(Locale.ROOT, "%sMetadataCountryCode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMetadataCountryCode()))));
        }

        // add `ProviderIds` to the URL query string
        if (getProviderIds() != null) {
            for (String _key : getProviderIds().keySet()) {
                joiner.add(String.format(Locale.ROOT, "%sProviderIds%s%s=%s", prefix, suffix,
                        "".equals(suffix) ? ""
                                : String.format(Locale.ROOT, "%s%d%s", containerPrefix, _key, containerSuffix),
                        getProviderIds().get(_key),
                        ApiClient.urlEncode(ApiClient.valueToString(getProviderIds().get(_key)))));
            }
        }

        // add `Year` to the URL query string
        if (getYear() != null) {
            joiner.add(String.format(Locale.ROOT, "%sYear%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getYear()))));
        }

        // add `IndexNumber` to the URL query string
        if (getIndexNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIndexNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIndexNumber()))));
        }

        // add `ParentIndexNumber` to the URL query string
        if (getParentIndexNumber() != null) {
            joiner.add(String.format(Locale.ROOT, "%sParentIndexNumber%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getParentIndexNumber()))));
        }

        // add `PremiereDate` to the URL query string
        if (getPremiereDate() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPremiereDate%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPremiereDate()))));
        }

        // add `IsAutomated` to the URL query string
        if (getIsAutomated() != null) {
            joiner.add(String.format(Locale.ROOT, "%sIsAutomated%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsAutomated()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private BoxSetInfo instance;

        public Builder() {
            this(new BoxSetInfo());
        }

        protected Builder(BoxSetInfo instance) {
            this.instance = instance;
        }

        public BoxSetInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public BoxSetInfo.Builder originalTitle(String originalTitle) {
            this.instance.originalTitle = originalTitle;
            return this;
        }

        public BoxSetInfo.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public BoxSetInfo.Builder metadataLanguage(String metadataLanguage) {
            this.instance.metadataLanguage = metadataLanguage;
            return this;
        }

        public BoxSetInfo.Builder metadataCountryCode(String metadataCountryCode) {
            this.instance.metadataCountryCode = metadataCountryCode;
            return this;
        }

        public BoxSetInfo.Builder providerIds(Map<String, String> providerIds) {
            this.instance.providerIds = providerIds;
            return this;
        }

        public BoxSetInfo.Builder year(Integer year) {
            this.instance.year = year;
            return this;
        }

        public BoxSetInfo.Builder indexNumber(Integer indexNumber) {
            this.instance.indexNumber = indexNumber;
            return this;
        }

        public BoxSetInfo.Builder parentIndexNumber(Integer parentIndexNumber) {
            this.instance.parentIndexNumber = parentIndexNumber;
            return this;
        }

        public BoxSetInfo.Builder premiereDate(OffsetDateTime premiereDate) {
            this.instance.premiereDate = premiereDate;
            return this;
        }

        public BoxSetInfo.Builder isAutomated(Boolean isAutomated) {
            this.instance.isAutomated = isAutomated;
            return this;
        }

        /**
         * returns a built BoxSetInfo instance.
         *
         * The builder is not reusable.
         */
        public BoxSetInfo build() {
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
    public static BoxSetInfo.Builder builder() {
        return new BoxSetInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BoxSetInfo.Builder toBuilder() {
        return new BoxSetInfo.Builder().name(getName()).originalTitle(getOriginalTitle()).path(getPath())
                .metadataLanguage(getMetadataLanguage()).metadataCountryCode(getMetadataCountryCode())
                .providerIds(getProviderIds()).year(getYear()).indexNumber(getIndexNumber())
                .parentIndexNumber(getParentIndexNumber()).premiereDate(getPremiereDate())
                .isAutomated(getIsAutomated());
    }
}
