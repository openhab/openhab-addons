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
 * Library option info dto.
 */
@JsonPropertyOrder({ LibraryOptionInfoDto.JSON_PROPERTY_NAME, LibraryOptionInfoDto.JSON_PROPERTY_DEFAULT_ENABLED })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LibraryOptionInfoDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_DEFAULT_ENABLED = "DefaultEnabled";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean defaultEnabled;

    public LibraryOptionInfoDto() {
    }

    public LibraryOptionInfoDto name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
    }

    public LibraryOptionInfoDto defaultEnabled(@org.eclipse.jdt.annotation.Nullable Boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
        return this;
    }

    /**
     * Gets or sets a value indicating whether default enabled.
     * 
     * @return defaultEnabled
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_DEFAULT_ENABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getDefaultEnabled() {
        return defaultEnabled;
    }

    @JsonProperty(value = JSON_PROPERTY_DEFAULT_ENABLED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultEnabled(@org.eclipse.jdt.annotation.Nullable Boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
    }

    /**
     * Return true if this LibraryOptionInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LibraryOptionInfoDto libraryOptionInfoDto = (LibraryOptionInfoDto) o;
        return Objects.equals(this.name, libraryOptionInfoDto.name)
                && Objects.equals(this.defaultEnabled, libraryOptionInfoDto.defaultEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, defaultEnabled);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LibraryOptionInfoDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    defaultEnabled: ").append(toIndentedString(defaultEnabled)).append("\n");
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
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `DefaultEnabled` to the URL query string
        if (getDefaultEnabled() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sDefaultEnabled%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDefaultEnabled()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private LibraryOptionInfoDto instance;

        public Builder() {
            this(new LibraryOptionInfoDto());
        }

        protected Builder(LibraryOptionInfoDto instance) {
            this.instance = instance;
        }

        public LibraryOptionInfoDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public LibraryOptionInfoDto.Builder defaultEnabled(Boolean defaultEnabled) {
            this.instance.defaultEnabled = defaultEnabled;
            return this;
        }

        /**
         * returns a built LibraryOptionInfoDto instance.
         *
         * The builder is not reusable.
         */
        public LibraryOptionInfoDto build() {
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
    public static LibraryOptionInfoDto.Builder builder() {
        return new LibraryOptionInfoDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LibraryOptionInfoDto.Builder toBuilder() {
        return new LibraryOptionInfoDto.Builder().name(getName()).defaultEnabled(getDefaultEnabled());
    }
}
