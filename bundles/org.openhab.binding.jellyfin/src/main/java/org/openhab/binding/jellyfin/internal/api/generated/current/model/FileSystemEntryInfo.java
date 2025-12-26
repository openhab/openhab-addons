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
 * Class FileSystemEntryInfo.
 */
@JsonPropertyOrder({ FileSystemEntryInfo.JSON_PROPERTY_NAME, FileSystemEntryInfo.JSON_PROPERTY_PATH,
        FileSystemEntryInfo.JSON_PROPERTY_TYPE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class FileSystemEntryInfo {
    public static final String JSON_PROPERTY_NAME = "Name";
    @org.eclipse.jdt.annotation.NonNull
    private String name;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_TYPE = "Type";
    @org.eclipse.jdt.annotation.NonNull
    private FileSystemEntryType type;

    public FileSystemEntryInfo() {
    }

    public FileSystemEntryInfo name(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return name
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getName() {
        return name;
    }

    @JsonProperty(value = JSON_PROPERTY_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setName(@org.eclipse.jdt.annotation.NonNull String name) {
        this.name = name;
    }

    public FileSystemEntryInfo path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets the path.
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

    public FileSystemEntryInfo type(@org.eclipse.jdt.annotation.NonNull FileSystemEntryType type) {
        this.type = type;
        return this;
    }

    /**
     * Gets the type.
     * 
     * @return type
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public FileSystemEntryType getType() {
        return type;
    }

    @JsonProperty(value = JSON_PROPERTY_TYPE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setType(@org.eclipse.jdt.annotation.NonNull FileSystemEntryType type) {
        this.type = type;
    }

    /**
     * Return true if this FileSystemEntryInfo object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        FileSystemEntryInfo fileSystemEntryInfo = (FileSystemEntryInfo) o;
        return Objects.equals(this.name, fileSystemEntryInfo.name)
                && Objects.equals(this.path, fileSystemEntryInfo.path)
                && Objects.equals(this.type, fileSystemEntryInfo.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, path, type);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class FileSystemEntryInfo {\n");
        sb.append("    name: ").append(toIndentedString(name)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    type: ").append(toIndentedString(type)).append("\n");
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
            joiner.add(String.format(Locale.ROOT, "%sName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getName()))));
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `Type` to the URL query string
        if (getType() != null) {
            joiner.add(String.format(Locale.ROOT, "%sType%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getType()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private FileSystemEntryInfo instance;

        public Builder() {
            this(new FileSystemEntryInfo());
        }

        protected Builder(FileSystemEntryInfo instance) {
            this.instance = instance;
        }

        public FileSystemEntryInfo.Builder name(String name) {
            this.instance.name = name;
            return this;
        }

        public FileSystemEntryInfo.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public FileSystemEntryInfo.Builder type(FileSystemEntryType type) {
            this.instance.type = type;
            return this;
        }

        /**
         * returns a built FileSystemEntryInfo instance.
         *
         * The builder is not reusable.
         */
        public FileSystemEntryInfo build() {
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
    public static FileSystemEntryInfo.Builder builder() {
        return new FileSystemEntryInfo.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public FileSystemEntryInfo.Builder toBuilder() {
        return new FileSystemEntryInfo.Builder().name(getName()).path(getPath()).type(getType());
    }
}
