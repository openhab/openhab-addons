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
 * LocalizationOption
 */
@JsonPropertyOrder({ LocalizationOption.JSON_PROPERTY_NAME, LocalizationOption.JSON_PROPERTY_VALUE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class LocalizationOption {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_VALUE = "Value";
    @org.eclipse.jdt.annotation.Nullable
    private String value;

    public LocalizationOption() {
    }

    public LocalizationOption name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
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

    public LocalizationOption value(@org.eclipse.jdt.annotation.Nullable String value) {
        this.value = value;
        return this;
    }

    /**
     * Get value
     * 
     * @return value
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VALUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getValue() {
        return value;
    }

    @JsonProperty(value = JSON_PROPERTY_VALUE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValue(@org.eclipse.jdt.annotation.Nullable String value) {
        this.value = value;
    }

    /**
     * Return true if this LocalizationOption object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LocalizationOption localizationOption = (LocalizationOption) o;
        return Objects.equals(this.name, localizationOption.name)
                && Objects.equals(this.value, localizationOption.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LocalizationOption {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
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

        // add `Value` to the URL query string
        if (getValue() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sValue%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getValue()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private LocalizationOption instance;

        public Builder() {
            this(new LocalizationOption());
        }

        protected Builder(LocalizationOption instance) {
            this.instance = instance;
        }

        public LocalizationOption.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public LocalizationOption.Builder value(String value) {
            this.instance.value = value;
            return this;
        }

        /**
         * returns a built LocalizationOption instance.
         *
         * The builder is not reusable.
         */
        public LocalizationOption build() {
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
    public static LocalizationOption.Builder builder() {
        return new LocalizationOption.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public LocalizationOption.Builder toBuilder() {
        return new LocalizationOption.Builder().name(getName()).value(getValue());
    }
}
