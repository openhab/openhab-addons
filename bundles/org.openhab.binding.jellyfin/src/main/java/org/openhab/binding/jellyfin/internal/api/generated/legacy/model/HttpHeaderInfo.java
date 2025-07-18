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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * HttpHeaderInfo
 */
@JsonPropertyOrder({ HttpHeaderInfo.JSON_PROPERTY_NAME, HttpHeaderInfo.JSON_PROPERTY_VALUE,
        HttpHeaderInfo.JSON_PROPERTY_MATCH })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class HttpHeaderInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_VALUE = "Value";
    @org.eclipse.jdt.annotation.NonNull
    private String value;

    public static final String JSON_PROPERTY_MATCH = "Match";
    @org.eclipse.jdt.annotation.NonNull
    private HeaderMatchType match;

    public HttpHeaderInfo() {
    }

    public HttpHeaderInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Get name
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

    public HttpHeaderInfo value(@org.eclipse.jdt.annotation.NonNull String value) {
        this.value = value;
        return this;
    }

    /**
     * Get value
     * 
     * @return value
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getValue() {
        return value;
    }

    @JsonProperty(JSON_PROPERTY_VALUE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValue(@org.eclipse.jdt.annotation.NonNull String value) {
        this.value = value;
    }

    public HttpHeaderInfo match(@org.eclipse.jdt.annotation.NonNull HeaderMatchType match) {
        this.match = match;
        return this;
    }

    /**
     * Get match
     * 
     * @return match
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MATCH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public HeaderMatchType getMatch() {
        return match;
    }

    @JsonProperty(JSON_PROPERTY_MATCH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMatch(@org.eclipse.jdt.annotation.NonNull HeaderMatchType match) {
        this.match = match;
    }

    /**
     * Return true if this HttpHeaderInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        HttpHeaderInfo httpHeaderInfo = (HttpHeaderInfo) o;
        return Objects.equals(this.name, httpHeaderInfo.name) && Objects.equals(this.value, httpHeaderInfo.value)
                && Objects.equals(this.match, httpHeaderInfo.match);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, value, match);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class HttpHeaderInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    value: ").append(toIndentedString(value)).append("\n");
        sb.append("    match: ").append(toIndentedString(match)).append("\n");
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
            joiner.add(String.format("%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Value` to the URL query string
        if (getValue() != null) {
            joiner.add(String.format("%sValue%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getValue()))));
        }

        // add `Match` to the URL query string
        if (getMatch() != null) {
            joiner.add(String.format("%sMatch%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getMatch()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private HttpHeaderInfo instance;

        public Builder() {
            this(new HttpHeaderInfo());
        }

        protected Builder(HttpHeaderInfo instance) {
            this.instance = instance;
        }

        public HttpHeaderInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public HttpHeaderInfo.Builder value(String value) {
            this.instance.value = value;
            return this;
        }

        public HttpHeaderInfo.Builder match(HeaderMatchType match) {
            this.instance.match = match;
            return this;
        }

        /**
         * returns a built HttpHeaderInfo instance.
         *
         * The builder is not reusable.
         */
        public HttpHeaderInfo build() {
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
    public static HttpHeaderInfo.Builder builder() {
        return new HttpHeaderInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public HttpHeaderInfo.Builder toBuilder() {
        return new HttpHeaderInfo.Builder().name(getName()).value(getValue()).match(getMatch());
    }
}
