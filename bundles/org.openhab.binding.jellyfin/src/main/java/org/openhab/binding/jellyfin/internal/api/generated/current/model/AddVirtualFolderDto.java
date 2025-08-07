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
    @JsonProperty(JSON_PROPERTY_LIBRARY_OPTIONS)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public LibraryOptions getLibraryOptions() {
        return libraryOptions;
    }

    @JsonProperty(JSON_PROPERTY_LIBRARY_OPTIONS)
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
}
