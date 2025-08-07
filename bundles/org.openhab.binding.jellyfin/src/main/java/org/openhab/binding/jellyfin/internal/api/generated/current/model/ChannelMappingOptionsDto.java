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

package org.openhab.binding.jellyfin.internal.api.generated.current.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Channel mapping options dto.
 */
@JsonPropertyOrder({ ChannelMappingOptionsDto.JSON_PROPERTY_TUNER_CHANNELS,
        ChannelMappingOptionsDto.JSON_PROPERTY_PROVIDER_CHANNELS, ChannelMappingOptionsDto.JSON_PROPERTY_MAPPINGS,
        ChannelMappingOptionsDto.JSON_PROPERTY_PROVIDER_NAME })

public class ChannelMappingOptionsDto {
    public static final String JSON_PROPERTY_TUNER_CHANNELS = "TunerChannels";
    @org.eclipse.jdt.annotation.NonNull
    private List<TunerChannelMapping> tunerChannels = new ArrayList<>();

    public static final String JSON_PROPERTY_PROVIDER_CHANNELS = "ProviderChannels";
    @org.eclipse.jdt.annotation.NonNull
    private List<NameIdPair> providerChannels = new ArrayList<>();

    public static final String JSON_PROPERTY_MAPPINGS = "Mappings";
    @org.eclipse.jdt.annotation.NonNull
    private List<NameValuePair> mappings = new ArrayList<>();

    public static final String JSON_PROPERTY_PROVIDER_NAME = "ProviderName";
    @org.eclipse.jdt.annotation.NonNull
    private String providerName;

    public ChannelMappingOptionsDto() {
    }

    public ChannelMappingOptionsDto tunerChannels(
            @org.eclipse.jdt.annotation.NonNull List<TunerChannelMapping> tunerChannels) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TUNER_CHANNELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<TunerChannelMapping> getTunerChannels() {
        return tunerChannels;
    }

    @JsonProperty(JSON_PROPERTY_TUNER_CHANNELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setTunerChannels(@org.eclipse.jdt.annotation.NonNull List<TunerChannelMapping> tunerChannels) {
        this.tunerChannels = tunerChannels;
    }

    public ChannelMappingOptionsDto providerChannels(
            @org.eclipse.jdt.annotation.NonNull List<NameIdPair> providerChannels) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROVIDER_CHANNELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<NameIdPair> getProviderChannels() {
        return providerChannels;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDER_CHANNELS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderChannels(@org.eclipse.jdt.annotation.NonNull List<NameIdPair> providerChannels) {
        this.providerChannels = providerChannels;
    }

    public ChannelMappingOptionsDto mappings(@org.eclipse.jdt.annotation.NonNull List<NameValuePair> mappings) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_MAPPINGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public List<NameValuePair> getMappings() {
        return mappings;
    }

    @JsonProperty(JSON_PROPERTY_MAPPINGS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setMappings(@org.eclipse.jdt.annotation.NonNull List<NameValuePair> mappings) {
        this.mappings = mappings;
    }

    public ChannelMappingOptionsDto providerName(@org.eclipse.jdt.annotation.NonNull String providerName) {
        this.providerName = providerName;
        return this;
    }

    /**
     * Gets or sets provider name.
     * 
     * @return providerName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROVIDER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProviderName() {
        return providerName;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDER_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderName(@org.eclipse.jdt.annotation.NonNull String providerName) {
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
}
