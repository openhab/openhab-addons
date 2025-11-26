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
 * The media update info path.
 */
@JsonPropertyOrder({ MediaUpdateInfoPathDto.JSON_PROPERTY_PATH, MediaUpdateInfoPathDto.JSON_PROPERTY_UPDATE_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaUpdateInfoPathDto {
    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_UPDATE_TYPE = "UpdateType";
    @org.eclipse.jdt.annotation.NonNull
    private String updateType;

    public MediaUpdateInfoPathDto() {
    }

    public MediaUpdateInfoPathDto path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets media path.
     * 
     * @return path
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getPath() {
        return path;
    }

    @JsonProperty(value = JSON_PROPERTY_PATH, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setPath(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
    }

    public MediaUpdateInfoPathDto updateType(@org.eclipse.jdt.annotation.NonNull String updateType) {
        this.updateType = updateType;
        return this;
    }

    /**
     * Gets or sets media update type. Created, Modified, Deleted.
     * 
     * @return updateType
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_UPDATE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getUpdateType() {
        return updateType;
    }

    @JsonProperty(value = JSON_PROPERTY_UPDATE_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setUpdateType(@org.eclipse.jdt.annotation.NonNull String updateType) {
        this.updateType = updateType;
    }

    /**
     * Return true if this MediaUpdateInfoPathDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaUpdateInfoPathDto mediaUpdateInfoPathDto = (MediaUpdateInfoPathDto) o;
        return Objects.equals(this.path, mediaUpdateInfoPathDto.path)
                && Objects.equals(this.updateType, mediaUpdateInfoPathDto.updateType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, updateType);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaUpdateInfoPathDto {\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    updateType: ").append(toIndentedString(updateType)).append("\n");
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
            joiner.add(String.format(Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `UpdateType` to the URL query string
        if (getUpdateType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sUpdateType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getUpdateType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MediaUpdateInfoPathDto instance;

        public Builder() {
            this(new MediaUpdateInfoPathDto());
        }

        protected Builder(MediaUpdateInfoPathDto instance) {
            this.instance = instance;
        }

        public MediaUpdateInfoPathDto.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public MediaUpdateInfoPathDto.Builder updateType(String updateType) {
            this.instance.updateType = updateType;
            return this;
        }

        /**
         * returns a built MediaUpdateInfoPathDto instance.
         *
         * The builder is not reusable.
         */
        public MediaUpdateInfoPathDto build() {
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
    public static MediaUpdateInfoPathDto.Builder builder() {
        return new MediaUpdateInfoPathDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MediaUpdateInfoPathDto.Builder toBuilder() {
        return new MediaUpdateInfoPathDto.Builder().path(getPath()).updateType(getUpdateType());
    }
}
