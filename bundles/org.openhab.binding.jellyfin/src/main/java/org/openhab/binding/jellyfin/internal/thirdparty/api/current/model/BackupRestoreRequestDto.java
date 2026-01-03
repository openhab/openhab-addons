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
 * Defines properties used to start a restore process.
 */
@JsonPropertyOrder({ BackupRestoreRequestDto.JSON_PROPERTY_ARCHIVE_FILE_NAME })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BackupRestoreRequestDto {
    public static final String JSON_PROPERTY_ARCHIVE_FILE_NAME = "ArchiveFileName";
    @org.eclipse.jdt.annotation.Nullable
    private String archiveFileName;

    public BackupRestoreRequestDto() {
    }

    public BackupRestoreRequestDto archiveFileName(@org.eclipse.jdt.annotation.Nullable String archiveFileName) {
        this.archiveFileName = archiveFileName;
        return this;
    }

    /**
     * Gets or Sets the name of the backup archive to restore from. Must be present in
     * MediaBrowser.Common.Configuration.IApplicationPaths.BackupPath.
     * 
     * @return archiveFileName
     */
    @org.eclipse.jdt.annotation.Nullable
    @JsonProperty(value = JSON_PROPERTY_ARCHIVE_FILE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getArchiveFileName() {
        return archiveFileName;
    }

    @JsonProperty(value = JSON_PROPERTY_ARCHIVE_FILE_NAME, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setArchiveFileName(@org.eclipse.jdt.annotation.Nullable String archiveFileName) {
        this.archiveFileName = archiveFileName;
    }

    /**
     * Return true if this BackupRestoreRequestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BackupRestoreRequestDto backupRestoreRequestDto = (BackupRestoreRequestDto) o;
        return Objects.equals(this.archiveFileName, backupRestoreRequestDto.archiveFileName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(archiveFileName);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BackupRestoreRequestDto {\n");
        sb.append("    archiveFileName: ").append(toIndentedString(archiveFileName)).append("\n");
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

        // add `ArchiveFileName` to the URL query string
        if (getArchiveFileName() != null) {
            joiner.add(String.format(java.util.Locale.ROOT, "%sArchiveFileName%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getArchiveFileName()))));
        }

        return joiner.toString();
    }

    public static class Builder {

        private BackupRestoreRequestDto instance;

        public Builder() {
            this(new BackupRestoreRequestDto());
        }

        protected Builder(BackupRestoreRequestDto instance) {
            this.instance = instance;
        }

        public BackupRestoreRequestDto.Builder archiveFileName(String archiveFileName) {
            this.instance.archiveFileName = archiveFileName;
            return this;
        }

        /**
         * returns a built BackupRestoreRequestDto instance.
         *
         * The builder is not reusable.
         */
        public BackupRestoreRequestDto build() {
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
    public static BackupRestoreRequestDto.Builder builder() {
        return new BackupRestoreRequestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BackupRestoreRequestDto.Builder toBuilder() {
        return new BackupRestoreRequestDto.Builder().archiveFileName(getArchiveFileName());
    }
}
