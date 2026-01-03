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

import org.openhab.binding.jellyfin.internal.thirdparty.api.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Media Path dto.
 */
@JsonPropertyOrder({ MediaPathDto.JSON_PROPERTY_NAME, MediaPathDto.JSON_PROPERTY_PATH,
        MediaPathDto.JSON_PROPERTY_PATH_INFO })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaPathDto {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.Nullable
    private String path;

    public static final String JSON_PROPERTY_PATH_INFO = "PathInfo";
    @org.eclipse.jdt.annotation.Nullable
    private MediaPathInfo pathInfo;

    public MediaPathDto() {
    }

    public MediaPathDto name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets or sets the name of the library.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = true)
    @JsonInclude(value = JsonInclude.Include.ALWAYS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public MediaPathDto path(@org.eclipse.jdt.annotation.Nullable String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path to add.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPath() {
        return path;
    }

    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.Nullable String path) {
        this.path = path;
    }

    public MediaPathDto pathInfo(@org.eclipse.jdt.annotation.Nullable MediaPathInfo pathInfo) {
        this.pathInfo = pathInfo;
        return this;
    }

    /**
     * Gets or sets the path info.
     * 
     * @return pathInfo
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_PATH_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public MediaPathInfo getPathInfo() {
        return pathInfo;
    }

    @JsonProperty(value = JSON_PROPERTY_PATH_INFO, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPathInfo(@org.eclipse.jdt.annotation.Nullable MediaPathInfo pathInfo) {
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

        // add `Name` to the URL query string
        if (getName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `PathInfo` to the URL query string
        if (getPathInfo() != null) {
            joiner.add(getPathInfo().toUrlQueryString(prefix + "PathInfo" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MediaPathDto instance;

        public Builder() {
            this(new MediaPathDto());
        }

        protected Builder(MediaPathDto instance) {
            this.instance = instance;
        }

        public MediaPathDto.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public MediaPathDto.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public MediaPathDto.Builder pathInfo(MediaPathInfo pathInfo) {
            this.instance.pathInfo = pathInfo;
            return this;
        }

        /**
         * returns a built MediaPathDto instance.
         *
         * The builder is not reusable.
         */
        public MediaPathDto build() {
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
    public static MediaPathDto.Builder builder() {
        return new MediaPathDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MediaPathDto.Builder toBuilder() {
        return new MediaPathDto.Builder().name(getName()).path(getPath()).pathInfo(getPathInfo());
    }
}
