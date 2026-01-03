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
 * Represents the external id information for serialization to the client.
 */
@JsonPropertyOrder({ ExternalIdInfo.JSON_PROPERTY_NAME, ExternalIdInfo.JSON_PROPERTY_KEY,
        ExternalIdInfo.JSON_PROPERTY_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ExternalIdInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_KEY = "Key";
    @org.eclipse.jdt.annotation.Nullable
    private String key;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.Nullable
    private ExternalIdMediaType type;

    public ExternalIdInfo() {
    }

    public ExternalIdInfo name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the display name of the external id provider (IE: IMDB, MusicBrainz, etc).
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

    public ExternalIdInfo key(@org.eclipse.jdt.annotation.Nullable String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets or sets the unique key for this id. This key should be unique across all providers.
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

    public ExternalIdInfo type(@org.eclipse.jdt.annotation.Nullable ExternalIdMediaType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets or sets the specific media type for this id. This is used to distinguish between the different external id
     * types for providers with multiple ids. A null value indicates there is no specific media type associated with the
     * external id, or this is the default id for the external provider so there is no need to specify a type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public ExternalIdMediaType getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.Nullable ExternalIdMediaType type) {
        this.type = type;
    }

    /**
     * Return true if this ExternalIdInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ExternalIdInfo externalIdInfo = (ExternalIdInfo) o;
        return Objects.equals(this.name, externalIdInfo.name) && Objects.equals(this.key, externalIdInfo.key)
                && Objects.equals(this.type, externalIdInfo.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, key, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExternalIdInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

        // add `Key` to the URL query string
        if (getKey() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sKey%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getKey()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ExternalIdInfo instance;

        public Builder() {
            this(new ExternalIdInfo());
        }

        protected Builder(ExternalIdInfo instance) {
            this.instance = instance;
        }

        public ExternalIdInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public ExternalIdInfo.Builder key(String key) {
            this.instance.key = key;
            return this;
        }

        public ExternalIdInfo.Builder type(ExternalIdMediaType type) {
            this.instance.type = type;
            return this;
        }

        /**
         * returns a built ExternalIdInfo instance.
         *
         * The builder is not reusable.
         */
        public ExternalIdInfo build() {
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
    public static ExternalIdInfo.Builder builder() {
        return new ExternalIdInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ExternalIdInfo.Builder toBuilder() {
        return new ExternalIdInfo.Builder().name(getName()).key(getKey()).type(getType());
    }
}
