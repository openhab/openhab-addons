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

import java.util.Objects;

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
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_PROVIDER_CHANNEL_NAME = "ProviderChannelName";
    @org.eclipse.jdt.annotation.NonNull
    private String providerChannelName;

    public static final String JSON_PROPERTY_PROVIDER_CHANNEL_ID = "ProviderChannelId";
    @org.eclipse.jdt.annotation.NonNull
    private String providerChannelId;

    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.NonNull
    private String id;

    public TunerChannelMapping() {
    }

    public TunerChannelMapping name(@org.eclipse.jdt.annotation.NonNull String name) {
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

    public TunerChannelMapping providerChannelName(@org.eclipse.jdt.annotation.NonNull String providerChannelName) {
        this.providerChannelName = providerChannelName;
        return this;
    }

    /**
     * Get providerChannelName
     * 
     * @return providerChannelName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROVIDER_CHANNEL_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProviderChannelName() {
        return providerChannelName;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDER_CHANNEL_NAME)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderChannelName(@org.eclipse.jdt.annotation.NonNull String providerChannelName) {
        this.providerChannelName = providerChannelName;
    }

    public TunerChannelMapping providerChannelId(@org.eclipse.jdt.annotation.NonNull String providerChannelId) {
        this.providerChannelId = providerChannelId;
        return this;
    }

    /**
     * Get providerChannelId
     * 
     * @return providerChannelId
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PROVIDER_CHANNEL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getProviderChannelId() {
        return providerChannelId;
    }

    @JsonProperty(JSON_PROPERTY_PROVIDER_CHANNEL_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setProviderChannelId(@org.eclipse.jdt.annotation.NonNull String providerChannelId) {
        this.providerChannelId = providerChannelId;
    }

    public TunerChannelMapping id(@org.eclipse.jdt.annotation.NonNull String id) {
        this.id = id;
        return this;
    }

    /**
     * Get id
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull String id) {
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
}
