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
 * ExternalUrl
 */
@JsonPropertyOrder({ ExternalUrl.JSON_PROPERTY_NAME, ExternalUrl.JSON_PROPERTY_URL })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ExternalUrl {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_URL = "Url";
    @org.eclipse.jdt.annotation.Nullable
    private String url;

    public ExternalUrl() {
    }

    public ExternalUrl name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name.
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

    public ExternalUrl url(@org.eclipse.jdt.annotation.Nullable String url) {
        this.url = url;
        return this;
    }

    /**
     * Gets or sets the type of the item.
     * 
     * @return url
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUrl() {
        return url;
    }

    @JsonProperty(value = JSON_PROPERTY_URL, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUrl(@org.eclipse.jdt.annotation.Nullable String url) {
        this.url = url;
    }

    /**
     * Return true if this ExternalUrl object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalUrl externalUrl = (ExternalUrl) o;
        return Objects.equals(this.name, externalUrl.name) && Objects.equals(this.url, externalUrl.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, url);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExternalUrl {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    url: ").append(toIndentedString(url)).append("\n");
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

        // add `Url` to the URL query string
        if (getUrl() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sUrl%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUrl()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ExternalUrl instance;

        public Builder() {
            this(new ExternalUrl());
        }

        protected Builder(ExternalUrl instance) {
            this.instance = instance;
        }

        public ExternalUrl.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public ExternalUrl.Builder url(String url) {
            this.instance.url = url;
            return this;
        }

        /**
         * returns a built ExternalUrl instance.
         *
         * The builder is not reusable.
         */
        public ExternalUrl build() {
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
    public static ExternalUrl.Builder builder() {
        return new ExternalUrl.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ExternalUrl.Builder toBuilder() {
        return new ExternalUrl.Builder().name(getName()).url(getUrl());
    }
}
