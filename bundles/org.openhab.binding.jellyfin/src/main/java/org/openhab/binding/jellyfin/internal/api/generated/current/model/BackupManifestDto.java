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

import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.StringJoiner;

import org.openhab.binding.jellyfin.internal.api.generated.ApiClient;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Manifest type for backups internal structure.
 */
@JsonPropertyOrder({ BackupManifestDto.JSON_PROPERTY_SERVER_VERSION,
        BackupManifestDto.JSON_PROPERTY_BACKUP_ENGINE_VERSION, BackupManifestDto.JSON_PROPERTY_DATE_CREATED,
        BackupManifestDto.JSON_PROPERTY_PATH, BackupManifestDto.JSON_PROPERTY_OPTIONS })
@jakarta.annotation.Generated(value = "org.openapitools.codegen.languages.JavaClientCodegen", comments = "OpenAPI Generator")
public class BackupManifestDto {
    public static final String JSON_PROPERTY_SERVER_VERSION = "ServerVersion";
    @org.eclipse.jdt.annotation.NonNull
    private String serverVersion;

    public static final String JSON_PROPERTY_BACKUP_ENGINE_VERSION = "BackupEngineVersion";
    @org.eclipse.jdt.annotation.NonNull
    private String backupEngineVersion;

    public static final String JSON_PROPERTY_DATE_CREATED = "DateCreated";
    @org.eclipse.jdt.annotation.NonNull
    private OffsetDateTime dateCreated;

    public static final String JSON_PROPERTY_PATH = "Path";
    @org.eclipse.jdt.annotation.NonNull
    private String path;

    public static final String JSON_PROPERTY_OPTIONS = "Options";
    @org.eclipse.jdt.annotation.NonNull
    private BackupOptionsDto options;

    public BackupManifestDto() {
    }

    public BackupManifestDto serverVersion(@org.eclipse.jdt.annotation.NonNull String serverVersion) {
        this.serverVersion = serverVersion;
        return this;
    }

    /**
     * Gets or sets the jellyfin version this backup was created with.
     * 
     * @return serverVersion
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_SERVER_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getServerVersion() {
        return serverVersion;
    }

    @JsonProperty(value = JSON_PROPERTY_SERVER_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setServerVersion(@org.eclipse.jdt.annotation.NonNull String serverVersion) {
        this.serverVersion = serverVersion;
    }

    public BackupManifestDto backupEngineVersion(@org.eclipse.jdt.annotation.NonNull String backupEngineVersion) {
        this.backupEngineVersion = backupEngineVersion;
        return this;
    }

    /**
     * Gets or sets the backup engine version this backup was created with.
     * 
     * @return backupEngineVersion
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_BACKUP_ENGINE_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public String getBackupEngineVersion() {
        return backupEngineVersion;
    }

    @JsonProperty(value = JSON_PROPERTY_BACKUP_ENGINE_VERSION, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setBackupEngineVersion(@org.eclipse.jdt.annotation.NonNull String backupEngineVersion) {
        this.backupEngineVersion = backupEngineVersion;
    }

    public BackupManifestDto dateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
        return this;
    }

    /**
     * Gets or sets the date this backup was created with.
     * 
     * @return dateCreated
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_DATE_CREATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public OffsetDateTime getDateCreated() {
        return dateCreated;
    }

    @JsonProperty(value = JSON_PROPERTY_DATE_CREATED, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setDateCreated(@org.eclipse.jdt.annotation.NonNull OffsetDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public BackupManifestDto path(@org.eclipse.jdt.annotation.NonNull String path) {
        this.path = path;
        return this;
    }

    /**
     * Gets or sets the path to the backup on the system.
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

    public BackupManifestDto options(@org.eclipse.jdt.annotation.NonNull BackupOptionsDto options) {
        this.options = options;
        return this;
    }

    /**
     * Gets or sets the contents of the backup archive.
     * 
     * @return options
     */
    @org.eclipse.jdt.annotation.NonNull
    @JsonProperty(value = JSON_PROPERTY_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public BackupOptionsDto getOptions() {
        return options;
    }

    @JsonProperty(value = JSON_PROPERTY_OPTIONS, required = false)
    @JsonInclude(value = JsonInclude.Include.USE_DEFAULTS)
    public void setOptions(@org.eclipse.jdt.annotation.NonNull BackupOptionsDto options) {
        this.options = options;
    }

    /**
     * Return true if this BackupManifestDto object is equal to o.
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BackupManifestDto backupManifestDto = (BackupManifestDto) o;
        return Objects.equals(this.serverVersion, backupManifestDto.serverVersion)
                && Objects.equals(this.backupEngineVersion, backupManifestDto.backupEngineVersion)
                && Objects.equals(this.dateCreated, backupManifestDto.dateCreated)
                && Objects.equals(this.path, backupManifestDto.path)
                && Objects.equals(this.options, backupManifestDto.options);
    }

    @Override
    public int hashCode() {
        return Objects.hash(serverVersion, backupEngineVersion, dateCreated, path, options);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("class BackupManifestDto {\n");
        sb.append("    serverVersion: ").append(toIndentedString(serverVersion)).append("\n");
        sb.append("    backupEngineVersion: ").append(toIndentedString(backupEngineVersion)).append("\n");
        sb.append("    dateCreated: ").append(toIndentedString(dateCreated)).append("\n");
        sb.append("    path: ").append(toIndentedString(path)).append("\n");
        sb.append("    options: ").append(toIndentedString(options)).append("\n");
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

        // add `ServerVersion` to the URL query string
        if (getServerVersion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sServerVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getServerVersion()))));
        }

        // add `BackupEngineVersion` to the URL query string
        if (getBackupEngineVersion() != null) {
            joiner.add(String.format(Locale.ROOT, "%sBackupEngineVersion%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getBackupEngineVersion()))));
        }

        // add `DateCreated` to the URL query string
        if (getDateCreated() != null) {
            joiner.add(String.format(Locale.ROOT, "%sDateCreated%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getDateCreated()))));
        }

        // add `Path` to the URL query string
        if (getPath() != null) {
            joiner.add(String.format(Locale.ROOT, "%sPath%s=%s", prefix, suffix,
                    ApiClient.urlEncode(ApiClient.valueToString(getPath()))));
        }

        // add `Options` to the URL query string
        if (getOptions() != null) {
            joiner.add(getOptions().toUrlQueryString(prefix + "Options" + suffix));
        }

        return joiner.toString();
    }

    public static class Builder {

        private BackupManifestDto instance;

        public Builder() {
            this(new BackupManifestDto());
        }

        protected Builder(BackupManifestDto instance) {
            this.instance = instance;
        }

        public BackupManifestDto.Builder serverVersion(String serverVersion) {
            this.instance.serverVersion = serverVersion;
            return this;
        }

        public BackupManifestDto.Builder backupEngineVersion(String backupEngineVersion) {
            this.instance.backupEngineVersion = backupEngineVersion;
            return this;
        }

        public BackupManifestDto.Builder dateCreated(OffsetDateTime dateCreated) {
            this.instance.dateCreated = dateCreated;
            return this;
        }

        public BackupManifestDto.Builder path(String path) {
            this.instance.path = path;
            return this;
        }

        public BackupManifestDto.Builder options(BackupOptionsDto options) {
            this.instance.options = options;
            return this;
        }

        /**
         * returns a built BackupManifestDto instance.
         *
         * The builder is not reusable.
         */
        public BackupManifestDto build() {
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
    public static BackupManifestDto.Builder builder() {
        return new BackupManifestDto.Builder();
    }

    /**
     * Create a builder with a shallow copy of this instance.
     */
    public BackupManifestDto.Builder toBuilder() {
        return new BackupManifestDto.Builder().serverVersion(getServerVersion())
                .backupEngineVersion(getBackupEngineVersion()).dateCreated(getDateCreated()).path(getPath())
                .options(getOptions());
    }
}
