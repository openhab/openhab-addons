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
 * Media Path dto.
 */
@JsonPropertyOrder({ MediaPathDto.JSON_PROPERTY_NAME, MediaPathDto.JSON_PROPERTY_PATH,
        MediaPathDto.JSON_PROPERTY_PATH_INFO })

public class MediaPathDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.Nullable
    private String name;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_PATH_INFO = "PathInfo";
    @org.eclipse.jdt.annotation.NonNull
    private MediaPathInfo pathInfo;

    public MediaPathDto() {
    }

    public MediaPathDto name(@org.eclipse.jdt.annotation.Nullable String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name of the library.
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

    public MediaPathDto path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path to add.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public String getPath() {
        return path;
    }

    @JsonProperty(JSON_PROPERTY_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public MediaPathDto pathInfo(@org.eclipse.jdt.annotation.NonNull MediaPathInfo pathInfo) {
        this.pathInfo = pathInfo;
        return this;
    }

    /**
     * Gets or sets the path info.
     * 
     * @return pathInfo
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PATH_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public MediaPathInfo getPathInfo() {
        return pathInfo;
    }

    @JsonProperty(JSON_PROPERTY_PATH_INFO)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPathInfo(@org.eclipse.jdt.annotation.NonNull MediaPathInfo pathInfo) {
        this.pathInfo = pathInfo;
    }

    /**
     * Return true if this MediaPathDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaPathDto mediaPathDto = (MediaPathDto) o;
        return Objects.equals(this.name, mediaPathDto.name) && Objects.equals(this.path, mediaPathDto.path)
                && Objects.equals(this.pathInfo, mediaPathDto.pathInfo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, pathInfo);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaPathDto {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
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
