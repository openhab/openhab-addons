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

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * The startup configuration DTO.
 */
@JsonPropertyOrder({ StartupConfigurationDto.JSON_PROPERTY_SERVER_NAME,
        StartupConfigurationDto.JSON_PROPERTY_UI_CULTURE, StartupConfigurationDto.JSON_PROPERTY_METADATA_COUNTRY_CODE,
        StartupConfigurationDto.JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class StartupConfigurationDto {
    public static final String JSON_PROPERTY_SERVER_NAME = "ServerName";
    @org.eclipse.jdt.annotation.Nullable
    private String serverName;

    public static final String JSON_PROPERTY_UI_CULTURE = "UICulture";
    @org.eclipse.jdt.annotation.Nullable
    private String uiCulture;

    public static final String JSON_PROPERTY_METADATA_COUNTRY_CODE = "MetadataCountryCode";
    @org.eclipse.jdt.annotation.Nullable
    private String metadataCountryCode;

    public static final String JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE = "PreferredMetadataLanguage";
    @org.eclipse.jdt.annotation.Nullable
    private String preferredMetadataLanguage;

    public StartupConfigurationDto() {
    }

    public StartupConfigurationDto serverName(@org.eclipse.jdt.annotation.Nullable String serverName) {
        this.serverName = serverName;
        return this;
    }

    /**
     * Gets or sets the server name.
     * 
     * @return serverName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_SERVER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServerName() {
        return serverName;
    }

    @JsonProperty(value = JSON_PROPERTY_SERVER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServerName(@org.eclipse.jdt.annotation.Nullable String serverName) {
        this.serverName = serverName;
    }

    public StartupConfigurationDto uiCulture(@org.eclipse.jdt.annotation.Nullable String uiCulture) {
        this.uiCulture = uiCulture;
        return this;
    }

    /**
     * Gets or sets UI language culture.
     * 
     * @return uiCulture
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_UI_CULTURE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUiCulture() {
        return uiCulture;
    }

    @JsonProperty(value = JSON_PROPERTY_UI_CULTURE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUiCulture(@org.eclipse.jdt.annotation.Nullable String uiCulture) {
        this.uiCulture = uiCulture;
    }

    public StartupConfigurationDto metadataCountryCode(
            @org.eclipse.jdt.annotation.Nullable String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
        return this;
    }

    /**
     * Gets or sets the metadata country code.
     * 
     * @return metadataCountryCode
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_METADATA_COUNTRY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getMetadataCountryCode() {
        return metadataCountryCode;
    }

    @JsonProperty(value = JSON_PROPERTY_METADATA_COUNTRY_CODE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMetadataCountryCode(@org.eclipse.jdt.annotation.Nullable String metadataCountryCode) {
        this.metadataCountryCode = metadataCountryCode;
    }

    public StartupConfigurationDto preferredMetadataLanguage(
            @org.eclipse.jdt.annotation.Nullable String preferredMetadataLanguage) {
        this.preferredMetadataLanguage = preferredMetadataLanguage;
        return this;
    }

    /**
     * Gets or sets the preferred language for the metadata.
     * 
     * @return preferredMetadataLanguage
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPreferredMetadataLanguage() {
        return preferredMetadataLanguage;
    }

    @JsonProperty(value = JSON_PROPERTY_PREFERRED_METADATA_LANGUAGE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPreferredMetadataLanguage(@org.eclipse.jdt.annotation.Nullable String preferredMetadataLanguage) {
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
        return Objects.equals(this.serverName, startupConfigurationDto.serverName)
                && Objects.equals(this.uiCulture, startupConfigurationDto.uiCulture)
                && Objects.equals(this.metadataCountryCode, startupConfigurationDto.metadataCountryCode)
                && Objects.equals(this.preferredMetadataLanguage, startupConfigurationDto.preferredMetadataLanguage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverName, uiCulture, metadataCountryCode, preferredMetadataLanguage);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class StartupConfigurationDto {\n");
        sb.append("    serverName: ").append(toIndentedString(serverName)).append("\n");
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

        // add `ServerName` to the URL query string
        if (getServerName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sServerName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerName()))));
        }

        // add `UICulture` to the URL query string
        if (getUiCulture() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sUICulture%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUiCulture()))));
        }

        // add `MetadataCountryCode` to the URL query string
        if (getMetadataCountryCode() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sMetadataCountryCode%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMetadataCountryCode()))));
        }

        // add `PreferredMetadataLanguage` to the URL query string
        if (getPreferredMetadataLanguage() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPreferredMetadataLanguage%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPreferredMetadataLanguage()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private StartupConfigurationDto instance;

        public Builder() {
            this(new StartupConfigurationDto());
        }

        protected Builder(StartupConfigurationDto instance) {
            this.instance = instance;
        }

        public StartupConfigurationDto.Builder serverName(String serverName) {
            this.instance.serverName = serverName;
            return this;
        }

        public StartupConfigurationDto.Builder uiCulture(String uiCulture) {
            this.instance.uiCulture = uiCulture;
            return this;
        }

        public StartupConfigurationDto.Builder metadataCountryCode(String metadataCountryCode) {
            this.instance.metadataCountryCode = metadataCountryCode;
            return this;
        }

        public StartupConfigurationDto.Builder preferredMetadataLanguage(String preferredMetadataLanguage) {
            this.instance.preferredMetadataLanguage = preferredMetadataLanguage;
            return this;
        }

        /**
         * returns a built StartupConfigurationDto instance.
         *
         * The builder is not reusable.
         */
        public StartupConfigurationDto build() {
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
    public static StartupConfigurationDto.Builder builder() {
        return new StartupConfigurationDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public StartupConfigurationDto.Builder toBuilder() {
        return new StartupConfigurationDto.Builder().serverName(getServerName()).uiCulture(getUiCulture())
                .metadataCountryCode(getMetadataCountryCode())
                .preferredMetadataLanguage(getPreferredMetadataLanguage());
    }
}
