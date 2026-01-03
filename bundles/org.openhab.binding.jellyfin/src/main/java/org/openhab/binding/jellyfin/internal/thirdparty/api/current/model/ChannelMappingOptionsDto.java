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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Channel mapping options dto.
 */
@JsonPropertyOrder({ ChannelMappingOptionsDto.JSON_PROPERTY_TUNER_CHANNELS,
        ChannelMappingOptionsDto.JSON_PROPERTY_PROVIDER_CHANNELS, ChannelMappingOptionsDto.JSON_PROPERTY_MAPPINGS,
        ChannelMappingOptionsDto.JSON_PROPERTY_PROVIDER_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ChannelMappingOptionsDto {
    public static final String JSON_PROPERTY_TUNER_CHANNELS = "TunerChannels";
    @org.eclipse.jdt.annotation.Nullable
    private List<TunerChannelMapping> tunerChannels = new ArrayList<>();

    public static final String JSON_PROPERTY_PROVIDER_CHANNELS = "ProviderChannels";
    @org.eclipse.jdt.annotation.Nullable
    private List<NameIdPair> providerChannels = new ArrayList<>();

    public static final String JSON_PROPERTY_MAPPINGS = "Mappings";
    @org.eclipse.jdt.annotation.Nullable
    private List<NameValuePair> mappings = new ArrayList<>();

    public static final String JSON_PROPERTY_PROVIDER_NAME = "ProviderName";
    @org.eclipse.jdt.annotation.Nullable
    private String providerName;

    public ChannelMappingOptionsDto() {
    }

    public ChannelMappingOptionsDto tunerChannels(
            @org.eclipse.jdt.annotation.Nullable List<TunerChannelMapping> tunerChannels) {
        this.tunerChannels = tunerChannels;
        return this;
    }

    public ChannelMappingOptionsDto addTunerChannelsItem(TunerChannelMapping tunerChannelsItem) {
        if (this.tunerChannels == null) {
            this.tunerChannels = new ArrayList<>();
        }
        this.tunerChannels.add(tunerChannelsItem);
        return this;
    }

    /**
     * Gets or sets list of tuner channels.
     * 
     * @return tunerChannels
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_TUNER_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<TunerChannelMapping> getTunerChannels() {
        return tunerChannels;
    }

    @JsonProperty(value = JSON_PROPERTY_TUNER_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTunerChannels(@org.eclipse.jdt.annotation.Nullable List<TunerChannelMapping> tunerChannels) {
        this.tunerChannels = tunerChannels;
    }

    public ChannelMappingOptionsDto providerChannels(
            @org.eclipse.jdt.annotation.Nullable List<NameIdPair> providerChannels) {
        this.providerChannels = providerChannels;
        return this;
    }

    public ChannelMappingOptionsDto addProviderChannelsItem(NameIdPair providerChannelsItem) {
        if (this.providerChannels == null) {
            this.providerChannels = new ArrayList<>();
        }
        this.providerChannels.add(providerChannelsItem);
        return this;
    }

    /**
     * Gets or sets list of provider channels.
     * 
     * @return providerChannels
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NameIdPair> getProviderChannels() {
        return providerChannels;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_CHANNELS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderChannels(@org.eclipse.jdt.annotation.Nullable List<NameIdPair> providerChannels) {
        this.providerChannels = providerChannels;
    }

    public ChannelMappingOptionsDto mappings(@org.eclipse.jdt.annotation.Nullable List<NameValuePair> mappings) {
        this.mappings = mappings;
        return this;
    }

    public ChannelMappingOptionsDto addMappingsItem(NameValuePair mappingsItem) {
        if (this.mappings == null) {
            this.mappings = new ArrayList<>();
        }
        this.mappings.add(mappingsItem);
        return this;
    }

    /**
     * Gets or sets list of mappings.
     * 
     * @return mappings
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_MAPPINGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public List<NameValuePair> getMappings() {
        return mappings;
    }

    @JsonProperty(value = JSON_PROPERTY_MAPPINGS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMappings(@org.eclipse.jdt.annotation.Nullable List<NameValuePair> mappings) {
        this.mappings = mappings;
    }

    public ChannelMappingOptionsDto providerName(@org.eclipse.jdt.annotation.Nullable String providerName) {
        this.providerName = providerName;
        return this;
    }

    /**
     * Gets or sets provider name.
     * 
     * @return providerName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PROVIDER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getProviderName() {
        return providerName;
    }

    @JsonProperty(value = JSON_PROPERTY_PROVIDER_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderName(@org.eclipse.jdt.annotation.Nullable String providerName) {
        this.providerName = providerName;
    }

    /**
     * Return true if this ChannelMappingOptionsDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChannelMappingOptionsDto channelMappingOptionsDto = (ChannelMappingOptionsDto) o;
        return Objects.equals(this.tunerChannels, channelMappingOptionsDto.tunerChannels)
                && Objects.equals(this.providerChannels, channelMappingOptionsDto.providerChannels)
                && Objects.equals(this.mappings, channelMappingOptionsDto.mappings)
                && Objects.equals(this.providerName, channelMappingOptionsDto.providerName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tunerChannels, providerChannels, mappings, providerName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ChannelMappingOptionsDto {\n");
        sb.append("    tunerChannels: ").append(toIndentedString(tunerChannels)).append("\n");
        sb.append("    providerChannels: ").append(toIndentedString(providerChannels)).append("\n");
        sb.append("    mappings: ").append(toIndentedString(mappings)).append("\n");
        sb.append("    providerName: ").append(toIndentedString(providerName)).append("\n");
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

        // add `TunerChannels` to the URL query string
        if (getTunerChannels() != null) {
            for (int i = 0; i < getTunerChannels().size(); i++) {
                if (getTunerChannels().get(i) != null) {
                    joiner.add(getTunerChannels().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sTunerChannels%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `ProviderChannels` to the URL query string
        if (getProviderChannels() != null) {
            for (int i = 0; i < getProviderChannels().size(); i++) {
                if (getProviderChannels().get(i) != null) {
                    joiner.add(getProviderChannels().get(i).toUrlQueryString(
                            String.format(java.util.Locale.ROOT, "%sProviderChannels%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `Mappings` to the URL query string
        if (getMappings() != null) {
            for (int i = 0; i < getMappings().size(); i++) {
                if (getMappings().get(i) != null) {
                    joiner.add(getMappings().get(i)
                            .toUrlQueryString(String.format(java.util.Locale.ROOT, "%sMappings%s%s", prefix, suffix,
                                    "".equals(suffix) ? ""
                                            : String.format(java.util.Locale.ROOT, "%s%d%s", containerPrefix, i,
                                                    containerSuffix))));
                }
            }
        }

        // add `ProviderName` to the URL query string
        if (getProviderName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sProviderName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getProviderName()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ChannelMappingOptionsDto instance;

        public Builder() {
            this(new ChannelMappingOptionsDto());
        }

        protected Builder(ChannelMappingOptionsDto instance) {
            this.instance = instance;
        }

        public ChannelMappingOptionsDto.Builder tunerChannels(List<TunerChannelMapping> tunerChannels) {
            this.instance.tunerChannels = tunerChannels;
            return this;
        }

        public ChannelMappingOptionsDto.Builder providerChannels(List<NameIdPair> providerChannels) {
            this.instance.providerChannels = providerChannels;
            return this;
        }

        public ChannelMappingOptionsDto.Builder mappings(List<NameValuePair> mappings) {
            this.instance.mappings = mappings;
            return this;
        }

        public ChannelMappingOptionsDto.Builder providerName(String providerName) {
            this.instance.providerName = providerName;
            return this;
        }

        /**
         * returns a built ChannelMappingOptionsDto instance.
         *
         * The builder is not reusable.
         */
        public ChannelMappingOptionsDto build() {
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
    public static ChannelMappingOptionsDto.Builder builder() {
        return new ChannelMappingOptionsDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ChannelMappingOptionsDto.Builder toBuilder() {
        return new ChannelMappingOptionsDto.Builder().tunerChannels(getTunerChannels())
                .providerChannels(getProviderChannels()).mappings(getMappings()).providerName(getProviderName());
    }
}
