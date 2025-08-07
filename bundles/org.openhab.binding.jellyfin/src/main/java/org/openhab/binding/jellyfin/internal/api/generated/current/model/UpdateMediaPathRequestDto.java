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
 * Update library options dto.
 */
@JsonPropertyOrder({ UpdateMediaPathRequestDto.JSON_PROPERTY_NAME, UpdateMediaPathRequestDto.JSON_PROPERTY_PATH_INFO })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class UpdateMediaPathRequestDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_PATH_INFO = "PathInfo";
    @org.eclipse.jdt.annotation.Nullable
    private MediaPathInfo pathInfo;

    public UpdateMediaPathRequestDto() {
    }

    public UpdateMediaPathRequestDto name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the library name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public String getName() {
        return name;
    }

    @JsonProperty(JSON_PROPERTY_NAME)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setName(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
    }

    public UpdateMediaPathRequestDto pathInfo(@org.eclipse.jdt.annotation.Nullable MediaPathInfo pathInfo) {
        this.pathInfo = pathInfo;
        return this;
    }

    /**
     * Gets or sets library folder path information.
     * 
     * @return pathInfo
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(JSON_PROPERTY_PATH_INFO)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)

    public MediaPathInfo getPathInfo() {
        return pathInfo;
    }

    @JsonProperty(JSON_PROPERTY_PATH_INFO)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setPathInfo(@org.eclipse.jdt.annotation.Nullable MediaPathInfo pathInfo) {
        this.pathInfo = pathInfo;
    }

    /**
     * Return true if this UpdateMediaPathRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UpdateMediaPathRequestDto updateMediaPathRequestDto = (UpdateMediaPathRequestDto) o;
        return Objects.equals(this.name, updateMediaPathRequestDto.name)
                && Objects.equals(this.pathInfo, updateMediaPathRequestDto.pathInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, pathInfo);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class UpdateMediaPathRequestDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    pathInfo: ").append(toIndentedString(pathInfo)).append("\n");
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
