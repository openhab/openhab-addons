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

import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The startup configuration DTO.
 */
@JsonPropertyOrder({ StartupConfigurationDto.JSON_PROPERTY_UI_CULTURE,
        StartupConfigurationDto.JSON_PROPERTY_METADATA_COUNTRY_CODE,
        StartupConfigurationDto.JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class StartupConfigurationDto {
    public static final String JSON_PROPERTY_UI_CULTURE = "UICulture";
    @org.eclipse.jdt.annotation.NonNull
    private String uiCulture;

    public static final String JSON_PROPERTY_METADATA_COUNTRY_CODE = "MetadataCountryCode";
    @org.eclipse.jdt.annotation.NonNull
    private String metadataCountryCode;

    public static final String JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE = "PreferredMetadataLanguage";
    @org.eclipse.jdt.annotation.NonNull
    private String preferredMetadataLanguage;

    public StartupConfigurationDto() {
    }

    public StartupConfigurationDto uiCulture(@org.eclipse.jdt.annotation.NonNull String uiCulture) {
        this.uiCulture = uiCulture;
        return this;
    }

    /**
     * Gets or sets UI language culture.
     * 
     * @return uiCulture
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_UI_CULTURE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUiCulture() {
        return uiCulture;
    }

    @JsonProperty(JSON_PROPERTY_UI_CULTURE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUiCulture(@org.eclipse.jdt.annotation.NonNull String uiCulture) {
        this.uiCulture = uiCulture;
    }

    public StartupConfigurationDto metadataCountryCode(@org.eclipse.jdt.annotation.NonNull String metadataCountryCode) {
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

    public StartupConfigurationDto preferredMetadataLanguage(
            @org.eclipse.jdt.annotation.NonNull String preferredMetadataLanguage) {
        this.preferredMetadataLanguage = preferredMetadataLanguage;
        return this;
    }

    /**
     * Gets or sets the preferred language for the metadata.
     * 
     * @return preferredMetadataLanguage
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPreferredMetadataLanguage() {
        return preferredMetadataLanguage;
    }

    @JsonProperty(JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferredMetadataLanguage(@org.eclipse.jdt.annotation.NonNull String preferredMetadataLanguage) {
        this.preferredMetadataLanguage = preferredMetadataLanguage;
    }

    /**
     * Return true if this StartupConfigurationDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StartupConfigurationDto startupConfigurationDto = (StartupConfigurationDto) o;
        return Objects.equals(this.uiCulture, startupConfigurationDto.uiCulture)
                && Objects.equals(this.metadataCountryCode, startupConfigurationDto.metadataCountryCode)
                && Objects.equals(this.preferredMetadataLanguage, startupConfigurationDto.preferredMetadataLanguage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uiCulture, metadataCountryCode, preferredMetadataLanguage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class StartupConfigurationDto {\n");
        sb.append("    uiCulture: ").append(toIndentedString(uiCulture)).append("\n");
        sb.append("    metadataCountryCode: ").append(toIndentedString(metadataCountryCode)).append("\n");
        sb.append("    preferredMetadataLanguage: ").append(toIndentedString(preferredMetadataLanguage)).append("\n");
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
