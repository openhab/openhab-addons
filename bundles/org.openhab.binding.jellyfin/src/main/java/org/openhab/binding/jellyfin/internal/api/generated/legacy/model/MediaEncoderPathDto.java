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

package org.openhab.binding.jellyfin.internal.api.generated.legacy.model;

import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Media Encoder Path Dto.
 */
@JsonPropertyOrder({ MediaEncoderPathDto.JSON_PROPERTY_PATH, MediaEncoderPathDto.JSON_PROPERTY_PATH_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaEncoderPathDto {
    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_PATH_TYPE = "PathType";
    @org.eclipse.jdt.annotation.NonNull
    private String pathType;

    public MediaEncoderPathDto() {
    }

    public MediaEncoderPathDto path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets media encoder path.
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

    public MediaEncoderPathDto pathType(@org.eclipse.jdt.annotation.NonNull String pathType) {
        this.pathType = pathType;
        return this;
    }

    /**
     * Gets or sets media encoder path type.
     * 
     * @return pathType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_PATH_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPathType() {
        return pathType;
    }

    @JsonProperty(JSON_PROPERTY_PATH_TYPE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPathType(@org.eclipse.jdt.annotation.NonNull String pathType) {
        this.pathType = pathType;
    }

    /**
     * Return true if this MediaEncoderPathDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaEncoderPathDto mediaEncoderPathDto = (MediaEncoderPathDto) o;
        return Objects.equals(this.path, mediaEncoderPathDto.path)
                && Objects.equals(this.pathType, mediaEncoderPathDto.pathType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, pathType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaEncoderPathDto {\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    pathType: ").append(toIndentedString(pathType)).append("\n");
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

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format("%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `PathType` to the URL query string
        if (getPathType() != null) {
            joiner.add(String.format("%sPathType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPathType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MediaEncoderPathDto instance;

        public Builder() {
            this(new MediaEncoderPathDto());
        }

        protected Builder(MediaEncoderPathDto instance) {
            this.instance = instance;
        }

        public MediaEncoderPathDto.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public MediaEncoderPathDto.Builder pathType(String pathType) {
            this.instance.pathType = pathType;
            return this;
        }

        /**
         * returns a built MediaEncoderPathDto instance.
         *
         * The builder is not reusable.
         */
        public MediaEncoderPathDto build() {
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
    public static MediaEncoderPathDto.Builder builder() {
        return new MediaEncoderPathDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MediaEncoderPathDto.Builder toBuilder() {
        return new MediaEncoderPathDto.Builder().path(getPath()).pathType(getPathType());
    }
}
