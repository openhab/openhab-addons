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
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * TrailerInfo
 */
@JsonPropertyOrder({ TrailerInfo.JSON_PROPERTY_NAME, TrailerInfo.JSON_PROPERTY_ORIGINAL_TITLE,
        TrailerInfo.JSON_PROPERTY_PATH, TrailerInfo.JSON_PROPERTY_METADATA_LANGUAGE,
        TrailerInfo.JSON_PROPERTY_METADATA_COUNTRY_CODE, TrailerInfo.JSON_PROPERTY_PROVIDER_IDS,
        TrailerInfo.JSON_PROPERTY_YEAR, TrailerInfo.JSON_PROPERTY_INDEX_NUMBER,
        TrailerInfo.JSON_PROPERTY_PARENT_INDEX_NUMBER, TrailerInfo.JSON_PROPERTY_PREMIERE_DATE,
        TrailerInfo.JSON_PROPERTY_IS_AUTOMATED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TrailerInfo {
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

    public TrailerInfo() {
    }

    public TrailerInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public TrailerInfo originalTitle(@org.eclipse.jdt.annotation.NonNull String originalTitle) {
        this.originalTitle = originalTitle;
        return this;
    }

    /**
     * Gets or sets the original title.
     * 
     * @return originalTitle
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ORIGINAL_TITLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getOriginalTitle() {
        return originalTitle;
    }

    @JsonProperty(JSON_PROPERTY_ORIGINAL_TITLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOriginalTitle(@org.eclipse.jdt.annotation.NonNull String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public TrailerInfo path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPath() {
        return path;
    }

    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public TrailerInfo metadataLanguage(@org.eclipse.jdt.annotation.NonNull String metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
        return this;
    }

    /**
     * Gets or sets the metadata language.
     * 
     * @return metadataLanguage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMetadataLanguage() {
        return metadataLanguage;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataLanguage(@org.eclipse.jdt.annotation.NonNull String metadataLanguage) {
        this.metadataLanguage = metadataLanguage;
    }

    public TrailerInfo metadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
        return this;
    }

    /**
     * Gets or sets the metadata country code.
     * 
     * @return metadataCountryCode
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_METADATA_COUNTRY_CODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getMetadataCountryCode() {
        return metadataCountryCode;
    }

    @JsonProperty(JSON_PROPERTY_METADATA_COUNTRY_CODE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
    }

    public TrailerInfo providerIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> providerIds) {
        this.providerIds = providerIds;
        return this;
    }

    public TrailerInfo putProviderIdsItem(String key, String providerIdsItem) {
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
    @JsonProperty(JSON_PROPERTY_PROVIDER_IDS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)

    public Map<String, String> getProviderIds() {
        return providerIds;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDER_IDS)
    @JsonInclude(content = JsonInclude.Include.ALWAYS, value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderIds(@org.eclipse.jdt.annotation.NonNull Map<String, String> providerIds) {
        this.providerIds = providerIds;
    }

    public TrailerInfo year(@org.eclipse.jdt.annotation.NonNull Integer year) {
        this.year = year;
        return this;
    }

    /**
     * Gets or sets the year.
     * 
     * @return year
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_YEAR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getYear() {
        return year;
    }

    @JsonProperty(JSON_PROPERTY_YEAR)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setYear(@org.eclipse.jdt.annotation.NonNull Integer year) {
        this.year = year;
    }

    public TrailerInfo indexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
        return this;
    }

    /**
     * Get indexNumber
     * 
     * @return indexNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_INDEX_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getIndexNumber() {
        return indexNumber;
    }

    @JsonProperty(JSON_PROPERTY_INDEX_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer indexNumber) {
        this.indexNumber = indexNumber;
    }

    public TrailerInfo parentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
        return this;
    }

    /**
     * Get parentIndexNumber
     * 
     * @return parentIndexNumber
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PARENT_INDEX_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Integer getParentIndexNumber() {
        return parentIndexNumber;
    }

    @JsonProperty(JSON_PROPERTY_PARENT_INDEX_NUMBER)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setParentIndexNumber(@org.eclipse.jdt.annotation.NonNull Integer parentIndexNumber) {
        this.parentIndexNumber = parentIndexNumber;
    }

    public TrailerInfo premiereDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
        return this;
    }

    /**
     * Get premiereDate
     * 
     * @return premiereDate
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PREMIERE_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public OffsetDateTime getPremiereDate() {
        return premiereDate;
    }

    @JsonProperty(JSON_PROPERTY_PREMIERE_DATE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPremiereDate(@org.eclipse.jdt.annotation.NonNull OffsetDateTime premiereDate) {
        this.premiereDate = premiereDate;
    }

    public TrailerInfo isAutomated(@org.eclipse.jdt.annotation.NonNull Boolean isAutomated) {
        this.isAutomated = isAutomated;
        return this;
    }

    /**
     * Get isAutomated
     * 
     * @return isAutomated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_AUTOMATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsAutomated() {
        return isAutomated;
    }

    @JsonProperty(JSON_PROPERTY_IS_AUTOMATED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsAutomated(@org.eclipse.jdt.annotation.NonNull Boolean isAutomated) {
        this.isAutomated = isAutomated;
    }

    /**
     * Return true if this TrailerInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TrailerInfo trailerInfo = (TrailerInfo) o;
        return Objects.equals(this.name, trailerInfo.name)
                && Objects.equals(this.originalTitle, trailerInfo.originalTitle)
                && Objects.equals(this.path, trailerInfo.path)
                && Objects.equals(this.metadataLanguage, trailerInfo.metadataLanguage)
                && Objects.equals(this.metadataCountryCode, trailerInfo.metadataCountryCode)
                && Objects.equals(this.providerIds, trailerInfo.providerIds)
                && Objects.equals(this.year, trailerInfo.year)
                && Objects.equals(this.indexNumber, trailerInfo.indexNumber)
                && Objects.equals(this.parentIndexNumber, trailerInfo.parentIndexNumber)
                && Objects.equals(this.premiereDate, trailerInfo.premiereDate)
                && Objects.equals(this.isAutomated, trailerInfo.isAutomated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, originalTitle, path, metadataLanguage, metadataCountryCode, providerIds, year,
                indexNumber, parentIndexNumber, premiereDate, isAutomated);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TrailerInfo {\n");
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
}
