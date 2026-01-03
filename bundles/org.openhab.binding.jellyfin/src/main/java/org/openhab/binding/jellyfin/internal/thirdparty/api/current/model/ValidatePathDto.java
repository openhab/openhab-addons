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
 * Validate path object.
 */
@JsonPropertyOrder({ ValidatePathDto.JSON_PROPERTY_VALIDATE_WRITABLE, ValidatePathDto.JSON_PROPERTY_PATH,
        ValidatePathDto.JSON_PROPERTY_IS_FILE })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class ValidatePathDto {
    public static final String JSON_PROPERTY_VALIDATE_WRITABLE = "ValidateWritable";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean validateWritable;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.Nullable
    private String path;

    public static final String JSON_PROPERTY_IS_FILE = "IsFile";
    @org.eclipse.jdt.annotation.Nullable
    private Boolean isFile;

    public ValidatePathDto() {
    }

    public ValidatePathDto validateWritable(@org.eclipse.jdt.annotation.Nullable Boolean validateWritable) {
        this.validateWritable = validateWritable;
        return this;
    }

    /**
     * Gets or sets a value indicating whether validate if path is writable.
     * 
     * @return validateWritable
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_VALIDATE_WRITABLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getValidateWritable() {
        return validateWritable;
    }

    @JsonProperty(value = JSON_PROPERTY_VALIDATE_WRITABLE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValidateWritable(@org.eclipse.jdt.annotation.Nullable Boolean validateWritable) {
        this.validateWritable = validateWritable;
    }

    public ValidatePathDto path(@org.eclipse.jdt.annotation.Nullable String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path.
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

    public ValidatePathDto isFile(@org.eclipse.jdt.annotation.Nullable Boolean isFile) {
        this.isFile = isFile;
        return this;
    }

    /**
     * Gets or sets is path file.
     * 
     * @return isFile
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_IS_FILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public Boolean getIsFile() {
        return isFile;
    }

    @JsonProperty(value = JSON_PROPERTY_IS_FILE, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsFile(@org.eclipse.jdt.annotation.Nullable Boolean isFile) {
        this.isFile = isFile;
    }

    /**
     * Return true if this ValidatePathDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ValidatePathDto validatePathDto = (ValidatePathDto) o;
        return Objects.equals(this.validateWritable, validatePathDto.validateWritable)
                && Objects.equals(this.path, validatePathDto.path)
                && Objects.equals(this.isFile, validatePathDto.isFile);
    }

    @Override
    public int hashCode() {
        return Objects.hash(validateWritable, path, isFile);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class ValidatePathDto {\n");
        sb.append("    validateWritable: ").append(toIndentedString(validateWritable)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    isFile: ").append(toIndentedString(isFile)).append("\n");
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

        // add `ValidateWritable` to the URL query string
        if (getValidateWritable() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sValidateWritable%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getValidateWritable()))));
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `IsFile` to the URL query string
        if (getIsFile() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sIsFile%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getIsFile()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private ValidatePathDto instance;

        public Builder() {
            this(new ValidatePathDto());
        }

        protected Builder(ValidatePathDto instance) {
            this.instance = instance;
        }

        public ValidatePathDto.Builder validateWritable(Boolean validateWritable) {
            this.instance.validateWritable = validateWritable;
            return this;
        }

        public ValidatePathDto.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public ValidatePathDto.Builder isFile(Boolean isFile) {
            this.instance.isFile = isFile;
            return this;
        }

        /**
         * returns a built ValidatePathDto instance.
         *
         * The builder is not reusable.
         */
        public ValidatePathDto build() {
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
    public static ValidatePathDto.Builder builder() {
        return new ValidatePathDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public ValidatePathDto.Builder toBuilder() {
        return new ValidatePathDto.Builder().validateWritable(getValidateWritable()).path(getPath())
                .isFile(getIsFile());
    }
}
