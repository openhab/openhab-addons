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
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Add virtual folder dto.
 */
@JsonPropertyOrder({ AddVirtualFolderDto.JSON_PROPERTY_LIBRARY_OPTIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class AddVirtualFolderDto {
    public static final String JSON_PROPERTY_LIBRARY_OPTIONS = "LibraryOptions";
    @org.eclipse.jdt.annotation.NonNull
    private LibraryOptions libraryOptions;

    public AddVirtualFolderDto() {
    }

    public AddVirtualFolderDto libraryOptions(@org.eclipse.jdt.annotation.NonNull LibraryOptions libraryOptions) {
        this.libraryOptions = libraryOptions;
        return this;
    }

    /**
     * Gets or sets library options.
     * 
     * @return libraryOptions
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_LIBRARY_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public LibraryOptions getLibraryOptions() {
        return libraryOptions;
    }

    @JsonProperty(value = JSON_PROPERTY_LIBRARY_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setLibraryOptions(@org.eclipse.jdt.annotation.NonNull LibraryOptions libraryOptions) {
        this.libraryOptions = libraryOptions;
    }

    /**
     * Return true if this AddVirtualFolderDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AddVirtualFolderDto addVirtualFolderDto = (AddVirtualFolderDto) o;
        return Objects.equals(this.libraryOptions, addVirtualFolderDto.libraryOptions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(libraryOptions);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class AddVirtualFolderDto {\n");
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

        // add `LibraryOptions` to the URL query string
        if (getLibraryOptions() != null) {
            joiner.add(getLibraryOptions().toUrlQueryString(prefix + "LibraryOptions" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private AddVirtualFolderDto instance;

        public Builder() {
            this(new AddVirtualFolderDto());
        }

        protected Builder(AddVirtualFolderDto instance) {
            this.instance = instance;
        }

        public AddVirtualFolderDto.Builder libraryOptions(LibraryOptions libraryOptions) {
            this.instance.libraryOptions = libraryOptions;
            return this;
        }

        /**
         * returns a built AddVirtualFolderDto instance.
         *
         * The builder is not reusable.
         */
        public AddVirtualFolderDto build() {
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
    public static AddVirtualFolderDto.Builder builder() {
        return new AddVirtualFolderDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public AddVirtualFolderDto.Builder toBuilder() {
        return new AddVirtualFolderDto.Builder().libraryOptions(getLibraryOptions());
    }
}
