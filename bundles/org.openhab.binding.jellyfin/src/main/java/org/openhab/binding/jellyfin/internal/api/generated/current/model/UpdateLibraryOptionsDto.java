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
import java.util.UUID;

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
    @org.eclipse.jdt.annotation.NonNull
    private UUID id;

    public static final String JSON_PROPERTY_LIBRARY_OPTIONS = "LibraryOptions";
    @org.eclipse.jdt.annotation.NonNull
    private LibraryOptions libraryOptions;

    public UpdateLibraryOptionsDto() {
    }

    public UpdateLibraryOptionsDto id(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
        return this;
    }

    /**
     * Gets or sets the library item id.
     * 
     * @return id
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public UUID getId() {
        return id;
    }

    @JsonProperty(JSON_PROPERTY_ID)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setId(@org.eclipse.jdt.annotation.NonNull UUID id) {
        this.id = id;
    }

    public UpdateLibraryOptionsDto libraryOptions(@org.eclipse.jdt.annotation.NonNull LibraryOptions libraryOptions) {
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
}
