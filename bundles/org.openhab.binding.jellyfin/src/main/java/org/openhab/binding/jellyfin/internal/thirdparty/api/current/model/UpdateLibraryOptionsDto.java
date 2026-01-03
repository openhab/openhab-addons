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
import java.util.UUID;

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Update library options dto.
 */
@JsonPropertyOrder({ UpdateLibraryOptionsDto.JSON_PROPERTY_ID, UpdateLibraryOptionsDto.JSON_PROPERTY_LIBRARY_OPTIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UpdateLibraryOptionsDto {
    public static final String JSON_PROPERTY_ID = "Id";
    @org.eclipse.jdt.annotation.Nullable
    private UUID id;

    public static final String JSON_PROPERTY_LIBRARY_OPTIONS = "LibraryOptions";
    @org.eclipse.jdt.annotation.Nullable
    private LibraryOptions libraryOptions;

    public UpdateLibraryOptionsDto() {
    }

    public UpdateLibraryOptionsDto id(@org.eclipse.jdt.annotation.Nullable UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the library item id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public UUID getId() {
        return id;
    }

    @JsonProperty(value = JSON_PROPERTY_ID, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.Nullable UUID id) {
        this.id = id;
    }

    public UpdateLibraryOptionsDto libraryOptions(@org.eclipse.jdt.annotation.Nullable LibraryOptions libraryOptions) {
        this.libraryOptions = libraryOptions;
        return this;
    }

    /**
     * Gets or sets library options.
     * 
     * @return libraryOptions
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_LIBRARY_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public LibraryOptions getLibraryOptions() {
        return libraryOptions;
    }

    @JsonProperty(value = JSON_PROPERTY_LIBRARY_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLibraryOptions(@org.eclipse.jdt.annotation.Nullable LibraryOptions libraryOptions) {
        this.libraryOptions = libraryOptions;
    }

    /**
     * Return true if this UpdateLibraryOptionsDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateLibraryOptionsDto updateLibraryOptionsDto = (UpdateLibraryOptionsDto) o;
        return Objects.equals(this.id, updateLibraryOptionsDto.id)
                && Objects.equals(this.libraryOptions, updateLibraryOptionsDto.libraryOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, libraryOptions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdateLibraryOptionsDto {\n");
        sb.append("    id: ").append(toIndentedString(id)).append("\n");
        sb.append("    libraryOptions: ").append(toIndentedString(libraryOptions)).append("\n");
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

        // add `Id` to the URL query string
        if (getId() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sId%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getId()))));
        }

        // add `LibraryOptions` to the URL query string
        if (getLibraryOptions() != null) {
            joiner.add(getLibraryOptions().toUrlQueryString(prefix + "LibraryOptions" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private UpdateLibraryOptionsDto instance;

        public Builder() {
            this(new UpdateLibraryOptionsDto());
        }

        protected Builder(UpdateLibraryOptionsDto instance) {
            this.instance = instance;
        }

        public UpdateLibraryOptionsDto.Builder id(UUID id) {
            this.instance.id = id;
            return this;
        }

        public UpdateLibraryOptionsDto.Builder libraryOptions(LibraryOptions libraryOptions) {
            this.instance.libraryOptions = libraryOptions;
            return this;
        }

        /**
         * returns a built UpdateLibraryOptionsDto instance.
         *
         * The builder is not reusable.
         */
        public UpdateLibraryOptionsDto build() {
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
    public static UpdateLibraryOptionsDto.Builder builder() {
        return new UpdateLibraryOptionsDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public UpdateLibraryOptionsDto.Builder toBuilder() {
        return new UpdateLibraryOptionsDto.Builder().id(getId()).libraryOptions(getLibraryOptions());
    }
}
