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

import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Client log document response dto.
 */
@JsonPropertyOrder({ ClientLogDocumentResponseDto.JSON_PROPERTY_FILE_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ClientLogDocumentResponseDto {
    public static final String JSON_PROPERTY_FILE_NAME = "FileName";
    @org.eclipse.jdt.annotation.NonNull
    private String fileName;

    public ClientLogDocumentResponseDto() {
    }

    public ClientLogDocumentResponseDto fileName(@org.eclipse.jdt.annotation.NonNull String fileName) {
        this.fileName = fileName;
        return this;
    }

    /**
     * Gets the resulting filename.
     * 
     * @return fileName
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_FILE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getFileName() {
        return fileName;
    }

    @JsonProperty(value = JSON_PROPERTY_FILE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setFileName(@org.eclipse.jdt.annotation.NonNull String fileName) {
        this.fileName = fileName;
    }

    /**
     * Return true if this ClientLogDocumentResponseDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ClientLogDocumentResponseDto clientLogDocumentResponseDto = (ClientLogDocumentResponseDto) o;
        return Objects.equals(this.fileName, clientLogDocumentResponseDto.fileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fileName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ClientLogDocumentResponseDto {\n");
        sb.append("    fileName: ").append(toIndentedString(fileName)).append("\n");
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

        // add `FileName` to the URL query string
        if (getFileName() != null) {
            joiner.add(String.format(Locale.ROOT, "%sFileName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getFileName()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ClientLogDocumentResponseDto instance;

        public Builder() {
            this(new ClientLogDocumentResponseDto());
        }

        protected Builder(ClientLogDocumentResponseDto instance) {
            this.instance = instance;
        }

        public ClientLogDocumentResponseDto.Builder fileName(String fileName) {
            this.instance.fileName = fileName;
            return this;
        }

        /**
         * returns a built ClientLogDocumentResponseDto instance.
         *
         * The builder is not reusable.
         */
        public ClientLogDocumentResponseDto build() {
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
    public static ClientLogDocumentResponseDto.Builder builder() {
        return new ClientLogDocumentResponseDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ClientLogDocumentResponseDto.Builder toBuilder() {
        return new ClientLogDocumentResponseDto.Builder().fileName(getFileName());
    }
}
