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
 * The custom value option for custom database providers.
 */
@JsonPropertyOrder({ CustomDatabaseOption.JSON_PROPERTY_KEY, CustomDatabaseOption.JSON_PROPERTY_VALUE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class CustomDatabaseOption {
    public static final String JSON_PROPERTY_KEY = "Key";
    @org.eclipse.jdt.annotation.Nullable
    private String key;

    public static final String JSON_PROPERTY_VALUE = "Value";
    @org.eclipse.jdt.annotation.Nullable
    private String value;

    public CustomDatabaseOption() {
    }

    public CustomDatabaseOption key(@org.eclipse.jdt.annotation.Nullable String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets or sets the key of the value.
     * 
     * @return key
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_KEY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getKey() {
        return key;
    }

    @JsonProperty(value = JSON_PROPERTY_KEY, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKey(@org.eclipse.jdt.annotation.Nullable String key) {
        this.key = key;
    }

    public CustomDatabaseOption value(@org.eclipse.jdt.annotation.Nullable String value) {
        this.value = value;
        return this;
    }

    /**
     * Gets or sets the value.
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
     * Return true if this CustomDatabaseOption object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CustomDatabaseOption customDatabaseOption = (CustomDatabaseOption) o;
        return Objects.equals(this.key, customDatabaseOption.key)
                && Objects.equals(this.value, customDatabaseOption.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class CustomDatabaseOption {\n");
        sb.append("    key: ").append(toIndentedString(key)).append("\n");
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

        // add `Key` to the URL query string
        if (getKey() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sKey%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getKey()))));
        }

        // add `Value` to the URL query string
        if (getValue() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sValue%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getValue()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private CustomDatabaseOption instance;

        public Builder() {
            this(new CustomDatabaseOption());
        }

        protected Builder(CustomDatabaseOption instance) {
            this.instance = instance;
        }

        public CustomDatabaseOption.Builder key(String key) {
            this.instance.key = key;
            return this;
        }

        public CustomDatabaseOption.Builder value(String value) {
            this.instance.value = value;
            return this;
        }

        /**
         * returns a built CustomDatabaseOption instance.
         *
         * The builder is not reusable.
         */
        public CustomDatabaseOption build() {
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
    public static CustomDatabaseOption.Builder builder() {
        return new CustomDatabaseOption.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public CustomDatabaseOption.Builder toBuilder() {
        return new CustomDatabaseOption.Builder().key(getKey()).value(getValue());
    }
}
