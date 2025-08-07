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
 * Library option info dto.
 */
@JsonPropertyOrder({ LibraryOptionInfoDto.JSON_PROPERTY_NAME, LibraryOptionInfoDto.JSON_PROPERTY_DEFAULT_ENABLED })

public class LibraryOptionInfoDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_DEFAULT_ENABLED = "DefaultEnabled";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean defaultEnabled;

    public LibraryOptionInfoDto() {
    }

    public LibraryOptionInfoDto name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets name.
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

    public LibraryOptionInfoDto defaultEnabled(@org.eclipse.jdt.annotation.NonNull Boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
        return this;
    }

    /**
     * Gets or sets a value indicating whether default enabled.
     * 
     * @return defaultEnabled
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_DEFAULT_ENABLED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getDefaultEnabled() {
        return defaultEnabled;
    }

    @JsonProperty(JSON_PROPERTY_DEFAULT_ENABLED)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDefaultEnabled(@org.eclipse.jdt.annotation.NonNull Boolean defaultEnabled) {
        this.defaultEnabled = defaultEnabled;
    }

    /**
     * Return true if this LibraryOptionInfoDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        LibraryOptionInfoDto libraryOptionInfoDto = (LibraryOptionInfoDto) o;
        return Objects.equals(this.name, libraryOptionInfoDto.name)
                && Objects.equals(this.defaultEnabled, libraryOptionInfoDto.defaultEnabled);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, defaultEnabled);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class LibraryOptionInfoDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    defaultEnabled: ").append(toIndentedString(defaultEnabled)).append("\n");
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
