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
 * TunerChannelMapping
 */
@JsonPropertyOrder({ TunerChannelMapping.JSON_PROPERTY_NAME, TunerChannelMapping.JSON_PROPERTY_PROVIDER_CHANNEL_NAME,
        TunerChannelMapping.JSON_PROPERTY_PROVIDER_CHANNEL_ID, TunerChannelMapping.JSON_PROPERTY_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class TunerChannelMapping {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_PROVIDER_CHANNEL_NAME = "ProviderChannelName";
    @org.eclipse.jdt.annotation.Nullable
    private String providerChannelName;

    public static final String JSON_PROPERTY_PROVIDER_CHANNEL_ID = "ProviderChannelId";
    @org.eclipse.jdt.annotation.Nullable
    private String providerChannelId;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private String id;

    public TunerChannelMapping() {
    }

    public TunerChannelMapping name(@org.eclipse.jdt.annotation.Nullable String name) {
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

    public TunerChannelMapping providerChannelName(@org.eclipse.jdt.annotation.Nullable String providerChannelName) {
        this.providerChannelName = providerChannelName;
        return this;
    }

    /**
     * Get providerChannelName
     * 
     * @return providerChannelName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_CHANNEL_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProviderChannelName() {
        return providerChannelName;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_CHANNEL_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderChannelName(@org.eclipse.jdt.annotation.Nullable String providerChannelName) {
        this.providerChannelName = providerChannelName;
    }

    public TunerChannelMapping providerChannelId(@org.eclipse.jdt.annotation.Nullable String providerChannelId) {
        this.providerChannelId = providerChannelId;
        return this;
    }

    /**
     * Get providerChannelId
     * 
     * @return providerChannelId
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_CHANNEL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProviderChannelId() {
        return providerChannelId;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_CHANNEL_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderChannelId(@org.eclipse.jdt.annotation.Nullable String providerChannelId) {
        this.providerChannelId = providerChannelId;
    }

    public TunerChannelMapping id(@org.eclipse.jdt.annotation.Nullable String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.Nullable String id) {
        this.id = id;
    }

    /**
     * Return true if this TunerChannelMapping object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        TunerChannelMapping tunerChannelMapping = (TunerChannelMapping) o;
        return Objects.equals(this.name, tunerChannelMapping.name)
                && Objects.equals(this.providerChannelName, tunerChannelMapping.providerChannelName)
                && Objects.equals(this.providerChannelId, tunerChannelMapping.providerChannelId)
                && Objects.equals(this.id, tunerChannelMapping.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, providerChannelName, providerChannelId, id);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class TunerChannelMapping {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    providerChannelName: ").append(toIndentedString(providerChannelName)).append("\n");
        sb.append("    providerChannelId: ").append(toIndentedString(providerChannelId)).append("\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
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

        // add `ProviderChannelName` to the URL query string
        if (getProviderChannelName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProviderChannelName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProviderChannelName()))));
        }

        // add `ProviderChannelId` to the URL query string
        if (getProviderChannelId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProviderChannelId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProviderChannelId()))));
        }

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private TunerChannelMapping instance;

        public Builder() {
            this(new TunerChannelMapping());
        }

        protected Builder(TunerChannelMapping instance) {
            this.instance = instance;
        }

        public TunerChannelMapping.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public TunerChannelMapping.Builder providerChannelName(String providerChannelName) {
            this.instance.providerChannelName = providerChannelName;
            return this;
        }

        public TunerChannelMapping.Builder providerChannelId(String providerChannelId) {
            this.instance.providerChannelId = providerChannelId;
            return this;
        }

        public TunerChannelMapping.Builder id(String id) {
            this.instance.id = id;
            return this;
        }

        /**
         * returns a built TunerChannelMapping instance.
         *
         * The builder is not reusable.
         */
        public TunerChannelMapping build() {
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
    public static TunerChannelMapping.Builder builder() {
        return new TunerChannelMapping.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public TunerChannelMapping.Builder toBuilder() {
        return new TunerChannelMapping.Builder().name(getName()).providerChannelName(getProviderChannelName())
                .providerChannelId(getProviderChannelId()).id(getId());
    }
}
