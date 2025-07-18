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
 * MediaPathInfo
 */
@JsonPropertyOrder({ MediaPathInfo.JSON_PROPERTY_PATH, MediaPathInfo.JSON_PROPERTY_NETWORK_PATH })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class MediaPathInfo {
    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_NETWORK_PATH = "NetworkPath";
    @org.eclipse.jdt.annotation.NonNull
    private String networkPath;

    public MediaPathInfo() {
    }

    public MediaPathInfo path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Get path
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

    public MediaPathInfo networkPath(@org.eclipse.jdt.annotation.NonNull String networkPath) {
        this.networkPath = networkPath;
        return this;
    }

    /**
     * Get networkPath
     * 
     * @return networkPath
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_NETWORK_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getNetworkPath() {
        return networkPath;
    }

    @JsonProperty(JSON_PROPERTY_NETWORK_PATH)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setNetworkPath(@org.eclipse.jdt.annotation.NonNull String networkPath) {
        this.networkPath = networkPath;
    }

    /**
     * Return true if this MediaPathInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MediaPathInfo mediaPathInfo = (MediaPathInfo) o;
        return Objects.equals(this.path, mediaPathInfo.path)
                && Objects.equals(this.networkPath, mediaPathInfo.networkPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path, networkPath);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class MediaPathInfo {\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    networkPath: ").append(toIndentedString(networkPath)).append("\n");
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

        // add `NetworkPath` to the URL query string
        if (getNetworkPath() != null) {
            joiner.add(String.format("%sNetworkPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getNetworkPath()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private MediaPathInfo instance;

        public Builder() {
            this(new MediaPathInfo());
        }

        protected Builder(MediaPathInfo instance) {
            this.instance = instance;
        }

        public MediaPathInfo.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public MediaPathInfo.Builder networkPath(String networkPath) {
            this.instance.networkPath = networkPath;
            return this;
        }

        /**
         * returns a built MediaPathInfo instance.
         *
         * The builder is not reusable.
         */
        public MediaPathInfo build() {
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
    public static MediaPathInfo.Builder builder() {
        return new MediaPathInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public MediaPathInfo.Builder toBuilder() {
        return new MediaPathInfo.Builder().path(getPath()).networkPath(getNetworkPath());
    }
}
