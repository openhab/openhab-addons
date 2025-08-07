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
 * Represents the external id information for serialization to the client.
 */
@JsonPropertyOrder({ ExternalIdInfo.JSON_PROPERTY_NAME, ExternalIdInfo.JSON_PROPERTY_KEY,
        ExternalIdInfo.JSON_PROPERTY_TYPE, ExternalIdInfo.JSON_PROPERTY_URL_FORMAT_STRING })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ExternalIdInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_KEY = "Key";
    @org.eclipse.jdt.annotation.NonNull
    private String key;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private ExternalIdMediaType type;

    public static final String JSON_PROPERTY_URL_FORMAT_STRING = "UrlFormatString";
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    private String urlFormatString;

    public ExternalIdInfo() {
    }

    public ExternalIdInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the display name of the external id provider (IE: IMDB, MusicBrainz, etc).
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

    public ExternalIdInfo key(@org.eclipse.jdt.annotation.NonNull String key) {
        this.key = key;
        return this;
    }

    /**
     * Gets or sets the unique key for this id. This key should be unique across all providers.
     * 
     * @return key
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_KEY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getKey() {
        return key;
    }

    @JsonProperty(JSON_PROPERTY_KEY)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setKey(@org.eclipse.jdt.annotation.NonNull String key) {
        this.key = key;
    }

    public ExternalIdInfo type(@org.eclipse.jdt.annotation.NonNull ExternalIdMediaType type) {
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
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public ExternalIdMediaType getType() {
        return type;
    }

    @JsonProperty(JSON_PROPERTY_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull ExternalIdMediaType type) {
        this.type = type;
    }

    @Deprecated
    public ExternalIdInfo urlFormatString(@org.eclipse.jdt.annotation.NonNull String urlFormatString) {
        this.urlFormatString = urlFormatString;
        return this;
    }

    /**
     * Gets or sets the URL format string.
     * 
     * @return urlFormatString
     * @deprecated
     */
    @Deprecated
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_URL_FORMAT_STRING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getUrlFormatString() {
        return urlFormatString;
    }

    @Deprecated
    @JsonProperty(JSON_PROPERTY_URL_FORMAT_STRING)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUrlFormatString(@org.eclipse.jdt.annotation.NonNull String urlFormatString) {
        this.urlFormatString = urlFormatString;
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
                && Objects.equals(this.type, externalIdInfo.type)
                && Objects.equals(this.urlFormatString, externalIdInfo.urlFormatString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, key, type, urlFormatString);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ExternalIdInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    key: ").append(toIndentedString(key)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
        sb.append("    urlFormatString: ").append(toIndentedString(urlFormatString)).append("\n");
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
