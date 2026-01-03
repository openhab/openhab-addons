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
 * Set channel mapping dto.
 */
@JsonPropertyOrder({ SetChannelMappingDto.JSON_PROPERTY_PROVIDER_ID,
        SetChannelMappingDto.JSON_PROPERTY_TUNER_CHANNEL_ID, SetChannelMappingDto.JSON_PROPERTY_PROVIDER_CHANNEL_ID })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class SetChannelMappingDto {
    public static final String JSON_PROPERTY_PROVIDER_ID = "ProviderId";
    @org.eclipse.jdt.annotation.NonNull
    private String providerId;

    public static final String JSON_PROPERTY_TUNER_CHANNEL_ID = "TunerChannelId";
    @org.eclipse.jdt.annotation.NonNull
    private String tunerChannelId;

    public static final String JSON_PROPERTY_PROVIDER_CHANNEL_ID = "ProviderChannelId";
    @org.eclipse.jdt.annotation.NonNull
    private String providerChannelId;

    public SetChannelMappingDto() {
    }

    public SetChannelMappingDto providerId(@org.eclipse.jdt.annotation.NonNull String providerId) {
        this.providerId = providerId;
        return this;
    }

    /**
     * Gets or sets the provider id.
     * 
     * @return providerId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_ID, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getProviderId() {
        return providerId;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_ID, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setProviderId(@org.eclipse.jdt.annotation.NonNull String providerId) {
        this.providerId = providerId;
    }

    public SetChannelMappingDto tunerChannelId(@org.eclipse.jdt.annotation.NonNull String tunerChannelId) {
        this.tunerChannelId = tunerChannelId;
        return this;
    }

    /**
     * Gets or sets the tuner channel id.
     * 
     * @return tunerChannelId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TUNER_CHANNEL_ID, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getTunerChannelId() {
        return tunerChannelId;
    }

    @JsonProperty(value = JSON_PROPERTY_TUNER_CHANNEL_ID, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setTunerChannelId(@org.eclipse.jdt.annotation.NonNull String tunerChannelId) {
        this.tunerChannelId = tunerChannelId;
    }

    public SetChannelMappingDto providerChannelId(@org.eclipse.jdt.annotation.NonNull String providerChannelId) {
        this.providerChannelId = providerChannelId;
        return this;
    }

    /**
     * Gets or sets the provider channel id.
     * 
     * @return providerChannelId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_CHANNEL_ID, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getProviderChannelId() {
        return providerChannelId;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_CHANNEL_ID, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setProviderChannelId(@org.eclipse.jdt.annotation.NonNull String providerChannelId) {
        this.providerChannelId = providerChannelId;
    }

    /**
     * Return true if this SetChannelMappingDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SetChannelMappingDto setChannelMappingDto = (SetChannelMappingDto) o;
        return Objects.equals(this.providerId, setChannelMappingDto.providerId)
                && Objects.equals(this.tunerChannelId, setChannelMappingDto.tunerChannelId)
                && Objects.equals(this.providerChannelId, setChannelMappingDto.providerChannelId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(providerId, tunerChannelId, providerChannelId);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class SetChannelMappingDto {\n");
        sb.append("    providerId: ").append(toIndentedString(providerId)).append("\n");
        sb.append("    tunerChannelId: ").append(toIndentedString(tunerChannelId)).append("\n");
        sb.append("    providerChannelId: ").append(toIndentedString(providerChannelId)).append("\n");
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

        // add `ProviderId` to the URL query string
        if (getProviderId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProviderId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProviderId()))));
        }

        // add `TunerChannelId` to the URL query string
        if (getTunerChannelId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sTunerChannelId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getTunerChannelId()))));
        }

        // add `ProviderChannelId` to the URL query string
        if (getProviderChannelId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProviderChannelId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProviderChannelId()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private SetChannelMappingDto instance;

        public Builder() {
            this(new SetChannelMappingDto());
        }

        protected Builder(SetChannelMappingDto instance) {
            this.instance = instance;
        }

        public SetChannelMappingDto.Builder providerId(String providerId) {
            this.instance.providerId = providerId;
            return this;
        }

        public SetChannelMappingDto.Builder tunerChannelId(String tunerChannelId) {
            this.instance.tunerChannelId = tunerChannelId;
            return this;
        }

        public SetChannelMappingDto.Builder providerChannelId(String providerChannelId) {
            this.instance.providerChannelId = providerChannelId;
            return this;
        }

        /**
         * returns a built SetChannelMappingDto instance.
         *
         * The builder is not reusable.
         */
        public SetChannelMappingDto build() {
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
    public static SetChannelMappingDto.Builder builder() {
        return new SetChannelMappingDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public SetChannelMappingDto.Builder toBuilder() {
        return new SetChannelMappingDto.Builder().providerId(getProviderId()).tunerChannelId(getTunerChannelId())
                .providerChannelId(getProviderChannelId());
    }
}
