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
 * Validate path object.
 */
@JsonPropertyOrder({ ValidatePathDto.JSON_PROPERTY_VALIDATE_WRITABLE, ValidatePathDto.JSON_PROPERTY_PATH,
        ValidatePathDto.JSON_PROPERTY_IS_FILE })

public class ValidatePathDto {
    public static final String JSON_PROPERTY_VALIDATE_WRITABLE = "ValidateWritable";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean validateWritable;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_IS_FILE = "IsFile";
    @org.eclipse.jdt.annotation.NonNull
    private Boolean isFile;

    public ValidatePathDto() {
    }

    public ValidatePathDto validateWritable(@org.eclipse.jdt.annotation.NonNull Boolean validateWritable) {
        this.validateWritable = validateWritable;
        return this;
    }

    /**
     * Gets or sets a value indicating whether validate if path is writable.
     * 
     * @return validateWritable
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_VALIDATE_WRITABLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getValidateWritable() {
        return validateWritable;
    }

    @JsonProperty(JSON_PROPERTY_VALIDATE_WRITABLE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setValidateWritable(@org.eclipse.jdt.annotation.NonNull Boolean validateWritable) {
        this.validateWritable = validateWritable;
    }

    public ValidatePathDto path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path.
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

    public ValidatePathDto isFile(@org.eclipse.jdt.annotation.NonNull Boolean isFile) {
        this.isFile = isFile;
        return this;
    }

    /**
     * Gets or sets is path file.
     * 
     * @return isFile
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(JSON_PROPERTY_IS_FILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)

    public Boolean getIsFile() {
        return isFile;
    }

    @JsonProperty(JSON_PROPERTY_IS_FILE)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setIsFile(@org.eclipse.jdt.annotation.NonNull Boolean isFile) {
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
}
